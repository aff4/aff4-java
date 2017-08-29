/*
  This file is part of AFF4 Java.
  
  Copyright (c) 2017 Schatz Forensic Pty Ltd

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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4Map;

/**
 * Example application that will export the first image in the container as a RAW/dd file.
 */
public class Export {

	private final static Logger logger = LoggerFactory.getLogger(Export.class);

	/**
	 * Application entry point.
	 * 
	 * @param args The application arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: Export <sourcefile> <targetfile>");
			return;
		}
		String filename = args[0];
		String targetFilename = args[1];
		File file = new File(filename);

		/*
		 * Open the container, and our target
		 */
		try (IAFF4Container container = Containers.open(file);
				RandomAccessFile outContainer = new RandomAccessFile(new File(targetFilename), "rw");) {
			/*
			 * Get an iterator to all images available in this container.
			 */
			Iterator<IAFF4Image> images = container.getImages();
			/*
			 * Get the first image in the container, and print some details of the image.
			 */
			IAFF4Image image = images.next();
			exportProperties(image.getResourceID(), image.getProperties());

			/*
			 * Get the map object of the image, and construct a channel based on the map.
			 */
			IAFF4Map map = image.getMap();
			try (SeekableByteChannel channel = map.getChannel();
					FileChannel outputChannel = outContainer.getChannel()) {

				/*
				 * Read a buffer full of the stream at a time, adding to the output channel as we go.
				 */
				channel.position(0);
				outputChannel.position(0);
				/*
				 * Note: The following works in limited testing, however as per API documentation, is not guaranteed to
				 * actually transfer the whole source channel in a single call.
				 */
				outputChannel.transferFrom(channel, 0, channel.size());
				/*
				 * And finish
				 */
				System.out.println("Export to " + targetFilename + " Complete.");
			}
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Export a list of properties for this image.
	 * 
	 * @param resource The main image resource
	 * @param properties The properties of the object.
	 */
	private static void exportProperties(String resource, Map<AFF4Lexicon, Collection<Object>> properties) {
		System.out.println("Image: " + resource);
		print(properties.get(AFF4Lexicon.examiner), "Examiner: ");
		print(properties.get(AFF4Lexicon.caseName), "Case Name: ");
		print(properties.get(AFF4Lexicon.caseDescription), "Description: ");
		print(properties.get(AFF4Lexicon.diskDeviceName), "Device: ");
		print(properties.get(AFF4Lexicon.diskMake), "Device Make: ");
		print(properties.get(AFF4Lexicon.diskModel), "Device Model: ");
		print(properties.get(AFF4Lexicon.diskSerial), "Device Serial: ");
		print(properties.get(AFF4Lexicon.diskFirmware), "Device Firmware: ");
	}

	/**
	 * Print the itme if present.
	 * 
	 * @param types The collection of property values
	 * @param description The description of the values.
	 */
	private static void print(Collection<Object> types, String description) {
		if (types != null) {
			System.out.println(description + types.iterator().next().toString());
		}
	}

}
