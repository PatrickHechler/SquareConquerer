package de.hechler.patrick.games.squareconqerer.world.turn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.Direction;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class Turn {
	
	public final User  usr;
	public final World world;
	
	private final Map<Entity, EntityTurn> turns;
	
	public Turn(World world) {
		this.usr   = world.user();
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
			throw new AssertionError("entities not found in the world");
		});
	}
	
	public void put(Entity e, EntityTurn et) {
		if (e == null) throw new NullPointerException("entity is null");
		if (et == null) throw new NullPointerException("entity turn is null");
		if (e.owner() != world.user()) throw new IllegalArgumentException("I do not own this user");
		turns.put(e, et);
	}
	
	public Collection<EntityTurn> turns() throws UnsupportedOperationException {
		return Collections.unmodifiableCollection(turns.values());
	}
	
	public static final int  CMD_TURN = 0x67A31709;
	private static final int ET_CARRY = 0x5E209AC4;
	private static final int ET_MOVE  = 0x037255BF;
	private static final int ET_STORE = 0xC9690B0E;
	private static final int FIN_TURN = 0x00E7B2EF;
	
	public void retrieveTurn(Connection conn) throws IOException {
		if (!turns.isEmpty()) {
			throw new IllegalStateException("turns is not empty");
		}
		int len = conn.readInt();
		if (len < 0) {
			throw new IOException("read a negative length");
		}
		while (len-- > 0) {
			Entity     e;
			EntityTurn et;
			int        x = conn.readInt();
			int        y = conn.readInt();
			switch (conn.readInt(ET_CARRY, ET_MOVE, ET_STORE)) {
			case ET_CARRY -> {
				e = world.tile(x, y).unit();
				Resource res    = RemoteWorld.readRes(conn);
				int      amount = conn.readInt();
				if (amount < 0) {
					throw new IOException("read a negative amount!");
				}
				et = new CarryTurn((Unit) e, res, amount);
			}
			case ET_MOVE -> {
				e = world.tile(x, y).unit();
				int             moves = conn.readInt();
				List<Direction> dirs  = new ArrayList<>(moves);
				while (moves-- > 0) {
					dirs.add(Direction.of(conn.readByte()));
				}
				et = new MoveTurn((Unit) e, dirs);
			}
			case ET_STORE -> {
				e = world.tile(x, y).unit();
				int amount = conn.readInt();
				et = new StoreTurn((Unit) e, amount);
			}
			default -> throw new AssertionError("illegal return value from conn.readInt(int...)");
			}
			turns.put(e, et);
		}
	}
	
	@SuppressWarnings("preview")
	public void sendTurn(Connection conn) throws IOException {
		conn.writeInt(CMD_TURN);
		conn.writeInt(turns.size());
		for (Entry<Entity, EntityTurn> turn : turns.entrySet()) {
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
			}
			default -> throw new AssertionError("invalid type: " + et.getClass());
			}
		}
		conn.writeInt(FIN_TURN);
	}
	
}
