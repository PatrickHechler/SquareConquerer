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
package de.hechler.patrick.games.sc.turn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.world.OpenWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.resource.Resource;

/**
 * this class represents a Turn in the game.<br>
 * a turn maps entities from a user to {@link EntityTurn} instances.
 * 
 * @author Patrick Hechler
 */
public final class Turn {
	
	/**
	 * the world to which this turn belongs to
	 */
	public final World world;
	
	private final Map<Entity<?, ?>, EntityTurn> turns;
	
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
			for (Entity<?, ?> c : world.tile(a.x(), ac).entities()) {
				if (c.equals(a)) return 1;
				if (c.equals(b)) return -1;
			}
			throw new AssertionError("the entities are not in my world");
		});
	}
	
	/**
	 * associates the given {@link Entity} with an {@link EntityTurn}
	 * 
	 * @param e  the entity which should make a turn
	 * @param et the new entity turn to associate with the entity
	 * 
	 * @return the previous turn associated with the entity
	 */
	public EntityTurn put(Entity<?, ?> e, EntityTurn et) {
		if (e == null) throw new NullPointerException("entity");
		if (et == null) throw new NullPointerException("entity turn");
		if (e.owner() != this.world.user()) throw new IllegalArgumentException("I do not own this entity");
		if (et.entity() != e) throw new IllegalArgumentException("entity turn not belong to entity");
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
	 * @see #sendTurn(Connection)
	 * @see #retrieveTurn(Connection)
	 */
	public static final int  CMD_TURN = 0x67A31709;
	private static final int ET_CARRY = 0x5E209AC4;
	private static final int ET_MOVE  = 0x037255BF;
	private static final int ET_STORE = 0xC9690B0E;
	private static final int ET_MINE  = 0x0E2192A4;
	private static final int ET_WORK  = 0xB3532A22;
	private static final int FIN_TURN = 0x00E7B2EF;
	
	/**
	 * retrieves a turn from the given connection
	 * 
	 * @param conn the connection
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void retrieveTurn(Connection conn) throws IOException {
		conn.readInt(CMD_TURN);
		if (!this.turns.isEmpty()) throw new IllegalStateException("turns is not empty");
		int len = conn.readPos();
		while (len-- > 0) {
			EntityTurn   et;
			Entity<?, ?> e = (Entity<?, ?>) conn.world().get(conn.readUUID());
			switch (conn.readInt(ET_CARRY, ET_MOVE, ET_STORE, ET_MINE)) {
			case ET_CARRY -> {
				Resource res = (Resource) OpenWorld.readThing(conn);
				et = new CarryTurn((Unit) e, res);
			}
			case ET_MOVE -> {
				int           moves = conn.readInt();
				List<MoveAct> dirs  = new ArrayList<>(moves);
				while (moves-- > 0) {
					int orid = conn.readByte();
					if (orid != 0xFF) {
						dirs.add(Direction.of(orid));
					} else {
						dirs.add(new Attack((Entity<?, ?>) this.world.get(conn.readUUID())));
					}
				}
				et = new MoveTurn((Unit) e, dirs);
			}
			case ET_STORE -> {
				Resource res = (Resource) OpenWorld.readThing(conn);
				et = new StoreTurn((Unit) e, res);
			}
			case ET_MINE -> {
				Resource res = (Resource) OpenWorld.readThing(conn);
				et = new MineTurn((Unit) e, res);
			}
			case ET_WORK -> //
				et = new WorkTurn((Unit) e);
			default -> throw new AssertionError("illegal return value from conn.readInt(int...)"); //$NON-NLS-1$ this should not be possible
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
	 * @param conn the connection
	 * 
	 * @throws IOException if an IO error occurs
	 */
	@SuppressWarnings("preview")
	public void sendTurn(Connection conn) throws IOException {
		conn.writeInt(CMD_TURN);
		conn.writeInt(this.turns.size());
		for (Entry<Entity<?, ?>, EntityTurn> turn : this.turns.entrySet()) {
			Entity<?, ?> e  = turn.getKey();
			EntityTurn   et = turn.getValue();
			conn.writeUUID(e.uuid);
			switch (et) {
			case CarryTurn ct -> {
				conn.writeInt(ET_CARRY);
				OpenWorld.writeThing(conn, ct.res());
			}
			case MoveTurn mt -> {
				conn.writeInt(ET_MOVE);
				List<MoveAct> dirs = mt.acts();
				conn.writeInt(dirs.size());
				for (MoveAct ma : dirs) {
					switch (ma) {
					case Direction dir -> conn.writeByte(dir.ordinal());
					case Attack(Entity<?, ?> enemy) -> {
						conn.writeByte(0xFF);
						conn.writeUUID(enemy.uuid);
					}
					}
				}
			}
			case StoreTurn st -> {
				conn.writeInt(ET_STORE);
				OpenWorld.writeThing(conn, st.resource());
			}
			case MineTurn mt -> {
				conn.writeInt(ET_MINE);
				OpenWorld.writeThing(conn, mt.resource());
			}
			case WorkTurn wt -> //
				conn.writeInt(ET_WORK);
			}
		}
		conn.writeInt(FIN_TURN);
	}
	
}
