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
package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.hechler.patrick.games.sc.Settings;
import de.hechler.patrick.games.sc.addons.Addon;
import de.hechler.patrick.games.sc.addons.Addons;
import de.hechler.patrick.games.sc.addons.GroupTree;
import de.hechler.patrick.games.sc.addons.TheBaseAddonProvider;
import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.addons.addable.GroundType;
import de.hechler.patrick.games.sc.addons.addable.ResourceType;
import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.ui.display.PageDisplay;
import de.hechler.patrick.games.sc.ui.display.TextPageDisplay;
import de.hechler.patrick.games.sc.ui.display.world.utils.FPNumberDocument;
import de.hechler.patrick.games.sc.ui.display.world.utils.NumberDocument;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.BooleanValue;
import de.hechler.patrick.games.sc.values.DoubleValue;
import de.hechler.patrick.games.sc.values.EnumValue;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.LongValue;
import de.hechler.patrick.games.sc.values.StringValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.UserValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.spec.BooleanSpec;
import de.hechler.patrick.games.sc.values.spec.DoubleSpec;
import de.hechler.patrick.games.sc.values.spec.EnumSpec;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.values.spec.JustASpec;
import de.hechler.patrick.games.sc.values.spec.ListSpec;
import de.hechler.patrick.games.sc.values.spec.LongSpec;
import de.hechler.patrick.games.sc.values.spec.MapSpec;
import de.hechler.patrick.games.sc.values.spec.StringSpec;
import de.hechler.patrick.games.sc.values.spec.TypeSpec;
import de.hechler.patrick.games.sc.values.spec.UserSpec;
import de.hechler.patrick.games.sc.values.spec.ValueSpec;
import de.hechler.patrick.games.sc.values.spec.WorldThingSpec;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.OpenWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.ground.Ground;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;
import de.hechler.patrick.utils.objects.Random2;

public class WorldDisplay implements ButtonGridListener {
	
	private Thread                serverThread;
	private Map<User, Connection> connects;
	private World                 world;
	private ButtonGrid            grid;
	private ScrollPane            scroll;
	private Frame                 frame;
	
	public WorldDisplay(World world) {
		this.world  = world;
		this.grid   = new ButtonGrid(world.xlen(), world.ylen(), 128);
		this.scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		this.frame  = new Frame();
	}
	
	private static final Image MARK_IMG;
	private static final Image SINGLE_MARK_IMG;
	
