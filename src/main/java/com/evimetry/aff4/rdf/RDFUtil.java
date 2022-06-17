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
package com.evimetry.aff4.rdf;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import com.evimetry.aff4.AFF4Lexicon;

/**
 * Collection of utility functions to use when dealing with the JENA RDF instance.
 */
public class RDFUtil {

	/**
	 * Get the Integer value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found integer.
	 */
	public static Optional<Integer> readIntProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			if (stm != null) {
				return Optional.of(stm.getInt());
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the Integer value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found integer.
	 */
	public static Optional<Integer> readIntProperty(Model model, String resource, AFF4Lexicon property) {
		return readIntProperty(model, resource, property.getValue());
	}

	/**
	 * Get the Long value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found long.
	 */
	public static Optional<Long> readLongProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			if (stm != null) {
				return Optional.of(stm.getLong());
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the Long value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found long.
	 */
	public static Optional<Long> readLongProperty(Model model, String resource, AFF4Lexicon property) {
		return readLongProperty(model, resource, property.getValue());
	}

	/**
	 * Get the String value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found String.
	 */
	public static Optional<String> readStringProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			if (stm != null) {
				return Optional.of(stm.getString());
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the String value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found String.
	 */
	public static Optional<String> readStringProperty(Model model, String resource, AFF4Lexicon property) {
		return readStringProperty(model, resource, property.getValue());
	}

	/**
	 * Get the String value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found String.
	 */
	public static Optional<String> readResourceProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			if (stm != null) {
				return Optional.of(stm.getResource().getURI());
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the String value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found String.
	 */
	public static Optional<String> readResourceProperty(Model model, String resource, AFF4Lexicon property) {
		return readResourceProperty(model, resource, property.getValue());
	}

	/**
	 * Get the Instant Time value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found time.
	 */
	public static Optional<Instant> readDateTimeProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			Literal value = stm.getLiteral();
			if (value != null) {
				Object literalValue = value.getValue();
				if (literalValue instanceof XSDDateTime) {
					XSDDateTime datetime = (XSDDateTime) literalValue;
					return Optional.of(datetime.asCalendar().toInstant());
				} else if (literalValue instanceof BaseDatatype.TypedValue) {
					BaseDatatype.TypedValue type = (BaseDatatype.TypedValue) literalValue;
					if (type.datatypeURI.toLowerCase().contains("datetime")) {
						Calendar datetime = Calendar.getInstance();
						datetime.setTime(Date.from(ZonedDateTime.parse(type.lexicalValue).toInstant()));
						return Optional.of(datetime.toInstant());
					}
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the Instant Time value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found time.
	 */
	public static Optional<Instant> readDateTimeProperty(Model model, String resource, AFF4Lexicon property) {
		return readDateTimeProperty(model, resource, property.getValue());
	}

	/**
	 * Get the Boolean value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found boolean value.
	 */
	public static Optional<Boolean> readBooleanProperty(Model model, String resource, String property) {
		Resource r = model.createResource(resource);
		Property p = model.createProperty(property);
		if (r.hasProperty(p)) {
			Statement stm = r.getProperty(p);
			if (stm != null) {
				return Optional.of(stm.getBoolean());
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the Boolean value of the property for the given resource.
	 * 
	 * @param model The model to use
	 * @param resource The resource
	 * @param property the property
	 * @return An optional with the found boolean value.
	 */
	public static Optional<Boolean> readBooleanProperty(Model model, String resource, AFF4Lexicon property) {
		return readBooleanProperty(model, resource, property.getValue());
	}

	/**
	 * Get the resource for the given type, who points to the supplied resource.
	 * 
	 * @param model The RDF model to use
	 * @param resource The resource which is the target item
	 * @param rdftype The rdf type to enquire
	 * @return The resource of the rdf object type that points to the given resource.
	 */
	public static Optional<String> getResourceTarget(Model model, String resource, AFF4Lexicon rdftype) {
		ResIterator resources = model.listResourcesWithProperty(RDF.type, model.createResource(rdftype.getValue()));
		while (resources.hasNext()) {
			Resource res = resources.next();
			Property t = model.createProperty(AFF4Lexicon.target.getValue());
			if (res.hasProperty(t)) {
				Resource target = res.getPropertyResourceValue(t);
				if (target.getURI().equals(resource)) {
					return Optional.of(res.getURI());
				}
			}
		}
		return Optional.empty();
	}
}
