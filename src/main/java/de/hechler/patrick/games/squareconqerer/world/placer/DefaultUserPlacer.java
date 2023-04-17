package de.hechler.patrick.games.squareconqerer.world.placer;

import java.awt.Point;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.Random;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.stuff.UserPlacer;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;


public class DefaultUserPlacer implements UserPlacer {
	
	private final EnumIntMap<EntityType> entityAmounts;
	
	public DefaultUserPlacer() {
		this.entityAmounts = new EnumIntMap<>(EntityType.class);
		// TODO default values
	}
	
	public DefaultUserPlacer(EnumIntMap<EntityType> entityAmounts) {
		this.entityAmounts = entityAmounts;
	}
	
	@Override
	public void initilize(World world, User[] usrs, Random rnd) {
		int[] arr = entityAmounts.array();
		int   sum = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] < 0) throw new IllegalStateException("amount is negative: " + arr[i] + " : " + EntityType.of(i));
			sum += arr[i];
		}
		RootWorld.shuffle(rnd, usrs);
		int     size = (int) Math.sqrt(sum) + 1;
		Point[] p    = new Point[usrs.length];
		int     xlen = world.xlen();
		int     ylen = world.ylen();
		for (int i = 0; i < p.length; i++) {
			int x;
			int y;
			int cnt = 0;
			do {
				if (cnt++ == 8) {
					checkPossible(p, i, size, xlen, ylen);
					System.err.println("tried 8 random invalid positions, there is at least one possible free position, I will continue");
				}
				x = rnd.nextInt(xlen - size);
				y = rnd.nextInt(ylen - size);
			} while (isUsed(p, i, size, x, y));
			p[i] = new Point(x, y);
			initUsr(rnd, usrs[i], world, x, y, size, sum);
		}
	}
	
	private void initUsr(Random rnd, User usr, World world, int x, int y, int size, int unitCount) {
		int[]   iarr = entityAmounts.array().clone();
		Point[] p    = new Point[unitCount];
		for (int i = 0; unitCount > 0; i++) {
			int  unit    = rnd.nextInt(unitCount--);
			int  ordinal = orid(iarr, unit);
			Tile t       = world.tile(x, y);
			int  x0;
			int  y0;
			int  cnt     = 0;
			do {
				if (cnt++ == 8) {
					checkPossible(p, i, 1, size, size);
					System.err.println("tried 8 random invalid positions, there is at least one possible free position, I will continue");
				}
				x0 = rnd.nextInt(size);
				y0 = rnd.nextInt(size);
			} while (isUsed(p, i, size, x0, y0));
			x0 += x;
			y0 += y;
			switch (EntityType.of(ordinal)) {
			case CARRIER -> t.unit(new Carrier(x0, y0, usr));
			case STORE_BUILD -> t.build(new StoreBuild(x0, y0, usr));
			default -> throw new AssertionError("unknown entity type: " + EntityType.of(ordinal));
			}
		}
	}
	
	private static int orid(int[] iarr, int unit) {
		for (int o = 0;; o++) {
			unit -= iarr[o];
			if (unit < 0) {
				iarr[o]--;
				return o;
			}
		}
	}
	
	private static void checkPossible(Point[] p, int i, int size, int xlen, int ylen) {
		xlen -= size;
		ylen -= size;
		int need = p.length - i;
		for (int x = 0; x < xlen; x++) {
			for (int y = 0; y < ylen; y++) {
				if (!isUsed(p, i, size, x, y) && --need <= 0) return;
			}
		}
		throw new IllegalStateException("world is full, there is not enugh place for all players");
	}
	
	private static boolean isUsed(Point[] p, int i, int size, int x, int y) {
		while (i-- > 0) {
			if (Math.abs(x - p[i].x) < size && Math.abs(y - p[i].y) < size) {
				return true;
			}
		}
		return false;
	}
	
	public int get(EntityType e) { return entityAmounts.get(e); }
	
	public void set(EntityType e, int val) { entityAmounts.set(e, val); }
	
	public int add(EntityType e, int val) { return entityAmounts.add(e, val); }
	
	public int sub(EntityType e, int val) { return entityAmounts.sub(e, val); }
	
	public int inc(EntityType e) { return entityAmounts.inc(e); }
	
	public int dec(EntityType e) { return entityAmounts.dec(e); }
	
}
