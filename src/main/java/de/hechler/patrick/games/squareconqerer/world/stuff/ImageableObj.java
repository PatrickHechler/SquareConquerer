package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.awt.image.BufferedImage;
import java.net.URL;

public interface ImageableObj {
	
	int ordinal();
	
	BufferedImage resource();
	
	void resource(BufferedImage nval);
	
	String name();
	
	default String cls() { return getClass().getSimpleName(); }
	
	default URL url() { return this.getClass().getResource("/img/" + this.cls() + "/" + this.name() + ".png"); }
	
}
