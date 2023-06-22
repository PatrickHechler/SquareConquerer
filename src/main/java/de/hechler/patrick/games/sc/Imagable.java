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
package de.hechler.patrick.games.sc;

import java.awt.Image;

/**
 * an Imagable object can be represented with an {@link Image}.
 * 
 * @author Patrick Hechler
 */
public interface Imagable {
	
	/**
	 * returns an Image which represents this object
	 * <p>
	 * width and height should have the same value, if not the returned image will potentially look ugly<br>
	 * also note that the returned image is allowed to have any sizes, thus a resize may be needed after this call
	 * <p>
	 * the returned image is not allowed to be modified and can be cached by this object
	 * 
	 * @param width  the width to which the image will be resized
	 * @param height the height to which the image will be resized
	 * 
	 * @return an Image which represents this object
	 */
	Image image(int width, int height);
	
}
