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
package com.evimetry.aff4.struct;

import java.nio.ByteBuffer;

/**
 * Basic structure for an Image Stream index point.
 */
public class ImageStreamPoint implements Comparable<ImageStreamPoint> {

	/**
	 * The bevvy offset
	 */
	private long offset;
	/**
	 * The length of the chunk.
	 */
	private int length;

	/**
	 * Create a new Image Point from the information in the ByteBuffer.
	 * 
	 * @param buffer The buffer to read the values from. The position of this buffer will be updated.
	 * @return A new ImageStreamPoint instance, or NULL if there is not enough left in the buffer to create an instance.
	 */
	public static ImageStreamPoint create(ByteBuffer buffer) {
		if (buffer.remaining() >= getSize()) {
			ImageStreamPoint point = new ImageStreamPoint();
			point.offset = buffer.getLong();
			point.length = buffer.getInt();
			return point;
		}
		return null;
	}

	/**
	 * Get the offset into the bevvy that this entry represents.
	 * 
	 * @return The offset into the bevvy that this entry represents.
	 */
	public long getOffset() {
		return offset;
	}
	
	/**
	 * Get the length of the raw chunk.
	 * 
	 * @return The length.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Get the size of the struct.
	 * 
	 * @return The size of the struct in bytes.
	 */
	public static int getSize() {
		return 12;
	}

	@Override
	public String toString() {
		return String.format("[0x%08x:0x%08x]", offset, length);
	}

	@Override
	public int compareTo(ImageStreamPoint o) {
		if (offset < o.offset) {
			return -1;
		} else if (offset > o.offset) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + (int) (offset ^ (offset >>> 32));
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
		ImageStreamPoint other = (ImageStreamPoint) obj;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}

}
