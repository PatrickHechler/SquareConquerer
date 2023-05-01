// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world;

import static de.hechler.patrick.games.squareconqerer.Settings.threadStart;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.connect.WrongInputHandler;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.tile.RemoteTile;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

/**
 * this world implementation uses a {@link Connection} to allow together with {@link OpenWorld} remote Worlds, which are not from the local system
 * 
 * @author Patrick Hechler
 */
public final class RemoteWorld implements World, Closeable {
	
	private static final String ONLY_MY_TURNS                    = Messages.get("RemoteWorld.only-my-turns");                    //$NON-NLS-1$
	private static final String HAVE_ALREADY_VALIDATE_DATA       = Messages.get("RemoteWorld.already-have-val-data");            //$NON-NLS-1$
	private static final String GAME_STARTED                     = Messages.get("RemoteWorld.log-game-started");                 //$NON-NLS-1$
	private static final String TAB_TURNS                        = Messages.get("RemoteWorld.log--turns");                       //$NON-NLS-1$
	private static final String TAB_WORLD                        = Messages.get("RemoteWorld.log--world");                       //$NON-NLS-1$
	private static final String NEXT_TURN                        = Messages.get("RemoteWorld.log-next-turn");                    //$NON-NLS-1$
	private static final String TURN_DIFFERENT_HASH              = Messages.get("RemoteWorld.error-different-turn-hash");        //$NON-NLS-1$
	private static final String WORLD_DIFFERENT_HASH             = Messages.get("RemoteWorld.error-different-world-hash");       //$NON-NLS-1$
	private static final String NOT_EXPECTED_START_HASH          = Messages.get("RemoteWorld.error-different-start-world-hash"); //$NON-NLS-1$
	private static final String DIFFERENT_SEED_FOR_ME            = Messages.get("RemoteWorld.error-not-my-seed");                //$NON-NLS-1$
	private static final String ITER_COUNT_OF_WORLD_NOT_EXPECTED = Messages.get("RemoteWorld.error-different-turn-count");       //$NON-NLS-1$
	private static final String SKIP_VALIDATION_NO_DATA_TO_CHECK = Messages.get("RemoteWorld.log-skip-check-no-val");            //$NON-NLS-1$
	private static final String INVALID_NOTIFICATION             = Messages.get("RemoteWorld.invalid-notify");                   //$NON-NLS-1$
	private static final String KNOWN_USERS                      = Messages.get("RemoteWorld.known-users");                      //$NON-NLS-1$
	private static final String UNKNOWN_USERNAME                 = Messages.get("RemoteWorld.unknown-username");                 //$NON-NLS-1$
	private static final String NO_RECTANGULAR_FORM              = Messages.get("RemoteWorld.no-rect-form");                     //$NON-NLS-1$
	private static final String NEW                              = Messages.get("RemoteWorld.new");                              //$NON-NLS-1$
	private static final String WARN_WORLD_SIZE_CHANGED          = Messages.get("RemoteWorld.warn-size-change");                 //$NON-NLS-1$
	private static final String DIFFERENT_WORLD_SIZES            = Messages.get("RemoteWorld.diff-world-size");                  //$NON-NLS-1$
	private static final String NEGATIVE_COORDINATE              = Messages.get("RemoteWorld.negative-coordinate");              //$NON-NLS-1$
	
	private final Connection        conn;
	private int                     xlen;
	private int                     ylen;
	private RemoteTile[][]          tiles;
	private long                    needUpdate;
	private long                    lastWorldUpdate;
	private boolean                 getWorld = true;
	private Map<User, List<Entity>> entities;
	private Map<String, User>       users;
	private List<byte[]>            validateData;
	
