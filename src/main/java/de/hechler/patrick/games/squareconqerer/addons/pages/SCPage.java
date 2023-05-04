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
 * this class represents a page, which be displayed to the user
 * 
 * @author Patrick Hechler
 * 
 * @param blocks the blocks of this page
 */
public record SCPage(List<SCPageBlock> blocks) {
	
	private static final String THERE_ARE_NO_BLOCKS = Messages.getString("SCPage.no-blocks"); //$NON-NLS-1$
	private static final String NULL_BLOCKS_ARE_NOT_SUPPORETD = Messages.getString("SCPage.there-is-a-no-block"); //$NON-NLS-1$
	
	/**
	 * create an page from the given blocks
	 * 
	 * @param blocks the blocks of the page
	 */
	public SCPage(List<SCPageBlock> blocks) {
		if (blocks == null) throw new NullPointerException(THERE_ARE_NO_BLOCKS);
		ArrayList<SCPageBlock> list = new ArrayList<>(blocks);
		for (SCPageBlock b : list) {
			if (b == null) throw new NullPointerException(NULL_BLOCKS_ARE_NOT_SUPPORETD);
		}
		this.blocks = Collections.unmodifiableList(list);
	}
	
	/**
	 * create an page from the given blocks
	 * 
	 * @param blocks the blocks of the page
	 */
	public SCPage(SCPageBlock... blocks) {
		this(Arrays.asList(blocks));
	}
	
}
