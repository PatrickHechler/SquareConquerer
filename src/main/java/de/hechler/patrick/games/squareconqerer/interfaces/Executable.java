package de.hechler.patrick.games.squareconqerer.interfaces;


@FunctionalInterface
public interface Executable<T extends Throwable> {
	
	void execute() throws T;
	
}
