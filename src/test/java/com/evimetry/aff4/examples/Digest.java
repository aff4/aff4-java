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
package com.evimetry.aff4.examples;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.imagestream.Streams;

/**
 * Example application that will calculate the SHA1 digest of the first image in the provided container.
 */
public class Digest {

	private final static Logger logger = LoggerFactory.getLogger(Digest.class);

	/**
	 * Application entry point.
	 * 
	 * @param args The application arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Digest <file>");
			return;
		}
		String filename = args[0];
		File file = new File(filename);
		/*
		 * Open the container.
		 */
		try (IAFF4Container container = Containers.open(file)) {
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
			try (SeekableByteChannel channel = map.getChannel()) {
				/*
				 * Construct a message digest to generate the digest.
				 */
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024).order(ByteOrder.LITTLE_ENDIAN);

				/*
				 * Read a buffer full of the stream at a time, adding to the message digest as we go.
				 */
				channel.position(0);
				long length = channel.size();
				long offset = 0;
				while (offset < length) {
					/*
					 * Use the helper to read buffers at a time.
					 */
					int read = Streams.readFull(channel, offset, buffer);
					buffer.flip();
					md.update(buffer);
					buffer.position(0); // don't forget to reset the position in the buffer after adding to the digest.
					offset += read;
				}
				/*
				 * Get and print the result of the digest.
				 */
				String result = Hex.encodeHexString(md.digest());
				System.out.println("SHA1 : " + result);
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
