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
package de.hechler.patrick.games.sc.world.ground;

import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.world.WorldThing;

public abstract non-sealed class Ground extends WorldThing<GroundType, Ground> {
	
	public Ground(UUID uuid) {
		super(uuid);
	}
	
	public static final String LAND_WALK   = "walk:land";
	public static final String WATER_WALK  = "walk:water";
	public static final String HILL_HEIGTH = "walk:hills";
	
}
