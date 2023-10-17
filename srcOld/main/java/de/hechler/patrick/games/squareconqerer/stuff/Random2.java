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
package de.hechler.patrick.games.squareconqerer.stuff;

import java.util.concurrent.atomic.AtomicLong;
import de.hechler.patrick.utils.objects.ACORNRandom;


/**
 * this is a clone of {@link java.util.Random}. differences are that there is a {@code getCurrentSeed()} and some unneeded stuff is removed. Also this class is
 * {@code final}
 * <p>
 * An instance of this class is used to generate a stream of pseudorandom numbers; its period is only 2<sup>48</sup>. The class uses a 48-bit seed, which is modified
 * using a linear congruential formula. (See Donald E. Knuth, <cite>The Art of Computer Programming, Volume 2, Third edition: Seminumerical Algorithms</cite>,
 * Section 3.2.1.)
 * <p>
 * If two instances of {@code Random} are created with the same seed, and the same sequence of method calls is made for each, they will generate and return identical
 * sequences of numbers. In order to guarantee this property, particular algorithms are specified for the class {@code Random}. Java implementations must use all the
 * algorithms shown here for the class {@code Random}, for the sake of absolute portability of Java code. However, subclasses of class {@code Random} are permitted
 * to use other algorithms, so long as they adhere to the general contracts for all the methods.
 * <p>
 * The algorithms implemented by class {@code Random} use a {@code protected} utility method that on each invocation can supply up to 32 pseudorandomly generated
 * bits.
 * <p>
 * Many applications will find the method {@link Math#random} simpler to use.
 * <p>
 * Instances of {@code java.util.Random} are threadsafe. However, the concurrent use of the same {@code java.util.Random} instance across threads may encounter
 * contention and consequent poor performance. Consider instead using {@link java.util.concurrent.ThreadLocalRandom} in multithreaded designs.
 * <p>
 * Instances of {@code java.util.Random} are not cryptographically secure. Consider instead using {@link java.security.SecureRandom} to get a cryptographically
 * secure pseudo-random number generator for use by security-sensitive applications.
 *
 * @author Frank Yellin
 * 
 * @since 1.0
 * 
 * @see java.util.Random
 */
public final class ACORNRandom implements ACORNRandom, java.io.Serializable {
	
	/*
	 * RandomWrapper is not here Class used to wrap a {@link java.util.random.ACORNRandom} to {@link java.util.Random}.
	 */
	
	private static final long serialVersionUID = -9068865220583004496L;
	
	/**
	 * The internal state associated with this pseudorandom number generator. (The specs for the methods in this class describe the ongoing computation of this
	 * value.)
	 */
	private final AtomicLong seed;
	
	private static final long MULTIPLIER = 0x5DEECE66DL;
	private static final long ADDEND     = 0xBL;
	private static final long MASK       = (1L << 48) - 1;
	
	private static final double DOUBLE_UNIT = 0x1.0p-53;  // 1.0 / (1L << Double.PRECISION)
	private static final float  FLOAT_UNIT  = 0x1.0p-24f; // 1.0f / (1 << Float.PRECISION)
	
	/**
	 * Creates a new random number generator. This constructor sets the seed of the random number generator to a value very likely to be distinct from any other
	 * invocation of this constructor.
	 */
	public ACORNRandom() {
		this(seedUniquifier() ^ System.nanoTime());
	}
	
	private static long seedUniquifier() {
		// L'Ecuyer, "Tables of Linear Congruential Generators of
		// Different Sizes and Good Lattice Structure", 1999
		for (;;) {
			long current = seedUniquifier.get();
			long next    = current * 1181783497276652981L;
			if (seedUniquifier.compareAndSet(current, next)) return next;
		}
	}
	
	private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
	
	/**
	 * Creates a new random number generator using a single {@code long} seed. The seed is the initial value of the internal state of the pseudorandom number
	 * generator which is maintained by method {@link #next}.
	 *
	 * @param seed the initial seed
	 */
	public ACORNRandom(long seed) {
		this.seed = new AtomicLong(seed);
	}
	
	/**
	 * returns the current seed of this {@link ACORNRandom}
	 * 
	 * @return the current seed of this {@link ACORNRandom}
	 */
	public long getCurrentSeed() {
		return this.seed.get();
	}
	
