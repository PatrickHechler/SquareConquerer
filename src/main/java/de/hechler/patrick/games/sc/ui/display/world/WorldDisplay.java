package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.OpenWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.tile.Tile;

public class WorldDisplay implements ButtonGridListener {
	
	private World      world;
	private ButtonGrid grid;
	private ScrollPane scroll;
	private JFrame     frame;
	
	public WorldDisplay(World world) {
		this.world  = world;
		this.grid   = new ButtonGrid(world.xlen(), world.ylen(), 128);
		this.scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		this.frame  = new JFrame();
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
	private static final int MODE_DRAG        = 2;
	private static final int MODE_SINGLE_MARK = 3;
	
	private int mode;
	private int x;
	private int y;
	private int lx;
	private int ly;
	
	private void execDragAct() {
		System.out.println("exec mark: x: " + this.x + " y: " + this.y + " lx: " + this.lx + " ly: " + this.ly);
	}
	
	private void execTileAct() {
		System.out.println("exec: x: " + this.x + " y: " + this.y);
		
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
		case KeyEvent.VK_UP -> {
			prevY = this.y;
			prevX = this.x;
			if (prevY <= 0) {
				return;
			}
			this.y = prevY - 1;
		}
		case KeyEvent.VK_DOWN -> {
			prevY = this.y;
			prevX = this.x;
			if (prevY + 1 >= this.world.ylen()) {
				return;
			}
			this.y = prevY + 1;
		}
		case KeyEvent.VK_LEFT -> {
			prevY = this.y;
			prevX = this.x;
			if (prevX <= 0) {
				return;
			}
			this.x = prevX - 1;
		}
		case KeyEvent.VK_RIGHT -> {
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
			e.setKeyChar('\n');
			e.setKeyCode(KeyEvent.VK_ENTER);
			this.frame.getMenuBar().getHelpMenu().getItem(0).dispatchEvent(e);
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
			this.mode = MODE_DRAG;
			if (x == this.lx && y == this.ly) {
				return;
			}
			int mul = this.grid.getButtonSize();
			updateBtns();
			for (int lx = Math.min(x, this.x); lx <= x || lx <= this.x; lx++) {
				for (int ly = Math.min(y, this.y); ly <= y || ly <= this.y; ly++) {
					this.grid.paintBtn(lx, ly, MARK_IMG);
				}
			}
			this.grid.repaint(Math.min(x, this.x) * mul, Math.min(y, this.y) * mul, Math.abs(x - this.x) * mul, Math.abs(x - this.x) * mul);
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
		} else if (this.mode == MODE_DRAG) {
			updateBtns();
		}
		this.x    = x;
		this.y    = y;
		this.lx   = -1;
		this.ly   = -1;
		this.mode = MODE_PRESS;
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void mReleased(MouseEvent e, int x, int y, int subx, int suby) {
		int ty = this.y;
		int tx = this.x;
		if (x != tx || y != ty || this.mode == MODE_DRAG) {
			execDragAct();
			this.mode = MODE_NONE;
			updateBtns();
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
				System.exit(0);
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
		MenuBar mb = new MenuBar();
		mb.setHelpMenu(menuHelp());
		mb.add(menuBuild());
		mb.add(menuServer());
		mb.add(menuSave());
		this.frame.setMenuBar(mb);
		updateBtns();
		this.frame.setLocationByPlatform(true);
		this.scroll.setPreferredSize(this.grid.getPreferredSize());
		pack(firstCall);
	}
	
	private Menu menuSave() {
		Menu     m    = new Menu("Saves");
		MenuItem load = new MenuItem("load");
		m.add(load);
		load.addActionListener(e -> {
			try {
				simpleLoad: {
					int c = JOptionPane.showConfirmDialog(this.frame, "does the save also contain the users and everything else?", "load everything?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					switch (c) {
					case JOptionPane.YES_OPTION:
						break;
					case JOptionPane.NO_OPTION:
						break simpleLoad;
					case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION:
						return;
					default:
						System.err.println("unknown return value from JOptPane: " + c);
						return;
					}
					File file = new File("./complete-saves/");
					if (!file.exists()) {
						file.mkdir();
					}
					JFileChooser fc = new JFileChooser(file);
					fc.setMultiSelectionEnabled(false);
					int val = fc.showSaveDialog(this.frame);
					if (val != JFileChooser.APPROVE_OPTION) return;
					file = fc.getSelectedFile();
					CompleteWorld cw;
					User          usr     = this.world.user();
					boolean       wasRoot = usr.isRoot();
					User          root    = usr;
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
				}
				File file = new File("./saves/");
				if (!file.exists()) {
					file.mkdir();
				}
				JFileChooser fc = new JFileChooser(file);
				fc.setMultiSelectionEnabled(false);
				int val = fc.showSaveDialog(this.frame);
				if (val != JFileChooser.APPROVE_OPTION) return;
				file = fc.getSelectedFile();
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
		});
		MenuItem save = new MenuItem("save", new MenuShortcut(KeyEvent.VK_S, false));
		m.add(save);
		save.addActionListener(e -> {
			try {
				simpleSave: if (this.world instanceof CompleteWorld cw) {
					int c = JOptionPane.showConfirmDialog(this.frame, "should the save also contain the users and everything else?", "save everything?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					switch (c) {
					case JOptionPane.YES_OPTION:
						break;
					case JOptionPane.NO_OPTION:
						break simpleSave;
					case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION:
						return;
					default:
						System.err.println("unknown return value from JOptPane: " + c);
						return;
					}
					File file = new File("./complete-saves/");
					if (!file.exists()) {
						file.mkdir();
					}
					JFileChooser fc = new JFileChooser(file);
					fc.setMultiSelectionEnabled(false);
					int val = fc.showSaveDialog(this.frame);
					if (val != JFileChooser.APPROVE_OPTION) return;
					file = fc.getSelectedFile();
					try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), this.world.user(), this.world)) {
						cw.saveEverything(conn);
					}
					return;
				} else {
					int c = JOptionPane.showConfirmDialog(this.frame, "the save may not contain all tiles, save anyway?", "no complete world",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (c != JOptionPane.OK_OPTION) {
						return;
					}
				}
				File file = new File("./saves/");
				if (!file.exists()) {
					file.mkdir();
				}
				JFileChooser fc = new JFileChooser(file);
				fc.setMultiSelectionEnabled(false);
				int val = fc.showSaveDialog(this.frame);
				if (val != JFileChooser.APPROVE_OPTION) return;
				file = fc.getSelectedFile();
				try (Connection conn = Connection.OneWayAccept.acceptReadOnly(new FileInputStream(file), this.world.user(), this.world)) {
					OpenWorld.saveWorld(this.world, conn);
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this.frame, ioe.toString(), "io error on save", JOptionPane.ERROR_MESSAGE);
			}
		});
		return m;
	}
	
	private Menu menuServer() {
		Menu m = new Menu("Server");
		if (this.world instanceof CompleteWorld) {
			
		}
		// TODO
		return m;
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
					for (int x = 0, xlen = this.world.xlen(); x < xlen; x++) {
						for (int y = 0, ylen = this.world.ylen(); y < ylen; y++) {
							Tile t = this.world.tile(x, y);
							b.setTile(x, y, t);
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
	
	private Menu menuHelp() {
		Menu         m            = new Menu("Help");
		MenuShortcut helpShortCut = new MenuShortcut(KeyEvent.VK_F1);
		MenuItem     help         = new MenuItem("HELP ME!", helpShortCut);
		m.add(help);
		help.addActionListener(
			e -> JOptionPane.showMessageDialog(this.frame, "there is no help, good luck", "well, you are on your own", JOptionPane.INFORMATION_MESSAGE));
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
		for (int x = 0, xlen = this.world.xlen(); x < xlen; x++) {
			for (int y = 0, ylen = this.world.ylen(); y < ylen; y++) {
				updateBtn(img, g, x, y, bsize);
			}
		}
		g.dispose();
		this.grid.repaint();
	}
	
	private void updateBtn(BufferedImage img, Graphics2D g, int x, int y, int bsize) {
		Tile t = this.world.tile(x, y);
		g.drawImage(t.ground().image(bsize, bsize), 0, 0, bsize, bsize, null);
		Build b = t.build();
		if (b != null) {
			g.drawImage(b.image(bsize, bsize), 0, 0, bsize, bsize, null);
		}
		t.resourcesStream().forEach(consumer(bsize, g, t, t.resourceCount()));
		t.unitsStream().forEach(consumer(bsize, g, t, t.unitCount()));
		this.grid.paintBtn(x, y, img);
	}
	
	private static <T extends WorldThing<?, ?>> Consumer<T> consumer(final int bsize, final Graphics2D g, final Tile t, int thingCount) {
		final boolean manyRes = t.resourceCount() > 1;
		final int     size    = (int) Math.sqrt((bsize * bsize) / (double) thingCount);
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
