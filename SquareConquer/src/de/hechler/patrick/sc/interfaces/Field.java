package de.hechler.patrick.sc.interfaces;

import de.hechler.patrick.sc.enums.Grounds;

public interface Field {
	
	Position position();
	
	Grounds ground();
	
	boolean hasEntity();
	
	Entity getEntity();
	
	void setEntity(Entity entity);
	
}
