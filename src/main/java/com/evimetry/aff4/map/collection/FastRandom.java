/*
  This file is part of AFF4 Java.

  AFF4 Java is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  AFF4 Java is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with AFF4 Java.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.evimetry.aff4.map.collection;

import java.util.Random;

/**
 * Fast random number generator for Int's and Long's. (uses the normal Java Random class for other types).
 * <p>
 * This PRNG implements a XORShift generator (very fast, medium quality), and is NOT crypto safe. <br>
 * See: http://www.jstatsoft.org/v08/i14/ for the original paper.
 */
public class FastRandom extends Random {

	/**
	 * Class serial number
	 */
	private static final long serialVersionUID = -831311257679036173L;
	/**
	 * The current value;
	 */
	private long x = 0;

	/**
	 * Create a new Fast Random number generator with a random starting value.
	 */
	public FastRandom() {
		x = new Random().nextLong();
	}

	/**
	 * Create a new Fast Random number generator with a supplied seed.
	 * 
	 * @param seed The starting seed
	 */
	public FastRandom(long seed) {
		x = seed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Random#nextLong()
	 */
	@Override
	public long nextLong() {
		/*
		 * The "magic" values of 21, 35 and 4 have been found to produce good results. With these values, the generator
		 * has a full period of (2^64)-1, and the resulting values pass Marsaglia's "Diehard battery" of statistical
		 * tests for randomness.
		 */
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Random#nextInt()
	 */
	@Override
	public int nextInt() {
		return (int) nextLong();
	}
}
