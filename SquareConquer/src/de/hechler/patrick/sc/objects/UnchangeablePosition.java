package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;


public class UnchangeablePosition implements Position {
	
	public final int x;
	public final int y;
	
	
	
	public UnchangeablePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public UnchangeablePosition(Position pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}
	
	
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	@Override
	public void move(Direction dir) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public UnchangeablePosition newCreateMove(Direction dir) {
		switch (dir) {
		case up:
			return new UnchangeablePosition(x, y + 1);
		case down:
			return new UnchangeablePosition(x, y - 1);
		case rigth:
			return new UnchangeablePosition(x + 1, y);
		case left:
			return new UnchangeablePosition(x - 1, y);
		}
		throw new RuntimeException("unknown direction: name=" + dir.name() + " toString() -> '" + dir.toString() + "'");
	}
	
	@Override
	public UnchangeablePosition clone() {
		try {
			return (UnchangeablePosition) super.clone();
		} catch (CloneNotSupportedException e) {
			return new UnchangeablePosition(x, y);
		}
	}
	
	@Override
	public int hashCode() {
		int xP = this.x * 79;
		int yP = this.y * 97;
		return xP ^ yP;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		UnchangeablePosition other = (UnchangeablePosition) obj;
		if (x != other.x) return false;
		if (y != other.y) return false;
		return true;
	}
	
}
