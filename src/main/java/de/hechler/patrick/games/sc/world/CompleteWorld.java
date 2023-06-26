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
package de.hechler.patrick.games.sc.world;

import static de.hechler.patrick.games.sc.Settings.threadStart;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.Addon;
import de.hechler.patrick.games.sc.addons.Addons;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.turn.Attack;
import de.hechler.patrick.games.sc.turn.CarryTurn;
import de.hechler.patrick.games.sc.turn.Direction;
import de.hechler.patrick.games.sc.turn.EntityTurn;
import de.hechler.patrick.games.sc.turn.MineTurn;
import de.hechler.patrick.games.sc.turn.MoveAct;
import de.hechler.patrick.games.sc.turn.MoveTurn;
import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.StoreTurn;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.turn.WorkTurn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.init.DefaultUserPlacer;
import de.hechler.patrick.games.sc.world.init.UserPlacer;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.NeigbourTiles;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.objects.Pos;
import de.hechler.patrick.utils.objects.Random2;

/**
 * the complete world knows everything
 * <p>
 * after the game started, it can iterate over the old game states
 * 
 * @author Patrick Hechler
 */
public class CompleteWorld implements World, Iterable<CompleteWorld> {
	
	private final User                   root;
	private final Tile[][]               tiles;
	private final UserPlacer             placer;
	private final Map<User, UserWorld>   subWorlds;
	private final List<NextTurnListener> nextTurnListeneres;
	private final Map<User, Turn>        userTurns;
	private final List<Map<User, Turn>>  allTurns;
	private volatile boolean             allowRootTurns;
	private volatile Tile[][]            starttiles;
	private volatile byte[]              seed;
	private volatile Random2             rnd;
	
	private CompleteWorld(User root, Tile[][] tiles, UserPlacer placer) {
		this.root               = root;
		this.tiles              = tiles;
		this.placer             = placer == null ? DefaultUserPlacer.createWithDefaults() : placer;
		this.subWorlds          = new HashMap<>();
		this.nextTurnListeneres = new ArrayList<>();
		this.userTurns          = new TreeMap<>();
		this.allTurns           = new ArrayList<>();
		this.allowRootTurns     = false;
	}
	
	private CompleteWorld(CompleteWorld rw, UserPlacer placer) {
		this.root               = rw.root;
		this.tiles              = rw.tiles;
		this.placer             = placer;
		this.subWorlds          = rw.subWorlds;
		this.nextTurnListeneres = rw.nextTurnListeneres;
		this.userTurns          = rw.userTurns;
		this.allowRootTurns     = rw.allowRootTurns;
		this.allTurns           = rw.allTurns;
		this.starttiles         = rw.starttiles;
		this.seed               = rw.seed;
		this.rnd                = rw.rnd;
		if (placer == null || !this.nextTurnListeneres.isEmpty()) throw new AssertionError();
	}
	
	/**
	 * returns <code>true</code> if the root user is allowed to execute turns and <code>false</code> if not
	 * 
	 * @return <code>true</code> if the root user is allowed to execute turns and <code>false</code> if not
	 */
	public boolean allowRootTurns() {
		return this.allowRootTurns;
	}
	
	/**
	 * sets the {@link #allowRootTurns() allowRootTurns} value
	 * 
	 * @param allowRootTurns the new {@link #allowRootTurns() allowRootTurns} value
	 * 
	 * @throws IllegalStateException if the game already started
	 */
	public void allowRootTurns(boolean allowRootTurns) throws IllegalStateException {
		if (this.rnd != null) throw new IllegalStateException("the game already started");
		this.allowRootTurns = allowRootTurns;
	}
	
	/** {@inheritDoc} */
	@Override
	public User user() {
		return this.root;
	}
	
	/** {@inheritDoc} */
	@Override
	public int xlen() {
		return this.tiles.length;
	}
	
	/** {@inheritDoc} */
	@Override
	public int ylen() {
		return this.tiles[0].length;
	}
	
	/**
	 * returns a modifiable tile of the given position<br>
	 * the tile will be valid as long as the world is valid (even after the next turn/game start)
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Tile tile(int x, int y) {
		return this.tiles[x][y];
	}
	
	private byte[] calcHash() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(String.format("SHA-256 was not found: %s", e), e);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Connection            conn = Connection.createUnsecure(this.root, baos, this);
		try {
			saveEverything(conn, false);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return digest.digest(baos.toByteArray());
	}
	
	private byte[] calcHashOnlyWorld() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(String.format("SHA-256 was not found: %s", e), e);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Connection            conn = Connection.createUnsecure(this.root, baos, this);
		try {
			OpenWorld.saveWorld(this, conn);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return digest.digest(baos.toByteArray());
	}
	
	private static byte[] calcHash(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(String.format("SHA-256 was not found: %s", e), e);
		}
		return digest.digest(data);
	}
	
	private void executeNTL(byte[] myhash, byte[] turnhash) {
		for (NextTurnListener r : this.nextTurnListeneres) {
			r.nextTurn(this.allTurns.size(), myhash, turnhash);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public int turn() {
		return this.starttiles == null ? -1 : this.allTurns.size();
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
	/** @see #REQ_RND */
	public static final int GIV_RND = 0x460C8B92;
	