	static {
		try {
			MARK_IMG        = ImageIO.read(WorldDisplay.class.getResource("/img/mark.png"));
			SINGLE_MARK_IMG = ImageIO.read(WorldDisplay.class.getResource("/img/single-mark.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	private static final int MODE_NONE        = 0;
	private static final int MODE_PRESS       = 1;
	private static final int MODE_DRAG_MARK   = 2;
	private static final int MODE_SINGLE_MARK = 3;
	
	private Object data;
	
	private int modifiers;
	private int mode;
	private int x;
	private int y;
	private int lx;
	private int ly;
	
	private void execMarkedAct() {
		System.out.println("exec mark: x: " + this.x + " y: " + this.y + " lx: " + this.lx + " ly: " + this.ly);
	}
	
	@SuppressWarnings("preview")
	private void execTileAct() {
		System.out.println("exec: x: " + this.x + " y: " + this.y);
		if (this.world instanceof CompleteWorld.Builder b) {
			switch (this.data) {
			case Ground g when this.modifiers == InputEvent.BUTTON1_DOWN_MASK -> {
				try {// save to use UUID.random, because the game did not start, so it does not need to be reproduced
					b.setGround(this.x, this.y, g.type().withValues(g.values(), UUID.randomUUID()));
					updateBtn(this.x, this.y);
				} catch (@SuppressWarnings("unused") TurnExecutionException e) {/**/}
			}
			case Resource r when this.modifiers == InputEvent.BUTTON1_DOWN_MASK -> {
				try {// save to use UUID.random, because the game did not start, so it does not need to be reproduced
					b.addResource(this.x, this.y, r.type().withValues(r.values(), UUID.randomUUID()));
					updateBtn(this.x, this.y);
				} catch (@SuppressWarnings("unused") TurnExecutionException e) {/**/}
			}
			case Resource r when this.modifiers == InputEvent.BUTTON3_DOWN_MASK -> {
				try {// save to use UUID.random, because the game did not start, so it does not need to be reproduced
					b.removeResource(this.x, this.y, r.type().withValues(r.values(), UUID.randomUUID()));
					updateBtn(this.x, this.y);
				} catch (@SuppressWarnings("unused") TurnExecutionException e) {/**/}
			}
			case null, default -> {
				JDialog d = new JDialog(this.frame);
				d.setTitle("choose action");
				JPanel dp = new JPanel();
				dp.setLayout(null);
				d.setContentPane(dp);
				int  maxx;
				int  yoff;
				Tile t = b.get(this.x, this.y);
				if (t == null) {
					JButton btn = new JButton("empty tile");
					btn.setLocation(0, 0);
					btn.setSize(btn.getPreferredSize());
					dp.add(btn);
					maxx = btn.getWidth();
					yoff = btn.getHeight();
					btn.addActionListener(e -> chooseGround(d));
				} else {
					JLabel l = new JLabel("ground: ");
					l.setLocation(0, 0);
					l.setSize(l.getPreferredSize());
					dp.add(l);
					maxx = l.getWidth();
					JButton btn = new JButton(t.ground().type().localName);
					l.setLocation(maxx, 0);
					l.setSize(l.getPreferredSize());
					dp.add(l);
					maxx = btn.getWidth();
					yoff = Math.max(l.getHeight(), btn.getHeight());
					btn.addActionListener(e -> chooseGround(d));
					if (t.resourceCount() != 0) {
						l = new JLabel("resources: ");
						l.setLocation(0, yoff);
						l.setSize(l.getPreferredSize());
						dp.add(l);
						JList<?> list = new JList<>(t.resourcesStream().map(r -> r.type().localName).toArray());
						list.setLocation(l.getWidth(), yoff);
						list.setSize(list.getPreferredSize());
						dp.add(list);
						maxx  = Math.max(maxx, l.getWidth() + list.getWidth());
						yoff += Math.max(l.getHeight(), list.getHeight());
					}
					if (t.build() != null) {
						l = new JLabel("build: " + t.build().type().localName);
						l.setLocation(0, yoff);
						l.setSize(l.getPreferredSize());
						dp.add(l);
						maxx  = Math.max(maxx, l.getWidth());
						yoff += l.getHeight();
					}
					if (t.unitCount() != 0) {
						l = new JLabel("resources: ");
						l.setLocation(0, yoff);
						l.setSize(l.getPreferredSize());
						dp.add(l);
						JList<?> list = new JList<>(t.unitsStream().map(u -> u.type().localName).toArray());
						list.setLocation(l.getWidth(), yoff);
						list.setSize(list.getPreferredSize());
						dp.add(list);
						maxx  = Math.max(maxx, l.getWidth() + list.getWidth());
						yoff += Math.max(l.getHeight(), list.getHeight());
					}
				}
				JButton btn = new JButton("set ground");
				btn.setLocation(0, yoff);
				btn.setSize(btn.getPreferredSize());
				dp.add(btn);
				btn.addActionListener(e -> chooseGround(d));
				maxx  = Math.max(maxx, btn.getWidth());
				yoff += btn.getHeight();
				btn   = new JButton("add resource");
				btn.setLocation(0, yoff);
				btn.setSize(btn.getPreferredSize());
				dp.add(btn);
				btn.addActionListener(e -> chooseResource(d, true));
				maxx  = Math.max(maxx, btn.getWidth());
				yoff += btn.getHeight();
				btn   = new JButton("remove resource");
				btn.setLocation(0, yoff);
				btn.setSize(btn.getPreferredSize());
				dp.add(btn);
				btn.addActionListener(e -> chooseResource(d, false));
				maxx  = Math.max(maxx, btn.getWidth());
				yoff += btn.getHeight();
				dp.setPreferredSize(new Dimension(maxx, yoff));
				PageDisplay.initDialog(d, this.frame);
			}
			}
		} else {
			System.out.println("else");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void costumize(JDialog grandParent, JDialog parent, AddableType<?, ?> type, int modifiers) {
		JDialog d  = new JDialog(parent);
		JPanel  dp = new JPanel();
		dp.setLayout(null);
		d.setContentPane(new JScrollPane(dp));
		d.setTitle("customize type");
		int                maxx = 0;
		int                yoff = 0;
		Map<String, Value> val  = type.withDefaultValues(this.world, new Random2(), this.x, this.y).values();
		for (ValueSpec spec : type.values.values()) {
			JLabel l = new JLabel(spec.localName() + ": ");
			l.setLocation(0, yoff);
			l.setSize(l.getPreferredSize());
			dp.add(l);
			switch (spec) {
			case @SuppressWarnings("preview") BooleanSpec b -> {
				JCheckBox cb = new JCheckBox();
				cb.setSelected(((BooleanValue) val.get(b.name())).value());
				cb.addActionListener(e -> val.put(b.name(), b.withValue(cb.isSelected())));
				cb.setSize(cb.getPreferredSize());
				cb.setLocation(l.getWidth(), yoff);
				dp.add(cb);
				maxx  = Math.max(maxx, l.getWidth() + cb.getWidth());
				yoff += Math.max(l.getHeight(), cb.getHeight());
			}
			case @SuppressWarnings("preview") DoubleSpec n -> {
				FPNumberDocument doc = new FPNumberDocument(n.min(), n.max());
				JTextField       t   = new JTextField(doc, Double.toString(((DoubleValue) val.get(n.name())).value()), 0);
				t.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusLost(@SuppressWarnings("unused") java.awt.event.FocusEvent e) {
						val.put(n.name(), n.withValue(doc.getNumber(((DoubleValue) val.get(n.name())).value())));
					}
					
				});
				t.setSize(t.getPreferredSize());
				t.setLocation(l.getWidth(), yoff);
				dp.add(t);
				maxx  = Math.max(maxx, l.getWidth() + t.getWidth());
				yoff += Math.max(l.getHeight(), t.getHeight());
			}
			case @SuppressWarnings("preview") EnumSpec s -> {
				JComboBox<?> cb = new JComboBox<>(s.cls().getEnumConstants());
				cb.setSelectedIndex(((EnumValue<?>) val.get(s.name())).value().ordinal());
				cb.addActionListener(e -> val.put(s.name(), s.withValue((Enum) cb.getSelectedItem())));
				cb.setSize(cb.getPreferredSize());
				cb.setLocation(l.getWidth(), yoff);
				dp.add(cb);
				maxx  = Math.max(maxx, l.getWidth() + cb.getWidth());
				yoff += Math.max(l.getHeight(), cb.getHeight());
			}
			case @SuppressWarnings("preview") IntSpec i -> {
				NumberDocument doc = new NumberDocument(i.min(), i.max());
				JTextField     t   = new JTextField(doc, Integer.toString(((IntValue) val.get(i.name())).value()), 0);
				t.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusLost(@SuppressWarnings("unused") java.awt.event.FocusEvent e) {
						val.put(i.name(), i.withValue(doc.getNumber(((IntValue) val.get(i.name())).value())));
					}
					
				});
				t.setSize(t.getPreferredSize());
				t.setLocation(l.getWidth(), yoff);
				dp.add(t);
				maxx  = Math.max(maxx, l.getWidth() + t.getWidth());
				yoff += Math.max(l.getHeight(), t.getHeight());
			}
			case @SuppressWarnings("preview") JustASpec j -> {
				JLabel ol = new JLabel("just some value");
				ol.setSize(ol.getPreferredSize());
				ol.setLocation(l.getWidth(), yoff);
				dp.add(ol);
				maxx  = Math.max(maxx, l.getWidth() + ol.getWidth());
				yoff += Math.max(l.getHeight(), ol.getHeight());
			}
			case @SuppressWarnings("preview") LongSpec n -> {
				NumberDocument doc = new NumberDocument(n.min(), n.max());
				JTextField     t   = new JTextField(doc, Long.toString(((LongValue) val.get(n.name())).value()), 0);
				t.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusLost(@SuppressWarnings("unused") java.awt.event.FocusEvent e) {
						val.put(n.name(), n.withValue(doc.getNumber(((LongValue) val.get(n.name())).value())));
					}
					
				});
				t.setSize(t.getPreferredSize());
				t.setLocation(l.getWidth(), yoff);
				dp.add(t);
				maxx  = Math.max(maxx, l.getWidth() + t.getWidth());
				yoff += Math.max(l.getHeight(), t.getHeight());
			}
			case @SuppressWarnings("preview") StringSpec s -> {
				JTextField t = new JTextField(((StringValue) val.get(s.name())).value());
				t.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusLost(@SuppressWarnings("unused") java.awt.event.FocusEvent e) {
						val.put(s.name(), s.withValue(t.getText()));
					}
					
				});
				t.setSize(t.getPreferredSize());
				t.setLocation(l.getWidth(), yoff);
				dp.add(t);
				maxx  = Math.max(maxx, l.getWidth() + t.getWidth());
				yoff += Math.max(l.getHeight(), t.getHeight());
			}
			case @SuppressWarnings("preview") TypeSpec t -> {
				List<AddableType<?, ?>> list = new ArrayList<>();
				for (Addon a : Addons.addons().values()) {
					for (AddableType<?, ?> at : a.add.values()) {
						if (t.cls().isInstance(at)) list.add(at);
					}
				}
				JComboBox<?> cb = new JComboBox<>(list.toArray());
				cb.setSelectedItem(((TypeValue<?>) val.get(t.name())).value());
				cb.addActionListener(e -> val.put(t.name(), t.withValue((AddableType<?, ?>) cb.getSelectedItem())));
				cb.setSize(cb.getPreferredSize());
				cb.setLocation(l.getWidth(), yoff);
				dp.add(cb);
				maxx  = Math.max(maxx, l.getWidth() + cb.getWidth());
				yoff += Math.max(l.getHeight(), cb.getHeight());
			}
			case @SuppressWarnings("preview") UserSpec u -> {
				List<User> list = new ArrayList<>();
				list.addAll(this.world.user().users().values());
				list.sort((a,b) -> a.name().compareTo(b.name()));
				JComboBox<?> cb = new JComboBox<>(list.toArray());
				cb.setSelectedItem(((UserValue) val.get(u.name())).value());
				cb.addActionListener(e -> val.put(u.name(), u.withValue((User) cb.getSelectedItem())));
				cb.setSize(cb.getPreferredSize());
				cb.setLocation(l.getWidth(), yoff);
				dp.add(cb);
				maxx  = Math.max(maxx, l.getWidth() + cb.getWidth());
				yoff += Math.max(l.getHeight(), cb.getHeight());
			}
			case @SuppressWarnings("preview") ListSpec u -> {
				//TODO
			}
			case @SuppressWarnings("preview") MapSpec m -> {
				//TODO
			}
			case @SuppressWarnings("preview") WorldThingSpec w -> {
				//TODO
			}
			}
		}
	}
	
	private void chooseResource(JDialog parent, boolean add) {
		JDialog d  = new JDialog(parent);
		JPanel  dp = new JPanel();
		dp.setLayout(null);
		d.setContentPane(new JScrollPane(dp));
		d.setTitle("choose resource");
		List<ResourceType> list = new ArrayList<>();
		for (Addon a : Addons.addons().values()) {
			for (AddableType<?, ?> at : a.add.values()) {
				if (at instanceof ResourceType rt) list.add(rt);
			}
		}
		list.sort((a, b) -> {
			int c = a.localName.compareTo(b.localName);
			if (c == 0 && a != b) c = a.name.compareTo(b.name); // well, the user can to try both, the order remains
			return c;
		});
		JComboBox<?> cb = new JComboBox<>(list.stream().map(rt -> rt.localName).toArray());
		cb.setLocation(0, 0);
		cb.setSize(cb.getPreferredSize());
		dp.add(cb);
		cb.addActionListener(e -> costumize(parent, d, list.get(cb.getSelectedIndex()), add ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK));
		PageDisplay.initDialog(d, parent);
	}
	
	private void chooseGround(JDialog parent) {
		JDialog d  = new JDialog(parent);
		JPanel  dp = new JPanel();
		dp.setLayout(null);
		d.setContentPane(new JScrollPane(dp));
		d.setTitle("choose ground");
		List<GroundType> list = new ArrayList<>();
		for (Addon a : Addons.addons().values()) {
			for (AddableType<?, ?> at : a.add.values()) {
				if (at instanceof GroundType gt) list.add(gt);
			}
		}
		list.sort((a, b) -> {
			int c = a.localName.compareTo(b.localName);
			if (c == 0 && a != b) c = a.name.compareTo(b.name); // well, the user can to try both, the order remains
			return c;
		});
		JComboBox<?> cb = new JComboBox<>(list.stream().map(rt -> rt.localName).toArray());
		cb.setLocation(0, 0);
		cb.setSize(cb.getPreferredSize());
		dp.add(cb);
		cb.addActionListener(e -> costumize(parent, d, list.get(cb.getSelectedIndex()), InputEvent.BUTTON1_DOWN_MASK));
		PageDisplay.initDialog(d, parent);
	}
	
	/** {@inheritDoc} */
	@Override
	public void kTyped(KeyEvent e) {
		if (this.mode != MODE_SINGLE_MARK) {
			return;
		}
		int prevX;
		int prevY;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP, KeyEvent.VK_W -> {
			prevY = this.y;
			prevX = this.x;
			if (prevY <= 0) {
				return;
			}
			this.y = prevY - 1;
		}
		case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
			prevY = this.y;
			prevX = this.x;
			if (prevY + 1 >= this.world.ylen()) {
				return;
			}
			this.y = prevY + 1;
		}
		case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
			prevY = this.y;
			prevX = this.x;
			if (prevX <= 0) {
				return;
			}
			this.x = prevX - 1;
		}
		case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
			prevY = this.y;
			prevX = this.x;
			if (prevX + 1 >= this.world.xlen()) {
				return;
			}
			this.x = prevX + 1;
		}
		case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
			execTileAct();
			return;
		}
		case KeyEvent.VK_F1 -> {
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0) {
				e.setKeyChar('\n');
				e.setKeyCode(KeyEvent.VK_ENTER);
				this.frame.getMenuBar().getHelpMenu().getItem(0).dispatchEvent(e);
			}
			return;
		}
		default -> {
			return;
		}
		}
		int           mul = this.grid.getButtonSize();
		BufferedImage img = new BufferedImage(mul, mul, BufferedImage.TYPE_INT_RGB);
		Graphics2D    g   = img.createGraphics();
		updateBtn(img, g, prevX, prevY, mul);
		g.dispose();
		this.grid.paintBtn(this.x, this.y, SINGLE_MARK_IMG);
		if (this.x < prevX) {
			this.grid.repaint(this.x * mul, prevY * mul, mul << 1, mul);
		} else if (this.x > prevX) {
			this.grid.repaint(prevX * mul, prevY * mul, mul << 1, mul);
		} else if (this.y < prevY) {
			this.grid.repaint(prevX * mul, this.y * mul, mul, mul << 1);
		} else {
			this.grid.repaint(prevX * mul, prevY * mul, mul, mul << 1);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void mMoved(MouseEvent e, int x, int y, int subx, int suby, boolean dragging) {
		if (dragging) {
			this.mode = MODE_DRAG_MARK;
			x         = Math.max(0, Math.min(x, this.world.xlen() - 1));
			y         = Math.max(0, Math.min(y, this.world.ylen() - 1));
			if (x == this.lx && y == this.ly) {
				return;
			}
			if (this.lx == -1) {
				this.x  = x;
				this.lx = x;
				this.y  = y;
				this.ly = y;
				int mul = this.grid.getButtonSize();
				this.grid.paintBtn(x, y, MARK_IMG);
				this.grid.repaint(x * mul, y * mul, mul, mul);
				return;
			}
			int           mul    = this.grid.getButtonSize();
			int           olowx  = Math.min(this.x, this.lx);
			int           olowy  = Math.min(this.y, this.ly);
			int           ohighx = Math.max(this.x, this.lx);
			int           ohighy = Math.max(this.y, this.ly);
			int           nlowx  = Math.min(this.x, x);
			int           nlowy  = Math.min(this.y, y);
			int           nhighx = Math.max(this.x, x);
			int           nhighy = Math.max(this.y, y);
			BufferedImage img    = new BufferedImage(mul, mul, BufferedImage.TYPE_INT_RGB);
			Graphics2D    g      = img.createGraphics();
			for (int cx = Math.max(0, olowx); cx <= ohighx && cx < this.world.xlen(); cx++) {
				for (int cy = Math.max(0, olowy); cy <= ohighy && cy < this.world.ylen(); cy++) {
					if (nlowx <= cx && cx <= nhighx && nlowy <= cy && cy <= nhighy) {
						cy = nhighy;
						continue;
					}
					updateBtn(img, g, cx, cy, mul);
				}
			}
			for (int cx = nlowx; cx <= nhighx; cx++) {
				for (int cy = nlowy; cy <= nhighy; cy++) {
					if (olowx <= cx && cx <= ohighx && olowy <= cy && cy <= ohighy) {
						cy = ohighy;
						continue;
					}
					this.grid.paintBtn(cx, cy, MARK_IMG);
				}
			}
			int minx = Math.min(nlowx, olowx);
			int miny = Math.min(nlowy, olowy);
			int maxx = Math.max(nhighx, ohighx);
			int maxy = Math.max(nhighy, ohighy);
			this.grid.repaint(minx * mul, miny * mul, (maxx - minx + 1) * mul, (maxy - miny + 1) * mul);
			g.dispose();
			this.lx = x;
			this.ly = y;
		}
		
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void mPressed(MouseEvent e, int x, int y, int subx, int suby) {
		if (this.mode == MODE_SINGLE_MARK) {
			int           mul = this.grid.getButtonSize();
			int           tx  = this.x;
			int           ty  = this.y;
			BufferedImage img = new BufferedImage(mul, mul, BufferedImage.TYPE_INT_RGB);
			Graphics2D    g   = img.createGraphics();
			updateBtn(img, g, tx, ty, mul);
			g.dispose();
			this.grid.repaint(tx * mul, ty * mul, mul, mul);
		} else if (this.mode == MODE_DRAG_MARK) {
			updateBtns();
		}
		this.x         = x;
		this.y         = y;
		this.lx        = -1;
		this.ly        = -1;
		this.mode      = MODE_PRESS;
		this.modifiers = e.getModifiersEx();
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void mReleased(MouseEvent e, int x, int y, int subx, int suby) {
		int ty = this.y;
		int tx = this.x;
		x = Math.max(0, Math.min(x, this.world.xlen() - 1));
		y = Math.max(0, Math.min(y, this.world.ylen() - 1));
		if (x != tx || y != ty || this.mode == MODE_DRAG_MARK) {
			execMarkedAct();
			this.mode = MODE_NONE;
			int           minx  = Math.min(this.x, this.lx);
			int           miny  = Math.min(this.y, this.ly);
			int           maxx  = Math.max(this.x, this.lx);
			int           maxy  = Math.max(this.y, this.ly);
			int           bsize = this.grid.getButtonSize();
			BufferedImage img   = new BufferedImage(bsize, bsize, BufferedImage.TYPE_INT_RGB);
			Graphics2D    g     = img.createGraphics();
			for (int cx = minx; cx <= maxx; cx++) {
				for (int cy = miny; cy <= maxy; cy++) {
					updateBtn(img, g, cx, cy, bsize);
				}
			}
			g.dispose();
			this.grid.repaint(minx * bsize, miny * bsize, (maxx - minx + 1) * bsize, (maxy - miny + 1) * bsize);
			return;
		}
		this.mode = MODE_SINGLE_MARK;
		this.grid.paintBtn(tx, ty, SINGLE_MARK_IMG);
		int mul = this.grid.getButtonSize();
		this.grid.repaint(tx * mul, ty * mul, mul, mul);
		execTileAct();
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void mWhelling(MouseWheelEvent e, int x, int y, int subx, int suby) {
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			this.grid.setButtonSize(Math.max(1, this.grid.getButtonSize() - e.getUnitsToScroll()));
			updateBtns();
			this.scroll.setPreferredSize(this.grid.getPreferredSize());
			pack(false);
			this.grid.repaint();
		} else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
			Point pos = this.scroll.getScrollPosition();
			this.scroll.setScrollPosition(pos.x + e.getUnitsToScroll() * this.grid.getButtonSize() / 16, pos.y);
		} else {
			Point pos = this.scroll.getScrollPosition();
			this.scroll.setScrollPosition(pos.x, pos.y + e.getUnitsToScroll() * this.grid.getButtonSize() / 16);
		}
	}
	
	/**
	 * initializes this display and shows it
	 * <p>
	 * this method should only be called once after construction
	 */
	public void init() {
		this.scroll.add(this.grid);
		this.frame.add(this.scroll);
		this.frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(@SuppressWarnings("unused") WindowEvent e) {
				doQuit();
			}
			
		});
		this.grid.addBGListener(this);
		rebuildFrame(true);
	}
	
	private void rebuildFrame(boolean firstCall) {
		this.frame.setTitle(this.world.getClass().getSimpleName() + ": (" + this.world.xlen() + '|' + this.world.ylen() + ") : " + this.world.user().name());
		if (this.grid.getXlen() != this.world.xlen() || this.grid.getYlen() != this.world.ylen()) {
			this.grid.setButtonCount(this.world.xlen(), this.world.ylen());
		}
		MenuBar   mb = new MenuBar();
		GroupTree gt = new GroupTree();
		for (Addon a : Addons.addons().values()) gt.add(a);
		mb.setHelpMenu(menuHelp(gt));
		mb.add(menuSave());
		mb.add(menuBuild());
		mb.add(menuServer());
		mb.add(menuAddons(gt));
		mb.add(menuLicense(gt));
		mb.add(menuCredits(gt));
		this.frame.setMenuBar(mb);
		updateBtns();
		this.frame.setLocationByPlatform(true);
		this.scroll.setPreferredSize(this.grid.getPreferredSize());
		pack(firstCall);
	}
	
	private final PageDisplay pd = new PageDisplay((d, w) -> {
		if (EventQueue.isDispatchThread()) {
			int c = JOptionPane.showConfirmDialog(d, "replace the current world (" + this.world.getClass().getSimpleName() + ')', "replace world",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (c != JOptionPane.OK_OPTION) return;
			if (this.world instanceof Closeable clos) {
				try {
					clos.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(d, e.toString(), "error on close", JOptionPane.WARNING_MESSAGE);
				}
			}
			this.world = w;
			rebuildFrame(false);
			return;
		}
		EventQueue.invokeLater(() -> {
			int c = JOptionPane.showConfirmDialog(d, "replace the current world (" + this.world.getClass().getSimpleName() + ')', "replace world",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (c != JOptionPane.OK_OPTION) return;
			if (this.world instanceof Closeable clos) {
				try {
					clos.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(d, e.toString(), "error on close", JOptionPane.WARNING_MESSAGE);
				}
			}
			this.world = w;
			rebuildFrame(false);
		});
	}, () -> "discard the " + this.world.getClass().getSimpleName() + " world?");
	
	private Menu menuCredits(GroupTree gt) {
		Menu m = new Menu("Credits");
		gt.forEach(a -> {
			if (!a.hasCredits()) return;
			MenuItem mi = new MenuItem(a.localName);
			m.add(mi);
			mi.addActionListener(e -> this.pd.display(a.credits(), this.frame));
		}, this.addLicenseRBC, m);
		return m;
	}
	
	private final BiConsumer<GroupTree, Menu> addLicenseRBC = addLicenseRBC();
	
	// do this in a method in order to be able to refer to ADD_LICENSE_RBC
	private BiConsumer<GroupTree, Menu> addLicenseRBC() {
		return (gt, m) -> {
			Menu sub = new Menu(gt.name());
			m.add(sub);
			gt.forEach(a -> {
				MenuItem mi = new MenuItem(a.localName + " : " + a.licenseName);
				sub.add(mi);
				mi.addActionListener(e -> TextPageDisplay.display(a.license(), this.frame));
			}, this.addLicenseRBC, sub);
		};
	}
	
	private Menu menuLicense(GroupTree gt) {
		Menu m = new Menu("License");
		gt.forEach(a -> {
			MenuItem mi = new MenuItem(a.localName + " : " + a.licenseName);
			m.add(mi);
			mi.addActionListener(e -> TextPageDisplay.display(a.license(), this.frame));
		}, this.addLicenseRBC, m);
		return m;
	}
	
	private Menu menuAddons(GroupTree gt) {
		Menu     m      = new Menu("Addons");
		MenuItem manage = new MenuItem("manage");
		m.add(manage);
		manage.addActionListener(e -> doManageAddons(gt));
		return m;
	}
	
	private static final BiConsumer<GroupTree, DefaultMutableTreeNode> ADD_ADDONS_TO_NODE = addAddonsToList();
	
	private static BiConsumer<GroupTree, DefaultMutableTreeNode> addAddonsToList() {
		return (gt, node) -> {
			DefaultMutableTreeNode inner = new DefaultMutableTreeNode(gt.name());
			gt.forEach(a -> inner.add(new DefaultMutableTreeNode(a.localName)), ADD_ADDONS_TO_NODE, inner);
			node.add(inner);
		};
	}
	
	private void doManageAddons(GroupTree enabled) {
		GroupTree disabled = Addons.disabledGT();
		JDialog   d        = new JDialog(this.frame);
		JPanel    dp       = new JPanel();
		d.setContentPane(new JScrollPane(dp));
		DefaultMutableTreeNode eroot = new DefaultMutableTreeNode("enabled");
		enabled.forEach(a -> eroot.add(new DefaultMutableTreeNode(a.localName)), ADD_ADDONS_TO_NODE, eroot);
		JTree etree = new JTree(eroot);
		etree.setRootVisible(true);
		dp.add(etree);
		DefaultMutableTreeNode droot = new DefaultMutableTreeNode("disabled");
		disabled.forEach(a -> droot.add(new DefaultMutableTreeNode(a.localName)), ADD_ADDONS_TO_NODE, droot);
		JTree dtree = new JTree(droot);
		dtree.setRootVisible(true);
		dp.add(dtree);
		PageDisplay.initDialog(d, this.frame);
	}
	
	private Menu menuSave() {
		Menu     m    = new Menu("Saves");
		MenuItem load = new MenuItem("load");
		m.add(load);
		load.addActionListener(e -> doSave());
		MenuItem save = new MenuItem("save", new MenuShortcut(KeyEvent.VK_S, false));
		m.add(save);
		save.addActionListener(e -> doLoad());
		MenuItem quit = new MenuItem("quit", new MenuShortcut(KeyEvent.VK_Q, false));
		m.add(quit);
		quit.addActionListener(e -> doQuit());
		return m;
	}
	
	private void doQuit() {
		int c = JOptionPane.showConfirmDialog(this.frame, "Quit Square Conquerer?", "quit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (c == JOptionPane.OK_OPTION) {
			if (this.world instanceof Closeable clos) {
				try {
					clos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			System.exit(0);
		}
	}
	
	private File pickFile(File file, boolean save) {
		if (!file.exists()) {
			file.mkdir();
		}
		JFileChooser fc = new JFileChooser(file);
		fc.setMultiSelectionEnabled(false);
		int val = save ? fc.showSaveDialog(this.frame) : fc.showOpenDialog(this.frame);
		if (val != JFileChooser.APPROVE_OPTION) return null;
		file = fc.getSelectedFile();
		if (save) {
			if (!file.isFile()) {
				if (file.exists()) {
					JOptionPane.showConfirmDialog(this.frame, "the chosen file is no file, but exists already. delete? (" + file + ')', "already exists",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					return null;
				}
			} else {
				int c = JOptionPane.showConfirmDialog(this.frame, "the file already exists, overwrite? (" + file + ')', "file already exists",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (c != JOptionPane.OK_OPTION) return null;
			}
		} else if (!file.exists()) {
			JOptionPane.showMessageDialog(this.frame, "the chosen file does not exists (" + file + ')', "file not found", JOptionPane.ERROR_MESSAGE);
			return null;
		} else if (!file.isFile()) {
			JOptionPane.showMessageDialog(this.frame, "the chosen file is no file (" + file + ')', "no file", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return file;
	}
	
	private void doLoad() {
		try {
			if (!(this.world instanceof CompleteWorld cw)) {
				int c = JOptionPane.showConfirmDialog(this.frame, "the save may not contain all tiles, save anyway?", "no complete world",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (c != JOptionPane.OK_OPTION) {
					return;
				}
			} else {
				int c = JOptionPane.showConfirmDialog(this.frame, "should the save also contain the users and everything else?", "save everything?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				switch (c) {
				case JOptionPane.YES_OPTION:
					File file = new File("./complete-saves/");
					file = pickFile(file, false);
					try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), this.world.user(), this.world)) {
						cw.saveEverything(conn);
					}
					return;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION:
					return;
				default:
					System.err.println("unknown return value from JOptPane: " + c);
					return;
				}
			}
			File file = pickFile(new File("./saves/"), false);
			try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), this.world.user(), this.world)) {
				OpenWorld.saveWorld(this.world, conn);
			}
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(this.frame, ioe.toString(), "io error on save", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void doSave() {
		try {
			int c = JOptionPane.showConfirmDialog(this.frame, "does the save also contain the users and everything else?", "load everything?",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (c) {
			case JOptionPane.YES_OPTION:
				break;
			case JOptionPane.NO_OPTION:
				File file = pickFile(new File("./complete-saves/"), true);
				if (file == null) return;
				CompleteWorld cw;
				User usr = this.world.user();
				boolean wasRoot = usr.isRoot();
				User root = usr;
				if (!wasRoot) {
					root = usr.rootClone();
				}
				try {
					try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), usr, null)) {
						cw = CompleteWorld.loadEverything(conn);
					}
				} catch (Throwable t) {
					if (!wasRoot) {
						root.close();
					}
					throw t;
				}
				this.world = cw;
				usr.close();
				return;
			case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION:
				return;
			default:
				System.err.println("unknown return value from JOptPane: " + c);
				return;
			}
			File file = pickFile(new File("./saves/"), true);
			if (file == null) return;
			Tile[][] ts;
			User     usr     = this.world.user();
			boolean  wasRoot = usr.isRoot();
			User     root    = usr;
			if (!wasRoot) {
				root = usr.rootClone();
			}
			try {
				try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), this.world.user(), this.world)) {
					ts = OpenWorld.loadWorld(null, conn);
				}
			} catch (Throwable t) {
				if (!wasRoot) {
					root.close();
				}
				throw t;
			}
			usr.close();
			this.world = CompleteWorld.Builder.create(root, ts);
			JOptionPane.showMessageDialog(this.frame, "loaded world (world is now in build mode)", "finish load", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(this.frame, ioe.toString(), "io error on save", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private Menu menuServer() {
		Menu m = new Menu("Server");
		if (this.world instanceof CompleteWorld || this.serverThread != null) {
			MenuItem start = new MenuItem(this.serverThread == null ? "start" : "stop");
			m.add(start);
			start.addActionListener(e -> {
				Thread st = this.serverThread;
				if (st != null) {
					Map<User, Connection> cs = this.connects;
					st.interrupt();
					try {
						st.join(1000L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (cs != null) {
						for (Connection c : cs.values()) {
							try {
								c.logOut();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (st.isAlive()) {
						JOptionPane.showMessageDialog(this.frame, "I told the server to stop", "server still running", JOptionPane.WARNING_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this.frame, "the server stoped", "server shut down", JOptionPane.INFORMATION_MESSAGE);
					}
					start.setName("start");
					return;
				}
				doStartServer(start);
			});
		}
		if (this.world instanceof CompleteWorld) {
			MenuItem addUsr = new MenuItem("add User");
			m.add(addUsr);
		}
		return m;
	}
	
	private void doStartServer(MenuItem mi) {
		JDialog dialog = new JDialog(this.frame);
		dialog.setTitle("Open Server");
		
		JPanel dp = new JPanel();
		dialog.setContentPane(dp);
		dp.setLayout(new GridLayout(3, 2));
		dp.add(new JLabel("Port:"));
		NumberDocument portDoc = new NumberDocument(0x0000, 0xFFFF);
		JTextField     portTxt = new JTextField(5);
		portTxt.setDocument(portDoc);
		portTxt.setText(Integer.toString(Connection.DEFAULT_PORT));
		dp.add(portTxt);
		
		JCheckBox serverPWCB = new JCheckBox("Server Password");
		serverPWCB.setToolTipText(//
				/*		*/"<html>a server password lets remote users create accounts themself<br>" //
						+ "It is then no longer needed to add all users manually,<br>"//
						+ "but everyone with the server password can create an infinit amount of users</html>"//
		);
		dp.add(serverPWCB);
		JPasswordField serverPWPF = new JPasswordField(16);
		dp.add(serverPWPF);
		serverPWPF.setVisible(false);
		serverPWCB.addActionListener(oe -> serverPWPF.setVisible(serverPWCB.isSelected()));
		
		dp.add(new JLabel());
		JButton start = new JButton("start");
		dp.add(start);
		
		start.addActionListener(oe -> {
			try {
				int          port = portDoc.getNumber();
				ServerSocket ss   = new ServerSocket(port);
				synchronized (WorldDisplay.this) {
					final Map<User, Connection> cs = new HashMap<>();
					this.connects = cs;
					
					this.serverThread = Settings.threadStart(() -> {
						try {
							if (!(this.world instanceof CompleteWorld cw)) {
								synchronized (this) {
									if (this.serverThread == Thread.currentThread()) {
										this.serverThread = null;
										this.connects     = null;
									}
								}
								return;
							}
							Connection.ServerAccept.accept(ss, cw, (conn, sok) -> Settings.threadStart(() -> {
								String name = conn.usr.name();
								if (sok == null) {
									JOptionPane.showMessageDialog(this.frame, "'" + name + "' disconnected", "remote log out", JOptionPane.INFORMATION_MESSAGE);
								} else {
									InetAddress addr = sok.getInetAddress();
									JOptionPane.showMessageDialog(this.frame, "'" + name + "' logged in from " + addr, "remote log in",
											JOptionPane.INFORMATION_MESSAGE);
								}
							}), cs, serverPWCB.isSelected() ? serverPWPF.getPassword() : null);
						} catch (IOException err) {
							if (err instanceof ClosedByInterruptException || Thread.interrupted()) { return; }
							JOptionPane.showMessageDialog(this.frame, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
						} finally {
							synchronized (this) {
								if (this.serverThread == Thread.currentThread()) {
									this.serverThread = null;
									this.connects     = null;
								}
							}
							for (Connection conn : cs.values()) {
								try {
									conn.logOut();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					});
				}
				mi.setName("stop");
				JOptionPane.showMessageDialog(this.frame, "server started on port " + ss.getLocalPort(), "Server Started", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(dialog, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			dialog.dispose();
		});
		PageDisplay.initDialog(dialog, this.frame);
	}
	
	private Menu menuBuild() {
		Menu m = new Menu("Build");
		if (this.world instanceof CompleteWorld.Builder) {
			MenuItem fillRandom = new MenuItem("fill with random tiles");
			m.add(fillRandom);
			fillRandom.addActionListener(e -> {
				if (!(this.world instanceof CompleteWorld.Builder b)) return;
				b.fillRandom();
				updateBtns();
			});
			MenuItem buildWorld = new MenuItem("build world");
			m.add(buildWorld);
			buildWorld.addActionListener(e -> {
				if (!(this.world instanceof CompleteWorld.Builder b)) return;
				try {
					this.world = b.create();
					rebuildFrame(false);
					JOptionPane.showMessageDialog(this.frame, "finished building", "finish convert", JOptionPane.INFORMATION_MESSAGE);
				} catch (IllegalStateException ise) {
					JOptionPane.showMessageDialog(this.frame, ise.toString(), "error on build", JOptionPane.ERROR_MESSAGE);
				}
			});
		} else {
			MenuItem toBuild = new MenuItem("convert to build world");
			m.add(toBuild);
			toBuild.addActionListener(e -> {
				if (this.world instanceof CompleteWorld.Builder b) {
					rebuildFrame(false);
					JOptionPane.showMessageDialog(this.frame, "the world is already a build world", "thats stange", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				User    usr     = this.world.user();
				boolean wasRoot = usr.isRoot();
				User    root;
				if (wasRoot) {
					root = usr;
				} else {
					root = usr.rootClone();
				}
				CompleteWorld.Builder b;
				try {
					b = new CompleteWorld.Builder(root, this.world.xlen(), this.world.ylen());
					for (int cx = 0, xlen = this.world.xlen(); cx < xlen; cx++) {
						for (int cy = 0, ylen = this.world.ylen(); cy < ylen; cy++) {
							Tile t = this.world.tile(cx, cy);
							b.setTile(cx, cy, t);
						}
					}
				} catch (Throwable t) {
					if (!wasRoot) {
						root.close();
					}
					throw t;
				}
				this.world = b;
				if (!wasRoot) {
					usr.close();
				}
				rebuildFrame(false);
				JOptionPane.showMessageDialog(this.frame, "finished converting to build", "finish convert", JOptionPane.INFORMATION_MESSAGE);
			});
		}
		return m;
	}
	
	private final BiConsumer<GroupTree, Menu> addHelps = addHelps();
	
	private BiConsumer<GroupTree, Menu> addHelps() {
		return (gt, mi) -> {
			Menu inner = new Menu(gt.name());
			gt.forEach(a -> {
				if (a == TheBaseAddonProvider.BASE_ADDON || !a.hasHelp()) return;
				MenuItem i = new MenuItem(a.localName);
				inner.add(i);
				mi.addActionListener(e -> this.pd.display(a.help()));
			}, this.addHelps, inner);
			mi.add(inner);
		};
	}
	
	private Menu menuHelp(GroupTree gt) {
		Menu         m            = new Menu("Help");
		MenuShortcut helpShortCut = new MenuShortcut(KeyEvent.VK_F1);
		MenuItem     help         = new MenuItem("HELP ME!", helpShortCut);
		m.add(help);
		help.addActionListener(e -> this.pd.display(TheBaseAddonProvider.BASE_ADDON.help(), this.frame));
		gt.forEach(a -> {
			if (a == TheBaseAddonProvider.BASE_ADDON || !a.hasHelp()) return;
			MenuItem mi = new MenuItem(a.localName);
			m.add(mi);
			mi.addActionListener(e -> this.pd.display(a.help()));
		}, this.addHelps, m);
		return m;
	}
	
	private void pack(boolean setVisible) {
		if (setVisible) {
			this.frame.setVisible(true);
		}
		Dimension psize = this.grid.getPreferredSize();
		Insets    i     = this.frame.getInsets();
		psize.height += i.left + i.right;
		psize.width  += i.bottom + i.top;
		GraphicsConfiguration gc = this.frame.getGraphicsConfiguration();
		Rectangle             b  = gc.getBounds();
		i         = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		b.x      += i.left;
		b.y      += i.top;
		b.width  -= i.left + i.right;
		b.height -= i.bottom + i.top;
		int fx  = this.frame.getX();
		int fy  = this.frame.getY();
		int rfx = fx - b.x;
		int rfy = fy - b.y;
		int w   = Math.min(psize.width, b.width - rfx);
		int h   = Math.min(psize.height, b.height - rfy);
		this.frame.setSize(w, h);
	}
	
	private void updateBtns() {
		int           bsize = this.grid.getButtonSize();
		BufferedImage img   = new BufferedImage(bsize, bsize, BufferedImage.TYPE_INT_RGB);
		Graphics2D    g     = img.createGraphics();
		for (int cx = 0, xlen = this.world.xlen(); cx < xlen; cx++) {
			for (int cy = 0, ylen = this.world.ylen(); cy < ylen; cy++) {
				updateBtn(img, g, cx, cy, bsize);
			}
		}
		g.dispose();
		this.grid.repaint();
	}
	
	private void updateBtn(int x, int y) {
		int           bsize = this.grid.getButtonSize();
		BufferedImage img   = new BufferedImage(bsize, bsize, BufferedImage.TYPE_INT_RGB);
		Graphics2D    g     = img.createGraphics();
		updateBtn(img, g, x, y, bsize);
		g.dispose();
		this.grid.repaint(x * bsize, y * bsize, bsize, bsize);
	}
	
	private void updateBtn(BufferedImage img, Graphics2D g, int x, int y, int bsize) {
		if (this.world instanceof CompleteWorld.Builder b) {
			if (b.get(x, y) == null) {
				this.grid.paintBtn(x, y, GroundType.NOT_EXPLORED_GRND.image(bsize, bsize));
				return;
			}
		}
		Tile t = this.world.tile(x, y);
		g.drawImage(t.ground().image(bsize, bsize), 0, 0, bsize, bsize, null);
		Build b = t.build();
		if (b != null) {
			g.drawImage(b.image(bsize, bsize), 0, 0, bsize, bsize, null);
		}
		t.resourcesStream().forEach(addImageConsumer(bsize, g, t, t.resourceCount()));
		t.unitsStream().forEach(addImageConsumer(bsize, g, t, t.unitCount()));
		this.grid.paintBtn(x, y, img);
	}
	
	private static <T extends WorldThing<?, ?>> Consumer<T> addImageConsumer(final int bsize, final Graphics2D g, final Tile t, int thingCount) {
		final boolean manyRes = t.resourceCount() > 1;
		final int     size    = size(bsize, thingCount);
		return new Consumer<>() {
			
			int x;
			int y;
			
			@Override
			public void accept(T wt) {
				if (!manyRes) {
					g.drawImage(wt.image(bsize, bsize), 0, 0, bsize, bsize, null);
				} else {
					g.drawImage(wt.image(bsize, bsize), this.x, this.y, bsize, bsize, null);
					this.x += size;
					if (this.x >= bsize) {
						this.x  = 0;
						this.y += size;
					}
				}
			}
			
		};
	}
	
	private static int size(final int bsize, int thingCount) {
		if (thingCount <= 1) return bsize;
		int cnt = 2;
		while (cnt * cnt < thingCount) cnt++;
		return bsize / cnt;
	}
	
	/**
	 * main
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			CompleteWorld.Builder w = new CompleteWorld.Builder(User.nopw("nice user"), 16, 16);
			WorldDisplay          d = new WorldDisplay(w);
			d.init();
		});
	}
	
}
