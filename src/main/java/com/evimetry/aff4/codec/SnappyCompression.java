/*
  This file is part of AFF4 Java.
  
  Copyright (c) 2017 Schatz Forensic Pty Ltd

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

import org.xerial.snappy.Snappy;

import com.evimetry.aff4.AFF4Lexicon;

/**
 * Snappy codec
 */
public class SnappyCompression implements CompressionCodec {

	/**
	 * The chunk size of the stream we are decompression.
	 */
	private final int chunkSize;

	/**
	 * Create a new Snappy decompression codec.
	 * 
	 * @param chunkSize The chunk size the regions should be.
	 * @throws IllegalArgumentException If the chunk size is negative or zero.
	 */
	public SnappyCompression(int chunkSize) {
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("Chunksize must be larger than 0");
		}
		this.chunkSize = chunkSize;
	}

	@Override
	public ByteBuffer decompress(ByteBuffer source) throws IOException {
		if(!source.isDirect()){
			ByteBuffer newSrc = ByteBuffer.allocateDirect(source.remaining()).order(ByteOrder.LITTLE_ENDIAN);
			newSrc.put(source);
			source = newSrc;
		}
		ByteBuffer destination = ByteBuffer.allocateDirect(chunkSize).order(ByteOrder.LITTLE_ENDIAN);
		Snappy.uncompress(source, destination);
		destination.position(0);
		return destination;
	}

	@Override
	public String getResourceID() {
		return AFF4Lexicon.SnappyCompression.getValue();
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
		SnappyCompression other = (SnappyCompression) obj;
		if (chunkSize != other.chunkSize)
			return false;
		return true;
	}

}
