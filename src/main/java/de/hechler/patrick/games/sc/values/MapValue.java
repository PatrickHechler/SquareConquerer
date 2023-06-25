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
package de.hechler.patrick.games.sc.values;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public record MapValue<V extends Value>(String name, Map<String, V> value) implements Value {
	
	@SuppressWarnings("cast")
	public MapValue(String name, Map<String, V> value) {
		this.name  = Objects.requireNonNull(name, "name");
		this.value = Collections.unmodifiableNavigableMap(new TreeMap<>(value));
		this.value.forEach((k, v) -> {
			Objects.requireNonNull(k, "entry.key");
			Objects.requireNonNull(v, "entry.value");
			if (k.getClass() != String.class || !(v instanceof Value)) {
				throw new AssertionError("key or value are from an illegal type");
			}
		});
	}
	
	public NavigableMap<String, V> navigatableMap() {
		return (NavigableMap<String, V>) this.value;
	}
	
}

