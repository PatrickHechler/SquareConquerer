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
package de.hechler.patrick.games.sc.world;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import de.hechler.patrick.games.sc.Imagable;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.BooleanValue;
import de.hechler.patrick.games.sc.values.DoubleValue;
import de.hechler.patrick.games.sc.values.EnumValue;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.JustAValue;
import de.hechler.patrick.games.sc.values.LongValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.StringValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.UserListValue;
import de.hechler.patrick.games.sc.values.UserValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;

public abstract sealed class WorldThing<T extends AddableType<T, M>, M extends WorldThing<T, M>> implements Imagable, Comparable<WorldThing<?, ?>>
	permits Entity<?, ?>, Ground, Resource {
	
	// everything can block view, so this is here
	public static final String VIEW_BLOCK = "view:block";
	
	public final UUID uuid;
	
	private int     hash;
	private boolean knownHash;
	
	public WorldThing(UUID uuid) {
		this.uuid = Objects.requireNonNull(uuid);
	}
	
	public void resetHash() {
		this.hash      = 0;
		this.knownHash = false;
	}
	
	public abstract T type();
	
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
	
	public WorldThingValue worldThingValue(String name) {
		return (WorldThingValue) value(name);
	}
	
	@SuppressWarnings("unchecked")
	public <K extends Value, V extends Value> MapValue<K, V> mapValue(String name) {
		return (MapValue<K, V>) value(name);
	}
	
	@SuppressWarnings("unchecked")
	public <A extends AddableType<A, ?>> TypeValue<A> typeValue(String name) {
		return (TypeValue<A>) value(name);
	}
	
	public int viewBlock() {
		return intValue(VIEW_BLOCK).value();
	}
	
	// the order is needed for reproducibility
	@Override
	public final int compareTo(WorldThing<?, ?> o) {
		if (this == o) return 0;
		T                 t  = type();
		AddableType<?, ?> ot = o.type();
		if (t != ot) {
			int cmp = t.name.compareTo(ot.name);
			if (cmp != 0) return cmp;
			throw new AssertionError("types are not the same, but have the same name!");
		}
		for (String key : t.values.keySet()) {
			int cmp = compare(value(key), o.value(key));
			if (cmp != 0) return cmp;
		}
		return this.uuid.compareTo(o.uuid);
	}
	
	private static int compare(Value v, Value ov) {
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
			Iterator<User> iter  = ulv.value().iterator();
			Iterator<User> oiter = ((UserListValue) ov).value().iterator();
			while (iter.hasNext()) {
				if (!oiter.hasNext()) return 1;
				int cmp = iter.next().name().compareTo(oiter.next().name());
				if (cmp != 0) return cmp;
			}
			if (oiter.hasNext()) return -1;
			return 0;
		}
		case @SuppressWarnings("preview") WorldThingValue wtv -> {
			if (wtv.hasValue() ^ ((WorldThingValue) ov).hasValue()) {
				if (wtv.hasValue()) return 1;
				return -1;
			}
			UUID uuid  = wtv.value().uuid;
			UUID ouuid = ((WorldThingValue) ov).value().uuid;
			return uuid.compareTo(ouuid);
		}
		case @SuppressWarnings("preview") MapValue<?, ?> mv -> {
			return mapCompare(mv, (MapValue<?, ?>) ov);
		}
		case @SuppressWarnings("preview") TypeValue<?> tv -> {
			return tv.name().compareTo(((TypeValue<?>) ov).name());
		}
		}
	}
	
	private static int mapCompare(MapValue<?, ?> mv, MapValue<?, ?> omv) {
		Set<?>                                            oes  = omv.navigatableMap().entrySet();
		@SuppressWarnings("unchecked")
		Iterator<Entry<? extends Value, ? extends Value>> iter = ((Set<Entry<? extends Value, ? extends Value>>) oes).iterator();
		for (Entry<? extends Value, ? extends Value> e : mv.navigatableMap().entrySet()) {
			if (!iter.hasNext()) return 1;
			Entry<? extends Value, ? extends Value> oe = iter.next();
			Value                                   o  = e.getKey();
			Value                                   oo = oe.getKey();
			
			int cmp = o.name().compareTo(oo.name());
			if (cmp != 0) return cmp;
			cmp = compare(o, oo);
			if (cmp != 0) return cmp;
			
			o   = e.getValue();
			oo  = oe.getValue();
			cmp = o.name().compareTo(oo.name());
			if (cmp != 0) return cmp;
			cmp = compare(o, oo);
			if (cmp != 0) return cmp;
		}
		if (iter.hasNext()) return -1;
		return 0;
	}
	
	@Override
	public final int hashCode() {
		if (!this.knownHash) {
			calcHash();
		}
		return this.hash;
	}
	
	private void calcHash() {
		T   t = type();
		int h = 39;
		for (String key : t.values.navigableKeySet()) {
			h = hash(h, value(key));
		}
		this.knownHash = true;
		this.hash      = h;
	}
	
	@SuppressWarnings("unchecked")
	private static int hash(int h, Value v) {
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
		case @SuppressWarnings("preview") WorldThingValue wtv -> h = h * 41 + 4;
		case @SuppressWarnings("preview") MapValue<?, ?> mv -> {
			for (Entry<? extends Value, ? extends Value> e : (Set<Entry<? extends Value, ? extends Value>>) (Set<?>) mv.navigatableMap().entrySet()) {
				h = hash(h, e.getKey());
				h = hash(h, e.getValue());
			}
		}
		case @SuppressWarnings("preview") TypeValue<?> tv -> h = h * 39 + tv.value().name.hashCode();
		}
		return h;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WorldThing<?, ?> wt)) return false;
		if (this.uuid == wt.uuid) return true;
		return this.uuid.equals(wt.uuid);
	}
	
	public final boolean equals(WorldThing<?, ?> t) {
		return this.uuid == t.uuid || this.uuid.equals(t.uuid);
	}
	
	// needed to compare two enum values from an unknown class
	// fails if they have a different class
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> int cmp(Enum<T> v1, Enum<?> v2) {
		return v1.compareTo((T) v2);
	}
	
}
