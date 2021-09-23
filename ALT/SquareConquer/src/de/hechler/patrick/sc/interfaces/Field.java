package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Grounds;

public interface Field {
	
	/**
	 * returns <code>true</code> if this {@link Field} is an memory<br>
	 * a memory contains only the {@link #ground()} and the last {@link Entity}, if it was an {@link UnmovableEntity}<br>
	 * if the last known {@link Entity} was no {@link UnmovableEntity} it will be removed from this {@link Field}
	 * 
	 * @return <code>true</code> if this {@link Field} is an memory
	 */
	boolean isMemory();
	
	/**
	 * returns the {@link Position} of this {@link Field}
	 * 
	 * @return the {@link Position} of this {@link Field}
	 */
	Position position();
	
	/**
	 * returns the X-Coordinate of the {@link #position()} of this {@link Field}
	 * 
	 * @return the X-Coordinate of the {@link #position()} of this {@link Field}
	 * @see Position#getX()
	 */
	int getXPos();
	
	/**
	 * returns the Y-Coordinate of the {@link #position()} of this {@link Field}
	 * 
	 * @return the Y-Coordinate of the {@link #position()} of this {@link Field}
	 * @see Position#getY()
	 */
	int getYPos();
	
	/**
	 * returns the {@link Grounds} of this {@link Field}<br>
	 * if the {@link Grounds} is unknown, because the {@link Field} has not been explored, it will return {@link Grounds#unknown}
	 * 
	 * @return the {@link Grounds} of this {@link Field} or {@link Grounds#unknown} if this {@link Field} has not been explored
	 */
	Grounds ground();
	
	/**
	 * returns <code>true</code>, if {@link #getEntity()} would not return <code>null</code><br>
	 * this method is the equals to <code>{@link #getEntity()} == null</code>
	 * 
	 * @return <code>true</code>, if {@link #getEntity()} would not return <code>null</code>
	 * @see <code>{@link #getEntity()} == null</code>
	 */
	boolean hasEntity();
	
	/**
	 * returns the {@link Entity} of this {@link Field}<br>
	 * if this {@link Field} does not contain an {@link Entity} it will return <code>null</code><br>
	 * A {@link Field} can only contain one {@link Entity} at once or no {@link Entity}
	 * 
	 * @return the {@link Entity} of this {@link Field} or <code>null</code> if this {@link Field} has no {@link Entity}
	 */
	Entity getEntity();
	
	/**
	 * overrides the {@link Entity} of this {@link Field} with the <code>{@link Entity} entity</code><br>
	 * 
	 * @param entity
	 *            the new {@link Entity} of this {@link Field}
	 */
	void setEntity(Entity entity);
	
}
