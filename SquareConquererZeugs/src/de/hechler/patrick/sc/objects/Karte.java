package de.hechler.patrick.sc.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.enums.GeländerEnum;
import de.hechler.patrick.sc.fehler.GebäudeBauException;
import de.hechler.patrick.sc.zeugs.GebäudeFactory;

public class Karte {
	
	/**
	 * Hier werden die {@link GebäudeObj} mit ihr Ur{@link Position} gespeichert, damit man einerseits über die {@link #felder} von {@link Position} zu {@link GebäudeObj} kann und mit dieser
	 * {@link Map} von {@link GebäudeObj} zu {@link Position}
	 */
	private final Map <GebäudeObj, Bounds> gebäude;
	/**
	 * Die einzelnen {@link Feld}er der {@link Karte} werden hier gespeichert.
	 */
	private final Feld[][]                 felder;
	
	
	
	/**
	 * Erstellt eine neue {@link Karte} mit übergebener {@code breite} und {@code höhe}.
	 * 
	 * @param breite
	 *            die breite der {@link Karte}
	 * @param höhe
	 *            die höhe der {@link Karte}
	 */
	public Karte(int breite, int höhe) {
		breite = Math.max(1, breite);
		höhe = Math.max(1, höhe);
		felder = new Feld[breite][höhe];
		gebäude = new HashMap <>();
		for (int i = 0; i < felder.length; i ++ ) {
			for (int ii = 0; ii < felder.length; ii ++ ) {
				felder[i][ii] = new Feld();
			}
		}
	}
	
	
	
	/**
	 * gibt <code>true</code> zurück, wenn es sich bei dieser Position um eine UrPosition handelt. wenn nicht, wird <code>false</code> zurückgegeben, wenn sich dort ein {@link GebäudeObj} befindet und
	 * <code>null</code> wenn sich dort kein {@link GebäudeObj} befindet.
	 * 
	 * @param x
	 *            die x Koordinate der Position
	 * @param y
	 *            die < Koordinate der Position
	 * @return <code>true</code>, wenn es sich bei dieser Position um eine UrPosition handelt. wenn nicht, wird <code>false</code> zurückgegeben, wenn sich dort ein {@link GebäudeObj} befindet und
	 *         <code>null</code> wenn sich dort kein {@link GebäudeObj} befindet.
	 * @see #urPos(Position)
	 */
	public Boolean urPos(int x, int y) {
		return felder[x][y].urposition ? true : felder[x][y].gebäude == null ? null : false;
	}
	
	/**
	 * gibt <code>true</code> zurück, wenn es sich bei dieser {@link Position} um eine UrPosition handelt. wenn nicht, wird <code>false</code> zurückgegeben, wenn sich dort ein {@link GebäudeObj}
	 * befindet und <code>null</code> wenn sich dort kein {@link GebäudeObj} befindet.
	 * 
	 * @param pos
	 *            die {@link Position} der potentiellen UrPosition
	 * @return <code>true</code>, wenn es sich bei dieser {@link Position} um eine UrPosition handelt. wenn nicht, wird <code>false</code> zurückgegeben, wenn sich dort ein {@link GebäudeObj} befindet
	 *         und <code>null</code> wenn sich dort kein {@link GebäudeObj} befindet.
	 */
	public Boolean urPos(Position pos) {
		return felder[pos.x][pos.y].urposition ? true : felder[pos.x][pos.y].gebäude == null ? null : false;
	}
	
	/**
	 * Gibt das {@link GebäudeObj}, welches sich zumindestens Teilweise bei dieser Position befindet zurück oder <code>null</code>, wenn dort kein {@link GebäudeObj} ist.
	 * 
	 * @param x
	 *            die x-Koordinate des {@link GebäudeObj}
	 * @param y
	 *            die y-Koordinate des {@link GebäudeObj}
	 * @return das {@link GebäudeObj}, welches sich zumindestens Teilweise bei dieser Position befindet oder <code>null</code>, wenn dort kein {@link GebäudeObj} ist.
	 * @see #gebäudeVon(Position)
	 */
	public GebäudeObj gebäudeVon(int x, int y) {
		return felder[x][y].gebäude;
	}
	
	/**
	 * Gibt das {@link GebäudeObj}, welches sich zumindestens Teilweise bei dieser {@link Position} befindet zurück oder <code>null</code>, wenn dort kein {@link GebäudeObj} ist.
	 * 
	 * @param pos
	 *            die {@link Position} des {@link GebäudeObj}
	 * @return das {@link GebäudeObj}, welches sich zumindestens Teilweise bei dieser {@link Position} befindet oder <code>null</code>, wenn dort kein {@link GebäudeObj} ist.
	 */
	public GebäudeObj gebäudeVon(Position pos) {
		return felder[pos.x][pos.y].gebäude;
	}
	
	/**
	 * Gibt das {@link GeländerEnum}, welches sich bei dieser Position befindet zurück.
	 * 
	 * @param x
	 *            die x-Koordinate des {@link GebäudeObj}
	 * @param y
	 *            die y-Koordinate des {@link GebäudeObj}
	 * @return das {@link GeländerEnum}, welches sich bei dieser Position befindet.
	 * @see #geländeVon(Position)
	 */
	public GeländerEnum geländeVon(int x, int y) {
		return felder[x][y].gelände;
	}
	
