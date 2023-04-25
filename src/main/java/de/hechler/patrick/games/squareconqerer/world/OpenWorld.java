package de.hechler.patrick.games.squareconqerer.world;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.connect.AbstractWrongInputHandler;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.interfaces.Executable;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public class OpenWorld extends AbstractWrongInputHandler implements Executable<IOException> {
	
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_SIZE}</li>
	 * <li>Server: {@link #SUB0_GET_SIZE}</li>
	 * <li>Server: xlen</li>
	 * <li>Server: ylen</li>
	 * </ol>
	 */
	static final int CMD_GET_SIZE                = 0xF14BC0F5;
	static final int SUB0_GET_SIZE               = 0x15285168;
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_TILE}</li>
	 * <li>Server: {@link #SUB0_GET_SIZE}</li>
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
	 * <li>write the name of the owner {@link Connection#writeString(String)} ({@link RootUser#ROOT_NAME} if there is no owner)</li>
	 * <li>{@link #sendUnit(Unit, Connection)}</li>
	 * </ol>
	 * </li>
	 * <li>if there is an building:
	 * <ol>
	 * <li>write the name of the owner {@link Connection#writeString(String)} ({@link RootUser#ROOT_NAME} if there is no owner)</li>
	 * <li>{@link #sendBuilding(Building, Connection)}</li>
	 * </ol>
	 * </li>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.type.oridinal</code></li>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.resource.oridinal</code></li>
	 * <li>Server: visible ({@code 0}: not visible ; {@code 1} visible)</li>
	 * </ol>
	 */
	static final int CMD_GET_TILE                = 0xB62FD4F7;
	static final int SUB0_GET_TILE               = 0xFA35FA34;
	static final int GET_TILE_YES_UNIT_YES_BUILD = 0x0CB0A7C8;
	static final int GET_TILE_NO_UNIT_YES_BUILD  = 0x994024A5;
	static final int GET_TILE_YES_UNIT_NO_BUILD  = 0x6741243A;
	static final int GET_TILE_NO_UNIT_NO_BUILD   = 0x623E9F1A;
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_WORLD}</li>
	 * <li>Server: {@link #SUB0_GET_WORLD}</li>
	 * <li>Server: xlen</li>
	 * <li>Server: ylen</li>
	 * <li><code> for (x = 0 ; x < xlen ; x++) </code>
	 * <ol>
	 * <li><code> for (y = 0 ; y < ylen ; y++) </code>
	 * <ol>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.type.oridinal</code></li>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.resource.oridinal</code></li>
	 * <li>Server: visible ({@code 0}: not visible ; {@code 1}: visible)</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB1_GET_WORLD}</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB2_GET_WORLD}</li>
	 * <li>Server: player count</li>
	 * <li><code> for (i = 0 ; i < player count ; i ++) </code>
	 * <ol>
	 * <li>Server: sends the players name {@link Connection#writeString(String)}</li>
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
	 * <li>Server: {@link #SUB3_GET_WORLD}</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB4_GET_WORLD}</li>
	 * <li>Client: {@link #FIN_GET_WORLD}</li>
	 * </ol>
	 */
	static final int CMD_GET_WORLD               = 0xC4F640DE;
	static final int SUB0_GET_WORLD              = 0xFA48B370;
	static final int SUB1_GET_WORLD              = 0xFA48B370;
	static final int SUB2_GET_WORLD              = 0xFA48B370;
	static final int SUB3_GET_WORLD              = 0x67E14E35;
	static final int SUB4_GET_WORLD              = 0xA9250E35;
	static final int FIN_GET_WORLD               = 0x030F4D21;
	
	static final int CMD_UNIT   = 0x9D527756;
	static final int CMD_BUILD  = 0x1112CD5A;
	static final int FIN_ENTITY = 0x5A96A583;
	
	static final int NOTIFY_NEXT_TURN = 0xE30212DB;
	static final int FIN_NEXT_TURN    = 0xCFB3B299;
	
	private final Connection conn;
	private final World      world;
	
	private OpenWorld(Connection conn, World world) {
		this.conn  = conn;
		this.world = world;
	}
	
	// maybe delegate later to different constructors for different worlds (root
	// world or non root world for example)
	public static OpenWorld of(Connection conn, World world) {
		return new OpenWorld(conn, world);
	}
	
	private void nextTurn() {
		try {
			this.conn.blocked(250, () -> this.conn.writeReadInt(NOTIFY_NEXT_TURN, FIN_NEXT_TURN), Connection.NOP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean executing;
	
	@Override
	public void execute() throws IOException {
		this.conn.replaceWongInput(null, this);
		this.conn.setTimeout(250);
		Runnable ntl = this::nextTurn;
		this.world.addNextTurnListener(ntl);
		try {
			while (this.executing) singleExecute();
		} finally {
			this.world.removeNextTurnListener(ntl);
			this.conn.close();
		}
	}
	
	private void singleExecute() throws IOException {
		this.conn.blocked(250, () -> {
			long val0 = this.conn.readInt0();
			if (val0 == -1L) {
				this.executing = false;
				return;
			}
			exec((int) val0);
		}, Connection.NOP);
	}
	
	private void exec(int val) throws IOException {
		this.conn.setTimeout(0);
		switch (val) {
		case CMD_GET_SIZE -> {
			this.conn.writeInt(SUB0_GET_SIZE);
			this.conn.writeInt(this.world.xlen());
			this.conn.writeInt(this.world.ylen());
		}
		case CMD_GET_TILE -> sendTile();
		case CMD_GET_WORLD -> sendWorld(this.world, this.conn, true);
		case Turn.CMD_TURN -> {
			Turn t = new Turn(this.world);
			t.retrieveTurn(this.conn);
			this.world.finish(t);
		}
		default -> throw new StreamCorruptedException("read invalid data (username: '" + this.world.user().name() + "')");
		}
	}
	
	/*
	 * when there is a race condition, the client gets its action executed first, then the server needs to retry its action from the beginning
	 */
	
	@Override
	protected void handleWrongInputInt(int value, boolean readwrite) throws IOException, StreamCorruptedException, EOFException {
		if (!readwrite) { throw new StreamCorruptedException("read an unexpected value: " + value); }
		exec(value);
	}
	
	@Override
	protected void handleWrongInputByte(int value) throws IOException, StreamCorruptedException, EOFException {
		throw new StreamCorruptedException("read an unexpected value: " + value);
	}
	
	private void sendTile() throws IOException {
		this.conn.writeInt(SUB0_GET_TILE);
		int  x = this.conn.readInt();
		int  y = this.conn.readInt();
		Tile t;
		try {
			t = this.world.tile(x, y);
		} catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
			this.conn.writeInt(-1);
			return;
		}
		Unit     u = t.unit();
		Building b = t.building();
		if (u == null && b == null) {
			this.conn.writeInt(GET_TILE_NO_UNIT_NO_BUILD);
		} else if (u == null) {
			this.conn.writeInt(GET_TILE_NO_UNIT_YES_BUILD);
		} else if (b == null) {
			this.conn.writeInt(GET_TILE_YES_UNIT_NO_BUILD);
		} else {
			this.conn.writeInt(GET_TILE_YES_UNIT_YES_BUILD);
		}
		if (u != null) {
			User usr = u.owner();
			this.conn.writeString(usr != null ? usr.name() : RootUser.ROOT_NAME);
			sendUnit(u, this.conn);
		}
		if (b != null) {
			User usr = b.owner();
			this.conn.writeString(usr != null ? usr.name() : RootUser.ROOT_NAME);
			sendBuilding(b, this.conn);
		}
		this.conn.writeInt(t.type.ordinal());
		this.conn.writeInt(t.resource.ordinal());
		this.conn.writeByte(t.visible() ? 1 : 0);
	}
	
	public static void saveWorld(World world, Connection conn) throws IOException {
		sendWorld(world, conn, false);
	}
	
	private static void sendWorld(World world, Connection conn, boolean alsoRead) throws IOException {
		if (!alsoRead) {
			conn.writeInt(OpenWorld.CMD_GET_WORLD);
		} else {
			conn.writeInt(OpenWorld.SUB0_GET_WORLD);
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
			conn.writeInt(SUB1_GET_WORLD);
		}
		conn.writeInt(SUB2_GET_WORLD);
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
			conn.writeInt(SUB3_GET_WORLD);
		}
		conn.writeInt(SUB4_GET_WORLD);
		if (alsoRead) {
			conn.readInt(FIN_GET_WORLD);
		}
	}
	
	public static void writeRes(Connection conn, Resource r) throws AssertionError, IOException {
		Class<? extends Resource> cls = r.getClass();
		if (!cls.isEnum()) { throw new AssertionError("resource class is no enum"); }
		try {
			conn.writeInt(cls.getDeclaredField("NUMBER").getInt(null));
			conn.writeInt(((Enum<?>) r).ordinal());
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new AssertionError(e.toString(), e);
		}
	}
	
	private static void sendUnit(Unit u, Connection conn) throws IOException {
		SquareConquererAddon addon = SquareConquererAddon.addon(u);
		if (addon == SquareConquererAddon.theGame()) {
			conn.writeInt(TheGameAddon.THE_GAME);
		} else {
			conn.writeInt(TheGameAddon.OTHER_ADDON);
			conn.writeString(addon.name);
		}
		addon.entities().sendUnit(conn, u);
		conn.writeInt(FIN_ENTITY);
	}
	
	private static void sendBuilding(Building b, Connection conn) throws IOException {
		SquareConquererAddon addon = SquareConquererAddon.addon(b);
		if (addon == SquareConquererAddon.theGame()) {
			conn.writeInt(TheGameAddon.THE_GAME);
		} else {
			conn.writeInt(TheGameAddon.OTHER_ADDON);
			conn.writeString(addon.name);
		}
		addon.entities().sendBuild(conn, b);
		conn.writeInt(FIN_ENTITY);
	}
	
}
