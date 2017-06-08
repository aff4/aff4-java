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
import java.nio.channels.SeekableByteChannel;

public class MapEntryPoint implements Comparable<MapEntryPoint> {

	/**
	 * The offset into the map
	 */
	private long offset;
	/**
	 * The length region.
	 */
	private long length;
	/**
	 * The offset into the stream which this entry pertains
	 */
	private long streamOffset;
	/**
	 * The id of the stream.
	 */
	private int streamID;
	/**
	 * The actual stream.
	 */
	private SeekableByteChannel stream;

	/**
	 * Create a new Map Entry Point
	 * 
	 * @param offset he offset into the map
	 * @param length The length region.
	 * @param streamOffset The offset into the stream which this entry pertains
	 * @param streamID The id of the stream.
	 * @param stream The actual stream.
	 * @return A new map point.
	 */
	public static MapEntryPoint create(long offset, long length, long streamOffset, int streamID,
			SeekableByteChannel stream) {
		MapEntryPoint point = new MapEntryPoint();
		point.offset = offset;
		point.length = length;
		point.streamOffset = streamOffset;
		point.streamID = streamID;
		point.stream = stream;
		return point;
	}

	/**
	 * Create a new Map Entry Point from the information in the ByteBuffer.
	 * 
	 * @param buffer The buffer to read the values from. The position of this buffer will be updated.
	 * @return A new ImageStreamPoint instance, or NULL if there is not enough left in the buffer to create an instance.
	 */
	public static MapEntryPoint create(ByteBuffer buffer) {
		if (buffer.remaining() >= getSize()) {
			MapEntryPoint point = new MapEntryPoint();
			point.offset = buffer.getLong();
			point.length = buffer.getLong();
			point.streamOffset = buffer.getLong();
			point.streamID = buffer.getInt();
			return point;
		}
		return null;
	}

	/**
	 * Get the stream for this entry
	 * 
	 * @return The stream to read from this entry
	 */
	public SeekableByteChannel getStream() {
		return stream;
	}

	/**
	 * Set the stream for this entry
	 * 
	 * @param stream The stream to set for this entry
	 */
	public void setStream(SeekableByteChannel stream) {
		this.stream = stream;
	}

	/**
	 * Get the map offset for this entry
	 * 
	 * @return The map offset for this entry.
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Get the length of this entry
	 * 
	 * @return The length of the region for this entry
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Get the offset into the stream which this map entry represents.
	 * 
	 * @return The offset into the stream which the entry represents.
	 */
	public long getStreamOffset() {
		return streamOffset;
	}

	/**
	 * Get the stream / target ID for this entry.
	 * 
	 * @return The stream ID.
	 */
	public int getStreamID() {
		return streamID;
	}

	/**
	 * Get the size of the struct.
	 * 
	 * @return The size of the struct in bytes.
	 */
	public static int getSize() {
		return 28;
	}

	@Override
	public String toString() {
		return String.format("[0x%08x:0x%08x] => %s [0x%08x:0x%08x]", offset, length,
				streamID, streamOffset, length);
	}

	@Override
	public int compareTo(MapEntryPoint o) {
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
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + (int) (offset ^ (offset >>> 32));
		result = prime * result + streamID;
		result = prime * result + (int) (streamOffset ^ (streamOffset >>> 32));
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
		MapEntryPoint other = (MapEntryPoint) obj;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		if (streamID != other.streamID)
			return false;
		if (streamOffset != other.streamOffset)
			return false;
		return true;
	}

}
