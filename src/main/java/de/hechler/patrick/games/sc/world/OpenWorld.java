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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.Addons;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.EntityType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.values.BooleanValue;
import de.hechler.patrick.games.sc.values.DoubleValue;
import de.hechler.patrick.games.sc.values.EnumValue;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.JustAValue;
import de.hechler.patrick.games.sc.values.ListValue;
import de.hechler.patrick.games.sc.values.LongValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.StringValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.UserValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.interfaces.Executable;

public class OpenWorld implements NextTurnListener {
	
	private final Connection conn;
	private final World      world;
	
	private OpenWorld(Connection conn, World world) {
		this.conn  = conn;
		this.world = world;
	}
	
	public static OpenWorld of(Connection conn, World w) {
		return new OpenWorld(conn, w);
	}
	
	/**
	 * <ol>
	 * <li>send {@link #START_BLOCK}</li>
	 * <li>receive {@link #ACCEPT_BLOCK}</li>
	 * <li>do stuff</li></li>receive {@link #STOP_BLOCK}</li></li>send {@link #STOP_BLOCK}</li>
	 * </ol>
	 */
	public static final int START_BLOCK  = 0x78C79319;
	/** @see #START_BLOCK */
	public static final int ACCEPT_BLOCK = 0x6CD001D7;
	/** @see #START_BLOCK */
	public static final int STOP_BLOCK   = 0xBF10CAD1;
	
	public void execute() throws IOException {
		this.world.addNextTurnListener(this);
		try {
			while (!this.conn.closed()) {
				this.conn.blocked(250, () -> this.exec(true), Connection.NOP);
			}
		} finally {
			this.world.removeNextTurnListener(this);
		}
	}
	
	// MAKE_* are send by the client to do stuff
	
	/**
	 * <ol>
	 * <li>send {@link #MAKE_TURN}</li>
	 * <li>receive {@link #MAKE_TURN}</li>
	 * <li>{@link Turn#retrieveTurn(Connection)} / {@link Turn#sendTurn(Connection)}</li>
	 * </ol>
	 */
	public static final int MAKE_TURN = 0xC255537F;
	
	// GET_* are send by the client to get stuff
	
	/**
	 * <ol>
	 * <li>{@link #START_BLOCK start} block</li>
	 * <li>send {@link #GET_TURN}</li>
	 * <li>receive {@link Connection#readInt() int} turn</li>
	 * <li>stop block</li>
	 * </ol>
	 */
	public static final int GET_TURN  = 0x21FB7A28;
	/**
	 * <ol>
	 * <li>{@link #START_BLOCK start} block</li>
	 * <li>send {@link #GET_WORLD}</li>
	 * <li>{@link #loadWorld(Tile[][], Connection)} / {@link #saveWorld(World, Connection)}</li>
	 * <li>stop block</li>
	 * </ol>
	 */
	public static final int GET_WORLD = 0xAD505582;
	/**
	 * <ol>
	 * <li>{@link #START_BLOCK start} block</li>
	 * <li>send {@link #GET_SIZE}</li>
	 * <li>receive {@link #GET_SIZE}</li>
	 * <li>receive {@link World#xlen()}</li>
	 * <li>receive {@link World#ylen()}</li>
	 * <li>stop block</li>
	 * </ol>
	 */
	public static final int GET_SIZE  = 0x62049F3D;
	/**
	 * <ol>
	 * <li>{@link #START_BLOCK start} block</li>
	 * <li>send {@link #GET_THING}</li>
	 * <li>receive {@link #GET_THING}</li>
	 * <li>send {@link Connection#writeUUID(UUID) UUID}</li>
	 * <li>{@link #readThing(Connection)} / {@link #writeThing(Connection, WorldThing)}</li>
	 * <li>stop block</li>
	 * </ol>
	 */
	public static final int GET_THING = 0xC8F3FAE8;
	
	// NOTIFY_* are send by the server to notify the client about stuff
	
