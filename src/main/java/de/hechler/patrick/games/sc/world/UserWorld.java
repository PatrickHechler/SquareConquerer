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

import java.lang.StackWalker.Option;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.objects.Pos;

@SuppressWarnings("javadoc")
public class UserWorld implements World {
	
	private final CompleteWorld cw;
	private final User          usr;
	public final int            modCnt;
	private volatile Pos        off;
	private volatile Tile[][]   tiles;
	private volatile boolean    needUpdate = true;
	
	public UserWorld(CompleteWorld rw, User usr, int modCnt) {
		this.cw     = rw;
		this.usr    = usr;
		this.modCnt = modCnt;
	}
	
	public static World of(CompleteWorld rw, User usr, int modCnt) {
		if (rw.user() == usr) {
			return rw;
		}
		usr.checkModCnt(modCnt);
		return new UserWorld(rw, usr, modCnt);
	}
	
	public static UserWorld usrOf(CompleteWorld rw, User usr, int modCnt) {
		if (rw.user() == usr) {
			throw new IllegalArgumentException("this is user of, the root world is not allowed");
		}
		usr.checkModCnt(modCnt);
		return new UserWorld(rw, usr, modCnt);
	}
	
	@Override
	public User user() {
		return this.usr;
	}
	
	@Override
	public int xlen() {
		if (this.needUpdate) {
			updateWorld();
		}
		return this.tiles.length;
	}
	
	@Override
	public int ylen() {
		if (this.needUpdate) {
			updateWorld();
		}
		return this.tiles[0].length;
	}
	
	@Override
	public int turn() {
		return this.cw.turn();
	}
	
	private synchronized void updateWorld() {
		if (!this.needUpdate) {
			return;
		}
		Pos                           oldOff   = this.off;
		Map<User, List<Entity<?, ?>>> all      = this.cw.entities();
		List<Entity<?, ?>>            mine     = all.get(this.usr);
		Tile[][]                      oldTiles = this.tiles;
		if (mine == null || mine.isEmpty()) {
			if (oldTiles == null) {
				this.tiles = new Tile[1][1];
			}
			return;
		}
		int minx = this.cw.xlen();
		int miny = this.cw.ylen();
		int maxx = 0;
		int maxy = 0;
		for (Entity<?, ?> e : mine) {
			int x  = e.x();
			int y  = e.y();
			int vr = e.viewRange();
			minx = Math.min(minx, x - vr);
			miny = Math.min(miny, y - vr);
			maxx = Math.min(maxx, x + vr);
			maxy = Math.min(maxy, y + vr);
		}
		if (minx > maxx) {
			if (oldTiles == null) {
				this.tiles = new Tile[1][1];
			}
			return;
		}
		maxx++;
		maxy++;
		minx = Math.max(minx, 0);
		miny = Math.max(miny, 0);
		maxx = Math.min(maxx, this.cw.xlen());
		maxy = Math.min(maxy, this.cw.ylen());
		if (oldTiles != null) {
			minx = Math.min(minx, oldOff.x());
			miny = Math.min(miny, oldOff.y());
			maxx = Math.min(maxx, oldOff.x() + oldTiles.length);
			maxy = Math.min(maxy, oldOff.y() + oldTiles[0].length);
			if (minx == oldOff.x() && maxx == oldOff.x() + oldTiles.length && miny == oldOff.y() && maxy == oldOff.y() + oldTiles[0].length) {
				updateNoResize(mine, oldOff);
				this.needUpdate = false;
				return;
			}
		}
		Tile[][] newTiles = new Tile[maxx - minx][maxy - miny];
		assert newTiles.length < this.cw.xlen();
		assert newTiles[0].length < this.cw.ylen();
		for (Entity<?, ?> e : mine) {
			gAdd(newTiles, minx, miny, e);
		}
		if (oldTiles != null) {
			addOldData(oldOff, minx, miny, newTiles);
		}
		this.needUpdate = false;
	}
	
	private void addOldData(Pos oldOff, int minx, int miny, Tile[][] newTiles) {
		int      addx     = oldOff.x() - minx;
		int      addy     = oldOff.y() - miny;
		Tile[][] oldTiles = this.tiles;
		for (int ox = 0; ox < oldTiles.length; ox++) {
			int    nx  = ox + addx;
			Tile[] oar = oldTiles[ox];
			Tile[] nar = newTiles[nx];
			for (int oy = 0; oy < oar.length; oy++) {
				int  ny = oy + addy;
				Tile ot = oar[ny];
				if (ot == null) continue;
				Tile nt = nar[ny];
				if (nt == null) {
					nar[ny] = ot;
				} else if (!nt.visible()) {
					addOldData(nar, ny, ot, nt);
				}
			}
		}
	}
	
	private static void addOldData(Tile[] nar, int ny, Tile ot, Tile nt) {
		Tile t = ot;
		if (nt.ground().type() != GroundType.NOT_EXPLORED_TYPE) {
			t.setGround(nt.ground());
		}
		if (nt.build() != null) {
			t.setBuild(nt.build());
		}
		nt.resourcesStream().forEach(r -> {
			if (t.resourcesStream().noneMatch(r::equals)) {
				t.addResource(r);
			}
		});
		nt.unitsStream().forEach(u -> {
			if (t.unitsStream().noneMatch(u::equals)) {
				try {
					t.addUnit(u);
				} catch (TurnExecutionException e) {
					throw new IllegalStateException(e);
				}
			}
		});
		nar[ny] = t;
	}
	
