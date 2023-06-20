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
package de.hechler.patrick.games.sc.world.tile;

import java.util.List;
import java.util.stream.Stream;

import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;

public interface Tile {
	
	Ground ground();
	
	int resourceCount();
	
	Resource resource(int index);
	
	List<Resource> resourcesList();
	
	Stream<Resource> resourcesStream();
	
	Build build();
	
	int unitCount();
	
	Unit unit(int index);
	
	List<Unit> unitsList();
	
	Stream<Unit> unitsStream();
	
	Tile unmodifiable();
	
}