	/**
	 * <ol>
	 * <li>receive {@link #NOTIFY_NEXT_TURN}</li>
	 * <li>receive {@link Connection#readInt() int} turn</li>
	 * <li>receive {@link Connection#readArr(byte[]) byte[]} world hash (SHA-256: 32 bytes)</li>
	 * <li>receive {@link Connection#readArr(byte[]) byte[]} turn hash (SHA-256: 32 bytes)</li>
	 * </ol>
	 */
	public static final int NOTIFY_NEXT_TURN = 0x12C6AEAE;
	
	public static void acceptBlockClient(Connection conn, int timeout, Executable<? extends IOException> exe) throws IOException {
		acceptBlockClient(conn, timeout, exe, Connection.NOP);
	}
	
	public static void acceptBlockClient(Connection conn, int timeout, Executable<? extends IOException> exe, Executable<IOException> onTimeout) throws IOException {
		conn.blocked(timeout, () -> {
			conn.readInt(START_BLOCK);
			conn.writeInt(ACCEPT_BLOCK);
			exe.execute();
			conn.writeInt(STOP_BLOCK);
			conn.readInt(STOP_BLOCK);
		}, onTimeout);
	}
	
	public static void doBlockedClient(Connection conn, Executable<? extends IOException> exe) throws IOException {
		conn.blocked(() -> {
			do {
				conn.writeInt(START_BLOCK);
				// the client has a higher priority
			} while (conn.readInt(ACCEPT_BLOCK, START_BLOCK) != ACCEPT_BLOCK);
			exe.execute();
			conn.readInt(STOP_BLOCK);
			conn.writeInt(STOP_BLOCK);
		});
	}
	
	private void doBlockedServer(Connection conn, Executable<? extends IOException> exe) throws IOException {
		conn.blocked(() -> {
			do {
				conn.writeInt(START_BLOCK);
				// the client has a higher priority
				if (conn.readInt(ACCEPT_BLOCK, START_BLOCK) == ACCEPT_BLOCK) {
					break;
				}
				this.exec(false);
			} while (true);
			exe.execute();
			conn.readInt(STOP_BLOCK);
			conn.writeInt(STOP_BLOCK);
		});
	}
	
	@Override
	public void nextTurn(int turn, byte[] whash, byte[] thash) {
		assert whash == null || whash.length == 32;
		assert thash == null || thash.length == 32;
		assert turn >= -1;
		try {
			doBlockedServer(this.conn, () -> {
				this.conn.writeInt(NOTIFY_NEXT_TURN);
				this.conn.writeArr(whash);
				this.conn.writeArr(thash);
			});
		} catch (IOException e) {
			try {
				this.conn.logOut();
			} catch (IOException e1) {
				e.addSuppressed(e1);
			}
			throw new IllegalStateException(e);
		}
	}
	
	private void exec(boolean sbnr) throws IOException {
		if (sbnr) {
			if (this.conn.readInt(START_BLOCK, Connection.CON_LOG_OUT) == Connection.CON_LOG_OUT) {
				this.conn.close();
				return;
			}
			this.conn.setTimeout(0);
		}
		this.conn.writeInt(ACCEPT_BLOCK);
		switch (this.conn.readInt(GET_TURN, GET_WORLD, GET_SIZE, GET_THING, MAKE_TURN, Connection.CON_LOG_OUT)) {
		case GET_TURN -> this.conn.writeInt(this.world.turn());
		case GET_WORLD -> saveWorld(this.world, this.conn);
		case GET_SIZE -> {
			this.conn.writeInt(GET_SIZE);
			this.conn.writeInt(this.world.xlen());
			this.conn.writeInt(this.world.ylen());
		}
		case GET_THING -> {
			this.conn.writeInt(GET_THING);
			UUID             uuid  = this.conn.readUUID();
			WorldThing<?, ?> thing = this.world.get(uuid);
			if (thing != null) {
				this.conn.writeByte(1);
				writeThing(this.conn, thing);
			} else {
				this.conn.writeByte(0);
			}
		}
		case MAKE_TURN -> {
			this.conn.writeInt(MAKE_TURN);
			Turn t = new Turn(this.world);
			t.retrieveTurn(this.conn);
			this.world.finish(t);
		}
		case Connection.CON_LOG_OUT -> {
			this.conn.close();
			return;
		}
		default -> throw new AssertionError("illegal return value from readInt(int...)");
		}
		this.conn.writeInt(STOP_BLOCK);
		this.conn.readInt(STOP_BLOCK);
	}
	