	private void updateNoResize(List<Entity<?, ?>> mine, Pos off) {
		int      xoff    = off.x();
		int      yoff    = off.y();
		Tile[][] myTiles = this.tiles;
		for (Entity<?, ?> e : mine) {
			gAdd(myTiles, xoff, yoff, e);
		}
	}
	
	private void gAdd(Tile[][] tiles, int xoff, int yoff, Entity<?, ?> e) {
		try {
			Map<Pos, Integer> nmap = new HashMap<>();
			Map<Pos, Integer> emap = new HashMap<>();
			Map<Pos, Integer> tmap = new HashMap<>();
			gInitLoop(tiles, xoff, yoff, e, nmap);
			while (!nmap.isEmpty()) {
				Iterator<Entry<Pos, Integer>> iter = nmap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Pos, Integer> n  = iter.next();
					Pos                 p  = n.getKey();
					Integer             r  = n.getValue();
					int                 ri = r.intValue();
					iter.remove();
					Integer i = emap.get(p);
					if (i != null && i.intValue() >= ri) {
						continue;
					}
					emap.put(p, r);
					int  x = p.x();
					int  y = p.y();
					Tile t = this.cw.tile(x, y);
					tiles[x - xoff][y - yoff] = t;
					if (ri > 0) {
						gAdd(tmap, tiles, x + 1, y, xoff, yoff, t, e, ri);
						gAdd(tmap, tiles, x - 1, y, xoff, yoff, t, e, ri);
						gAdd(tmap, tiles, x, y + 1, xoff, yoff, t, e, ri);
						gAdd(tmap, tiles, x, y - 1, xoff, yoff, t, e, ri);
					}
				}
				Map<Pos, Integer> smap = tmap;
				tmap = nmap;
				nmap = smap;
			}
		} catch (TurnExecutionException e1) {
			throw new IllegalStateException(e1);
		}
	}
	
	private void gAdd(Map<Pos, Integer> tmap, Tile[][] tiles, int x, int y, int xoff, int yoff, Tile old, Entity<?, ?> e, int range) {
		if (x < 0 || y < 0 || x >= this.cw.xlen() || y >= this.cw.ylen()) {
			return;
		}
		Tile target = this.cw.tile(x, y);
		tiles[x - xoff][y - yoff] = target;
		// need to give the complete world because I am currently in (re)construction
		int need = e.neededView(this.cw, x, y, target, old);
		if (need <= 0) {
			throw new AssertionError("neededView <= 0");
		}
		range -= need;
		if (range > 0) {
			Integer val = Integer.valueOf(range);
			tmap.put(new Pos(x, y), val);
		}
	}
	
	private void gInitLoop(Tile[][] tiles, int xoff, int yoff, Entity<?, ?> e, Map<Pos, Integer> nmap) throws TurnExecutionException {
		int  x  = e.x();
		int  y  = e.y();
		int  vr = e.viewRange();
		Tile t  = tiles[x - xoff][y - yoff];
		if (vr > 0 && (t == null || !t.visible())) { // view > 0 and not visible
			t                         = this.cw.tile(x, y);
			tiles[x - xoff][y - yoff] = t;
		} else if (t == null || !t.visible()) { // view = 0 and not visible
			if (t == null) {
				t                         = new Tile(GroundType.NOT_EXPLORED_GRND);
				tiles[x - xoff][y - yoff] = t;
			}
			if (e instanceof Unit u) {
				t.addUnit(u);
			} else {
				t.setBuild((Build) e);
			}
		}
		nmap.put(new Pos(x, y), Integer.valueOf(vr));
	}
	
	void init(Tile[][] tiles, int xoff, int yoff) {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != CompleteWorld.class) {
			throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
		}
		if (this.tiles != null) {
			throw new IllegalStateException("already initilized!");
		}
		this.tiles = tiles;
		this.off   = new Pos(xoff, yoff);
	}
	
	Pos offset() {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != CompleteWorld.class) {
			throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
		}
		return this.off;
	}
	
	@Override
	public Tile tile(int x, int y) {
		if (this.needUpdate) {
			updateWorld();
		}
		Tile[][] myTiles = this.tiles;
		Tile[]   arr     = myTiles[x];
		Tile     t       = arr[y];
		if (t == null) {
			t      = new Tile(GroundType.NOT_EXPLORED_GRND);
			arr[y] = t;
		}
		return t;
	}
	
	@Override
	public void addNextTurnListener(NextTurnListener listener) {
		// well technically you can now remove the handler with a different user world or the complete world
		this.cw.addNextTurnListener(listener);
	}
	
	@Override
	public void removeNextTurnListener(NextTurnListener listener) {
		this.cw.removeNextTurnListener(listener);
	}
	
	@Override
	public void finish(Turn t) {
		if (t.world != this) {
			throw new IllegalArgumentException("the turn does not belong to me!");
		}
		this.cw.finish(t);
	}
	
	@Override
	public Map<User, List<Entity<?, ?>>> entities() {
		if (this.needUpdate) {
			updateWorld();
		}
		return CompleteWorld.entities(this.tiles);
	}
	
	@Override
	public WorldThing<?, ?> get(UUID uuid) {
		if (this.needUpdate) {
			updateWorld();
		}
		return CompleteWorld.get(this.tiles, uuid);
	}
	
}
