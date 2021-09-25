package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class AttackEntetyAction extends EntetyAction {
	
	final Entety defender;
	
	public AttackEntetyAction(Entety attacker, Entety defender) {
		super(attacker);
		this.defender = defender;
	}
	
	@Override
	public String toString() {
		return "AttackEntetyAction [attacker=" + e + ", defender=" + defender + "]";
	}
	
}
