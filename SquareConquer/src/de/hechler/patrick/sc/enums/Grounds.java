package de.hechler.patrick.sc.enums;

import java.util.HashMap;
import java.util.Map;

public enum Grounds {
	
	water,
	
	mountain,
	
	flat,;
	
	public char character() {
		switch (this) {
		case flat:
			return 'F';
		case mountain:
			return 'M';
		case water:
			return 'W';
		}
		throw new RuntimeException("unknown Ground: name=" + this.name() + " toString -> '" + this + "'");
	}
	
	public static Grounds fromCharacter(char c) {
		switch (c) {
		case 'F':
			return flat;
		case 'M':
			return mountain;
		case 'W':
			return water;
		}
		throw new RuntimeException("unknown char: c='" + c + "'");
	}
	
	public static Map <String, Grounds> names() {
		Map <String, Grounds> res = new HashMap <String, Grounds>();
		for (Grounds g : values()) {
			res.put(g.name(), g);
		}
		return res;
	}
	
}
