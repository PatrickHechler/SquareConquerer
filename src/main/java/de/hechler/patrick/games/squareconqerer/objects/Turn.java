package de.hechler.patrick.games.squareconqerer.objects;

import de.hechler.patrick.games.squareconqerer.interfaces.*;
import java.util.*;

public class Turn {
	
	private final List <MoveUnitAction> actions;
	private final Player player;
	
	public Turn(Player player) {
		this(player, new ArrayList <>());
	}
	
	private Turn(Player player, List <MoveUnitAction> actions) {
		this.player = player;
		this.actions = actions;
	}
	
	public void addAction(MoveUnitAction act) {
		this.actions.add(act);
	}
	
	@SuppressWarnings("unchecked")
	public List <MoveUnitAction> getActions() {
		if (actions instanceof ArrayList <?>) {
			return (ArrayList <MoveUnitAction>) ((ArrayList <MoveUnitAction>) this.actions).clone();
		} else {
			return actions;// this is a product of mekeTurn() which returns an unmodifiable list, so no clone is needed
		}
	}
	
	void remove(int i) {
		this.actions.remove(i);
	}
	
	Player getPlayer() {
		return this.player;
	}
	
	public Turn makeTurn() {
		return new Turn(this.player, Collections.unmodifiableList(new ArrayList <>(this.actions)));
	}
	
}
