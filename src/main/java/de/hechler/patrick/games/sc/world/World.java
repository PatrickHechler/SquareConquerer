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

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.WorldType;
import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.tile.Tile;

public abstract non-sealed class World<P extends Pos> extends WorldThing<WorldType<P>, World<P>> {
	
	public static final UUID NULL_UUID = new UUID(0L, 0L);

	public World() {
		super(NULL_UUID);
	}
	
	public World(UUID uuid) {
		super(uuid);
	}
	
	public abstract User user();
	
	public abstract int turn();
	
	public abstract int xlen();
	
	public abstract int ylen();
	
	public abstract Tile tile(int x, int y);
	
	public abstract void addNextTurnListener(NextTurnListener listener);
	
	public abstract void removeNextTurnListener(NextTurnListener listener);
	
	public abstract void finish(Turn t);
	
	public abstract Map<User, List<Entity<?, ?>>> entities();
	
	public abstract WorldThing<?, ?> get(UUID uuid);
	
	@Override
	public Image image(int width, int height) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void value(Value newValue) {
		throw new UnsupportedOperationException();
	}
	
}
