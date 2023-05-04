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

import static de.hechler.patrick.games.squareconqerer.Settings.threadStart;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hechler.patrick.games.squareconqerer.Settings;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RemoteWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;

/**
 * this class is used to communicate with the user using a console or streams
 * 
 * @author Patrick Hechler
 */
public class SquareConquererCUI implements Runnable {
	
	private static final String HELP = "help"; //$NON-NLS-1$
	
	private static final String CMD_HELP     = HELP;
	private static final String CMD_VERSION  = "version"; //$NON-NLS-1$
	private static final String CMD_STATUS   = "status"; //$NON-NLS-1$
	private static final String CMD_USERNAME = "username"; //$NON-NLS-1$
	private static final String CMD_WORLD    = "world"; //$NON-NLS-1$
	private static final String CMD_SERVER   = "server"; //$NON-NLS-1$
	private static final String CMD_SETPW    = "setpw"; //$NON-NLS-1$
	private static final String CMD_SERVERPW = "serverpw"; //$NON-NLS-1$
	private static final String CMD_QUIT     = "quit"; //$NON-NLS-1$
	private static final String CMD_EXIT     = "exit"; //$NON-NLS-1$
	
	private static final Pattern PTRN_ARG       = Pattern.compile("([^\\s\\\\'\"]+|'[^']*'|\"[^\"]*\")+"); //$NON-NLS-1$
	private static final Pattern PTRN_STR       = Pattern.compile("[^\\\\]('([^'\\\\]*|\\\\.)*'|\"([^\"\\\\]*|\\\\.)*\")"); //$NON-NLS-1$
	private static final Pattern PTRN_BACKSLASH = Pattern.compile("\\\\(.)"); //$NON-NLS-1$
	
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
				b.append(arg, off, strMatcher.start() + 1);
				String str = arg.substring(strMatcher.start() + 2, strMatcher.end() - 1);
				b.append(str);
				off = strMatcher.end();
			}
			if (b != null) {
				b.append(arg, off, arg.length());
				arg = b.toString();
			}
			Matcher bsMatcher = PTRN_BACKSLASH.matcher(arg);
			arg = bsMatcher.replaceAll("$1"); //$NON-NLS-1$
			args.add(arg);
		}
		return args;
	}
	
	private final Cons c;
	private boolean    interactive;
	
	private volatile Thread                serverThread;
	private volatile Map<User, Connection> connects;
	
	private User   usr;
	private World  world;
	private String username;
	private char[] serverPW;
	
	private List<Object> tasks = new LinkedList<>();
	
	/**
	 * this is equivalent to <code>{@link #SquareConquererCUI(Cons) new SquareConquererCU}({@link System#console()} != null ?
	 * {@link ConsoleCons#ConsoleCons(Console) new ConsoleCons}({@link System#console()}) :
	 * {@link IOCons#IOCons(Scanner, java.io.PrintStream) new IOCons}
	 * ({@link Scanner#Scanner(InputStream) new Scanner}({@link System#in}, {@link System#out})))</code>
	 */
	public SquareConquererCUI() {
		Console console = System.console();
		if (console != null) {
			this.c           = new ConsoleCons(console);
			this.interactive = true;
		} else {
			Scanner sc = new Scanner(System.in);
			this.c           = new IOCons(sc, System.out);
			this.interactive = false;
		}
	}
	
	/**
	 * this is equivalent to <code>{@link #SquareConquererCUI(Cons, boolean) new SquareConquererCUI}(c, c instanceof {@link ConsoleCons})</code>
	 * 
	 * @param c the {@link Cons} to be used
	 */
	public SquareConquererCUI(Cons c) {
		this(c, c instanceof ConsoleCons);
	}
	
	/**
	 * creates a new {@link SquareConquererCUI} using the given {@link Cons} and mode
	 * 
	 * @param c           the {@link Cons} to be used
	 * @param interactive if the {@link Cons} should be in interactive mode or not
	 */
	public SquareConquererCUI(Cons c, boolean interactive) {
		if (c == null) {
			throw new NullPointerException("there is no Cons");
		}
		this.c           = c;
		this.interactive = interactive;
	}
	
	public void setInteractive(boolean interactive) { this.interactive = interactive; }
	
	public void setWorld(World world, Thread serverThread, Map<User, Connection> connects) {
		this.world        = world;
		this.serverThread = serverThread;
		this.connects     = connects;
		this.usr          = world.user();
		if (serverThread != null) {
			threadStart(() -> {
				while (serverThread.isAlive()) {
					try {
						serverThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				synchronized (SquareConquererCUI.this) {
					if (this.serverThread == serverThread) {
						this.serverThread = null;
						this.connects     = null;
					}
				}
			});
		}
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
		boolean greet = this.interactive;
		while (true) {
			doTasks();
			if (greet) { // no need to greet scripts
				this.c.writeLine("Welcome to the Square Conquerer Console");
				this.c.writeLine("if you don't know what to do use the '" + HELP + "' command");
				greet = false;
			}
			String line = (this.interactive ? this.c.readLine(prompt()) : this.c.readLine()).trim();
			if (line.isEmpty()) {
				continue;
			}
			List<String> args = arguments(line);
			if (args.isEmpty()) {
				this.c.writeLine("stange, there is nothing");
				continue;
			}
			exec(args);
		}
	}
	
	private void exec(List<String> args) {
		try {
			switch (args.get(0).toLowerCase()) {
			case CMD_HELP -> cmdHelp(args);
			case CMD_VERSION -> cmdVersion(args);
			case CMD_STATUS -> cmdStatus(args);
			case CMD_USERNAME -> cmdUsername(args);
			case CMD_WORLD -> cmdWorld(args);
			case CMD_SERVER -> cmdServer(args);
			case CMD_SETPW -> cmdSetPW(args);
			case CMD_SERVERPW -> cmdServerPW(args);
			case CMD_QUIT, CMD_EXIT -> cmdQuit(args);
			default -> this.c.writeLine("unknown command: '" + args.get(0) + "'");
			}
		} catch (RuntimeException e) {
			this.c.writeLine("error while executing the command: " + e.toString());
		}
	}
	
	private void cmdQuit(List<String> args) {
		if (args.size() == 1) {
			if (this.interactive) {
				this.c.writeLine("goodbye, exit now with 0");
			}
			System.exit(0);
		}
		for (int i = 1; i < args.size(); i++) {
			if (HELP.equalsIgnoreCase(args.get(i))) {
				this.c.writeLine("quit help:");
				this.c.writeLine("no args: terminates this program with the exit value 0");
				this.c.writeLine("help: print this message");
				this.c.writeLine("<NUMBER>: terminates this program with the given exit value");
				this.c.writeLine("anything else: terminates this program with the exit value 1");
			} else {
				try {
					int e = Integer.parseInt(args.get(i));
					this.c.writeLine("goodbye, exit now with " + e);
					System.exit(e);
				} catch (NumberFormatException e) {
					this.c.writeLine("error while parsing exit number: " + e.toString());
					this.c.writeLine("goodbye, exit now with 1");
					System.exit(1);
				}
			}
		}
	}
	
	private void cmdServerPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask("do you want to [s]et" + (serverPW != null ? "/[r]emove" : "") + " the server password or do [n]othing? ",
				serverPW != null ? "srn" : "sn")) {
			case 's' -> serverPW = this.c.readPassword("enter now the new server password: ");
			case 'r' -> serverPW = null;
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask");
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLine("server password help:");
				this.c.writeLine("status: print an message indicating if there is currently a server password set");
				this.c.writeLine("set: prompt for the new server password");
				this.c.writeLine("remove: remove and clear the current server password");
				this.c.writeLine("remove-no-fail: same as remove, but do not show an error message if there is no server password");
			}
			case "status" -> {
				if (serverPW != null) {
					this.c.writeLine("I hava a server password set");
				} else {
					this.c.writeLine("I don't have a server password set");
				}
			}
			case "set" -> {
				if (++i >= args.size()) {
					this.c.writeLine("not enugh arguments for the set arg");
					return;
				}
				serverPW = this.c.readPassword("enter the new server password");
				this.c.writeLine("the server password was updated");
			}
			case "remove" -> {
				if (serverPW == null) {
					this.c.writeLine("there is no server password I could remove");
					return;
				}
				serverPW = null;
			}
			case "remove-no-fail" -> serverPW = null;
			default -> this.c.writeLine("unknown argument: " + args.get(i));
			}
		}
	}
	
	private void cmdSetPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask("set [y]our password" + (usr instanceof RootUser ? ", [s]omeones password" : "") + " or do [n]othing? ",
				usr instanceof RootUser ? "ysn" : "yn")) {
			case 'y' -> setMyPW();
			case 's' -> {
				String name = this.c.readLine("enter now the username of the given user: ");
				User   user = ((RootUser) usr).get(name);
				if (user == null) {
					this.c.writeLine("the user '" + name + "' could not be found");
					return;
				}
				char[] npw = this.c.readPassword("enter now the new password of the user: ");
				((RootUser) usr).changePW(user, npw);
				this.c.writeLine("the password of the user '" + name + "' changed");
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask");
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLine("setpw help:");
				this.c.writeLine("without args: prompt whoses password should be changed and then change the password");
				this.c.writeLine("args:");
				this.c.writeLine(HELP + ": print this message");
				this.c.writeLine("set/of <USERNAME>: prompt for the new password of USERNAME");
				this.c.writeLine("me: prompt for your new password");
				this.c.writeLine("  note, that if you are connected to an server, that connection will become invalid");
				this.c.writeLine("<USERNAME>: prompt for the new password of USERNAME");
				this.c.writeLine("  note, that if the username is 'set' or 'me' they are triggered instead");
			}
			case "set", "of" -> {
				if (++i >= args.size()) {
					this.c.writeLine("there are not enugh arguments for the set/of argument");
					return;
				}
				setPW(args.get(i));
			}
			case "me" -> setMyPW();
			default -> setPW(args.get(i));
			}
		}
	}
	
	private void setPW(String arg) {
		if (arg.equals(username) || arg.equals(usr == null ? RootUser.ROOT_NAME : usr.name())) {
			setMyPW();
		} else if (!(usr instanceof RootUser root)) {
			this.c.writeLine("this is not your username and you are not root");
		} else {
			setOtherPW(arg, root);
		}
	}
	
	private void setOtherPW(String name, RootUser root) {
		User user = root.get(name);
		if (user == null) {
			this.c.writeLine("the user '" + name + "' could not be found");
		}
		char[] npw = this.c.readPassword("enter now the new password of the user: ");
		((RootUser) usr).changePW(user, npw);
		this.c.writeLine("the password of the user '" + name + "' changed");
	}
	
	private void setMyPW() {
		char[] pw = this.c.readPassword("enter your new password: ");
		if (username != null) {
			usr = User.create(username, pw);
		} else if (usr == null) {
			usr = RootUser.create(pw);
		} else {
			usr.changePassword(pw);
		}
		this.c.writeLine("your password was changed");
	}
	
	private void cmdServer(List<String> args) {
		if (args.size() == 1) {
			cmdServerNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLine("server help:");
				this.c.writeLine("base commands:");
				this.c.writeLine("  status: print an message indicating if you are running a server/are connected to a server or nothing of it");
				this.c.writeLine("remote server commands:");
				this.c.writeLine("  connect <SERVER_ADDRESS>: connect to the given server");
				this.c.writeLine("  disconnect: disconnect from the server you are currently connected to");
				this.c.writeLine("your server commands:");
				this.c.writeLine("  start [PORT]: start your own server");
				this.c.writeLine("    use minus ('-') instead of PORT to use the defaul port " + Connection.DEFAULT_PORT);
				this.c.writeLine("    if the loaded world is no root world, accepted connections will not get an user world, but the direct world");
				this.c.writeLine("    you need to be logged in as root");
				this.c.writeLine("  stop: stop your own server");
			}
			case "status" -> {
				if (serverThread != null) {
					this.c.writeLine("the server thread is currently running");
				} else if (world instanceof RemoteWorld) {
					this.c.writeLine("your loaded world is a remote world");
				} else {
					this.c.writeLine("it seems that there is no server running and your world does not seem to be connected to some server");
				}
			}
			case "connect" -> {
				if (++i >= args.size()) {
					this.c.writeLine("there are not enugh arguments for the connect argument");
					return;
				}
				if (usr == null) {
					this.c.writeLine("you are not logged in, retry after you logged in (look at " + CMD_SETPW + " and " + CMD_USERNAME + ')');
					return;
				}
				int    port = Connection.DEFAULT_PORT;
				String addr = args.get(i);
				int    li   = addr.lastIndexOf(':');
				if (li != -1 && li - 1 == addr.lastIndexOf(']') && addr.charAt(0) == '[') {
					try {
						port = Integer.parseInt(addr.substring(li + 1));
						addr = addr.substring(1, li - 1);
					} catch (NumberFormatException e) {
						this.c.writeLine("could not parse the port: " + e.toString());
						return;
					}
				}
				try {
					Connection conn;
					if (serverPW != null) {
						conn = Connection.ClientConnect.connectNew(addr, port, usr, serverPW);
					} else {
						conn = Connection.ClientConnect.connect(addr, port, usr);
					}
					world = new RemoteWorld(conn);
				} catch (IOException e) {
					this.c.writeLine("error while connecting: " + e.toString());
					return;
				}
			}
			case "disconnect" -> {
				if (!(world instanceof RemoteWorld rw)) {
					this.c.writeLine("I don't know any server connection");
					return;
				}
				world = null;
				try {
					rw.close();
					this.c.writeLine("closed old remote world successfully");
				} catch (IOException e) {
					this.c.writeLine("error while closing: (proceed anyway, do not retry) " + e.toString());
				}
			}
			case "start" -> {
				if (++i >= args.size()) {
					this.c.writeLine("there are not enugh arguments for the start argument");
					return;
				}
				synchronized (this) {
					if (serverThread != null) {
						this.c.writeLine("I am already running an server");
						return;
					}
					if (world == null) {
						this.c.writeLine("there is no world loaded");
						return;
					}
					if (!(world instanceof RootWorld rw)) {
						this.c.writeLine("your loaded world is no root world");
						return;
					}
					try {
						int    port = "-".equals(args.get(i)) ? Connection.DEFAULT_PORT : Integer.parseInt(args.get(i));
						char[] spw  = serverPW;
						serverPW = null;
						Map<User, Connection> cs = new HashMap<>();
						this.connects = cs;
						serverThread  = threadStart(() -> {
											try {
												Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
																	if (sok == null) {
																		this.c.writeLine("the user '" + conn.modCnt() + "' disconnected");
																	} else {
																		this.c.writeLine("the user '" + conn.modCnt() + "' logged in from " + sok.getInetAddress());
																	}
																}, cs, spw);
											} catch (IOException e) {
												this.c.writeLine("error at the server thread: " + e.toString());
											} finally {
												synchronized (SquareConquererCUI.this) {
													if (serverThread == Thread.currentThread()) {
														serverThread = null;
														connects     = null;
													}
												}
											}
										});
						this.c.writeLine("started the server thread");
					} catch (NumberFormatException e) {
						this.c.writeLine("error parsing the port: " + e.toString());
						return;
					}
				}
			}
			case "stop" -> {
				Thread st = serverThread;
				if (st == null) {
					this.c.writeLine("there seems to be no server running");
					return;
				}
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					this.c.writeLine("I was interrupted: " + e.toString());
				}
				if (st.isAlive()) {
					this.c.writeLine("I told the server thread to stop");
				} else {
					this.c.writeLine("the server stopped");
				}
				Map<User, Connection> cs = connects;
				if (cs != null) {
					connects = null;
					for (Connection conn : cs.values()) {
						try {
							conn.logOut();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			default -> this.c.writeLine("unknown argument: " + args.get(i));
			}
		}
	}
	
	private void cmdServerNoArgs() {
		Thread st = serverThread;
		if (st != null) {
			this.c.writeLine("the server is currently running");
			if (ask("dou you want to [c]lose the server [n]othing?", "cn") == 'c') {
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (st.isAlive()) {
					this.c.writeLine("I told the server thread to stop");
				} else {
					this.c.writeLine("stopped the server, all remote connections should be closed now");
				}
			}
		} else if (world instanceof RemoteWorld rw) {
			this.c.writeLine("you are currently connected to a server");
			if (ask("dou you want to [d]isconnect or do [n]othing?", "dn") == 'd') {
				World nw = null;
				if (ask("dou you want to contain your current world in build mode? ([y]es|[n]o)", "yn") == 'y') {
					int               xlen = rw.xlen();
					int               ylen = rw.ylen();
					RootWorld.Builder b    = new RootWorld.Builder(usr.rootClone(), xlen, ylen);
					for (int x = 0; x < xlen; x++) {
						for (int y = 0; y < ylen; y++) {
							b.set(x, y, world.tile(x, y));
						}
					}
					nw = b;
				}
				try {
					rw.close();
					this.c.writeLine("closed old world successfully");
				} catch (IOException e) {
					this.c.writeLine("error while closing: (proceed anyway, do not retry) " + e.toString());
				}
				world = nw;
			}
		} else if (usr == null) {
			this.c.writeLine("you are not logged in, retry after you logged in (take a look at the " + CMD_SETPW + " and " + CMD_USERNAME + " commands)");
		} else {
			switch (ask("do you want to [c]onnect to a server" + (world instanceof RootWorld ? ", [s]tart a server" : "") + " or do [n]othing?",
				world instanceof RootWorld ? "csn" : "cn")) {
			case 'c' -> {
				if (world != null && ask("if you proceed you current world will be discarded. ([p]rocced/[c]ancel)? ", "pc") == 'c') {
					return;
				}
				String host = this.c.readLine("type now the serverhost: ").trim();
				int    port = Connection.DEFAULT_PORT;
				int    li   = host.lastIndexOf(':');
				if (li != -1 && li - 1 == host.lastIndexOf(']') && host.charAt(0) == '[') {
					try {
						port = Integer.parseInt(host.substring(li + 1));
						host = host.substring(1, li - 1);
					} catch (NumberFormatException e) {
						this.c.writeLine("error while parsing the port: " + e.toString());
						return;
					}
				}
				Connection conn;
				try {
					char[] sp = serverPW;
					if (sp != null) {
						serverPW = null;
						conn     = Connection.ClientConnect.connectNew(host, port, usr, sp);
					} else {
						conn = Connection.ClientConnect.connect(host, port, usr);
					}
					world = new RemoteWorld(conn);
					this.c.writeLine("connected successful to " + host + " (at port " + port + ')');
				} catch (IOException e) {
					this.c.writeLine("error: " + e.toString());
				}
			}
			case 's' -> {
				RootWorld rw      = (RootWorld) world;
				String    portStr = this.c.readLine("enter now the port on which the server should listen (default is " + Connection.DEFAULT_PORT + "): ").trim();
				int       port    = portStr.isEmpty() ? Connection.DEFAULT_PORT : Integer.parseInt(portStr);
				char[]    sp      = serverPW;
				serverPW = null;
				Map<User, Connection> cs = new HashMap<>();
				connects = cs;
				threadStart(() -> {
					try {
						Map<User, Connection> cs0 = new HashMap<>();
						connects = cs0;
						Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
							if (sok == null) {
								this.c.writeLine("the user '" + conn.usr.name() + "' disconnected");
							} else {
								this.c.writeLine("the user '" + conn.usr.name() + "' logged in from " + sok.getInetAddress());
							}
						}, cs0, sp);
					} catch (IOException e) {
						this.c.writeLine("error on server thread: " + e.toString());
					}
				});
				this.c.writeLine("started server");
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask");
			}
		}
	}
	
	private void cmdWorld(List<String> args) {
		if (args.size() == 1) {
			cmdWorldNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLine(CMD_WORLD + " help: change or display the world");
				this.c.writeLine("base commands: (those work always)");
				this.c.writeLine("  " + HELP + ": print this message");
				this.c.writeLine("  load-all <SAVE_FILE>: load everything from the file");
				this.c.writeLine("    the loaded world will be in root mode");
				this.c.writeLine("    if there is no user this operation will fail");
				this.c.writeLine("    if the user is currently not root, it will be after this operation");
				this.c.writeLine("    if the user already is root, all subusers will be deleted");
				this.c.writeLine("    the SAVE_FILE has to be created with save-all or save-all-force");
				this.c.writeLine("  load <SAVE_FILE>: load other users and world from the file");
				this.c.writeLine("    the loaded world will be in build mode");
				this.c.writeLine("    if there is no user this operation will fail");
				this.c.writeLine("    if the user is currently not root, it will be after this operation");
				this.c.writeLine("    if the user already is root, all subusers will be deleted");
				this.c.writeLine("    the SAVE_FILE has to be created with save or save-force");
				this.c.writeLine("  create <X-LEN/WIDTH> <Y-LEN/HEIGHT>: create a new build world with the given sizes");
				this.c.writeLine("    note that this command needs you to be logged in");
				this.c.writeLine("    note that this command will convert you to a new root user");
				this.c.writeLine("    the newly created world will be in build mode");
				this.c.writeLine("simple commands: (work, when there is an world)");
				this.c.writeLine("  print or print.types: print all tile types of the world");
				this.c.writeLine("  print.resources: print all resources of the world");
				this.c.writeLine("  tile <X> <Y>: print the tile at the given coordinates");
				this.c.writeLine("  to-build: convert the world to a build world");
				this.c.writeLine("  save <FILE>: save the current world to the given file");
				this.c.writeLine("  save-force <FILE>: like save, but do not ask if the file already exist");
				this.c.writeLine("root world commands: (world needs to be in root mode)");
				this.c.writeLine("  save-all <FILE>: save everything to the given file");
				this.c.writeLine("  save-all-force <FILE>: like save-all, but do not ask if the file already exist");
				this.c.writeLine("build world commands: (world needs to be in build mode)");
				this.c.writeLine("  build: convert the world to a root world");
				this.c.writeLine("  tile.type <TYPE> <X> <Y>: set the type of the given tile");
				this.c.writeLine("    valid TYPE values: water sand grass forest swamp mountain not-explored");
				this.c.writeLine("    [sand grass forest swamp] accept the '+hill' suffix");
				this.c.writeLine("    [water] accepts the '+deep' suffix");
				this.c.writeLine("    all types except of not-explored accept the '+normal' suffix, which is just an alias for no suffix");
				this.c.writeLine("    if there is only a suffix given it will replace the current suffix (if this is valid)");
				this.c.writeLine("  tile.resource <RESOURCE> <X> <Y>: set the resource of the given tile");
				this.c.writeLine("    valid RESOURCE values: none gold iron coal");
				this.c.writeLine("  fill-random: all tiles with type not-explored with random values");
				this.c.writeLine("    note that the potential existing resource values of these types will be randomly overwritten");
			}
			case "load-all" -> {
				if (++i >= args.size()) {
					this.c.writeLine("not enugh args for the load argument");
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLine("the file does not exist");
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLine("the path does not refer to a regular file");
					return;
				}
				if (usr == null) {
					this.c.writeLine("there is no user logged in");
					return;
				}
				if (!(usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLine("changed to root user");
				}
				usr      = usr.makeRoot();
				username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr)) {
					world = RootWorld.loadEverything(conn);
				} catch (IOException e) {
					this.c.writeLine("error: " + e.toString());
				}
			}
			case "load" -> {
				if (++i >= args.size()) {
					this.c.writeLine("not enugh args for the load argument");
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLine("the file does not exist");
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLine("the path does not refer to a regular file");
					return;
				}
				if (usr == null) {
					this.c.writeLine("there is no user logged in");
					return;
				}
				if (!(usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLine("changed to root user");
				}
				usr      = usr.makeRoot();
				username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr)) {
					((RootUser) usr).load(conn);
					Tile[][] tiles = RemoteWorld.loadWorld(conn, ((RootUser) usr).users());
					world = RootWorld.Builder.createBuilder((RootUser) usr, tiles);
					this.c.writeLine("loaded world and users successfully from the file");
				} catch (IOException e) {
					this.c.writeLine("error: " + e.toString());
				}
			}
			case "create" -> {
				if (usr == null) {
					this.c.writeLine("you need to be logged in for the create operation");
					return;
				}
				i += 2;
				if (i >= args.size()) {
					this.c.writeLine("not enugh arguments for create <xlen> <ylen>");
					return;
				}
				int xlen;
				int ylen;
				try {
					xlen = Integer.parseInt(args.get(i - 1));
					ylen = Integer.parseInt(args.get(i));
				} catch (NumberFormatException nfe) {
					this.c.writeLine("could not parse the world size: <" + args.get(i - 1) + "> <" + args.get(i) + "> error: " + nfe);
					return;
				}
				if (world instanceof RemoteWorld rw) {
					try {
						rw.close();
					} catch (IOException e) {
						this.c.writeLine("error while closing remote world: (I will ignore the error and proceed now)");
						e.printStackTrace();
					}
				}
				world = null;
				Thread st = serverThread;
				while (st != null) {
					st.interrupt();
					this.c.writeLine("close now the server thread");
					try {
						st.join(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					st = serverThread;
				}
				Map<User, Connection> cs = connects;
				if (cs != null) {
					for (Entry<User, Connection> e : cs.entrySet()) {
						try {
							e.getValue().logOut();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.getKey().close();
					}
				}
				world = new RootWorld.Builder(usr.makeRoot(), xlen, ylen);
				usr   = world.user();
			}
			case "print", "print.types" -> cmdWorldAllTilesType();
			case "print.resources" -> cmdWorldAllTilesResources();
			case "tile" -> {
				i += 2;
				if (i >= args.size()) {
					this.c.writeLine("not enugh arguments for the tile argument");
					return;
				}
				if (world == null) {
					this.c.writeLine("there is no world, so I can not print a tile from the world");
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= world.xlen() || y < 0 || y >= world.ylen()) {
					this.c.writeLine("coordinate is out of bounds: (xlen=" + world.xlen() + "|ylen=" + world.ylen() + ") (x=" + x + "|y=" + y + ")");
					return;
				}
				writeTile(x, y);
			}
			case "to-build" -> {
				if (world == null) {
					this.c.writeLine("there is no world, so I can't convert it");
					return;
				}
				World old = world;
				if (old instanceof RemoteWorld rw) {
					if (!rw.getWorld()) {
						rw.getWorld(true);
						rw.needUpdate();
					} // ensure the complete world is loaded
					rw.tile(0, 0); // so the old user can be deleted (makeRoot)
				}
				int xlen = old.xlen();
				int ylen = old.ylen();
				usr = usr.makeRoot();
				RootWorld.Builder b = new RootWorld.Builder((RootUser) usr, xlen, ylen);
				for (int x = 0; x < xlen; x++) {
					for (int y = 0; y < ylen; y++) {
						b.set(x, y, old.tile(x, y));
					}
				}
				world = b;
				this.c.writeLine("successfully converted the current world to a build world");
			}
			case "save-all", "save-all-force" -> {
				if (++i >= args.size()) {
					this.c.writeLine("not enugh arguments for the save argument");
					return;
				}
				if (world == null) {
					this.c.writeLine("there is no world I can save");
					return;
				}
				if (!(world instanceof RootWorld rw)) {
					this.c.writeLine("the current world is not in root world");
					return;
				}
				Path p = Path.of(args.get(i));
				if (!"save-all-force".equals(args.get(i)) && Files.exists(p) && ask("the save file already exists, proceed? ([p]roceed|[c]ancel)", "pc") == 'c') {
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, usr)) {
					rw.saveEverything(conn);
					this.c.writeLine("saved everything in the given file");
				} catch (IOException e) {
					this.c.writeLine("error while saving: " + e.toString());
				}
			}
			case "save", "save-force" -> {
				if (++i >= args.size()) {
					this.c.writeLine("not enugh arguments for the save argument");
					return;
				}
				if (world == null) {
					this.c.writeLine("there is no world I can save");
					return;
				}
				Path p = Path.of(args.get(i));
				if (!"save-force".equals(args.get(i)) && Files.exists(p) && ask("the save file already exists, proceed? ([p]roceed|[c]ancel)", "pc") == 'c') {
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, usr)) {
					if (usr instanceof RootUser ru) {
						ru.save(conn);
					} else {
						try (RootUser tmp = RootUser.create(new char[0])) {
							tmp.save(conn);
						}
					}
					OpenWorld.saveWorld(world, conn);
					this.c.writeLine("saved wolrd and users in the given file");
				} catch (IOException e) {
					this.c.writeLine("error while saving: " + e.toString());
				}
			}
			case "build" -> {
				if (world == null) {
					this.c.writeLine("there is no world, so I can't build it");
					return;
				}
				if (!(world instanceof RootWorld.Builder b)) {
					this.c.writeLine("the world is no build world, I can only build build worlds");
					return;
				}
				try {
					world = b.create();
					this.c.writeLine("builded the current world");
				} catch (IllegalStateException | NullPointerException e) {
					this.c.writeLine("the build failed: " + e.toString());
				}
			}
			case "tile.type" -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLine("not enugh arguments for the tile.type argument");
					return;
				}
				if (world == null) {
					this.c.writeLine("there is no world, so I can't modify it");
					return;
				}
				if (!(world instanceof RootWorld.Builder b)) {
					this.c.writeLine("the world is no build world, I can only modify build worlds");
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= world.xlen() || y < 0 || y >= world.ylen()) {
					this.c.writeLine("coordinate is out of bounds: (xlen=" + world.xlen() + "|ylen=" + world.ylen() + ") (x=" + x + "|y=" + y + ")");
					return;
				}
				GroundType type = switch (args.get(i - 2).toLowerCase()) {
				case "not-explored" -> GroundType.NOT_EXPLORED;
				case "water", "water+normal" -> GroundType.WATER_NORMAL;
				case "sand", "sand+normal" -> GroundType.SAND;
				case "grass", "grass+normal" -> GroundType.GRASS;
				case "forest", "forest+normal" -> GroundType.FOREST;
				case "swamp", "swamp+normal" -> GroundType.SWAMP;
				case "mountain", "mountain+normal" -> GroundType.MOUNTAIN;
				case "+normal" -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || old.ground == GroundType.NOT_EXPLORED) {
						this.c.writeLine("the current type does not accept the +normal suffix");
						yield null;
					}
					yield old.ground.addNormal(false, true);
				}
				case "water+deep" -> GroundType.WATER_DEEP;
				case "+deep" -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isWater()) {
						this.c.writeLine("the current type does not accept the +deep suffix");
						yield null;
					}
					yield GroundType.WATER_DEEP;
				}
				case "sand+hill" -> GroundType.SAND_HILL;
				case "grass+hill" -> GroundType.GRASS_HILL;
				case "forest+hill" -> GroundType.FOREST_HILL;
				case "swamp+hill" -> GroundType.SWAMP_HILL;
				case "+hill" -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isHill() && !old.ground.isFlat()) {
						this.c.writeLine("the current type does not accept the +hill suffix");
						yield null;
					}
					yield old.ground.addHill(false, true);
				}
				default -> {
					this.c.writeLine("unknown type: '" + args.get(i - 2) + '\'');
					yield null;
				}
				};
				if (type != null) {
					b.set(x, y, type);
				}
			}
			case "tile.resource" -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLine("not enugh arguments for the tile.type argument");
					return;
				}
				if (world == null) {
					this.c.writeLine("there is no world, so I can't modify it");
					return;
				}
				if (!(world instanceof RootWorld.Builder b)) {
					this.c.writeLine("the world is no build world, I can only modify build worlds");
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= world.xlen() || y < 0 || y >= world.ylen()) {
					this.c.writeLine("coordinate is out of bounds: (xlen=" + world.xlen() + "|ylen=" + world.ylen() + ") (x=" + x + "|y=" + y + ")");
					return;
				}
				OreResourceType res = switch (args.get(i - 2).toLowerCase()) {
				case "none" -> OreResourceType.NONE;
				case "gold" -> OreResourceType.GOLD_ORE;
				case "iron" -> OreResourceType.IRON_ORE;
				case "coal" -> OreResourceType.COAL_ORE;
				default -> {
					this.c.writeLine("unknown type: '" + args.get(i - 2) + '\'');
					yield null;
				}
				};
				if (res != null) {
					b.set(x, y, res);
				}
			}
			case "fill-random" -> {
				if (world == null) {
					this.c.writeLine("there is no world, so I can't modify it");
					return;
				}
				if (!(world instanceof RootWorld.Builder b)) {
					this.c.writeLine("the world is no build world, I can only modify build worlds");
					return;
				}
				b.fillRandom();
			}
			default -> this.c.writeLine("unknown argument: '" + args.get(i) + '\'');
			}
		}
	}
	
	private void cmdWorldNoArgs() {
		if (world == null || ask("[c]hange or [d]isplay the current world? ", "cd") == 'c') {
			if (ask("[l]oad from file or create [n]ew world", "ln") == 'l') {
				cmdWorldInteractiveLoad();
			} else {
				rootLogin(usr == null);
				int xlen = readNumber("enter now the X-Length (Width) if the World", 1, Integer.MAX_VALUE);
				if (xlen > 0) {
					int ylen = readNumber("enter now the Y-Length (Height) if the World", 1, Integer.MAX_VALUE);
					if (ylen > 0) {
						world = new RootWorld.Builder((RootUser) usr, xlen, ylen);
						this.c.writeLine("created new world");
						if (ask("fill world with random tiles? ([y]es|[n]o): ", "yn") == 'y') {
							((RootWorld.Builder) world).fillRandom();
							this.c.writeLine("filled world with random tiles");
							if (ask("build world? ([y]es|[n]o): ", "yn") == 'y') {
								world = ((RootWorld.Builder) world).create();
								this.c.writeLine("world builded");
							}
						}
					}
				}
			}
		} else if (ask("display the [c]omplete world or only a [t]ile", "ct") == 'c') {
			cmdWorldAllTilesType();
		} else {
			int x = readNumber("enter now the X-coordinate of the tile", 0, world.xlen());
			if (x >= 0) {
				int y = readNumber("enter now the Y-coordinate of the tile", 0, world.ylen());
				if (y >= 0) {
					writeTile(x, y);
				}
			}
		}
	}
	
	private void writeTile(int x, int y) {
		Tile tile = world.tile(x, y);
		this.c.writeLine("Tile: (" + x + '|' + y + ')');
		this.c.writeLine("  Type: " + tile.ground);
		this.c.writeLine("  Resource: " + tile.resource);
	}
	
	private void cmdWorldInteractiveLoad() {
		boolean askPW = usr == null;
		do {
			Path p;
			do {
				p = Path.of(this.c.readLine("enter now the file, which should be loaded: "));
			} while (retry(!Files.exists(p) || !Files.isRegularFile(p)));
			if (!Files.exists(p) || !Files.isRegularFile(p)) {
				break;
			}
			rootLogin(askPW);
			try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, usr)) {
				((RootUser) usr).load(conn);
				Tile[][] tiles = RemoteWorld.loadWorld(conn, ((RootUser) usr).users());
				this.c.writeLine("loaded from file, build now the world");
				try {
					world = RootWorld.Builder.create((RootUser) usr, tiles);
				} catch (IllegalStateException e) {
					this.c.writeLine("build failed: " + e.toString());
					this.c.writeLine("world stays in build mode now");
				}
			} catch (IOException e) {
				this.c.writeLine("error: " + e.toString());
			}
		} while (retry(true));
	}
	
	private void rootLogin(boolean askPW) {
		if (askPW) {
			char[] pw = this.c.readPassword("enter now the password");
			if (usr != null && !(usr instanceof RootUser)) {
				username = "";
			}
			usr = RootUser.create(pw);
			if (username != null) {
				username = null;
				this.c.writeLine("changed to root user");
			}
		} else {
			usr = usr.makeRoot();
		}
	}
	
	private void cmdWorldAllTilesResources() {
		if (world == null) {
			this.c.writeLine("there is no world I can print");
			return;
		}
		this.c.writeLine(" : no resource");
		this.c.writeLine("G: gold ore");
		this.c.writeLine("I: iron ore");
		this.c.writeLine("C: coal ore");
		int xlen = world.xlen();
		int ylen = world.ylen();
		this.c.writeLine("world (" + xlen + '|' + ylen + "):");
		for (int y = 0; y < ylen; y++) {
			StringBuilder b = new StringBuilder(xlen);
			for (int x = 0; x < xlen; x++) {
				Tile t = world.tile(x, y);
				b.append(switch (t.resource) {
				case Object o when o == OreResourceType.NONE -> ' ';
				case Object o when o == OreResourceType.GOLD_ORE -> 'G';
				case Object o when o == OreResourceType.IRON_ORE -> 'I';
				case Object o when o == OreResourceType.COAL_ORE -> 'C';
				default -> throw new AssertionError("unknown tile resource: " + t.resource.name());
				});
			}
			this.c.writeLine(b.toString());
		}
	}
	
	private void cmdWorldAllTilesType() {
		if (world == null) {
			this.c.writeLine("there is no world I can print");
			return;
		}
		this.c.writeLine("#: not explored");
		this.c.writeLine("w: water");
		this.c.writeLine("W: water deep");
		this.c.writeLine("b: sand");
		this.c.writeLine("B: sand hill");
		this.c.writeLine("g: grass");
		this.c.writeLine("G: grass hill");
		this.c.writeLine("f: forest");
		this.c.writeLine("F: forest hill");
		this.c.writeLine("s: swamp");
		this.c.writeLine("S: swamp hill");
		this.c.writeLine("m: mountain");
		int xlen = world.xlen();
		int ylen = world.ylen();
		this.c.writeLine("world (" + xlen + '|' + ylen + "):");
		for (int y = 0; y < ylen; y++) {
			StringBuilder b = new StringBuilder(xlen);
			for (int x = 0; x < xlen; x++) {
				Tile t = world.tile(x, y);
				b.append(switch (t.ground) {
				case NOT_EXPLORED -> '#';
				case WATER_NORMAL -> 'w';
				case WATER_DEEP -> 'W';
				case SAND -> 'b';
				case SAND_HILL -> 'B';
				case GRASS -> 'g';
				case GRASS_HILL -> 'G';
				case FOREST -> 'f';
				case FOREST_HILL -> 'F';
				case SWAMP -> 's';
				case SWAMP_HILL -> 'S';
				case MOUNTAIN -> 'm';
				default -> throw new AssertionError("unknown tile type: " + t.ground.name());
				});
			}
			this.c.writeLine(b.toString());
		}
	}
	
	private void cmdUsername(List<String> args) {
		if (args.size() == 1) {
			cmdUsernameNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLine(CMD_USERNAME + " help: set or get your username");
				this.c.writeLine(HELP + ": print this message");
				this.c.writeLine("set <USERNAME>: set the new username");
				this.c.writeLine("get: print the current username");
				this.c.writeLine("without arguments, print the current username and then prompt for a new username");
			}
			case "set" -> {
				i++;
				if (i >= args.size()) {
					this.c.writeLine("not enugh arguments for the set arg");
					return;
				}
				String cur = args.get(i);
				if (usr == null) {
					username = RootUser.ROOT_NAME.equals(cur) ? null : cur;
				} else if (RootUser.ROOT_NAME.equals(cur)) {
					usr = usr.makeRoot();
				} else {
					usr = usr.changeName(cur);
				}
			}
			case "get" -> {
				String cur = usr == null ? username : usr.name();
				cur = cur == null ? RootUser.ROOT_NAME : cur;
				this.c.writeLine(cur);
			}
			default -> this.c.writeLine("unknown argument: '" + args.get(i) + '\'');
			}
		}
	}
	
	private void cmdUsernameNoArgs() {
		String cur = usr == null ? username : usr.name();
		if (cur != null) {
			this.c.writeLine("your current username is '" + cur + '\'');
		}
		cur = this.c.readLine("enter your new username: ");
		if (usr == null) {
			username = RootUser.ROOT_NAME.equals(cur) ? null : cur;
		} else if (RootUser.ROOT_NAME.equals(cur)) {
			if (!(usr instanceof RootUser)) {
				usr = usr.makeRoot();
			}
		} else {
			usr = usr.changeName(cur);
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
				switch (args.get(i).toLowerCase()) {
				case HELP -> {
					this.c.writeLine("Status command: help");
					this.c.writeLine("without arguments, write all status types");
					this.c.writeLine("with arguments:");
					this.c.writeLine("  '" + HELP + "' write this message");
					this.c.writeLine("  'user' write the user status");
					this.c.writeLine("  'world-remote-size' update the size of the remote world and then write the world status");
					this.c.writeLine("  'world-remote-all' or 'world-remote-world' update the complete remote world and then write the world status");
					this.c.writeLine("  'server' write the server status");
					this.c.writeLine("  'serverpw' or 'server-pw' write the server password status");
				}
				case "user" -> cmdStatusUser();
				case "world" -> cmdStatusWorld();
				case "world-remote-size" -> cmdStatusWorldRemoteSize();
				case "world-remote-all", "world-remote-world" -> cmdStatusWorldRemoteAll();
				case "server" -> cmdStatusServer();
				case "serverpw", "server-pw" -> cmdStatusServerPW();
				default -> this.c.writeLine("unknown argument: '" + args.get(i) + "'");
				}
			}
		}
	}
	
	private void cmdStatusServerPW() {
		if (serverPW != null) {
			this.c.writeLine("serverPassword: set");
		} else {
			this.c.writeLine("serverPassword: there is no server password");
			if (serverThread != null) {
				this.c.writeLine("  note that I remove my reference of the server password after starting the server");
				this.c.writeLine("  only because I do not know a server password, does not mean that the server knows no password");
			}
		}
	}
	
	private void cmdStatusServer() {
		if (serverThread != null) {
			this.c.writeLine("MyServer: running");
		} else {
			this.c.writeLine("MyServer: there is no server");
		}
	}
	
	private void cmdStatusWorldRemoteAll() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			this.c.writeLine("there is no remote world");
			return;
		}
		try {
			rw.updateWorld();
			this.c.writeLine("World: remote world loaded");
			this.c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorldRemoteSize() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			this.c.writeLine("there is no remote world");
			return;
		}
		try {
			rw.updateWorldSize();
			this.c.writeLine("World: remote world loaded");
			this.c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorld() {
		boolean writeBounds = true;
		if (world == null) {
			this.c.writeLine("World: there is no world");
			writeBounds = false;
		} else if (world instanceof RootWorld) {
			this.c.writeLine("World: root world loaded");
		} else if (world instanceof RootWorld.Builder) {
			this.c.writeLine("World: builder world loaded");
		} else if (world instanceof RemoteWorld rw) {
			this.c.writeLine("World: remote world loaded");
			if (rw.loadedBounds()) {
				this.c.writeLine("  Bounds: not loaded");
				writeBounds = false;
			}
		} else if (world instanceof UserWorld) {
			this.c.writeLine("World: user world loaded");
		} else {
			this.c.writeLine("World: unknown world type loaded");
			this.c.writeLine("  Type: " + world.getClass().getSimpleName());
		}
		if (writeBounds) {
			this.c.writeLine("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		}
	}
	
	private void cmdStatusUser() {
		if (usr == null) {
			this.c.writeLine("User: there is no User");
			if (username != null) {
				this.c.writeLine("  UserName: " + username);
			}
		} else {
			this.c.writeLine("User: Name: " + usr.name());
		}
	}
	
	private void cmdVersion(List<String> args) {
		if (args.size() == 1) {
			this.c.writeLine("Square Conquerer Console version: " + Settings.VERSION_STRING);
		} else if (args.size() == 2 && HELP.equalsIgnoreCase(args.get(1))) {
			this.c.writeLine("without args, I only write some version information and I can not do much more");
			this.c.writeLine("with one argument, I write this message, when the argumentis the " + HELP + " argument");
			this.c.writeLine("In all other cases I print an error message");
		} else {
			this.c.writeLine("I dont know what I should do with arguments!");
		}
	}
	
	private void cmdHelp(List<String> args) {
		switch (args.size()) {
		case 1 -> {
			this.c.writeLine("Square Conquerer Console " + Settings.VERSION_STRING + " help:");
			this.c.writeLine("Commands:");
			this.c.writeLine("  " + CMD_HELP);
			this.c.writeLine("    print this message");
			this.c.writeLine("  " + CMD_VERSION);
			this.c.writeLine("    print version information");
			this.c.writeLine("  " + CMD_STATUS);
			this.c.writeLine("    print status information");
			this.c.writeLine("  " + CMD_USERNAME);
			this.c.writeLine("    set your username");
			this.c.writeLine("  " + CMD_WORLD);
			this.c.writeLine("    change or display the world");
			this.c.writeLine("  " + CMD_SERVER);
			this.c.writeLine("    connect to a server or start/stop your server");
			this.c.writeLine("  " + CMD_SETPW);
			this.c.writeLine("    change the password (your or someone others, it doesn't matter)");
			this.c.writeLine("  " + CMD_SERVERPW);
			this.c.writeLine("    set the server password");
			this.c.writeLine("  " + CMD_QUIT);
			this.c.writeLine("    quit this program");
			this.c.writeLine("  " + CMD_EXIT);
			this.c.writeLine("    alias for " + CMD_QUIT);
			this.c.writeLine("");
			this.c.writeLine("General:");
			this.c.writeLine("  all comands support the " + HELP + " argument");
			this.c.writeLine("  If you want further information for a specific command, ask the command");
			this.c.writeLine("  or use " + CMD_HELP + " <command>");
			this.c.writeLine("");
			this.c.writeLine("  passwords will never be accepted in the arguments");
		}
		case 2 -> {
			if (CMD_HELP.equalsIgnoreCase(args.get(1))) {
				this.c.writeLine("print a general help message or the help message of the argument");
			} else {
				args.set(0, args.get(1));
				args.set(1, HELP);
				exec(args);
			}
		}
		default -> this.c.writeLine("either give me the name of an command as argument or no argument");
		}
	}
	
	private char ask(String prompt, String validChars) {
		while (true) {
			String line = this.c.readLine(prompt).trim();
			if (line.isEmpty()) continue;
			char character = line.charAt(0);
			if (validChars.indexOf(character) == -1) continue;
			return character;
		}
	}
	
	private String prompt() {
		if (usr != null) {
			return "[" + usr.name() + "]: ";
		} else if (username != null) {
			return "(" + username + "): ";
		} else {
			return "(" + RootUser.ROOT_NAME + "): ";
		}
	}
	
	private void doTasks() throws AssertionError {
		while (!tasks.isEmpty()) {
			Object task = tasks.remove(0);
			if (task == null) {
				continue;
			}
			try {
				if (task instanceof Path loadFile) {
					doTaskLoadFile(loadFile);
				} else if (task instanceof StartServerTask sst) {
					doTaskStartServer(sst);
				} else if (task instanceof ConnectToServerTask cst) {
					doTaskConnectToServer(cst);
				} else {
					throw new AssertionError("task has an unknown class: " + task.getClass().getName());
				}
			} catch (RuntimeException e) {
				System.err.println("error while executing task");
				e.printStackTrace();
			}
		}
	}
	
	private void doTaskConnectToServer(ConnectToServerTask cst) {
		boolean askUsr = usr != null;
		boolean askUN  = username != null;
		do {
			if (askUsr) {
				if (askUN) {
					username = this.c.readLine("enter now your username: ");
				}
				char[] pw = this.c.readPassword("enter now your password: ");
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
					this.c.writeLine("connected to the server, remote world created");
				} catch (IOException e) {
					this.c.writeLine("could not connect to the server: " + e.toString());
				}
			}
		} while (retry(world == null));
	}
	
	private void doTaskStartServer(StartServerTask sst) {
		if (world == null) {
			boolean load;
			while (true) {
				String line = this.c.readLine("[l]oad world or create a [n]ew world?").trim();
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
			synchronized (this) {
				final RootWorld       rw = (RootWorld) world;
				Map<User, Connection> cs = new HashMap<>();
				connects     = cs;
				serverThread = threadStart(() -> {
									try {
										Connection.ServerAccept.accept(sst.port, rw, (conn, sok) -> {
															if (sok == null) {
																this.c.writeLine("the user '" + conn.usr.name() + "' disskonnected");
															} else {
																this.c.writeLine("accepted connection from '" + conn.usr.name() + "' (" + sok.getInetAddress() + ")");
															}
														}, cs, serverPW);
									} catch (IOException e) {
										this.c.writeLine("error on server thread: " + e.toString());
									} finally {
										synchronized (SquareConquererCUI.this) {
											if (serverThread == Thread.currentThread()) {
												serverThread = null;
												connects     = null;
											}
										}
									}
								});
				this.c.writeLine("the server should now accept connections");
			}
		}
	}
	
	private void subTaskStartServerNewWorld() {
		int xlen = readNumber("enter now the X-length of the world (Width): ", 1, Integer.MAX_VALUE);
		if (xlen > 0) {
			int ylen = readNumber("enter now the Y-length of the world (Height): ", 1, Integer.MAX_VALUE);
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
				loadFile = Path.of(this.c.readLine("enter now the path of the save-file: "));
				if (!Files.exists(loadFile)) {
					this.c.writeLine("the file '" + loadFile + "' does not exist");
					continue;
				}
				if (!Files.isRegularFile(loadFile)) {
					this.c.writeLine("the pat '" + loadFile + "' does not refer to a file");
					continue;
				}
				break;
			} while (retry(true));
			if (askPW) {
				char[] pw = this.c.readPassword("enter now the password: ");
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
				this.c.writeLine("load now the file '" + loadFile + "'");
				char[] pw = this.c.readPassword("enter the password: ");
				if (username != null && !RootUser.ROOT_NAME.equals(username)) {
					this.c.writeLine("changed to root user");
				}
				usr = RootUser.create(pw);
			} else if (!(usr instanceof RootUser)) {
				this.c.writeLine("changed to root user");
				usr = usr.makeRoot();
			} else { // reset other users
				usr = usr.makeRoot();
			}
		} while (retry(loadFile(loadFile)));
	}
	
	private int readNumber(String prompt, int min, int max) {
		int val = -1;
		do {
			String line = this.c.readLine(prompt);
			try {
				val = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				this.c.writeLine("error: " + e.toString());
			}
		} while (retry(val < min || val > max, "the minimum number is " + min + (max != Integer.MAX_VALUE ? " and the maximum number is " + max : "")));
		return val;
	}
	
	private boolean retry(boolean failed) {
		return retry(failed, "retry? ([y]es|[n]o)");
	}
	
	private boolean retry(boolean failed, String errorPrompt) {
		if (!failed) return false;
		while (true) {
			String line = this.c.readLine(errorPrompt).trim();
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
			Tile[][] tiles = RemoteWorld.loadWorld(conn, root.users());
			world = RootWorld.Builder.createBuilder(root, tiles);
			try {
				world = ((RootWorld.Builder) world).create();
			} catch (IllegalStateException ise) {
				this.c.writeLine("could not build the world: " + ise);
				this.c.writeLine("the current world is in building mode");
			}
			username = null;
			this.c.writeLine("world successful loaded");
			return false;
		} catch (IOException e) {
			usr = null;
			this.c.writeLine("error: " + e.toString());
			return true;
		}
	}
	
}
