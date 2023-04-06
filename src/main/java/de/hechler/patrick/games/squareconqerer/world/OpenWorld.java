package de.hechler.patrick.games.squareconqerer.world;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.interfaces.Executable;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public class OpenWorld implements Executable<IOException> {
	
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_SIZE}</li>
	 * <li>Server: xlen</li>
	 * <li>Server: ylen</li>
	 * </ol>
	 */
	static final int CMD_GET_SIZE                = 0xF14BC0F5;
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_TILE}</li>
	 * <li>Client: x coordinate</li>
	 * <li>Client: y coordinate</li>
	 * <li>is there an entity, is there a building?
	 * <ul>
	 * <li>entity: no , building: no
	 * <ol>
	 * <li>Server: {@link #GET_TILE_NO_UNIT_NO_BUILD}</li>
	 * </ol>
	 * </li>
	 * <li>entity: yes , building: no
	 * <ol>
	 * <li>Server: {@link #GET_TILE_YES_UNIT_NO_BUILD}</li>
	 * </ol>
	 * </li>
	 * <li>entity: no , building: yes
	 * <ol>
	 * <li>Server: {@link #GET_TILE_NO_UNIT_YES_BUILD}</li>
	 * </ol>
	 * </li>
	 * <li>entity: yes , building: yes
	 * <ol>
	 * <li>Server: {@link #GET_TILE_YES_UNIT_YES_BUILD}</li>
	 * </ol>
	 * </li>
	 * </ul>
	 * </li>
	 * <li>if there is an unit:
	 * <ol>
	 * <li>write the name of the owner {@link Connection#writeString(String)}
	 * ({@link RootUser#ROOT_NAME} if there is no owner)</li>
	 * <li>{@link #sendUnit(Unit, Connection)}</li>
	 * </ol>
	 * </li>
	 * <li>if there is an building:
	 * <ol>
	 * <li>write the name of the owner {@link Connection#writeString(String)}
	 * ({@link RootUser#ROOT_NAME} if there is no owner)</li>
	 * <li>{@link #sendBuilding(Building, Connection)}</li>
	 * </ol>
	 * </li>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.type.oridinal</code>
	 * </li>
	 * <li>Server:
	 * <code>{@link World#tile(int, int) tile(x,y)}.resource.oridinal</code></li>
	 * <li>Server: visible ({@code 0}: not visible ; {@code 1} visible)</li>
	 * </ol>
	 */
	static final int CMD_GET_TILE                = 0xB62FD4F7;
	static final int GET_TILE_YES_UNIT_YES_BUILD = 0x0CB0A7C8;
	static final int GET_TILE_NO_UNIT_YES_BUILD  = 0x994024A5;
	static final int GET_TILE_YES_UNIT_NO_BUILD  = 0x6741243A;
	static final int GET_TILE_NO_UNIT_NO_BUILD   = 0x623E9F1A;
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_WORLD}</li>
	 * <li>Server: xlen</li>
	 * <li>Server: ylen</li>
	 * <li><code> for (x = 0 ; x < xlen ; x++) </code>
	 * <ol>
	 * <li><code> for (y = 0 ; y < ylen ; y++) </code>
	 * <ol>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.type.oridinal</code>
	 * </li>
	 * <li>Server:
	 * <code>{@link World#tile(int, int) tile(x,y)}.resource.oridinal</code></li>
	 * <li>Server: visible ({@code 0}: not visible ; {@code 1}: visible)</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_0}</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_1}</li>
	 * <li>Server: player count</li>
	 * <li><code> for (i = 0 ; i < player count ; i ++) </code>
	 * <ol>
	 * <li>Server: sends the players name {@link Connection#writeString(String)}
	 * </li>
	 * <li>Server: entity count of the player</li>
	 * <li><code> for (i = 0 ; i < entity count ; i ++) </code>
	 * <ul>
	 * <li>if entity is unit
	 * <ol>
	 * <li>Server: {@link #CMD_UNIT}</li>
	 * <li>Server: {@link #sendUnit(Unit, Connection)}</li>
	 * </ol>
	 * </li>
	 * <li>if entity is building
	 * <ol>
	 * <li>Server: {@link #CMD_BUILD}</li>
	 * <li>Server: {@link #sendBuilding(Building, Connection)}</li>
	 * </ol>
	 * </li>
	 * </ul>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_2}</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_3}</li>
	 * <li>Client: {@link #FIN_GET_WORLD}</li>
	 * </ol>
	 */
	static final int CMD_GET_WORLD               = 0xC4F640DE;
	static final int SUB_GET_WORLD_0             = 0xFA48B370;
	static final int SUB_GET_WORLD_1             = 0xFA48B370;
	static final int SUB_GET_WORLD_2             = 0x67E14E35;
	static final int SUB_GET_WORLD_3             = 0xA9250E35;
	static final int FIN_GET_WORLD               = 0x030F4D21;
	
	static final int CMD_UNIT   = 0x9D527756;
	static final int CMD_BUILD  = 0x1112CD5A;
	static final int FIN_ENTITY = 0x5A96A583;
	
	static final int NOTIFY_NEXT_TURN = 0xE30212DB;
	
	private final Connection conn;
	private final World      world;
	
	public OpenWorld(Connection conn, World world) {
		this.conn  = conn;
		this.world = world;
	}
	
	private volatile Thread busy;
	
	private void nextTurn() {
		try {
			block(250);
			try {
				conn.writeInt(NOTIFY_NEXT_TURN);
			} catch (SocketTimeoutException e) {//
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			unblock();
		}
	}
	
	@Override
	public void execute() throws IOException {
		conn.setTimeout(250);
		Runnable ntl = this::nextTurn;
		world.addNextTurnListener(ntl);
		try {
			while (true) {
				singleExecute();
			}
		} finally {
			world.removeNextTurnListener(ntl);
			conn.close();
		}
	}
	
	private void singleExecute() throws IOException {
		try {
			block(250);
			long val0 = conn.readInt0();
			if (val0 == -1L) {
				return;
			}
			int val = (int) val0;
			conn.setTimeout(0);
			switch (val) {
			case CMD_GET_SIZE -> {
				conn.writeInt(world.xlen());
				conn.writeInt(world.ylen());
			}
			case CMD_GET_TILE -> sendTile();
			case CMD_GET_WORLD -> sendWorld(world, conn, true);
			case Turn.CMD_TURN -> {
				Turn t = new Turn(world);
				t.retrieveTurn(conn);
				world.finish(t);
			}
			default -> throw new IOException("read invalid data (username: '" + world.user().name() + "')");
			}
		} catch (SocketTimeoutException e) {//
		} finally {
			unblock();
		}
	}
	
	private void sendTile() throws IOException {
		int  x = conn.readInt();
		int  y = conn.readInt();
		Tile t;
		try {
			t = world.tile(x, y);
		} catch (IndexOutOfBoundsException e) {
			conn.writeInt(-1);
			return;
		}
		Unit     u = t.unit();
		Building b = t.building();
		if (u == null && b == null) {
			conn.writeInt(GET_TILE_NO_UNIT_NO_BUILD);
		} else if (u == null) {
			conn.writeInt(GET_TILE_NO_UNIT_YES_BUILD);
		} else if (b == null) {
			conn.writeInt(GET_TILE_YES_UNIT_NO_BUILD);
		} else {
			conn.writeInt(GET_TILE_YES_UNIT_YES_BUILD);
		}
		if (u != null) {
			User usr = u.owner();
			conn.writeString(usr != null ? usr.name() : RootUser.ROOT_NAME);
			sendUnit(u, conn);
		}
		if (b != null) {
			User usr = b.owner();
			conn.writeString(usr != null ? usr.name() : RootUser.ROOT_NAME);
			sendBuilding(b, conn);
		}
		conn.writeInt(t.type.ordinal());
		conn.writeInt(t.resource.ordinal());
		conn.writeByte(t.visible() ? 1 : 0);
	}
	
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
	
	public static void saveWorld(World world, Connection conn) throws IOException {
		sendWorld(world, conn, false);
	}
	
	private static void sendWorld(World world, Connection conn, boolean alsoRead) throws IOException {
		if (!alsoRead) {
			conn.writeInt(OpenWorld.CMD_GET_WORLD);
		}
		int xlen = world.xlen();
		int ylen = world.ylen();
		conn.writeInt(xlen);
		conn.writeInt(ylen);
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				Tile t = world.tile(x, y);
				conn.writeInt(t.type.ordinal());
				conn.writeInt(t.resource.ordinal());
				conn.writeByte(t.visible() ? 1 : 0);
			}
			conn.writeInt(SUB_GET_WORLD_0);
		}
		conn.writeInt(SUB_GET_WORLD_1);
		Map<User, List<Entity>> entities = world.entities();
		conn.writeInt(entities.size());
		for (Map.Entry<User, List<Entity>> entry : entities.entrySet()) {
			User         usr  = entry.getKey();
			List<Entity> list = entry.getValue();
			final int    s    = list.size();
			conn.writeString(usr.name());
			conn.writeInt(s);
			for (int i = 0; i < s; i++) {
				Entity e = list.get(i);
				if (e instanceof Unit u) {
					conn.writeInt(CMD_UNIT);
					sendUnit(u, conn);
				} else if (e instanceof Building b) {
					conn.writeInt(CMD_BUILD);
					sendBuilding(b, conn);
				} else {
					throw new AssertionError("the entity is neither an unit nor a building: " + (e == null ? "null" : e.getClass()));
				}
			}
			conn.writeInt(SUB_GET_WORLD_2);
		}
		conn.writeInt(SUB_GET_WORLD_3);
		if (alsoRead) {
			conn.readInt(FIN_GET_WORLD);
		}
	}
	
	private static void sendUnit(Unit u, Connection conn) throws IOException {
		conn.writeInt(u.x());
		conn.writeInt(u.y());
		conn.writeInt(u.lives());
		int ca = u.carryAmount();
		conn.writeInt(ca);
		if (ca != 0) {
			writeRes(conn, u.carryRes());
		}
		if (u instanceof Carrier) {
			conn.writeInt(Carrier.NUMBER);
		} else {
			throw new AssertionError("unknown unit type: " + u.getClass());
		}
		conn.writeInt(FIN_ENTITY);
	}
	
	public static void writeRes(Connection conn, Resource r) throws AssertionError, IOException {
		Class<? extends Resource> cls = r.getClass();
		if (!cls.isEnum()) {
			throw new AssertionError("resource class is no enum");
		}
		try {
			conn.writeInt(cls.getDeclaredField("NUMBER").getInt(null));
			conn.writeInt(((Enum<?>) r).ordinal());
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new AssertionError(e.toString(), e);
		}
	}
	
	@SuppressWarnings("preview")
	private static void sendBuilding(Building b, Connection conn) throws IOException {
		conn.writeInt(b.x());
		conn.writeInt(b.y());
		conn.writeInt(b.lives());
		boolean fb = b.isFinishedBuild();
		conn.writeByte(fb ? 1 : 0);
		if (!fb) {
			conn.writeInt(b.remainingBuildTurns());
			EnumIntMap<ProducableResourceType> res = b.neededResources();
			if (res == null) {
				conn.writeInt(0);
			} else {
				int[] arr = res.array();
				conn.writeInt(arr.length);
				for (int i = 0; i < arr.length; i++) {
					conn.writeInt(arr[i]);
				}
			}
		}
		switch (b) {
		case StoreBuild sb -> {
			conn.writeInt(StoreBuild.NUMBER);
			if (fb) {
				EnumIntMap<OreResourceType> ores = sb.ores();
				int[]                       oa   = ores.array();
				conn.writeInt(oa.length);
				for (int i = 0; i < oa.length; i++) {
					conn.writeInt(oa[i]);
				}
				EnumIntMap<ProducableResourceType> producable = sb.producable();
				int[]                              pa         = producable.array();
				conn.writeInt(pa.length);
				for (int i = 0; i < pa.length; i++) {
					conn.writeInt(pa[i]);
				}
			}
		}
		default -> throw new AssertionError("unknown building type: " + b.getClass());
		}
		conn.writeInt(FIN_ENTITY);
	}
	
}
