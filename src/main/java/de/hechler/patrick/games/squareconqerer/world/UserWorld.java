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

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

/**
 * this class filters everything the user can not see from a world.<br>
 * if the user could see a tile once, its tile type and resource are saved, but everything other is discarded
 * 
 * @author Patrick Hechler
 */
public final class UserWorld implements World {
	
	private static final String USR_OF_ONLY_WHEN_USER_CHANGE = "user of is only possible, when the user changes!";
	private static final String THIS_METHOD_IS_INTERN        = "this method is intern";
	
	private static final Tile NOT_EXPLORED = new Tile(GroundType.NOT_EXPLORED, OreResourceType.NONE, false);
	
	private final World             world;
	private final User              usr;
	private Map<User, List<Entity>> entities;
	private Tile[][]                cach;
	private boolean[][]             visible;
	
	/**
	 * this variable stores the modify count of the user at creation time of the world
	 */
	public final int modCnt;
	
	private UserWorld(World world, User usr, int modCnt) {
		this.world  = world;
		this.usr    = usr;
		this.modCnt = modCnt;
		world.addNextTurnListener(this::nextTurn);
	}
	
	/**
	 * returns a {@link World} with the given user.<br>
	 * if the user is the worlds {@link World#user() user}, the given world is returned.<br>
	 * otherwise a wrapping {@link UserWorld} will be returned
	 * <p>
	 * the given world must support finishing the turn of different users (see {@link #finish(Turn)})
	 * 
	 * @param w      the original {@link World}
	 * @param usr    the user
	 * @param modCnt the modify count of the user
	 * 
	 * @return a world with the given user, which is base on the given world
	 */
	public static World of(World w, User usr, int modCnt) {
		usr.checkModCnt(modCnt);
		if (w.user() == usr) return w;
		return new UserWorld(w, usr, modCnt);
	}
	
	/**
	 * returns a {@link World} with the given user.<br>
	 * if the user is the worlds {@link World#user() user}, this method fails with an {@link IllegalStateException}.<br>
	 * otherwise a wrapping {@link UserWorld} will be returned
	 * <p>
	 * the given world must support finishing the turn of different users (see {@link #finish(Turn)})
	 * 
	 * @param w      the original {@link World}
	 * @param usr    the user
	 * @param modCnt the modify count of the user
	 * 
	 * @return a world with the given user, which is base on the given world
	 * 
	 * @throws IllegalStateException if the worlds user is the given user
	 */
	public static UserWorld usrOf(World w, User usr, int modCnt) throws IllegalStateException {
		usr.checkModCnt(modCnt);
		if (w.user() == usr) throw new IllegalStateException(USR_OF_ONLY_WHEN_USER_CHANGE);
		return new UserWorld(w, usr, modCnt);
	}
	
