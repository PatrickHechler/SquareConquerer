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
package de.hechler.patrick.games.squareconqerer.addons.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.Messages;

/**
 * {@link SCPage pages} are build from blocks, which implement this interface
 * 
 * @author Patrick Hechler
 */
public sealed interface SCPageBlock {
	
	/**
	 * a {@link SeparatingBlock} is just a line from the left to the right
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param bold <code>true</code> if the line should be large and <code>false</code>
	 */
	record SeparatingBlock(boolean bold) implements SCPageBlock {}
	
	/**
	 * an entry block fills an line with its {@link SCPageEntry page entries}
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param entries the entries
	 */
	record EntryBlock(List<SCPageEntry> entries) implements SCPageBlock {
		
		private static final String NULL_ENTRIES_ARE_NOT_SUPPORTED = Messages.getString("SCPageBlock.found-a-not-existing-entry"); //$NON-NLS-1$
		private static final String THERE_ARE_NO_ENTRIES = Messages.getString("SCPageBlock.no-entries"); //$NON-NLS-1$
		private static final String THERE_IS_NO_TEXT = Messages.getString("SCPageBlock.no-text"); //$NON-NLS-1$
		
		public EntryBlock(List<SCPageEntry> entries) {
			if (entries == null) throw new NullPointerException(THERE_ARE_NO_ENTRIES);
			ArrayList<SCPageEntry> list = new ArrayList<>(entries);
			for (SCPageEntry e : list) {
				if (e == null) throw new NullPointerException(NULL_ENTRIES_ARE_NOT_SUPPORTED);
			}
			this.entries = Collections.unmodifiableList(list);
		}
		
		public EntryBlock(SCPageEntry... entries) {
			this(Arrays.asList(entries));
		}
		
	}
	
	/**
	 * an text block fills its place with the (possibly multiline) text
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text the (possibly multiline) text
	 */
	record TextBlock(String text) implements SCPageBlock {
		
		public TextBlock {
			if (text == null) throw new NullPointerException(EntryBlock.THERE_IS_NO_TEXT);
		}
		
	}
	
}
