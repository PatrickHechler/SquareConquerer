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

import de.hechler.patrick.games.sc.addons.addable.AddableType;

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
	public static final String DISABLED_ADDONS_KEY = "squareconquerer.addons.disabled"; //$NON-NLS-1$
	
	private static Map<String, Addon>             addons;
	private static Map<String, AddableType<?, ?>> added;
	
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
	
	/**
	 * returns the type with the given name
	 * 
	 * @param name the name of the type
	 * 
	 * @return the type with the given name
	 */
	public static AddableType<?, ?> type(String name) {
		if (added == null) {
			loadAddons();
		}
		AddableType<?, ?> a = added.get(name);
		if (a == null) {
			throw new IllegalArgumentException("the type '" + name + "' could not be found!");
		}
		return a;
	}
	
	/**
	 * reload all addons.
	 * <p>
	 * this method may lead to some hidden bugs, when there is still a open world or something, which uses some now potentially disabled addons or is not aware of
	 * some new addons
	 */
	public static synchronized void reloadAddons() {
		addons = null;
		added  = null;
		loadAddons();
	}
	
	private static boolean loadAddons = false;
	
	private static synchronized void loadAddons() {
		if (addons != null) return;
		if (loadAddons) throw new AssertionError("circular loading of addons detected!");
		loadAddons = true;
		try {
			ServiceLoader<AddonProvider> sl = ServiceLoader.load(AddonProvider.class, Addon.class.getClassLoader());
			Map<String, Addon>           as = new HashMap<>();
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
			Map<String, AddableType<?, ?>> ad = HashMap.newHashMap(as.size() + (as.size() >>> 3));
			for (Addon a : as.values()) {
				a.add.forEach((n, add) -> {
					AddableType<?, ?> old = ad.put(n, add);
					if (old != null) {
						throw new AssertionError("multiple things with the same name where added! name:" + n);
					}
				});
			}
			addons = Collections.unmodifiableMap(as);
			added  = Collections.unmodifiableMap(ad);
		} finally {
			loadAddons = false;
		}
		// always let the base addon check its dependencies (even if it is removed)
		// the base addon only checks for itself and for the existence of at least one ground type (which is not the not explored ground type)
		TheBaseAddonProvider.BASE_ADDON.checkDependencies(addons, added);
		for (Addon a : addons.values()) {
			a.checkDependencies(addons, added);
		}
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
	
	public static GroupTree disabledGT() {
		GroupTree                    gt = new GroupTree();
		ServiceLoader<AddonProvider> sl = ServiceLoader.load(AddonProvider.class, Addon.class.getClassLoader());
		sl.forEach(ap -> ap.addons().forEach(gt::add));
		addons().values().forEach(gt::remove);
		return gt;
	}
	
}
