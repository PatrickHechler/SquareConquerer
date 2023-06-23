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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.turn.NextTurnListener;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.tile.Tile;

public interface World {
	
	User user();
	
	int xlen();
	
	int ylen();
	
	int turn();
	
	Tile tile(int x, int y);

	void addNextTurnListener(NextTurnListener listener);
	
	void removeNextTurnListener(NextTurnListener listener);
	
	void finish(Turn t);
	
	Map<User, List<Entity<?, ?>>> entities();

	WorldThing<?, ?> get(UUID uuid);
	
}
