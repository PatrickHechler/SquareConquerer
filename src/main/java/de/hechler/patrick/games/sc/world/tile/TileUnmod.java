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

import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;

public final class TileUnmod implements Tile {
	
	private final Tile t;
	
	public TileUnmod(Tile t) {
		this.t = t;
	}
	
	public Ground ground() {
		return this.t.ground();
	}
	
	public int resourceCount() {
		return this.t.resourceCount();
	}
	
	public Resource resource(int index) {
		return this.t.resource(index);
	}
	
	public List<Resource> resourcesList() {
		return this.t.resourcesList();
	}
	
	public Stream<Resource> resourcesStream() {
		return this.t.resourcesStream();
	}
	
	public Build build() {
		return this.t.build();
	}
	
	public int unitCount() {
		return this.t.unitCount();
	}
	
	public Unit unit(int index) {
		return this.t.unit(index);
	}
	
	public List<Unit> unitsList() {
		return this.t.unitsList();
	}
	
	public Stream<Unit> unitsStream() {
		return this.t.unitsStream();
	}
	
	@Override
	public Tile unmodifiable() {
		return this;
	}
	
	@Override
	public TileImpl copy() {
		return this.t.copy();
	}
	
	@Override
	public Entity<?, ?>[] entities() {
		return this.t.entities();
	}
	
	@Override
	public void addUnit(Unit u) throws TurnExecutionException {
		throw new UnsupportedOperationException("this tile is unmodifiable");
	}
	
	@Override
	public void removeUnit(Unit u) throws TurnExecutionException {
		throw new UnsupportedOperationException("this tile is unmodifiable");
	}
	
	@Override
	public void setBuild(Build b) throws TurnExecutionException {
		throw new UnsupportedOperationException("this tile is unmodifiable");
	}
	
	@Override
	public boolean same(Tile t) {
		if (t instanceof TileUnmod tu) {
			t = tu.t;
		}
		return this.t.same(t);
	}
	
}
