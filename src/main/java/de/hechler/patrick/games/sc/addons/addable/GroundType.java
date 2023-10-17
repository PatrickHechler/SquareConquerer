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

import java.awt.Image;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.ground.SimpleGroundType;
import de.hechler.patrick.games.sc.world.tile.NeigbourTiles;
import de.hechler.patrick.utils.objects.ACORNRandom;

public abstract non-sealed class GroundType extends AddableType<GroundType, Ground> {
	
	public GroundType(String name, String localName, Map<String, ValueSpec> values) {
		super(name, localName, values);
	}
	
	public abstract int propability(World world, int x, int y, NeigbourTiles neigbours);
	
	public abstract Ground withNeigbours(World world, int x, int y, NeigbourTiles neigbours);
	
	@SuppressWarnings("unused")
	public static final SimpleGroundType NOT_EXPLORED_TYPE = new SimpleGroundType("base:not_explored", "not yet explored",
			Map.of(WorldThing.VIEW_BLOCK, new IntSpec(WorldThing.VIEW_BLOCK, WorldThing.VIEW_BLOCK_LOC, Integer.MAX_VALUE, Integer.MAX_VALUE)),
			Map.of(WorldThing.VIEW_BLOCK, new IntValue(WorldThing.VIEW_BLOCK, Integer.MAX_VALUE))) {
		
		@Override
		protected Image loadImage() {
			try {
				return ImageIO.read(GroundType.class.getResource("/img/ground/not-explored.png"));
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		
		@Override
		public int propability(World world, int x, int y, NeigbourTiles neigbours) {
			return 0;
		}
		
		@Override
		public Ground withNeigbours(World world, int x, int y, NeigbourTiles neigbours) {
			throw new AssertionError("withNeigbours called, but my probability is zero!");
		}
		
		@Override
		public Ground withDefaultValues(World w, ACORNRandom r, int x, int y) {
			throw new AssertionError("withDefaultValues called!");
		}
		
		@Override
		public Ground withRandomValues(World w, ACORNRandom r, int x, int y) {
			throw new AssertionError("withRandomValues called!");
		}
		
	};
	
	public static final Ground NOT_EXPLORED_GRND = NOT_EXPLORED_TYPE.newInstance(new UUID(0L, 0L));
	
}

