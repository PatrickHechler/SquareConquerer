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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;

/**
 * this class is used to get {@link BufferedImage} objects from {@link ImageableObj} instances
 * <p>
 * note that the {@link BufferedImage} instances retrieved from this class are <b>NOT</b> allowed to be modified
 * 
 * @author Patrick Hechler
 */
public class ImageableObjs {
	
	private static final String LOADING_IMG_ERROR = Messages.get("ImageableObjs.load-img-error"); //$NON-NLS-1$
	
	private ImageableObjs() {}
	
	private static BufferedImage[] imgs;
	
	/**
	 * returns the image representing a tile with the given ground, resource, building and unit
	 * <p>
	 * the given {@link BufferedImage} instance is <b>NOT</b> allowed to be modified!
	 * 
	 * @param ground the ground of the tile to be represented
	 * @param res    the resource of the tile to be represented
	 * @param b      the building of the tile to be represented or <code>null</code> if there is none
	 * @param u      the unit of the tile to be represented or <code>null</code> if there is none
	 * 
	 * @return the image representing the given tile. Do <b>NOT</b> modify the image
	 */
	public static BufferedImage immage(GroundType ground, OreResourceType res, Building b, Unit u) {
		if (imgs == null) {
			imgs = new BufferedImage[OreResourceType.count() * GroundType.count() * (Building.COUNT) * (Unit.COUNT)];
		}
		int to    = ground.ordinal();
		int ro    = res.ordinal();
		int index = (to * OreResourceType.count() + ro) * (Building.COUNT);
		index  = (index + Building.ordinal(b)) * (Unit.COUNT);
		index += Unit.ordinal(u);
		BufferedImage result = imgs[index];
		if (result != null) {
			return result;
		}
		BufferedImage ti = immage(ground);
		result = new BufferedImage(ti.getWidth(), ti.getHeight(), ti.getType());
		Graphics2D g = result.createGraphics();
		g.drawImage(ti, 0, 0, null);
		BufferedImage ri = immage(res);
		g.drawImage(ri, 0, 0, null);
		if (b != null) {
			BufferedImage bi = immage(b);
			g.drawImage(bi, 0, 0, null);
		}
		if (u != null) {
			BufferedImage ui = immage(u);
			g.drawImage(ui, 0, 0, null);
		}
		g.dispose();
		imgs[index] = result;
		return result;
	}
	
	/**
	 * returns an image for the given object
	 * <p>
	 * this method will directly return {@link ImageableObj#image() obj.image()} if it is already set (to a non <code>null</code> value)<br>
	 * otherwise the image will be loaded from the {@link ImageableObj#url() obj.url()} with {@link ImageIO#read(java.net.URL) ImageIO.read(URL)}
	 * 
	 * @param obj the object from which the image should be represented in an image
	 * 
	 * @return the objects {@link ImageableObj#image()} (possibly after initialization)
	 */
	public static BufferedImage immage(ImageableObj obj) {
		BufferedImage r = obj.image();
		if (r != null) return r;
		synchronized (obj) {
			r = obj.image();
			if (r == null) {
				r = loadImg(obj);
				obj.image(r);
			}
			return r;
		}
	}
	
	private static BufferedImage loadImg(ImageableObj e) throws IOError {
		try {
			return ImageIO.read(e.url());
		} catch (IllegalArgumentException err) {
			System.err.println(LOADING_IMG_ERROR + e + ": " + err); //$NON-NLS-1$
			throw err;
		} catch (IOException err) {
			System.err.println(LOADING_IMG_ERROR + e + ": " + err); //$NON-NLS-1$
			throw new IOError(err);
		}
	}
	
}
