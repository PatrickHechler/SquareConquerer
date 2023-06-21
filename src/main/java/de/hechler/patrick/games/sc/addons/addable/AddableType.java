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
package de.hechler.patrick.games.sc.addons.addable;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import de.hechler.patrick.games.sc.values.ValueSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.BooleanSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.DoubleSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.EnumSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.IntSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.JustASpec;
import de.hechler.patrick.games.sc.values.ValueSpec.LongSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.StringSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.UserListSpec;
import de.hechler.patrick.games.sc.values.ValueSpec.UserSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.utils.objects.Random2;

public abstract sealed class AddableType<M extends AddableType<M, A>, A extends WorldThing<M, A>> permits EntityType<?, ?>, GroundType, ResourceType {
	
	public final String                          name;
	public final String                          localName;
	public final NavigableMap<String, ValueSpec> values;
	
	public AddableType(String name, String localName, Map<String, ValueSpec> values) {
		this.name      = Objects.requireNonNull(name, "name");
		this.localName = Objects.requireNonNullElse(localName, name);
		this.values    = Collections.unmodifiableNavigableMap(new TreeMap<>(values));
	}
	
	public abstract A withDefaultValues(World w, Random2 r, int x, int y);
	
	public abstract A withRandomValues(World w, Random2 r, int x, int y);
	
	public abstract Map<String, ValueSpec> specs();
	
	public abstract ValueSpec spec(String name);
	
	public JustASpec justSpec(String name) {
		return (JustASpec) spec(name);
	}
	
	public IntSpec intSpec(String name) {
		return (IntSpec) spec(name);
	}
	
	public LongSpec longSpec(String name) {
		return (LongSpec) spec(name);
	}
	
	public DoubleSpec doubleSpec(String name) {
		return (DoubleSpec) spec(name);
	}
	
	public BooleanSpec booleanSpec(String name) {
		return (BooleanSpec) spec(name);
	}
	
	public EnumSpec<?> enumSpec(String name) {
		return (EnumSpec<?>) spec(name);
	}
	
	public StringSpec stringSpec(String name) {
		return (StringSpec) spec(name);
	}
	
	public UserSpec userSpec(String name) {
		return (UserSpec) spec(name);
	}
	
	public UserListSpec userListSpec(String name) {
		return (UserListSpec) spec(name);
	}
	
}
