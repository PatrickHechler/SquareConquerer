package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

public interface ImageableEnum {
	
	BufferedImage resource();
	
	boolean resolution();
	
	void resource(BufferedImage nval);
	
	void resolution(boolean nval);
	
}
