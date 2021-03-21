package de.hechler.patrick.sc.enums;

import java.util.HashMap;
import java.util.Map;

public enum Type {
	
	simple,
	
	meele, bow,
	
	builder,
	
	boat,
	
	
	house, houseMelee, houseBow, houseBuilder,
	
	storage,
	
	spring, farm, mine,;
	
	public static Map <String, Type> names() {
		Map <String, Type> res = new HashMap <>();
		for (Type g : values()) {
			res.put(g.name(), g);
		}
		return res;
	}
	
}
