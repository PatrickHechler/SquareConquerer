//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.turn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.Direction;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this class represents a Turn in the game.<br>
 * a turn maps entities from a user to {@link EntityTurn} instances.
 * 
 * @author Patrick Hechler
 */
public final class Turn {
	
	private static final String UNKNOWN_ENTITY_TURN_TYPE         = Messages.get("Turn.unknown-entity-turn");         //$NON-NLS-1$
	private static final String TURNS_IS_NOT_EMPTY               = Messages.get("Turn.turn-not-empty");              //$NON-NLS-1$
	private static final String ENTITY_TURN_NOT_BELONG_TO_ENTITY = Messages.get("Turn.entity-turn-not-from-entity"); //$NON-NLS-1$
	private static final String I_DO_NOT_OWN_THIS_ENTITY         = Messages.get("Turn.not-my-entity");               //$NON-NLS-1$
	private static final String ENTITY_TURN_IS_NULL              = Messages.get("Turn.no-entity-turn");              //$NON-NLS-1$
	private static final String ENTITY_IS_NULL                   = Messages.get("Turn.no-entity");                   //$NON-NLS-1$
	private static final String ENTITIES_NOT_IN_WORLD            = Messages.get("Turn.entity-not-found");            //$NON-NLS-1$
	
	/**
	 * the world to which this turn belongs to
	 */
	public final World world;
	
	private final Map<Entity, EntityTurn> turns;
	
	/**
	 * create a new turn instance with the given world
	 * 
	 * @param world the world of the turn
	 */
	public Turn(World world) {
		this.world = world;
		
		this.turns = new TreeMap<>((a, b) -> {
			int ac = a.x();
			int bc = b.x();
			if (ac > bc) return 1;
			else if (ac < bc) return -1;
			ac = a.y();
			bc = b.y();
			if (ac > bc) return 1;
			else if (ac < bc) return -1;
			if (a == b) return 0;
			for (Entity c : world.tile(a.x(), ac).entities()) {
				if (c == a) return 1;
				if (c == b) return -1;
			}
			throw new AssertionError(ENTITIES_NOT_IN_WORLD);
		});
	}
	
	/**
	 * associates the given {@link Entity} with an {@link EntityTurn}
	 * 
	 * @param e  the entity which should make a turn
	 * @param et the new entity turn to associate with the entity
	 * @return the previous turn associated with the entity
	 */
	public EntityTurn put(Entity e, EntityTurn et) {
		if (e == null) throw new NullPointerException(ENTITY_IS_NULL);
		if (et == null) throw new NullPointerException(ENTITY_TURN_IS_NULL);
		if (e.owner() != this.world.user()) throw new IllegalArgumentException(I_DO_NOT_OWN_THIS_ENTITY);
		if (et.entity() != e) throw new IllegalArgumentException(ENTITY_TURN_NOT_BELONG_TO_ENTITY);
		return this.turns.put(e, et);
	}
	
	/**
	 * returns an unmodifiable collection containing all entity turns
	 * 
	 * @return an unmodifiable collection containing all entity turns
	 */
	public Collection<EntityTurn> turns() {
		return Collections.unmodifiableCollection(this.turns.values());
	}
	
	/**
	 * this constant is send to start the Turn send sequence
	 * 
	 * @see #sendTurn(Connection, boolean)
	 * @see #retrieveTurn(Connection, boolean)
	 */
	public static final int  CMD_TURN  = 0x67A31709;
	private static final int SUB0_TURN = 0x7A881D25;
	private static final int ET_CARRY  = 0x5E209AC4;
	private static final int ET_MOVE   = 0x037255BF;
	private static final int ET_STORE  = 0xC9690B0E;
	private static final int FIN_TURN  = 0x00E7B2EF;
	
	/**
	 * retrieves a turn from the given connection
	 * <p>
	 * if the turn was send with {@link #sendTurn(Connection, boolean) sendTurn(conn, true)}, <code>alsoWrite</code> must be set to <code>true</code> otherwise it
	 * must be set to <code>false</code>
	 * 
	 * @param conn      the connection
	 * @param alsoWrite if also write operations should be made
	 * @throws IOException if an IO error occurs
	 */
	public void retrieveTurn(Connection conn, boolean alsoWrite) throws IOException {
		if (alsoWrite) conn.writeInt(SUB0_TURN);
		if (!this.turns.isEmpty()) throw new IllegalStateException(TURNS_IS_NOT_EMPTY);
		int len = conn.readPos();
		while (len-- > 0) {
			Entity     e;
			EntityTurn et;
			int        x = conn.readInt();
			int        y = conn.readInt();
			switch (conn.readInt(ET_CARRY, ET_MOVE, ET_STORE)) {
			case ET_CARRY -> {
				e = this.world.tile(x, y).unit();
				Resource res    = RemoteWorld.readRes(conn);
				int      amount = conn.readPos();
				et = new CarryTurn((Unit) e, res, amount);
			}
			case ET_MOVE -> {
				e = this.world.tile(x, y).unit();
				int             moves = conn.readInt();
				List<Direction> dirs  = new ArrayList<>(moves);
				while (moves-- > 0) {
					dirs.add(Direction.of(conn.readByte()));
				}
				et = new MoveTurn((Unit) e, dirs);
			}
			case ET_STORE -> {
				e = this.world.tile(x, y).unit();
				int amount = conn.readInt();
				Resource res = RemoteWorld.readRes(conn);
				et = new StoreTurn((Unit) e, res, amount);
			}
			default -> throw new AssertionError("illegal return value from conn.readInt(int...) (this should not be possible)"); //$NON-NLS-1$
			}
			this.turns.put(e, et);
		}
	}
	
	/**
	 * send this turn over the given connection.
	 * <p>
	 * if <code>alsoRead</code> is true, also read operations will be made to avoid race conditions<br>
	 * if this is no one-ways connection <code>alsoRead</code> should be set to <code>true</code>
	 * 
	 * @param conn     the connection
	 * @param alsoRead if the connection should also be used to make read operations
	 * @throws IOException if an IO error occurs
	 */
	@SuppressWarnings("preview")
	public void sendTurn(Connection conn, boolean alsoRead) throws IOException {
		if (alsoRead) conn.writeReadInt(CMD_TURN, SUB0_TURN);
		else conn.writeInt(CMD_TURN);
		conn.writeInt(this.turns.size());
		for (Entry<Entity, EntityTurn> turn : this.turns.entrySet()) {
			Entity     e  = turn.getKey();
			EntityTurn et = turn.getValue();
			conn.writeInt(e.x());
			conn.writeInt(e.y());
			switch (et) {
			case CarryTurn ct -> {
				conn.writeInt(ET_CARRY);
				OpenWorld.writeRes(conn, ct.res());
				conn.writeInt(ct.amount());
			}
			case MoveTurn mt -> {
				conn.writeInt(ET_MOVE);
				List<Direction> dirs = mt.dirs();
				conn.writeInt(dirs.size());
				for (Direction dir : dirs) {
					conn.writeByte(dir.ordinal());
				}
			}
			case StoreTurn st -> {
				conn.writeInt(ET_STORE);
				conn.writeInt(st.amount());
				OpenWorld.writeRes(conn, st.resource());
			}
			default -> throw new AssertionError(UNKNOWN_ENTITY_TURN_TYPE + et.getClass());
			}
		}
		conn.writeInt(FIN_TURN);
	}
	
}
