//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import java.util.HashMap;
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

import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.connect.Connection.ClientConnect;
import de.hechler.patrick.games.squareconqerer.connect.Connection.ServerAccept;
import de.hechler.patrick.games.squareconqerer.world.RootWorld;

/**
 * this class represents a user with a password
 * <p>
 * this class is {@link Closeable}.<br>
 * when a User is {@link #close() closed}, its password is cleared and the most operations will result in a {@link NullPointerException}
 * <p>
 * this class is {@link Comparable}.<br>
 * when two Users are compared, their name is compared.
 * 
 * @author Patrick Hechler
 */
public sealed class User implements Closeable, Comparable<User> {
	
	private static final String CMP_DIFF_USERS_WITH_SAME_NAME = Messages.getString("User.diff-same");           //$NON-NLS-1$
	private static final String NO_STRONG_SECURE_RANDOM_FOUND = Messages.getString("User.no-strong-rnd");       //$NON-NLS-1$
	private static final String THIS_USER_HAS_BEEN_MODIFIED   = Messages.getString("User.modified");            //$NON-NLS-1$
	private static final String THIS_METHOD_IS_INTERN         = Messages.getString("User.intern");              //$NON-NLS-1$
	private static final String THERE_ARE_ALREADY_OTHER_USERS = Messages.getString("User.already-loaded");      //$NON-NLS-1$
	private static final String CHANGE_PW_NOT_MY_USR          = Messages.getString("User.chang-pw-not-my-usr"); //$NON-NLS-1$
	private static final String USER_NOT_FOUND                = Messages.getString("User.unknown-usr");         //$NON-NLS-1$
	private static final String DUPLICATE_NAME                = Messages.getString("User.dup-name");            //$NON-NLS-1$
	private static final String MAX_USER_AMOUNT_REACHED       = Messages.getString("User.max-usr-cnt");         //$NON-NLS-1$
	private static final String NO_NEW_USERS_ALLOWED          = Messages.getString("User.no-new-usr");          //$NON-NLS-1$
	private static final String NEGATIVE_MAX_USERS            = Messages.getString("User.negative-max-cnt");    //$NON-NLS-1$
	
	private volatile int     modCnt;
	private volatile Secret0 s;
	
	private User(Secret0 s) {
		this.s = s;
	}
	
