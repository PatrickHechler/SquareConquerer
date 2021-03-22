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
		int x = Math.abs(getX() - to.getX());
		int y = Math.abs(getY() - to.getY());
		return x + y;
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
	
	/**
	 * creates an {@link String} representing this {@link Position}. <br>
	 * this {@link String} must be formatted like <code>( X_POS | Y_POS )</code>. The whitespace might be left away or added, but it must not be '\r' or '\n'! <br>
	 * <code>pos.toPosStr().replaceAll(" \t", "").equals("(" + pos.getX() + "|" + pos.getY() + ")")</code> must return <code>true</code>!
	 * 
	 * 
	 * @return creates an {@link String} representing this {@link Position}.
	 * 
	 */
	default String toPosStr() {
		return '(' + getX() + " | " + getY() + ')';
	}
	
	default boolean equals(Position pos) {
		return pos.getX() == getX() && pos.getY() == getY();
	}
	
}
