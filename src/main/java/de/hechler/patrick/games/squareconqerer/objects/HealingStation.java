package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class HealingStation implements Building, Cloneable {
	
	private int build = 2;
	
	@Override
	public void use(Entety e) {
		if (build > 0) {
			build -- ;
		} else {
			e.heal(1);
		}
	}
	
	@Override
	public boolean usable(Entety e) {
		return true;
	}
	
	@Override
	public void act(Tile pos) throws TurnExecutionException {
		throw new TurnExecutionException("a healing station can't act on it's own!");
	}
	
	@Override
	public boolean actable(Tile pos) {
		return false;
	}
	
	@Override
	public BuildingFactory factory() {
		return BuildingFactory.healing_station;
	}
	
	@Override
	public int buildLen() {
		return build;
	}
	
	@Override
	public HealingStation clone() {
		try {
			return (HealingStation) super.clone();
		} catch (CloneNotSupportedException e) {
			HealingStation hs = new HealingStation();
			hs.build = this.build;
			return hs;
		}
	}
	
	@Override
	public String toString() {
		return "HealingStation[build=" + build + "]";
	}
	
}
