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

import java.net.URI;
import java.util.function.Supplier;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.world.World;

/**
 * page entries are parts of a line, which contains text and (possibly some linke or whatever)
 * 
 * @author Patrick Hechler
 */
public sealed interface SCPageEntry {
	
	/**
	 * this entry contains some text
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text the text (without line breaks)
	 */
	record TextEntry(String text) implements SCPageEntry {
		
		private static final String THERE_IS_NO_TEXT = Messages.getString("SCPageEntry.no-text"); //$NON-NLS-1$
		private static final String THERE_IS_NO_LINK = Messages.getString("SCPageEntry.no-link"); //$NON-NLS-1$
		private static final String THERE_IS_NO_TITLE = Messages.getString("SCPageEntry.no-title"); //$NON-NLS-1$
		private static final String THERE_IS_NO_WORLD_NAME = Messages.getString("SCPageEntry.no-world-name"); //$NON-NLS-1$
		private static final String THERE_IS_NO_WORLD_SUPPLIER = Messages.getString("SCPageEntry.no-world-supplier"); //$NON-NLS-1$
		private static final String THERE_IS_NO_PAGE_SUPPLIER = Messages.getString("SCPageEntry.no-page-supplier"); //$NON-NLS-1$
		private static final String TEXT_ENTRY_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-text-entry"); //$NON-NLS-1$
		private static final String LINK_ENTRY_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-link-entry"); //$NON-NLS-1$
		private static final String PAGE_ENTRY_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-page-entry"); //$NON-NLS-1$
		private static final String WORLD_ENTRY_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-world-entry"); //$NON-NLS-1$
		private static final String TITLE_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-title"); //$NON-NLS-1$
		private static final String WORLD_NAME_IS_MULTILINE = Messages.getString("SCPageEntry.multiline-world-name"); //$NON-NLS-1$
		
		
		
		public TextEntry {
			if (text == null) {
				throw new NullPointerException(THERE_IS_NO_TEXT);
			}
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException(TEXT_ENTRY_IS_MULTILINE);
			}
		}
		
	}
	
	/**
	 * the link entry contains some text and a target URI
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text the text (without line breaks)
	 * @param link the target link
	 */
	record LinkEntry(String text, URI link) implements SCPageEntry {
		
		public LinkEntry(String text, String link) {
			this(text, URI.create(link));
		}
		
		public LinkEntry {
			if (text == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_TEXT);
			}
			if (link == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_LINK);
			}
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException(TextEntry.LINK_ENTRY_IS_MULTILINE);
			}
		}
		
	}
	
	/**
	 * the page entry contains some text, which links to a page
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text  the text (without line breaks)
	 * @param title the pages title (without line breaks)
	 * @param page  the target page supplier
	 */
	record PageEntry(String text, String title, Supplier<SCPage> page) implements SCPageEntry {
		
		public PageEntry(String title, Supplier<SCPage> page) {
			this(title, title, page);
		}
		
		public PageEntry {
			if (text == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_TEXT);
			}
			if (title == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_TITLE);
			}
			if (page == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_PAGE_SUPPLIER);
			}
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException(TextEntry.PAGE_ENTRY_IS_MULTILINE);
			}
			if (title.indexOf('\n') != -1 || title.indexOf('\r') != -1) {
				throw new IllegalStateException(TextEntry.TITLE_IS_MULTILINE);
			}
		}
		
	}
	
	/**
	 * the world entry contains some text, which links to a world
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text      the text (without line breaks)
	 * @param worldName the worlds name (without line breaks)
	 * @param world     the target world supplier
	 */
	record WorldEntry(String text, String worldName, Supplier<World> world) implements SCPageEntry {
		
		public WorldEntry(String worldName, Supplier<World> world) {
			this(worldName, worldName, world);
		}
		
		public WorldEntry {
			if (text == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_TEXT);
			}
			if (worldName == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_WORLD_NAME);
			}
			if (world == null) {
				throw new NullPointerException(TextEntry.THERE_IS_NO_WORLD_SUPPLIER);
			}
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException(TextEntry.WORLD_ENTRY_IS_MULTILINE);
			}
			if (worldName.indexOf('\n') != -1 || worldName.indexOf('\r') != -1) {
				throw new IllegalStateException(TextEntry.WORLD_NAME_IS_MULTILINE);
			}
		}
		
	}
	
}
