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
package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;

public class ImageableObjs {
	
	private ImageableObjs() {}
	
	private static BufferedImage[] imgs;
	
	public static BufferedImage immage(GroundType tile, OreResourceType res, Building b, Unit u) {
		if (imgs == null) {
			imgs = new BufferedImage[OreResourceType.count() * GroundType.count() * (Building.COUNT) * (Unit.COUNT)];
		}
		int to    = tile.ordinal();
		int ro    = res.ordinal();
		int index = (to * OreResourceType.count() + ro) * (Building.COUNT);
		index  = (index + Building.ordinal(b)) * (Unit.COUNT);
		index += Unit.ordinal(u);
		BufferedImage result = imgs[index];
		if (result != null) {
			return result;
		}
		BufferedImage ti = immage(tile);
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
	
	public static BufferedImage immage(ImageableObj obj) {
		synchronized (obj) {
			BufferedImage r = obj.resource();
			if (r == null) {
				r = loadImg(obj);
				obj.resource(r);
			}
			return r;
		}
	}
	
	private static BufferedImage loadImg(ImageableObj e) throws IOError {
		try {
			return ImageIO.read(e.url());
		} catch (IllegalArgumentException err) {
			System.err.println("error while loading image for " + e.cls() + ':' + e.name() + ": " + err.toString());
			throw err;
		} catch (IOException err) {
			System.err.println("error while loading image for " + e.cls() + ':' + e.name() + ": " + err.toString());
			throw new IOError(err);
		}
	}
	
}
