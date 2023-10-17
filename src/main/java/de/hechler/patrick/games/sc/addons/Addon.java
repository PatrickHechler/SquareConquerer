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

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.ui.pages.Page;
import de.hechler.patrick.games.sc.ui.pages.TextOnlyPage;
import de.hechler.patrick.utils.objects.Version;

public abstract class Addon {
	
	public final String                                   name;
	public final String                                   localName;
	private final String[]                                groups;
	public final Version                                  version;
	public final Map<String, ? extends AddableType<?, ?>> add;
	public final String                                   licenseName;
	private TextOnlyPage                                  license;
	private Page                                          credits;
	private Page                                          help;
	
	public Addon(String name, String localName, String[] groups, Version version, Map<String, ? extends AddableType<?, ?>> add, String licenseName) {
		this.name        = Objects.requireNonNull(name, "name is null");
		this.localName   = Objects.requireNonNullElse(localName, name);
		this.groups      = groups.clone();
		this.version     = Objects.requireNonNull(version, "version is null");
		this.add         = Map.copyOf(add);
		this.licenseName = Objects.requireNonNull(licenseName, "license name is null");
	}
	
	public int groupDepth() {
		return this.groups.length;
	}
	
	public String group(int index) {
		return this.groups[index];
	}
	
	protected abstract TextOnlyPage loadLicense();
	
	public TextOnlyPage license() {
		if (this.license == null) {
			this.license = this.loadLicense();
		}
		return this.license;
	}
	
	protected abstract Page loadCredits();
	
	public abstract boolean hasCredits();
	
	public Page credits() {
		if (this.credits == null) {
			this.credits = this.loadCredits();
		}
		return this.credits;
	}
	
	protected abstract Page loadHelp();
	
	public abstract boolean hasHelp();
	
	public Page help() {
		if (this.help == null) {
			this.help = this.loadHelp();
		}
		return this.help;
	}
	
	public abstract void checkDependencies(Map<String, Addon> addons, Map<String, AddableType<?, ?>> added);
	
	@Override
	public String toString() {
		return this.localName;
	}
	
}
