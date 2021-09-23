package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.*;


public class Tile {
	
	private Building build;
	private Unit unit;
	
	public Tile() {
		this.build = null;
		this.unit = null;
	}
	
	public Building getBuild() {
		return build;
	}
	
	public Unit getUnit() {
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
	
	void setUnit(Unit unit) {
		if (this.unit != null) {
			throw new IllegalStateException("I have already a entety! this.unit='" + this.unit + "', unit='" + unit + "'");
		}
		if (unit == null) {
			throw new IllegalArgumentException("No null argument allowed!");
		}
		this.unit = unit;
	}
	
	Unit remUnit() {
		Unit u = this.unit;
		if (u == null) {
			throw new IllegalStateException("I have no unit!");
		}
		this.unit = null;
		return u;
	}
	
	void copy(Tile from) {
		assert build == null;
		assert unit == null;
		this.build = from.build;
		this.unit = from.unit;
	}
	
}
