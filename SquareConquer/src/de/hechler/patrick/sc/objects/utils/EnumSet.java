package de.hechler.patrick.sc.objects.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class EnumSet <E extends Enum <?>> implements Set <E> {
	
	private int modifycations;
	
	private final E[]      values;
	private boolean[]      finder;
	public final Class <E> clas;
	
	
	public EnumSet(Class <E> classs) {
		this.values = classs.getEnumConstants();
		this.finder = new boolean[values.length];
		this.clas = classs;
		this.modifycations = 0;
	}
	
	@Override
	public int size() {
		int res = 0;
		for (boolean b : finder) {
			if (b) res ++ ;
		}
		return res;
	}
	
	@Override
	public boolean isEmpty() {
		for (boolean b : finder) {
			if (b) return false;
		}
		return true;
	}
	
	@Override
	public boolean contains(Object o) {
		try {
			E t = clas.cast(o);
			return finder[t.ordinal()];
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public Iterator <E> iterator() {
		return new Iter();
	}
	
	@Override
	public Object[] toArray() {
		Iter iter = new Iter();
		Object[] res = new Object[iter.size];
		while (iter.hasNext()) {
			res[iter.cnt] = iter.next();
		}
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <Z> Z[] toArray(Z[] res) {
		Iter iter = new Iter();
		if (res.length != iter.size) {
			res = (Z[]) Array.newInstance(res.getClass().getComponentType(), iter.size);
		}
		while (iter.hasNext()) {
			res[iter.cnt] = (Z) iter.next();
		}
		return res;
	}
	
	@Override
	public boolean add(E e) {
		if ( !finder[e.ordinal()]) {
			finder[e.ordinal()] = true;
			modifycations ++ ;
			return true;
		} else return false;
	}
	
	@Override
	public boolean remove(Object o) {
		try {
			E e = clas.cast(o);
			int orid = e.ordinal();
			if (finder[orid]) {
				finder[orid] = false;
				modifycations ++ ;
				return true;
			}
		} catch (ClassCastException e) {
		}
		return false;
	}
	
	@Override
	public boolean containsAll(Collection <?> c) {
		for (Object check : c) {
			if ( !contains(check)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection <? extends E> c) {
		int mod = modifycations;
		for (E ad : c) {
			add(ad);
		}
		return mod != modifycations;
	}
	
	@Override
	public boolean retainAll(Collection <?> c) {
		if ( ! (c instanceof Set <?>)) {
			c = new HashSet <>(c);
		}
		final int startMods = modifycations;
		int mods = modifycations;
		Iter iter = new Iter();
		while (iter.hasNext()) {
			E check = iter.next();
			int o = check.ordinal();
			if ( !finder[o]) {
				mods ++ ;
				finder[o] = false;
			}
		}
		modifycations = mods;
		return startMods != modifycations;
	}
	
	@Override
	public boolean removeAll(Collection <?> c) {
		int mod = modifycations;
		for (Object check : c) {
			try {
				int o = clas.cast(check).ordinal();
				if (finder[o]) {
					modifycations ++ ;
					finder[0] = false;
				}
			} catch (ClassCastException e) {
			}
		}
		return mod != modifycations;
	}
	
	@Override
	public void clear() {
		modifycations ++ ;
		finder = new boolean[values.length];
	}
	
	private class Iter implements Iterator <E> {
		
		int i    = 0;
		int cnt  = 0;
		int size = size();
		int mod  = modifycations;
		
		@Override
		public boolean hasNext() {
			if (mod == modifycations) {
				return cnt < size;
			}
			mod = modifycations;
			try {
				next();
				i -- ;
				cnt -- ;
				return true;
			} catch (NoSuchElementException e) {
				return false;
			}
		}
		
		@Override
		public E next() throws NoSuchElementException {
			for (; i < finder.length; i ++ ) {
				if (finder[i]) {
					cnt ++ ;
					i ++ ;
					return values[i];
				}
			}
			throw new NoSuchElementException();
		}
		
	}
	
}
