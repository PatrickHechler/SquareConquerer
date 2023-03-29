package de.hechler.patrick.games.squareconqerer.ui;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.Tile;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.connect.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.connect.RemoteWorld;

public class SquareConquererCUI implements Runnable {
	
	private static final String HELP = "help";
	
	// TODO more commands
	private static final String CMD_HELP    = HELP;
	private static final String CMD_VERSION = "version";
	private static final String CMD_STATUS  = "status";
	
	private static final Pattern PTRN_ARG       = Pattern.compile("([^\\s\\\\'\"]+|'[^']*'|\"[^\"]*\")+");
	private static final Pattern PTRN_STR   = Pattern.compile("'([^']*|\\\\.)*'|\"([^\"]*|\\\\.)*\"");
	private static final Pattern PTRN_BACKSLASH = Pattern.compile("\\\\(.)");
	
	private static List<String> arguments(String line) {
		List<String> args       = new ArrayList<>();
		Matcher      argMatcher = PTRN_ARG.matcher(line);
		while (argMatcher.find()) {
			String        arg        = argMatcher.group(0);
			Matcher       strMatcher = PTRN_STR.matcher(arg);
			StringBuilder b          = null;
			int           off        = 0;
			while (strMatcher.find()) {
				if (b == null) {
					b = new StringBuilder();
				}
				b.append(arg, off, strMatcher.start());
				String str = arg.substring(strMatcher.start() + 1, strMatcher.end() - 1);
				b.append(str);
				off = strMatcher.end();
			}
			if (b != null) {
				b.append(arg, off, arg.length());
				arg = b.toString();
			}
			Matcher bsMatcher = PTRN_BACKSLASH.matcher(arg);
			arg = bsMatcher.replaceAll("$1");
			args.add(arg);
		}
		return args;
	}
	
	private final Con c;
	
	private User   usr;
	private World  world;
	private Thread serverThread;
	private String username;
	private char[] serverPW;
	
	private List<Object> tasks = new LinkedList<>();
	
	public SquareConquererCUI() {
		Console console = System.console();
		if (console != null) {
			this.c = new ConsoleCon(console);
		} else {
			this.c = new IOCon(new Scanner(System.in), System.out);
		}
	}
	
	public void setWorld(World world, Thread serverThread) {
		this.world        = world;
		this.serverThread = serverThread;
		this.usr          = world.user();
	}
	
	public void setUsr(User usr) { this.usr = usr; }
	
	public void setName(String name) { this.username = name; }
	
	public void setSPW(char[] serverpw) { this.serverPW = serverpw; }
	
	public void startLoad(Path worldFile) { tasks.add(worldFile); }
	
	public void startServer(int p) { tasks.add(new StartServerTask(p)); }
	
	public void startConnect(String host, int p) { tasks.add(new ConnectToServerTask(host, p)); }
	
	private static class StartServerTask {
		
		private final int port;
		
		private StartServerTask(int port) { this.port = port; }
		
	}
	
	private static class ConnectToServerTask {
		
		private final String host;
		private final int    port;
		
		private ConnectToServerTask(String host, int port) { this.host = host; this.port = port; }
		
	}
	
	@Override
	public void run() {
		boolean first = true;
		while (true) {
			doTasks();
			if (first && c instanceof ConsoleCon) { // no need to greet scripts
				c.writeLine("Welcome to the Square Conquerer Console");
				c.writeLine("if you don't know what to do use the '" + HELP + "' command");
				first = false;
			}
			String line = c.readLine(prompt()).trim();
			if (line.isEmpty()) {
				continue;
			}
			List<String> args = arguments(line);
			if (args.isEmpty()) {
				c.writeLine("stange, there is nothing");
				continue;
			}
			switch (args.get(0).toLowerCase()) {
			case CMD_HELP -> cmdHelp(args);
			case CMD_VERSION -> cmdVersion(args);
			case CMD_STATUS -> cmdStatus(args);
			default -> c.writeLine("unknown command: '" + args.get(0) + "'");
			}
		}
	}
	
	private void cmdStatus(List<String> args) {
		if (args.size() == 1) {
			cmdStatusUser();
			cmdStatusWorld();
			cmdStatusServer();
			cmdStatusServerPW();
		} else {
			for (int i = 1; i < args.size(); i++) {
				switch (args.get(i)) {
				case HELP -> {
					c.writeLine("Status command: help");
					c.writeLine("without arguments, write all status types");
					c.writeLine("with arguments:");
					c.writeLine("  " + HELP + " write this message");
					c.writeLine("  'user' write the user status");
					c.writeLine("  'world-remote-size' update the size of the remote world and then write the world status");
					c.writeLine("  'world-remote-all' or 'world-remote-world' update the complete remote world and then write the world status");
					c.writeLine("  'server' write the server status");
					c.writeLine("  'serverpw' or 'server-pw' write the server password status");
				}
				case "user" -> cmdStatusUser();
				case "world" -> cmdStatusWorld();
				case "world-remote-size" -> cmdStatusWorldRemoteSize();
				case "world-remote-all", "world-remote-world" -> cmdStatusWorldRemoteAll();
				case "server" -> cmdStatusServer();
				case "serverpw", "server-pw" -> cmdStatusServerPW();
				default -> c.writeLine("unknown argument: '" + args.get(i) + "'");
				}
			}
		}
	}
	
