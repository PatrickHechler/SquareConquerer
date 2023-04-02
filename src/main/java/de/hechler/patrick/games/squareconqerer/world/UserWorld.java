package de.hechler.patrick.games.squareconqerer.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.enums.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.enums.TileType;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

public class UserWorld implements World {
	
	private final World             world;
	private final User              usr;
	public final int                modCnt;
	private Map<User, List<Entity>> entities;
	private Tile[][]                cach;
	private boolean[][]             visible;
	
	public UserWorld(World world, User usr, int modCnt) {
		this.world  = world;
		this.usr    = usr;
		this.modCnt = modCnt;
		usr.checkModCnt(modCnt);
		world.addNextTurnListener(this::nextTurn);
	}
	
	@Override
	public User user() {
		usr.checkModCnt(modCnt);
		return usr;
	}
	
	@Override
	public int xlen() {
		usr.checkModCnt(modCnt);
		return world.xlen();
	}
	
	@Override
	public int ylen() {
		usr.checkModCnt(modCnt);
		return world.ylen();
	}
	
	@Override
	public Tile tile(int x, int y) {
		usr.checkModCnt(modCnt);
		if (entities == null) {
			update();
		}
		if (visible != null && visible[x][y]) {
			return cach[x][y];
		} else {
			Tile t = cach != null && cach[x][y] != null ? cach[x][y] : new Tile(TileType.NOT_EXPLORED, OreResourceType.NONE, false);
			addMyEntities(x, y, t);
			return t;
		}
	}
	
	private void addMyEntities(int x, int y, Tile t) throws AssertionError {
		List<Entity> list = entities.get(usr);
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
	public void addNextTurnListener(Runnable listener) { world.addNextTurnListener(listener); }
	
	@Override
	public void removeNextTurnListener(Runnable listener) { world.removeNextTurnListener(listener); }
	
	@Override
	public Map<User, List<Entity>> entities() {
		if (entities == null) {
			update();
		}
		return entities;
	}
	
	private void update() {
		Map<User, List<Entity>> es   = new HashMap<>();
		Map<User, List<Entity>> all  = world.entities();
		List<Entity>            list = all.get(usr);
		if (list == null) {
			entities     = Collections.emptyMap();
			this.visible = null;
			return;
		}
		es.put(usr, list);
		if (cach == null) {
			cach = new Tile[world.xlen()][world.ylen()];
		}
		if (visible == null) {
			visible = new boolean[world.xlen()][world.ylen()];
		} else {
			for (boolean[] v : visible) { Arrays.fill(v, false); }
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
		if (x0 < 0 || y0 < 0 || x0 >= visible.length || y0 >= visible[x0].length || visible[x0][y0]) {
			return;
		}
		visible[x0][y0] = true;
		Tile t = world.tile(x0, y0);
		add(es, t.unit());
		add(es, t.building());
		cach[x][y] = t.copy();
	}
	
	private static void add(Map<User, List<Entity>> result, Entity e) {
		if (e != null) {
			List<Entity> list = result.computeIfAbsent(e.owner(), usr -> new ArrayList<>());
			if (!list.contains(e)) {
				list.add(e);
			}
		}
	}
	
	private void nextTurn() { entities = null; }
	
	@Override
	public void finish(Turn t) { world.finish(t); }
	
}
