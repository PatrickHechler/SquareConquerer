package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;

public class AbsoluteManipulablePosition implements Position {
	
	public int x;
	public int y;
	
	
	
	public AbsoluteManipulablePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public AbsoluteManipulablePosition(Position pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}
	
	
	
	@Override
	public int getX() {
		return x;
	}
	
	/**
	 * There is no real need for this method, because {@link #x} is {@code public}
	 * 
	 * @param x
	 *            the new value of {@link #x}
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	/**
	 * There is no real need for this method, because {@link #y} is {@code public}
	 * 
	 * @param y
	 *            the new value of {@link #y}
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public void move(Direction dir) throws UnsupportedOperationException {
		switch (dir) {
		case up:
			y ++ ;
			break;
		case down:
			y -- ;
			break;
		case rigth:
			x ++ ;
			break;
		case left:
			x -- ;
			break;
		default:
			throw new RuntimeException("unknown direction: " + dir);
		}
	}
	
	@Override
	public AbsoluteManipulablePosition newCreateMove(Direction dir) {
		AbsoluteManipulablePosition acp = clone();
		acp.move(dir);
		return acp;
	}
	
	@Override
	public AbsoluteManipulablePosition clone() {
		try {
			return (AbsoluteManipulablePosition) super.clone();
		} catch (CloneNotSupportedException e) {
			return new AbsoluteManipulablePosition(x, y);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass()) return equals((Position) obj);
		else return false;
	}
	
	@Override
	public boolean equals(Position pos) {
		return x == pos.getX() && y == pos.getY();
	}
	
}
