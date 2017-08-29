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
package com.evimetry.aff4.imagestream;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.container.TestContainer;

/**
 * Test reading of raw zip segments.
 */
public class TestZipImageStream {

	private final String filename = "/Base-Linear.aff4";
	private final String resource = "aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044";
	private final String mapSHA1 = "5204743948cafe73b9c3d75052e52ead3d319cc7";
	private final String streamSHA1 = "ba85b601a65aef8adf7b0e0fb3144b217d4cd27c";
	private final String streamIndexSHA1 = "8bb7f7820cffbb3b14007e36d2b0ad2459c4d9fa";

	/**
	 * Test reading a basic file entry.
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testContainerDescription() throws Exception {
		URL url = TestContainer.class.getResource(filename);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(resource, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;

			// Get the base container.description file
			testContainerDescription(container, con.getSegment("container.description"));
			// Get the base container.description file
			testContainerDescription(container, con.getSegment("/container.description"));
			testContainerDescription(container, con.getSegment("//container.description"));
			testContainerDescription(container, con.getSegment("///container.description"));
			testContainerDescription(container, con.getSegment("////container.description"));
			// Get the base container.description file, but prefixed with the containers resource URL.
			testContainerDescription(container,	con.getSegment("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044/container.description"));
		}
	}

	/**
	 * Test for missing entries.
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testContainerMissingResource() throws Exception {
		URL url = TestContainer.class.getResource(filename);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(resource, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;

			// Try for a non-existent entry
			assertNull(con.getSegment("container.description2"));

			// Try for a non-existent entry
			assertNull(con.getSegment("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));

			// Try for the ImageStream contained...
			assertNull(con.getSegment("aff4://c215ba20-5648-4209-a793-1f918c723610"));
		}
	}

	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testContainerMapContents() throws Exception {
		URL url = TestContainer.class.getResource(filename);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(resource, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;

			/*
			 * Test the contents of the map stream via a sha1 hash. These are all the same file just addressed differently.
			 */
			testStreamContents(con.getSegment("aff4://fcbfdce7-4488-4677-abf6-08bc931e195b/map"), mapSHA1);
			testStreamContents(con.getSegment("/aff4://fcbfdce7-4488-4677-abf6-08bc931e195b/map"), mapSHA1);
			testStreamContents(con.getSegment("aff4%3A%2F%2Ffcbfdce7-4488-4677-abf6-08bc931e195b/map"), mapSHA1);
			testStreamContents(con.getSegment("/aff4%3A%2F%2Ffcbfdce7-4488-4677-abf6-08bc931e195b/map"), mapSHA1);
		}
	}

	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testContainerImageStreamContents() throws Exception {
		URL url = TestContainer.class.getResource(filename);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(resource, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;

			// Test the contents of the map stream via a sha1 hash.
			testStreamContents(con.getSegment("aff4://c215ba20-5648-4209-a793-1f918c723610/00000000.index"),
					streamIndexSHA1);
			testStreamContents(con.getSegment("aff4://c215ba20-5648-4209-a793-1f918c723610/00000000"), streamSHA1);
		}
	}

	/**
	 * Test that the given image stream contains the same contents as the containers resource id.
	 * 
	 * @param container The container
	 * @param stream The stream
	 * @throws IOException IO Failed.
	 */
	private void testContainerDescription(IAFF4Container container, IAFF4ImageStream stream) throws IOException {
		Collection<Object> rdfType = stream.getProperty(AFF4Lexicon.RDFType);
		assertEquals(AFF4Lexicon.ImageStream, rdfType.iterator().next());
		try (SeekableByteChannel channel = stream.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			channel.read(buffer);
			buffer.flip();
			String value = StandardCharsets.UTF_8.decode(buffer).toString();
			assertEquals(container.getResourceID(), value);
		}
	}

	/**
	 * Read the contents of the given segment, and compare to a sha1 of the contents.
	 * 
	 * @param segment The segment to read
	 * @param sha1 The expected sha1 of the contents.
	 * @throws IOException IO Failed.
	 * @throws NoSuchAlgorithmException
	 */
	private void testStreamContents(IAFF4ImageStream segment, String sha1)
			throws IOException, NoSuchAlgorithmException {
		Collection<Object> rdfType = segment.getProperty(AFF4Lexicon.RDFType);
		assertEquals(AFF4Lexicon.ImageStream, rdfType.iterator().next());
		try (SeekableByteChannel channel = segment.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size()).order(ByteOrder.LITTLE_ENDIAN);
			channel.read(buffer);
			buffer.flip();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(buffer);
			String result = Hex.encodeHexString(md.digest());
			assertEquals(sha1, result);
		}
	}
}
