package de.hechler.patrick.sc.objects;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.interfaces.Position;

public class Area implements Set <Position> {
	
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
	
	
	private NavigableSet <? extends Position> xMin;
	private NavigableSet <? extends Position> yMin;
	
	
	
	public Area() {
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
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public boolean add(Position e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean containsAll(Collection <?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean addAll(Collection <? extends Position> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean retainAll(Collection <?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public boolean removeAll(Collection <?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	private Rightangle get(Position pos) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Set <Rightangle> getAll(Position pos) {
		Set <Rightangle> all = new HashSet<Rightangle>();
		NavigableSet <Position> xm = (NavigableSet <Position>) xMin;
		NavigableSet <Position> ym = (NavigableSet <Position>) yMin;
		Rightangle xx = (Rightangle) xm.floor(pos);
		Rightangle yy = (Rightangle) ym.floor(pos);
		if (xx == null || yy == null) return all;
		final int x = pos.getX(), y = pos.getY();
		if (xx.y <= y) {
			all.add(xx);
		}
		if (yy.x <= x) {
			all.add(yy);
		}
		
		while (true) {
			xx = (Rightangle) xm.lower(xx);
			if (xx == null) break;
			yy = (Rightangle) ym.lower(yy);
			if (yy == null) break;
			
			if (xx.y <= y) {
				all.add(xx);
			}
			if (yy.x <= x) {
				all.add(yy);
			}
		}
		
		return all;
	}
	
}
