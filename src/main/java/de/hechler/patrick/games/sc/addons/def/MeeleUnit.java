package de.hechler.patrick.games.sc.addons.def;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.addons.addable.UnitType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.entity.Entity;

public class MeeleUnit extends SimpleUnit {
	
	private static BufferedImage img;
	
	MeeleUnit(UUID uuid, Map<String, Value> vals) {
		super(uuid, vals);
	}
	
	private Image last;
	private int   lastLives;
	
	@Override
	public Image image(int width, int height) {
		Image l     = this.last;
		int   lives = lives();
		if (l != null && this.lastLives == lives && l.getWidth(null) == width) {
			return l;
		}
		if (img == null) {
			loadImg();
		}
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D    g      = result.createGraphics();
		g.drawImage(img, 0, 0, width, height, null);
		int maxxlen = width - 10;
		if (maxxlen > 0) {
			int len = (int) (maxxlen * (MeeleType.MAX_LIVES / (double) lives));
			g.setColor(Color.RED);
			g.drawLine(5, height - 5, 5 + len, height - 5);
		}
		g.dispose();
		this.last      = result;
		this.lastLives = lives;
		return result;
	}
	
	private static synchronized void loadImg() {
		if (img != null) return;
		try {
			img = ImageIO.read(MeeleUnit.class.getResource("/img/unit/meele.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	@Override
	public UnitType type() {
		return FigtherAddonProvider.MEELE_TYPE;
	}
	
	@Override
	protected double attackStrength(Entity<?, ?> enemy, int oldLives) throws TurnExecutionException {
		return 2.5D * super.attackStrength(enemy, oldLives);
	}
	
}
