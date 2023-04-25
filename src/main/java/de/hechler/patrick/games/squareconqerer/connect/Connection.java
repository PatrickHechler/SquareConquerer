package de.hechler.patrick.games.squareconqerer.connect;

import static de.hechler.patrick.games.squareconqerer.Settings.threadBuilder;

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

public class Connection implements Closeable, WrongInputHandler {
	
	private static final IntFunction<String> STRICT_POS_ERROR     = v -> "expected some strictly positive number, but got: " + v;
	private static final IntFunction<String> NON_STRICT_POS_ERROR = v -> "expected some positive number (or zero), but got: " + v;
	
	/**
	 * this {@link WrongInputHandler} just always throws an {@link StreamCorruptedException}
	 */
	public static final WrongInputHandler DEFAULT_WRONG_INPUT = new WrongInputHandler() {
		
		@Override
		public void wrongInputEOF(int expectedTotalLen, int missingLen) throws EOFException {
			throw new EOFException("invalid input! got: EOF but expected " + missingLen + " more bytes for something " + expectedTotalLen + " bytes big");
		}
		
		public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
			throw new EOFException("got: " + read + " after I wrote " + wrote + " but expected " + expectedRead);
		}
		
		@Override
		public void wrongInputInt(int read, IntFunction<String> msgGen) throws StreamCorruptedException {
			throw new StreamCorruptedException(msgGen.apply(read));
		}
		
