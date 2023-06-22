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
package de.hechler.patrick.games.sc.connect;

import static de.hechler.patrick.games.sc.Settings.threadStart;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.CompleteWorld;
import de.hechler.patrick.games.sc.world.OpenWorld;
import de.hechler.patrick.games.sc.world.RemoteWorld;
import de.hechler.patrick.games.sc.world.UserWorld;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.utils.interfaces.Executable;
import de.hechler.patrick.utils.interfaces.ThrowBiConsumer;
import de.hechler.patrick.utils.objects.IgnoreCloseInputStream;
import de.hechler.patrick.utils.objects.IgnoreCloseOutputStream;
import de.hechler.patrick.utils.objects.InvalidInputStream;
import de.hechler.patrick.utils.objects.InvalidOutputStream;

/**
 * this class is used to communicate with a remote connection or write to or read from a file
 * <p>
 * note that all Connection instances provide read and write methods, but with the {@link #createUnsecure(User, InputStream, OutputStream, IntConsumer, World)
 * createUnsecure} methods and the {@link OneWayAccept} class Connections without read or write support can be created (they then use the {@link InvalidInputStream}
 * or {@link InvalidOutputStream})
 * 
 * @author Patrick Hechler
 */
public class Connection implements WrongInputHandler, Closeable {
	
	/**
	 * this {@link WrongInputHandler} just always throws an {@link StreamCorruptedException} (or {@link EOFException} in
	 * {@link WrongInputHandler#wrongInputEOF(int, int)})
	 */
	public static final WrongInputHandler DEFAULT_WRONG_INPUT = new WrongInputHandler() {
		
		@Override
		public void wrongInputEOF(int expectedTotalLen, int missingLen) throws EOFException {
			throw new EOFException(
				String.format("got <EOF> expected %d bytes for a %d bytes big struct", Integer.valueOf(missingLen), Integer.valueOf(expectedTotalLen)));
		}
		
		@Override
		public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
			throw new StreamCorruptedException(
				String.format("got %d0 after I wrote %d expected %d", Integer.valueOf(read), Integer.valueOf(wrote), Integer.valueOf(expectedRead)));
		}
		
		@Override
		public int wrongInputInt(int read, @SuppressWarnings("unused") int[] expected, IntFunction<String> msgGen) throws StreamCorruptedException {
			throw new StreamCorruptedException(msgGen.apply(read));
		}
		
		@Override
		public int wrongInputInt(int read, int[] expected) throws StreamCorruptedException {
			throw new StreamCorruptedException(String.format("invalid input: got %d expected on of %s", Integer.valueOf(read), Arrays.toString(expected)));
		}
		
		@Override
		public int wrongInputInt(int read, int expected, int expected2) throws StreamCorruptedException {
			throw new StreamCorruptedException(
				String.format("invalid input: got %d expected on of [%d, %d]", Integer.valueOf(read), Integer.valueOf(expected), Integer.valueOf(expected2)));
		}
		
		@Override
		public void wrongInputInt(int read, int expected) throws StreamCorruptedException {
			throw new StreamCorruptedException(String.format("invalid input: got %d expected %d", Integer.valueOf(read), Integer.valueOf(expected)));
		}
		
		// just delegate to the int methods
		
		@Override
		public int wrongInputByte(int read, int[] expected, IntFunction<String> msgGen) throws IOException {
			return wrongInputInt(read, expected, msgGen);
		}
		
		@Override
		public int wrongInputByte(int read, int[] expected) throws IOException {
			return wrongInputInt(read, expected);
		}
		
		@Override
		public int wrongInputByte(int read, int expected, int expected2) throws IOException {
			return wrongInputInt(read, expected, expected2);
		}
		
		@Override
		public void wrongInputByte(int read, int expected) throws IOException {
			wrongInputInt(read, expected);
		}
		
