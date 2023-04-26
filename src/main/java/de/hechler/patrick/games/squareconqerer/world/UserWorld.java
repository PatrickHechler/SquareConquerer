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
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public final class UserWorld implements World {
	
	private final World             world;
	private final User              usr;
	public final int                modCnt;
	private Map<User, List<Entity>> entities;
	private Tile[][]                cach;
	private boolean[][]             visible;
	
	private UserWorld(World world, User usr, int modCnt) {
		this.world  = world;
		this.usr    = usr;
		this.modCnt = modCnt;
		world.addNextTurnListener(this::nextTurn);
	}

	public static World of(World w, User usr, int modCnt) {
		usr.checkModCnt(modCnt);
		if (w.user() == usr) return w;
		return new UserWorld(w, usr, modCnt);
	}
	
	public static UserWorld usrOf(World w, User usr, int modCnt) {
		usr.checkModCnt(modCnt);
		if (w.user() == usr) throw new IllegalStateException("user of is only possible, when the user changes!");
		return new UserWorld(w, usr, modCnt);
	}
	
	Tile[][] cach() {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != RootWorld.class) {
			throw new IllegalCallerException("this method is intern");
		}
		return this.cach;
	}
	
	@Override
	public User user() {
		this.usr.checkModCnt(this.modCnt);
		return this.usr;
	}
	
	@Override
	public int xlen() {
		this.usr.checkModCnt(this.modCnt);
		return this.world.xlen();
	}
	
	@Override
	public int ylen() {
		this.usr.checkModCnt(this.modCnt);
		return this.world.ylen();
	}
	
	@Override
	public Tile tile(int x, int y) {
		this.usr.checkModCnt(this.modCnt);
		if (this.entities == null) {
			update();
		}
		if (this.visible != null && this.visible[x][y]) {
			return this.cach[x][y];
		} else {
			Tile t = this.cach != null && this.cach[x][y] != null ? this.cach[x][y] : new Tile(TileType.NOT_EXPLORED, OreResourceType.NONE, false);
			addMyEntities(x, y, t);
			return t;
		}
	}
	
	private void addMyEntities(int x, int y, Tile t) throws AssertionError {
		List<Entity> list = this.entities.get(this.usr);
		if (list != null) {
			Unit     u = null;
			Building b = null;
			for (Entity e : list) {
				if (e.x() == x && e.y() == y) {
					if (e instanceof Unit u0) {
						u = u0;
					} else if (e instanceof Building b0) {
						b = b0;
					} else {
						throw new AssertionError("unknown entity type: " + e.getClass());
					}
				}
			}
			t.unit(u);
			t.build(b);
		}
	}
	
	@Override
	public void addNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.world.addNextTurnListener(listener); }
	
	@Override
	public void removeNextTurnListener(BiConsumer<byte[], byte[]> listener) { this.world.removeNextTurnListener(listener); }
	
	@Override
	public Map<User, List<Entity>> entities() {
		if (this.entities == null) {
			update();
		}
		return this.entities;
	}
	
	private void update() {
		Map<User, List<Entity>> es   = new HashMap<>();
		Map<User, List<Entity>> all  = this.world.entities();
		List<Entity>            list = all.get(this.usr);
		if (list == null) {
			this.entities     = Collections.emptyMap();
			this.visible = null;
			return;
		}
		es.put(this.usr, list);
		if (this.cach == null) {
			this.cach = new Tile[this.world.xlen()][this.world.ylen()];
		}
		if (this.visible == null) {
			this.visible = new boolean[this.world.xlen()][this.world.ylen()];
		} else {
			for (boolean[] v : this.visible) { Arrays.fill(v, false); }
		}
		for (Entity e : list) {
			int v = e.viewRange();
			int x = e.x();
			int y = e.y();
			while (--v >= 0) {
				int x0 = x - v;
				int y0 = y;
				for (; x0 <= x; x0++, y0++) {
					cach(es, x, y, x0, y0);
				}
				x0 = x;
				y0 = y - v;
				for (; y0 <= y; x0++, y0++) {
					cach(es, x, y, x0, y0);
				}
				x0 = x + v - 1;
				y0 = y;
				for (; x0 >= x; x0--, y0++) {
					cach(es, x, y, x0, y0);
				}
				x0 = x;
				y0 = y + v - 1;
				for (; x0 >= x; x0--, y0--) {
					cach(es, x, y, x0, y0);
				}
			}
		}
	}
	
	private void cach(Map<User, List<Entity>> es, int x, int y, int x0, int y0) {
		if (x0 < 0 || y0 < 0 || x0 >= this.visible.length || y0 >= this.visible[x0].length || this.visible[x0][y0]) {
			return;
		}
		this.visible[x0][y0] = true;
		Tile t = this.world.tile(x0, y0);
		add(es, t.unit());
		add(es, t.building());
		this.cach[x][y] = t.copy();
	}
	
	private static void add(Map<User, List<Entity>> result, Entity e) {
		if (e != null) {
			List<Entity> list = result.computeIfAbsent(e.owner(), usr -> new ArrayList<>());
			if (!list.contains(e)) {
				list.add(e);
			}
		}
	}
	
	private void nextTurn(byte[] wh, byte[] th) { this.entities = null; }
	
	@Override
	public void finish(Turn t) { this.world.finish(t); }
	
}
