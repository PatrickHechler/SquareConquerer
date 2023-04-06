package de.hechler.patrick.games.squareconqerer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Settings {
	
	public static final int     VERSION_MAJOR    = 3;
	public static final int     VERSION_MINOR    = 0;
	public static final int     VERSION_FIX      = 0;
	public static final boolean VERSION_SNAPSHOT = true;
	public static final String  VERSION_STRING   = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_FIX + (VERSION_SNAPSHOT ? "-SNAPSHOT" : "");

	private Settings() {}
	
	private static final Path       PATH  = Path.of("square-conquerer.properties");
	private static final Properties PROPS = new Properties();
	
	static {
		if (Files.exists(PATH)) {
			try (BufferedReader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				PROPS.load(r);
			} catch (IOException e) {
				System.err.println("[Settings]: could not load the old properties");
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (BufferedWriter w = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				PROPS.store(w, null);
			} catch (IOException e) {
				System.err.println("[Settings]: could not save the current properties");
				e.printStackTrace();
			}
		}));
	}
	
	private static final String TRUE  = "true";
	private static final String FALSE = "false";
	
	private static final String ICON_SIZE       = "gui.icon_size";
	
	@SuppressWarnings("unused")
	private static boolean getProp(String name, boolean def) {
		return switch (PROPS.getProperty(name, def ? TRUE : FALSE)) {
		case TRUE -> true;
		case FALSE -> false;
		default -> {
			System.err.println("[Settings]: unknown value of prop " + name + ": " + PROPS.getProperty(name));
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
			return Integer.valueOf(val);
		} catch (NumberFormatException e) {
			System.err.println("[Settings]: illegal value of prop " + name + ": " + PROPS.getProperty(name));
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
	
	public static int iconSize() {
		return getProp(ICON_SIZE, 64);
	}
	
	public static void iconSize(int value) {
		setProp(ICON_SIZE, value);
	}
	
	public static Thread.Builder threadBuilder() {
		return Thread.ofVirtual();
	}
	
}
