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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.jena.rdf.model.Model;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.codec.CompressionCodec;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.rdf.RDFUtil;
import com.evimetry.aff4.resource.AFF4Resource;
import com.evimetry.aff4.struct.BevvyIndex;
import com.evimetry.aff4.struct.BevvyIndexLoaderFunction;
import com.evimetry.aff4.struct.ChunkLoaderFunction;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * aff4:ImageStream implementation for compressed data stream in AFF4 Container.
 */
public class AFF4ImageStream extends AFF4Resource implements IAFF4ImageStream, SeekableByteChannel {

	/**
	 * The maximum number of bevvy index to keep in memory
	 */
	private final static int BEVVY_CACHE_SIZE = 10;
	/**
	 * The amount of chunk data (in bytes) to keep in memory.
	 */
	private final static long CHUNK_CACHE_SIZE = 8l * 1024l * 1024l;
	/**
	 * The parent Zip container for this entry
	 */
	private final AFF4ZipContainer parent;

	/**
	 * The position of the channel.
	 */
	private long position;

	/**
	 * The size of this entry;
	 */
	private final long size;
	/**
	 * The size of each chunk in the bevvy
	 */
	private final int chunkSize;

	/**
	 * The number of chunks per segment.
	 */
	private final int chunksInSegment;
	/**
	 * The decompressor to use for compressed chunks.
	 */
	private final CompressionCodec codec;

	/**
	 * Closed flag.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Cache of recently read bevvy index
	 */
	private final Cache<Integer, BevvyIndex> bevvyCache;
	/**
	 * Loader for the bevvy index cache
	 */
	private final BevvyIndexLoaderFunction bevvyLoader;
	/**
	 * Cache of recently read bevvy index
	 */
	private final Cache<Long, ByteBuffer> chunkCache;
	/**
	 * Loader for the chunk cache
	 */
	private final ChunkLoaderFunction chunkLoader;

	/**
	 * Create a new AFF4 Image Stream
	 * 
	 * @param resource The resource
	 * @param parent The parent container
	 * @param channel The channel to use for IO.
	 * @param zipContainer The zip container.
	 * @param model The RDF model to query about this image stream.
	 */
	public AFF4ImageStream(String resource, AFF4ZipContainer parent, ZipFile zipContainer, FileChannel channel,
			Model model) {
		super(resource);
		this.parent = parent;
		this.size = RDFUtil.readLongProperty(model, resource, AFF4Lexicon.size).orElse(0l);
		this.chunkSize = RDFUtil.readIntProperty(model, resource, AFF4Lexicon.chunkSize).orElse(AFF4.DEFAULT_CHUNK_SIZE);
		this.chunksInSegment = RDFUtil.readIntProperty(model, resource, AFF4Lexicon.chunksInSegment).orElse(AFF4.DEFAULT_CHUNKS_PER_SEGMENT);
		String compression = RDFUtil.readResourceProperty(model, resource, AFF4Lexicon.compressionMethod).orElse(AFF4Lexicon.NoCompression.getValue());
		this.codec = CompressionCodec.getCodec(compression, chunkSize);
		this.bevvyCache = Caffeine.newBuilder().maximumSize(BEVVY_CACHE_SIZE).build();
		this.chunkCache = Caffeine.newBuilder().maximumSize((int) (CHUNK_CACHE_SIZE / (long) chunkSize)).build();
		this.bevvyLoader = new BevvyIndexLoaderFunction(resource, parent, zipContainer);
		this.chunkLoader = new ChunkLoaderFunction(parent, channel, bevvyCache, bevvyLoader, chunkSize, chunksInSegment, codec);
		initProperties();
	}

	/**
	 * Initialise the properties for this aff4 object.
	 */
	private void initProperties() {
		properties.put(AFF4Lexicon.RDFType, Collections.singletonList(AFF4Lexicon.ImageStream));
		properties.put(AFF4Lexicon.size, Collections.singletonList(size));
		properties.put(AFF4Lexicon.chunkSize, Collections.singletonList(chunkSize));
		properties.put(AFF4Lexicon.chunksInSegment, Collections.singletonList(chunksInSegment));
		properties.put(AFF4Lexicon.compressionMethod, Collections.singletonList(AFF4Lexicon.forValue(codec.getResourceID())));
		// TODO: Add in digest values for this image stream as stored in the model.
	}

	@Override
	public boolean isOpen() {
		return !closed.get();
	}

	@Override
	public void close() throws IOException {
		if (!closed.getAndSet(true)) {
			parent.release(this);
			chunkCache.cleanUp();
			chunkCache.invalidateAll();
			bevvyCache.cleanUp();
			bevvyCache.invalidateAll();
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
		if (position + 1 >= size) {
			return -1;
		}
		// Determine the chunk buffer offset.
		long offset = floor(position, chunkSize);
		ByteBuffer regionBuffer = chunkCache.get(offset, chunkLoader);
		if(regionBuffer == null){
			throw new IOException("Read failed");
		}
		// set the position in our region buffer...
		long delta = position - offset;
		regionBuffer.position((int) delta);
		int count = 0;
		while (dst.hasRemaining() && regionBuffer.hasRemaining()) {
			dst.put(regionBuffer.get());
			count++;
		}
		this.position += count;
		return count;
	}

	private long floor(long offset, long size) {
		return (offset / size) * size;
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
		result = prime * result + chunkSize;
		result = prime * result + chunksInSegment;
		result = prime * result + ((codec == null) ? 0 : codec.hashCode());
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
		AFF4ImageStream other = (AFF4ImageStream) obj;
		if (chunkSize != other.chunkSize)
			return false;
		if (chunksInSegment != other.chunksInSegment)
			return false;
		if (codec == null) {
			if (other.codec != null)
				return false;
		} else if (!codec.equals(other.codec))
			return false;
		if (size != other.size)
			return false;
		return true;
	}
}
