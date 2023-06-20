package de.hechler.patrick.utils.interfaces;

@FunctionalInterface
public interface Executable<T extends Throwable> {
	
	void execute() throws T;
	
}
