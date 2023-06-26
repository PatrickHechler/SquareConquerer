package de.hechler.patrick.games.sc.addons.def;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.ground.Ground;


public class GrassGround extends Ground {
	
	private static BufferedImage img;
	
	public GrassGround(UUID uuid) {
		super(uuid);
	}
	
	private Image last;
	
	@Override
	public Image image(int width, int height) {
		Image l     = this.last;
		if (l != null && l.getWidth(null) == width) {
			return l;
		}
		if (img == null) {
			loadImg();
		}
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D    g      = result.createGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		this.last      = result;
		return result;
	}
	
	private static synchronized void loadImg() {
		if (img != null) return;
		try {
			img = ImageIO.read(MeeleUnit.class.getResource("/img/ground/grass.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	@Override
	public GroundType type() {
		return FigtherAddonProvider.GRASS_TYPE;
	}
	
	@Override
	public Map<String, Value> values() {
		return Map.of();
	}
	
	@Override
	public Value value(String name) {
		throw new UnsupportedOperationException("I have no values");
	}
	
	@Override
	public void value(Value newValue) {
		throw new UnsupportedOperationException("I can not have values");
	}
	
}
