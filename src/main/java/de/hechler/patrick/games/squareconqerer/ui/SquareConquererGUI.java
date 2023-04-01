package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;

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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.BorderUIResource.CompoundBorderUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import de.hechler.patrick.games.squareconqerer.Settings;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.connect.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.connect.RemoteWorld;

public class SquareConquererGUI {
	
	private World world;
	
	private JFrame      frame;
	private JButton[][] btns;
	
	private final Runnable loadFinishedHook = () -> JOptionPane.showMessageDialog(frame, "finishd loading", "load", JOptionPane.INFORMATION_MESSAGE);
	
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
		
		frame = new JFrame(world.getClass().getSimpleName() + ": " + world.user().name());
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
		frame.setVisible(initialVisible);
	}
	
	private JMenuBar initMenu() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuGeneral());
		if (world instanceof RootWorld) {
			menuBar.add(menuServer());
		}
		frame.setJMenuBar(menuBar);
		return menuBar;
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
			
			JPanel panel = new JPanel();
			dialog.setContentPane(panel);
			panel.setLayout(new GridLayout(3, 2));
			
			panel.add(new JLabel("User:"));
			JComboBox<String> combo = new JComboBox<>();
			panel.add(combo);
			
			panel.add(new JLabel("New Password:"));
			JPasswordField pw = new JPasswordField(16);
			panel.add(pw);
			
			panel.add(new JLabel());
			JButton change = new JButton("change password");
			panel.add(change);
			
			combo.addItem(RootUser.ROOT_NAME);
			RootUser root = ((RootWorld) world).user();
			root.names().forEach(combo::addItem);
			
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
			
			JPanel panel = new JPanel();
			dialog.setContentPane(panel);
			panel.setLayout(new GridLayout(3, 2));
			
			JComboBox<String> combo  = new JComboBox<>();
			JButton           delBtn = new JButton("DELTE USER");
			
			RootUser root = ((RootWorld) world).user();
			root.names().forEach(combo::addItem);
			
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
			
			panel.add(new JLabel("User:"));
			panel.add(combo);
			panel.add(new JLabel());
			panel.add(new JLabel());
			panel.add(new JLabel());
			panel.add(delBtn);
			
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
			
			JPanel panel = new JPanel();
			dialog.setContentPane(panel);
			panel.setLayout(new GridLayout(3, 2));
			
			panel.add(new JLabel("Username:"));
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
			panel.add(userField);
			panel.add(new JLabel("Password:"));
			JPasswordField pw = new JPasswordField(16);
			panel.add(pw);
			panel.add(new JLabel());
			JButton finishBtn = new JButton("add User");
			panel.add(finishBtn);
			
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
			
			JPanel panel = new JPanel();
			dialog.setContentPane(panel);
			panel.setLayout(new GridLayout(3, 2));
			panel.add(new JLabel("Port:"));
			NumberDocument portDoc = new NumberDocument(0x0000, 0xFFFF);
			JTextField     portTxt = new JTextField(5);
			portTxt.setDocument(portDoc);
			portTxt.setText(Integer.toString(Connection.DEFAULT_PORT));
			panel.add(portTxt);
			
			JCheckBox serverPWCB = new JCheckBox("Server Password");
			serverPWCB.setToolTipText("<html>a server password lets remote users create accounts themself<br>"
					+ "It is then no longer needed to add all users manually,<br>"
					+ "but everyone with the server password can create an infinit amount of users</html>");
			panel.add(serverPWCB);
			JPasswordField serverPWPF = new JPasswordField(16);
			panel.add(serverPWPF);
			serverPWPF.setVisible(false);
			serverPWCB.addActionListener(oe -> serverPWPF.setVisible(serverPWCB.isSelected()));
			
			panel.add(new JLabel());
			JButton start = new JButton("start");
			panel.add(start);
			
			start.addActionListener(oe -> {
				try {
					int          port = portDoc.getNumber();
					ServerSocket ss   = new ServerSocket(port);
					serverThread = threadBuilder().start(() -> {
						try {
							RootWorld rw   = (RootWorld) world;
							RootUser  root = rw.user();
							Connection.ServerAccept.accept(ss, root, (conn, sok) -> {
								threadBuilder().start(
										() -> JOptionPane.showMessageDialog(frame, "'" + conn.usr.name() + "' logged in from " + sok.getInetAddress(),
												"remote log in", JOptionPane.INFORMATION_MESSAGE));
								UserWorld userWorld = rw.of(conn.usr, conn.modCnt());
								OpenWorld openWorld = new OpenWorld(conn, userWorld);
								openWorld.execute();
							}, serverPWCB.isSelected() ? serverPWPF.getPassword() : null);
						} catch (IOException err) {
							if (err instanceof ClosedByInterruptException || Thread.interrupted()) {
								return;
							}
							JOptionPane.showMessageDialog(frame, "error: " + err.getMessage(), err.getClass().getSimpleName(),
									JOptionPane.ERROR_MESSAGE);
						}
					});
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
		initMenuGeneralSave(generalMenu, fc);
		initMenuGeneralLoad(generalMenu, fc);
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
	
	private void initMenuGeneralLoad(JMenu generalMenu, JFileChooser fc) {
		JMenuItem loadItem = new JMenuItem("Load");
		loadItem.addActionListener(e -> {
			int result = fc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				threadBuilder().start(() -> {
					User usr = world.user();
					try (FileInputStream in = new FileInputStream(file); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr)) {
						RootUser root = usr.rootClone();
						Tile[][] tiles;
						try {
							root.load(conn);
							tiles = RemoteWorld.readWorld(conn);
						} catch (Throwable t) {
							root.close();
							throw t;
						}
						usr.close();
						synchronized (SquareConquererGUI.this) {
							while (updateThread != null) {
								if (!updateThread.isAlive()) {
									System.err.println("update thread is dead");
									updateThread     = null;
									updateFinishHook = null;
									break;
								}
								updateThread.interrupt();
							}
							updateFinishHook = loadFinishedHook;
							RootWorld rw = RootWorld.Builder.create(root, tiles);
							this.world = rw;
						}
						SwingUtilities.invokeLater(() -> reload(loadFinishedHook, true));
					} catch (IOException | RuntimeException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame, e1.toString(), e1.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		});
		generalMenu.add(loadItem);
	}
	
	private void initMenuGeneralSave(JMenu generalMenu, JFileChooser fc) {
		// only root can see the full world and thus save it
		if (!(world instanceof RootWorld rw)) {
			return;
		}
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(e -> {
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
						rw.user().save(conn);
						OpenWorld.sendWorld(rw, conn, false);
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
		
		JMenuBar  menu  = initMenu();
		JPanel    panel = new JPanel();
		int       mul   = Settings.iconSize();
		int       xlen  = world.xlen();
		int       ylen  = world.ylen();
		Dimension d     = new Dimension(mul, mul);
		panel.setLayout(new GridLayout(xlen, ylen, 0, 0));
		panel.setPreferredSize(new Dimension(xlen * mul, ylen * mul));
		panel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		frame.setContentPane(scrollPane);
		btns = new JButton[xlen][ylen];
		for (int y = 0; y < ylen; y++) {
			for (int x = 0; x < xlen; x++) {
				final JButton btn = new JButton();
				btns[x][y] = btn;
				panel.add(btn);
				btn.setPreferredSize(d);
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
						btn.setBorderPainted(true);
					}
					
				});
			}
		}
		btns[0][0].addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				update();
			}
			
		});
		if (!reaload) {
			frame.setLocationByPlatform(true);
		}
		frame.pack();
		Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
		Dimension dim    = new Dimension(Math.min(frame.getWidth(), bounds.width), Math.min(frame.getHeight(), bounds.height));
		frame.setSize(dim);
		if (!(world.user() instanceof RootUser)) {
			JButton finishTurn;
			try {
				BufferedImage img   = ImageIO.read(getClass().getResource("/img/FINISH_TURN.png"));
				ImageIcon     icon0 = new ImageIcon(img.getScaledInstance((mul >>> 1) + (mul >>> 2), (mul >>> 1) + (mul >>> 2), Image.SCALE_SMOOTH));
				final JButton ft    = new JButton(icon0);
				ft.setDisabledIcon(icon0);
				ft.addComponentListener(new ComponentAdapter() {
					
					int curw = mul;
					
					@Override
					public void componentResized(ComponentEvent e) {
						int w   = ft.getWidth();
						int h   = ft.getHeight();
						int min = Math.min(w, h);
						if (min == curw) {
							return;
						}
						ImageIcon icon = new ImageIcon(img.getScaledInstance(min, min, Image.SCALE_SMOOTH));
						ft.setIcon(icon);
						ft.setDisabledIcon(icon);
					}
					
				});
				ft.setSize((mul >>> 1) + (mul >>> 2), (mul >>> 1) + (mul >>> 2));
				finishTurn = ft;
			} catch (IOException e) {
				e.printStackTrace();
				finishTurn = new JButton("FINISH TURN");
			}
			final JButton ft = finishTurn;
			JPanel        p  = new JPanel();
			p.setLayout(null);
			p.add(ft);
			frame.setGlassPane(p);
			p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
			p.setVisible(true);
			p.setOpaque(false);
			ft.setOpaque(false);
			ft.setFocusPainted(false);
			ft.setBorderPainted(false);
			ft.setContentAreaFilled(false);
			ft.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			scrollPane.addComponentListener(new ComponentAdapter() {
				
				@Override
				public void componentResized(ComponentEvent e) {
					p.setBounds(0, 0, scrollPane.getWidth(), scrollPane.getHeight() + menu.getHeight());
					ft.setLocation(scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth() - ft.getWidth(),
							menu.getHeight() + scrollPane.getHeight() - scrollPane.getHorizontalScrollBar().getHeight() - ft.getHeight());
				}
				
			});
			MouseAdapter val = new MouseAdapter() {
				
				JButton lastbtn;
				
				@Override
				public void mouseMoved(MouseEvent e) {
					if (ignore(ft, e)) {
						Component comp = panel.findComponentAt(ft.getX() + e.getX(), ft.getY() + e.getY() - menu.getHeight());
						if (comp != lastbtn && lastbtn != null) {
							lastbtn.setBorderPainted(false);
						} else if (comp == lastbtn) {
							return;
						}
						if (comp instanceof JButton btn) {
							lastbtn = btn;
							btn.setBorderPainted(true);
							MouseEvent event = new MouseEvent(btn, MouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiersEx(),
									ft.getX() + e.getX() - btn.getX(), ft.getY() + e.getY() - btn.getY(), e.getXOnScreen(), e.getYOnScreen(),
									e.getClickCount(), e.isPopupTrigger(), e.getButton());
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
				
			};
			ft.addMouseMotionListener(val);
			ft.addMouseListener(val);
			btns[0][0].addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseEntered(MouseEvent e) {
					System.out.println("mouse event");
					SwingUtilities.invokeLater(() -> {
						System.out.println(btns[0][0].getBorder().getClass());
						System.out.println(btns[0][0].getBorder());
						javax.swing.plaf.BorderUIResource.CompoundBorderUIResource res = (CompoundBorderUIResource) btns[0][0].getBorder();
						System.out.println(res.getInsideBorder());
					});
					UIDefaults table        = UIManager.getLookAndFeelDefaults();
					Border     buttonBorder = new BorderUIResource.CompoundBorderUIResource(
							new BasicBorders.ButtonBorder(table.getColor("Button.shadow"), table.getColor("Button.darkShadow"),
									table.getColor("Button.light"), table.getColor("Button.highlight")),
							new MarginBorder());
					System.out.println(buttonBorder);
				}
				
			});
			ft.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseClicked(MouseEvent e) {
					finishMouseClick(panel, ft, e);
				}
				
			});
			ft.addActionListener(this::finishAction);
		}
		update(ufh);
	}
	
	private boolean skipAction;
	
	private void finishMouseClick(JPanel panel, JButton ft, MouseEvent e) {
		if (ignore(ft, e)) {
			Component comp = panel.findComponentAt(ft.getX() + e.getX(), ft.getY() + e.getY() - frame.getJMenuBar().getHeight());
			if (comp instanceof JButton btn) {
				btn.doClick(1);
				skipAction = true;
			}
		}
	}
	
	private static boolean ignore(JButton ft, MouseEvent e) {
		int    midx  = ft.getWidth() >>> 1;
		int    midy  = ft.getHeight() >>> 1;
		int    x     = e.getX();
		int    y     = e.getY();
		int    diffx = Math.abs(midx - x);
		int    diffy = Math.abs(midy - y);
		double dist  = Math.sqrt(diffx * diffx + diffy * diffy);
		return dist > (62d * 64d) / (Math.min(ft.getWidth(), ft.getHeight()));
	}
	
	private void finishAction(ActionEvent e) {
		if (skipAction) {
			skipAction = false;
			return;
		}
		int chosen = JOptionPane.showConfirmDialog(frame, "finish your turn", "finish", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chosen != JOptionPane.YES_OPTION) {
			return;
		}
		// TODO finish turn
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
			frame.setVisible(true);
			update();
		} else {
			frame.setVisible(false);
		}
	}
	
	private volatile Thread   updateThread;
	private volatile Runnable updateFinishHook;
	
	public void update() {
		update(null);
	}
	
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
		int       size = iconSize();
		final int xlen = btns.length;
		final int ylen = btns[0].length;
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
		System.out.println("pressed: " + x + " " + y);
		Tile t    = world.tile(x, y);
		int  size = iconSize();
		Icon icon = t.icon(size, size);
		btns[x][y].setIcon(icon);
	}
	
	private static boolean ensureGUIThread(Runnable r) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
				return true;
			} catch (InvocationTargetException e) {
				Throwable c = e.getCause();
				if (c instanceof RuntimeException re) {
					throw re;
				}
				if (c instanceof Error err) {
					throw err;
				}
				throw new AssertionError(e);
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
		}
		return false;
	}
	
	private static boolean ensureNotGUIThread(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			threadBuilder().start(r);
			return true;
		}
		return false;
	}
	
}
