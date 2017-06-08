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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.IAFF4Resolver;
import com.evimetry.aff4.IAFF4Resource;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.imagestream.ImageStreamFactory;
import com.evimetry.aff4.imagestream.Streams;
import com.evimetry.aff4.map.collection.LongTreap;
import com.evimetry.aff4.rdf.NameCodec;
import com.evimetry.aff4.rdf.RDFUtil;
import com.evimetry.aff4.struct.MapEntryPoint;

/**
 * Implementation to materialise the map for the given aff4:Map object
 */
public class AFF4MapMaterialiser {

	private final static Logger logger = LoggerFactory.getLogger(AFF4MapMaterialiser.class);
	/**
	 * The resource id for this map
	 */
	private final String resource;
	/**
	 * The parent container.
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The RDF model to query about this map.
	 */
	private final Model model;
	/**
	 * The stream to use to override the model's defined aff4:mapGapStream definition.
	 */
	private IAFF4ImageStream mapGapStream;
	/**
	 * The stream to use to override the aff4:Unknown stream definition.
	 */
	private IAFF4ImageStream unknownStream;
	/**
	 * The stream to use to override the defaults when a dependent stream cannot be located/materialised.
	 */
	private IAFF4ImageStream missingStream;

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
	 * Create a new map materialiser.
	 * 
	 * @param resource The resource
	 * @param parent The parent container
	 * @param model The RDF model to query about this image stream.
	 */
	protected AFF4MapMaterialiser(String resource, AFF4ZipContainer parent, Model model) {
		this.resource = sanitize(resource, parent.getResourceID());
		this.parent = parent;
		this.model = model;
	}

	/**
	 * Initialise and build the map.
	 * 
	 * @throws IOException If reading the map or materialisation fails.
	 * @return itself.
	 */
	protected synchronized AFF4MapMaterialiser build() throws IOException {
		if (map == null) {
			map = new LongTreap<>();
			externalContainers = new ArrayList<>();
			streams = new HashMap<>();
			setMapGapDefaultStream();
			long size = RDFUtil.readLongProperty(model, resource, AFF4Lexicon.size).orElse(0l);
			if (size == 0) {
				logger.warn("Map is zero sized?");
				return this;
			}
			// Read in the target index.
			readTargetIndex();
			// Add now build the map itself.
			readMap(getIsSparse(), size);
		}
		return this;
	}

	/**
	 * Set the map gap stream if not set.
	 */
	private void setMapGapDefaultStream() {
		if (mapGapStream == null) {
			// See if we have an entry in the model?
			Optional<String> mapGPS = RDFUtil.readResourceProperty(model, resource, AFF4Lexicon.mapGapDefaultStream);
			if (mapGPS.isPresent()) {
				IAFF4Resource stream = parent.open(mapGPS.get());
				if (stream != null && stream instanceof IAFF4ImageStream) {
					mapGapStream = (IAFF4ImageStream) stream;
				}
			}
			// Standard default;
			if (mapGapStream == null) {
				mapGapStream = ImageStreamFactory.createZeroStream();
			}
		}
	}

