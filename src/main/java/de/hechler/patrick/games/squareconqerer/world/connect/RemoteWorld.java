package de.hechler.patrick.games.squareconqerer.world.connect;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.RemoteTile;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;

public class RemoteWorld implements World, Closeable {
	
	private final Connection conn;
	private int              xlen;
	private int              ylen;
	private RemoteTile[][]   tiles;
	private long             needUpdate;
	private boolean          getWorld = true;
	
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
			RemoteTile[][] t = readWorld(conn, this.tiles, true);
			if (t != this.tiles) {
				this.tiles = t;
				this.xlen  = t.length;
				this.ylen  = t[0].length;
			}
		} finally {
			unblock();
		}
	}
	
	public static Tile[][] readWorld(Connection c) throws IOException {
		return readWorld(c, null, false);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Tile> T[][] readWorld(Connection conn, T[][] tiles, boolean remote) throws IOException {
		if (remote) {
			conn.writeInt(OpenWorld.CMD_GET_WORLD);
		} else {
			conn.readInt(OpenWorld.CMD_GET_WORLD);
		}
		int xlen = conn.readInt();
		int ylen = conn.readInt();
		if (tiles == null) {
			tiles = (T[][]) (remote ? new RemoteTile[xlen][ylen] : new Tile[xlen][ylen]);
		} else if (xlen != tiles.length || ylen != tiles[0].length) {
			System.err.println("[RemoteWorld]: WARN: world size changed (old xlen=" + tiles.length + " ylen=" + tiles[0].length + " new xlen=" + xlen
					+ " ylen=" + ylen + ')');
			tiles = (T[][]) (remote ? new RemoteTile[xlen][ylen] : new Tile[xlen][ylen]);
		}
		long time = System.currentTimeMillis();
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				int             tto = conn.readInt();
				int             rto = conn.readInt();
				TileType        tt  = TileType.of(tto);
				OreResourceType rt  = OreResourceType.of(rto);
				tiles[x][y] = (T) (remote ? new RemoteTile(time, tt, rt) : new Tile(tt, rt));
			}
			conn.readInt(OpenWorld.SUB_GET_WORLD_0);
		}
		conn.readInt(OpenWorld.SUB_GET_WORLD_1);
		conn.readInt(0);
		conn.readInt(0);
		conn.readInt(OpenWorld.SUB_GET_WORLD_2);
		conn.readInt(OpenWorld.SUB_GET_WORLD_3);
		if (remote) {
			conn.writeInt(OpenWorld.FIN_GET_WORLD);
		}
		return tiles;
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
			tiles[x][y] = new RemoteTile(tt, rt);
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
	public void close() throws IOException {
		conn.close();
	}
	
}
