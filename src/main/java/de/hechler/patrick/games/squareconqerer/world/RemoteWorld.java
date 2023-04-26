package de.hechler.patrick.games.squareconqerer.world;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.connect.WrongInputHandler;
import de.hechler.patrick.games.squareconqerer.objects.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.RemoteTile;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public final class RemoteWorld implements WrongInputHandler, World, Closeable {
	
	private final Connection        conn;
	private int                     xlen;
	private int                     ylen;
	private RemoteTile[][]          tiles;
	private long                    needUpdate;
	private boolean                 getWorld = true;
	private long                    lastWorldUpdate;
	private Map<User, List<Entity>> entities;
	private Map<String, User>       users;
	private byte[]                  myrnd;
	
	public RemoteWorld(Connection conn) {
		this.conn = conn;
		threadBuilder().start(this::deamon);
	}
	
	/**
	 * if the entire world should be updated, when an tile is out-dated
	 * <p>
	 * by default getWorld is <code>true</code><br>
	 * if {@link #needUpdate()} is often called, this value might be set to <code>false</code>
	 * 
	 * @param getWorld the new value of {@link #this.getWorld}
	 */
	public void getWorld(boolean getWorld) { this.getWorld = getWorld; }
	
	public boolean getWorld() { return this.getWorld; }
	
	public void needUpdate() { this.needUpdate = System.currentTimeMillis(); }
	
	@Override
	public User user() {
		return this.conn.usr;
	}
	
	@Override
	public int xlen() {
		if (this.xlen == 0) {
			try {
				updateWorldSize();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return this.xlen;
	}
	
	/**
	 * this method should not be needed, because the world size (should be/)is immutable
	 * 
	 * @throws IOException
	 */
	public synchronized void updateWorldSize() throws IOException {
		this.conn.blocked(() -> {
			this.conn.writeReadInt(OpenWorld.CMD_GET_SIZE, OpenWorld.SUB0_GET_SIZE);
			this.xlen  = this.conn.readInt();
			this.ylen  = this.conn.readInt();
			this.tiles = new RemoteTile[this.xlen][this.ylen];
		});
	}
	
	@Override
	public int ylen() {
		if (this.ylen == 0) {
			xlen();
		}
		return this.ylen;
	}
	
	public boolean loadedBounds() {
		return this.xlen != 0;
	}
	
	@Override
	public Tile tile(int x, int y) {
		if (x < 0 || y < 0) { throw new IndexOutOfBoundsException("negative coordinate: x=" + x + " y=" + y); }
		if (x >= xlen() || y >= this.ylen) { throw new IndexOutOfBoundsException("x=" + x + " y=" + y + " xlen=" + this.xlen + " ylen=" + this.ylen); }
		try {
			if (this.tiles[x][y] == null || this.tiles[x][y].created <= this.needUpdate) {
				if (this.getWorld) {
					updateWorld();
				} else {
					updateSingleTile(x, y);
				}
			}
			return this.tiles[x][y];
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	public synchronized void updateWorld() throws IOException {
		this.conn.blocked(() -> {
			this.lastWorldUpdate = System.currentTimeMillis();
			if (this.entities == null) {
				this.entities = new HashMap<>();
				this.users    = new HashMap<>();
				this.users.put(this.conn.usr.name(), this.conn.usr);
			} else {
				this.entities.clear();
			}
			RemoteTile[][] t = readWorld(this.conn, this.tiles, this.lastWorldUpdate, this.entities, this.users);
			if (t != this.tiles) {
				this.tiles = t;
				this.xlen  = t.length;
				this.ylen  = t[0].length;
			}
			Map<User, List<Entity>> unmodCopy = new HashMap<>(this.entities.size());
			for (Entry<User, List<Entity>> entry : this.entities.entrySet()) {
				User         key = entry.getKey();
				List<Entity> val = entry.getValue();
				unmodCopy.put(key, Collections.unmodifiableList(val));
			}
			this.entities = Collections.unmodifiableMap(unmodCopy);
		});
	}
	
	/*
	 * on race condition input, the client has priority, its request gets executed, than the server can send its request again
	 */
	
	@Override
	public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
		this.conn.readInt(expectedRead);
	}
	
	public static Tile[][] loadWorld(Connection c, Map<String, User> users) throws IOException {
		return readWorld(c, null, -1L, null, users);
	}
	
	public static void loadWorld(Connection c, Map<String, User> users, Tile[][] tiles) throws IOException {
		readWorld(c, tiles, -1L, null, users);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Tile> T[][] readWorld(Connection conn, T[][] tiles, long timestamp, Map<User, List<Entity>> entities, Map<String, User> users)
		throws IOException {
		if (entities != null) {
			conn.writeReadInt(OpenWorld.CMD_GET_WORLD, OpenWorld.SUB0_GET_WORLD);
		} else {
			conn.readInt(OpenWorld.CMD_GET_WORLD);
		}
		int xlen = conn.readInt();
		int ylen = conn.readInt();
		if (tiles == null) {
			tiles = (T[][]) (entities != null ? new RemoteTile[xlen][ylen] : new Tile[xlen][ylen]);
		} else if (xlen != tiles.length || ylen != tiles[0].length) {
			if (entities == null) throw new IllegalArgumentException("different world sizes");
			System.err.println(
				"[RemoteWorld]: WARN: world size changed (old xlen=" + tiles.length + " ylen=" + tiles[0].length + " new xlen=" + xlen + " ylen=" + ylen + ')');
			tiles = (T[][]) (new RemoteTile[xlen][ylen]);
		}
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				if (tiles[x].length != ylen) throw new IllegalArgumentException("world has no rectangular form!");
				int             tto = conn.readInt();
				int             rto = conn.readInt();
				TileType        tt  = TileType.of(tto);
				OreResourceType rt  = OreResourceType.of(rto);
				boolean         v   = conn.readByte(0, 1) != 0;
				tiles[x][y] = (T) (entities != null ? new RemoteTile(timestamp, tt, rt, v) : new Tile(tt, rt, v));
			}
			conn.readInt(OpenWorld.SUB1_GET_WORLD);
		}
		conn.readInt(OpenWorld.SUB2_GET_WORLD);
		int players = conn.readInt();
		while (players-- > 0) {
			String username = conn.readString();
			User   usr      = entities == null ? users.get(username) : users.computeIfAbsent(username, User::nopw);
			if (usr == null) { throw new IllegalStateException("got an unknown username (not in map: '" + username + "')"); }
			List<Entity> list = entities == null ? null : entities.computeIfAbsent(usr, u -> new ArrayList<>());
			for (int i = conn.readInt(); i > 0; i--) {
				List<Entity> l = list;
				if (conn.readInt(null, OpenWorld.CMD_UNIT, OpenWorld.CMD_BUILD) == OpenWorld.CMD_UNIT) {
					Unit u = readUnit(conn, usr);
					tiles[u.x()][u.y()].unit(u);
					if (entities != null) l.add(u);
				} else {
					Building b = readBuilding(conn, usr);
					tiles[b.x()][b.y()].build(b);
					if (entities != null) l.add(b);
				}
			}
			conn.readInt(OpenWorld.SUB3_GET_WORLD);
		}
		conn.readInt(OpenWorld.SUB4_GET_WORLD);
		if (entities != null) {
			conn.writeInt(OpenWorld.FIN_GET_WORLD);
		}
		return tiles;
	}
	
	private static Unit readUnit(Connection conn, User usr) throws IOException {
		int      x     = conn.readInt();
		int      y     = conn.readInt();
		int      lives = conn.readInt();
		int      ca    = conn.readInt();
		Resource res   = null;
		if (ca != 0) {
			res = readRes(conn);
		}
		conn.readInt(Carrier.NUMBER);
		conn.readInt(OpenWorld.FIN_ENTITY);
		return new Carrier(x, y, usr, lives, res, ca);
	}
	
	public static Resource readRes(Connection conn) throws IOException {
		return switch (conn.readInt(ProducableResourceType.NUMBER, OreResourceType.NUMBER)) {
		case ProducableResourceType.NUMBER -> ProducableResourceType.of(conn.readInt());
		case OreResourceType.NUMBER -> OreResourceType.of(conn.readInt());
		default -> throw new AssertionError("invalid return type of conn.readInt(int,int)");
		};
	}
	
	private static Building readBuilding(Connection conn, User usr) throws IOException {
		int                                x              = conn.readInt();
		int                                y              = conn.readInt();
		int                                lives          = conn.readInt();
		int                                remainTurns    = 0;
		EnumIntMap<ProducableResourceType> neededBuildRes = null;
		boolean                            fb             = conn.readByte(0, 1) != 0;
		if (fb) {
			remainTurns = conn.readInt();
			int len = conn.readInt(ProducableResourceType.count(), 0);
			if (len != 0) {
				neededBuildRes = new EnumIntMap<>(ProducableResourceType.class);
				int[] arr = neededBuildRes.array();
				for (int i = 0; i < arr.length; i++) {
					arr[i] = conn.readPos();
				}
			}
		}
		conn.readInt(StoreBuild.NUMBER);
		EnumIntMap<OreResourceType>        ores       = new EnumIntMap<>(OreResourceType.class);
		EnumIntMap<ProducableResourceType> producable = new EnumIntMap<>(ProducableResourceType.class);
		if (fb) {
			int[] oa = ores.array();
			conn.readInt(oa.length);
			for (int i = 0; i < oa.length; i++) {
				oa[i] = conn.readInt();
			}
			int[] pa = producable.array();
			conn.readInt(pa.length);
			for (int i = 0; i < pa.length; i++) {
				pa[i] = conn.readInt();
			}
		}
		conn.readInt(OpenWorld.FIN_ENTITY);
		return new StoreBuild(x, y, usr, lives, neededBuildRes, remainTurns, ores, producable);
	}
	
	public synchronized void updateSingleTile(int x, int y) throws IOException {
		this.conn.blocked(() -> {
			this.conn.writeReadInt(OpenWorld.CMD_GET_TILE, OpenWorld.SUB0_GET_TILE);
			this.conn.writeInt(x);
			this.conn.writeInt(y);
			boolean unit;
			boolean build;
			switch (this.conn.readInt(OpenWorld.GET_TILE_NO_UNIT_NO_BUILD, OpenWorld.GET_TILE_NO_UNIT_YES_BUILD, OpenWorld.GET_TILE_YES_UNIT_NO_BUILD,
				OpenWorld.GET_TILE_YES_UNIT_YES_BUILD)) {
			case OpenWorld.GET_TILE_NO_UNIT_NO_BUILD -> {
				unit  = false;
				build = false;
			}
			case OpenWorld.GET_TILE_NO_UNIT_YES_BUILD -> {
				unit  = false;
				build = true;
			}
			case OpenWorld.GET_TILE_YES_UNIT_NO_BUILD -> {
				unit  = true;
				build = false;
			}
			case OpenWorld.GET_TILE_YES_UNIT_YES_BUILD -> {
				unit  = true;
				build = true;
			}
			default -> throw new AssertionError("illegal return value from readInt(int...)");
			}
			Unit     u = null;
			Building b = null;
			if (unit) {
				User owner = this.users.computeIfAbsent(this.conn.readString(), User::nopw);
				u = readUnit(this.conn, owner);
			}
			if (build) {
				User owner = this.users.computeIfAbsent(this.conn.readString(), User::nopw);
				b = readBuilding(this.conn, owner);
			}
			int             typeOrid = this.conn.readInt();
			int             resOrid  = this.conn.readInt();
			TileType        tt       = TileType.of(typeOrid);
			OreResourceType rt       = OreResourceType.of(resOrid);
			boolean         v        = this.conn.readByte(0, 1) != 0;
			RemoteTile      t        = new RemoteTile(tt, rt, v);
			if (unit) t.unit(u);
			if (build) t.build(b);
			this.tiles[x][y] = t;
		});
	}
	
	private List<BiConsumer<byte[], byte[]>> nextTurnListeners = new LinkedList<>();
	
	private void deamon() {
		while (true) {
			try {
				this.conn.blocked(250, () -> {
					long val = this.conn.readInt0();
					if (val == -1L) { return; }
					int val0 = (int) val;
					switch (val0) {
					case RootWorld.REQ_RND -> {
						if (this.myrnd != null) throw new AssertionError("I already have a random value!");
						this.conn.writeInt(RootWorld.GIV_RND);
						this.myrnd = new byte[16];
						User.fillRandom(this.myrnd);
						this.conn.writeArr(this.myrnd);
					}
					case OpenWorld.NOTIFY_WORLD_CHANGE -> {
						this.conn.writeInt(OpenWorld.FIN_WORLD_CHANGE);
						this.needUpdate = System.currentTimeMillis();
						for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
							r.accept(null, null);
						}
					}
					case OpenWorld.NOTIFY_GAME_START -> {
						this.conn.writeInt(OpenWorld.SUB0_GAME_START);
						byte[] worldhash = new byte[256 / 8];
						this.conn.readArr(worldhash);
						this.conn.readInt(OpenWorld.FIN_GAME_START);
						this.needUpdate = System.currentTimeMillis();
						for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
							r.accept(worldhash, null);
						}
					}
					case OpenWorld.NOTIFY_NEXT_TURN -> {
						this.conn.writeInt(OpenWorld.SUB0_NEXT_TURN);
						byte[] worldhash = new byte[256 / 8];
						this.conn.readArr(worldhash);
						this.conn.readInt(OpenWorld.SUB1_NEXT_TURN);
						byte[] turndhash = new byte[256 / 8];
						this.conn.readArr(turndhash);
						this.conn.readInt(OpenWorld.FIN_NEXT_TURN);
						this.needUpdate = System.currentTimeMillis();
						for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
							r.accept(worldhash, null);
						}
					}
					default -> {
						System.err.println("got an invalid notification: 0x" + Integer.toHexString(val0));
						this.conn.close();
					}
					}
				}, Connection.NOP);
				Thread.sleep(10L);
			} catch (IOException | InterruptedException e) {
				if (this.conn.closed()) { return; }
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void addNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.conn.blocked(() -> this.nextTurnListeners.add(listener));
	}
	
	@Override
	public void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.conn.blocked(() -> this.nextTurnListeners.remove(listener));
	}
	
	@Override
	public Map<User, List<Entity>> entities() {
		if (this.needUpdate >= this.lastWorldUpdate) {
			try {
				updateWorld();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return this.entities;
	}
	
	@Override
	public void close() throws IOException {
		this.conn.close();
	}
	
	@Override
	public void finish(Turn t) {
		if (t.usr != this.conn.usr) { throw new IllegalStateException("I can only finish my turns"); }
		try {
			t.sendTurn(this.conn, true);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
}