	private static final int SEND_WORLD      = 0xAFD2E294;
	private static final int SEND_WORLD_SUB0 = 0x97EC3497;
	private static final int SEND_WORLD_SUB1 = 0xEC885A44;
	private static final int SEND_WORLD_SUB2 = 0xB3E08946;
	private static final int SEND_WORLD_SUB3 = 0x026484C9;
	private static final int SEND_WORLD_SUB4 = 0xE231D32C;
	private static final int SEND_WORLD_FIN  = 0xB677A464;
	
	public static void saveWorld(World world, Connection conn) throws IOException {
		conn.writeInt(SEND_WORLD);
		int xlen = world.xlen();
		int ylen = world.ylen();
		conn.writeInt(xlen);
		conn.writeInt(ylen);
		for (int x = 0; x < xlen; x++) {
			conn.writeInt(SEND_WORLD_SUB0);
			for (int y = 0; y < ylen; y++) {
				Tile t = world.tile(x, y);
				conn.writeInt(SEND_WORLD_SUB1);
				conn.writeInt(t.lastTimeSeen0());
				writeThing(conn, t.ground());
				conn.writeInt(SEND_WORLD_SUB2);
				int cnt = t.resourceCount();
				conn.writeInt(cnt);
				while (cnt-- > 0) {
					writeThing(conn, t.resource(cnt));
				}
				conn.writeInt(SEND_WORLD_SUB3);
				cnt = t.unitCount();
				conn.writeInt(cnt);
				while (cnt-- > 0) {
					writeThing(conn, t.unit(cnt));
				}
				conn.writeInt(SEND_WORLD_SUB4);
				if (t.build() == null) {
					conn.writeByte(0);
				} else {
					conn.writeByte(1);
					writeThing(conn, t.build());
				}
			}
		}
		conn.writeInt(SEND_WORLD_FIN);
	}
	
	public static Tile[][] loadWorld(Tile[][] tiles, Connection conn) throws IOException {
		conn.readInt(SEND_WORLD);
		int xlen = conn.readStrictPos();
		int ylen = conn.readStrictPos();
		if (tiles == null || xlen != tiles.length || ylen != tiles[0].length) {
			tiles = new Tile[xlen][ylen];
		}
		for (int x = 0; x < xlen; x++) {
			if (tiles[x].length != ylen) {
				throw new AssertionError("the given array has no rectangular form!");
			}
			conn.readInt(SEND_WORLD_SUB0);
			for (int y = 0; y < ylen; y++) {
				conn.readInt(SEND_WORLD_SUB1);
				int lastSeen = conn.readInt();
				if (lastSeen < -2) { // well thats near and it fails anyway
					lastSeen = conn.wrongInputPositive(lastSeen, false);
				}
				Ground g = (Ground) readThing(conn);
				conn.readInt(SEND_WORLD_SUB2);
				int                         cnt = conn.readPos();
				Map<ResourceType, Resource> r   = HashMap.newHashMap(cnt);
				while (cnt-- > 0) {
					Resource res = (Resource) readThing(conn);
					Resource old = r.put(res.type(), res);
					assert old == null;
				}
				conn.readInt(SEND_WORLD_SUB3);
				cnt = conn.readPos();
				List<Unit> u = new ArrayList<>(cnt);
				while (cnt-- > 0) {
					u.add((Unit) readThing(conn));
				}
				conn.readInt(SEND_WORLD_SUB4);
				Build b = null;
				if (conn.readByte(0, 1) != 0) {
					b = (Build) readThing(conn);
				}
				tiles[x][y] = new Tile(g, r, b, u, lastSeen);
			}
		}
		conn.readInt(SEND_WORLD_FIN);
		return tiles;
	}
	
