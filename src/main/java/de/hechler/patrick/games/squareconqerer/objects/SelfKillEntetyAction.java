package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class SelfKillEntetyAction extends EntetyAction {
	
	public SelfKillEntetyAction(Entety e) {
		super(e);
	}
	
	@Override
	public String toString() {
		return "Selfkill[entety=" + super.e + "]";
	}
	
}
