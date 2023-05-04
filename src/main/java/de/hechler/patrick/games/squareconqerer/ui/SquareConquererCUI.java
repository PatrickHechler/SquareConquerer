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
import java.text.MessageFormat;
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
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

/**
 * this class is used to communicate with the user using a console or streams
 * 
 * @author Patrick Hechler
 */
public class SquareConquererCUI implements Runnable {
	
	private static final String WORLD_NOARG_PROMPT_ENTER_Y_LEN = "enter now the Y-Length (Height) if the World";
	private static final String WORLD_NOARG_PROMPT_ENTER_X_LEN = "enter now the X-Length (Width) if the World";
	private static final String WORLD_NOARG_L_OAD_FILE_CREATE_N_EW_OR_C_ANCEL = "[l]oad from file or create [n]ew world (or [c]ancel)";
	private static final String WORLD_NOARG_C_HAGE_OR_D_ISPLAY = "[c]hange or [d]isplay the current world? ";
	private static final String WORLD_TILE_RESOURCE_UNKNOWN_RESOURCE                                          = "unknown resource type: ''{0}''";
	private static final String WORLD_TILE_TYPE_UNKNOWN_GROUND                                                = "unknown ground type: ''{0}''";
	private static final String WORLD_TILE_TYPE_CURRENT_TYPE_0_NOT_ACCEPT_1_SUFFIX                            =
		"the current type ({0}) does not accept the {1} suffix";
	private static final String WORLD_MODIFY_NO_BUILD_WORLD                                                   =
		"the world is no build world, I can only modify build worlds";
	private static final String WORLD_MODIFY_MISSING_WORLD                                                    = "there is no world, so I can't modify it";
	private static final String WORLD_BUILD_ERROR_ON_BUILD                                                    = "the build failed: {0}";
	private static final String WORLD_BUILD_FINISH                                                            = "builded the current world";
	private static final String WORLD_BUILD_NO_BUILD_WORLD                                                    =
		"the world is no build world, I can only build build worlds";
	private static final String WORLD_SAVE_FINISH                                                             = "saved wolrd and users in the given file";
	private static final String WORLD_SAVE__ALL_ERROR_ON_SAVE                                                 = "error while saving: ";
	private static final String WORLD_SAVE_ALL_FINISH                                                         = "saved everything in the given file";
	private static final String WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL                                  =
		"the save file already exists, proceed? ([p]roceed|[c]ancel)";
	private static final String WORLD_SAVE_ALL_NOT_ROOT_WORLD                                                 = "the current world is not a root world";
	private static final String WORLD_SAVE_ALL_MISSING_WORLD                                                  = "there is no world I can save";
	private static final String WORLD_TO_BUILD_FINISH                                                         =
		"successfully converted the current world to a build world";
	private static final String WORLD_CONVERT_MISSING_WORLD                                                   = "there is no world, so I can't convert it";
	private static final String WORLD_TILE_MISSING_WORLD_TO_PRINT                                             =
		"there is no world, so I can not print a tile from the world";
	private static final String WORLD_CREATE_CLOSE_SERVER_THREAD                                              = "close now the server thread";
	private static final String WORLD_CREATE_ERROR_ON_CLOSING_REMOTE_WORLD_I_IGNORE_ERROR_AND_PROCEED_ERROR_0 =
		"error while closing remote world: (I will ignore the error and proceed now) error: {0}";
	private static final String WORLD_CREATE_COULD_NOT_PARSE_THE_WORLD_SIZE_0_1_ERROR_2                       =
		"could not parse the world size: <{0}> <{1}> error: {2}";
	private static final String WORLD_CREATE_NOT_LOGGED_IN                                                    = "you need to be logged in for the create operation";
	private static final String WORLD_LOAD_LOADED_WORLD_AND_USERS_SUCCESSFULLY                                = "loaded world and users successfully from the file";
	private static final String WORLD_LAOD_LOADED_EVERYTHING                                                  = "loaded everything from the file";
	private static final String WORLD_LOAD_ERROR_ON_LOAD                                                      = "error while loading: {0}";
	private static final String WORLD_LOAD_CHANGED_TO_ROOT                                                    = "changed to root user";
	private static final String WORLD_LOAD_NOT_LOGGED_IN                                                      = "there is no user logged in";
	private static final String WORLD_LOAD_NO_REGULAR_FILE                                                    = "the path does not refer to a regular file";
	private static final String WORLD_LOAD_FILE_NOT_EXIST                                                     = "the file does not exist";
	private static final String WORLD_HELP_BLA_BLA_MNY_ARGS                                                   =
		"{0} help: change or display the world\nbase commands: (those work always)\n  {1}: print this message\n  {2} <SAVE_FILE>: load everything from the file\n    the loaded world will be in root mode\n    if there is no user this operation will fail\n    if the user is currently not root, it will be after this operation\n    if the user already is root, all subusers will be deleted\n    the SAVE_FILE has to be created with save-all or save-all-force\n  {3} <SAVE_FILE>: load other users and world from the file\n    the loaded world will be in build mode\n    if there is no user this operation will fail\n    if the user is currently not root, it will be after this operation\n    if the user already is root, all subusers will be deleted\n    the SAVE_FILE has to be created with save or save-force\n  {4} <X-LEN/WIDTH> <Y-LEN/HEIGHT>: create a new build world with the given sizes\n    note that this command needs you to be logged in\n    note that this command will convert you to a new root user\n    the newly created world will be in build mode\nsimple commands: (work, when there is an world)\n  {5} or print.types: print all tile types of the world\n  {6}: print all resources of the world\n  {7} <X> <Y>: print the tile at the given coordinates\n  {8}: convert the world to a build world\n  {9} <FILE>: save the current world to the given file\n  {10} <FILE>: like save, but do not ask if the file already exist\nroot world commands: (world needs to be in root mode)\n  {11} <FILE>: save everything to the given file\n  {12} <FILE>: like save-all, but do not ask if the file already exist\nbuild world commands: (world needs to be in build mode)\n  {13}: convert the world to a root world\n  {14} <TYPE> <X> <Y>: set the type of the given tile\n    valid TYPE values: water sand grass forest swamp mountain not-explored\n    [sand grass forest swamp] accept the ''+hill'' suffix\n    [water] accepts the ''+deep'' suffix\n    all types except of not-explored accept the ''+normal'' suffix, which is just an alias for no suffix\n    if there is only a suffix given it will replace the current suffix (if this is valid)\\n  {15} <RESOURCE> <X> <Y>: set the resource of the given tile\n    valid RESOURCE values: none gold iron coal\n  {16}: all tiles with type not-explored with random values\n    the random tiles may depend on their enviromnet (in contrast to {17})\n    note that the potential existing resource values of these types will be randomly overwritten\n  {18}: all tiles with type not-explored with random values\n    note that the potential existing resource values of these types will be randomly overwritten";
	// server constants
	private static final String SERVER_STARTED_SERVER                                                     = "started server";
	private static final String SERVER_SERVER_STOPPED_WITH_ERROR                                          = "the server stopped with an error: ";
	private static final String SERVER_CLOSED_MESSAGE                                                     =
		"stopped the server, all remote connections should be closed now";
	private static final String SERVER_NOARG_ENTER_NOW_SERVER_PORT_DEFAULT_IS_0                           =
		"enter now the port on which the server should listen (default is {0}): ";
	private static final String SERVER_NOARG_ERROR_DURING_CONNECT                                         = "error while connecting to the server: {0}";
	private static final String SERVER_NOARG_CONNECTED_TO_0_AT_PORT_1                                     = "connected successful to {0} (at port {1})";
	private static final String SERVER_NOARG_PROMPT_ENTER_SERVERHOST                                      = "enter now the serverhost: ";
	private static final String SERVER_NOARG_P_ROCEED_AND_DISCARD_WORLD_OR_C_ANCEL                        =
		"if you proceed you current world will be discarded. ([p]rocced/[c]ancel)? ";
	private static final String SERVER_NOARG_S_TART_SERVER                                                = ", [s]tart a server";
	private static final String SERVER_NOARG_C_ONNECT_0_OR_N_OTHING                                       =
		"do you want to [c]onnect to a server{0} or do [n]othing?";
	private static final String SERVER_NOARG_NOT_LOGGED_IN                                                =
		"you are not logged in, retry after you logged in (take a look at the {0} and {1} commands)";
	private static final String SERVER_NOARG_ERROR_WHILE_DISCONNECT_NO_RETRY_0                            = "error while closing: (proceed anyway, do not retry) ";
	private static final String SERVER_NOARG_DISCONNECTED                                                 = "closed old world successfully";
	private static final String SERVER_NOARG_CONTAIN_CUR_WORLD_AS_BUILD_YN                                =
		"dou you want to contain your current world in build mode? ([y]es|[n]o)";
	private static final String SERVER_NOARG_D_ISCONNECT_OR_N_OTHING                                      = "dou you want to [d]isconnect or do [n]othing?";
	private static final String SERVER_NOARG_CONNECTE_TO_SERVER                                           = "you are currently connected to a server";
	private static final String SERVER_NOARG_C_LOSE_SERVER_OR_N_OTHING                                    = "dou you want to [c]lose the server [n]othing?";
	private static final String SERVER_NOARG_SERVER_IS_RUNNING                                            = "the server is currently running";
	private static final String SERVER_STOP_STOPPED                                                       = "the server stopped";
	private static final String SERVER_STOP_TOLD_TO_STOP                                                  = "I told the server thread to stop";
	private static final String SERVER_STOP_NO_SERVER                                                     = "there seems to be no server running";
	private static final String SERVER_START_STARTED_SERVER                                               = "started the server thread";
	private static final String SERVER_START_ERROR_AT_SERVER_THREAD_0                                     = "error at the server thread: {0}";
	private static final String SERVER_START_USER_0_LOGGED_IN_FROM_1                                      = "the user ''{0}'' logged in from {1}";
	private static final String SERVER_USER_0_DISCONNECTED                                                = "the user ''{0}'' disconnected";
	private static final String SERVER_START_NO_ROOT_WORLD                                                = "your loaded world is no root world";
	private static final String SERVER_START_MISSING_WORLD                                                = "there is no world loaded";
	private static final String SERVER_START_ALREADY_RUNNING                                              = "I am already running an server";
	private static final String SERVER_DISCONNECT_DISCONNECTING_ERROR_NOT_RETRY_0                         = "error while disconnecting: (do not retry) {0}";
	private static final String SERVER_DISCONNECT_CLOSED_REMOTE_WORLD                                     = "closed old remote world successfully";
	private static final String SERVER_DISCONNECT_NO_SERVER_CONNECTION                                    = "I don't know any server connection";
	private static final String SERVER_CONNECT_ERROR_WHILE_CONNECTING_0                                   = "error while connecting: {0}";
	private static final String SERVER_COULD_NOT_PARSE_PORT_0                                             = "could not parse the port: {0}";
	private static final String SERVER_CONNECT_NOT_LOGGED_IN                                              =
		"you are not logged in, retry after you logged in (look at {0} and {1})";
	private static final String SERVER_STATUS_NOTHING                                                     =
		"it seems that there is no server running and your world does not seem to be connected to some server";
	private static final String SERVER_STATS_CONNECTED                                                    = "your loaded world is a remote world";
	private static final String SERVER_STATUS_RUNNING                                                     = "the server thread is currently running";
	private static final String SERVER_HELP_STATUS_0_CONNECT_1_DISCONNECT_2_START_3_DEFAULT_PORT_4_STOP_5 =
		"server help:\nbase commands:\n  {0}: print an message indicating if you are running a server/are connected to a server or nothing of it\nremote server commands:\n  {1} <SERVER_ADDRESS>: connect to the given server\n  {2}: disconnect from the server you are currently connected to\nyour server commands:\n  {3} [PORT]: start your own server\n    use minus (''-'') instead of PORT to use the defaul port {4}\n    if the loaded world is no root world, accepted connections will not get an user world, but the direct world\n    you need to be logged in as root\n  {5}: stop your own server";
	// set password constants
	private static final String SET_PW_PROMPT_ENTER_YOUR_PASSWORD = "enter now the new password of the user: ";
	private static final String SET_PW_PROMPT_ENTER_USER_PASSWORD = "enter now the new password of the user: ";
	private static final String SET_PW_PROMPT_ENTER_USERNAME      = "enter now the username of the given user: ";
	private static final String SET_PW_YOUR_PW_WAS_CHANGED        = "your password was changed";
	private static final String SET_PW_PW_OF_0_CHANGED            = "the password of the user ''{0}'' changed";
	private static final String SET_PW_ONLY_ROOT_HAS_OTHR_USRS    = "this is not your username and you are not root";
	private static final String SET_PW_USER_0_NOT_FOUND           = "the user ''{0}'' could not be found";
	private static final String SET_PW_HELP_0_SET_OF_1_2_ME_3     =
		"setpw help:\nwithout args: prompt whoses password should be changed and then change the password\nargs:\n{0}: print this message\n{1}/{2} <USERNAME>: prompt for the new password of USERNAME\n{3}: prompt for your new password\n  note, that if you are connected to an server, that connection will become invalid\n<USERNAME>: prompt for the new password of USERNAME\n  note, that if the username is ''{1}'', ''{2}'' or ''{3}'' the arguments are triggered instead";
	private static final String SET_PW_S_OMEONES_PASSWORD         = ", [s]omeones password";
	private static final String SET_PW_Y_OUR_PW_0_OR_DO_N_OTHING  = "set [y]our password{0} or do [n]othing? ";
	// server PW constants
	private static final String SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD              = "enter now the new server password: ";
	private static final String SERVER_PW_REMOVE_THERE_IS_NO_SERVER_PW                  = "there is no server password I could remove";
	private static final String SERVER_PW_STATUS_THERE_IS_SPW                           = "I hava a server password set";
	private static final String SERVER_PW_UPDATED_SERVER_PW                             = "the server password was updated";
	private static final String SERVER_PW_STATUS_THERE_IS_NO_SPW                        = "I don't hava a server password";
	private static final String SERVER_PW_HELP_0_STATUS_1_SET_2_REMOVE_3_REMOVE_NO_FAIL =
		"server password help:\n{0}: print an message indicating if there is currently a server password set\n{1}: prompt for the new server password\n{2}: remove and clear the current server password\n{3}: same as remove, but do not show an error message if there is no server password";
	private static final String SERVER_PW_R_EMOVE                                       = "/[r]emove";
	private static final String SERVER_PW_S_ET_0_THE_SERVER_PW_OR_DO_N_OTHING           = "do you want to [s]et{0} the server password or do [n]othing? ";
	// quit constants
	private static final String QUIT_ERROR_WHILE_PARSING_EXIT_NUMBER_0 = "error while parsing exit number: {0}";
	private static final String QUIT_HELP_BLA_BLA_HELP_WITH_0          =
		"quit help:\nno args: terminates this program with the exit value 0\n{0}: print this message\n<NUMBER>: terminates this program with the given exit value\nanything else: terminates this program with the exit value 1";
	private static final String QUIT_GOODBYE_EXIT_NOW_WITH_0           = "goodbye, exit now with {0}";
	// general constants
	private static final String COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3 = "coordinate is out of bounds: (xlen={0}|ylen={1}) (x={2}|y={3})";
	private static final String INTERRUPT_ERROR_0                                 = "I was interrupted: {0}";
	private static final String NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG                 = "not enugh arguments for the {0} arg";
	private static final String UNKNOWN_ARGUMENT_0                                = "unknown argument: {0}";
	private static final String ERROR_WHILE_EXECUTING_THE_COMMAND_0               = "error while executing the command: {0}";
	private static final String UNKNOWN_COMMAND_0                                 = "unknown command: ''{0}''";
	private static final String GREET_BLA_BLA_HELP_WITH_0_COMMAND                 =
		"Welcome to the Square Conquerer Console\nif you don''t know what to do use the ''{0}'' command";
	private static final String THERE_IS_NO_CONS                                  = "there is no Cons";
	
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
			throw new NullPointerException(THERE_IS_NO_CONS);
		}
		this.c           = c;
		this.interactive = interactive;
	}
	
	/**
	 * set the interactive mode
	 * 
	 * @param interactive the new interactive mode
	 */
	public void interactive(boolean interactive) { this.interactive = interactive; }
	
	/**
	 * sets the world, server thread and connections
	 * 
	 * @param world        the world
	 * @param serverThread the server thread or <code>null</code> if no server is running
	 * @param connects     the connections map or <code>null</code> if no server is running
	 */
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
	
	/**
	 * sets the logged in user
	 * 
	 * @param usr the new logged in user
	 */
	public void user(User usr) { this.usr = usr; }
	
	/**
	 * sets the name of the not logged in user (ignored if a user is logged in)
	 * 
	 * @param name the name of the not logged in user
	 */
	public void name(String name) { this.username = name; }
	
	/**
	 * sets the server password
	 * 
	 * @param serverpw the new server password
	 */
	public void serverPW(char[] serverpw) { this.serverPW = serverpw; }
	
	/**
	 * add a load world task
	 * 
	 * @param worldFile the file from which the world should be loaded
	 */
	public void startLoad(Path worldFile) { this.tasks.add(worldFile); }
	
	/**
	 * add a start server task
	 * 
	 * @param p the port of the server
	 */
	public void startServer(int p) { this.tasks.add(new StartServerTask(p)); }
	
	/**
	 * add a connect to server task
	 * 
	 * @param host the host name
	 * @param p    the server port
	 */
	public void startConnect(String host, int p) { this.tasks.add(new ConnectToServerTask(host, p)); }
	
	private static class StartServerTask {
		
		private final int port;
		
		private StartServerTask(int port) { this.port = port; }
		
	}
	
	private static class ConnectToServerTask {
		
		private final String host;
		private final int    port;
		
		private ConnectToServerTask(String host, int port) { this.host = host; this.port = port; }
		
	}
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		boolean greet = this.interactive;
		while (true) {
			doTasks();
			if (greet) {
				this.c.writeLines(MessageFormat.format(GREET_BLA_BLA_HELP_WITH_0_COMMAND, HELP));
				greet = false;
			}
			String line = (this.interactive ? this.c.readLine(prompt()) : this.c.readLine()).trim();
			if (line.isEmpty()) {
				continue;
			}
			List<String> args = arguments(line);
			if (args.isEmpty()) {
				this.c.writeLines("stange, there is nothing"); //$NON-NLS-1$ this should never happen
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
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_COMMAND_0, args.get(0)));
			}
		} catch (RuntimeException e) {
			this.c.writeLines(MessageFormat.format(ERROR_WHILE_EXECUTING_THE_COMMAND_0, e));
		}
	}
	
	private void cmdQuit(List<String> args) {
		if (args.size() == 1) {
			if (this.interactive) {
				this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, "0")); //$NON-NLS-1$
			}
			System.exit(0);
		}
		for (int i = 1; i < args.size(); i++) {
			if (HELP.equalsIgnoreCase(args.get(i))) {
				this.c.writeLines(MessageFormat.format(QUIT_HELP_BLA_BLA_HELP_WITH_0, HELP));
			} else {
				try {
					int e = Integer.parseInt(args.get(i));
					this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, Integer.toString(e)));
					System.exit(e);
				} catch (NumberFormatException e) {
					this.c.writeLines(MessageFormat.format(QUIT_ERROR_WHILE_PARSING_EXIT_NUMBER_0, e));
					this.c.writeLines(MessageFormat.format(QUIT_GOODBYE_EXIT_NOW_WITH_0, "1")); //$NON-NLS-1$
					System.exit(1);
				}
			}
		}
	}
	
	private void cmdServerPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask(MessageFormat.format(SERVER_PW_S_ET_0_THE_SERVER_PW_OR_DO_N_OTHING, (this.serverPW != null ? SERVER_PW_R_EMOVE : "")), //$NON-NLS-1$
				this.serverPW != null ? "srn" : "sn")) {  //$NON-NLS-1$ //$NON-NLS-2$
			case 's' -> this.serverPW = this.c.readPassword(SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD);
			case 'r' -> this.serverPW = null;
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$ this should never happen
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argStatus       = "status"; //$NON-NLS-1$
			final String argSet          = "set"; //$NON-NLS-1$
			final String argRemove       = "remove"; //$NON-NLS-1$
			final String argRemoveNoFail = "remove-no-fail"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SERVER_PW_HELP_0_STATUS_1_SET_2_REMOVE_3_REMOVE_NO_FAIL, argStatus, argSet, argRemove, argRemoveNoFail));
			}
			case argStatus -> {
				if (this.serverPW != null) {
					this.c.writeLines(SERVER_PW_STATUS_THERE_IS_SPW);
				} else {
					this.c.writeLines(SERVER_PW_STATUS_THERE_IS_NO_SPW);
				}
			}
			case argSet -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argSet));
					return;
				}
				this.serverPW = this.c.readPassword(SERVER_PW_PROMPT_ENTER_NEW_SERVER_PASSWORD);
				this.c.writeLines(SERVER_PW_UPDATED_SERVER_PW);
			}
			case argRemove -> {
				if (this.serverPW == null) {
					this.c.writeLines(SERVER_PW_REMOVE_THERE_IS_NO_SERVER_PW);
					return;
				}
				this.serverPW = null;
			}
			case argRemoveNoFail -> this.serverPW = null;
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void cmdSetPW(List<String> args) {
		if (args.size() == 1) {
			switch (ask(MessageFormat.format(SET_PW_Y_OUR_PW_0_OR_DO_N_OTHING, (this.usr instanceof RootUser ? SET_PW_S_OMEONES_PASSWORD : "")), //$NON-NLS-1$
				this.usr instanceof RootUser ? "ysn" : "yn")) { //$NON-NLS-1$ //$NON-NLS-2$
			case 'y' -> setMyPW();
			case 's' -> {
				String name = this.c.readLine(SET_PW_PROMPT_ENTER_USERNAME);
				User   user = ((RootUser) this.usr).get(name);
				if (user == null) {
					this.c.writeLines(MessageFormat.format(SET_PW_USER_0_NOT_FOUND, name));
					return;
				}
				char[] npw = this.c.readPassword(SET_PW_PROMPT_ENTER_USER_PASSWORD);
				((RootUser) this.usr).changePW(user, npw);
				this.c.writeLines(MessageFormat.format(SET_PW_PW_OF_0_CHANGED, name));
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$
			}
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argSet = "set"; //$NON-NLS-1$
			final String argOf  = "of"; //$NON-NLS-1$
			final String argMe  = "me"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SET_PW_HELP_0_SET_OF_1_2_ME_3, HELP, argSet, argOf, argMe, argSet, argOf, argMe));
			}
			case argSet, argOf -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argSet + "/" + argOf)); //$NON-NLS-1$
					return;
				}
				setPW(args.get(i));
			}
			case argMe -> setMyPW();
			default -> setPW(args.get(i));
			}
		}
	}
	
	private void setPW(String arg) {
		if (arg.equals(this.username) || arg.equals(this.usr == null ? RootUser.ROOT_NAME : this.usr.name())) {
			setMyPW();
		} else if (!(this.usr instanceof RootUser root)) {
			this.c.writeLines(SET_PW_ONLY_ROOT_HAS_OTHR_USRS);
		} else {
			setOtherPW(arg, root);
		}
	}
	
	private void setOtherPW(String name, RootUser root) {
		User user = root.get(name);
		if (user == null) {
			this.c.writeLines(MessageFormat.format(SET_PW_USER_0_NOT_FOUND, name));
		}
		char[] npw = this.c.readPassword(SET_PW_PROMPT_ENTER_USER_PASSWORD);
		((RootUser) this.usr).changePW(user, npw);
		this.c.writeLines(MessageFormat.format(SET_PW_PW_OF_0_CHANGED, name));
	}
	
	private void setMyPW() {
		char[] pw = this.c.readPassword(SET_PW_PROMPT_ENTER_YOUR_PASSWORD);
		if (this.username != null) {
			this.usr = User.create(this.username, pw);
		} else if (this.usr == null) {
			this.usr = RootUser.create(pw);
		} else {
			this.usr.changePassword(pw);
		}
		this.c.writeLines(SET_PW_YOUR_PW_WAS_CHANGED);
	}
	
	private void cmdServer(List<String> args) {
		if (args.size() == 1) {
			cmdServerNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argStatus     = "status"; //$NON-NLS-1$
			final String argConnect    = "connect"; //$NON-NLS-1$
			final String argDisconnect = "disconnect"; //$NON-NLS-1$
			final String argStart      = "start"; //$NON-NLS-1$
			final String argStop       = "stop"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(SERVER_HELP_STATUS_0_CONNECT_1_DISCONNECT_2_START_3_DEFAULT_PORT_4_STOP_5, argStatus, argConnect,
					argDisconnect, argStart, "" + Connection.DEFAULT_PORT, argStop)); //$NON-NLS-1$
			}
			case argStatus -> {
				if (this.serverThread != null) {
					this.c.writeLines(SERVER_STATUS_RUNNING);
				} else if (this.world instanceof RemoteWorld) {
					this.c.writeLines(SERVER_STATS_CONNECTED);
				} else {
					this.c.writeLines(SERVER_STATUS_NOTHING);
				}
			}
			case argConnect -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argConnect));
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(MessageFormat.format(SERVER_CONNECT_NOT_LOGGED_IN, CMD_SETPW, CMD_USERNAME));
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
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
				try {
					Connection conn;
					if (this.serverPW != null) {
						conn = Connection.ClientConnect.connectNew(addr, port, this.usr, this.serverPW);
					} else {
						conn = Connection.ClientConnect.connect(addr, port, this.usr);
					}
					this.world = new RemoteWorld(conn);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_CONNECT_ERROR_WHILE_CONNECTING_0, e));
					return;
				}
			}
			case argDisconnect -> {
				if (!(this.world instanceof RemoteWorld rw)) {
					this.c.writeLines(SERVER_DISCONNECT_NO_SERVER_CONNECTION);
					return;
				}
				this.world = null;
				try {
					rw.close();
					this.c.writeLines(SERVER_DISCONNECT_CLOSED_REMOTE_WORLD);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_DISCONNECT_DISCONNECTING_ERROR_NOT_RETRY_0, e));
				}
			}
			case argStart -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argStart));
					return;
				}
				synchronized (this) {
					if (this.serverThread != null) {
						this.c.writeLines(SERVER_START_ALREADY_RUNNING);
						return;
					}
					if (this.world == null) {
						this.c.writeLines(SERVER_START_MISSING_WORLD);
						return;
					}
					if (!(this.world instanceof RootWorld rw)) {
						this.c.writeLines(SERVER_START_NO_ROOT_WORLD);
						return;
					}
					try {
						int    port = "-".equals(args.get(i)) ? Connection.DEFAULT_PORT : Integer.parseInt(args.get(i)); //$NON-NLS-1$
						char[] spw  = this.serverPW;
						this.serverPW = null;
						Map<User, Connection> cs = new HashMap<>();
						this.connects     = cs;
						this.serverThread = threadStart(() -> {
												try {
													Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
																			if (sok == null) {
																				this.c.writeLines(MessageFormat.format(SERVER_USER_0_DISCONNECTED, conn.usr.name()));
																			} else {
																				this.c.writeLines(MessageFormat.format(SERVER_START_USER_0_LOGGED_IN_FROM_1,
																					conn.usr.name(), sok.getInetAddress()));
																			}
																		},
														cs, spw);
												} catch (IOException e) {
													this.c.writeLines(MessageFormat.format(SERVER_START_ERROR_AT_SERVER_THREAD_0, e));
												} finally {
													synchronized (SquareConquererCUI.this) {
														if (this.serverThread == Thread.currentThread()) {
															this.serverThread = null;
															this.connects     = null;
														}
													}
												}
											});
						this.c.writeLines(SERVER_START_STARTED_SERVER);
					} catch (NumberFormatException e) {
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
			}
			case argStop -> {
				Thread st = this.serverThread;
				if (st == null) {
					this.c.writeLines(SERVER_STOP_NO_SERVER);
					return;
				}
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
				}
				if (st.isAlive()) {
					this.c.writeLines(SERVER_STOP_TOLD_TO_STOP);
				} else {
					this.c.writeLines(SERVER_STOP_STOPPED);
				}
				closeConnections();
			}
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void cmdServerNoArgs() {
		Thread st = this.serverThread;
		if (st != null) {
			this.c.writeLines(SERVER_NOARG_SERVER_IS_RUNNING);
			if (ask(SERVER_NOARG_C_LOSE_SERVER_OR_N_OTHING, "cn") == 'c') { //$NON-NLS-1$
				st.interrupt();
				try {
					st.join(1000L);
				} catch (InterruptedException e) {
					this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
				}
				closeConnections();
				if (st.isAlive()) {
					this.c.writeLines(SERVER_STOP_TOLD_TO_STOP);
				} else {
					this.c.writeLines(SERVER_CLOSED_MESSAGE);
				}
			}
		} else if (this.world instanceof RemoteWorld rw) {
			this.c.writeLines(SERVER_NOARG_CONNECTE_TO_SERVER);
			if (ask(SERVER_NOARG_D_ISCONNECT_OR_N_OTHING, "dn") == 'd') { //$NON-NLS-1$
				World nw = null;
				if (ask(SERVER_NOARG_CONTAIN_CUR_WORLD_AS_BUILD_YN, "yn") == 'y') { //$NON-NLS-1$
					int               xlen = rw.xlen();
					int               ylen = rw.ylen();
					RootWorld.Builder b    = new RootWorld.Builder(this.usr.rootClone(), xlen, ylen);
					for (int x = 0; x < xlen; x++) {
						for (int y = 0; y < ylen; y++) {
							b.set(x, y, this.world.tile(x, y));
						}
					}
					nw = b;
				}
				try {
					rw.close();
					this.c.writeLines(SERVER_NOARG_DISCONNECTED);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_ERROR_WHILE_DISCONNECT_NO_RETRY_0, e));
				}
				this.world = nw;
			}
		} else if (this.usr == null) {
			this.c.writeLines(MessageFormat.format(SERVER_NOARG_NOT_LOGGED_IN, CMD_SETPW, CMD_USERNAME));
		} else {
			switch (ask(MessageFormat.format(SERVER_NOARG_C_ONNECT_0_OR_N_OTHING, (this.world instanceof RootWorld ? SERVER_NOARG_S_TART_SERVER : "")), //$NON-NLS-1$
				this.world instanceof RootWorld ? "csn" : "cn")) { //$NON-NLS-1$ //$NON-NLS-2$
			case 'c' -> {
				if (this.world != null && ask(SERVER_NOARG_P_ROCEED_AND_DISCARD_WORLD_OR_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					return;
				}
				String host = this.c.readLine(SERVER_NOARG_PROMPT_ENTER_SERVERHOST).trim();
				int    port = Connection.DEFAULT_PORT;
				int    li   = host.lastIndexOf(':');
				if (li != -1 && li - 1 == host.lastIndexOf(']') && host.charAt(0) == '[') {
					try {
						port = Integer.parseInt(host.substring(li + 1));
						host = host.substring(1, li - 1);
					} catch (NumberFormatException e) {
						this.c.writeLines(MessageFormat.format(SERVER_COULD_NOT_PARSE_PORT_0, e));
						return;
					}
				}
				Connection conn;
				try {
					char[] sp = this.serverPW;
					if (sp != null) {
						this.serverPW = null;
						conn          = Connection.ClientConnect.connectNew(host, port, this.usr, sp);
					} else {
						conn = Connection.ClientConnect.connect(host, port, this.usr);
					}
					this.world = new RemoteWorld(conn);
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_CONNECTED_TO_0_AT_PORT_1, host, Integer.toString(port)));
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(SERVER_NOARG_ERROR_DURING_CONNECT, e));
				}
			}
			case 's' -> {
				RootWorld rw      = (RootWorld) this.world;
				String    portStr = this.c.readLine(MessageFormat.format(SERVER_NOARG_ENTER_NOW_SERVER_PORT_DEFAULT_IS_0, "" + Connection.DEFAULT_PORT)).trim(); //$NON-NLS-1$
				int       port    = portStr.isEmpty() ? Connection.DEFAULT_PORT : Integer.parseInt(portStr);
				char[]    sp      = this.serverPW;
				this.serverPW = null;
				Map<User, Connection> cs = new HashMap<>();
				this.connects = cs;
				threadStart(() -> {
					try {
						Map<User, Connection> cs0 = new HashMap<>();
						this.connects = cs0;
						Connection.ServerAccept.accept(port, rw, (conn, sok) -> {
							if (sok == null) {
								this.c.writeLines(MessageFormat.format(SERVER_USER_0_DISCONNECTED, conn.usr.name()));
							} else {
								this.c.writeLines(MessageFormat.format(SERVER_START_USER_0_LOGGED_IN_FROM_1, conn.usr.name(), sok.getInetAddress()));
							}
						}, cs0, sp);
					} catch (IOException e) {
						this.c.writeLines(SERVER_SERVER_STOPPED_WITH_ERROR + e);
					}
				});
				this.c.writeLines(SERVER_STARTED_SERVER);
			}
			case 'n' -> {/**/}
			default -> throw new AssertionError("illegal return value from ask"); //$NON-NLS-1$ this should never happen
			}
		}
	}
	
	private void cmdWorld(List<String> args) {
		if (args.size() == 1) {
			cmdWorldNoArgs();
			return;
		}
		for (int i = 1; i < args.size(); i++) {
			final String argLoadAll           = "load-all"; //$NON-NLS-1$
			final String argLoad              = "load"; //$NON-NLS-1$
			final String argCreate            = "create"; //$NON-NLS-1$
			final String argPrint             = "print"; //$NON-NLS-1$
			final String argPrintTypes        = "print.types"; //$NON-NLS-1$
			final String argPrintResources    = "print.resources"; //$NON-NLS-1$
			final String argTile              = "tile"; //$NON-NLS-1$
			final String argToBuild           = "to-build"; //$NON-NLS-1$
			final String argSaveAll           = "save-all"; //$NON-NLS-1$
			final String argSaveAllForce      = "save-all-force"; //$NON-NLS-1$
			final String argSave              = "save"; //$NON-NLS-1$
			final String argSaveForce         = "save-force"; //$NON-NLS-1$
			final String argBuild             = "build"; //$NON-NLS-1$
			final String argTileType          = "tile.type"; //$NON-NLS-1$
			final String argTileResource      = "tile.resource"; //$NON-NLS-1$
			final String argFillRandom        = "fill-random"; //$NON-NLS-1$
			final String argFillTotallyRandom = "fill-totally-random"; //$NON-NLS-1$
			switch (args.get(i).toLowerCase()) {
			case HELP -> {
				this.c.writeLines(MessageFormat.format(WORLD_HELP_BLA_BLA_MNY_ARGS, CMD_WORLD, HELP, argLoadAll, argLoad, argCreate, argPrint, argPrintResources,
					argTile, argToBuild, argSave, argSaveForce, argSaveAll, argSaveAllForce, argBuild, argTileType, argTileResource, argFillRandom,
					argFillTotallyRandom, argFillTotallyRandom));
			}
			case argLoadAll -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argLoadAll));
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLines(WORLD_LOAD_FILE_NOT_EXIST);
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLines(WORLD_LOAD_NO_REGULAR_FILE);
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(WORLD_LOAD_NOT_LOGGED_IN);
					return;
				}
				if (!(this.usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				this.usr      = this.usr.makeRoot();
				this.username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr)) {
					this.world = RootWorld.loadEverything(conn);
					this.c.writeLines(WORLD_LAOD_LOADED_EVERYTHING);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
				}
			}
			case argLoad -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argLoad));
					return;
				}
				Path p = Path.of(args.get(i));
				if (!Files.exists(p)) {
					this.c.writeLines(WORLD_LOAD_FILE_NOT_EXIST);
					return;
				}
				if (!Files.isRegularFile(p)) {
					this.c.writeLines(WORLD_LOAD_NO_REGULAR_FILE);
					return;
				}
				if (this.usr == null) {
					this.c.writeLines(WORLD_LOAD_NOT_LOGGED_IN);
					return;
				}
				if (!(this.usr instanceof RootUser root) || !root.users().isEmpty()) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				this.usr      = this.usr.makeRoot();
				this.username = null;
				try (InputStream in = Files.newInputStream(p); Connection conn = Connection.OneWayAccept.acceptReadOnly(in, this.usr)) {
					((RootUser) this.usr).load(conn);
					Tile[][] tiles = RemoteWorld.loadWorld(conn, ((RootUser) this.usr).users());
					this.world = RootWorld.Builder.createBuilder((RootUser) this.usr, tiles);
					this.c.writeLines(WORLD_LOAD_LOADED_WORLD_AND_USERS_SUCCESSFULLY);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_LOAD_ERROR_ON_LOAD, e));
				}
			}
			case argCreate -> {
				if (this.usr == null) {
					this.c.writeLines(WORLD_CREATE_NOT_LOGGED_IN);
					return;
				}
				i += 2;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argCreate));
					return;
				}
				int xlen;
				int ylen;
				try {
					xlen = Integer.parseInt(args.get(i - 1));
					ylen = Integer.parseInt(args.get(i));
				} catch (NumberFormatException nfe) {
					this.c.writeLines(MessageFormat.format(WORLD_CREATE_COULD_NOT_PARSE_THE_WORLD_SIZE_0_1_ERROR_2, args.get(i - 1), args.get(i), nfe));
					return;
				}
				if (this.world instanceof RemoteWorld rw) {
					try {
						rw.close();
					} catch (IOException e) {
						this.c.writeLines(MessageFormat.format(WORLD_CREATE_ERROR_ON_CLOSING_REMOTE_WORLD_I_IGNORE_ERROR_AND_PROCEED_ERROR_0, e));
					}
				}
				this.world = null;
				Thread st = this.serverThread;
				if (st != null) {
					st.interrupt();
					this.c.writeLines(WORLD_CREATE_CLOSE_SERVER_THREAD);
					try {
						st.join(1000L);
					} catch (InterruptedException e) {
						this.c.writeLines(MessageFormat.format(INTERRUPT_ERROR_0, e));
					}
				}
				closeConnections();
				this.world = new RootWorld.Builder(this.usr.makeRoot(), xlen, ylen);
				this.usr   = this.world.user();
			}
			case argPrint, argPrintTypes -> cmdWorldAllTilesType();
			case argPrintResources -> cmdWorldAllTilesResources();
			case argTile -> {
				i += 2;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTile));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_TILE_MISSING_WORLD_TO_PRINT);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				writeTile(x, y);
			}
			case argToBuild -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_CONVERT_MISSING_WORLD);
					return;
				}
				World old = this.world;
				if (old instanceof RemoteWorld rw) {
					if (!rw.getWorld()) {
						rw.getWorld(true);
						rw.needUpdate();
					} // ensure the complete world is loaded
					rw.tile(0, 0); // so the old user can be deleted (makeRoot)
				}
				int xlen = old.xlen();
				int ylen = old.ylen();
				this.usr = this.usr.makeRoot();
				RootWorld.Builder b = new RootWorld.Builder((RootUser) this.usr, xlen, ylen);
				for (int x = 0; x < xlen; x++) {
					for (int y = 0; y < ylen; y++) {
						b.set(x, y, old.tile(x, y));
					}
				}
				this.world = b;
				this.c.writeLines(WORLD_TO_BUILD_FINISH);
			}
			case argSaveAll, argSaveAllForce -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, args.get(i - 1)));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_SAVE_ALL_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld rw)) {
					this.c.writeLines(WORLD_SAVE_ALL_NOT_ROOT_WORLD);
					return;
				}
				Path p = Path.of(args.get(i));
				if (!argSaveAllForce.equals(args.get(i - 1)) && Files.exists(p) && ask(WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, this.usr)) {
					rw.saveEverything(conn);
					this.c.writeLines(WORLD_SAVE_ALL_FINISH);
				} catch (IOException e) {
					this.c.writeLines(WORLD_SAVE__ALL_ERROR_ON_SAVE + e.toString());
				}
			}
			case argSave, argSaveForce -> {
				if (++i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, args.get(i - 1)));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_SAVE_ALL_MISSING_WORLD);
					return;
				}
				Path p = Path.of(args.get(i));
				if (!argSaveForce.equals(args.get(i - 1)) && Files.exists(p) && ask(WORLD_SAVE__ALL_FILE_EXISTS_P_ROCEED_C_ANCEL, "pc") == 'c') { //$NON-NLS-1$
					break;
				}
				try (OutputStream out = Files.newOutputStream(p); Connection conn = Connection.OneWayAccept.acceptWriteOnly(out, this.usr)) {
					if (this.usr instanceof RootUser ru) {
						ru.save(conn);
					} else {
						try (RootUser tmp = RootUser.create(new char[0])) {
							tmp.save(conn);
						}
					}
					OpenWorld.saveWorld(this.world, conn);
					this.c.writeLines(WORLD_SAVE_FINISH);
				} catch (IOException e) {
					this.c.writeLines(MessageFormat.format(WORLD_SAVE__ALL_ERROR_ON_SAVE, e));
				}
			}
			case argBuild -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_CONVERT_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_BUILD_NO_BUILD_WORLD);
					return;
				}
				try {
					this.world = b.create();
					this.c.writeLines(WORLD_BUILD_FINISH);
				} catch (IllegalStateException | NullPointerException e) {
					this.c.writeLines(MessageFormat.format(WORLD_BUILD_ERROR_ON_BUILD, e));
				}
			}
			case argTileType -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTileType));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				final String normalSuffix = "+normal"; //$NON-NLS-1$
				final String deepSuffix   = "+deep"; //$NON-NLS-1$
				final String hillSuffix   = "+hill"; //$NON-NLS-1$
				
				GroundType type = switch (args.get(i - 2).toLowerCase()) {
				case "not-explored" -> GroundType.NOT_EXPLORED; //$NON-NLS-1$
				case "water", "water+normal" -> GroundType.WATER_NORMAL; //$NON-NLS-1$ //$NON-NLS-2$
				case "sand", "sand+normal" -> GroundType.SAND; //$NON-NLS-1$ //$NON-NLS-2$
				case "grass", "grass+normal" -> GroundType.GRASS; //$NON-NLS-1$ //$NON-NLS-2$
				case "forest", "forest+normal" -> GroundType.FOREST; //$NON-NLS-1$ //$NON-NLS-2$
				case "swamp", "swamp+normal" -> GroundType.SWAMP; //$NON-NLS-1$ //$NON-NLS-2$
				case "mountain", "mountain+normal" -> GroundType.MOUNTAIN; //$NON-NLS-1$ //$NON-NLS-2$
				case normalSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || old.ground == GroundType.NOT_EXPLORED) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_CURRENT_TYPE_0_NOT_ACCEPT_1_SUFFIX,
							old == null ? GroundType.NOT_EXPLORED : old.ground, normalSuffix));
						yield null;
					}
					yield old.ground.addNormal(false, true);
				}
				case "water+deep" -> GroundType.WATER_DEEP; //$NON-NLS-1$
				case deepSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isWater()) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_CURRENT_TYPE_0_NOT_ACCEPT_1_SUFFIX,
							old == null ? GroundType.NOT_EXPLORED : old.ground, deepSuffix));
						yield null;
					}
					yield GroundType.WATER_DEEP;
				}
				case "sand+hill" -> GroundType.SAND_HILL; //$NON-NLS-1$
				case "grass+hill" -> GroundType.GRASS_HILL; //$NON-NLS-1$
				case "forest+hill" -> GroundType.FOREST_HILL; //$NON-NLS-1$
				case "swamp+hill" -> GroundType.SWAMP_HILL; //$NON-NLS-1$
				case hillSuffix -> {
					Tile old = b.get(x, y);
					if (old == null || old.ground == null || !old.ground.isHill() && !old.ground.isFlat()) {
						this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_CURRENT_TYPE_0_NOT_ACCEPT_1_SUFFIX,
							old == null ? GroundType.NOT_EXPLORED : old.ground, hillSuffix));
						yield null;
					}
					yield old.ground.addHill(false, true);
				}
				default -> {
					this.c.writeLines(MessageFormat.format(WORLD_TILE_TYPE_UNKNOWN_GROUND, args.get(i - 2)));
					yield null;
				}
				};
				if (type != null) {
					b.set(x, y, type);
				}
			}
			case argTileResource -> {
				i += 3;
				if (i >= args.size()) {
					this.c.writeLines(MessageFormat.format(NOT_ENUGH_ARGUMENTS_FOR_THE_0_ARG, argTileResource));
					return;
				}
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				int x = Integer.parseInt(args.get(i - 1));
				int y = Integer.parseInt(args.get(i));
				if (x < 0 || x >= this.world.xlen() || y < 0 || y >= this.world.ylen()) {
					this.c.writeLines(MessageFormat.format(COORDINATE_IS_OUT_OF_BOUNDS_XLEN_0_YLEN_1_X_2_Y_3, Integer.toString(this.world.xlen()),
						Integer.toString(this.world.ylen()), Integer.toString(x), Integer.toString(y)));
					return;
				}
				OreResourceType res = switch (args.get(i - 2).toLowerCase()) {
				case "none" -> OreResourceType.NONE; //$NON-NLS-1$
				case "gold" -> OreResourceType.GOLD_ORE; //$NON-NLS-1$
				case "iron" -> OreResourceType.IRON_ORE; //$NON-NLS-1$
				case "coal" -> OreResourceType.COAL_ORE; //$NON-NLS-1$
				default -> {
					this.c.writeLines(MessageFormat.format(WORLD_TILE_RESOURCE_UNKNOWN_RESOURCE, args.get(i - 2)));
					yield null;
				}
				};
				if (res != null) {
					b.set(x, y, res);
				}
			}
			case argFillRandom -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				b.fillRandom();
			}
			case argFillTotallyRandom -> {
				if (this.world == null) {
					this.c.writeLines(WORLD_MODIFY_MISSING_WORLD);
					return;
				}
				if (!(this.world instanceof RootWorld.Builder b)) {
					this.c.writeLines(WORLD_MODIFY_NO_BUILD_WORLD);
					return;
				}
				b.fillTotallyRandom();
			}
			default -> this.c.writeLines(MessageFormat.format(UNKNOWN_ARGUMENT_0, args.get(i)));
			}
		}
	}
	
	private void closeConnections() {
		Map<User, Connection> cs = this.connects;
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
	}
	
	private void cmdWorldNoArgs() {
		if (this.world == null || ask(WORLD_NOARG_C_HAGE_OR_D_ISPLAY, "cd") == 'c') { //$NON-NLS-1$
			switch (ask(WORLD_NOARG_L_OAD_FILE_CREATE_N_EW_OR_C_ANCEL, "lnc")) { //$NON-NLS-1$
			case 'l' -> cmdWorldInteractiveLoad();
			case 'n' -> {
				rootLogin(usr == null);
				int xlen = readNumber(WORLD_NOARG_PROMPT_ENTER_X_LEN, 1, Integer.MAX_VALUE);
				if (xlen > 0) {
					int ylen = readNumber(WORLD_NOARG_PROMPT_ENTER_Y_LEN, 1, Integer.MAX_VALUE);
					if (ylen > 0) {
						world = new RootWorld.Builder((RootUser) usr, xlen, ylen);
						this.c.writeLines("created new world");
						if (ask("fill world with random tiles? ([y]es|[n]o): ", "yn") == 'y') {
							((RootWorld.Builder) world).fillRandom();
							this.c.writeLines("filled world with random tiles");
							if (ask("build world? ([y]es|[n]o): ", "yn") == 'y') {
								world = ((RootWorld.Builder) world).create();
								this.c.writeLines("world builded");
							}
						}
					}
				}
			}
			case 'c' -> { return; }
			default -> throw new AssertionError("illegal return value from ask!"); //$NON-NLS-1$
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
		this.c.writeLines("Tile: (" + x + '|' + y + ')');
		this.c.writeLines("  Type: " + tile.ground);
		this.c.writeLines("  Resource: " + tile.resource);
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
				this.c.writeLines("loaded from file, build now the world");
				try {
					world = RootWorld.Builder.create((RootUser) usr, tiles);
				} catch (IllegalStateException e) {
					this.c.writeLines("build failed: " + e.toString());
					this.c.writeLines("world stays in build mode now");
				}
			} catch (IOException e) {
				this.c.writeLines("error: " + e.toString());
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
				this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
			}
		} else {
			usr = usr.makeRoot();
		}
	}
	
	private void cmdWorldAllTilesResources() {
		if (world == null) {
			this.c.writeLines("there is no world I can print");
			return;
		}
		this.c.writeLines(" : no resource");
		this.c.writeLines("G: gold ore");
		this.c.writeLines("I: iron ore");
		this.c.writeLines("C: coal ore");
		int xlen = world.xlen();
		int ylen = world.ylen();
		this.c.writeLines("world (" + xlen + '|' + ylen + "):");
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
			this.c.writeLines(b.toString());
		}
	}
	
	private void cmdWorldAllTilesType() {
		if (world == null) {
			this.c.writeLines("there is no world I can print");
			return;
		}
		this.c.writeLines("#: not explored");
		this.c.writeLines("w: water");
		this.c.writeLines("W: water deep");
		this.c.writeLines("b: sand");
		this.c.writeLines("B: sand hill");
		this.c.writeLines("g: grass");
		this.c.writeLines("G: grass hill");
		this.c.writeLines("f: forest");
		this.c.writeLines("F: forest hill");
		this.c.writeLines("s: swamp");
		this.c.writeLines("S: swamp hill");
		this.c.writeLines("m: mountain");
		int xlen = world.xlen();
		int ylen = world.ylen();
		this.c.writeLines("world (" + xlen + '|' + ylen + "):");
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
			this.c.writeLines(b.toString());
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
				this.c.writeLines(CMD_USERNAME + " help: set or get your username");
				this.c.writeLines(HELP + ": print this message");
				this.c.writeLines("set <USERNAME>: set the new username");
				this.c.writeLines("get: print the current username");
				this.c.writeLines("without arguments, print the current username and then prompt for a new username");
			}
			case "set" -> {
				i++;
				if (i >= args.size()) {
					this.c.writeLines("not enugh arguments for the set arg");
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
				this.c.writeLines(cur);
			}
			default -> this.c.writeLines("unknown argument: '" + args.get(i) + '\'');
			}
		}
	}
	
	private void cmdUsernameNoArgs() {
		String cur = usr == null ? username : usr.name();
		if (cur != null) {
			this.c.writeLines("your current username is '" + cur + '\'');
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
					this.c.writeLines("Status command: help");
					this.c.writeLines("without arguments, write all status types");
					this.c.writeLines("with arguments:");
					this.c.writeLines("  '" + HELP + "' write this message");
					this.c.writeLines("  'user' write the user status");
					this.c.writeLines("  'world-remote-size' update the size of the remote world and then write the world status");
					this.c.writeLines("  'world-remote-all' or 'world-remote-world' update the complete remote world and then write the world status");
					this.c.writeLines("  'server' write the server status");
					this.c.writeLines("  'serverpw' or 'server-pw' write the server password status");
				}
				case "user" -> cmdStatusUser();
				case "world" -> cmdStatusWorld();
				case "world-remote-size" -> cmdStatusWorldRemoteSize();
				case "world-remote-all", "world-remote-world" -> cmdStatusWorldRemoteAll();
				case "server" -> cmdStatusServer();
				case "serverpw", "server-pw" -> cmdStatusServerPW();
				default -> this.c.writeLines("unknown argument: '" + args.get(i) + "'");
				}
			}
		}
	}
	
	private void cmdStatusServerPW() {
		if (serverPW != null) {
			this.c.writeLines("serverPassword: set");
		} else {
			this.c.writeLines("serverPassword: there is no server password");
			if (serverThread != null) {
				this.c.writeLines("  note that I remove my reference of the server password after starting the server");
				this.c.writeLines("  only because I do not know a server password, does not mean that the server knows no password");
			}
		}
	}
	
	private void cmdStatusServer() {
		if (serverThread != null) {
			this.c.writeLines("MyServer: running");
		} else {
			this.c.writeLines("MyServer: there is no server");
		}
	}
	
	private void cmdStatusWorldRemoteAll() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			this.c.writeLines("there is no remote world");
			return;
		}
		try {
			rw.updateWorld();
			this.c.writeLines("World: remote world loaded");
			this.c.writeLines("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorldRemoteSize() {
		if (world == null || !(world instanceof RemoteWorld rw)) {
			this.c.writeLines("there is no remote world");
			return;
		}
		try {
			rw.updateWorldSize();
			this.c.writeLines("World: remote world loaded");
			this.c.writeLines("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void cmdStatusWorld() {
		boolean writeBounds = true;
		if (world == null) {
			this.c.writeLines("World: there is no world");
			writeBounds = false;
		} else if (world instanceof RootWorld) {
			this.c.writeLines("World: root world loaded");
		} else if (world instanceof RootWorld.Builder) {
			this.c.writeLines("World: builder world loaded");
		} else if (world instanceof RemoteWorld rw) {
			this.c.writeLines("World: remote world loaded");
			if (rw.loadedBounds()) {
				this.c.writeLines("  Bounds: not loaded");
				writeBounds = false;
			}
		} else if (world instanceof UserWorld) {
			this.c.writeLines("World: user world loaded");
		} else {
			this.c.writeLines("World: unknown world type loaded");
			this.c.writeLines("  Type: " + world.getClass().getSimpleName());
		}
		if (writeBounds) {
			this.c.writeLines("  Bounds: [xlen=" + world.xlen() + " ylen=" + world.ylen() + ']');
		}
	}
	
	private void cmdStatusUser() {
		if (usr == null) {
			this.c.writeLines("User: there is no User");
			if (username != null) {
				this.c.writeLines("  UserName: " + username);
			}
		} else {
			this.c.writeLines("User: Name: " + usr.name());
		}
	}
	
	private void cmdVersion(List<String> args) {
		if (args.size() == 1) {
			this.c.writeLines("Square Conquerer Console version: " + Settings.VERSION_STRING);
		} else if (args.size() == 2 && HELP.equalsIgnoreCase(args.get(1))) {
			this.c.writeLines("without args, I only write some version information and I can not do much more");
			this.c.writeLines("with one argument, I write this message, when the argumentis the " + HELP + " argument");
			this.c.writeLines("In all other cases I print an error message");
		} else {
			this.c.writeLines("I dont know what I should do with arguments!");
		}
	}
	
	private void cmdHelp(List<String> args) {
		switch (args.size()) {
		case 1 -> {
			this.c.writeLines("Square Conquerer Console " + Settings.VERSION_STRING + " help:");
			this.c.writeLines("Commands:");
			this.c.writeLines("  " + CMD_HELP);
			this.c.writeLines("    print this message");
			this.c.writeLines("  " + CMD_VERSION);
			this.c.writeLines("    print version information");
			this.c.writeLines("  " + CMD_STATUS);
			this.c.writeLines("    print status information");
			this.c.writeLines("  " + CMD_USERNAME);
			this.c.writeLines("    set your username");
			this.c.writeLines("  " + CMD_WORLD);
			this.c.writeLines("    change or display the world");
			this.c.writeLines("  " + CMD_SERVER);
			this.c.writeLines("    connect to a server or start/stop your server");
			this.c.writeLines("  " + CMD_SETPW);
			this.c.writeLines("    change the password (your or someone others, it doesn't matter)");
			this.c.writeLines("  " + CMD_SERVERPW);
			this.c.writeLines("    set the server password");
			this.c.writeLines("  " + CMD_QUIT);
			this.c.writeLines("    quit this program");
			this.c.writeLines("  " + CMD_EXIT);
			this.c.writeLines("    alias for " + CMD_QUIT);
			this.c.writeLines("");
			this.c.writeLines("General:");
			this.c.writeLines("  all comands support the " + HELP + " argument");
			this.c.writeLines("  If you want further information for a specific command, ask the command");
			this.c.writeLines("  or use " + CMD_HELP + " <command>");
			this.c.writeLines("");
			this.c.writeLines("  passwords will never be accepted in the arguments");
		}
		case 2 -> {
			if (CMD_HELP.equalsIgnoreCase(args.get(1))) {
				this.c.writeLines("print a general help message or the help message of the argument");
			} else {
				args.set(0, args.get(1));
				args.set(1, HELP);
				exec(args);
			}
		}
		default -> this.c.writeLines("either give me the name of an command as argument or no argument");
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
					this.c.writeLines("connected to the server, remote world created");
				} catch (IOException e) {
					this.c.writeLines("could not connect to the server: " + e.toString());
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
																this.c.writeLines("the user '" + conn.usr.name() + "' disskonnected");
															} else {
																this.c
																	.writeLines("accepted connection from '" + conn.usr.name() + "' (" + sok.getInetAddress() + ")");
															}
														},
											cs, serverPW);
									} catch (IOException e) {
										this.c.writeLines(SERVER_SERVER_STOPPED_WITH_ERROR + e.toString());
									} finally {
										synchronized (SquareConquererCUI.this) {
											if (serverThread == Thread.currentThread()) {
												serverThread = null;
												connects     = null;
											}
										}
									}
								});
				this.c.writeLines("the server should now accept connections");
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
					this.c.writeLines("the file '" + loadFile + "' does not exist");
					continue;
				}
				if (!Files.isRegularFile(loadFile)) {
					this.c.writeLines("the pat '" + loadFile + "' does not refer to a file");
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
				this.c.writeLines("load now the file '" + loadFile + "'");
				char[] pw = this.c.readPassword("enter the password: ");
				if (username != null && !RootUser.ROOT_NAME.equals(username)) {
					this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
				}
				usr = RootUser.create(pw);
			} else if (!(usr instanceof RootUser)) {
				this.c.writeLines(WORLD_LOAD_CHANGED_TO_ROOT);
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
				this.c.writeLines("error: " + e.toString());
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
				this.c.writeLines("could not build the world: " + ise);
				this.c.writeLines("the current world is in building mode");
			}
			username = null;
			this.c.writeLines("world successful loaded");
			return false;
		} catch (IOException e) {
			usr = null;
			this.c.writeLines("error: " + e.toString());
			return true;
		}
	}
	
}
