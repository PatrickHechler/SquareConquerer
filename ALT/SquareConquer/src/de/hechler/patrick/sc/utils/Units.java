package de.hechler.patrick.sc.utils;

import java.util.Collections;
import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.utils.objects.EnumSet;

public class Units {
	
	private static final Set <Grounds> WATER           = set(Grounds.water);
	private static final Set <Grounds> FLAT_AND_FOREST = set(Grounds.flat, Grounds.forest);
	private static final Set <Grounds> FOREST          = set(Grounds.forest);
	private static final Set <Grounds> ALL_EXEPT_WATER = set(Grounds.mountain, Grounds.flat, Grounds.forest);
	
	private static final Set <Grounds> set(Grounds... grounds) {
		Set <Grounds> res = new EnumSet <Grounds>(Grounds.class);
		for (Grounds ad : grounds) {
			res.add(ad);
		}
		return Collections.unmodifiableSet(res);
	}
	
	
	
	public static boolean canExistOn(Type entity, Grounds ground) {
		switch (entity) {
		case boat:
		case fightingBoat:
			return ground == Grounds.water;
		case bow:
		case builder:
		case simple:
		case meele:
		case carrier:
		case farm:
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
		case spring:
		case storage:
			return ground == Grounds.flat || ground == Grounds.forest;
		case mine:
			return ground != Grounds.water;
		case woodFarm:
			return ground == Grounds.forest;
		case buildplace:
			throw new UnsupportedOperationException("buildplace does not support this operation!");
		}
		throw new RuntimeException("unknown entytyType: entity=" + entity);
	}
	
	public static Set <Grounds> canExistOn(Type entity) {
		switch (entity) {
		case boat:
		case fightingBoat:
			return WATER;
		case bow:
		case builder:
		case farm:
		case house:
		case houseBow:
		case carrier:
		case houseBuilder:
		case houseMelee:
		case meele:
		case simple:
		case spring:
		case storage:
			return FLAT_AND_FOREST;
		case mine:
			return ALL_EXEPT_WATER;
		case woodFarm:
			return FOREST;
		case buildplace:
			throw new UnsupportedOperationException("buildplace does not support this operation!");
		}
		throw new RuntimeException("unknown Type: " + entity);
	}
	
}
