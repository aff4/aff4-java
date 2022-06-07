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
package com.evimetry.aff4.struct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.imagestream.Streams;
import com.evimetry.aff4.rdf.NameCodec;

/**
 * An ImageStream/Bevvy index reader.
 */
public class BevvyIndex {

	/**
	 * The bevvy index ID which we represent
	 */
	private final int bevvyID;

	/**
	 * The offset of the first chunk in the parent's channel
	 */
	private final long offset;

	/**
	 * The collection of image stream points.
	 */
	private ImageStreamPoint[] entries;

	/**
	 * Create a new Bevvy Index reader
	 * 
	 * @param resource The resource of the image stream we are servicing.
	 * @param bevvyID The bevvy id
	 * @param parent The parent zip container
	 * @param zipContainer The zip container.
	 * @throws IOException If reading the zip container fails.
	 */
	public BevvyIndex(String resource, int bevvyID, AFF4ZipContainer parent, ZipFile zipContainer) throws IOException {
		this.bevvyID = bevvyID;

		// Get the offset of the bevvy segment into the primary channel.
		String resourceID = String.format("%s/%08d", resource, bevvyID);
		String bevvyChunkName = NameCodec.encode(resourceID);
		ZipArchiveEntry entry = zipContainer.getEntry(bevvyChunkName);
		if (entry == null) {
			bevvyChunkName = NameCodec.SanitizeResource(resourceID, parent.getResourceID());
			entry = zipContainer.getEntry(bevvyChunkName);
			// Some AFF4 tools strip leading '/' characters from the
			// entity name, others leave it in.  Same goes for trailing
			// '/' characters on ARNs.
			if (entry == null) {
				entry = zipContainer.getEntry("/" + bevvyChunkName);
			}
		}
		if (entry == null)
			throw new IOException("Missing bevvy segment");
		this.offset = entry.getDataOffset();

		// Load the indices
		String bevvyIndexID = String.format("%s/%08d.index", resource, bevvyID);
		String bevvyIndexName = NameCodec.encode(bevvyIndexID);
		IAFF4ImageStream stream = parent.getSegment(bevvyIndexName);
		if (stream == null) {
			stream = parent.getSegment(bevvyIndexID);
		}
		try (SeekableByteChannel channel = stream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			Streams.readFull(channel, 0, buffer);
			buffer.flip();
			int sz = ImageStreamPoint.getSize();
			this.entries = new ImageStreamPoint[buffer.remaining() / sz];
			int index = 0;
			while (buffer.remaining() >= sz) {
				entries[index++] = ImageStreamPoint.create(buffer);
			}
		}
	}

	/**
	 * Get the bevvy id.
	 * 
	 * @return The Bevvy ID
	 */
	public int getBevvyID() {
		return bevvyID;
	}

	/**
	 * The offset of the first chunk in the parent's channel
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Get the image point for this region
	 * 
	 * @param offset The chunk offset.
	 * @return The image point, or null if none exist.
	 */
	public ImageStreamPoint getPoint(int offset) {
		if (offset < 0 || offset >= entries.length) {
			return null;
		}
		return entries[offset];
	}
}
