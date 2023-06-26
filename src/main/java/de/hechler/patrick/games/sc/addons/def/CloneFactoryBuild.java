package de.hechler.patrick.games.sc.addons.def;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.BuildType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.ui.display.world.WorldDisplay;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Unit;


public class CloneFactoryBuild extends SimpleBuild {
	
	private static BufferedImage img;
	
	public CloneFactoryBuild(UUID uuid, Map<String, Value> vals) {
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
			TypeValue<?> type = typeValue(CloneFactoryType.CLONE_TYPE);
			if (type.value() != GroundType.NOT_EXPLORED_TYPE) {
				int len = (int) (maxxlen * (CloneFactoryType.MAX_TIME / (double) lives));
				g.setColor(Color.BLUE);
				g.drawLine(5, height - 10, 5 + len, height - 10);
			}
			int len = (int) (maxxlen * (CloneFactoryType.MAX_LIVES / (double) lives));
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
			img = ImageIO.read(MeeleUnit.class.getResource("/img/build/clone-factory.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	@Override
	public BuildType type() {
		return SimpleAddonsProvider.CLONE_FACTORY_TYPE;
	}
	
	@Override
	public void work(Unit u) throws TurnExecutionException {
		if (intValue(NEEDED_WORK_TURNS).value() > 0) {
			super.work(u);
			return;
		}
		TypeValue<?> type = typeValue(CloneFactoryType.CLONE_TYPE);
		if (type.value() == GroundType.NOT_EXPLORED_TYPE) {
			value(new TypeValue<>(CloneFactoryType.CLONE_TYPE, u.type()));
			value(new MapValue<Value>(CloneFactoryType.CLONE_STATS, u.values()));
			value(new IntValue(CloneFactoryType.CLONE_TIME, CloneFactoryType.MAX_TIME));
			return;
		}
		value(new IntValue(CloneFactoryType.CLONE_TIME, intValue(CloneFactoryType.CLONE_TIME).value() - u.workEfficency()));
	}
	
	@Override
	public Iterable<WorldThing<?, ?>> nextTurnNotify(int playerCount) {
		if (intValue(CloneFactoryType.CLONE_TIME).value() <= 0) {
			value(new IntValue(CloneFactoryType.CLONE_TIME, CloneFactoryType.MAX_TIME));
			AddableType<?, ?>  type = typeValue(CloneFactoryType.CLONE_TYPE).value();
			Map<String, Value> map  = mapValue(CloneFactoryType.CLONE_STATS).value();
			try {
				return List.of(type.withValues(map, WorldDisplay.NULL_UUID));
			} catch (@SuppressWarnings("unused") TurnExecutionException e) {
				value(new TypeValue<>(CloneFactoryType.CLONE_TYPE, GroundType.NOT_EXPLORED_TYPE));
			}
		}
		return null;
	}
	
}
