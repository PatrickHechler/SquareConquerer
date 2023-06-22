package de.hechler.patrick.games.sc.ui.display.world;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import de.hechler.patrick.games.sc.addons.addable.GroundType;

public class ButtonGrid extends Canvas {
	
	private static final long serialVersionUID = 961913053698535524L;
	
	private final List<ButtonGridMouseListener> listeners = new LinkedList<>();
	
	private int bsize;
	private int xlen;
	private int ylen;
	
	private BufferedImage img;
	private Graphics2D    g;
	
	public ButtonGrid(int xlen, int ylen, int buttonSize) {
		super();
		if (xlen <= 0 || ylen <= 0 || buttonSize <= 0) {
			throw new IllegalArgumentException("xlen: " + xlen + " ylen: " + ylen + " btnSize: " + buttonSize);
		}
		this.bsize = buttonSize;
		this.xlen  = xlen;
		this.ylen  = ylen;
		resetImg();
		MouseAdapter ma = new BGMouseAdapter();
		super.addMouseListener(ma);
		super.addMouseMotionListener(ma);
		super.addMouseWheelListener(ma);
	}
	
	/**
	 * adds the given mouse listener
	 * 
	 * @param bgml the mouse listener
	 */
	public void addBGMouseListener(ButtonGridMouseListener bgml) {
		this.listeners.add(bgml);
	}
	
	/**
	 * removes the given mouse listener
	 * 
	 * @param bgml the mouse listener
	 * 
	 * @return <code>true</code> if the listener was register before this operation
	 */
	public boolean removeBGMouseListener(ButtonGridMouseListener bgml) {
		return this.listeners.remove(bgml);
	}
	
	/**
	 * returns all the mouse listeners
	 * 
	 * @return an array containing all registered mouse listeners
	 */
	public ButtonGridMouseListener[] getBGMouseListener() {
		return this.listeners.toArray(new ButtonGridMouseListener[this.listeners.size()]);
	}
	
