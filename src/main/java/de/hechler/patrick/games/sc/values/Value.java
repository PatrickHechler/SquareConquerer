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
import de.hechler.patrick.games.sc.world.WorldThing;

public sealed interface Value {
	
	String name();
	
	record JustAValue(String name) implements Value {
		
		public JustAValue {
			Objects.requireNonNull(name, "name");
		}
		
	}
	
	record IntValue(String name, int value) implements Value {
		
		public IntValue {
			Objects.requireNonNull(name, "name");
		}
		
	}
	
	record LongValue(String name, long value) implements Value {
		
		public LongValue {
			Objects.requireNonNull(name, "name");
		}
		
	}
	
	record DoubleValue(String name, double value) implements Value {
		
		public DoubleValue {
			Objects.requireNonNull(name, "name");
		}
		
	}
	
	record BooleanValue(String name, boolean value) implements Value {
		
		public BooleanValue {
			Objects.requireNonNull(name, "name");
		}
		
	}
	
	record EnumValue<T extends Enum<T>>(String name, T value) implements Value {
		
		public EnumValue {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(value, "value");
		}
		
	}
	
	record StringValue(String name, String value) implements Value {
		
		public StringValue {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(value, "value");
		}
		
	}
	
	record UserValue(String name, User value) implements Value {
		
		public UserValue {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(value, "value");
		}
		
	}
	
	record UserListValue(String name, List<User> value) implements Value {
		
		public UserListValue(String name, List<User> value) {
			this.name  = Objects.requireNonNull(name, "name");
			this.value = List.copyOf(value);
		}
		
	}
	
	record WorldThingValue(String name, WorldThing<?,?> value) implements Value {
		
		public WorldThingValue {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(value, "value");
		}
		
	}
	
}
