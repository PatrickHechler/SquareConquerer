package de.hechler.patrick.sc.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import de.hechler.patrick.sc.interfaces.Position;

@Deprecated
public class AreaOLD implements Set <Position> {
	
	/**
	 * compares primary the x-coordinates, if they are equals, it compares the y-coordinate
	 */
	private final static Comparator <Position> cmp = (a, b) -> {
		int aa = a.getX(), bb = b.getX();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		aa = a.getY();
		bb = b.getY();
		if (aa < bb) return -1;
		if (aa > bb) return 1;
		
		return 0;
	};
	
	
	
	/**
	 * Saves all {@link Rightangle}s in this {@link AreaOLD} and are sorted with the {@link Comparator} {@code cmp}.
	 */
	private NavigableMap <Position, Rightangle> xMin;
	
	
	
	public AreaOLD() {
		xMin = new TreeMap <>(cmp);
	}
	
	@Override
	public int size() {
		int size = 0;
		for (Rightangle r : xMin.values()) {
			size += r.size();
		}
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return xMin.isEmpty();
	}
	
	public boolean contains(Position pos) {
		return get(pos) != null;
	}
	
	public boolean remove(Position pos) {
		Rightangle r = get(pos);
		if (r == null) return false;
		final int xx = pos.getX(), yy = pos.getY();
		Rightangle a, b, c, d;
		xMin.remove(new UnchangeablePosition(r.x, r.y));
		a = new Rightangle(r.x, r.y, r.xCnt + r.x - xx, r.yCnt);
		b = new Rightangle(r.x + a.xCnt, r.y, 1, r.yCnt + r.y - yy);
		c = new Rightangle(b.x, yy + 1, 1, r.y + r.yCnt - r.y);
		d = new Rightangle(xx + 1, r.y, r.xCnt + r.x - r.x, r.yCnt);
		if ( !a.isEmpty()) xMin.put(new UnchangeablePosition(a.x, a.y), a);
		if ( !b.isEmpty()) xMin.put(new UnchangeablePosition(b.x, b.y), b);
		if ( !c.isEmpty()) xMin.put(new UnchangeablePosition(c.x, c.y), c);
		if ( !d.isEmpty()) xMin.put(new UnchangeablePosition(d.x, d.y), d);
		return true;
	}
	
