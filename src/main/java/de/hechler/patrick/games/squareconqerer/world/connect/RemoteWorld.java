package de.hechler.patrick.games.squareconqerer.world.connect;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.RemoteTile;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;
import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public class RemoteWorld implements World, Closeable {
	
	private final Connection        conn;
	private int                     xlen;
	private int                     ylen;
	private RemoteTile[][]          tiles;
	private long                    needUpdate;
	private boolean                 getWorld = true;
	private long                    lastWorldUpdate;
	private Map<User, List<Entity>> entities;
	private Map<String, User>       users;
	
	public RemoteWorld(Connection conn) {
		this.conn = conn;
		threadBuilder().start(this::deamon);
	}
	
	/**
	 * if the entire world should be updated, when an tile is out-dated
	 * <p>
	 * by default getWorld is <code>true</code><br>
	 * if {@link #needUpdate()} is often called, this value might be set to
	 * <code>false</code>
	 * 
	 * @param getWorld the new value of {@link #getWorld}
	 */
	public void getWorld(boolean getWorld) { this.getWorld = getWorld; }
	
	public boolean getWorld() { return this.getWorld; }
	
	public void needUpdate() { this.needUpdate = System.currentTimeMillis(); }
	
	@Override
	public User user() {
		return conn.usr;
	}
	
	@Override
	public int xlen() {
		if (xlen == 0) {
			try {
				updateWorldSize();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return xlen;
	}
	
	/**
	 * this method should not be needed, because the world size (should be/)is
	 * immutable
	 * 
	 * @throws IOException
	 */
	public synchronized void updateWorldSize() throws IOException {
		try {
			block(0);
			conn.writeInt(OpenWorld.CMD_GET_SIZE);
			xlen       = conn.readInt();
			ylen       = conn.readInt();
			this.tiles = new RemoteTile[xlen][ylen];
		} finally {
			unblock();
		}
	}
	
	@Override
	public int ylen() {
		if (ylen == 0) {
			xlen();
		}
		return ylen;
	}
	
	public boolean loadedBounds() {
		return xlen != 0;
	}
	
	@Override
	public Tile tile(int x, int y) {
		if (x < 0 || y < 0) {
			throw new IndexOutOfBoundsException("negative coordinate: x=" + x + " y=" + y);
		}
		if (x >= xlen() || y >= ylen) {
			throw new IndexOutOfBoundsException("x=" + x + " y=" + y + " xlen=" + xlen + " ylen=" + ylen);
		}
		try {
			if (tiles[x][y] == null || tiles[x][y].created <= needUpdate) {
				if (getWorld) {
					updateWorld();
				} else {
					updateSingleTile(x, y);
				}
			}
			return tiles[x][y];
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	public synchronized void updateWorld() throws IOException {
		try {
			block(0);
			lastWorldUpdate = System.currentTimeMillis();
			if (entities == null) {
				entities = new HashMap<>();
				users    = new HashMap<>();
				users.put(conn.usr.name(), conn.usr);
			} else {
				entities.clear();
			}
			RemoteTile[][] t = readWorld(conn, this.tiles, lastWorldUpdate, entities, users);
			if (t != this.tiles) {
				this.tiles = t;
				this.xlen  = t.length;
				this.ylen  = t[0].length;
			}
			Map<User, List<Entity>> unmodCopy = new HashMap<>(entities.size());
			for (Entry<User, List<Entity>> entry : entities.entrySet()) {
				User         key = entry.getKey();
				List<Entity> val = entry.getValue();
				unmodCopy.put(key, Collections.unmodifiableList(val));
			}
			entities = Collections.unmodifiableMap(unmodCopy);
		} finally {
			unblock();
		}
	}
	
	public static Tile[][] loadWorld(Connection c, Map<String, User> users) throws IOException {
		return readWorld(c, null, -1L, null, users);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Tile> T[][] readWorld(Connection conn, T[][] tiles, long timestamp, Map<User, List<Entity>> entities,
			Map<String, User> users) throws IOException {
		if (entities != null) {
			conn.writeInt(OpenWorld.CMD_GET_WORLD);
		} else {
			conn.readInt(OpenWorld.CMD_GET_WORLD);
		}
		int xlen = conn.readInt();
		int ylen = conn.readInt();
		if (tiles == null) {
			tiles = (T[][]) (entities != null ? new RemoteTile[xlen][ylen] : new Tile[xlen][ylen]);
		} else if (xlen != tiles.length || ylen != tiles[0].length) {
			System.err.println("[RemoteWorld]: WARN: world size changed (old xlen=" + tiles.length + " ylen=" + tiles[0].length + " new xlen=" + xlen
					+ " ylen=" + ylen + ')');
			tiles = (T[][]) (entities != null ? new RemoteTile[xlen][ylen] : new Tile[xlen][ylen]);
		}
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				int             tto = conn.readInt();
				int             rto = conn.readInt();
				TileType        tt  = TileType.of(tto);
				OreResourceType rt  = OreResourceType.of(rto);
				boolean         v   = conn.readByte(0, 1) != 0;
				tiles[x][y] = (T) (entities != null ? new RemoteTile(timestamp, tt, rt, v) : new Tile(tt, rt, v));
			}
			conn.readInt(OpenWorld.SUB_GET_WORLD_0);
		}
		conn.readInt(OpenWorld.SUB_GET_WORLD_1);
		int players = conn.readInt();
		while (players-- > 0) {
			String username = conn.readString();
			User   usr      = entities == null ? users.get(username) : users.computeIfAbsent(username, User::nopw);
			if (usr == null) {
				throw new IllegalStateException("got an unknown username (not in map: '" + username + "')");
			}
			List<Entity> list = entities.computeIfAbsent(usr, u -> new ArrayList<>());
			if (conn.readInt(null, OpenWorld.CMD_UNIT, OpenWorld.CMD_BUILD) == OpenWorld.CMD_UNIT) {
				list.add(readUnit(conn, usr));
			} else {
				list.add(readBuilding(conn, usr));
			}
			conn.readInt(OpenWorld.SUB_GET_WORLD_2);
		}
		conn.readInt(OpenWorld.SUB_GET_WORLD_3);
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
		if (conn.readByte(0, 1) != 0) {
			remainTurns = conn.readInt();
			int len = conn.readInt(ProducableResourceType.count(), 0);
			if (len != 0) {
				neededBuildRes = new EnumIntMap<>(ProducableResourceType.class);
				int[] arr = neededBuildRes.array();
				for (int i = 0; i < arr.length; i++) {
					arr[i] = conn.readInt();
					if (arr[i] < 0) {
						throw new IOException("read an negative amount for a needed resource");
					}
				}
			}
		}
		conn.readInt(StoreBuild.NUMBER);
		Resource storedRes   = readRes(conn);
		int      storeAmount = conn.readInt();
		if (storeAmount < 0) {
			throw new IOException("read an negative amount for the store amount");
		}
		conn.readInt(OpenWorld.FIN_ENTITY);
		return new StoreBuild(x, y, usr, lives, neededBuildRes, remainTurns, storedRes, storeAmount);
	}
	
	public synchronized void updateSingleTile(int x, int y) throws IOException {
		try {
			block(0);
			conn.writeInt(OpenWorld.CMD_GET_TILE);
			conn.writeInt(x);
			conn.writeInt(y);
			conn.readInt(OpenWorld.GET_TILE_NO_UNIT_NO_BUILD);
			int             typeOrid = conn.readInt();
			int             resOrid  = conn.readInt();
			TileType        tt       = TileType.of(typeOrid);
			OreResourceType rt       = OreResourceType.of(resOrid);
			boolean         v        = conn.readByte(0, 1) != 0;
			tiles[x][y] = new RemoteTile(tt, rt, v);
		} finally {
			unblock();
		}
	}
	
	private volatile Thread busy;
	
	private void unblock() {
		synchronized (this) {
			if (busy != Thread.currentThread()) {
				throw new AssertionError("I am not the busy thread");
			}
			busy = null;
			notifyAll();
		}
	}
	
	private void block(int timeout) {
		synchronized (this) {
			while (busy != null) {
				if (busy == Thread.currentThread()) {
					throw new AssertionError("deadlock detected");
				}
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			busy = Thread.currentThread();
		}
		conn.setTimeout(timeout);
	}
	
	private List<Runnable> nextTurnListeners = new LinkedList<>();
	
	private void deamon() {
		while (true) {
			try {
				block(250);
				long val = conn.readInt0();
				if (val == -1L) {
					return;
				}
				switch ((int) val) {
				case OpenWorld.NOTIFY_NEXT_TURN -> {
					needUpdate = System.currentTimeMillis();
					for (Runnable r : nextTurnListeners) {
						r.run();
					}
				}
				case -1 -> {
					System.err.println("got an invalid notification: 0xFFFFFFFF");
					conn.close();
				}
				default -> {
					System.err.println("got an invalid notification: 0x" + Integer.toHexString((int) val));
					conn.close();
				}
				}
			} catch (SocketTimeoutException e) {//
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				unblock();
			}
			try {
				Thread.sleep(0L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void addNextTurnListener(Runnable listener) {
		try {
			block(0);
			nextTurnListeners.add(listener);
		} finally {
			unblock();
		}
	}
	
	@Override
	public void removeNextTurnListener(Runnable listener) {
		try {
			block(0);
			nextTurnListeners.remove(listener);
		} finally {
			unblock();
		}
	}
	
	@Override
	public Map<User, List<Entity>> entities() {
		if (needUpdate >= lastWorldUpdate) {
			try {
				updateWorld();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return entities;
	}
	
	@Override
	public void close() throws IOException {
		conn.close();
	}
	
	@Override
	public void finish(Turn t) {
		if (t.usr != conn.usr) {
			throw new IllegalStateException("I can only finish my turns");
		}
		try {
			t.sendTurn(conn);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
}
