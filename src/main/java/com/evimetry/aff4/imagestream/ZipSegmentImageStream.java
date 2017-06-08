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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * A IAFF4ImageStream implementation that uses a raw Zip Segment as the backing store.
 */
public class ZipSegmentImageStream extends AFF4Resource implements IAFF4ImageStream, SeekableByteChannel {

	/**
	 * The parent Zip container for this entry
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The zip entry that this segment is tied to.
	 */
	private final ZipArchiveEntry entry;

	/**
	 * The parent channel to perform reads from.
	 */
	private final FileChannel channel;

	/**
	 * The offset that this zip segment data starts at in the above channel.
	 */
	private final long offset;

	/**
	 * The position of the channel.
	 */
	private long position;

	/**
	 * The size of this entry;
	 */
	private final long size;

	/**
	 * Closed flag.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Create a new ImageStream based on a raw Zip Segment. The zip entry MUST be uncompressed.
	 * 
	 * @param resource The resource ID for this zip segment.
	 * @param parent The parent zip container
	 * @param channel The file channel to perform IO against.
	 * @param entry The zip entry.
	 * @throws IOException If creation of the image stream fails.
	 */
	public ZipSegmentImageStream(String resource, AFF4ZipContainer parent, FileChannel channel, ZipArchiveEntry entry)
			throws IOException {
		super(resource);
		this.parent = parent;
		this.channel = channel;
		this.entry = entry;
		this.size = entry.getSize();
		this.offset = entry.getDataOffset();
		if (offset <= 0) {
			throw new IOException("Invalid offset in datastream");
		}
		initProperties();
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
		return channel.read(dst, offset + position);
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
		this.position = newPosition;
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
		result = prime * result + (int) (offset ^ (offset >>> 32));
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
		ZipSegmentImageStream other = (ZipSegmentImageStream) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (offset != other.offset)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

}
