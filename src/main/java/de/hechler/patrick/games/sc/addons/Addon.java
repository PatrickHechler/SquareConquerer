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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import de.hechler.patrick.games.sc.addable.AddableType;
import de.hechler.patrick.utils.objects.Version;

public abstract class Addon {
	
	public final String                             name;
	public final String                             localName;
	private final String[]                          groups;
	private final Supplier<String>                  licenseSup;
	private String                                  license;
	public final Version                            version;
	public final Map<String, ? extends AddableType> add;
	
	public Addon(String name, String localName, String[] groups, Supplier<String> licenseSup, Version version, Map<String, ? extends AddableType> add) {
		this.name       = Objects.requireNonNull(name, "name is null");
		this.localName  = Objects.requireNonNullElse(localName, name);
		this.groups     = groups.clone();
		this.licenseSup = Objects.requireNonNull(licenseSup, "license is null");
		this.version    = Objects.requireNonNull(version, "version is null");
		this.add        = Map.copyOf(add);
	}
	
	public int groupDepth() {
		return this.groups.length;
	}
	
	public String group(int index) {
		return this.groups[index];
	}
	
	public String license() {
		if (this.license == null) {
			this.license = this.licenseSup.get();
		}
		return this.license;
	}
	
}
