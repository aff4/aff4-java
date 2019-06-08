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
package com.evimetry.aff4;

import java.util.Iterator;

/**
 * The common interface for all AFF4 Containers.
 */
public interface IAFF4Container extends IAFF4Resource, IAFF4Resolver, AutoCloseable {

	/**
	 * Get an iterator to all AFF4 Images within this container
	 * 
	 * @return An iterator to all AFF4 Images within this container.
	 */
	public Iterator<IAFF4Image> getImages();
	
	/**
	 * Set an external resolver that this container can use to query and acquire AFF4 objects not in this container.
	 * 
	 * @param newResolver The new resolver to set
	 */
	public void setResolver(IAFF4Resolver newResolver);
	
	/**
	 * Get the external resolver for this container.
	 * 
	 * @return The external resolver. (may be NULL if none set).
	 */
	public IAFF4Resolver getResolver();

}
