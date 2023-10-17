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
import java.util.function.Predicate;

import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.WorldThing;

@SuppressWarnings("javadoc")
public record WorldThingSpec(String name, String localName, Predicate<WorldThing<?, ?>> validator) implements ValueSpec {
	
	public WorldThingSpec {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(validator, "validator");
	}
	
	public String localName() {
		return this.localName == null ? this.name : this.localName;
	}
	
	public WorldThingValue withValue(WorldThing<?, ?> val) {
		if (this.validator.test(val)) throw new IllegalArgumentException("the given value is invalid");
		return new WorldThingValue(this.name, val);
	}
	
	@Override
	public void validate(Value v) throws IllegalArgumentException {
		if (!(v instanceof WorldThingValue wt)) throw new IllegalArgumentException("the given value is no world thing value");
		if (this.validator.test(wt.value())) throw new IllegalArgumentException("the given value is invalid");
	}
	
}

