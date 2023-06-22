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

import de.hechler.patrick.games.sc.values.DoubleValue;
import de.hechler.patrick.games.sc.values.Value;

@SuppressWarnings("javadoc")
public record DoubleSpec(String name, double min, double max) implements ValueSpec {
	
	public DoubleSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public DoubleValue withValue(double val) {
		if (val < this.min || val > this.max) {
			throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
		}
		return new DoubleValue(this.name, val);
	}
	
	@Override
	public void validate(Value v) {
		if (!(v instanceof DoubleValue d)) throw new IllegalArgumentException("the given value is no double value");
		if (d.value() < this.min || d.value() > this.max) throw new IllegalArgumentException("the given value is outside of the allowed bounds");
	}
	
}