	/**
	 * Generates the next pseudorandom number. This method returns an {@code int} value such that, if the argument {@code bits} is between {@code 1} and {@code 32}
	 * (inclusive), then that many low-order bits of the returned value will be (approximately) independently chosen bit values, each of which is (approximately)
	 * equally likely to be {@code 0} or {@code 1}.
	 *
	 * <p>
	 * <b>apiNote</b> The other random-producing methods in this class are implemented in terms of this method, so subclasses can override just this method to
	 * provide a different source of pseudorandom numbers for the entire class.
	 * 
	 * <p>
	 * <b>implSpec</b> The implementation in this class atomically updates the seed to
	 * 
	 * <pre>{@code
	 * (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)
	 * }</pre>
	 * 
	 * and returns
	 * 
	 * <pre>{@code
	 * (int) (seed >>> (48 - bits))
	 * }.</pre>
	 * <p>
	 * This is a linear congruential pseudorandom number generator, as defined by D. H. Lehmer and described by Donald E. Knuth in <cite>The Art of Computer
	 * Programming, Volume 2, Third edition: Seminumerical Algorithms</cite>, section 3.2.1.
	 * 
	 * @param bits random bits
	 * 
	 * @return the next pseudorandom value from this random number generator's sequence
	 * 
	 * @since 1.1
	 */
	protected int next(int bits) {
		long       oldseed;
		long       nextseed;
		AtomicLong myseed = this.seed;
		do {
			oldseed  = myseed.get();
			nextseed = (oldseed * MULTIPLIER + ADDEND) & MASK;
		} while (!myseed.compareAndSet(oldseed, nextseed));
		return (int) (nextseed >>> (48 - bits));
	}
	
