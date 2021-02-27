package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Richtung;
import de.hechler.patrick.sc.fehler.UnbewegbarException;

public interface Einheit {
	
	/**
	 * Gibt die aktuelle {@link Position} der {@link Einheit} zurück.
	 * 
	 * @return die aktuelle {@link Position} der {@link Einheit}
	 */
	Position poition();
	
	/**
	 * Setzt die {@link Position} der {@link Einheit} auf die neue {@link Position}
	 * 
	 * @throws NullPointerException
	 *             wenn {@link Position} neu <code>null</code> ist
	 * @param neu
	 *            die neue {@link Position} der {@link Einheit}
	 */
	void position(Position neu);
	
	/**
	 * Gibt die Anzahl an Bewegungen zurück, welche die {@link Einheit} am Anfang nächsten Zuges voraussichtlich haben wird.
	 * 
	 * @return die Anzahl an Bewegungen, welche die {@link Einheit} am Anfang nächsten Zuges voraussichtlich haben wird.
	 */
	int geschwindigkeit();
	
	/**
	 * Gibt die anzahl an verbleibenden Bewegungen zurück, welche die {@link Einheit} in diesem Zug noch durchführen kann.
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
	void bewege(Richtung richtung) throws UnbewegbarException, IllegalStateException;
	
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
	 */
	void werdeGetragen() throws UnsupportedOperationException;
	
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
	 */
	void stoppeTragen();
	
	/**
	 * signalisiert der {@link Einheit} dass ein neuer Zug begonnen hat.
	 */
	void neuerZug();
	
	
	void positionChangeChecker(PosChangeChecker pcl);
	
	public interface PosChangeChecker {
		
		boolean posChanged(Position old, Position neu);
		
	}
	
}
