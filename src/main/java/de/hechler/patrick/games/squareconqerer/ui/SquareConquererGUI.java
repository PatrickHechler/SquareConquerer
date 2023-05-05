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
package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadStart;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent.Cause;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import de.hechler.patrick.games.squareconqerer.Settings;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock.SeparatingBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.PageWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.Storage;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.turn.CarryTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.EntityTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.MoveTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.StoreTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

@SuppressWarnings("preview")
public class SquareConquererGUI {
	
	private final RootWorld     source;
	private int                 index;
	private Iterator<RootWorld> iter;
	private SquareConquererGUI  logView;
	
	private World world;
	
	private JFrame         frame;
	private JPanel         panel;
	private JMenuBar       menu;
	private JScrollPane    scrollPane;
	private JButton        hoveringRigthButton;
	private JButton        hoveringLeftButton;
	private TileChanger    myBuildMode;
	private JButton[][]    btns;
	private EntityTurn[][] turns;
	
	private volatile Map<User, Connection> connects;
	
	private final Runnable loadFinishedHook = () -> {
		if (this.world instanceof RootWorld) {
			JOptionPane.showMessageDialog(this.frame, "finishd loading", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (this.world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(this.frame, "finishd loading, the loaded world is in build mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (this.world instanceof UserWorld) {
			JOptionPane.showMessageDialog(this.frame, "finishd loading, the loaded world is in user mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (this.world instanceof RemoteWorld) {
			JOptionPane.showMessageDialog(this.frame, "finishd loading, the loaded world is in remote mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (this.world != null) {
			JOptionPane.showMessageDialog(this.frame, "finishd loading, the loaded world is of type: " + this.world.getClass().getSimpleName(), "load",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this.frame, "could not load for some reason", "load failed", JOptionPane.ERROR_MESSAGE);
		}
	};
	
	private final Runnable buildFinishHook = () -> {
		if (this.world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(this.frame, "the converting to a build mode world is now completed", "converted to build",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this.frame, "the world build operation is now completed", "finished build", JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Thread serverThread;
	
	public SquareConquererGUI(RootWorld source, Iterator<RootWorld> iter) {
		this.source = source;
		this.iter   = iter;
		this.world  = iter.next();
	}
	
	public SquareConquererGUI() {
		this.source = null;
	}
	
	public SquareConquererGUI(World world) {
		if (world == null) { throw new NullPointerException("world is null"); }
		this.source = null;
		this.world  = world;
	}
	
	public void load(Thread t) {
		if (this.frame != null) { throw new IllegalStateException("already loaded"); } // I don't need a second method for that
		if (ensureGUIThread(() -> load(t))) { return; }
		this.serverThread = t;
		load();
	}
	
	public void load() {
		if (this.frame != null) { throw new IllegalStateException("already loaded"); } // I don't need a second method for that
		if (ensureGUIThread(this::load)) { return; }
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				if (SquareConquererGUI.this.source != null) {
					int chosen = JOptionPane.showConfirmDialog(SquareConquererGUI.this.frame, "Close Square Conquerer History?", "Close", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (chosen == JOptionPane.YES_OPTION) {
						if (SquareConquererGUI.this.logView == SquareConquererGUI.this) {
							SquareConquererGUI.this.logView = null;
						}
						SquareConquererGUI.this.frame.dispose();
					}
				} else {
					int chosen = JOptionPane.showConfirmDialog(SquareConquererGUI.this.frame, "Exit Square Conquerer?", "Exit", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (chosen == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				}
			}
			
		});
		
		reload(null, false, true);
	}
	
	private void initMenu() {
		this.menu = new JMenuBar();
		this.menu.add(menuGeneral());
		if (this.world instanceof RootWorld) {
			this.menu.add(menuGameHistory());
			if (this.source == null) {
				this.menu.add(menuServer());
			}
		}
		this.menu.add(menuBuild());
		this.menu.add(menuHelp());
		this.menu.add(menuCredits());
		this.menu.add(menuLicense());
		this.frame.setJMenuBar(this.menu);
	}
	
	private JMenu menuLicense() {
		JMenu licenseMenu = new JMenu("License");
		addLicense(licenseMenu, SCAddon.theGame().license(), SCAddon.GAME_ADDON_NAME);
		for (SCAddon addon : SCAddon.onlyAddons()) {
			addLicense(licenseMenu, addon.license(), "Addon: " + addon.name);
		}
		return licenseMenu;
	}
	
	private void addLicense(JMenu licenseMenu, SCLicense license, String addonName) {
		JMenuItem item = new JMenuItem(addonName + ": " + license.name());
		item.addActionListener(e -> {
			JDialog dialog = createDialog();
			dialog.setTitle("License: " + license.name() + " : " + addonName);
			JTextArea textArea = new JTextArea(license.text());
			textArea.setEditable(false);
			JScrollPane scroll = new JScrollPane(textArea);
			dialog.setContentPane(scroll);
			initDialog(dialog, true);
		});
		licenseMenu.add(item);
	}
	
	private JMenu menuCredits() {
		JMenu creditsMenu = new JMenu("Credits");
		if (!SCAddon.onlyAddons().isEmpty()) {
			List<SCPageBlock> blocks = new ArrayList<>();
			blocks.addAll(SCAddon.theGame().credits().blocks());
			for (SCAddon addon : SCAddon.onlyAddons()) {
				blocks.add(new SCPageBlock.SeparatingBlock(true));
				blocks.addAll(addon.credits().blocks());
			}
			SCPage page = new SCPage(blocks);
			addWorldPage(creditsMenu, page, "All Credits (World)", "All Credits World");
			addPage(creditsMenu, page, "All Credits (Boring)", "Credits: ");
		}
		addWorldPage(creditsMenu, SCAddon.theGame().credits(), SCAddon.GAME_ADDON_NAME + " (World)", SCAddon.GAME_ADDON_NAME + " (Credits World)");
		addPage(creditsMenu, SCAddon.theGame().credits(), SCAddon.GAME_ADDON_NAME + " (Boring)", "Credits: ");
		for (SCAddon addon : SCAddon.onlyAddons()) {
			addWorldPage(creditsMenu, addon.credits(), "Addon: " + addon.name + " (World)", "Addon: " + addon.name + " (Credits World)");
			addPage(creditsMenu, addon.credits(), "Addon: " + addon.name + " (Boring)", "Credits: ");
		}
		return creditsMenu;
	}
	
	private JMenu menuHelp() {
		JMenu helpMenu = new JMenu("Help");
		if (!SCAddon.onlyAddons().isEmpty()) {
			List<SCPageBlock> blocks = new ArrayList<>();
			blocks.addAll(SCAddon.theGame().help().blocks());
			for (SCAddon addon : SCAddon.onlyAddons()) {
				blocks.add(new SCPageBlock.SeparatingBlock(true));
				blocks.addAll(addon.help().blocks());
			}
			SCPage page = new SCPage(blocks);
			addWorldPage(helpMenu, page, "All Helps (World)", "All Helps World");
			addPage(helpMenu, page, "All Help Pages", "Help: ");
		}
		addWorldPage(helpMenu, SCAddon.theGame().help(), SCAddon.GAME_ADDON_NAME + " (World)", SCAddon.GAME_ADDON_NAME + " (Help World)");
		addPage(helpMenu, SCAddon.theGame().help(), SCAddon.GAME_ADDON_NAME, "Help: ");
		for (SCAddon addon : SCAddon.onlyAddons()) {
			addWorldPage(helpMenu, addon.help(), "Addon: " + addon.name + " (World)", "Addon: " + addon.name + " (Help World)");
			addPage(helpMenu, addon.help(), "Addon: " + addon.name, "Help: ");
		}
		return helpMenu;
	}
	
	private void addWorldPage(JMenu menu, SCPage page, String addonName, String title) {
		JMenuItem item = new JMenuItem(addonName);
		item.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(this.frame, "load " + title + "? (your current world will be discarded)", "load page world",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) return;
			PageWorld pw = new PageWorld(page);
			if (this.world instanceof RemoteWorld rw) {
				try {
					rw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this.frame, "error wile closing remote world: " + e1.toString() + " (I will continue anyway)",
							"error on close (ignored)", JOptionPane.ERROR_MESSAGE);
				}
			}
			Thread st = this.serverThread;
			while (st != null) {
				st.interrupt();
				try {
					st.join(10L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				st = this.serverThread;
			}
			this.world = pw.createWorld().create();
			reload(this.loadFinishedHook, true, true);
		});
		menu.add(item);
	}
	
	private void addPage(JMenu menu, SCPage page, String addonName, String titlePrefix) {
		JMenuItem item = new JMenuItem(addonName);
		item.addActionListener(e -> showPage(new JDialog(this.frame), page, addonName, titlePrefix));
		menu.add(item);
	}
	
	private void showPage(JDialog dialog, SCPage page, String addonName, String titlePrefix) {
		dialog = createDialog(dialog);
		dialog.setTitle(titlePrefix == null ? addonName : titlePrefix + addonName);
		JPanel dp = new JPanel();
		dp.setLayout(null);
		dialog.setContentPane(new JScrollPane(dp));
		int yoff = 0;
		int maxx = 0;
		for (SCPageBlock block : page.blocks()) {
			switch (block) {
			case SCPageBlock.SeparatingBlock sb -> yoff += pageAddSepBlock(dp, yoff, sb);
			case SCPageBlock.EntryBlock eb -> {
				int xoff = 0;
				int yadd = 0;
				for (SCPageEntry entry : eb.entries()) {
					switch (entry) {
					case SCPageEntry.TextEntry te -> {
						Dimension pref = pageAddTextEntry(dp, yoff, xoff, te);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case SCPageEntry.LinkEntry le -> {
						Dimension pref = pageAddLinkEntry(dialog, dp, yoff, xoff, le);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case SCPageEntry.PageEntry pe -> {
						Dimension pref = pageAddPageEntry(dialog, dp, yoff, xoff, pe);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					case SCPageEntry.WorldEntry we -> {
						Dimension pref = pageAddWorldEntry(dialog, dp, yoff, xoff, we);
						xoff += pref.width;
						yadd  = Math.max(yadd, pref.height);
					}
					}
				}
				yoff += yadd;
				maxx  = Math.max(maxx, xoff);
			}
			case SCPageBlock.TextBlock tb -> {
				Dimension pref = pageAddTextBlock(dp, yoff, tb);
				yoff += pref.height;
				maxx  = Math.max(maxx, pref.width);
			}
			}
		}
		dp.setPreferredSize(new Dimension(maxx, yoff));
		initDialog(dialog, true);
	}
	
	private static int pageAddSepBlock(JPanel dp, int yoff, SeparatingBlock sb) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setForeground(Color.BLACK);
		panel.setLocation(0, yoff);
		panel.setSize(1, sb.bold() ? 4 : 3);
		panel.setOpaque(true);
		dp.add(panel);
		dp.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				panel.setSize(dp.getWidth(), sb.bold() ? 4 : 3);
			}
			
		});
		return sb.bold() ? 4 : 3;
	}
	
	private Dimension pageAddWorldEntry(JDialog dialog, JPanel dp, int yoff, int xoff, SCPageEntry.WorldEntry we) {
		JTextArea text = new JTextArea(we.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(new Color(191, 150, 0));
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("World: " + we.worldName());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				int chosen = JOptionPane.showConfirmDialog(dialog,
						"load world " + we.worldName() + '?' + (SquareConquererGUI.this.world == null ? "" : " current world will be discarded!"),
						"load " + we.worldName() + '?', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) return;
				World w = we.world().get();
				if (w == null) {
					JOptionPane.showMessageDialog(dialog, "error: world is null", "there is no world", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (SquareConquererGUI.this.world instanceof RemoteWorld rw) {
					try {
						rw.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(dialog, "error: while closing remote world: " + e1.toString() + " (I will continue anyway)",
								e1.getClass().toString(), JOptionPane.ERROR_MESSAGE);
					}
				}
				Thread st = SquareConquererGUI.this.serverThread;
				while (st != null) {
					st.interrupt();
					Map<User, Connection> cs = SquareConquererGUI.this.connects;
					try {
						st.join(10L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (cs != null) {
						for (Connection conn : cs.values()) {
							try {
								conn.logOut();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					st = SquareConquererGUI.this.serverThread;
				}
				SquareConquererGUI.this.world = w;
				reload(SquareConquererGUI.this.loadFinishedHook, true, true);
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private Dimension pageAddPageEntry(JDialog dialog, JPanel dp, int yoff, int xoff, SCPageEntry.PageEntry pe) {
		JTextArea text = new JTextArea(pe.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(new Color(225, 64, 64));
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("Page: " + pe.title());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				showPage(new JDialog(dialog), pe.page().get(), pe.title(), null);
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private static Dimension pageAddLinkEntry(JDialog dialog, JPanel dp, int yoff, int xoff, SCPageEntry.LinkEntry le) {
		JTextArea text = new JTextArea(le.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		text.setForeground(Color.BLUE);
		text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		text.setToolTipText("Link: " + le.link());
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URI uri    = le.link();
					int chosen = JOptionPane.showConfirmDialog(dialog, "open link: " + uri, "open link", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (chosen != JOptionPane.YES_OPTION) return;
					Desktop.getDesktop().browse(uri);
				} catch (IOException | RuntimeException e1) {
					JOptionPane.showMessageDialog(dialog, "error while opening link '" + le.link() + "': " + e1.toString(), "could not open the link",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private static Dimension pageAddTextEntry(JPanel dp, int yoff, int xoff, SCPageEntry.TextEntry te) {
		JTextArea text = new JTextArea(te.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		Dimension pref = text.getPreferredSize();
		text.setBounds(xoff, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private static Dimension pageAddTextBlock(JPanel dp, int yoff, SCPageBlock.TextBlock tb) {
		JTextArea text = new JTextArea(tb.text());
		text.setTabSize(1);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder());
		text.setFont(UIManager.getDefaults().getFont("Label.font"));
		text.getCaret().deinstall(text);
		text.setBackground(dp.getBackground());
		Dimension pref = text.getPreferredSize();
		text.setBounds(0, yoff, pref.width, pref.height);
		dp.add(text);
		return pref;
	}
	
	private JMenu menuBuild() {
		JMenu buildMenu = new JMenu("Build");
		if (this.world instanceof RootWorld.Builder) {
			addFillRandomMenuItems(buildMenu);
		} else {
			addConvertToBuildMenuItem(buildMenu);
		}
		return buildMenu;
	}
	
	private void addConvertToBuildMenuItem(JMenu buildMenu) {
		JMenuItem toBuild = new JMenuItem("to build world");
		toBuild.setToolTipText("convert this world to a build world");
		toBuild.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(this.frame,
					"convert to a build world?" + (this.world instanceof RemoteWorld || this.serverThread != null ? " (this will close the server connection)" : ""),
					"to build world", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) { return; }
			Thread st = this.serverThread;
			if (st != null) {
				st.interrupt();
			}
			User              oldUsr = this.world.user();
			RootUser          root   = oldUsr.rootClone();
			RootWorld.Builder b      = new RootWorld.Builder(root, this.world.xlen(), this.world.ylen());
			for (int x = 0; x < b.xlen(); x++) {
				for (int y = 0; y < b.ylen(); y++) {
					b.set(x, y, this.world.tile(x, y));
				}
			}
			if (this.world instanceof RemoteWorld rw) {
				try {
					rw.close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this.frame, "error while closing remote world (do not retry, I will proceed anyway): " + e1.toString(),
							"error: " + e.getClass(), JOptionPane.ERROR_MESSAGE);
				}
			}
			oldUsr.close();
			this.world = b;
			b.addNextTurnListener((wh, th) -> update(null));
			reload(this.buildFinishHook, true, true);
		});
		buildMenu.add(toBuild);
	}
	
	private void addFillRandomMenuItems(JMenu buildMenu) {
		JMenuItem fillRandom = new JMenuItem("fill with random tiles");
		fillRandom.setToolTipText("<html>replace all tiles with type not-explored with random tiles<br>"
				+ "note that then also not-explored tiles with a resource set may get their resource replaced<br>"
				+ "the world builder may use rules, which change the possibility for some tiles (such as ocean tiles can only be placed near other water tiles)</html>");
		fillRandom.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(this.frame, "fill all not-exlpored tiles", "fill random", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) { return; }
			((RootWorld.Builder) this.world).fillRandom();
			threadStart(() -> update(null));
			JOptionPane.showMessageDialog(this.frame, "filled with random tiles world", "filled world", JOptionPane.INFORMATION_MESSAGE);
		});
		buildMenu.add(fillRandom);
		JMenuItem fillTotallyRandom = new JMenuItem("fill with totally random tiles");
		fillTotallyRandom.setToolTipText("<html>replace all tiles with type not-explored with random tiles<br>"
				+ "note that then also not-explred tiles with a resource set may get their resource replaced</html>");
		fillTotallyRandom.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(this.frame, "fill all not-exlpored tiles", "fill random", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) { return; }
			((RootWorld.Builder) this.world).fillTotallyRandom();
			JOptionPane.showMessageDialog(this.frame, "filled with random tiles world", "filled world", JOptionPane.INFORMATION_MESSAGE);
		});
		buildMenu.add(fillTotallyRandom);
	}
	
	private JMenu menuGameHistory() {
		JMenu gameHistoryMenu = new JMenu("Game History");
		if (this.source != null) {
			JMenuItem nextItem = new JMenuItem("Next Turn");
			JMenuItem prevItem = new JMenuItem("Prev Turn");
			JMenuItem gotoItem = new JMenuItem("Goto Turn");
			
			nextItem.addActionListener(e -> gotoTurn(this.index + 1));
			prevItem.addActionListener(e -> gotoTurn(this.index - 1));
			gotoItem.addActionListener(e -> {
				JDialog dialog = createDialog();
				JPanel  dp     = new JPanel(new GridLayout(1, 2));
				dialog.setContentPane(dp);
				dp.add(new JLabel("Enter Turn: "));
				NumberDocument doc = new NumberDocument(0, this.source.iterCount() - 1);
				dp.add(new JTextField(doc, Integer.toString(this.index), 0));
				dp.add(new JLabel());
				JButton fin = new JButton("goto");
				dp.add(fin);
				fin.addActionListener(e1 -> {
					gotoTurn(doc.getNumber());
					dialog.dispose();
				});
				initDialog(dialog, false);
			});
			
			gameHistoryMenu.add(nextItem);
			gameHistoryMenu.add(prevItem);
			gameHistoryMenu.add(gotoItem);
		} else {
			JMenuItem showItem = new JMenuItem("Show");
			
			showItem.addActionListener(menuGameHistoryShowListener());
			
			gameHistoryMenu.add(showItem);
			
			addSendUsers(gameHistoryMenu);
		}
		return gameHistoryMenu;
	}
	
	private void gotoTurn(int turn) {
		if (turn < 0) {
			JOptionPane.showMessageDialog(this.frame, "there is no negative turn", "invalid turn", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (turn >= this.source.iterCount()) {
			JOptionPane.showMessageDialog(this.frame, "there is no turn " + turn + " (yet)", "invalid turn", JOptionPane.ERROR_MESSAGE);
			return;
		} // the turns can only be done forward
		if (turn < this.index) {
			this.iter  = this.source.iterator();
			this.index = 0;
			this.world = this.iter.next();
		}
		while (turn > this.index) {
			this.world = this.iter.next();
			this.index++;
		}
		update(null);
	}
	
	private ActionListener menuGameHistoryShowListener() {
		return e -> {
			if (this.logView != null) {
				this.logView.frame.toFront();
				this.logView.frame.requestFocus(Cause.TRAVERSAL);
				return;
			}
			int c = JOptionPane.showConfirmDialog(this.frame, "show the game history?", "show history", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (c != JOptionPane.YES_OPTION) return;
			this.logView         = new SquareConquererGUI((RootWorld) this.world, ((RootWorld) this.world).iterator());
			this.logView.logView = this;
			this.logView.load();
			this.logView.frame.toFront();
			this.logView.frame.requestFocusInWindow(Cause.ACTIVATION);
		};
	}
	
	private JMenu menuServer() {
		JMenu     serverMenu   = new JMenu("Server");
		JMenuItem openItem     = new JMenuItem("Open Server");
		JMenuItem closeItem    = new JMenuItem("Close Running Server");
		JMenuItem addItem      = new JMenuItem("Add Player");
		JMenuItem delItem      = new JMenuItem("Delete Player");
		JMenuItem pwChangeItem = new JMenuItem("Change Password");
		
		
		openItem.addActionListener(menuServerOpenListener(serverMenu, openItem, closeItem));
		closeItem.addActionListener(menuServerCloseListener(serverMenu, openItem, closeItem));
		addItem.addActionListener(menuServerAddListener());
		delItem.addActionListener(menuServerDeleteListener());
		pwChangeItem.addActionListener(menuServerPWChangeListener());
		
		serverMenu.add(openItem, 0);
		serverMenu.add(addItem);
		serverMenu.add(delItem);
		serverMenu.add(pwChangeItem);
		addSendUsers(serverMenu);
		return serverMenu;
	}
	
	private void addSendUsers(JMenu addMenu) {
		JMenuItem sendUsersItem = new JMenuItem("Send Users");
		JMenuItem sendUserItem  = new JMenuItem("Send Single User");
		
		sendUserItem.addActionListener(e -> {
			JDialog dialog = createDialog();
			JPanel  dp     = new JPanel();
			dialog.setContentPane(dp);
			dp.setLayout(new GridLayout(1, 2));
			dp.add(new JLabel("Select User: "));
			Object[] usrs = (this.logView == null ? this : this.logView).connects.keySet().toArray();
			Arrays.sort(usrs);
			String[] names = new String[usrs.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = ((User) usrs[i]).name();
			}
			JComboBox<String> combo = new JComboBox<>(names);
			dp.add(combo);
			combo.addActionListener(e1 -> {
				Connection conn = (this.logView == null ? this : this.logView).connects.get(usrs[combo.getSelectedIndex()]);
				if (conn == null) {
					JOptionPane.showMessageDialog(combo, "the user seeems to be disconnected", "no connection", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int c = JOptionPane.showConfirmDialog(combo, "send '" + conn.usr.name() + "' the game history?", "validate?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (c != JOptionPane.YES_OPTION) return;
				try {
					conn.blocked(() -> ((RootWorld) this.world).validateGame(conn));
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(combo, "the validation failed: " + e2.toString(), "error: could not validate", JOptionPane.ERROR_MESSAGE);
				}
			});
			initDialog(dialog, false);
		});
		
		sendUsersItem.addActionListener(e -> {
			int c = JOptionPane.showConfirmDialog(this.frame, "send all connected users the game history?", "validate?", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (c != JOptionPane.YES_OPTION) return;
			for (Connection conn : this.connects.values()) {
				try {
					conn.blocked(() -> ((RootWorld) this.world).validateGame(conn));
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(this.frame, "the validation failed for the user (" + conn.usr.name() + "): " + e2.toString(),
							"error: could not validate", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		addMenu.add(sendUsersItem);
		addMenu.add(sendUserItem);
		
	}
	
	private ActionListener menuServerPWChangeListener() {
		return e -> {
			JDialog dialog = createDialog();
			dialog.setTitle("Open Server");
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			JPanel dp = new JPanel();
			dialog.setContentPane(dp);
			dp.setLayout(new GridLayout(3, 2));
			
			dp.add(new JLabel("User:"));
			JComboBox<String> combo = new JComboBox<>();
			combo.setEditable(false);
			dp.add(combo);
			
			dp.add(new JLabel("New Password:"));
			JPasswordField pw = new JPasswordField(16);
			dp.add(pw);
			
			dp.add(new JLabel());
			JButton change = new JButton("change password");
			dp.add(change);
			
			combo.addItem(RootUser.ROOT_NAME);
			RootUser root = ((RootWorld) this.world).user();
			root.users().keySet().forEach(combo::addItem);
			
			change.addActionListener(oe -> {
				String name   = combo.getSelectedItem().toString();
				int    chosen = JOptionPane.showConfirmDialog(dialog, "change the password of '" + name + "'?", "change password", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) { return; }
				root.changePW(root.get(name), pw.getPassword());
				dialog.dispose();
			});
			
			initDialog(dialog, false);
		};
	}
	
	private JDialog createDialog() {
		return createDialog(new JDialog(this.frame));
	}
	
	private static JDialog createDialog(JDialog dialog) {
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JRootPane rootPane = dialog.getRootPane();
		KeyStroke stroke   = KeyStroke.getKeyStroke("ESCAPE");
		InputMap  inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(stroke, "ESCAPE");
		inputMap = rootPane.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
			
			private static final long serialVersionUID = -5661612443847132035L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
			
		});
		return dialog;
	}
	
	private void initDialog(JDialog dialog, boolean scrollable) {
		dialog.setLocationRelativeTo(this.frame);
		if (scrollable) {
			JScrollPane scroll = (JScrollPane) dialog.getContentPane();
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			dialog.pack();
			Rectangle bounds = dialog.getGraphicsConfiguration().getBounds();
			int       w      = dialog.getWidth();
			int       h      = dialog.getHeight();
			boolean   set    = false;
			if (bounds.width < dialog.getWidth()) {
				w    = bounds.width;
				set  = true;
				h   += scroll.getHorizontalScrollBar().getHeight();
			}
			if (bounds.height < dialog.getHeight()) {
				h   = bounds.height;
				set = true;
				w   = Math.min(w + scroll.getVerticalScrollBar().getWidth(), bounds.width);
			}
			if (set) {
				dialog.setSize(w, h);
			}
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		} else dialog.pack();
		dialog.setLocationRelativeTo(this.frame);
		dialog.setVisible(true);
	}
	
	private ActionListener menuServerDeleteListener() {
		return e -> {
			JDialog dialog = createDialog();
			dialog.setTitle("Open Server");
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			JPanel dp = new JPanel();
			dialog.setContentPane(dp);
			dp.setLayout(new GridLayout(3, 2));
			
			JComboBox<String> combo = new JComboBox<>();
			combo.setEditable(false);
			JButton delBtn = new JButton("DELTE USER");
			
			RootUser root = ((RootWorld) this.world).user();
			root.users().keySet().forEach(combo::addItem);
			
			delBtn.addActionListener(oe -> {
				String name   = combo.getSelectedItem().toString();
				int    chosen = JOptionPane.showConfirmDialog(dialog, "DELETE '" + name + "'?", "DELETE USER", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) { return; }
				
				threadStart(() -> {
					root.remove(root.get(name));
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.frame, "the user '" + name + "' was successfully deleted", "user DELETED",
							JOptionPane.INFORMATION_MESSAGE));
				});
			});
			
			dp.add(new JLabel("User:"));
			dp.add(combo);
			dp.add(new JLabel());
			dp.add(new JLabel());
			dp.add(new JLabel());
			dp.add(delBtn);
			
			initDialog(dialog, false);
		};
	}
	
	private ActionListener menuServerAddListener() {
		return e -> {
			JDialog dialog = createDialog();
			dialog.setTitle("Open Server");
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			JPanel dp = new JPanel();
			dialog.setContentPane(dp);
			dp.setLayout(new GridLayout(3, 2));
			
			dp.add(new JLabel("Username:"));
			JTextField userField = new JTextField();
			userField.setDocument(new PlainDocument() {
				
				/** UUID */
				private static final long serialVersionUID = -6551458638919976402L;
				
				@Override
				public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
					if ((getLength() + str.length()) >= 127) {
						int len = getText(0, getLength()).getBytes(StandardCharsets.UTF_8).length;
						len += str.getBytes(StandardCharsets.UTF_8).length;
						if (len > 0xFF) { return; }
					}
					super.insertString(offs, str, a);
				}
				
			});
			dp.add(userField);
			dp.add(new JLabel("Password:"));
			JPasswordField pw = new JPasswordField(16);
			dp.add(pw);
			dp.add(new JLabel());
			JButton finishBtn = new JButton("add User");
			dp.add(finishBtn);
			
			finishBtn.addActionListener(oe -> {
				try {
					RootUser root = ((RootWorld) this.world).user();
					root.add(userField.getText(), pw.getPassword());
				} catch (Exception err) {
					JOptionPane.showMessageDialog(dialog, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
					return;
				}
				dialog.dispose();
			});
			
			initDialog(dialog, false);
		};
	}
	
	private ActionListener menuServerCloseListener(JMenu serverMenu, JMenuItem openItem, JMenuItem closeItem) {
		return e -> {
			Thread st = this.serverThread;
			this.serverThread = null;
			st.interrupt();
			serverMenu.remove(closeItem);
			threadStart(() -> {
				try {
					st.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				SwingUtilities.invokeLater(() -> serverMenu.add(openItem, 0));
				JOptionPane.showMessageDialog(this.frame, "server stopped successfully", "Server Stopped", JOptionPane.INFORMATION_MESSAGE);
			});
		};
	}
	
	private ActionListener menuServerOpenListener(JMenu serverMenu, JMenuItem openItem, JMenuItem closeItem) {
		return e -> {
			JDialog dialog = createDialog();
			dialog.setTitle("Open Server");
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
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
			serverPWCB.setToolTipText(
					"<html>a server password lets remote users create accounts themself<br>" + "It is then no longer needed to add all users manually,<br>"
							+ "but everyone with the server password can create an infinit amount of users</html>");
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
					synchronized (SquareConquererGUI.this) {
						final Map<User, Connection> cs = new HashMap<>();
						this.connects = cs;
						
						this.serverThread = threadStart(() -> {
							try {
								RootWorld rw = (RootWorld) this.world;
								Connection.ServerAccept.accept(ss, rw, (conn, sok) -> threadStart(() -> {
									String name = conn.usr.name();
									if (sok == null) {
										JOptionPane.showMessageDialog(this.frame, "'" + name + "' disconnected", "remote log in", JOptionPane.INFORMATION_MESSAGE);
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
								synchronized (SquareConquererGUI.this) {
									if (this.connects == cs) {
										this.connects = null;
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
					serverMenu.remove(openItem);
					serverMenu.add(closeItem, 0);
					JOptionPane.showMessageDialog(this.frame, "server started on port " + ss.getLocalPort(), "Server Started", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception err) {
					JOptionPane.showMessageDialog(dialog, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
					return;
				}
				dialog.dispose();
			});
			
			initDialog(dialog, false);
		};
	}
	
	private JMenu menuGeneral() {
		JMenu        generalMenu = new JMenu("General");
		JFileChooser fc          = new JFileChooser(new File("."));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		initMenuGeneralSave(generalMenu, fc, false);
		initMenuGeneralSave(generalMenu, fc, true);
		initMenuGeneralLoad(generalMenu, fc, false);
		initMenuGeneralLoad(generalMenu, fc, true);
		initMenuGeneralExit(generalMenu);
		return generalMenu;
	}
	
	@SuppressWarnings("static-method")
	private void initMenuGeneralExit(JMenu generalMenu) {
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(exitItem, "Exit Square Conquerer?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		});
		generalMenu.add(exitItem);
	}
	
	private void initMenuGeneralLoad(JMenu generalMenu, JFileChooser fc, boolean loadEverything) {
		JMenuItem loadItem = new JMenuItem(loadEverything ? "Load Everything" : "Load");
		loadItem.addActionListener(e -> {
			int result = fc.showOpenDialog(this.frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (loadEverything) {
					threadStart(() -> loadEverythingFromFile(file));
				} else {
					threadStart(() -> loadFromFile(file));
				}
			}
		});
		generalMenu.add(loadItem);
	}
	
	private void loadEverythingFromFile(File file) {
		User     usr  = this.world.user();
		RootUser root = usr.rootClone();
		try {
			try (FileInputStream in = new FileInputStream(file); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, root)) {
				World w = RootWorld.loadEverything(conn);
				synchronized (SquareConquererGUI.this) {
					while (this.updateThread != null) {
						this.updateThread.interrupt();
						try {
							this.updateThread.join(10L);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					this.world = w;
				}
				SwingUtilities.invokeLater(() -> reload(this.loadFinishedHook, true, true));
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.frame, e.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Throwable t) {
			root.close();
			throw t;
		}
	}
	
	private void loadFromFile(File file) {
		User usr = this.world.user();
		try (FileInputStream in = new FileInputStream(file); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr)) {
			Tile[][] tiles;
			RootUser root = usr.rootClone();
			try {
				root.load(conn);
				tiles = RemoteWorld.loadWorld(conn, root.users());
			} catch (Throwable t) {
				root.close();
				throw t;
			}
			usr.close();
			synchronized (SquareConquererGUI.this) {
				while (this.updateThread != null) {
					this.updateThread.interrupt();
					try {
						this.updateThread.join(100L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				try {
					this.world = RootWorld.Builder.create(root, tiles);
				} catch (@SuppressWarnings("unused") IllegalStateException err) {
					this.world = RootWorld.Builder.createBuilder(root, tiles);
				}
			}
			SwingUtilities.invokeLater(() -> reload(this.loadFinishedHook, true, true));
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.frame, e.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initMenuGeneralSave(JMenu generalMenu, JFileChooser fc, boolean saveAll) {
		if (saveAll && !(this.world instanceof RootWorld)) return;
		JMenuItem saveItem = new JMenuItem(saveAll ? "Save Everything" : "Save");
		saveItem.addActionListener(e -> saveItemActionListener(fc, saveAll));
		generalMenu.add(saveItem);
	}
	
	private void saveItemActionListener(JFileChooser fc, boolean saveAll) {
		if (!(this.world instanceof RootWorld)) {
			if (saveAll) {
				JOptionPane.showMessageDialog(this.frame, "the save everything buton should not exist, only root worlds can save everything", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int choosen = JOptionPane.showConfirmDialog(this.frame,
					"the world is no root world, it may not contain the full information and thus there may be unexplred tiles", "save non root world",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choosen != JOptionPane.YES_OPTION) { return; }
		}
		int result = fc.showSaveDialog(this.frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				int chosen = JOptionPane.showConfirmDialog(this.frame, "overwrite '" + file + "'?", "overwrite file", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) { return; }
			}
			try {
				try (FileOutputStream out = new FileOutputStream(file); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, this.world.user());) {
					if (saveAll) {
						((RootWorld) this.world).saveEverything(conn);
					} else {
						User u = this.world.user();
						if (u instanceof RootUser root) {
							root.save(conn);
						} else {
							RootUser.nopw().save(conn);
						}
						OpenWorld.saveWorld(this.world, conn);
					}
					JOptionPane.showMessageDialog(this.frame, "finishd saving", "save", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this.frame, e1.toString(), e1.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void reload(Runnable ufh, boolean reload, boolean addNTL) {
		this.updateFinishHook = ufh;
		
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		if (reload) {
			this.frame.setEnabled(false);
		}
		
		this.frame.setTitle(this.world.getClass().getSimpleName() + ": " + this.world.user().name() + " (" + this.world.xlen() + '|' + this.world.ylen() + ')');
		
		initMenu();
		System.out.println("reload: menu initialized");
		int xlen     = this.world.xlen();
		int ylen     = this.world.ylen();
		int iconSize = Settings.iconSize();
		this.panel = new JPanel();
		this.panel.addMouseWheelListener(this::wheelScroll);
		this.panel.setLayout(new GridLayout(ylen, xlen, 0, 0));
		this.panel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		Dimension panelDim = new Dimension(xlen * iconSize, ylen * iconSize);
		this.panel.setPreferredSize(panelDim);
		this.scrollPane = new JScrollPane(this.panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		this.scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		this.frame.setContentPane(this.scrollPane);
		if (reload) {
			this.frame.setEnabled(true);
		}
		this.btns = new JButton[xlen][ylen];
		for (int y = 0; y < ylen; y++) {
			for (int x = 0; x < xlen; x++) {
				final JButton btn = new JButton();
				this.btns[x][y] = btn;
				this.panel.add(btn);
				btn.setBorderPainted(false);
				final int fx = x;
				final int fy = y;
				btn.addActionListener(e -> pressed(fx, fy));
				btn.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(java.awt.event.FocusEvent e) {
						btn.setBorderPainted(false);
					}
					
					@Override
					public void focusGained(java.awt.event.FocusEvent e) {
						btn.setBorderPainted(true);
					}
					
				});
				btn.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseExited(MouseEvent e) {
						btn.setBorderPainted(false);
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						if (SquareConquererGUI.this.world instanceof RootWorld.Builder && SquareConquererGUI.this.myBuildMode != null
								&& SquareConquererGUI.this.myBuildMode.isActive()) {
							int mod = e.getModifiersEx();
							if ((mod & InputEvent.BUTTON1_DOWN_MASK) != 0) {
								pressed(fx, fy);
							}
						}
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						btn.requestFocusInWindow(Cause.MOUSE_EVENT);
						if (SquareConquererGUI.this.world instanceof RootWorld.Builder && SquareConquererGUI.this.myBuildMode != null
								&& SquareConquererGUI.this.myBuildMode.isActive()) {
							int mod = e.getModifiersEx();
							if ((mod & InputEvent.BUTTON1_DOWN_MASK) != 0) {
								pressed(fx, fy);
							}
						}
					}
					
				});
			}
		}
		this.btns[0][0].addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				if (SquareConquererGUI.this.btns[0][0].getWidth() != SquareConquererGUI.this.btns[0][0].getHeight()) {
					int max = Math.max(SquareConquererGUI.this.btns[0][0].getWidth(), SquareConquererGUI.this.btns[0][0].getHeight());
					Settings.iconSize(max);
					Dimension dim = new Dimension(max * SquareConquererGUI.this.btns.length, max * SquareConquererGUI.this.btns[0].length);
					SquareConquererGUI.this.panel.setPreferredSize(dim);
				}
				update(null);
			}
			
		});
		System.out.println("reload: buttons initialized");
		if (!reload) {
			this.frame.setLocationByPlatform(true);
			System.out.println("set now visible");
			long start = System.currentTimeMillis();
			this.frame.setVisible(true);
			long end = System.currentTimeMillis();
			System.out.println("needed " + (end - start) + "ms to make the frame visible");
		}
		resizeFrame(panelDim);
		addHoveringBtn(iconSize);
		update(ufh);
		if (addNTL) {
			this.world.addNextTurnListener((wh, th) -> update(null));
		}
	}
	
	private void wheelScroll(MouseWheelEvent e) {
		JScrollBar horizont = this.scrollPane.getHorizontalScrollBar();
		JScrollBar vertical = this.scrollPane.getVerticalScrollBar();
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0 || e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			JScrollBar sb;
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
				sb = horizont;
			} else {
				sb = vertical;
			}
			sb.dispatchEvent(e);
			return;
		}
		e.consume();
		double relativeHorizont = (horizont.getValue() + horizont.getVisibleAmount() * .5D) / horizont.getMaximum();
		double relativeVertical = (vertical.getValue() + vertical.getVisibleAmount() * .5D) / vertical.getMaximum();
		int    amnt             = e.getWheelRotation();
		int    w                = Settings.iconSize();
		if (amnt > 0) {
			int ow = w;
			do {
				w = (int) (w / 1.0625D);
			} while (amnt-- > 0);
			if (w == ow) w--;
		} else if (amnt < 0) {
			int ow = w;
			do {
				w = (int) (w * 1.125D);
			} while (amnt++ > 0);
			if (w == ow) w++;
		}
		Settings.iconSize(Settings.between(2, w, 4096));
		int       is  = Settings.iconSize();
		Dimension dim = new Dimension(is * this.btns.length, is * this.btns[0].length);
		this.panel.setPreferredSize(dim);
		resizeFrame(dim);
		addHoveringBtn(is);
		horizont.setValue((int) (relativeHorizont * horizont.getMaximum() - horizont.getVisibleAmount() * .5D));
		vertical.setValue((int) (relativeVertical * vertical.getMaximum() - vertical.getVisibleAmount() * .5D));
		this.panel.invalidate();
	}
	
	private void resizeFrame(Dimension panelDim) {
		// the frame.pack() method is just to slow for large worlds
		GraphicsConfiguration conf = this.frame.getGraphicsConfiguration();
		doReize(panelDim, conf, true);
	}
	
	private void doReize(Dimension panelDim, GraphicsConfiguration conf, boolean firstCall) {
		int x = this.frame.getX();
		int y = this.frame.getY();
		System.out.println("x=" + x + " y=" + y);
		if (firstCall) conf = getConf(conf, x, y);
		System.out.println("conf: conf=" + conf);
		Rectangle bounds = conf.getBounds();
		System.out.println("      bnds=" + bounds);
		Insets confInsets = Toolkit.getDefaultToolkit().getScreenInsets(conf);
		System.out.println("      cins=" + confInsets);
		bounds.x      += confInsets.left;
		bounds.y      += confInsets.top;
		bounds.width  -= confInsets.left + confInsets.right;
		bounds.height -= confInsets.top + confInsets.bottom;
		Insets insets = this.frame.getInsets();
		System.out.println("      fins=" + insets);
		Dimension menuDim = this.menu.getPreferredSize();
		int       w       = Math.min(Math.max(panelDim.width, menuDim.width) + insets.left + insets.right, bounds.width);
		int       h       = Math.min(panelDim.height + menuDim.height + insets.bottom + insets.top, bounds.height);
		if (w != bounds.width || h != bounds.height) { // small world, do pack (getInsets before pack or visible is useless)
			this.frame.pack();
			w = Math.min(this.frame.getWidth(), bounds.width);
			h = Math.min(this.frame.getHeight(), bounds.height);
		}
		if (this.frame.getWidth() != w || this.frame.getHeight() != h) {
			this.frame.setSize(w, h);
		}
		x = this.frame.getX();
		y = this.frame.getY();
		System.out.println("x=" + x + " y=" + y);
		GraphicsConfiguration conf2 = getConf(conf, x, y);
		if (conf2 != conf && firstCall) {
			doReize(panelDim, conf2, false);
			return;
		}
		if (bounds.x > x) {
			x = bounds.x;
		} else if (bounds.x < x) {
			int bw = bounds.width - x + bounds.x;
			if (bw < w) {
				x += bw - w;
			}
		}
		if (bounds.y > y) {
			y = bounds.y;
		} else if (bounds.y < y) {
			int bh = bounds.height - y + bounds.y;
			if (bh < h) {
				y += bh - h;
			}
		}
		if (this.frame.getX() != x || this.frame.getY() != y) {
			this.frame.setLocation(x, y);
		}
	}
	
	private GraphicsConfiguration getConf(GraphicsConfiguration conf, int x, int y) {
		for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			GraphicsConfiguration screenConf = screen.getDefaultConfiguration();
			Rectangle             bnds       = screenConf.getBounds();
			if (bnds.contains(x + (this.frame.getWidth() >>> 1), y + (this.frame.getHeight() >>> 1))) {
				System.out.println("found conf: conf=" + conf);
				conf = screenConf;
				break;
			}
		}
		return conf;
	}
	
	private void addHoveringBtn(int tileIconSize) {
		if (this.hoveringRigthButton == null) {
			this.hoveringRigthButton = new JButton();
			this.hoveringRigthButton.addActionListener(this::hoverBtnAction);
			initHoverBtn(this.hoveringRigthButton);
			initHBMouseListeners(this.hoveringRigthButton);
		}
		setHBImage(tileIconSize, this.hoveringRigthButton);
		if (this.world instanceof RootWorld.Builder || this.source != null) {
			if (this.hoveringLeftButton == null) {
				this.hoveringLeftButton = new JButton();
				this.hoveringLeftButton.addActionListener(this::hoverLeftBtnAction);
				initHoverBtn(this.hoveringLeftButton);
				initHBMouseListeners(this.hoveringLeftButton);
			}
			setHBImage(tileIconSize, this.hoveringLeftButton);
		}
		JPanel p = new JPanel();
		p.setLayout(null);
		p.add(this.hoveringRigthButton);
		if (this.world instanceof RootWorld.Builder || this.source != null) p.add(this.hoveringLeftButton);
		this.frame.setGlassPane(p);
		p.setBounds(0, 0, this.scrollPane.getWidth(), this.scrollPane.getHeight() + this.menu.getHeight());
		p.setVisible(true);
		p.setOpaque(false);
		setHBLocation(this.hoveringRigthButton);
		if (this.world instanceof RootWorld.Builder || this.source != null) setHBLocation(this.hoveringLeftButton);
		this.scrollPane.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				p.setBounds(0, 0, SquareConquererGUI.this.scrollPane.getWidth(),
						SquareConquererGUI.this.scrollPane.getHeight() + SquareConquererGUI.this.menu.getHeight());
				setHBLocation(SquareConquererGUI.this.hoveringRigthButton);
				if (SquareConquererGUI.this.world instanceof RootWorld.Builder || SquareConquererGUI.this.source != null)
					setHBLocation(SquareConquererGUI.this.hoveringLeftButton);
			}
			
		});
		
	}
	
	private void setHBLocation(JButton hoveringButton) {
		int y = this.scrollPane.getY() + this.scrollPane.getHeight() - hoveringButton.getHeight();
		if (this.scrollPane.getHorizontalScrollBar().isShowing()) {
			y -= this.scrollPane.getHorizontalScrollBar().getHeight();
		}
		if (hoveringButton == this.hoveringRigthButton) {
			int x = this.scrollPane.getWidth() - hoveringButton.getWidth();
			if (this.scrollPane.getVerticalScrollBar().isShowing()) {
				x -= this.scrollPane.getVerticalScrollBar().getWidth();
			}
			hoveringButton.setLocation(x, y);
		} else {
			hoveringButton.setLocation(this.scrollPane.getX(), y);
		}
	}
	
	private void initHBMouseListeners(JButton hoveringButton) {
		MouseAdapter val = generateHBMouseAdapter(hoveringButton);
		hoveringButton.addMouseMotionListener(val);
		hoveringButton.addMouseListener(val);
		hoveringButton.addMouseWheelListener(e -> {
			if (ignore(hoveringButton, e)) {
				Component  comp = this.panel.findComponentAt(hoveringButton.getX() + e.getX() - this.panel.getX(),
						hoveringButton.getY() + e.getY() - this.panel.getY());
				MouseEvent newE = SwingUtilities.convertMouseEvent(hoveringButton, e, comp);
				comp.dispatchEvent(newE);
			}
		});
	}
	
	private MouseAdapter generateHBMouseAdapter(JButton hoveringButton) {
		return new MouseAdapter() {
			
			JButton lastbtn;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					Component comp = findComp(hoveringButton, e);
					if (comp instanceof JButton btn) {
						MouseEvent event = new MouseEvent(btn, e.getID(), e.getWhen(), e.getModifiersEx(), hoveringButton.getX() + e.getX() - btn.getX(),
								hoveringButton.getY() + e.getY() - btn.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(),
								e.getButton());
						btn.dispatchEvent(event);
					}
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					Component comp = findComp(hoveringButton, e);
					if (comp instanceof JButton btn) {
						MouseEvent event = new MouseEvent(btn, e.getID(), e.getWhen(), e.getModifiersEx(), hoveringButton.getX() + e.getX() - btn.getX(),
								hoveringButton.getY() + e.getY() - btn.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(),
								e.getButton());
						btn.dispatchEvent(event);
					}
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					Component comp = findComp(hoveringButton, e);
					if (comp != this.lastbtn && this.lastbtn != null) {
						this.lastbtn.setBorderPainted(false);
					} else if (comp == this.lastbtn) {
						if (comp == null) return;
						MouseEvent event = new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiersEx(), hoveringButton.getX() + e.getX() - comp.getX(),
								hoveringButton.getY() + e.getY() - comp.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(),
								e.getButton());
						comp.dispatchEvent(event);
						return;
					}
					if (comp == null) return;
					if (comp instanceof JButton btn) {
						btn.setBorderPainted(true);
						this.lastbtn = btn;
					}
					MouseEvent event = new MouseEvent(comp, MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiersEx(),
							hoveringButton.getX() + e.getX() - comp.getX(), hoveringButton.getY() + e.getY() - comp.getY(), e.getXOnScreen(), e.getYOnScreen(),
							e.getClickCount(), e.isPopupTrigger(), e.getButton());
					comp.dispatchEvent(event); // send mouse enter event, so the button displays the correct border
					// no need to send an mouse leave event (I only let the mouse hover border print
					comp.requestFocusInWindow(Cause.MOUSE_EVENT);// or non border at all be displayed)
				} else if (this.lastbtn != null) {
					this.lastbtn.setBorderPainted(false);
					hoveringButton.requestFocusInWindow(Cause.MOUSE_EVENT);
					this.lastbtn = null;
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mouseMoved(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				this.lastbtn = null;
				mouseMoved(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					SquareConquererGUI.this.skipAction++;
				}
			}
			
		};
	}
	
	private static void initHoverBtn(JButton hoveringButton) {
		hoveringButton.setOpaque(false);
		hoveringButton.setFocusPainted(false);
		hoveringButton.setBorderPainted(false);
		hoveringButton.setContentAreaFilled(false);
		hoveringButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}
	
	private void setHBImage(int tileIconSize, JButton hoveringButton) {
		try {
			BufferedImage img = ImageIO.read(getClass().getResource(switch (this.world) {
			case RootWorld.Builder b -> {
				if (hoveringButton == this.hoveringRigthButton) {
					b.addNextTurnListener((wh, th) -> hoveringButton.setVisible(b.buildable()));
					hoveringButton.setToolTipText("<html>build the world</html>");
					hoveringButton.setVisible(b.buildable());
					yield "/img/BUILD.png";
				}
				hoveringButton.setVisible(true);
				yield "/img/BUILD_MODE.png";
			}
			case RootWorld rw -> {
				if (this.source != null) {
					if (hoveringButton == this.hoveringRigthButton) {
						hoveringButton.setVisible(true);
						hoveringButton.setToolTipText("<html>goto the next turn</html>");
						yield "/img/NEXT_TURN.png";
					}
					hoveringButton.setVisible(true);
					hoveringButton.setToolTipText("<html>goto the previos turn</html>");
					yield "/img/PREV_TURN.png";
				}
				rw.addNextTurnListener((wh, th) -> hoveringButton.setVisible(rw.running()));
				hoveringButton.setVisible(!rw.running());
				hoveringButton.setToolTipText("<html>start the game</html>");
				yield "/img/START_GAME.png";
			}
			default -> {
				hoveringButton.setToolTipText("<html>finish your turn</html>");
				yield "/img/FINISH_TURN.png";
			}
			}));
			
			int       ftIconSize = hoverButtonSize(tileIconSize);
			ImageIcon icon0      = new ImageIcon(img.getScaledInstance(ftIconSize, ftIconSize, Image.SCALE_SMOOTH));
			hoveringButton.setIcon(icon0);
			hoveringButton.setDisabledIcon(icon0);
			hoveringButton.addComponentListener(new ComponentAdapter() {
				
				int curw = tileIconSize;
				
				@Override
				public void componentResized(ComponentEvent e) {
					int w   = hoveringButton.getWidth();
					int h   = hoveringButton.getHeight();
					int min = Math.min(w, h);
					if (min == this.curw) { return; }
					ImageIcon icon = new ImageIcon(img.getScaledInstance(min, min, Image.SCALE_SMOOTH));
					hoveringButton.setIcon(icon);
				}
				
			});
			hoveringButton.setSize(ftIconSize, ftIconSize);
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	private static int hoverButtonSize(int tileIconSize) { // 3/4 tileSize
		return (tileIconSize >>> 1) + (tileIconSize >>> 2);
	}
	
	private Component findComp(JButton hb, MouseEvent e) {
		return this.panel.findComponentAt(hb.getX() + e.getX() - this.panel.getX(), hb.getY() + e.getY() - this.panel.getY());
	}
	
	private int skipAction;
	// FIXME: find a better solution
	
	private static boolean ignore(JButton hb, MouseEvent e) {
		int    len     = hb.getWidth();
		int    mid     = len >>> 1;
		int    x       = e.getX();
		int    y       = e.getY();
		double diffx   = Math.abs(mid - x);
		double diffy   = Math.abs(mid - y);
		double sqrx    = diffx * diffx;
		double sqry    = diffy * diffy;
		double dist    = Math.sqrt(sqrx + sqry);
		double relDist = dist;
		return relDist > 0.465D * len;
	}
	
	private void hoverLeftBtnAction(@SuppressWarnings("unused") ActionEvent ignore) {
		if (this.skipAction > 0) {
			this.skipAction--;
			return;
		}
		
		if (this.source != null) {
			gotoTurn(this.index - 1);
			return;
		}
		
		if (this.myBuildMode == null) {
			this.myBuildMode = new TileChanger();
		}
		JDialog dialog = createDialog();
		dialog.setTitle("change the build mode");
		JPanel dp = new JPanel();
		dialog.setContentPane(dp);
		dp.setLayout(new GridLayout(0, 1));
		JButton btn = new JButton("none/inactive");
		btn.setToolTipText("<html>no build mode, open a dialog when clicking at a tile</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			this.myBuildMode.makeInactive();
			dialog.dispose();
		});
		btn = new JButton("+normal");
		btn.setToolTipText("<html>change the tile type to a normal tile type (remove hills and make water non-deep)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			this.myBuildMode.makePlusNormal();
			dialog.dispose();
		});
		btn = new JButton("+hill");
		btn.setToolTipText("<html>add hills to the tile type (do nothing if the type does not support hills)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			this.myBuildMode.makePlusHill();
			dialog.dispose();
		});
		btn = new JButton("+deep");
		btn.setToolTipText("<html>make (normal) water deep/ocean water (do nothing if the type does not support the deep mode)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			this.myBuildMode.makePlusDeep();
			dialog.dispose();
		});
		String[] vals = new String[GroundType.count() + 1];
		for (int i = 0; i < vals.length - 1; i++) { vals[i] = GroundType.of(i).toString(); }
		vals[vals.length - 1] = "set the ground";
		JComboBox<String> ttcombo = new JComboBox<>(vals);
		ttcombo.setSelectedIndex(vals.length - 1);
		ttcombo.setEditable(false);
		dp.add(ttcombo);
		ttcombo.addActionListener(e -> {
			if (ttcombo.getSelectedIndex() >= GroundType.count()) return;
			this.myBuildMode.makeSetGround(GroundType.of(ttcombo.getSelectedIndex()));
			dialog.dispose();
		});
		vals = new String[OreResourceType.count() + 1];
		for (int i = 0; i < vals.length - 1; i++) { vals[i] = GroundType.of(i).toString(); }
		vals[vals.length - 1] = "set the ore";
		JComboBox<String> ortcombo = new JComboBox<>(vals);
		ortcombo.setSelectedIndex(vals.length - 1);
		ortcombo.setEditable(false);
		dp.add(ortcombo);
		ortcombo.addActionListener(e -> {
			if (ortcombo.getSelectedIndex() >= GroundType.count()) return;
			this.myBuildMode.makeSetGround(GroundType.of(ortcombo.getSelectedIndex()));
			dialog.dispose();
		});
		initDialog(dialog, false);
	}
	
	private void hoverBtnAction(ActionEvent e) {
		// Well I can't get the position of an action event
		if (this.skipAction > 0) {
			this.skipAction--;
			return;
		}
		
		switch (this.world) {
		case RootWorld rw -> {
			if (this.source != null) {
				gotoTurn(this.index + 1);
				return;
			}
			if (this.connects == null) {
				JOptionPane.showMessageDialog(this.frame, "you need to start a server and all players need to be connected at game start", "no server started",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int chosen = JOptionPane.showConfirmDialog(this.frame, "start the game", "START", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) { return; }
			synchronized (this) {
				Map<User, Connection> conns = this.connects;
				byte[]                seed  = new byte[(conns.size() + 1) << 4];
				byte[]                tmp   = new byte[16];
				User.fillRandom(tmp);
				System.arraycopy(tmp, 0, seed, 0, 16);
				int i = 16;
				try {
					List<Connection> list = new ArrayList<>(conns.values());
					Collections.sort(list, (a, b) -> a.usr.name().compareTo(b.usr.name()));
					for (Iterator<Connection> iter = list.iterator(); iter.hasNext(); i += 16) {
						final Connection conn = iter.next();
						conn.blocked(() -> RootWorld.fillRnd(conn, tmp));
						System.arraycopy(tmp, 0, seed, i, 16);
					}
					if (i < seed.length) throw new ConcurrentModificationException();
					rw.startGame(seed);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this.frame, "could not get the random value from a client: " + e1.toString(), "start failed",
							JOptionPane.ERROR_MESSAGE);
				} catch (@SuppressWarnings("unused") ConcurrentModificationException err) {
					JOptionPane.showMessageDialog(this.frame, "accepted/lost a connection during the initilation", "start failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		case RootWorld.Builder b -> {
			int chosen = JOptionPane.showConfirmDialog(this.frame, "build the world", "BUILD", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) { return; }
			RootWorld rw = b.create();
			this.world = rw;
			reload(this.buildFinishHook, true, true);
		}
		case RemoteWorld rw -> threadStart(this::finishTurn);
		default -> throw new AssertionError("illegal world type: " + this.world.getClass());
		}
	}
	
	private void finishTurn() throws AssertionError {
		int chosen = JOptionPane.showConfirmDialog(this.frame, "finish your turn", "finish", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chosen != JOptionPane.YES_OPTION) { return; }
		Turn t = new Turn(this.world);
		for (int x = 0; x < this.turns.length; x++) {
			EntityTurn[] ets = this.turns[x];
			for (int y = 0; y < ets.length; y++) {
				EntityTurn et = ets[y];
				if (et == null) {
					continue;
				}
				ets[y] = null;
				Tile   tile = this.world.tile(x, y);
				Entity ett  = switch (et) {
							case CarryTurn val -> tile.unit();
							case MoveTurn val -> tile.unit();
							case StoreTurn val -> tile.unit();
							default -> throw new AssertionError("unknown entity turn type: " + et.getClass());
							};
				t.put(ett, et);
			}
		}
		this.world.finish(t);
	}
	
	public void visible(boolean b) {
		if (this.frame == null) { throw new IllegalStateException("not yet loaded!"); }
		if (ensureGUIThread(() -> visible(b))) { return; }
		if (this.frame.isVisible() == b) { return; }
		if (b) {
			System.out.println("set now visible (large worlds may need some time) world size: (xlen=" + this.world.xlen() + "|ylen=" + this.world.ylen() + ')');
			this.frame.setVisible(true);
			update(null);
		} else {
			this.frame.setVisible(false);
		}
	}
	
	private volatile Thread   updateThread;
	private volatile Runnable updateFinishHook;
	
	private void update(Runnable ufh) {
		if (this.frame == null) throw new IllegalStateException("not yet loaded!");
		Runnable updateFin = this.updateFinishHook;
		if (ufh != updateFin) {
			assert ufh == null || updateFin == null;
			return;
		}
		if (this.updateThread != null) {
			assert ufh == null;
			return;
		}
		if (ensureNotGUIThread(() -> update(ufh))) return;
		synchronized (SquareConquererGUI.this) {
			if (this.updateThread != null) return;
			this.updateThread = Thread.currentThread();
		}
		try {
			updateBtns();
			if (ufh != null) {
				System.out.println("update: buttons updated");
			}
			Runnable ufh0;
			synchronized (SquareConquererGUI.this) {
				ufh0 = this.updateFinishHook;
				if (ufh != null) {
					this.updateFinishHook = null;
				}
			}
			SwingUtilities.invokeLater(() -> {
				if (this.frame.isVisible()) { // invalidate and repaint does not work, setVisible does
					this.frame.setVisible(true);
				}
				if (ufh0 != null) {
					ufh0.run();
				}
			});
		} finally {
			synchronized (SquareConquererGUI.this) {
				this.updateThread = null;
				SquareConquererGUI.this.notifyAll();
			}
		}
	}
	
	private void updateBtns() {
		int       size = iconSize();
		final int xlen = this.btns.length;
		final int ylen = this.btns[0].length;
		for (int x = 0; x < xlen; x++) {
			JButton[] bs = this.btns[x];
			for (int y = 0; y < ylen; y++) {
				if (Thread.interrupted()) return;
				Tile          t = this.world.tile(x, y);
				final Icon    i = t.icon(size, size);
				StringBuilder b = new StringBuilder().append("<html>");
				if (t.hasPage()) {
					b.append("page: ").append(t.pageTitle()).append("<br>");
				}
				b.append("ground: ").append(t.ground);
				switch (t.resource) {
				case Object o when o == OreResourceType.GOLD_ORE -> b.append("<br>resource: Gold Ore");
				case Object o when o == OreResourceType.IRON_ORE -> b.append("<br>resource: Iron Ore");
				case Object o when o == OreResourceType.COAL_ORE -> b.append("<br>resource: Coal Ore");
				case Object o when o == OreResourceType.NONE -> {/**/}
				default -> throw new AssertionError(t.resource.name());
				}
				final String  tt  = b.append("</html>").toString();
				final JButton btn = bs[y];
				SwingUtilities.invokeLater(() -> {
					btn.setIcon(i);
					btn.setToolTipText(tt);
				});
			}
		}
	}
	
	private int iconSize() {
		int size = Settings.iconSize();
		if (this.btns[0][0].getWidth() > 0 && this.btns[0][0].getHeight() > 0) {
			size = Math.min(this.btns[0][0].getWidth(), this.btns[0][0].getHeight());
		}
		return size;
	}
	
	private void pressed(int x, int y) {
		if (this.world instanceof RootWorld.Builder b && this.myBuildMode != null && this.myBuildMode.isActive()) {
			Tile t = this.myBuildMode.modify(b.tile(x, y));
			b.set(x, y, t);
			return;
		}
		Tile    t      = this.world.tile(x, y);
		JDialog dialog = createDialog();
		dialog.setTitle("Tile at (" + x + '|' + y + ')');
		JPanel dp = new JPanel();
		dialog.setContentPane(dp);
		dp.setLayout(new GridLayout(0, 2));
		if (t.hasPage()) {
			dp.add(new JLabel("Page:"));
			JButton pbtn = new JButton(t.pageTitle());
			dp.add(pbtn);
			pbtn.addActionListener(e -> showPage(new JDialog(dialog), t.page(), t.pageTitle(), null));
		}
		if (this.world instanceof RootWorld.Builder rb) {
			JComboBox<GroundType>      cbt = new JComboBox<>(GroundType.values());
			JComboBox<OreResourceType> cbr = new JComboBox<>(OreResourceType.values());
			cbt.setEditable(false);
			cbr.setEditable(false);
			cbt.setSelectedIndex(t.ground.ordinal());
			cbr.setSelectedIndex(t.resource.ordinal());
			cbt.addActionListener(e -> rb.set(x, y, GroundType.of(cbt.getSelectedIndex())));
			cbr.addActionListener(e -> rb.set(x, y, OreResourceType.of(cbr.getSelectedIndex())));
			dp.add(new JLabel("ground:"));
			dp.add(cbt);
			dp.add(new JLabel("resource:"));
			dp.add(cbr);
		} else {
			dp.add(new JLabel("ground:"));
			dp.add(new JLabel(t.ground.toString()));
			dp.add(new JLabel("resource:"));
			dp.add(new JLabel(t.resource.toString()));
		}
		if (!t.visible()) {
			dp.add(new JLabel("visible"));
			dp.add(new JLabel("currently not visible"));
		}
		Building b = t.building();
		dp.add(new JLabel("Building:"));
		if (b != null) {
			switch (b) {
			case Storage sb -> {
				dp.add(new JLabel("Storage"));
				generalBuildingInfo(dp, sb, "    ");
				IntMap<OreResourceType> ores = sb.ores();
				int[]                   arr  = ores.array();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] > 0) {
						dp.add(new JLabel("    " + OreResourceType.of(i) + ": "));
						dp.add(new JLabel(Integer.toString(arr[i])));
					}
				}
				IntMap<ProducableResourceType> producable = sb.producable();
				arr = producable.array();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] > 0) {
						dp.add(new JLabel("    " + ProducableResourceType.of(i) + ": "));
						dp.add(new JLabel(Integer.toString(arr[i])));
					}
				}
			}
			default -> throw new AssertionError("unknown build type: " + b.getClass());
			}
		} else {
			dp.add(new JLabel("none"));
		}
		Unit u = t.unit();
		dp.add(new JLabel("Unit:"));
		if (u != null) {
			switch (u) {
			case Carrier c -> {
				dp.add(new JLabel("Carrier"));
				generalUnitInfo(dp, c, "    ");
			}
			default -> throw new AssertionError("unknown unit type: " + u.getClass());
			}
		} else {
			dp.add(new JLabel("none"));
		}
		initDialog(dialog, false);
	}
	
	private static void generalUnitInfo(JPanel p, Unit u, String indention) {
		generalEntityInfo(p, u, indention);
		p.add(new JLabel(indention + "can carry at max:"));
		p.add(new JLabel(Integer.toString(u.carryMaxAmount())));
		int carry = u.carryAmount();
		if (carry > 0) {
			p.add(new JLabel(indention + "carries:"));
			p.add(new JLabel(carry + " " + u.carryRes()));
		}
	}
	
	private static void generalBuildingInfo(JPanel p, Building b, String indention) {
		generalEntityInfo(p, b, indention);
		p.add(new JLabel(indention + "build state:"));
		if (b.isFinishedBuild()) {
			p.add(new JLabel("finished"));
		} else {
			p.add(new JLabel("not yet finished"));
			IntMap<ProducableResourceType> res = b.neededResources();
			if (res != null) {
				int[] arr = res.array();
				for (int i = 0; i < ProducableResourceType.count(); i++) {
					int cnt = arr[i];
					if (cnt <= 0) {
						continue;
					}
					p.add(new JLabel(indention + "    needed " + ProducableResourceType.of(i) + ":"));
					p.add(new JLabel(Integer.toString(cnt)));
				}
			}
			p.add(new JLabel(indention + "    needed build turns:"));
			p.add(new JLabel(Integer.toString(b.remainingBuildTurns())));
		}
	}
	
	private static void generalEntityInfo(JPanel p, Entity e, String indention) {
		p.add(new JLabel(indention + "owner:"));
		p.add(new JLabel(e.owner().name()));
		p.add(new JLabel(indention + "lives:"));
		p.add(new JLabel(e.lives() + " of " + e.maxLives()));
	}
	
	private static boolean ensureGUIThread(Runnable r) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
				return true;
			} catch (InvocationTargetException e) {
				Throwable c = e.getCause();
				if (c instanceof RuntimeException re) throw re;
				if (c instanceof Error err) throw err;
				throw new AssertionError(e.toString(), e);
			} catch (InterruptedException e) {
				throw new AssertionError(e.toString(), e);
			}
		}
		return false;
	}
	
	private static boolean ensureNotGUIThread(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			Thread t = threadStart(r);
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new AssertionError(e.toString(), e);
			}
			return true;
		}
		return false;
	}
	
}
