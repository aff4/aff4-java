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
package com.evimetry.aff4.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;

/**
 * Test enumeration of Images from containers.
 */
public class TestImages {

	@Test
	public void testContainerLinear() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://cf853d0b-5589-4c7c-8358-2ca1572b87eb", image.getResourceID());
			Map<AFF4Lexicon, Collection<Object>> properties = image.getProperties();
			testProperty(properties, AFF4Lexicon.blockSize, 512);
			testProperty(properties, AFF4Lexicon.sectorCount, 524288);
			testProperty(properties, AFF4Lexicon.diskDeviceName, "/dev/sdz");
			testProperty(properties, AFF4Lexicon.dataStream, "aff4://fcbfdce7-4488-4677-abf6-08bc931e195b");
			testProperty(properties, AFF4Lexicon.size, 268435456);
			testProperty(properties, AFF4Lexicon.diskSerial, "SGAT5060001234");
			testProperty(properties, AFF4Lexicon.caseDescription, "Canonical Image Generation Test Case");
		}
	}

	@Test
	public void testContainerAllocated() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Allocated.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://ce24a0d0-a540-442a-939e-938b848add9a", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://8fcced2b-989f-4f51-bfa2-38d4a4d818fe", image.getResourceID());
			Map<AFF4Lexicon, Collection<Object>> properties = image.getProperties();
			testProperty(properties, AFF4Lexicon.blockSize, 512);
			testProperty(properties, AFF4Lexicon.sectorCount, 524288);
			testProperty(properties, AFF4Lexicon.diskDeviceName, "/dev/sdz");
			testProperty(properties, AFF4Lexicon.dataStream, "aff4://e9cd53d3-b682-4f12-8045-86ba50a0239c");
			testProperty(properties, AFF4Lexicon.size, 268435456);
			testProperty(properties, AFF4Lexicon.diskSerial, "SGAT5060001234");
			testProperty(properties, AFF4Lexicon.caseDescription, "Canonical Image Generation Test Case");
		}
	}

	@Test
	public void testContainerLinearReadError() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear-ReadError.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://686e3512-b568-48fd-ac7b-73764b98a9aa", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://3a873665-7bf6-47b5-a12a-d6632a58ddf9", image.getResourceID());
			Map<AFF4Lexicon, Collection<Object>> properties = image.getProperties();
			testProperty(properties, AFF4Lexicon.blockSize, 512);
			testProperty(properties, AFF4Lexicon.sectorCount, 524288);
			testProperty(properties, AFF4Lexicon.diskDeviceName, "/dev/sdz");
			testProperty(properties, AFF4Lexicon.dataStream, "aff4://b282d5f4-333a-4f6a-b96f-0e5138bb18c8");
			testProperty(properties, AFF4Lexicon.size, 268435456);
			testProperty(properties, AFF4Lexicon.diskSerial, "SGAT5060001234");
			testProperty(properties, AFF4Lexicon.caseDescription, "Canonical Image Generation Test Case");
		}
	}

	@Test
	public void testContainerLinearAllHashes() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear-AllHashes.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://7a86cb01-217c-4852-b8e0-c94be1ca5ac5", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://e8733831-f8fc-4573-87d7-beb7fe708e96", image.getResourceID());
			Map<AFF4Lexicon, Collection<Object>> properties = image.getProperties();
			testProperty(properties, AFF4Lexicon.blockSize, 512);
			testProperty(properties, AFF4Lexicon.sectorCount, 524288);
			testProperty(properties, AFF4Lexicon.diskDeviceName, "/dev/sdz");
			testProperty(properties, AFF4Lexicon.dataStream, "aff4://2a497fe5-0221-4156-8b4d-176bebf7163f");
			testProperty(properties, AFF4Lexicon.size, 268435456);
			testProperty(properties, AFF4Lexicon.diskSerial, "SGAT5060001234");
			testProperty(properties, AFF4Lexicon.caseDescription, "Canonical Image Generation Test Case");
		}
	}

	/**
	 * Ensure the collection of properties, has the given item of the expected value.
	 * 
	 * @param properties The collection of properties
	 * @param property The property to test for.
	 * @param expected The expected value for this property.
	 */
	private void testProperty(Map<AFF4Lexicon, Collection<Object>> properties, AFF4Lexicon property, String expected) {
		Collection<Object> elements = properties.get(property);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		String element = elements.iterator().next().toString();
		assertEquals(expected, element);
	}

	/**
	 * Ensure the collection of properties, has the given item of the expected value.
	 * 
	 * @param properties The collection of properties
	 * @param property The property to test for.
	 * @param expected The expected value for this property.
	 */
	private void testProperty(Map<AFF4Lexicon, Collection<Object>> properties, AFF4Lexicon property, long expected) {
		Collection<Object> elements = properties.get(property);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		Object element = elements.iterator().next();
		if (element instanceof Long) {
			long e = (Long) element;
			assertEquals(expected, e);
		} else if (element instanceof Integer) {
			int e = (Integer) element;
			assertEquals(expected, e);
		} else {
			fail("Incorrect data type.");
		}
	}

}
