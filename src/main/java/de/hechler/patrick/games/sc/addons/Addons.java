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
import java.util.ServiceLoader;

/**
 * this class can provides methods to get addons (for example all active addons or the addon which is responsible for an entity)
 * 
 * @author Patrick Hechler
 */
public class Addons {
	
	private Addons() {}
	
	/**
	 * the environment/property key ({@link System#getenv(String)} and {@link System#getProperty(String)}) of a colon ({@code :}) separated list of all disabled
	 * addons.<br>
	 * a backslash leads to the next character to be used in te current name, so to disable a addon with a colon or backslash in its name insert a backslash before
	 * it<br>
	 * if an existing addon is disabled, it will be be interpreted as non existing by this class<br>
	 * if an addon is disabled multiple times, it will have the same effect as disabling it once<br>
	 * if a non existing addon is disabled, it will be ignored
	 */
	public static final String DISABLED_ADDONS_KEY = "squareconquerer.addons.disabled";                 //$NON-NLS-1$
	
	private static Map<String, Addon> addons;
	
	/**
	 * returns a unmodifiable map containing all addons
	 * <p>
	 * the key values are the names of the addons
	 * 
	 * @return a unmodifiable map containing all addons
	 */
	public static Map<String, Addon> addons() {
		if (addons == null) {
			loadAddons();
		}
		return addons;
	}
	
	private static synchronized void loadAddons() {
		if (addons != null) return;
		Map<String, Addon>           as = new HashMap<>();
		ServiceLoader<AddonProvider> sl = ServiceLoader.load(AddonProvider.class, Addon.class.getClassLoader());
		for (AddonProvider scap : sl) {
			for (Addon sc : scap.addons()) {
				// ignore the groups, the name must be unique
				Addon old = as.put(sc.name, sc);
				if (old != null) {
					throw new AssertionError("multiple addons with the same name! name: " + sc.name + " classes: " + old.getClass().getModule() + "/"
						+ old.getClass().getName() + " and " + sc.getClass().getModule() + "/" + sc.getClass().getName());
				}
			}
		}
		removeDisabled(as, System.getProperty(DISABLED_ADDONS_KEY));
		removeDisabled(as, System.getenv(DISABLED_ADDONS_KEY));
		addons = Collections.unmodifiableMap(as);
	}
	
	private static void removeDisabled(Map<String, Addon> as, String disabledList) {
		if (disabledList == null) return;
		int nextC  = disabledList.indexOf(':');
		int nextBS = disabledList.indexOf('\\');
		if (nextBS == -1) {
			simpleRemoveDisabled(as, disabledList, nextC);
			return;
		}
		int index = 0;
		while (index < disabledList.length()) {
			if (nextC < nextBS || nextBS == -1) {
				as.remove(disabledList.substring(index, nextC));
				index = nextC + 1;
				nextC = nextC(disabledList, index);
				continue;
			}
			StringBuilder b = new StringBuilder();
			do {
				b.append(disabledList, index, nextBS);
				b.append(disabledList.charAt(nextBS + 1));
				index  = nextBS + 2;
				nextBS = nextBS(disabledList, index);
				nextC  = nextC(disabledList, index);
			} while (nextC > nextBS && nextBS != -1);
			b.append(disabledList, index, nextC);
			as.remove(b.toString());
			index = nextC + 1;
			nextC = nextC(disabledList, index);
		}
	}
	
	private static int nextBS(String disabledList, int index) { return nextChar(disabledList, index, '\\'); }
	
	private static int nextC(String disabledList, int index) { return nextChar(disabledList, index, ':'); }
	
	private static int nextChar(String disabledList, int index, int c) {
		int result = disabledList.indexOf(c, index);
		if (result != -1) {
			return result;
		}
		return disabledList.length();
	}
	
	private static void simpleRemoveDisabled(Map<String, Addon> as, String disabledList, int nextC) {
		if (nextC == -1) {
			as.remove(disabledList);
		} else {
			for (String rem : disabledList.split(":")) {
				as.remove(rem);
			}
			if (disabledList.charAt(disabledList.length() - 1) == ':') {
				as.remove("");
			}
		}
	}
	
}
