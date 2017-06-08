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
package com.evimetry.aff4;

import java.util.Collection;
import java.util.Map;

/**
 * General interface for all aff4 resources.
 */
public interface IAFF4Resource {

	/**
	 * Get the resource URN for this AFF4 Object
	 * 
	 * @return The resource URN for this AFF4 Object.
	 */
	public String getResourceID();

	/**
	 * Get a basic collection of properties for this object.
	 * 
	 * @return A basic collection of properties for this object.
	 */
	public Map<AFF4Lexicon, Collection<Object>> getProperties();

	/**
	 * Get the collection of objects for the given property
	 * 
	 * @param resource The resource to acquired
	 * @return The collection of objects for the given key.
	 */
	public Collection<Object> getProperty(AFF4Lexicon resource);
}