	private class BGMouseAdapter extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			int x    = e.getX();
			int y    = e.getY();
			int subx = x % ButtonGrid.this.bsize;
			x /= ButtonGrid.this.bsize;
			int suby = y % ButtonGrid.this.bsize;
			y /= ButtonGrid.this.bsize;
			for (ButtonGridMouseListener bgml : ButtonGrid.this.listeners) {
				bgml.pressed(e, x, y, subx, suby);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			int x    = e.getX();
			int y    = e.getY();
			int subx = x % ButtonGrid.this.bsize;
			x /= ButtonGrid.this.bsize;
			int suby = y % ButtonGrid.this.bsize;
			y /= ButtonGrid.this.bsize;
			for (ButtonGridMouseListener bgml : ButtonGrid.this.listeners) {
				bgml.released(e, x, y, subx, suby);
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			int x    = e.getX();
			int y    = e.getY();
			int subx = x % ButtonGrid.this.bsize;
			x /= ButtonGrid.this.bsize;
			int suby = y % ButtonGrid.this.bsize;
			y /= ButtonGrid.this.bsize;
			for (ButtonGridMouseListener bgml : ButtonGrid.this.listeners) {
				bgml.moved(e, x, y, subx, suby, false);
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int x    = e.getX();
			int y    = e.getY();
			int subx = x % ButtonGrid.this.bsize;
			x /= ButtonGrid.this.bsize;
			int suby = y % ButtonGrid.this.bsize;
			y /= ButtonGrid.this.bsize;
			for (ButtonGridMouseListener bgml : ButtonGrid.this.listeners) {
				bgml.moved(e, x, y, subx, suby, true);
			}
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int x    = e.getX();
			int y    = e.getY();
			int subx = x % ButtonGrid.this.bsize;
			x /= ButtonGrid.this.bsize;
			int suby = y % ButtonGrid.this.bsize;
			y /= ButtonGrid.this.bsize;
			for (ButtonGridMouseListener bgml : ButtonGrid.this.listeners) {
				bgml.whelling(e, x, y, subx, suby);
			}
		}
		
	}
	
	public static void main(String[] args) {
		Frame f = new Frame("some nice title");
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(@SuppressWarnings("unused") WindowEvent e) {
				System.exit(0);
			}
		});
		ButtonGrid bg = new ButtonGrid(3, 3, 64);
		bg.setBtn(2, 0, GroundType.NOT_EXPLORED_GRND.image(16, 16));
		f.add(bg);
		f.pack();
		f.setLocationByPlatform(true);
		f.setVisible(true);
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bg.setBtn(0, 0, GroundType.NOT_EXPLORED_GRND.image(256, 256));
		System.out.println("setbtn");
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bg.repaint();
		System.out.println("repaint");
		bg.addBGMouseListener(new ButtonGridMouseListener() {
			
			@Override
			public void whelling(MouseWheelEvent e, int x, int y, int subx, int suby) {
				System.out.println("WHELLING");
				System.out.println("x: " + x + " y: " + y);
				System.out.println(e.getUnitsToScroll());
				System.out.println(e.getModifiersEx());
				bg.setButtonSize(bg.getButtonSize() + e.getUnitsToScroll());
				bg.setBtn(2, 0, GroundType.NOT_EXPLORED_GRND.image(16, 16));
				bg.setBtn(0, 0, GroundType.NOT_EXPLORED_GRND.image(16, 16));
				f.pack();
				bg.repaint();
			}
			
			@Override
			public void released(MouseEvent e, int x, int y, int subx, int suby) {
				System.out.println("ARREST THEM!");
				System.out.println("x: " + x + " y: " + y);
				System.out.println(e.getModifiersEx());
				bg.repaint();
			}
			
			@Override
			public void pressed(MouseEvent e, int x, int y, int subx, int suby) {
				System.out.println("RELEASE THE HOSTAGE!");
				System.out.println("x: " + x + " y: " + y);
			}
			
			@Override
			public void moved(MouseEvent e, int x, int y, int subx, int suby, boolean dragging) {
				if (dragging) {
					System.out.println("BRING THE MOUSE TO ME!");
					System.out.println("x: " + x + " y: " + y);
				} else {
					System.out.println("CATCH THE MOUSE!");
					System.out.println("x: " + x + " y: " + y);
				}
			}
		});
	}
	
	/**
	 * reset the image and the preferred size.
	 */
	public final void resetImg() {
		this.img = new BufferedImage(this.xlen * this.bsize, this.ylen * this.bsize, BufferedImage.TYPE_INT_RGB);
		this.g   = this.img.createGraphics();
		super.setPreferredSize(new Dimension(this.img.getWidth(), this.img.getHeight()));
	}
	
	/**
	 * set the size of all buttons
	 * <p>
	 * note that this operation {@link #resetImg() resets} the image
	 * 
	 * @param buttonSize the new button size
	 */
	public void setButtonSize(int buttonSize) {
		if (buttonSize <= 0) {
			throw new IllegalArgumentException("btnSize: " + buttonSize);
		}
		this.img = new BufferedImage(this.xlen * buttonSize, this.ylen * buttonSize, BufferedImage.TYPE_INT_RGB);
		super.setPreferredSize(new Dimension(this.img.getWidth(), this.img.getHeight()));
	}
	
	/**
	 * returns the current button size
	 * 
	 * @return the current button size
	 */
	public int getButtonSize() {
		return this.bsize;
	}
	
	/**
	 * set the amount of buttons
	 * <p>
	 * note that this operation {@link #resetImg() resets} the image
	 * 
	 * @param xlen the width
	 * @param ylen the height
	 */
	public void setButtonCount(int xlen, int ylen) {
		if (xlen <= 0 || ylen <= 0) {
			throw new IllegalArgumentException("xlen: " + xlen + " ylen: " + ylen);
		}
		this.xlen = xlen;
		this.ylen = ylen;
		resetImg();
	}
	
	/**
	 * returns the current width
	 * 
	 * @return the current width
	 */
	public int getXlen() {
		return this.xlen;
	}
	
	/**
	 * returns the current height
	 * 
	 * @return the current height
	 */
	public int getYlen() {
		return this.ylen;
	}
	
	/**
	 * sets the width, height and button size
	 * <p>
	 * note that this operation {@link #resetImg() resets} the image
	 * 
	 * @param xlen       the width
	 * @param ylen       the height
	 * @param buttonSize the button size
	 */
	public void setValues(int xlen, int ylen, int buttonSize) {
		if (xlen <= 0 || ylen <= 0 || buttonSize <= 0) {
			throw new IllegalArgumentException("xlen: " + xlen + " ylen: " + ylen + " btnSize: " + buttonSize);
		}
		this.bsize = buttonSize;
		this.xlen  = xlen;
		this.ylen  = ylen;
		resetImg();
	}
	
	/**
	 * paints all buttons to the graphics
	 */
	@Override
	public void paint(Graphics g) {
		g.drawImage(this.img, 0, 0, getWidth(), getHeight(), this);
	}
	
	/**
	 * set the image of the button with the given coordinates to the given value
	 * <p>
	 * note that this method does not call {@link #repaint()} or anything similar<br>
	 * this allows setting multiple buttons without repainting the canvas many times, but makes it necessary that the caller of this method also lets this canvas
	 * repaint
	 * 
	 * @param x   the x coordinate of the button
	 * @param y   the y coordinate of the button
	 * @param img the new image of the button
	 */
	public void setBtn(int x, int y, Image img) {
		if (x < 0 || y < 0 || x > this.xlen || y > this.ylen) {
			throw new IllegalArgumentException("x: " + x + " y: " + y + " xlen: " + this.xlen + " ylen: " + this.ylen);
		}
		this.g.drawImage(img, x * this.bsize, y * this.bsize, this.bsize, this.bsize, this);
	}
	
}
