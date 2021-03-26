package de.hechler.patrick.sc.objects;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.hechler.patrick.sc.interfaces.Position;

public class Rightangle extends AbsoluteManipulablePosition implements Set <Position> {
	
	private int xCnt;
	private int yCnt;
	
	
	
	public Rightangle(int x, int y, int xCnt, int yCnt) {
		super(x, y);
		this.xCnt = xCnt;
		this.yCnt = yCnt;
	}
	
	public Rightangle() {
		super(0, 0);
		xCnt = yCnt = 0;
	}
	
	
	
	public int getxCnt() {
		return xCnt;
	}
	
	public void setxCnt(int xCnt) {
		this.xCnt = xCnt;
	}
	
	public int getyCnt() {
		return yCnt;
	}
	
	public void setyCnt(int yCnt) {
		this.yCnt = yCnt;
	}
	
	@Override
	public int size() {
		return xCnt * yCnt;
	}
	
	@Override
	public boolean isEmpty() {
		return xCnt == 0 && yCnt == 0;
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Position) {
			Position pos = (Position) o;
			final int x = pos.getX(), y = pos.getY();
			if (x < super.getX()) return false;
			if (y < super.getY()) return false;
			if (x >= super.getX() + this.xCnt) return false;
			if (y >= super.getY() + this.yCnt) return false;
			return true;
		}
		return false;
	}
	
	public boolean contains(Position pos) {
		final int x = pos.getX(), y = pos.getY();
		if (x < super.getX()) return false;
		if (y < super.getY()) return false;
		if (x >= super.getX() + this.xCnt) return false;
		if (y >= super.getY() + this.yCnt) return false;
		return true;
	}
	
	@Override
	public Iterator <Position> iterator() {
		return new Iterator <Position>() {
			
			int x = 0;
			int y = 0;
			
			@Override
			public boolean hasNext() {
				return x >= Rightangle.this.xCnt && y >= Rightangle.this.yCnt;
			}
			
			@Override
			public Position next() {
				if (y >= Rightangle.this.yCnt) {
					y = 0;
					x ++ ;
					if (x >= Rightangle.this.xCnt) {
						throw new NoSuchElementException("all iterated!");
					}
				}
				return new UnchangeablePosition(x + Rightangle.super.getX(), y ++ + Rightangle.super.getY());
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] res = new Object[yCnt * xCnt];
		for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
			for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
				res[i] = new UnchangeablePosition(super.getX() + xx, super.getY() + yy);
			}
		}
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int len = xCnt * yCnt;
		Class <?> comp = a.getClass().getComponentType();
		if (a.length != len) {
			a = (T[]) Array.newInstance(comp, len);
		}
		comp.asSubclass(Position.class);
		if (comp == Position.class) {
			Position[] arr = (Position[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new NicePosition(super.getX() + xx, super.getY() + yy);// Does not really matter what for an Position-Impl
				}
			}
		} else if (NicePosition.class == comp) {
			NicePosition[] arr = (NicePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new NicePosition(super.getX() + xx, super.getY() + yy);
				}
			}
		} else if (UnchangeablePosition.class == comp) {
			UnchangeablePosition[] arr = (UnchangeablePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new UnchangeablePosition(super.getX() + xx, super.getY() + yy);
				}
			}
		} else if (AbsoluteManipulablePosition.class == comp) {
			AbsoluteManipulablePosition[] arr = (AbsoluteManipulablePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new AbsoluteManipulablePosition(super.getX() + xx, super.getY() + yy);
				}
			}
		} else if (PositionListener.class == comp) {
			PositionListener[] arr = (PositionListener[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new PositionListener(new UnchangeablePosition(super.getX() + xx, super.getY() + yy));
				}
			}
		} else if (AbsoluteMegaManipulablePosition.class == comp) {
			AbsoluteMegaManipulablePosition[] arr = (AbsoluteMegaManipulablePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new AbsoluteMegaManipulablePosition(super.getX() + xx, super.getY() + yy);
				}
			}
		} else {
			throw new RuntimeException("unknown componentType: '" + comp.getName() + "' of the array: " + Arrays.deepToString(a));
		}
		return a;
	}
	
	@Override
	public boolean add(Position e) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			if (super.getX() > r.getX()) return false;
			if (super.getX() > r.getX()) return false;
			int xOff = super.getX() - r.getX(), yOff = super.getX() - r.getX();
			int xNeedCount = xOff + r.xCnt, yNeedCount = yOff + r.yCnt;
			if (xNeedCount > xCnt) return false;
			if (yNeedCount > yCnt) return false;
			return true;
		} else {
			for (Object check : c) {
				if ( !contains(check)) return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean addAll(Collection <? extends Position> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear() {
		super.setX(0);
		super.setY(0);
		yCnt = xCnt = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) return super.equals((Position) obj);
		return false;
	}
	
	public boolean equals(Rightangle r) {
		return super.equals(r) && r.xCnt == xCnt && r.yCnt == yCnt;
	}
	
	// EDGES:
	
	public Position xMinYMax() {
		return new UnchangeablePosition(super.getX(), super.getY() + yCnt - 1);
	}
	
	public Position xMaxYMin() {
		return new UnchangeablePosition(super.getX() + xCnt - 1, super.getY());
	}
	
	public Position xMaxYMax() {
		return new UnchangeablePosition(super.getX() + xCnt - 1, super.getY() + yCnt - 1);
	}
	
	@Override
	public Rightangle clone() {
		try {
			return (Rightangle) super.clone();
		} catch (ClassCastException e) {
			return new Rightangle(super.getX(), super.getY(), xCnt, yCnt);
		}
	}
	
	@Override
	public int hashCode() {
		return (27 * xCnt) ^ super.hashCode() ^ (53 * yCnt);
	}
	
}
