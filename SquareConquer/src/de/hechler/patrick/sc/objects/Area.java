package de.hechler.patrick.sc.objects;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;

public class Area extends Rightangle {
	
	private boolean[][] inside;
	
	
	
	public Area(int x, int y, int xCnt, int yCnt) {
		super(x, y, xCnt, yCnt);
		inside = new boolean[xCnt][yCnt];
	}
	
	public Area() {
		this(0, 0, 0, 0);
	}
	
	
	
	@Override
	public void setxCnt(int xCnt) {
		int i = inside.length;
		inside = Arrays.copyOf(inside, xCnt);
		if (i < xCnt) {
			int yc = super.getY();
			for (; i < inside.length; i ++ ) {
				inside[i] = new boolean[yc];
			}
		}
		super.setxCnt(xCnt);
	}
	
	@Override
	public void setyCnt(int yCnt) {
		for (int i = 0; i < inside.length; i ++ ) {
			inside[i] = Arrays.copyOf(inside[i], yCnt);
		}
		super.setyCnt(yCnt);
	}
	
	@Override
	public int size() {
		int s = 0;
		for (boolean[] bs : inside) {
			for (boolean b : bs) {
				if (b) s ++ ;
			}
		}
		return s;
	}
	
	@Override
	public boolean isEmpty() {
		for (boolean[] bs : inside) {
			for (boolean b : bs) {
				if (b) return false;
			}
		}
		return true;
	}
	
