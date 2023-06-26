package de.hechler.patrick.games.sc.addons.def;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.entity.Unit;

public abstract class SimpleUnit extends Unit {
	
	protected final Map<String, Value> vals;
	
	public SimpleUnit(UUID uuid, Map<String, Value> vals) {
		super(uuid);
		this.vals = new HashMap<>(vals);
	}
	
	@Override
	public Map<String, Value> values() {
		return new TreeMap<>(this.vals);
	}
	
	@Override
	public Value value(String name) {
		Value v = this.vals.get(name);
		if (v == null) {
			throw new IllegalArgumentException("no value with the name '" + name + "' found");
		}
		return v;
	}
	
	@Override
	public void value(Value newValue) {
		this.vals.put(newValue.name(), newValue);
	}
	
}
