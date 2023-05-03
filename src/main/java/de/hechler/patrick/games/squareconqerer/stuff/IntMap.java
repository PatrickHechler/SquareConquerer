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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Arrays;

public class IntMap<T> {
	
	private static final String       COUNT_FIELD  = "COUNT";                         //$NON-NLS-1$
	private static final String       COUNT_METHOD = "count";                         //$NON-NLS-1$
	private static final String       ORDINAL_NAME = "ordinal";                       //$NON-NLS-1$
	private static final Lookup       LOOKUP       = MethodHandles.lookup();
	private static final MethodType   METHOD_TYPE  = MethodType.methodType(int.class);
	private static final MethodHandle ENUM_HANDLE;
	
	static {
		try {
			ENUM_HANDLE = LOOKUP.findVirtual(Enum.class, ORDINAL_NAME, METHOD_TYPE);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError("could not get ordinal handle: " + e.toString(), e);
		}
	}
	
	private final MethodHandle ordinal;
	private final Class<T>     cls;
	private final int[]        arr;
	
	private IntMap(Class<T> cls, int[] arr, MethodHandle ordinal) {
		this.cls     = cls;
		this.arr     = arr;
		this.ordinal = ordinal;
	}
	
	/**
	 * creates a new {@link IntMap} for the given class.
	 * <p>
	 * if <code>cls</code> is no <code>enum</code> class:<br>
	 * <ol>
	 * <li>if there is a <code>public static int COUNT</code> field, it is assumed that this is the number of different instances this class has.</li>
	 * <li>if there is a <code>public static int count()</code> method, it is assumed that the return value is the number of different instances this class has.</li>
	 * </ol>
	 * the <code>ordinal</code> value of each instance:
	 * <ol>
	 * <li>if there is a <code>public int ordinal</code> field , its value will be used as the ordinal</li>
	 * <li>if there is a <code>public int ordinal()</code> method, its return value will be used as the ordinal</li>
	 * </ol>
	 * 
	 * @param <T> the type of the map
	 * @param cls the type of the map
	 * @return the map
	 */
	public static <T> IntMap<T> create(Class<T> cls) {
		if (cls.isEnum()) {
			return create(cls, new int[cls.getEnumConstants().length], ENUM_HANDLE);
		}
		try {
			return create(cls, new int[(int) findHandleStatic(cls, COUNT_FIELD, COUNT_METHOD).invoke()], findHandleVirtual(cls, ORDINAL_NAME));
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	public static <T> IntMap<T> create(Class<T> cls, int count, String ordinalName) {
		return create(cls, new int[count], findHandleVirtual(cls, ordinalName));
	}
	
	public static <T> IntMap<T> create(Class<T> cls, int count, MethodHandle ordinal) {
		return create(cls, new int[count], ordinal);
	}
	
	public static <T> IntMap<T> create(Class<T> cls, int[] arr, MethodHandle ordinal) {
		return new IntMap<>(cls, arr, ordinal);
	}
	
	public static int staticIntField(Class<?> cls, String fieldName) {
		try {
			return (int) findHandleStatic(cls, fieldName, null).invoke();
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	public static int count(Class<?> cls) {
		if (cls.isEnum()) {
			return cls.getEnumConstants().length;
		}
		try {
			return (int) findHandleStatic(cls, COUNT_FIELD, COUNT_METHOD).invoke();
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	public static int ordinal(Object obj) {
		if (obj instanceof Enum<?> e) return e.ordinal();
		try {
			return (int) findHandleVirtual(obj.getClass(), ORDINAL_NAME).invoke(obj);
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	private static MethodHandle findHandleVirtual(Class<?> cls, String name) throws AssertionError {
		return findHandle(cls, name, name, true);
	}
	
	private static MethodHandle findHandleStatic(Class<?> cls, String fieldName, String methodName) throws AssertionError {
		return findHandle(cls, fieldName, methodName, false);
	}
	
	private static MethodHandle findHandle(Class<?> cls, String fieldName, String methodName, boolean virtual) throws AssertionError {
		try {
			if (virtual) return LOOKUP.findGetter(cls, fieldName, int.class);
			return LOOKUP.findStaticGetter(cls, fieldName, int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			try {
				if (virtual) return LOOKUP.findVirtual(cls, methodName, METHOD_TYPE);
				if (methodName == null) throw new AssertionError("could not get ordinal handle: " + e.toString(), e);
				return LOOKUP.findStatic(cls, methodName, METHOD_TYPE);
			} catch (NoSuchMethodException | IllegalAccessException e1) {
				e.addSuppressed(e1);
				throw new AssertionError("could not get ordinal handle: " + e.toString(), e);
			}
		}
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
		try {
			return this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable t) {
			throw rethrow(t);
		}
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
		try {
			this.arr[(int) this.ordinal.invoke(e)] = val;
		} catch (Throwable t) {
			throw rethrow(t);
		}
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
		try {
			return ++this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	public int inc(int ordinal) {
		return ++this.arr[ordinal];
	}
	
	public int addBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("add is not greather than zero: add=" + val);
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(e.toString() + " (" + e.getClass() + ") is no instance of " + this.cls); //$NON-NLS-1$
		}
		try {
			int o = (int) this.ordinal.invoke(e);
			this.arr[o] += val;
			return this.arr[o];
		} catch (Throwable t) {
			throw rethrow(t);
		}
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
		try {
			return --this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable e1) {
			throw rethrow(e1);
		}
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
		try {
			int o = (int) this.ordinal.invoke(e);
			this.arr[o] -= val;
			return this.arr[o];
		} catch (Throwable t) {
			throw rethrow(t);
		}
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
	 * return the backing array of this map, changes to the array will be visible to this map and changes to the map will be visible to the array
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
		return new IntMap<>(this.cls, this.arr.clone(), this.ordinal);
	}
	
	private int hash;
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (this.hash != 0) return this.hash;
		int sum = 0;
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
	
	private static RuntimeException rethrow(Throwable t) {
		if (t instanceof RuntimeException re) throw re;
		if (t instanceof Error err) throw err;
		throw new AssertionError(t);
	}
	
}
