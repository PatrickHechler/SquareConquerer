package de.hechler.patrick.sc.objects;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Position;

public class StorageBuilding extends Building {
	
	protected final Map <Resources, Int> store;
	
	protected int capacity;
	protected int blocked;
	
	
	
	public StorageBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity, Resources... storable) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, capacity);
	}
	
	public StorageBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity, Resources... storable) {
		this(pos, canExsistOn, type, totalActions, 0, map(storable), capacity);
	}
	
	public StorageBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, Map <Resources, ? extends Number> store, int capacity) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, remainingActions, store, capacity);
	}
	
	public StorageBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remaining, Map <Resources, ? extends Number> store, int capacity) {
		this(0, pos, canExsistOn, type, totalActions, remaining);
	}
	
	public StorageBuilding(int sight, Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity, Resources... storable) {
		this(sight, new UnchangeablePosition(pos), canExsistOn, type, totalActions, capacity);
	}
	
	public StorageBuilding(int sight, UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity, Resources... storable) {
		this(sight, pos, canExsistOn, type, totalActions, 0, map(storable), capacity);
	}
	
	protected final static Map <Resources, ? extends Number> map(Resources[] storable) {
		Map <Resources, Integer> map = new EnumMap <Resources, Integer>(Resources.class);
		for (Resources r : storable) {
			map.put(r, 0);
		}
		return map;
	}
	
	public StorageBuilding(int sight, Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, Map <Resources, ? extends Number> store, int capacity) {
		this(sight, new UnchangeablePosition(pos), canExsistOn, type, totalActions, remainingActions, store, capacity);
	}
	
	public StorageBuilding(int sight, UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remaining, Map <Resources, ? extends Number> store, int capacity) {
		super(sight, pos, canExsistOn, type, totalActions, remaining);
		this.capacity = capacity;
		this.store = new EnumMap <Resources, StorageBuilding.Int>(Resources.class);
		store.forEach((k, v) -> {
			int val = v.intValue();
			this.store.put(k, new Int(val));
			this.blocked += val;
		});
	}
	
	
	
	public int capacity() {
		return capacity;
	}
	
	public int freeSpace() {
		return capacity - blocked;
	}
	
	public int blockedSpace() {
		return blocked;
	}
	
	/**
	 * returns the count of stored elements of the {@link Resources} {@code resource}.<br>
	 * if the {@link Resources} can't be stored by this {@link StorageBuilding}, it will return {@code -1}
	 * 
	 * @param resource
	 *            the {@link Resources} to be counted
	 * @return the count of stored elements of the {@link Resources} {@code resource}, but if the {@link Resources} can't be stored by this {@link StorageBuilding} it will return {@code -1}
	 */
	public int contains(Resources resource) {
		Int i = store.get(resource);
		if (i == null) return -1;
		else return i.value;
	}
	
	public void store(Resources resource, int cnt) throws IllegalStateException, IllegalArgumentException {
		if (cnt + blocked > capacity) {
			throw new IllegalStateException("cnt + blocked > capaciy cnt=" + cnt + " blocked=" + blocked + " capacity=" + capacity);
		}
		if ( !store.containsKey(resource)) {
			throw new IllegalArgumentException("can't store resource '" + resource + "' store=" + store);
		}
		store.get(resource).value += cnt;
		blocked += cnt;
	}
	
	public void remove(Resources resource, int cnt) throws IllegalStateException, IllegalArgumentException {
		Int i = store.get(resource);
		if (i == null) {
			throw new IllegalArgumentException("can't store or remove the resource: " + resource);
		} else if (i.value < cnt) {
			throw new IllegalStateException("does not contain enught resources: contain: " + i.value + " should remove: " + cnt + " resource='" + resource + '\'');
		}
	}
	
	/**
	 * returns an unmodifiable version of the {@link #store}
	 * 
	 * @return an unmodifiable version of the {@link #store}
	 */
	public Map <Resources, Int> getStore() {
		return Collections.unmodifiableMap(store);
	}
	
	protected class Int extends Number {
		
		/** UID */
		private static final long serialVersionUID = -4869900778040743043L;
		
		public int value;
		
		public Int(int value) {
			this.value = value;
		}
		
		@Override
		public int intValue() {
			return value;
		}
		
		@Override
		public long longValue() {
			return (long) value;
		}
		
		@Override
		public float floatValue() {
			return (float) value;
		}
		
		@Override
		public double doubleValue() {
			return (double) value;
		}
		
	}
	
}
