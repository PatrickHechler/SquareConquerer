package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.enums.BuildingFactory;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.interfaces.Building;
import de.hechler.patrick.games.squareconqerer.interfaces.Entety;
import de.hechler.patrick.games.squareconqerer.interfaces.Player;


public class UnitMaker implements Building, Cloneable {
	
	private int build = 4;
	private Player last;
	
	@Override
	public void use(Entety e) throws TurnExecutionException {
		if (e.owner() != last) {
			last = e.owner();
		} else if (build > 0) {
			build -- ;
		} else {
			throw new TurnExecutionException("the unit maker does not allow using units when full builded!");
		}
	}
	
	@Override
	public boolean usable(Entety e) {
		return build > 0;
	}
	
	@Override
	public void act(Tile t) throws TurnExecutionException {
		if (build > 0) {
			throw new TurnExecutionException("the unit maker is not fully builded!");
		} else {
			t.setEntety(new Unit(last, t.getX(), t.getY()));
			build = 3;
		}
	}
	
	@Override
	public boolean actable(Tile pos) {
		return build <= 0 && pos.getEntety() == null;
	}
	
	@Override
	public BuildingFactory factory() {
		return BuildingFactory.unit_maker;
	}
	
	@Override
	public int buildLen() {
		return build;
	}
	
	@Override
	public Object snapshot() {
		return new UMSnapshot(this.last, this.build);
	}
	
	@Override
	public void rollback(Object sn) {
		UMSnapshot s = (UMSnapshot) sn;
		this.last = s.last;
		this.build = s.build;
	}
	
	private static final class UMSnapshot {
		
		private final Player last;
		private final int build;
		
		public UMSnapshot(Player last, int build) {
			this.last = last;
			this.build = build;
		}
		
	}
	
	@Override
	public char infoLetter() {
		return last == null ? '-' : last.letter();
	}
	
	@Override
	public String toString() {
		return "UnitMaker [build=" + build + ", last=" + last + "]";
	}
	
}
