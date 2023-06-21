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
package de.hechler.patrick.games.sc.world.tile;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;
import jdk.incubator.concurrent.ScopedValue;

public final class TileImpl implements Tile {
	
	private Ground                              ground;
	private final Map<Resource, List<Resource>> resources;
	private Build                               build;
	private final Map<Unit, List<Unit>>         units;
	
	public TileImpl(Ground ground) {
		this.ground    = Objects.requireNonNull(ground, "ground is null");
		this.resources = new TreeMap<>();
		this.units     = new TreeMap<>();
	}
	
	public TileImpl(Ground ground, Map<Resource, List<Resource>> resources, Build build, Map<Unit, List<Unit>> units) {
		this.ground    = ground;
		this.resources = new TreeMap<>(resources); // the tree map uses a different init method for sorted maps
		this.build     = build;
		this.units     = new TreeMap<>(units);
	}
	
	public Ground ground() {
		return this.ground.unmodifiable();
	}
	
	public int resourceCount() {
		return this.resources.size();
	}
	
	public Resource resource(int index) {
		Iterator<Resource> iter = this.resources.keySet().iterator();
		while (index-- > 0) iter.next();
		return iter.next().unmodifiable();
	}
	
	public List<Resource> resourcesList() {
		return this.resources.keySet().stream().map(Resource::unmodifiable).toList();
	}
	
	public Stream<Resource> resourcesStream() {
		return this.resources.keySet().stream().map(Resource::unmodifiable);
	}
	
	public Build build() {
		return this.build != null ? this.build.unmodifiable() : null;
	}
	
	public int unitCount() {
		return this.units.size();
	}
	
	public Unit unit(int index) {
		Iterator<Unit> iter = this.units.keySet().iterator();
		while (index-- > 0) iter.next();
		return iter.next().unmodifiable();
	}
	
	public List<Unit> unitsList() {
		return this.units.keySet().stream().map(Unit::unmodifiable).toList();
	}
	
	public Stream<Unit> unitsStream() {
		return this.units.keySet().stream().map(Unit::unmodifiable);
	}
	
	public Tile unmodifiable() {
		return new TileUnmod(this);
	}
	
	@Override
	public TileImpl copy() {
		return new TileImpl(this.ground, this.resources, this.build, this.units);
	}
	
	@Override
	public Entity<?, ?>[] entities() {
		int            s      = this.units.size();
		Entity<?, ?>[] result = new Entity<?, ?>[s + (this.build != null ? 1 : 0)];
		int            off;
		if (this.build != null) {
			result[0] = this.build.unmodifiable();
			off       = 1;
		} else {
			off = 0;
		}
		for (Entity<?, ?> e : this.units.keySet()) {
			result[off++] = e.unmodifiable();
		}
		return result;
	}
	
	private static final ScopedValue<Boolean> NO_CHK = ScopedValue.newInstance();
	
	public static <T extends Throwable> void noCheck(Runnable r) throws T {
		ScopedValue.where(NO_CHK, Boolean.TRUE, r);
	}
	
	@SuppressWarnings("removal")
	public static <T extends Throwable> void withCheck(Runnable r) throws T {
		Boolean f = Boolean.FALSE;
		if (f.booleanValue()) {
			f = new Boolean(false);
		}
		ScopedValue.where(NO_CHK, f, r);
	}
	
	private static boolean checkModify() {
		return !NO_CHK.isBound() || !NO_CHK.get().booleanValue();
	}
	
	@Override
	public void addUnit(Unit u) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.units.compute(u, (u0, ul) -> {
			if (ul == null) {
				ul = new ArrayList<>();
			}
			ul.add(u0);
			return ul;
		});
	}
	
	@Override
	public void removeUnit(Unit u) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.units.compute(u, (u0, ul) -> {
			for (int i = 0; i < ul.size(); i++) {
				if (u0.same(ul.get(i))) {
					ul.remove(i);
					if (ul.isEmpty()) {
						return null;
					}
					return ul;
				}
			}
			throw new AssertionError("did not found my unit");
		});
	}

	@Override
	public boolean same(Tile t) {
		if (this == t) return true;
		if (t instanceof TileUnmod) return t.same(this);
		return false;
	}
	
}
