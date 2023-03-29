package de.hechler.patrick.games.squareconqerer.interfaces;


public interface ThrowConsumer<T, E extends Throwable> {
	
	void accept(T t) throws E;
	
}
