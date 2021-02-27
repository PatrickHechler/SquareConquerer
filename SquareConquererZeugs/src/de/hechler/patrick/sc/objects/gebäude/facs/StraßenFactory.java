package de.hechler.patrick.sc.objects.gebäude.facs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.hechler.patrick.sc.enums.GeländerEnum;
import de.hechler.patrick.sc.fehler.GebäudeBauException;
import de.hechler.patrick.sc.objects.Karte;
import de.hechler.patrick.sc.objects.Position;
import de.hechler.patrick.sc.objects.gebäude.StaßenGebäude;
import de.hechler.patrick.sc.zeugs.GebäudeFactory;


public class StraßenFactory extends GebäudeFactory <StaßenGebäude> {
	
	private static final Set <GeländerEnum> GELÄNDER;
	
	static {
		Set <GeländerEnum> gel = new HashSet <GeländerEnum>();
		gel.add(GeländerEnum.steinberg);
		gel.add(GeländerEnum.goldberg);
		gel.add(GeländerEnum.silberberg);
		gel.add(GeländerEnum.eisenberg);
		gel.add(GeländerEnum.kohleberg);
		gel.add(GeländerEnum.hügel);
		gel.add(GeländerEnum.wald);
		gel.add(GeländerEnum.wiese);
		gel.add(GeländerEnum.wüste);
		GELÄNDER = Collections.unmodifiableSet(gel);
	}
	
	@Override
	public StaßenGebäude create(Karte karte, Position pos) throws GebäudeBauException {
		return karte.baue(this, pos);
	}
	
	@Override
	public StaßenGebäude create() throws GebäudeBauException {
		return new StaßenGebäude();
	}
	
	@Override
	public Set <GeländerEnum>[][] platz() {
		@SuppressWarnings("unchecked")
		Set <GeländerEnum>[][] erg = new Set[1][1];
		erg[0][0] = GELÄNDER;
		return erg;
	}
	
}
