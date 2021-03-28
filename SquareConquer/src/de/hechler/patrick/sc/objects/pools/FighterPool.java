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
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.objects.Unit;

public class FighterPool implements Set <Unit>, Serializable {
	
	/** UID */
	private static final long serialVersionUID = 5146285562241342671L;
	
	private transient FighterPool listen;
	
	private final Set <Unit> myBows;
	private final Set <Unit> myMeeles;
	private final Set <Unit> myWarBoats;
	
	
	
	public FighterPool() {
		myBows = new HashSet <Unit>();
		myMeeles = new HashSet <Unit>();
		myWarBoats = new HashSet <Unit>();
	}
	
	public FighterPool(FighterPool copy) {
		myBows = new HashSet <Unit>(copy.myBows);
		myMeeles = new HashSet <Unit>(copy.myMeeles);
		myWarBoats = new HashSet <Unit>(copy.myWarBoats);
	}
	
	private FighterPool(FighterPool copy, Void onlyListen) {
		listen = this;
		myBows = Collections.unmodifiableSet(copy.myBows);
		myMeeles = Collections.unmodifiableSet(copy.myMeeles);
		myWarBoats = Collections.unmodifiableSet(copy.myWarBoats);
	}
	
	
	
	@Override
	public int size() {
		long s = (long) myBows.size() + (long) myMeeles.size() + (long) myWarBoats.size();
		return (int) Math.min(s, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean isEmpty() {
		return myBows.isEmpty() && myMeeles.isEmpty() && myWarBoats.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if ( ! (o instanceof Unit)) return false;
		Unit u = (Unit) o;
		switch (u.type) {
		case bow:
			return myBows.contains(u);
		case fightingBoat:
			return myWarBoats.contains(u);
		case meele:
			return myMeeles.contains(u);
		default:
			return false;
		}
	}
	
	public boolean contains(Unit u) {
		switch (u.type) {
		case bow:
			return myBows.contains(u);
		case fightingBoat:
			return myWarBoats.contains(u);
		case meele:
			return myMeeles.contains(u);
		default:
			return false;
		}
	}
	
	@Override
	public Iterator <Unit> iterator() {
		return new Iterator <Unit>() {
			
			int                       state = 0;
			Iterator <? extends Unit> i     = myBows.iterator();
			
			@Override
			public boolean hasNext() {
				boolean n = i.hasNext();
				if ( !n) switch (state) {
				case 0:
					state ++ ;
					i = myMeeles.iterator();
					n = i.hasNext();
					if (n) break;
				case 1:
					state ++ ;
					i = myWarBoats.iterator();
					n = i.hasNext();
					break;
				default:
					break;
				}
				return n;
			}
			
			@Override
			public Unit next() {
				if ( !i.hasNext()) switch (state) {
				case 0:
					state ++ ;
					i = myMeeles.iterator();
					if (i.hasNext()) break;
				case 1:
					state ++ ;
					i = myWarBoats.iterator();
					break;
				default:
					break;
				}
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		int i = 0;
		for (Object n : this) {
			o[i ++ ] = n;
		}
		return o;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int s = size();
		Class <?> comp = a.getClass().getComponentType();
		if (s != a.length) a = (T[]) Array.newInstance(comp, s);
		if (comp == Entity.class) {
			Entity[] o = (Entity[]) a;
			int i = 0;
			for (Entity n : this) {
				o[i ++ ] = n;
			}
		} else if (comp == Unit.class) {
			Unit[] o = (Unit[]) a;
			int i = 0;
			for (Unit n : this) {
				o[i ++ ] = n;
			}
		} else if (comp == MovableEntity.class) {
			MovableEntity[] o = (MovableEntity[]) a;
			int i = 0;
			for (Unit n : this) {
				o[i ++ ] = n;
			}
		} else if (comp == Object.class) {
			Object[] o = (Object[]) a;
			int i = 0;
			for (Unit n : this) {
				o[i ++ ] = n;
			}
		} else {
			int i = 0;
			for (Unit n : this) {
				a[i ++ ] = (T) n;
			}
		}
		return a;
	}
	
	@Override
	public boolean add(Unit e) {
		switch (e.type) {
		case meele:
			return myMeeles.add(e);
		case bow:
			return myBows.add(e);
		case fightingBoat:
			return myWarBoats.add(e);
		default:
			throw new IllegalArgumentException("illegal unit type: " + e.type.name() + " of unit: '" + e + "'");
		}
	}
	
	@Override
	public boolean remove(Object o) {
		if ( ! (o instanceof Unit)) return false;
		switch ( ((Unit) o).type) {
		case meele:
			return myMeeles.remove(o);
		case bow:
			return myBows.remove(o);
		case fightingBoat:
			return myWarBoats.remove(o);
		default:
			throw new IllegalArgumentException("illegal unit type: " + ((Unit) o).type.name() + " of unit: '" + o + "'");
		}
	}
	
	public boolean remove(Unit o) {
		switch (o.type) {
		case meele:
			return myMeeles.remove(o);
		case bow:
			return myBows.remove(o);
		case fightingBoat:
			return myWarBoats.remove(o);
		default:
			throw new IllegalArgumentException("illegal unit type: " + o.type.name() + " of unit: '" + o + "'");
		}
	}
	
	public boolean containsAll(Collection <?> c) {
		if (c instanceof FighterPool) {
			FighterPool fp = (FighterPool) c;
			if ( !myMeeles.containsAll(fp.myMeeles)) return false;
			if ( !myBows.containsAll(fp.myBows)) return false;
			return myWarBoats.containsAll(fp.myWarBoats);
		} else if (c instanceof UnitPool) {
			UnitPool up = (UnitPool) c;
			if ( !myMeeles.containsAll(up.fighters().myMeeles)) return false;
			if ( !myBows.containsAll(up.fighters().myBows)) return false;
			return myWarBoats.containsAll(up.fighters().myWarBoats);
		} else if (c instanceof EntityPool) {
			EntityPool ep = (EntityPool) c;
			if ( !myMeeles.containsAll(ep.units().fighters().myMeeles)) return false;
			if ( !myBows.containsAll(ep.units().fighters().myBows)) return false;
			return myWarBoats.containsAll(ep.units().fighters().myWarBoats);
		} else {
			for (Object o : c) {
				if ( !contains(o)) return false;
			}
			return true;
		}
	}
	
	public boolean containsAll(FighterPool fp) {
		if ( !myMeeles.containsAll(fp.myMeeles)) return false;
		if ( !myBows.containsAll(fp.myBows)) return false;
		return myWarBoats.containsAll(fp.myWarBoats);
	}
	
	public boolean containsAll(UnitPool up) {
		if ( !myMeeles.containsAll(up.fighters().myMeeles)) return false;
		if ( !myBows.containsAll(up.fighters().myBows)) return false;
		return myWarBoats.containsAll(up.fighters().myWarBoats);
	}
	
	public boolean containsAll(EntityPool ep) {
		if ( !myMeeles.containsAll(ep.units().fighters().myMeeles)) return false;
		if ( !myBows.containsAll(ep.units().fighters().myBows)) return false;
		return myWarBoats.containsAll(ep.units().fighters().myWarBoats);
	}
	
	@Override
	public boolean addAll(Collection <? extends Unit> c) {
		boolean res = false;
		if (c instanceof FighterPool) {
			FighterPool fp = (FighterPool) c;
			res |= myMeeles.addAll(fp.myMeeles);
			res |= myBows.addAll(fp.myBows);
			res |= myWarBoats.addAll(fp.myWarBoats);
		} else {
			for (Unit o : c) {
				res |= add(o);
			}
		}
		return res;
	}
	
	public boolean addAll(FighterPool fp) {
		boolean res = myMeeles.addAll(fp.myMeeles);
		res |= myBows.addAll(fp.myBows);
		res |= myWarBoats.addAll(fp.myWarBoats);
		return res;
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		boolean res;
		if (c instanceof FighterPool) {
			FighterPool p = (FighterPool) c;
			res = myMeeles.retainAll(p.myMeeles);
			res |= myBows.retainAll(p.myBows);
			res |= myWarBoats.retainAll(p.myWarBoats);
		} else if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			res = myMeeles.retainAll(p.fighters().myMeeles);
			res |= myBows.retainAll(p.fighters().myBows);
			res |= myWarBoats.retainAll(p.fighters().myWarBoats);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			res = myMeeles.retainAll(p.units().fighters().myMeeles);
			res |= myBows.retainAll(p.units().fighters().myBows);
			res |= myWarBoats.retainAll(p.units().fighters().myWarBoats);
		} else {
			if ( ! (c instanceof Set)) c = new HashSet <>(c);
			List <Unit> b = new ArrayList <Unit>(), m = new ArrayList <Unit>(), wb = new ArrayList <>();
			for (Unit unit : myBows) {
				if ( !c.contains(unit)) b.add(unit);
			}
			res = !b.isEmpty();
			for (Unit unit : myMeeles) {
				if ( !c.contains(unit)) m.add(unit);
			}
			res |= !m.isEmpty();
			for (Unit unit : myWarBoats) {
				if ( !c.contains(unit)) wb.add(unit);
			}
			res |= !wb.isEmpty();
			b.forEach(u -> myBows.remove(u));
			m.forEach(u -> myMeeles.remove(u));
			wb.forEach(u -> myWarBoats.remove(u));
		}
		return res;
	}
	
	public boolean retainAll(FighterPool p) {
		boolean res = myMeeles.retainAll(p.myMeeles);
		res |= myBows.retainAll(p.myBows);
		return res | myWarBoats.retainAll(p.myWarBoats);
	}
	
	public boolean retainAll(UnitPool p) {
		boolean res = myMeeles.retainAll(p.fighters().myMeeles);
		res |= myBows.retainAll(p.fighters().myBows);
		return res | myWarBoats.retainAll(p.fighters().myWarBoats);
	}
	
	public boolean retainAll(EntityPool p) {
		boolean res = myMeeles.retainAll(p.units().fighters().myMeeles);
		res |= myBows.retainAll(p.units().fighters().myBows);
		return res | myWarBoats.retainAll(p.units().fighters().myWarBoats);
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		boolean res;
		if (c instanceof FighterPool) {
			FighterPool p = (FighterPool) c;
			res = myBows.removeAll(p.myBows);
			res |= myMeeles.removeAll(p.myMeeles);
			res |= myWarBoats.removeAll(p.myWarBoats);
		} else if (c instanceof UnitPool) {
			UnitPool p = (UnitPool) c;
			res = myBows.removeAll(p.fighters().myBows);
			res |= myMeeles.removeAll(p.fighters().myMeeles);
			res |= myWarBoats.removeAll(p.fighters().myWarBoats);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			res = myBows.removeAll(p.units().fighters().myBows);
			res |= myMeeles.removeAll(p.units().fighters().myMeeles);
			res |= myWarBoats.removeAll(p.units().fighters().myWarBoats);
		} else {
			res = false;
			for (Object o : c) {
				res |= remove(o);
			}
		}
		return res;
	}
	
	public boolean removeAll(FighterPool p) {
		boolean res = myBows.removeAll(p.myBows);
		res |= myMeeles.removeAll(p.myMeeles);
		return res | myWarBoats.removeAll(p.myWarBoats);
	}
	
	public boolean removeAll(UnitPool p) {
		boolean res = myBows.removeAll(p.fighters().myBows);
		res |= myMeeles.removeAll(p.fighters().myMeeles);
		return res | myWarBoats.removeAll(p.fighters().myWarBoats);
	}
	
	public boolean removeAll(EntityPool p) {
		boolean res = myBows.removeAll(p.units().fighters().myBows);
		res |= myMeeles.removeAll(p.units().fighters().myMeeles);
		return res | myWarBoats.removeAll(p.units().fighters().myWarBoats);
	}
	
	@Override
	public void clear() {
		myBows.clear();
		myMeeles.clear();
		myWarBoats.clear();
	}
	
	public FighterPool listener() {
		if (listen == null) listen = new FighterPool(this, null);
		return listen;
	}
	
}
