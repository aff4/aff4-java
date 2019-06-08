/*
  This file is part of AFF4 Java.
  
  Copyright (c) 2017-2019 Schatz Forensic Pty Ltd
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.evimetry.aff4.examples;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.IAFF4Resource;

/**
 * Example application that will print container / image information to the console.
 */
public class Information {

	private final static Logger logger = LoggerFactory.getLogger(Information.class);

	/**
	 * Application entry point.
	 * 
	 * @param args The application arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Information <file>");
			return;
		}
		String filename = args[0];
		File file = new File(filename);
		/*
		 * Open the container.
		 */
		try (IAFF4Container container = Containers.open(file)) {

			exportProperties("Container: ", container.getResourceID(), container.getProperties());
			/*
			 * Get an iterator to all images available in this container.
			 */
			Iterator<IAFF4Image> images = container.getImages();
			/*
			 * Get the first image in the container, and print some details of the image.
			 */
			while (images.hasNext()) {
				IAFF4Image image = images.next();
				exportProperties("\nImage: ", image.getResourceID(), image.getProperties());

				/*
				 * Get the map object of the image, and print come details of the map.
				 */
				IAFF4Map map = image.getMap();
				exportProperties("\nMap: ", map.getResourceID(), map.getProperties());

				/*
				 * Add look for dependent streams on the map.
				 */
				Collection<Object> streams = map.getProperty(AFF4Lexicon.dependentStream);
				if (streams != null) {
					Iterator<Object> elements = streams.iterator();
					while (elements.hasNext()) {
						String element = elements.next().toString();
						IAFF4Resource resource = container.open(element);
						if (resource != null) {
							exportProperties("\nStream: ", resource.getResourceID(), resource.getProperties());
						}
					}
				}

			}
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Export a list of properties for this image.
	 * 
	 * @param type The type of resource.
	 * @param resource The main image resource
	 * @param properties The properties of the object.
	 */
	private static void exportProperties(String type, String resource,
			Map<AFF4Lexicon, Collection<Object>> properties) {
		System.out.println(type + resource);
		Collection<Object> types = properties.get(AFF4Lexicon.RDFType);
		if (types != null) {
			System.out.print(" AFF4 Types: ");
			print(types);

		}
		for (Entry<AFF4Lexicon, Collection<Object>> entry : properties.entrySet()) {
			if (entry.getKey() != AFF4Lexicon.RDFType) {
				System.out.print("  " + entry.getKey() + ": ");
				print(entry.getValue());
			}
		}
	}

	/**
	 * Export a list of properties for this image.
	 * 
	 * @param types The values.
	 */
	private static void print(Collection<Object> types) {
		Iterator<Object> objects = types.iterator();
		boolean first = true;
		while (objects.hasNext()) {
			if (!first) {
				System.out.print(", ");
			} else {
				first = false;
			}
			System.out.print(objects.next().toString());
		}
		System.out.println();
	}
}
