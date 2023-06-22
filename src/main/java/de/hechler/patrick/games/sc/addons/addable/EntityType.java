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

import java.util.List;
import java.util.Map;

import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.entity.Entity;

public abstract sealed class EntityType<T extends EntityType<T, E>, E extends Entity<T, E>> extends AddableType<T, E> permits UnitType, BuildType {
	
	public EntityType(String name, String localName, Map<String, ValueSpec> values) {
		super(name, localName, values);
	}
	
	public abstract List<Map<String, Value>> startEntities();
	
}
