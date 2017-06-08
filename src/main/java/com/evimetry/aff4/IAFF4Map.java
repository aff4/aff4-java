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
package com.evimetry.aff4;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * aff4:Map Object
 */
public interface IAFF4Map extends IAFF4Resource {

	/**
	 * The size of the Image.
	 * <p>
	 * For aff4:DiscontiguousImage, this is the size of the address space as a whole.
	 * 
	 * @return The size of the image in bytes.
	 * 
	 * @throws IOException If an IOException occurred trying to determine the size of the object.
	 */
	public long size() throws IOException;

	/**
	 * Get a Channel for the image.
	 * 
	 * @return A new channel instance for this image.
	 * @throws IOException If creation of the channel fails.
	 */
	public SeekableByteChannel getChannel() throws IOException;
}
