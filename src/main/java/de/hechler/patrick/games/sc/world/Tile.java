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
package de.hechler.patrick.games.sc.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;

public class Tile {
	
	private Ground               ground;
	private final List<Resource> resources = new ArrayList<>();
	private Build                build;
	private final List<Unit>     units     = new ArrayList<>();
	
	public Tile(Ground ground) {
		this.ground = Objects.requireNonNull(ground, "ground is null");
	}
	
	public Ground ground() {
		return this.ground.unmodifiable();
	}
	
	public List<Resource> resourcesList() {
		return this.resources.stream().map(Resource::unmodifiable).toList();
	}
	
	public Stream<Resource> resourcesStream() {
		return this.resources.stream().map(Resource::unmodifiable);
	}
	
	public Build build() {
		return this.build != null ? this.build.unmodifiable() : null;
	}
	
	public List<Unit> unitsList() {
		return this.units.stream().map(Unit::unmodifiable).toList();
	}
	
	public Stream<Unit> unitsStream() {
		return this.units.stream().map(Unit::unmodifiable);
	}
	
}
