package de.hechler.patrick.games.squareconqerer.addons.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public sealed interface SCHelpBlock {
	
	record EntryBlock(List<SCHelpEntry> entries) implements SCHelpBlock {
		
		public EntryBlock(List<SCHelpEntry> entries) {
			ArrayList<SCHelpEntry> list = new ArrayList<>(entries);
			for (SCHelpEntry e : list) {
				if (e == null) throw new NullPointerException("null entries are not supported!");
			}
			this.entries = Collections.unmodifiableList(list);
		}
		
		public EntryBlock(SCHelpEntry... entries) {
			this(Arrays.asList(entries));
		}
		
	}
	
	record TextBlock(String text) implements SCHelpBlock {
		
		public TextBlock {
			if (text == null) throw new NullPointerException("null texts are not supported");
		}
		
	}
	
}
