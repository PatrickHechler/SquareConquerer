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
package de.hechler.patrick.games.sc.ui.pages;

import java.util.List;

/**
 * this class represents a page, which be displayed to the user
 * 
 * @author Patrick Hechler
 * 
 * @param blocks the blocks of this page
 */
public record Page(List<PageBlock> blocks) {
	
	/**
	 * create an page from the given blocks
	 * 
	 * @param blocks the blocks of the page
	 */
	public Page(List<PageBlock> blocks) {
		this.blocks = List.copyOf(blocks);
	}
	
	/**
	 * create an page from the given blocks
	 * 
	 * @param blocks the blocks of the page
	 */
	public Page(PageBlock... blocks) {
		this(List.of(blocks));
	}
	
}
