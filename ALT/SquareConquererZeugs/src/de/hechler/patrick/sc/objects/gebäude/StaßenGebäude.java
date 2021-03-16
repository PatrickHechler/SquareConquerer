package de.hechler.patrick.sc.objects.gebäude;

import de.hechler.patrick.sc.enums.GebäudeEnum;
import de.hechler.patrick.sc.objects.GebäudeObj;

public class StaßenGebäude implements GebäudeObj {

	@Override
	public GebäudeEnum gebäudeEnum() {
		return GebäudeEnum.straße;
	}
	
}
