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
package com.evimetry.aff4.imagestream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * A IAFF4ImageStream implementation that uses a buffer for the stream.
 */
public class ZipSegmentImageCompressedStream extends AFF4Resource implements IAFF4ImageStream, SeekableByteChannel {

	/**
	 * The largest size this stream can be.
	 */
	public final static long MAX_BUFFER_SIZE = 32 * 1024 * 1024;
	/**
	 * The parent AFF4 Zip container for this entry
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The zip entry that this segment is tied to.
	 */
	private final ZipArchiveEntry entry;
	/**
	 * The size of this entry;
	 */
	private final long size;
	/**
	 * The position of the channel.
	 */
	private long position;
	/**
	 * Closed flag.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	/**
	 * The buffer to hold the decompressed image data.
	 */
	private final byte[] buffer;

	/**
	 * Create a new ImageStream based on a raw Zip Segment. The zip entry MUST be uncompressed.
	 * 
	 * @param resource The resource ID for this zip segment.
	 * @param parent The parent AFF4 zip container
	 * @param zip The parent Zip container.
	 * @param entry The zip entry.
	 * @throws IOException If creation of the image stream fails.
	 */
	public ZipSegmentImageCompressedStream(String resource, AFF4ZipContainer parent, ZipFile zip, ZipArchiveEntry entry)
			throws IOException {
		super(resource);
		this.parent = parent;
		this.entry = entry;
		this.size = entry.getSize();
		if (this.size > MAX_BUFFER_SIZE) {
			throw new IOException("IAFF4ImageStream buffered stream is too large");
		}
		initProperties();
		// Load the buffer.
		try (InputStream stream = zip.getInputStream(entry)) {
			buffer = IOUtils.readFully(stream, (int) size);
		}
	}

	/**
	 * Initialise the properties for this aff4 object.
	 */
	private void initProperties() {
		properties.put(AFF4Lexicon.RDFType, Collections.singletonList(AFF4Lexicon.ImageStream));
		properties.put(AFF4Lexicon.size, Collections.singletonList(size));
	}

	@Override
	public boolean isOpen() {
		return !closed.get();
	}

	@Override
	public void close() throws IOException {
		if (!closed.getAndSet(true)) {
			parent.release(this);
		}
	}

	@Override
	public synchronized int read(ByteBuffer dst) throws IOException {
		if (closed.get()) {
			throw new ClosedChannelException();
		}
		if (dst == null || !dst.hasRemaining()) {
			return 0;
		}
		int limit = (int) Math.min(dst.remaining(), size - position);
		if(limit <= 0) {
			return 0;
		}
		int offset = (int) position;
		dst.put(buffer, offset, limit);
		position += limit;
		return limit;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		throw new IOException(IAFF4ImageStream.WRITE_ERROR_MESSAGE);
	}

	@Override
	public synchronized long position() throws IOException {
		return position;
	}

	@Override
	public synchronized SeekableByteChannel position(long newPosition) throws IOException {
		if (newPosition >= size) {
			newPosition = size - 1;
		}
		position = newPosition;
		return this;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new IOException(IAFF4ImageStream.WRITE_ERROR_MESSAGE);
	}

	@Override
	public long size() throws IOException {
		return size;
	}

	@Override
	public SeekableByteChannel getChannel() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result + (int) (size ^ (size >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZipSegmentImageCompressedStream other = (ZipSegmentImageCompressedStream) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

}