	@Override
	public Iterator <Position> iterator() {
		return new Iterator <Position>() {
			
			int x = 0, y = 0;
			
			@Override
			public boolean hasNext() {
				for (; x < inside.length; x ++ ) {
					for (; y < inside[x].length; y ++ ) {
						if (inside[x][y]) return true;
					}
					y = 0;
				}
				return false;
			}
			
			@Override
			public UnchangeablePosition next() {
				for (; x < inside.length; x ++ ) {
					for (; y < inside[x].length; y ++ ) {
						if (inside[x][y]) return new UnchangeablePosition(x, y ++ );
					}
					y = 0;
				}
				throw new NoSuchElementException("finish!");
			}
			
		};
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Position) return contains((Position) o);
		else return false;
	}
	
	@Override
	public boolean contains(Position pos) {
		int x = pos.getX(), y = pos.getY();
		x -= super.getX();
		y -= super.getY();
		if (x >= super.getxCnt() || y >= super.getyCnt()) return false;
		return inside[x][y];
	}
	
	@Override
	public boolean add(Position e) {
		if ( !super.contains(e)) throw new IllegalArgumentException("this position ['" + e + "'] is not inside of the boarders!");
		int x = e.getX(), y = e.getY();
		x -= super.getX();
		y -= super.getY();
		boolean old = inside[x][y];
		inside[x][y] = true;
		return old;
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof Area) return containsAll((Area) c);
		boolean res = false;
		for (Object pos : c) {
			res |= contains(pos);
		}
		return res;
	}
	
	public boolean containsAll(Area a) {
		final int xoff = super.getX() - a.getX(), yoff = super.getY() - a.getY(), negxoff = -xoff, negyoff = -xoff;
		for (int x = negxoff, xsum = x + xoff; x < inside.length && xsum < a.inside.length; xsum = ++ x + xoff) {
			for (int y = negyoff, ysum = y + yoff; y < inside[x].length && ysum < a.inside[xsum].length; ysum = ++ y + yoff) {
				if ( !a.inside[xsum][ysum]) continue;
				if ( !inside[x][y]) return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection <? extends Position> c) {
		if (c instanceof Area) return addAll((Area) c);
		boolean res = false;
		for (Position pos : c) {
			res |= add(pos);
		}
		return res;
	}
	
	public boolean addAll(Area a) {
		boolean res = false;
		final int xoff = super.getX() - a.getX(), yoff = super.getY() - a.getY(), negxoff = -xoff, negyoff = -xoff;
		for (int x = negxoff, xsum = x + xoff; x < inside.length && xsum < a.inside.length; xsum = ++ x + xoff) {
			for (int y = negyoff, ysum = y + yoff; y < inside[x].length && ysum < a.inside[xsum].length; ysum = ++ y + yoff) {
				boolean old = inside[x][y];
				inside[x][y] |= a.inside[xsum][ysum];
				res |= old != inside[x][y];
			}
		}
		return res;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Position) return remove((Position) o);
		return false;
	}
	
	public boolean remove(Position pos) {
		if ( !super.contains(pos)) return false;
		int x = pos.getX(), y = pos.getY();
		x -= super.getX();
		y -= super.getY();
		if ( !inside[x][y]) return false;
		inside[x][y] = false;
		return false;
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		boolean res = false;
		for (Object obj : c) {
			if ( ! (obj instanceof Position)) continue;
			Position pos = (Position) obj;
			if ( !super.contains(pos)) continue;
			int x = pos.getX(), y = pos.getY();
			x -= super.getX();
			y -= super.getY();
			if ( !inside[x][y]) continue;
			inside[x][y] = false;
			res = true;
		}
		return res;
	}
	
	public boolean removeAll(Area a) {
		boolean res = false;
		for (int x = 0; x < inside.length; x ++ ) {
			for (int y = 0; y < inside[x].length; y ++ ) {
				if ( !inside[x][y]) continue;
				if ( !a.inside[x][y]) continue;
				inside[x][y] = false;
				res = true;
			}
		}
		return res;
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c instanceof Area) return retainAll((Area) c);
		boolean res = false;
		int xx = super.getX(), yy = super.getY();
		for (int x = 0; x < inside.length; x ++ ) {
			for (int y = 0; y < inside[x].length; y ++ ) {
				if ( !inside[x][y]) continue;
				if (c.contains(new NicePosition(x + xx, y + yy))) continue;
				inside[x][y] = false;
				res = true;
			}
		}
		return res;
	}
	
	public boolean retainAll(Area a) {
		boolean res = false;
		for (int x = 0; x < inside.length; x ++ ) {
			for (int y = 0; y < inside[x].length; y ++ ) {
				boolean old = inside[x][y];
				inside[x][y] &= a.inside[x][y];
				res = res || (old != inside[x][y]);
			}
		}
		return res;
	}
	
	@Override
	public Object[] toArray() {
		Object[] objs = new Object[size()];
		int i = 0;
		for (Position obj : this) {
			objs[i ++ ] = obj;
		}
		return objs;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int len = size();
		Class <?> comp = a.getClass().getComponentType();
		if (a.length != len) {
			a = (T[]) Array.newInstance(comp, len);
		}
		comp.asSubclass(Position.class);
		if (comp == Position.class) {
			Position[] arr = (Position[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = pos;
			}
		} else if (NicePosition.class == comp) {
			NicePosition[] arr = (NicePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new NicePosition(pos);
			}
		} else if (UnchangeablePosition.class == comp) {
			UnchangeablePosition[] arr = (UnchangeablePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new UnchangeablePosition(pos);
			}
		} else if (AbsoluteManipulablePosition.class == comp) {
			AbsoluteManipulablePosition[] arr = (AbsoluteManipulablePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new AbsoluteManipulablePosition(pos);
			}
		} else if (PositionListener.class == comp) {
			PositionListener[] arr = (PositionListener[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new PositionListener(pos);
			}
		} else if (AbsoluteMegaManipulablePosition.class == comp) {
			AbsoluteMegaManipulablePosition[] arr = (AbsoluteMegaManipulablePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new AbsoluteMegaManipulablePosition(pos);
			}
		} else {
			throw new RuntimeException("unknown componentType: '" + comp.getName() + "' of the array: " + Arrays.deepToString(a));
		}
		return a;
	}
	
	@Override
	public Area newCreateMove(Direction dir) {
		try {
			return (Area) super.newCreateMove(dir);
		} catch (ClassCastException e) {
			Area a = clone();
			a.move(dir);
			return a;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Area) {
			return equals((Area) obj);
		}
		return super.equals(obj);
	}
	
	public boolean equals(Area a) {
		if ( !super.equals((Rightangle) a)) return false;
		for (int i = 0; i < inside.length; i ++ ) {
			for (int ii = 0; ii < inside[i].length; ii ++ ) {
				if (inside[i][ii] != a.inside[i][ii]) return false;
			}
		}
		return true;
	}
	
	@Override
	public void clear() {
		inside = new boolean[0][0];
		super.clear();
	}
	
	@Override
	public Area clone() {
		Area a = (Area) super.clone();
		a.inside = new boolean[inside.length][];
		for (int i = 0; i < inside.length; i ++ ) {
			a.inside[i] = inside[i].clone();
		}
		return a;
	}
	
	@Override
	public int hashCode() {
		int res = super.hashCode();
		for (int x = 0; x < inside.length; x ++ ) {
			for (int y = 0; y < inside.length; y ++ ) {
				if (inside[x][y]) res ^= 31 * x ^ 13 * y;
			}
		}
		return res;
	}
	
}


