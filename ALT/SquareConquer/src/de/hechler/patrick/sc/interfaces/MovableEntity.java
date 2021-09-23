package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.objects.Game;

public interface MovableEntity extends Entity {
	
	@Override
	default boolean isMovable() {
		return true;
	}
	
	/**
	 * returns the ID of the owner<br>
	 * an owner ID is an randomly generated number, which is only given to one {@link Player} per {@link Game}<br>
	 * the ID of a {@link Player} is never {@code -1}, if the ID of this {@link MovableEntity} is {@code -1}, this {@link MovableEntity} does not belong to a {@link Player}
	 * 
	 * @return the ID of the owner
	 */
	int owner();
	
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
