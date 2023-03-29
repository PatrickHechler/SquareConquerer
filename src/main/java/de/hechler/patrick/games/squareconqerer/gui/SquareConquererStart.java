package de.hechler.patrick.games.squareconqerer.gui;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.connect.RemoteWorld;

public class SquareConquererStart {
	
	public static void main(String[] args) {
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		JFrame frame = new JFrame("Square Conquerer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frame.setContentPane(panel);
		GridLayout layout = new GridLayout(0, 2);
		layout.setHgap(50);
		layout.setVgap(10);
		panel.setLayout(layout);
		JCheckBox      remote      = new JCheckBox("Remote Login");
		JCheckBox      createUser  = new JCheckBox("Create Remote Account");
		JCheckBox      createNew   = new JCheckBox("Create new World");
		JFileChooser   fc          = new JFileChooser();
		JPasswordField pw          = new JPasswordField(16);
		JPasswordField serverPW    = new JPasswordField(16);
		JTextField     host        = new JTextField(16);
		JTextField     name        = new JTextField(16);
		JTextField     selectdFile = new JTextField(16);
		JTextField     xlen        = new JTextField(16);
		JTextField     ylen        = new JTextField(16);
		JButton        loadBtn     = new JButton("select file");
		JButton        startBtn    = new JButton("connect to server");
		JLabel         hostlbl     = new JLabel("Host:");
		JLabel         namelbl     = new JLabel("Username:");
		JLabel         xlenlbl     = new JLabel("Y-Length: (Width)");
		JLabel         ylenlbl     = new JLabel("X-Length: (Height)");
		JLabel         spwlabel    = new JLabel("Server Password:");
		JLabel         invisible0  = new JLabel();
		JLabel         invisible1  = new JLabel();
		JLabel         invisible2  = new JLabel();
		JLabel         invisible3  = new JLabel();
		JLabel         invisible4  = new JLabel();
		JLabel         invisible5  = new JLabel();
		NumberDocument xlenDoc     = new NumberDocument(0, Integer.MAX_VALUE); // the limit is the ram
		NumberDocument ylenDoc     = new NumberDocument(0, Integer.MAX_VALUE);
		xlen.setDocument(xlenDoc);
		ylen.setDocument(ylenDoc);
		xlen.setText("16");
		ylen.setText("16");
		fc.setMultiSelectionEnabled(false);
		remote.setSelected(true);
		createUser.setToolTipText("<html>connect to the server using the server password and then create an account on the server<br>"
				+ "the server password will only be used once to encrypt your password and send it to the server.</html>");
		
		panel.add(remote, 0);
		panel.add(createUser, 1);
		panel.add(new JLabel("Password:"), 2);
		panel.add(pw, 3);
		panel.add(hostlbl, 4);
		panel.add(host, 5);
		panel.add(namelbl, 6);
		panel.add(name, 7);
		panel.add(invisible0, 8);
		panel.add(invisible1, 9);
		panel.add(invisible2, 10);
		panel.add(invisible3, 11);
		panel.add(invisible4, 12);
		panel.add(startBtn, 13);
		
		ActionListener checkBoxListener = e -> {
			while (panel.getComponentCount() > 4) {
				panel.remove(4);
			}
			if (remote.isSelected()) {
				panel.add(hostlbl, 4);
				panel.add(host, 5);
				panel.add(namelbl, 6);
				panel.add(name, 7);
				if (createUser.isSelected()) {
					panel.add(spwlabel, 8);
					panel.add(serverPW, 9);
				} else {
					panel.add(invisible0, 8);
					panel.add(invisible1, 9);
				}
				startBtn.setText("connect to server");
				createUser.setVisible(true);
			} else {
				panel.add(createNew, 4);
				panel.add(invisible5, 5);
				if (createNew.isSelected()) {
					panel.add(xlenlbl, 6);
					panel.add(xlen, 7);
					panel.add(ylenlbl, 8);
					panel.add(ylen, 9);
					startBtn.setText("create new world");
				} else {
					panel.add(loadBtn, 6);
					panel.add(selectdFile, 7);
					panel.add(invisible0, 8);
					panel.add(invisible1, 9);
					startBtn.setText("load from file");
				}
				createUser.setVisible(false);
			}
			panel.add(invisible2, 10);
			panel.add(invisible3, 11);
			panel.add(invisible4, 12);
			panel.add(startBtn, 13);
			frame.pack();
			frame.repaint();
		};
		remote.addActionListener(checkBoxListener);
		createUser.addActionListener(checkBoxListener);
		createNew.addActionListener(checkBoxListener);
		loadBtn.addActionListener(e -> {
			int chosen = fc.showOpenDialog(frame);
			if (chosen != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(frame, "there is no such file", "file not found", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!file.isFile()) {
				JOptionPane.showMessageDialog(frame, "the selected path is no 'normal' file", "no file", JOptionPane.ERROR_MESSAGE);
				return;
			}
			selectdFile.setText(file.toString());
		});
		startBtn.addActionListener(e -> {
			try {
				World world;
				if (remote.isSelected()) {
					if (createUser.isSelected()) {
						world = connectToRemoteWorld(pw, serverPW.getPassword(), host, name);
					} else {
						world = connectToRemoteWorld(pw, null, host, name);
					}
				} else if (createNew.isSelected()) {
					world = createNewWorld(pw, xlenDoc, ylenDoc);
				} else {
					world = loadWorldFromFile(pw, selectdFile);
				}
				SquareConquererGUI gui = new SquareConquererGUI(world);
				gui.load(false);
				frame.dispose();
				gui.visible(true);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(frame, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		});
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private static World connectToRemoteWorld(JPasswordField pw, char[] serverPW, JTextField host, JTextField name) throws IOException {
		World       world;
		String      hostTxt = host.getText().trim();
		int         li      = hostTxt.lastIndexOf(':');
		User        usr     = User.create(name.getText(), pw.getPassword());
		InetAddress addr;
		int         port    = Connection.DEFAULT_PORT;
		if (li != -1) { // (-1 < li) is also true
			if (hostTxt.lastIndexOf(']') < li) {
				addr = InetAddress.getByName(hostTxt.substring(0, li));
				String portStr = hostTxt.substring(li + 1);
				port = Integer.parseInt(portStr);
			} else {
				addr = InetAddress.getByName(hostTxt);
			}
		} else {
			addr = InetAddress.getByName(hostTxt);
		}
		Connection conn;
		if (serverPW != null) {
			conn = Connection.ClientConnect.connectNew(addr, port, usr, serverPW);
		} else {
			conn = Connection.ClientConnect.connect(addr, port, usr);
		}
		world = new RemoteWorld(conn);
		return world;
	}
	
	private static World createNewWorld(JPasswordField pw, NumberDocument xlenDoc, NumberDocument ylenDoc) {
		World             world;
		RootUser          root = RootUser.create(pw.getPassword());
		RootWorld.Builder b    = new RootWorld.Builder(root, Math.max(1, xlenDoc.getNumber()), Math.max(1, ylenDoc.getNumber()));
		b.fillRandom();
		world = b.create();
		return world;
	}
	
	private static World loadWorldFromFile(JPasswordField pw, JTextField selectdFile) throws IOException {
		RootUser root = RootUser.create(pw.getPassword());
		try (FileInputStream in = new FileInputStream(selectdFile.getText()); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, root)) {
			Tile[][] tiles;
			root.load(conn);
			tiles = RemoteWorld.readWorld(conn, null, false);
			return RootWorld.Builder.create(root, tiles);
		} catch (Throwable t) {
			root.close();
			throw t;
		}
	}
	
}
