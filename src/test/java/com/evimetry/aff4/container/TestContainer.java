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
package com.evimetry.aff4.container;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;

/**
 * Test for opening container, and dumping RDF model.
 */
public class TestContainer {

	@Test
	public void testContainerLinear() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("Evimetry 2.2.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertEquals(Instant.parse("2016-12-07T03:40:09.126Z"), time.iterator().next());
		}
	}
	
	@Test
	public void testContainerAllocated() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Allocated.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://ce24a0d0-a540-442a-939e-938b848add9a", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("Evimetry 2.2.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertEquals(Instant.parse("2016-12-07T03:40:07.580Z"), time.iterator().next());
		}
	}
	
	@Test
	public void testContainerLinearReadError() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear-ReadError.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://686e3512-b568-48fd-ac7b-73764b98a9aa", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("Evimetry 2.2.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertEquals(Instant.parse("2016-12-07T03:40:10.647Z"), time.iterator().next());
		}
	}
	
	@Test
	public void testContainerLinearAllHashes() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Linear-AllHashes.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://7a86cb01-217c-4852-b8e0-c94be1ca5ac5", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("Evimetry 3.0.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertEquals(Instant.parse("2017-03-09T03:51:42.689Z"), time.iterator().next());
		}
	}
	
	@Test
	public void testContainerBlank() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/blank.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://b437c880-9f5a-420e-8553-8878f5518441", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("libaff4 1.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertTrue(time.isEmpty());
		}
	}
	
	@Test
	public void testContainerBlank5() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/blank5.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://37de92d3-24bb-4e5f-8279-a2b3992eba52", container.getResourceID());
			Collection<Object> tool = container.getProperty(AFF4Lexicon.Tool);
			assertEquals("libaff4 1.0", tool.iterator().next().toString());
			Collection<Object> major = container.getProperty(AFF4Lexicon.majorVersion);
			assertEquals("1", major.iterator().next().toString());
			Collection<Object> minor = container.getProperty(AFF4Lexicon.minorVersion);
			assertEquals("0", minor.iterator().next().toString());
			Collection<Object> rdfType = container.getProperty(AFF4Lexicon.RDFType);
			assertEquals(AFF4Lexicon.ZipVolume, rdfType.iterator().next());
			Collection<Object> time = container.getProperty(AFF4Lexicon.CreationTime);
			assertTrue(time.isEmpty());
		}
	}
}
