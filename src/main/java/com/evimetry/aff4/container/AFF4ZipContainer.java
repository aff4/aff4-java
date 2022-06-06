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
package com.evimetry.aff4.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.IAFF4Resolver;
import com.evimetry.aff4.IAFF4Resource;
import com.evimetry.aff4.image.AFF4Image;
import com.evimetry.aff4.imagestream.AFF4ImageStream;
import com.evimetry.aff4.imagestream.ImageStreamFactory;
import com.evimetry.aff4.imagestream.SymbolicImageStream;
import com.evimetry.aff4.imagestream.ZipSegmentImageCompressedStream;
import com.evimetry.aff4.imagestream.ZipSegmentImageStream;
import com.evimetry.aff4.map.AFF4Map;
import com.evimetry.aff4.rdf.NameCodec;
import com.evimetry.aff4.rdf.RDFUtil;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * AFF4 Container implementation based on a Zip file.
 */
public class AFF4ZipContainer extends AFF4Resource implements IAFF4Container {
	
	private final static Logger logger = LoggerFactory.getLogger(AFF4ZipContainer.class);
	/**
	 * The underlying zip file we are using.
	 */
	private final ZipFile zip;
	/**
	 * The parent file, the zip container is based on.
	 */
	private final File parentFile;
	/**
	 * The parent channel that is used to perform actual IO.
	 */
	private final FileChannel channel;
	/**
	 * The closed flag for this implementation.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	/**
	 * The RDF Model.
	 */
	private final Model model;
	/**
	 * An external resolver that may be queried for the aff4 objects that are not present in this container.
	 */
	private IAFF4Resolver resolver;
	/**
	 * Collection of open streams.
	 */
	private final Set<IAFF4ImageStream> openStreams = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Create a new AFF4 Container based on the given file information
	 * 
	 * @param resource The resource of the AFF4 Container
	 * @param parent The parent file.
	 * @param zip The Zip Container for this file.
	 * @throws IOException If reading the contents of the parent container or entries fail.
	 */
	public AFF4ZipContainer(String resource, File parent, ZipFile zip) throws IOException {
		super(resource);
		this.parentFile = parent;
		this.zip = zip;
		this.channel = FileChannel.open(parent.toPath(), StandardOpenOption.READ);
		setBasicProperties();
		loadVersionInformation();
		this.model = loadInformation();
		// Set the creation time property.
		Optional<Instant> time = RDFUtil.readDateTimeProperty(model, getResourceID(), AFF4Lexicon.CreationTime);
		if (time.isPresent()) {
			properties.put(AFF4Lexicon.CreationTime, Collections.singletonList(time.get()));
		}
	}

	/**
	 * The collection of base properties for this container.
	 */
	private void setBasicProperties() {
		properties.put(AFF4Lexicon.RDFType, Collections.singletonList(AFF4Lexicon.ZipVolume));
		properties.put(AFF4Lexicon.stored, Collections.singletonList(parentFile.getAbsolutePath()));
	}

