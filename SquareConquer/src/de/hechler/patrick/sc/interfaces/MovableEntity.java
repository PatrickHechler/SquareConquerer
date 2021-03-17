package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Direction;

public interface MovableEntity extends Entity {
	
	@Override
	default boolean isMovable() {
		return true;
	}
	
	/**
	 * moves this {@link Entity} and lowers the numbers of remaining actions for this turn by the {@link Position#distance(Position)} of the starting {@link Position} and the destiny {@link Position}.
	 * 
	 * @param dest
	 *            the {@link Position} to be moved
	 * @throws IllegalStateException
	 *             if this {@link Entity} has no more actions remaining for this turn.
	 */
	void move(Position dest) throws IllegalStateException;
	
	/**
	 * moves this {@link Entity} and lowers the numbers of remaining actions for this turn by the {@link Position#distance(Position)} of the starting {@link Position} and the destiny {@link Position}.
	 * 
	 * @param dest
	 *            the {@link Position} to be moved
	 * @throws IllegalStateException
	 *             if this {@link Entity} has no more actions remaining for this turn.
	 */
	void move(Direction dir) throws IllegalStateException;
	
	/**
	 * This method teleports the {@link Entity} direct to the {@link Position} {@code pos}
	 * 
	 * @param pos
	 *            The {@link Position} to bee teleported
	 */
	void setPosition(Position pos);
	
}
