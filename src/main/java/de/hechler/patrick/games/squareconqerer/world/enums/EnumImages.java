package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.squareconqerer.Settings;

public class EnumImages {
	
	private EnumImages() { }
	
	private static BufferedImage[][] imgs;
	
	public static BufferedImage immage(TileType tile, OreResourceType res) {
		if (imgs == null) {
			imgs = new BufferedImage[TileType.count()][];
		}
		int             to  = tile.ordinal();
		BufferedImage[] arr = imgs[to];
		if (arr == null) {
			arr      = new BufferedImage[OreResourceType.count()];
			imgs[to] = arr;
		}
		int           ro     = res.ordinal();
		BufferedImage result = arr[ro];
		if (result != null) {
			return result;
		}
		boolean       shr = Settings.highResolution();
		BufferedImage img = immage(shr, tile);
		BufferedImage b   = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D    g   = b.createGraphics();
		g.drawImage(img, 0, 0, null);
		BufferedImage ri = immage(shr, res);
		g.drawImage(ri, 0, 0, null);
		g.dispose();
		arr[ro] = b;
		return b;
	}
	
	public static BufferedImage immage(ImageableObj obj) {
		return immage(Settings.highResolution(), obj);
	}
	
	private static BufferedImage immage(boolean shr, ImageableObj obj) {
		synchronized (obj) {
			BufferedImage r = obj.resource();
			if (r == null || obj.resolution() != shr) {
				r = loadImg(obj, shr);
				obj.resolution(shr);
				obj.resource(r);
			}
			return r;
		}
	}
	
	private static BufferedImage loadImg(ImageableObj e, boolean shr) throws IOError {
		Class<?> cls = e.getClass();
		try {
			if (!e.multipleResolutions()) {
				return ImageIO.read(cls.getResource("/img/" + cls.getSimpleName() + "/" + e.name() + ".png"));
			} else if (shr) {
				return ImageIO.read(cls.getResource("/img/" + cls.getSimpleName() + "/" + e.name() + "-high_res.png"));
			} else {
				return ImageIO.read(cls.getResource("/img/" + cls.getSimpleName() + "/" + e.name() + "-low_res.png"));
			}
		} catch (IOException err) {
			throw new IOError(err);
		}
	}
	
}
