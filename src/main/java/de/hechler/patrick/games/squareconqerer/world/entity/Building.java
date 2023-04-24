package de.hechler.patrick.games.squareconqerer.world.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.objects.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

public sealed interface Building extends Entity, ImageableObj permits BuildingImpl {
	
	/**
	 * this value holds the amount of different building types plus {@code 1} (for
	 * <code>null</code>/none)
	 */
	static final int COUNT = Intern.calcCount();
	
	static final int COUNT_NO_NULL = COUNT - 1;
	
	static class Intern {
		
		private Intern() {}
		
		private static int calcCount() {
			int         cnt   = 1;
			Set<String> names = new HashSet<>();
			for (SquareConquererAddon addon : SquareConquererAddon.addons()) {
				Map<Class<? extends Entity>, String> cls = addon.entities().entityClassses();
				for (Entry<Class<? extends Entity>, String> entry : cls.entrySet()) {
					if (Building.class.isAssignableFrom(entry.getClass())) continue;
					if (names.add(entry.getValue())) cnt++;
				}
				names.clear();
			}
			return cnt;
		}
		
	}
	
	boolean isFinishedBuild();
	
	int remainingBuildTurns();
	
	void store(Unit u, int amount) throws TurnExecutionException;
	
	EnumIntMap<ProducableResourceType> neededResources();
	
	void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException;
	
	void build(Unit u) throws TurnExecutionException;
	
	@Override
	Building copy();
	
	static int ordinal(Building b) {
		return b == null ? 0 : b.ordinal();
	}
	
}
