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

public class BuildingPool implements Set <Building>, Serializable {
	
	/** UID */
	private static final long serialVersionUID = -4933200713671647385L;
	
	
	private transient BuildingPool listen;
	
	
	private final HousPool myHouses;
	
	private final Set <Building> myPorts;
	private final Set <Building> myStorages;
	private final Set <Building> mySprings;
	private final Set <Building> myFarms;
	private final Set <Building> myMines;
	private final Set <Building> myWoodFarms;
	private final Set <Building> myBuildingPlaces;
	
	
	
	public BuildingPool() {
		myHouses = new HousPool();
		myPorts = new HashSet <Building>();
		myStorages = new HashSet <Building>();
		mySprings = new HashSet <Building>();
		myFarms = new HashSet <Building>();
		myMines = new HashSet <Building>();
		myWoodFarms = new HashSet <Building>();
		myBuildingPlaces = new HashSet <Building>();
	}
	
	public BuildingPool(BuildingPool copy) {
		myHouses = new HousPool(copy.myHouses);
		myPorts = new HashSet <Building>(copy.myPorts);
		myStorages = new HashSet <Building>(copy.myStorages);
		mySprings = new HashSet <Building>(copy.mySprings);
		myFarms = new HashSet <Building>(copy.myFarms);
		myMines = new HashSet <Building>(copy.myMines);
		myWoodFarms = new HashSet <Building>(copy.myWoodFarms);
		myBuildingPlaces = new HashSet <Building>(copy.myBuildingPlaces);
	}
	
	private BuildingPool(BuildingPool copy, Void listener) {
		listen = this;
		myHouses = copy.myHouses.listener();
		myPorts = Collections.unmodifiableSet(copy.myPorts);
		myStorages = Collections.unmodifiableSet(copy.myStorages);
		mySprings = Collections.unmodifiableSet(copy.mySprings);
		myFarms = Collections.unmodifiableSet(copy.myFarms);
		myMines = Collections.unmodifiableSet(copy.myMines);
		myWoodFarms = Collections.unmodifiableSet(copy.myWoodFarms);
		myBuildingPlaces = Collections.unmodifiableSet(copy.myBuildingPlaces);
	}
	
	
	
	@Override
	public int size() {
		long s = (long) myFarms.size() + (long) myHouses.size() + (long) myMines.size() + (long) myPorts.size() + (long) mySprings.size() + (long) myStorages.size() + (long) myWoodFarms.size()
				+ (long) myBuildingPlaces.size();
		return (int) Math.min(s, Integer.MAX_VALUE);
	}
	
