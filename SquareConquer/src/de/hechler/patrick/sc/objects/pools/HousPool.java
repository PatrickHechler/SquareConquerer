package de.hechler.patrick.sc.objects.pools;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hechler.patrick.sc.objects.Building;
import de.hechler.patrick.sc.objects.HouseBuilding;

public class HousPool implements Set <HouseBuilding>, Serializable {
	
	/** UID */
	private static final long serialVersionUID = -5657466692977955798L;
	
	
	
	private transient HousPool listen;
	
	
	private final Set <HouseBuilding> mySmpleHouses;
	private final Set <HouseBuilding> myMeeleHouses;
	private final Set <HouseBuilding> myBowHouses;
	private final Set <HouseBuilding> myBuilderHouses;
	
	
	
	public HousPool() {
		mySmpleHouses = new HashSet <HouseBuilding>();
		myMeeleHouses = new HashSet <HouseBuilding>();
		myBowHouses = new HashSet <HouseBuilding>();
		myBuilderHouses = new HashSet <HouseBuilding>();
	}
	
	public HousPool(HousPool copy) {
		mySmpleHouses = new HashSet <HouseBuilding>(copy.mySmpleHouses);
		myMeeleHouses = new HashSet <HouseBuilding>(copy.myMeeleHouses);
		myBowHouses = new HashSet <HouseBuilding>(copy.myBowHouses);
		myBuilderHouses = new HashSet <HouseBuilding>(copy.myBuilderHouses);
	}
	
	public HousPool(HousPool copy, Void onlyListen) {
		listen = this;
		mySmpleHouses = Collections.unmodifiableSet(copy.mySmpleHouses);
		myMeeleHouses = Collections.unmodifiableSet(copy.myMeeleHouses);
		myBowHouses = Collections.unmodifiableSet(copy.myBowHouses);
		myBuilderHouses = Collections.unmodifiableSet(copy.myBuilderHouses);
	}
	
	
	
	@Override
	public int size() {
		long s = (long) mySmpleHouses.size() + (long) myMeeleHouses.size() + (long) myBowHouses.size() + (long) myBuilderHouses.size();
		return (int) Math.min(s, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean isEmpty() {
		return mySmpleHouses.isEmpty() && myMeeleHouses.isEmpty() && myBowHouses.isEmpty() && myBuilderHouses.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof HouseBuilding) return contains((HouseBuilding) o);
		else return false;
	}
	
	public boolean contains(Building b) {
		switch (b.type) {
		default:
			throw new IllegalArgumentException("unknown house type: " + b.type.name() + " of house: '" + b + "'");
		case house:
			return mySmpleHouses.contains(b);
		case houseBow:
			return myBowHouses.contains(b);
		case houseBuilder:
			return myBuilderHouses.contains(b);
		case houseMelee:
			return myMeeleHouses.contains(b);
		}
	}
	
	@Override
	public Iterator <HouseBuilding> iterator() {
		return new Iterator <HouseBuilding>() {
			
			int                      s = 0;
			Iterator <HouseBuilding> i = mySmpleHouses.iterator();
			
			@Override
			public boolean hasNext() {
				boolean n = i.hasNext();
				if ( !n) switch (s) {
				case 0:
					s ++ ;
					i = myBowHouses.iterator();
					n = i.hasNext();
				case 1:
					s ++ ;
					i = myBuilderHouses.iterator();
					n = i.hasNext();
				case 2:
					s ++ ;
					i = myMeeleHouses.iterator();
					n = i.hasNext();
				}
				return n;
			}
			
			@Override
			public HouseBuilding next() {
				hasNext();
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		int i = 0;
		for (HouseBuilding object : this) {
			o[i ++ ] = object;
		}
		return o;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int s = size();
		if (s != a.length) a = (T[]) Array.newInstance(a.getClass().getComponentType(), s);
		Object[] arr = a;
		int i = 0;
		for (HouseBuilding object : this) {
			arr[i ++ ] = object;
		}
		return a;
	}
	
	@Override
	public boolean add(HouseBuilding e) {
		switch (e.type) {
		case house:
			return mySmpleHouses.add(e);
		case houseBow:
			return mySmpleHouses.add(e);
		case houseBuilder:
			return mySmpleHouses.add(e);
		case houseMelee:
			return mySmpleHouses.add(e);
		default:
			throw new IllegalArgumentException("unknown house type: " + e.type.name() + " of house: '" + e + "'");
		}
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof HouseBuilding) return remove((HouseBuilding) o);
		else return false;
	}
	
	public boolean remove(HouseBuilding h) {
		switch (h.type) {
		case house:
			return mySmpleHouses.remove(h);
		case houseBow:
			return mySmpleHouses.remove(h);
		case houseBuilder:
			return mySmpleHouses.remove(h);
		case houseMelee:
			return mySmpleHouses.remove(h);
		default:
			return false;
		}
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof HousPool) {
			HousPool p = (HousPool) c;
			if ( !myBowHouses.containsAll(p.myBowHouses)) return false;
			if ( !myBuilderHouses.containsAll(p.myBuilderHouses)) return false;
			if ( !myMeeleHouses.containsAll(p.myMeeleHouses)) return false;
			return mySmpleHouses.containsAll(p.mySmpleHouses);
		} else if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			if ( !myBowHouses.containsAll(p.houses().myBowHouses)) return false;
			if ( !myBuilderHouses.containsAll(p.houses().myBuilderHouses)) return false;
			if ( !myMeeleHouses.containsAll(p.houses().myMeeleHouses)) return false;
			return mySmpleHouses.containsAll(p.houses().mySmpleHouses);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			if ( !myBowHouses.containsAll(p.buildings().houses().myBowHouses)) return false;
			if ( !myBuilderHouses.containsAll(p.buildings().houses().myBuilderHouses)) return false;
			if ( !myMeeleHouses.containsAll(p.buildings().houses().myMeeleHouses)) return false;
			return mySmpleHouses.containsAll(p.buildings().houses().mySmpleHouses);
		} else {
			for (Object object : c) {
				if ( !contains(object)) return false;
			}
			return true;
		}
	}
	
