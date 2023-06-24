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
	
	public static Thread threadStart(Runnable task) {
		return OF_VIRTUAL.start(task);
	}
	
}
