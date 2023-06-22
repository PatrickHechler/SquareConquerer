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
package de.hechler.patrick.games.sc.world.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.utils.objects.Random2;

public abstract non-sealed class Resource extends WorldThing<ResourceType, Resource> {
	
	public static final String AMOUNT = "amount";
	
	public Resource(UUID uuid) {
		super(uuid);
	}
	
	public int amount() {
		return intValue(AMOUNT).value();
	}
	
	public Resource sub(Resource sub, Random2 r) throws TurnExecutionException {
		if (type() != sub.type()) {
			throw new IllegalArgumentException("the given resource has a different type!");
		}
		int amount = sub.amount();
		if (amount <= 0) {
			throw new IllegalArgumentException("amount <= 0: " + amount);
		}
		int amnt = amount();
		if (amnt < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		Map<String, Value> vals = new HashMap<>(values());
		vals.put(AMOUNT, new IntValue(AMOUNT, amount));
		Resource result = type().withValues(vals, r.nextUUID());
		value(new IntValue(AMOUNT, amount));
		return result;
	}
	
	public void add(Resource res) {
		if (type() != res.type()) {
			throw new IllegalArgumentException("the given resource has a different type!");
		}
		value(new IntValue(AMOUNT, amount() + res.amount()));
	}
	
}
