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

/**
 * aff4:Resolver Object
 * <p>
 * Note: As a bare minimum a resolver must at least support resolution of volume resource IDs (that is return an
 * IAFF4Container). Support for streams (IAFF4Resource) and other artifacts such as time stamp metadata, query
 * information (RDFObjects) is optional.
 * <p>
 * It is recommended that for classes that utilise this interface to explicitly document what resources they resolve.
 */
public interface IAFF4Resolver {

	/**
	 * Open the given resource.
	 * <p>
	 * This will always create a new instance of the resource.
	 * 
	 * @param resource The resource to acquire
	 * @return The object if found, or NULL if not available.
	 */
	public IAFF4Resource open(String resource);

	/**
	 * Does this resolver know of the given resource
	 * 
	 * @param resource The resource to enquire about
	 * @return TRUE if the resource knows how to acquired and initialise this resource.
	 */
	public boolean hasResource(String resource);
}