	private static final Set<String> UNIT_IGNORE     = Set.of(Entity.OWNER, Entity.X, Entity.Y);
	private static final Set<String> BUILD_IGNORE    = Set.of(Entity.OWNER, Entity.X, Entity.Y);
	private static final Set<String> RESOURCE_IGNORE = Set.of(Resource.AMOUNT);
	private static final Set<String> GROUND_IGNORE   = Set.of();
	
	private static final int WRITE_THING      = 0xFA1DE764;
	private static final int WRITE_THING_SUB0 = 0x9BC4A451;
	private static final int WRITE_THING_FIN  = 0x92D478CB;
	
	public static void writeThing(Connection conn, WorldThing<?, ?> wt) throws IOException {
		conn.writeInt(WRITE_THING);
		conn.writeUUID(wt.uuid);
		conn.writeString(wt.type().name);
		Set<String>        ignore;
		Map<String, Value> map = wt.values();
		conn.writeInt(map.size());
		switch (wt) {
		case @SuppressWarnings("preview") Ground g -> //
			ignore = GROUND_IGNORE;
		case @SuppressWarnings("preview") Resource r -> {
			ignore = RESOURCE_IGNORE;
			conn.writeInt(r.amount());
		}
		case @SuppressWarnings("preview") Entity<?, ?> e -> {
			conn.writeInt(e.x());
			conn.writeInt(e.y());
			conn.writeString(e.owner().name());
			switch (e) {
			case @SuppressWarnings("preview") Build b -> //
				ignore = BUILD_IGNORE;
			case @SuppressWarnings("preview") Unit u -> //
				ignore = UNIT_IGNORE;
			}
		}
		}
		for (Value val : map.values()) {
			conn.writeInt(WRITE_THING_SUB0);
			if (ignore.contains(val.name())) {
				continue;
			}
			writeValue(conn, val);
		}
		conn.writeInt(WRITE_THING_FIN);
	}
	
	public static WorldThing<?, ?> readThing(Connection conn) throws IOException {
		conn.writeInt(WRITE_THING);
		UUID               uuid   = conn.readUUID();
		AddableType<?, ?>  type   = Addons.type(conn.readString());
		Map<String, Value> values = HashMap.newHashMap(conn.readPos());
		switch (type) {
		case @SuppressWarnings("preview") GroundType g -> {/**/}
		case @SuppressWarnings("preview") ResourceType r -> //
			values.put(Resource.AMOUNT, new IntValue(Resource.AMOUNT, conn.readPos()));
		case @SuppressWarnings("preview") EntityType<?, ?> e -> {
			values.put(Entity.X, new IntValue(Entity.X, conn.readPos()));
			values.put(Entity.Y, new IntValue(Entity.Y, conn.readPos()));
			values.put(Entity.OWNER, new UserValue(Entity.OWNER, conn.usr.get(conn.readString())));
		}
		}
		while (conn.readInt(WRITE_THING_SUB0, WRITE_THING_FIN) == WRITE_THING_SUB0) {
			Value val = readValue(conn);
			Value old = values.put(val.name(), val);
			assert old == null;
		}
		try {
			return type.withValues(values, uuid);
		} catch (TurnExecutionException e) {
			throw new AssertionError(e);
		}
	}
	
	private static final int SEND_VALUE = 0xB1128AD8;
	
