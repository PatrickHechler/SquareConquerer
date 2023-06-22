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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.UserWorld;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.entity.Entity;
import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.utils.objects.Random2;
import jdk.incubator.concurrent.ScopedValue;

@SuppressWarnings("javadoc")
public final class Tile {
	
	private final NavigableMap<ResourceType, Resource> resources = new TreeMap<>((a, b) -> a.name.compareTo(b.name));
	
	private int              lastTimeSeen;
	private Ground           ground;
	private Build            build;
	private final List<Unit> units;
	
	public Tile(Ground ground) {
		this.ground = Objects.requireNonNull(ground, "ground is null");
		this.units  = new ArrayList<>();
		this.lastTimeSeen = -1;
	}
	
	public Tile(Ground ground, Map<ResourceType, Resource> resources, Build build, List<Unit> units, int lastSeen) {
		if (lastSeen < -2) {
			throw new IllegalArgumentException("illegal last seen value: " + lastSeen);
		}
		this.ground = ground;
		this.resources.putAll(resources);
		this.build = build;
		this.units = new ArrayList<>(units);
		this.units.sort(null);
		this.lastTimeSeen = lastSeen;
	}
	
	public boolean visible() {
		return this.lastTimeSeen == -2;
	}
	
	public int lastTimeSeen() throws IllegalStateException {
		if (this.lastTimeSeen == -2) {
			throw new IllegalStateException("currently visible");
		}
		return this.lastTimeSeen;
	}
	
	public int lastTimeSeen0() {
		return this.lastTimeSeen;
	}
	
	public void setVisible(int turn, boolean visible) {
		// the user placer should not change the visibility of the tiles
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != UserWorld.class) {
			throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
		}
		if (turn < -1) {
			throw new IllegalArgumentException("turn < -1: " + turn);
		}
		if (visible) {
			this.lastTimeSeen = -2;
		} else {
			this.lastTimeSeen = turn;
		}
	}
	
	public Ground ground() {
		return this.ground.unmodifiable();
	}
	
	public int resourceCount() {
		return this.resources.size();
	}
	
	public Resource resource(int index) {
		Iterator<Entry<ResourceType, Resource>> iter = this.resources.entrySet().iterator();
		while (index-- > 0) iter.next();
		return iter.next().getValue().unmodifiable();
	}
	
	public List<Resource> resourcesList() {
		return this.resources.entrySet().stream().map(e -> e.getValue().unmodifiable()).toList();
	}
	
	public Stream<Resource> resourcesStream() {
		return this.resources.entrySet().stream().map(e -> e.getValue().unmodifiable());
	}
	
	public Map<ResourceType, Resource> resourcesMap() {
		Map<ResourceType, Resource> m = new HashMap<>(this.resources);
		m.replaceAll((t, r) -> r.unmodifiable());
		return m;
	}
	
	public Build build() {
		return this.build != null ? this.build.unmodifiable() : null;
	}
	
	public int unitCount() {
		return this.units.size();
	}
	
	public Unit unit(int index) {
		return this.units.get(index).unmodifiable();
	}
	
	public List<Unit> unitsList() {
		return this.units.stream().map(Unit::unmodifiable).toList();
	}
	
	public Stream<Unit> unitsStream() {
		return this.units.stream().map(Unit::unmodifiable);
	}
	
	public Tile copy() {
		return new Tile(this.ground, this.resources, this.build, this.units, this.lastTimeSeen);
	}
	
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
		for (Entity<?, ?> e : this.units) {
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
	
	public void addUnit(Unit u) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.units.add(u);
	}
	
	public void removeUnit(Unit u) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		if (!this.units.remove(u)) {
			throw new AssertionError("did not found my unit");
		}
	}
	
	public void setGround(Ground g) {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.ground = Objects.requireNonNull(g, "ground");
	}
	
	public void setBuild(Build b) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.build = b;
	}
	
	public void addResource(Resource r) {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		this.resources.merge(r.type(), r, (a, b) -> {
			a.add(b);
			return a;
		});
	}
	
	public Resource removeResource(Resource r, Random2 rnd) throws TurnExecutionException {
		if (checkModify()) {
			Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
			if (caller != CompleteWorld.class && caller != CompleteWorld.Builder.class) {
				throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
			}
		}
		Resource old = this.resources.get(r.type());
		return old.sub(r, rnd);
	}
	
}
