package de.hechler.patrick.sc.objects;


public class Bounds extends Position {
	
	public final int breiteX;
	public final int höheY;
	
	public Bounds(int x, int y, int breite_x, int länge_y) {
		super(x, y);
		this.breiteX = breite_x;
		this.höheY = länge_y;
	}
	
	public Bounds(Position pos, int breite_x, int länge_y) {
		super(pos.x, pos.y);
		this.breiteX = breite_x;
		this.höheY = länge_y;
	}
	
	@Override
	public String toString() {
		return "(" + x + "->" + breiteX + "|" + y + "->" + höheY + ")";
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() ^ breiteX ^ höheY;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( !super.equals(obj)) {
			return false;
		}
		return ((Bounds) obj).breiteX == breiteX && ((Bounds) obj).höheY == höheY;
	}
	
}