	/**
	 * Gibt das {@link GeländerEnum}, welches sich bei dieser {@link Position} befindet zurück.
	 * 
	 * @param pos
	 *            die {@link Position} des {@link GeländerEnum}s
	 * @return das {@link GeländerEnum}, welches sich bei dieser {@link Position} befindet.
	 */
	public GeländerEnum geländeVon(Position pos) {
		return felder[pos.x][pos.y].gelände;
	}
	
	/**
	 * Überschreibt das aktuelle {@link GeländerEnum} mit dem neuem {@link GeländerEnum} bei den x-y-Koordinaten.
	 * 
	 * @param x
	 *            die x-Koordinate
	 * @param y
	 *            die y-Koordinate
	 * @param neuesGelände
	 *            das neue {@link GeländerEnum}
	 * @see #setzteGelände(Position, GeländerEnum)
	 */
	public void setzteGelände(int x, int y, GeländerEnum neuesGelände) {
		felder[x][y].gelände = neuesGelände;
	}
	
	/**
	 * Überschreibt das aktuelle {@link GeländerEnum} mit dem neuem {@link GeländerEnum} bei der übergebenen {@link Position}
	 * 
	 * @param pos
	 *            die {@link Position} des zu ändernden {@link GeländerEnum}s
	 * @param neuesGelände
	 *            das neue {@link GeländerEnum}
	 */
	public void setzteGelände(Position pos, GeländerEnum neuesGelände) {
		felder[pos.x][pos.y].gelände = neuesGelände;
	}
	
	/**
	 * baut ein {@link GebäudeObj} bei der übergebenen {@link Position} und gibt dieses zurück.
	 * 
	 * @param <B>
	 *            die art der {@link GebäudeFactory}
	 * @param factory
	 *            die {@link GebäudeFactory}, welche zum kreieren des {@link GebäudeObj}s benutzt wird.
	 * @param pos
	 *            die UrPosition des Gebäudes
	 * @return das gebaute {@link GebäudeObj}
	 * @throws GebäudeBauException
	 *             wenn etwas schief läuft: ein Bereich, der zu groß für diese {@link Karte} ist, ein {@link GebäudeObj} auf dem zu bauendem Bereich oder ein unpassendes {@link GeländerEnum}.
	 */
	public <B extends GebäudeObj> B baue(GebäudeFactory <B> factory, final Position pos) throws GebäudeBauException {
		Set <GeländerEnum>[][] testen = factory.platz();
		if (pos.x + testen.length > felder.length) {
			throw new GebäudeBauException("out of map");
		}
		if (testen[0].length + pos.y > felder[0].length) {
			throw new GebäudeBauException("out of map");
		}
		for (int i = 0; i < testen.length; i ++ ) {
			if (testen[i].length != testen[i].length) {
				throw new GebäudeBauException("unknown formation");
			}
			for (int ii = 0; ii < testen.length; ii ++ ) {
				if (felder[i][ii].gebäude != null) {
					throw new GebäudeBauException("already built on area(FELD[" + i + "][" + ii + "])");
				}
				if ( !testen[i][ii].contains(felder[i][ii].gelände)) {
					throw new GebäudeBauException("wront terrain: accept:" + testen[i][ii] + " found:" + felder[i][ii].gelände);
				}
			}
		}
		B geb = factory.create();
		felder[pos.x][pos.y].urposition = true;
		gebäude.put(geb, new Bounds(pos, testen.length, testen[0].length));
		for (int i = 0; i < testen.length; i ++ ) {
			for (int ii = 0; ii < testen.length; ii ++ ) {
				felder[i][ii].gebäude = geb;
			}
		}
		return geb;
	}
	
	public <B extends GebäudeObj> B baue(GebäudeFactory <B> factory, final int x, final int y) throws GebäudeBauException {
		return baue(factory, new Position(x, y));
	}
	
	public int maximaleEinheitenAnzahl(Position pos) throws IndexOutOfBoundsException {
		if (pos.x >= felder.length || pos.y >= felder[0].length) throw new IndexOutOfBoundsException("pos=" + pos + " len=(" + felder.length + "|" + felder[0].length + ")");
		return 1;
	}
	
	/**
	 * Gibt <code>true</code> zurück, wenn das {@link GebäudeObj} vorher in der {@link Karte} vorhanden war. Wenn es nicht vorhanden war wird <code>false</code> zurückgegeben. Egal was danach ist es
	 * nicht mehr in der {@link Karte}.
	 * 
	 * @param gebäude
	 *            das zu vernichtende {@link GebäudeObj}
	 * @return <code>true</code>, wenn das {@link GebäudeObj} vorher in der {@link Karte} vorhanden war. Wenn es nicht vorhanden wird <code>false</code>.
	 */
	public boolean vernichteGebäude(GebäudeObj gebäude) {
		if ( !this.gebäude.containsKey(gebäude)) {
			return false;
		}
		Bounds bounds = this.gebäude.remove(gebäude);
		assert felder[bounds.x][bounds.y].urposition : "no urPos on the urPos: " + bounds;
		felder[bounds.x][bounds.y].urposition = false;
		for (int i = 0; i < bounds.breiteX; i ++ ) {
			for (int ii = 0; ii < bounds.höheY; ii ++ ) {
				assert felder[bounds.x + i][bounds.y + ii].gebäude == gebäude : "wrong remove";
				felder[bounds.x + i][bounds.y + ii].gebäude = null;
			}
		}
		return true;
	}
	
	private class Feld {
		
		private GebäudeObj   gebäude;
		private boolean      urposition;
		private GeländerEnum gelände;
		
	}
	
}
