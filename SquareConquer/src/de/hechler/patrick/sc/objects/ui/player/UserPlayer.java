package de.hechler.patrick.sc.objects.ui.player;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Player;
import de.hechler.patrick.sc.objects.FieldImpl;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.NicePosition;
import de.hechler.patrick.sc.objects.World;
import de.hechler.patrick.sc.objects.ui.WorldWindow;
import de.hechler.patrick.sc.utils.factory.EntityFactory;

public class UserPlayer implements Player {
	
	private JTextArea   actionText;
	private JFrame      actionFrame;
	private WorldWindow window;
	private World       world;
	// private int myID;
	
	
	
	public UserPlayer() {
	}
	
	
	
	public static void main(String[] args) {
		World world = new World(60, 35);
		UserPlayer user = new UserPlayer();
		Random rnd = new Random();
		Grounds[] g = Grounds.values();
		for (int i = 0; i < world.getXCnt(); i ++ ) {
			for (int ii = 0; ii < world.getYCnt(); ii ++ ) {
				world.overrideField(new FieldImpl(i, ii, g[rnd.nextInt(g.length - 1)])); // 'unknown' is the last
			}
		}
		user.init(world, 3);
		user.makeTurn();
		NicePosition pos = new NicePosition(0, 0);
		Entity e = EntityFactory.create(4, pos, Type.carrier, null);
		world.getField(pos).setEntity(e);
		pos.move(Direction.up);
		e = EntityFactory.create(4, pos, Type.boat, null);
		world.getField(pos).setEntity(e);
		user.makeTurn();
	}
	
	
	
