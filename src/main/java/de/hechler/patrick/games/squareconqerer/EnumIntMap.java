package de.hechler.patrick.games.squareconqerer;

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
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		return arr[e.ordinal()];
	}
	
	/**
	 * set the value
	 * 
	 * @param e   the enum constant
	 * @param val the new value
	 */
	public void set(T e, int val) {
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		arr[e.ordinal()] = val;
	}
	
	/**
	 * increment and return the new value
	 * 
	 * @param e the enum constant
	 * 
	 * @return the new value
	 */
	public int inc(T e) {
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		return ++arr[e.ordinal()];
	}
	
	public int add(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		int o = e.ordinal();
		arr[o] += val;
		return arr[o];
	}
	
	/**
	 * decrement and return the new value
	 * 
	 * @param e the enum constant
	 * 
	 * @return the new value
	 */
	public int dec(T e) {
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		return --arr[e.ordinal()];
	}
	
	public int sub(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		if (!cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + cls);
		}
		int o = e.ordinal();
		arr[o] -= val;
		return arr[o];
	}
	
	/**
	 * return the backing array of this map, changes to the array will be visible to
	 * this map and changes to the map will be visible to the array
	 * 
	 * @return the backing array
	 */
	public int[] array() {
		return arr;
	}
	
	@Override
	public int size() {
		return arr.length;
	}
	
	@Override
	public boolean isEmpty() { return arr.length == 0; }
	
	@Override
	public boolean containsKey(Object key) {
		return cls.isInstance(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		if (!(value instanceof Integer ival)) {
			return false;
		}
		int search = ival.intValue();
		for (int val : arr) {
			if (search == val) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Integer get(Object key) {
		if (!cls.isInstance(key)) {
			return null;
		}
		return Integer.valueOf(arr[((Enum<?>) key).ordinal()]);
	}
	
	@Override
	public Integer put(T key, Integer value) {
		int     o   = key.ordinal();
		Integer old = Integer.valueOf(arr[o]);
		arr[o] = value.intValue();
		return old;
	}
	
	@Override
	public Integer remove(Object key) {
		throw new UnsupportedOperationException("remove");
	}
	
	@Override
	public void putAll(Map<? extends T, ? extends Integer> m) {
		if (m instanceof EnumIntMap<?> eim) {
			if (cls != eim.cls) {
				throw new ClassCastException("can not cast from " + eim.cls + " to " + cls);
			}
			System.arraycopy(eim.arr, 0, arr, 0, arr.length);
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
		if (keySet == null) {
			keySet = Set.of(cls.getEnumConstants());
		}
		return keySet;
	}
	
	private Collection<Integer> values;
	
	@Override
	public Collection<Integer> values() {
		if (values == null) {
			values = new IntArrayList(arr);
		}
		return values;
	}
	
	private Set<Entry<T, Integer>> entrySet;
	
	@Override
	public Set<Entry<T, Integer>> entrySet() {
		if (entrySet == null) {
			entrySet = new EnumIntEntrySet();
		}
		return entrySet;
	}
	
	private class EnumIntEntrySet implements Set<Entry<T, Integer>> {
		
		private WeakReference<MyEntry>[] entries;
		
		@SuppressWarnings("unchecked")
		private MyEntry entry(int i) {
			MyEntry res;
			if (entries == null) {
				entries    = (WeakReference<MyEntry>[]) new WeakReference<?>[arr.length];
				res        = new MyEntry(i);
				entries[i] = new WeakReference<>(res);
			} else if (entries[i] == null) {
				res        = new MyEntry(i);
				entries[i] = new WeakReference<>(res);
			} else {
				res = entries[i].get();
				if (res == null) {
					res        = new MyEntry(i);
					entries[i] = new WeakReference<>(res);
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
				if (key == null) {
					key = cls.getEnumConstants()[index];
				}
				return key;
			}
			
			@Override
			public Integer getValue() { return Integer.valueOf(arr[index]); }
			
			@Override
			public Integer setValue(Integer value) {
				Integer old = Integer.valueOf(arr[index]);
				arr[index] = value.intValue();
				return old;
			}
			
			private int ival() {
				return arr[index];
			}
			
			private Class<T> cls() {
				return cls;
			}
			
			@Override
			public int hashCode() {
				return getKey().hashCode() ^ Integer.hashCode(arr[index]);
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
					return me.index == index && me.cls() == cls && me.ival() == arr[index];
				}
				if (!(obj instanceof Entry<?, ?> e2)) {
					return false;
				}
				Object ok = e2.getKey();
				if (!cls.isInstance(ok) || ((Enum<?>) ok).ordinal() != index) {
					return false;
				}
				Object ov = e2.getValue();
				return ov instanceof Integer val && val.intValue() == arr[index];
			}
			
		}
		
		@Override
		public int size() {
			return arr.length;
		}
		
		@Override
		public boolean isEmpty() { return arr.length == 0; }
		
		@Override
		public boolean contains(Object o) {
			if (o instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
				return cls == me.cls() && arr[me.index] == me.ival();
			}
			if (!(o instanceof Entry<?, ?> e)) {
				return false;
			}
			Object obj = e.getKey();
			if (!cls.isInstance(obj)) {
				return false;
			}
			Object val = e.getValue();
			if (!(val instanceof Integer ival)) {
				return false;
			}
			return arr[((Enum<?>) obj).ordinal()] == ival.intValue();
		}
		
		@Override
		public Iterator<Entry<T, Integer>> iterator() {
			return new Iterator<Entry<T, Integer>>() {
				
				private int index;
				
				@Override
				public boolean hasNext() {
					return index < arr.length;
				}
				
				@Override
				public Entry<T, Integer> next() {
					if (index >= arr.length) {
						throw new NoSuchElementException("no more elements");
					}
					return entry(index++);
				}
				
			};
		}
		
		@Override
		public Object[] toArray() {
			Object[] obj = new Object[arr.length];
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
			if (a.length < arr.length) {
				a = (A[]) Array.newInstance(a.getClass().componentType(), arr.length);
			} else if (a.length > arr.length) {
				a[arr.length] = null;
			}
			for (int i = 0; i < a.length; i++) {
				a[i] = (A) entry(i);
			}
			return a;
		}
		
		@Override
		public <A> A[] toArray(IntFunction<A[]> generator) {
			return toArray(generator.apply(arr.length));
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
				if (obj instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry e) {
					if (e.cls() != cls) return false;
					if (e.ival() != arr[e.index]) return false;
				} else if (!(obj instanceof Entry<?, ?> e)) {
					return false;
				} else {
					Object key = e.getKey();
					if (!cls.isInstance(key)) return false;
					Object val = e.getValue();
					if (!(val instanceof Integer ival)) return false;
					if (arr[((Enum<?>)key).ordinal()] != ival.intValue()) return false;
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
			return cls;
		}
		
		private int[] arr() {
			return arr;
		}
		
		@Override
		public int hashCode() { // I know that zero is a valid hash
			if (hash != 0) return hash;
			int sum  = 0;
			T[] vals = null;
			for (int i = 0; i < arr.length; i++) {
				if (entries[i] != null) {
					EnumIntMap<T>.EnumIntEntrySet.MyEntry e = entries[i].get();
					if (e != null) {
						sum += e.hashCode();
						continue;
					}
				}
				if (vals == null) {
					vals = cls.getEnumConstants();
				}
				sum += vals[i].hashCode() ^ Integer.hashCode(arr[i]);
			}
			hash = sum;
			return sum;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EnumIntMap<?>.EnumIntEntrySet s) {
				if (cls != s.cls()) return false;
				return Arrays.equals(arr, s.arr());
			}
			if (!(obj instanceof Set<?> s)) return false;
			if (s.size() != arr.length) return false;
			for (Object o : s) {
				if (!(o instanceof Entry<?, ?> e)) return false;
				if (o instanceof EnumIntMap<?>.EnumIntEntrySet.MyEntry me) {
					if (me.cls() != cls) return false;
					if (arr[me.index] != me.ival()) return false;
					continue;
				}
				Object k = e.getKey();
				Object v = e.getValue();
				if (k == null || v == null) return false;
				if (v.getClass() != Integer.class) return false;
				if (!cls.isInstance(k)) return false;
				if (arr[((Enum<?>) k).ordinal()] != ((Integer) v).intValue()) return false;
			} // false true result if the set allows multiple times the same entry or gets
				// modified
			return true;
		}
		
	}
	
	public EnumIntMap<T> copy() {
		return new EnumIntMap<>(cls, arr.clone());
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
		if (hash != 0) return hash;
		if (entrySet != null) {
			return entrySet.hashCode();
		}
		int sum  = 0;
		T[] vals = cls.getEnumConstants();
		for (int i = 0; i < arr.length; i++) {
			sum += vals[i].hashCode() ^ Integer.hashCode(arr[i]);
		}
		hash = sum;
		return sum;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnumIntMap<?> eim) {
			if (cls != eim.cls) return false;
			return Arrays.equals(arr, eim.arr);
		}
		if (!(obj instanceof Map<?, ?> m)) return false;
		if (m.size() != arr.length) return false;
		if (entrySet != null) {
			return entrySet.equals(m.entrySet());
		}
		T[] vals = cls.getEnumConstants();
		for (int i = 0; i < vals.length; i++) {
			Object val = m.get(vals[i]);
			if (!(val instanceof Integer ival)) return false;
			if (arr[i] != ival.intValue()) return false;
		}
		return true;
	}
	
}
