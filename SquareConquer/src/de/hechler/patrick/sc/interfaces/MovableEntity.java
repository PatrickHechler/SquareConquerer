package de.hechler.patrick.sc.interfaces;

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
	 */
	void move(Position dest);
	
}
