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
package com.evimetry.aff4.image;

import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.map.AFF4Map;
import com.evimetry.aff4.rdf.RDFUtil;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * aff4:Image implementation.
 */
public class AFF4Image extends AFF4Resource implements IAFF4Image {

	private final static Logger logger = LoggerFactory.getLogger(AFF4Image.class);
	/**
	 * The parent container.
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The RDF model to query about this image stream.
	 */
	private final Model model;

	/**
	 * Create a new AFF4 Image instance
	 * 
	 * @param resource The resource for this image.
	 * @param parent The parent container
	 * @param model The RDF model to query about this image stream.
	 */
	public AFF4Image(String resource, AFF4ZipContainer parent,  Model model) {
		super(resource);
		this.parent = parent;
		this.model = model;
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
		 * Add in image information:
		 */
		// Basic details
		addLongProperty(model, getResourceID(), AFF4Lexicon.size);
		addStringProperty(model, getResourceID(), AFF4Lexicon.acquisitionCompletionState);
		addStringProperty(model, getResourceID(), AFF4Lexicon.acquisitionType);
		addResourceProperty(model, getResourceID(), AFF4Lexicon.dataStream);
		addResourceProperty(model, getResourceID(), AFF4Lexicon.dependentStream);
		// Is disk or memory image?
		if (properties.get(AFF4Lexicon.RDFType).contains(AFF4Lexicon.MemoryImage)) {
			// Memory
			addLongProperty(model, getResourceID(), AFF4Lexicon.pageSize);
			addLongProperty(model, getResourceID(), AFF4Lexicon.memoryPageTableEntryOffset);
			addLongProperty(model, getResourceID(), AFF4Lexicon.memoryInstalledSize);
			addLongProperty(model, getResourceID(), AFF4Lexicon.memoryAddressableSize);
		} else {
			// Disk
			addIntProperty(model, getResourceID(), AFF4Lexicon.blockSize);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskDeviceName);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskFirmware);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskInterfaceType);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskMake);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskModel);
			addStringProperty(model, getResourceID(), AFF4Lexicon.diskSerial);
			addLongProperty(model, getResourceID(), AFF4Lexicon.sectorCount);
		}
		// Add basic case details.
		Optional<String> caseResource = RDFUtil.getResourceTarget(model, getResourceID(), AFF4Lexicon.CaseDetails);
		if (caseResource.isPresent()) {
			addStringProperty(model, caseResource.get(), AFF4Lexicon.caseName);
			addStringProperty(model, caseResource.get(), AFF4Lexicon.caseDescription);
			addStringProperty(model, caseResource.get(), AFF4Lexicon.examiner);
		}
	}

	@Override
	public IAFF4Map getMap() {
		if (properties.containsKey(AFF4Lexicon.dataStream)) {
			String mapResource = properties.get(AFF4Lexicon.dataStream).iterator().next().toString();
			return new AFF4Map(mapResource, parent, model);
		}
		if (properties.get(AFF4Lexicon.RDFType).contains(AFF4Lexicon.Map)) {
			// we are also a aff4:Map
			return new AFF4Map(getResourceID(), parent, model);
		}
		logger.warn("No Map aff4:dataStream defined for Image " + getResourceID());
		return null;
	}

}
