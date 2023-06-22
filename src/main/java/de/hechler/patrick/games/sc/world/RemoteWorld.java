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
import java.util.function.IntConsumer;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;

public class RemoteWorld implements World {
	
	public RemoteWorld(Connection conn) {
		
	}
	
	public static Tile[][] loadWorld(Connection conn, Map<String, User> users) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void loadWorld(Connection conn, Map<String, User> users, Tile[][] cach) {
		// TODO Auto-generated method stub
	}
	
	public static Resource readRes(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public User user() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int xlen() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int ylen() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Tile tile(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addNextTurnListener(IntConsumer listener) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeNextTurnListener(IntConsumer listener) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void finish(Turn t) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Map<User, List<Entity<?, ?>>> entities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorldThing<?, ?> get(UUID uuid) { 
	// TODO Auto-generated method stub
	return null; }
	
}
