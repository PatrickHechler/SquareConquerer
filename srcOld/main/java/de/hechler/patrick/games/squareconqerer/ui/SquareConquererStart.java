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

import static de.hechler.patrick.games.squareconqerer.Settings.VERSION_STRING;
import static de.hechler.patrick.games.squareconqerer.Settings.threadStart;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

public class SquareConquererStart {
	
	private static int     port           = -1;
	private static boolean gui            = true;
	private static boolean console        = false;
	private static boolean consoleInter   = false;
	private static boolean consoleNoInter = false;
	private static String  host           = null;
	private static boolean server         = false;
	private static String  name           = null;
	private static char[]  pw             = null;
	private static char[]  serverpw       = null;
	private static Path    worldFile      = null;
	private static boolean emptyWorld     = false;
	private static int     xlen           = -1;
	private static int     ylen           = -1;
	
	public static void main(String[] args) {
		readArgs(args);
		final int             p            = port != -1 ? port : Connection.DEFAULT_PORT;
		World                 world        = null;
		Thread                serverThread = null;
		Map<User, Connection> connects     = null;
		if (pw != null) {
			if (worldFile != null) {
				world = tryLoadWorld(args, world);
			} else if (xlen != -1) {
				world = tryCreateWorld();
			} else if (host != null && name != null) {
				world = tryConnectWorld(args, p, world);
			}
			if (server && world != null) {
				final RootWorld             rw = (RootWorld) world;
				final Map<User, Connection> cs = new HashMap<>();
				connects     = cs;
				serverThread = threadStart(() -> {
									try {
										Connection.ServerAccept.accept(p, rw, (conn, sok) -> {
															if (sok == null) {
																System.err.println("the user '" + conn.usr.name() + "' disconnected");
															} else {
																System.err.println("the user '" + conn.usr.name() + "' logged in from " + sok.getInetAddress());
															}
														}, cs, serverpw);
									} catch (IOException e) {
										e.printStackTrace();
									}
								});
			}
		}
		if (gui && console) {
			crash(-1, args, "gui and console modes are not allowed together");
		}
		if (gui) {
			startGUI(args, p, world, serverThread);
		}
		if (console) {
			startCON(p, world, serverThread, connects);
		}
	}
	
	private static World tryConnectWorld(String[] args, final int p, World world) {
		User       usr = User.create(name, pw);
		Connection conn;
		try {
			if (serverpw != null) {
				conn = Connection.ClientConnect.connect(host, p, usr);
			} else {
				conn = Connection.ClientConnect.connectNew(host, p, usr, serverpw);
			}
			world = new RemoteWorld(conn);
		} catch (IOException e) {
			for (int i = 0; i < pw.length; i++) { pw[i] = '\0'; }
			for (int i = 0; serverpw != null && i < serverpw.length; i++) { serverpw[i] = '\0'; }
			crash(-1, args, "could not establish a connection with the server: " + e.toString());
		}
		return world;
	}
	
	private static World tryCreateWorld() {
		World    world;
		RootUser root = RootUser.create(pw);
		world = new RootWorld.Builder(root, xlen, ylen);
		if (!emptyWorld) {
			((RootWorld.Builder) world).fillRandom();
		}
		return world;
	}
	
