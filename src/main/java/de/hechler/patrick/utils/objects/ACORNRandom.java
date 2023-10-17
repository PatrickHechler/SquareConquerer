package de.hechler.patrick.utils.objects;

import java.lang.StackWalker.Option;
import java.util.UUID;
import java.util.random.RandomGenerator;

import de.hechler.patrick.utils.objects.ACORNRandom;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.CompleteWorld;

/**
 * this PRNG uses the ACORN Algorithm
 * <br>
 * see http://acorn.wikramaratna.org/concept.html
 *
 * @author Patrick Hechler
 */
public class ACORNRandom implements RandomGenerator {
	
	/**
	 * the amount of 128 bit entries when creating instances with the {@link #ACORNRandom() no-arg constructor}
	 */
	public static final int DEFAULT_128_ENTRY_COUNT = 32;
	/**
	 * the amount of 64 bit entries when creating instances with the {@link #ACORNRandom() no-arg constructor}<br>
	 * in other words the length of the state array
	 */
	public static final int DEFAULT_ARR_LEN         = DEFAULT_128_ENTRY_COUNT * 2;
	
	private static final long HIGH_MASK = 0x00FFFFFFFFFFFFFFL; // 2^56-1 (interpreted as 2^120-1)
	
	private final long[] state;
	
	private ACORNRandom(long[] seed) {
		this.state     = seed;
		this.state[0] |= 1L;
		for (int i = 1; i < this.state.length; i += 2) {
			this.state[i] &= HIGH_MASK;
		}
	}
	
	/**
	 * create a new {@link ACORNRandom} instance with the given seed
	 * <p>
	 * if the seeds length is not even, an {@link IllegalArgumentException} will be thrown<br>
	 * if {@code allowSmallSeed} is <code>true</code> and the seeds length is below <code>4</code> an {@link IllegalArgumentException} will be thrown<br>
	 * if {@code allowSmallSeed} is <code>false</code> and the seeds length is below <code>22</code> an {@link IllegalArgumentException} will be thrown<br>
	 * 
	 * @param seed           the initial state of the instance
	 * @param allowSmallSeed if also small seeds are valid
	 * 
	 * @throws IllegalArgumentException if the seed is invalid
	 */
	public ACORNRandom(long[] seed, boolean allowSmallSeed) throws IllegalArgumentException {
		this(seed.clone());
		if ((seed.length & 1) != 0) {
			throw new IllegalArgumentException("the seed must have an even length");
		}
		if (allowSmallSeed) {
			if (seed.length < 4) {
				throw new IllegalArgumentException("the seed must have at least 4 entries");
			}
		} else if (seed.length < 22) {
			throw new IllegalArgumentException("the seed should have at least 22 entries");
		}
	}
	
	/**
	 * create a new {@link ACORNRandom} instance with a random seed
	 * <p>
	 * the newly created instance will have a state with {@value #DEFAULT_128_ENTRY_COUNT} 128 bit values
	 * (the array length will be {@value #DEFAULT_ARR_LEN})
	 */
	public ACORNRandom() {
		this(generateSeed());
	}
	
	private static long[] generateSeed() {
		long[] arr = new long[DEFAULT_ARR_LEN];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = User.randomLong();
		}
		return arr;
	}
	
	/**
	 * throws an IllegalCallerException if called from an other class than {@link CompleteWorld}
	 * <p>
	 * returns the current state.<br>
	 * pass this array to the {@link #ACORNRandom(long[], boolean)} constructor to get an copy of the current instance
	 * 
	 * @return the current state if called from {@link CompleteWorld}
	 * 
	 * @throws IllegalCallerException if called from an other class than {@link CompleteWorld}
	 */
	public long[] getCurrentState() throws IllegalCallerException {
		Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if (caller != CompleteWorld.class) {
			throw new IllegalCallerException("illegal caller: " + caller.getName());
		}
		return this.state;
	}
	
	/**
	 * return a new random UUID
	 * 
	 * @return a new random UUID
	 */
	public synchronized UUID nextUUID() {
		return new UUID(nextLong(), nextLong());
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized long nextLong() {
		long low  = this.state[0];
		long high = this.state[1];
		for (int i = 2; i < this.state.length; i += 2) {
			if ((low & this.state[i] & Long.MIN_VALUE) != 0) {
				high++;
			}
			low               += this.state[i];
			high              += this.state[i + 1];
			high              &= HIGH_MASK;
			this.state[i]      = low;
			this.state[i + 1]  = high;
		}
		return (low >> 56) | (high << 8);
	}
	
	/** {@inheritDoc} */
	@Override
	public void nextBytes(byte[] bytes) {
		nextBytes(bytes, 0, bytes.length);
	}
	
	/**
	 * fill the given array with random values in the given range
	 * 
	 * @param bytes the array
	 * @param off   the start index
	 * @param len   the length of the range to be filled with random values
	 */
	public synchronized void nextBytes(byte[] bytes, int off, int len) {
		if (len < 0 || off < 0 || bytes.length - off < len) {
			throw new IndexOutOfBoundsException("bytes.len=" + bytes.length + " off=" + off + " len=" + len);
		}
		for (; len >= 8; len -= 8, off += 8) {
			long val = nextLong();
			bytes[off]     = (byte) val;
			bytes[off + 1] = (byte) (val >> 8);
			bytes[off + 2] = (byte) (val >> 16);
			bytes[off + 3] = (byte) (val >> 24);
			bytes[off + 4] = (byte) (val >> 32);
			bytes[off + 5] = (byte) (val >> 40);
			bytes[off + 6] = (byte) (val >> 48);
			bytes[off + 7] = (byte) (val >> 56);
		}
		if (len > 0) {
			long val = nextLong();
			do {
				bytes[off]   = (byte) val;
				val        >>= 8;
				off++;
			} while (--len > 0);
		}
	}
	
}
