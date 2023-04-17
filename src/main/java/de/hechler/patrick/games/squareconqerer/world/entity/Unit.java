package de.hechler.patrick.games.squareconqerer.world.entity;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

public sealed interface Unit extends Entity, ImageableObj, Comparable<Unit> permits UnitImpl {
	
	static final int COUNT = 2;
	
	void changePos(int newx, int newy, Tile checkcanEnter) throws TurnExecutionException;
	
	Resource carryRes();
	
	int carryAmount();
	
	int carryMaxAmount();
	
	void carry(Resource res, int amount) throws TurnExecutionException;
	
	void uncarry(int amount) throws TurnExecutionException;
	
	int moveRange();
	
	@Override
	Unit copy();
	
	static int ordinal(Unit u) {
		return u == null ? 0 : u.ordinal();
	}
	
	/**
	 * compares this Unit with the given other unit
	 * <p>
	 * {@code 0} is only allowed to be returned if the unit has on all
	 * fields an equal value to the fields of this unit
	 * <ol>
	 * <li>the {@link #owner() owners} {@link User#name() name}</li>
	 * <li>the units {@link Object#getClass() class} {@link Class#getName()
	 * name}</li>
	 * <li>the {@link #lives() health}</li>
	 * <li>the {@link #moveRange() move range}</li>
	 * <li>the {@link #maxLives() maximum health}</li>
	 * <li>their {@link #units()}
	 * <ol>
	 * <li>the {@link List#size()}</li>
	 * <li>the lists content starting from {@code 0}</li>
	 * </ol>
	 * </li>
	 * <li>the {@link Object#getClass() class} {@link Class#getName() name}</li>
	 * <li>a unit type specific compare</li>
	 * </ol>
	 * 
	 * @implNote subclasses only have to implement the unit specific compare.<br>
	 *           They should do something like:
	 *           <code>int c = super.compareTo(0); if (c == 0) c = specificCompare((SpecificUnitType) o); return c;</code><br>
	 *           also note that the non unit type specific compare also compares the
	 *           {@link Object#getClass() class} {@link Class#getName() names}, so
	 *           the other unit has the same type if the specific compare is needed
	 */
	@Override
	default int compareTo(Unit o) {
		User u  = owner();
		User ou = o.owner();
		if (u != ou) {
			int cmp = u.name().compareTo(ou.name());
			if (cmp == 0) throw new AssertionError("different users with same name");
			return cmp;
		}
		int l  = lives();
		int ol = o.lives();
		if (l != ol) return Integer.compare(l, ol);
		l  = moveRange();
		ol = o.moveRange();
		if (l != ol) return Integer.compare(l, ol);
		l  = maxLives();
		ol = o.maxLives();
		if (l != ol) return Integer.compare(l, ol);
		List<Unit> us  = units();
		List<Unit> ous = o.units();
		if (us.size() != ous.size()) return Integer.compare(us.size(), ous.size());
		if (!us.isEmpty()) {
			Iterator<Unit> iter  = us.iterator();
			Iterator<Unit> oiter = ous.iterator();
			while (iter.hasNext()) {
				Unit n  = iter.next();
				Unit on = oiter.next();
				int  c  = n.compareTo(on);
				if (c != 0) return c;
			}
			if (oiter.hasNext()) throw new ConcurrentModificationException();
		}
		return getClass().getName().compareTo(o.getClass().getName());
	}
	
}
