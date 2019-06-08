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
package com.evimetry.aff4.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4Resource;
import com.evimetry.aff4.rdf.RDFUtil;

/**
 * Base class for all implemented AFF4 Objects
 */
public abstract class AFF4Resource implements IAFF4Resource {

	/**
	 * This objects resource id
	 */
	private final String resource;
	/**
	 * This objects properties.
	 */
	protected final Map<AFF4Lexicon, Collection<Object>> properties;

	/**
	 * Create a new AFF4 resource.
	 * 
	 * @param resource The resource to create.
	 */
	protected AFF4Resource(String resource) {
		this(resource, null);
	}

	/**
	 * Create a new AFF4 resource.
	 * 
	 * @param resource The resource to create.
	 * @param properties The properties of this object.
	 */
	protected AFF4Resource(String resource, Map<AFF4Lexicon, Collection<Object>> properties) {
		this.resource = resource;
		if (properties == null) {
			properties = new EnumMap<>(AFF4Lexicon.class);
		}
		this.properties = properties;
	}

	@Override
	public String getResourceID() {
		return resource;
	}

	@Override
	public Map<AFF4Lexicon, Collection<Object>> getProperties() {
		return properties;
	}

	@Override
	public Collection<Object> getProperty(AFF4Lexicon resource) {
		Collection<Object> objects = properties.get(resource);
		if (objects != null) {
			return objects;
		}
		return Collections.emptyList();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AFF4Resource other = (AFF4Resource) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return resource;
	}

	/**
	 * Add the given integer property to this objects property list if present.
	 * 
	 * @param model The RDF model to use.
	 * @param resource The resource to enquire
	 * @param property The property to add.
	 */
	protected void addIntProperty(Model model, String resource, AFF4Lexicon property) {
		Optional<Integer> value = RDFUtil.readIntProperty(model, resource, property);
		if (value.isPresent()) {
			properties.put(property, Collections.singletonList(value.get()));
		}
	}

	/**
	 * Add the given integer property to this objects property list if present.
	 * 
	 * @param model The RDF model to use.
	 * @param resource The resource to enquire
	 * @param property The property to add.
	 */
	protected void addLongProperty(Model model, String resource, AFF4Lexicon property) {
		Optional<Long> value = RDFUtil.readLongProperty(model, resource, property);
		if (value.isPresent()) {
			properties.put(property, Collections.singletonList(value.get()));
		}
	}

	/**
	 * Add the given string property to this objects property list if present.
	 * 
	 * @param model The RDF model to use.
	 * @param resource The resource to enquire
	 * @param property The property to add.
	 */
	protected void addStringProperty(Model model, String resource, AFF4Lexicon property) {
		Optional<String> value = RDFUtil.readStringProperty(model, resource, property);
		if (value.isPresent()) {
			properties.put(property, Collections.singletonList(value.get()));
		}
	}

	/**
	 * Add the given resource property to this objects property list if present.
	 * 
	 * @param model The RDF model to use.
	 * @param resource The resource to enquire
	 * @param property The property to add.
	 */
	protected void addResourceProperty(Model model, String resource, AFF4Lexicon property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property.getValue());
		Collection<Object> resources = new ArrayList<>();
		if (r.hasProperty(p)) {
			StmtIterator stmIter = r.listProperties(p);
			while (stmIter.hasNext()) {
				Statement stm = stmIter.next();
				resources.add(stm.getResource().getURI());
			}
		}
		if (!resources.isEmpty()) {
			properties.put(property, resources);
		}
	}

	/**
	 * Add the given resource property to this objects property list if present.
	 * 
	 * @param model The RDF model to use.
	 * @param resource The resource to enquire
	 */
	protected void addRDFTypeProperty(Model model, String resource) {
		Resource res = model.createResource(resource);
		/*
		 * Get all our image aff4 types.
		 */
		StmtIterator sIter = res.listProperties(RDF.type);
		List<Object> types = new ArrayList<>();
		while (sIter.hasNext()) {
			Statement stm = sIter.next();
			Resource r = stm.getResource();
			AFF4Lexicon type = AFF4Lexicon.forValue(r.getURI());
			if (type != AFF4Lexicon.UNKNOWN) {
				types.add(type);
			}
		}
		if (!types.isEmpty()) {
			properties.put(AFF4Lexicon.RDFType, types);
		}
	}

}
