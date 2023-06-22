package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.entity.Build;
import de.hechler.patrick.games.sc.world.tile.Tile;

public class WorldDisplay implements ButtonGridMouseListener {
	
	private World      world;
	private ButtonGrid grid;
	private ScrollPane scroll;
	private Frame      frame;
	
	public WorldDisplay(World world) {
		this.world  = world;
		this.grid   = new ButtonGrid(world.xlen(), world.ylen(), 128);
		this.scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		this.frame  = new Frame();
	}
	
	private static final Image MARK_IMG;
	
	static {
		try {
			MARK_IMG = ImageIO.read(WorldDisplay.class.getResource("/img/mark.png"));
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
	
	@Override
	public void moved(MouseEvent e, int x, int y, int subx, int suby, boolean dragging) {
		if (dragging) {
			this.drag = true;
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
	
	private boolean drag;
	private int     x;
	private int     y;
	private int     lx;
	private int     ly;
	
	@Override
	public void pressed(MouseEvent e, int x, int y, int subx, int suby) {
		this.x    = x;
		this.y    = y;
		this.lx   = -1;
		this.ly   = -1;
		this.drag = false;
		System.out.println("pressed x: " + x + " y: " + y);
	}
	
	@Override
	public void released(MouseEvent e, int x, int y, int subx, int suby) {
		int ty = this.y;
		int tx = this.x;
		this.x = -1;
		this.y = -1;
		if (x != tx || y != ty || this.drag) {
			updateBtns();
			return;
		}
		// TODO
	}
	
	@Override
	public void whelling(MouseWheelEvent e, int x, int y, int subx, int suby) {
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
	
	public void init() {
		this.frame.setTitle(this.world.getClass().getSimpleName() + ": (" + this.world.xlen() + '|' + this.world.ylen() + ") : " + this.world.user().name());
		this.scroll.add(this.grid);
		this.frame.add(this.scroll);
		this.frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(@SuppressWarnings("unused") WindowEvent e) {
				System.exit(0);
			}
			
		});
		this.grid.addBGMouseListener(this);
		updateBtns();
		this.frame.setLocationByPlatform(true);
		this.scroll.setPreferredSize(this.grid.getPreferredSize());
		pack(true);
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
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			CompleteWorld.Builder w = new CompleteWorld.Builder(User.nopw("nice user"), 16, 16);
			WorldDisplay          d = new WorldDisplay(w);
			d.init();
		});
	}
	
}
