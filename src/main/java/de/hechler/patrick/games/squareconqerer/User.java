package de.hechler.patrick.games.squareconqerer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.hechler.patrick.games.squareconqerer.world.RootWorld;
import de.hechler.patrick.games.squareconqerer.world.connect.Connection;

public sealed class User implements Closeable {
	
	private volatile int     modCnt;
	private volatile Secret0 s;
	
	private User(Secret0 s) {
		this.s = s;
	}
	
	public int modCnt() {
		return modCnt;
	}
	
	public synchronized void checkModCnt(int cnt) {
		if (modCnt != cnt) {
			throw new IllegalStateException("this user has been set to invalid (changed password/deleted/whatever)");
		}
	}
	
	public String name() { return s.name; }
	
	public synchronized RootUser rootClone() {
		Secret0 s0 = new Secret0(RootWorld.ROOT_NAME, s._pw.clone());
		return new RootUser(s0);
	}
	
	public static CipherInputStream decrypt(char[] pw, InputStream in, byte[] salt, byte[] initVec) {
		try {
			PBEKeySpec      pbeKeySpec = new PBEKeySpec(pw, salt, 16384, 128);
			SecretKey       pbeKey     = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
			SecretKeySpec   keySpec    = new SecretKeySpec(pbeKey.getEncoded(), "AES");
			Cipher          c0         = Cipher.getInstance("AES/CTR/NoPadding");
			IvParameterSpec param      = new IvParameterSpec(initVec);
			c0.init(Cipher.DECRYPT_MODE, keySpec, param);
			return new CipherInputStream(in, c0);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException e) {
			throw new AssertionError(e.toString(), e);
		}
	}
	
	public static CipherOutputStream encrypt(char[] pw, OutputStream out, byte[] salt, byte[] initVec) throws AssertionError {
		try {
			PBEKeySpec      pbeKeySpec = new PBEKeySpec(pw, salt, 16384, 128);
			SecretKey       pbeKey     = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
			SecretKeySpec   keySpec    = new SecretKeySpec(pbeKey.getEncoded(), "AES");
			Cipher          c1         = Cipher.getInstance("AES/CTR/NoPadding");
			IvParameterSpec param      = new IvParameterSpec(initVec);
			c1.init(Cipher.ENCRYPT_MODE, keySpec, param);
			return new CipherOutputStream(out, c1);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException e) {
			throw new AssertionError(e.toString(), e);
		}
	}
	
	public synchronized CipherInputStream decrypt(InputStream in, byte[] salt, byte[] initVec) {
		return decrypt(s._pw, in, salt, initVec);
	}
	
	public synchronized CipherOutputStream encrypt(OutputStream out, byte[] salt, byte[] initVec) {
		return encrypt(s._pw, out, salt, initVec);
	}
	
	private static final SecureRandom RND;
	
	static {
		SecureRandom r;
		try {
			r = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("[User]: WARN: no strong secure random found! (fall back to SecureRandom)");
			r = new SecureRandom();
		}
		RND = r;
	}
	
	public void fillRandom(byte[] bytes) {
		RND.nextBytes(bytes);
	}
	
	@Override
	public synchronized void close() {
		char[] pw;
		if (s == null) {
			return;
		}
		pw = s._pw;
		s  = null;
		for (int i = 0; i < pw.length; i++) {
			pw[i] = '\0';
		}
	}
	
	public static User create(String name, char[] pw) {
		return new User(new Secret0(name, pw));
	}
	
	public static final class RootUser extends User {
		
		private volatile Map<String, User> otherUsers = new HashMap<>();
		
		public RootUser(Secret0 s) {
			super(s);
			Runtime.getRuntime().addShutdownHook(new Thread(this::close));
		}
		
		public static RootUser create(char[] pw) {
			return new RootUser(new Secret0(RootWorld.ROOT_NAME, pw));
		}
		
		@Override
		public synchronized void close() {
			super.close();
			if (otherUsers == null) {
				return;
			}
			otherUsers.values().forEach(User::close);
			otherUsers.clear();
			otherUsers = null;
		}
		
