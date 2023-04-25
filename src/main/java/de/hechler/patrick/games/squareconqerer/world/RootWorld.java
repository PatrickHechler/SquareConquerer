package de.hechler.patrick.games.squareconqerer.world;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.objects.Random2;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.Direction;
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

public final class RootWorld implements World, Iterable<RootWorld> {
	
	private final RootUser              root;
	private final Tile[][]              tiles;
	private final UserPlacer            placer;
	private final Map<User, UserWorld>  subWorlds;
	private final List<Runnable>        nextTurnListeneres;
	private final Map<User, Turn>       userTurns;
	private final List<Map<User, Turn>> allTurns;
	private volatile Tile[][]           starttiles;
	private volatile byte[]             seed;
	private volatile Random2            rnd;
	
	private RootWorld(RootUser root, Tile[][] tiles, UserPlacer placer) {
		this.root               = root;
		this.tiles              = tiles;
		this.placer             = placer == null ? new DefaultUserPlacer() : placer;
		this.subWorlds          = new HashMap<>();
		this.nextTurnListeneres = new ArrayList<>();
		this.userTurns          = new TreeMap<>();
		this.allTurns           = new ArrayList<>();
	}
	
	private RootWorld(RootWorld rw, UserPlacer placer) {
		this.root               = rw.root;
		this.tiles              = rw.tiles;
		this.placer             = placer;
		this.subWorlds          = rw.subWorlds;
		this.nextTurnListeneres = rw.nextTurnListeneres;
		this.userTurns          = rw.userTurns;
		this.allTurns           = rw.allTurns;
		this.starttiles         = rw.starttiles;
		this.seed               = rw.seed;
		this.rnd                = rw.rnd;
		if (placer == null || !this.nextTurnListeneres.isEmpty()) throw new AssertionError();
	}
	
	@Override
	public RootUser user() {
		return this.root;
	}
	
	@Override
	public int xlen() {
		return this.tiles.length;
	}
	
	@Override
	public int ylen() {
		return this.tiles[0].length;
	}
	
	@Override
	public Tile tile(int x, int y) {
		return this.tiles[x][y];
	}
	
