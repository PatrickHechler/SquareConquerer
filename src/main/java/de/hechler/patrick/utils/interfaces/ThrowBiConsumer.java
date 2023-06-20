package de.hechler.patrick.utils.interfaces;

@FunctionalInterface
public interface ThrowBiConsumer<A, B, T extends Throwable> {
	
	void accept(A a, B b) throws T;
	
}
