package de.hechler.patrick.sc.objects.ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.objects.AbsoluteMegaManipulablePosition;
import de.hechler.patrick.sc.objects.FieldImpl;
import de.hechler.patrick.sc.objects.NicePosition;
import de.hechler.patrick.sc.objects.UnchangeablePosition;
import de.hechler.patrick.sc.objects.World;
import de.hechler.patrick.sc.utils.factory.EntityFactory;

public class WorldWindow extends JFrame {

	/** UID */
	private static final long serialVersionUID = 789464080026285409L;

	private final Map<UnchangeablePosition, JButton> fields;
	private final List<PositionListener> listen;
	private boolean debugWarns = true;

	public WorldWindow(int xc, int yc) {
		super("World-Window (" + xc + "|" + yc + ")");
		this.fields = new HashMap<>();
		this.listen = new ArrayList<>();
	}

	public static void main(String[] args) {
		World world = new World(60, 35); // biggest window (1920*1200)
		WorldWindow window = new WorldWindow(world.getXCnt(), world.getYCnt()).load(world.getXCnt(), world.getYCnt(),
				false);
		Random rnd = new Random();
		Grounds[] g = Grounds.values();
		for (int i = 0; i < world.getXCnt(); i++) {
			for (int ii = 0; ii < world.getYCnt(); ii++) {
				world.overrideField(new FieldImpl(i, ii, g[rnd.nextInt(g.length - 1)])); // unknown is the last
			}
		}
		window.update(world, true);
		NicePosition pos = new NicePosition(0, 0);
		Entity e = EntityFactory.create(4, pos, de.hechler.patrick.sc.enums.Type.carrier, null);
		world.getField(pos).setEntity(e);
		pos.move(Direction.up);
		e = EntityFactory.create(4, pos, de.hechler.patrick.sc.enums.Type.boat, null);
		world.getField(pos).setEntity(e);
		window.update(world, true);
	}

	public WorldWindow load(int xc, int yc, boolean visible) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(null);

		setBounds(0, 0, 15 + xc * 30, 40 + yc * 30);
		setLocationRelativeTo(null);
		setResizable(false);

		if (visible) {
			setVisible(true);
			toFront();
			repaint();
		} else {
			setVisible(false);
		}
		return this;
	}

	public void update(World world, boolean visible) {
		final int xc = world.getXCnt(), yc = world.getYCnt();
		for (AbsoluteMegaManipulablePosition pos = new AbsoluteMegaManipulablePosition(0, 0); pos.x < xc; pos.x++) {
			for (pos.y = 0; pos.y < yc; pos.y++) {
				setValueAt(world.getField(pos), pos.x, pos.y);
			}
		}
		if (visible) {
			setVisible(true);
			toFront();
			repaint();
		} else {
			setVisible(false);
		}
	}

	public void setValueAt(Field f, int x, int y) {
		// System.out.println(f.position().toPosStr() + " " + f.ground().name());//
		// debug
		final UnchangeablePosition pos = new UnchangeablePosition(x, y);
		JButton b;
		if (!fields.containsKey(pos)) {
			b = new JButton();
			b.setBounds(x * 30, y * 30, 30, 30);
			b.addActionListener(p -> {
				if ((p.getModifiers() & p.KEY_EVENT_MASK) != 0)
					return;
				buttonPressed(pos);
			});
			fields.put(pos, b);
			add(b);
		}
		b = fields.get(pos);
		Icon image = getIcon(f);
		b.setIcon(image);
		b.setDisabledIcon(image);
	}

	private void buttonPressed(UnchangeablePosition pos) {
		if (listen.isEmpty())
			warn("no listener, but Position" + pos.toPosStr() + " was pressed!");
		listen.forEach(l -> l.positionPressed(pos));
	}

	public static interface PositionListener {

		void positionPressed(UnchangeablePosition pressed);

	}

	private Icon getIcon(Field f) {
		BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		String zw = "./icons/" + f.ground().name() + ".png";
		if (!Files.exists(Paths.get(zw)))
			zw = "../icons/" + f.ground().name() + ".png";
		g2d.drawImage(new ImageIcon(zw).getImage(), 0, 0, null);
		if (f.hasEntity()) {
			zw = "./icons/" + f.getEntity().type().name() + ".png";
			if (!Files.exists(Paths.get(zw)))
				zw = "../icons/" + f.getEntity().type().name() + ".png";
			g2d.drawImage(new ImageIcon("./icons/" + f.getEntity().type().name() + ".png").getImage(), 0, 0, null);
		}
		g2d.dispose();
		// ImageIcon icon = new ImageIcon();
		// icon.setImage(image);
		// return icon;
		return new ImageIcon(image);
	}

	private void warn(String warn) {
		if (!this.debugWarns)
			return;
		System.err.println("WARNING: " + warn);
	}

	public void setDebugWarns(boolean debugWarns) {
		this.debugWarns = debugWarns;
	}

	public void addListener(PositionListener listen) {
		this.listen.add(listen);
	}

}
