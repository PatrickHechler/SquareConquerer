package de.hechler.patrick.games.squareconqerer.world;

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
import de.hechler.patrick.games.squareconqerer.enums.Direction;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.UserPlacer;
import de.hechler.patrick.games.squareconqerer.world.placer.DefaultUserPlacer;
import de.hechler.patrick.games.squareconqerer.world.turn.CarryTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.EntityTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.MoveTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.StoreTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public class RootWorld implements World {
	
	private final RootUser             root;
	private final Tile[][]             tiles;
	private final UserPlacer           placer;
	private final Map<User, UserWorld> subWorlds          = new HashMap<>();
	private final List<Runnable>       nextTurnListeneres = new LinkedList<>();
	private final Map<User, Turn>      userTurns          = new LinkedHashMap<>();
	
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
	
	@Override
	public synchronized void addNextTurnListener(Runnable listener) {
		nextTurnListeneres.add(listener);
	}
	
	@Override
	public synchronized void removeNextTurnListener(Runnable listener) {
		nextTurnListeneres.remove(listener);
	}
	
	public synchronized void finishUserTurn(Turn turn) {
		if (!subWorlds.containsKey(turn.usr)) {
			throw new AssertionError("this user is invalid");
		}
		userTurns.put(turn.usr, turn);
		if (userTurns.size() >= root.names().size()) {
			executeTurn();
		}
	}
	
	private void executeTurn() { // do the turns in the order the users finished
		for (Turn userturn : userTurns.values()) {
			for (Entry<Entity, EntityTurn> entry : randomOrder(userturn.turns().entrySet())) {
				User usr = userturn.usr;
				try {
					Entity e = entry.getKey();
					if (e.owner() != usr) {
						throw new TurnExecutionException(usr, ErrorType.UNKNOWN);
					}
					Tile t = tiles[e.x()][e.y()];
					executeEntityTurn(entry, usr, e, t);
				} catch (TurnExecutionException e) {
					System.err.println("error while executing the turn for the user '" + e.usr + "': " + e.type);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("error while executing the turn for the user '" + usr + '\'');
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("preview")
	private void executeEntityTurn(Entry<Entity, EntityTurn> entry, User usr, Entity e, Tile t) throws TurnExecutionException {
		switch (entry.getValue()) {
		case MoveTurn mt -> {
			checkHasUnit(usr, e, t);
			Unit            u    = (Unit) e;
			List<Direction> dirs = mt.dirs();
			if (u.moveRange() < dirs.size()) {
				throw new TurnExecutionException(usr, ErrorType.INVALID_TURN);
			}
			for (Direction dir : dirs) {
				int x = e.x();
				int y = e.y();
				if (tiles[x + dir.xadd][y + dir.yadd].unit() != null) {
					throw new TurnExecutionException(usr, ErrorType.BLOCKED_WAY);
				}
				u.xy(x + dir.xadd, y + dir.yadd);
				tiles[x][y].unit(null);
				tiles[x + dir.xadd][y + dir.yadd].unit(u);
			}
		}
		case CarryTurn ct -> {
			checkHasUnit(usr, e, t);
			Building b = t.building();
			if (b == null) {
				throw new TurnExecutionException(usr, ErrorType.INVALID_TURN);
			}
			if (!(e instanceof Unit u)) {
				throw new TurnExecutionException(usr, ErrorType.INVALID_TURN);
			}
			b.giveRes(u, ct.res(), ct.amount());
		}
		case StoreTurn st -> {
			checkHasUnit(usr, e, t);
			Building b = t.building();
			if (b == null) {
				throw new TurnExecutionException(usr, ErrorType.INVALID_TURN);
			}
			if (!(e instanceof Unit u)) {
				throw new TurnExecutionException(usr, ErrorType.INVALID_TURN);
			}
			b.store(u, st.amount());
		}
		default -> throw new AssertionError("unknown entity turn type: " + entry.getValue().getClass());
		}
	}
	
	private static void checkHasUnit(User usr, Entity e, Tile t) throws TurnExecutionException {
		if (t.unit() != e) {
			throw new TurnExecutionException(usr, ErrorType.UNKNOWN);
		}
	}
	
	private Entry<Entity, EntityTurn>[] randomOrder(Set<Entry<Entity, EntityTurn>> turns) {
		@SuppressWarnings("unchecked") // do the entity turns in random order
		Entry<Entity, EntityTurn>[] arr = turns.toArray((Entry<Entity, EntityTurn>[]) new Entry[turns.size()]);
		for (int i = 0; i < arr.length; i++) {
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
				return placer.apply(usr, usrModCnt);
			});
		}
	}
	
	public static class Builder implements World {
		
		private static final OreResourceType[] RES = OreResourceType.values();
		private static final TileType[]        TYPES;
		
		static {
			TileType[] v   = TileType.values();
			int        len = v.length - 1;
			TYPES = new TileType[len];
			System.arraycopy(v, 1, TYPES, 0, len);
		}
		
		private final Tile[][] tiles;
		private final Random   rnd;
		private final RootUser usr;
		
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
			this.usr   = usr;
			this.tiles = new Tile[xlen][ylen];
			this.rnd   = rnd;
		}
		
		private Builder(RootUser usr, Tile[][] tiles, Random rnd) {
			this.usr   = usr;
			this.tiles = tiles;
			this.rnd   = rnd;
		}
		
		public void fillRandom() {
			fillTotallyRandom();
		}
		
		public void fillTotallyRandom() {
			for (Tile[] ts : tiles) {
				for (int i = 0; i < ts.length; i++) {
					if (ts[i] != null) {
						continue;
					}
					TileType        t = TYPES[rnd.nextInt(TYPES.length)]; // skip not explored
					OreResourceType r = OreResourceType.NONE;
					if ((rnd.nextInt() & resourceMask) == 0) {
						r = RES[rnd.nextInt(RES.length)];
					}
					ts[i] = new Tile(t, r);
				}
			}
		}
		
		public void resourceMask(int resourceMask) { this.resourceMask = resourceMask; }
		
		public int resourceMask() { return this.resourceMask; }
		
		@Override
		public RootUser user() {
			return usr;
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
				tiles[x][y] = new Tile(TileType.NOT_EXPLORED, OreResourceType.NONE);
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
				tiles[x][y] = new Tile(t, OreResourceType.NONE);
			} else {
				tiles[x][y] = new Tile(t, tiles[x][y].resource);
			}
		}
		
		public void set(int x, int y, OreResourceType r) {
			if (tiles[x][y] == null) {
				throw new NullPointerException("the tile does not already exist");
			} else {
				tiles[x][y] = new Tile(tiles[x][y].type, r);
			}
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
			tiles[x][y] = new Tile(t, r);
		}
		
		public void set(int x, int y, Tile t) {
			tiles[x][y] = t;
		}
		
		public RootWorld create() throws IllegalStateException, NullPointerException {
			return create(usr, this.tiles);
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles) throws IllegalStateException, NullPointerException {
			return create(root, tiles, null);
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
		public void addNextTurnListener(Runnable listener) {/* I don't support turns */}
		
		@Override
		public void removeNextTurnListener(Runnable listener) {/* I don't support turns */}
		
	}
	
}