	public boolean containsAll(HousPool p) {
		if ( !myBowHouses.containsAll(p.myBowHouses)) return false;
		if ( !myBuilderHouses.containsAll(p.myBuilderHouses)) return false;
		if ( !myMeeleHouses.containsAll(p.myMeeleHouses)) return false;
		return mySmpleHouses.containsAll(p.mySmpleHouses);
	}
	
	public boolean containsAll(BuildingPool p) {
		if ( !myBowHouses.containsAll(p.houses().myBowHouses)) return false;
		if ( !myBuilderHouses.containsAll(p.houses().myBuilderHouses)) return false;
		if ( !myMeeleHouses.containsAll(p.houses().myMeeleHouses)) return false;
		return mySmpleHouses.containsAll(p.houses().mySmpleHouses);
	}
	
	public boolean containsAll(EntityPool p) {
		if ( !myBowHouses.containsAll(p.buildings().houses().myBowHouses)) return false;
		if ( !myBuilderHouses.containsAll(p.buildings().houses().myBuilderHouses)) return false;
		if ( !myMeeleHouses.containsAll(p.buildings().houses().myMeeleHouses)) return false;
		return mySmpleHouses.containsAll(p.buildings().houses().mySmpleHouses);
	}
	
	@Override
	public boolean addAll(Collection <? extends HouseBuilding> c) {
		if (c instanceof HousPool) {
			HousPool p = (HousPool) c;
			boolean res = myBowHouses.addAll(p.myBowHouses);
			res |= myBuilderHouses.addAll(p.myBuilderHouses);
			res |= myMeeleHouses.addAll(p.myMeeleHouses);
			return res | mySmpleHouses.addAll(p.mySmpleHouses);
		} else {
			boolean res = false;
			for (HouseBuilding houseBuilding : c) {
				res |= add(houseBuilding);
			}
			return res;
		}
	}
	