	@Override
	public boolean containsAll(Collection <?> contain) {
		if (contain.isEmpty()) return true;
		if (contain instanceof AreaOLD) {
			AreaOLD a = (AreaOLD) contain;
			for (Rightangle r : a.xMin.values()) {
				if ( !containsAll(r)) return false;
			}
			return true;
		} else if (contain instanceof Rightangle) {
			Rightangle r = (Rightangle) contain;
			Rightangle my = get(r);
			if (my == null) return false;
			Rightangle zw = new Rightangle(my.x, my.y, my.xCnt, my.xCnt);
			zw.xCnt += zw.x - r.x;
			zw.yCnt += zw.y - r.y;
			zw.x = r.x;
			zw.y = r.y;
			if (zw.xCnt >= r.xCnt && zw.yCnt >= r.yCnt) return true;
			Rightangle a, b, c;
			a = new Rightangle(zw.x, zw.y + zw.yCnt, zw.xCnt, r.yCnt - zw.yCnt);
			b = new Rightangle(zw.x + zw.xCnt, r.y, r.xCnt - zw.xCnt, zw.yCnt);
			c = new Rightangle(zw.x + zw.xCnt, zw.y + zw.yCnt, r.xCnt - zw.xCnt, r.yCnt - zw.yCnt);
			boolean res = this.containsAll(a);
			res &= this.containsAll(b);
			res &= this.containsAll(c);
			return res;
		} else {
			for (Object check : contain) {
				if ( !contains(check)) return false;
			}
			return true;
		}
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		if (c.isEmpty()) return false;
		if (c instanceof AreaOLD) {
			boolean res = false;
			for (Rightangle r : ((AreaOLD) c).xMin.values()) {
				res |= removeAll(r);
			}
			return res;
		} else if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			Rightangle my = get(r);
			if (my == null) return false;
			xMin.remove(new UnchangeablePosition(r.x, r.y));
			
			
			Rightangle zw = new Rightangle(my.x, my.y, my.xCnt, my.yCnt + my.y - r.y);
			if ( !zw.isEmpty()) xMin.put(new UnchangeablePosition(zw.x, zw.y), zw);
			zw = new Rightangle(my.x, zw.y + zw.yCnt, my.x + my.xCnt - r.x, my.x + my.xCnt - r.y);
			if ( !zw.isEmpty()) xMin.put(new UnchangeablePosition(zw.x, zw.y), zw);
			
			
			my = new Rightangle(r.x, r.y, my.x + my.xCnt - r.x, my.y + my.yCnt - my.y); // bring both to the same pos
			
			
			zw = new Rightangle(my.x, r.y + r.yCnt, my.xCnt, my.yCnt - r.yCnt);
			if (zw.xCnt > 0 && zw.yCnt > 0) xMin.put(new UnchangeablePosition(zw.x, zw.y), zw);
			zw = new Rightangle(r.x + r.xCnt, my.y, my.xCnt - r.xCnt, my.yCnt);
			if (zw.xCnt > 0 && zw.yCnt > 0) xMin.put(new UnchangeablePosition(zw.x, zw.y), zw);
			
			
			if (r.xCnt > my.xCnt) {
				zw = new Rightangle(my.x + my.xCnt, r.y, r.xCnt - my.xCnt, r.yCnt);
				removeAll(zw);
			}
			if (r.yCnt > my.yCnt) {
				zw = new Rightangle(r.x, my.y + my.yCnt, r.xCnt, r.yCnt - my.yCnt);
				removeAll(zw);
			}
			
			return true;
		} else {
			boolean res = false;
			for (Object obj : c) {
				res |= remove(obj);
			}
			return res;
		}
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Position) return get((Position) o) != null;
		else return false;
	}
	
	@Override
	public Iterator <Position> iterator() {
		return new Iterator <Position>() {
			
			private Iterator <Rightangle> ri;
			private Iterator <Position>   pi;
			
			@Override
			public boolean hasNext() {
				boolean pihn = pi.hasNext();
				while ( !pihn && ri.hasNext()) {
					pi = ri.next().iterator();
					pihn = pi.hasNext();
				}
				return pihn;
			}
			
			@Override
			public Position next() {
				boolean pihn = pi.hasNext();
				while ( !pihn && ri.hasNext()) {
					pi = ri.next().iterator();
					pihn = pi.hasNext();
				}
				return pi.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		final List <Position> res = new ArrayList <Position>();
		forEach(pos -> res.add(pos));
		return res.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		final List <Position> res = new ArrayList <Position>();
		forEach(pos -> res.add(pos));
		return res.toArray(a);
	}
	
	@Override
	public boolean add(Position pos) {
		if (get(pos) != null) return false;
		Rightangle r = new Rightangle(pos.getX(), pos.getY(), 1, 1);
		xMin.put(new UnchangeablePosition(r.x, r.y), r);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Position) return remove((Position) o);
		else return false;
	}
	
	@Override
	public boolean addAll(Collection <? extends Position> c) {
		if (c instanceof AreaOLD) {
			boolean res = false;
			for (Rightangle r : ((AreaOLD) c).xMin.values()) {
				res |= addAll(r);
			}
			return res;
		} else if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;
			Rightangle my = get(r);
			if (my == null) {
				xMin.put(r, r);
				makeCorrect(r);
				return true;
			}
			
			my = new Rightangle(r.x, r.y, my.x + my.xCnt - r.x, my.y + my.yCnt - r.y);
			
			
			if (my.xCnt < r.xCnt) {
				Rightangle add = new Rightangle(my.x + my.xCnt, my.y, my.x + my.xCnt - r.x, r.yCnt);
				addAll(add);
				if (my.yCnt < r.yCnt) {
					add = new Rightangle(my.x, my.y + my.yCnt, my.x, my.y + my.yCnt - r.yCnt);
					addAll(add);
				}
				makeCorrect(r);
				return true;
				
			} else if (my.yCnt < r.yCnt) {
				Rightangle add = new Rightangle(my.x, my.y + my.yCnt, r.x, my.y + my.yCnt - r.yCnt);
				addAll(add);
				makeCorrect(r);
				return true;
				
			} else return false;
		} else {
			boolean res = false;
			for (Position p : c) {
				res |= add(p);
			}
			return res;
		}
	}
	
	@SuppressWarnings("null")
	private void makeCorrect(Rightangle r) throws NullPointerException {
		Position maxPosition = r.maxPosition();
		Rightangle rebuild = getBest(maxPosition);
		while ( !r.equals(rebuild)) {
			Rightangle a, b = null, c = null;
			
			xMin.remove(rebuild);
			
			a = new Rightangle(r.x + r.xCnt, r.y + r.yCnt, rebuild.x + rebuild.xCnt - r.x - r.xCnt, rebuild.y + rebuild.yCnt - r.y - r.yCnt);
			if (a.xCnt > 0 && a.y > 0) xMin.put(a, a);
			// b = new Rightangle(rebuild.x, r.y + r.yCnt,
			if (b.xCnt > 0 && b.y > 0) xMin.put(b, b);
			// c = new Rightangle(
			if (c.xCnt > 0 && c.y > 0) xMin.put(c, c);
			
			rebuild = getBest(maxPosition);
		}
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c.isEmpty()) return false;
		if (c instanceof AreaOLD) {
			boolean res = false;
			for (Rightangle r : ((AreaOLD) c).xMin.values()) {
				res |= retainAll(r);
			}
			return res;
		} else if (c instanceof Rightangle) {
			Rightangle r = (Rightangle) c;// get\(new [a-zA-Z]+\([a.zA-Z. ,]+\)
			Rightangle my = get(r);
			if (my == null) return false;
			xMin.remove(new UnchangeablePosition(r.x, r.y));
			
			
			Rightangle zw = new Rightangle(my.x, my.y, my.xCnt, my.yCnt + my.y - r.y);
			if ( !zw.isEmpty()) xMin.put(zw, zw);
			zw = new Rightangle(my.x, zw.y + zw.yCnt, my.x + my.xCnt - r.x, my.x + my.xCnt - r.y);
			if ( !zw.isEmpty()) xMin.put(zw, zw);
			
			
			my = new Rightangle(r.x, r.y, my.x + my.xCnt - r.x, my.y + my.yCnt - my.y); // bring both to the same pos
			
			
			zw = new Rightangle(my.x, r.y + r.yCnt, my.xCnt, my.yCnt - r.yCnt);
			if (zw.xCnt > 0 && zw.yCnt > 0) xMin.put(zw, zw);
			zw = new Rightangle(r.x + r.xCnt, my.y, my.xCnt - r.xCnt, my.yCnt);
			if (zw.xCnt > 0 && zw.yCnt > 0) xMin.put(zw, zw);
			
			
			if (r.xCnt > my.xCnt) {
				zw = new Rightangle(my.x + my.xCnt, r.y, r.xCnt - my.xCnt, r.yCnt);
				removeAll(zw);
			}
			if (r.yCnt > my.yCnt) {
				zw = new Rightangle(r.x, my.y + my.yCnt, r.xCnt, r.yCnt - my.yCnt);
				removeAll(zw);
			}
			
			return true;
		} else {
			boolean res = false;
			Entry <Position, Rightangle> e = xMin.firstEntry();
			Rightangle r = e.getValue();
			Position p = e.getKey();
			if ( !c.contains(r)) {
				remove(p);
				res = true;
			}
			while (true) {
				e = xMin.higherEntry(p);
				if (e == null) return res;
				r = e.getValue();
				p = e.getKey();
				if ( !c.contains(r)) {
					res = true;
					remove(p);
				}
			}
		}
	}
	
	@Override
	public void clear() {
		xMin.clear();
	}
	
	private Rightangle get(Position pos) {
		if (pos == null) return null;
		final int x = pos.getX(), y = pos.getY();
		Position s = new NicePosition(x, y);
		Entry <Position, Rightangle> e = xMin.floorEntry(s);
		if (e == null) return null;
		s = e.getKey();
		Rightangle r = e.getValue();
		int yNear = Integer.MAX_VALUE;
		if (r.y <= y) {
			if ( ! (r.y + r.yCnt > y || r.x + r.xCnt > x)) return r;
			yNear = y - r.y;
		}
		while (true) {
			e = xMin.lowerEntry(s);
			if (e == null) return null;
			r = e.getValue();
			s = e.getKey();
			if (r.y > y) continue;
			int nyNear = y - r.y;
			if (nyNear >= yNear) continue;
			yNear = nyNear;
			if (r.y + r.yCnt > y) {
				if (yNear == 0) return null;
				continue;
			}
			if (r.x + r.xCnt > x) {
				if (yNear == 0) return null;
				continue;
			}
			return r;
		}
	}
	
	private Rightangle getBest(Position pos) {
		if (pos == null) return null;
		final int x = pos.getX(), y = pos.getY();
		Position s = new NicePosition(x, y);
		Entry <Position, Rightangle> e = xMin.floorEntry(s);
		if (e == null) return null;
		s = e.getKey();
		Rightangle r = e.getValue();
		if (r.y <= y) {
			return r;
		}
		while (true) {
			e = xMin.lowerEntry(s);
			if (e == null) return null;
			r = e.getValue();
			s = e.getKey();
			if (r.y > y) continue;
			return r;
		}
	}
	
	/**
	 * NOT THREAD-SAFE
	 */
	public void rebuild() {
		NavigableMap <Position, Rightangle> newXMin = new TreeMap <Position, Rightangle>(cmp);
		// TODO Auto-generated method stub
		
		xMin = newXMin;
	}
	
}