	/**
	 * create a new Remote world using the given {@link Connection}
	 * 
	 * @param conn the connection of the remote world
	 */
	public RemoteWorld(Connection conn) {
		this.conn = conn;
		this.conn.replaceWongInput(null, new WrongInputHandler() {
			/*
			 * on race condition input, the client has priority, its request gets executed, than the server can send its request again
			 */
			
			@Override
			public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
				RemoteWorld.this.conn.readInt(expectedRead);
			}
			
		});
		threadStart(this::deamon);
	}
	
	/**
	 * if the entire world should be updated, when an tile is out-dated
	 * <p>
	 * by default getWorld is <code>true</code><br>
	 * if {@link #needUpdate()} is often called, this value might be set to <code>false</code>
	 * 
	 * @param getWorld the new value of {@link #getWorld() getWorld}
	 */
	public void getWorld(boolean getWorld) { this.getWorld = getWorld; }
	
	/**
	 * returns <code>true</code> if the entire world should be updated, when an out-dated tile is needed and <code>false</code> if only the needed tile should be
	 * updated
	 * 
	 * @return <code>true</code> if the entire world should be updated, when an out-dated tile is needed and <code>false</code> if only the needed tile should be
	 *         updated
	 */
	public boolean getWorld() { return this.getWorld; }
	
	/**
	 * marks everything as out-dated
	 */
	public void needUpdate() { this.needUpdate = System.currentTimeMillis(); }
	
	/**
	 * returns the {@link Connection#usr}
	 */
	@Override
	public User user() {
		return this.conn.usr;
	}
	
	/** {@inheritDoc} */
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
	 * @throws IOException if an IO error occurs
	 */
	public synchronized void updateWorldSize() throws IOException {
		this.conn.blocked(() -> {
			this.conn.writeReadInt(OpenWorld.CMD_GET_SIZE, OpenWorld.SUB0_GET_SIZE);
			this.xlen  = this.conn.readInt();
			this.ylen  = this.conn.readInt();
			this.tiles = new RemoteTile[this.xlen][this.ylen];
		});
	}
	
	/** {@inheritDoc} */
	@Override
	public int ylen() {
		if (this.ylen == 0) {
			xlen();
		}
		return this.ylen;
	}
	
	/**
	 * returns <code>true</code> if the world bounds ({@link #xlen()}/{@link #ylen()}) are already loaded from the remote location and <code>false</code> if they are
	 * not yet known
	 * 
	 * @return <code>true</code> if the world bounds ({@link #xlen()}/{@link #ylen()}) are already loaded from the remote location and <code>false</code> if they are
	 *         not yet known
	 */
	public boolean loadedBounds() {
		return this.xlen != 0;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method will get the world size of the remote location, if they are not yet known<br>
	 * if the tile at the given coordinates is not known or out-dated it will be updated:
	 * <ul>
	 * <li>if {@link #getWorld()} is <code>true</code>: {@link #updateWorld()} will be called</li>
	 * <li>if {@link #getWorld()} is <code>false</code>: {@link #updateSingleTile(int, int) updateSingleTile(x,y)} will be called</li>
	 * </ul>
	 */
	@Override
	public Tile tile(int x, int y) {
		if (x < 0 || y < 0) throw new IndexOutOfBoundsException(NEGATIVE_COORDINATE + " x=" + x + " y=" + y); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			boolean updated = false;
			if (this.xlen == 0) {
				if (this.getWorld) {
					updateWorld();
					updated = true;
				} else {
					updateWorldSize();
				}
			}
			if (x >= this.xlen || y >= this.ylen) throw new IndexOutOfBoundsException("x=" + x + " y=" + y + " xlen=" + this.xlen + " ylen=" + this.ylen); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if (this.tiles[x][y] == null || (this.tiles[x][y].created <= this.needUpdate && !updated)) {
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
	
	/**
	 * updates the entire world from the remote location
	 * 
	 * @throws IOException if an IO error occurred
	 */
	public synchronized void updateWorld() throws IOException {
		this.conn.blocked(() -> {
			this.lastWorldUpdate = System.currentTimeMillis();
			this.needUpdate      = this.lastWorldUpdate - 1L; // if needs update was set previously
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
			this.entities = unmodCopy;
		});
	}
	
	/**
	 * load the world from the given connection.
	 * <p>
	 * if the world from the connection has users, which are not in the map, this operation will fail
	 * 
	 * @param conn  the connection
	 * @param users the users with their {@link User#name()} as key
	 * 
	 * @return the loaded world
	 * 
	 * @throws IOException if an IO error occurred
	 */
	public static Tile[][] loadWorld(Connection conn, Map<String, User> users) throws IOException {
		return readWorld(conn, null, -1L, null, users);
	}
	
	/**
	 * load the world from the given connection.
	 * <p>
	 * if the world from the connection has users, which are not in the map, this operation will fail<br>
	 * if the world from the connection has different borders, that the given tiles, this operation will fail<br>
	 * if the given tiles array has no rectangular form this operation will fail
	 * 
	 * @param conn  the connection
	 * @param users the users with their {@link User#name()} as key
	 * @param tiles the <code>Tile[][]</code> which will contain the loaded world after this method
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public static void loadWorld(Connection conn, Map<String, User> users, Tile[][] tiles) throws IOException {
		readWorld(conn, tiles, -1L, null, users);
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
			if (entities == null) throw new IllegalArgumentException(DIFFERENT_WORLD_SIZES);
			System.err.println(WARN_WORLD_SIZE_CHANGED + tiles.length + " ylen=" + tiles[0].length + NEW + " xlen=" + xlen + " ylen=" + ylen + ')'); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tiles = (T[][]) (new RemoteTile[xlen][ylen]);
		}
		for (int x = 0; x < xlen; x++) {
			if (tiles[x].length != ylen) throw new IllegalArgumentException(NO_RECTANGULAR_FORM);
			for (int y = 0; y < ylen; y++) {
				int             tto = conn.readInt();
				int             rto = conn.readInt();
				GroundType      tt  = GroundType.of(tto);
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
			if (usr == null) { throw new IllegalStateException(UNKNOWN_USERNAME + username + KNOWN_USERS + users + "')"); } //$NON-NLS-1$
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
	
	/**
	 * reads a resource from the connection
	 * 
	 * @param conn the connection
	 * 
	 * @return the resource, which was read from the connection
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public static Resource readRes(Connection conn) throws IOException {
		return switch (conn.readInt(ProducableResourceType.NUMBER, OreResourceType.NUMBER)) {
		case ProducableResourceType.NUMBER -> ProducableResourceType.of(conn.readInt());
		case OreResourceType.NUMBER -> OreResourceType.of(conn.readInt());
		default -> throw new AssertionError("invalid return type of conn.readInt(int,int) (this should never happen)"); //$NON-NLS-1$
		};
	}
	
	private static Unit readUnit(Connection conn, User usr) throws IOException {
		SCAddon addon;
		if (conn.readInt(TheGameAddon.THE_GAME, TheGameAddon.OTHER_ADDON) == TheGameAddon.THE_GAME) {
			addon = SCAddon.theGame();
		} else {
			addon = SCAddon.addon(conn.readString());
		}
		Unit u = addon.entities().recieveUnit(conn, usr);
		conn.readInt(OpenWorld.FIN_ENTITY);
		return u;
	}
	
	private static Building readBuilding(Connection conn, User usr) throws IOException {
		SCAddon addon;
		if (conn.readInt(TheGameAddon.THE_GAME, TheGameAddon.OTHER_ADDON) == TheGameAddon.THE_GAME) {
			addon = SCAddon.theGame();
		} else {
			addon = SCAddon.addon(conn.readString());
		}
		Building n = addon.entities().recieveBuild(conn, usr);
		conn.readInt(OpenWorld.FIN_ENTITY);
		return n;
	}
	
	/**
	 * updates a single tile from the world<br>
	 * if the given coordinates are outside of the world borders, this operation will fail.
	 * 
	 * @param x the x coordinate of the tile
	 * @param y the y coordinate of the tile
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public synchronized void updateSingleTile(int x, int y) throws IOException {
		this.conn.blocked(() -> {
			if (x < 0 || y < 0 || x >= xlen() || y >= this.ylen) {
				throw new IllegalArgumentException("x=" + x + " y=" + y + " xlen=" + this.xlen + " ylen=" + this.ylen); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
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
			default -> throw new AssertionError("illegal return value from readInt(int...) (this should never happen)"); //$NON-NLS-1$
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
			GroundType      tt       = GroundType.of(typeOrid);
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
					this.conn.setTimeout(0);
					switch (val0) {
					case RootWorld.REQ_RND -> deamonReqRnd();
					case OpenWorld.NOTIFY_WORLD_CHANGE -> deamonNotifyWorldChange();
					case OpenWorld.NOTIFY_GAME_START -> deamonNotifyGameStart();
					case OpenWorld.NOTIFY_NEXT_TURN -> deamonNotifyNextTurn();
					case RootWorld.RW_VAL_GAME -> deamonValidateGame();
					default -> {
						System.err.println(INVALID_NOTIFICATION + Integer.toHexString(val0));
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
	
	private void deamonValidateGame() throws IOException, AssertionError {
		this.conn.writeInt(RootWorld.SUB0_VAL_GAME);
		RootWorld rw = RootWorld.loadEverything(this.conn);
		this.conn.readInt(RootWorld.FIN_VAL_GAME);
		if (this.validateData == null) {
			System.out.println(SKIP_VALIDATION_NO_DATA_TO_CHECK);
		} else {
			Iterator<RootWorld> iter = rw.iterator();
			RootWorld           irw  = iter.next();
			if ((irw.iterCount() - 1) << 1 != this.validateData.size()) {
				throw new AssertionError(ITER_COUNT_OF_WORLD_NOT_EXPECTED);
			}
			irw.addNextTurnListener(new BiConsumer<byte[], byte[]>() {
				
				private int index;
				
				@Override
				public void accept(byte[] wh, byte[] th) {
					if (this.index == 0) {
						if (th == null) throw new AssertionError("there is a turn hash (this should never happen)"); //$NON-NLS-1$
						if (!irw.isSeed(RemoteWorld.this.conn.usr, RemoteWorld.this.validateData.get(0))) {
							throw new AssertionError(DIFFERENT_SEED_FOR_ME);
						}
						if (!Arrays.equals(wh, RemoteWorld.this.validateData.get(1))) {
							throw new AssertionError(NOT_EXPECTED_START_HASH);
						}
					} else {
						if (!Arrays.equals(wh, RemoteWorld.this.validateData.get(this.index))) {
							throw new AssertionError(WORLD_DIFFERENT_HASH + (this.index >> 1) + '!');
						}
						if (!Arrays.equals(th, RemoteWorld.this.validateData.get(this.index + 1))) {
							throw new AssertionError(TURN_DIFFERENT_HASH + (this.index >> 1) + '!');
						}
					}
					this.index += 2;
				}
				
			});
		}
	}
	
	private void deamonNotifyNextTurn() throws IOException {
		this.conn.writeInt(OpenWorld.SUB0_NEXT_TURN);
		byte[] worldhash = new byte[256 / 8];
		this.conn.readArr(worldhash);
		this.conn.readInt(OpenWorld.SUB1_NEXT_TURN);
		byte[] turnshash = new byte[256 / 8];
		this.conn.readArr(turnshash);
		this.conn.readInt(OpenWorld.FIN_NEXT_TURN);
		this.needUpdate = System.currentTimeMillis();
		if (this.validateData != null) {
			this.validateData.add(worldhash);
			this.validateData.add(turnshash);
		}
		System.out.println(NEXT_TURN);
		System.out.println(TAB_WORLD + hex(worldhash));
		System.out.println(TAB_TURNS + hex(turnshash));
		worldhash = worldhash.clone();
		turnshash = turnshash.clone();
		for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
			r.accept(worldhash, turnshash);
		}
	}
	
	private void deamonNotifyGameStart() throws IOException {
		this.conn.writeInt(OpenWorld.SUB0_GAME_START);
		byte[] worldhash = new byte[256 / 8];
		this.conn.readArr(worldhash);
		this.conn.readInt(OpenWorld.FIN_GAME_START);
		this.needUpdate = System.currentTimeMillis();
		if (this.validateData != null) this.validateData.add(worldhash);
		System.out.println(GAME_STARTED);
		System.out.println(TAB_WORLD + hex(worldhash));
		worldhash = worldhash.clone();
		for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
			r.accept(worldhash, null);
		}
	}
	
	private void deamonNotifyWorldChange() throws IOException {
		this.conn.writeInt(OpenWorld.FIN_WORLD_CHANGE);
		this.needUpdate = System.currentTimeMillis();
		for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
			r.accept(null, null);
		}
	}
	
	private void deamonReqRnd() throws IOException {
		this.conn.writeInt(RootWorld.GIV_RND);
		if (this.validateData != null) throw new IllegalStateException(HAVE_ALREADY_VALIDATE_DATA);
		ArrayList<byte[]> list = new ArrayList<>();
		this.validateData = list;
		byte[] myrnd = new byte[16];
		User.fillRandom(myrnd);
		this.conn.writeArr(myrnd);
		list.add(myrnd);
		System.out.println("remote world: my random part is " + hex(myrnd));
	}
	
	private static String hex(byte[] data) {
		StringBuilder b = new StringBuilder(data.length << 1);
		for (int i = 0; i < data.length; i++) {
			int val = data[i];
			b.append(hex((0xF0 & val) >> 4));
			b.append(hex(0x0F & val));
		}
		return b.toString();
	}
	
	private static char hex(int n) {
		if (n >= 0xA) return (char) ('A' + n);
		else return (char) ('0' + n);
	}
	
	/** {@inheritDoc} */
	@Override
	public void addNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.conn.blocked(() -> this.nextTurnListeners.add(listener));
	}
	
	/** {@inheritDoc} */
	@Override
	public void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.conn.blocked(() -> this.nextTurnListeners.remove(listener));
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this method will update the complete world if {@link #needUpdate()} was called since the last {@link #updateWorld()} call
	 */
	@Override
	public Map<User, List<Entity>> entities() {
		if (this.needUpdate >= this.lastWorldUpdate) {
			try {
				updateWorld();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return Collections.unmodifiableMap(this.entities);
	}
	
	/**
	 * this method just calls {@link Connection#close()}
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		this.conn.close();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this world only supports executing turns of the worlds {@link #user() user}
	 */
	@Override
	public void finish(Turn t) throws IllegalStateException {
		if (t.world.user() != this.conn.usr) throw new IllegalStateException(ONLY_MY_TURNS);
		try {
			t.sendTurn(this.conn, true);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
}
