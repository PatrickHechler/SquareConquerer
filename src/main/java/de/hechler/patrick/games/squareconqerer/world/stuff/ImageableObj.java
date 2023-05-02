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
package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * this interface is used for objects which can be represented in an limited amount of different {@link BufferedImage images}
 * 
 * @author Patrick Hechler
 */
public interface ImageableObj {
	
	/**
	 * returns the ordinal of this instance <br>
	 * all {@link ImageableObj} instances with the same {@link Object#getClass() class} and {@link #ordinal()} should be represented with the same {@link #url()} if
	 * they also have the same {@link ClassLoader}/{@link Module}
	 * 
	 * @return the ordinal of this instance
	 */
	int ordinal();
	
	/**
	 * returns the image of this object <br>
	 * if no image has yet been {@link #image(BufferedImage) set}, <code>null</code> is returned
	 * 
	 * @return the image of this object (or <code>null</code> if none was {@link #image(BufferedImage) set} yet)
	 */
	BufferedImage image();
	
	/**
	 * sets the image of this object to the new value
	 * 
	 * @param nval the new image of this object
	 */
	void image(BufferedImage nval);
	
	/**
	 * returns the name of the instance.<br>
	 * all {@link ImageableObj} instances with an equal {@link #type()} and {@link #name()} should have an equal {@link #url()} if they also have the same
	 * {@link ClassLoader}/{@link Module} ({@link Object#getClass()}.{@link Class#getResource(String)}))
	 * 
	 * @return the name of this instance
	 */
	String name();
	
	/**
	 * returns the type of this instance
	 * <p>
	 * the type is usually just <code>{@link Object#getClass() getClass}().{@link Class#getSimpleName() getSimpleName}()</code>
	 * 
	 * @return the type of this instance
	 */
	default String type() {
		Class<?> cls = this.getClass();
		while (cls.isAnonymousClass()) {
			cls = cls.getEnclosingClass();
		}
		return cls.getSimpleName();
	}
	
	/**
	 * returns the {@link URL} of this instance
	 * <p>
	 * the {@link URL} is usually just
	 * <code>{@link Object#getClass() getClass}().{@link Class#getResource(String) getResource}("/img/" + {@link #type}() + '/' + {@link #name}() + ".png")</code>
	 * 
	 * @return the {@link URL} of this instance
	 */
	default URL url() { return this.getClass().getResource("/img/" + this.type() + '/' + this.name() + ".png"); } //$NON-NLS-1$ //$NON-NLS-2$
	
}
