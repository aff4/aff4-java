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

import java.util.UUID;

public class AFF4 {

	/**
	 * The base URI for AFF4 Standard v1.0.
	 */
	public final static String AFF4_BASE_URI = "http://aff4.org/Schema#";

	/**
	 * Typical Prefix for aff4 URN. (eg "aff4://").
	 */
	public final static String AFF4_URN_PREFIX = "aff4://";

	/**
	 * RDF Prefix type for JENA.
	 */
	public final static String AFF4_RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * Black Bag Technologies base URI for custom properties
	 */
	public final static String BBT_BASE_URI = "https://blackbagtech.com/aff4/Schema#";
	
	/**
	 * The default chunk size
	 */
	public final static int DEFAULT_CHUNK_SIZE = 32 * 1024;
	/**
	 * The default number of chunks for each segment/bevvy instance.
	 */
	public final static int DEFAULT_CHUNKS_PER_SEGMENT = 2 * 1024;
	/**
	 * The default number of bytes per sector.
	 */
	public final static int BYTES_PER_SECTOR_DEFAULT = 512;

	/**
	 * The default filename extension for AFF4 files.
	 */
	public final static String DEFAULT_AFF4_EXTENSION = ".aff4";
	/**
	 * Filename of the TURTLE file being stored.
	 */
	public final static String INFORMATIONTURTLE = "information.turtle";
	/**
	 * Filename of the container description file used to hold the Volume AFF4 resource ID.
	 */
	public final static String FILEDESCRIPTOR = "container.description";
	/**
	 * Version file.
	 */
	public final static String VERSIONDESCRIPTIONFILE = "version.txt";
	
	/**
	 * Generate a new random AFF4 UUID resource ID as a string
	 * 
	 * @return A new aff4 resource ID.
	 */
	public static String generateID() {
		return (AFF4_URN_PREFIX + UUID.randomUUID().toString());
	}
}
