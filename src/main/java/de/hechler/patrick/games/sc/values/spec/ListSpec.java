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
package de.hechler.patrick.games.sc.values.spec;

import java.util.List;
import java.util.Objects;

import de.hechler.patrick.games.sc.values.ListValue;
import de.hechler.patrick.games.sc.values.Value;

@SuppressWarnings("javadoc")
public record ListSpec(String name, String localName, int minSize, int maxSize) implements ValueSpec {
	
	public ListSpec {
		Objects.requireNonNull(name, "name");
		if (minSize > maxSize || minSize < 0) {
			throw new IllegalArgumentException("min=" + minSize + " max=" + maxSize);
		}
	}
	
	public String localName() {
		return this.localName == null ? this.name : this.localName;
	}
	
	public ListValue withValue(List<Value> val) {
		int s = val.size();
		if (s < this.minSize || s > this.maxSize) {
			throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
		}
		return new ListValue(this.name, val);
	}
	
	public ListValue withValue(Value... val) {
		int s = val.length;
		if (s < this.minSize || s > this.maxSize) {
			throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
		}
		return new ListValue(this.name, List.of(val));
	}
	
	@Override
	public void validate(Value v) {
		if (!(v instanceof ListValue d)) throw new IllegalArgumentException("the given value is no user list value");
		if (d.value().size() < this.minSize || d.value().size() > this.maxSize)
			throw new IllegalArgumentException("the given values size is outside of the allowed bounds");
	}
	
}

