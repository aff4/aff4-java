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
package com.evimetry.aff4.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.evimetry.aff4.AFF4Lexicon;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

/**
 * LZ4 codec
 */
public class LZ4Compression implements CompressionCodec {

	/**
	 * The chunk size of the stream we are decompression.
	 */
	private final int chunkSize;
	/**
	 * The decompression module.
	 */
	private final LZ4SafeDecompressor decompressor;

	/**
	 * Create a new LZ4 decompression codec.
	 * 
	 * @param chunkSize The chunk size the regions should be.
	 * @param useJNI TRUE to enable the use of JNI
	 * @throws IllegalArgumentException If the chunk size is negative or zero.
	 */
	public LZ4Compression(int chunkSize, boolean useJNI) {
		if(chunkSize <= 0) {
			throw new IllegalArgumentException("Chunksize must be larger than 0");
		}
		this.chunkSize = chunkSize;
		LZ4Factory lz4factory;
		if (useJNI) {
			// Note: This will attempt to load the JNI version, and it fails, then it will use the java unsafe
			// implementation.
			lz4factory = LZ4Factory.fastestInstance();
		} else {
			// Use the Java unsage implementation.
			lz4factory = LZ4Factory.fastestJavaInstance();
		}
		decompressor = lz4factory.safeDecompressor();
	}

	@Override
	public ByteBuffer decompress(ByteBuffer source) throws IOException {
		int p = source.position();
		byte[] destination = new byte[chunkSize];
		byte[] srcArray = null;
		if (source.hasArray()) {
			srcArray = source.array();
		} else {
			srcArray = new byte[source.remaining()];
			source.get(srcArray);
		}
		try {
			decompressor.decompress(srcArray, 0, srcArray.length, destination, 0, chunkSize);
			source.position(p);
			return ByteBuffer.wrap(destination).order(ByteOrder.LITTLE_ENDIAN);
		} catch (Throwable e) {
			if (!(e instanceof IOException)) {
				throw new IOException(e);
			}
			throw e;
		}
	}

	@Override
	public String getResourceID() {
		return AFF4Lexicon.LZ4Compression.getValue();
	}

	@Override
	public String toString(){
		return getResourceID();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkSize;
		result = prime * result + getResourceID().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LZ4Compression other = (LZ4Compression) obj;
		if (chunkSize != other.chunkSize)
			return false;
		return true;
	}
	

}
