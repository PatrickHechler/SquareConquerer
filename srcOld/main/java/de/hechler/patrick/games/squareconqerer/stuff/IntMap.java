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
import java.text.Format;
import java.util.Arrays;

import de.hechler.patrick.games.squareconqerer.Messages;

/**
 * this class is used to map enum (like) classes to int values
 * 
 * @author Patrick Hechler
 * @param <T> the key class
 */
public class IntMap<T> {
	
	private static final Format THE_OTHER_MAP_HAS_A_DIFFERENT_TYPE   = Messages.getFormat("IntMap.different-type");          //$NON-NLS-1$
	private static final Format THE_OTHER_MAP_HAS_A_DIFFERENT_LEGNTH = Messages.getFormat("IntMap.different-length");        //$NON-NLS-1$
	private static final Format SUB_IS_NOT_GREATHER_THAN_ZERO_SUB_0  = Messages.getFormat("IntMap.sub-not-strict-positive"); //$NON-NLS-1$
	private static final Format ADD_IS_NOT_GREATHER_THAN_ZERO_ADD_0  = Messages.getFormat("IntMap.add-not-strict-positive"); //$NON-NLS-1$
	private static final Format VALUE_0_1_IS_NO_INSTANCE_OF_2        = Messages.getFormat("IntMap.not-my-type");             //$NON-NLS-1$
	private static final Format COULD_NOT_GET_ORDINAL_HANDLE_0       = Messages.getFormat("IntMap.could-not-get-ordinal");   //$NON-NLS-1$
	
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
			throw new AssertionError(Messages.format(COULD_NOT_GET_ORDINAL_HANDLE_0, e.toString()), e);
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
	
	/**
	 * create a new {@link IntMap}, which tries to find an ordinal field/method with the given <code>ordinalName</code> and assumes that there are <code>count</code>
	 * different values
	 * 
	 * @param <T>         the type of the class
	 * @param cls         the class
	 * @param count       the number of different values from the class
	 * @param ordinalName the name of the ordinal field/method
	 * @return the newly created {@link IntMap}
	 */
	public static <T> IntMap<T> create(Class<T> cls, int count, String ordinalName) {
		return create(cls, new int[count], findHandleVirtual(cls, ordinalName));
	}
	
	/**
	 * create a new {@link IntMap}, which tries to find an ordinal field/method with the given <code>ordinalName</code> and assumes that there are
	 * <code>arr.length</code> different values, which are mapped to the values of the array
	 * 
	 * @param <T>         the type of the class
	 * @param cls         the class
	 * @param arr         the array to store the mappings of the class
	 * @param ordinalName the name of the ordinal field/method
	 * @return the newly created {@link IntMap}
	 */
	public static <T> IntMap<T> create(Class<T> cls, int[] arr, String ordinalName) {
		return create(cls, arr, findHandleVirtual(cls, ordinalName));
	}
	
	/**
	 * create a new {@link IntMap}, which uses the given <code>ordinal</code> method and assumes that there are <code>count</code> different values
	 * 
	 * @param <T>     the type of the class
	 * @param cls     the class
	 * @param count   the number of different values from the class
	 * @param ordinal the ordinal method
	 * @return the newly created {@link IntMap}
	 */
	public static <T> IntMap<T> create(Class<T> cls, int count, MethodHandle ordinal) {
		return create(cls, new int[count], ordinal);
	}
	
	/**
	 * create a new {@link IntMap}, which uses the given <code>ordinal</code> method and assumes that there are <code>arr.length</code> different values, which are
	 * mapped to the values of the array
	 * 
	 * @param <T>     the type of the class
	 * @param cls     the class
	 * @param arr     the array to store the mappings of the class
	 * @param ordinal the ordinal method
	 * @return the newly created {@link IntMap}
	 */
	public static <T> IntMap<T> create(Class<T> cls, int[] arr, MethodHandle ordinal) {
		return new IntMap<>(cls, arr, ordinal);
	}
	
