package de.hechler.patrick.sc.objects.pools;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.objects.Building;
import de.hechler.patrick.sc.objects.Unit;

public class EntityPool implements Set <Entity>, Serializable {
	
	/** UID */
	private static final long serialVersionUID = -6092716577688533969L;
	
	private transient EntityPool listen;
	
	private final UnitPool     myUnits;
	private final BuildingPool myBuildings;
	
	
	
	public EntityPool() {
		myUnits = new UnitPool();
		myBuildings = new BuildingPool();
	}
	
	public EntityPool(EntityPool copy) {
		myUnits = new UnitPool(copy.myUnits);
		myBuildings = new BuildingPool(copy.myBuildings);
	}
	
	public EntityPool(EntityPool copy, Void onlyListen) {
		listen = this;
		myUnits = copy.myUnits.listener();
		myBuildings = copy.myBuildings.listener();
	}
	
	
	
	@Override
	public int size() {
		int s = myUnits.size();
		s += myBuildings.size();
		return s;
	}
	
	@Override
	public boolean isEmpty() {
		return myUnits.isEmpty() && myUnits.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Entity) return contains((Entity) o);
		return false;
	}
	
	public boolean contains(Entity e) {
		if (e.isMovable()) {
			return myUnits.contains((Unit) e);
		} else {
			return myBuildings.contains((Building) e);
		}
	}
	
	@Override
	public Iterator <Entity> iterator() {
		return new Iterator <Entity>() {
			
			Iterator <? extends Entity> i;
			boolean                     s;
			
			@Override
			public boolean hasNext() {
				boolean ihn = i.hasNext();
				if ( !ihn && !s) {
					s = true;
					i = myBuildings.iterator();
					ihn = i.hasNext();
				}
				return ihn;
			}
			
			@Override
			public Entity next() {
				if ( !i.hasNext() && !s) {
					s = true;
					i = myBuildings.iterator();
				}
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		int i = 0;
		for (Entity e : this) {
			o[i ++ ] = e;
		}
		return o;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int s = size();
		Class <?> comp = a.getClass().getComponentType();
		if (a.length != s) a = (T[]) Array.newInstance(comp, s);
		if (comp == Entity.class) {
			Entity[] arr = (Entity[]) a;
			int i = 0;
			for (Entity e : this) {
				arr[i ++ ] = e;
			}
		} else if (comp == Object.class) {
			Object[] arr = (Object[]) a;
			int i = 0;
			for (Entity e : this) {
				arr[i ++ ] = e;
			}
		} else {
			int i = 0;
			for (Entity e : this) {
				a[i ++ ] = (T) e;
			}
		}
		return a;
	}
	
	@Override
	public boolean add(Entity e) {
		if (e.isMovable()) return myUnits.add((Unit) e);
		else return myBuildings.add((Building) e);
	}
	
	@Override
	public boolean remove(Object o) {
		if ( ! (o instanceof Entity)) return false;
		if ( ((Entity) o).isMovable()) return myUnits.remove((Unit) o);
		else return myBuildings.remove((Building) o);
	}
	
	public boolean remove(Entity o) {
		if (o.isMovable()) return myUnits.remove((Unit) o);
		else return myBuildings.remove((Building) o);
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof EntityPool) {
			EntityPool ep = (EntityPool) c;
			return myUnits.containsAll(ep.myUnits) && myBuildings.containsAll(ep.myBuildings);
		} else if (c instanceof UnitPool) {
			return myUnits.containsAll((UnitPool) c);
		} else if (c instanceof BuildingPool) {
			return myBuildings.containsAll((BuildingPool) c);
		} else {
			for (Object obj : c) {
				if ( !contains(obj)) return false;
			}
			return true;
		}
	}
	
	public boolean containsAll(EntityPool ep) {
		return myUnits.containsAll(ep.myUnits) && myBuildings.containsAll(ep.myBuildings);
	}
	
	public boolean containsAll(UnitPool c) {
		return myUnits.containsAll(c);
	}
	
	public boolean containsAll(BuildingPool c) {
		return myBuildings.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection <? extends Entity> c) {
		if (c instanceof EntityPool) {
			EntityPool ep = (EntityPool) c;
			boolean u = myUnits.addAll(ep.myUnits);
			return u | myBuildings.addAll(ep.myBuildings);
		} else if (c instanceof UnitPool) {
			return myUnits.addAll((UnitPool) c);
		} else if (c instanceof BuildingPool) {
			return myBuildings.addAll((BuildingPool) c);
		} else {
			boolean res = false;
			for (Entity e : c) {
				res |= add(e);
			}
			return res;
		}
	}
	
	public boolean addAll(EntityPool ep) {
		boolean u = myUnits.addAll(ep.myUnits);
		return u | myBuildings.addAll(ep.myBuildings);
	}
	
	public boolean addAll(UnitPool c) {
		return myUnits.addAll(c);
	}
	
	public boolean addAll(BuildingPool c) {
		return myBuildings.addAll(c);
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c instanceof EntityPool) {
			EntityPool ep = (EntityPool) c;
			boolean u = myUnits.retainAll(ep.myUnits);
			return u | myBuildings.retainAll(ep.myBuildings);
		} else if (c instanceof UnitPool) {
			return myUnits.retainAll((UnitPool) c);
		} else if (c instanceof BuildingPool) {
			return myBuildings.retainAll((BuildingPool) c);
		} else {
			List <Unit> u = new ArrayList <Unit>();
			List <Building> b = new ArrayList <Building>();
			for (Unit unit : myUnits) {
				if ( !c.contains(unit)) u.add(unit);
			}
			for (Building building : myBuildings) {
				if ( !c.contains(building)) b.add(building);
			}
			myUnits.removeAll(u);
			myBuildings.removeAll(b);
			return ! (u.isEmpty() && b.isEmpty());
		}
	}
	
	public boolean retainAll(EntityPool ep) {
		boolean u = myUnits.retainAll(ep.myUnits);
		return u | myBuildings.retainAll(ep.myBuildings);
	}
	
	public boolean retainAll(UnitPool c) {
		return myUnits.retainAll(c);
	}
	
	public boolean retainAll(BuildingPool c) {
		return myBuildings.retainAll(c);
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		if (c instanceof EntityPool) {
			EntityPool ep = (EntityPool) c;
			boolean u = myUnits.removeAll(ep.myUnits);
			return u | myBuildings.removeAll(ep.myBuildings);
		} else if (c instanceof UnitPool) {
			return myUnits.removeAll((UnitPool) c);
		} else if (c instanceof BuildingPool) {
			return myBuildings.removeAll((BuildingPool) c);
		} else {
			List <Unit> u = new ArrayList <Unit>();
			List <Building> b = new ArrayList <Building>();
			for (Unit unit : myUnits) {
				if (c.contains(unit)) u.add(unit);
			}
			for (Building building : myBuildings) {
				if (c.contains(building)) b.add(building);
			}
			myUnits.removeAll(u);
			myBuildings.removeAll(b);
			return ! (u.isEmpty() && b.isEmpty());
		}
	}
	
	@Override
	public void clear() {
		myBuildings.clear();
		myUnits.clear();
	}
	
	
	public EntityPool listener() {
		if (listen == null) listen = new EntityPool(this, null);
		return listen;
	}
	
	public UnitPool units() {
		return listener().myUnits;
	}
	
	public BuildingPool buildings() {
		return listener().myBuildings;
	}
	
}
