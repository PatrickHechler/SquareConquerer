package de.hechler.patrick.games.squareconqerer.world.connect;

import java.io.IOException;

import de.hechler.patrick.games.squareconqerer.interfaces.Executable;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.World;

public class OpenWorld implements Executable<IOException> {
	
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_SIZE}</li>
	 * <li>Align: Client -&gt; Server</li>
	 * <li>Server: xlen</li>
	 * <li>Server: ylen</li>
	 * <li>Align: Client &lt;- Server</li>
	 * </ol>
	 */
	static final int CMD_GET_SIZE                = 0xF14BC0F5;
	/**
	 * <ol>
	 * <li>Client: {@link #CMD_GET_TILE}</li>
	 * <li>Client: x coordinate</li>
	 * <li>Client: y coordinate</li>
	 * <li>Align: Client -&gt; Server</li>
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
	 * <li>TODO send unit</li>
	 * </ol>
	 * </li>
	 * <li>if there is an building:
	 * <ol>
	 * <li>TODO send building</li>
	 * </ol>
	 * </li>
	 * <li>Server: <code>{@link World#tile(int, int) tile(x,y)}.type.oridinal</code>
	 * </li>
	 * <li>Server:
	 * <code>{@link World#tile(int, int) tile(x,y)}.resource.oridinal</code></li>
	 * <li>Align: Client &lt;- Server</li>
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
	 * <li>Align: Client -&gt; Server</li>
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
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_0}</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_1}</li>
	 * <li>Server: building count</li>
	 * <li>Server: unit count</li>
	 * <li><code> for (i = 0 ; i < building count ; i ++) </code>
	 * <ol>
	 * <li>TODO send building</li>
	 * </ol>
	 * <li>Server: {@link #SUB_GET_WORLD_2}</li>
	 * </li>
	 * <li><code> for (i = 0 ; i < unit count ; i ++) </code>
	 * <ol>
	 * <li>TODO send unit</li>
	 * </ol>
	 * </li>
	 * <li>Server: {@link #SUB_GET_WORLD_3}</li>
	 * <li>Align: Client &lt;- Server</li>
	 * <li>Client: {@link #FIN_GET_WORLD}</li>
	 * <li>Align: Client -&gt; Server</li>
	 * </ol>
	 */
	static final int CMD_GET_WORLD               = 0xC4F640DE;
	static final int SUB_GET_WORLD_0             = 0xFA48B370;
	static final int SUB_GET_WORLD_1             = 0xFA48B370;
	static final int SUB_GET_WORLD_2             = 0x67E14E35;
	static final int SUB_GET_WORLD_3             = 0xA9250E35;
	static final int FIN_GET_WORLD               = 0x030F4D21;
	
	private final Connection conn;
	private final World      world;
	
	public OpenWorld(Connection conn, World world) {
		this.conn  = conn;
		this.world = world;
	}
	
	@Override
	public void execute() throws IOException {
		try {
			while (true) {
				long val0 = conn.readInt0();
				if (val0 == -1L) {
					return;
				}
				int val = (int) val0;
				switch (val) {
				case CMD_GET_SIZE -> {
					conn.writeInt(world.xlen());
					conn.writeInt(world.ylen());
				}
				case CMD_GET_TILE -> {
					int  x = val;
					int  y = val;
					Tile t;
					try {
						t = world.tile(x, y);
					} catch (IndexOutOfBoundsException e) {
						conn.writeInt(-1);
						continue;
					}
					conn.writeInt(GET_TILE_NO_UNIT_NO_BUILD);
					conn.writeInt(t.type.ordinal());
					conn.writeInt(t.resource.ordinal());
				}
				case CMD_GET_WORLD -> {
					sendWorld(world, conn, true);
				}
				default -> {
					System.err.println("[OpenWorld]: read invalid data, close connection (" + world.user().name() + ")");
					return;
				}
				}
			}
		} finally {
			conn.close();
		}
	}
	
	public static void sendWorld(World world, Connection conn, boolean alsoRead) throws IOException {
		if (!alsoRead) {
			conn.writeInt(OpenWorld.CMD_GET_WORLD);
		}
		int xlen = world.xlen();
		int ylen = world.xlen();
		conn.writeInt(xlen);
		conn.writeInt(ylen);
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				Tile t = world.tile(x, y);
				conn.writeInt(t.type.ordinal());
				conn.writeInt(t.resource.ordinal());
			}
			conn.writeInt(SUB_GET_WORLD_0);
		}
		conn.writeInt(SUB_GET_WORLD_1);
		conn.writeInt(0);
		conn.writeInt(0);
		conn.writeInt(SUB_GET_WORLD_2);
		conn.writeInt(SUB_GET_WORLD_3);
		if (alsoRead) {
			conn.readInt(FIN_GET_WORLD);
		}
	}
	
}
