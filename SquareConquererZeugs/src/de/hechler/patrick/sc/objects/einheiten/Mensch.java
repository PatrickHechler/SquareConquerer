package de.hechler.patrick.sc.objects.einheiten;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import de.hechler.patrick.sc.enums.EinheitenEnum;
import de.hechler.patrick.sc.enums.GeländerEnum;
import de.hechler.patrick.sc.enums.Richtung;
import de.hechler.patrick.sc.fehler.UnbewegbarException;
import de.hechler.patrick.sc.fehler.UngültigeBewegungException;
import de.hechler.patrick.sc.objects.Einheit;
import de.hechler.patrick.sc.objects.Position;
import de.hechler.patrick.sc.zeugs.EinheitenFactory;

public class Mensch implements Einheit {
	
	private static final Object                OBJ = new Object();
	private static final Map <GeländerEnum, ?> BEWEGBAR;
	
	static {
		Map <GeländerEnum, Object> zw = new EnumMap <>(GeländerEnum.class);
		zw.put(GeländerEnum.eisenberg, OBJ);
		zw.put(GeländerEnum.goldberg, OBJ);
		zw.put(GeländerEnum.hügel, OBJ);
		zw.put(GeländerEnum.kohleberg, OBJ);
		zw.put(GeländerEnum.silberberg, OBJ);
		zw.put(GeländerEnum.steinberg, OBJ);
		zw.put(GeländerEnum.wald, OBJ);
		zw.put(GeländerEnum.wiese, OBJ);
		zw.put(GeländerEnum.wüste, OBJ);
		BEWEGBAR = Collections.unmodifiableMap(zw);
	}
	
	private final boolean    mann;
	private int              ges;
	private int              verb;
	private Position         pos;
	private Einheit          tragen;
	private PosChangeChecker pcl;
	private boolean          getragen;
	
	
	
	public Mensch(boolean mann, Position pos, PosChangeChecker pcl) {
		this.mann = mann;
		this.pos = pos;
		this.pcl = pcl;
		this.ges = 2;
		this.verb = 0;
		this.tragen = null;
		this.getragen = false;
	}
	
	public Mensch(boolean geschlecht, Position pos) {
		this(geschlecht, pos, null);
	}
	
	
	
	@Override
	public Position position() {
		return pos;
	}
	
	@Override
	public void position(Position neu) {
		pos = neu;
	}
	
	@Override
	public void springe(Position ziel) {
		pcl.posChanged(pos, ziel, tragen);
		if (tragen != null) {
			tragen.position(ziel);
		}
		pos = ziel;
	}
	
	@Override
	public int geschwindigkeit() {
		return ges;
	}
	
	@Override
	public int verbleibendeZüge() {
		if (tragen == null) return verb;
		else return verb >>> 1;
	}
	
	@Override
	public void bewege(Richtung richtung) throws UnbewegbarException, IllegalStateException, UngültigeBewegungException {
		if (verb - (tragen == null ? 1 : 2) < 0) {
			throw new IllegalStateException("kann nicht so weit laufen!");
		}
		Position dest = richtung.ziel(pos);
		pcl.posChanged(pos, dest, tragen);
		if (tragen != null) {
			verb -= 2;
			tragen.position(dest);
		} else {
			verb -= 1;
		}
		pos = dest;
	}
	
	@Override
	public void trage(Einheit einheit) throws UnsupportedOperationException, IllegalStateException {
		if (tragen != null || getragen) throw new IllegalStateException("trage bereits eine einheit: " + tragen);
		einheit.werdeGetragen();
		tragen = einheit;
	}
	
	@Override
	public void werdeGetragen() throws UnsupportedOperationException, IllegalStateException {
		if (getragen) throw new IllegalStateException("werde bereits getragen");
		getragen = true;
	}
	
	@Override
	public void werdeNichtGetragen() throws IllegalStateException {
		if ( !getragen) throw new IllegalStateException("werde gar nicht getragen!1");
		getragen = false;
	}
	
	@Override
	public Einheit trägt() {
		return tragen;
	}
	
	@Override
	public void stoppeTragen() throws IllegalStateException {
		if (tragen != null) throw new IllegalStateException("trage keine andere Einheit!");
		tragen.werdeNichtGetragen();
		tragen = null;
	}
	
	@Override
	public boolean neuerZug() {
		verb = ges;
		return false;
	}
	
	@Override
	public void positionChangeChecker(PosChangeChecker pcl) {
		this.pcl = pcl;
	}
	
	@Override
	public boolean kannAuf(GeländerEnum test) {
		return BEWEGBAR.containsKey(test);
	}
	
	@Override
	public EinheitenEnum spezies() {
		return mann ? EinheitenEnum.menschM : EinheitenEnum.menschW;
	}
	
	@Override
	public Integer entwickeltIn() {
		return null;
	}
	
	@Override
	public EinheitenEnum entwickeltZu() {
		return null;
	}
	
	@Override
	public void entwickele(Einheit ziel) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("bereits voll entwickelt");
	}
	
	@Override
	public EinheitenFactory <?> entwickleMit() {
		return null;
	}
	
}
