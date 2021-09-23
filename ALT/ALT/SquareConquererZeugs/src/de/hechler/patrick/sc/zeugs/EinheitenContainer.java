package de.hechler.patrick.sc.zeugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.enums.EinheitenEnum;
import de.hechler.patrick.sc.fehler.CreationException;
import de.hechler.patrick.sc.fehler.UngültigeBewegungException;
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
		final Einheit neu = EinheitenFactory.create(art);
		final Map <Position, Set <Einheit>> zw = einheiten.get(art);
		Set <Einheit> test = zw.get(pos);
		if (test == null) {
			test = new HashSet <Einheit>();
			zw.put(pos, test);
		}
		neu.positionChangeChecker((op, np, g) -> {
			Set <Einheit> s = zw.get(np);
			if (s.size() + (g == null ? 0 : 1) >= karte.maximaleEinheitenAnzahl(np)) {
				throw new UngültigeBewegungException("zu viele Einheiten auf dem Ziefeld");
			}
			if ( !neu.kannAuf(karte.geländeVon(np))) {
				throw new UngültigeBewegungException("Einheit kann das Zielfeld nicht betreten (ziel=" + karte.geländeVon(np));
			}
			s.add(neu);
			s = zw.get(op);
			s.remove(neu);
		});
		return neu;
	}
	
	/**
	 * signalisiert allen {@link Einheit}en mit {@link Einheit#neuerZug()}, dass ein neuer Zug angebrochen ist.
	 * 
	 * @param karte
	 *            Die {@link Karte}
	 */
	public static void neuerZug(Karte karte) {
		List <NewUnit> add = new ArrayList <>();
		Map <Position, List <Einheit>> rem = new HashMap <>();
		for (Map <Position, Set <Einheit>> outer : einheiten.values()) {
			for (Set <Einheit> middle : outer.values()) {
				for (Einheit inner : middle) {
					if (inner.neuerZug()) {
						try {
							EinheitenEnum entw = inner.entwickeltZu();
							EinheitenFactory <?> fac = inner.entwickleMit();
							Einheit created;
							if (fac == null) {
								created = EinheitenFactory.create(entw);
							} else {
								created = fac.create();
							}
							Position pos = inner.position();
							List <Einheit> del = rem.get(pos);
							if (del == null) {
								del = new ArrayList <Einheit>();
								rem.put(pos, del);
							}
							del.add(inner);
							add.add(new NewUnit(created, pos, entw));
						} catch (CreationException e) {
							System.err.println("could not evolve unit: " + inner);
							e.printStackTrace();
						}
					}
				}
			}
		}
		for (Map <Position, Set <Einheit>> outer : einheiten.values()) {
			rem.forEach((pos, units) -> {
				Set <Einheit> del = outer.get(pos);
				if (del == null) return;
				del.removeAll(units);
			});
		}
		for (NewUnit newUnit : add) {
			Map <Position, Set <Einheit>> map = einheiten.get(newUnit.eenum);
			if (map == null) {
				map = new HashMap <Position, Set<Einheit>>();
				einheiten.put(newUnit.eenum, map);
			}
			Set <Einheit> set = map.get(newUnit.pos);
			if (set == null) {
				set = new HashSet <Einheit>();
				map.put(newUnit.pos, set);
			}
			set.add(newUnit.unit);
		}
	}
	
	private static class NewUnit {
		
		Einheit       unit;
		Position      pos;
		EinheitenEnum eenum;
		
		public NewUnit(Einheit unit, Position pos, EinheitenEnum eenum) {
			this.unit = unit;
			this.pos = pos;
			this.eenum = eenum;
		}
		
	}
	
}