	/**
	 * this is an intern method which can only be called by the {@link ServerAccept} class
	 * 
	 * @return if this method does not fails <code>0</code>
	 * @throws IllegalCallerException if this method is called from a class other than {@link ServerAccept}
	 */
	public static int startModCnt() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != Connection.ServerAccept.class) {
			throw new IllegalCallerException(THIS_METHOD_IS_INTERN);
		}
		return 0;
	}
	
	/**
	 * this is an intern method, calling it from any class other than {@link ClientConnect} will result in an {@link IllegalCallerException}
	 * 
	 * @return the password array of this user
	 * 
	 * @throws IllegalCallerException if the caller is not valid
	 */
	public synchronized char[] pw() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != Connection.ClientConnect.class) {
			throw new IllegalCallerException(THIS_METHOD_IS_INTERN);
		}
		return this.s._pw;
	}
	
	/**
	 * this is an intern method which can only be called from {@link Connection}, its inner classes and the {@link RootWorld}
	 * 
	 * @return the current modify count
	 * @throws IllegalCallerException if the caller is not valid
	 */
	public synchronized int modifyCount() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != Connection.ServerAccept.class && caller != Connection.OneWayAccept.class && caller != RootWorld.class
			&& caller != Connection.ClientConnect.class && caller != Connection.class) {
			throw new IllegalCallerException(THIS_METHOD_IS_INTERN);
		}
		return this.modCnt;
	}
	
	/**
	 * checks that the current modify count has the same value than the given <code>cnt</code>
	 * 
	 * @param cnt the expected modify count
	 * @throws IllegalStateException if the current modify count has not the expected value
	 */
	public synchronized void checkModCnt(int cnt) throws IllegalStateException {
		if (this.modCnt != cnt) throw new IllegalStateException(THIS_USER_HAS_BEEN_MODIFIED);
	}
	
	/**
	 * returns the name of this user
	 * 
	 * @return the name of this user
	 */
	public String name() { return this.s.name; }
	
	/**
	 * change the password of this user to the new password
	 * 
	 * @param pw the new password of this user
	 */
	public synchronized void changePassword(char[] pw) {
		Secret0 os   = this.s;
		String  name = os.name;
		char[]  opw  = os._pw;
		for (int i = 0; i < opw.length; i++) { opw[i] = '\0'; }
		this.modCnt++;
		this.s = new Secret0(name, pw);
	}
	
	/**
	 * returns a user with the given name<br>
	 * if the name is equal to the current name of this user nothing is done and this user is returned<br>
	 * otherwise a new user is created and returned and this user is {@link #close() closed}
	 * <p>
	 * if <code>name</code> is equal to {@link RootUser#ROOT_NAME}, the returned user will be a {@link RootUser}.<br>
	 * otherwise a {@link User} will be returned
	 * 
	 * @param name the new name of this user
	 * @return the user with the given name
	 */
	public synchronized User changeName(String name) {
		if (name.equals(this.s.name)) return this;
		Secret0 s0 = new Secret0(name, this.s._pw);
		if (RootUser.ROOT_NAME.equals(name)) {
			this.s = null;
			close();
			return new RootUser(s0);
		} else if (this instanceof RootUser) {
			this.s = null;
			close();
			return new User(s0);
		} else {
			this.s = s0;
			return this;
		}
	}
	
	/**
	 * returns a new {@link RootUser}, which has a clone of this users password as password.
	 * <p>
	 * note that after this call, either the newly created root user or this user should be {@link #close() closed}
	 * 
	 * @return a new {@link RootUser}, which has a clone of this users password as password.
	 */
	public synchronized RootUser rootClone() {
		Secret0 s0 = new Secret0(RootUser.ROOT_NAME, this.s._pw.clone());
		return new RootUser(s0);
	}
	
	/**
	 * this method will create a new root user with the password of this user and then {@link #close() close} this user
	 * <p>
	 * this is not the same as {@link #changeName(String)}, because even if this user is already root, it will be replaced by a new root user
	 * 
	 * @return a new {@link RootUser} object
	 */
	public synchronized RootUser makeRoot() {
		Secret0 s0 = new Secret0(RootUser.ROOT_NAME, this.s._pw);
		this.s = null;
		this.modCnt++;
		close();
		return new RootUser(s0);
	}
	
	/**
	 * creates a new decrypt-stream, which uses the given password, salt and initVec to decrypt and the uses the stream to get its encrypted data
	 * 
	 * @param pw      the password of the cipher
	 * @param in      the input stream with encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
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
			throw new AssertionError(e.toString(), e);
		}
	}
	
	/**
	 * creates a new encrypt-stream, which uses the given password, salt and initVec to encrypt and the uses the stream to send the encrypted data
	 * 
	 * @param pw      the password of the cipher
	 * @param out     the output-stream, which will accept the encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
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
			throw new AssertionError(e.toString(), e);
		}
	}
	
	/**
	 * creates a new decrypt-stream using the given values and the user password
	 * 
	 * @param in      the input stream with encrypted data
	 * @param salt    the used salt
	 * @param initVec the init vector
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
	 * @return the newly created encrypt-stream
	 */
	public synchronized CipherOutputStream encrypt(OutputStream out, byte[] salt, byte[] initVec) {
		return encrypt(this.s._pw, out, salt, initVec);
	}
	
	private static final SecureRandom RND;
	
	static {
		SecureRandom r;
		try {
			r = SecureRandom.getInstanceStrong();
		} catch (@SuppressWarnings("unused") NoSuchAlgorithmException e) {
			System.err.println(NO_STRONG_SECURE_RANDOM_FOUND);
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
	 * returns the hash code from the object class
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
	/**
	 * this method returns only <code>true</code>, if the given instance is the same instance as this instance (<code>this == obj</code>)
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		return this == obj;
	}
	
	/**
	 * compares the {@link #name() name} of this user with the {@link #name() name} of the other user
	 * <p>
	 * {@inheritDoc}
	 * 
	 * @return a value equal to <code>{@link #name()}.{@link String#compareTo(String) compareTo}(o.{@link #name()})</code>
	 */
	@Override
	public final int compareTo(User o) {
		int cmp = this.s.name.compareTo(o.s.name);
		// users are from different root: different user but same name
		if (cmp == 0 && this != o) throw new AssertionError(CMP_DIFF_USERS_WITH_SAME_NAME);
		return cmp;
	}
	
	/**
	 * closes this user.<br>
	 * the most operations will result in an {@link NullPointerException}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void close() {
		char[] pw;
		if (this.s == null) return;
		pw     = this.s._pw;
		this.s = null;
		for (int i = 0; i < pw.length; i++) {
			pw[i] = '\0';
		}
	}
	
	/**
	 * creates a new user with the given name and password
	 * <p>
	 * if the given name is equal to {@link RootUser#ROOT_NAME}, a {@link RootUser} is created
	 * 
	 * @param name the name of the new user
	 * @param pw   the password
	 * @return the newly created user
	 */
	public static User create(String name, char[] pw) {
		if (RootUser.ROOT_NAME.equals(name)) return RootUser.create(pw);
		User usr = new User(new Secret0(name, pw));
		Runtime.getRuntime().addShutdownHook(new Thread(usr::close));
		return usr;
	}
	
	private static char[] empty;
	
	/**
	 * creates a new user with the given name and empty password<br>
	 * the newly created user will have an empty string as password
	 * <p>
	 * if the given name is equal to {@link RootUser#ROOT_NAME}, a {@link RootUser} is created
	 * 
	 * @param name the name of the new user
	 * @return the newly created user
	 */
	public static User nopw(String name) {
		if (empty == null) {
			empty = new char[0];
		}
		Secret0 s = new Secret0(name, empty);
		if (RootUser.ROOT_NAME.equals(name)) {
			return new RootUser(s);
		} else {
			return new User(s);
		}
	}
	
	/**
	 * this class extends the {@link User} class.<br>
	 * additionally to the normal {@link User} functionality, a {@link RootUser} can have sub users
	 * <ul>
	 * <li>a root user can create sub users.</li>
	 * <li>a root user itself can not be a sub user.</li>
	 * <li>the creation of new sub users can be limited/forbidden
	 * <ul>
	 * <li>with {@link #maxUsers(int)}/{@link #maxUsers()} the maximum amount of sub users can be set/get</li>
	 * <li>with {@link #allowNewUsers(boolean)} it can be forbidden/allowed to create new sub users</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @author Patrick Hechler
	 */
	public static final class RootUser extends User {
		
		/**
		 * the {@link #name() name} of all {@link RootUser} instances has this value<br>
		 * also there can never be a <code>{@link User non}-{@link RootUser}</code> instance with this {@link #name() name}
		 */
		public static final String ROOT_NAME = "root"; //$NON-NLS-1$
		
		private volatile int               maxUsers   = Integer.MAX_VALUE;
		private volatile Map<String, User> otherUsers = new HashMap<>();
		private volatile boolean           noNew;
		
		private RootUser(Secret0 s) {
			super(s);
			if (s._pw != null) {
				Runtime.getRuntime().addShutdownHook(new Thread(this::close));
			}
		}
		
		/**
		 * create a new root user with the given password
		 * 
		 * @param pw the password of the root user
		 * @return the newly created root user with the given password
		 */
		public static RootUser create(char[] pw) {
			return new RootUser(new Secret0(ROOT_NAME, pw));
		}
		
		/**
		 * create a new root user with an empty password<br>
		 * the password of the new root user will be an empty string
		 * 
		 * @return the newly created root user
		 */
		public static RootUser nopw() {
			if (User.empty == null) {
				User.empty = new char[0];
			}
			return new RootUser(new Secret0(ROOT_NAME, User.empty));
		}
		
		/**
		 * closes this root user and all its sub users
		 * <p>
		 * {@inheridDoc}
		 */
		@Override
		public synchronized void close() {
			super.close();
			if (this.otherUsers == null) { return; }
			this.otherUsers.values().forEach(User::close);
			this.otherUsers.clear();
			this.otherUsers = null;
		}
		
		/**
		 * limits the creation of sub users
		 * <p>
		 * note that if there are already more sub users, only the creation of new sub users will be forbidden, but no existing user will not be removed
		 * 
		 * @param maxUsers the maximum amount of sub users to be allowed
		 */
		public synchronized void maxUsers(int maxUsers) {
			if (maxUsers < 0) throw new IllegalArgumentException(NEGATIVE_MAX_USERS);
			this.maxUsers = maxUsers;
		}
		
		/**
		 * returns the maximum amount of sub users
		 * <p>
		 * note that there may be more sub users, if there where already more when the maximum was set
		 * 
		 * @return the maximum amount of sub users
		 */
		public synchronized int maxUsers() {
			return this.maxUsers;
		}
		
		/**
		 * allow or forbid the creation of new sub users
		 * 
		 * @param allow <code>true</code> if ne wsub users are allowed to be created and <code>false</code> if not
		 */
		public synchronized void allowNewUsers(boolean allow) {
			this.noNew = !allow;
		}
		
		/**
		 * returns the user with the given name or <code>null</code>
		 * <p>
		 * if <code>user</code> is {@link RootUser#ROOT_NAME}, <code>this</code> will be returned
		 * 
		 * @param user the username of the user to return
		 * @return the user with the given name
		 */
		public synchronized User get(String user) {
			User usr = this.otherUsers.get(user);
			if (usr != null) {
				return usr;
			} else if (RootUser.ROOT_NAME.equals(user)) {
				return this;
			} else {
				return null;
			}
		}
		
		/**
		 * create a new sub user
		 * 
		 * @param user the username of the user to create
		 * @param pw   the password of the new user
		 * @return the newly created user
		 */
		public synchronized User add(String user, char[] pw) {
			if (this.noNew) throw new IllegalStateException(NO_NEW_USERS_ALLOWED);
			Map<String, User> ou = this.otherUsers;
			if (this.maxUsers - 1 <= ou.size()) throw new IllegalStateException(MAX_USER_AMOUNT_REACHED);
			User usr = ou.get(user);
			if (usr != null || RootUser.ROOT_NAME.equals(user)) throw new IllegalArgumentException(DUPLICATE_NAME);
			usr = new User(new Secret0(user, pw));
			ou.put(user, usr);
			return usr;
		}
		
		/**
		 * creates a new user with an empty string as password
		 * 
		 * @param user the username of the user to create
		 * @return the newly created user
		 */
		public User addNopw(String user) {
			if (User.empty == null) {
				User.empty = new char[0];
			}
			return add(user, empty);
		}
		
		/**
		 * removes and {@link User#close() closes} the given sub user
		 * 
		 * @param remove the sub user to remove
		 */
		public synchronized void remove(User remove) {
			synchronized (remove) {
				Map<String, User> ot = this.otherUsers;
				if (ot.remove(remove.name(), remove)) throw new AssertionError(USER_NOT_FOUND);
				remove.close();
			}
		}
		
		/**
		 * changes the password of the given user
		 * <p>
		 * this method is like {@link User#changePassword(char[])}, with the additional check that the given user belongs to this root<br>
		 * it is valid to invoke this method with this root user as <code>usr</code> argument
		 * 
		 * @param usr   the user which should get a new password
		 * @param newPW the new password of the user
		 */
		public synchronized void changePW(User usr, char[] newPW) {
			if (get(usr.s.name) != usr) throw new AssertionError(CHANGE_PW_NOT_MY_USR);
			usr.changePassword(newPW);
		}
		
		private static final int RU_SAVE  = 0x4024C07F;
		private static final int FIN_SAVE = 0xF440E5E3;
		
		/**
		 * saves all sub users (with all their passwords) of this root user to the connection
		 * 
		 * @param conn the connection on which the data should be send
		 * @throws IOException if an IO error occurs
		 */
		public void save(Connection conn) throws IOException {
			conn.writeInt(RU_SAVE);
			Map<String, User> ou = this.otherUsers;
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
		
		/**
		 * loads all sub users (with their password) from the connection
		 * <p>
		 * this method fails, if this root user already has sub users
		 * 
		 * @param conn the connection which stores the sub users
		 * @throws IOException if an IO error occurs
		 */
		public synchronized void load(Connection conn) throws IOException {
			Map<String, User> ou = this.otherUsers;
			if (!ou.isEmpty()) throw new IllegalStateException(THERE_ARE_ALREADY_OTHER_USERS);
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
		
		/**
		 * returns a map containing all users which belong to this root user<br>
		 * the map contains all sub users of this root and this root itself
		 * <p>
		 * the maps uses the {@link User#name() names} as keys
		 * 
		 * @return a map containing all users which belong to this root user
		 */
		public synchronized Map<String, User> users() {
			HashMap<String, User> res = new HashMap<>(this.otherUsers.size() + 1);
			res.putAll(this.otherUsers);
			res.put(ROOT_NAME, this);
			return res;
		}
		
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
			final int prime  = 31;
			int       result = 1;
			result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Secret [user="); //$NON-NLS-1$
			builder.append(this.name);
			builder.append("]"); //$NON-NLS-1$
			return builder.toString();
		}
		
	}
	
}
