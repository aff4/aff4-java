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
package com.evimetry.aff4.map;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
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
import com.evimetry.aff4.imagestream.Streams;
import com.evimetry.aff4.map.collection.LongTreap;
import com.evimetry.aff4.rdf.NameCodec;
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
	 * The resource id of the image this map represents.
	 */
	private final String imageResource;
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
	 * @param imageResource The resource of the image this map represents
	 * @param parent The parent container
	 * @param model The RDF model to query about this image stream.
	 */
	public AFF4Map(String resource, String imageResource, AFF4ZipContainer parent, Model model) {
		super(resource);
		this.parent = parent;
		this.imageResource = imageResource;
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
		
		checkDependentStreamInformation(model, getResourceID());

		// TODO: Add in digest values for this map as stored in the model.
	}

	/**
	 * Ensure our properties has AFF4Lexicon.dependentStream information
	 * 
	 * @param model The model to read from if required.
	 * @param resourceID The map ID.
	 */
	private void checkDependentStreamInformation(Model model, String resourceID) {
		if (!properties.containsKey(AFF4Lexicon.dependentStream)) {
			// We don't have this property, so read the 'idx' file and obtain them.
			String mapTargetName = NameCodec.encode(String.format("%s/idx", resourceID));
			// Load the target map ids
			try {
				IAFF4ImageStream stream = parent.getSegment(mapTargetName);
				try (SeekableByteChannel channel = stream.getChannel()) {
					Collection<Object> streams = new ArrayList<>();
					ByteBuffer buffer = ByteBuffer.allocate((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
					Streams.readFull(channel, 0, buffer);
					buffer.flip();
					try (BufferedReader br = new BufferedReader(
							new InputStreamReader(new ByteArrayInputStream(buffer.array())))) {
						for (String line = br.readLine(); line != null; line = br.readLine()) {
							if (!line.isEmpty()) {
								if (line.equalsIgnoreCase(AFF4Lexicon.Zero.getValue())) {
									continue;
								}
								if (line.equalsIgnoreCase(AFF4Lexicon.UnknownData.getValue())) {
									continue;
								}
								if (line.equalsIgnoreCase(AFF4Lexicon.UnreadableData.getValue())) {
									continue;
								}
								if (line.startsWith(AFF4Lexicon.SymbolicData.getValue())) {
									continue;
								}
								streams.add(model.createResource(line));
							} else {
								logger.warn(String.format("Unexpected empty line in \"%s\"?", mapTargetName));
							}
						}
						if (!streams.isEmpty()) {
							properties.put(AFF4Lexicon.dependentStream, streams);
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Materialise the map.
	 */
	private synchronized void initialiseMap() throws IOException {
		if (map == null) {
			AFF4MapMaterialiser mapFactory = new AFF4MapMaterialiser(getResourceID(), imageResource, parent, model)//
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
		if (position + 1 >= size || position == Long.MAX_VALUE) {
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
		if (closed.get()) {
			throw new ClosedChannelException();
		}
		if (newPosition < 0) {
			throw new IllegalArgumentException();
		}
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
