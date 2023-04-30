//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.addons.entities;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public class TheGameEntities extends AbstractAddonEntities {
	
	public TheGameEntities() {
		super(Map.of(Carrier.class, Carrier.NAME, StoreBuild.class, StoreBuild.NAME));
	}
	
	@Override
	protected void finishSendUnit(Connection conn, Unit u) throws IOException {
		if (u instanceof Carrier) {
			conn.writeInt(Carrier.NUMBER);
		} else {
			throw new AssertionError("unknown unit type: " + u.getClass());
		}
	}
	
	@Override
	protected Unit finishRecieveUnit(Connection conn, User usr, int x, int y, int lives, int ca, Resource res) throws IOException {
		conn.readInt(Carrier.NUMBER);
		return new Carrier(x, y, usr, lives, res, ca);
	}
	
	@Override
	@SuppressWarnings("preview")
	protected void finishSendBuild(Connection conn, Building b) throws IOException {
		switch (b) {
		case StoreBuild sb -> {
			conn.writeInt(StoreBuild.NUMBER);
			if (sb.isFinishedBuild()) {
				EnumIntMap<OreResourceType> ores = sb.ores();
				int[]                       oa   = ores.array();
				conn.writeInt(oa.length);
				for (int i = 0; i < oa.length; i++) {
					conn.writeInt(oa[i]);
				}
				EnumIntMap<ProducableResourceType> producable = sb.producable();
				int[]                              pa         = producable.array();
				conn.writeInt(pa.length);
				for (int i = 0; i < pa.length; i++) {
					conn.writeInt(pa[i]);
				}
			}
		}
		default -> throw new AssertionError("unknown building type: " + b.getClass());
		}
	}
	
	@Override
	protected Building finishRecieveBuild(Connection conn, User usr, int x, int y, int lives, boolean fb, int remTurns,
			EnumIntMap<ProducableResourceType> res) throws IOException {
		conn.readInt(StoreBuild.NUMBER);
		EnumIntMap<OreResourceType>        os = new EnumIntMap<>(OreResourceType.class);
		EnumIntMap<ProducableResourceType> ps = new EnumIntMap<>(ProducableResourceType.class);
		if (fb) {
			int[] arr = os.array();
			conn.readInt(arr.length);
			for (int i = 0; i < arr.length; i++) {
				arr[i] = conn.readInt();
			}
			arr = ps.array();
			conn.readInt(arr.length);
			for (int i = 0; i < arr.length; i++) {
				arr[i] = conn.readInt();
			}
		}
		return new StoreBuild(x, y, usr, lives, res, remTurns, os, ps);
	}
	
	@Override
	public Map<String, EntityTrait> traits(String clsName) {
		return switch (clsName) {
		case Carrier.NAME -> Collections.emptyMap();
		case StoreBuild.NAME -> Collections.emptyMap();
		default -> throw new AssertionError("unknown class name: " + clsName);
		};
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Entity> E createEntity(String clsName, User usr, Map<String, EntityTraitWithVal> traits, int x, int y) {
		return (E) switch (clsName) {
		case Carrier.NAME -> new Carrier(x, y, usr);
		case StoreBuild.NAME -> new StoreBuild(x, y, usr);
		default -> throw new AssertionError("unknown class name: " + clsName);
		};
	}
	
}
