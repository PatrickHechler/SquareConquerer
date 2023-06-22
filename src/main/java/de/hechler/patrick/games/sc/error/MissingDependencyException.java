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
package de.hechler.patrick.games.sc.error;

import de.hechler.patrick.utils.objects.Version;

@SuppressWarnings("javadoc")
public class MissingDependencyException extends RuntimeException {
	
	private static final long serialVersionUID = -246377383352574879L;
	
	public final Version minVersion;
	public final Version maxVersion;
	public final String  name;
	
	public MissingDependencyException(Version minVersion, Version maxVersion, String name, String msg) {
		super(msg);
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
		this.name       = name;
	}
	
	public MissingDependencyException(Version minVersion, Version maxVersion, String name) {
		super("missing dependency: '" + name + "' min: " + minVersion + " max: " + maxVersion);
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
		this.name       = name;
	}
	
	public MissingDependencyException(Version minVersion, String name, String msg) {
		super(msg);
		this.minVersion = minVersion;
		this.maxVersion = null;
		this.name       = name;
	}
	
	public MissingDependencyException(Version minVersion, String name) {
		super("missing dependency: '" + name + "' min: " + minVersion);
		this.minVersion = minVersion;
		this.maxVersion = null;
		this.name       = name;
	}
	
	public MissingDependencyException(String name, String msg) {
		super(msg);
		this.minVersion = null;
		this.maxVersion = null;
		this.name       = name;
	}
	
	public MissingDependencyException(String name) {
		super("missing dependency: '" + name + '\'');
		this.minVersion = null;
		this.maxVersion = null;
		this.name       = name;
	}
	
}