	/**
	 * returns the value of the <code>public static int</code> field from the given class
	 * 
	 * @param cls       the class with the field
	 * @param fieldName the name of the field
	 * @return the value of the field
	 */
	public static int staticIntField(Class<?> cls, String fieldName) {
		try {
			return (int) findHandleStatic(cls, fieldName, null).invoke();
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	/**
	 * returns the value of the <code>public static int COUNT</code> field or <code>public static int count()</code>from the given class
	 * 
	 * @param cls the class with the field/method
	 * @return the value of the field/method
	 */
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
	
	/**
	 * returns the value of the <code>public int ordinal</code> field or <code>public int ordinal()</code>from the given object
	 * <p>
	 * if the given class is a enum class, the {@link Enum#ordinal()} value is return directly (without searching for a <code>public int ordinal</code> field)
	 * 
	 * @param obj the object with the field/method
	 * @return the value of the field
	 */
	public static int ordinal(Object obj) {
		if (obj instanceof Enum<?> e) return e.ordinal();
		try {
			return (int) findHandleVirtual(obj.getClass(), ORDINAL_NAME).invoke(obj);
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
	
	private static MethodHandle findHandleVirtual(Class<?> cls, String name) throws AssertionError {
		try {
			return LOOKUP.findGetter(cls, name, int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			try {
				return LOOKUP.findVirtual(cls, name, METHOD_TYPE);
			} catch (NoSuchMethodException | IllegalAccessException e1) {
				e.addSuppressed(e1);
				throw new AssertionError(Messages.format(COULD_NOT_GET_ORDINAL_HANDLE_0, e.toString()), e);
			}
		}
	}
	
	private static MethodHandle findHandleStatic(Class<?> cls, String fieldName, String methodName) throws AssertionError {
		try {
			return LOOKUP.findStaticGetter(cls, fieldName, int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			try {
				if (methodName == null) throw new AssertionError(Messages.format(COULD_NOT_GET_ORDINAL_HANDLE_0, e.toString()), e);
				return LOOKUP.findStatic(cls, methodName, METHOD_TYPE);
			} catch (NoSuchMethodException | IllegalAccessException e1) {
				e.addSuppressed(e1);
				throw new AssertionError(Messages.format(COULD_NOT_GET_ORDINAL_HANDLE_0, e.toString()), e);
			}
		}
	}
	
	/**
	 * return the current value
	 * 
	 * @param e the constant
	 * @return the current value
	 */
	public int get(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e.toString(), e.getClass(), this.cls));
		}
		try {
			return this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	/**
	 * returns the value of the mapping for the given ordinal
	 * 
	 * @param ordinal the ordinal value
	 * @return the value of the mapping for the given ordinal
	 */
	public int get(int ordinal) {
		return this.arr[ordinal];
	}
	
	/**
	 * set the value
	 * 
	 * @param e   the constant
	 * @param val the new value
	 */
	public void set(T e, int val) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e, e.getClass(), this.cls));
		}
		try {
			this.arr[(int) this.ordinal.invoke(e)] = val;
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	/**
	 * sets the value of the mapping for the given ordinal
	 * 
	 * @param ordinal the ordinal value
	 * @param val     the new value of the mapping for the given ordinal
	 */
	public void set(int ordinal, int val) {
		this.arr[ordinal] = val;
	}
	
	/**
	 * increment and return the new value
	 * 
	 * @param e the constant
	 * @return the new value
	 */
	public int inc(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e, e.getClass(), this.cls));
		}
		try {
			return ++this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	/**
	 * increment and return the new value
	 * 
	 * @param ordinal the ordinal
	 * @return the new value
	 */
	public int inc(int ordinal) {
		return ++this.arr[ordinal];
	}
	
	/**
	 * adds the given value to the value of the mapping for the given value
	 * 
	 * @param e   the constant
	 * @param val the new value of the mapping for the given ordinal
	 * @return the new value
	 */
	public int addBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException(Messages.format(ADD_IS_NOT_GREATHER_THAN_ZERO_ADD_0, Integer.toString(val)));
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e, e.getClass(), this.cls));
		}
		try {
			int o = (int) this.ordinal.invoke(e);
			this.arr[o] += val;
			return this.arr[o];
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	/**
	 * adds the given value to the value of the mapping for the given ordinal
	 * 
	 * @param ordinal the ordinal
	 * @param val     the new value of the mapping for the given ordinal
	 * @return the new value
	 */
	public int addBy(int ordinal, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException(Messages.format(ADD_IS_NOT_GREATHER_THAN_ZERO_ADD_0, Integer.toString(val)));
		}
		this.arr[ordinal] += val;
		return this.arr[ordinal];
	}
	
	/**
	 * decrement and return the new value
	 * 
	 * @param e the enum constant
	 * @return the new value
	 */
	public int dec(T e) {
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e, e.getClass(), this.cls));
		}
		try {
			return --this.arr[(int) this.ordinal.invoke(e)];
		} catch (Throwable e1) {
			throw rethrow(e1);
		}
	}
	
	/**
	 * decrement and return the new value
	 * 
	 * @param ordinal the ordinal
	 * @return the new value
	 */
	public int dec(int ordinal) {
		return --this.arr[ordinal];
	}
	
	/**
	 * subtracts the given value to the value of the mapping for the given value
	 * 
	 * @param e   the constant
	 * @param val the new value of the mapping for the given ordinal
	 * @return the new value
	 */
	public int subBy(T e, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException(Messages.format(SUB_IS_NOT_GREATHER_THAN_ZERO_SUB_0, Integer.toString(val)));
		}
		if (!this.cls.isInstance(e)) {
			throw new ClassCastException(Messages.format(VALUE_0_1_IS_NO_INSTANCE_OF_2, e, e.getClass(), this.cls)); // $NON-NLS-1$
		}
		try {
			int o = (int) this.ordinal.invoke(e);
			this.arr[o] -= val;
			return this.arr[o];
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	/**
	 * subtracts the given value to the value of the mapping for the given value
	 * 
	 * @param ordinal the ordinal
	 * @param val     the new value of the mapping for the given ordinal
	 * @return the new value
	 */
	public int subBy(int ordinal, int val) {
		if (val <= 0) {
			throw new IllegalArgumentException(Messages.format(SUB_IS_NOT_GREATHER_THAN_ZERO_SUB_0, Integer.toString(val)));
		}
		this.arr[ordinal] -= val;
		return this.arr[ordinal];
	}
	
	/**
	 * overwrites all values of this {@link IntMap} with the values of the given {@link IntMap}
	 * 
	 * @param other the other map containing the new values of this map
	 * @throws ClassCastException if the given map has a different type
	 */
	public void setAll(IntMap<?> other) throws ClassCastException {
		if (this.cls != other.cls) throw new ClassCastException(Messages.format(THE_OTHER_MAP_HAS_A_DIFFERENT_TYPE, this.cls, other.cls));
		if (this.arr.length != other.arr.length) throw new ClassCastException(
			Messages.format(THE_OTHER_MAP_HAS_A_DIFFERENT_LEGNTH, this.cls, Integer.toString(this.arr.length), Integer.toString(other.arr.length)));
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
	
	/**
	 * returns the number of mappings in this {@link IntMap}
	 * 
	 * @return the number of mappings in this {@link IntMap}
	 */
	public int length() {
		return this.arr.length;
	}
	
	/**
	 * returns a copy of this {@link IntMap}
	 * 
	 * @return a copy of this {@link IntMap}
	 */
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
