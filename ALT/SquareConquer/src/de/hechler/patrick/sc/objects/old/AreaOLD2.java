package de.hechler.patrick.sc.objects.old;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.objects.AbsoluteManipulablePosition;
import de.hechler.patrick.sc.objects.NicePosition;
import de.hechler.patrick.sc.objects.PositionListener;
import de.hechler.patrick.sc.objects.Rightangle;
import de.hechler.patrick.sc.objects.UnchangeablePosition;

@Deprecated(forRemoval = true)
public class AreaOLD2 implements Set <Position>, Cloneable {
	
	/**
	 * compares primary the x-coordinates, if they are equals, it compares the y-coordinate
	 */
	private final static Comparator <Position> xcmp = (a, b) -> {
		int aa = a.getX();
		int bb = b.getX();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		aa = a.getY();
		bb = b.getY();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		return 0;
	};
	
	/**
	 * compares primary the y-coordinates, if they are equals, it compares the x-coordinate
	 */
	private final static Comparator <Position> ycmp = (a, b) -> {
		int aa = a.getY();
		int bb = b.getY();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		bb = b.getX();
		aa = a.getX();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		return 0;
	};
	
	
	
	private NavigableSet <Rightangle> xMin;
	private NavigableSet <Rightangle> yMin;
	
	
	
	public AreaOLD2() {
		xMin = new TreeSet <Rightangle>(xcmp);
		yMin = new TreeSet <Rightangle>(ycmp);
	}
	
	
	
	@Override
	public int size() {
		int size = 0;
		for (Rightangle r : (Set <Rightangle>) xMin) {
			size += r.size();
		}
		return size;
	}
	
	
	@Override
	public boolean isEmpty() {
		return xMin.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if ( ! (o instanceof Position)) return false;
		else return contains((Position) o);
	}
	
	public boolean contains(Position pos) {
		return get(pos) != null;
	}
	
	@Override
	public Iterator <Position> iterator() {
		return new Iterator <Position>() {
			
			Iterator <Rightangle> r;
			Iterator <Position>   i;
			
			@Override
			public boolean hasNext() {
				boolean ihn = i.hasNext();
				while ( !ihn) {
					if (r.hasNext()) {
						i = r.next().iterator();
					} else return false;
					ihn = i.hasNext();
				}
				return ihn;
			}
			
			@Override
			public Position next() {
				boolean ihn = i.hasNext();
				while ( !ihn) {
					i = r.next().iterator();
					ihn = i.hasNext();
				}
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] res = new Object[size()];
		int i = 0;
		for (Position add : this) {
			res[i] = add;
		}
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.getClass() == Position[].class) {
			int s = size();
			if (a.length != s) a = (T[]) new Position[s];
			Position[] arr = (Position[]) a;
			int i = 0;
			for (Position add : this) {
				arr[i] = add;
			}
		} else if (a.getClass() == UnchangeablePosition[].class) {
			int s = size();
			if (a.length != s) a = (T[]) new UnchangeablePosition[s];
			UnchangeablePosition[] arr = (UnchangeablePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new UnchangeablePosition(pos);
			}
		} else if (a.getClass() == AbsoluteManipulablePosition[].class) {
			int s = size();
			if (a.length != s) a = (T[]) new AbsoluteManipulablePosition[s];
			AbsoluteManipulablePosition[] arr = (AbsoluteManipulablePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new AbsoluteManipulablePosition(pos);
			}
		} else if (a.getClass() == NicePosition[].class) {
			int s = size();
			if (a.length != s) a = (T[]) new NicePosition[s];
			NicePosition[] arr = (NicePosition[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new NicePosition(pos);
			}
		} else if (a.getClass() == PositionListener[].class) {
			int s = size();
			if (a.length != s) a = (T[]) new PositionListener[s];
			PositionListener[] arr = (PositionListener[]) a;
			int i = 0;
			for (Position pos : this) {
				arr[i] = new PositionListener(pos);
			}
		} else if (a.getClass() == Rightangle[].class) {
			int s = xMin.size();
			if (a.length != s) a = (T[]) new Rightangle[s];
			Rightangle[] arr = (Rightangle[]) a;
			int i = 0;
			for (Rightangle r : (Set <Rightangle>) xMin) {
				arr[i ++ ] = r;
			}
		} else {
			throw new ClassCastException("can't fill array a which is not an instance of Position[]");
		}
		return a;
	}
	
	@Override
	public boolean add(Position e) {
		Rightangle r = get(e);
		if (r != null) return false;
		r = new Rightangle(e.getX(), e.getY(), 1, 1);
		xMin.add(r);
		yMin.add(r);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Position) return remove((Position) o);
		return false;
	}
	
