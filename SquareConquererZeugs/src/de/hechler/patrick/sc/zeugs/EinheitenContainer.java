package de.hechler.patrick.sc.zeugs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.enums.EinheitenEnum;
import de.hechler.patrick.sc.fehler.CreationException;
import de.hechler.patrick.sc.objects.Einheit;
import de.hechler.patrick.sc.objects.Karte;
import de.hechler.patrick.sc.objects.Position;

public class EinheitenContainer {
	
	/**
	 * wird genutzt, um alle {@link Einheit} zu speichern
	 */
	private static Map <EinheitenEnum, Map <Position, Set <Einheit>>> einheiten;
	
	/**
	 * Kreiert eine neue {@link Einheit}
	 * 
	 * @param art
	 *            die art der {@link Einheit}
	 * @param karte
	 *            Die {@link Karte} der {@link Einheit}
	 * @param pos
	 *            die {@link Position} der {@link Einheit}
	 * @return die kreierte {@link Einheit}
	 * @throws CreationException
	 *             wenn etwas schiefgelaufen ist.
	 */
	public static Einheit create(final EinheitenEnum art, Karte karte, Position pos) throws CreationException {
		int cnt = 0;
		for (Map <Position, Set <Einheit>> zw : einheiten.values()) {
			Set <Einheit> add = zw.get(pos);
			if (add != null) cnt += add.size();
		}
		if (cnt >= karte.maximaleEinheitenAnzahl(pos)) {
			throw new CreationException("maximale Einheitenanzahl würde überschritten werden: cnt=" + cnt + " max=" + karte.maximaleEinheitenAnzahl(pos));
		}
		final Einheit neu = EinheitenFactory.create(art, karte, pos);
		final Map <Position, Set <Einheit>> zw = einheiten.get(art);
		Set <Einheit> test = zw.get(pos);
		if (test == null) {
			test = new HashSet <Einheit>();
			zw.put(pos, test);
		}
		neu.positionChangeChecker((op, np) -> {
			Set <Einheit> s = zw.get(np);
			if (s.size() >= karte.maximaleEinheitenAnzahl(np)) {
				return false;
			}
			s.add(neu);
			s = zw.get(op);
			s.remove(neu);
			return true;
		});
		return neu;
	}
	
	/**
	 * signalisiert allen {@link Einheit}en mit {@link Einheit#neuerZug()}, dass ein neuer Zug angebrochen ist.
	 */
	public void neuerZug() {
		for (Map <Position, Set <Einheit>> outer : einheiten.values()) {
			for (Set <Einheit> middle : outer.values()) {
				for (Einheit inner : middle) {
					inner.neuerZug();
				}
			}
		}
	}
	
}