	public boolean addAll(HousPool p) {
		boolean res = myBowHouses.addAll(p.myBowHouses);
		res |= myBuilderHouses.addAll(p.myBuilderHouses);
		res |= myMeeleHouses.addAll(p.myMeeleHouses);
		return res | mySmpleHouses.addAll(p.mySmpleHouses);
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		boolean res;
		if (c instanceof HousPool) {
			HousPool p = (HousPool) c;
			res = myBowHouses.retainAll(p.myBowHouses);
			res |= myBuilderHouses.retainAll(p.myBuilderHouses);
			res |= myMeeleHouses.retainAll(p.myMeeleHouses);
			res |= mySmpleHouses.retainAll(p.mySmpleHouses);
		} else if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			res = myBowHouses.retainAll(p.houses().myBowHouses);
			res |= myBuilderHouses.retainAll(p.houses().myBuilderHouses);
			res |= myMeeleHouses.retainAll(p.houses().myMeeleHouses);
			res |= mySmpleHouses.retainAll(p.houses().mySmpleHouses);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			res = myBowHouses.retainAll(p.buildings().houses().myBowHouses);
			res |= myBuilderHouses.retainAll(p.buildings().houses().myBuilderHouses);
			res |= myMeeleHouses.retainAll(p.buildings().houses().myMeeleHouses);
			res |= mySmpleHouses.retainAll(p.buildings().houses().mySmpleHouses);
		} else {
			if ( ! (c instanceof Set)) c = new HashSet <>(c);
			List <HouseBuilding> bow = new ArrayList <HouseBuilding>(), build = new ArrayList <HouseBuilding>(), meele = new ArrayList <HouseBuilding>(), simple = new ArrayList <HouseBuilding>();
			final Collection <?> col = c;
			myBowHouses.forEach(h -> {
				if ( !col.contains(h)) bow.add(h);
			});
			myBuilderHouses.forEach(h -> {
				if ( !col.contains(h)) build.add(h);
			});
			myMeeleHouses.forEach(h -> {
				if ( !col.contains(h)) meele.add(h);
			});
			mySmpleHouses.forEach(h -> {
				if ( !col.contains(h)) simple.add(h);
			});
			bow.forEach(h -> remove(h));
			build.forEach(h -> remove(h));
			meele.forEach(h -> remove(h));
			simple.forEach(h -> remove(h));
			res = bow.isEmpty();
			res |= build.isEmpty();
			res |= meele.isEmpty();
			res |= simple.isEmpty();
		}
		return res;
	}
	
	public boolean retainAll(HousPool p) {
		boolean res = myBowHouses.retainAll(p.myBowHouses);
		res |= myBuilderHouses.retainAll(p.myBuilderHouses);
		res |= myMeeleHouses.retainAll(p.myMeeleHouses);
		return res | mySmpleHouses.retainAll(p.mySmpleHouses);
	}
	
	public boolean retainAll(BuildingPool p) {
		boolean res = myBowHouses.retainAll(p.houses().myBowHouses);
		res |= myBuilderHouses.retainAll(p.houses().myBuilderHouses);
		res |= myMeeleHouses.retainAll(p.houses().myMeeleHouses);
		return res | mySmpleHouses.retainAll(p.houses().mySmpleHouses);
	}
	
	public boolean retainAll(EntityPool p) {
		boolean res = myBowHouses.retainAll(p.buildings().houses().myBowHouses);
		res |= myBuilderHouses.retainAll(p.buildings().houses().myBuilderHouses);
		res |= myMeeleHouses.retainAll(p.buildings().houses().myMeeleHouses);
		return res | mySmpleHouses.retainAll(p.buildings().houses().mySmpleHouses);
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		boolean res;
		if (c instanceof HousPool) {
			HousPool p = (HousPool) c;
			res = myBowHouses.removeAll(p.myBowHouses);
			res |= myBuilderHouses.removeAll(p.myBuilderHouses);
			res |= myMeeleHouses.removeAll(p.myMeeleHouses);
			res |= mySmpleHouses.removeAll(p.mySmpleHouses);
		} else if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			res = myBowHouses.removeAll(p.houses().myBowHouses);
			res |= myBuilderHouses.removeAll(p.houses().myBuilderHouses);
			res |= myMeeleHouses.removeAll(p.houses().myMeeleHouses);
			res |= mySmpleHouses.removeAll(p.houses().mySmpleHouses);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			res = myBowHouses.removeAll(p.buildings().houses().myBowHouses);
			res |= myBuilderHouses.removeAll(p.buildings().houses().myBuilderHouses);
			res |= myMeeleHouses.removeAll(p.buildings().houses().myMeeleHouses);
			res |= mySmpleHouses.removeAll(p.buildings().houses().mySmpleHouses);
		} else {
			res = false;
			for (Object object : c) {
				res |= remove(object);
			}
		}
		return res;
	}
	
	@Override
	public void clear() {
		myBowHouses.clear();
		myBuilderHouses.clear();
		myMeeleHouses.clear();
		mySmpleHouses.clear();
	}
	
	
	public HousPool listener() {
		if (listen == null) listen = new HousPool(this, null);
		return listen;
	}
	
}
