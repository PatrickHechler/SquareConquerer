package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.EinheitenEnum;
import de.hechler.patrick.sc.enums.GeländerEnum;
import de.hechler.patrick.sc.enums.Richtung;
import de.hechler.patrick.sc.fehler.UnbewegbarException;
import de.hechler.patrick.sc.fehler.UngültigeBewegungException;
import de.hechler.patrick.sc.zeugs.EinheitenFactory;

public interface Einheit {
	
	/**
	 * Gibt die aktuelle {@link Position} der {@link Einheit} zurück.
	 * 
	 * @return die aktuelle {@link Position} der {@link Einheit}
	 */
	Position position();
	
	/**
	 * Setzt die {@link Position} der {@link Einheit} auf die neue {@link Position}<br>
	 * 
	 * Diese Methode ist für getragene {@link Einheit}en, da hierbei der {@link PosChangeChecker} nicht benachrichtigt wird.
	 * 
	 * @throws NullPointerException
	 *             wenn {@link Position} neu <code>null</code> ist
	 * @param neu
	 *            die neue {@link Position} der {@link Einheit}
	 */
	void position(Position neu);
	
	/**
	 * springt zu der übergebenen {@link Position}. <br>
	 * Das springen zählt nicht als Bewegung, daher wird kein Bewegungspunkt abgezogen, allerdings wird der {@link PosChangeChecker} aufgerufen.
	 * 
	 * @param ziel
	 *            die Ziel-{@link Position}
	 */
	void springe(Position ziel);
	
	/**
	 * Gibt die Anzahl an Bewegungen zurück, welche die {@link Einheit} am Anfang nächsten Zuges voraussichtlich haben wird.
	 * 
	 * @return die Anzahl an Bewegungen, welche die {@link Einheit} am Anfang nächsten Zuges voraussichtlich haben wird.
	 */
	int geschwindigkeit();
	
	/**
	 * Gibt die anzahl an verbleibenden Bewegungen zurück, welche die {@link Einheit} in diesem Zug noch durchführen kann. wenn diese {@link Einheit} gerade {@link #arbeitet()} oder eine
	 * {@link Einheit} {@link #trägt()}, wird dies hier ignoriert.
	 * 
	 * @return die anzahl an verbleibenden Bewegungen, welche die {@link Einheit} in diesem Zug noch durchführen kann.
	 */
	int verbleibendeZüge();
	
	/**
	 * bewegt die Einheit ein Feld in die übergebene {@link Richtung}
	 * 
	 * @param richtung
	 *            die {@link Richtung} in die sich die {@link Einheit} bewegen soll.
	 * @throws UnbewegbarException
	 *             wenn die {@link Einheit} sich generell nicht bewegen kann
	 * @throws IllegalStateException
	 *             wenn die {@link Einheit} sich aktuell nicht bewegen kann, da Beispielsweise all ihre Bewegungen für diesen Zug aufgebraucht sind oder sie getragen wird.
	 */
	void bewege(Richtung richtung) throws UnbewegbarException, IllegalStateException, UngültigeBewegungException;
	
	/**
	 * Trägt von nun an die andere {@link Einheit}, bis {@link #stoppeTragen()} aufgerufen wird. Bewegungsaktionen werden nun möglicherweise mehr Bewegungspunkte kosten, dies wird allerdings bei
	 * {@link #verbleibendeZüge()} mitgerechnet.
	 * 
	 * @param einheit
	 *            die zu tragende {@link Einheit}
	 * @throws UnsupportedOperationException
	 *             wenn diese {@link Einheit} keine anderen {@link Einheit}en tragen kann oder die andere {@link Einheit} sich nicht trage lässt.
	 * @throws IllegalStateException
	 *             Wenn diese {@link Einheit} bereits eine {@link Einheit} trägt
	 */
	void trage(Einheit einheit) throws UnsupportedOperationException, IllegalStateException;
	
	/**
	 * signalisiert dieser {@link Einheit}, dass sie getragen wird, d.h. sie kann sich nicht länger von alleine fortbewegen.
	 * 
	 * @throws UnsupportedOperationException
	 *             wenn diese {@link Einheit} sich nicht tragen lässt
	 * @throws IllegalStateException
	 *             wenn diese {@link Einheit} bereits getragen wird
	 */
	void werdeGetragen() throws UnsupportedOperationException, IllegalStateException;
	
