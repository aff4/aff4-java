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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.evimetry.aff4.AFF4Lexicon;

/**
 * Zlib codec
 */
public class ZlibCompression implements CompressionCodec {

	/**
	 * The chunk size of the stream we are decompression.
	 */
	private final int chunkSize;
	/**
	 * Decompressor.
	 */
	private final Inflater decompressor = new Inflater(false);

	/**
	 * Create a new Zlib decompression codec.
	 * 
	 * @param chunkSize The chunk size the regions should be.
	 * @throws IllegalArgumentException If the chunk size is negative or zero.
	 */
	public ZlibCompression(int chunkSize) {
		if(chunkSize <= 0) {
			throw new IllegalArgumentException("Chunksize must be larger than 0");
		}
		this.chunkSize = chunkSize;
	}

	@Override
	public synchronized ByteBuffer decompress(ByteBuffer source) throws IOException {
		decompressor.reset();
		int p = source.position();
		byte[] destination = new byte[chunkSize];
		byte[] srcArray = null;
		if (source.hasArray()) {
			srcArray = source.array();
		} else {
			srcArray = new byte[source.remaining()];
			source.get(srcArray);
		}
		decompressor.setInput(srcArray);
		try {
			decompressor.inflate(destination);
			source.position(p);
			return ByteBuffer.wrap(destination).order(ByteOrder.LITTLE_ENDIAN);
		} catch (DataFormatException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String getResourceID() {
		return AFF4Lexicon.ZlibCompression.getValue();
	}

	@Override
	public String toString() {
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
		ZlibCompression other = (ZlibCompression) obj;
		if (chunkSize != other.chunkSize)
			return false;
		return true;
	}

}
