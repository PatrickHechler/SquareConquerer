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
