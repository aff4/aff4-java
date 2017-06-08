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
package com.evimetry.aff4.imagestream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Helper utility functions for reading from streams,
 */
public class Streams {
	
	/**
	 * Read the given channel into the given buffer.
	 * 
	 * @param channel The channel to read from
	 * @param position The position to read
	 * @param buffer The buffer to read into.
	 * @throws IOException If reading the buffer failed.
	 */
	public static int readFull(SeekableByteChannel channel, long position, ByteBuffer buffer) throws IOException {
		int read = 0;
		synchronized (channel) {
			long oldPosition = channel.position();
			channel.position(position);
			int toRead = (int) Math.min((long) buffer.remaining(), channel.size() - position);
			while (toRead > 0) {
				int readRes = channel.read(buffer);
				if (readRes <= 0) {
					break;
				}
				toRead -= readRes;
				read += readRes;
			}
			// reset the position.
			channel.position(oldPosition);
		}
		return read;
	}

}
