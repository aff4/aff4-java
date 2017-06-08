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
package com.evimetry.aff4.map;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.map.collection.LongTreap;
import com.evimetry.aff4.rdf.RDFUtil;
import com.evimetry.aff4.resource.AFF4Resource;
import com.evimetry.aff4.struct.MapEntryPoint;

public class AFF4Map extends AFF4Resource implements IAFF4Map, SeekableByteChannel {

	private final static Logger logger = LoggerFactory.getLogger(AFF4Map.class);
	/**
	 * The parent container.
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The RDF model to query about this map.
	 */
	private final Model model;
	/**
	 * The position of the channel.
	 */
	private long position = 0;
	/**
	 * The size of this entry;
	 */
	private final long size;
	/**
	 * Closed flag.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * The map for region lookup.
	 */
	private LongTreap<MapEntryPoint> map;
	/**
	 * Collection of streams.
	 */
	private Map<Integer, IAFF4ImageStream> streams;
	/**
	 * Any external containers that were needed to materialise this map.
	 */
	private Collection<IAFF4Container> externalContainers;

	/**
	 * The stream to use to override the model's defined aff4:mapGapStream definition.
	 */
	private IAFF4ImageStream mapGapStreamOverride;
	/**
	 * The stream to use to override the aff4:Unknown stream definition.
	 */
	private IAFF4ImageStream unknownStreamOverride;

	/**
	 * Create a new aff4:Map object
	 * 
	 * @param resource The resource
	 * @param parent The parent container
	 * @param model The RDF model to query about this image stream.
	 */
	public AFF4Map(String resource, AFF4ZipContainer parent, Model model) {
		super(resource);
		this.parent = parent;
		this.model = model;
		this.size = RDFUtil.readLongProperty(model, resource, AFF4Lexicon.size).orElse(0l);
		initProperties(model);
	}

	/**
	 * Initialise the properties for this aff4 object.
	 * 
	 * @param model The model to read for properties about this image.
	 */
	private void initProperties(Model model) {
		/*
		 * Get all our image aff4 types.
		 */
		addRDFTypeProperty(model, getResourceID());
		/*
		 * Add in map information
		 */
		addLongProperty(model, getResourceID(), AFF4Lexicon.size);
		addResourceProperty(model, getResourceID(), AFF4Lexicon.dependentStream);
		// TODO: Add in digest values for this map as stored in the model.
	}

	/**
	 * Materialise the map.
	 */
	private synchronized void initialiseMap() throws IOException {
		if (map == null) {
			AFF4MapMaterialiser mapFactory = new AFF4MapMaterialiser(getResourceID(), parent, model)//
					.setMapGapStreamOverride(mapGapStreamOverride)//
					.setUnknownStreamOverride(unknownStreamOverride)//
					.build();

			streams = mapFactory.getStreams();
			map = mapFactory.getMap();
			externalContainers = mapFactory.getExternalContainers();
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
		if (dst.remaining() > size - position) {
			// determine the correct limit for the buffer.
			int remaining = (int) (size - position);
			dst.limit(dst.position() + remaining);
		}
		int read = 0;
		// look for the first region that can service this read request.
		MapEntryPoint point = map.get(position);
		if (point == null) {
			point = map.findPrevious(position);
		}
		// get the delta between the map point, and our current position.
		long delta = position - point.getOffset();
		SeekableByteChannel stream = point.getStream();
		int oldLimit = dst.limit();
		// Ensure we limit the read to this region.
		long streamRead = point.getLength() - delta;
		if (dst.remaining() > streamRead) {
			dst.limit(oldLimit - (int)(dst.remaining() - streamRead));
		}
		int sread = 0;
		// Synchronize on the stream to ensure we don't have a race condition when setting our positions...
		synchronized (stream) {
			long oldStreamPosition = stream.position();
			stream.position(point.getStreamOffset() + delta);
			sread = stream.read(dst);
			stream.position(oldStreamPosition);
			//logger.info(String.format("Reading 0x%8x : %s", position, point.toString()));
		}
		if (sread >= 0) {
			read += sread;
		}
		// restore the limit;
		dst.limit(oldLimit);

		position += read;
		return read;
	}

	@Override
	public SeekableByteChannel getChannel() throws IOException {
		initialiseMap();
		return this;
	}

	@Override
	public boolean isOpen() {
		return !closed.get();
	}

	@Override
	public void close() throws IOException {
		if (!closed.getAndSet(true)) {
			// Mark all streams as closed.
			for (IAFF4ImageStream stream : streams.values()) {
				try {
					stream.getChannel().close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			// Close any external containers.
			for (IAFF4Container container : externalContainers) {
				try {
					container.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
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
	public long size() throws IOException {
		return size;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new IOException(IAFF4ImageStream.WRITE_ERROR_MESSAGE);
	}

	/**
	 * Get the stream used to override the map gap default stream property.
	 * 
	 * @return The stream used as an override.
	 */
	public IAFF4ImageStream getMapGapStreamOverride() {
		return mapGapStreamOverride;
	}

	/**
	 * Set the stream to use as an override for the aff4:mapGapDefaultStream.
	 * <p>
	 * Note: This MUST be called before {@link #getChannel()} for this setting to come into effect.
	 * 
	 * @param stream The stream to use.
	 */
	public synchronized void setMapGapStreamOverride(IAFF4ImageStream stream) {
		if (map == null) {
			this.mapGapStreamOverride = stream;
		}
	}

	/**
	 * Get the stream used to override any instances of aff4:Unknown stream used in the map.
	 * 
	 * @return The stream used as an override.
	 */
	public IAFF4ImageStream getUnknownStreamOverride() {
		return unknownStreamOverride;
	}

	/**
	 * Set the stream to use as an override for the aff4:Unknown stream.
	 * <p>
	 * Note: This MUST be called before {@link #getChannel()} for this setting to come into effect.
	 * 
	 * @param stream The stream to use.
	 */
	public synchronized void setUnknownStreamOverride(IAFF4ImageStream stream) {
		if (map == null) {
			this.unknownStreamOverride = stream;
		}
	}

}
