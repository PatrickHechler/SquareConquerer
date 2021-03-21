package de.hechler.patrick.sc.interfaces;

import java.util.Map;

import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.objects.World;

public interface Player {
	
	void makeTurn(World world, Map <Resources, ? extends Number> resources);
	
}
