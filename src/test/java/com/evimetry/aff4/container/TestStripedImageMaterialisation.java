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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.codec.binary.Hex;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Image;
import com.evimetry.aff4.IAFF4Map;
import com.evimetry.aff4.imagestream.Streams;

/**
 * Test enumeration of Images from containers, and building a readable channel.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestStripedImageMaterialisation {

	private final static String linearSHA1 = "7d3d27f667f95f7ec5b9d32121622c0f4b60b48d";

	/**
	 * The size of the read to perform.
	 */
	private final long readSize;

	@Parameters(name = "read={0}")
	public static Collection<Object> data() {
		return Arrays.asList( //
				512, //
				571, //
				AFF4.DEFAULT_CHUNK_SIZE - 1, //
				AFF4.DEFAULT_CHUNK_SIZE, //
				AFF4.DEFAULT_CHUNK_SIZE + 1, //
				512 * 1024 - 1, //
				512 * 1024, //
				512 * 1024 + 1, //
				1024 * 1024, //
				1024 * 1024 + 1, //
				32l * 1024l * 1024l);
	}

	@Rule
	public TestName name = new TestName();

	public TestStripedImageMaterialisation(long readSize) {
		this.readSize = readSize;
	}

	@Test
	public void testContainerLinear_Part1() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Striped/Base-Linear_1.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://7cbb47d0-b04c-42bc-8c04-87b7782739ad", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://951b3e29-6549-4266-8e81-3f88ddba61ae", image.getResourceID());
			IAFF4Map map = image.getMap();
			assertEquals("aff4://2dd04819-73c8-40e3-a32b-fdddb0317eac", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				assertEquals(linearSHA1, getDigest(channel, readSize));
			}
		}
	}
	
	@Test
	public void testContainerLinear_Part2() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Striped/Base-Linear_2.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://51725cd9-3769-4be7-a8ab-94e3ea62bf9a", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://951b3e29-6549-4266-8e81-3f88ddba61ae", image.getResourceID());
			IAFF4Map map = image.getMap();
			assertEquals("aff4://363ac10c-8d8d-4905-ac25-a14aaddd8a41", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				assertEquals(linearSHA1, getDigest(channel, readSize));
			}
		}
	}

	private String getDigest(SeekableByteChannel channel, long readSize) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		ByteBuffer buffer = ByteBuffer.allocateDirect((int) readSize).order(ByteOrder.LITTLE_ENDIAN);
		channel.position(0);
		long length = channel.size();
		long offset = 0;
		while (offset < length) {
			int read = Streams.readFull(channel, offset, buffer);
			buffer.flip();
			md.update(buffer);
			buffer.position(0);
			offset += read;
		}
		return Hex.encodeHexString(md.digest());
	}
}
