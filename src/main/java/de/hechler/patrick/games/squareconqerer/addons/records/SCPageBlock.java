package de.hechler.patrick.games.squareconqerer.addons.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public sealed interface SCPageBlock {
	
	record SeperatingBlock(boolean bold) implements SCPageBlock {}
	
	record EntryBlock(List<SCPageEntry> entries) implements SCPageBlock {
		
		public EntryBlock(List<SCPageEntry> entries) {
			ArrayList<SCPageEntry> list = new ArrayList<>(entries);
			for (SCPageEntry e : list) {
				if (e == null) throw new NullPointerException("null entries are not supported!");
			}
			this.entries = Collections.unmodifiableList(list);
		}
		
		public EntryBlock(SCPageEntry... entries) {
			this(Arrays.asList(entries));
		}
		
	}
	
	record TextBlock(String text) implements SCPageBlock {
		
		public TextBlock {
			if (text == null) throw new NullPointerException("null texts are not supported");
		}
		
	}
	
}
