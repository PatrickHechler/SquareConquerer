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

import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.objects.Unit;

public class UnitPool implements Set <Unit>, Serializable {
	
	/** UID */
	private static final long serialVersionUID = -590316455593344712L;
	
	
	private transient UnitPool listen;
	
	
	private final FighterPool myFighters;
	
	private final Set <Unit> myBoats;
	private final Set <Unit> myCarriers;
	private final Set <Unit> myBuilders;
	
	private final Set <Unit> mySimples;
	
	
	public UnitPool() {
		myFighters = new FighterPool();
		myBoats = new HashSet <Unit>();
		myCarriers = new HashSet <Unit>();
		myBuilders = new HashSet <Unit>();
		mySimples = new HashSet <Unit>();
	}
	
	public UnitPool(UnitPool copy) {
		myFighters = new FighterPool(copy.myFighters);
		myBoats = new HashSet <Unit>(copy.myBoats);
		myCarriers = new HashSet <Unit>(copy.myCarriers);
		myBuilders = new HashSet <Unit>(copy.myBuilders);
		mySimples = new HashSet <Unit>(copy.mySimples);
	}
	
	private UnitPool(UnitPool copy, Void unmodifiable) {
		listen = this;
		myFighters = copy.myFighters.listener();
		myBoats = Collections.unmodifiableSet(new HashSet <Unit>(copy.myBoats));
		myCarriers = Collections.unmodifiableSet(new HashSet <Unit>(copy.myCarriers));
		myBuilders = Collections.unmodifiableSet(new HashSet <Unit>(copy.myBuilders));
		mySimples = Collections.unmodifiableSet(new HashSet <Unit>(copy.mySimples));
	}
	