	private void cmdStatusServerPW() {
		if (serverPW != null) {
			c.writeLine("serverPassword: set");
		} else {
			c.writeLine("serverPassword: there is no server password");
			if (serverThread != null) {
				c.writeLine("  note that I remove my reference of the server password after starting the server");
				c.writeLine("  only because I do not know a server password, does not mean that the server knows no password");
			}
		}
	}
	
	private void cmdStatusServer() {
		if (serverThread != null) {
			c.writeLine("MyServer: running");
		} else {
			c.writeLine("MyServer: there is no server");
		}
	}
	
	private void cmdStatusWorldRemoteAll() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			c.writeLine("there is no remote world");
			return;
		}
		try {
			rw.updateWorld();
			c.writeLine("World: remote world loaded");
			c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorldRemoteSize() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			c.writeLine("there is no remote world");
			return;
		}
		try {
			rw.updateWorldSize();
			c.writeLine("World: remote world loaded");
			c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorld() {
		boolean writeBounds = true;
		if (world == null) {
			c.writeLine("World: there is no world");
			writeBounds = false;
		} else if (world instanceof RootWorld) {
			c.writeLine("World: root world loaded");
		} else if (world instanceof RootWorld.Builder) {
			c.writeLine("World: builder world loaded");
		} else if (world instanceof RemoteWorld rw) {
			c.writeLine("World: remote world loaded");
			if (rw.loadedBounds()) {
				c.writeLine("  Bounds: not loaded");
				writeBounds = false;
			}
		} else if (world instanceof UserWorld) {
			c.writeLine("World: user world loaded");
		} else {
			c.writeLine("World: unknown world type loaded");
			c.writeLine("  Type: " + world.getClass().getSimpleName());
		}
		if (writeBounds) {
			c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		}
	}
	
	private void cmdStatusUser() {
		if (usr == null) {
			c.writeLine("User: there is no User");
			if (username != null) {
				c.writeLine("  UserName: " + username);
			}
		} else {
			c.writeLine("User: Name: " + usr.name());
		}
	}
	
	private void cmdVersion(List<String> args) {
		if (args.size() == 1) {
			c.writeLine("Square Conquerer Console version: " + SquareConquererStart.VERSION_STRING);
		} else if (args.size() == 2 && HELP.equals(args.get(1))) {
			c.writeLine("without args, I only write some version information and I can not do much more");
			c.writeLine("with one argument, I write this message, when the argumentis the " + HELP + " argument");
			c.writeLine("In all other cases I print an error message");
		} else {
			c.writeLine("I dont know what I should do with arguments!");
		}
	}
	
	private void cmdHelp(List<String> args) {
		switch (args.size()) {
		case 1 -> {
			c.writeLine("Square Conquerer Console " + SquareConquererStart.VERSION_STRING + " help:");
			c.writeLine("Commands:");
			c.writeLine("  " + CMD_HELP);
			c.writeLine("    print this message");
			c.writeLine("  " + CMD_VERSION);
			c.writeLine("    print version information");
			c.writeLine("  " + CMD_STATUS);
			c.writeLine("    print status information");
			c.writeLine("General:");
			c.writeLine("  all comands support the " + HELP + " argument");
			c.writeLine("  If you want further information for a specific command, ask the command");
			c.writeLine("  or use " + CMD_HELP + " <command>");
		}
		case 2 -> {
			switch (args.get(1).toLowerCase()) {
			case CMD_HELP -> c.writeLine("print a general help message or the help message of the argument");
			case CMD_VERSION -> cmdVersion(args.subList(1, 2));
			case CMD_STATUS -> cmdStatus(args.subList(1, 2));
			default -> c.writeLine("I don't know this command (" + args.get(1) + ")");
			}
		}
		default -> c.writeLine("either give me the name of an command as argument or no argument");
		}
	}
	
	private String prompt() {
		if (usr != null) {
			return "[" + usr.name() + "]: ";
		} else if (username != null) {
			return "(" + username + "): ";
		} else {
			return "(---): ";
		}
	}
	
	private void doTasks() throws AssertionError {
		while (!tasks.isEmpty()) {
			Object task = tasks.remove(0);
			if (task == null) {
				continue;
			}
			if (task instanceof Path loadFile) {
				doTaskLoadFile(loadFile);
			} else if (task instanceof StartServerTask sst) {
				doTaskStartServer(sst);
			} else if (task instanceof ConnectToServerTask cst) {
				doTaskConnectToServer(cst);
			} else {
				throw new AssertionError("task has an unknown class: " + task.getClass().getName());
			}
		}
	}
	
	private void doTaskConnectToServer(ConnectToServerTask cst) {
		boolean askUsr = usr != null;
		boolean askUN  = username != null;
		do {
			if (askUsr) {
				if (askUN) {
					username = c.readLine("enter now your username: ");
				}
				char[] pw = c.readPassword("enter now your password: ");
				usr = User.create(username, pw);
			}
			if (usr != null) {
				try {
					Connection conn;
					if (serverPW != null) {
						conn = Connection.ClientConnect.connectNew(cst.host, cst.port, usr, serverPW);
					} else {
						conn = Connection.ClientConnect.connect(cst.host, cst.port, usr);
					}
					world = new RemoteWorld(conn);
					c.writeLine("connected to the server, remote world created");
				} catch (IOException e) {
					c.writeLine("could not connect to the server: " + e.toString());
				}
			}
		} while (retry(world == null));
	}
	
	private void doTaskStartServer(StartServerTask sst) {
		if (world == null) {
			boolean load;
			while (true) {
				String line = c.readLine("[l]oad world or create a [n]ew world?").trim();
				if (line.isEmpty()) {
					continue;
				}
				switch (line.charAt(0)) {
				case 'l', 'L' -> load = true;
				case 'n', 'N' -> load = false;
				default -> {
					continue;
				}
				}
				break;
			}
			if (load) {
				subTaskStartServerLoadWorld();
			} else {
				subTaskStartServerNewWorld();
			}
		}
		if (world != null) {
			final RootWorld rw = (RootWorld) world;
			serverThread = threadBuilder().start(() -> {
				try {
					Connection.ServerAccept.accept(sst.port, (conn, sok) -> {
						c.writeLine("accepted connection from '" + conn.usr.name() + "' (" + sok.getInetAddress() + ")");
						UserWorld uw = rw.of(conn.usr, conn.modCnt());
						OpenWorld ow = new OpenWorld(conn, uw);
						ow.execute();
					}, (RootUser) usr, serverPW);
				} catch (IOException e) {
					c.writeLine("error: " + e.toString());
				}
			});
			c.writeLine("the server should now accept connections");
		}
	}
	
	private void subTaskStartServerNewWorld() {
		int xlen = readPositiveNumber("enter now the X-length of the world (Width): ");
		if (xlen > 0) {
			int ylen = readPositiveNumber("enter now the Y-length of the world (Height): ");
			if (ylen > 0) {
				RootWorld.Builder b = new RootWorld.Builder((RootUser) usr, xlen, ylen);
				b.fillRandom();
				world = b.create();
			}
		}
	}
	
	private void subTaskStartServerLoadWorld() {
		boolean askPW = usr == null;
		boolean fail;
		do {
			Path loadFile;
			do {
				loadFile = Path.of(c.readLine("enter now the path of the save-file: "));
				if (!Files.exists(loadFile)) {
					c.writeLine("the file '" + loadFile + "' does not exist");
					continue;
				}
				if (!Files.isRegularFile(loadFile)) {
					c.writeLine("the pat '" + loadFile + "' does not refer to a file");
					continue;
				}
				break;
			} while (retry(true));
			if (askPW) {
				char[] pw = c.readPassword("enter now the password: ");
				usr = RootUser.create(pw);
			} else {
				usr = usr.makeRoot();
			}
			fail = loadFile(loadFile);
		} while (retry(fail));
	}
	
	private void doTaskLoadFile(Path loadFile) {
		boolean askUsr = usr == null;
		do {
			if (askUsr) {
				c.writeLine("load now the file '" + loadFile + "'");
				char[] pw = c.readPassword("enter the password: ");
				if (username != null && !RootWorld.ROOT_NAME.equals(username)) {
					c.writeLine("changed to root user");
				}
				usr = RootUser.create(pw);
			} else if (!(usr instanceof RootUser)) {
				c.writeLine("changed to root user");
				usr = usr.makeRoot();
			} else { // reset other users
				usr = usr.makeRoot();
			}
		} while (retry(loadFile(loadFile)));
	}
	
	private int readPositiveNumber(String prompt) {
		int xlen = -1;
		do {
			String line = c.readLine(prompt);
			try {
				xlen = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				c.writeLine("error: " + e.toString());
			}
		} while (retry(xlen <= 0));
		return xlen;
	}
	
	private boolean retry(boolean failed) {
		if (!failed) return false;
		while (true) {
			String line = c.readLine("retry? ([y]es|[n]o)").trim();
			if (line.isEmpty()) continue;
			switch (line.charAt(0)) {
			case 'y', 'Y':
				return true;
			case 'n', 'N':
				return false;
			default:
			}
		}
	}
	
	private boolean loadFile(Path loadFile) {
		try (InputStream in = Files.newInputStream(loadFile)) {
			Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr);
			RootUser   root = (RootUser) usr;
			root.load(conn);
			Tile[][] tiles = RemoteWorld.readWorld(conn, null, false);
			world = RootWorld.Builder.createBuilder(root, tiles);
			try {
				world = ((RootWorld.Builder) world).create();
			} catch (IllegalStateException ise) {
				c.writeLine("could not build the world: " + ise);
				c.writeLine("the current world is in building mode");
			}
			username = null;
			c.writeLine("world successful loaded");
			return false;
		} catch (IOException e) {
			usr = null;
			c.writeLine("error: " + e.toString());
			return true;
		}
	}
	
}
