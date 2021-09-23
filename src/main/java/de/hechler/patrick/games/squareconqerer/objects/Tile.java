package de.hechler.patrick.games.squareconqerer.objects;

import java.util.Map;

import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionRuntimeException;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;


public class Tile {
	
	private Building build;
	private Entety unit;
	private final int x;
	private final int y;
	
	public Tile(int x, int y) {
		this.build = null;
		this.unit = null;
		this.x = x;
		this.y = y;
	}
	
	
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Building getBuild() {
		return build;
	}
	
	public Entety getUnit() {
		return unit;
	}
	
	void setBuild(Building build) {
		if (this.build != null) {
			throw new IllegalStateException("I have already a building! this.build='" + this.build + "', build='" + build + "'");
		}
		if (build == null) {
			throw new IllegalArgumentException("No null argument allowed!");
		}
		this.build = build;
	}
	
	void setEntety(Entety unit) {
		if (this.unit != null) {
			throw new IllegalStateException("I have already a entety! this.unit='" + this.unit + "', unit='" + unit + "'");
		}
		if (unit == null) {
			throw new IllegalArgumentException("No null argument allowed!");
		}
		this.unit = unit;
	}
	
	Entety remEntety() {
		Entety u = this.unit;
		if (u == null) {
			throw new IllegalStateException("I have no unit!");
		}
		this.unit = null;
		return u;
	}
	
	void copy(Tile from, Map <Object, Object> mapping) {
		assert build == null;
		assert unit == null;
		if (from.build != null) {
			this.build = from.build.clone();
			if (null != mapping.put(from.build, this.build)) {
				throw new InternalError("mapped a object multilple times!");
			}
		}
		if (from.unit != null) {
			this.unit = from.unit.clone();
			if (null != mapping.put(from.unit, this.unit)) {
				throw new InternalError("mapped a object multilple times!");
			}
		}
	}
	
}
