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
package de.hechler.patrick.games.sc.world.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.BuildType;
import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.utils.objects.ACORNRandom;

public abstract non-sealed class Build extends Entity<BuildType, Build> {
	
	public static final String STORE                 = "store";
	public static final String STORE_LOC             = "stored resources";
	public static final String NEEDED_RESOURCES      = "need:resource";
	public static final String NEEDED_RESOURCES_LOC  = "need resource for construction";
	public static final String NEEDED_WORK_TURNS     = "need:work:turns";
	public static final String NEEDED_WORK_TURNS_LOC = "need work turns for construction";
	
	public Build(UUID uuid) {
		super(uuid);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, WorldThingValue> resources() {
		return (Map) mapValue(STORE).value();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Optional<Resource> resource(ResourceType res) {
		Map<String, WorldThingValue> map = resources();
		WorldThingValue              val = map.get(res.name);
		if (val == null) return Optional.empty();
		return (Optional) val.asOptional();
	}
	
	public void giveRes(Unit u, Resource res, ACORNRandom r) throws TurnExecutionException {
		Resource myRes = resource(res.type()).orElseThrow(() -> new TurnExecutionException(ErrorType.INVALID_TURN));
		Resource add   = myRes.sub(res, r);
		try {
			Resource reAdd = u.addResource(add);
			myRes.add(reAdd);
			if (myRes.amount() == 0) {
				Map<String, WorldThingValue> all = new HashMap<>(resources());
				all.remove(myRes.type().name);
			}
		} catch (Throwable t) {
			myRes.add(add);
			throw t;
		}
	}
	
	public void store(Unit u, Resource resource) {
		resource(resource.type()).ifPresentOrElse(r -> r.add(resource), () -> {
			ResourceType                 res = resource.type();
			Map<String, WorldThingValue> map = new HashMap<>(resources());
			map.put(res.name, new WorldThingValue(res.name, resource));
			value(new MapValue<>(STORE, map));
		});
	}
	
	public void work(Unit u) throws TurnExecutionException {
		Map<String, Value> map = mapValue(NEEDED_RESOURCES).value();
		if (!map.isEmpty()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		int val = intValue(NEEDED_WORK_TURNS).value();
		if (val < 1) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		int e = u.workEfficency();
		if (e < 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		value(new IntValue(NEEDED_WORK_TURNS, Math.max(0, val - e)));
	}
	
	/** {@inheritDoc} */
	@Override
	public int defend(@SuppressWarnings("unused") Unit unit, int attackStrength) {
		if (attackStrength < 0) throw new AssertionError();
		value(new IntValue(LIVES, Math.max(0, lives() - attackStrength)));
		return 0;
	}
	
}
