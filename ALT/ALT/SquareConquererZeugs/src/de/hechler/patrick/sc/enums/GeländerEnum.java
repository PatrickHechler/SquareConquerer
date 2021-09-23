package de.hechler.patrick.sc.enums;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum GeländerEnum {
	
	steinberg,
	
	goldberg,
	
	silberberg,
	
	eisenberg,
	
	kohleberg,
	
	
	
	hügel,
	
	
	wald,
	
	wiese,
	
	wüste,
	
	
	
	küstenwasser,
	
	tiefeswasser,
	
	ozeanwasser,
	
	
	
	flussHoch,
	
	flussRechts,
	
	flussRunter,
	
	flussLinks,
	
	
	flussHochRechtsKurve,
	
	flussHochLinksKurve,
	
	flussRunterRechtsKurve,
	
	flussRunterLinksKurve,
	
	;
	
	public static final Set <GeländerEnum> ALLE;
	static {
		Set <GeländerEnum> zw = new HashSet <>();
		GeländerEnum[] vals = GeländerEnum.values();
		for (GeländerEnum add : vals) {
			zw.add(add);
		}
		ALLE = Collections.unmodifiableSet(zw);
	}
	
}