	@Override
	public int size() {
		long s = (long) myBoats.size() + (long) myCarriers.size() + (long) myFighters.size() + (long) mySimples.size() + (long) myBuilders.size();
		return (int) Math.min(s, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean isEmpty() {
		return myBoats.isEmpty() && myCarriers.isEmpty() && myFighters.isEmpty() && mySimples.isEmpty() && myBuilders.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if (myBoats.contains(o)) return true;
		else if (myCarriers.contains(o)) return true;
		else if (myFighters.contains(o)) return true;
		else if (myBuilders.contains(o)) return true;
		else if (mySimples.contains(o)) return true;
		else return false;
	}
	
	public boolean contains(Unit u) {
		if (myBoats.contains(u)) return true;
		else if (myCarriers.contains(u)) return true;
		else if (myFighters.contains(u)) return true;
		else if (myBuilders.contains(u)) return true;
		else if (mySimples.contains(u)) return true;
		else return false;
	}
	
	@Override
	public Iterator <Unit> iterator() {
		return new Iterator <Unit>() {
			
			int                       s = 0;
			Iterator <? extends Unit> i = mySimples.iterator();
			
			@Override
			public boolean hasNext() {
				boolean n = i.hasNext();
				if ( !n) switch (s) {
				case 0:
					s ++ ;
					i = myCarriers.iterator();
					n = i.hasNext();
					if (n) break;
				case 1:
					s ++ ;
					i = myBoats.iterator();
					n = i.hasNext();
					if (n) break;
				case 2:
					s ++ ;
					i = myBuilders.iterator();
					n = i.hasNext();
					if (n) break;
				case 3:
					s ++ ;
					i = myFighters.iterator();
					n = i.hasNext();
				}
				return n;
			}
			
			@Override
			public Unit next() {
				hasNext();
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		int i = 0;
		for (Object obj : this) {
			o[i ++ ] = obj;
		}
		return o;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int s = size();
		Class <?> comp = a.getClass().getComponentType();
		if (s != a.length) a = (T[]) Array.newInstance(comp, s);
		int i = 0;
		if (Entity.class.isAssignableFrom(comp)) {
			Entity[] us = (Entity[]) a;
			for (Unit obj : this) {
				us[i ++ ] = obj;
			}
		} else if (comp == Object.class) {
			Object[] objs = (Object[]) a;
			for (Unit obj : this) {
				objs[i ++ ] = obj;
			}
		} else for (Unit un : this) {
			a[i ++ ] = (T) un;
		}
		return a;
	}
	
	@Override
	public boolean add(Unit e) {
		switch (e.type) {
		case bow:
		case meele:
		case fightingBoat:
			return myFighters.add(e);
		case boat:
			return myBoats.add(e);
		case builder:
			return myBuilders.add(e);
		case carrier:
			return myCarriers.add(e);
		case simple:
			return mySimples.add(e);
		default:
			throw new IllegalArgumentException(
					"the UnitPool can only contain units, which does not include types of non movable Entitys (even if they are an instance of the Unit class type=" + e.type.name() + " unit='" + e + "'");
		}
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Unit) return remove((Unit) o);
		return false;
	}
	
	public boolean remove(Unit u) {
		switch (u.type) {
		case bow:
		case meele:
		case fightingBoat:
			return myFighters.remove(u);
		case boat:
			return myBoats.remove(u);
		case builder:
			return myBuilders.remove(u);
		case carrier:
			return myCarriers.remove(u);
		case simple:
			return mySimples.remove(u);
		default:
			return false;
		}
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			if ( !myBoats.containsAll(p.myBoats)) return false;
			if ( !myBuilders.containsAll(p.myBuilders)) return false;
			if ( !myCarriers.containsAll(p.myCarriers)) return false;
			if ( !myFighters.containsAll(p.myFighters)) return false;
			return mySimples.containsAll(p.mySimples);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			if ( !myBoats.containsAll(p.units().myBoats)) return false;
			if ( !myBuilders.containsAll(p.units().myBuilders)) return false;
			if ( !myCarriers.containsAll(p.units().myCarriers)) return false;
			if ( !myFighters.containsAll(p.units().myFighters)) return false;
			return mySimples.containsAll(p.units().mySimples);
		} else if (c instanceof FighterPool) {
			return myFighters.containsAll((FighterPool) c);
		} else {
			for (Object o : c) {
				if ( !contains(o)) return false;
			}
			return true;
		}
	}
	
	public boolean containsAll(UnitPool p) {
		if ( !myBoats.containsAll(p.myBoats)) return false;
		if ( !myBuilders.containsAll(p.myBuilders)) return false;
		if ( !myCarriers.containsAll(p.myCarriers)) return false;
		if ( !myFighters.containsAll(p.myFighters)) return false;
		return mySimples.containsAll(p.mySimples);
	}
	
	public boolean containsAll(EntityPool p) {
		if ( !myBoats.containsAll(p.units().myBoats)) return false;
		if ( !myBuilders.containsAll(p.units().myBuilders)) return false;
		if ( !myCarriers.containsAll(p.units().myCarriers)) return false;
		if ( !myFighters.containsAll(p.units().myFighters)) return false;
		return mySimples.containsAll(p.units().mySimples);
	}
	
	public boolean containsAll(FighterPool c) {
		return myFighters.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection <? extends Unit> c) {
		boolean res;
		if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			res = myBoats.addAll(p.myBoats);
			res |= myBuilders.addAll(p.myBuilders);
			res |= myCarriers.addAll(p.myCarriers);
			res |= myFighters.addAll(p.myFighters);
			res |= mySimples.addAll(p.mySimples);
		} else if (c instanceof FighterPool) {
			res = myFighters.addAll((FighterPool) c);
		} else {
			res = false;
			for (Unit unit : c) {
				res |= add(unit);
			}
		}
		return res;
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			boolean res = myBoats.retainAll(p.myBoats);
			res |= myBuilders.retainAll(p.myBuilders);
			res |= myCarriers.retainAll(p.myCarriers);
			res |= myFighters.retainAll(p.myFighters);
			return res | mySimples.retainAll(p.mySimples);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			boolean res = myBoats.retainAll(p.units().myBoats);
			res |= myBuilders.retainAll(p.units().myBuilders);
			res |= myCarriers.retainAll(p.units().myCarriers);
			res |= myFighters.retainAll(p.units().myFighters);
			return res | mySimples.retainAll(p.units().mySimples);
		} else if (c instanceof FighterPool) {
			boolean res = ! (myBoats.isEmpty() && myBuilders.isEmpty() && myCarriers.isEmpty() && mySimples.isEmpty());
			myBoats.clear();
			myBuilders.clear();
			myCarriers.clear();
			mySimples.clear();
			return res | myFighters.retainAll((FighterPool) c);
		} else {
			List <Unit> fight = new ArrayList <Unit>(), build = new ArrayList <Unit>(), boat = new ArrayList <Unit>(), carrie = new ArrayList <Unit>(), simple = new ArrayList <Unit>();
			myFighters.forEach(o -> {
				if ( !c.contains(o)) fight.add(o);
			});
			myBuilders.forEach(o -> {
				if ( !c.contains(o)) build.add(o);
			});
			myBoats.forEach(o -> {
				if ( !c.contains(o)) boat.add(o);
			});
			myCarriers.forEach(o -> {
				if ( !c.contains(o)) carrie.add(o);
			});
			mySimples.forEach(o -> {
				if ( !c.contains(o)) simple.add(o);
			});
			fight.forEach(o -> myFighters.remove(o));
			build.forEach(o -> myBoats.remove(o));
			boat.forEach(o -> myBuilders.remove(o));
			carrie.forEach(o -> myCarriers.remove(o));
			simple.forEach(o -> mySimples.remove(o));
			return ! (fight.isEmpty() && build.isEmpty() && boat.isEmpty() && carrie.isEmpty() && simple.isEmpty());
		}
	}
	
