package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.Entety;

public class UsingEntetyAction extends EntetyAction {
	
	public UsingEntetyAction(Entety e) {
		super(e);
	}
	
	@Override
	public String toString() {
		return "UsingEntetyAction[entety=" + e + "]";
	}
	
}
