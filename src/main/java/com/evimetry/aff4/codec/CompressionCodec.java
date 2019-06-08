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

import com.evimetry.aff4.AFF4Lexicon;

public interface CompressionCodec {

	/**
	 * Get an instance of the compression codec based in the resource identification.
	 * 
	 * @param resource The resource identifier
	 * @param chunkSize The chunk size to initialise with.
	 * @return A compression codec
	 * @throws UnsupportedOperationException If the resource is not available.
	 */
	public static CompressionCodec getCodec(String resource, int chunkSize) {
		return getCodec(AFF4Lexicon.forValue(resource), chunkSize);
	}

	/**
	 * Get an instance of the compression codec based in the resource identification.
	 * 
	 * @param resource The resource identifier
	 * @param chunkSize The chunk size to initialise with.
	 * @return A compression codec
	 * @throws UnsupportedOperationException If the resource is not available, or the resource is not a compression
	 *             type.
	 */
	public static CompressionCodec getCodec(AFF4Lexicon resource, int chunkSize) {
		if (resource != null) {
			switch (resource) {
			case SnappyCompression:
				return new SnappyCompression(chunkSize);
			case LZ4Compression:
				return new LZ4Compression(chunkSize, true);
			case DeflateCompression:
				return new DeflateCompression(chunkSize);
			case NoCompression:
				return new NullCompression(chunkSize);
			case ZlibCompression:
				return new ZlibCompression(chunkSize);
			default:
				// Fall through.
			}
		}
		throw new UnsupportedOperationException("Compression resource is not available in this implementation");
	}

	/**
	 * Decompress the given buffer
	 * <p>
	 * Note: The position of the source buffer will be unchanged after this call.
	 * 
	 * @param source The source buffer to decompress
	 * @return A buffer with the decompressed version
	 * @throws IOException If decompression fails.
	 */
	public ByteBuffer decompress(ByteBuffer source) throws IOException;

	/**
	 * Get the resource ID of this Compression Codec
	 * 
	 * @return The resource ID of this Compression Codec.
	 */
	public String getResourceID();
	
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

}
