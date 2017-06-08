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
import com.evimetry.aff4.imagestream.ImageStreamFactory;
import com.evimetry.aff4.imagestream.Streams;
import com.evimetry.aff4.map.AFF4Map;

/**
 * Test enumeration of Images from containers, and building a readable channel.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestImageMaterialisation {

	private final static String linearSHA1 = "7d3d27f667f95f7ec5b9d32121622c0f4b60b48d";
	private final static String allocatedSHA1 = "e8650e89b262cf0b4b73c025312488d5a6317a26";
	private final static String readErrorSHA1 = "67e245a640e2784ead30c1ff1a3f8d237b58310f";

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

	public TestImageMaterialisation(long readSize) {
		this.readSize = readSize;
	}

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
			IAFF4Map map = image.getMap();
			assertEquals("aff4://fcbfdce7-4488-4677-abf6-08bc931e195b", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				assertEquals(linearSHA1, getDigest(channel, readSize));
			}
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
			IAFF4Map map = image.getMap();
			assertEquals("aff4://e9cd53d3-b682-4f12-8045-86ba50a0239c", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				assertEquals(allocatedSHA1, getDigest(channel, readSize));
			}
		}
	}

	@Test
	public void testContainerAllocatedUnknown() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Base-Allocated.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://ce24a0d0-a540-442a-939e-938b848add9a", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://8fcced2b-989f-4f51-bfa2-38d4a4d818fe", image.getResourceID());
			IAFF4Map map = image.getMap();
			// Override the aff4:UnknownData stream with aff4:Zero stream
			((AFF4Map) map).setUnknownStreamOverride(ImageStreamFactory.createZeroStream());
			assertEquals("aff4://e9cd53d3-b682-4f12-8045-86ba50a0239c", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				/*
				 * By setting aff4:UnknownData to aff4:Zero, we should get the same exact digest as at the full linear
				 * image. (those areas which are unallocated in the source image contain zeros).
				 */
				assertEquals(linearSHA1, getDigest(channel, readSize));
			}
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
			IAFF4Map map = image.getMap();
			assertEquals("aff4://b282d5f4-333a-4f6a-b96f-0e5138bb18c8", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(268435456, channel.size());
				assertEquals(readErrorSHA1, getDigest(channel, readSize));
			}
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
			IAFF4Map map = image.getMap();
			assertEquals("aff4://2a497fe5-0221-4156-8b4d-176bebf7163f", map.getResourceID());
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
