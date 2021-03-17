package de.hechler.patrick.sc.utils;

import java.util.Collections;
import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.utils.objects.EnumSet;

public class Units {
	
	private static final Set <Grounds> WATER             = set(Grounds.water);
	private static final Set <Grounds> FLAT              = set(Grounds.flat);
	private static final Set <Grounds> MOUNTAIN_AND_FLAT = set(Grounds.mountain, Grounds.flat);
	
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
			return ground == Grounds.water;
		case bow:
		case builder:
		case simple:
		case meele:
		case farm:
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
		case spring:
		case storage:
			return ground == Grounds.flat;
		case mine:
			return ground != Grounds.water;
		default:
			throw new RuntimeException("unknown entytyType: entity=" + entity);
		}
	}
	
	public static Set <Grounds> canExistOn(Type entity) {
		switch (entity) {
		case boat:
			return WATER;
		case bow:
		case builder:
		case farm:
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
		case meele:
		case simple:
		case spring:
		case storage:
			return FLAT;
		case mine:
			return MOUNTAIN_AND_FLAT;
		}
		throw new RuntimeException("unknown Type: " + entity);
	}
	
}
