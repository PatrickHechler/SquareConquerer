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

import java.util.Objects;

import de.hechler.patrick.games.sc.values.EnumValue;
import de.hechler.patrick.games.sc.values.Value;

@SuppressWarnings("javadoc")
public record EnumSpec<T extends Enum<T>>(String name, String localName, Class<T> cls) implements ValueSpec {
	
	public EnumSpec {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(cls, "class");
	}
	
	public String localName() {
		return this.localName == null ? this.name : this.localName;
	}
	
	public EnumValue<T> withValue(T val) {
		if (!this.cls.isInstance(val)) {
			throw new IllegalArgumentException("the value is no instance of the given class: " + this.cls + " def: " + (val != null ? val.getClass() : "null"));
		}
		return new EnumValue<>(this.name, val);
	}

	@Override
	public void validate(Value v) { 
		if (!(v instanceof EnumValue<?> e)) throw new IllegalArgumentException("the given value is no enum value");
		if (!this.cls.isInstance(e)) throw new IllegalArgumentException("the given enum value is of the wron type");
	 }
	
}