	private static final int BOOLEAN_VALUE     = 0x5172D772;
	private static final int DOUBLE_VALUE      = 0x38A010D6;
	private static final int ENUM_VALUE        = 0x4367939B;
	private static final int INT_VALUE         = 0xA9F47A84;
	private static final int JUST_A_VALUE      = 0x2C8DCD65;
	private static final int LONG_VALUE        = 0x5B34C353;
	private static final int MAP_VALUE         = 0xF0D90AD5;
	private static final int MAP_VALUE_SUB0    = 0x44ADDBB8;
	private static final int MAP_VALUE_SUB1    = 0x15DE6915;
	private static final int MAP_VALUE_FIN     = 0x8BF1B021;
	private static final int STRING_VALUE      = 0x2E72C490;
	private static final int TYPE_VALUE        = 0x8F4CE1D6;
	private static final int USER_LIST_VALUE   = 0x5F61F8B0;
	private static final int USER_LIST_VALUE_S = 0xB6150365;
	private static final int USER_LIST_VALUE_F = 0x47ADF410;
	private static final int USER_VALUE        = 0xE3C3681A;
	private static final int WORLD_THING_VALUE = 0x0BE4C78B;
	
	public static void writeValue(Connection conn, Value val) throws IOException {
		conn.writeInt(SEND_VALUE);
		conn.writeString(val.name());
		switch (val) {
		case @SuppressWarnings("preview") BooleanValue v -> {
			conn.writeInt(BOOLEAN_VALUE);
			conn.writeByte(v.value() ? 1 : 0);
		}
		case @SuppressWarnings("preview") DoubleValue v -> {
			conn.writeInt(DOUBLE_VALUE);
			conn.writeLong(Double.doubleToRawLongBits(v.value()));
		}
		case @SuppressWarnings("preview") EnumValue<?> v -> {
			conn.writeInt(ENUM_VALUE);
			conn.writeClass(v.value().getDeclaringClass());
			conn.writeInt(v.value().ordinal());
		}
		case @SuppressWarnings("preview") IntValue v -> {
			conn.writeInt(INT_VALUE);
			conn.writeInt(v.value());
		}
		case @SuppressWarnings("preview") JustAValue v -> //
			conn.writeInt(JUST_A_VALUE);
		case @SuppressWarnings("preview") LongValue v -> {
			conn.writeInt(LONG_VALUE);
			conn.writeLong(v.value());
		}
		case @SuppressWarnings("preview") MapValue<?> v -> {
			conn.writeInt(MAP_VALUE);
			Map<?, ?> map = v.value();
			conn.writeInt(map.size());
			for (Entry<?, ?> e : map.entrySet()) {
				conn.writeInt(MAP_VALUE_SUB0);
				conn.writeString((String) e.getKey());
				conn.writeInt(MAP_VALUE_SUB1);
				writeValue(conn, (Value) e.getValue());
			}
			conn.writeInt(MAP_VALUE_FIN);
		}
		case @SuppressWarnings("preview") StringValue v -> {
			conn.writeInt(STRING_VALUE);
			conn.writeString(v.value());
		}
		case @SuppressWarnings("preview") TypeValue<?> v -> {
			conn.writeInt(TYPE_VALUE);
			conn.writeString(v.value().name);
		}
		case @SuppressWarnings("preview") ListValue v -> {
			conn.writeInt(USER_LIST_VALUE);
			List<Value> list = v.value();
			conn.writeInt(list.size());
			for (Value user : list) {
				conn.writeInt(USER_LIST_VALUE_S);
				writeValue(conn, user);
			}
			conn.writeInt(USER_LIST_VALUE_F);
		}
		case @SuppressWarnings("preview") UserValue v -> {
			conn.writeInt(USER_VALUE);
			if (v.hasValue()) {
				conn.writeByte(1);
				conn.writeString(v.value().name());
			} else {
				conn.writeByte(0);
			}
		}
		case @SuppressWarnings("preview") WorldThingValue v -> {
			conn.writeInt(WORLD_THING_VALUE);
			if (v.knownType() && v.knownUUID()) {
				conn.writeByte(3);
				conn.writeString(v.type().name);
				conn.writeUUID(v.uuid());
			} else if (v.knownType()) {
				conn.writeByte(2);
				conn.writeString(v.value().type().name);
			} else if (v.knownUUID()) {
				conn.writeByte(1);
				conn.writeUUID(v.uuid());
			} else {
				conn.writeByte(0);
			}
		}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Value readValue(Connection conn) throws IOException {
		conn.writeInt(SEND_VALUE);
		String name = conn.readString();
		switch (conn.readInt(BOOLEAN_VALUE, DOUBLE_VALUE, ENUM_VALUE, INT_VALUE, JUST_A_VALUE, LONG_VALUE, MAP_VALUE, STRING_VALUE, TYPE_VALUE, USER_LIST_VALUE,
			USER_VALUE, WORLD_THING_VALUE)) {
		case BOOLEAN_VALUE -> {
			return new BooleanValue(name, conn.readByte(1, 0) != 0);
		}
		case DOUBLE_VALUE -> {
			return new DoubleValue(name, Double.longBitsToDouble(conn.readLong()));
		}
		case ENUM_VALUE -> {
			Class<?> cls = conn.readClass();
			int      p   = conn.readPos();
			return new EnumValue(name, (Enum) cls.getEnumConstants()[p]);
		}
		case INT_VALUE -> {
			return new IntValue(name, conn.readInt());
		}
		case JUST_A_VALUE -> {
			return new JustAValue(name);
		}
		case LONG_VALUE -> {
			return new LongValue(name, conn.readLong());
		}
		case MAP_VALUE -> {
			Map<String, Value> map = new HashMap<>();
			conn.writeInt(map.size());
			for (;;) {
				if (conn.readInt(MAP_VALUE_SUB0, MAP_VALUE_FIN) == MAP_VALUE_FIN) {
					break;
				}
				String k = conn.readString();
				conn.readInt(MAP_VALUE_SUB1);
				Value v = readValue(conn);
				map.put(k, v);
			}
			return new MapValue<Value>(name, map);
		}
		case STRING_VALUE -> {
			return new StringValue(name, conn.readString());
		}
		case TYPE_VALUE -> {
			return new TypeValue(name, Addons.type(conn.readString()));
		}
		case USER_LIST_VALUE -> {
			List<Value> list = new ArrayList<>(conn.readPos());
			for (;;) {
				if (conn.readInt(USER_LIST_VALUE_S, USER_LIST_VALUE_F) == USER_LIST_VALUE_F) {
					break;
				}
				list.add(readValue(conn));
			}
			return new ListValue(name, list);
		}
		case USER_VALUE -> {
			if (conn.readByte(1, 0) != 0) {
				return new UserValue(name, conn.usr.get(conn.readString()));
			}
			return new UserValue(name, null);
		}
		case WORLD_THING_VALUE -> {
			switch (conn.readByte(3, 2, 1, 0)) {
			case 3: {
				AddableType<?, ?> type = Addons.type(conn.readString());
				UUID              uuid = conn.readUUID();
				WorldThing<?, ?>  wt   = conn.world().get(uuid);
				if (wt == null) {
					return new WorldThingValue(name, uuid, type);
				}
				if (wt.type() != type) {
					throw new AssertionError();
				}
				return new WorldThingValue(name, wt);
			}
			case 2: {
				AddableType<?, ?> type = Addons.type(conn.readString());
				return new WorldThingValue(name, null, type);
			}
			case 1: {
				UUID uuid = conn.readUUID();
				// no need to search for the thing when the server did not find it
				return new WorldThingValue(name, uuid, null);
			}
			case 0:
				return new WorldThingValue(name, (WorldThing<?, ?>) null);
			default:
				throw new AssertionError("illegal return value from readByte(int...)");
			}
		}
		default -> throw new AssertionError("illegal return value from readInt(int...)");
		}
	}
	
}
