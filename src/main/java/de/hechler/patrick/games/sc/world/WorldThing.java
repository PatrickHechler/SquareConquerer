package de.hechler.patrick.games.sc.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.Value.BooleanValue;
import de.hechler.patrick.games.sc.values.Value.DoubleValue;
import de.hechler.patrick.games.sc.values.Value.EnumValue;
import de.hechler.patrick.games.sc.values.Value.IntValue;
import de.hechler.patrick.games.sc.values.Value.JustAValue;
import de.hechler.patrick.games.sc.values.Value.LongValue;
import de.hechler.patrick.games.sc.values.Value.StringValue;
import de.hechler.patrick.games.sc.values.Value.UserListValue;
import de.hechler.patrick.games.sc.values.Value.UserValue;
import de.hechler.patrick.games.sc.values.Value.WorldThingValue;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;

public abstract sealed class WorldThing<T extends AddableType<T, M>, M extends WorldThing<T, M>> implements Imagable, Comparable<WorldThing<?, ?>>
		permits Entity<?, ?>, Ground, Resource {
	
	private int     hash;
	private boolean knownHash;
	
	public void resetHash() {
		this.hash = 0;
		this.knownHash = false;
	}
	
	public abstract T type();
	
	public abstract M unmodifiable();
	
	public abstract boolean same(M t);
	
	public abstract Map<String, Value> values();
	
	public abstract Value value(String name);
	
	public abstract void value(Value newValue);
	
	public JustAValue justValue(String name) {
		return (JustAValue) value(name);
	}
	
	public IntValue intValue(String name) {
		return (IntValue) value(name);
	}
	
	public LongValue longValue(String name) {
		return (LongValue) value(name);
	}
	
	public DoubleValue doubleValue(String name) {
		return (DoubleValue) value(name);
	}
	
	public BooleanValue booleanValue(String name) {
		return (BooleanValue) value(name);
	}
	
	public EnumValue<?> enumValue(String name) {
		return (EnumValue<?>) value(name);
	}
	
	public StringValue stringValue(String name) {
		return (StringValue) value(name);
	}
	
	public UserValue userValue(String name) {
		return (UserValue) value(name);
	}
	
	public UserListValue userListValue(String name) {
		return (UserListValue) value(name);
	}
	
	public static final String X = "x";
	public static final String Y = "y";
	
	public int x() {
		return intValue(X).value();
	}
	
	public int y() {
		return intValue(Y).value();
	}
	
	// TODO test
	private static class TwoVals<A extends Comparable<A>> implements Comparable<TwoVals<A>> {
		
		private final A a;
		private final A b;
		
		private int cmpRes;
		
		private TwoVals(A a, A b) { super(); this.a = a; this.b = b; }
		
		@Override
		public int hashCode() {
			return this.a.hashCode() ^ this.b.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj.getClass() != getClass()) return false;
			TwoVals<?> o = (TwoVals<?>) obj;
			return this.a.equals(o.a) && this.b.equals(o.b) || this.a.equals(o.b) && this.b.equals(o.a);
		}
		
		@Override
		public int compareTo(TwoVals<A> o) {
			int aa = sgn(this.a.compareTo(o.a));
			int bb = sgn(this.b.compareTo(o.b));
			if (aa == bb) {
				return aa;
			}
			int ab = sgn(this.a.compareTo(o.b));
			int ba = sgn(this.b.compareTo(o.a));
			if (ab == ba) {
				return ab;
			}
			if (aa > 0) {
				this.cmpRes = 1;
				o.cmpRes    = -1;
			} else {
				this.cmpRes = -1;
				o.cmpRes    = 1;
			}
			throw new IllegalStateException("ab");
		}
		
		private static int sgn(int val) {
			if (val < 0) return -1;
			if (val > 0) return 1;
			return 0;
		}
		
	}
	
	// the order is needed for reproducibility
	@Override
	public int compareTo(WorldThing<?, ?> o) {
		if (this == o) return 0;
		Map<TwoVals<WorldThing<?, ?>>, Boolean> things = new HashMap<>();
		things.put(new TwoVals<>(this, o), Boolean.FALSE);
		return compare(o, things);
	}
	
	private int compare(WorldThing<?, ?> o, Map<TwoVals<WorldThing<?, ?>>, Boolean> things) throws AssertionError {
		T                 t  = type();
		AddableType<?, ?> ot = o.type();
		if (t != ot) {
			int cmp = t.name.compareTo(ot.name);
			if (cmp != 0) return cmp;
			throw new AssertionError("types are not the same, but have the same name!");
		}
		for (String key : t.values.keySet()) {
			int cmp = compare(value(key), o.value(key), things);
			if (cmp != 0) return cmp;
		}
		return 0;
	}
	
	private static int compare(Value v, Value ov, Map<TwoVals<WorldThing<?, ?>>, Boolean> things) {
		switch (v) {
		case @SuppressWarnings("preview") JustAValue jav -> {
			return 0;
		}
		case @SuppressWarnings("preview") IntValue iv -> {
			return Integer.compare(iv.value(), ((IntValue) ov).value());
		}
		case @SuppressWarnings("preview") LongValue lv -> {
			return Long.compare(lv.value(), ((LongValue) ov).value());
		}
		case @SuppressWarnings("preview") DoubleValue dv -> {
			return Double.compare(dv.value(), ((DoubleValue) ov).value());
		}
		case @SuppressWarnings("preview") BooleanValue bv -> {
			return Boolean.compare(bv.value(), ((BooleanValue) ov).value());
		}
		case @SuppressWarnings("preview") EnumValue<?> ev -> {
			return cmp(ev.value(), ((EnumValue<?>) ov).value());
		}
		case @SuppressWarnings("preview") StringValue sv -> {
			return sv.value().compareTo(((StringValue) ov).value());
		}
		case @SuppressWarnings("preview") UserValue uv -> {
			return uv.value().name().compareTo(((UserValue) ov).value().name());
		}
		case @SuppressWarnings("preview") UserListValue ulv -> {
			List<User>     ul    = ulv.value();
			List<User>     oul   = ((UserListValue) ov).value();
			Iterator<User> iter  = ul.iterator();
			Iterator<User> oiter = oul.iterator();
			while (iter.hasNext()) {
				if (!oiter.hasNext()) return 1;
				int cmp = iter.next().name().compareTo(oiter.next().name());
				if (cmp != 0) return cmp;
			}
			if (oiter.hasNext()) return -1;
			return 0;
		}
		case @SuppressWarnings("preview") WorldThingValue wtv -> {
			WorldThing<?, ?> wt  = wtv.value();
			WorldThing<?, ?> owt = ((WorldThingValue) ov).value();
			if (wt == owt) return 0;
			TwoVals<WorldThing<?, ?>> tv = new TwoVals<>(wt, owt);
			try {
				Boolean old = things.put(tv, Boolean.FALSE);
				if (old != null && !old.booleanValue()) {
					return 0; // loop/recursion detected
				}
			} catch (IllegalStateException e) {
				if ("ab".equals(e.getMessage()) && tv.cmpRes != 0) {
					return tv.cmpRes;
				}
				throw e;
			}
			int cmp = wt.compare(owt, things);
			things.put(tv, Boolean.TRUE);
			return cmp;
		}
		}
	}
	
	@Override
	public int hashCode() {
		if (!this.knownHash) {
			calcHash();
		}
		return this.hash;
	}
	
	private void calcHash() {
		this.knownHash = true; // prevent the need to check if there is a loop. just use zero, until finish calculating.
		T   t = type();
		int h = 39;
		for (String key : t.values.keySet()) {
			Value v = value(key);
			switch (v) {
			case @SuppressWarnings("preview") JustAValue jav -> h = h * 14 + 5;
			case @SuppressWarnings("preview") IntValue iv -> h = h * 17 + iv.value();
			case @SuppressWarnings("preview") LongValue lv -> h = h * 15 + Long.hashCode(lv.value());
			case @SuppressWarnings("preview") DoubleValue dv -> h = h * 7 + Double.hashCode(dv.value());
			case @SuppressWarnings("preview") BooleanValue bv -> h = h * 3 + Boolean.hashCode(bv.value());
			case @SuppressWarnings("preview") EnumValue<?> ev -> h = h * 53 + ev.value().hashCode();
			case @SuppressWarnings("preview") StringValue sv -> h = h * 11 + sv.value().hashCode();
			case @SuppressWarnings("preview") UserValue uv -> h = h * 51 + uv.value().name().hashCode();
			case @SuppressWarnings("preview") UserListValue ulv -> {
				for (User usr : ulv.value()) {
					h = h * 37 + usr.name().hashCode();
				}
			}
			case @SuppressWarnings("preview") WorldThingValue wtv -> h = h * 41 + wtv.value().hashCode();
			}
		}
		this.hash = h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WorldThing<?, ?> wt)) {
			return false;
		}
		return compareTo(wt) == 0;
	}
	
	// needed to compare two enum values from an unknown class
	// fails if they have a different class
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> int cmp(Enum<T> v1, Enum<?> v2) {
		return v1.compareTo((T) v2);
	}
	
}
