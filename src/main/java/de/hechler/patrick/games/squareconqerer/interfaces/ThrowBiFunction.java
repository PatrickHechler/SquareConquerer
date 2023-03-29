package de.hechler.patrick.games.squareconqerer.interfaces;


public interface ThrowBiFunction<R, A, B, E extends Throwable> {
	
	R accept(A a, B b) throws E;
	
}
