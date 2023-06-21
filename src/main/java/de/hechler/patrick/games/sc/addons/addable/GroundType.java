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

import java.io.IOError;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.ground.SimpleGroundType;

public abstract non-sealed class GroundType extends AddableType<GroundType, Ground> {
	
	public GroundType(String name, String localName, Map<String, ValueSpec> values) {
		super(name, localName, values);
	}
	
	public static final SimpleGroundType NOT_EXPLORED_TYPE = new SimpleGroundType("base:not_explored", "not yet explored", () -> {
		try {
			return ImageIO.read(GroundType.class.getResource("/img/ground/not-explored.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	});
	
}
