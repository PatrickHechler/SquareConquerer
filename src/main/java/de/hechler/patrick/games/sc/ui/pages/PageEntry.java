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
package de.hechler.patrick.games.sc.ui.pages;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

import de.hechler.patrick.games.sc.world.World;

/**
 * page entries are parts of a line, which contains text and (possibly some links or whatever)
 * 
 * @author Patrick Hechler
 */
public sealed interface PageEntry {
	
	/**
	 * this entry contains some text
	 * 
	 * @author Patrick Hechler
	 * 
	 * @param text the text (without line breaks)
	 */
	record TextEntry(String text) implements PageEntry {
		
		public TextEntry {
			Objects.requireNonNull(text, "text");
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException("the entry is multiline");
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
	record LinkEntry(String text, URI link) implements PageEntry {
		
		public LinkEntry(String text, String link) {
			this(text, URI.create(link));
		}
		
		public LinkEntry {
			Objects.requireNonNull(text, "text");
			Objects.requireNonNull(text, "link target");
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException("the entry is multiline");
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
	record PageLinkEntry(String text, String title, Supplier<Page> page) implements PageEntry {
		
		public PageLinkEntry {
			Objects.requireNonNull(text, "text");
			Objects.requireNonNull(title, "title");
			Objects.requireNonNull(page, "page");
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException("the entry is multiline");
			}
			if (title.indexOf('\n') != -1 || title.indexOf('\r') != -1) {
				throw new IllegalStateException("the title is multiline");
			}
		}
		
		public PageLinkEntry(String title, Supplier<Page> page) {
			this(title, title, page);
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
	record WorldEntry(String text, String worldName, Supplier<World> world) implements PageEntry {
		
		public WorldEntry(String worldName, Supplier<World> world) {
			this(worldName, worldName, world);
		}
		
		public WorldEntry {
			Objects.requireNonNull(text, "text");
			Objects.requireNonNull(worldName, "world name");
			Objects.requireNonNull(world, "world");
			if (text.indexOf('\n') != -1 || text.indexOf('\r') != -1) {
				throw new IllegalStateException("entry is multiline");
			}
			if (worldName.indexOf('\n') != -1 || worldName.indexOf('\r') != -1) {
				throw new IllegalStateException("world name is multiline");
			}
		}
		
	}
	
}
