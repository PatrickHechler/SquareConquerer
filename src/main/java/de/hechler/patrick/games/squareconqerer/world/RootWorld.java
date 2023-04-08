package de.hechler.patrick.games.squareconqerer.world;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.enums.Direction;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.placer.DefaultUserPlacer;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.stuff.UserPlacer;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;
import de.hechler.patrick.games.squareconqerer.world.turn.CarryTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.EntityTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.MoveTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.StoreTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public final class RootWorld implements World {
	
	private final RootUser             root;
	private final Tile[][]             tiles;
	private final UserPlacer           placer;
	private final Map<User, UserWorld> subWorlds          = new HashMap<>();
	private final List<Runnable>       nextTurnListeneres = new LinkedList<>();
	private final Map<User, Turn>      userTurns          = new LinkedHashMap<>();
	private volatile boolean           started            = false;
	
	private RootWorld(RootUser root, Tile[][] tiles, UserPlacer placer) {
		this.root   = root;
		this.tiles  = tiles;
		this.placer = placer == null ? new DefaultUserPlacer(this) : placer;
	}
	
	@Override
	public RootUser user() {
		return root;
	}
	
	@Override
	public int xlen() {
		return tiles.length;
	}
	
	@Override
	public int ylen() {
		return tiles[0].length;
	}
	
	@Override
	public Tile tile(int x, int y) {
		return tiles[x][y];
	}
	
	private void executeNTL() {
		for (Runnable r : nextTurnListeneres) {
			r.run();
		}
	}
	
	public boolean running() {
		return started;
	}
	
	public synchronized void start() {
		if (started) {
			throw new IllegalStateException("the game already started");
		}
		started = true;
		root.allowNewUsers(false);
		threadBuilder().start(this::executeNTL);
	}
	
	public synchronized void stop() {
		if (!started) {
			throw new IllegalStateException("the game is not started");
		}
		started = false;
		root.allowNewUsers(true);
		threadBuilder().start(this::executeNTL);
	}
	
	@Override
	public synchronized void addNextTurnListener(Runnable listener) {
		nextTurnListeneres.add(listener);
	}
	
	@Override
	public synchronized void removeNextTurnListener(Runnable listener) {
		nextTurnListeneres.remove(listener);
	}
	
	@Override
	public synchronized void finish(Turn t) {
		if (t.usr == root) {
			throw new UnsupportedOperationException("the root can not execute turns");
		}
		if (!started) {
			throw new IllegalStateException("not started");
		}
		if (!subWorlds.containsKey(t.usr)) {
			throw new AssertionError("this user is invalid");
		}
		userTurns.put(t.usr, t);
		if (userTurns.size() >= root.users().keySet().size()) {
			threadBuilder().start(this::executeTurn);
		}
	}
	
	private void executeTurn() { // do the turns in the order the users finished
		for (Turn userturn : userTurns.values()) { // and the entity turns in random order
			for (Entry<Entity, EntityTurn> entry : randomOrder(userturn.turns().entrySet())) {
				User usr = userturn.usr;
				try {
					Entity e = entry.getKey();
					if (e.owner() != usr) {
						throw new TurnExecutionException(ErrorType.UNKNOWN);
					}
					Tile t = tiles[e.x()][e.y()];
					executeEntityTurn(entry, e, t);
				} catch (TurnExecutionException e) {
					System.err.println("error while executing the turn for the user '" + usr + "': " + e.type);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("error while executing the turn for the user '" + usr + '\'');
					e.printStackTrace();
				}
			}
		}
		executeNTL();
	}
	
	@SuppressWarnings("preview")
	private void executeEntityTurn(Entry<Entity, EntityTurn> entry, Entity e, Tile t) throws TurnExecutionException {
		switch (entry.getValue()) {
		case MoveTurn mt -> {
			checkHasUnit(e, t);
			Unit            u    = (Unit) e;
			List<Direction> dirs = mt.dirs();
			if (u.moveRange() < dirs.size()) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			for (Direction dir : dirs) {
				int  x       = e.x();
				int  y       = e.y();
				Tile newTile = tiles[x + dir.xadd][y + dir.yadd];
				if (newTile.unit() != null) {
					throw new TurnExecutionException(ErrorType.BLOCKED_WAY);
				}
				u.changePos(x + dir.xadd, y + dir.yadd, newTile);
				tiles[x][y].unit(null);
				newTile.unit(u);
			}
		}
		case CarryTurn ct -> {
			checkHasUnit(e, t);
			Building b = t.building();
			if (b == null) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			if (!(e instanceof Unit u)) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			b.giveRes(u, ct.res(), ct.amount());
		}
		case StoreTurn st -> {
			checkHasUnit(e, t);
			Building b = t.building();
			if (b == null) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			if (!(e instanceof Unit u)) {
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
			b.store(u, st.amount());
		}
		default -> throw new AssertionError("unknown entity turn type: " + entry.getValue().getClass());
		}
	}
	
	private static void checkHasUnit(Entity e, Tile t) throws TurnExecutionException {
		if (t.unit() != e) {
			throw new TurnExecutionException(ErrorType.UNKNOWN);
		}
	}
	
	private Entry<Entity, EntityTurn>[] randomOrder(Set<Entry<Entity, EntityTurn>> turns) {
		@SuppressWarnings("unchecked") // do the entity turns in random order
		Entry<Entity, EntityTurn>[] arr = turns.toArray((Entry<Entity, EntityTurn>[]) new Entry[turns.size()]);
		for (int i = 0; i < arr.length - 1; i++) {
			int val = root.randomInt();
			val &= 0x7FFFFFFF;
			val %= arr.length - i;
			val += i;
			Entry<Entity, EntityTurn> e = arr[val];
			arr[val] = arr[i];
			arr[i]   = e;
		}
		return arr;
	}
	
	public synchronized UserWorld of(User usr, int usrModCnt) {
		if (usr == root) {
			throw new IllegalArgumentException("the root has no user world");
		} else if (usr != root.get(usr.name())) {
			throw new AssertionError("the user is not from my root");
		} else {
			return subWorlds.compute(usr, (u, uw) -> {
				if (uw != null && u.modifyCount() == uw.modCnt) {
					return uw;
				}
				return new UserWorld(this, usr, usrModCnt);
			});
		}
	}
	
	@Override
	public Map<User, List<Entity>> entities() {
		return entities(tiles);
	}
	
	public static final class Builder implements World {
		
		private static final OreResourceType[] RES = OreResourceType.values();
		private static final TileType[]        TYPES;
		
		static {
			TileType[] v   = TileType.values();
			int        len = v.length - 1;
			TYPES = new TileType[len];
			System.arraycopy(v, 1, TYPES, 0, len);
		}
		
		private List<Runnable> nextTurnListeners = new ArrayList<>();
		private final Tile[][] tiles;
		private final Random   rnd;
		private final RootUser root;
		
		private int resourceMask = 7;
		
		public Builder(RootUser usr, int xlen, int ylen) {
			this(usr, xlen, ylen, new Random());
		}
		
		public Builder(RootUser usr, int xlen, int ylen, Random rnd) {
			if (xlen <= 0 || ylen <= 0) {
				throw new IllegalArgumentException("xlen=" + xlen + " ylen=" + ylen);
			}
			if (rnd == null) {
				throw new NullPointerException("rnd is null");
			}
			if (usr == null) {
				throw new NullPointerException("usr is null");
			}
			this.root  = usr;
			this.tiles = new Tile[xlen][ylen];
			this.rnd   = rnd;
		}
		
		private Builder(RootUser usr, Tile[][] tiles, Random rnd) {
			this.root  = usr;
			this.tiles = tiles;
			this.rnd   = rnd;
		}
		
		private void executeNTL() {
			for (Runnable r : nextTurnListeners) {
				r.run();
			}
		}
		
		public void fillRandom() {
			placeSomePoints();
			boolean emptyTile;
			do {
				emptyTile = false;
				for (int x = 0; x < tiles.length; x++) {
					for (int y = 0; y < tiles[x].length; y++) {
						if (tiles[x][y] != null && tiles[x][y].type != TileType.NOT_EXPLORED) {
							continue;
						}
						Tile xDown = null;
						Tile yDown = null;
						Tile xUp   = null;
						Tile yUp   = null;
						if (x > 0) { xDown = tiles[x - 1][y]; }
						if (y > 0) { yDown = tiles[x][y - 1]; }
						if (x < tiles.length - 1) { xUp = tiles[x + 1][y]; }
						if (y < tiles[x].length - 1) { yUp = tiles[x][y + 1]; }
						if (xDown == null && yDown == null && xUp == null && yUp == null) {
							emptyTile = true;
							continue;
						}
						int rndLen;
						int oceanStart = 0;
						int oceanEnd   = 0;
						if ((xDown == null || xDown.type.isWater()) && (yDown == null || yDown.type.isWater()) && (xUp == null || xUp.type.isWater())
								&& (yUp == null || yUp.type.isWater())) {
							oceanEnd = 128;
							if (xDown != null && xDown.type == TileType.WATER_DEEP) { oceanEnd*=2; }
							if (yDown != null && yDown.type == TileType.WATER_DEEP) { oceanEnd*=2; }
							if (xUp != null && xUp.type == TileType.WATER_DEEP) { oceanEnd*=2; }
							if (yUp != null && yUp.type == TileType.WATER_DEEP) { oceanEnd*=2; }
						}
						int waterNormalStart = oceanEnd;
						int waterNormalEnd   = waterNormalStart + 2;
						if (xDown != null && xDown.type.isSwamp()) { waterNormalEnd+=2; }
						if (yDown != null && yDown.type.isSwamp()) { waterNormalEnd+=2; }
						if (xUp != null && xUp.type.isSwamp()) { waterNormalEnd+=2; }
						if (yUp != null && yUp.type.isSwamp()) { waterNormalEnd+=2; }
						rndLen            = waterNormalEnd;
						int sandStart     = 0;
						int sandEnd       = 0;
						int grassStart    = 0;
						int grassEnd      = 0;
						int forestStart   = 0;
						int forestEnd     = 0;
						int swampStart    = 0;
						int swampEnd      = 0;
						int mountainStart = 0;
						int mountainEnd   = 0;
						if ((xDown == null || xDown.type != TileType.WATER_DEEP) && (yDown == null || yDown.type != TileType.WATER_DEEP)
								&& (xUp == null || xUp.type != TileType.WATER_DEEP) && (yUp == null || yUp.type != TileType.WATER_DEEP)) {
							sandStart     = rndLen;
							sandEnd       = sandStart + 2;
							grassStart    = sandEnd;
							grassEnd      = grassStart + 2;
							forestStart   = grassEnd;
							forestEnd     = forestStart + 2;
							swampStart    = forestEnd;
							swampEnd      = swampStart + 2;
							mountainStart = swampEnd;
							mountainEnd   = mountainStart + 1;
							rndLen        = mountainEnd;
						}
						int      rndVal = rnd.nextInt(rndLen);
						TileType type;
						if (rndVal >= oceanStart && rndVal < oceanEnd) type = TileType.WATER_DEEP;
						else if (rndVal >= waterNormalStart && rndVal < waterNormalEnd) type = TileType.WATER_NORMAL;
						else if (rndVal >= sandStart && rndVal < sandEnd) type = (rndVal & 1) != 0 ? TileType.SAND : TileType.SAND_HILL;
						else if (rndVal >= grassStart && rndVal < grassEnd) type = (rndVal & 1) != 0 ? TileType.GRASS : TileType.GRASS_HILL;
						else if (rndVal >= forestStart && rndVal < forestEnd) type = (rndVal & 1) != 0 ? TileType.FOREST : TileType.FOREST_HILL;
						else if (rndVal >= swampStart && rndVal < swampEnd) type = (rndVal & 1) != 0 ? TileType.SWAMP : TileType.SWAMP_HILL;
						else if (rndVal >= mountainStart && rndVal < mountainEnd) type = TileType.MOUNTAIN;
						else throw new AssertionError("unexpected random value: " + rndVal);
						EnumIntMap<OreResourceType> resRndLens = new EnumIntMap<>(OreResourceType.class);
						resRndLens.set(OreResourceType.NONE, 16);
						resRndLens.set(OreResourceType.GOLD_ORE, 1);
						resRndLens.set(OreResourceType.IRON_ORE, 2);
						resRndLens.set(OreResourceType.COAL_ORE, 4);
						if (xDown != null) resRndLens.inc(xDown.resource);
						if (yDown != null) resRndLens.inc(yDown.resource);
						if (xUp != null) resRndLens.inc(xUp.resource);
						if (yUp != null) resRndLens.inc(yUp.resource);
						int noneStart = 0;
						int noneEnd   = resRndLens.get(OreResourceType.NONE);
						int goldStart = noneEnd;
						int goldEnd   = goldStart + resRndLens.get(OreResourceType.GOLD_ORE);
						int ironStart = goldEnd;
						int ironEnd   = ironStart + resRndLens.get(OreResourceType.IRON_ORE);
						int coalStart = ironEnd;
						int coalEnd   = coalStart + resRndLens.get(OreResourceType.COAL_ORE);
						rndLen = coalEnd;
						rndVal = rnd.nextInt(rndLen);
						OreResourceType ore;
						if (rndVal >= noneStart && rndVal < noneEnd) ore = OreResourceType.NONE;
						else if (rndVal >= goldStart && rndVal < goldEnd) ore = OreResourceType.GOLD_ORE;
						else if (rndVal >= ironStart && rndVal < ironEnd) ore = OreResourceType.IRON_ORE;
						else if (rndVal >= coalStart && rndVal < coalEnd) ore = OreResourceType.COAL_ORE;
						else throw new AssertionError("unexpected random value: " + rndVal);
						tiles[x][y] = new Tile(type, ore, true);
					}
				}
			} while (emptyTile);
		}
		
		private void placeSomePoints() {
			boolean doRndPoints = true;
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[x].length; y++) {
					if (tiles[x][y] == null || tiles[x][y].type == TileType.NOT_EXPLORED) {
						continue;
					}
					doRndPoints = false;
				}
				if (!doRndPoints) {
					break;
				}
			}
			if (doRndPoints) {
				for (int x = 0; x < tiles.length; x += 10) {
					for (int y = x % 20 == 0 ? 0 : 5; y < tiles[x].length; y += 10) {
						int rndVal = rnd.nextInt(TYPES.length << 1);
						TileType        t = rndVal >= TYPES.length ? TileType.WATER_DEEP : TYPES[rndVal];
						OreResourceType r = OreResourceType.NONE;
						if ((rnd.nextInt() & resourceMask) == 0) {
							r = RES[rnd.nextInt(RES.length)];
						}
						tiles[x][y] = new Tile(t, r, true);
					}
				}
			}
		}
		
		public void fillTotallyRandom() {
			for (Tile[] ts : tiles) {
				for (int i = 0; i < ts.length; i++) {
					if (ts[i] != null && ts[i].type != TileType.NOT_EXPLORED) {
						continue;
					}
					TileType        t = TYPES[rnd.nextInt(TYPES.length)]; // skip not explored
					OreResourceType r = OreResourceType.NONE;
					if ((rnd.nextInt() & resourceMask) == 0) {
						r = RES[rnd.nextInt(RES.length)];
					}
					ts[i] = new Tile(t, r, true);
				}
			}
			executeNTL();
		}
		
		public void resourceMask(int resourceMask) { this.resourceMask = resourceMask; }
		
		public int resourceMask() { return this.resourceMask; }
		
		@Override
		public RootUser user() {
			return root;
		}
		
		@Override
		public int xlen() {
			return tiles.length;
		}
		
		@Override
		public int ylen() {
			return tiles[0].length;
		}
		
		@Override
		public Tile tile(int x, int y) {
			if (tiles[x][y] == null) {
				tiles[x][y] = new Tile(TileType.NOT_EXPLORED, OreResourceType.NONE, true);
			}
			return tiles[x][y];
		}
		
		/**
		 * returns the tile at the given position
		 * <p>
		 * the difference between {@link #tile(int, int)} and {@link #get(int, int)} is,
		 * that this method returns <code>null</code>, if the tile is not set, while
		 * {@link #tile(int, int)} creates a new non explored tile without resource in
		 * this case
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * 
		 * @return
		 */
		public Tile get(int x, int y) {
			return tiles[x][y];
		}
		
		public void set(int x, int y, TileType t) {
			if (tiles[x][y] == null) {
				tiles[x][y] = new Tile(t, OreResourceType.NONE, true);
			} else {
				tiles[x][y] = new Tile(t, tiles[x][y].resource, true);
			}
			executeNTL();
		}
		
		public void set(int x, int y, OreResourceType r) {
			if (tiles[x][y] == null) {
				throw new NullPointerException("the tile does not already exist");
			} else {
				tiles[x][y] = new Tile(tiles[x][y].type, r, true);
			}
			executeNTL();
		}
		
		public void set(int x, int y, TileType t, OreResourceType r) {
			if (t == null) {
				throw new NullPointerException("type is null");
			}
			if (t == TileType.NOT_EXPLORED) {
				throw new NullPointerException("NOT_EXPLORED is not allowed");
			}
			if (r == null) {
				throw new NullPointerException("resource is null");
			}
			tiles[x][y] = new Tile(t, r, true);
			executeNTL();
		}
		
		public void set(int x, int y, Tile t) {
			tiles[x][y] = t;
			executeNTL();
		}
		
		public RootWorld create() throws IllegalStateException, NullPointerException {
			return create(root, this.tiles);
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles) throws IllegalStateException, NullPointerException {
			return create(root, tiles, null);
		}
		
		public boolean buildable() {
			for (int x = 0; x < tiles.length; x++) {
				Tile[] ts = tiles[x].clone();
				if (ts.length != tiles[0].length) {
					return false;
				}
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y];
					if (t == null) {
						return false;
					}
					if (t.type == null || t.resource == null) {
						return false;
					}
					if (t.type == TileType.NOT_EXPLORED) {
						return false;
					}
				}
			}
			return true;
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles, UserPlacer placer) throws IllegalStateException, NullPointerException {
			Tile[][] copy = tiles.clone();
			for (int x = 0; x < copy.length; x++) {
				Tile[] ts = copy[x].clone();
				if (ts.length != copy[0].length) {
					throw new IllegalStateException("the world has the have an rectangular form!");
				}
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y].copy();
					if (t == null) {
						throw new NullPointerException("no tile is allowed to be null");
					}
					if (t.type == null || t.resource == null) {
						throw new NullPointerException("a tile has a null type/resource");
					}
					if (t.type == TileType.NOT_EXPLORED) {
						throw new IllegalStateException("a tile with type NOT_EXPLORED was found");
					}
				}
			}
			return new RootWorld(root, copy, placer);
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles) {
			return createBuilder(root, tiles, new Random());
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles, Random rnd) {
			Tile[][] copy = tiles.clone();
			for (int x = 0; x < copy.length; x++) {
				if (copy[x].length != copy[0].length) {
					throw new IllegalStateException("the world has the have an rectangular form!");
				} // only enforce an rectangular form for the builder
			}
			return new Builder(root, tiles, rnd);
		}
		
		@Override
		public void addNextTurnListener(Runnable listener) { nextTurnListeners.add(listener); }
		
		@Override
		public void removeNextTurnListener(Runnable listener) { nextTurnListeners.remove(listener); }
		
		@Override
		public Map<User, List<Entity>> entities() {
			return RootWorld.entities(tiles);
		}
		
		@Override
		public void finish(Turn t) {
			throw new UnsupportedOperationException("this is an build world");
		}
		
		public void set(int x, int y, Building build) {
			tile(x, y).build(build);
			executeNTL();
		}
		
		public void set(int x, int y, Unit unit) {
			tile(x, y).unit(unit);
			executeNTL();
		}
		
	}
	
	private static Map<User, List<Entity>> entities(Tile[][] tiles) {
		Map<User, List<Entity>> result = new HashMap<>();
		for (int x = 0; x < tiles.length; x++) {
			Tile[] ts = tiles[x];
			for (int y = 0; y < ts.length; y++) {
				Tile t = ts[y];
				if (t == null) {
					continue;
				}
				add(result, t.unit());
				add(result, t.building());
			}
		}
		return result;
	}
	
	private static void add(Map<User, List<Entity>> result, Entity u) {
		if (u != null) {
			result.computeIfAbsent(u.owner(), usr -> new ArrayList<>()).add(u);
		}
	}
	
}
