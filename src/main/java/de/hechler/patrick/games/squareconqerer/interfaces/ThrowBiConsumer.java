package de.hechler.patrick.games.squareconqerer.interfaces;


public interface ThrowBiConsumer<A, B, E extends Throwable> {
	
	void accept(A a, B b) throws E;
	
}
