package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;


public class DefenceTower implements Building {
	
	private int build = 1;
	
	@Override
	public void use(Entety e) throws TurnExecutionException {
		if (build > 0) {
			build -- ;
		} else {
			throw new TurnExecutionException("this building will be automaticly used if an entety gets attaced on this field!");
		}
	}
	
	@Override
	public boolean usable(Entety e) {
		return build > 0;
	}
	
	@Override
	public void act(Tile pos) throws TurnExecutionException {
		throw new TurnExecutionException("this building can't act");
	}
	
	@Override
	public boolean actable(Tile pos) {
		return false;
	}
	
	@Override
	public BuildingFactory factory() {
		return BuildingFactory.defence_tower;
	}
	
	@Override
	public int buildLen() {
		return build;
	}
	
	@Override
	public char infoLetter() {
		return '-';
	}
	
	@Override
	public Object snapshot() {
		return Integer.valueOf(build);
	}
	
	@Override
	public void rollback(Object sn) {
		this.build = ((Integer) sn).intValue();
	}
	
}
