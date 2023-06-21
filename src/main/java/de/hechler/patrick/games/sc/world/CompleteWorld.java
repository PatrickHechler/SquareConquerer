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
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.turn.CarryTurn;
import de.hechler.patrick.games.sc.turn.Direction;
import de.hechler.patrick.games.sc.turn.EntityTurn;
import de.hechler.patrick.games.sc.turn.MoveTurn;
import de.hechler.patrick.games.sc.turn.StoreTurn;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.init.DefaultUserPlacer;
import de.hechler.patrick.games.sc.world.init.UserPlacer;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.games.sc.world.tile.TileImpl;
import de.hechler.patrick.utils.objects.Random2;

public class CompleteWorld implements World, Iterable<CompleteWorld> {
	
	private final User                             root;
	private final Tile[][]                         tiles;
	private final UserPlacer                       placer;
	private final Map<User, UserWorld>             subWorlds;
	private final List<BiConsumer<byte[], byte[]>> nextTurnListeneres;
	private final Map<User, Turn>                  userTurns;
	private final List<Map<User, Turn>>            allTurns;
	private volatile boolean                       allowRootTurns;
	private volatile Tile[][]                      starttiles;
	private volatile byte[]                        seed;
	private volatile Random2                       rnd;
	
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
		Connection            conn = Connection.createUnsecure(this.root, baos);
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
		Connection            conn = Connection.createUnsecure(this.root, baos);
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
		for (BiConsumer<byte[], byte[]> r : this.nextTurnListeneres) {
			r.accept(myhash, turnhash);
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
		return new Iterator<CompleteWorld>() {
			
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
				t.sendTurn(conn, false);
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
		Tile[][]      tiles = RemoteWorld.loadWorld(conn, root.users());
		CompleteWorld res   = Builder.create(root, tiles);
		conn.readInt(RWS_SUB2);
		res.starttiles = RemoteWorld.loadWorld(conn, root.users());
		conn.readInt(RWS_SUB2);
		res.seed = seed;
		res.rnd  = new Random2(curSeed);
		for (int remain = conn.readPos(); remain > 0; remain--) {
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
				conn.readInt(Turn.CMD_TURN);
				t.retrieveTurn(conn, false);
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
			TileImpl.noCheck(() -> this.placer.initilize(this, users, this.rnd));
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
	public synchronized void addNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.nextTurnListeneres.add(listener);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) {
		this.nextTurnListeneres.remove(listener);
	}
	
	/**
	 * finish the turn.<br>
	 * if the user is the root user this operation will fail if {@link #allowRootTurns()} is <code>false</code><br>
	 * if the user does not belong to this world this operation will fail
	 * <p>
	 * when all users have finished their turn, all turns are executed in random order and then the next turn starts.
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
		if (this.userTurns.size() >= (this.allowRootTurns ? this.root.users().keySet().size() + 1 : this.root.users().keySet().size())) {
			executeTurn();
		}
	}
	
	private synchronized void executeTurn() {
		List<EntityTurn>      list = new ArrayList<>();                         // list is sorted: user names and then entity turns
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Connection            conn = Connection.createUnsecure(this.root, baos);
		for (Turn userturn : this.userTurns.values()) {
			list.addAll(userturn.turns());
			try {
				userturn.sendTurn(conn, false);
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		this.allTurns.add(new HashMap<>(this.userTurns));
		for (EntityTurn et : randomOrder(list)) {
			try {
				Entity<?, ?> e = et.entity();
				Tile         t = this.tiles[e.x()][e.y()];
				executeEntityTurn(et, e, t);
			} catch (TurnExecutionException e) {
				System.err.println(String.format("error while executing the user turn: %s: %s", et.entity().owner(), e.type));
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(String.format("error while executing the user turn: %s: %s", et.entity().owner(), ErrorType.UNKNOWN));
				e.printStackTrace();
			}
		}
		executeNTL(calcHash(), calcHash(baos.toByteArray()));
	}
	
	@SuppressWarnings("preview")
	private void executeEntityTurn(EntityTurn turn, Entity<?, ?> e, Tile t) throws TurnExecutionException {
		switch (turn) {
		case MoveTurn mt -> {
			checkHasUnit(e, t);
			Unit            u    = (Unit) e;
			List<Direction> dirs = mt.dirs();
			if (u.moveRange() < dirs.size()) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			for (Direction dir : dirs) {
				int  x       = e.x();
				int  y       = e.y();
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
		}
		case CarryTurn ct -> {
			checkHasUnit(e, t);
			Build b = t.build();
			if (b == null) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			if (!(e instanceof Unit u)) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			b.giveRes(u, ct.res(), ct.amount());
		}
		case StoreTurn st -> {
			checkHasUnit(e, t);
			Build b = t.build();
			if (b == null) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			if (!(e instanceof Unit u)) throw new TurnExecutionException(ErrorType.INVALID_TURN);
			b.store(u, st.resource(), st.amount());
		}
		default -> throw new AssertionError(String.format(UNKNOWN_ENTITY_TURN_TYPE, turn.getClass()));
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
			if (uw != null && u.modifyCount() == uw.modCnt) return uw;
			return UserWorld.usrOf(this, usr, usrModCnt);
		});
	}
	
	/**
	 * returns all entities on this world
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Map<User, List<Entity>> entities() {
		return entities(this.tiles);
	}
	
	/**
	 * this class can be used to build a {@link RootWorld}
	 * 
	 * @author Patrick Hechler
	 */
	public static final class Builder implements World {
		
		private static final OreResourceType[] RES = OreResourceType.values();
		private static final GroundType[]      TYPES;
		
		static {
			GroundType[] v   = GroundType.values();
			int          len = v.length - 1;
			TYPES = new GroundType[len];
			System.arraycopy(v, 1, TYPES, 0, len);
		}
		
		private List<BiConsumer<byte[], byte[]>> nextTurnListeners = new ArrayList<>();
		private final Tile[][]                   tiles;
		private final Random2                    rnd;
		private final RootUser                   root;
		
		private int resourceMask = 7;
		
		/**
		 * creates a new {@link Building} with the given root, x-len and y-len
		 * 
		 * @param usr  the root user of the builder
		 * @param xlen the x-len (width) of the builder
		 * @param ylen the y-len (height) of the builder
		 */
		public Builder(RootUser usr, int xlen, int ylen) {
			this(usr, xlen, ylen, new Random2());
		}
		
		/**
		 * creates a new {@link Build} with the given root, x-len, y-len and random
		 * 
		 * @param usr  the root user of the builder
		 * @param xlen the x-len (width) of the builder
		 * @param ylen the y-len (height) of the builder
		 * @param rnd  the random of the builder
		 */
		public Builder(RootUser usr, int xlen, int ylen, Random2 rnd) {
			if (xlen <= 0 || ylen <= 0) { throw new IllegalArgumentException("xlen=" + xlen + " ylen=" + ylen); } //$NON-NLS-1$ //$NON-NLS-2$
			if (rnd == null) throw new NullPointerException(RND_IS_NULL);
			if (usr == null) throw new NullPointerException(USR_IS_NULL);
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
			for (BiConsumer<byte[], byte[]> r : this.nextTurnListeners) {
				r.accept(null, null);
			}
		}
		
		/**
		 * replace all {@link Tile tiles} with <code>{@link Tile#ground} = {@link GroundType#NOT_EXPLORED not-explored}</code> to a random value.<br>
		 * this method will try to generate random tiles which fit in their environment
		 */
		public void fillRandom() {
			placeSomePoints();
			while (fillOnce()) {/**/}
			executeNTL();
		}
		
		private boolean fillOnce() {
			boolean unfilledTile = false;
			for (int x = 0; x < this.tiles.length; x++) {
				for (int y = 0; y < this.tiles[x].length; y++) {
					if (this.tiles[x][y] != null && this.tiles[x][y].ground != GroundType.NOT_EXPLORED) {
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
			GroundType      type = type(xDown, xUp, yDown, yUp);
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
		
		private GroundType type(Tile xDown, Tile xUp, Tile yDown, Tile yUp) {
			int ocean = ocean(xDown) + ocean(xUp) + ocean(yDown) + ocean(yUp);
			int land  = land(xDown) + land(xUp) + land(yDown) + land(yUp);
			if (ocean != 0 && land != 0) { return GroundType.WATER_NORMAL; }
			int        water    = water(xDown) + water(xUp) + water(yDown) + water(yUp);
			int        flat     = flat(xDown) + flat(xUp) + flat(yDown) + flat(yUp);
			int        mountain = mountain(xDown) + mountain(xUp) + mountain(yDown) + mountain(yUp);
			GroundType type     = rawType(xDown, xUp, yDown, yUp, ocean, water, flat, mountain);
			if (type.isLand() && !type.isMountain()) {
				int hill    = hill(xDown) + hill(xUp) + hill(yDown) + hill(yUp);
				int posHill = (mountain * 4) + (hill * 2) + 1;
				int posFlat = (flat * 2) + 1;
				int rndVal1 = this.rnd.nextInt(posHill + posFlat);
				if (rndVal1 < posHill) {
					type = type.addHill(true, true);
				}
			} else if (type.isWater()) {
				if (land == 0) {
					int posOcean  = ocean * 2 + 1;
					int posNormal = Math.max((water - ocean) * 2 + 1, 1);
					int rndVal1   = this.rnd.nextInt(posOcean + posNormal);
					if (rndVal1 < posOcean) {
						type = GroundType.WATER_DEEP;
					}
				}
			} else if (!type.isMountain()) throw new AssertionError(UNKNOWN_TILE_TYPE + type.name());
			return type;
		}
		
		private GroundType rawType(Tile xDown, Tile xUp, Tile yDown, Tile yUp, int ocean, int water, int flat, int mountain) {
			int sand   = sand(xDown) + sand(xUp) + sand(yDown) + sand(yUp);
			int grass  = grass(xDown) + grass(xUp) + grass(yDown) + grass(yUp);
			int forest = forest(xDown) + forest(xUp) + forest(yDown) + forest(yUp);
			int swamp  = swamp(xDown) + swamp(xUp) + swamp(yDown) + swamp(yUp);
			// ocean disables everything except water
			// flat disables mountain
			int        posWater    = (water * 2) + 1;
			int        posMountain = ocean == 0 ? Math.max(mountain - (flat * 2) + 1, 1) : 0;
			int        posSand     = ocean == 0 ? (sand * 2) + 1 : 0;
			int        posGrass    = ocean == 0 ? (grass * 2) + 1 : 0;
			int        posForest   = ocean == 0 ? (forest * 2) + 1 : 0;
			int        posSwamp    = ocean == 0 ? (swamp * 2) + 1 : 0;
			int        rndVal0     = this.rnd.nextInt(posWater + posMountain + posSand + posGrass + posForest + posSwamp);
			GroundType type        = null;
			if (rndVal0 >= posWater) rndVal0 -= posWater;
			else type = GroundType.WATER_NORMAL;
			if (rndVal0 >= posMountain) rndVal0 -= posMountain;
			else if (type == null) type = GroundType.MOUNTAIN;
			if (rndVal0 >= posSand) rndVal0 -= posSand;
			else if (type == null) type = GroundType.SAND;
			if (rndVal0 >= posGrass) rndVal0 -= posGrass;
			else if (type == null) type = GroundType.GRASS;
			if (rndVal0 >= posForest) rndVal0 -= posForest;
			else if (type == null) type = GroundType.FOREST;
			if (rndVal0 >= posSwamp && type == null) throw new AssertionError("illegal random value (this sould not be possible)"); //$NON-NLS-1$
			else if (type == null) type = GroundType.SWAMP;
			return type;
		}
		
		// @formatter:off
		private static int swamp(Tile t)    { return t != null && t.ground.isSwamp()    ? 1 : 0; }
		private static int forest(Tile t)   { return t != null && t.ground.isForest()   ? 1 : 0; }
		private static int grass(Tile t)    { return t != null && t.ground.isGrass()    ? 1 : 0; }
		private static int sand(Tile t)     { return t != null && t.ground.isSand()     ? 1 : 0; }
		private static int land(Tile t)     { return t != null && t.ground.isLand()     ? 1 : 0; }
		private static int flat(Tile t)     { return t != null && t.ground.isFlat()     ? 1 : 0; }
		private static int hill(Tile t)     { return t != null && t.ground.isHill()     ? 1 : 0; }
		private static int ocean(Tile t)    { return t != null && t.ground.isDeep()    ? 1 : 0; }
		private static int water(Tile t)    { return t != null && t.ground.isWater()    ? 1 : 0; }
		private static int mountain(Tile t) { return t != null && t.ground.isMountain() ? 1 : 0; }
		// @formatter:on
		
		private void placeSomePoints() {
			for (int x = 0; x < this.tiles.length; x += 8) {
				for (int y = x % 16 == 0 ? 0 : 4; y < this.tiles[x].length; y += 8) {
					if ((this.tiles[x][y] != null && this.tiles[x][y].ground != GroundType.NOT_EXPLORED) // do not place nearby other tiles and do not overwrite
							// tiles
							|| (x > 0 && (this.tiles[x - 1][y] != null && this.tiles[x - 1][y].ground != GroundType.NOT_EXPLORED))
							|| (y > 0 && (this.tiles[x][y - 1] != null && this.tiles[x][y - 1].ground != GroundType.NOT_EXPLORED))
							|| (x + 1 < this.tiles.length && (this.tiles[x + 1][y] != null && this.tiles[x + 1][y].ground != GroundType.NOT_EXPLORED))
							|| (y + 1 < this.tiles[x].length && (this.tiles[x][y + 1] != null && this.tiles[x][y + 1].ground != GroundType.NOT_EXPLORED))) {
						continue;
					}
					int             rndVal = this.rnd.nextInt(TYPES.length << 1);
					GroundType      t      = rndVal >= TYPES.length ? GroundType.WATER_DEEP : TYPES[rndVal];
					OreResourceType r      = OreResourceType.NONE;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						r = RES[this.rnd.nextInt(RES.length)];
					}
					this.tiles[x][y] = new Tile(t, r, true);
				}
			}
		}
		
		/**
		 * replace all {@link Tile tiles} with <code>{@link Tile#ground} = {@link GroundType#NOT_EXPLORED not-explored}</code> to a random value.<br>
		 * unlike {@link #fillRandom()}, this method will just generate random tiles and ignores the environment of those tiles
		 */
		public void fillTotallyRandom() {
			for (Tile[] ts : this.tiles) {
				for (int i = 0; i < ts.length; i++) {
					if (ts[i] != null && ts[i].ground != GroundType.NOT_EXPLORED) {
						continue;
					}
					GroundType      t = TYPES[this.rnd.nextInt(TYPES.length)]; // skip not explored
					OreResourceType r = OreResourceType.NONE;
					if ((this.rnd.nextInt() & this.resourceMask) == 0) {
						r = RES[this.rnd.nextInt(RES.length)];
					}
					ts[i] = new Tile(t, r, true);
				}
			}
			executeNTL();
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
		public RootUser user() {
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
		 * if there is no tile yet, a new visible {@link GroundType#NOT_EXPLORED not-explored} tile {@link OreResourceType#NONE without} resource will be generated
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public Tile tile(int x, int y) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(GroundType.NOT_EXPLORED, OreResourceType.NONE, true);
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
		 * @param t the new type of the tile
		 */
		public void set(int x, int y, GroundType t) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(t, OreResourceType.NONE, true);
			} else {
				this.tiles[x][y] = new Tile(t, this.tiles[x][y].resource, true);
			}
			executeNTL();
		}
		
		/**
		 * sets the resource of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param r the new resource type of the tile
		 */
		public void set(int x, int y, OreResourceType r) {
			if (this.tiles[x][y] == null) {
				this.tiles[x][y] = new Tile(GroundType.NOT_EXPLORED, r, true);
			} else {
				this.tiles[x][y] = new Tile(this.tiles[x][y].ground, r, true);
			}
			executeNTL();
		}
		
		/**
		 * sets the type and resource of the tile at the given coordinates
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param t the new type of the tile
		 * @param r the new resource of the tile
		 */
		public void set(int x, int y, GroundType t, OreResourceType r) {
			if (t == null) throw new NullPointerException(TYPE_IS_NULL);
			if (r == null) throw new NullPointerException(RESOURCE_IS_NULL);
			this.tiles[x][y] = new Tile(t, r, true);
			executeNTL();
		}
		
		/**
		 * sets the tile at the given coordinates at a copy of the given tile
		 * 
		 * @param x the x coordinate of the tile
		 * @param y the y coordinate of the tile
		 * @param t the new original value of the tile
		 */
		public void set(int x, int y, Tile t) {
			this.tiles[x][y] = t.copy();
			executeNTL();
		}
		
		/**
		 * create a {@link CompleteWorld} from the current builder
		 * 
		 * @return the newly created {@link CompleteWorld}
		 * 
		 * @throws IllegalStateException if the world contains not yet explored tiles
		 */
		public RootWorld create() throws IllegalStateException {
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
		public static RootWorld create(RootUser root, Tile[][] tiles) throws IllegalStateException {
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
					if (t.ground == null || t.resource == null) { return false; }
					if (t.ground == GroundType.NOT_EXPLORED) { return false; }
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
		public static RootWorld create(RootUser root, Tile[][] tiles, UserPlacer placer) throws IllegalStateException {
			Tile[][] copy = new Tile[tiles.length][tiles[0].length];
			for (int x = 0; x < copy.length; x++) {
				Tile[] ts  = copy[x];
				Tile[] ots = tiles[x];
				if (ts.length != ots.length) throw new IllegalStateException(NON_RECTANGULAR_FORM);
				for (int y = 0; y < ts.length; y++) {
					Tile t = ots[y];
					if (t == null) throw new IllegalStateException(NULL_TILE);
					t = t.copy();
					if (t.ground == null || t.resource == null) throw new NullPointerException(NULL_TYPE_OR_RESOURCE);
					if (t.ground == GroundType.NOT_EXPLORED) throw new IllegalStateException(NOT_EXPLORED_TILE);
					ts[y] = t;
				}
			}
			return new RootWorld(root, copy, placer);
		}
		
		/**
		 * create a new {@link Builder} with the given tiles and root user
		 * 
		 * @param root  the root user of the builder
		 * @param tiles the tiles of the builder
		 * 
		 * @return the newly created builder
		 */
		public static Builder createBuilder(RootUser root, Tile[][] tiles) {
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
		public static Builder createBuilder(RootUser root, Tile[][] tiles, Random2 rnd) {
			Tile[][] copy = tiles.clone(); // do clone, so the rectangular form can not be destroyed
			int      ylen = copy[0].length;
			for (int x = 0; x < copy.length; x++) { // only enforce the rectangular form, the builder is allowed to contain invalid tiles
				if (copy[x].length != ylen) throw new IllegalStateException(NON_RECTANGULAR_FORM);
			}
			return new Builder(root, tiles, rnd);
		}
		
		/** {@inheritDoc} */
		@Override
		public void addNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.nextTurnListeners.add(listener); }
		
		/** {@inheritDoc} */
		@Override
		public void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.nextTurnListeners.remove(listener); }
		
		/**
		 * returns all entities on this world
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public Map<User, List<Entity>> entities() {
			return RootWorld.entities(this.tiles);
		}
		
		/**
		 * this operation is not supported, this method will always throw an {@link UnsupportedOperationException}
		 * 
		 * @throws UnsupportedOperationException always
		 */
		@Override
		public void finish(Turn t) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(BUILD_WORLD_NO_SUPPORT_TURNS);
		}
		
		/**
		 * sets the building at the given coordinates
		 * 
		 * @param x     the x coordinate of the building
		 * @param y     the y coordinate of the building
		 * @param build the building to be placed at the given position
		 */
		public void set(int x, int y, Building build) {
			tile(x, y).build(build);
			executeNTL();
		}
		
		/**
		 * sets the unit at the given coordinates
		 * 
		 * @param x    the x coordinate of the unit
		 * @param y    the y coordinate of the unit
		 * @param unit the unit to be placed at the given position
		 */
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
				add(result, t.build());
			}
		}
		return result;
	}
	
	private static void add(Map<User, List<Entity>> result, Entity u) {
		if (u != null) {
			result.computeIfAbsent(u.owner(), usr -> new ArrayList<>()).add(u);
		}
	}
	
	private void addNopw(String string1) {}
	
}
