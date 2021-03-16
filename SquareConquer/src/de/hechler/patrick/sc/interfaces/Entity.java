package de.hechler.patrick.sc.interfaces;

import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;

public interface Entity {
	
	/**
	 * returns the actual {@link Position} of this {@link Entity}
	 * 
	 * @return the actual {@link Position} of this {@link Entity}
	 */
	Position position();
	
	/**
	 * returns the {@link Field} on which this {@link Entity} stays at the moment
	 * 
	 * @return the {@link Field} on which this {@link Entity} stays at the moment
	 */
	Field field();
	
	/**
	 * returns <code>true</code>, if this {@link Position} has the ability to move. <br>
	 * if this {@link Entity} is movable, it should be an {@code instanceof} {@link MovableEntity}.
	 * 
	 * @return <code>true</code>, if this {@link Position} has the ability to move
	 */
	boolean isMovable();
	
	/**
	 * returns a {@link Set} of all {@link Grounds}, on which this {@link Entity} can Exist (be built or move (if movable))
	 * 
	 * @return
	 */
	Set <Grounds> canExsitOn();
	
	/**
	 * returns the remaining actions for this turn of this {@link Entity}
	 * 
	 * @return the remaining actions for this turn of this {@link Entity}
	 */
	int remainingActions();
	
	/**
	 * tells this {@link Entity}, to start a new turn, so the {@link #remainingActions()} will be reseted after this call
	 */
	void newTurn();
	
}
