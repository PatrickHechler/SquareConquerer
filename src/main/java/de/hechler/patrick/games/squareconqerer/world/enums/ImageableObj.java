package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

public interface ImageableObj {
	
	int ordinal();
	
	BufferedImage resource();
	
	boolean resolution();
	
	void resource(BufferedImage nval);
	
	void resolution(boolean nval);
	
	boolean multipleResolutions();
	
	String name();
	
}
