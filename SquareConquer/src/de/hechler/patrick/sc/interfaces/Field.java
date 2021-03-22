package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Grounds;

public interface Field {
	
	boolean isMemory();
	
	Position position();
	
	int getXPos();
	
	int getYPos();
	
	Grounds ground();
	
	boolean hasEntity();
	
	Entity getEntity();
	
	void setEntity(Entity entity);
	
}