	public boolean retainAll(UnitPool p) {
		boolean res = myBoats.retainAll(p.myBoats);
		res |= myBuilders.retainAll(p.myBuilders);
		res |= myCarriers.retainAll(p.myCarriers);
		res |= myFighters.retainAll(p.myFighters);
		return res | mySimples.retainAll(p.mySimples);
	}
	
	public boolean retainAll(EntityPool p) {
		boolean res = myBoats.retainAll(p.units().myBoats);
		res |= myBuilders.retainAll(p.units().myBuilders);
		res |= myCarriers.retainAll(p.units().myCarriers);
		res |= myFighters.retainAll(p.units().myFighters);
		return res | mySimples.retainAll(p.units().mySimples);
	}
	
	public boolean retainAll(FighterPool f) {
		boolean res = ! (myBoats.isEmpty() && myBuilders.isEmpty() && myCarriers.isEmpty() && mySimples.isEmpty());
		myBoats.clear();
		myBuilders.clear();
		myCarriers.clear();
		mySimples.clear();
		return res | myFighters.retainAll(f);
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			boolean res = myBoats.removeAll(p.myBoats);
			res |= myBuilders.removeAll(p.myBuilders);
			res |= myCarriers.removeAll(p.myCarriers);
			res |= myFighters.removeAll(p.myFighters);
			return res | mySimples.removeAll(p.mySimples);
		} else if (c instanceof FighterPool) {
			return myFighters.removeAll((FighterPool) c);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			boolean res = myBoats.removeAll(p.units().myBoats);
			res |= myBuilders.removeAll(p.units().myBuilders);
			res |= myCarriers.removeAll(p.units().myCarriers);
			res |= myFighters.removeAll(p.units().myFighters);
			return res | mySimples.removeAll(p.units().mySimples);
		} else {
			boolean res = false;
			for (Object object : c) {
				res |= remove(object);
			}
			return res;
		}
	}
	
	public boolean removeAll(UnitPool p) {
		boolean res = myBoats.removeAll(p.myBoats);
		res |= myBuilders.removeAll(p.myBuilders);
		res |= myCarriers.removeAll(p.myCarriers);
		res |= myFighters.removeAll(p.myFighters);
		return res | mySimples.removeAll(p.mySimples);
	}
	
	public boolean removeAll(FighterPool p) {
		return myFighters.removeAll(p);
	}
	
	public boolean removeAll(EntityPool p) {
		boolean res = myBoats.removeAll(p.units().myBoats);
		res |= myBuilders.removeAll(p.units().myBuilders);
		res |= myCarriers.removeAll(p.units().myCarriers);
		res |= myFighters.removeAll(p.units().myFighters);
		return res | mySimples.removeAll(p.units().mySimples);
	}
	
	@Override
	public void clear() {
		myBoats.clear();
		myBuilders.clear();
		myCarriers.clear();
		myFighters.clear();
		mySimples.clear();
	}
	
	public UnitPool listener() {
		if (listen == null) listen = new UnitPool(this, null);
		return listen;
	}

	public FighterPool fighters() {
		return listener().myFighters;
	}
	
}