	private static World tryLoadWorld(String[] args, World world) {
		RootUser root = RootUser.create(pw);
		try (InputStream in = Files.newInputStream(worldFile, StandardOpenOption.READ); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, root)) {
			root.load(conn);
			Tile[][] tiles = RemoteWorld.loadWorld(conn, root.users());
			world = RootWorld.Builder.create(root, tiles);
		} catch (IOException e) {
			for (int i = 0; i < pw.length; i++) { pw[i] = '\0'; }
			for (int i = 0; serverpw != null && i < serverpw.length; i++) { serverpw[i] = '\0'; }
			crash(-1, args, "could not load the data from the file: " + e.toString());
		}
		return world;
	}
	
	private static void startCON(final int p, World world, Thread serverThread, Map<User, Connection> connects) {
		SquareConquererCUI cui = new SquareConquererCUI();
		if (world != null) {
			cui.setWorld(world, serverThread, connects);
		} else {
			if (pw != null) {
				if (name != null) {
					cui.user(User.create(name, pw));
				} else {
					cui.user(RootUser.create(pw));
				}
			} else if (name != null) {
				cui.name(name);
			}
			if (serverpw != null) {
				cui.serverPW(serverpw);
			}
			
			if (worldFile != null) {
				cui.startLoad(worldFile);
				if (port != -1) {
					cui.startServer(p);
				}
			} else if (server) {
				cui.startConnect(host, p);
			}
		}
		if (consoleInter || consoleNoInter) {
			cui.interactive(consoleInter);
		}
		cui.run();
	}
	
	private static void startGUI(String[] args, final int p, World world, Thread serverThread) {
		setDefaultFonts();
		if (world != null) {
			SquareConquererGUI gui = new SquareConquererGUI(world);
			gui.load(serverThread);
		} else {
			SwingUtilities.invokeLater(() -> {
				JFrame frame = initStartFrame();
				if (pw != null) {
					setPW(args);
				}
				if (serverpw != null) {
					setServerPW(args);
				}
				if (host != null) {
					setHost(p);
				}
				if (name != null) {
					nameField.setText(name);
				}
				if (worldFile != null) {
					selectdFileField.setText(worldFile.toString());
				}
				if (xlen != -1) {
					xlenField.setText(Integer.toString(xlen));
				}
				if (ylen != -1) {
					ylenField.setText(Integer.toString(ylen));
				}
				createUser.setSelected(serverpw != null);
				if (name != null || host != null) {
					remote.setSelected(true);
				} else if (xlen != -1) {
					remote.setSelected(false);
					createNew.setSelected(true);
				} else if (worldFile != null) {
					remote.setSelected(false);
					createNew.setSelected(false);
				}
				checkBoxListener.actionPerformed(null);
				frame.setVisible(true);
			});
		}
	}
	
	private static void setHost(final int p) {
		String h;
		if (host.indexOf(':') != -1) {
			h = "[" + host + "]:" + p;
		} else {
			h = host + ":" + p;
		}
		hostField.setText(h);
	}
	
	private static void setServerPW(String[] args) {
		Document doc = serverPWField.getDocument();
		for (int i = 0; i < serverpw.length; i++) {
			try {
				doc.insertString(i, Character.toString(serverpw[i]), null);
			} catch (BadLocationException e) {
				for (; i < serverpw.length; i++) { serverpw[i] = '\0'; }
				crash(-1, args, "could not initilize the server password field: " + e.toString());
			}
			serverpw[i] = '\0';
		}
	}
	
	private static void setPW(String[] args) {
		Document doc = pwField.getDocument();
		for (int i = 0; i < pw.length; i++) {
			try {
				doc.insertString(i, Character.toString(pw[i]), null);
			} catch (BadLocationException e) {
				for (; i < pw.length; i++) { pw[i] = '\0'; }
				for (i = 0; serverpw != null && i < serverpw.length; i++) { serverpw[i] = '\0'; }
				crash(-1, args, "could not initilize the password field: " + e.toString());
			}
			pw[i] = '\0';
		}
	}
	
	private static void readArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "--help" -> argHelp();
			case "--version" -> argVersion();
			case "--gui" -> argGui();
			case "--no-gui" -> argNoGui();
			case "--console" -> argConsole();
			case "--console-interactive" -> argConsoleInteractive();
			case "--console-no-interactive" -> argConsoleNoInteractive();
			case "--no-console" -> argNoConsole();
			case "--name" -> argName(args, ++i);
			case "--name-file" -> argNameFile(args, ++i);
			case "--pw" -> argPw(args, ++i);
			case "--pw-file" -> argPwFile(args, ++i);
			case "--identy-file" -> argIdentyFile(args, ++i);
			case "--server" -> argServer(args, i);
			case "--port" -> argPort(args, ++i);
			case "--connect" -> argConnect(args, ++i);
			case "--server-pw" -> argServerPw(args, i);
			case "--server-pw-file" -> argServerpwFile(args, i);
			case "--load" -> argLoad(args, i);
			case "--world-size" -> argWorldSize(args, i += 2);
			case "--empty-world" -> argEmptyWorld(args, i);
			default -> crash(i, args, "unknown argument");
			}
		}
	}
	
	private static void setDefaultFonts() {
		FontUIResource      f    = new FontUIResource("Sans", Font.PLAIN, 14);
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key   = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource fur) {
				FontUIResource val = f;
				val = new FontUIResource(val.getFontName(), fur.getStyle(), (int) (fur.getSize() * 1.25));
				UIManager.put(key, val);
			}
		}
	}
	
	private static void argEmptyWorld(String[] args, int i) {
		if (host != null) {
			crash(i, args, "host and new word are set");
		}
		if (worldFile != null) {
			crash(i, args, "new world and load world are set");
		}
		emptyWorld = true;
	}
	
	private static void argWorldSize(String[] args, int i) {
		checkEnughArgs(args, i);
		if (xlen != -1) {
			crash(i, args, "world sizes are already set");
		}
		if (host != null) {
			crash(i, args, "host and new word are set");
		}
		if (worldFile != null) {
			crash(i, args, "new world and load world are set");
		}
		try {
			xlen = Integer.parseInt(args[i - 1]);
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			crash(i - 1, args, "invalid number");
		}
		if (xlen <= 0) {
			crash(i - 1, args, "invalid number");
		}
		try {
			ylen = Integer.parseInt(args[i]);
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			crash(i, args, "invalid number");
		}
		if (ylen <= 0) {
			crash(i, args, "invalid number");
		}
	}
	
	private static void argLoad(String[] args, int i) {
		checkEnughArgs(args, i);
		if (xlen != -1) {
			crash(i, args, "create new world and load world are set");
		}
		if (host != null) {
			crash(i, args, "host and load world are set");
		}
		worldFile = Path.of(args[i]);
		if (!Files.exists(worldFile)) {
			crash(i, args, "the load file does not exist");
		}
		if (!Files.isRegularFile(worldFile)) {
			crash(i, args, "the load file is no regular file");
		}
	}
	
	private static void argServerpwFile(String[] args, int i) {
		checkEnughArgs(args, i);
		if (pw != null) {
			crash(i, args, "server password already set");
		}
		try {
			byte[]     utf8bytes = Files.readAllBytes(Path.of(args[i]));
			CharBuffer buf       = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8bytes));
			for (int u8i = 0; u8i < utf8bytes.length; u8i++) { utf8bytes[u8i] = 0; }
			serverpw = new char[buf.limit()];
			buf.get(serverpw);
			for (int bi = 0; bi < serverpw.length; bi++) { buf.put(bi, '\0'); }
		} catch (IOException e) {
			crash(i, args, e.toString());
		}
		args[i] = null;
	}
	
	private static void argServerPw(String[] args, int i) {
		checkEnughArgs(args, i);
		if (serverpw != null) {
			crash(i, args, "server password is already set");
		}
		serverpw = args[i].toCharArray();
		args[i]  = null;
	}
	
	private static void argConnect(String[] args, int i) {
		checkEnughArgs(args, i);
		if (server) {
			crash(i, args, "server and host are both set");
		}
		if (worldFile != null) {
			crash(i, args, "load and host are both set");
		}
		if (host != null) {
			crash(i, args, "host is already set");
		}
		int li = args[i].lastIndexOf(':');
		if (li != -1) {
			int oli = args[i].lastIndexOf(']');
			if (oli == li - 1 && args[i].charAt(0) == '[') {
				if (port != -1) {
					crash(i, args, "port is already set");
				}
				host = args[i].substring(1, oli);
				port = Integer.parseInt(args[i].substring(li + 1));
			} else {
				host = args[i];
			}
		} else {
			host = args[i];
		}
	}
	
	private static void argPort(String[] args, int i) {
		checkEnughArgs(args, i);
		if (port != -1) {
			crash(i, args, "port already set");
		}
		try {
			port = Integer.parseUnsignedInt(args[i]);
		} catch (NumberFormatException e) {
			crash(i, args, e.getLocalizedMessage());
		}
		if (port > 0xFFFF) {
			crash(i, args, "port number is too large (max=" + 0xFFFF + " port=" + port + ')');
		}
	}
	
	private static void argServer(String[] args, int i) {
		if (host != null) {
			crash(i, args, "server and host are both set");
		}
		server = true;
	}
	
	private static void argIdentyFile(String[] args, int i) {
		checkEnughArgs(args, i);
		if (name != null) {
			crash(i, args, "username already set");
		}
		if (pw != null) {
			crash(i, args, "password already set");
		}
		byte[] utf8bytes;
		try {
			utf8bytes = Files.readAllBytes(Path.of(args[i]));
			CharBuffer buf = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8bytes));
			for (int u8i = 0; u8i < utf8bytes.length; u8i++) { utf8bytes[u8i] = 0; }
			char[] chars = new char[buf.limit()];
			buf.get(chars);
			for (int bi = 0; bi < chars.length; bi++) { buf.put(bi, '\0'); }
			int nl;
			for (nl = 0; nl < chars.length; nl++) {
				if (chars[nl] == '\n' || chars[nl] == '\r') {
					break;
				}
			}
			name = new String(chars, 0, nl);
			if (nl >= chars.length) {
				pw = new char[0];
			} else {
				if (chars[nl] == '\r' && chars[nl + 1] == '\n') {
					nl += 2;
				} else {
					nl += 1;
				}
				pw = new char[chars.length - nl];
				System.arraycopy(chars, nl, pw, 0, pw.length);
			}
			for (int ci = 0; ci < chars.length; ci++) { chars[ci] = '\0'; }
		} catch (IOException e) {
			crash(i, args, e.toString());
		}
	}
	
	private static void argPwFile(String[] args, int i) {
		checkEnughArgs(args, i);
		if (pw != null) {
			crash(i, args, "password already set");
		}
		try {
			byte[]     utf8bytes = Files.readAllBytes(Path.of(args[i]));
			CharBuffer buf       = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8bytes));
			for (int u8i = 0; u8i < utf8bytes.length; u8i++) { utf8bytes[u8i] = 0; }
			pw = new char[buf.limit()];
			buf.get(pw);
			for (int bi = 0; bi < pw.length; bi++) { buf.put(bi, '\0'); }
		} catch (IOException e) {
			crash(i, args, e.toString());
		}
		args[i] = null;
	}
	
	private static void argPw(String[] args, int i) {
		checkEnughArgs(args, i);
		if (pw != null) {
			crash(i, args, "password already set");
		}
		pw      = args[i].toCharArray();
		args[i] = null;                 // well, at least allow the GC to overwrite the content
	}
	
	private static void argNameFile(String[] args, int i) {
		checkEnughArgs(args, i);
		if (name != null) {
			crash(i, args, "name already set");
		}
		try {
			name = Files.readString(Path.of(args[i]), StandardCharsets.UTF_8);
		} catch (IOException e) {
			crash(i, args, e.toString());
		}
	}
	
	private static void argName(String[] args, int i) {
		checkEnughArgs(args, i);
		if (name != null) {
			crash(i, args, "name already set");
		}
		if (xlen != -1 || worldFile != null) {
			crash(i, args, "the root user has no name");
		}
		name = args[i];
	}
	
	private static void argNoConsole() { console = false; consoleInter = false; consoleNoInter = false; }
	
	private static void argConsole() { console = true; consoleInter = false; consoleNoInter = false; }
	
	private static void argConsoleInteractive() { console = true; consoleInter = true; consoleNoInter = false; }
	
	private static void argConsoleNoInteractive() { console = true; consoleInter = false; consoleNoInter = true; }
	
	private static void argNoGui() { gui = false; }
	
	private static void argGui() { gui = true; }
	
	private static void argVersion() {
		System.out.println("Square Conquerer: " + VERSION_STRING);
		System.exit(1);
	}
	
	private static void argHelp() {
		System.out.println("Square Conquerer " + VERSION_STRING + " help:");
		System.out.println("    --help");
		System.out.println("        to print this message and exit");
		System.out.println("    --version");
		System.out.println("        to print this version information and exit");
		System.out.println("    --gui");
		System.out.println("        to start a graphical interface");
		System.out.println("        overwrites previous --no-gui");
		System.out.println("        this is set by default");
		System.out.println("    --no-gui");
		System.out.println("        to not start a graphical interface");
		System.out.println("        overwrites previous --gui");
		System.out.println("    --console");
		System.out.println("        to start an user interface using the stdin/stdout streams");
		System.out.println("        overwrites previous --no-console");
		System.out.println("    --console-interactive");
		System.out.println("      like --console, but set the console to interactive mode");
		System.out.println("    --console-no-interactive");
		System.out.println("      like --console, but set the console to non interactive mode");
		System.out.println("    --no-console");
		System.out.println("        to start not an user interface using the stdin/stdout streams");
		System.out.println("        overwrites previous --console");
		System.out.println("        this is set by default");
		System.out.println("    --name <USERNAME>");
		System.out.println("        to set the username");
		System.out.println("    --name-file <USERNAME_FILE>");
		System.out.println("        to set the file, which contains the username");
		System.out.println("        the file is UTF-8 encoded");
		System.out.println("    --pw <PASSWORD>");
		System.out.println("        to set the password");
		System.out.println("        it is not recommended to use this argument, because the password can not be cleared");
		System.out.println("    --pw-file <PASSWORD_FILE>");
		System.out.println("        to set the file, which contains the password");
		System.out.println("        the file is UTF-8 encoded");
		System.out.println("    --identy-file <IDENTY_FILE>");
		System.out.println("        to set the identi file");
		System.out.println("        the first line of the identy file is the username");
		System.out.println("        everything after the first line is the password");
		System.out.println("        the file is UTF-8 encoded");
		System.out.println("    --server");
		System.out.println("        to start an Square Conquerer server");
		System.out.println("        if port is not set, " + Connection.DEFAULT_PORT + " will be used");
		System.out.println("    --port <PORT>");
		System.out.println("        to set the port");
		System.out.println("    --connect <HOST>[:PORT]");
		System.out.println("        to set the remote host");
		System.out.println("        optionally also set the port");
		System.out.println("        if port is not set, " + Connection.DEFAULT_PORT + " will be used");
		System.out.println("    --server-pw <SERVER_PW>");
		System.out.println("        to set the server password");
		System.out.println("        it is not recommended to use this argument, because the password can not be cleared");
		System.out.println("    --server-pw-file <SERVER_PW_FILE>");
		System.out.println("        to set the file, which contains the server password");
		System.out.println("        the file is UTF-8 encoded");
		System.out.println("    --load <FILE>");
		System.out.println("        to load the world (and users) from the given file");
		System.out.println("    --world-size <X-LEN> <Y-LEN>");
		System.out.println("        to set the size of the new world");
		System.out.println("        when this option is set a new world will be created");
		System.out.println("    --empty-world");
		System.out.println("        to create an empty world and do not fill the world with random tiles");
		System.exit(1);
	}
	
	private static void checkEnughArgs(String[] args, int i) {
		if (i >= args.length) {
			crash(i, args, "not enugh arguments");
		}
	}
	
	private static void crash(int index, String[] args, String msg) {
		if (msg != null) {
			System.err.println("error: " + msg);
		}
		for (int i = 0; i < args.length; i++) {
			System.err.print("args[" + i + "]: '" + args[i] + '\'');
			if (i == index) {
				System.err.print(" <-- error occured here");
			}
			System.err.println();
		}
		System.exit(2);
	}
	
	private static JPasswordField pwField;
	private static JPasswordField serverPWField;
	private static JCheckBox      remote;
	private static JCheckBox      createUser;
	private static JCheckBox      createNew;
	private static JTextField     hostField;
	private static JTextField     nameField;
	private static JTextField     selectdFileField;
	private static JTextField     xlenField;
	private static JTextField     ylenField;
	private static ActionListener checkBoxListener;
	
	private static JFrame initStartFrame() {
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		JFrame frame = new JFrame("Square Conquerer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frame.setContentPane(panel);
		GridLayout layout = new GridLayout(0, 2);
		layout.setHgap(50);
		layout.setVgap(10);
		panel.setLayout(layout);
		pwField          = new JPasswordField(16);
		serverPWField    = new JPasswordField(16);
		remote           = new JCheckBox("Remote Login");
		createUser       = new JCheckBox("Create Remote Account");
		createNew        = new JCheckBox("Create new World");
		hostField        = new JTextField(16);
		nameField        = new JTextField(16);
		selectdFileField = new JTextField(16);
		xlenField        = new JTextField(16);
		ylenField        = new JTextField(16);
		JFileChooser   fc         = new JFileChooser();
		JButton        loadBtn    = new JButton("select file");
		JButton        startBtn   = new JButton("connect to server");
		JLabel         hostlbl    = new JLabel("Host:");
		JLabel         namelbl    = new JLabel("Username:");
		JLabel         xlenlbl    = new JLabel("X-Length: (Width)");
		JLabel         ylenlbl    = new JLabel("Y-Length: (Height)");
		JLabel         spwlabel   = new JLabel("Server Password:");
		JLabel         invisible0 = new JLabel();
		JLabel         invisible1 = new JLabel();
		JLabel         invisible2 = new JLabel();
		JLabel         invisible3 = new JLabel();
		JLabel         invisible4 = new JLabel();
		JLabel         invisible5 = new JLabel();
		NumberDocument xlenDoc    = new NumberDocument(0, Integer.MAX_VALUE); // the limit is the ram
		NumberDocument ylenDoc    = new NumberDocument(0, Integer.MAX_VALUE);
		xlenField.setDocument(xlenDoc);
		ylenField.setDocument(ylenDoc);
		xlenField.setText("128");
		ylenField.setText("128");
		fc.setMultiSelectionEnabled(false);
		remote.setSelected(true);
		createUser.setToolTipText("<html>connect to the server using the server password and then create an account on the server<br>"
				+ "the server password will only be used once to encrypt your password and send it to the server.</html>");
		
		panel.add(remote, 0);
		panel.add(createUser, 1);
		panel.add(new JLabel("Password:"), 2);
		panel.add(pwField, 3);
		panel.add(hostlbl, 4);
		panel.add(hostField, 5);
		panel.add(namelbl, 6);
		panel.add(nameField, 7);
		panel.add(invisible0, 8);
		panel.add(invisible1, 9);
		panel.add(invisible2, 10);
		panel.add(invisible3, 11);
		panel.add(invisible4, 12);
		panel.add(startBtn, 13);
		
		checkBoxListener = e -> {
			while (panel.getComponentCount() > 4) {
				panel.remove(4);
			}
			if (remote.isSelected()) {
				panel.add(hostlbl, 4);
				panel.add(hostField, 5);
				panel.add(namelbl, 6);
				panel.add(nameField, 7);
				if (createUser.isSelected()) {
					panel.add(spwlabel, 8);
					panel.add(serverPWField, 9);
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
					panel.add(xlenField, 7);
					panel.add(ylenlbl, 8);
					panel.add(ylenField, 9);
					startBtn.setText("create new world");
				} else {
					panel.add(loadBtn, 6);
					panel.add(selectdFileField, 7);
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
			if (chosen != JFileChooser.APPROVE_OPTION) { return; }
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(frame, "there is no such file", "file not found", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!file.isFile()) {
				JOptionPane.showMessageDialog(frame, "the selected path is no 'normal' file", "no file", JOptionPane.ERROR_MESSAGE);
				return;
			}
			selectdFileField.setText(file.toString());
		});
		startBtn.addActionListener(e -> {
			try {
				World world;
				if (remote.isSelected()) {
					if (createUser.isSelected()) {
						world = connectToRemoteWorld(pwField, serverPWField.getPassword(), hostField, nameField);
					} else {
						world = connectToRemoteWorld(pwField, null, hostField, nameField);
					}
				} else if (createNew.isSelected()) {
					world = createNewWorld(pwField, xlenDoc, ylenDoc);
				} else {
					world = loadWorldFromFile(pwField, selectdFileField);
				}
				SquareConquererGUI gui = new SquareConquererGUI(world);
				frame.dispose();
				gui.load();
			} catch (Exception err) {
				JOptionPane.showMessageDialog(frame, "error: " + err.getMessage(), err.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		});
		
		frame.pack();
		frame.setLocationByPlatform(true);
		return frame;
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
		RootUser root = RootUser.create(pw.getPassword());
		return new RootWorld.Builder(root, Math.max(1, xlenDoc.getNumber()), Math.max(1, ylenDoc.getNumber()));
	}
	
	private static World loadWorldFromFile(JPasswordField pw, JTextField selectdFile) throws IOException {
		RootUser root = RootUser.create(pw.getPassword());
		try (FileInputStream in = new FileInputStream(selectdFile.getText()); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, root)) {
			Tile[][] tiles;
			root.load(conn);
			tiles = RemoteWorld.loadWorld(conn, root.users());
			return RootWorld.Builder.create(root, tiles);
		} catch (Throwable t) {
			root.close();
			throw t;
		}
	}
	
}
