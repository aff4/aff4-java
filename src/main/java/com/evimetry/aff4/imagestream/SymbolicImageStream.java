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
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * Symbolic Image Stream implementation for aff4:Zero and aff4:SymbolicDataXX
 */
public class SymbolicImageStream extends AFF4Resource implements IAFF4ImageStream, SeekableByteChannel {

	private final static Logger logger = LoggerFactory.getLogger(SymbolicImageStream.class);
	/**
	 * The symbol that this ImageStream consists of.
	 */
	private final byte symbol;

	/**
	 * The position of the channel.
	 */
	private long position;

	/**
	 * Create a new Symbolic Image Stream for the given resource
	 * <p>
	 * This constructor will attempt to guess the correct symbol based on the resource given.
	 * 
	 * @param resource The resource of the Image Stream to create.
	 */
	public SymbolicImageStream(String resource) {
		super(resource);
		/*
		 * Attempt to determine the symbol from the resource
		 */
		byte sym = 0;
		AFF4Lexicon type = AFF4Lexicon.forValue(resource);
		if (type == AFF4Lexicon.Zero) {
			sym = 0;
		} else if (resource.startsWith(AFF4Lexicon.SymbolicData.getValue())) {
			sym = getSymbol(resource.substring(AFF4Lexicon.SymbolicData.getValue().length()));
		}
		this.symbol = sym;
		initProperties();
	}

	/**
	 * Create a new Symbolic Image Stream for the given resource
	 * 
	 * @param resource The resource of the Image Stream to create.
	 * @param symbol The symbol to use for this stream.
	 */
	protected SymbolicImageStream(String resource, byte symbol) {
		super(resource);
		this.symbol = symbol;
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

	/**
	 * Get the symbol for this Symbolic Image Stream.
	 * 
	 * @return The symbol.
	 */
	public byte getSymbol() {
		return symbol;
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
		//int position = dst.position();
		while (dst.hasRemaining()) {
			dst.put(symbol);
		}
		//dst.position(position);
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

	/**
	 * Determine the symbol for the given string value
	 * 
	 * @param value The string value (hex)
	 * @return The symbol, or 0 if not parsable.
	 */
	private byte getSymbol(String value) {
		byte sym = 0;
		try {
			sym = Integer.valueOf(value, 16).byteValue();
		} catch (NumberFormatException e) {
			logger.warn(e.getMessage(), e);
		}
		return sym;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + symbol;
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
		SymbolicImageStream other = (SymbolicImageStream) obj;
		if (symbol != other.symbol)
			return false;
		return true;
	}

}
