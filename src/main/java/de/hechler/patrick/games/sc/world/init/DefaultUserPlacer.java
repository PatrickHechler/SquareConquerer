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
package de.hechler.patrick.games.sc.world.init;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.sc.addons.Addon;
import de.hechler.patrick.games.sc.addons.Addons;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.EntityType;
import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.OpenWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.objects.Random2;
import de.hechler.patrick.utils.objects.TwoVals;

public class DefaultUserPlacer implements UserPlacer {
	
	public final List<TwoVals<EntityType<?, ?>, Map<String, Value>>> starts;
	
	public DefaultUserPlacer() {
		this.starts = new ArrayList<>();
	}
	
	public DefaultUserPlacer(int initialCapacity) {
		this.starts = new ArrayList<>(initialCapacity);
	}
	
	public static UserPlacer createWithDefaults() {
		DefaultUserPlacer up = new DefaultUserPlacer();
		for (Addon a : Addons.addons().values()) {
			for (AddableType<?, ?> add : a.add.values()) {
				if (!(add instanceof EntityType<?, ?> et)) {
					continue;
				}
				for (Map<String, Value> map : et.startEntities()) {
					up.starts.add(new TwoVals<>(et, Map.copyOf(map)));
				}
			}
		}
		return up;
	}
	
	@Override
	public void initilize(World w, User[] users, Random2 rnd) throws TurnExecutionException {
		int     sum  = this.starts.size();
		int     size = (int) Math.sqrt(sum) + 1;
		Point[] p    = new Point[users.length];
		int     xlen = w.xlen();
		int     ylen = w.ylen();
		for (int i = 0; i < p.length; i++) {
			int x;
			int y;
			int cnt = -1;
			do {
				if (++cnt == 8) {
					checkPossible(p, i, size, xlen, ylen);
					System.err.println("I tried 8 invalid positions, there is a free position, so I will continue now");
				}
				x = rnd.nextInt(xlen - size);
				y = rnd.nextInt(ylen - size);
			} while (isUsed(p, i, size, x, y));
			p[i] = new Point(x, y);
			initUsr(rnd, users[i], w, x, y, size, sum);
		}
	}
	
	private void initUsr(Random2 rnd, User usr, World world, int x, int y, int size, int remainUnitCount) throws TurnExecutionException {
		Point[] p = new Point[remainUnitCount];
		for (TwoVals<EntityType<?, ?>, Map<String, Value>> tv : this.starts) {
			EntityType<?, ?>   type = tv.a;
			Map<String, Value> list = tv.b;
			int                x0;
			int                y0;
			int                cnt  = -1;
			do {
				if (++cnt == 8) {
					checkPossible(p, remainUnitCount, 1, size, size);
					System.err.println("I tried 8 invalid positions, there is a free position, so I will continue now");
				}
				x0 = rnd.nextInt(size);
				y0 = rnd.nextInt(size);
			} while (isUsed(p, p.length - remainUnitCount, size, x0, y0));
			x0 += x;
			y0 += y;
			Entity<?, ?> e = type.withValues(list, rnd.nextUUID());
			Tile         t = world.tile(x0, y0);
			p[p.length - remainUnitCount--] = new Point(x0, y0);
			switch (e) {
			case @SuppressWarnings("preview") Unit u -> t.addUnit(u);
			case @SuppressWarnings("preview") Build b -> t.setBuild(b);
			}
		}
	}
	
	private static void checkPossible(Point[] p, int i, int size, int xlen, int ylen) throws TurnExecutionException {
		xlen -= size;
		ylen -= size;
		int need = p.length - i;
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				if (!isUsed(p, i, size, x, y) && --need <= 0) return;
			}
		}
		throw new TurnExecutionException(ErrorType.BLOCKED_WAY);
	}
	
	private static boolean isUsed(Point[] p, int i, int size, int x, int y) {
		while (i-- > 0) {
			if (Math.abs(x - p[i].x) < size && Math.abs(y - p[i].y) < size) {
				return true;
			}
		}
		return false;
	}
	
	private static final int WRITE_DEF_PLACER      = 0x48BDEE3F;
	private static final int WRITE_DEF_PLACER_SUB0 = 0xF3AE8632;
	private static final int WRITE_DEF_PLACER_SUB1 = 0x7223583C;
	private static final int WRITE_DEF_PLACER_SUB2 = 0x822B051E;
	private static final int WRITE_DEF_PLACER_FIN  = 0x0B51E0BE;
	
	@Override
	public void writePlacer(Connection conn) throws IOException {
		conn.writeInt(WRITE_DEF_PLACER);
		conn.writeInt(this.starts.size());
		for (TwoVals<EntityType<?, ?>, Map<String, Value>> tv : this.starts) {
			conn.writeInt(WRITE_DEF_PLACER_SUB0);
			conn.writeString(tv.a.name);
			conn.writeInt(tv.b.size());
			for (Value v : tv.b.values()) {
				conn.writeInt(WRITE_DEF_PLACER_SUB1);
				OpenWorld.writeValue(conn, v);
			}
			conn.writeInt(WRITE_DEF_PLACER_SUB2);
		}
		conn.writeInt(WRITE_DEF_PLACER_FIN);
	}
	
	public static UserPlacer readPlacer(Connection conn) throws IOException {
		conn.readInt(WRITE_DEF_PLACER);
		DefaultUserPlacer dup = new DefaultUserPlacer(conn.readPos());
		while (conn.readInt(WRITE_DEF_PLACER_SUB0, WRITE_DEF_PLACER_FIN) == WRITE_DEF_PLACER_SUB0) {
			EntityType<?, ?>   type = (EntityType<?, ?>) Addons.type(conn.readString());
			Map<String, Value> map  = HashMap.newHashMap(conn.readPos());
			while (conn.readInt(WRITE_DEF_PLACER_SUB1, WRITE_DEF_PLACER_SUB2) == WRITE_DEF_PLACER_SUB1) {
				Value val = OpenWorld.readValue(conn);
				Value old = map.put(val.name(), val);
				assert old == null;
			}
			dup.starts.add(new TwoVals<EntityType<?, ?>, Map<String, Value>>(type, map));
		}
		return dup;
	}
	
}
