package de.hechler.patrick.sc.objects;


public class Position {
	
	public final int x;
	public final int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	
	@Override
	public String toString() {
		return "(" + x + "|" + y + ")";
	}
	
	@Override
	public int hashCode() {
		return x ^ y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Position o = (Position) obj;
		if (x != o.x) return false;
		if (y != o.y) return false;
		return true;
	}
	
}
