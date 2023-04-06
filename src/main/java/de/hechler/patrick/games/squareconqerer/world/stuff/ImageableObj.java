package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.awt.image.BufferedImage;

public interface ImageableObj {
	
	int ordinal();
	
	BufferedImage resource();
	
	void resource(BufferedImage nval);
	
	String name();
	
	default String cls() { return getClass().getSimpleName(); }
	
}
