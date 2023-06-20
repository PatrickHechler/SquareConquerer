package de.hechler.patrick.games.sc;

import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.Reader;
import java.lang.Thread.Builder.OfVirtual;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {
	
	private Settings() {}
	
	@SuppressWarnings("preview")
	private static final OfVirtual OF_VIRTUAL = Thread.ofVirtual();
	
	public static final String PROPS_PATH_KEY = "squareconquerer.settings.path";
	
	public static class Props {
		
		private Props() {}
		
		private static final Properties PROPS;
		
		static {
			String     env = System.getenv(PROPS_PATH_KEY);
			final Path p;
			if (env != null) {
				p = Path.of(env);
			} else {
				p = Path.of("./sc.properties");
			}
			PROPS = new Properties();
			if (Files.exists(p)) {
				try (Reader in = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
					PROPS.load(in);
				} catch (IOException e) {
					throw new IOError(e);
				}
			}
			Runtime.getRuntime().addShutdownHook(OF_VIRTUAL.unstarted(() -> {
				try (BufferedWriter out = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
					PROPS.store(out, null);
				} catch (IOException e) {
					System.err.println("failed to save the properties:");
					e.printStackTrace();
				}
			}));
		}
		
	}
	
	public static void threadStart(Runnable task) {
		OF_VIRTUAL.start(task);
	}
	
}
