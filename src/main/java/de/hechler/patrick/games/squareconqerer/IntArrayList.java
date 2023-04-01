package de.hechler.patrick.games.squareconqerer;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;


public class IntArrayList implements List<Integer> {
	
	private final int[] arr;
	private final int   off;
	private final int   len;
	
	public IntArrayList(int[] arr) {
		if (arr == null) {
			throw new NullPointerException("array is null");
		}
		this.arr = arr;
		this.off = 0;
		this.len = arr.length;
	}
	
	public IntArrayList(int[] arr, int off, int len) {
		if (arr == null) {
			throw new NullPointerException("array is null");
		}
		if (off < 0 || len < 0) {
			throw new IllegalArgumentException("offset or length is negative: offset=" + off + " length=" + len);
		}
		if (arr.length - off < len) {
			throw new IllegalArgumentException("the array is too small (array.len=" + arr.length + " offset=" + off + " length=" + len + ')');
		}
		this.arr = arr;
		this.off = off;
		this.len = len;
	}
	
	@Override
	public int size() {
		return len;
	}
	
	@Override
	public boolean isEmpty() { return len == 0; }
	
	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer val)) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (val == arr[off + i]) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new IntArrayListIter();
	}
	
	@Override
	public Object[] toArray() {
		Object[] a = new Object[len];
		for (int i = 0; i < a.length; i++) {
			a[i] = Integer.valueOf(arr[off + i]);
		}
		return a;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		Integer[].class.asSubclass(a.getClass());
		if (a.length < len) {
			a = (T[]) Array.newInstance(a.getClass().componentType(), len);
		} else if (a.length > len) {
			a[len] = null;
		}
		for (int i = 0; i < len; i++) {
			a[i] = (T) Integer.valueOf(arr[off + i]);
		}
		return a;
	}
	
	@Override
	public <T> T[] toArray(IntFunction<T[]> generator) {
		return toArray(generator.apply(len));
	}
	
	@Override
	public boolean add(Integer e) {
		throw new UnsupportedOperationException("add");
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("add");
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object obj : c) {
			if (!(obj instanceof Integer val)) {
				return false;
			}
			int v = val.intValue();
			for (int i = 0;; i++) {
				if (i >= len) {
					return false;
				}
				if (arr[off + i] == v) {
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		throw new UnsupportedOperationException("add");
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends Integer> c) {
		throw new UnsupportedOperationException("add");
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("remove");
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("remove");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear");
	}
	
	@Override
	public Integer get(int index) {
		if (index < 0 || index > len) {
			throw new IndexOutOfBoundsException("index=" + index + " length=" + len);
		}
		return Integer.valueOf(arr[off + index]);
	}
	
	@Override
	public Integer set(int index, Integer element) {
		if (index < 0 || index > len) {
			throw new IndexOutOfBoundsException("index=" + index + " length=" + len);
		}
		Integer old = Integer.valueOf(arr[off + index]);
		arr[off + index] = element.intValue();
		return old;
	}
	
	@Override
	public void add(int index, Integer element) {
		throw new UnsupportedOperationException("remove");
	}
	
	@Override
	public Integer remove(int index) {
		throw new UnsupportedOperationException("add");
	}
	
	@Override
	public int indexOf(Object o) {
		if (!(o instanceof Integer val)) {
			return -1;
		}
		int v = val.intValue();
		for (int i = 0; i < len; i++) {
			if (v == arr[off + i]) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		if (!(o instanceof Integer val)) {
			return -1;
		}
		int v = val.intValue();
		for (int i = len - 1; i >= 0; i--) {
			if (v == arr[off + i]) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public ListIterator<Integer> listIterator() {
		return new IntArrayListIter();
	}
	
	@Override
	public ListIterator<Integer> listIterator(int index) {
		return new IntArrayListIter(index);
	}
	
	private class IntArrayListIter implements ListIterator<Integer> {
		
		private int index;
		private int direction = Integer.MIN_VALUE; // ensure error
		
		public IntArrayListIter(int index) { this.index = index; }
		
		public IntArrayListIter() {}
		
		@Override
		public boolean hasNext() {
			return index < len;
		}
		
		@Override
		public Integer next() {
			if (index >= len) {
				throw new NoSuchElementException("no next element");
			}
			direction = -1;
			return Integer.valueOf(arr[off + (index++)]);
		}
		
		@Override
		public boolean hasPrevious() {
			return index > 0;
		}
		
		@Override
		public Integer previous() {
			if (index < 0) {
				throw new NoSuchElementException("no previous element");
			}
			direction = 0;
			return Integer.valueOf(arr[off + (--index)]);
		}
		
		@Override
		public int nextIndex() {
			return index;
		}
		
		@Override
		public int previousIndex() {
			return index - 1;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("add");
		}
		
		@Override
		public void set(Integer e) {
			arr[off + index + direction] = e.intValue();
		}
		
		@Override
		public void add(Integer e) {
			throw new UnsupportedOperationException("add");
		}
		
	}
	
	@Override
	public List<Integer> subList(int fromIndex, int toIndex) {
		if (toIndex < fromIndex || toIndex >= len || fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + " toIndex=" + toIndex + " length=" + len);
		}
		return new IntArrayList(arr, off + fromIndex, toIndex - fromIndex);
	}
	
}
