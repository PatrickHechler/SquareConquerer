package de.hechler.patrick.sc.enums;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum ResourcenEnum {
	
	münzen,
	
	goldbarren, silberbarren,
	
	goldbrocken, silberbrocken,
	
	eisenbaren, eisenbrocken,
	
	kohle,
	
	wasser,
	
	brot,
	
	mehl,
	
	getreide,
	
	baumstämme, holzbretter,
	
	steinbrocken, steinziegel,
	
	;
	
	public static final Set <ResourcenEnum> ALLE;
	static {
		Set <ResourcenEnum> zw = new HashSet <>();
		ResourcenEnum[] vals = ResourcenEnum.values();
		for (ResourcenEnum add : vals) {
			zw.add(add);
		}
		ALLE = Collections.unmodifiableSet(zw);
	}
	
}