	public boolean remove(Position pos) {
		Rightangle r = get(pos);
		if (r == null) return false;
		xMin.remove(r);
		yMin.remove(r);
		Rightangle a, b, c, d;
		final int px = pos.getX(), py = pos.getY();
		
		a = new Rightangle(r.getX(), py + 1, px + 1 - r.getX(), r.getY() + r.getyCnt() - py - 2);
		b = new Rightangle(px + 1, py, r.getX() + r.getxCnt() - px - 2, a.getyCnt() + 1);
		c = new Rightangle(px, r.getY(), r.getX() + r.getxCnt() - 1 - px, py - r.getY());
		d = new Rightangle(r.getX(), r.getY(), a.getxCnt() - 1, c.getyCnt() + 1);
		
		if (a.getxCnt() > 0 && a.getyCnt() > 0) {
			xMin.add(a);
			yMin.add(a);
		}
		if (b.getxCnt() > 0 && b.getyCnt() > 0) {
			xMin.add(b);
			yMin.add(b);
		}
		if (c.getxCnt() > 0 && c.getyCnt() > 0) {
			xMin.add(c);
			yMin.add(c);
		}
		if (d.getxCnt() > 0 && d.getyCnt() > 0) {
			xMin.add(d);
			yMin.add(d);
		}
		
		return true;
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c == null) return false;
		if (c.isEmpty()) return true;
		if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			NavigableSet <Rightangle> mys = getRightangles(r);
			if (mys.size() == 1) {
				return mys.iterator().next().containsAll(r);
			}
			AreaOLD2 a = new AreaOLD2();
			a.addAll(r);
			for (Rightangle rem : mys) {
				a.removeAll(rem);
			}
			return a.isEmpty();
		} else if (c instanceof AreaOLD2) {
			for (Rightangle r : ((AreaOLD2) c).xMin) {
				if ( !containsAll(r)) return false;
			}
			return true;
		} else {
			for (Object object : c) {
				if ( !contains(object)) return false;
			}
			return true;
		}
	}
	
	@Override
	public boolean addAll(Collection <? extends Position> c) {
		if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			if (containsAll(r)) return false;
			removeAll(r);
			xMin.add(r);
			yMin.add(r);
			return true;
		} else if (c instanceof AreaOLD2) {
			boolean res = false;
			for (Rightangle r : ((AreaOLD2) c).xMin) {
				res |= addAll(r);
			}
			return res;
		} else {
			boolean res = false;
			for (Position pos : c) {
				res |= add(pos);
			}
			return res;
		}
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			NavigableSet <Rightangle> pos = getRightangles(r);
			final Bool res = new Bool(pos.size() != xMin.size());
			xMin.clear();
			yMin.clear();
			pos.forEach(rebuild -> {
				if (r.getX() > rebuild.getX()) {
					rebuild.setxCnt(rebuild.getX() + rebuild.getxCnt() - r.getX());
					rebuild.setX(r.getX());
					res.b = true;
				}
				if (r.getY() > rebuild.getY()) {
					rebuild.setyCnt(rebuild.getY() + rebuild.getyCnt() - r.getY());
					rebuild.setY(r.getY());
					res.b = true;
				}
				if (r.getY() + r.getyCnt() < rebuild.getY() + rebuild.getyCnt()) {
					rebuild.setyCnt(r.getY() + r.getyCnt() - rebuild.getY());
					res.b = true;
				}
				if (r.getY() + r.getyCnt() < rebuild.getY() + rebuild.getyCnt()) {
					rebuild.setyCnt(r.getY() + r.getyCnt() - rebuild.getY());
					res.b = true;
				}
				xMin.add(rebuild);
				yMin.add(rebuild);
			});
			return res.b;
		} else if (c instanceof AreaOLD2) {
			
		} else {
			@SuppressWarnings("rawtypes")
			Class <? extends Collection> clas = c.getClass();
			boolean rightangleCollection = false;
			try {
				Method irc = clas.getMethod("isRightangleCollection");
				rightangleCollection = (boolean) (Boolean) irc.invoke(c);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
			// TODO Auto-generated method stub
		}
		return false;
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void clear() {
		xMin.clear();
		yMin.clear();
	}
	
	Rightangle get(Position pos) {
		@SuppressWarnings("unchecked")
		NavigableSet <Position> xm = (NavigableSet <Position>) (NavigableSet <? extends Position>) xMin;
		@SuppressWarnings("unchecked")
		NavigableSet <Position> ym = (NavigableSet <Position>) (NavigableSet <? extends Position>) yMin;
		Rightangle xx = (Rightangle) xm.floor(pos);
		Rightangle yy = (Rightangle) ym.floor(pos);
		if (xx == null || yy == null) return null;
		
		if (xx.contains(pos)) {
			return xx;
		}
		if (yy.contains(pos)) {
			return yy;
		}
		
		while (true) {
			xx = (Rightangle) xm.lower(xx);
			if (xx == null) return null;
			
			if (xx.contains(pos)) {
				return xx;
			}
			yy = (Rightangle) ym.lower(yy);
			if (yy == null) return null;
			if (yy.contains(pos)) {
				return yy;
			}
		}
	}
	
	NavigableSet <Rightangle> getRightangles(Rightangle r) {
		NavigableSet <Rightangle> all = new TreeSet <Rightangle>(xcmp);
		Rightangle xx = (Rightangle) xMin.floor(r);
		Rightangle yy = (Rightangle) yMin.floor(r);
		if (xx == null || yy == null) return all;
		
		final Position a = new UnchangeablePosition(r), b = r.xMinYMax(), c = r.xMaxYMin(), d = r.xMaxYMax();
		
		if (xx.contains(a)) all.add(xx);
		else if (xx.contains(b)) all.add(xx);
		else if (xx.contains(c)) all.add(xx);
		else if (xx.contains(d)) all.add(xx);
		
		if (yy.contains(a)) all.add(yy);
		else if (yy.contains(b)) all.add(yy);
		else if (yy.contains(c)) all.add(yy);
		else if (yy.contains(d)) all.add(yy);
		
		while (true) {
			xx = (Rightangle) xMin.lower(xx);
			if (xx == null) break;
			yy = (Rightangle) yMin.lower(yy);
			if (yy == null) break;
			
			if (xx.contains(a)) all.add(xx);
			else if (xx.contains(b)) all.add(xx);
			else if (xx.contains(c)) all.add(xx);
			else if (xx.contains(d)) all.add(xx);
			
			if (yy.contains(a)) all.add(yy);
			else if (yy.contains(b)) all.add(yy);
			else if (yy.contains(c)) all.add(yy);
			else if (yy.contains(d)) all.add(yy);
		}
		
		return all;
	}
	
	@SuppressWarnings("unchecked")
	List <Rightangle> getAll(Position pos) {
		List <Rightangle> all = new ArrayList <Rightangle>();
		NavigableSet <Position> xm = (NavigableSet <Position>) (NavigableSet <? extends Position>) xMin;
		NavigableSet <Position> ym = (NavigableSet <Position>) (NavigableSet <? extends Position>) yMin;
		Rightangle xx = (Rightangle) xm.floor(pos);
		Rightangle yy = (Rightangle) ym.floor(pos);
		if (xx == null || yy == null) return all;
		final int x = pos.getX(), y = pos.getY();
		if (xx.getY() <= y) {
			all.add(xx);
		}
		if (yy.getX() <= x) {
			all.add(yy);
		}
		
		while (true) {
			xx = (Rightangle) xm.lower(xx);
			if (xx == null) break;
			yy = (Rightangle) ym.lower(yy);
			if (yy == null) break;
			
			if (xx.getY() <= y) {
				all.add(xx);
			}
			if (yy.getX() <= x) {
				all.add(yy);
			}
		}
		
		return all;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected AreaOLD2 clone() {
		AreaOLD2 a;
		try {
			a = (AreaOLD2) super.clone();
			try {
				a.xMin = (NavigableSet <Rightangle>) ((TreeSet <Rightangle>) xMin).clone();
				a.yMin = (NavigableSet <Rightangle>) ((TreeSet <Rightangle>) yMin).clone();
			} catch (Exception e) {
				a.xMin.clear();
				a.yMin.clear();
				a.xMin.addAll(yMin);
				a.yMin.addAll(xMin);
			}
			return a;
		} catch (CloneNotSupportedException e) {
			a = new AreaOLD2();
			a.xMin.addAll(yMin);
			a.yMin.addAll(xMin);
		}
		return a;
	}
	
	private class Bool {
		
		boolean b;
		
		public Bool(boolean b) {
			this.b = b;
		}
		
	}
	
}
