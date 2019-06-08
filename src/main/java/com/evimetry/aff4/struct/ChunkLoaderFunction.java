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
import java.nio.channels.FileChannel;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.codec.CompressionCodec;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * Function for loading a Chunk into memory for the given offset.
 */
public class ChunkLoaderFunction implements Function<Long, ByteBuffer> {

	private final static Logger logger = LoggerFactory.getLogger(ChunkLoaderFunction.class);
	/**
	 * The parent container
	 */
	@SuppressWarnings("unused")
	private final AFF4ZipContainer parent;
	/**
	 * The channel to load our buffer from
	 */
	private final FileChannel channel;
	/**
	 * The bevvy cache
	 */
	private final Cache<Integer, BevvyIndex> bevvyCache;
	/**
	 * Loader function for the cache.
	 */
	private final BevvyIndexLoaderFunction bevvyLoader;
	/**
	 * The chunksize
	 */
	private final long chunkSize;
	/**
	 * The number of chunks per segment
	 */
	private final long chunksInSegment;
	/**
	 * The compression codec to decompress raw buffers.
	 */
	private final CompressionCodec codec;

	/**
	 * Function for loading a Chunk into memory for the given offset.
	 * 
	 * @param parent The parent container
	 * @param channel The channel to load our buffer from
	 * @param bevvyCache The bevvy cache
	 * @param bevvyLoader Loader function for the bevvy cache.
	 * @param chunkSize The chunksize
	 * @param chunksInSegment The number of chunks per segment
	 * @param codec The compression codec to decompress raw buffers.
	 */
	public ChunkLoaderFunction(AFF4ZipContainer parent, FileChannel channel, Cache<Integer, BevvyIndex> bevvyCache,
			BevvyIndexLoaderFunction bevvyLoader, int chunkSize, int chunksInSegment, CompressionCodec codec) {
		this.parent = parent;
		this.channel = channel;
		this.bevvyCache = bevvyCache;
		this.bevvyLoader = bevvyLoader;
		this.chunksInSegment = chunksInSegment;
		this.chunkSize = chunkSize;
		this.codec = codec;
	}

	@Override
	public ByteBuffer apply(Long offset) {
		// Determine the bevvy ID.
		long bevvyID = (offset / chunkSize) / chunksInSegment;
		BevvyIndex index = bevvyCache.get((int) bevvyID, bevvyLoader);
		if (index == null) {
			logger.error("Failed to read bevvy index");
			return null;
		}
		// Determine the offset into the bevvy index our chunk is.
		long chunkID = (offset / chunkSize) % chunksInSegment;
		ImageStreamPoint point = index.getPoint((int) chunkID);
		if (point == null) {
			logger.error("Failed to read bevvy index point");
			return null;
		}
		long chunkOffset = index.getOffset() + point.getOffset();
		long chunkLength = point.getLength();
		try {
			ByteBuffer buffer = ByteBuffer.allocateDirect((int) chunkLength).order(ByteOrder.LITTLE_ENDIAN);
			int toRead = (int)chunkLength;
			// In all typical circumstances this should be a single read, but be careful otherwise.
			while(toRead >= 0){
				int read = channel.read(buffer, chunkOffset);
				if(read <= 0){
					break;
				}
				toRead -= read;
				chunkOffset += read;
			}
			buffer.flip();
			if (toRead > 0) {
				throw new IOException("Failed to read");
			}
			// now decompress if the buffer is not chunk length;
			if (chunkLength != chunkSize) {
				return codec.decompress(buffer);
			}
			return buffer;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