		@Override
		public int wrongInputPositive(int read, boolean strictlyPositive) throws IOException {
			throw new StreamCorruptedException(String.format("invalid input got %d but expected a %s value%s!", Integer.valueOf(read),
				(strictlyPositive ? "strict positive" : "positive"), (strictlyPositive ? "" : " or zero"))); //$NON-NLS-1$
		}
		
	};
	
	/**
	 * the default port which is used to open a server
	 */
	public static final int DEFAULT_PORT = 21226;
	
	/**
	 * this value is send by the client or server before disconnection
	 */
	public static final int CON_LOG_OUT = 0x50DDC7F1;
	
	/**
	 * the user which belongs to this connection
	 */
	public final User                  usr;
	private final InputStream          in;
	private final OutputStream         out;
	private final IntConsumer          setTimeout;
	private final int                  modCnt;
	private volatile boolean           closed;
	private volatile WrongInputHandler wih;
	private World                      world;
	
	private Connection(User usr, InputStream in, OutputStream out, IntConsumer setTimeout, int modCnt, World world) {
		this.usr        = usr;
		this.in         = in;
		this.out        = out;
		this.setTimeout = setTimeout;
		this.modCnt     = modCnt;
		this.world      = world;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}
	
	/**
	 * Initializes the world associated with this connection
	 * 
	 * @param world the world which should be associated with this connection
	 * 
	 * @throws IllegalStateException if the connection already has a world
	 * 
	 * @see #world()
	 */
	public void initWorld(World world) throws IllegalStateException {
		if (this.world != null) {
			throw new IllegalStateException("the world is already set!");
		}
		this.world = world;
	}
	
	/**
	 * returns <code>true</code> if the connection already has a world associated with it and <code>false</code> if not
	 * <p>
	 * normally all Connections should have a world after leaving the create methods from one of the inner classes<br>
	 * the only way to create a {@link Connection} without a world is by using a {@link #createUnsecure(User, InputStream, OutputStream, IntConsumer, World)
	 * createUnsecure} method or by using the {@link OneWayAccept} class and setting the World argument to <code>null</code> .
	 * 
	 * @return <code>true</code> if the connection already has a world associated with it and <code>false</code> if not
	 * 
	 * @see #world()
	 * @see #initWorld(World)
	 */
	public boolean hasWorld() {
		return this.world != null;
	}
	
	/**
	 * returns the world associated with this connection
	 * 
	 * @return the world associated with this connection
	 * 
	 * @throws IllegalStateException if the world is not yet set (see {@link #hasWorld()})
	 */
	public World world() throws IllegalStateException {
		if (this.world == null) {
			throw new IllegalStateException("the world is not yet set!");
		}
		return this.world;
	}
	
	/**
	 * creates a new connection which uses the given stream directly without encrypting anything
	 * <p>
	 * the returned connection will only support read operation, no timeout and write
	 * <p>
	 * this method is like
	 * <code>{@link #createUnsecure(User, InputStream, OutputStream, IntConsumer, World) createUnsecure}(usr, in, {@link InvalidInputStream#INSTANCE}, null)</code>
	 * 
	 * @param usr   the user
	 * @param in    the input stream
	 * @param world the {@link #world()} of the connection
	 * 
	 * @return the read only connection
	 */
	public static Connection createUnsecure(User usr, InputStream in, World world) {
		return createUnsecure(usr, in, InvalidOutputStream.INSTANCE, null, world);
	}
	
	/**
	 * creates a new connection which uses the given stream directly without encrypting anything
	 * <p>
	 * the returned connection will only support write operation, no timeout and read
	 * <p>
	 * this method is like
	 * <code>{@link #createUnsecure(User, InputStream, OutputStream, IntConsumer, World) createUnsecure}(usr, {@link InvalidOutputStream#INSTANCE}, out, null)</code>
	 * 
	 * @param usr   the user
	 * @param out   the output stream
	 * @param world the {@link #world()} of the connection
	 * 
	 * @return the write only connection
	 */
	public static Connection createUnsecure(User usr, OutputStream out, World world) {
		return createUnsecure(usr, InvalidInputStream.INSTANCE, out, null, world);
	}
	
	/**
	 * creates a new connection which uses the given streams directly without encrypting anything
	 * <p>
	 * the returned connection will support read and write operation, but not timeout
	 * <p>
	 * this method is like <code>{@link #createUnsecure(User, InputStream, OutputStream, IntConsumer, World) createUnsecure}(usr, in, out, null)</code>
	 * 
	 * @param usr   the user
	 * @param in    the input stream
	 * @param out   the output stream
	 * @param world the {@link #world()} of the connection
	 * 
	 * @return the read/write connection
	 */
	public static Connection createUnsecure(User usr, InputStream in, OutputStream out, World world) {
		return createUnsecure(usr, in, out, null, world);
	}
	
	/**
	 * creates a new connection which uses the given streams directly without encrypting anything
	 * <p>
	 * if <code>setTimeout</code> has a <code>non-null</code> value, {@link #setTimeout(int) timeout} is supported
	 * <p>
	 * all other methods (except for the other <code>createUnsecure(...)</code> methods which redirect here) create connection wich use encrypt all send data streams
	 * 
	 * @param usr        the user
	 * @param in         the input stream
	 * @param out        the output stream
	 * @param setTimeout the {@link #setTimeout(int)} method or <code>null</code> if timeout is not supported
	 * @param world      the {@link #world()} of the connection
	 * 
	 * @return the connection
	 */
	public static Connection createUnsecure(User usr, InputStream in, OutputStream out, IntConsumer setTimeout, World world) {
		if (usr == null) throw new NullPointerException("usr");
		if (in == null) throw new NullPointerException("input stream");
		if (out == null) throw new NullPointerException("output stream");
		return new Connection(usr, in, out, setTimeout, usr.modifyCount(), world);
	}
	
	/**
	 * this class can be used to create a read only/write only connection from a stream
	 * 
	 * @author Patrick Hechler
	 */
	public static class OneWayAccept {
		
		private OneWayAccept() {}
		
		/**
		 * creates a read only connection from the given stream
		 * 
		 * @param in    the input stream
		 * @param usr   the user
		 * @param world the {@link #world()} of the connection
		 * 
		 * @return the read only connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection acceptReadOnly(InputStream in, User usr, World world) throws IOException {
			readInt(DEFAULT_WRONG_INPUT, in, ClientConnect.S_CONECT);
			byte[] salt = new byte[64];
			readArr(DEFAULT_WRONG_INPUT, in, salt);
			byte[] initVec = new byte[16];
			readArr(DEFAULT_WRONG_INPUT, in, initVec);
			CipherInputStream cin = null;
			try {
				int mc;
				synchronized (usr) {
					cin = usr.decrypt(in, salt, initVec);
					mc  = usr.modifyCount();
				}
				readInt(DEFAULT_WRONG_INPUT, cin, ClientConnect.FS_CONECT);
				return new Connection(usr, cin, InvalidOutputStream.INSTANCE, null, mc, world);
			} catch (Throwable t) {
				if (cin != null) {
					cin.close();
				}
				throw t;
			}
		}
		
		/**
		 * creates a write only connection from the given stream
		 * 
		 * @param out   the output stream
		 * @param usr   the user
		 * @param world the {@link #world()} of the connection
		 * 
		 * @return the write only connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection acceptWriteOnly(OutputStream out, User usr, World world) throws IOException {
			writeInt(out, ClientConnect.S_CONECT);
			byte[] salt = new byte[64];
			User.fillRandom(salt);
			out.write(salt);
			byte[] initVec = new byte[16];
			User.fillRandom(initVec);
			out.write(initVec);
			CipherOutputStream cout = null;
			try {
				int mc;
				synchronized (usr) {
					cout = usr.encrypt(out, salt, initVec);
					mc   = usr.modifyCount();
				}
				writeInt(cout, ClientConnect.FS_CONECT);
				return new Connection(usr, InvalidInputStream.INSTANCE, cout, null, mc, world);
			} catch (Throwable t) {
				if (cout != null) {
					cout.close();
				}
				throw t;
			}
		}
		
	}
	
	/**
	 * this class is used to allows remote connections to join a world
	 * 
	 * @author Patrick Hechler
	 */
	public static class ServerAccept {
		
		private ServerAccept() {}
		
		/**
		 * opens a server socket which listens on the given port and accepts remote connections to view their (user/root) world
		 * 
		 * @param port       to port on which the world should be opened
		 * @param rw         the root world which should be made accessible
		 * @param logConnect used to log (dis-)connections
		 * @param connects   the map which stores the all connected users with their connection
		 * @param serverPW   the server password or <code>null</code> if user creation is not allowed
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static void accept(int port, CompleteWorld rw, ThrowBiConsumer<Connection, Socket, IOException> logConnect, Map<User, Connection> connects,
			char[] serverPW) throws IOException {
			try (ServerSocket ss = new ServerSocket(port)) {
				System.err.println(
					String.format("[Connection.Server]: accept connections at port %d (server name: %s)", Integer.valueOf(ss.getLocalPort()), ss.getInetAddress()));
				accept(ss, rw, logConnect, connects, serverPW);
			}
		}
		
		/**
		 * opens a server which accepts on the given socket remote connections to view their (user/root) world
		 * 
		 * @param ss         the server socket which is used to accept new connections
		 * @param rw         the root world which should be made accessible
		 * @param logConnect used to log (dis-)connections
		 * @param connects   the map which stores the all connected users with their connection
		 * @param serverPW   the server password or <code>null</code> if user creation is not allowed
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static void accept(ServerSocket ss, CompleteWorld rw, ThrowBiConsumer<Connection, Socket, IOException> logConnect, Map<User, Connection> connects,
			char[] serverPW) throws IOException {
			List<Socket> soks = new ArrayList<>();
			IOException  err;
			try {
				ss.setSoTimeout(250);
				while (true) {
					try {
						Socket sok = ss.accept();
						soks.add(sok);
						threadStart(() -> {
							try {
								Connection conn = accept(sok, rw.user(), serverPW);
								User       usr  = conn.usr;
								World      uw   = UserWorld.of(rw, conn.usr, conn.modCnt);
								OpenWorld  ow   = OpenWorld.of(conn, uw);
								Connection old  = connects.put(usr, conn);
								if (old != null) {
									old.logOut();
								}
								try {
									conn.initWorld(uw);
									logConnect.accept(conn, sok);
									ow.execute();
								} finally {
									connects.remove(usr);
									logConnect.accept(conn, null);
								}
							} catch (IOException e) {
								try {
									sok.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
						});
					} catch (@SuppressWarnings("unused") SocketTimeoutException timeout) {
						if (Thread.interrupted()) throw new ClosedByInterruptException();
					}
				}
			} catch (ClosedByInterruptException | EOFException e) {
				err = e;
			} catch (IOException e) {
				if (!Thread.currentThread().isInterrupted()) throw e;
				err = e;
			} finally {
				if (serverPW != null) {
					for (int i = 0; i < serverPW.length; i++) {
						serverPW[i] = '\0';
					}
				}
			}
			for (Socket sok : soks) {
				try {
					sok.close();
				} catch (IOException e) {
					err.addSuppressed(e);
				}
			}
			throw err;
		}
		
		private static Connection accept(Socket sok, User root, char[] serverPW) throws IOException {
			InputStream  in  = sok.getInputStream();
			OutputStream out = sok.getOutputStream();
			try {
				return accept(in, out, to -> {
					try {
						sok.setSoTimeout(to);
					} catch (SocketException e) {
						throw new IOError(e);
					}
				}, root, serverPW);
			} catch (Throwable t) {
				in.close();
				out.close();
				throw t;
			}
		}
		
		private static Connection accept(InputStream in, OutputStream out, IntConsumer setTimeout, User root, char[] serverPW) throws IOException {
			if (readInt(DEFAULT_WRONG_INPUT, in, ClientConnect.C_CONECT, ClientConnect.C_NEW) != ClientConnect.C_CONECT) {
				if (serverPW == null) {
					in.close();
					out.close();
					return null;
				}
				return acceptNew(in, out, setTimeout, root, serverPW);
			}
			writeInt(out, ClientConnect.S_CONECT);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			User.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			readArr(DEFAULT_WRONG_INPUT, in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			User.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			readArr(DEFAULT_WRONG_INPUT, in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			String user = readString(DEFAULT_WRONG_INPUT, in);
			User   usr  = root.get(user);
			if (usr == null) {
				in.close();
				out.close();
				return null;
			}
			CipherInputStream  cin  = null;
			CipherOutputStream cout = null;
			try {
				int mc;
				synchronized (usr) {
					cin  = usr.decrypt(in, salt, initVec);
					cout = usr.encrypt(out, salt, initVec);
					mc   = usr.modifyCount();
				}
				writeInt(cout, ClientConnect.FS_CONECT);
				readInt(DEFAULT_WRONG_INPUT, cin, ClientConnect.FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, mc, null);
			} catch (Throwable t) {
				if (cin != null) {
					cin.close();
				}
				if (cout != null) {
					cout.close();
				}
				throw t;
			}
		}
		
		private static Connection acceptNew(InputStream in, OutputStream out, IntConsumer setTimeout, User root, char[] serverPW) throws IOException {
			writeInt(out, ClientConnect.S_NEW);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			User.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			readArr(DEFAULT_WRONG_INPUT, in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			User.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			readArr(DEFAULT_WRONG_INPUT, in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			CipherInputStream  tcin  = User.decrypt(serverPW, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream tcout = User.encrypt(serverPW, new IgnoreCloseOutputStream(out), salt, initVec);
			char[]             pw;
			try {
				readInt(DEFAULT_WRONG_INPUT, tcin, ClientConnect.C_CONECT);
				writeInt(tcout, ClientConnect.S_CONECT);
				User.fillRandom(saltHalf);
				tcout.write(saltHalf);
				System.arraycopy(saltHalf, 0, salt, 0, 32);
				readArr(DEFAULT_WRONG_INPUT, tcin, saltHalf);
				System.arraycopy(saltHalf, 0, salt, 32, 32);
				byte[] pwUTF8 = new byte[readInt(DEFAULT_WRONG_INPUT, tcin)];
				readArr(DEFAULT_WRONG_INPUT, tcin, pwUTF8);
				CharBuffer buf = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pwUTF8));
				for (int i = 0; i < pwUTF8.length; i++) {
					pwUTF8[i] = 0;
				}
				pw = new char[buf.limit()];
				buf.get(pw);
				for (int i = 0; i < pw.length; i++) {
					buf.put(i, '\0');
				}
				User.fillRandom(initVecHalf);
				tcout.write(initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 0, 8);
				readArr(DEFAULT_WRONG_INPUT, tcin, initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			} finally {
				tcin.close();
				tcout.close();
			}
			CipherInputStream  cin  = User.decrypt(pw, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream cout = User.encrypt(pw, new IgnoreCloseOutputStream(out), salt, initVec);
			try {
				writeInt(cout, ClientConnect.SUB_NEW);
				readInt(DEFAULT_WRONG_INPUT, cin, ClientConnect.SUB_NEW);
				String name = readString(DEFAULT_WRONG_INPUT, cin);
				User   usr  = root.addUser(name, pw);
				writeInt(cout, ClientConnect.FS_CONECT);
				readInt(DEFAULT_WRONG_INPUT, cin, ClientConnect.FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, 0, null);
			} catch (Throwable t) {
				cin.close();
				cout.close();
				throw t;
			}
		}
		
	}
	
	/**
	 * this class is used to connect to a remote connection
	 * 
	 * @author Patrick Hechler
	 */
	public static class ClientConnect {
		
		private ClientConnect() {}
		
		/**
		 * <ol>
		 * <li>Client: {@link #C_CONECT}</li>
		 * <li>Server: {@link #S_CONECT}</li>
		 * <li>Server: 32-byte salt (the first bytes)</li>
		 * <li>Client: 32-byte salt (the last bytes)</li>
		 * <li>Server: 8-byte InitVec (the first bytes)</li>
		 * <li>Client: 8-byte InitVec (the last bytes)</li>
		 * <li>Client: user-name length (UTF-8 encoded)</li>
		 * <li>Client: user-name data (UTF-8 encoded)</li>
		 * <li>Both: encrypt connection</li>
		 * <li>Server: {@link #FS_CONECT}</li>
		 * <li>Client: {@link #FC_CONECT}</li>
		 * </ol>
		 */
		private static final int C_CONECT  = 0x5E0C7D0F;
		private static final int S_CONECT  = 0x3AE85D1B;
		private static final int FS_CONECT = 0x7549D188;
		private static final int FC_CONECT = 0x23698F96;
		
		/**
		 * <ol>
		 * <li>Client: {@link #C_NEW}</li>
		 * <li>Server: {@link #S_NEW}</li>
		 * <li>Server: 32-byte salt (the first bytes)</li>
		 * <li>Client: 32-byte salt (the last bytes)</li>
		 * <li>Server: 8-byte InitVec (the first bytes)</li>
		 * <li>Client: 8-byte InitVec (the last bytes)</li>
		 * <li>Both: temporary encrypt connection</li>
		 * <li>Client: {@link #C_CONECT}</li>
		 * <li>Server: {@link #S_CONECT}</li>
		 * <li>Server: 32-byte salt (the first bytes)</li>
		 * <li>Client: 32-byte salt (the last bytes)</li>
		 * <li>Client: password length (UTF-8 encoded)</li>
		 * <li>Client: password data (UTF-8 encoded)</li>
		 * <li>Server: 8-byte InitVec (the first bytes)</li>
		 * <li>Client: 8-byte InitVec (the last bytes)</li>
		 * <li>Both: encrypt connection</li>
		 * <li>Server: {@link #SUB_NEW}</li>
		 * <li>Client: {@link #SUB_NEW}</li>
		 * <li>Client: user-name length (UTF-8 encoded)</li>
		 * <li>Client: user-name data (UTF-8 encoded)</li>
		 * <li>Server: {@link #FS_CONECT}</li>
		 * <li>Client: {@link #FC_CONECT}</li>
		 * </ol>
		 * 
		 * @see #C_CONECT
		 */
		private static final int C_NEW   = 0x0F7B0AE7;
		private static final int S_NEW   = 0x35712540;
		private static final int SUB_NEW = 0x68213004;
		
		/**
		 * connects to a remote connection with the given address on the given port and creates a new remote user
		 * 
		 * @param addr     the server address
		 * @param port     the remote port to connect to
		 * @param usr      the user to create
		 * @param serverPW the server password
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connectNew(InetAddress addr, int port, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(addr, port), usr, serverPW);
		}
		
		/**
		 * connects to a remote connection with the given host on the given port and creates a new remote user
		 * 
		 * @param host     the remote host
		 * @param port     the remote port to connect to
		 * @param usr      the user to create
		 * @param serverPW the server password
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connectNew(String host, int port, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(host, port), usr, serverPW);
		}
		
		/**
		 * connects to a remote connection with the given socket and creates a new remote user
		 * 
		 * @param sok      the remote socket
		 * @param usr      the user to create
		 * @param serverPW the server password
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connectNew(Socket sok, User usr, char[] serverPW) throws IOException {
			return connectNew(sok.getInputStream(), sok.getOutputStream(), to -> {
				try {
					sok.setSoTimeout(to);
				} catch (SocketException e) {
					throw new IOError(e);
				}
			}, usr, serverPW);
		}
		
		/**
		 * connects to a remote connection with the given streams and creates a new remote user
		 * 
		 * @param in         the input stream
		 * @param out        the output stream
		 * @param setTimeout the {@link Connection#setTimeout(int) setTimeout} method
		 * @param usr        the user to create
		 * @param serverPW   the server password
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connectNew(InputStream in, OutputStream out, IntConsumer setTimeout, User usr, char[] serverPW) throws IOException {
			if (serverPW == null) throw new NullPointerException("server password");
			writeInt(out, C_NEW);
			readInt(DEFAULT_WRONG_INPUT, in, S_NEW);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			readArr(DEFAULT_WRONG_INPUT, in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			User.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			readArr(DEFAULT_WRONG_INPUT, in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			User.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			CipherInputStream  tcin  = User.decrypt(serverPW, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream tcout = User.encrypt(serverPW, new IgnoreCloseOutputStream(out), salt, initVec);
			try {
				for (int i = 0; i < serverPW.length; i++) {
					serverPW[i] = '\0';
				}
				writeInt(tcout, C_CONECT);
				readInt(DEFAULT_WRONG_INPUT, tcin, S_CONECT);
				readArr(DEFAULT_WRONG_INPUT, tcin, saltHalf);
				System.arraycopy(saltHalf, 0, salt, 0, 32);
				User.fillRandom(saltHalf);
				tcout.write(saltHalf);
				System.arraycopy(saltHalf, 0, salt, 32, 32);
				ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(usr._pw()));
				int        len = buf.limit();
				writeInt(tcout, len);
				if (buf.hasArray()) {
					byte[] arr = buf.array();
					tcout.write(arr, buf.arrayOffset(), buf.limit());
					for (int i = 0; i < buf.limit(); i++) {
						buf.put(i, (byte) 0);
					}
				} else {
					byte[] tmp = new byte[buf.limit()];
					buf.get(tmp);
					tcout.write(tmp, 0, buf.limit());
					for (int i = 0; i < tmp.length; i++) {
						tmp[i] = 0;
						buf.put(i, (byte) 0);
					}
				}
				readArr(DEFAULT_WRONG_INPUT, tcin, initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 0, 8);
				User.fillRandom(initVecHalf);
				tcout.write(initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			} finally {
				tcin.close(); // close the cipher, but not the backing stream
				tcout.close();
			}
			CipherInputStream  cin  = null;
			CipherOutputStream cout = null;
			try {
				int mc;
				synchronized (usr) {
					cin  = usr.decrypt(in, salt, initVec);
					cout = usr.encrypt(out, salt, initVec);
					mc   = usr.modifyCount();
				}
				readInt(DEFAULT_WRONG_INPUT, cin, SUB_NEW);
				writeInt(cout, SUB_NEW);
				writeString(cout, usr.name());
				readInt(DEFAULT_WRONG_INPUT, cin, FS_CONECT);
				writeInt(cout, FC_CONECT);
				Connection conn = new Connection(usr, cin, cout, setTimeout, mc, null);
				conn.initWorld(new RemoteWorld(conn));
				return conn;
			} catch (Throwable t) {
				if (cin != null) {
					cin.close();
				}
				if (cout != null) {
					cout.close();
				}
				throw t;
			}
		}
		
		/**
		 * connects to a remote connection with the given address on the given port and remote user
		 * 
		 * @param addr the server address
		 * @param port the remote port to connect to
		 * @param usr  the user
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connect(InetAddress addr, int port, User usr) throws IOException {
			return connect(new Socket(addr, port), usr);
		}
		
		/**
		 * connects to a remote connection with the given host on the given port and remote user
		 * 
		 * @param host the server host
		 * @param port the remote port to connect to
		 * @param usr  the user
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connect(String host, int port, User usr) throws IOException {
			return connect(new Socket(host, port), usr);
		}
		
		/**
		 * connects to a remote connection with the given socket and remote user
		 * 
		 * @param sok the remote socket
		 * @param usr the user
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connect(Socket sok, User usr) throws IOException {
			return connect(sok.getInputStream(), sok.getOutputStream(), to -> {
				try {
					sok.setSoTimeout(to);
				} catch (SocketException e) {
					throw new IOError(e);
				}
			}, usr);
		}
		
		/**
		 * connects to a remote connection with the streams and remote user
		 * 
		 * @param in         the input stream
		 * @param out        the output stream
		 * @param setTimeout the {@link Connection#setTimeout(int) setTimeout} method
		 * @param usr        the user
		 * 
		 * @return the opened connection
		 * 
		 * @throws IOException if an IO error occurs
		 */
		public static Connection connect(InputStream in, OutputStream out, IntConsumer setTimeout, User usr) throws IOException {
			writeInt(out, C_CONECT);
			readInt(DEFAULT_WRONG_INPUT, in, S_CONECT);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			readArr(DEFAULT_WRONG_INPUT, in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			User.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			readArr(DEFAULT_WRONG_INPUT, in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			User.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			byte[] userArr = usr.name().getBytes(StandardCharsets.UTF_8);
			writeInt(out, userArr.length);
			out.write(userArr);
			CipherInputStream  cin  = null;
			CipherOutputStream cout = null;
			try {
				int mc;
				synchronized (usr) {
					cin  = usr.decrypt(in, salt, initVec);
					cout = usr.encrypt(out, salt, initVec);
					mc   = usr.modifyCount();
				}
				readInt(DEFAULT_WRONG_INPUT, cin, FS_CONECT);
				writeInt(cout, FC_CONECT);
				Connection conn = new Connection(usr, cin, cout, setTimeout, mc, null);
				conn.initWorld(new RemoteWorld(conn));
				return conn;
			} catch (Throwable t) {
				if (cin != null) {
					cin.close();
				}
				if (cout != null) {
					cout.close();
				}
				throw t;
			}
		}
		
	}
	
	/**
	 * replaces the {@link WrongInputHandler} of this connection
	 * <p>
	 * all connections starts with a <code>null</code> {@link WrongInputHandler}<br>
	 * so to initialize the {@link WrongInputHandler}, pass <code>null</code> as <code>oldWIH</code><br>
	 * otherwise pass the {@link WrongInputHandler}, which was passed as <code>newWIH</code> of the last successful call to this method
	 * 
	 * @param oldWIH the expected old {@link WrongInputHandler}
	 * @param newWIH the new {@link WrongInputHandler}
	 * 
	 * @throws IllegalStateException if the current {@link WrongInputHandler} is not the same as the <code>oldWIH</code>
	 */
	public synchronized void replaceWongInput(WrongInputHandler oldWIH, WrongInputHandler newWIH) throws IllegalStateException {
		if (this.wih != oldWIH) throw new IllegalStateException("I have not the expected wih");
		this.wih = newWIH;
	}
	
	/**
	 * act like a wrong EOF was read
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("null")
	public void wrongInputEOF(int expectedTotalLen, int missingLen) throws StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputEOF(expectedTotalLen, missingLen);
		cwih.wrongInputEOF(expectedTotalLen, missingLen);
	}
	
	/**
	 * act like an invalid <code>byte</code> was read {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("null")
	public void wrongInputByte(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
		cwih.wrongInputByte(read, expected);
	}
	
	/**
	 * act like an invalid <code>byte</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputByte(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputByte(read, expected, expected2);
		return cwih.wrongInputByte(read, expected, expected2);
	}
	
	/**
	 * act like an invalid <code>byte</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputByte(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
		return cwih.wrongInputByte(read, expected);
	}
	
	/**
	 * act like an invalid <code>byte</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputByte(int read, int[] expected, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputByte(read, expected, msgGen);
		return cwih.wrongInputByte(read, expected, msgGen);
	}
	
	/**
	 * act like an invalid <code>int</code> was read after <code>wrote</code> was sent {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("null")
	public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputWRInt(read, wrote, expectedRead);
		cwih.wrongInputWRInt(read, wrote, expectedRead);
	}
	
	/**
	 * act like an invalid <code>int</code> was read {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("null")
	public void wrongInputInt(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
		cwih.wrongInputInt(read, expected);
	}
	
	/**
	 * act like an invalid <code>int</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputInt(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputInt(read, expected, expected2);
		return cwih.wrongInputInt(read, expected, expected2);
	}
	
	/**
	 * act like an invalid <code>int</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputInt(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
		return cwih.wrongInputInt(read, expected);
	}
	
	/**
	 * act like an invalid <code>int</code> was read {@inheritDoc}
	 */
	@Override
	public int wrongInputInt(int read, int[] expected, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputInt(read, expected, msgGen);
		return cwih.wrongInputInt(read, expected, msgGen);
	}
	
	/**
	 * act like an invalid <code>int</code> was read
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int wrongInputPositive(int read, boolean strictlyPositive) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT.wrongInputPositive(read, strictlyPositive);
		return cwih.wrongInputPositive(read, strictlyPositive);
	}
	
	/**
	 * set the timeout of this connection.
	 * <p>
	 * if the timeout was created using streams and no timeout method was passed to the creator, an {@link UnsupportedOperationException} will be thrown
	 * 
	 * @param timeout the new timeout
	 * 
	 * @throws UnsupportedOperationException if this connection does not support timeout
	 */
	public void setTimeout(int timeout) throws UnsupportedOperationException {
		if (this.setTimeout == null) throw new UnsupportedOperationException("I support no timeout");
		this.setTimeout.accept(timeout);
	}
	
	/**
	 * this is the same as {@link #blocked(int, Executable, Executable) blocked(0, exec, null)}
	 * 
	 * @param <T>  the type of the throwable the executable may throw
	 * @param exec the executable to execute
	 * 
	 * @throws T if the executable throws it
	 */
	public <T extends Throwable> void blocked(Executable<T> exec) throws T {
		blocked(0, exec, null);
	}
	
	/**
	 * this executable just returns directly
	 */
	public static final Executable<IOException> NOP = () -> {
		//
	};
	
	/**
	 * returns an executable wich directly returns without doing anything
	 * 
	 * @param <T> the type of the executable, the returned exec will never throw an exception itself
	 * 
	 * @return an executable wich directly returns without doing anything
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> Executable<T> nop() {
		return (Executable<T>) NOP;
	}
	
	/**
	 * blocks this connection to execute the given {@link Executable}
	 * <p>
	 * note that this method only guarantees that no other thread invokes code with the {@code blocked(DEFAULT_WRONG_INPUT, int, Executable, int)} method.<br>
	 * it is still possible to use the read/write methods without being blocked by this method
	 * <p>
	 * also note that this method will fail if no timeout is supported
	 * <p>
	 * if no timeout was reached the timeoutHandler is ignored (for example if timeout is {@code 0}, <code>null</code> can safely be passed)
	 * 
	 * @param <T>            the throwable which can be thrown by the executable
	 * @param timeout        the timeout for this connection during (and after) the execution
	 * @param exec           the code to be executed while this connection is blocked
	 * @param timeoutHandler the executable to be executed after the timeout was reached if no timeout was reached the timeoutHandler is ignored (for example if
	 *                           timeout is {@code 0}, <code>null</code> can safely be passed)
	 * 							
	 * @throws T if the executable throws it
	 */
	public <T extends Throwable> void blocked(int timeout, Executable<T> exec, Executable<T> timeoutHandler) throws T {
		synchronized (this) {
			setTimeout(timeout);
			try {
				exec.execute();
			} catch (Throwable t) {
				if (!(t instanceof SocketTimeoutException)) throw t;
				timeoutHandler.execute();
			}
		}
	}
	
	/**
	 * returns the modify count of the user when this connection was created
	 * 
	 * @return the modify count of the user when this connection was created
	 */
	public int modCnt() {
		return this.modCnt;
	}
	
	/**
	 * writes the <code>write</code> value and then expects <code>read</code>
	 * 
	 * @param write the value to write
	 * @param read  the expected response
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeReadInt(int write, int read) throws IOException {
		writeInt(write);
		int val = readInt();
		if (val == read) return;
		wrongInputWRInt(val, write, read);
	}
	
	/**
	 * reads an <code>int</code> value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readInt() throws IOException {
		return readInt(this, this.in);
	}
	
	/**
	 * reads a non-strict positive <code>int</code> (zero is allowed)
	 * 
	 * @return the positive number which was read
	 * 
	 * @throws IOException if an IO-error occurs
	 */
	public int readPos() throws IOException {
		return readPos(this, this.in);
	}
	
	/**
	 * reads a strictly positive <code>int</code> (zero is not allowed)
	 * 
	 * @return the strict positive number which was read
	 * 
	 * @throws IOException if an IO-error occurs
	 */
	public int readStrictPos() throws IOException {
		return readStrictPos(this, this.in);
	}
	
	/**
	 * reads a <code>byte</code> value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readByte() throws IOException {
		return readByte(this, this.in);
	}
	
	/**
	 * reads one of the expected <code>byte</code> values
	 * <p>
	 * if one of the two values is <code>-1</code>, EOF is valid and will result in the return value <code>-1</code>
	 * 
	 * @param a an expected value
	 * @param b an expected value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readByte(int a, int b) throws IOException {
		return readByte(this, this.in, a, b);
	}
	
	/**
	 * reads one of the expected <code>byte</code> values
	 * <p>
	 * if one of the two values is <code>-1</code>, EOF is valid and will result in the return value <code>-1</code>
	 * 
	 * @param a an expected value
	 * @param b an expected value
	 * @param c an expected value
	 * @param d an expected value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readByte(int a, int b, int c, int d) throws IOException {
		return readByte(this, this.in, a, b, c, d);
	}
	
	/**
	 * reads an <code>int</code> value or EOF
	 * <p>
	 * on EOF <code>-1L</code> is returned<br>
	 * otherwise the unsigned int value is returned (see {@link Integer#toUnsignedLong(int)})
	 * 
	 * @return the value wich was read or <code>-1L</code> on EOF
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public long readInt0() throws IOException {
		return readInt0(this, this.in);
	}
	
	/**
	 * reads an {@link String}
	 * <ol>
	 * <li>first {@link #readPos()} is used to get the {@link StandardCharsets#UTF_8 UTF-8} length of the {@link String}</li>
	 * <li>the {@link #readArr(byte[])} is used to read length bytes</li>
	 * <li>the byte array is converted to a {@link String} (using the {@link StandardCharsets#UTF_8 UTF-8} charset)</li>
	 * </ol>
	 * 
	 * @return the string wich was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public String readString() throws IOException {
		return readString(this, this.in);
	}
	
	/**
	 * reads a <code>long</code> value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public long readLong() throws IOException {
		return readLong(this, this.in);
	}
	
	/**
	 * reads a {@link UUID} value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public UUID readUUID() throws IOException {
		long low  = readLong(this, this.in);
		long high = readLong(this, this.in);
		return new UUID(high, low);
	}
	
	/**
	 * reads the expected <code>int</code> value
	 * 
	 * @param val the expected value
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void readInt(int val) throws IOException {
		readInt(this, this.in, val);
	}
	
	/**
	 * reads on of the expected <code>int</code> values
	 * 
	 * @param a an expected value
	 * @param b an expected value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readInt(int a, int b) throws IOException {
		return readInt(this, this.in, a, b);
	}
	
	/**
	 * reads on of the expected <code>int</code> values
	 * 
	 * @param vals the expected values
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readInt(int... vals) throws IOException {
		int reat = readInt(this, this.in);
		for (int val : vals) {
			if (reat == val) { return val; }
		}
		return wrongInputInt(reat, vals);
	}
	
	/**
	 * reads on of the expected <code>int</code> values
	 * 
	 * @param msg the error message generator
	 * @param a   an expected value
	 * @param b   an expected value
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readInt(IntFunction<String> msg, int a, int b) throws IOException {
		int reat = readInt(this, this.in);
		if (a == reat || b == reat) return reat;
		return wrongInputInt(reat, new int[] { a, b }, msg);
	}
	
	/**
	 * reads on of the expected <code>int</code> values
	 * 
	 * @param msg  the error message generator
	 * @param vals the expected values
	 * 
	 * @return the value which was read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public int readInt(IntFunction<String> msg, int... vals) throws IOException {
		int reat = readInt(this, this.in);
		for (int val : vals) {
			if (reat == val) return val;
		}
		return wrongInputInt(reat, vals, msg);
	}
	
	/** @see #writeClass(Class) */
	public static final int CLS_NAMED   = 0x0621BAC4;
	/** @see #writeClass(Class) */
	public static final int CLS_UNNAMED = 0xA216458E;
	
	/**
	 * reads an class
	 * <ol>
	 * <li>first a <code>{@link #readInt(int, int) readInt}({@value #CLS_NAMED}, {@value #CLS_UNNAMED})</code> is done</li>
	 * <li>if the class was in a {@link Module#isNamed() named} {@link Class#getModule() module} was send the its {@link Module#getName() name} is
	 * {@link #readString() read}</li>
	 * <li>at last the {@link Class#getName() name} of the class is read</li>
	 * </ol>
	 * 
	 * @return the class which was read
	 * 
	 * @throws IOException          if an IO error occurs
	 * @throws NoClassDefFoundError if the class (or module) could not be found
	 */
	public Class<?> readClass() throws IOException, NoClassDefFoundError {
		try {
			if (readInt(CLS_NAMED, CLS_UNNAMED) == CLS_NAMED) {
				String modName = readString();
				Module mod     =
					ModuleLayer.boot().findModule(modName).orElseThrow(() -> new NoClassDefFoundError(String.format("could not find the module %s", modName)));
				return Class.forName(mod, readString());
			}
			return Class.forName(readString());
		} catch (ClassNotFoundException cnfe) {
			throw new NoClassDefFoundError(cnfe.toString());
		}
	}
	
	/**
	 * writes an class
	 * <ol>
	 * <li>if the class {@link Class#getModule() module} is {@link Module#isNamed() named}, {@value #CLS_NAMED} is send, otherwise {@value #CLS_UNNAMED} is send</li>
	 * <li>if the class {@link Class#getModule() module} is {@link Module#isNamed() named}, the {@link Module#getName()} is send (see
	 * {@link #writeString(String)})</li>
	 * <li>the {@link Class#getName()} is send</li>
	 * </ol>
	 * 
	 * @param cls the class to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeClass(Class<?> cls) throws IOException {
		Module mod = cls.getModule();
		if (mod.isNamed()) {
			writeInt(CLS_NAMED);
			writeString(this.out, mod.getName());
		} else {
			writeInt(CLS_UNNAMED);
		}
		writeString(this.out, cls.getName());
	}
	
	/**
	 * reads the given array
	 * 
	 * @param arr the array to read
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void readArr(byte[] arr) throws IOException {
		readArr(this, this.in, arr);
	}
	
	/**
	 * writes the given <code>int</code> value
	 * 
	 * @param val the value to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeInt(int val) throws IOException {
		writeInt(this.out, val);
	}
	
	/**
	 * writes the given <code>long</code> value
	 * 
	 * @param val the value to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeLong(long val) throws IOException {
		writeLong(this.out, val);
	}
	
	/**
	 * writes the given {@link UUID} value
	 * 
	 * @param val the value to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeUUID(UUID val) throws IOException {
		writeLong(this.out, val.getLeastSignificantBits());
		writeLong(this.out, val.getMostSignificantBits());
	}
	
	/**
	 * writes the given <code>byte</code> value
	 * 
	 * @param val the value to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeByte(int val) throws IOException {
		if ((val & 0xFF) != val) throw new IllegalArgumentException(String.format("value out of the byte bounds %X", Integer.valueOf(val)));
		this.out.write(val);
	}
	
	/**
	 * writes the given {@link String} value
	 * <ol>
	 * <li>first the {@link String#length()} is written (see {@link #writeInt(int)})</li>
	 * <li>the its content array <code>{@link String#getBytes() String.getBytes}({@link StandardCharsets#UTF_8})</code> (see {@link #writeArr(byte[])})
	 * </ol>
	 * 
	 * @param str the value to write
	 * 
	 * @throws IOException if an IO error occurs
	 * 
	 * @see #readString()
	 */
	public void writeString(String str) throws IOException {
		writeString(this.out, str);
	}
	
	/**
	 * writes the given data
	 * 
	 * @param arr the array to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeArr(byte[] arr) throws IOException {
		this.out.write(arr);
	}
	
	/**
	 * writes the given data
	 * 
	 * @param arr the array containing the data to write
	 * @param off the offset from the array
	 * @param len the number of bytes to write
	 * 
	 * @throws IOException if an IO error occurs
	 */
	public void writeArr(byte[] arr, int off, int len) throws IOException {
		this.out.write(arr, off, len);
	}
	
	/**
	 * {@link #writeInt(int) writes} {@value #CON_LOG_OUT} and then {@link #close() closes} this connection
	 * 
	 * @throws IOException if an IO error occurs (the connection will be {@link #close() closed} anyway)
	 */
	public void logOut() throws IOException {
		blocked(() -> {
			if (this.closed) return;
			try {
				writeInt(CON_LOG_OUT);
			} finally {
				close();
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		if (this.closed) return;
		this.closed = true;
		try {
			this.in.close();
		} finally {
			this.out.close();
		}
	}
	
	/**
	 * returns <code>true</code> if the connection was already {@link #close() closed} and <code>false</code> if it is still open
	 * 
	 * @return <code>true</code> if the connection was already {@link #close() closed}
	 */
	public boolean closed() {
		return this.closed;
	}
	
	private static void writeString(OutputStream out, String str) throws IOException {
		byte[] arr = str.getBytes(StandardCharsets.UTF_8);
		writeInt(out, arr.length);
		out.write(arr);
	}
	
	private static void writeInt(OutputStream out, int value) throws IOException {
		out.write(new byte[] { (byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24) });
	}
	
	private static void writeLong(OutputStream out, long value) throws IOException {
		out.write(new byte[] { (byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24), (byte) (value >>> 32), (byte) (value >>> 40),
			(byte) (value >>> 48), (byte) (value >>> 56) });
	}
	
	private static int readByte(WrongInputHandler wih, InputStream in) throws IOException {
		int val = in.read();
		if (val != -1) return val;
		wih.wrongInputEOF(1, 1);
		throw new AssertionError("eof handler returned normally!");
	}
	
	private static int readByte(WrongInputHandler wih, InputStream in, int a, int b) throws IOException {
		int val = in.read();
		if (val == a || val == b) return val;
		if (val == -1) wih.wrongInputEOF(1, 1);
		return wih.wrongInputByte(val, a, b);
	}
	
	private static int readByte(WrongInputHandler wih, InputStream in, int a, int b, int c, int d) throws IOException {
		int val = in.read();
		if (val == a || val == b || val == c || val == d) return val;
		if (val == -1) wih.wrongInputEOF(1, 1);
		return wih.wrongInputByte(val, new int[] { a, b, c, d });
	}
	
	private static long readLong(WrongInputHandler wih, InputStream in) throws IOException {
		byte[] arr = new byte[8];
		readArr(wih, in, arr);
		long val = 0xFFL & arr[0];
		val |= (0xFFL & arr[1]) << 8;
		val |= (0xFFL & arr[2]) << 16;
		val |= (0xFFL & arr[3]) << 24;
		val |= (0xFFL & arr[4]) << 32;
		val |= (0xFFL & arr[5]) << 40;
		val |= (0xFFL & arr[6]) << 48;
		val |= (0xFFL & arr[7]) << 56;
		return val;
	}
	
	private static int readInt(WrongInputHandler wih, InputStream in) throws IOException {
		byte[] arr = new byte[4];
		readArr(wih, in, arr);
		int val = 0xFF & arr[0];
		val |= (0xFF & arr[1]) << 8;
		val |= (0xFF & arr[2]) << 16;
		val |= (0xFF & arr[3]) << 24;
		return val;
	}
	
	@SuppressWarnings("cast")
	private static long readInt0(WrongInputHandler wih, InputStream in) throws IOException {
		int val = in.read();
		if (val == -1) return -1L;
		byte[] arr = new byte[3];
		readArr(wih, in, arr);
		val |= (0xFF & arr[0]) << 8;
		val |= (0xFF & arr[1]) << 16;
		val |= (0xFF & arr[2]) << 24;
		return (long) val;
	}
	
	private static void readArr(WrongInputHandler wih, InputStream in, byte[] arr) throws IOException {
		int i = 0;
		while (i < arr.length) {
			int r = in.read(arr, i, arr.length - i);
			if (r == -1) wih.wrongInputEOF(arr.length, arr.length - i);
			i += r;
		}
	}
	
	private static String readString(WrongInputHandler wih, InputStream in) throws IOException {
		int    len     = readPos(wih, in);
		byte[] userArr = new byte[len];
		readArr(wih, in, userArr);
		return new String(userArr, StandardCharsets.UTF_8);
	}
	
	private static int readPos(WrongInputHandler wih, InputStream in) throws IOException {
		int val = readInt(wih, in);
		if (val >= 0) return val;
		return wih.wrongInputPositive(val, false);
	}
	
	private static int readStrictPos(WrongInputHandler wih, InputStream in) throws IOException {
		int val = readInt(wih, in);
		if (val > 0) return val;
		return wih.wrongInputPositive(val, true);
	}
	
	private static void readInt(WrongInputHandler wih, InputStream in, int value) throws IOException {
		int reat = readInt(wih, in);
		if (reat == value) return;
		wih.wrongInputInt(reat, value);
	}
	
	private static int readInt(WrongInputHandler wih, InputStream in, int valueA, int valueB) throws IOException {
		int reat = readInt(wih, in);
		if (reat == valueA || reat == valueB) return reat;
		return wih.wrongInputInt(reat, valueA, valueB);
	}
	
}