	@Override
	public void init(final World world, final int myID) {
		// this.myID = myID;
		this.world = world;
		this.actionText = new JTextArea();
		this.actionFrame = new JFrame("AKTION");
		this.actionFrame.add(this.actionText);
		this.window = new WorldWindow(world.getXCnt(), world.getYCnt()).load(world.getXCnt(), world.getYCnt(), false);
		this.window.update(world, false);
		JButton b = new JButton();
		b.addActionListener(a ->  {//TODO does not work too
			System.out.println(a.getClass().getName());
			System.out.println(a);
		});
		this.window.getRootPane().setDefaultButton(b);
		this.window.addKeyListener(new KeyAdapter() {//TODO does not react (don't print anything even a lineSeperator (so the methods do not get called))
			
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyChar());
				System.out.println(e);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e.getKeyChar());
				System.out.println(e);
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
				System.out.println(e.getKeyChar());
				System.out.println(e);
				int kc = e.getKeyCode();
				switch (kc) {
				case KeyEvent.VK_ENTER:
					UserPlayer.this.finishTurnMaby();
				case KeyEvent.VK_ESCAPE:
					UserPlayer.this.closeMaby();
				}
			}
			
		});
		this.window.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				System.out.println("open");
				JFrame frame = new JFrame();
				JTextPane text = new JTextPane();
				text.setText("Drücke einfach Enter, um deine Züge zu beenden.");
				text.setBounds(0, 0, 250, 75);
				frame.add(text);
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setBounds(0, 0, 250, 75);
				frame.setLocationRelativeTo(null);
				frame.toFront();
				frame.setVisible(true);
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				UserPlayer.this.closeMaby();
			}
			
		});
		this.window.addListener(pos -> {
			final Int lineCnt = new Int();
			final StringBuilder text;
			REST: {
				Field f = world.getField(pos);
				text = new StringBuilder("Position: ").append(f.position().toPosStr());
				lineCnt.i ++ ;
				if (f.ground() == Grounds.unknown) {
					text.append("\n   Dieses Feld wurde noch nicht erkundet");
					lineCnt.i ++ ;
					break REST;
				}
				if (f.isMemory()) {
					text.append("\n   Dieses Feld ist nur eine Erinnerung");
					lineCnt.i ++ ;
				}
				text.append("\nBoden: ").append(f.ground().name());
				lineCnt.i ++ ;
				if ( !f.hasEntity()) break REST;
				Entity e = f.getEntity();
				text.append("\nEntity: ").append(e.type().name());
				lineCnt.i ++ ;
				text.append("\n   Leben: ").append(e.health());
				lineCnt.i ++ ;
				text.append("\n   Sichtweite: ").append(e.sight());
				lineCnt.i ++ ;
				text.append("\n   Aktionen:\n      verbleibende: ").append(e.remainingActions());
				lineCnt.i ++ ;
				text.append("\n      gesamt:       ").append(e.totalActions());
				lineCnt.i ++ ;
				if (e.isMovable()) {
					MovableEntity me = (MovableEntity) e;
					int owner = me.owner();
					if (owner == myID) text.append("\n   Dies ist deine Einheit");
					else text.append("\n   Besitzer: ").append(owner);
					lineCnt.i ++ ;
					text.append("\n   kann sein auf:");
					lineCnt.i ++ ;
					me.canExsitOn().forEach(g -> {
						text.append("\n      ").append(g.name());
						lineCnt.i ++ ;
					});
				} else {
					if (e.type().isHouse()) {
						HouseBuilding hb = (HouseBuilding) e;
						Set <MovableEntity> inside = hb.inside();
						if (inside.isEmpty()) {
							text.append("\n   In dem Haus ist keine Einheit");
							lineCnt.i ++ ;
						} else if (inside.size() == 1) {
							text.append("\n   In dem Haus ist eine Einheit:");
							lineCnt.i ++ ;
							MovableEntity me = inside.iterator().next();
							text.append("\n   Entity: ").append(e.type().name());
							lineCnt.i ++ ;
							text.append("\n      Leben: ").append(e.health());
							lineCnt.i ++ ;
							text.append("\n      Aktionen:\n         verbleibende: ").append(e.remainingActions());
							lineCnt.i += 2;
							text.append("\n         gesamt:       ").append(e.totalActions());
							lineCnt.i ++ ;
							int owner = me.owner();
							if (owner == myID) text.append("\n      Dies ist deine Einheit");
							else text.append("\n      Besitzer: ").append(owner);
							lineCnt.i ++ ;
							text.append("\n      kann sein auf:");
							lineCnt.i ++ ;
							me.canExsitOn().forEach(g -> {
								text.append("\n         ").append(g.name());
								lineCnt.i ++ ;
							});
						} else {
							text.append("\n   In dem Haus sind ").append(inside.size()).append(" Einheiten");
							lineCnt.i ++ ;
							inside.forEach(me -> {
								text.append("\n   Entity: ").append(e.type().name());
								lineCnt.i ++ ;
								text.append("\n      Leben: ").append(e.health());
								lineCnt.i ++ ;
								text.append("\n      Aktionen:\n         verbleibende: ").append(e.remainingActions());
								lineCnt.i += 2;
								text.append("\n         gesamt:       ").append(e.totalActions());
								lineCnt.i ++ ;
								int owner = me.owner();
								if (owner == myID) text.append("\n      Dies ist deine Einheit");
								else text.append("\n      Besitzer: ").append(owner);
								lineCnt.i ++ ;
								text.append("\n      kann sein auf:");
								lineCnt.i ++ ;
								me.canExsitOn().forEach(g -> {
									text.append("\n         ").append(g.name());
									lineCnt.i ++ ;
								});
							});
						}
					}
				}
			}
			this.actionText.setText(text.toString());
			this.actionFrame.setBounds(0, 0, 250, lineCnt.i * 18 + 40);
			this.actionFrame.setLocationRelativeTo(null);
			this.actionFrame.setVisible(true);
			this.actionFrame.toFront();
		});
	}
	
	protected void finishTurnMaby() {
		int chose = JOptionPane.showConfirmDialog((Component) null, "Zug beenden?", "WICHTIG", JOptionPane.OK_CANCEL_OPTION);
		if (chose == JOptionPane.CANCEL_OPTION) {
			UserPlayer.this.window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		} else {
			synchronized (UserPlayer.this) {
				UserPlayer.this.notify();
			}
			UserPlayer.this.window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		}
	}
	
	protected void closeMaby() {
		int chose = JOptionPane.showConfirmDialog((Component) null, "Spiel beenden?", "WICHTIG", JOptionPane.OK_CANCEL_OPTION);
		if (chose == JOptionPane.CANCEL_OPTION) {
			UserPlayer.this.window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		} else {
			synchronized (UserPlayer.this) {
				UserPlayer.this.notify();
			}
			Runtime.getRuntime().runFinalization();
			System.exit(0);
			UserPlayer.this.window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}
	}
	
	@Override
	public void makeTurn() {
		this.window.update(world, true);
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static class Int {int i;}
	
}