	Tile[][] cach() {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != RootWorld.class) {
			throw new IllegalCallerException(THIS_METHOD_IS_INTERN);
		}
		return this.cach;
	}
	
	/**
	 * returns the user of this user world
	 * <p>
	 * this method checks the users mod count
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public User user() {
		this.usr.checkModCnt(this.modCnt);
		return this.usr;
	}
	
	/**
	 * returns the {@link World#xlen() x-len} of the backing world
	 * <p>
	 * this method checks the users mod count
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int xlen() {
		this.usr.checkModCnt(this.modCnt);
		return this.world.xlen();
	}
	
	/**
	 * returns the {@link World#ylen() y-len} of the backing world
	 * <p>
	 * this method checks the users mod count
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int ylen() {
		this.usr.checkModCnt(this.modCnt);
		return this.world.ylen();
	}
	
	/**
	 * returns the tile at the given position
	 * <ul>
	 * <li>if the user currently sees the tile, the tile of the backing world will be returned</li>
	 * <li>if the user never saw the tile, an unexplored, not-visible tile without resource will be returned</li>
	 * <li>if the user saw the tile at least once, an cached tile will be returned
	 * <ul>
	 * <li>the cached tile will be set to not-visible</li>
	 * <li>the cached tile will contain the {@link Tile#ground} and {@link Tile#resource} from the last time the user saw the tile</li>
	 * <li>if there is an entity of the user at the given tile it will also be on the cached tile</li>
	 * <li>everything else will not be on the cached</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * this method checks the users mod count
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Tile tile(int x, int y) {
		this.usr.checkModCnt(this.modCnt);
		if (this.entities == null) {
			update();
		}
		if (this.visible != null && this.visible[x][y]) {
			if (this.cach[x][y] == null) {
				this.cach[x][y] = this.world.tile(x, y).copy();
			}
			return this.cach[x][y];
		} else if (this.cach == null || this.cach[x][y] == null) {
			return NOT_EXPLORED;
		} else {
			return this.cach[x][y];
		}
	}
	
	/**
	 * this methods just delegates to its backing world
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void addNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.world.addNextTurnListener(listener); }
	
	/**
	 * this methods just delegates to its backing world
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.world.removeNextTurnListener(listener); }
	
	/**
	 * this methods returns all entities which are visible to the {@link #user() user} or owned by the {@link #user() user}
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public Map<User, List<Entity>> entities() {
		if (this.entities == null) {
			update();
		}
		return this.entities;
	}
	
	@SuppressWarnings("preview")
	private void update() {
		Map<User, List<Entity>> es   = new HashMap<>();
		Map<User, List<Entity>> all  = this.world.entities();
		List<Entity>            list = all.get(this.usr);
		if (list == null) {
			this.entities = Collections.emptyMap();
			if (this.visible != null) {
				for (boolean[] v : this.visible) {
					Arrays.fill(v, false);
				}
			}
			this.visible = null;
			return;
		}
		if (this.cach == null) {
			this.cach = new Tile[this.world.xlen()][this.world.ylen()];
		}
		if (this.visible == null) {
			this.visible = new boolean[this.world.xlen()][this.world.ylen()];
		} else {
			for (boolean[] v : this.visible) {
				Arrays.fill(v, false);
			}
		}
		for (Entity e : list) {
			int v = e.viewRange();
			int x = e.x();
			int y = e.y();
			if (v == 0) {
				if (this.cach[x][y] == null) {
					this.cach[x][y] = NOT_EXPLORED.copy();
				}
				switch (e) {
				case Unit u -> this.cach[x][y].unit(u);
				case Building b -> this.cach[x][y].build(b);
				}
			}
			while (--v >= 0) {
				for (int v0 = 0; v0 <= v; v0++) {
					cach(es, x + v - v0, y + v0);
					cach(es, x + v - v0, y - v0);
					cach(es, x - v + v0, y + v0);
					cach(es, x - v + v0, y - v0);
				}
			}
		}
		for (int x = 0; x < this.cach.length; x++) {
			for (int y = 0; y < this.cach[x].length; y++) {
				if (this.cach[x][y] == null) continue;
				for (Entity e : this.cach[x][y].entities()) {
					es.computeIfAbsent(e.owner(), u -> new ArrayList<>()).add(e);
				}
			}
		}
		es.replaceAll((u, l) -> Collections.unmodifiableList(l));
		this.entities = Collections.unmodifiableMap(es);
	}
	
	private void cach(Map<User, List<Entity>> es, int x0, int y0) {
		if (x0 < 0 || y0 < 0 || x0 >= this.visible.length || y0 >= this.visible[x0].length || this.visible[x0][y0]) {
			return;
		}
		this.visible[x0][y0] = true;
		Tile t = this.world.tile(x0, y0).copy();
		this.cach[x0][y0] = t;
	}
	
	private void nextTurn(@SuppressWarnings("unused") byte[] wh, @SuppressWarnings("unused") byte[] th) { this.entities = null; }
	
	/**
	 * this methods just delegates to its backing world<br>
	 * note that this means that if the backing world does not support finishing the turn of different users this method fails
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void finish(Turn t) { this.world.finish(t); }
	
}
