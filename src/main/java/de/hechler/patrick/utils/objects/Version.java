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
package de.hechler.patrick.utils.objects;

/**
 * a version is represented with three non negative <code>int</code> values ({@link #major()}, {@link #minor()} and {@link #patch()})
 * <p>
 * the {@link #patch() patch} value should indicate bug fixes and similar<br>
 * the {@link #minor() minor} value should indicate added features, small changes and similar<br>
 * the {@link #major() major} value should indicate breaking changes and similar
 * 
 * @param major the major version value
 * @param minor the minor version value
 * @param patch the patch version value
 * 
 * @author Patrick Hechler
 */
public record Version(int major, int minor, int patch) implements Comparable<Version> {
	
	/**
	 * Initializes a new {@link Version}
	 * 
	 * @param major the {@link #major()} value of the version
	 * @param minor the {@link #minor()} value of the version
	 * @param patch the {@link #patch()} value of the version
	 */
	public Version {
		if (major < 0) {
			throw new IllegalStateException("negative major value (" + this + ')');
		}
		if (minor < 0) {
			throw new IllegalStateException("negative major value (" + this + ')');
		}
		if (patch < 0) {
			throw new IllegalStateException("negative major value (" + this + ')');
		}
	}
	
	/**
	 * compares the two versions:
	 * <ol>
	 * <li>if the other version has a higher {@link #major()} number a negative value is returned</li>
	 * <li>if the other version has a lower {@link #major()} number a positive value is returned</li>
	 * <li>if the other version has a higher {@link #minor()} number a negative value is returned</li>
	 * <li>if the other version has a lower {@link #minor()} number a positive value is returned</li>
	 * <li>if the other version has a higher {@link #patch()} number a negative value is returned</li>
	 * <li>if the other version has a lower {@link #patch()} number a positive value is returned</li>
	 * <li>zero is returned</li>
	 * </ol>
	 */
	@Override
	public int compareTo(Version o) {
		int cmp = Integer.compare(this.major, o.major);
		if (cmp != 0) return cmp;
		cmp = Integer.compare(this.minor, o.minor);
		if (cmp != 0) return cmp;
		return Integer.compare(this.patch, o.patch);
	}
	
	/**
	 * returns <code>({@link #major()} + "." + {@link #minor()} + "." + {@link #patch()})</code>
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch;
	}
	
	/**
	 * returns the {@link Version} which is represented by the given {@link String}
	 * <p>
	 * a version string can be one of the following:
	 * <ul>
	 * <li><code>major</code></li>
	 * <li><code>major '.' minor</code></li>
	 * <li><code>major '.' minor '.' patch</code></li>
	 * </ul>
	 * the {@link #toString()} method produces the last/full version
	 * 
	 * @param str the string which represents a version
	 * 
	 * @return the {@link Version} which is represented by the given {@link String}
	 */
	public static Version of(String str) {
		int fi = str.indexOf('.');
		if (fi == -1) {
			int val = Integer.parseInt(str.trim());
			return new Version(val, 0, 0);
		}
		int major = Integer.parseInt(str.substring(0, fi).trim());
		int si    = str.indexOf(fi + 1, '.');
		if (si == -1) {
			int val = Integer.parseInt(str.substring(fi + 1).trim());
			return new Version(major, val, 0);
		}
		int minor = Integer.parseInt(str.substring(fi + 1, si).trim());
		int patch = Integer.parseInt(str.substring(si + 1).trim());
		return new Version(major, minor, patch);
	}
	
}
