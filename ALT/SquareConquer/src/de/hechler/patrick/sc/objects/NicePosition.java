package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;


public class NicePosition implements Position {
	
	private int x;
	private int y;
	
	
	
	public NicePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public NicePosition(Position pos) {
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
	public NicePosition newCreateMove(Direction dir) {
		NicePosition np = clone();
		np.move(dir);
		return np;
	}
	
	@Override
	public NicePosition clone() {
		try {
			return (NicePosition) super.clone();
		} catch (CloneNotSupportedException e) {
			return new NicePosition(x, y);
		}
	}
	
}
