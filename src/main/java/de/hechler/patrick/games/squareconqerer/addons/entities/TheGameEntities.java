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
import java.util.Map;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Storage;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this is the {@link AddonEntities} implementation for the {@link TheGameAddon}
 * 
 * @author Patrick Hechler
 */
public class TheGameEntities extends AbstractAddonEntities {
	
	private static final String UNKNOWN_CLASS_NAME                    = Messages.get("TheGameEntities.unknown-type");     //$NON-NLS-1$
	private static final String INVALID_MAX_LIVES_OR_VIEW_RANGE_TRAIT = Messages.get("TheGameEntities.invlaid-trait");    //$NON-NLS-1$
	private static final String UNKNOWN_BUILDING_TYPE                 = Messages.get("TheGameEntities.unknown-building"); //$NON-NLS-1$
	private static final String UNKNOWN_UNIT_TYPE                     = Messages.get("TheGameEntities.unknown-unit");     //$NON-NLS-1$
	
	/**
	 * creates a new instance
	 */
	public TheGameEntities() {
		super(Map.of(Carrier.class, Carrier.NAME, Storage.class, Storage.NAME));
	}
	
	/** {@inheritDoc} */
	@Override
	protected void finishSendUnit(Connection conn, Unit u) throws IOException {
		switch (u) {
		case @SuppressWarnings("preview") Carrier c -> conn.writeInt(Carrier.NUMBER);
		default -> throw new AssertionError(UNKNOWN_UNIT_TYPE + u.getClass());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected Unit finishRecieveUnit(Connection conn, User usr, int x, int y, int lives, int ca, Resource res) throws IOException {
		conn.readInt(Carrier.NUMBER);
		return new Carrier(x, y, usr, lives, res, ca);
	}
	
	/** {@inheritDoc} */
	@Override
	protected void finishSendBuild(Connection conn, Building b) throws IOException {
		switch (b) {
		case @SuppressWarnings("preview") Storage sb -> {
			conn.writeInt(Storage.NUMBER);
			if (sb.isFinishedBuild()) {
				IntMap<OreResourceType> ores = sb.ores();
				int[]                   oa   = ores.array();
				conn.writeInt(oa.length);
				for (int i = 0; i < oa.length; i++) {
					conn.writeInt(oa[i]);
				}
				IntMap<ProducableResourceType> producable = sb.producable();
				int[]                          pa         = producable.array();
				conn.writeInt(pa.length);
				for (int i = 0; i < pa.length; i++) {
					conn.writeInt(pa[i]);
				}
			}
		}
		default -> throw new AssertionError(UNKNOWN_BUILDING_TYPE + b.getClass());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected Building finishRecieveBuild(Connection conn, User usr, int x, int y, int lives, boolean fb, int remTurns, IntMap<ProducableResourceType> res)
		throws IOException {
		conn.readInt(Storage.NUMBER);
		IntMap<OreResourceType>        os = IntMap.create(OreResourceType.class);
		IntMap<ProducableResourceType> ps = IntMap.create(ProducableResourceType.class);
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
		return new Storage(x, y, usr, lives, res, remTurns, os, ps);
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String, EntityTrait> traits(String clsName) {
		return switch (clsName) {
		case Carrier.NAME -> Map.of(//
			EntityTrait.TRAIT_VIEW_RANGE, new EntityTrait.NumberTrait(EntityTrait.TRAIT_VIEW_RANGE, Carrier.VIEW_RANGE), //
			EntityTrait.TRAIT_MAX_LIVES, new EntityTrait.NumberTrait(EntityTrait.TRAIT_MAX_LIVES, Carrier.MAX_LIVES), //
			EntityTrait.TRAIT_LIVES, new EntityTrait.NumberTrait(EntityTrait.TRAIT_LIVES, 0, Carrier.MAX_LIVES, Carrier.MAX_LIVES));
		case Storage.NAME -> Map.of(//
			EntityTrait.TRAIT_VIEW_RANGE, new EntityTrait.NumberTrait(EntityTrait.TRAIT_VIEW_RANGE, Storage.VIEW_RANGE), //
			EntityTrait.TRAIT_MAX_LIVES, new EntityTrait.NumberTrait(EntityTrait.TRAIT_MAX_LIVES, Storage.MAX_LIVES), //
			EntityTrait.TRAIT_LIVES, new EntityTrait.NumberTrait(EntityTrait.TRAIT_LIVES, 0, Storage.MAX_LIVES, Storage.MAX_LIVES));
		default -> throw new AssertionError(UNKNOWN_CLASS_NAME + clsName);
		};
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Entity> E createEntity(String clsName, User usr, Map<String, EntityTraitWithVal> traits, int x, int y) {
		return (E) switch (clsName) {
		case Carrier.NAME -> {
			if (EntityTrait.intValue(traits, EntityTrait.TRAIT_MAX_LIVES) != Carrier.MAX_LIVES
				|| EntityTrait.intValue(traits, EntityTrait.TRAIT_VIEW_RANGE) != Carrier.VIEW_RANGE) {
				throw new IllegalArgumentException(INVALID_MAX_LIVES_OR_VIEW_RANGE_TRAIT + traits);
			}
			yield new Carrier(x, y, usr, EntityTrait.intValue(traits, EntityTrait.TRAIT_LIVES), null, 0);
		}
		case Storage.NAME -> {
			if (EntityTrait.intValue(traits, EntityTrait.TRAIT_MAX_LIVES) != Storage.MAX_LIVES
				|| EntityTrait.intValue(traits, EntityTrait.TRAIT_VIEW_RANGE) != Storage.VIEW_RANGE) {
				throw new IllegalArgumentException(INVALID_MAX_LIVES_OR_VIEW_RANGE_TRAIT + traits);
			}
			yield new Storage(x, y, usr, EntityTrait.intValue(traits, EntityTrait.TRAIT_LIVES), null, 0, IntMap.create(OreResourceType.class),
				IntMap.create(ProducableResourceType.class));
		}
		default -> throw new AssertionError(UNKNOWN_CLASS_NAME + clsName);
		};
	}
	
}
