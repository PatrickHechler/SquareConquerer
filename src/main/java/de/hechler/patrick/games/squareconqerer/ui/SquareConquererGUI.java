package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.awt.Component;
import java.awt.ComponentOrientation;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import de.hechler.patrick.games.squareconqerer.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.Settings;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
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
	private JButton[][]    btns;
	private EntityTurn[][] turns;
	
	private volatile Map<User, Connection> connects;
	
	private final Runnable loadFinishedHook = () -> {
		if (world instanceof RootWorld) {
			JOptionPane.showMessageDialog(frame, "finishd loading", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is in build mode", "load", JOptionPane.INFORMATION_MESSAGE);
		} else if (world != null) {
			JOptionPane.showMessageDialog(frame, "finishd loading, the loaded world is of type: " + world.getClass().getSimpleName(), "load",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(frame, "could not load for some reason", "load failed", JOptionPane.ERROR_MESSAGE);
		}
	};
	
	private final Runnable buildFinishHook = () -> {
		if (world instanceof RootWorld.Builder) {
			JOptionPane.showMessageDialog(frame, "converting to build mode finished", "converted to build", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(frame, "the world build operation is now finish", "finished build", JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Thread serverThread;
	
	public SquareConquererGUI(World world) {
		if (world == null) {
			throw new NullPointerException("world is null");
		}
		this.world = world;
	}

	public void load(boolean initialVisible, Thread t) {
		if (frame != null) {
			throw new IllegalStateException("already loaded");
		} // I don't need a second method for that
		if (ensureGUIThread(() -> load(initialVisible))) {
			return;
		}
		serverThread = t;
		load(initialVisible);
	}
	
	public void load(boolean initialVisible) {
		if (frame != null) {
			throw new IllegalStateException("already loaded");
		} // I don't need a second method for that
		if (ensureGUIThread(() -> load(initialVisible))) {
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
		
		reload(null, false);
		world.addNextTurnListener(() -> update(null));
		
		panel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) == 0 || e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					JScrollBar sb;
					if ((e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) == 0) {
						sb = scrollPane.getHorizontalScrollBar();
					} else {
						sb = scrollPane.getVerticalScrollBar();
					}
					// MouseEvent me = SwingUtilities.convertMouseEvent(e.getSource() instanceof
					// Component c ? c : panel, e, sb);
					sb.dispatchEvent(e);
					return;
				}
				e.consume();
				int amnt = e.getUnitsToScroll();
				int w    = Settings.iconSize();
				for (; amnt > 0; amnt--) {
					w = (int) (w / 1.125D);
				}
				for (; amnt < 0; amnt++) {
					w = (int) (w * 1.125D);
				}
				Settings.iconSize(Settings.between(16, w, 1024));
				int       is  = Settings.iconSize();
				Dimension dim = new Dimension(is * btns.length, is * btns[0].length);
				panel.setPreferredSize(dim);
				resizeFrame(false, is, dim);
			}
			
		});
		
		if (initialVisible) {
			System.out.println("set now visible (large worlds may need some time) world size: (xlen=" + world.xlen() + "|ylen=" + world.ylen() + ')');
			long start = System.currentTimeMillis();
			frame.setVisible(true);
			long end = System.currentTimeMillis();
			System.out.println("needed " + (end - start) + " milliseconds to set the frame visible (" + (end - start) / 60000 + ")");
		} else {
			frame.setVisible(false);
		}
	}
	
	private void initMenu() {
		menu = new JMenuBar();
		menu.add(menuGeneral());
		if (world instanceof RootWorld) {
			menu.add(menuServer());
		}
		menu.add(menuBuild());
		frame.setJMenuBar(menu);
	}
	
	private JMenu menuBuild() {
		JMenu buildMenu = new JMenu("Build");
		if (world instanceof RootWorld.Builder) {
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
		} else {
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
						JOptionPane.showMessageDialog(frame,
								"error while closing remote world (do not retry, I will proceed anyway): " + e.toString(), "error: " + e.getClass(),
								JOptionPane.ERROR_MESSAGE);
					}
				}
				oldUsr.close();
				world = b;
				reload(buildFinishHook, true);
				world.addNextTurnListener(() -> update(null));
			});
			buildMenu.add(toBuild);
		}
		return buildMenu;
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
			JDialog dialog = new JDialog(frame);
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
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
			
			dialog.pack();
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
		};
	}
	
	private ActionListener menuServerDeleteListener() {
		return e -> {
			JDialog dialog = new JDialog(frame);
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
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
			
			dialog.pack();
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
		};
	}
	
	private ActionListener menuServerAddListener() {
		return e -> {
			JDialog dialog = new JDialog(frame);
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
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
			
			dialog.pack();
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
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
			JDialog dialog = new JDialog(frame);
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
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
			
			dialog.pack();
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
		};
	}
	
	private JMenu menuGeneral() {
		JMenu        generalMenu = new JMenu("General");
		JFileChooser fc          = new JFileChooser();
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
					updateFinishHook = loadFinishedHook;
					this.world       = w;
					world.addNextTurnListener(() -> update(null));
				}
				SwingUtilities.invokeLater(() -> reload(loadFinishedHook, true));
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
				updateFinishHook = loadFinishedHook;
				try {
					this.world = RootWorld.Builder.create(root, tiles);
				} catch (IllegalStateException err) {
					this.world = RootWorld.Builder.createBuilder(root, tiles);
				}
				world.addNextTurnListener(() -> update(null));
			}
			SwingUtilities.invokeLater(() -> reload(loadFinishedHook, true));
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initMenuGeneralSave(JMenu generalMenu, JFileChooser fc, boolean saveAll) {
		if (saveAll && !(world instanceof RootWorld)) return;
		JMenuItem saveItem = new JMenuItem(saveAll ? "Save Everything" : "Save");
		saveItem.addActionListener(e -> {
			if (!(world instanceof RootWorld)) {
				if (saveAll) {
					JOptionPane.showMessageDialog(frame, "the save everything buton should not exist, only root worlds can save everything",
							"ERROR", JOptionPane.ERROR_MESSAGE);
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
		});
		generalMenu.add(saveItem);
	}
	
	private void reload(Runnable ufh, boolean reaload) {
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		frame.setTitle(world.getClass().getSimpleName() + ": " + world.user().name());
		
		initMenu();
		int xlen     = world.xlen();
		int ylen     = world.ylen();
		int iconSize = Settings.iconSize();
		panel = new JPanel();
		panel.setLayout(new GridLayout(ylen, xlen, 0, 0));
		panel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		Dimension panelDim = new Dimension(xlen * iconSize, ylen * iconSize);
		panel.setPreferredSize(panelDim);
		scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		frame.setContentPane(scrollPane);
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
				btn.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseExited(MouseEvent e) {
						btn.setBorderPainted(false);
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						btn.requestFocusInWindow(Cause.MOUSE_EVENT);
						btn.setBorderPainted(true);
					}
					
				});
			}
		}
		btns[0][0].addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				update(null);
			}
			
		});
		if (!reaload) {
			frame.setLocationByPlatform(true);
		}
		resizeFrame(reaload, iconSize, panelDim);
		update(ufh);
	}
	
	private void resizeFrame(boolean reaload, int tileSize, Dimension panelDim) {
		// the frame.pack() method is just to slow for large worlds
		GraphicsConfiguration conf    = frame.getGraphicsConfiguration();
		Rectangle             bounds  = conf.getBounds();
		Insets                insets  = frame.getInsets();
		Dimension             menuDim = menu.getPreferredSize();
		int                   w       = Math.min(Math.max(panelDim.width, menuDim.width) + insets.left + insets.right, bounds.width);
		int                   h       = Math.min(panelDim.height + menuDim.height + insets.bottom + insets.top, bounds.height);
		if (!reaload && (w != bounds.width || h != bounds.height)) { // small world, do pack (getInsets before pack or visible is useless)
			frame.pack();
			w = Math.min(frame.getWidth(), bounds.width);
			h = Math.min(frame.getHeight(), bounds.height);
		}
		frame.setSize(w, h);
		if (!reaload) {
			addHoveringBtn(tileSize);
		}
	}
	
	private void addHoveringBtn(int tileIconSize) {
		JButton hoveringButton = new JButton();
		try {
			String        resPath    = switch (world) {
										case RootWorld.Builder b when b.buildable() -> {
											hoveringButton.setToolTipText("<html>build the world</html>");
											yield "/img/BUILD.png";
										}
										case RootWorld.Builder b -> {
											b.addNextTurnListener(() -> hoveringButton.setVisible(b.buildable()));
											hoveringButton.setToolTipText("<html>build the world</html>");
											hoveringButton.setVisible(false);
											yield "/img/BUILD.png";
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
										};
			BufferedImage img        = ImageIO.read(getClass().getResource(resPath));
			int           ftIconSize = (tileIconSize >>> 1) + (tileIconSize >>> 2);                                     // 3/4 tileSize
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
					hoveringButton.setDisabledIcon(icon);
				}
				
			});
			hoveringButton.setSize(ftIconSize, ftIconSize);
		} catch (IOException e) {
			throw new IOError(e);
		}
		JPanel p = new JPanel();
		p.setLayout(null);
		p.add(hoveringButton);
		frame.setGlassPane(p);
		p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
		p.setVisible(true);
		p.setOpaque(false);
		hoveringButton.setOpaque(false);
		hoveringButton.setFocusPainted(false);
		hoveringButton.setBorderPainted(false);
		hoveringButton.setContentAreaFilled(false);
		hoveringButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		hoveringButton.setLocation(scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth() - hoveringButton.getWidth(),
				menu.getHeight() + scrollPane.getHeight() - scrollPane.getHorizontalScrollBar().getHeight() - hoveringButton.getHeight());
		scrollPane.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
				hoveringButton.setLocation(scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth() - hoveringButton.getWidth(),
						menu.getHeight() + scrollPane.getHeight() - scrollPane.getHorizontalScrollBar().getHeight() - hoveringButton.getHeight());
			}
			
		});
		
		MouseAdapter val = new MouseAdapter() {
			
			JButton lastbtn;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					skipAction = System.currentTimeMillis();
					Component comp = panel.findComponentAt(hoveringButton.getX() + e.getX(), hoveringButton.getY() + e.getY() - menu.getHeight());
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
					Component comp = panel.findComponentAt(hoveringButton.getX() + e.getX(), hoveringButton.getY() + e.getY() - menu.getHeight());
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
					Component comp = panel.findComponentAt(hoveringButton.getX() + e.getX(), hoveringButton.getY() + e.getY() - menu.getHeight());
					if (comp != lastbtn && lastbtn != null) {
						lastbtn.setBorderPainted(false);
					} else if (comp == lastbtn) {
						MouseEvent event = new MouseEvent(lastbtn, e.getID(), e.getWhen(), e.getModifiersEx(),
								hoveringButton.getX() + e.getX() - lastbtn.getX(), hoveringButton.getY() + e.getY() - lastbtn.getY(),
								e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
						lastbtn.dispatchEvent(event);
						return;
					}
					if (comp instanceof JButton btn) {
						lastbtn = btn;
						btn.requestFocusInWindow(Cause.MOUSE_EVENT);
						btn.setBorderPainted(true);
						MouseEvent event = new MouseEvent(btn, MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiersEx(),
								hoveringButton.getX() + e.getX() - btn.getX(), hoveringButton.getY() + e.getY() - btn.getY(), e.getXOnScreen(),
								e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
						btn.dispatchEvent(event); // send mouse enter event, so the button displays the correct border
					} // no need to send an mouse leave event (I only let the mouse hover border print
				} else if (lastbtn != null) { // or non border at all)
					lastbtn.setBorderPainted(false);
					lastbtn = null;
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (lastbtn != null) {
					lastbtn.setBorderPainted(false);
					lastbtn = null;
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				lastbtn = null;
				mouseMoved(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (ignore(hoveringButton, e)) {
					skipAction = System.currentTimeMillis();
				}
			}
			
		};
		hoveringButton.addMouseMotionListener(val);
		hoveringButton.addMouseListener(val);
		hoveringButton.addActionListener(this::hoverBtnAction);
	}
	
	private long skipAction;
	
	private static boolean ignore(JButton ft, MouseEvent e) {
		int    midx  = ft.getWidth() >>> 1;
		int    midy  = ft.getHeight() >>> 1;
		int    x     = e.getX();
		int    y     = e.getY();
		int    diffx = Math.abs(midx - x);
		int    diffy = Math.abs(midy - y);
		double dist  = Math.sqrt(diffx * diffx + (double) diffy * diffy);
		return dist > (62d * 64d) / (Math.min(ft.getWidth(), ft.getHeight()));
	}
	
	private void hoverBtnAction(ActionEvent e) {
		if (e.getWhen() - skipAction < 250L) {
			return;
		}
		
		switch (world) {
		case RootWorld rw -> {
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
					ArrayList<Connection> list = new ArrayList<Connection>(conns.values());
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
			world = b.create();
			reload(buildFinishHook, true);
			world.addNextTurnListener(() -> update(null));
		}
		case RemoteWorld rw -> threadBuilder().start(() -> finishTurn());
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
		if (ufh != updateFinishHook) {
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
			SwingUtilities.invokeLater(() -> {
				if (frame.isVisible()) {
					frame.invalidate();
				}
				Runnable ufh0 = updateFinishHook;
				if (ufh0 != null) {
					ufh0.run();
				}
			});
		} finally {
			synchronized (SquareConquererGUI.this) {
				updateThread     = null;
				updateFinishHook = null;
				SquareConquererGUI.this.notifyAll();
			}
		}
	}
	
	private void updateBtns() {
		int             size = iconSize();
		final int       xlen = btns.length;
		final int       ylen = btns[0].length;
		final Dimension dim  = new Dimension(size, size);
		for (int y = 0; y < ylen; y++) {
			for (int x = 0; x < xlen; x++) {
				if (Thread.interrupted()) {
					return;
				}
				Tile          t    = world.tile(x, y);
				final Icon    icon = t.icon(size, size);
				StringBuilder b    = new StringBuilder();
				b.append("<html>ground: ").append(t.type);
				switch (t.resource) {
				case GOLD_ORE -> b.append("<br>resource: Gold Ore");
				case IRON_ORE -> b.append("<br>resource: Iron Ore");
				case COAL_ORE -> b.append("<br>resource: Coal Ore");
				case NONE -> {/**/}
				default -> throw new AssertionError(t.resource.name());
				}
				final JButton btn     = btns[x][y];
				final String  toolTip = b.append("</html>").toString();
				SwingUtilities.invokeLater(() -> {
					btn.setMinimumSize(dim);
					btn.setIcon(icon);
					btn.setToolTipText(toolTip);
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
		Tile    t      = world.tile(x, y);
		JDialog dialog = new JDialog();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("Tile at (" + x + '|' + y + ')');
		JPanel p = new JPanel();
		dialog.setContentPane(p);
		p.setLayout(new GridLayout(0, 2));
		
		if (world instanceof RootWorld.Builder rb) {
			JComboBox<TileType>        cbt = new JComboBox<>(TileType.values());
			JComboBox<OreResourceType> cbr = new JComboBox<>(OreResourceType.values());
			cbt.setEditable(false);
			cbr.setEditable(false);
			cbt.setSelectedIndex(t.type.ordinal());
			cbr.setSelectedIndex(t.resource.ordinal());
			cbt.addActionListener(e -> rb.set(x, y, TileType.of(cbt.getSelectedIndex())));
			cbr.addActionListener(e -> rb.set(x, y, OreResourceType.of(cbr.getSelectedIndex())));
			p.add(new JLabel("ground:"));
			p.add(cbt);
			p.add(new JLabel("resource:"));
			p.add(cbr);
		} else {
			p.add(new JLabel("ground:"));
			p.add(new JLabel(t.type.toString()));
			p.add(new JLabel("resource:"));
			p.add(new JLabel(t.resource.toString()));
		}
		if (!t.visible()) {
			p.add(new JLabel("visibiel: currently not visible"));
		}
		Building b = t.building();
		p.add(new JLabel("Building:"));
		final JComboBox<String> build;
		if (world instanceof RootWorld.Builder) {
			build = new JComboBox<>(new String[] { "none", "Storage" });
			build.setEditable(false);
			p.add(build);
		} else build = null;
		if (b != null) {
			switch (b) {
			case StoreBuild sb -> {
				if (build != null) {
					build.setSelectedItem("Storage");
				} else {
					p.add(new JLabel("Storage"));
				}
				generalBuildingInfo(p, sb, "    ");
				EnumIntMap<OreResourceType> ores = sb.ores();
				int[]                       arr  = ores.array();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] > 0) {
						p.add(new JLabel("    " + OreResourceType.of(i) + ": "));
						p.add(new JLabel(Integer.toString(arr[i])));
					}
				}
				EnumIntMap<ProducableResourceType> producable = sb.producable();
				arr = producable.array();
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] > 0) {
						p.add(new JLabel("    " + ProducableResourceType.of(i) + ": "));
						p.add(new JLabel(Integer.toString(arr[i])));
					}
				}
			}
			default -> throw new AssertionError("unknown build type: " + b.getClass());
			}
		} else if (build != null) {
			build.setSelectedItem("none");
		} else {
			p.add(new JLabel("none"));
		}
		build.addActionListener(e -> {
			switch ((String) build.getSelectedItem()) {
			case "none" -> ((RootWorld.Builder) world).set(x, y, (Building) null);
			case "Storage" -> {
				RootUser          root  = (RootUser) world.user();
				Map<String, User> users = root.users();
				Object            value = JOptionPane.showInputDialog(dialog, "select the owner", "store owner", JOptionPane.QUESTION_MESSAGE, null,
						users.keySet().toArray(), RootUser.ROOT_NAME);
				if (value != null) {
					User usr = users.get((String) value);
					((RootWorld.Builder) world).set(x, y, new StoreBuild(x, y, usr));
				}
			}
			default -> throw new AssertionError("unknown selected item: " + build.getSelectedItem());
			}
		});
		Unit u = t.unit();
		p.add(new JLabel("Unit:"));
		if (u != null) {
			switch (u) {
			case Carrier c -> {
				p.add(new JLabel("Carrier"));
				generalUnitInfo(p, c, "    ");
			}
			default -> throw new AssertionError("unknown unit type: " + u.getClass());
			}
		} else {
			p.add(new JLabel("none"));
		}
		
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
		
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
