package de.hechler.patrick.games.squareconqerer.world;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
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
	private volatile long              seed;
	private volatile Random            rnd;
	
	private RootWorld(RootUser root, Tile[][] tiles, UserPlacer placer) {
		this.root   = root;
		this.tiles  = tiles;
		this.placer = placer == null ? new DefaultUserPlacer() : placer;
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
	
	/**
	 * this is only done when the game starts for the random seed of the world
	 * <ol>
	 * <li>Server: {@link #REQ_RND}</li>
	 * <li>Client: {@link #GIV_RND}</li>
	 * <li>Client: 16 random bytes</li>
	 * </ol>
	 */
	public static final int REQ_RND = 0x558F7BB1;
	public static final int GIV_RND = 0x460C8B92;
	
	public static void fillRnd(Connection conn, byte[] arr) throws IOException {
		if (arr.length != 16) throw new AssertionError("the given array has an inavlid size");
		conn.blocked(() -> {
			conn.writeInt(REQ_RND);
			conn.readInt(GIV_RND);
			conn.readArr(arr);
		});
	}
	
	public synchronized void start(byte[] s) {
		if (s == null) throw new NullPointerException("the given seed is null");
		if (rnd != null) throw new IllegalStateException("the game already started");
		synchronized (root) {
			root.allowNewUsers(false);
			Map<String, User> map = root.users();
			map.remove(RootUser.ROOT_NAME);
			Collection<User> values = map.values();
			User[]           users  = values.toArray(new User[values.size()]);
			Arrays.sort(users, (a, b) -> a.name().compareTo(b.name()));
			if ((users.length + 1) << 4 != s.length) throw new IllegalArgumentException("the given seed is illegal");
			long sval = seed(s);
			seed = sval;
			rnd  = new Random(sval);
			placer.initilize(this, users, rnd);
		}
		threadBuilder().start(this::executeNTL);
	}
	
	private static long seed(byte[] s) {
		long val = 0x6AAF88D7759474ABL;
		for (int i = 0; i < s.length; i += 16) {
			long vi = val(s, i);
			val  = (val * vi) ^ ~Math.multiplyHigh(val, vi);
			val ^= val(s, i + 8);
		}
		return val;
	}
	
	private static long val(byte[] s, int i) {
		return s[i] & 0xFF | ((s[i + 1] & 0xFFL) << 8) | ((s[i + 2] & 0xFFL) << 16) | ((s[i + 3] & 0xFFL) << 24) | ((s[i + 4] & 0xFFL) << 32)
				| ((s[i + 5] & 0xFFL) << 40) | ((s[i + 6] & 0xFFL) << 48) | ((s[i + 7] & 0xFFL) << 56);
	}
	
	public boolean running() {
		return rnd != null;
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
		if (rnd == null) {
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
		shuffle(rnd, arr);
		return arr;
	}
	
	public static <T> void shuffle(Random rnd, T[] arr) {
		for (int i = 0; i < arr.length - 1; i++) {
			int val = rnd.nextInt();
			val &= 0x7FFFFFFF;
			val %= arr.length - i;
			val += i;
			T e = arr[val];
			arr[val] = arr[i];
			arr[i]   = e;
		}
	}
	
	public synchronized World of(User usr, int usrModCnt) {
		if (usr == root) {
			return this;
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
			while (fillOnce());
			executeNTL();
		}
		
		private boolean fillOnce() {
			boolean unfilledTile = false;
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
						unfilledTile = true;
						continue;
					}
					tiles[x][y] = tile(xDown, xUp, yDown, yUp);
				}
			}
			return unfilledTile;
		}
		
		private Tile tile(Tile xDown, Tile xUp, Tile yDown, Tile yUp) {
			TileType        type = type(xDown, xUp, yDown, yUp);
			OreResourceType ore  = resource(xDown, xUp, yDown, yUp);
			return new Tile(type, ore, true);
		}
		
		private OreResourceType resource(Tile xDown, Tile xUp, Tile yDown, Tile yUp) {
			int none = none(xDown) + none(xUp) + none(yDown) + none(yUp);
			int gold = gold(xDown) + gold(xUp) + gold(yDown) + gold(yUp);
			int iron = iron(xDown) + iron(xUp) + iron(yDown) + iron(yUp);
			int coal = coal(xDown) + coal(xUp) + coal(yDown) + coal(yUp);
			
			int posNone = (none * 2) + 1;
			int posGold = (gold * 2) + 1;
			int posIron = (iron * 2) + 1;
			int posCoal = (coal * 2) + 1;
			int rndVal  = rnd.nextInt(posNone + posGold + posIron + posCoal);
			
			OreResourceType ore = null;
			if (rndVal >= posNone) rndVal -= posNone;
			else ore = OreResourceType.NONE;
			if (rndVal >= posGold) rndVal -= posGold;
			else if (ore == null) ore = OreResourceType.GOLD_ORE;
			if (rndVal >= posIron) rndVal -= posIron;
			else if (ore == null) ore = OreResourceType.IRON_ORE;
			if (rndVal >= posCoal) {/**/} //
			else if (ore == null) ore = OreResourceType.COAL_ORE;
			return ore;
		}
		
		// @formatter:off
		private static int none(Tile t) { return t != null && t.resource == OreResourceType.NONE     ? 1 : 0; }
		private static int gold(Tile t) { return t != null && t.resource == OreResourceType.GOLD_ORE ? 1 : 0; }
		private static int iron(Tile t) { return t != null && t.resource == OreResourceType.IRON_ORE ? 1 : 0; }
		private static int coal(Tile t) { return t != null && t.resource == OreResourceType.COAL_ORE ? 1 : 0; }
		// @formatter:on
		
		private TileType type(Tile xDown, Tile xUp, Tile yDown, Tile yUp) {
			int ocean = ocean(xDown) + ocean(xUp) + ocean(yDown) + ocean(yUp);
			int land  = land(xDown) + land(xUp) + land(yDown) + land(yUp);
			if (ocean != 0 && land != 0) {
				return TileType.WATER_NORMAL;
			}
			int      water    = water(xDown) + water(xUp) + water(yDown) + water(yUp);
			int      flat     = flat(xDown) + flat(xUp) + flat(yDown) + flat(yUp);
			int      mountain = mountain(xDown) + mountain(xUp) + mountain(yDown) + mountain(yUp);
			TileType type     = rawType(xDown, xUp, yDown, yUp, ocean, water, flat, mountain);
			if (type.isLand() && !type.isMountain()) {
				int hill    = hill(xDown) + hill(xUp) + hill(yDown) + hill(yUp);
				int posHill = (mountain * 4) + (hill * 2) + 1;
				int posFlat = (flat * 2) + 1;
				int rndVal1 = rnd.nextInt(posHill + posFlat);
				if (rndVal1 < posHill) {
					type = type.addHill();
				}
			} else if (type.isWater()) {
				if (land == 0) {
					int posOcean  = ocean * 2 + 1;
					int posNormal = (water - ocean) * 2 + 1;
					int rndVal1   = rnd.nextInt(posOcean + posNormal);
					if (rndVal1 < posOcean) {
						type = TileType.WATER_DEEP;
					}
				}
			} else if (!type.isMountain()) throw new AssertionError("unknown type: " + type.name());
			return type;
		}
		
		private TileType rawType(Tile xDown, Tile xUp, Tile yDown, Tile yUp, int ocean, int water, int flat, int mountain) {
			int sand   = sand(xDown) + sand(xUp) + sand(yDown) + sand(yUp);
			int grass  = grass(xDown) + grass(xUp) + grass(yDown) + grass(yUp);
			int forest = forest(xDown) + forest(xUp) + forest(yDown) + forest(yUp);
			int swamp  = swamp(xDown) + swamp(xUp) + swamp(yDown) + swamp(yUp);
			// ocean disables everything except water
			// flat disables mountain
			int      posWater    = (water * 2) + 1;
			int      posMountain = ocean == 0 ? Math.max((mountain * 2) - (flat * 2) + 1, 1) : 0;
			int      posSand     = ocean == 0 ? (sand * 2) + 1 : 0;
			int      posGrass    = ocean == 0 ? (grass * 2) + 1 : 0;
			int      posForest   = ocean == 0 ? (forest * 2) + 1 : 0;
			int      posSwamp    = ocean == 0 ? (swamp * 2) + 1 : 0;
			int      rndVal0     = rnd.nextInt(posWater + posMountain + posSand + posGrass + posForest + posSwamp);
			TileType type        = null;
			if (rndVal0 >= posWater) rndVal0 -= posWater;
			else type = TileType.WATER_NORMAL;
			if (rndVal0 >= posMountain) rndVal0 -= posMountain;
			else if (type == null) type = TileType.MOUNTAIN;
			if (rndVal0 >= posSand) rndVal0 -= posSand;
			else if (type == null) type = TileType.SAND;
			if (rndVal0 >= posGrass) rndVal0 -= posGrass;
			else if (type == null) type = TileType.GRASS;
			if (rndVal0 >= posForest) rndVal0 -= posForest;
			else if (type == null) type = TileType.FOREST;
			if (rndVal0 >= posSwamp && type == null) throw new AssertionError("unexpected random value");
			else if (type == null) type = TileType.SWAMP;
			return type;
		}
		
		// @formatter:off
		private static int swamp(Tile t)    { return t != null && t.type.isSwamp()    ? 1 : 0; }
		private static int forest(Tile t)   { return t != null && t.type.isForest()   ? 1 : 0; }
		private static int grass(Tile t)    { return t != null && t.type.isGrass()    ? 1 : 0; }
		private static int sand(Tile t)     { return t != null && t.type.isSand()     ? 1 : 0; }
		private static int land(Tile t)     { return t != null && t.type.isLand()     ? 1 : 0; }
		private static int flat(Tile t)     { return t != null && t.type.isFlat()     ? 1 : 0; }
		private static int hill(Tile t)     { return t != null && t.type.isHill()     ? 1 : 0; }
		private static int ocean(Tile t)    { return t != null && t.type.isOcean()    ? 1 : 0; }
		private static int water(Tile t)    { return t != null && t.type.isWater()    ? 1 : 0; }
		private static int mountain(Tile t) { return t != null && t.type.isMountain() ? 1 : 0; }
		// @formatter:on
		
		private void placeSomePoints() {
			for (int x = 0; x < tiles.length; x += 8) {
				for (int y = x % 16 == 0 ? 0 : 4; y < tiles[x].length; y += 8) {
					if ((tiles[x][y] != null && tiles[x][y].type != TileType.NOT_EXPLORED) // do not place nearby other tiles and do not overwrite
																							// tiles
							|| (x > 0 && (tiles[x - 1][y] != null && tiles[x - 1][y].type != TileType.NOT_EXPLORED))
							|| (y > 0 && (tiles[x][y - 1] != null && tiles[x][y - 1].type != TileType.NOT_EXPLORED))
							|| (x + 1 < tiles.length && (tiles[x + 1][y] != null && tiles[x + 1][y].type != TileType.NOT_EXPLORED))
							|| (y + 1 < tiles[x].length && (tiles[x][y + 1] != null && tiles[x][y + 1].type != TileType.NOT_EXPLORED))) {
						continue;
					}
					int             rndVal = rnd.nextInt(TYPES.length << 1);
					TileType        t      = rndVal >= TYPES.length ? TileType.WATER_DEEP : TYPES[rndVal];
					OreResourceType r      = OreResourceType.NONE;
					if ((rnd.nextInt() & resourceMask) == 0) {
						r = RES[rnd.nextInt(RES.length)];
					}
					tiles[x][y] = new Tile(t, r, true);
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
