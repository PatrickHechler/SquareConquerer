package de.hechler.patrick.sc.interfaces;

import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;

public interface Entity {
	
	/**
	 * returns the actual {@link Position} of this {@link Entity}
	 * 
	 * @return the actual {@link Position} of this {@link Entity}
	 */
	Position position();
	
	/**
	 * returns <code>true</code>, if this {@link Position} has the ability to move. <br>
	 * if this {@link Entity} is movable, it should be an {@code instanceof} {@link MovableEntity}. <br>
	 * if this {@link Entity} is not movable, it should be an {@code instanceof} {@link UnmovableEntity}.
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
	 * returns the total actions of this {@link Entity}, which it gets when a turn starts
	 * 
	 * @return the remaining actions of this {@link Entity}
	 */
	int totalActions();
	
	/**
	 * uses one action, so the return-value from {@link #remainingActions()} will be lowered by {@code 1}.
	 * 
	 * @throws IllegalStateException
	 *             if there are no actions left to use.
	 */
	void useAction() throws IllegalStateException;
	
	/**
	 * tells this {@link Entity}, to start a new turn, so the {@link #remainingActions()} will be reseted after this call
	 */
	void newTurn();
	
	/**
	 * returns the {@link Type} of this {@link Entity}
	 * 
	 * @return the {@link Type} of this {@link Entity}
	 */
	Type type();
	
	
	/**
	 * returns the sight of this {@link Entity} in {@link Field}s
	 * 
	 * @return the sight of this {@link Entity} in {@link Field}s
	 */
	int sight();
	
}