	/**
	 * Generates random bytes and places them into a user-supplied byte array. The number of random bytes produced is equal to the length of the byte array.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextBytes} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public void nextBytes(byte[] bytes) {
	 * 	for (int i = 0; i < bytes.length;) for (int rnd = nextInt(), n = Math.min(bytes.length - i, 4); n-- > 0; rnd >>= 8) bytes[i++] = (byte) rnd;
	 * }
	 * }</pre>
	 *
	 * @param bytes the byte array to fill with random bytes
	 * 
	 * @throws NullPointerException if the byte array is null
	 * 
	 * @since 1.1
	 */
	@Override
	public void nextBytes(byte[] bytes) {
		for (int i = 0, len = bytes.length; i < len;)
			for (int rnd = nextInt(), n = Math.min(len - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) bytes[i++] = (byte) rnd;
	}
	
	/**
	 * Returns the next pseudorandom, uniformly distributed {@code int} value from this random number generator's sequence. The general contract of {@code nextInt}
	 * is that one {@code int} value is pseudorandomly generated and returned. All 2<sup>32</sup> possible {@code int} values are produced with (approximately) equal
	 * probability.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextInt} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public int nextInt() {
	 * 	return next(32);
	 * }
	 * }</pre>
	 *
	 * @return the next pseudorandom, uniformly distributed {@code int} value from this random number generator's sequence
	 */
	@Override
	public int nextInt() {
		return next(32);
	}
	
	/**
	 * Returns a pseudorandom, uniformly distributed {@code int} value between 0 (inclusive) and the specified value (exclusive), drawn from this random number
	 * generator's sequence. The general contract of {@code nextInt} is that one {@code int} value in the specified range is pseudorandomly generated and returned.
	 * All {@code bound} possible {@code int} values are produced with (approximately) equal probability.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextInt(int bound)} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public int nextInt(int bound) {
	 * 	if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
	 *	
	 * 	if ((bound & -bound) == bound) // i.e., bound is a power of 2
	 * 		return (int) ((bound * (long) next(31)) >> 31);
	 *	
	 * 	int bits, val;
	 * 	do {
	 * 		bits = next(31);
	 * 		val = bits % bound;
	 * 	} while (bits - val + (bound - 1) < 0);
	 * 	return val;
	 * }
	 * }</pre>
	 * <p>
	 * The hedge "approximately" is used in the foregoing description only because the next method is only approximately an unbiased source of independently chosen
	 * bits. If it were a perfect source of randomly chosen bits, then the algorithm shown would choose {@code int} values from the stated range with perfect
	 * uniformity.
	 * <p>
	 * The algorithm is slightly tricky. It rejects values that would result in an uneven distribution (due to the fact that 2^31 is not divisible by n). The
	 * probability of a value being rejected depends on n. The worst case is n=2^30+1, for which the probability of a reject is 1/2, and the expected number of
	 * iterations before the loop terminates is 2.
	 * <p>
	 * The algorithm treats the case where n is a power of two specially: it returns the correct number of high-order bits from the underlying pseudo-random number
	 * generator. In the absence of special treatment, the correct number of <i>low-order</i> bits would be returned. Linear congruential pseudo-random number
	 * generators such as the one implemented by this class are known to have short periods in the sequence of values of their low-order bits. Thus, this special
	 * case greatly increases the length of the sequence of values returned by successive calls to this method if n is a small power of two.
	 * 
	 * @param bound the upper bound (exclusive). Must be positive.
	 * 
	 * @return the next pseudorandom, uniformly distributed {@code int} value between zero (inclusive) and {@code bound} (exclusive) from this random number
	 *         generator's sequence
	 * 
	 * @throws IllegalArgumentException if bound is not positive
	 * 
	 * @since 1.2
	 */
	@Override
	public int nextInt(int bound) {
		if (bound <= 0) throw new IllegalArgumentException("bad bound: " + bound); //$NON-NLS-1$
		int r = next(31);
		int m = bound - 1;
		if ((bound & m) == 0) // i.e., bound is a power of 2
			r = (int) ((bound * (long) r) >> 31);
		else { // reject over-represented candidates
			int u = r;
			r = u % bound;
			while (u - r + m < 0) {
				u = next(31);
				r = u % bound;
			}
		}
		return r;
	}
	
	/**
	 * Returns the next pseudorandom, uniformly distributed {@code long} value from this random number generator's sequence. The general contract of {@code nextLong}
	 * is that one {@code long} value is pseudorandomly generated and returned.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextLong} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public long nextLong() {
	 * 	return ((long) next(32) << 32) + next(32);
	 * }
	 * }</pre>
	 *
	 * Because class {@code Random} uses a seed with only 48 bits, this algorithm will not return all possible {@code long} values.
	 * 
	 * @return the next pseudorandom, uniformly distributed {@code long} value from this random number generator's sequence
	 */
	@Override
	public long nextLong() {
		// it's okay that the bottom word remains signed.
		return ((long) (next(32)) << 32) + next(32);
	}
	
	/**
	 * Returns the next pseudorandom, uniformly distributed {@code boolean} value from this random number generator's sequence. The general contract of
	 * {@code nextBoolean} is that one {@code boolean} value is pseudorandomly generated and returned. The values {@code true} and {@code false} are produced with
	 * (approximately) equal probability.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextBoolean} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public boolean nextBoolean() {
	 * 	return next(1) != 0;
	 * }
	 * }</pre>
	 *
	 * @return the next pseudorandom, uniformly distributed {@code boolean} value from this random number generator's sequence
	 * 
	 * @since 1.2
	 */
	@Override
	public boolean nextBoolean() {
		return next(1) != 0;
	}
	
	/**
	 * Returns the next pseudorandom, uniformly distributed {@code float} value between {@code 0.0} and {@code 1.0} from this random number generator's sequence.
	 * <p>
	 * The general contract of {@code nextFloat} is that one {@code float} value, chosen (approximately) uniformly from the range {@code 0.0f} (inclusive) to
	 * {@code 1.0f} (exclusive), is pseudorandomly generated and returned. All 2<sup>24</sup> possible {@code float} values of the form
	 * <i>m&nbsp;x&nbsp;</i>2<sup>-24</sup>, where <i>m</i> is a positive integer less than 2<sup>24</sup>, are produced with (approximately) equal probability.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextFloat} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public float nextFloat() {
	 * 	return next(24) / ((float) (1 << 24));
	 * }
	 * }</pre>
	 * <p>
	 * The hedge "approximately" is used in the foregoing description only because the next method is only approximately an unbiased source of independently chosen
	 * bits. If it were a perfect source of randomly chosen bits, then the algorithm shown would choose {@code float} values from the stated range with perfect
	 * uniformity.
	 * <p>
	 * [In early versions of Java, the result was incorrectly calculated as:
	 * 
	 * <pre> {@code
	 * return next(30) / ((float) (1 << 30));
	 * }</pre>
	 * 
	 * This might seem to be equivalent, if not better, but in fact it introduced a slight nonuniformity because of the bias in the rounding of floating-point
	 * numbers: it was slightly more likely that the low-order bit of the significand would be 0 than that it would be 1.]
	 *
	 * @return the next pseudorandom, uniformly distributed {@code float} value between {@code 0.0f} and {@code 1.0f} from this random number generator's sequence
	 */
	@Override
	public float nextFloat() {
		return next(Float.PRECISION) * FLOAT_UNIT;
	}
	
	/**
	 * Returns the next pseudorandom, uniformly distributed {@code double} value between {@code 0.0} and {@code 1.0} from this random number generator's sequence.
	 * <p>
	 * The general contract of {@code nextDouble} is that one {@code double} value, chosen (approximately) uniformly from the range {@code 0.0d} (inclusive) to
	 * {@code 1.0d} (exclusive), is pseudorandomly generated and returned.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextDouble} is implemented by class {@code Random} as if by:
	 * 
	 * <pre>{@code
	 * public double nextDouble() {
	 * 	return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	 * }
	 * }</pre>
	 * <p>
	 * The hedge "approximately" is used in the foregoing description only because the {@code next} method is only approximately an unbiased source of independently
	 * chosen bits. If it were a perfect source of randomly chosen bits, then the algorithm shown would choose {@code double} values from the stated range with
	 * perfect uniformity.
	 * <p>
	 * [In early versions of Java, the result was incorrectly calculated as:
	 * 
	 * <pre> {@code
	 * return (((long) next(27) << 27) + next(27)) / (double) (1L << 54);
	 * }</pre>
	 * 
	 * This might seem to be equivalent, if not better, but in fact it introduced a large nonuniformity because of the bias in the rounding of floating-point
	 * numbers: it was three times as likely that the low-order bit of the significand would be 0 than that it would be 1! This nonuniformity probably doesn't matter
	 * much in practice, but we strive for perfection.]
	 *
	 * @return the next pseudorandom, uniformly distributed {@code double} value between {@code 0.0} and {@code 1.0} from this random number generator's sequence
	 * 
	 * @see Math#random
	 */
	@Override
	public double nextDouble() {
		return (((long) (next(Double.PRECISION - 27)) << 27) + next(27)) * DOUBLE_UNIT;
	}
	
	private double  nextNextGaussian;
	private boolean haveNextNextGaussian = false;
	
	/**
	 * Returns the next pseudorandom, Gaussian ("normally") distributed {@code double} value with mean {@code 0.0} and standard deviation {@code 1.0} from this
	 * random number generator's sequence.
	 * <p>
	 * The general contract of {@code nextGaussian} is that one {@code double} value, chosen from (approximately) the usual normal distribution with mean {@code 0.0}
	 * and standard deviation {@code 1.0}, is pseudorandomly generated and returned.
	 *
	 * <p>
	 * <b>implSpec</b> The method {@code nextGaussian} is implemented by class {@code Random} as if by a threadsafe version of the following:
	 * 
	 * <pre>{@code
	 * private double nextNextGaussian;
	 * private boolean haveNextNextGaussian = false;
	 *
	 * public double nextGaussian() {
	 * 	if (haveNextNextGaussian) {
	 * 		haveNextNextGaussian = false;
	 * 		return nextNextGaussian;
	 * 	} else {
	 * 		double v1, v2, s;
	 * 		do {
	 * 			v1 = 2 * nextDouble() - 1; // between -1.0 and 1.0
	 * 			v2 = 2 * nextDouble() - 1; // between -1.0 and 1.0
	 * 			s = v1 * v1 + v2 * v2;
	 * 		} while (s >= 1 || s == 0);
	 * 		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
	 * 		nextNextGaussian = v2 * multiplier;
	 * 		haveNextNextGaussian = true;
	 * 		return v1 * multiplier;
	 * 	}
	 * }
	 * }</pre>
	 *
	 * This uses the <i>polar method</i> of G. E. P. Box, M. E. Muller, and G. Marsaglia, as described by Donald E. Knuth in <cite>The Art of Computer Programming,
	 * Volume 2, third edition: Seminumerical Algorithms</cite>, section 3.4.1, subsection C, algorithm P. Note that it generates two independent values at the cost
	 * of only one call to {@code StrictMath.log} and one call to {@code StrictMath.sqrt}.
	 * 
	 * @return the next pseudorandom, Gaussian ("normally") distributed {@code double} value with mean {@code 0.0} and standard deviation {@code 1.0} from this
	 *         random number generator's sequence
	 */
	@Override
	public synchronized double nextGaussian() {
		// See Knuth, TAOCP, Vol. 2, 3rd edition, Section 3.4.1 Algorithm C.
		if (this.haveNextNextGaussian) {
			this.haveNextNextGaussian = false;
			return this.nextNextGaussian;
		}
		double v1;
		double v2;
		double s;
		do {
			v1 = 2 * nextDouble() - 1; // between -1 and 1
			v2 = 2 * nextDouble() - 1; // between -1 and 1
			s  = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
		this.nextNextGaussian     = v2 * multiplier;
		this.haveNextNextGaussian = true;
		return v1 * multiplier;
	}
	
	
}
