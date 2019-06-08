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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.Collections;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * Repeated Image Stream implementation for aff4:Unknown and aff4:UnreadableData.
 */
public class RepeatedImageStream extends AFF4Resource implements IAFF4ImageStream, SeekableByteChannel {

	/**
	 * The pattern that this ImageStream consists of.
	 */
	private final byte[] PATTERN;
	/**
	 * The length of the pattern.
	 */
	private final int PATTERN_LENGTH;

	/**
	 * The position of the channel.
	 */
	private long position;

	/**
	 * Predefinition to 1MB.
	 */
	private final static long UNITS_M = 1024 * 1024;
	/**
	 * Address mask for correct calculation of fill pattern alignment.
	 */
	private final static long MASK = (UNITS_M - 1);

	/**
	 * Create a new Repeated Pattern Image Stream for the given resource
	 * <p>
	 * This constructor will attempt to guess the correct symbol based on the resource given.
	 * 
	 * @param resource The resource of the Image Stream to create.
	 */
	protected RepeatedImageStream(String resource) {
		super(resource);
		/*
		 * Attempt to determine the pattern from the resource
		 */
		String pattern = resource;
		AFF4Lexicon type = AFF4Lexicon.forValue(resource);
		if (type == AFF4Lexicon.UnknownData) {
			pattern = "UNKNOWN";
		} else if (type == AFF4Lexicon.UnreadableData) {
			pattern = "UNREADABLEDATA";
		}
		this.PATTERN = pattern.getBytes();
		this.PATTERN_LENGTH = PATTERN.length;
		initProperties();
	}

	/**
	 * Create a new Repeated Pattern Image Stream for the given resource
	 * 
	 * @param resource The resource of the Image Stream to create.
	 * @param pattern The pattern to use for this stream.
	 */
	protected RepeatedImageStream(String resource, String pattern) {
		super(resource);
		this.PATTERN = pattern.getBytes();
		this.PATTERN_LENGTH = PATTERN.length;
		initProperties();
	}

	/**
	 * Initialise the properties for this aff4 object.
	 */
	private void initProperties() {
		properties.put(AFF4Lexicon.RDFType, Collections.singletonList(AFF4Lexicon.ImageStream));
		properties.put(AFF4Lexicon.size, Collections.singletonList(Long.MAX_VALUE));
		properties.put(AFF4Lexicon.compressionMethod, Collections.singletonList(AFF4Lexicon.NoCompression));
		properties.put(AFF4Lexicon.chunkSize, Collections.singletonList(new Integer(AFF4.DEFAULT_CHUNK_SIZE)));
		properties.put(AFF4Lexicon.chunksInSegment,
				Collections.singletonList(new Integer(AFF4.DEFAULT_CHUNKS_PER_SEGMENT)));
	}

	@Override
	public long size() throws IOException {
		return Long.MAX_VALUE;
	}

	@Override
	public SeekableByteChannel getChannel() {
		return this;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
		// NOP for this implementation.
	}

	@Override
	public synchronized int read(ByteBuffer dst) throws IOException {
		if (dst == null || !dst.hasRemaining()) {
			return 0;
		}
		int remaining = dst.remaining();
		//int bposition = dst.position();

		// Specification for repeated pattern ImageStream works on 1MB boundaries.
		long offset = position & MASK;
		int delta = (int) (offset % (long) PATTERN_LENGTH);
		int limit = (int) Math.min(remaining, UNITS_M - offset);
		int remainder = remaining;
		while (remainder > 0) {
			// fill the buffer with the pattern.
			for (int i = 0; i < limit; i++) {
				dst.put(PATTERN[(i + delta) % PATTERN_LENGTH]);
			}
			remainder -= limit;
			// calculate next limit.
			limit = (int) Math.min(remainder, UNITS_M);
			delta = 0;
		}
		//dst.position(bposition);
		this.position += remaining;
		if (this.position <= 0) {
			this.position = 0;
		}
		return remaining;
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
		this.position = newPosition;
		return this;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new IOException(IAFF4ImageStream.WRITE_ERROR_MESSAGE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(PATTERN);
		result = prime * result + PATTERN_LENGTH;
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
		RepeatedImageStream other = (RepeatedImageStream) obj;
		if (!Arrays.equals(PATTERN, other.PATTERN))
			return false;
		if (PATTERN_LENGTH != other.PATTERN_LENGTH)
			return false;
		return true;
	}

}
