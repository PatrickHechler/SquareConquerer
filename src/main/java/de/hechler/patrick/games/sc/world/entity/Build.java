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
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.utils.objects.Random2;

public abstract non-sealed class Build extends Entity<BuildType, Build> {
	
	public static final String STORE             = "store";
	public static final String NEEDED_RESOURCES  = "need:resource";
	public static final String NEEDED_WORK_TURNS = "need:work:turns";
	
	public Build(UUID uuid) {
		super(uuid);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<TypeValue<ResourceType>, WorldThingValue> resources() {
		return (Map) mapValue(STORE).value();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Optional<Resource> resource(ResourceType res) {
		Map<TypeValue<ResourceType>, WorldThingValue> map = resources();
		WorldThingValue                               val = map.get(new TypeValue(res.name, res));
		if (val == null) return Optional.empty();
		return (Optional) val.asOptional();
	}
	
	public void giveRes(Unit u, Resource res, Random2 r) throws TurnExecutionException {
		Resource myRes = resource(res.type()).orElseThrow(() -> new TurnExecutionException(ErrorType.INVALID_TURN));
		Resource add   = myRes.sub(res, r);
		try {
			u.addResource(add);
		} catch (Throwable t) {
			myRes.add(add);
			throw t;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void store(Unit u, Resource resource) {
		resource(resource.type()).ifPresentOrElse(r -> r.add(resource), () -> {
			ResourceType                                  res = resource.type();
			Map<TypeValue<ResourceType>, WorldThingValue> map = new HashMap<>(resources());
			map.put(new TypeValue(res.name, res), new WorldThingValue(res.name, resource));
			value(new MapValue<TypeValue<ResourceType>, WorldThingValue>(STORE, map));
		});
	}
	
	public void work(Unit u) throws TurnExecutionException {
		Map<Value, Value> map = mapValue(NEEDED_RESOURCES).value();
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
	
}
