package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public abstract class EntetyAction extends Action {
	
	final Entety e;

	public EntetyAction(Entety e) {
		this.e = e;
	}
	
}
