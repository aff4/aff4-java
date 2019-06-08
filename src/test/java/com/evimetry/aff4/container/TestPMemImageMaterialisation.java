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
public class TestPMemImageMaterialisation {

	private final static String micro7sha1 = "deaa3877443908ac4d01ca410ff1012c8ab0db35";
	private final static String micro9sha1 = "2b0496d2ec4189f8190a8c3f8bf92f503706cf96";

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
				512 * 1024 + 1);
	}

	@Rule
	public TestName name = new TestName();

	public TestPMemImageMaterialisation(long readSize) {
		this.readSize = readSize;
	}

	@Test
	public void testContainer7() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Micro7.001.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://2ec3bb84-c9fb-4c4e-beba-add19fe60b99", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://1df01c49-a36a-4770-b6e4-1ef4efe0f4b7", image.getResourceID());
			IAFF4Map map = image.getMap();
			assertEquals("aff4://02928200-4eef-4e8b-b2fb-e8b36f368063", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(262144, channel.size());
				assertEquals(micro7sha1, getDigest(channel, readSize));
			}
		}
	}

	@Test
	public void testContainer9() throws UnsupportedOperationException, IOException, Exception {
		URL url = TestContainer.class.getResource("/Micro9.001.aff4");
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			assertEquals("aff4://80b64e67-39f1-4564-948a-911f06f6343f", container.getResourceID());
			Iterator<IAFF4Image> images = container.getImages();
			assertTrue(images.hasNext());
			IAFF4Image image = images.next();
			assertFalse(images.hasNext());
			assertEquals("aff4://6ead3312-8e9e-4dd8-931c-9d91c2dba63c", image.getResourceID());
			IAFF4Map map = image.getMap();
			assertEquals("aff4://7101b94e-2cde-4ecd-b177-8f542644091d", map.getResourceID());
			try (SeekableByteChannel channel = map.getChannel()) {
				assertEquals(73728, channel.size());
				assertEquals(micro9sha1, getDigest(channel, readSize));
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
