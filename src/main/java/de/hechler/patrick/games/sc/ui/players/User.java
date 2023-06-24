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
package de.hechler.patrick.games.sc.ui.players;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.connect.Connection.ClientConnect;
import de.hechler.patrick.games.sc.world.CompleteWorld;

public class User implements Closeable {
	
	private final User              parent;
	private final Map<String, User> childs;
	private volatile boolean        forbidNew;
	private volatile int            modCnt;
	private volatile Secret0        s;
	
	private User(Secret0 s) {
		this.parent = null;
		this.childs = new HashMap<>();
		this.s      = s;
	}
	
	private User(User owner, Secret0 s) {
		this.parent = owner;
		this.childs = null;
		this.s      = s;
	}
	
	public static User createUser(String name, char[] pw) {
		return new User(new Secret0(name, pw));
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
	
	public User rootClone() {
		Secret0 ms = this.s;
		return new User(new Secret0(ms.name, ms._pw));
	}
	
	public User get(String name) {
		Objects.requireNonNull(name, "name");
		@SuppressWarnings("resource")
		User u = this.parent == null ? this : this.parent;
		synchronized (u) {
			if (this.s.name.equals(name)) {
				return this;
			}
			if (this.childs == null) {
				throw new IllegalArgumentException("no user with name '" + name + "' found");
			}
			User usr = this.childs.get(name);
			if (usr == null) {
				throw new IllegalArgumentException("no user with name '" + name + "' found");
			}
			return usr;
		}
	}
	
	public User addUser(String name, char[] pw) {
		if (pw == null) {
			throw new NullPointerException("pw");
		}
		try {
			if (name == null) {
				throw new NullPointerException("name");
			}
			if (this.parent != null) {
				throw new IllegalStateException("only the root user can add users!");
			}
			synchronized (this) {
				if (this.forbidNew) {
					throw new IllegalStateException("new users are currently forbidden!");
				}
				this.checkNewUser(name);
				User result = new User(new Secret0(name, pw));
				this.childs.put(name, result);
				return result;
			}
		} catch (Throwable t) {
			for (int i = 0; i < pw.length; i++) pw[i] = '\0';
			throw t;
		}
	}
	
	public void removeUser(String name) {
		Objects.requireNonNull(name, "name");
		if (this.parent == null) {
			throw new IllegalStateException("only the root user can add users!");
		}
		synchronized (this) {
			User old = this.childs.remove(name);
			if (old == null) {
				throw new IllegalStateException("I have no direct subuser with the name " + name);
			}
		}
	}
	
	private void checkNewUser(String name) {
		if (this.s.name.equals(name)) {
			throw new IllegalArgumentException("the name '" + name + "' is already used");
		}
		for (User usr : this.childs.values()) {
			if (usr.s.name.equals(name)) {
				throw new IllegalArgumentException("the name '" + name + "' is already used");
			}
		}
	}
	
	public void changeName(String name) {
		if (this.parent == null) {
			synchronized (this) {
				this.s = new Secret0(name, this.s._pw);
			}
		} else {
			synchronized (this.parent) {
				this.parent.checkNewUser(name);
				this.parent.childs.remove(this.s.name);
				this.s = new Secret0(name, this.s._pw);
				this.parent.childs.put(name, this);
			}
		}
	}
	
	/**
	 * checks that the current modify count has the same value than the given <code>cnt</code>
	 * 
	 * @param cnt the expected modify count
	 * 
	 * @throws IllegalStateException if the current modify count has not the expected value
	 */
	public synchronized void checkModCnt(int cnt) throws IllegalStateException {
		if (this.modCnt != cnt) throw new IllegalStateException("the user was modified (" + this.s.name + ')');
	}
	
	/**
	 * this is an intern method which can only be called from {@link Connection}, its inner classes and the {@link CompleteWorld}
	 * 
	 * @return the current modify count
	 * 
	 * @throws IllegalCallerException if the caller is not valid
	 */
	public synchronized int modifyCount() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != Connection.ServerAccept.class && caller != Connection.OneWayAccept.class && caller != CompleteWorld.class
				&& caller != Connection.ClientConnect.class && caller != Connection.class) {
			throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
		}
		return this.modCnt;
	}
	
	/**
	 * returns the name of this user
	 * 
	 * @return the name of this user
	 */
	public String name() { return this.s.name; }
	
	/**
	 * this is an intern method, calling it from any class other than {@link ClientConnect} will result in an {@link IllegalCallerException}
	 * 
	 * @return the password array of this user
	 * 
	 * @throws IllegalCallerException if the caller is not valid
	 */
	public synchronized char[] _pw() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != Connection.ClientConnect.class) {
			throw new IllegalCallerException(String.format("illegal caller: %s/%s", caller.getModule(), caller.getName()));
		}
		return this.s._pw;
	}
	
	private static final SecureRandom RND;
	
	private static final char[] NO_PW = new char[0];
	
	static {
		SecureRandom r;
		try {
			r = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			System.err.println(String.format("no strong secure random found, use the default version (%s)", e));
			r = new SecureRandom();
		}
		RND = r;
	}
	
	/**
	 * fills the given bytes with random values
	 * 
	 * @param bytes the array to be filled with random values
	 */
	public static void fillRandom(byte[] bytes) {
		RND.nextBytes(bytes);
	}
	
	/**
	 * returns a random integer
	 * 
	 * @return a random integer
	 */
	public static int randomInt() {
		return RND.nextInt();
	}
	
	/**
	 * creates a new decrypt-stream, which uses the given password, salt and initVec to decrypt and the uses the stream to get its encrypted data
	 * 
	 * @param pw      the password of the cipher
	 * @param in      the input stream with encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
	 * 
	 * @return the newly created decrypt-stream
	 */
	public static CipherInputStream decrypt(char[] pw, InputStream in, byte[] salt, byte[] initVec) {
		try {
			PBEKeySpec      pbeKeySpec = new PBEKeySpec(pw, salt, 16384, 128);
			SecretKey       pbeKey     = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec); //$NON-NLS-1$
			SecretKeySpec   keySpec    = new SecretKeySpec(pbeKey.getEncoded(), "AES");                                   //$NON-NLS-1$
			Cipher          c0         = Cipher.getInstance("AES/CTR/NoPadding");                                         //$NON-NLS-1$
			IvParameterSpec param      = new IvParameterSpec(initVec);
			c0.init(Cipher.DECRYPT_MODE, keySpec, param);
			return new CipherInputStream(in, c0);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * creates a new encrypt-stream, which uses the given password, salt and initVec to encrypt and the uses the stream to send the encrypted data
	 * 
	 * @param pw      the password of the cipher
	 * @param out     the output-stream, which will accept the encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
	 * 
	 * @return the newly created encrypt-stream
	 */
	public static CipherOutputStream encrypt(char[] pw, OutputStream out, byte[] salt, byte[] initVec) {
		try {
			PBEKeySpec      pbeKeySpec = new PBEKeySpec(pw, salt, 16384, 128);
			SecretKey       pbeKey     = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec); //$NON-NLS-1$
			SecretKeySpec   keySpec    = new SecretKeySpec(pbeKey.getEncoded(), "AES");                                   //$NON-NLS-1$
			Cipher          c1         = Cipher.getInstance("AES/CTR/NoPadding");                                         //$NON-NLS-1$
			IvParameterSpec param      = new IvParameterSpec(initVec);
			c1.init(Cipher.ENCRYPT_MODE, keySpec, param);
			return new CipherOutputStream(out, c1);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * creates a new decrypt-stream using the given values and the user password
	 * 
	 * @param in      the input stream with encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
	 * 
	 * @return the newly created decrypt-stream
	 */
	public synchronized CipherInputStream decrypt(InputStream in, byte[] salt, byte[] initVec) {
		return decrypt(this.s._pw, in, salt, initVec);
	}
	
	/**
	 * creates a new encrypt-stream using the given values and the user password
	 * 
	 * @param out     the output-stream, which will accept the encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
	 * 
	 * @return the newly created encrypt-stream
	 */
	public synchronized CipherOutputStream encrypt(OutputStream out, byte[] salt, byte[] initVec) {
		return encrypt(this.s._pw, out, salt, initVec);
	}
	
	@Override
	public void close() {
		if (this.s == null) {
			return;
		}
		synchronized (this) {
			Secret0 s = this.s;
			if (s == null) {
				return;
			}
			for (int i = 0; i < s._pw.length; i++) s._pw[i] = '\0';
			this.s = null;
			this.modCnt++;
		}
	}
	
	@Override
	public String toString() {
		return name();
	}
	
	private static record Secret0(String name, char[] _pw) {
		
		private Secret0 {
			if (name == null || _pw == null) throw new NullPointerException("user or pw is null"); //$NON-NLS-1$
		}
		
		@SuppressWarnings({ "static-method", "unused" })
		public char[] _pw() {
			throw new UnsupportedOperationException("the password is not visible"); //$NON-NLS-1$
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
	}
	
	public static User nopw(String name) {
		return createUser(name, NO_PW);
	}
	
	public Map<String, User> users() {
		if (this.parent != null) {
			throw new IllegalStateException("only the root can return its users");
		}
		Map<String, User> c = new HashMap<>(this.childs);
		c.put(this.s.name, this);
		return Collections.unmodifiableMap(c);
	}
	
	public Map<String, User> subUsers() {
		if (this.parent != null) {
			throw new IllegalStateException("only the root can return its users");
		}
		return Collections.unmodifiableMap(this.childs);
	}
	
	public User addNopw(String name) {
		return addUser(name, NO_PW);
	}
	
	private static final int SEND_USR      = 0x2BA395E3;
	private static final int SEND_USR_SUB0 = 0x44B8BA5B;
	private static final int SEND_USR_SUB1 = 0xD05E8CAF;
	private static final int SEND_USR_FIN  = 0x8FD6FF7A;
	
	public void save(Connection conn) throws IOException {
		conn.writeInt(SEND_USR);
		conn.writeString(this.s.name);
		if (this.parent != null) {
			conn.writeInt(SEND_USR_SUB0);
			for (User u : this.childs.values()) {
				conn.writeInt(SEND_USR_SUB1);
				conn.writeInt(u.s._pw.length);
				ByteBuffer bb  = StandardCharsets.UTF_8.encode(CharBuffer.wrap(u.s._pw));
				int        len = bb.limit();
				if (bb.hasArray()) {
					byte[] arr = bb.array();
					int    off = bb.arrayOffset();
					conn.writeArr(arr, off, len);
					for (int i = 0; i < len; i++) arr[off + i] = 0;
				} else {
					for (int i = 0; i < len; i++) {
						conn.writeByte(bb.get(i) & 0xFF);
						bb.put(i, (byte) 0);
					}
				}
				u.save(conn);
			}
		}
		conn.writeInt(SEND_USR_FIN);
	}
	
	public void load(Connection conn) throws IOException {
		if (this.childs != null && !this.childs.isEmpty()) {
			throw new IllegalStateException("only an user with no sub/child users can load");
		}
		conn.readInt(SEND_USR);
		changeName(conn.readString());
		if (conn.readInt(SEND_USR_FIN, SEND_USR_SUB0) == SEND_USR_SUB0) {
			if (this.parent != null) {
				throw new IllegalStateException("I am no root, but recieved a root user");
			}
			while (conn.readInt(SEND_USR_SUB1, SEND_USR_FIN) == SEND_USR_SUB1) {
				char[] pw;
				byte[] data = new byte[conn.readPos()];
				conn.readArr(data);
				CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(data));
				for (int i = 0; i < data.length; i++) data[i] = 0;
				int len = cb.limit();
				if (cb.hasArray() && cb.arrayOffset() == 0 && len == cb.array().length) {
					pw = cb.array();
				} else {
					pw = new char[len];
					for (int i = 0; i < len; i++) {
						pw[i] = cb.get(i);
						cb.put(i, '\0');
					}
				}
				User sub = new User(this, new Secret0("", pw));
				sub.load(conn);
				User old = this.childs.put(sub.name(), sub);
				assert old == null;
			}
		} else if (this.parent == null) {
			throw new IllegalStateException("I am a root, but recieved a non root user");
		}
	}
	
	public void allowNewUsers(boolean b) {
		if (this.parent != null) {
			throw new IllegalStateException("I am no root user!");
		}
		this.forbidNew = !b;
	}
	
	@Override
	public int hashCode() {
		return this.s.name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
	
}
