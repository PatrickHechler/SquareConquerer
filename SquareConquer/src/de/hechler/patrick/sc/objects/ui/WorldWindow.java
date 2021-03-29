package de.hechler.patrick.sc.objects.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTable;

import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.objects.AbsoluteMegaManipulablePosition;
import de.hechler.patrick.sc.objects.World;

public class WorldWindow extends JFrame {
	
	/** UID */
	private static final long serialVersionUID = 789464080026285409L;
	
	private final WorldTable table;
	private final World      world;
	
	
	public WorldWindow(World world) {
		super("World-Window (" + world.getXCnt() + "|" + world.getYCnt() + ")");
		this.table = new WorldTable(world.getXCnt(), world.getYCnt());
		this.world = world;
	}
	
	public static void main(String[] args) {
		new WorldWindow(new World(10, 5)).load(true);
	}
	
	public WorldWindow load(boolean visible) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(null);
		
		table.setRowHeight(30);
		table.setRowMargin(30);
		table.setShowGrid(true);
		table.setBounds(0, 0, table.getColumnCount() * 30, table.getRowCount() * 30);
		table.setRowSelectionAllowed(false);
		add(table);
		
		setBounds(0, 0, 100 + table.getColumnCount() * 30, 100 + table.getRowCount() * 30);
		setLocationRelativeTo(null);
		
		if (visible) {
			setVisible(true);
			toFront();
			repaint();
		} else {
			setVisible(false);
		}
		return this;
	}
	
	public void update(boolean visible) {
		final int xc = world.getXCnt(), yc = world.getYCnt();
		for (AbsoluteMegaManipulablePosition pos = new AbsoluteMegaManipulablePosition(0, 0); pos.x < xc; pos.x ++ ) {
			for (pos.y = 0; pos.y < yc; pos.y ++ ) {
				table.setValueAt(world.getField(pos), pos.x, pos.y);
			}
		}
	}
	
	private class WorldTable extends JTable {
		
		/** UID */
		private static final long serialVersionUID = -686578042675992458L;
		
		public WorldTable(int xCnt, int yCnt) {
			super(yCnt, xCnt);
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
		}
		
		public void setValueAt(Field f, int x, int y) {
			Object i = getIcon(f);
			super.setValueAt(i, y, x);
		}
		
		private Object getIcon(Field f) {
			BufferedImage newImg = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = newImg.createGraphics();
			g2d.drawImage(new ImageIcon("./icons/" + f.ground().name() + ".png").getImage(), 0, 0, null);
			if (f.hasEntity()) {
				g2d.drawImage(new ImageIcon("./icons/" + f.getEntity().type().name() + ".png").getImage(), 0, 0, null);
			}
			g2d.dispose();
			return newImg;
		}
		
	}
	
}
