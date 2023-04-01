package de.hechler.patrick.games.squareconqerer.world.turn;

import java.lang.StackWalker.Option;
import java.util.HashMap;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;

public final class Turn {
	
	public final User               usr;
	private Map<Entity, EntityTurn> turns = new HashMap<>();
	
	public Turn(User usr) {
		this.usr = usr;
	}
	
	/**
	 * this is an intern method, do not use it
	 * 
	 * @throws UnsupportedOperationException for you, always
	 */
	public Map<Entity, EntityTurn> turns() throws UnsupportedOperationException {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != RootWorld.class) {
			throw new UnsupportedOperationException("this is an intern method");
		}
		return turns;
	}
	
}