		public synchronized User get(String user) {
			User usr = otherUsers.get(user);
			if (usr != null) {
				return usr;
			} else if (RootWorld.ROOT_NAME.equals(user)) {
				return this;
			} else {
				return null;
			}
		}
		
		public synchronized User add(String user, char[] pw) {
			Map<String, User> ou  = otherUsers;
			User              usr = ou.get(user);
			if (usr != null || RootWorld.ROOT_NAME.equals(user)) {
				throw new IllegalArgumentException("there is already an user with that name");
			}
			usr = new User(new Secret0(user, pw));
			ou.put(user, usr);
			return usr;
		}
		
		public synchronized void remove(User remove) {
			synchronized (remove) {
				Map<String, User> ot = otherUsers;
				if (ot.remove(remove.name(), remove)) {
					throw new AssertionError("I could not find the given user");
				}
				remove.modCnt++;
			}
		}
		
		public synchronized void changePW(User usr, char[] newPW) {
			synchronized (usr) {
				Map<String, User> ot = otherUsers;
				if (ot.get(usr.name()) != usr) {
					throw new AssertionError("I can only change the password of my users!");
				}
				Secret0 oldSecret = usr.s;
				char[]  oldPW     = oldSecret._pw;
				usr.s = new Secret0(oldSecret.name, newPW);
				usr.modCnt++;
				for (int i = 0; i < oldPW.length; i++) {
					oldPW[i] = '\0';
				}
			}
		}
		
		private static final int RU_SAVE  = 0x4024C07F;
		private static final int FIN_SAVE = 0xF440E5E3;
		
		public void save(Connection conn) throws IOException {
			conn.writeInt(RU_SAVE);
			Map<String, User> ou = otherUsers;
			conn.writeInt(ou.size());
			for (User usr : ou.values()) {
				conn.writeString(usr.s.name);
				ByteBuffer buf   = StandardCharsets.UTF_8.encode(CharBuffer.wrap(usr.s._pw));
				int        limit = buf.limit();
				byte[]     utf8  = null;
				try {
					if (buf.hasArray()) {
						utf8 = buf.array();
					} else {
						utf8 = new byte[limit];
						int pos = buf.position();
						buf.get(utf8);
						buf.position(pos);
					}
					conn.writeInt(limit);
					conn.writeArr(utf8, 0, limit);
				} finally {
					for (int i = 0; i < limit; i++) {
						buf.put((byte) 0);
						if (utf8 != null) {
							utf8[i] = 0;
						}
					}
				}
			}
			conn.writeInt(FIN_SAVE);
		}
		
		public synchronized void load(Connection conn) throws IOException {
			Map<String, User> ou = otherUsers;
			if (!ou.isEmpty()) {
				throw new IllegalStateException("there are already other users!");
			}
			conn.readInt(RU_SAVE);
			int remain = conn.readInt();
			for (; remain > 0; remain--) {
				String name    = conn.readString();
				int    pwLen   = conn.readInt();
				byte[] pwBytes = new byte[pwLen];
				conn.readArr(pwBytes);
				CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pwBytes));
				char[]     pw      = new char[decoded.limit()];
				decoded.get(pw);
				int cap = decoded.capacity();
				decoded.limit(cap);
				for (int i = 0; i < cap; i++) {
					decoded.put(i, '\0');
				}
				User usr = new User(new Secret0(name, pw));
				ou.put(name, usr);
			}
			conn.readInt(FIN_SAVE);
		}
		
		public synchronized List<String> names() {
			return new ArrayList<>(otherUsers.keySet());
		}
		
	}
	
	private static record Secret0(String name, char[] _pw) {
		
		private Secret0(String name, char[] _pw) {
			if (name == null || _pw == null) {
				throw new NullPointerException("user or pw is null");
			}
			this.name = name;
			this._pw  = _pw;
		}
		
		@SuppressWarnings({ "static-method", "unused" })
		public char[] _pw() {
			throw new UnsupportedOperationException("the password is not visible");
		}
		
		@Override
		public int hashCode() {
			final int prime  = 31;
			int       result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Secret [user=");
			builder.append(name);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
}
