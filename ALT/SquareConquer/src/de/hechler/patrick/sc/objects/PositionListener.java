package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;

public class PositionListener implements Position {
	
	private Position delegate;
	
	
	
	public PositionListener(Position pos) {
		this.delegate = pos;
	}
	
	
	
	@Override
	public int getX() {
		return delegate.getX();
	}
	
	@Override
	public int getY() {
		return delegate.getY();
	}
	
	@Override
	public int compareTo(Position o) {
		return delegate.compareTo(o);
	}
	
	@Override
	public int distance(Position to) {
		return delegate.distance(to);
	}
	
	@Override
	public void move(Direction dir) throws UnsupportedOperationException {
		delegate.move(dir);
	}
	
	@Override
	public Position newCreateMove(Direction dir) {
		return delegate.newCreateMove(dir);
	}
	
	@Override
	public PositionListener clone() {
		try {
			return (PositionListener) super.clone();
		} catch (CloneNotSupportedException e) {
			return new PositionListener(delegate);
		}
	}
	
}
