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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.turn.Turn;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.interfaces.Executable;

@SuppressWarnings("javadoc")
public class RemoteWorld implements World, Executable<IOException> {
	
	private final Connection  conn;
	private volatile Tile[][] tiles;
	private volatile int      turn = -2;
	private List<IntConsumer> ntl;
	
	public RemoteWorld(Connection conn) {
		this.conn = conn;
		this.ntl  = new ArrayList<>();
	}
	
	@Override
	public void execute() throws IOException {
		while (!this.conn.closed()) {
			OpenWorld.acceptBlockClient(this.conn, 250, this::exec);
		}
	}
	
	private void exec() throws IOException {
		this.conn.readInt(OpenWorld.NOTIFY_NEXT_TURN);
		int turn = this.conn.readInt();
		if (turn < -1) { // fails anyway
			turn = this.conn.wrongInputPositive(turn, false);
		}
		for (IntConsumer c : this.ntl) {
			c.accept(turn);
		}
	}
	
	@Override
	public User user() {
		return this.conn.usr;
	}
	
	@Override
	public int xlen() {
		Tile[][] ts = this.tiles;
		while (ts == null) {
			update();
			ts = this.tiles;
		}
		return ts.length;
	}
	
	@Override
	public int ylen() {
		Tile[][] ts = this.tiles;
		while (ts == null) {
			update();
			ts = this.tiles;
		}
		return ts[0].length;
	}
	
	@Override
	public int turn() {
		int t = this.turn;
		while (t < -1) {
			updateTurn();
			t = this.turn;
		}
		return 0;
	}
	
	@Override
	public Tile tile(int x, int y) {
		Tile[][] ts = this.tiles;
		while (ts == null) {
			update();
			ts = this.tiles;
		}
		return ts[x][y];
	}
	
	@Override
	public void addNextTurnListener(IntConsumer listener) {
		this.ntl.add(listener);
	}
	
	@Override
	public void removeNextTurnListener(IntConsumer listener) {
		if (!this.ntl.remove(listener)) {
			throw new IllegalArgumentException("the given listener was not registered!");
		}
	}
	
	@Override
	public void finish(Turn t) {
		try {
			OpenWorld.doBlockedClient(this.conn, () -> {
				this.conn.writeInt(OpenWorld.MAKE_TURN);
				this.conn.readInt(OpenWorld.MAKE_TURN);
				t.sendTurn(this.conn);
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public Map<User, List<Entity<?, ?>>> entities() {
		Tile[][] ts = this.tiles;
		while (ts == null) {
			update();
			ts = this.tiles;
		}
		return CompleteWorld.entities(ts);
	}
	
	@Override
	public WorldThing<?, ?> get(UUID uuid) {
		Tile[][] ts = this.tiles;
		while (ts == null) {
			update();
			ts = this.tiles;
		}
		return CompleteWorld.get(ts, uuid);
	}
	
	private synchronized void update() {
		final Tile[][] ts = this.tiles;
		if (ts != null) {
			return;
		}
		try {
			OpenWorld.doBlockedClient(this.conn, () -> {
				this.conn.writeInt(OpenWorld.GET_WORLD);
				this.tiles = OpenWorld.loadWorld(ts, this.conn);
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private synchronized void updateTurn() {
		if (this.turn >= -1) {
			return;
		}
		try {
			OpenWorld.doBlockedClient(this.conn, () -> {
				this.conn.writeInt(OpenWorld.GET_TURN);
				int t = this.conn.readInt();
				if (t < -1) {
					t = this.conn.wrongInputPositive(t, false);
				}
				this.turn = t;
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
}
