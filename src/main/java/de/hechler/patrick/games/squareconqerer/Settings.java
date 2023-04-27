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
package de.hechler.patrick.games.squareconqerer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.Thread.Builder.OfVirtual;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * this class is used to configure the game
 * <p>
 * for example the icon resolution (see {@link #iconSize()}/{@link #iconSize(int)})
 * <p>
 * this class also stores the version information of this project (see {@value #VERSION_STRING})
 * 
 * @author Patrick Hechler
 */
public class Settings {
	
	private static final OfVirtual OF_VIRTUAL = Thread.ofVirtual();
	
	private static final String ILLEGAL_VALUE  = Messages.get("Settings.illegal-prop"); //$NON-NLS-1$
	private static final String FAILED_TO_SAVE = Messages.get("Settings.save-failed");  //$NON-NLS-1$
	private static final String FAILED_TO_LOAD = Messages.get("Settings.load-failed");  //$NON-NLS-1$
	
	/**
	 * the major version of the Square Conquerer Project
	 */
	public static final int     VERSION_MAJOR    = 3;
	/**
	 * the minor version of the Square Conquerer Project
	 */
	public static final int     VERSION_MINOR    = 0;
	/**
	 * the (bug-)fix version of the Square Conquerer Project
	 */
	public static final int     VERSION_FIX      = 0;
	/**
	 * the flag indicating if this version is a snapshot or not
	 */
	public static final boolean VERSION_SNAPSHOT = true;
	/**
	 * the version string of the Square Conquerer Project:
	 * <code>{@value #VERSION_MAJOR} + "." + {@value #VERSION_MINOR} + "." + {@value #VERSION_FIX} + ({@value #VERSION_SNAPSHOT} ? "-SNAPSHOT" : "")</code>
	 */
	public static final String  VERSION_STRING   = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_FIX + (VERSION_SNAPSHOT ? "-SNAPSHOT" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	private Settings() {}
	
	private static final Path       PATH  = Path.of("square-conquerer.properties"); //$NON-NLS-1$
	private static final Properties PROPS = new Properties();
	
	static {
		if (Files.exists(PATH)) {
			try (BufferedReader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				PROPS.load(r);
			} catch (IOException e) {
				System.err.println(FAILED_TO_LOAD);
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (BufferedWriter w =
				Files.newBufferedWriter(PATH, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				PROPS.store(w, null);
			} catch (IOException e) {
				System.err.println(FAILED_TO_SAVE);
				e.printStackTrace();
			}
		}));
	}
	
	private static final String TRUE  = "true";  //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$
	
	private static final String ICON_SIZE = "gui.icon_size"; //$NON-NLS-1$
	
	@SuppressWarnings("unused")
	private static boolean getProp(String name, boolean def) {
		return switch (PROPS.getProperty(name, def ? TRUE : FALSE)) {
		case TRUE -> true;
		case FALSE -> false;
		default -> {
			System.err.println(ILLEGAL_VALUE + name + ": " + PROPS.getProperty(name)); //$NON-NLS-1$
			yield def;
		}
		};
	}
	
	private static int getProp(String name, int def) {
		String val = PROPS.getProperty(name);
		if (val == null) {
			return def;
		}
		try {
			return Integer.parseInt(val);
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			System.err.println(ILLEGAL_VALUE + name + ": " + PROPS.getProperty(name)); //$NON-NLS-1$
			return def;
		}
	}
	
	@SuppressWarnings("unused")
	private static void setProp(String name, boolean val) {
		PROPS.setProperty(name, val ? TRUE : FALSE);
	}
	
	private static void setProp(String name, int val) {
		PROPS.setProperty(name, Integer.toString(val));
	}
	
	/**
	 * returns the current value of the icon size property
	 * 
	 * @return the current value of the icon size property
	 */
	public static int iconSize() {
		return getProp(ICON_SIZE, 64);
	}
	
	/**
	 * sets the icon size property to the given value
	 * 
	 * @param value the new value of the icon size property
	 */
	public static void iconSize(int value) {
		setProp(ICON_SIZE, value);
	}
	
	/**
	 * returns a thread builder object
	 * 
	 * @return a thread builder object
	 */
	public static Thread.Builder threadBuilder() {
		return Thread.ofVirtual();
	}
	
	/**
	 * starts the given task in a new thread
	 * 
	 * @param task the task to be executed in a new thread
	 * @return the thread, which executes the task
	 */
	public static Thread threadStart(Runnable task) {
		return OF_VIRTUAL.start(task);
	}
	
	/**
	 * returns the integer value between {@code min} and {@code max}, which is most near to {@code val}.
	 * <ul>
	 * <li>if {@code val} is lower than {@code min}, {@code min} is returned</li>
	 * <li>if {@code val} is greater than {@code max}, {@code max} is returned</li>
	 * <li>if {@code val} is already between {@code min} and {@code max}, {@code val} is returned</li>
	 * </ul>
	 * if <code>min > max</code> the behavior is undefined
	 * 
	 * @param min the minimum value
	 * @param val the value
	 * @param max the maximum value
	 * 
	 * @return the integer value between {@code min} and {@code max}, which is most near to {@code val}
	 */
	public static int between(int min, int val, int max) {
		if (min >= val) return min;
		else if (max <= val) return max;
		else return val;
	}
	
}