	@Override
	public boolean isEmpty() {
		return myFarms.isEmpty() && myHouses.isEmpty() && myMines.isEmpty() && myPorts.isEmpty() && mySprings.isEmpty() && myStorages.isEmpty() && myWoodFarms.isEmpty() && myBuildingPlaces.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Building) return contains((Building) o);
		else return false;
	}
	
	public boolean contains(Building o) {
		switch (o.type) {
		case buildplace:
			return myBuildingPlaces.contains(o);
		case farm:
			return myFarms.contains(o);
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			return myHouses.contains(o);
		case mine:
			return myMines.contains(o);
		case spring:
			return mySprings.contains(o);
		case storage:
			return myStorages.contains(o);
		case woodFarm:
			return myWoodFarms.contains(o);
		default:
			return false;
		}
	}
	
	@Override
	public Iterator <Building> iterator() {
		return new Iterator <Building>() {
			
			int                           s = 0;
			Iterator <? extends Building> i = myBuildingPlaces.iterator();
			
			@Override
			public boolean hasNext() {
				boolean n = i.hasNext();
				if ( !n) switch (s) {
				case 0:
					s ++ ;
					i = myFarms.iterator();
					n = i.hasNext();
					if (n) break;
				case 1:
					s ++ ;
					i = myHouses.iterator();
					n = i.hasNext();
					if (n) break;
				case 2:
					s ++ ;
					i = myMines.iterator();
					n = i.hasNext();
					if (n) break;
				case 3:
					s ++ ;
					i = mySprings.iterator();
					n = i.hasNext();
					if (n) break;
				case 4:
					s ++ ;
					i = myStorages.iterator();
					n = i.hasNext();
					if (n) break;
				case 5:
					s ++ ;
					i = myWoodFarms.iterator();
					n = i.hasNext();
				}
				return n;
			}
			
			@Override
			public Building next() {
				hasNext();
				return i.next();
			}
			
		};
	}
	
	@Override
	public Object[] toArray() {
		Object[] o = new Object[size()];
		int i = 0;
		for (Building object : this) {
			o[i ++ ] = object;
		}
		return o;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int s = size();
		if (s != a.length) a = (T[]) Array.newInstance(a.getClass().getComponentType(), s);
		Object[] o = a;
		int i = 0;
		for (Building object : this) {
			o[i ++ ] = object;
		}
		return (T[]) a;
	}
	
	@Override
	public boolean add(Building e) {
		switch (e.type) {
		case buildplace:
			return myBuildingPlaces.add(e);
		case farm:
			return myFarms.add(e);
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			return myHouses.add((HouseBuilding) e);
		case mine:
			return myMines.add(e);
		case spring:
			return mySprings.add(e);
		case storage:
			return myStorages.add(e);
		case woodFarm:
			return myWoodFarms.add(e);
		default:
			throw new IllegalArgumentException("illegal build type");
		}
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Building) return remove((Building) o);
		return false;
	}
	
	public boolean remove(Building b) {
		switch (b.type) {
		case buildplace:
			return myBuildingPlaces.remove(b);
		case farm:
			return myFarms.remove(b);
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			return myHouses.remove(b);
		case mine:
			return myMines.remove(b);
		case spring:
			return mySprings.remove(b);
		case storage:
			return myStorages.remove(b);
		case woodFarm:
			return myWoodFarms.remove(b);
		default:
			return false;
		}
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			if ( !myBuildingPlaces.containsAll(p.myBuildingPlaces)) return false;
			if ( !myFarms.containsAll(p.myFarms)) return false;
			if ( !myHouses.containsAll(p.myHouses)) return false;
			if ( !myMines.containsAll(p.myMines)) return false;
			if ( !myPorts.containsAll(p.myPorts)) return false;
			if ( !mySprings.containsAll(p.mySprings)) return false;
			if ( !myStorages.containsAll(p.myStorages)) return false;
			return myWoodFarms.containsAll(p.myWoodFarms);
		} else if (c instanceof HousPool) {
			return myHouses.containsAll((HousPool) c);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			if ( !myBuildingPlaces.containsAll(p.buildings().myBuildingPlaces)) return false;
			if ( !myFarms.containsAll(p.buildings().myFarms)) return false;
			if ( !myHouses.containsAll(p.buildings().myHouses)) return false;
			if ( !myMines.containsAll(p.buildings().myMines)) return false;
			if ( !myPorts.containsAll(p.buildings().myPorts)) return false;
			if ( !mySprings.containsAll(p.buildings().mySprings)) return false;
			if ( !myStorages.containsAll(p.buildings().myStorages)) return false;
			return myWoodFarms.containsAll(p.buildings().myWoodFarms);
		} else {
			for (Object object : c) {
				if ( !contains(object)) return false;
			}
			return true;
		}
	}
	
	public boolean containsAll(BuildingPool p) {
		if ( !myBuildingPlaces.containsAll(p.myBuildingPlaces)) return false;
		if ( !myFarms.containsAll(p.myFarms)) return false;
		if ( !myHouses.containsAll(p.myHouses)) return false;
		if ( !myMines.containsAll(p.myMines)) return false;
		if ( !myPorts.containsAll(p.myPorts)) return false;
		if ( !mySprings.containsAll(p.mySprings)) return false;
		if ( !myStorages.containsAll(p.myStorages)) return false;
		return myWoodFarms.containsAll(p.myWoodFarms);
	}
	
	public boolean containsAll(HousPool c) {
		return myHouses.containsAll(c);
	}
	
	public boolean containsAll(EntityPool p) {
		if ( !myBuildingPlaces.containsAll(p.buildings().myBuildingPlaces)) return false;
		if ( !myFarms.containsAll(p.buildings().myFarms)) return false;
		if ( !myHouses.containsAll(p.buildings().myHouses)) return false;
		if ( !myMines.containsAll(p.buildings().myMines)) return false;
		if ( !myPorts.containsAll(p.buildings().myPorts)) return false;
		if ( !mySprings.containsAll(p.buildings().mySprings)) return false;
		if ( !myStorages.containsAll(p.buildings().myStorages)) return false;
		return myWoodFarms.containsAll(p.buildings().myWoodFarms);
	}
	
	@Override
	public boolean addAll(Collection <? extends Building> c) {
		if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			boolean res = myBuildingPlaces.addAll(p.myBuildingPlaces);
			res |= myFarms.addAll(p.myFarms);
			res |= myHouses.addAll(p.myHouses);
			res |= myMines.addAll(p.myMines);
			res |= myPorts.addAll(p.myPorts);
			res |= mySprings.addAll(p.mySprings);
			res |= myStorages.addAll(p.myStorages);
			return res | myWoodFarms.addAll(p.myWoodFarms);
		} else if (c instanceof HousPool) {
			return myHouses.addAll((HousPool) c);
		} else {
			boolean res = false;
			for (Building building : c) {
				res |= add(building);
			}
			return res;
		}
	}
	
	public boolean addAll(BuildingPool p) {
		boolean res = myBuildingPlaces.addAll(p.myBuildingPlaces);
		res |= myFarms.addAll(p.myFarms);
		res |= myHouses.addAll(p.myHouses);
		res |= myMines.addAll(p.myMines);
		res |= myPorts.addAll(p.myPorts);
		res |= mySprings.addAll(p.mySprings);
		res |= myStorages.addAll(p.myStorages);
		return res | myWoodFarms.addAll(p.myWoodFarms);
	}
	
	public boolean addAll(HousPool p) {
		return myHouses.addAll(p);
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			boolean res = myBuildingPlaces.retainAll(p.myBuildingPlaces);
			res |= myFarms.retainAll(p.myFarms);
			res |= myHouses.retainAll(p.myHouses);
			res |= myMines.retainAll(p.myMines);
			res |= myPorts.retainAll(p.myPorts);
			res |= mySprings.retainAll(p.mySprings);
			res |= myStorages.retainAll(p.myStorages);
			return res | myWoodFarms.retainAll(p.myWoodFarms);
		} else if (c instanceof HousPool) {
			return myHouses.retainAll((HousPool) c);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			boolean res = myBuildingPlaces.retainAll(p.buildings().myBuildingPlaces);
			res |= myFarms.retainAll(p.buildings().myFarms);
			res |= myHouses.retainAll(p.buildings().myHouses);
			res |= myMines.retainAll(p.buildings().myMines);
			res |= myPorts.retainAll(p.buildings().myPorts);
			res |= mySprings.retainAll(p.buildings().mySprings);
			res |= myStorages.retainAll(p.buildings().myStorages);
			return res | myWoodFarms.retainAll(p.buildings().myWoodFarms);
		} else {
			List <Object> builldingPlaces = new ArrayList <>(), farms = new ArrayList <>(), houses = new ArrayList <>(), mines = new ArrayList <>(), ports = new ArrayList <>(), springs = new ArrayList <>(),
					storages = new ArrayList <>(), woodFarms = new ArrayList <>();
			myBuildingPlaces.forEach(o -> {
				if ( !c.contains(o)) builldingPlaces.add(o);
			});
			myFarms.forEach(o -> {
				if ( !c.contains(o)) farms.add(o);
			});
			myHouses.forEach(o -> {
				if ( !c.contains(o)) houses.add(o);
			});
			myMines.forEach(o -> {
				if ( !c.contains(o)) mines.add(o);
			});
			myPorts.forEach(o -> {
				if ( !c.contains(o)) ports.add(o);
			});
			mySprings.forEach(o -> {
				if ( !c.contains(o)) springs.add(o);
			});
			myStorages.forEach(o -> {
				if ( !c.contains(o)) storages.add(o);
			});
			myWoodFarms.forEach(o -> {
				if ( !c.contains(o)) woodFarms.add(o);
			});
			builldingPlaces.forEach(o -> myBuildingPlaces.remove(o));
			farms.forEach(o -> myFarms.remove(o));
			houses.forEach(o -> myHouses.remove(o));
			mines.forEach(o -> myMines.remove(o));
			ports.forEach(o -> myPorts.remove(o));
			springs.forEach(o -> mySprings.remove(o));
			storages.forEach(o -> myStorages.remove(o));
			woodFarms.forEach(o -> myWoodFarms.remove(o));
			return ! (builldingPlaces.isEmpty() && farms.isEmpty() && houses.isEmpty() && mines.isEmpty() && ports.isEmpty() && springs.isEmpty() && storages.isEmpty() && woodFarms.isEmpty());
		}
	}
	
	public boolean retainAll(BuildingPool p) {
		boolean res = myBuildingPlaces.retainAll(p.myBuildingPlaces);
		res |= myFarms.retainAll(p.myFarms);
		res |= myHouses.retainAll(p.myHouses);
		res |= myMines.retainAll(p.myMines);
		res |= myPorts.retainAll(p.myPorts);
		res |= mySprings.retainAll(p.mySprings);
		res |= myStorages.retainAll(p.myStorages);
		return res | myWoodFarms.retainAll(p.myWoodFarms);
	}
	
	public boolean retainAll(HousPool c) {
		return myHouses.retainAll(c);
	}
	
	public boolean retainAll(EntityPool p) {
		boolean res = myBuildingPlaces.retainAll(p.buildings().myBuildingPlaces);
		res |= myFarms.retainAll(p.buildings().myFarms);
		res |= myHouses.retainAll(p.buildings().myHouses);
		res |= myMines.retainAll(p.buildings().myMines);
		res |= myPorts.retainAll(p.buildings().myPorts);
		res |= mySprings.retainAll(p.buildings().mySprings);
		res |= myStorages.retainAll(p.buildings().myStorages);
		return res | myWoodFarms.retainAll(p.buildings().myWoodFarms);
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		if (c instanceof BuildingPool) {
			BuildingPool p = (BuildingPool) c;
			boolean res = myBuildingPlaces.removeAll(p.myBuildingPlaces);
			res |= myFarms.removeAll(p.myFarms);
			res |= myHouses.removeAll(p.myHouses);
			res |= myMines.removeAll(p.myMines);
			res |= myPorts.removeAll(p.myPorts);
			res |= mySprings.removeAll(p.mySprings);
			res |= myStorages.removeAll(p.myStorages);
			return res | myWoodFarms.removeAll(p.myWoodFarms);
		} else if (c instanceof HousPool) {
			return myHouses.removeAll((HousPool) c);
		} else if (c instanceof EntityPool) {
			EntityPool p = (EntityPool) c;
			boolean res = myBuildingPlaces.removeAll(p.buildings().myBuildingPlaces);
			res |= myFarms.removeAll(p.buildings().myFarms);
			res |= myHouses.removeAll(p.buildings().myHouses);
			res |= myMines.removeAll(p.buildings().myMines);
			res |= myPorts.removeAll(p.buildings().myPorts);
			res |= mySprings.removeAll(p.buildings().mySprings);
			res |= myStorages.removeAll(p.buildings().myStorages);
			return res | myWoodFarms.removeAll(p.buildings().myWoodFarms);
		} else {
			boolean res = false;
			for (Object object : c) {
				res |= remove(object);
			}
			return res;
		}
	}
	
	public boolean removeAll(BuildingPool p) {
		boolean res = myBuildingPlaces.removeAll(p.myBuildingPlaces);
		res |= myFarms.removeAll(p.myFarms);
		res |= myHouses.removeAll(p.myHouses);
		res |= myMines.removeAll(p.myMines);
		res |= myPorts.removeAll(p.myPorts);
		res |= mySprings.removeAll(p.mySprings);
		res |= myStorages.removeAll(p.myStorages);
		return res | myWoodFarms.removeAll(p.myWoodFarms);
	}
	
	public boolean removeAll(HousPool c) {
		return myHouses.removeAll(c);
	}
	
	public boolean removeAll(EntityPool p) {
		boolean res = myBuildingPlaces.removeAll(p.buildings().myBuildingPlaces);
		res |= myFarms.removeAll(p.buildings().myFarms);
		res |= myHouses.removeAll(p.buildings().myHouses);
		res |= myMines.removeAll(p.buildings().myMines);
		res |= myPorts.removeAll(p.buildings().myPorts);
		res |= mySprings.removeAll(p.buildings().mySprings);
		res |= myStorages.removeAll(p.buildings().myStorages);
		return res | myWoodFarms.removeAll(p.buildings().myWoodFarms);
	}
	
	@Override
	public void clear() {
		myBuildingPlaces.clear();
		myFarms.clear();
		myHouses.clear();
		myMines.clear();
		myPorts.clear();
		mySprings.clear();
		myStorages.clear();
		myWoodFarms.clear();
	}
	
	
	public BuildingPool listener() {
		if (listen == null) listen = new BuildingPool(this, null);
		return listen;
	}
	
	public HousPool housesListner() {
		return listener().myHouses;
	}
	
	public HousPool houses() {
		return myHouses;
	}
	
}