	/**
	 * signalisiert dieser {@link Einheit}, dass sie nicht länger getragen wird, d.h. sie kann sich wieder von alleine fortbewegen.
	 * 
	 * @throws IllegalStateException
	 *             wenn diese {@link Einheit} gar nicht getragen wird
	 */
	void werdeNichtGetragen() throws IllegalStateException;
	
	/**
	 * Gibt die {@link Einheit} zurück, welche von dieser {@link Einheit} getragen wird. <code>null</code> wenn diese {@link Einheit} keine andere {@link Einheit} trägt.
	 * 
	 * @return die {@link Einheit}, welche von dieser {@link Einheit} getragen wird. <code>null</code> wenn diese {@link Einheit} keine andere {@link Einheit} trägt.
	 */
	Einheit trägt();
	
	/**
	 * Hört auf die andere {@link Einheit} zu tragen.
	 * 
	 * @throws IllegalStateException
	 *             wenn die {@link Einheit} keine andere {@link Einheit} trägt
	 */
	void stoppeTragen() throws IllegalStateException;
	
	/**
	 * Befiehlt der {@link Einheit} bei der aktuellen {@link Position} auf der {@link Karte} zu arbeiten. <br>
	 * Eine {@link Einheit}, die arbeitet kann sich nicht bewegen, bis sie aufhört zu arbeiten ({@link #stoppeArbeiten()}) <br>
	 * Wenn sich dort kein Gebäude befindet oder keines, bei dem diese {@link Einheit} arbeiten kann wird eine {@link UnsupportedOperationException} geworfen.
	 * 
	 * @param karte
	 *            die {@link Karte}
	 * @throws UnsupportedOperationException
	 *             Wenn sich dort kein Gebäude befindet oder keines, bei dem diese {@link Einheit} arbeiten kann
	 * @throws IllegalStateException
	 *             wenn die {@link Einheit} bereits arbeitet.
	 */
	void arbeite(Karte karte) throws UnsupportedOperationException, IllegalStateException;
	
	/**
	 * gibt <code>true</code> zurück, wenn die {@link Einheit} arbeitet, wenn nicht <code>false</code>.
	 * 
	 * @return <code>true</code>, wenn die {@link Einheit} arbeitet, wenn nicht <code>false</code>.
	 */
	boolean arbeitet();
	
	/**
	 * Signalisiert der {@link Einheit} mit dem arbeiten aufzuhören.<br>
	 * Wenn die {@link Einheit} gar nicht gearbeitet hat wird eine {@link IllegalStateException} geworfen.
	 * 
	 * @throws IllegalStateException
	 *             Wenn die {@link Einheit} gar nicht gearbeitet hat
	 */
	void stoppeArbeiten() throws IllegalStateException;
	
	/**
	 * signalisiert der {@link Einheit} dass ein neuer Zug begonnen hat.<br>
	 * gibt <code>true</code> zurück, wenn sich die {@link Einheit} entwickeln möchte.
	 * 
	 * @return <code>true</code>, wenn sich die {@link Einheit} entwickeln möchte.
	 */
	boolean neuerZug();
	
	void positionChangeChecker(PosChangeChecker pcl);
	
	public interface PosChangeChecker {
		
		void posChanged(Position old, Position neu, Einheit getragen) throws UngültigeBewegungException;
		
	}
	
	/**
	 * prüft, ob diese Einheit sich auf dieses {@link GeländerEnum} bewegen kann oder nicht.
	 * 
	 * @param test
	 *            das zu testende {@link GeländerEnum}
	 * @return ob sich die {@link Einheit} zu diesem {@link GeländerEnum} bewegen kann.
	 */
	boolean kannAuf(GeländerEnum test);
	
	/**
	 * Gibt die Spezies/das {@link EinheitenEnum} dieser {@link Einheit} zurück.
	 * 
	 * @return Die Spezies/das {@link EinheitenEnum} dieser {@link Einheit}
	 */
	EinheitenEnum spezies();
	
	Integer entwickeltIn();
	
	EinheitenEnum entwickeltZu();
	
	void entwickele(Einheit ziel) throws UnsupportedOperationException;
	
	EinheitenFactory <?> entwickleMit();
	
}
