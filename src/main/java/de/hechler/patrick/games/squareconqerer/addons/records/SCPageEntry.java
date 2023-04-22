package de.hechler.patrick.games.squareconqerer.addons.records;

import java.net.URI;
import java.util.function.Supplier;

import de.hechler.patrick.games.squareconqerer.world.World;

public sealed interface SCPageEntry {
	
	record TextEntry(String text) implements SCPageEntry {
		
		public TextEntry {
			if (text == null) {
				throw new NullPointerException("null texts are not supported");
			}
			if (text.indexOf('\n') != -1) {
				throw new IllegalStateException("TextEntries are not allowed to be multiline! (use TextBlock for that)");
			}
		}
		
	}
	
	record LinkEntry(String text, URI link) implements SCPageEntry {
		
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
			if (text.indexOf('\n') != -1) {
				throw new IllegalStateException("LinkEntries are not allowed to be multiline! (use TextBlock for that)");
			}
		}
		
	}
	
	record PageEntry(String text, String title, Supplier<SCPage> page) implements SCPageEntry {
		
		public PageEntry(String title, Supplier<SCPage> page) {
			this(title, title, page);
		}
		
		public PageEntry {
			if (text == null) {
				throw new NullPointerException("null texts are not supported");
			}
			if (title == null) {
				throw new NullPointerException("null world names are not supported");
			}
			if (page == null) {
				throw new NullPointerException("null suppliers are not supported");
			}
			if (text.indexOf('\n') != -1 || title.indexOf('\n') != -1) {
				throw new IllegalStateException("WorldEntries are not allowed to be multiline! (use TextBlock for that)");
			}
		}
		
	}
	
	record WorldEntry(String text, String worldName, Supplier<World> world) implements SCPageEntry {
		
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
			if (text.indexOf('\n') != -1 || worldName.indexOf('\n') != -1) {
				throw new IllegalStateException("WorldEntries are not allowed to be multiline! (use TextBlock for that)");
			}
		}
		
	}
	
}
