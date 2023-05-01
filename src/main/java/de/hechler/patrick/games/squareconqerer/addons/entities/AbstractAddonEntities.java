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
package de.hechler.patrick.games.squareconqerer.addons.entities;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public abstract class AbstractAddonEntities implements AddonEntities {
	
	private final Map<Class<? extends Entity>, String> entitiCls;
	
	public AbstractAddonEntities(Map<Class<? extends Entity>, String> entitiCls) {
		this.entitiCls = Collections.unmodifiableMap(new HashMap<>(entitiCls));
	}
	
	@Override
	public void sendUnit(Connection conn, Unit u) throws IOException {
		conn.writeInt(u.x());
		conn.writeInt(u.y());
		conn.writeInt(u.lives());
		int ca = u.carryAmount();
		conn.writeInt(ca);
		if (ca != 0) {
			OpenWorld.writeRes(conn, u.carryRes());
		}
		finishSendUnit(conn, u);
	}
	
	protected abstract void finishSendUnit(Connection conn, Unit u) throws IOException;
	
	@Override
	public Unit recieveUnit(Connection conn, User usr) throws IOException, StreamCorruptedException {
		int      x     = conn.readPos();
		int      y     = conn.readPos();
		int      lives = conn.readStrictPos();
		int      ca    = conn.readPos();
		Resource res   = null;
		if (ca != 0) {
			res = RemoteWorld.readRes(conn);
		}
		return finishRecieveUnit(conn, usr, x, y, lives, ca, res);
	}
	
	protected abstract Unit finishRecieveUnit(Connection conn, User usr, int x, int y, int lives, int ca, Resource res) throws IOException;
	
	@Override
	public void sendBuild(Connection conn, Building b) throws IOException {
		conn.writeInt(b.x());
		conn.writeInt(b.y());
		conn.writeInt(b.lives());
		boolean fb = b.isFinishedBuild();
		conn.writeByte(fb ? 1 : 0);
		if (!fb) {
			conn.writeInt(b.remainingBuildTurns());
			EnumIntMap<ProducableResourceType> res = b.neededResources();
			if (res == null) {
				conn.writeInt(0);
			} else {
				int[] arr = res.array();
				conn.writeInt(arr.length);
				for (int i = 0; i < arr.length; i++) {
					conn.writeInt(arr[i]);
				}
			}
		}
		finishSendBuild(conn, b);
	}
	
	protected abstract void finishSendBuild(Connection conn, Building b) throws IOException, StreamCorruptedException;
	
	@Override
	public Building recieveBuild(Connection conn, User usr) throws IOException, StreamCorruptedException {
		int                                x          = conn.readPos();
		int                                y          = conn.readPos();
		int                                lives      = conn.readStrictPos();
		boolean                            fb         = conn.readByte(0, 1) != 0;
		int                                buildTurns = 0;
		EnumIntMap<ProducableResourceType> res        = null;
		if (!fb) {
			buildTurns = conn.readPos();
			if (conn.readInt(0, ProducableResourceType.count()) != 0) {
				res = new EnumIntMap<>(ProducableResourceType.class);
				int[] arr = res.array();
				for (int i = 0; i < arr.length; i++) {
					arr[i] = conn.readPos();
				}
			}
		}
		return finishRecieveBuild(conn, usr, x, y, lives, fb, buildTurns, res);
	}
	
	protected abstract Building finishRecieveBuild(Connection conn, User usr, int x, int y, int lives, boolean fb, int remTurns,
			EnumIntMap<ProducableResourceType> res) throws IOException, StreamCorruptedException;
	
	@Override
	public Map<Class<? extends Entity>, String> entityClassses() { return this.entitiCls; }
	
}
