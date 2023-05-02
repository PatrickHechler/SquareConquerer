// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.stuff;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class IntMap<T> {
	
	private final Class<T> cls;
	private final int[]    arr;
	
	private IntMap(Class<T> cls) {
		if (!cls.isEnum()) {
			throw new IllegalArgumentException("the class is no enum class");
		}
		this.cls = cls;
		this.arr = new int[cls.getEnumConstants().length];
	}
	
	private IntMap(Class<T> cls, int[] arr) {
		this.cls = cls;
		this.arr = arr;
	}
	
	public static <T extends Enum<T>> IntMap<T> createEnumIntMap(Class<T> cls) {
		return new IntMap<>(cls);
	}
	
	public static <T> IntMap<T> createIntIntMap(Class<T> cls) {
		try {
			return new IntMap<>(cls, new int[((Integer)cls.getDeclaredMethod("count").invoke(null)).intValue()]);//$NON-NLS-1$
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		} 
	}
	
	public static IntMap<Integer> createIntIntMap(int length) {
		return new IntMap<>(int.class, new int[length]);
	}
	
	public static IntMap<Integer> createIntIntMap(int[] arr) {
		return new IntMap<>(int.class, arr);
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
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls); //$NON-NLS-1$
		}
		return this.arr[((Enum<?>) e).ordinal()];
	}
	
	public int get(int ordinal) {
		return this.arr[ordinal];
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
		this.arr[((Enum<?>) e).ordinal()] = val;
	}
	
	public void set(int ordinal, int val) {
		this.arr[ordinal] = val;
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
		return ++this.arr[((Enum<?>) e).ordinal()];
	}
	
	public int inc(int ordinal) {
		return ++this.arr[ordinal];
	}
	
	public int addBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls);
		}
		int o = ((Enum<?>) e).ordinal();
		this.arr[o] += val;
		return this.arr[o];
	}
	
	public int addBy(int ordinal, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		this.arr[ordinal] += val;
		return this.arr[ordinal];
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
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls); //$NON-NLS-1$
		}
		return --this.arr[((Enum<?>) e).ordinal()];
	}
	
	public int dec(int ordinal) {
		return --this.arr[ordinal];
	}
	
	public int subBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("sub is not greather than zero: sub=" + val);
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls); //$NON-NLS-1$
		}
		int o = ((Enum<?>) e).ordinal();
		this.arr[o] -= val;
		return this.arr[o];
	}
	
	public int subBy(int ordinal, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("sub is not greather than zero: sub=" + val);
		}
		this.arr[ordinal] -= val;
		return this.arr[ordinal];
	}
	
	public void setAll(IntMap<?> other) {
		if (this.cls != other.cls || this.arr.length != other.arr.length) throw new ClassCastException("the other map has a different type");
		System.arraycopy(other.arr, 0, this.arr, 0, this.arr.length);
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
	
	public int length() {
		return this.arr.length;
	}
	
	public IntMap<T> copy() {
		return new IntMap<>(this.cls, this.arr.clone());
	}
	
	private int hash;
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (this.hash != 0) return this.hash;
		int sum  = 0;
		for (int i = 0; i < this.arr.length; i++) {
			sum += Integer.hashCode(this.arr[i]);
		}
		this.hash = sum;
		return sum;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntMap<?> eim) {
			if (this.cls != eim.cls) return false;
			return Arrays.equals(this.arr, eim.arr);
		}
		if (!(obj instanceof IntMap<?> m)) return false;
		if (m.cls != this.cls) return false;
		return Arrays.equals(m.arr, this.arr);
	}
	
}
