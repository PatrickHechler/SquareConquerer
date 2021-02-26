package de.hechler.patrick.sc.zeugs;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.enums.GebäudeEnum;
import de.hechler.patrick.sc.enums.GeländerEnum;
import de.hechler.patrick.sc.fehler.GebäudeBauException;
import de.hechler.patrick.sc.objects.GebäudeObj;
import de.hechler.patrick.sc.objects.Karte;
import de.hechler.patrick.sc.objects.Position;

public abstract class GebäudeFactory <G extends GebäudeObj> {
	
	/**
	 * saves the {@link GebäudeFactory} to create buildings from the {@link GeländerEnum}s.
	 */
	private static Map <GebäudeEnum, GebäudeFactory <?>> facs;
	
	/**
	 * resets the map {@link #facs} and may put some default {@link GebäudeFactory}s in it.
	 */
	public final static void load() {
		facs = new EnumMap <GebäudeEnum, GebäudeFactory <?>>(GebäudeEnum.class);
	}
	
	/**
	 * puts the @{@code art} and {@code fac} in the map {@link #facs} and returns the old {@link GebäudeFactory} for the enum {@code art}. 
	 * @param art the {@link GeländerEnum} of the buildings, to which the {@link GebäudeObj} belongs when they are created by the {@link GebäudeFactory} {@code fac}. 
	 * @param fac the {@link GebäudeFactory}, which will creates buildings fromm {@link GeländerEnum} {@code art}.
	 * @return the old mapping of {@code art}
	 */
	public final static GebäudeFactory <?> put(GebäudeEnum art, GebäudeFactory <?> fac) {
		if (art == null || fac == null) {
			throw new NullPointerException("(art|fac)=null art=" + art + " fac=" + fac);
		}
		return facs.put(art, fac);
	}
	
	
	
	/**
	 * Creates an {@code new} {@link GebäudeObj} and adds it automatically to the {@link Karte} map. <br>
	 * if that is not possible a {@link GebäudeBauException} will be thrown.
	 * 
	 * @param art
	 *            the {@link GebäudeEnum} of the new building. it will be used to find the right {@link GebäudeFactory} in {@link #facs}.
	 * @param karte
	 *            the map, on which the building will be added
	 * @param pos
	 *            the {@link Position} of the new building
	 * @return the created {@link GebäudeObj}
	 * @throws GebäudeBauException
	 *             if no new {@link GebäudeObj} can be created and added.
	 */
	public final static GebäudeObj create(GebäudeEnum art, Karte karte, Position pos) throws GebäudeBauException {
		GebäudeFactory <?> fac = facs.get(art);
		if (fac == null) {
			throw new GebäudeBauException("unknown art=" + art + " facs=" + facs);
		}
		return fac.create(karte, pos);
	}
	
	/**
	 * Creates an {@code new} {@link GebäudeObj} (of type {@code G}) and adds it automatically to the {@link Karte} map. <br>
	 * if that is not possible a {@link GebäudeBauException} will be thrown.
	 * 
	 * @param karte
	 *            the map, on which the building will be added
	 * @param pos
	 *            the {@link Position} of the new building
	 * @return the created {@link GebäudeObj} (of type {@code G})
	 * @throws GebäudeBauException
	 *             if no new {@link GebäudeObj} can be created and added.
	 */
	public abstract G create(Karte karte, Position pos) throws GebäudeBauException;
	
	/**
	 * Creates an {@code new} {@link GebäudeObj} (of type {@code G}). <br>
	 * if that is not possible a {@link GebäudeBauException} will be thrown.
	 * 
	 * @return the created {@link GebäudeObj} (of type {@code G})
	 * @throws GebäudeBauException
	 *             if no new {@link GebäudeObj} can be created and added.
	 */
	public abstract G create() throws GebäudeBauException;
	
	/**
	 * returns the {@link GeländerEnum}, which the buildings need to be created by this {@link GebäudeFactory}
	 * 
	 * @return the {@link GeländerEnum}, which the buildings need to be created by this {@link GebäudeFactory}
	 */
	public abstract Set <GeländerEnum>[][] platz();
	
}