	/**
	 * request over the given connection random values and than fill the given array with these random values
	 * <p>
	 * this method fails if the array has not length <code>16</code>
	 * 
	 * @param conn the connection, which will be used to retrieve random values
	 * @param arr  the array to be filled with random values
	 * 
	 * @throws IOException    if an IO error occurs
	 * @throws AssertionError if the array has a size different than <code>16</code>
	 * 
	 * @see #REQ_RND
	 */
	public static void fillRnd(Connection conn, byte[] arr) throws IOException, AssertionError {
		if (arr.length != 16) throw new AssertionError("inavlid array size");
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
		if (this.rnd == null) throw new IllegalStateException("the game did not yet start");
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
	@Override
	public Iterator<CompleteWorld> iterator() {
		if (this.rnd == null) throw new IllegalStateException("the game did not yet start");
		return new Iterator<>() {
			
			private Iterator<Map<User, Turn>> iter  = CompleteWorld.this.allTurns.iterator();
			private CompleteWorld             world = null;
			
			@Override
			public boolean hasNext() {
				return this.iter.hasNext() || this.world == null;
			}
			
			@Override
			public CompleteWorld next() {
				if (this.world == null) { // do a copy of the start tiles
					this.world = Builder.create(CompleteWorld.this.root, CompleteWorld.this.starttiles, CompleteWorld.this.placer);
				} else if (!this.world.running()) {
					this.world.startGame0(CompleteWorld.this.seed, false);
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
	
	/**
	 * saves the current world, the initial world, the random seed, all sub worlds and their users and the game history to the given connection
	 * 
	 * @param conn the connection which should be used to save everything
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public synchronized void saveEverything(Connection conn) throws IOException {
		conn.blocked(() -> saveEverything(conn, true));
	}
	
	private /* synchronized */ void saveEverything(Connection conn, boolean savePWs) throws IOException {
		if (this.seed == null) throw new IllegalStateException("the game did not yet start");
		conn.writeInt(RWS_START);
		conn.writeLong(this.rnd.getCurrentSeed());
		conn.writeInt(this.seed.length);
		conn.writeArr(this.seed);
		conn.writeInt(RWS_SUB0);
		if (savePWs) {
			this.root.save(conn);
		} else {
			User r = User.nopw(this.root.name());
			this.root.subUsers().keySet().forEach(r::addNopw);
			r.save(conn);
		}
		conn.writeInt(RWS_SUB1);
		OpenWorld.saveWorld(this, conn);
		conn.writeInt(RWS_SUB2); // I just need a world with the startTiles
		OpenWorld.saveWorld(new CompleteWorld(this.root, this.starttiles, this.placer), conn);
		conn.writeInt(RWS_SUB3);
		conn.writeInt(this.subWorlds.size());
		for (Entry<User, UserWorld> e : this.subWorlds.entrySet()) {
			User      usr = e.getKey();
			UserWorld uw  = e.getValue();
			conn.writeString(usr.name());
			conn.writeInt(RWS_SUB4);
			Pos p = uw.offset();
			conn.writeInt(p.x());
			conn.writeInt(p.y());
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
	
	/**
	 * loads the current world, the initial world, the random seed, all sub worlds and their users and the game history from the given connection
	 * 
	 * @param conn the connection which stores everything
	 * 
	 * @return the loaded {@link CompleteWorld}
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public static CompleteWorld loadEverything(Connection conn) throws IOException {
		conn.readInt(RWS_START);
		long   curSeed = conn.readLong();
		byte[] seed    = new byte[conn.readInt()];
		conn.readArr(seed);
		conn.readInt(RWS_SUB0);
		User root = conn.usr;
		root.load(conn);
		conn.readInt(RWS_SUB1);
		Tile[][]      tiles = OpenWorld.loadWorld(null, conn);
		CompleteWorld res   = Builder.create(root, tiles);
		conn.readInt(RWS_SUB2);
		res.starttiles = OpenWorld.loadWorld(null, conn);
		conn.readInt(RWS_SUB2);
		res.seed = seed;
		res.rnd  = new Random2(curSeed);
		for (int remain = conn.readPos(); remain > 0; remain--) {
			String name = conn.readString();
			User   usr  = root.get(name);
			conn.readInt(RWS_SUB4);
			int       xoff     = conn.readPos();
			int       yoff     = conn.readPos();
			Tile[][]  usrTiles = OpenWorld.loadWorld(null, conn);
			UserWorld uw       = res.usrOf(usr, 0);
			uw.init(usrTiles, xoff, yoff);
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
				conn.readInt(Turn.CMD_TURN);
				t.retrieveTurn(conn);
				add.put(usr, t);
			}
			res.allTurns.add(add);
		}
		conn.readInt(RWS_SUB7);
		try {
			Class<?> placerCls = Class.forName(conn.readString());
			conn.readInt(RWS_SUB8);
			Method     met    = placerCls.getMethod("readPlacer", Connection.class); //$NON-NLS-1$
			UserPlacer placer = (UserPlacer) met.invoke(null, conn);
			conn.readInt(RWS_FINISH);
			return new CompleteWorld(res, placer);
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
	
	/**
	 * checks that the given seed was send by the given user
	 * 
	 * @param user     the user
	 * @param seedPart the seed which was send by the user
	 * 
	 * @return <code>true</code> if the seeds are equal
	 */
	public boolean isSeed(User user, byte[] seedPart) {
		if (seedPart.length != 16) throw new AssertionError("inavlid array size");
		byte[] mySeed = this.seed;
		if (mySeed == null) throw new IllegalStateException("the game did not yet start");
		Map<String, User> map    = this.root.subUsers();
		Collection<User>  values = map.values();
		User[]            users  = values.toArray(new User[values.size()]);
		Arrays.sort(users, null);
		int i;
		for (i = 1; user != users[i]; i++) {/**/}
		i *= 16;
		return Arrays.equals(seedPart, 0, 16, mySeed, i, i + 16);
	}
	
	/**
	 * start the game with the given seed
	 * <p>
	 * the seed needs to be build the following way:<br>
	 * the first 16 bytes are randomly generated by the root<br>
	 * every 16 bytes are generated by a sub world user. the sub world users are ordered according to their name (sort them with {@link String#compareTo(String)}).
	 * 
	 * @param s the random seed
	 */
	public void startGame(byte[] s) {
		startGame0(s, true);
	}
	
	private synchronized void startGame0(byte[] s, boolean modifyRoot) {
		if (s == null) throw new NullPointerException("seed is null");
		if (this.rnd != null) throw new IllegalStateException("the game already started");
		byte[] hash;
		synchronized (this.root) {
			hash            = calcHashOnlyWorld();
			this.starttiles = new Tile[this.tiles.length][this.tiles[0].length];
			for (int x = 0; x < this.starttiles.length; x++) {
				for (int y = 0; y < this.starttiles[x].length; y++) {
					this.starttiles[x][y] = this.tiles[x][y].copy();
				}
			}
			if (modifyRoot) this.root.allowNewUsers(false);
			Map<String, User> map    = this.root.subUsers();
			Collection<User>  values = map.values();
			User[]            users  = values.toArray(new User[values.size()]);
			if ((users.length + 1) * 16 != s.length) throw new IllegalArgumentException("inavlid array size");
			long sval = seed(s);
			this.seed = s;
			this.rnd  = new Random2(sval);
			Arrays.sort(users, null);
			shuffle(this.rnd, users);
			Tile.noCheck(() -> {
				try {
					this.placer.initilize(this, users, this.rnd);
				} catch (TurnExecutionException e) {
					throw new IllegalStateException(e);
				}
			});
		}
		threadStart(() -> executeNTL(hash, null));
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
	
	private static long val(byte[] s, int off) {
		return s[off] & 0xFF | ((s[off + 1] & 0xFFL) << 8) | ((s[off + 2] & 0xFFL) << 16) | ((s[off + 3] & 0xFFL) << 24) | ((s[off + 4] & 0xFFL) << 32)
				| ((s[off + 5] & 0xFFL) << 40) | ((s[off + 6] & 0xFFL) << 48) | ((s[off + 7] & 0xFFL) << 56);
	}
	
	/**
	 * returns <code>true</code> if the game is currently running and <code>false</code> if not
	 * <p>
	 * note that a game that once started can not be stopped
	 * 
	 * @return <code>true</code> if the game is currently running and <code>false</code> if not
	 */
	public synchronized boolean running() {
		return this.rnd != null;
	}
	
	/**
	 * value to send by the server to validate the game
	 * <ol>
	 * <li>Server: {@link #RW_VAL_GAME}</li>
	 * <li>Client: {@link #SUB0_VAL_GAME}</li>
	 * <li>Server: {@link #saveEverything(Connection)} (with empty passwords)</li>
	 * <li>Server: {@link #FIN_VAL_GAME}</li>
	 * </ol>
	 */
	public static final int RW_VAL_GAME   = 0x7C6879B3;
	/** @see #RW_VAL_GAME */
	public static final int SUB0_VAL_GAME = 0xD597E0E5;
	/** @see #RW_VAL_GAME */
	public static final int FIN_VAL_GAME  = 0xF6DB6A6B;
	
	/**
	 * send all data needed to validate the game over the connection
	 * 
	 * @param conn the connection which should be used to validate the game
	 * 
	 * @throws IOException if an IO error occurs
	 * 
	 * @see #RW_VAL_GAME
	 */
	public synchronized void validateGame(Connection conn) throws IOException {
		conn.blocked(() -> {
			conn.writeReadInt(RW_VAL_GAME, SUB0_VAL_GAME);
			saveEverything(conn, false);
			conn.writeInt(FIN_VAL_GAME);
		});
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized void addNextTurnListener(NextTurnListener listener) {
		this.nextTurnListeneres.add(listener);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized void removeNextTurnListener(NextTurnListener listener) {
		this.nextTurnListeneres.remove(listener);
	}
	
	/**
	 * finish the turn.<br>
	 * if the user is the root user this operation will fail if {@link #allowRootTurns()} is <code>false</code><br>
	 * if the user does not belong to this world this operation will fail
	 * <p>
	 * when the current user has finished its turn, the turn is executed in random order and then the next turn starts.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void finish(Turn t) {
		if (t.world.user() == this.root && !this.allowRootTurns) throw new UnsupportedOperationException("the root user is not allowed to execute turns");
		if (this.rnd == null) throw new IllegalStateException("the game did not yet start");
		if (!this.subWorlds.containsKey(t.world.user()) && t.world.user() != this.root) throw new AssertionError("unknown user!");
		for (EntityTurn e : t.turns()) {
			if (e.entity().owner() != t.world.user()) throw new IllegalArgumentException("turn uses not owned entities");
		}
		this.userTurns.put(t.world.user(), t);
		executeTurn();
	}
	
	private synchronized void executeTurn() {
		while (true) {
			int turnNum = turn();
			if (turnNum < 0) {
				return;
			}
			NavigableMap<String, User> map = new TreeMap<>(this.allowRootTurns ? this.root.users() : this.root.subUsers());
			turnNum %= map.size();
			User usr      = map.values().stream().skip(turnNum).findFirst().orElseThrow();
			Turn execTurn = this.userTurns.get(usr);
			if (execTurn == null) {
				return;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Connection            conn = Connection.createUnsecure(this.root, baos, this);
			try {
				execTurn.sendTurn(conn);
			} catch (IOException e) {
				throw new IOError(e);
			}
			this.allTurns.add(new HashMap<>(this.userTurns));
			for (EntityTurn et : randomOrder(execTurn.turns())) {
				try {
					Entity<?, ?> e = et.entity();
					Tile         t = this.tiles[e.x()][e.y()];
					executeEntityTurn(et, t);
				} catch (TurnExecutionException e) {
					System.err.println(String.format("error while executing the user turn: %s: %s", et.entity().owner(), e.type));
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println(String.format("error while executing the user turn: %s:", et.entity().owner()));
					e.printStackTrace();
				}
			}
			int pc = this.allowRootTurns ? this.user().users().size() : this.user().subUsers().size();
			for (int x = 0; x < this.tiles.length; x++) {
				Tile[] ts = this.tiles[x];
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y];
					t.ground().nextTurnNotify(pc);
					WorldThing<?, ?> w = t.build();
					if (w != null) w.nextTurnNotify(pc);
					t.resourcesStream().forEach(r -> r.nextTurnNotify(pc));
					t.unitsStream().forEach(u -> u.nextTurnNotify(pc));
				}
			}
			executeNTL(calcHash(), calcHash(baos.toByteArray()));
		}
	}
	
	@SuppressWarnings("preview")
	private void executeEntityTurn(EntityTurn turn, Tile t) throws TurnExecutionException {
		if (turn.entity().lives() <= 0) {
			throw new TurnExecutionException(ErrorType.DEAD);
		}
		switch (turn) {
		case MoveTurn(Unit u, List<MoveAct> dirs) -> {
			checkHasUnit(u, t);
			if (u.moveRange() < dirs.size()) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			for (MoveAct ma : dirs) {
				int x = u.x();
				int y = u.y();
				switch (ma) {
				case Direction dir -> {
					Tile newTile = this.tiles[x + dir.xadd][y + dir.yadd];
					Tile oldTile = this.tiles[x][y];
					u.changePos(x + dir.xadd, y + dir.yadd, newTile, oldTile);
					boolean b = false;
					try {
						newTile.addUnit(u);
						b = true;
						oldTile.removeUnit(u);
					} catch (TurnExecutionException tee) {
						u.changePos(x, y, oldTile, newTile);
						if (b) {
							newTile.removeUnit(u);
						}
						throw tee;
					}
				}
				case Attack(Entity<?, ?> enemy) -> {
					if (enemy.owner() == u.owner()) {
						throw new TurnExecutionException(ErrorType.INVALID_TURN);
					}
					if (enemy.lives() <= 0) {
						break;
					}
					u.attack(enemy);
					if (enemy.lives() <= 0) {
						switch (enemy) {
						case Unit eu -> this.tiles[eu.x()][eu.y()].removeUnit(eu);
						case Build eb -> this.tiles[eb.x()][eb.y()].removeBuild(eb);
						}
					}
					if (u.lives() <= 0) {
						this.tiles[x][y].removeUnit(u);
						throw new TurnExecutionException(ErrorType.DEAD);
					}
				}
				}
			}
		}
		case CarryTurn(Unit u, Resource res) -> {
			checkHasUnit(u, t);
			Build b = t.build();
			if (b == null) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			b.giveRes(u, res, this.rnd);
		}
		case StoreTurn(Unit u, Resource res) -> {
			checkHasUnit(u, t);
			Build b = t.build();
			if (b == null) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			b.store(u, res);
		}
		case MineTurn(Unit u, Resource res) -> {
			checkHasUnit(u, t);
			Resource r = t.removeResource(res, this.rnd);
			try {
				u.addResource(r);
			} catch (Throwable err) {
				t.addResource(r);
				throw err;
			}
		}
		case WorkTurn(Unit u) -> {
			checkHasUnit(u, t);
			Build b = t.build();
			if (b == null) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			b.work(u);
		}
		}
	}
	
	private static void checkHasUnit(Entity<?, ?> e, Tile t) throws TurnExecutionException {
		if (t.unitsStream().noneMatch(e::equals)) {
			throw new TurnExecutionException(ErrorType.UNKNOWN);
		}
	}
	
	private EntityTurn[] randomOrder(Collection<EntityTurn> c) {
		EntityTurn[] arr = c.toArray(new EntityTurn[c.size()]);
		shuffle(this.rnd, arr);
		return arr;
	}
	
	/**
	 * orders the elements in the given array by random
	 * 
	 * @param <T> the type of the array
	 * @param rnd the random to be used
	 * @param arr the array to be shuffled
	 */
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
	
	/**
	 * returns the World of the given user
	 * <p>
	 * if the user is the root user this world is returned, otherwise its {@link UserWorld} is returned
	 * 
	 * @param usr       the user
	 * @param usrModCnt the users modify count
	 * 
	 * @return the world of the user
	 */
	public synchronized World of(User usr, int usrModCnt) {
		if (usr == this.root) {
			return this;
		} else if (usr != this.root.get(usr.name())) {
			throw new AssertionError("unknown user!");
		} else {
			return usrOf(usr, usrModCnt);
		}
	}
	
	private UserWorld usrOf(User usr, int usrModCnt) {
		return this.subWorlds.compute(usr, (u, uw) -> {
			if (uw != null && usrModCnt == uw.modCnt) return uw;
			return UserWorld.usrOf(this, usr, usrModCnt);
		});
	}
	
	/**
	 * returns all entities on this world
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Map<User, List<Entity<?, ?>>> entities() {
		return entities(this.tiles);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorldThing<?, ?> get(UUID uuid) {
		return get(this.tiles, uuid);
	}
	
	/**
	 * this class can be used to build a {@link CompleteWorld}
	 * 
	 * @author Patrick Hechler
	 */
	public static final class Builder implements World {
		
		private List<NextTurnListener> nextTurnListeners = new ArrayList<>();
		private final Tile[][]         tiles;
		private final Random2          rnd;
		private final User             root;
		
		private int resourceMask = 7;
		
		/**
		 * creates a new {@link Builder} with the given root, x-len and y-len
		 * 
		 * @param usr  the root user of the builder
		 * @param xlen the x-len (width) of the builder
		 * @param ylen the y-len (height) of the builder
		 */
		public Builder(User usr, int xlen, int ylen) {
			this(usr, xlen, ylen, new Random2());
		}
		
		/**
		 * creates a new {@link Builder} with the given root, x-len, y-len and random
		 * 
		 * @param usr  the root user of the builder
		 * @param xlen the x-len (width) of the builder
		 * @param ylen the y-len (height) of the builder
		 * @param rnd  the random of the builder
		 */
		public Builder(User usr, int xlen, int ylen, Random2 rnd) {
			if (xlen <= 0 || ylen <= 0) { throw new IllegalArgumentException("xlen=" + xlen + " ylen=" + ylen); } //$NON-NLS-1$ //$NON-NLS-2$
			if (rnd == null) throw new NullPointerException("random is null");
			if (usr == null) throw new NullPointerException("user is null");
			this.root  = usr;
			this.tiles = new Tile[xlen][ylen];
			this.rnd   = rnd;
		}
		
		private Builder(User usr, Tile[][] tiles, Random2 rnd) {
			this.root  = usr;
			this.tiles = tiles;
			this.rnd   = rnd;
		}
		
		private void executeNTL() {
			for (NextTurnListener r : this.nextTurnListeners) {
				r.nextTurn(-1, null, null);
			}
		}
		
		/**
		 * just returns <code>-1</code>
		 * <p>
		 * {@inheritDoc}
		 * 
		 * @return <code>-1</code>
		 */
		@Override
		public int turn() {
			return -1;
		}
		
		/**
		 * replace all {@link Tile tiles} with <code>{@link Tile#ground} = {@link GroundType#NOT_EXPLORED_TYPE not-explored}</code> to a random value.<br>
		 * this method will try to generate random tiles which fit in their environment
		 */
		public void fillRandom() {
			placeSomePoints();
			if (grounds == null) {
				randomGrnd(0, 0);
			}
			while (fillOnce()) {/**/}
			executeNTL();
		}
		
		private boolean fillOnce() {
			boolean unfilledTile = false;
			for (int x = 0; x < this.tiles.length; x++) {
				for (int y = 0; y < this.tiles[x].length; y++) {
					if (this.tiles[x][y] != null && this.tiles[x][y].ground().type() != GroundType.NOT_EXPLORED_TYPE) {
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
						NeigbourTiles neigbours = new NeigbourTiles(xDown, xUp, yDown, yUp);
						this.tiles[x][y] = tile(x, y, neigbours);
					} else {
						unfilledTile = true;
					}
				}
			}
			return unfilledTile;
		}
		
		private Tile tile(int x, int y, NeigbourTiles neigbours) {
			Ground   ground   = type(x, y, neigbours);
			Resource resource = resource(x, y, neigbours);
			Tile     t        = new Tile(ground);
			if (resource != null) {
				t.addResource(resource);
			}
			return t;
		}
		
		private Resource resource(int x, int y, NeigbourTiles neigbours) {
			int   sum   = 0;
			int[] props = new int[resources.size() + 1];
			sum      = 2 + emptyCount(neigbours);
			props[0] = sum;
			for (int i = 1; i < props.length; i++) {
				int p = resources.get(i - 1).propability(this, x, y, neigbours);
				props[i]  = p;
				sum      += p;
			}
			int val = this.rnd.nextInt(sum);
			int i;
			for (i = 0; props[i] <= val; i++) {
				val -= props[i];
			}
			if (i >= resources.size()) {
				return null;
			}
			return resources.get(i).withNeigbours(this, x, y, neigbours);
		}
		
		private static int emptyCount(NeigbourTiles neigbours) {
			int res = 0;
			if (neigbours.xDown() != null && neigbours.xDown().resourceCount() == 0) {
				res++;
			}
			if (neigbours.yDown() != null && neigbours.yDown().resourceCount() == 0) {
				res++;
			}
			if (neigbours.xUp() != null && neigbours.xUp().resourceCount() == 0) {
				res++;
			}
			if (neigbours.yUp() != null && neigbours.yUp().resourceCount() == 0) {
				res++;
			}
			return res;
		}
		
		private Ground type(int x, int y, NeigbourTiles neigbours) {
			int   sum   = 0;
			int[] props = new int[grounds.size()];
			for (int i = 0; i < props.length; i++) {
				int p = grounds.get(i).propability(this, x, y, neigbours);
				props[i]  = p;
				sum      += p;
			}
			int val = this.rnd.nextInt(sum);
			int i;
			for (i = 0; props[i] <= val; i++) {
				val -= props[i];
			}
			return grounds.get(i).withNeigbours(this, x, y, neigbours);
		}
		
		private void placeSomePoints() {
			for (int x = 0; x < this.tiles.length; x += 8) {
				for (int y = x % 16 == 0 ? 0 : 4; y < this.tiles[x].length; y += 8) {
					if ((this.tiles[x][y] != null && this.tiles[x][y].ground().type() != GroundType.NOT_EXPLORED_TYPE)
							// do not place nearby other tiles and do not overwrite tiles
							|| (x > 0 && (this.tiles[x - 1][y] != null && this.tiles[x - 1][y].ground().type() != GroundType.NOT_EXPLORED_TYPE))
							|| (y > 0 && (this.tiles[x][y - 1] != null && this.tiles[x][y - 1].ground().type() != GroundType.NOT_EXPLORED_TYPE))
							|| (x + 1 < this.tiles.length && (this.tiles[x + 1][y] != null && this.tiles[x + 1][y].ground().type() != GroundType.NOT_EXPLORED_TYPE))
							|| (y + 1 < this.tiles[x].length
									&& (this.tiles[x][y + 1] != null && this.tiles[x][y + 1].ground().type() != GroundType.NOT_EXPLORED_TYPE))) {
						continue;
					}
					Tile t = new Tile(randomGrnd(x, y));
					this.tiles[x][y] = t;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						Resource res = randomRes(x, y);
						if (res != null) {
							t.addResource(res);
						}
					}
				}
			}
		}
		
		/**
		 * replace all {@link Tile tiles} with <code>{@link Tile#ground} = {@link GroundType#NOT_EXPLORED_TYPE not-explored}</code> to a random value.<br>
		 * unlike {@link #fillRandom()}, this method will just generate random tiles and ignores the environment of those tiles
		 */
		public void fillTotallyRandom() {
			for (int x = 0; x < this.tiles.length; x++) {
				Tile[] ts = this.tiles[x];
				for (int y = 0; y < ts.length; y++) {
					if (ts[y] != null && ts[y].ground().type() != GroundType.NOT_EXPLORED_TYPE) {
						continue;
					}
					Tile t = new Tile(randomGrnd(x, y));
					ts[y] = t;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						Resource res = randomRes(x, y);
						if (res != null) {
							t.addResource(res);
						}
					}
				}
			}
			executeNTL();
		}
		
		private static List<GroundType>   grounds;
		private static List<ResourceType> resources;
		
		private Resource randomRes(int x, int y) {
			if (resources == null) {
				randomRes(x, y);
			}
			if (resources.isEmpty()) {
				return null;
			}
			return resources.get(this.rnd.nextInt(resources.size())).withRandomValues(this, this.rnd, x, y);
		}
		
		private Ground randomGrnd(int x, int y) {
			if (grounds == null) {
				initGrnd();
			}
			return grounds.get(this.rnd.nextInt(grounds.size())).withRandomValues(this, this.rnd, x, y);
		}
		
		private static synchronized void initGrnd() throws AssertionError {
			if (grounds != null) {
				return;
			}
			List<GroundType>   gl = new ArrayList<>();
			List<ResourceType> rl = new ArrayList<>();
			for (Addon a : Addons.addons().values()) {
				for (AddableType<?, ?> add : a.add.values()) {
					if (add instanceof GroundType gt && gt != GroundType.NOT_EXPLORED_TYPE) {
						gl.add(gt);
					} else if (add instanceof ResourceType rt) {
						rl.add(rt);
					}
				}
			}
			if (gl.isEmpty()) {
				throw new AssertionError("no ground found!");
			}
			grounds   = gl;
			resources = rl;
		}
		
		/**
		 * sets the resource mask<br>
		 * the resource mask is used to check if a tile should get a resource or not<br>
		 * every one bit adds a 50/50 chance
		 * 
		 * @param resourceMask the new resource mask
		 */
		public void resourceMask(int resourceMask) { this.resourceMask = resourceMask; }
		
		/**
		 * returns the current resource mask
		 * 
		 * @return the current resource mask
		 * 
		 * @see #resourceMask(int)
		 */
		public int resourceMask() { return this.resourceMask; }
		
		/** {@inheritDoc} */
		@Override
		public User user() {
			return this.root;
		}
		
		/** {@inheritDoc} */
		@Override
		public int xlen() {
			return this.tiles.length;
		}
		
		/** {@inheritDoc} */
		@Override
		public int ylen() {
			return this.tiles[0].length;
		}
		
		/**
		 * returns the tile at the given position<br>
		 * if there is no tile yet, a new visible {@link GroundType#NOT_EXPLORED_TYPE not-explored} tile will be generated
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public Tile tile(int x, int y) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(GroundType.NOT_EXPLORED_GRND);
			}
			return this.tiles[x][y];
		}
		
		/**
		 * returns the tile at the given position
		 * <p>
		 * the difference between {@link #tile(int, int)} and {@link #get(int, int)} is, that this method returns <code>null</code>, if the tile is not set, while
		 * {@link #tile(int, int)} creates a new non explored tile in this case
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * 
		 * @return the at the given position or <code>null</code>, if there is not a tile (yet)
		 */
		public Tile get(int x, int y) {
			return this.tiles[x][y];
		}
		
		/**
		 * sets the type of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param g the new type of the tile
		 */
		public void setGround(int x, int y, Ground g) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(g);
			}
			executeNTL();
		}
		
		/**
		 * adds the resource of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param r the new resource type of the tile
		 */
		public void addResource(int x, int y, Resource r) {
			if (this.tiles[x][y] == null) {
				return;
			}
			this.tiles[x][y].addResource(r);
			executeNTL();
		}
		
		/**
		 * removes the resource of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param r the resource to be removed
		 */
		public void removeResource(int x, int y, Resource r) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(GroundType.NOT_EXPLORED_GRND);
			}
			try {
				this.tiles[x][y].removeResource(r, this.rnd);
				executeNTL();
			} catch (TurnExecutionException e) {
				throw new IllegalStateException(e);
			}
		}
		
		/**
		 * sets the type and resource of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param g the new type of the tile
		 * @param r the new resource of the tile
		 */
		public void setGroundResource(int x, int y, Ground g, Resource r) {
			if (g == null) throw new NullPointerException("the ground is null");
			if (r == null) throw new NullPointerException("the resource is null");
			this.tiles[x][y] = new Tile(g);
			this.tiles[x][y].addResource(r);
			executeNTL();
		}
		
		/**
		 * sets the tile at the given coordinates at a copy of the given tile
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param t the new original value of the tile
		 */
		public void setTile(int x, int y, Tile t) {
			this.tiles[x][y] = t.copy();
			executeNTL();
		}
		
		/**
		 * sets the building at the given coordinates
		 * 
		 * @param x     the x coordinate of the building
		 * @param y     the y coordinate of the building
		 * @param build the building to be placed at the given position
		 */
		public void setBuild(int x, int y, Build build) {
			tile(x, y).setBuild(build);
			executeNTL();
		}
		
		/**
		 * adds the unit at the given coordinates
		 * 
		 * @param x    the x coordinate of the unit
		 * @param y    the y coordinate of the unit
		 * @param unit the unit to be placed at the given position
		 */
		public void addUnit(int x, int y, Unit unit) {
			try {
				tile(x, y).addUnit(unit);
				executeNTL();
			} catch (TurnExecutionException e) {
				throw new IllegalStateException(e);
			}
		}
		
		/**
		 * create a {@link CompleteWorld} from the current builder
		 * 
		 * @return the newly created {@link CompleteWorld}
		 * 
		 * @throws IllegalStateException if the world contains not yet explored tiles
		 */
		public CompleteWorld create() throws IllegalStateException {
			return create(this.root, this.tiles);
		}
		
		/**
		 * create a {@link CompleteWorld} from the given tiles
		 * 
		 * @param root  the root user of the new world
		 * @param tiles the tiles of the new world
		 * 
		 * @return the newly created root world
		 * 
		 * @throws IllegalStateException if the tiles are not valid for a root world
		 */
		public static CompleteWorld create(User root, Tile[][] tiles) throws IllegalStateException {
			return create(root, tiles, null);
		}
		
		/**
		 * returns <code>true</code> if this {@link Builder} can currently {@link #create() create} a root world and <code>false</code> if an invocation to
		 * {@link #create()} would result in an error
		 * 
		 * @return <code>true</code> if this {@link Builder} can currently {@link #create() create} a root world and <code>false</code> if not
		 */
		public boolean buildable() {
			for (int x = 0; x < this.tiles.length; x++) {
				Tile[] ts = this.tiles[x].clone();
				if (ts.length != this.tiles[0].length) { return false; }
				for (int y = 0; y < ts.length; y++) {
					Tile t = ts[y];
					if (t == null) { return false; }
					if (t.ground() == null) { return false; }
					if (t.ground().type() == GroundType.NOT_EXPLORED_TYPE) { return false; }
				}
			}
			return true;
		}
		
		/**
		 * create a new {@link CompleteWorld} using the given tiles, root user and user placer
		 * 
		 * @param root   the root user of the new world
		 * @param tiles  the tiles of the new world
		 * @param placer the placer of the world (or <code>null</code> if a {@link DefaultUserPlacer} should be used)
		 * 
		 * @return the newly created root world
		 * 
		 * @throws IllegalStateException if the tiles are not valid for a root world
		 */
		public static CompleteWorld create(User root, Tile[][] tiles, UserPlacer placer) throws IllegalStateException {
			Tile[][] copy = new Tile[tiles.length][tiles[0].length];
			for (int x = 0; x < copy.length; x++) {
				Tile[] ts  = copy[x];
				Tile[] ots = tiles[x];
				if (ts.length != ots.length) throw new IllegalStateException("the given array has no rectangular form!");
				for (int y = 0; y < ts.length; y++) {
					Tile t = ots[y];
					if (t == null) throw new IllegalStateException("there is a null tile!");
					t = t.copy();
					if (t.ground() == null) throw new IllegalStateException("there is a tile with a null ground!");
					if (t.ground().type() == GroundType.NOT_EXPLORED_TYPE) throw new IllegalStateException("there is a tile with a not yet explored ground!");
					ts[y] = t;
				}
			}
			return new CompleteWorld(root, copy, placer);
		}
		
		/**
		 * create a new {@link Builder} with the given tiles and root user
		 * 
		 * @param root  the root user of the builder
		 * @param tiles the tiles of the builder
		 * 
		 * @return the newly created builder
		 */
		public static Builder createBuilder(User root, Tile[][] tiles) {
			return createBuilder(root, tiles, new Random2());
		}
		
		/**
		 * create a new {@link Builder} with the given tiles, root user and random
		 * 
		 * @param root  the root user of the new builder
		 * @param tiles the tiles of the new builder
		 * @param rnd   the random of the new builder
		 * 
		 * @return the newly created builder
		 */
		public static Builder createBuilder(User root, Tile[][] tiles, Random2 rnd) {
			Tile[][] copy = tiles.clone(); // do clone, so the rectangular form can not be destroyed
			int      ylen = copy[0].length;
			for (int x = 0; x < copy.length; x++) { // only enforce the rectangular form, the builder is allowed to contain invalid tiles
				if (copy[x].length != ylen) throw new IllegalStateException("the given array has no rectangular form!");
			}
			return new Builder(root, tiles, rnd);
		}
		
		/** {@inheritDoc} */
		@Override
		public void addNextTurnListener(NextTurnListener listener) {
			this.nextTurnListeners.add(listener);
		}
		
		/** {@inheritDoc} */
		@Override
		public void removeNextTurnListener(NextTurnListener listener) {
			this.nextTurnListeners.remove(listener);
		}
		
		/**
		 * returns all entities on this world
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public Map<User, List<Entity<?, ?>>> entities() {
			return CompleteWorld.entities(this.tiles);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public WorldThing<?, ?> get(UUID uuid) {
			return CompleteWorld.get(this.tiles, uuid);
		}
		
		/**
		 * this operation is not supported, this method will always throw an {@link UnsupportedOperationException}
		 * 
		 * @throws UnsupportedOperationException always
		 */
		@Override
		public void finish(@SuppressWarnings("unused") Turn t) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("the build world does not support turns!");
		}
		
	}
	
	static WorldThing<?, ?> get(Tile[][] tiles, UUID uuid) {
		for (int x = 0; x < tiles.length; x++) {
			Tile[] ts = tiles[x];
			for (int y = 0; y < ts.length; y++) {
				Tile t = ts[y];
				if (t == null) {
					continue;
				}
				WorldThing<?, ?> wt = t.ground();
				if (wt.uuid.equals(uuid)) {
					return wt;
				}
				wt = t.build();
				if (wt != null && wt.uuid.equals(uuid)) {
					return wt;
				}
				Optional<? extends WorldThing<?, ?>> o = t.unitsStream().filter(u -> uuid.equals(u.uuid)).findAny();
				if (o.isPresent()) {
					return o.get();
				}
				o = t.resourcesStream().filter(r -> uuid.equals(r.uuid)).findAny();
				if (o.isPresent()) {
					return o.get();
				}
			}
		}
		return null;
	}
	
	static Map<User, List<Entity<?, ?>>> entities(Tile[][] tiles) {
		Map<User, List<Entity<?, ?>>> result = new HashMap<>();
		for (int x = 0; x < tiles.length; x++) {
			Tile[] ts = tiles[x];
			for (int y = 0; y < ts.length; y++) {
				Tile t = ts[y];
				if (t == null) {
					continue;
				}
				t.unitsStream().forEach(u -> add(result, u));
				add(result, t.build());
			}
		}
		return result;
	}
	
	private static void add(Map<User, List<Entity<?, ?>>> result, Entity<?, ?> u) {
		if (u != null) {
			result.computeIfAbsent(u.owner(), usr -> new ArrayList<>()).add(u);
		}
	}
	
}
