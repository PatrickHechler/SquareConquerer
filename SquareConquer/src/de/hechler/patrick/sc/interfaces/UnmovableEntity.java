package de.hechler.patrick.sc.interfaces;

public interface UnmovableEntity extends Entity {
	
	@Override
	default boolean isMovable() {
		return false;
	}
	
}
