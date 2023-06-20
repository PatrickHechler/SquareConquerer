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

import java.util.List;
import java.util.Objects;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.Value.BooleanValue;
import de.hechler.patrick.games.sc.values.Value.DoubleValue;
import de.hechler.patrick.games.sc.values.Value.EnumValue;
import de.hechler.patrick.games.sc.values.Value.IntValue;
import de.hechler.patrick.games.sc.values.Value.JustAValue;
import de.hechler.patrick.games.sc.values.Value.LongValue;
import de.hechler.patrick.games.sc.values.Value.StringValue;
import de.hechler.patrick.games.sc.values.Value.UserListValue;
import de.hechler.patrick.games.sc.values.Value.UserValue;

public sealed interface ValueSpec {
	
	String name();
	
	record JustASpec(String name) implements ValueSpec {
		
		public JustASpec {
			Objects.requireNonNull(name, "name");
		}
		
		public JustAValue asValue() {
			return new JustAValue(this.name);
		}

	}
	
	record IntSpec(String name, long min, long max) implements ValueSpec {
		
		public IntSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public IntValue withValue(int val) {
			if (val < this.min || val > this.max) {
				throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
			}
			return new IntValue(this.name, val);
		}
		
	}
	
	record LongSpec(String name, long min, long max) implements ValueSpec {
		
		public LongSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public LongValue withValue(long val) {
			if (val < this.min || val > this.max) {
				throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
			}
			return new LongValue(this.name, val);
		}
		
	}
	
	record DoubleSpec(String name, double min, double max) implements ValueSpec {
		
		public DoubleSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public DoubleValue withValue(double val) {
			if (val < this.min || val > this.max) {
				throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
			}
			return new DoubleValue(this.name, val);
		}
		
	}
	
	record BooleanSpec(String name) implements ValueSpec {
		
		public BooleanSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public BooleanValue withValue(boolean val) {
			return new BooleanValue(this.name, val);
		}
		
	}
	
	record EnumSpec<T extends Enum<T>>(String name, Class<T> cls) implements ValueSpec {
		
		public EnumSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public EnumValue<T> withValue(T val) {
			if (!this.cls.isInstance(val)) {
				throw new IllegalArgumentException("the value is no instance of the given class: " + this.cls + " def: " + (val != null ? val.getClass() : "null"));
			}
			return new EnumValue<>(this.name, val);
		}
		
	}
	
	record StringSpec(String name) implements ValueSpec {
		
		public StringSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public StringValue withValue(String val) {
			return new StringValue(this.name, val);
		}
		
	}
	
	record UserSpec(String name) implements ValueSpec {
		
		public UserSpec {
			Objects.requireNonNull(name, "name");
		}
		
		public UserValue withValue(User val) {
			return new UserValue(this.name, val);
		}
		
	}
	
	record UserListSpec(String name, int minSize, int maxSize) implements ValueSpec {
		
		public UserListSpec {
			Objects.requireNonNull(name, "name");
			if (minSize > maxSize || minSize < 0) {
				throw new IllegalArgumentException("min=" + minSize + " max=" + maxSize);
			}
		}
		
		public UserListValue withValue(List<User> val) {
			int s = val.size();
			if (s < this.minSize || s > this.maxSize) {
				throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
			}
			return new UserListValue(this.name, val);
		}
		
		public UserListValue withValue(User... val) {
			int s = val.length;
			if (s < this.minSize || s > this.maxSize) {
				throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
			}
			return new UserListValue(this.name, List.of(val));
		}
		
	}
	
}
