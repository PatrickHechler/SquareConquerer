package de.hechler.patrick.games.squareconqerer.addons.records;

import java.net.URI;
import java.util.function.Supplier;

import de.hechler.patrick.games.squareconqerer.world.World;

public sealed interface SCHelpEntry {
	
	record TextEntry(String text) implements SCHelpEntry {
		
		public TextEntry {
			if (text == null) {
				throw new NullPointerException("null texts are not supported");
			}
			if (text.indexOf('\n') != -1) {
				throw new IllegalStateException("TextEntries are not allowed to be multiline! (use TextBlock for that)");
			}
		}
		
	}

	record LinkEntry(String text, URI link) implements SCHelpEntry {
		
		public LinkEntry(String text, String link) {
			this(text, URI.create(link));
		}
		
		public LinkEntry {
			if (text == null) {
				throw new NullPointerException("null texts are not supported");
			}
			if (link == null) {
				throw new NullPointerException("null links are not supported");
			}
		}
		
	}

	record WorldEntry(String text, String worldName, Supplier<World> world) implements SCHelpEntry {
		
		public WorldEntry(String worldName, Supplier<World> world) {
			this(worldName, worldName, world);
		}
		
		public WorldEntry {
			if (text == null) {
				throw new NullPointerException("null texts are not supported");
			}
			if (worldName == null) {
				throw new NullPointerException("null world names are not supported");
			}
			if (world == null) {
				throw new NullPointerException("null suppliers are not supported");
			}
		}
		
	}
	
}
