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

import de.hechler.patrick.games.sc.values.BooleanValue;
import de.hechler.patrick.games.sc.values.Value;

@SuppressWarnings("javadoc")
public record BooleanSpec(String name, String localName) implements ValueSpec {
	
	public BooleanSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public String localName() {
		return this.localName == null ? this.name : this.localName;
	}
	
	public BooleanValue withValue(boolean val) {
		return new BooleanValue(this.name, val);
	}
	
	@Override
	public void validate(Value v) {
		if (!(v instanceof BooleanValue)) throw new IllegalArgumentException("the given value is no boolean value");
	}
	
}

