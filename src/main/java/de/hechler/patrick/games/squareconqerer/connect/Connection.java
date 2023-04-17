package de.hechler.patrick.games.squareconqerer.connect;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.interfaces.Executable;
import de.hechler.patrick.games.squareconqerer.interfaces.ThrowBiConsumer;
import de.hechler.patrick.games.squareconqerer.objects.IgnoreCloseInputStream;
import de.hechler.patrick.games.squareconqerer.objects.IgnoreCloseOutputStream;
import de.hechler.patrick.games.squareconqerer.objects.InvalidInputStream;
import de.hechler.patrick.games.squareconqerer.objects.InvalidOutputStream;
import de.hechler.patrick.games.squareconqerer.world.OpenWorld;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;

public class Connection implements Closeable {
	
	public static final int DEFAULT_PORT = 21226;
	
	public static final int CON_LOG_OUT = 0x50DDC7F1;
	
	public final User          usr;
	private final InputStream  in;
	private final OutputStream out;
	private final IntConsumer  setTimeout;
	private final int          modCnt;
	private volatile boolean   closed;
	
	private Connection(User usr, InputStream in, OutputStream out, IntConsumer setTimeout, int modCnt) {
		this.usr        = usr;
		this.in         = in;
		this.out        = out;
		this.setTimeout = setTimeout;
		this.modCnt     = modCnt;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}
	
	public static class OneWayAccept {
		
		private OneWayAccept() {}
		
		public static Connection acceptReadOnly(InputStream in, User usr) throws IOException {
			readInt(in, ClientConnect.S_CONECT);
			byte[] salt = new byte[64];
			readArr(in, salt);
			byte[] initVec = new byte[16];
			readArr(in, initVec);
			CipherInputStream cin = null;
			try {
				int mc;
				synchronized (usr) {
					cin = usr.decrypt(in, salt, initVec);
					mc  = usr.modifyCount();
				}
				readInt(cin, ClientConnect.FS_CONECT);
				return new Connection(usr, cin, InvalidOutputStream.INSTANCE, null, mc);
			} catch (Throwable t) {
				if (cin != null) {
					cin.close();
				}
				throw t;
			}
		}
		
		public static Connection acceptWriteOnly(OutputStream out, User usr) throws IOException {
			writeInt(out, ClientConnect.S_CONECT);
			byte[] salt = new byte[64];
			usr.fillRandom(salt);
			out.write(salt);
			byte[] initVec = new byte[16];
			usr.fillRandom(initVec);
			out.write(initVec);
			CipherOutputStream cout = null;
			try {
				int mc;
				synchronized (usr) {
					cout = usr.encrypt(out, salt, initVec);
					mc   = usr.modifyCount();
				}
				writeInt(cout, ClientConnect.FS_CONECT);
				return new Connection(usr, InvalidInputStream.INSTANCE, cout, null, mc);
			} catch (Throwable t) {
				if (cout != null) {
					cout.close();
				}
				throw t;
			}
		}
		
	}
	
	public static class ServerAccept {
		
		private ServerAccept() {}
		
		public static void accept(int port, RootWorld rw, ThrowBiConsumer<Connection, Socket, IOException> logConnect, Map<User, Connection> connects,
				char[] serverPW) throws IOException {
			try (ServerSocket ss = new ServerSocket(port)) {
				System.err.println("[Connect.ServerAccept]: accept connections at " + ss.getInetAddress() + " : " + ss.getLocalPort());
				accept(ss, rw, logConnect, connects, serverPW);
			}
		}
		
