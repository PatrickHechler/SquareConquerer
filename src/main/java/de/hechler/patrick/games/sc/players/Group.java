package de.hechler.patrick.games.sc.players;

import java.util.HashMap;
import java.util.Map;

public class Group {
	
	public final String              name;
	private final Map<String, Group> sub = new HashMap<>();
	
	public Group(String name) {
		this.name = name;
	}
	
}
