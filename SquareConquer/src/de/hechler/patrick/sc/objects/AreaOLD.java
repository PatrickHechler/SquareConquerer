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

@Deprecated(forRemoval = true)
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
		xMin.remove(new UnchangeablePosition(r.getX(), r.getY()));
		a = new Rightangle(r.getX(), r.getY(), r.getxCnt() + r.getX() - xx, r.getyCnt());
		b = new Rightangle(r.getX() + a.getxCnt(), r.getY(), 1, r.getyCnt() + r.getY() - yy);
		c = new Rightangle(b.getX(), yy + 1, 1, r.getY() + r.getyCnt() - r.getY());
		d = new Rightangle(xx + 1, r.getY(), r.getxCnt() + r.getX() - r.getX(), r.getyCnt());
		if ( !a.isEmpty()) xMin.put(new UnchangeablePosition(a.getX(), a.getY()), a);
		if ( !b.isEmpty()) xMin.put(new UnchangeablePosition(b.getX(), b.getY()), b);
		if ( !c.isEmpty()) xMin.put(new UnchangeablePosition(c.getX(), c.getY()), c);
		if ( !d.isEmpty()) xMin.put(new UnchangeablePosition(d.getX(), d.getY()), d);
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
			Rightangle zw = new Rightangle(my.getX(), my.getY(), my.getxCnt(), my.getY());
			zw.setxCnt(zw.getxCnt() + zw.getX() - r.getX());
			zw.setyCnt(zw.getyCnt() + zw.getY() - r.getY());
			zw.setX(r.getX());
			zw.setY(r.getY());
			if (zw.getxCnt() >= r.getxCnt() && zw.getyCnt() >= r.getyCnt()) return true;
			Rightangle a, b, c;
			a = new Rightangle(zw.getX(), zw.getY() + zw.getyCnt(), zw.getxCnt(), r.getyCnt() - zw.getyCnt());
			b = new Rightangle(zw.getX() + zw.getxCnt(), r.getY(), r.getxCnt() - zw.getxCnt(), zw.getyCnt());
			c = new Rightangle(zw.getX() + zw.getxCnt(), zw.getY() + zw.getyCnt(), r.getxCnt() - zw.getxCnt(), r.getyCnt() - zw.getyCnt());
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
			xMin.remove(new UnchangeablePosition(r.getX(), r.getY()));
			
			
			Rightangle zw = new Rightangle(my.getX(), my.getY(), my.getxCnt(), my.getyCnt() + my.getY() - r.getY());
			if ( !zw.isEmpty()) xMin.put(new UnchangeablePosition(zw.getX(), zw.getY()), zw);
			zw = new Rightangle(my.getX(), zw.getY() + zw.getyCnt(), my.getX() + my.getxCnt() - r.getX(), my.getX() + my.getxCnt() - r.getY());
			if ( !zw.isEmpty()) xMin.put(new UnchangeablePosition(zw.getX(), zw.getY()), zw);
			
			
			my = new Rightangle(r.getX(), r.getY(), my.getX() + my.getxCnt() - r.getX(), my.getY() + my.getyCnt() - my.getY()); // bring both to the same pos
			
			
			zw = new Rightangle(my.getX(), r.getY() + r.getyCnt(), my.getxCnt(), my.getyCnt() - r.getyCnt());
			if (zw.getxCnt() > 0 && zw.getyCnt() > 0) xMin.put(new UnchangeablePosition(zw.getX(), zw.getY()), zw);
			zw = new Rightangle(r.getX() + r.getxCnt(), my.getY(), my.getxCnt() - r.getxCnt(), my.getyCnt());
			if (zw.getxCnt() > 0 && zw.getyCnt() > 0) xMin.put(new UnchangeablePosition(zw.getX(), zw.getY()), zw);
			
			
			if (r.getxCnt() > my.getxCnt()) {
				zw = new Rightangle(my.getX() + my.getxCnt(), r.getY(), r.getxCnt() - my.getxCnt(), r.getyCnt());
				removeAll(zw);
			}
			if (r.getyCnt() > my.getyCnt()) {
				zw = new Rightangle(r.getX(), my.getY() + my.getyCnt(), r.getxCnt(), r.getyCnt() - my.getyCnt());
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
		xMin.put(new UnchangeablePosition(r.getX(), r.getY()), r);
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
			
			my = new Rightangle(r.getX(), r.getY(), my.getX() + my.getxCnt() - r.getX(), my.getY() + my.getyCnt() - r.getY());
			
			
			if (my.getxCnt() < r.getxCnt()) {
				Rightangle add = new Rightangle(my.getX() + my.getxCnt(), my.getY(), my.getX() + my.getxCnt() - r.getX(), r.getyCnt());
				addAll(add);
				if (my.getyCnt() < r.getyCnt()) {
					add = new Rightangle(my.getX(), my.getY() + my.getyCnt(), my.getX(), my.getY() + my.getyCnt() - r.getyCnt());
					addAll(add);
				}
				makeCorrect(r);
				return true;
				
			} else if (my.getyCnt() < r.getyCnt()) {
				Rightangle add = new Rightangle(my.getX(), my.getY() + my.getyCnt(), r.getX(), my.getY() + my.getyCnt() - r.getyCnt());
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
		Position maxPosition = r.xMaxYMax();
		Rightangle rebuild = getBest(maxPosition);
		while ( !r.equals(rebuild)) {
			Rightangle a, b = null, c = null;
			
			xMin.remove(rebuild);
			
			a = new Rightangle(r.getX() + r.getxCnt(), r.getY() + r.getyCnt(), rebuild.getX() + rebuild.getxCnt() - r.getX() - r.getxCnt(), rebuild.getY() + rebuild.getyCnt() - r.getY() - r.getyCnt());
			if (a.getxCnt() > 0 && a.getY() > 0) xMin.put(a, a);
			// b = new Rightangle(rebuild.x, r.getY() + r.getyCnt(),
			if (b.getxCnt() > 0 && b.getY() > 0) xMin.put(b, b);
			// c = new Rightangle(
			if (c.getxCnt() > 0 && c.getY() > 0) xMin.put(c, c);
			
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
			xMin.remove(new UnchangeablePosition(r.getX(), r.getY()));
			
			
			Rightangle zw = new Rightangle(my.getX(), my.getY(), my.getxCnt(), my.getyCnt() + my.getY() - r.getY());
			if ( !zw.isEmpty()) xMin.put(zw, zw);
			zw = new Rightangle(my.getX(), zw.getY() + zw.getyCnt(), my.getX() + my.getxCnt() - r.getX(), my.getX() + my.getxCnt() - r.getY());
			if ( !zw.isEmpty()) xMin.put(zw, zw);
			
			
			my = new Rightangle(r.getX(), r.getY(), my.getX() + my.getxCnt() - r.getX(), my.getY() + my.getyCnt() - my.getY()); // bring both to the same pos
			
			
			zw = new Rightangle(my.getX(), r.getY() + r.getyCnt(), my.getxCnt(), my.getyCnt() - r.getyCnt());
			if (zw.getxCnt() > 0 && zw.getyCnt() > 0) xMin.put(zw, zw);
			zw = new Rightangle(r.getX() + r.getxCnt(), my.getY(), my.getxCnt() - r.getxCnt(), my.getyCnt());
			if (zw.getxCnt() > 0 && zw.getyCnt() > 0) xMin.put(zw, zw);
			
			
			if (r.getxCnt() > my.getxCnt()) {
				zw = new Rightangle(my.getX() + my.getxCnt(), r.getY(), r.getxCnt() - my.getxCnt(), r.getyCnt());
				removeAll(zw);
			}
			if (r.getyCnt() > my.getyCnt()) {
				zw = new Rightangle(r.getX(), my.getY() + my.getyCnt(), r.getxCnt(), r.getyCnt() - my.getyCnt());
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
		if (r.getY() <= y) {
			if ( ! (r.getY() + r.getyCnt() > y || r.getX() + r.getxCnt() > x)) return r;
			yNear = y - r.getY();
		}
		while (true) {
			e = xMin.lowerEntry(s);
			if (e == null) return null;
			r = e.getValue();
			s = e.getKey();
			if (r.getY() > y) continue;
			int nyNear = y - r.getY();
			if (nyNear >= yNear) continue;
			yNear = nyNear;
			if (r.getY() + r.getyCnt() > y) {
				if (yNear == 0) return null;
				continue;
			}
			if (r.getX() + r.getxCnt() > x) {
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
		if (r.getY() <= y) {
			return r;
		}
		while (true) {
			e = xMin.lowerEntry(s);
			if (e == null) return null;
			r = e.getValue();
			s = e.getKey();
			if (r.getY() > y) continue;
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