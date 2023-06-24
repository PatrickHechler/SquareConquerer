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
package de.hechler.patrick.games.sc.addons;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GroupTree {
	
	private static final Consumer<GroupTree> DEEP_CLEAR = new Consumer<>() {
		
		@Override
		public void accept(GroupTree g) {
			g.addons.clear();
			g.forEachGroup(this);
			g.groups.clear();
		}
		
	};
	
	private static final BiConsumer<GroupTree, Consumer<? super Addon>> FOR_EACH_DEEP = new BiConsumer<>() {
		
		@Override
		public void accept(GroupTree g, Consumer<? super Addon> c) {
			g.forEach(c, this, c);
		}
		
	};
	
	private final GroupTree                   parent;
	private final String                      name;
	private final int                         depth;
	private final Function<String, GroupTree> addFunc;
	private final Map<String, Addon>          addons;
	private final Map<String, GroupTree>      groups;
	
	public GroupTree() {
		this(null, null);
	}
	
	private GroupTree(GroupTree parent, String name) {
		this.parent  = parent;
		this.name    = name;
		this.depth   = parent != null ? parent.depth + 1 : 0;
		this.addFunc = str -> new GroupTree(this, str);
		this.addons  = new TreeMap<>();
		this.groups  = new TreeMap<>();
	}
	
	public boolean isEmpty() {
		return this.addons.isEmpty() && this.groups.isEmpty();
	}
	
	public String name() {
		if (this.parent == null) throw new IllegalStateException("the root tree has no name!");
		return this.name;
	}
	
	public void transferFrom(GroupTree g) {
		if (g == this) {
			throw new IllegalArgumentException("I won't transfer to myself");
		}
		if (this.depth != 0) {
			if (this.depth > g.depth) {
				throw new IllegalArgumentException("I am a more specialized tree than the source tree");
			}
			GroupTree og = g;
			while (og.depth != g.depth) og = g.parent;
			if (og == this) {
				throw new IllegalArgumentException("I won't transfer to myself");
			}
			GroupTree mg = this;
			while (og.parent != null) {
				if (!Objects.equals(og.name, mg.name)) {
					throw new IllegalArgumentException("I am differently specialized tree than the source tree");
				}
				og = og.parent;
				mg = mg.parent;
			}
		}
		forEachDeep(this::add);
		DEEP_CLEAR.accept(g);
		if (g.parent != null) {
			g.parent.groups.remove(g.name);
		}
	}
	
	public void add(Addon addon) {
		if (this.depth == addon.groupDepth()) {
			this.addons.put(addon.name, addon);
		} else {
			this.groups.computeIfAbsent(addon.group(this.depth), this.addFunc);
		}
	}
	
	public void remove(Addon addon) {
		if (this.depth == addon.groupDepth()) {
			this.addons.remove(addon.name, addon);
		} else {
			GroupTree g = this.groups.get(addon.group(this.depth));
			if (g != null) {
				g.remove(addon);
				if (g.isEmpty()) {
					this.groups.remove(addon.group(this.depth));
				}
			}
		}
	}
	
	public boolean contains(Addon addon) {
		if (this.depth == addon.groupDepth()) {
			return this.addons.containsKey(addon.name);
		}
		GroupTree g = this.groups.get(addon.group(this.depth));
		if (g == null) {
			return false;
		}
		return g.contains(addon);
	}
	
	public <T> void forEach(Consumer<? super Addon> c0, BiConsumer<? super GroupTree, T> c1, T arg) {
		Objects.requireNonNull(c0);
		Objects.requireNonNull(c1);
		this.addons.values().forEach(c0);
		for (GroupTree g : this.groups.values()) {
			c1.accept(g, arg);
		}
	}
	
	public void forEachDeep(Consumer<? super Addon> c0) {
		Objects.requireNonNull(c0);
		forEach(c0, FOR_EACH_DEEP, c0);
	}
	
	public void forEachDirect(Consumer<? super Addon> c0) {
		Objects.requireNonNull(c0);
		this.addons.values().forEach(c0);
	}
	
	public void forEachGroup(Consumer<? super GroupTree> c0) {
		Objects.requireNonNull(c0);
		this.groups.values().forEach(c0);
	}
	
}
