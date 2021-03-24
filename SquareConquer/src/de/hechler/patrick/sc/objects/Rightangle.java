package de.hechler.patrick.sc.objects;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.hechler.patrick.sc.interfaces.Position;

public class Rightangle extends AbsoluteManipulablePosition implements Set <Position> {
	
	public int xCnt;
	public int yCnt;
	
	
	
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
			if (x < this.x) return false;
			if (y < this.y) return false;
			if (x >= this.x + this.xCnt) return false;
			if (y >= this.y + this.yCnt) return false;
			return true;
		}
		return false;
	}
	
	public boolean contains(Position pos) {
		final int x = pos.getX(), y = pos.getY();
		if (x < this.x) return false;
		if (y < this.y) return false;
		if (x >= this.x + this.xCnt) return false;
		if (y >= this.y + this.yCnt) return false;
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
				return new UnchangeablePosition(x + Rightangle.this.x, y ++ + Rightangle.this.y);
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] res = new Object[yCnt * xCnt];
		for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
			for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
				res[i] = new UnchangeablePosition(this.x + xx, this.y + yy);
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
					arr[i] = new NicePosition(this.x + xx, this.y + yy);// Does not really matter what for an Position-Impl
				}
			}
		} else if (NicePosition.class.isAssignableFrom(comp)) {
			NicePosition[] arr = (NicePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new NicePosition(this.x + xx, this.y + yy);
				}
			}
		} else if (UnchangeablePosition.class.isAssignableFrom(comp)) {
			UnchangeablePosition[] arr = (UnchangeablePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new UnchangeablePosition(this.x + xx, this.y + yy);
				}
			}
		} else if (AbsoluteManipulablePosition.class.isAssignableFrom(comp)) {
			AbsoluteManipulablePosition[] arr = (AbsoluteManipulablePosition[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new AbsoluteManipulablePosition(this.x + xx, this.y + yy);
				}
			}
		} else if (PositionListener.class.isAssignableFrom(comp)) {
			PositionListener[] arr = (PositionListener[]) a;
			for (int xx = 0, i = 0; xx < xCnt; xx ++ ) {
				for (int yy = 0; yy < xCnt; yy ++ , i ++ ) {
					arr[i] = new PositionListener(new UnchangeablePosition(this.x + xx, this.y + yy));
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
			if (x > r.x) return false;
			if (y > r.y) return false;
			int xOff = x - r.x, yOff = y - r.y;
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
		x = y = yCnt = xCnt = 0;
	}
	
	public Position maxPosition() {
		return new UnchangeablePosition(x + xCnt - 1, y + yCnt - 1);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) return super.equals((Position) obj);
		return false;
	}
	
	public boolean equals(Rightangle r) {
		return super.equals(r) && r.xCnt == xCnt && r.yCnt == yCnt;
	}
	
}
