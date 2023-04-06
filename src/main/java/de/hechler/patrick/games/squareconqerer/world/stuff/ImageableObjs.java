package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;

public class ImageableObjs {
	
	private ImageableObjs() {}
	
	private static BufferedImage[] imgs;
	
	public static BufferedImage immage(TileType tile, OreResourceType res, Building b, Unit u) {
		if (imgs == null) {
			imgs = new BufferedImage[OreResourceType.count() * TileType.count() * (Building.COUNT + 1) * (Unit.COUNT + 1)];
		}
		int to    = tile.ordinal();
		int ro    = res.ordinal();
		int index = (to * OreResourceType.count() + ro) * (Building.COUNT + 1);
		index  = (index + Building.ordinal(b)) * (Unit.COUNT + 1);
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
			return ImageIO.read(e.getClass().getResource("/img/" + e.cls() + "/" + e.name() + ".png"));
		} catch (IllegalArgumentException err) {
			System.err.println("error while loading image for " + e.cls() + ':' + e.name() + ": " + err.toString());
			throw err;
		} catch (IOException err) {
			System.err.println("error while loading image for " + e.cls() + ':' + e.name() + ": " + err.toString());
			throw new IOError(err);
		}
	}
	
}
