package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Direction;

public interface Position extends Comparable <Position>, Cloneable {
	
	/**
	 * returns the X-Position of this {@link Position}
	 * 
	 * @return the X-Position of this {@link Position}
	 */
	int getX();
	
	/**
	 * returns the Y-Position of this {@link Position}
	 * 
	 * @return the Y-Position of this {@link Position}
	 */
	int getY();
	
	/**
	 * compares the sum of its own coordinates with the sum of the other coordinates.
	 */
	@Override
	default int compareTo(Position o) {
		int res = getX() + getY() - o.getX() - o.getY();
		return ( (res > 0) ? 1 : ( (res < 0) ? -1 : 0));
	}
	
	default int distance(Position to) {
		int xd = getX() - to.getX();
		int yd = getY() - to.getY();
		return Math.abs(xd) + Math.abs(yd);
	}
	
	/**
	 * Creates no new {@link Position} and moves this {@link Position} one {@link Field} to the {@link Direction} {@code dir}.
	 * 
	 * @param dir
	 *            the {@link Direction} to bee moved
	 * @throws UnsupportedOperationException
	 *             if this {@link Position} is not changeable or movable
	 */
	void move(Direction dir) throws UnsupportedOperationException;
	
	/**
	 * Creates a new {@link Position} and moves the new {@link Position} one {@link Field} to the {@link Direction} {@code dir}
	 * 
	 * @param dir
	 *            the {@link Direction} of the new {@link Position} to be moved
	 * @return the moved and created {@link Position}
	 * @implNote it has to behave exactly as <code>pos.{@link #clone()}.{@link #move(Direction)}</code>
	 */
	Position newCreateMove(Direction dir);
	
	Position clone();
	
}
