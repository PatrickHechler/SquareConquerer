package de.hechler.patrick.games.squareconqerer.objects;

import java.util.function.BiConsumer;

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
	
	public Entety getEntety() {
		return unit;
	}
	
	//@formatter:off
	static BiConsumer <Tile, Building> bc = (a, b) -> {};
	static BiConsumer <Tile, Entety> ec = (a, b) -> {};
	//@formatter:on
	
	void setBuild(Building build) {
		if (this.build != null) {
			throw new IllegalStateException("I have already a building! this.build='" + this.build + "', build='" + build + "'");
		}
		if (build == null) {
			throw new IllegalArgumentException("No null argument allowed!");
		}
		Tile.bc.accept(this, build);
		this.build = build;
	}
	
	void setEntety(Entety unit) {
		if (this.unit != null) {
			throw new IllegalStateException("I have already a entety! this.unit='" + this.unit + "', unit='" + unit + "'");
		}
		if (unit == null) {
			throw new IllegalArgumentException("No null argument allowed!");
		}
		Tile.ec.accept(this, unit);
		this.unit = unit;
	}
	
	void remEntety(Entety check) {
		Entety e = this.unit;
		if (e != check) {
			throw new IllegalStateException("I do not have this unit: my='" + e + "' given='" + check + "'");
		}
		this.unit = null;
	}
	
	void remBuild(Building check) {
		Building b = this.build;
		if (b != check) {
			throw new IllegalStateException("I do not have this building: my='" + b + "' given='" + check + "'");
		}
		this.build = null;
	}
	
}
