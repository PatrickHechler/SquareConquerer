//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.objects;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.IntFunction;

public class EnumIntMap<T extends Enum<?>> implements Map<T, Integer> {
	
	private final Class<T> cls;
	private final int[]    arr;
	
	public EnumIntMap(Class<T> cls) {
		if (!cls.isEnum()) {
			throw new IllegalArgumentException("the class is no enum class");
		}
		this.cls = cls;
		this.arr = new int[cls.getEnumConstants().length];
	}
	
	private EnumIntMap(Class<T> cls, int[] arr) {
		this.cls = cls;
		this.arr = arr;
	}
	
	/**
	 * return the current value
	 * 
	 * @param e the enum constant
	 * 
	 * @return the current value
	 */
	public int get(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		return this.arr[e.ordinal()];
	}
	
	/**
	 * set the value
	 * 
	 * @param e   the enum constant
	 * @param val the new value
	 */
	public void set(T e, int val) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		this.arr[e.ordinal()] = val;
	}
	
	/**
	 * increment and return the new value
	 * 
	 * @param e the enum constant
	 * 
	 * @return the new value
	 */
	public int inc(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		return ++this.arr[e.ordinal()];
	}
	
	public int addBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		int o = e.ordinal();
		this.arr[o] += val;
		return this.arr[o];
	}
	
	/**
	 * decrement and return the new value
	 * 
	 * @param e the enum constant
	 * 
	 * @return the new value
	 */
	public int dec(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		return --this.arr[e.ordinal()];
	}
	
	public int subBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("sub is not greather than zero: sub=" + val);
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		int o = e.ordinal();
		this.arr[o] -= val;
		return this.arr[o];
	}
	
	/**
	 * return the backing array of this map, changes to the array will be visible to
	 * this map and changes to the map will be visible to the array
	 * 
	 * @return the backing array
	 */
	public int[] array() {
		return this.arr;
	}
	
	@Override
	public int size() {
		return this.arr.length;
	}
	
	@Override
	public boolean isEmpty() { return this.arr.length == 0; }
	
	@Override
	public boolean containsKey(Object key) {
		return this.cls.isInstance(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		if (!(value instanceof Integer ival)) {
			return false;
		}
		int search = ival.intValue();
		for (int val : this.arr) {
			if (search == val) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Integer get(Object key) {
		if (!this.cls.isInstance(key)) {
			return null;
		}
		return Integer.valueOf(this.arr[((Enum<?>) key).ordinal()]);
	}
	
	@Override
	public Integer put(T key, Integer value) {
		int     o   = key.ordinal();
		Integer old = Integer.valueOf(this.arr[o]);
		this.arr[o] = value.intValue();
		return old;
	}
	
	@Override
	public Integer remove(Object key) {
		throw new UnsupportedOperationException("remove");
	}
	
	@Override
	public void putAll(Map<? extends T, ? extends Integer> m) {
		if (m instanceof EnumIntMap<?> eim) {
			if (this.cls != eim.cls) {
				throw new ClassCastException("can not cast from " + eim.cls + " to " + this.cls);
			}
			System.arraycopy(eim.arr, 0, this.arr, 0, this.arr.length);
			return;
		}
		for (Entry<? extends T, ?> e : m.entrySet()) {
			put(e.getKey(), (Integer) e.getValue());
		}
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear");
	}
	
	private Set<T> keySet;
	
	@Override
	public Set<T> keySet() { // this is stupid
		if (this.keySet == null) {
			this.keySet = Set.of(this.cls.getEnumConstants());
		}
		return this.keySet;
	}
	
	private Collection<Integer> values;
	
	@Override
	public Collection<Integer> values() {
		if (this.values == null) {
			this.values = new IntArrayList(this.arr);
		}
		return this.values;
	}
	
	private Set<Entry<T, Integer>> entrySet;
	
	@Override
	public Set<Entry<T, Integer>> entrySet() {
		if (this.entrySet == null) {
			this.entrySet = new EnumIntEntrySet();
		}
		return this.entrySet;
	}
	
	private class EnumIntEntrySet implements Set<Entry<T, Integer>> {
		
		private WeakReference<MyEntry>[] entries;
		
		@SuppressWarnings("unchecked")
		private MyEntry entry(int i) {
			MyEntry res;
			if (this.entries == null) {
				this.entries    = (WeakReference<MyEntry>[]) new WeakReference<?>[EnumIntMap.this.arr.length];
				res        = new MyEntry(i);
				this.entries[i] = new WeakReference<>(res);
			} else if (this.entries[i] == null) {
				res        = new MyEntry(i);
				this.entries[i] = new WeakReference<>(res);
			} else {
				res = this.entries[i].get();
				if (res == null) {
					res        = new MyEntry(i);
					this.entries[i] = new WeakReference<>(res);
				}
			}
			return res;
		}
		
		private class MyEntry implements Entry<T, Integer> {
			
			private final int index;
			private T         key;
			
			private MyEntry(int index) { this.index = index; }
			
			@Override
			public T getKey() {
				if (this.key == null) {
					this.key = EnumIntMap.this.cls.getEnumConstants()[this.index];
				}
				return this.key;
			}
			
			@Override
			public Integer getValue() { return Integer.valueOf(EnumIntMap.this.arr[this.index]); }
			
			@Override
			public Integer setValue(Integer value) {
				Integer old = Integer.valueOf(EnumIntMap.this.arr[this.index]);
				EnumIntMap.this.arr[this.index] = value.intValue();
				return old;
			}
			
			private int ival() {
				return EnumIntMap.this.arr[this.index];
			}
			
			private Class<T> cls() {
				return EnumIntMap.this.cls;
			}
			
			@Override
			public int hashCode() {
				return getKey().hashCode() ^ Integer.hashCode(EnumIntMap.this.arr[this.index]);
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
					return me.index == this.index && me.cls() == EnumIntMap.this.cls && me.ival() == EnumIntMap.this.arr[this.index];
				}
				if (!(obj instanceof Entry<?, ?> e2)) {
					return false;
				}
				Object ok = e2.getKey();
				if (!EnumIntMap.this.cls.isInstance(ok) || ((Enum<?>) ok).ordinal() != this.index) {
					return false;
				}
				Object ov = e2.getValue();
				return ov instanceof Integer val && val.intValue() == EnumIntMap.this.arr[this.index];
			}
			
		}
		
		@Override
		public int size() {
			return EnumIntMap.this.arr.length;
		}
		
		@Override
		public boolean isEmpty() { return EnumIntMap.this.arr.length == 0; }
		
		@Override
		public boolean contains(Object o) {
			if (o instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
				return EnumIntMap.this.cls == me.cls() && EnumIntMap.this.arr[me.index] == me.ival();
			}
			if (!(o instanceof Entry<?, ?> e)) {
				return false;
			}
			Object obj = e.getKey();
			if (!EnumIntMap.this.cls.isInstance(obj)) {
				return false;
			}
			Object val = e.getValue();
			if (!(val instanceof Integer ival)) {
				return false;
			}
			return EnumIntMap.this.arr[((Enum<?>) obj).ordinal()] == ival.intValue();
		}
		
		@Override
		public Iterator<Entry<T, Integer>> iterator() {
			return new Iterator<Entry<T, Integer>>() {
				
				private int index;
				
				@Override
				public boolean hasNext() {
					return this.index < EnumIntMap.this.arr.length;
				}
				
				@Override
				public Entry<T, Integer> next() {
					if (this.index >= EnumIntMap.this.arr.length) {
						throw new NoSuchElementException("no more elements");
					}
					return entry(this.index++);
				}
				
			};
		}
		
		@Override
		public Object[] toArray() {
			Object[] obj = new Object[EnumIntMap.this.arr.length];
			for (int i = 0; i < obj.length; i++) {
				obj[i] = entry(i);
			}
			return obj;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <A> A[] toArray(A[] a) {
			Class<?> ct = a.getClass().componentType();
			MyEntry.class.asSubclass(ct);
			if (a.length < EnumIntMap.this.arr.length) {
				a = (A[]) Array.newInstance(a.getClass().componentType(), EnumIntMap.this.arr.length);
			} else if (a.length > EnumIntMap.this.arr.length) {
				a[EnumIntMap.this.arr.length] = null;
			}
			for (int i = 0; i < a.length; i++) {
				a[i] = (A) entry(i);
			}
			return a;
		}
		
		@Override
		public <A> A[] toArray(IntFunction<A[]> generator) {
			return toArray(generator.apply(EnumIntMap.this.arr.length));
		}
		
		@Override
		public boolean add(Entry<T, Integer> e) {
			throw new UnsupportedOperationException("add");
		}
		
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("remove");
		}
		
		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object obj : c) {
				if (obj instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
					if (me.cls() != EnumIntMap.this.cls) return false;
					if (me.ival() != EnumIntMap.this.arr[me.index]) return false;
				} else if (!(obj instanceof Entry<?, ?> e)) {
					return false;
				} else {
					Object key = e.getKey();
					if (!EnumIntMap.this.cls.isInstance(key)) return false;
					Object val = e.getValue();
					if (!(val instanceof Integer ival)) return false;
					if (EnumIntMap.this.arr[((Enum<?>)key).ordinal()] != ival.intValue()) return false;
				}
			}
			return true;
		}
		
		@Override
		public boolean addAll(Collection<? extends Entry<T, Integer>> c) {
			throw new UnsupportedOperationException("add");
		}
		
		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("remove");
		}
		
		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("remove");
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException("clear");
		}
		
		private Class<T> cls() {
			return EnumIntMap.this.cls;
		}
		
		private int[] arr() {
			return EnumIntMap.this.arr;
		}
		
		@Override
		public int hashCode() { // I know that zero is a valid hash
			if (EnumIntMap.this.hash != 0) return EnumIntMap.this.hash;
			int sum  = 0;
			T[] vals = null;
			for (int i = 0; i < EnumIntMap.this.arr.length; i++) {
				if (this.entries[i] != null) {
					EnumIntMap<T>.EnumIntEntrySet.MyEntry e = this.entries[i].get();
					if (e != null) {
						sum += e.hashCode();
						continue;
					}
				}
				if (vals == null) {
					vals = EnumIntMap.this.cls.getEnumConstants();
				}
				sum += vals[i].hashCode() ^ Integer.hashCode(EnumIntMap.this.arr[i]);
			}
			EnumIntMap.this.hash = sum;
			return sum;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EnumIntMap<?>.EnumIntEntrySet s) {
				if (EnumIntMap.this.cls != s.cls()) return false;
				return Arrays.equals(EnumIntMap.this.arr, s.arr());
			}
			if (!(obj instanceof Set<?> s)) return false;
			if (s.size() != EnumIntMap.this.arr.length) return false;
			for (Object o : s) {
				if (!(o instanceof Entry<?, ?> e)) return false;
				if (o instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
					if (me.cls() != EnumIntMap.this.cls) return false;
					if (EnumIntMap.this.arr[me.index] != me.ival()) return false;
					continue;
				}
				Object k = e.getKey();
				Object v = e.getValue();
				if (k == null || v == null) return false;
				if (v.getClass() != Integer.class) return false;
				if (!EnumIntMap.this.cls.isInstance(k)) return false;
				if (EnumIntMap.this.arr[((Enum<?>) k).ordinal()] != ((Integer) v).intValue()) return false;
			} // false true result if the set allows multiple times the same entry or gets
				// modified
			return true;
		}
		
	}
	
	public EnumIntMap<T> copy() {
		return new EnumIntMap<>(this.cls, this.arr.clone());
	}
	
	private int hash;
	
	@Override
	public int hashCode() {
		// if the hash value is already calculated return it
		// if the entry set exists, let the set calculate the hash
		// :: reason: if the entry sets cache contains all values, there is no need to
		// create a new array
		// :: possible, because the maps hash code is effectively defined as the entry
		// sets hash code
		// otherwise calculate the value
		if (this.hash != 0) return this.hash;
		if (this.entrySet != null) {
			return this.entrySet.hashCode();
		}
		int sum  = 0;
		T[] vals = this.cls.getEnumConstants();
		for (int i = 0; i < this.arr.length; i++) {
			sum += vals[i].hashCode() ^ Integer.hashCode(this.arr[i]);
		}
		this.hash = sum;
		return sum;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnumIntMap<?> eim) {
			if (this.cls != eim.cls) return false;
			return Arrays.equals(this.arr, eim.arr);
		}
		if (!(obj instanceof Map<?, ?> m)) return false;
		if (m.size() != this.arr.length) return false;
		if (this.entrySet != null) {
			return this.entrySet.equals(m.entrySet());
		}
		T[] vals = this.cls.getEnumConstants();
		for (int i = 0; i < vals.length; i++) {
			@SuppressWarnings("unlikely-arg-type")
			Object val = m.get(vals[i]);
			if (!(val instanceof Integer ival)) return false;
			if (this.arr[i] != ival.intValue()) return false;
		}
		return true;
	}
	
}
