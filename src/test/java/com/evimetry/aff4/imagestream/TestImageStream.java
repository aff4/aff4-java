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

package com.evimetry.aff4.imagestream;

import static org.junit.Assert.assertEquals;
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
import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4ImageStream;
import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.container.TestContainer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestImageStream {

	private final String file_1 = "/Base-Linear.aff4";
	private final String container_1 = "aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044";
	private final String stream_1 = "aff4://c215ba20-5648-4209-a793-1f918c723610";
	private final String streamSHA1_1 = "fbac22cca549310bc5df03b7560afcf490995fbb";

	private final String file_2 = "/Base-Allocated.aff4";
	private final String container_2 = "aff4://ce24a0d0-a540-442a-939e-938b848add9a";
	private final String stream_2 = "aff4://fce3df71-dce8-4a17-af67-36bed58f25c9";
	private final String streamSHA1_2 = "eb6aa5ba18ec68e94ddc9e7a06871127dcafdaa6";

	private final String file_3 = "/Base-Linear-ReadError.aff4";
	private final String container_3 = "aff4://686e3512-b568-48fd-ac7b-73764b98a9aa";
	private final String stream_3 = "aff4://4b4396f1-0b68-4be0-af0f-5bf4667fe27b";
	private final String streamSHA1_3 = "ac152b5b8598aef49f21e6e23e8fdc40d2946a6e";

	private final String file_4 = "/Base-Linear-AllHashes.aff4";
	private final String container_4 = "aff4://7a86cb01-217c-4852-b8e0-c94be1ca5ac5";
	private final String stream_4 = "aff4://e53a108a-bb2e-41f4-ab2e-28fe4ef578c1";
	private final String streamSHA1_4 = "fbac22cca549310bc5df03b7560afcf490995fbb";

	/**
	 * The size of the read to perform.
	 */
	private final long readSize;

	@Parameters(name = "read={0}")
	public static Collection<Object> data() {
		return Arrays.asList( //
				512, //
				571, //
				1024, //
				2048, //
				4096, //
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

	public TestImageStream(long readSize) {
		this.readSize = readSize;
	}
	
	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testLinearImageStreamContents() throws Exception {
		URL url = TestContainer.class.getResource(file_1);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(container_1, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;
			testStreamContentsRead(con.getImageStream(stream_1), streamSHA1_1, readSize);
		}
	}

	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testAllocatedImageStreamContents() throws Exception {
		URL url = TestContainer.class.getResource(file_2);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(container_2, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;
			testStreamContentsRead(con.getImageStream(stream_2), streamSHA1_2, readSize);
		}
	}

	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testReadErrorImageStreamContents() throws Exception {
		URL url = TestContainer.class.getResource(file_3);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(container_3, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;
			testStreamContentsRead(con.getImageStream(stream_3), streamSHA1_3, readSize);
		}
	}

	/**
	 * Test for accessing the a map index entry
	 * 
	 * @throws Exception something went wrong.
	 */
	@Test
	public void testAllHashsImageStreamContents() throws Exception {
		URL url = TestContainer.class.getResource(file_4);
		File file = Paths.get(url.toURI()).toFile();
		try (IAFF4Container container = Containers.open(file)) {
			// Confirm we have the correct container.
			assertEquals(container_4, container.getResourceID());
			assertTrue(container instanceof AFF4ZipContainer);

			@SuppressWarnings("resource")
			AFF4ZipContainer con = (AFF4ZipContainer) container;
			testStreamContentsRead(con.getImageStream(stream_4), streamSHA1_4, readSize);
		}
	}

	/**
	 * Read the contents of the given segment, and compare to a sha1 of the contents.
	 * 
	 * @param segment The segment to read
	 * @param sha1 The expected sha1 of the contents.
	 * @param readSize The size of reads to perform.
	 * @throws IOException IO Failed.
	 * @throws NoSuchAlgorithmException
	 */
	private void testStreamContentsRead(IAFF4ImageStream segment, String sha1, long readSize)
			throws IOException, NoSuchAlgorithmException {
		Collection<Object> rdfType = segment.getProperty(AFF4Lexicon.RDFType);
		assertEquals(AFF4Lexicon.ImageStream, rdfType.iterator().next());
		try (SeekableByteChannel channel = segment.getChannel()) {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			ByteBuffer buffer = ByteBuffer.allocateDirect((int)readSize).order(ByteOrder.LITTLE_ENDIAN);
			long length = channel.size();
			while (length > channel.position()) {
				readFully(channel, buffer);
				buffer.flip();
				md.update(buffer);
				buffer.position(0);
			}
			String result = Hex.encodeHexString(md.digest());
			assertEquals(sha1, result);
		}
	}

	/**
	 * Read fully from the channel into the buffer
	 * 
	 * @param channel The channel to read from
	 * @param buffer The buffer to read into
	 * @throws IOException If reading failed.
	 */
	private void readFully(SeekableByteChannel channel, ByteBuffer buffer) throws IOException {
		int toRead = buffer.remaining();
		while (toRead != 0) {
			int read = channel.read(buffer);
			if (read <= 0) {
				break;
			}
			toRead -= read;
		}
	}
}
