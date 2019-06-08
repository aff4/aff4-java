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
package com.evimetry.aff4;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * aff4:ImageStream Object
 */
public interface IAFF4ImageStream extends IAFF4Resource {

	/**
	 * General error message for failed write operations.
	 */
	public static final String WRITE_ERROR_MESSAGE = "Channel implementation does not support write operations.";

	/**
	 * The size of the stream
	 * 
	 * @return The size of the stream.
	 * @throws IOException
	 */
	public long size() throws IOException;

	/**
	 * Get a Channel for the ImageStream.
	 * <p>
	 * This will typically return the same single instance of the same Channel each time this is called. Therefore care
	 * is required to ensure that {@link SeekableByteChannel#close()} is only called once.
	 * <p>
	 * Note: Most implementations of {@link IAFF4ImageStream} will also implement {@link SeekableByteChannel}, thus this
	 * method will simply return itself.
	 * 
	 * @return The channel instance for this image.
	 */
	public SeekableByteChannel getChannel();
}