		@Override
		public void wrongInputInt(int read, int[] expected) throws StreamCorruptedException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected on of: " + Arrays.toString(expected));
		}
		
		@Override
		public void wrongInputInt(int read, int expected, int expected2) throws StreamCorruptedException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected: " + expected + " or " + expected2);
		}
		
		@Override
		public void wrongInputInt(int read, int expected) throws StreamCorruptedException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected: " + expected);
		}
		
		@Override
		public void wrongInputByte(int read, IntFunction<String> msgGen) throws IOException {
			throw new StreamCorruptedException(msgGen.apply(read));
		}
		
		@Override
		public void wrongInputByte(int read, int[] expected) throws IOException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected on of: " + Arrays.toString(expected));
		}
		
		@Override
		public void wrongInputByte(int read, int expected, int expected2) throws IOException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected: " + expected + " or " + expected2);
		}
		
		@Override
		public void wrongInputByte(int read, int expected) throws IOException {
			throw new StreamCorruptedException("invalid input! got: " + read + " but expected: " + expected);
		}
		
	};
	
	public static final int DEFAULT_PORT = 21226;
	
	public static final int CON_LOG_OUT = 0x50DDC7F1;
	
	public final User                  usr;
	private final InputStream          in;
	private final OutputStream         out;
	private final IntConsumer          setTimeout;
	private final int                  modCnt;
	private volatile boolean           closed;
	private volatile WrongInputHandler wih;
	
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
	
	public static Connection createUnsecure(User usr, InputStream in) {
		return createUnsecure(usr, in, InvalidOutputStream.INSTANCE, null);
	}
	
	public static Connection createUnsecure(User usr, OutputStream out) {
		return createUnsecure(usr, InvalidInputStream.INSTANCE, out, null);
	}
	
	public static Connection createUnsecure(User usr, InputStream in, OutputStream out) {
		return createUnsecure(usr, in, out, null);
	}
	
	public static Connection createUnsecure(User usr, InputStream in, OutputStream out, IntConsumer setTimeout) {
		if (usr == null || in == null || out == null) { throw new NullPointerException("usr=" + usr + " in=" + in + " out=" + out); }
		return new Connection(usr, in, out, setTimeout, usr.modifyCount());
	}
	
	public static class OneWayAccept {
		
		private OneWayAccept() {}
		
		public static Connection acceptReadOnly(InputStream in, User usr) throws IOException {
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
		
		public static void accept(ServerSocket ss, RootWorld rw, ThrowBiConsumer<Connection, Socket, IOException> logConnect, Map<User, Connection> connects,
			char[] serverPW) throws IOException {
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
								World      uw   = UserWorld.of(rw, conn.usr, conn.modCnt);
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
					} catch (@SuppressWarnings("unused") SocketTimeoutException timeout) {
						if (Thread.interrupted()) { throw new ClosedByInterruptException(); }
					}
				}
			} catch (ClosedByInterruptException | EOFException e) {
				err = e;
			} catch (IOException e) {
				if (!Thread.currentThread().isInterrupted()) { throw e; }
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
		
		private static Connection accept(InputStream in, OutputStream out, IntConsumer setTimeout, RootUser root, char[] serverPW) throws IOException {
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
		
		private static Connection acceptNew(InputStream in, OutputStream out, IntConsumer setTimeout, RootUser root, char[] serverPW) throws IOException {
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
				for (int i = 0; i < pwUTF8.length; i++) { pwUTF8[i] = 0; }
				pw = new char[buf.limit()];
				buf.get(pw);
				for (int i = 0; i < pw.length; i++) { buf.put(i, '\0'); }
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
				User   usr  = root.add(name, pw);
				writeInt(cout, ClientConnect.FS_CONECT);
				readInt(DEFAULT_WRONG_INPUT, cin, ClientConnect.FC_CONECT);
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
			if (serverPW == null) { throw new NullPointerException("serverPW is null"); }
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
				for (int i = 0; i < serverPW.length; i++) { serverPW[i] = '\0'; }
				writeInt(tcout, C_CONECT);
				readInt(DEFAULT_WRONG_INPUT, tcin, S_CONECT);
				readArr(DEFAULT_WRONG_INPUT, tcin, saltHalf);
				System.arraycopy(saltHalf, 0, salt, 0, 32);
				User.fillRandom(saltHalf);
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
	
	public synchronized void replaceWongInput(WrongInputHandler oldWIH, WrongInputHandler newWIH) {
		if (this.wih != oldWIH) throw new AssertionError("I have a different WrongInputHandler!");
		this.wih = newWIH;
	}
	
	/**
	 * returns the current effective {@link WrongInputHandler}, to which this connection delegates to
	 * 
	 * @return the current effective {@link WrongInputHandler}, to which this connection delegates to
	 */
	public WrongInputHandler wrongInputHandler() {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) return DEFAULT_WRONG_INPUT;
		else return cwih;
	}
	
	@Override
	public void wrongInputEOF(int expectedTotalLen, int missingLen) throws StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputEOF(expectedTotalLen, missingLen);
		else cwih.wrongInputEOF(expectedTotalLen, missingLen);
	}
	
	@Override
	public void wrongInputByte(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
		else cwih.wrongInputByte(read, expected);
	}
	
	@Override
	public void wrongInputByte(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputByte(read, expected, expected2);
		else cwih.wrongInputByte(read, expected, expected2);
	}
	
	@Override
	public void wrongInputByte(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputByte(read, expected);
		else cwih.wrongInputByte(read, expected);
	}
	
	@Override
	public void wrongInputByte(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputByte(read, msgGen);
		else cwih.wrongInputByte(read, msgGen);
	}
	
	@Override
	public void wrongInputWRInt(int read, int wrote, int expectedRead) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputWRInt(read, wrote, expectedRead);
		else cwih.wrongInputWRInt(read, wrote, expectedRead);
	}
	
	@Override
	public void wrongInputInt(int read, int expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
		else cwih.wrongInputInt(read, expected);
	}
	
	@Override
	public void wrongInputInt(int read, int expected, int expected2) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputInt(read, expected, expected2);
		else cwih.wrongInputInt(read, expected, expected2);
	}
	
	@Override
	public void wrongInputInt(int read, int[] expected) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputInt(read, expected);
		else cwih.wrongInputInt(read, expected);
	}
	
	@Override
	public void wrongInputInt(int read, IntFunction<String> msgGen) throws IOException, StreamCorruptedException, EOFException {
		WrongInputHandler cwih = this.wih;
		if (cwih == null) DEFAULT_WRONG_INPUT.wrongInputInt(read, msgGen);
		else cwih.wrongInputInt(read, msgGen);
	}
	
	public void setTimeout(int timeout) {
		if (this.setTimeout == null) { throw new UnsupportedOperationException("no timeout supported"); }
		this.setTimeout.accept(timeout);
	}
	
	/**
	 * this is the same as {@link #blocked(DEFAULT_WRONG_INPUT, int, Executable, Executable) blocked(0, exec, null)}
	 * 
	 * @param exec the executable to execute
	 * 
	 * @throws T if the executable throws it
	 */
	public <T extends Throwable> void blocked(Executable<T> exec) throws T {
		blocked(0, exec, null);
	}
	
	public static final Executable<IOException> NOP = () -> {
	};
	
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
	 * @param timeout        the timeout for this connection during (and after) the execution
	 * @param exec           the code to be executed while this connection is blocked
	 * @param timeoutHandler the executable to be executed after the timeout was reached if no timeout was reached the timeoutHandler is ignored (for example if
	 *                       timeout is {@code 0}, <code>null</code> can safely be passed)
	 * @param exec           the executable to execute
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
	
	public boolean isBlocking() {
		return Thread.holdsLock(this);
	}
	
	public int modCnt() {
		return this.modCnt;
	}
	
	public void writeReadInt(int write, int read) throws IOException {
		while (true) {
			writeInt(write);
			int val = readInt();
			if (val == read) return;
			wrongInputWRInt(val, write, read);
		}
	}
	
	public int readInt() throws IOException {
		return readInt(this, this.in);
	}
	
	/**
	 * reads a non-strict positive number (zero is allowed)
	 * 
	 * @return the positive number which was read
	 * @throws IOException if an IO-error occurs
	 */
	public int readPos() throws IOException {
		while (true) {
			int val = readInt(this, this.in);
			if (val >= 0) return val;
			wrongInputInt(val, NON_STRICT_POS_ERROR);
		}
	}
	
	/**
	 * reads a strictly positive number (zero is not allowed)
	 * 
	 * @return the strict positive number which was read
	 * @throws IOException if an IO-error occurs
	 */
	public int readStrictPos() throws IOException {
		while (true) {
			int val = readInt(this, this.in);
			if (val > 0) return val;
			wrongInputInt(val, STRICT_POS_ERROR);
		}
	}
	
	public int readByte() throws IOException {
		return readByte(this, this.in);
	}
	
	public int readByte(int a, int b) throws IOException {
		return readByte(this, this.in, a, b);
	}
	
	public long readInt0() throws IOException {
		return readInt0(this, this.in);
	}
	
	public String readString() throws IOException {
		return readString(this, this.in);
	}
	
	public long readLong() throws IOException {
		return readLong(this, this.in);
	}
	
	public void readInt(int val) throws IOException {
		readInt(this, this.in, val);
	}
	
	public int readInt(int a, int b) throws IOException {
		return readInt(this, this.in, a, b);
	}
	
	public int readInt(int... vals) throws IOException {
		while (true) {
			int reat = readInt(this, this.in);
			for (int val : vals) {
				if (reat == val) { return val; }
			}
			wrongInputInt(reat, vals);
		}
	}
	
	public int readInt(IntFunction<String> msg, int a, int b) throws IOException {
		while (true) {
			int reat = readInt(this, this.in);
			if (a == reat || b == reat) return reat;
			wrongInputInt(reat, msg);
		}
	}
	
	public int readInt(IntFunction<String> msg, int... vals) throws IOException {
		while (true) {
			int reat = readInt(this, this.in);
			for (int val : vals) {
				if (reat == val) { return val; }
			}
			wrongInputInt(reat, msg);
		}
	}
	
	public void readArr(byte[] arr) throws IOException {
		readArr(this, this.in, arr);
	}
	
	public void writeInt(int val) throws IOException {
		writeInt(this.out, val);
	}
	
	public void writeLong(long val) throws IOException {
		writeLong(this.out, val);
	}
	
	public void writeByte(int val) throws IOException {
		if ((val & 0xFF) != val) { throw new IllegalArgumentException("the given value is outside of the byte bounds: 0x" + Integer.toHexString(val)); }
		this.out.write(val);
	}
	
	public void writeString(String str) throws IOException {
		writeString(this.out, str);
	}
	
	public void writeArr(byte[] arr) throws IOException {
		this.out.write(arr);
	}
	
	public void writeArr(byte[] arr, int off, int len) throws IOException {
		this.out.write(arr, off, len);
	}
	
	@Override
	public void close() throws IOException {
		this.closed = true;
		try { // invoke both methods even if in.close fails
			this.in.close();
		} finally {
			this.out.close();
		}
	}
	
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
		while (true) {
			int val = in.read();
			if (val != -1) return val;
			wih.wrongInputEOF(1, 1);
		}
	}
	
	private static int readByte(WrongInputHandler wih, InputStream in, int a, int b) throws IOException {
		while (true) {
			int val = in.read();
			if (val == a || val == b) return val;
			if (val == -1) wih.wrongInputEOF(1, 1);
			wih.wrongInputByte(val, a, b);
		}
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
	
	private static long readInt0(WrongInputHandler wih, InputStream in) throws IOException {
		int val = in.read();
		if (val == -1) { return -1L; }
		byte[] arr = new byte[3];
		readArr(wih, in, arr);
		val |= (0xFF & arr[0]) << 8;
		val |= (0xFF & arr[1]) << 16;
		val |= (0xFF & arr[2]) << 24;
		return val;
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
		int    len     = readInt(wih, in);
		byte[] userArr = new byte[len];
		readArr(wih, in, userArr);
		return new String(userArr, StandardCharsets.UTF_8);
	}
	
	private static void readInt(WrongInputHandler wih, InputStream in, int value) throws IOException {
		while (true) {
			int reat = readInt(wih, in);
			if (reat == value) return;
			wih.wrongInputInt(reat, value);
		}
	}
	
	private static int readInt(WrongInputHandler wih, InputStream in, int valueA, int valueB) throws IOException {
		while (true) {
			int reat = readInt(wih, in);
			if (reat == valueA || reat == valueB) return reat;
			wih.wrongInputInt(reat, valueA, valueB);
		}
	}
	
}