	/**
	 * Load the version.txt file and add to the containers properties.
	 * 
	 * @throws ZipException Reading the zip container or contents failed.
	 * @throws IOException Reading the zip container or contents failed.
	 */
	private void loadVersionInformation() throws ZipException, IOException {
		ZipArchiveEntry entry = zip.getEntry(AFF4.VERSIONDESCRIPTIONFILE);
		if (entry != null) {
			try (InputStream stream = zip.getInputStream(entry)) {
				Properties prop = new Properties();
				prop.load(stream);
				setProperty(prop, "tool", AFF4Lexicon.Tool);
				setProperty(prop, "major", AFF4Lexicon.majorVersion);
				setProperty(prop, "minor", AFF4Lexicon.minorVersion);
				if(!checkSupportedVersion()) {
					try {
						close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					throw new IOException("AFF4 File appears to be of an unsupported version.");
				}
			}
			return;
		}
		try {
			close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		throw new IOException("File does not appear to be an AFF4 File.");
	}

	/**
	 * Set the given property if exists in the input
	 * 
	 * @param input The input Properties
	 * @param property The property to enquire
	 * @param key The key to use to insert into the main properties.
	 */
	private void setProperty(Properties input, String property, AFF4Lexicon key) {
		String v = input.getProperty(property);
		if (v != null) {
			this.properties.put(key, Collections.singletonList(v));
		}
	}

	/**
	 * Read the information.turtle file and create the RDF model.
	 * 
	 * @return The RDF model created by reading the information.turtle file.
	 * @throws ZipException Reading the zip container or contents failed.
	 * @throws IOException Reading the zip container or contents failed.
	 */
	private Model loadInformation() throws ZipException, IOException {
		/*
		 * Attempt to load the RDF model from the zip container.
		 */
		ZipArchiveEntry entry = zip.getEntry(AFF4.INFORMATIONTURTLE);
		if (entry != null) {
			try (InputStream stream = zip.getInputStream(entry)) {
				Model model = ModelFactory.createDefaultModel();
				model.read(stream, AFF4.AFF4_BASE_URI, "TURTLE");
				model.setNsPrefix("aff4", AFF4.AFF4_BASE_URI);
				model.setNsPrefix("rdf", AFF4.AFF4_RDF_PREFIX);
				model.setNsPrefix("xsd", XSDDatatype.XSD + "#");
				return model;
			}
		}
		try {
			close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		throw new IOException("File does not appear to be an AFF4 File.");
	}
	
	/**
	 * Check is a supported version.
	 * 
	 * @returns TRUE for supported version.
	 */
	private boolean checkSupportedVersion() throws IOException {
		Collection<Object> major = getProperty(AFF4Lexicon.majorVersion);
		Collection<Object> minor = getProperty(AFF4Lexicon.minorVersion);
		if (major.isEmpty() || minor.isEmpty()) {
			return false;
		}
		try {
			long maj = Long.parseLong(major.iterator().next().toString());
			long min = Long.parseLong(minor.iterator().next().toString());
			return maj == 1 && (min == 0 || min == 1);
		} catch (NumberFormatException e) {
			// Ignore.
			logger.warn(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void close() throws Exception {
		if (!closed.getAndSet(true)) {
			// Close any streams that have been opened through this container.
			Set<IAFF4ImageStream> streams = new HashSet<>(openStreams);
			for (IAFF4ImageStream stream : streams) {
				try {
					stream.getChannel().close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			// Close the zip container and IO channel.
			try {
				zip.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				channel.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public Iterator<IAFF4Image> getImages() {
		List<IAFF4Image> images = new ArrayList<>();
		ResIterator resources = model.listResourcesWithProperty(RDF.type,
				model.createResource(AFF4Lexicon.Image.getValue()));
		while (resources.hasNext()) {
			Resource res = resources.next();
			images.add(new AFF4Image(res.getURI(), this, model));
		}
		return images.iterator();
	}

	@Override
	public void setResolver(IAFF4Resolver newResolver) {
		this.resolver = newResolver;
	}

	@Override
	public IAFF4Resolver getResolver() {
		return resolver;
	}

	@Override
	public IAFF4Resource open(String resource) {
		// Check for null or empty resource request.
		if (resource == null || resource.trim().isEmpty()) {
			return null;
		}
		// See if the request is for us.
		if (getResourceID().equals(resource)) {
			return this;
		}
		IAFF4Resource r = null;
		// See if the request is for an aff4:image contained in us.
		try {
			r = getImage(resource);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (r != null) {
			return r;
		}

		// See if the request is a aff4:map contained in us.
		try {
			r = getMap(resource);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (r != null) {
			return r;
		}

		// See if the request is for a aff4:imagestream contained in us.
		try {
			r = getImageStream(resource);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (r != null) {
			return r;
		}
		// See if the request is for a zip segment contained in us.

		try {
			r = getSegment(resource);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (r != null) {
			return r;
		}
		return null;
	}

	@Override
	public boolean hasResource(String resource) {
		IAFF4Resource object = open(resource);
		if (object == null) {
			return false;
		}
		if (object instanceof IAFF4ImageStream) {
			release((IAFF4ImageStream) object);
		}
		return true;
	}

	/**
	 * Get the RDF model as stored in this container.
	 * 
	 * @return The RDF model.
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Get a segment from this Zip container.
	 * <p>
	 * This method is only public for testing purposes. It is recommended to use the {@link #open(String)} method
	 * instead.
	 * 
	 * @param resource The resource to acquire.
	 * @return A IAFF4ImageStream of the given segment, or NULL if not found.
	 * @throws IOException If creating the Zip Segment Image Stream fails.
	 */
	public IAFF4ImageStream getSegment(String resource) throws IOException {
		// Strip any leading URI for this container.
		String res = sanitizeResource(resource);
		ZipArchiveEntry entry = zip.getEntry(res);
		if (entry != null) {
			if (entry.getMethod() != ZipMethod.STORED.getCode()) {
				if (entry.getSize() < ZipSegmentImageCompressedStream.MAX_BUFFER_SIZE) {
					IAFF4ImageStream stream = new ZipSegmentImageCompressedStream(resource, this, zip, entry);
					openStreams.add(stream);
					return stream;
				}
				throw new IOException("Unable to create ImageStream from non-Stored zip segment larger than 32MB");
			}
			IAFF4ImageStream stream = new ZipSegmentImageStream(resource, this, channel, entry);
			openStreams.add(stream);
			return stream;
		}
		return null;
	}

	/**
	 * Get a ImageStream from this container.
	 * <p>
	 * This method is only public for testing purposes. It is recommended to use the {@link #open(String)} method
	 * instead.
	 * 
	 * @param resource The resource to acquire.
	 * @return A IAFF4ImageStream of the given segment, or NULL if not found.
	 * @throws IOException If creating the Image Stream fails.
	 */
	public IAFF4ImageStream getImageStream(String resource) throws IOException {

		// Check for computed streams.
		if (resource.equals(AFF4Lexicon.Zero.getValue())) {
			return ImageStreamFactory.createZeroStream();
		}
		if (resource.equals(AFF4Lexicon.UnknownData.getValue())) {
			return ImageStreamFactory.createUnknownStream();
		}
		if (resource.equals(AFF4Lexicon.UnreadableData.getValue())) {
			return ImageStreamFactory.createUnreadableStream();
		}
		if (resource.startsWith(AFF4Lexicon.SymbolicData.getValue())) {
			return new SymbolicImageStream(resource);
		}
		// Check our model if we have a resource of the correct type, which has length.
		Resource rdfResource = model.createResource(resource);
		if (rdfResource.hasProperty(RDF.type, model.createProperty(AFF4Lexicon.ImageStream.getValue()))) {
			Optional<String> stored = RDFUtil.readResourceProperty(model, resource, AFF4Lexicon.stored);
			if (stored.isPresent()) {
				if (stored.get().equals(getResourceID())) {
					// This is us!
					IAFF4ImageStream stream = new AFF4ImageStream(resource, this, zip, channel, model);
					openStreams.add(stream);
					return stream;
				}
			} else {
				// Check for index file.
				String res = sanitizeResource(resource + "/00000000.index");
				ZipArchiveEntry entry = zip.getEntry(res);
				if (entry != null) {
					// This is us!
					IAFF4ImageStream stream = new AFF4ImageStream(resource, this, zip, channel, model);
					openStreams.add(stream);
					return stream;
				}
			}
		}
		return null;
	}

	/**
	 * Get a aff4:Map from this container.
	 * <p>
	 * This method is only public for testing purposes. It is recommended to use the {@link #open(String)} method
	 * instead.
	 * 
	 * @param resource The resource to acquire.
	 * @return A IAFF4ImageStream of the given segment, or NULL if not found.
	 * @throws IOException If creating the Zip Segment Image Stream fails.
	 */
	public IAFF4Map getMap(String resource) throws IOException {
		Resource rdfResource = model.createResource(resource);
		if (rdfResource.hasProperty(RDF.type, model.createProperty(AFF4Lexicon.Map.getValue()))) {
			return new AFF4Map(resource, resource, this, model);
		}
		return null;
	}

	/**
	 * Get a aff4:image from this container.
	 * <p>
	 * This method is only public for testing purposes. It is recommended to use the {@link #open(String)} method
	 * instead.
	 * 
	 * @param resource The resource to acquire.
	 * @return A IAFF4ImageStream of the given segment, or NULL if not found.
	 * @throws IOException If creating the Zip Segment Image Stream fails.
	 */
	public IAFF4Image getImage(String resource) throws IOException {
		Resource rdfResource = model.createResource(resource);
		if (rdfResource.hasProperty(RDF.type, model.createProperty(AFF4Lexicon.Image.getValue()))) {
			return new AFF4Image(resource, this, model);
		}
		return null;
	}

	/**
	 * Attempt to sanitise the given resource string
	 * 
	 * @param res The resource string to sanitise
	 * @return The sanitised resource string.
	 */
	private String sanitizeResource(String res) {
		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		if (res.startsWith(getResourceID())) {
			res = res.substring(getResourceID().length());
		}
		// Convert any "aff4://" characters to "aff4%3A%2F%2F"
		res = NameCodec.encode(res);
		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		return res;
	}

	/**
	 * Notify this container that the ZipImageStream has been closed.
	 * 
	 * @param stream The stream which has been closed.
	 */
	public void release(IAFF4ImageStream stream) {
		openStreams.remove(stream);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parentFile == null) ? 0 : parentFile.hashCode());
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
		AFF4ZipContainer other = (AFF4ZipContainer) obj;
		if (parentFile == null) {
			if (other.parentFile != null)
				return false;
		} else if (!parentFile.equals(other.parentFile))
			return false;
		return true;
	}
}