	/**
	 * Read in the target index file, populating the {@link #streams} field.
	 * 
	 * @throws IOException If reading the map failed.
	 */
	private void readTargetIndex() throws IOException {
		String mapTargetName = NameCodec.encode(String.format("%s/idx", resource));
		// Load the target map ids
		IAFF4ImageStream stream = parent.getSegment(mapTargetName);
		try (SeekableByteChannel channel = stream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			Streams.readFull(channel, 0, buffer);
			buffer.flip();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(buffer.array())))) {
				int index = 0;
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					if (!line.isEmpty()) {
						IAFF4Resource res;
						// Allow for unknown override.
						if (line.equals(AFF4Lexicon.UnknownData.getValue()) && unknownStream != null) {
							res = unknownStream;
						} else {
							res = parent.open(line);
						}
						if (res != null && res instanceof IAFF4ImageStream) {
							streams.put(index, (IAFF4ImageStream) res);
						} else {
							res = queryResolver(parent.getResolver(), line);
							if (res != null && res instanceof IAFF4ImageStream) {
								streams.put(index, (IAFF4ImageStream) res);
							} else {
								String uri = AFF4Lexicon.UnknownData.getValue();
								if (missingStream != null) {
									uri = missingStream.getResourceID();
								}
								logger.warn("Unable to locate stream resource {}, replacing with {}", line, uri);
								if (missingStream == null) {
									streams.put(index, ImageStreamFactory.createUnknownStream(line));
								} else {
									streams.put(index, missingStream);
								}
							}
						}
						index += 1;

					} else {
						logger.warn(String.format("Unexpected empty line in \"%s\"?", mapTargetName));
					}
				}
			}
		}
	}

	/**
	 * Read in and materialise the map.
	 * 
	 * @param isSparse TRUE if this map is expected to be sparse.
	 * @param size The size of the map.
	 * 
	 * @throws IOException If reading the map failed.
	 */
	private void readMap(boolean isSparse, long size) throws IOException {
		String mapTargetName = NameCodec.encode(String.format("%s/map", resource));
		// Load the target map ids
		IAFF4ImageStream stream = parent.getSegment(mapTargetName);
		try (SeekableByteChannel channel = stream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			Streams.readFull(channel, 0, buffer);
			buffer.flip();
			int sz = MapEntryPoint.getSize();
			long offset = 0;
			while (buffer.remaining() >= sz) {
				MapEntryPoint mapPoint = MapEntryPoint.create(buffer);
				if (offset != mapPoint.getOffset()) {
					if (!isSparse) {
						logger.warn(String.format("Map %s expected offset 0x%08x, found offset 0x%08x.", resource,
								offset, mapPoint.getOffset()));
					}
					// fill in with mapGapStream.
					map.put(offset, MapEntryPoint.create(offset, mapPoint.getOffset() - offset, offset, -1,
							mapGapStream.getChannel()));
					offset = mapPoint.getOffset();
				}
				IAFF4ImageStream lstream = streams.get(mapPoint.getStreamID());
				if (lstream == null) {
					logger.warn("Missing stream reference {}, replacing with unknown?", mapPoint.getStreamID());
					lstream = missingStream != null ? missingStream : ImageStreamFactory.createUnknownStream();
					streams.put(mapPoint.getStreamID(), lstream);
				}
				mapPoint.setStream(lstream.getChannel());
				map.put(offset, mapPoint);
				offset += mapPoint.getLength();
			}
			if (offset != size) {
				// missing end?
				map.put(offset, MapEntryPoint.create(offset, size - offset, offset, -1, mapGapStream.getChannel()));
			}
		}
	}

	/**
	 * Query an external resolver for this resource.
	 * 
	 * @param resolver The resolver to use.
	 * @param resource The resource to query for.
	 * @return The AFF4 object requested or NULL if not found
	 */
	private IAFF4Resource queryResolver(IAFF4Resolver resolver, String resource) {
		if (resolver == null) {
			return null;
		}
		// See if the resolver can open this resource directly.
		if (resolver.hasResource(resource)) {
			return resolver.open(resource);
		}
		// If not, see if the resolver has the parent of this resource, and we can open it from there.
		Optional<String> stored = RDFUtil.readResourceProperty(model, resource, AFF4Lexicon.stored);
		if(stored.isPresent()){
			// Our object has a stored property...
			if(resolver.hasResource(stored.get())){
				IAFF4Resource object = resolver.open(stored.get());
				if(object != null){
					if(object instanceof IAFF4Container){
						IAFF4Container extContainer = (IAFF4Container)object;
						externalContainers.add(extContainer);
						object = extContainer.open(resource);
					}
				}
				return object;
			}
		}
		return null;
	}

	/**
	 * Attempt to sanitize this resources uri
	 * 
	 * @param res The resource to sanitise
	 * @param parent The parent containers uri.
	 * @return
	 */
	private String sanitize(String res, String parent) {
		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		if (res.startsWith(parent)) {
			res = res.substring(parent.length());
		}

		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		return res;
	}

	/**
	 * Attempt to determine if this map is expected to be sparse?
	 * 
	 * @return TRUE if according to the RDF model we are expected to be a sparse map.
	 */
	private boolean getIsSparse() {
		Resource res = model.createResource(resource);
		Property prop = model.createProperty(AFF4Lexicon.DiscontiguousImage.getValue());
		if (res.hasProperty(RDF.type, prop)) {
			return true;
		}
		Optional<String> target = RDFUtil.readResourceProperty(model, resource, AFF4Lexicon.target);
		if (target.isPresent()) {
			res = model.createResource(target.get());
			if (res.hasProperty(RDF.type, prop)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the collection of streams opened for this map.
	 * 
	 * @return The collection of stream opened for this map.
	 */
	protected Map<Integer, IAFF4ImageStream> getStreams() {
		try {
			build();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return streams;
	}

	/**
	 * Get the map, as a LongTreap instance.
	 * 
	 * @return The map.
	 */
	protected LongTreap<MapEntryPoint> getMap() {
		try {
			build();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return map;
	}

	/**
	 * Get any external containers utilised in materialising this map.
	 * 
	 * @return A collection of containers utilised.
	 */
	protected Collection<IAFF4Container> getExternalContainers() {
		try {
			build();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return externalContainers;
	}

	/**
	 * Set the stream to use as an override for the aff4:mapGapDefaultStream.
	 * <p>
	 * Note: This MUST be called before {@link #build()} for this setting to come into effect.
	 * 
	 * @param stream The stream to use.
	 * 
	 * @return itself.
	 */
	protected AFF4MapMaterialiser setMapGapStreamOverride(IAFF4ImageStream stream) {
		this.mapGapStream = stream;
		return this;
	}

	/**
	 * Set the stream to use as an override for the aff4:Unknown stream.
	 * <p>
	 * Note: This MUST be called before {@link #build()} for this setting to come into effect.
	 * 
	 * @param stream The stream to use.
	 * @return itself.
	 */
	protected AFF4MapMaterialiser setUnknownStreamOverride(IAFF4ImageStream stream) {
		this.unknownStream = stream;
		return this;
	}

	/**
	 * Set the stream to use as an override for when a dependent stream cannot be located.
	 * <p>
	 * Note: This MUST be called before {@link #build()} for this setting to come into effect.
	 * 
	 * @param stream The stream to use.
	 * @return itself.
	 */
	protected AFF4MapMaterialiser setMissingStreamOverride(IAFF4ImageStream stream) {
		this.missingStream = stream;
		return this;
	}
}
