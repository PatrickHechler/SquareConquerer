package de.hechler.patrick.sc.zeugs;

import java.util.EnumMap;
import java.util.Map;

import de.hechler.patrick.sc.enums.EinheitenEnum;
import de.hechler.patrick.sc.fehler.CreationException;
import de.hechler.patrick.sc.fehler.GebäudeBauException;
import de.hechler.patrick.sc.objects.Einheit;
import de.hechler.patrick.sc.objects.Karte;
import de.hechler.patrick.sc.objects.Position;

public abstract class EinheitenFactory <E extends Einheit> {
	
	/**
	 * saves the {@link EinheitenFactory}s to create buildings from the {@link EinheitenEnum}s.
	 */
	private static Map <EinheitenEnum, EinheitenFactory <?>> facs;
	
	/**
	 * resets the map {@link #facs} and may put some default {@link EinheitenFactory}s in it.
	 */
	public static void load() {
		facs = new EnumMap <EinheitenEnum, EinheitenFactory <?>>(EinheitenEnum.class);
	}
	
	/**
	 * puts the new {@link EinheitenFactory} in the {@link Map} {@link #facs} and returns the old mapping for the {@link EinheitenEnum} or <code>null</code>
	 * 
	 * @param art
	 *            the {@link EinheitenEnum} of the fac
	 * @param fac
	 *            the new {@link EinheitenFactory}
	 * @return the old mapping for the {@link EinheitenEnum} or <code>null</code>
	 */
	public static EinheitenFactory <?> put(EinheitenEnum art, EinheitenFactory <?> fac) {
		if (art == null || fac == null) {
			throw new NullPointerException("(art|fac)=null art=" + art + " fac=" + fac);
		}
		return facs.put(art, fac);
	}
	
	
	
	/**
	 * creates an new {@link Einheit} and returns it. The {@link Einheit} will be created on the {@link Position} pos on the {@link Karte} karte.<br>
	 * 
	 * @param art
	 *            the {@link EinheitenEnum} of the {@link Einheit}
	 * @param karte
	 *            the Karte where the {@link Einheit} will be placed
	 * @param pos
	 *            the {@link Position} on the {@link Karte} of the {@link Einheit}
	 * @return the created {@link Einheit}
	 * @throws CreationException
	 *             if something went wrong
	 */
	public static Einheit create(EinheitenEnum art, Karte karte, Position pos) throws CreationException {
		EinheitenFactory <?> fac = facs.get(art);
		if (fac == null) {
			throw new GebäudeBauException("unknown art=" + art + " facs=" + facs);
		}
		return fac.create(karte, pos);
	}
	
	/**
	 * creates an new {@link Einheit} and returns it. The {@link Einheit} will be created on the {@link Position} pos on the {@link Karte} karte.
	 * 
	 * @param karte
	 *            the Karte where the {@link Einheit} will be placed
	 * @param pos
	 *            the {@link Position} on the {@link Karte} of the {@link Einheit}
	 * @return the created {@link Einheit}
	 * @throws CreationException
	 *             if something went wrong
	 */
	public abstract E create(Karte karte, Position pos) throws CreationException;
	
	/**
	 * creates an new {@link Einheit} and returns it.
	 * 
	 * @return the created {@link Einheit}
	 * @throws CreationException
	 *             if something went wrong
	 */
	public abstract E create() throws CreationException;
	
}