	private void executeNTL() {
		for (Runnable r : this.nextTurnListeneres) {
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
			conn.writeReadInt(REQ_RND, GIV_RND);
			conn.readArr(arr);
		});
	}
	
	/**
	 * returns the number of worlds the iterator can currently return
	 * 
	 * @return the number of worlds the iterator can currently return
	 */
	public int iterCount() {
		return this.allTurns.size() + 2;
	}
	
	/**
	 * iterate over all turns made until now
	 * <p>
	 * note that the returned iterator will reuse the same element for everything
	 * <p>
	 * the first element will be the world before the start of the game<br>
	 * the second element will be the world before the first turn<br>
	 * then every time {@link Iterator#next()} is called, one turn more will executed in the returned world<br>
	 * at the end the returned world should look like the current world
	 */
	public Iterator<RootWorld> iterator() {
		if (this.rnd == null) throw new IllegalStateException("I can only iterate over all turns, when there is a running game!");
		return new Iterator<RootWorld>() {
			
			private Iterator<Map<User, Turn>> iter  = RootWorld.this.allTurns.iterator();
			private RootWorld                 world = null;
			
			@Override
			public boolean hasNext() {
				return this.iter.hasNext() || this.world == null;
			}
			
			@Override
			public RootWorld next() {
				if (this.world == null) { // do a copy of the start tiles
					this.world = Builder.create(RootWorld.this.root, RootWorld.this.starttiles, RootWorld.this.placer);
				} else if (!this.world.running()) {
					this.world.startGame0(RootWorld.this.seed, false);
				} else if (!this.iter.hasNext()) {
					throw new NoSuchElementException("no more elements");
				} else {
					Map<User, Turn> val = this.iter.next();
					for (Turn t : val.values()) {
						this.world.finish(t);
					}
				}
				return this.world;
			}
			
		};
	}
	
	private static final int RWS_START  = 0x07D78BDC;
	private static final int RWS_SUB0   = 0x327B8CFB;
	private static final int RWS_SUB1   = 0xCC2BB5FA;
	private static final int RWS_SUB2   = 0xB5F22CF2;
	private static final int RWS_SUB3   = 0xBA376590;
	private static final int RWS_SUB4   = 0x2DB9E5C9;
	private static final int RWS_SUB5   = 0x0FFE8516;
	private static final int RWS_SUB6   = 0xB99A5E60;
	private static final int RWS_SUB7   = 0x8B25AC3E;
	private static final int RWS_SUB8   = 0xB6676572;
	private static final int RWS_FINISH = 0x934ABD64;
	
	public synchronized void saveEverything(Connection conn) throws IOException {
		saveEverything(conn, true);
	}
	
	private /* synchronized */ void saveEverything(Connection conn, boolean savePWs) throws IOException {
		if (this.seed == null) { throw new IllegalStateException("the world needs to be started for that"); }
		conn.writeInt(RWS_START);
		conn.writeLong(this.rnd.getCurrentSeed());
		conn.writeInt(this.seed.length);
		conn.writeArr(this.seed);
		conn.writeInt(RWS_SUB0);
		if (savePWs) {
			this.root.save(conn);
		} else {
			RootUser r = RootUser.create(new char[0]);
			this.root.users().keySet().forEach(name -> r.add(name, new char[0]));
		}
		conn.writeInt(RWS_SUB1);
		OpenWorld.saveWorld(this, conn);
		conn.writeInt(RWS_SUB2); // I just need a world with the startTiles
		OpenWorld.saveWorld(new RootWorld(this.root, this.starttiles, this.placer), conn);
		conn.writeInt(RWS_SUB3);
		conn.writeInt(this.subWorlds.size());
		for (Entry<User, UserWorld> e : this.subWorlds.entrySet()) {
			User      usr = e.getKey();
			UserWorld uw  = e.getValue();
			conn.writeString(usr.name());
			conn.writeInt(RWS_SUB4);
			OpenWorld.saveWorld(uw, conn);
		}
		conn.writeInt(RWS_SUB5);
		conn.writeInt(this.allTurns.size());
		for (Map<User, Turn> ts : this.allTurns) {
			conn.writeInt(RWS_SUB6);
			conn.writeInt(ts.size());
			for (Entry<User, Turn> e : ts.entrySet()) {
				User u = e.getKey();
				Turn t = e.getValue();
				conn.writeString(u.name());
				conn.writeInt(RWS_SUB7);
				t.sendTurn(conn);
			}
		}
		conn.writeInt(RWS_SUB7);
		conn.writeString(this.placer.getClass().getName());
		conn.writeInt(RWS_SUB8);
		this.placer.writePlacer(conn);
		conn.writeInt(RWS_FINISH);
	}
	
	public static RootWorld loadEverything(Connection conn) throws IOException {
		conn.readInt(RWS_START);
		long   curSeed = conn.readLong();
		byte[] seed    = new byte[conn.readInt()];
		conn.readArr(seed);
		conn.readInt(RWS_SUB0);
		RootUser root = (RootUser) conn.usr;
		root.load(conn);
		conn.readInt(RWS_SUB1);
		Tile[][]  tiles = RemoteWorld.loadWorld(conn, root.users());
		RootWorld res   = RootWorld.Builder.create(root, tiles);
		conn.readInt(RWS_SUB2);
		res.starttiles = RemoteWorld.loadWorld(conn, root.users());
		conn.readInt(RWS_SUB2);
		res.seed = seed;
		res.rnd  = new Random2(curSeed);
		for (int remain = conn.readInt(); remain > 0; remain--) {
			String name = conn.readString();
			User   usr  = root.get(name);
			conn.readInt(RWS_SUB4);
			UserWorld uw = res.usrOf(usr, 0);
			RemoteWorld.loadWorld(conn, root.users(), uw.cach());
		}
		conn.readInt(RWS_SUB5);
		for (int remain = conn.readInt(); remain > 0; remain--) {
			conn.readInt(RWS_SUB6);
			Map<User, Turn> add = new HashMap<>();
			for (int iremain = conn.readInt(); iremain > 0; iremain--) {
				String name = conn.readString();
				User   usr  = root.get(name);
				conn.readInt(RWS_SUB7);
				Turn t = new Turn(res.usrOf(usr, 0));
				t.retrieveTurn(conn);
				add.put(usr, t);
			}
			res.allTurns.add(add);
		}
		conn.readInt(RWS_SUB7);
		try {
			Class<?> placerCls = Class.forName(conn.readString());
			conn.readInt(RWS_SUB8);
			Method     met    = placerCls.getMethod("readPlacer", Connection.class);
			UserPlacer placer = (UserPlacer) met.invoke(null, conn);
			conn.readInt(RWS_FINISH);
			return new RootWorld(res, placer);
		} catch (IllegalAccessException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			Throwable c = e.getCause();
			if (c instanceof IOException ioe) throw ioe;
			if (c instanceof RuntimeException re) throw re;
			if (c instanceof Error err) throw err;
			throw new AssertionError(e);
		}
	}
	
	public void startGame(byte[] s) {
		startGame0(s, true);
	}
	
	private synchronized void startGame0(byte[] s, boolean modifyRoot) {
		if (s == null) throw new NullPointerException("the given seed is null");
		if (this.rnd != null) throw new IllegalStateException("the game already started");
		synchronized (this.root) {
			this.starttiles = new Tile[this.tiles.length][this.tiles[0].length];
			for (int x = 0; x < this.starttiles.length; x++) {
				for (int y = 0; y < this.starttiles[x].length; y++) {
					this.starttiles[x][y] = this.tiles[x][y].copy();
				}
			}
			if (modifyRoot) this.root.allowNewUsers(false);
			Map<String, User> map = this.root.users();
			map.remove(RootUser.ROOT_NAME);
			Collection<User> values = map.values();
			User[]           users  = values.toArray(new User[values.size()]);
			Arrays.sort(users, null);
			if ((users.length + 1) * 16 != s.length) throw new IllegalArgumentException("the given seed is illegal");
			long sval = seed(s);
			this.seed = s;
			this.rnd  = new Random2(sval);
			this.placer.initilize(this, users, this.rnd);
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
	
	public synchronized boolean running() {
		return this.rnd != null;
	}
	
	public static final int START_VAL_GAME = 0x7C6879B3;
	public static final int SUB0_VAL_GAME  = 0xD597E0E5;
	public static final int SUB1_VAL_GAME  = 0xF8777674;
	public static final int FIN_VAL_GAME   = 0xF6DB6A6B;
	
	public synchronized void validateGame(Connection conn) throws IOException {
		if (!conn.isBlocking()) throw new IllegalStateException("the connection has to be in block state");
		conn.writeReadInt(START_VAL_GAME, SUB0_VAL_GAME);
		saveEverything(conn, false);
		conn.writeInt(FIN_VAL_GAME);
		conn.readInt(SUB1_VAL_GAME);
	}
	
	@Override
	public synchronized void addNextTurnListener(Runnable listener) {
		this.nextTurnListeneres.add(listener);
	}
	
	@Override
	public synchronized void removeNextTurnListener(Runnable listener) {
		this.nextTurnListeneres.remove(listener);
	}
	
	@Override
	public synchronized void finish(Turn t) {
		if (t.usr == this.root) { throw new UnsupportedOperationException("the root can not execute turns"); }
		if (this.rnd == null) { throw new IllegalStateException("not started"); }
		if (!this.subWorlds.containsKey(t.usr)) { throw new AssertionError("this user is invalid"); }
		for (EntityTurn e : t.turns()) {
			if (e.entity().owner() != t.usr) { throw new IllegalArgumentException("the turn wants to use not owned entities"); }
		}
		this.userTurns.put(t.usr, t);
		if (this.userTurns.size() >= this.root.users().keySet().size()) {
			threadBuilder().start(this::executeTurn);
		}
	}
	
	private void executeTurn() {
		List<EntityTurn> list = new ArrayList<>();
		for (Turn userturn : this.userTurns.values()) {
			list.addAll(userturn.turns());
		} // list is sorted: user names and then entity turns
		this.allTurns.add(new HashMap<>(this.userTurns));
		for (EntityTurn et : randomOrder(list)) {
			try {
				Entity e = et.entity();
				Tile   t = this.tiles[e.x()][e.y()];
				executeEntityTurn(et, e, t);
			} catch (TurnExecutionException e) {
				System.err.println("error while executing a turn for the user '" + et.entity().owner() + "': " + e.type);
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("error while executing a turn for the user '" + et.entity().owner() + '\'');
				e.printStackTrace();
			}
		}
		executeNTL();
	}
	
	@SuppressWarnings("preview")
	private void executeEntityTurn(EntityTurn turn, Entity e, Tile t) throws TurnExecutionException {
		switch (turn) {
		case MoveTurn mt -> {
			checkHasUnit(e, t);
			Unit            u    = (Unit) e;
			List<Direction> dirs = mt.dirs();
			if (u.moveRange() < dirs.size()) { throw new TurnExecutionException(ErrorType.INVALID_TURN); }
			for (Direction dir : dirs) {
				int  x       = e.x();
				int  y       = e.y();
				Tile newTile = this.tiles[x + dir.xadd][y + dir.yadd];
				if (newTile.unit() != null) { throw new TurnExecutionException(ErrorType.BLOCKED_WAY); }
				u.changePos(x + dir.xadd, y + dir.yadd, newTile);
				this.tiles[x][y].unit(null);
				newTile.unit(u);
			}
		}
		case CarryTurn ct -> {
			checkHasUnit(e, t);
			Building b = t.building();
			if (b == null) { throw new TurnExecutionException(ErrorType.INVALID_TURN); }
			if (!(e instanceof Unit u)) { throw new TurnExecutionException(ErrorType.INVALID_TURN); }
			b.giveRes(u, ct.res(), ct.amount());
		}
		case StoreTurn st -> {
			checkHasUnit(e, t);
			Building b = t.building();
			if (b == null) { throw new TurnExecutionException(ErrorType.INVALID_TURN); }
			if (!(e instanceof Unit u)) { throw new TurnExecutionException(ErrorType.INVALID_TURN); }
			b.store(u, st.amount());
		}
		default -> throw new AssertionError("unknown entity turn type: " + turn.getClass());
		}
	}
	
	private static void checkHasUnit(Entity e, Tile t) throws TurnExecutionException {
		if (t.unit() != e) { throw new TurnExecutionException(ErrorType.UNKNOWN); }
	}
	
	private EntityTurn[] randomOrder(Collection<EntityTurn> c) {
		EntityTurn[] arr = c.toArray(new EntityTurn[c.size()]);
		shuffle(this.rnd, arr);
		return arr;
	}
	
	public static <T> void shuffle(Random2 rnd, T[] arr) {
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
		if (usr == this.root) {
			return this;
		} else if (usr != this.root.get(usr.name())) {
			throw new AssertionError("the user is not from my root");
		} else {
			return usrOf(usr, usrModCnt);
		}
	}
	
	private UserWorld usrOf(User usr, int usrModCnt) {
		return this.subWorlds.compute(usr, (u, uw) -> {
			if (uw != null && u.modifyCount() == uw.modCnt) { return uw; }
			return UserWorld.usrOf(this, usr, usrModCnt);
		});
	}
	
	@Override
	public Map<User, List<Entity>> entities() {
		return entities(this.tiles);
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
		private final Random2  rnd;
		private final RootUser root;
		
		private int resourceMask = 7;
		
		public Builder(RootUser usr, int xlen, int ylen) {
			this(usr, xlen, ylen, new Random2());
		}
		
		public Builder(RootUser usr, int xlen, int ylen, Random2 rnd) {
			if (xlen <= 0 || ylen <= 0) { throw new IllegalArgumentException("xlen=" + xlen + " ylen=" + ylen); }
			if (rnd == null) { throw new NullPointerException("rnd is null"); }
			if (usr == null) { throw new NullPointerException("usr is null"); }
			this.root  = usr;
			this.tiles = new Tile[xlen][ylen];
			this.rnd   = rnd;
		}
		
		private Builder(RootUser usr, Tile[][] tiles, Random2 rnd) {
			this.root  = usr;
			this.tiles = tiles;
			this.rnd   = rnd;
		}
		
		private void executeNTL() {
			for (Runnable r : this.nextTurnListeners) {
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
			for (int x = 0; x < this.tiles.length; x++) {
				for (int y = 0; y < this.tiles[x].length; y++) {
					if (this.tiles[x][y] != null && this.tiles[x][y].type != TileType.NOT_EXPLORED) {
						continue;
					}
					Tile xDown = null;
					Tile yDown = null;
					Tile xUp   = null;
					Tile yUp   = null;
					if (x > 0) { xDown = this.tiles[x - 1][y]; }
					if (y > 0) { yDown = this.tiles[x][y - 1]; }
					if (x < this.tiles.length - 1) { xUp = this.tiles[x + 1][y]; }
					if (y < this.tiles[x].length - 1) { yUp = this.tiles[x][y + 1]; }
					if (xDown != null || yDown != null || xUp != null || yUp != null) {
						this.tiles[x][y] = tile(xDown, xUp, yDown, yUp);
					} else {
						unfilledTile = true;
					}
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
			
			int posNone = (none * 5) + 2;
			int posGold = (gold * 4) + 1;
			int posIron = (iron * 3) + 2;
			int posCoal = (coal * 5) + 1;
			int rndVal  = this.rnd.nextInt(posNone + posGold + posIron + posCoal);
			
			OreResourceType ore = null;
			if (rndVal >= posNone) rndVal -= posNone;
			else ore = OreResourceType.NONE;
			if (rndVal >= posGold) rndVal -= posGold;
			else if (ore == null) ore = OreResourceType.GOLD_ORE;
			if (rndVal >= posIron) rndVal -= posIron;
			else if (ore == null) ore = OreResourceType.IRON_ORE;
			if (rndVal >= posCoal) {
				/**/} //
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
			if (ocean != 0 && land != 0) { return TileType.WATER_NORMAL; }
			int      water    = water(xDown) + water(xUp) + water(yDown) + water(yUp);
			int      flat     = flat(xDown) + flat(xUp) + flat(yDown) + flat(yUp);
			int      mountain = mountain(xDown) + mountain(xUp) + mountain(yDown) + mountain(yUp);
			TileType type     = rawType(xDown, xUp, yDown, yUp, ocean, water, flat, mountain);
			if (type.isLand() && !type.isMountain()) {
				int hill    = hill(xDown) + hill(xUp) + hill(yDown) + hill(yUp);
				int posHill = (mountain * 4) + (hill * 2) + 1;
				int posFlat = (flat * 2) + 1;
				int rndVal1 = this.rnd.nextInt(posHill + posFlat);
				if (rndVal1 < posHill) {
					type = type.addHill(true);
				}
			} else if (type.isWater()) {
				if (land == 0) {
					int posOcean  = ocean * 2 + 1;
					int posNormal = Math.max((water - ocean) * 2 + 1, 1);
					int rndVal1   = this.rnd.nextInt(posOcean + posNormal);
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
			int      posMountain = ocean == 0 ? Math.max(mountain - (flat * 2) + 1, 1) : 0;
			int      posSand     = ocean == 0 ? (sand * 2) + 1 : 0;
			int      posGrass    = ocean == 0 ? (grass * 2) + 1 : 0;
			int      posForest   = ocean == 0 ? (forest * 2) + 1 : 0;
			int      posSwamp    = ocean == 0 ? (swamp * 2) + 1 : 0;
			int      rndVal0     = this.rnd.nextInt(posWater + posMountain + posSand + posGrass + posForest + posSwamp);
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
			for (int x = 0; x < this.tiles.length; x += 8) {
				for (int y = x % 16 == 0 ? 0 : 4; y < this.tiles[x].length; y += 8) {
					if ((this.tiles[x][y] != null && this.tiles[x][y].type != TileType.NOT_EXPLORED) // do not place nearby other tiles and do not overwrite
						// tiles
						|| (x > 0 && (this.tiles[x - 1][y] != null && this.tiles[x - 1][y].type != TileType.NOT_EXPLORED))
						|| (y > 0 && (this.tiles[x][y - 1] != null && this.tiles[x][y - 1].type != TileType.NOT_EXPLORED))
						|| (x + 1 < this.tiles.length && (this.tiles[x + 1][y] != null && this.tiles[x + 1][y].type != TileType.NOT_EXPLORED))
						|| (y + 1 < this.tiles[x].length && (this.tiles[x][y + 1] != null && this.tiles[x][y + 1].type != TileType.NOT_EXPLORED))) {
						continue;
					}
					int             rndVal = this.rnd.nextInt(TYPES.length << 1);
					TileType        t      = rndVal >= TYPES.length ? TileType.WATER_DEEP : TYPES[rndVal];
					OreResourceType r      = OreResourceType.NONE;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						r = RES[this.rnd.nextInt(RES.length)];
					}
					this.tiles[x][y] = new Tile(t, r, true);
				}
			}
		}
		
		public void fillTotallyRandom() {
			for (Tile[] ts : this.tiles) {
				for (int i = 0; i < ts.length; i++) {
					if (ts[i] != null && ts[i].type != TileType.NOT_EXPLORED) {
						continue;
					}
					TileType        t = TYPES[this.rnd.nextInt(TYPES.length)]; // skip not explored
					OreResourceType r = OreResourceType.NONE;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						r = RES[this.rnd.nextInt(RES.length)];
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
			return this.root;
		}
		
		@Override
		public int xlen() {
			return this.tiles.length;
		}
		
		@Override
		public int ylen() {
			return this.tiles[0].length;
		}
		
		@Override
		public Tile tile(int x, int y) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(TileType.NOT_EXPLORED, OreResourceType.NONE, true);
			}
			return this.tiles[x][y];
		}
		
		/**
		 * returns the tile at the given position
		 * <p>
		 * the difference between {@link #tile(int, int)} and {@link #get(int, int)} is, that this method returns <code>null</code>, if the tile is not set, while
		 * {@link #tile(int, int)} creates a new non explored tile without resource in this case
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * 
		 * @return
		 */
		public Tile get(int x, int y) {
			return this.tiles[x][y];
		}
		
		public void set(int x, int y, TileType t) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(t, OreResourceType.NONE, true);
			} else {
				this.tiles[x][y] = new Tile(t, this.tiles[x][y].resource, true);
			}
			executeNTL();
		}
		
		public void set(int x, int y, OreResourceType r) {
			if (this.tiles[x][y] == null) {
				throw new NullPointerException("the tile does not already exist");
			} else {
				this.tiles[x][y] = new Tile(this.tiles[x][y].type, r, true);
			}
			executeNTL();
		}
		
		public void set(int x, int y, TileType t, OreResourceType r) {
			if (t == null) { throw new NullPointerException("type is null"); }
			if (t == TileType.NOT_EXPLORED) { throw new NullPointerException("NOT_EXPLORED is not allowed"); }
			if (r == null) { throw new NullPointerException("resource is null"); }
			this.tiles[x][y] = new Tile(t, r, true);
			executeNTL();
		}
		
		public void set(int x, int y, Tile t) {
			this.tiles[x][y] = t.copy();
			executeNTL();
		}
		
		public RootWorld create() throws IllegalStateException, NullPointerException {
			return create(this.root, this.tiles);
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles) throws IllegalStateException, NullPointerException {
			return create(root, tiles, null);
		}
		
		public boolean buildable() {
			for (int x = 0; x < this.tiles.length; x++) {
				Tile[] ts = this.tiles[x].clone();
				if (ts.length != this.tiles[0].length) { return false; }
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y];
					if (t == null) { return false; }
					if (t.type == null || t.resource == null) { return false; }
					if (t.type == TileType.NOT_EXPLORED) { return false; }
				}
			}
			return true;
		}
		
		public static RootWorld create(RootUser root, Tile[][] tiles, UserPlacer placer) throws IllegalStateException, NullPointerException {
			Tile[][] copy = new Tile[tiles.length][tiles[0].length];
			for (int x = 0; x < copy.length; x++) {
				Tile[] ts  = copy[x];
				Tile[] ots = tiles[x];
				if (ts.length != ots.length) { throw new IllegalStateException("the world has the have an rectangular form!"); }
				for (int y = 0; y < ts.length; y++) {
					Tile t = ots[y].copy();
					if (t == null) { throw new NullPointerException("no tile is allowed to be null"); }
					if (t.type == null || t.resource == null) { throw new NullPointerException("a tile has a null type/resource"); }
					if (t.type == TileType.NOT_EXPLORED) { throw new IllegalStateException("a tile with type NOT_EXPLORED was found"); }
					ts[y] = t;
				}
			}
			return new RootWorld(root, copy, placer);
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles) {
			return createBuilder(root, tiles, new Random2());
		}
		
		public static Builder createBuilder(RootUser root, Tile[][] tiles, Random2 rnd) {
			Tile[][] copy = tiles.clone(); // do clone, so the rectangular form can not be destroyed
			int      ylen = copy[0].length;
			for (int x = 0; x < copy.length; x++) { // only enforce the rectangular form, the builder is allowed to contain invalid tiles
				if (copy[x].length != ylen) throw new IllegalStateException("the world has the have an rectangular form!");
			}
			return new Builder(root, tiles, rnd);
		}
		
		@Override
		public void addNextTurnListener(Runnable listener) { this.nextTurnListeners.add(listener); }
		
		@Override
		public void removeNextTurnListener(Runnable listener) { this.nextTurnListeners.remove(listener); }
		
		@Override
		public Map<User, List<Entity>> entities() {
			return RootWorld.entities(this.tiles);
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