		public static void accept(ServerSocket ss, RootWorld rw, ThrowBiConsumer<Connection, Socket, IOException> logConnect,
				Map<User, Connection> connects, char[] serverPW) throws IOException {
			List<Socket> soks = new ArrayList<>();
			IOException  err;
			try {
				ss.setSoTimeout(250);
				while (true) {
					try {
						Socket sok = ss.accept();
						soks.add(sok);
						threadBuilder().start(() -> {
							try {
								Connection conn = accept(sok, rw.user(), serverPW);
								User       usr  = conn.usr;
								World      uw   = new UserWorld(rw, conn.usr, conn.modCnt);
								OpenWorld  ow   = OpenWorld.of(conn, uw);
								Connection old  = connects.put(usr, conn);
								if (old != null) {
									old.blocked(() -> {
										old.writeInt(CON_LOG_OUT);
										old.close();
									});
								}
								try {
									logConnect.accept(conn, sok);
									ow.execute();
								} finally {
									connects.remove(usr);
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
					} catch (SocketTimeoutException timeout) {
						if (Thread.interrupted()) {
							throw new ClosedByInterruptException();
						}
					}
				}
			} catch (ClosedByInterruptException | EOFException e) {
				err = e;
			} catch (IOException e) {
				if (!Thread.currentThread().isInterrupted()) {
					throw e;
				}
				err = e;
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
		
		private static Connection accept(Socket sok, RootUser root, char[] serverPW) throws IOException {
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
		
		private static Connection accept(InputStream in, OutputStream out, IntConsumer setTimeout, RootUser root, char[] serverPW)
				throws IOException {
			if (readInt(in, ClientConnect.C_CONECT, ClientConnect.C_NEW) != ClientConnect.C_CONECT) {
				if (serverPW == null) {
					in.close();
					out.close();
					return null;
				}
				return acceptNew(in, out, setTimeout, root, serverPW);
			}
			writeInt(out, ClientConnect.S_CONECT);
			byte[]       salt     = new byte[64];
			byte[]       saltHalf = new byte[32];
			SecureRandom rnd;
			try {
				rnd = SecureRandom.getInstanceStrong();
			} catch (NoSuchAlgorithmException e) {
				System.err.println("[Connect.ServerConnect]: WARN: no strong secure random found! (fall back to SecureRandom for salt)");
				rnd = new SecureRandom();
			}
			rnd.nextBytes(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			readArr(in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			rnd.nextBytes(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			readArr(in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			String user = readString(in);
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
				readInt(cin, ClientConnect.FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, mc);
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
		
		private static Connection acceptNew(InputStream in, OutputStream out, IntConsumer setTimeout, RootUser root, char[] serverPW)
				throws IOException {
			writeInt(out, ClientConnect.S_NEW);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			root.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			readArr(in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			root.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			readArr(in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			CipherInputStream  tcin  = User.decrypt(serverPW, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream tcout = User.encrypt(serverPW, new IgnoreCloseOutputStream(out), salt, initVec);
			char[]             pw;
			try {
				readInt(tcin, ClientConnect.C_CONECT);
				writeInt(tcout, ClientConnect.S_CONECT);
				root.fillRandom(saltHalf);
				tcout.write(saltHalf);
				System.arraycopy(saltHalf, 0, salt, 0, 32);
				readArr(tcin, saltHalf);
				System.arraycopy(saltHalf, 0, salt, 32, 32);
				byte[] pwUTF8 = new byte[readInt(tcin)];
				readArr(tcin, pwUTF8);
				CharBuffer buf = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pwUTF8));
				for (int i = 0; i < pwUTF8.length; i++) { pwUTF8[i] = 0; }
				pw = new char[buf.limit()];
				buf.get(pw);
				for (int i = 0; i < pw.length; i++) { buf.put(i, '\0'); }
				root.fillRandom(initVecHalf);
				tcout.write(initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 0, 8);
				readArr(tcin, initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			} finally {
				tcin.close();
				tcout.close();
			}
			CipherInputStream  cin  = User.decrypt(pw, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream cout = User.encrypt(pw, new IgnoreCloseOutputStream(out), salt, initVec);
			try {
				writeInt(cout, ClientConnect.SUB_NEW);
				readInt(cin, ClientConnect.SUB_NEW);
				String name = readString(cin);
				User   usr  = root.add(name, pw);
				writeInt(cout, ClientConnect.FS_CONECT);
				readInt(cin, ClientConnect.FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, User.startModCnt());
			} catch (Throwable t) {
				cin.close();
				cout.close();
				throw t;
			}
		}
		
	}
	
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
		
		
		public static Connection connectNew(InetAddress addr, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(addr, DEFAULT_PORT), usr, serverPW);
		}
		
		public static Connection connectNew(InetAddress addr, int port, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(addr, port), usr, serverPW);
		}
		
		public static Connection connectNew(String host, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(host, DEFAULT_PORT), usr, serverPW);
		}
		
		public static Connection connectNew(String host, int port, User usr, char[] serverPW) throws IOException {
			return connectNew(new Socket(host, port), usr, serverPW);
		}
		
		public static Connection connectNew(Socket sok, User usr, char[] serverPW) throws IOException {
			return connectNew(sok.getInputStream(), sok.getOutputStream(), to -> {
				try {
					sok.setSoTimeout(to);
				} catch (SocketException e) {
					throw new IOError(e);
				}
			}, usr, serverPW);
		}
		
		public static Connection connectNew(InputStream in, OutputStream out, IntConsumer setTimeout, User usr, char[] serverPW) throws IOException {
			if (serverPW == null) {
				throw new NullPointerException("serverPW is null");
			}
			writeInt(out, C_NEW);
			readInt(in, S_NEW);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			readArr(in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			usr.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			readArr(in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			usr.fillRandom(initVecHalf);
			out.write(initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 8, 8);
			CipherInputStream  tcin  = User.decrypt(serverPW, new IgnoreCloseInputStream(in), salt, initVec);
			CipherOutputStream tcout = User.encrypt(serverPW, new IgnoreCloseOutputStream(out), salt, initVec);
			try {
				for (int i = 0; i < serverPW.length; i++) { serverPW[i] = '\0'; }
				writeInt(tcout, C_CONECT);
				readInt(tcin, S_CONECT);
				readArr(tcin, saltHalf);
				System.arraycopy(saltHalf, 0, salt, 0, 32);
				usr.fillRandom(saltHalf);
				tcout.write(saltHalf);
				System.arraycopy(saltHalf, 0, salt, 32, 32);
				ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(usr.pw()));
				int        len = buf.limit();
				writeInt(tcout, len);
				if (buf.hasArray()) {
					byte[] arr = buf.array();
					tcout.write(arr, buf.arrayOffset(), buf.limit());
					for (int i = 0; i < buf.limit(); i++) { buf.put(i, (byte) 0); }
				} else {
					byte[] tmp = new byte[buf.limit()];
					buf.get(tmp);
					tcout.write(tmp, 0, buf.limit());
					for (int i = 0; i < tmp.length; i++) { tmp[i] = 0; buf.put(i, (byte) 0); }
				}
				readArr(tcin, initVecHalf);
				System.arraycopy(initVecHalf, 0, initVec, 0, 8);
				usr.fillRandom(initVecHalf);
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
				readInt(cin, SUB_NEW);
				writeInt(cout, SUB_NEW);
				writeString(cout, usr.name());
				readInt(cin, FS_CONECT);
				writeInt(cout, FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, mc);
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
		
		public static Connection connect(InetAddress addr, User usr) throws IOException {
			return connect(new Socket(addr, DEFAULT_PORT), usr);
		}
		
		public static Connection connect(InetAddress addr, int port, User usr) throws IOException {
			return connect(new Socket(addr, port), usr);
		}
		
		public static Connection connect(String host, User usr) throws IOException {
			return connect(new Socket(host, DEFAULT_PORT), usr);
		}
		
		public static Connection connect(String host, int port, User usr) throws IOException {
			return connect(new Socket(host, port), usr);
		}
		
		public static Connection connect(Socket sok, User usr) throws IOException {
			return connect(sok.getInputStream(), sok.getOutputStream(), to -> {
				try {
					sok.setSoTimeout(to);
				} catch (SocketException e) {
					throw new IOError(e);
				}
			}, usr);
		}
		
		public static Connection connect(InputStream in, OutputStream out, IntConsumer setTimeout, User usr) throws IOException {
			writeInt(out, C_CONECT);
			readInt(in, S_CONECT);
			byte[] salt     = new byte[64];
			byte[] saltHalf = new byte[32];
			readArr(in, saltHalf);
			System.arraycopy(saltHalf, 0, salt, 0, 32);
			usr.fillRandom(saltHalf);
			out.write(saltHalf);
			System.arraycopy(saltHalf, 0, salt, 32, 32);
			byte[] initVec     = new byte[16];
			byte[] initVecHalf = new byte[8];
			readArr(in, initVecHalf);
			System.arraycopy(initVecHalf, 0, initVec, 0, 8);
			usr.fillRandom(initVecHalf);
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
				readInt(cin, FS_CONECT);
				writeInt(cout, FC_CONECT);
				return new Connection(usr, cin, cout, setTimeout, mc);
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
	
	public void setTimeout(int timeout) {
		if (setTimeout == null) {
			throw new UnsupportedOperationException("no timeout supported");
		}
		setTimeout.accept(timeout);
	}
	
	/**
	 * this is the same as {@link #blocked(int, Executable, Executable) blocked(0,
	 * exec, null)}
	 * 
	 * @param exec
	 * 
	 * @throws IOException
	 */
	public <T extends Throwable> void blocked(Executable<T> exec) throws T {
		blocked(0, exec, null);
	}
	
	public static final Executable<IOException> NOP = () -> {};
	
	/**
	 * blocks this connection to execute the given {@link Executable}
	 * <p>
	 * note that this method only guarantees that no other thread invokes code with
	 * the {@link #blocked(int, Executable, int)} method.<br>
	 * it is still possible to use the read/write methods without being blocked by
	 * this method
	 * <p>
	 * also note that this method will fail if no timeout is supported
	 * <p>
	 * if no timeout was reached the timeoutHandler is
	 * ignored (for example if timeout is {@code 0},
	 * <code>null</code> can safely be passed)
	 * 
	 * @param timeout        the timeout for this connection during (and after) the
	 *                       execution
	 * @param exec           the code to be executed while this connection is
	 *                       blocked
	 * @param timeoutHandler the executable to be executed after the timeout was
	 *                       reached if no timeout was reached the timeoutHandler is
	 *                       ignored (for example if timeout is {@code 0},
	 *                       <code>null</code> can safely be passed)
	 * 						
	 * @throws IOException
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
	
	public int modCnt() {
		return modCnt;
	}
	
	public int readInt() throws IOException {
		return readInt(in);
	}
	
	public int readByte() throws IOException {
		return readByte(in);
	}
	
	public int readByte(int a, int b) throws IOException {
		return readByte(in, a, b);
	}
	
	public long readInt0() throws IOException {
		return readInt0(in);
	}
	
	public String readString() throws IOException {
		return readString(in);
	}
	
	public long readLong() throws IOException {
		return readLong(in);
	}
	
	public void readInt(int val) throws IOException {
		readInt(in, val);
	}
	
	public int readInt(int a, int b) throws IOException {
		return readInt(in, a, b);
	}
	
	public int readInt(int... vals) throws IOException {
		int reat = readInt(in);
		for (int val : vals) {
			if (reat == val) {
				return val;
			}
		}
		throw new InputMismatchException("unexpected value: " + reat + " expected: " + Arrays.toString(vals));
	}
	
	
	public int readInt(IntFunction<String> msg, int a, int b) throws IOException {
		int reat = readInt(in);
		if (a == reat || b == reat) {
			return reat;
		}
		throw new InputMismatchException(msg.apply(reat));
	}
	
	public int readInt(IntFunction<String> msg, int... vals) throws IOException {
		int reat = readInt(in);
		for (int val : vals) {
			if (reat == val) {
				return val;
			}
		}
		throw new InputMismatchException(msg.apply(reat));
	}
	
	public void readArr(byte[] arr) throws IOException {
		readArr(in, arr);
	}
	
	public void writeInt(int val) throws IOException {
		writeInt(out, val);
	}
	
	public void writeLong(long val) throws IOException {
		writeLong(out, val);
	}
	
	public void writeByte(int val) throws IOException {
		if ((val & 0xFF) != val) {
			throw new IllegalArgumentException("the given value is outside of the byte bounds: 0x" + Integer.toHexString(val));
		}
		out.write(val);
	}
	
	public void writeString(String str) throws IOException {
		writeString(out, str);
	}
	
	public void writeArr(byte[] arr) throws IOException {
		out.write(arr);
	}
	
	public void writeArr(byte[] arr, int off, int len) throws IOException {
		out.write(arr, off, len);
	}
	
	@Override
	public void close() throws IOException {
		closed = true;
		try { // invoke both methods even if in.close fails
			in.close();
		} finally {
			out.close();
		}
	}
	
	public boolean closed() {
		return closed;
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
		out.write(new byte[] { (byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24), (byte) (value >>> 32),
				(byte) (value >>> 40), (byte) (value >>> 48), (byte) (value >>> 56) });
	}
	
	private static int readByte(InputStream in) throws IOException {
		int val = in.read();
		if (val == -1) {
			throw new EOFException();
		}
		return val;
	}
	
	private static int readByte(InputStream in, int a, int b) throws IOException {
		int val = in.read();
		if (val == -1) {
			throw new EOFException();
		}
		if (val == a || val == b) {
			return val;
		}
		throw new IOException("expected 0x" + Integer.toHexString(a) + " or 0x" + Integer.toHexString(b) + " but got 0x" + Integer.toHexString(val));
	}
	
	private static long readLong(InputStream in) throws IOException {
		byte[] arr = new byte[8];
		readArr(in, arr);
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
	
	private static int readInt(InputStream in) throws IOException {
		byte[] arr = new byte[4];
		readArr(in, arr);
		int val = 0xFF & arr[0];
		val |= (0xFF & arr[1]) << 8;
		val |= (0xFF & arr[2]) << 16;
		val |= (0xFF & arr[3]) << 24;
		return val;
	}
	
	private static long readInt0(InputStream in) throws IOException {
		int val = in.read();
		if (val == -1) {
			return -1L;
		}
		byte[] arr = new byte[3];
		readArr(in, arr);
		val |= (0xFF & arr[0]) << 8;
		val |= (0xFF & arr[1]) << 16;
		val |= (0xFF & arr[2]) << 24;
		return val;
	}
	
	private static void readArr(InputStream in, byte[] arr) throws IOException {
		int i = 0;
		while (i < arr.length) {
			int r = in.read(arr, i, arr.length - i);
			if (r == -1) {
				throw new EOFException("unexpected EOF, expected to read " + arr.length + " bytes (" + (arr.length - i) + " bytes left)");
			}
			i += r;
		}
	}
	
	private static String readString(InputStream in) throws IOException {
		int    len     = readInt(in);
		byte[] userArr = new byte[len];
		readArr(in, userArr);
		return new String(userArr, StandardCharsets.UTF_8);
	}
	
	private static void readInt(InputStream in, int value) throws IOException {
		int reat = readInt(in);
		if (reat != value) {
			throw new InputMismatchException("expected 0x" + Integer.toHexString(value) + " but got 0x" + Integer.toHexString(reat));
		}
	}
	
	private static int readInt(InputStream in, int valueA, int valueB) throws IOException {
		int reat = readInt(in);
		if (reat != valueA && reat != valueB) {
			throw new InputMismatchException(
					"expected 0x" + Integer.toHexString(valueA) + " or 0x" + Integer.toHexString(valueB) + " but got 0x" + Integer.toHexString(reat));
		}
		return reat;
	}
	
}
