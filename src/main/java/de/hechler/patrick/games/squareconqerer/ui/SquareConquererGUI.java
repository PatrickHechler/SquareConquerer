package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
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
import java.net.ServerSocket;
import java.net.URI;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCLicense;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPage;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageEntry;
import de.hechler.patrick.games.squareconqerer.addons.pages.SCPageBlock.SeperatingBlock;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.objects.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.PageWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Carrier;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.entity.StoreBuild;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;
import de.hechler.patrick.games.squareconqerer.world.turn.CarryTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.EntityTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.MoveTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.StoreTurn;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

@SuppressWarnings("preview")
public class SquareConquererGUI {
	
	private World world;
	
	private JFrame         frame;
	private JPanel         panel;
	private JMenuBar       menu;
	private JScrollPane    scrollPane;
	private JButton        hoveringButton;
	private JButton        hoveringBuildModeButton;
	private BuildMode      myBuildMode;
	private JButton[][]    btns;
	private EntityTurn[][] turns;
	
	private volatile Map<User, Connection> connects;
	
	private final Runnable loadFinishedHook = () -> {
		if (world instanceof RootWorld) {
			JOptionPane.showMessageDialog(frame, "finishd loading", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is in build mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world instanceof UserWorld) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is in user mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world instanceof RemoteWorld) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is in remote mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world != null) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is of type: " + world.getClass().getSimpleName(), "load",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(frame, "could not load for some reason", "load failed", JOptionPane.ERROR_MESSAGE);
		}
	};
	
	private final Runnable buildFinishHook = () -> {
		if (world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(frame, "the converting to a build mode world is now completed", "converted to build",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(frame, "the world build operation is now completed", "finished build", JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Thread serverThread;
	
	public SquareConquererGUI(World world) {
		if (world == null) {
			throw new NullPointerException("world is null");
		}
		this.world = world;
	}
	
	public void load(Thread t) {
		if (frame != null) {
			throw new IllegalStateException("already loaded");
		} // I don't need a second method for that
		if (ensureGUIThread(() -> load(t))) {
			return;
		}
		serverThread = t;
		load();
	}
	
	public void load() {
		if (frame != null) {
			throw new IllegalStateException("already loaded");
		} // I don't need a second method for that
		if (ensureGUIThread(this::load)) {
			return;
		}
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				int chosen = JOptionPane.showConfirmDialog(frame, "Exit Square Conquerer?", "Exit", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
			
		});
		
		reload(null, false, true);
	}
	
	private void initMenu() {
		menu = new JMenuBar();
		menu.add(menuGeneral());
		if (world instanceof RootWorld) {
			menu.add(menuServer());
		}
		menu.add(menuBuild());
		menu.add(menuHelp());
		menu.add(menuCredits());
		menu.add(menuLicense());
		frame.setJMenuBar(menu);
	}
	
	private JMenu menuLicense() {
		JMenu licenseMenu = new JMenu("License");
		addLicense(licenseMenu, SquareConquererAddon.theGame().license(), SquareConquererAddon.GAME_ADDON_NAME);
		for (SquareConquererAddon addon : SquareConquererAddon.onlyAddons()) {
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
		if (!SquareConquererAddon.onlyAddons().isEmpty()) {
			List<SCPageBlock> blocks = new ArrayList<>();
			blocks.addAll(SquareConquererAddon.theGame().credits().blocks());
			for (SquareConquererAddon addon : SquareConquererAddon.onlyAddons()) {
				blocks.add(new SCPageBlock.SeperatingBlock(true));
				blocks.addAll(addon.credits().blocks());
			}
			SCPage page = new SCPage(blocks);
			addWorldPage(creditsMenu, page, "All Credits (World)", "All Credits World");
			addPage(creditsMenu, page, "All Credits (Boring)", "Credits: ");
		}
		addWorldPage(creditsMenu, SquareConquererAddon.theGame().credits(), SquareConquererAddon.GAME_ADDON_NAME + " (World)",
				SquareConquererAddon.GAME_ADDON_NAME + " (Credits World)");
		addPage(creditsMenu, SquareConquererAddon.theGame().credits(), SquareConquererAddon.GAME_ADDON_NAME + " (Boring)", "Credits: ");
		for (SquareConquererAddon addon : SquareConquererAddon.onlyAddons()) {
			addWorldPage(creditsMenu, addon.credits(), "Addon: " + addon.name + " (World)", "Addon: " + addon.name + " (Credits World)");
			addPage(creditsMenu, addon.credits(), "Addon: " + addon.name + " (Boring)", "Credits: ");
		}
		return creditsMenu;
	}
	
	private JMenu menuHelp() {
		JMenu helpMenu = new JMenu("Help");
		if (!SquareConquererAddon.onlyAddons().isEmpty()) {
			List<SCPageBlock> blocks = new ArrayList<>();
			blocks.addAll(SquareConquererAddon.theGame().help().blocks());
			for (SquareConquererAddon addon : SquareConquererAddon.onlyAddons()) {
				blocks.add(new SCPageBlock.SeperatingBlock(true));
				blocks.addAll(addon.help().blocks());
			}
			SCPage page = new SCPage(blocks);
			addWorldPage(helpMenu, page, "All Helps (World)", "All Helps World");
			addPage(helpMenu, page, "All Help Pages", "Help: ");
		}
		addWorldPage(helpMenu, SquareConquererAddon.theGame().help(), SquareConquererAddon.GAME_ADDON_NAME + " (World)",
				SquareConquererAddon.GAME_ADDON_NAME + " (Help World)");
		addPage(helpMenu, SquareConquererAddon.theGame().help(), SquareConquererAddon.GAME_ADDON_NAME, "Help: ");
		for (SquareConquererAddon addon : SquareConquererAddon.onlyAddons()) {
			addWorldPage(helpMenu, addon.help(), "Addon: " + addon.name + " (World)", "Addon: " + addon.name + " (Help World)");
			addPage(helpMenu, addon.help(), "Addon: " + addon.name, "Help: ");
		}
		return helpMenu;
	}
	
	private void addWorldPage(JMenu menu, SCPage page, String addonName, String title) {
		JMenuItem item = new JMenuItem(addonName);
		item.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(frame, "load " + title + "? (your current world will be discarded)", "load page world",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) return;
			PageWorld pw = new PageWorld(page);
			if (world instanceof RemoteWorld rw) {
				try {
					rw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame, "error wile closing remote world: " + e1.toString() + " (I will continue anyway)",
							"error on close (ignored)", JOptionPane.ERROR_MESSAGE);
				}
			}
			Thread st = serverThread;
			while (st != null) {
				st.interrupt();
				try {
					st.join(10L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				st = serverThread;
			}
			world = pw.createWorld().create();
			reload(loadFinishedHook, true, true);
		});
		menu.add(item);
	}
	
	private void addPage(JMenu menu, SCPage page, String addonName, String titlePrefix) {
		JMenuItem item = new JMenuItem(addonName);
		item.addActionListener(e -> showPage(new JDialog(frame), page, addonName, titlePrefix));
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
			case SCPageBlock.SeperatingBlock sb -> yoff += pageAddSepBlock(dp, yoff, sb);
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
	
	private static int pageAddSepBlock(JPanel dp, int yoff, SeperatingBlock sb) {
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
						"load world " + we.worldName() + '?' + (world == null ? "" : " current world will be discarded!"),
						"load " + we.worldName() + '?', JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) return;
				World w = we.world().get();
				if (w == null) {
					JOptionPane.showMessageDialog(dialog, "error: world is null", "there is no world", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (world instanceof RemoteWorld rw) {
					try {
						rw.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(dialog, "error: while closing remote world: " + e1.toString() + " (I will continue anyway)",
								e1.getClass().toString(), JOptionPane.ERROR_MESSAGE);
					}
				}
				Thread st = serverThread;
				while (st != null) {
					st.interrupt();
					Map<User, Connection> cs = connects;
					try {
						st.join(10L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (cs != null) {
						for (Connection conn : cs.values()) {
							try {
								conn.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					st = serverThread;
				}
				world = w;
				reload(loadFinishedHook, true, true);
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
					int chosen = JOptionPane.showConfirmDialog(dialog, "open link: " + uri, "open link", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
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
		if (world instanceof RootWorld.Builder) {
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
			int chosen = JOptionPane.showConfirmDialog(frame,
					"convert to a build world?"
							+ (world instanceof RemoteWorld || serverThread != null ? " (this will close the server connection)" : ""),
					"to build world", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) {
				return;
			}
			Thread st = serverThread;
			if (st != null) {
				st.interrupt();
			}
			User              oldUsr = world.user();
			RootUser          root   = oldUsr.rootClone();
			RootWorld.Builder b      = new RootWorld.Builder(root, world.xlen(), world.ylen());
			for (int x = 0; x < b.xlen(); x++) {
				for (int y = 0; y < b.ylen(); y++) {
					b.set(x, y, world.tile(x, y));
				}
			}
			if (world instanceof RemoteWorld rw) {
				try {
					rw.close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "error while closing remote world (do not retry, I will proceed anyway): " + e.toString(),
							"error: " + e.getClass(), JOptionPane.ERROR_MESSAGE);
				}
			}
			oldUsr.close();
			world = b;
			b.addNextTurnListener(() -> update(null));
			reload(buildFinishHook, true, true);
		});
		buildMenu.add(toBuild);
	}
	
	private void addFillRandomMenuItems(JMenu buildMenu) {
		JMenuItem fillRandom = new JMenuItem("fill with random tiles");
		fillRandom.setToolTipText("<html>replace all tiles with type not-explored with random tiles<br>"
				+ "note that then also not-explored tiles with a resource set may get their resource replaced<br>"
				+ "the world builder may use rules, which change the possibility for some tiles (such as ocean tiles can only be placed near other water tiles)</html>");
		fillRandom.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(frame, "fill all not-exlpored tiles", "fill random", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) {
				return;
			}
			((RootWorld.Builder) world).fillRandom();
			threadBuilder().start(() -> update(null));
			JOptionPane.showMessageDialog(frame, "filled with random tiles world", "filled world", JOptionPane.INFORMATION_MESSAGE);
		});
		buildMenu.add(fillRandom);
		JMenuItem fillTotallyRandom = new JMenuItem("fill with totally random tiles");
		fillTotallyRandom.setToolTipText("<html>replace all tiles with type not-explored with random tiles<br>"
				+ "note that then also not-explred tiles with a resource set may get their resource replaced</html>");
		fillTotallyRandom.addActionListener(e -> {
			int chosen = JOptionPane.showConfirmDialog(frame, "fill all not-exlpored tiles", "fill random", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) {
				return;
			}
			((RootWorld.Builder) world).fillTotallyRandom();
			JOptionPane.showMessageDialog(frame, "filled with random tiles world", "filled world", JOptionPane.INFORMATION_MESSAGE);
		});
		buildMenu.add(fillTotallyRandom);
	}
	
	private JMenu menuServer() {
		JMenu     serverMenu   = new JMenu("Server");
		JMenuItem openItem     = new JMenuItem("Open Server");
		JMenuItem closeItem    = new JMenuItem("Close Running Server");
		JMenuItem addItem      = new JMenuItem("Add Player");
		JMenuItem delItem      = new JMenuItem("Delete Player");
		JMenuItem pwChangeItem = new JMenuItem("change Password");
		
		openItem.addActionListener(menuServerOpenListener(serverMenu, openItem, closeItem));
		closeItem.addActionListener(menuServerCloseListener(serverMenu, openItem, closeItem));
		addItem.addActionListener(menuServerAddListener());
		delItem.addActionListener(menuServerDeleteListener());
		pwChangeItem.addActionListener(menuServerPWChangeListener());
		
		serverMenu.add(openItem, 0);
		serverMenu.add(addItem);
		serverMenu.add(delItem);
		serverMenu.add(pwChangeItem);
		return serverMenu;
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
			RootUser root = ((RootWorld) world).user();
			root.users().keySet().forEach(combo::addItem);
			
			change.addActionListener(oe -> {
				String name   = combo.getSelectedItem().toString();
				int    chosen = JOptionPane.showConfirmDialog(dialog, "change the password of '" + name + "'?", "change password",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) {
					return;
				}
				root.changePW(root.get(name), pw.getPassword());
				dialog.dispose();
			});
			
			initDialog(dialog, false);
		};
	}
	
	private JDialog createDialog() {
		return createDialog(new JDialog(frame));
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
		dialog.setLocationRelativeTo(frame);
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
		dialog.setLocationRelativeTo(frame);
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
			
			RootUser root = ((RootWorld) world).user();
			root.users().keySet().forEach(combo::addItem);
			
			delBtn.addActionListener(oe -> {
				String name   = combo.getSelectedItem().toString();
				int    chosen = JOptionPane.showConfirmDialog(dialog, "DELETE '" + name + "'?", "DELETE USER", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) {
					return;
				}
				
				threadBuilder().start(() -> {
					root.remove(root.get(name));
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "the user '" + name + "' was successfully deleted",
							"user DELETED", JOptionPane.INFORMATION_MESSAGE));
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
						if (len > 0xFF) {
							return;
						}
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
					RootUser root = ((RootWorld) world).user();
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
			Thread st = serverThread;
			serverThread = null;
			st.interrupt();
			serverMenu.remove(closeItem);
			threadBuilder().start(() -> {
				try {
					st.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				SwingUtilities.invokeLater(() -> serverMenu.add(openItem, 0));
				JOptionPane.showMessageDialog(frame, "server stopped successfully", "Server Stopped", JOptionPane.INFORMATION_MESSAGE);
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
			serverPWCB.setToolTipText("<html>a server password lets remote users create accounts themself<br>"
					+ "It is then no longer needed to add all users manually,<br>"
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
						final HashMap<User, Connection> hm = new HashMap<>();
						connects     = hm;
						serverThread = threadBuilder().start(() -> {
											try {
												RootWorld rw = (RootWorld) world;
												Connection.ServerAccept.accept(ss, rw, (conn, sok) -> {
																	threadBuilder().start(() -> JOptionPane.showMessageDialog(frame,
																			"'" + conn.usr.name() + "' logged in from " + sok.getInetAddress(),
																			"remote log in", JOptionPane.INFORMATION_MESSAGE));
																},
														hm, serverPWCB.isSelected() ? serverPWPF.getPassword() : null);
											} catch (IOException err) {
												if (err instanceof ClosedByInterruptException || Thread.interrupted()) {
													return;
												}
												JOptionPane.showMessageDialog(frame, "error: " + err.getMessage(), err.getClass().getSimpleName(),
														JOptionPane.ERROR_MESSAGE);
											} finally {
												synchronized (SquareConquererGUI.this) {
													if (connects == hm) {
														connects = null;
													}
												}
												for (Connection conn : hm.values()) {
													try {
														conn.close();
													} catch (IOException e1) {
														e1.printStackTrace();
													}
												}
											}
										});
					}
					serverMenu.remove(openItem);
					serverMenu.add(closeItem, 0);
					JOptionPane.showMessageDialog(frame, "server started on port " + ss.getLocalPort(), "Server Started",
							JOptionPane.INFORMATION_MESSAGE);
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
			int chosen = JOptionPane.showConfirmDialog(exitItem, "Exit Square Conquerer?", "Exit", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (chosen == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		});
		generalMenu.add(exitItem);
	}
	
	private void initMenuGeneralLoad(JMenu generalMenu, JFileChooser fc, boolean loadEverything) {
		JMenuItem loadItem = new JMenuItem(loadEverything ? "Load Everything" : "Load");
		loadItem.addActionListener(e -> {
			int result = fc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (loadEverything) {
					threadBuilder().start(() -> loadEverythingFromFile(file));
				} else {
					threadBuilder().start(() -> loadFromFile(file));
				}
			}
		});
		generalMenu.add(loadItem);
	}
	
	private void loadEverythingFromFile(File file) {
		User     usr  = world.user();
		RootUser root = usr.rootClone();
		try {
			try (FileInputStream in = new FileInputStream(file); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, root)) {
				World w = RootWorld.loadEverything(conn);
				synchronized (SquareConquererGUI.this) {
					while (updateThread != null) {
						updateThread.interrupt();
						try {
							updateThread.join(10L);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					this.world = w;
				}
				SwingUtilities.invokeLater(() -> reload(loadFinishedHook, true, true));
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, e.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Throwable t) {
			root.close();
			throw t;
		}
	}
	
	private void loadFromFile(File file) {
		User usr = world.user();
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
				while (updateThread != null) {
					updateThread.interrupt();
					try {
						updateThread.join(100L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				try {
					this.world = RootWorld.Builder.create(root, tiles);
				} catch (IllegalStateException err) {
					this.world = RootWorld.Builder.createBuilder(root, tiles);
				}
			}
			SwingUtilities.invokeLater(() -> reload(loadFinishedHook, true, true));
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initMenuGeneralSave(JMenu generalMenu, JFileChooser fc, boolean saveAll) {
		if (saveAll && !(world instanceof RootWorld)) return;
		JMenuItem saveItem = new JMenuItem(saveAll ? "Save Everything" : "Save");
		saveItem.addActionListener(e -> saveItemActionListener(fc, saveAll));
		generalMenu.add(saveItem);
	}
	
	private void saveItemActionListener(JFileChooser fc, boolean saveAll) {
		if (!(world instanceof RootWorld)) {
			if (saveAll) {
				JOptionPane.showMessageDialog(frame, "the save everything buton should not exist, only root worlds can save everything", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int choosen = JOptionPane.showConfirmDialog(frame,
					"the world is no root world, it may not contain the full information and thus there may be unexplred tiles",
					"save non root world", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choosen != JOptionPane.YES_OPTION) {
				return;
			}
		}
		int result = fc.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				int chosen = JOptionPane.showConfirmDialog(frame, "overwrite '" + file + "'?", "overwrite file", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (chosen != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				try (FileOutputStream out = new FileOutputStream(file);
						Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, world.user());) {
					if (saveAll) {
						((RootWorld) world).saveEverything(conn);
					} else {
						User u = world.user();
						if (u instanceof RootUser root) {
							root.save(conn);
						} else {
							RootUser.nopw().save(conn);
						}
						OpenWorld.saveWorld(world, conn);
					}
					JOptionPane.showMessageDialog(frame, "finishd saving", "save", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(frame, e1.toString(), e1.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void reload(Runnable ufh, boolean reload, boolean addNTL) {
		updateFinishHook = ufh;
		
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		if (reload) {
			frame.setEnabled(false);
		}
		
		frame.setTitle(world.getClass().getSimpleName() + ": " + world.user().name() + " (" + world.xlen() + '|' + world.ylen() + ')');
		
		initMenu();
		System.out.println("reload: menu initialized");
		int xlen     = world.xlen();
		int ylen     = world.ylen();
		int iconSize = Settings.iconSize();
		panel = new JPanel();
		panel.addMouseWheelListener(this::wheelScroll);
		panel.setLayout(new GridLayout(ylen, xlen, 0, 0));
		panel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		Dimension panelDim = new Dimension(xlen * iconSize, ylen * iconSize);
		panel.setPreferredSize(panelDim);
		scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		frame.setContentPane(scrollPane);
		if (reload) {
			frame.setEnabled(true);
		}
		btns = new JButton[xlen][ylen];
		for (int y = 0; y < ylen; y++) {
			for (int x = 0; x < xlen; x++) {
				final JButton btn = new JButton();
				btns[x][y] = btn;
				panel.add(btn);
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
					public void mousePressed(MouseEvent e) {
						if (world instanceof RootWorld.Builder && myBuildMode != null && myBuildMode.isActive()) {
							int mod = e.getModifiersEx();
							if ((mod & InputEvent.BUTTON1_DOWN_MASK) != 0) {
								pressed(fx, fy);
							}
						}
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						btn.requestFocusInWindow(Cause.MOUSE_EVENT);
						if (world instanceof RootWorld.Builder && myBuildMode != null && myBuildMode.isActive()) {
							int mod = e.getModifiersEx();
							if ((mod & InputEvent.BUTTON1_DOWN_MASK) != 0) {
								pressed(fx, fy);
							}
						}
					}
					
				});
			}
		}
		btns[0][0].addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				if (btns[0][0].getWidth() != btns[0][0].getHeight()) {
					int max = Math.max(btns[0][0].getWidth(), btns[0][0].getHeight());
					Settings.iconSize(max);
					Dimension dim = new Dimension(max * btns.length, max * btns[0].length);
					panel.setPreferredSize(dim);
				}
				update(null);
			}
			
		});
		System.out.println("reload: buttons initialized");
		if (!reload) {
			frame.setLocationByPlatform(true);
		}
		resizeFrame(panelDim);
		if (!reload) {
			System.out.println("set now visible");
			long start = System.currentTimeMillis();
			frame.setVisible(true);
			long end = System.currentTimeMillis();
			System.out.println("needed " + (end - start) + "ms to make the frame visible");
		}
		addHoveringBtn(iconSize);
		update(ufh);
		if (addNTL) {
			world.addNextTurnListener(() -> update(null));
		}
	}
	
	private void wheelScroll(MouseWheelEvent e) {
		JScrollBar horizont = scrollPane.getHorizontalScrollBar();
		JScrollBar vertical = scrollPane.getVerticalScrollBar();
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
		Dimension dim = new Dimension(is * btns.length, is * btns[0].length);
		panel.setPreferredSize(dim);
		resizeFrame(dim);
		addHoveringBtn(is);
		horizont.setValue((int) (relativeHorizont * horizont.getMaximum() - horizont.getVisibleAmount() * .5D));
		vertical.setValue((int) (relativeVertical * vertical.getMaximum() - vertical.getVisibleAmount() * .5D));
		panel.invalidate();
	}
	
	private void resizeFrame(Dimension panelDim) {
		// the frame.pack() method is just to slow for large worlds
		GraphicsConfiguration conf    = frame.getGraphicsConfiguration();
		Rectangle             bounds  = conf.getBounds();
		Insets                insets  = frame.getInsets();
		Dimension             menuDim = menu.getPreferredSize();
		int                   w       = Math.min(Math.max(panelDim.width, menuDim.width) + insets.left + insets.right, bounds.width);
		int                   h       = Math.min(panelDim.height + menuDim.height + insets.bottom + insets.top, bounds.height);
		if (w != bounds.width || h != bounds.height) { // small world, do pack (getInsets before pack or visible is useless)
			frame.pack();
			w = Math.min(frame.getWidth(), bounds.width);
			h = Math.min(frame.getHeight(), bounds.height);
		}
		frame.setSize(w, h);
	}
	
	private void addHoveringBtn(int tileIconSize) {
		if (hoveringButton == null) {
			hoveringButton = new JButton();
			hoveringButton.addActionListener(this::hoverBtnAction);
			initHoverBtn(hoveringButton);
			initHBMouseListeners(hoveringButton);
		}
		setHBImage(tileIconSize, hoveringButton);
		if (world instanceof RootWorld.Builder) {
			if (hoveringBuildModeButton == null) {
				hoveringBuildModeButton = new JButton();
				hoveringBuildModeButton.addActionListener(this::hoverBuildBtnAction);
				initHoverBtn(hoveringBuildModeButton);
				initHBMouseListeners(hoveringBuildModeButton);
			}
			setHBImage(tileIconSize, hoveringBuildModeButton);
		}
		JPanel p = new JPanel();
		p.setLayout(null);
		p.add(hoveringButton);
		if (world instanceof RootWorld.Builder) p.add(hoveringBuildModeButton);
		frame.setGlassPane(p);
		p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
		p.setVisible(true);
		p.setOpaque(false);
		setHBLocation(hoveringButton);
		if (world instanceof RootWorld.Builder) setHBLocation(hoveringBuildModeButton);
		scrollPane.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
				setHBLocation(hoveringButton);
				if (world instanceof RootWorld.Builder) setHBLocation(hoveringBuildModeButton);
			}
			
		});
		
	}
	
	private void setHBLocation(JButton hoveringButton) {
		int y = scrollPane.getY() + scrollPane.getHeight() - hoveringButton.getHeight();
		if (scrollPane.getHorizontalScrollBar().isShowing()) {
			y -= scrollPane.getHorizontalScrollBar().getHeight();
		}
		if (hoveringButton == this.hoveringButton) {
			int x = scrollPane.getWidth() - hoveringButton.getWidth();
			if (scrollPane.getVerticalScrollBar().isShowing()) {
				x -= scrollPane.getVerticalScrollBar().getWidth();
			}
			hoveringButton.setLocation(x, y);
		} else {
			hoveringButton.setLocation(scrollPane.getX(), y);
		}
	}
	
	private void initHBMouseListeners(JButton hoveringButton) {
		MouseAdapter val = generateHBMouseAdapter(hoveringButton);
		hoveringButton.addMouseMotionListener(val);
		hoveringButton.addMouseListener(val);
		hoveringButton.addMouseWheelListener(e -> {
			if (ignore(hoveringButton, e)) {
				Component  comp = panel.findComponentAt(hoveringButton.getX() + e.getX() - panel.getX(),
						hoveringButton.getY() + e.getY() - panel.getY());
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
						MouseEvent event = new MouseEvent(btn, e.getID(), e.getWhen(), e.getModifiersEx(),
								hoveringButton.getX() + e.getX() - btn.getX(), hoveringButton.getY() + e.getY() - btn.getY(), e.getXOnScreen(),
								e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
						btn.dispatchEvent(event);
					}
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					Component comp = findComp(hoveringButton, e);
					if (comp instanceof JButton btn) {
						MouseEvent event = new MouseEvent(btn, e.getID(), e.getWhen(), e.getModifiersEx(),
								hoveringButton.getX() + e.getX() - btn.getX(), hoveringButton.getY() + e.getY() - btn.getY(), e.getXOnScreen(),
								e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
						btn.dispatchEvent(event);
					}
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					Component comp = findComp(hoveringButton, e);
					if (comp != lastbtn && lastbtn != null) {
						lastbtn.setBorderPainted(false);
					} else if (comp == lastbtn) {
						if (comp == null) return;
						MouseEvent event = new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiersEx(),
								hoveringButton.getX() + e.getX() - comp.getX(), hoveringButton.getY() + e.getY() - comp.getY(), e.getXOnScreen(),
								e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
						comp.dispatchEvent(event);
						return;
					}
					if (comp == null) return;
					if (comp instanceof JButton btn) {
						btn.setBorderPainted(true);
						lastbtn = btn;
					}
					MouseEvent event = new MouseEvent(comp, MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiersEx(),
							hoveringButton.getX() + e.getX() - comp.getX(), hoveringButton.getY() + e.getY() - comp.getY(), e.getXOnScreen(),
							e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
					comp.dispatchEvent(event); // send mouse enter event, so the button displays the correct border
					// no need to send an mouse leave event (I only let the mouse hover border print
					comp.requestFocusInWindow(Cause.MOUSE_EVENT);// or non border at all be displayed)
				} else if (lastbtn != null) {
					lastbtn.setBorderPainted(false);
					hoveringButton.requestFocusInWindow(Cause.MOUSE_EVENT);
					lastbtn = null;
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mouseMoved(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				lastbtn = null;
				mouseMoved(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					skipAction++;
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
			BufferedImage img        = ImageIO.read(getClass().getResource(switch (world) {
										case RootWorld.Builder b -> {
											if (hoveringButton == this.hoveringButton) {
												b.addNextTurnListener(() -> hoveringButton.setVisible(b.buildable()));
												hoveringButton.setToolTipText("<html>build the world</html>");
												hoveringButton.setVisible(b.buildable());
												yield "/img/BUILD.png";
											} else {
												hoveringButton.setVisible(true);
												yield "/img/BUILD_MODE.png";
											}
										}
										case RootWorld rw -> {
											rw.addNextTurnListener(() -> hoveringButton.setVisible(rw.running()));
											hoveringButton.setVisible(!rw.running());
											hoveringButton.setToolTipText("<html>start the game</html>");
											yield "/img/START_GAME.png";
										}
										default -> {
											hoveringButton.setToolTipText("<html>finish your turn</html>");
											yield "/img/FINISH_TURN.png";
										}
										}));
			int           ftIconSize = hoverButtonSize(tileIconSize);
			ImageIcon     icon0      = new ImageIcon(img.getScaledInstance(ftIconSize, ftIconSize, Image.SCALE_SMOOTH));
			hoveringButton.setIcon(icon0);
			hoveringButton.setDisabledIcon(icon0);
			hoveringButton.addComponentListener(new ComponentAdapter() {
				
				int curw = tileIconSize;
				
				@Override
				public void componentResized(ComponentEvent e) {
					int w   = hoveringButton.getWidth();
					int h   = hoveringButton.getHeight();
					int min = Math.min(w, h);
					if (min == curw) {
						return;
					}
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
		return panel.findComponentAt(hb.getX() + e.getX() - panel.getX(), hb.getY() + e.getY() - panel.getY());
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
	
	private void hoverBuildBtnAction(ActionEvent ignore) {
		if (skipAction > 0) {
			skipAction--;
			return;
		}
		
		if (myBuildMode == null) {
			myBuildMode = new BuildMode();
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
			myBuildMode.makeInactive();
			dialog.dispose();
		});
		btn = new JButton("+normal");
		btn.setToolTipText("<html>change the tile type to a normal tile type (remove hills and make water non-deep)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			myBuildMode.makePlusNormal();
			dialog.dispose();
		});
		btn = new JButton("+hill");
		btn.setToolTipText("<html>add hills to the tile type (do nothing if the type does not support hills)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			myBuildMode.makePlusHill();
			dialog.dispose();
		});
		btn = new JButton("+deep");
		btn.setToolTipText("<html>make (normal) water deep/ocean water (do nothing if the type does not support the deep mode)</html>");
		dp.add(btn);
		btn.addActionListener(e -> {
			myBuildMode.makePlusDeep();
			dialog.dispose();
		});
		String[] vals = new String[TileType.count() + 1];
		for (int i = 0; i < vals.length - 1; i++) { vals[i] = TileType.of(i).toString(); }
		vals[vals.length - 1] = "set the ground";
		JComboBox<String> ttcombo = new JComboBox<>(vals);
		ttcombo.setSelectedIndex(vals.length - 1);
		ttcombo.setEditable(false);
		dp.add(ttcombo);
		ttcombo.addActionListener(e -> {
			if (ttcombo.getSelectedIndex() >= TileType.count()) return;
			myBuildMode.makeSetGround(TileType.of(ttcombo.getSelectedIndex()));
			dialog.dispose();
		});
		vals = new String[OreResourceType.count() + 1];
		for (int i = 0; i < vals.length - 1; i++) { vals[i] = TileType.of(i).toString(); }
		vals[vals.length - 1] = "set the ore";
		JComboBox<String> ortcombo = new JComboBox<>(vals);
		ortcombo.setSelectedIndex(vals.length - 1);
		ortcombo.setEditable(false);
		dp.add(ortcombo);
		ortcombo.addActionListener(e -> {
			if (ortcombo.getSelectedIndex() >= TileType.count()) return;
			myBuildMode.makeSetGround(TileType.of(ortcombo.getSelectedIndex()));
			dialog.dispose();
		});
		initDialog(dialog, false);
	}
	
	private void hoverBtnAction(ActionEvent e) {
		// Well I can't get the position of an action event
		if (skipAction > 0) {
			skipAction--;
			return;
		}
		
		switch (world) {
		case RootWorld rw -> {
			if (connects == null) {
				JOptionPane.showMessageDialog(frame, "you need to start a server and all players need to be connected at game start",
						"no server started", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int chosen = JOptionPane.showConfirmDialog(frame, "start the game", "START", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) {
				return;
			}
			synchronized (this) {
				Map<User, Connection> conns = this.connects;
				byte[]                seed  = new byte[(conns.size() + 1) << 4];
				byte[]                tmp   = new byte[16];
				rw.user().fillRandom(tmp);
				System.arraycopy(tmp, 0, seed, 0, 16);
				int i = 16;
				try {
					List<Connection> list = new ArrayList<>(conns.values());
					Collections.sort(list, (a, b) -> a.usr.name().compareTo(b.usr.name()));
					for (Iterator<Connection> iter = list.iterator(); iter.hasNext(); i += 16) {
						final Connection conn = iter.next();
						try {
							conn.blocked(() -> RootWorld.fillRnd(conn, tmp));
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(frame, "could not get the random value from a client", "start failed",
									JOptionPane.ERROR_MESSAGE);
						}
						System.arraycopy(tmp, 0, seed, i, 16);
					}
					if (i < seed.length) {
						throw new ConcurrentModificationException();
					}
					rw.startGame(seed);
				} catch (ConcurrentModificationException err) {
					JOptionPane.showMessageDialog(frame, "accepted/lost a connection during the initilation", "start failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		case RootWorld.Builder b -> {
			int chosen = JOptionPane.showConfirmDialog(frame, "build the world", "BUILD", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (chosen != JOptionPane.YES_OPTION) {
				return;
			}
			RootWorld rw = b.create();
			world = rw;
			reload(buildFinishHook, true, true);
		}
		case RemoteWorld rw -> threadBuilder().start(this::finishTurn);
		default -> throw new AssertionError("illegal world type: " + world.getClass());
		}
	}
	
	private void finishTurn() throws AssertionError {
		int chosen = JOptionPane.showConfirmDialog(frame, "finish your turn", "finish", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chosen != JOptionPane.YES_OPTION) {
			return;
		}
		Turn t = new Turn(world);
		for (int x = 0; x < turns.length; x++) {
			EntityTurn[] ets = turns[x];
			for (int y = 0; y < ets.length; y++) {
				EntityTurn et = ets[y];
				if (et == null) {
					continue;
				}
				ets[y] = null;
				Tile   tile = world.tile(x, y);
				Entity ett  = switch (et) {
							case CarryTurn val -> tile.unit();
							case MoveTurn val -> tile.unit();
							case StoreTurn val -> tile.unit();
							default -> throw new AssertionError("unknown entity turn type: " + et.getClass());
							};
				t.put(ett, et);
			}
		}
		world.finish(t);
	}
	
	public void visible(boolean b) {
		if (frame == null) {
			throw new IllegalStateException("not yet loaded!");
		}
		if (ensureGUIThread(() -> visible(b))) {
			return;
		}
		if (frame.isVisible() == b) {
			return;
		}
		if (b) {
			System.out.println("set now visible (large worlds may need some time) world size: (xlen=" + world.xlen() + "|ylen=" + world.ylen() + ')');
			frame.setVisible(true);
			update(null);
		} else {
			frame.setVisible(false);
		}
	}
	
	private volatile Thread   updateThread;
	private volatile Runnable updateFinishHook;
	
	private void update(Runnable ufh) {
		if (frame == null) {
			throw new IllegalStateException("not yet loaded!");
		}
		Runnable updateFin = updateFinishHook;
		if (ufh != updateFin) {
			assert ufh == null || updateFin == null;
			return;
		}
		if (updateThread != null) {
			assert ufh == null;
			return;
		}
		if (ensureNotGUIThread(() -> update(ufh))) {
			return;
		}
		synchronized (SquareConquererGUI.this) {
			if (updateThread != null) {
				return;
			}
			updateThread = Thread.currentThread();
		}
		try {
			updateBtns();
			if (ufh != null) {
				System.out.println("update: buttons updated");
			}
			Runnable ufh0;
			synchronized (SquareConquererGUI.this) {
				ufh0 = updateFinishHook;
				if (ufh != null) {
					updateFinishHook = null;
				}
			}
			SwingUtilities.invokeLater(() -> {
				if (frame.isVisible()) { // invalidate and repaint does not work, setVisible does
					frame.setVisible(true);
				}
				if (ufh0 != null) {
					ufh0.run();
				}
			});
		} finally {
			synchronized (SquareConquererGUI.this) {
				updateThread = null;
				SquareConquererGUI.this.notifyAll();
			}
		}
	}
	
	private void updateBtns() {
		int       size = iconSize();
		final int xlen = btns.length;
		final int ylen = btns[0].length;
		for (int x = 0; x < xlen; x++) {
			JButton[] bs = btns[x];
			for (int y = 0; y < ylen; y++) {
				if (Thread.interrupted()) {
					return;
				}
				Tile          t = world.tile(x, y);
				final Icon    i = t.icon(size, size);
				StringBuilder b = new StringBuilder().append("<html>");
				if (t.hasPage()) {
					b.append("page: ").append(t.pageTitle()).append("<br>");
				}
				b.append("ground: ").append(t.type);
				switch (t.resource) {
				case GOLD_ORE -> b.append("<br>resource: Gold Ore");
				case IRON_ORE -> b.append("<br>resource: Iron Ore");
				case COAL_ORE -> b.append("<br>resource: Coal Ore");
				case NONE -> {/**/}
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
		if (btns[0][0].getWidth() > 0 && btns[0][0].getHeight() > 0) {
			size = Math.min(btns[0][0].getWidth(), btns[0][0].getHeight());
		}
		return size;
	}
	
	private void pressed(int x, int y) {
		if (world instanceof RootWorld.Builder b && myBuildMode != null && myBuildMode.isActive()) {
			Tile t = myBuildMode.modify(b.tile(x, y));
			b.set(x, y, t);
			return;
		}
		Tile    t      = world.tile(x, y);
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
		if (world instanceof RootWorld.Builder rb) {
			JComboBox<TileType>        cbt = new JComboBox<>(TileType.values());
			JComboBox<OreResourceType> cbr = new JComboBox<>(OreResourceType.values());
			cbt.setEditable(false);
			cbr.setEditable(false);
			cbt.setSelectedIndex(t.type.ordinal());
			cbr.setSelectedIndex(t.resource.ordinal());
			cbt.addActionListener(e -> rb.set(x, y, TileType.of(cbt.getSelectedIndex())));
			cbr.addActionListener(e -> rb.set(x, y, OreResourceType.of(cbr.getSelectedIndex())));
			dp.add(new JLabel("ground:"));
			dp.add(cbt);
			dp.add(new JLabel("resource:"));
			dp.add(cbr);
		} else {
			dp.add(new JLabel("ground:"));
			dp.add(new JLabel(t.type.toString()));
			dp.add(new JLabel("resource:"));
			dp.add(new JLabel(t.resource.toString()));
		}
		if (!t.visible()) {
			dp.add(new JLabel("visibiel: currently not visible"));
		}
		Building b = t.building();
		dp.add(new JLabel("Building:"));
		if (b != null) {
			switch (b) {
			case StoreBuild sb -> {
				dp.add(new JLabel("Storage"));
				generalBuildingInfo(dp, sb, "    ");
				EnumIntMap<OreResourceType> ores = sb.ores();
				int[]                       arr  = ores.array();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] > 0) {
						dp.add(new JLabel("    " + OreResourceType.of(i) + ": "));
						dp.add(new JLabel(Integer.toString(arr[i])));
					}
				}
				EnumIntMap<ProducableResourceType> producable = sb.producable();
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
			EnumIntMap<ProducableResourceType> res = b.neededResources();
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
			Thread t = threadBuilder().start(r);
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
