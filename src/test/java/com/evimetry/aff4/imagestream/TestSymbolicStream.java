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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;

/**
 * Test for creation and read of aff4:Zero and aff4:SymbolicStreamXX
 */
public class TestSymbolicStream {

	/**
	 * Test the creation of the stream, ensuring start values are correct.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testCreateZero() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createZeroStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.Zero.getValue(), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
		}
	}

	/**
	 * Test ensure writes fail
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test(expected = IOException.class)
	public void testWriteFailZero() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createZeroStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.Zero.getValue(), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			channel.write(null);
		}
	}

	/**
	 * Test ensure truncates fail
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test(expected = IOException.class)
	public void testTruncateFailZero() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createZeroStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.Zero.getValue(), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			channel.truncate(0);
		}
	}

	/**
	 * Test simple 16 byte read from the stream
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testRead() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createZeroStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
			ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			assertEquals(16, read);
			assertEquals(16, channel.position());
			dst.flip();
			assertEquals(16, dst.remaining());

			// Now check the contents of the read operation.
			while (dst.hasRemaining()) {
				assertEquals(0, dst.get());
			}
		}
	}

	/**
	 * Test the creation of the stream, ensuring start values are correct.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testCreateSymbolicStream00() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createSymbolicStream(0);
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(0), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
		}
	}

	/**
	 * Test ensure writes fail
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test(expected = IOException.class)
	public void testWriteFailSymbolicStream00() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createSymbolicStream(0);
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(0), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			channel.write(null);
		}
	}

	/**
	 * Test ensure truncates fail
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test(expected = IOException.class)
	public void testTruncateFailSymbolicStream00() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createSymbolicStream(0);
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(0), stream.getResourceID());

		assertEquals(AFF4Lexicon.ImageStream, stream.getProperty(AFF4Lexicon.RDFType).iterator().next());
		assertEquals(Long.MAX_VALUE, stream.getProperty(AFF4Lexicon.size).iterator().next());

		try (SeekableByteChannel channel = stream.getChannel()) {
			assertEquals(Long.MAX_VALUE, channel.size());
			assertTrue(channel.isOpen());
			channel.truncate(0);
		}
	}

	/**
	 * Test simple 16 byte read from the stream
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadSymbolicStream00() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createSymbolicStream(0);
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
			ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			assertEquals(16, read);
			assertEquals(16, channel.position());
			dst.flip();
			assertEquals(16, dst.remaining());

			// Now check the contents of the read operation.
			while (dst.hasRemaining()) {
				assertEquals(0, dst.get());
			}
		}
	}

	/**
	 * Test create via a resource string.
	 */
	@Test
	public void testCreateViaResource() {
		String resource = AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(0);
		SymbolicImageStream stream = new SymbolicImageStream(resource);
		assertNotNull(stream);
		assertEquals(resource, stream.getResourceID());
		assertEquals(0, stream.getSymbol());
	}

	/**
	 * Test create via a resource string. (for all possible values).
	 */
	@Test
	public void testCreateViaResourceAllValues() {
		for (int i = 0; i < 256; i++) {
			String resource = AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(i);
			SymbolicImageStream stream = new SymbolicImageStream(resource);
			assertNotNull(stream);
			assertEquals(resource, stream.getResourceID());
			assertEquals(i, ((int) stream.getSymbol() & 0xff));
		}
	}
	
	/**
	 * Test create via a resource string. (for all possible values).
	 * @throws IOException  IO operation failure
	 */
	@Test
	public void testCreateAndReadAllValues() throws IOException {
		for (int i = 0; i < 256; i++) {
			String resource = AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(i);
			SymbolicImageStream stream = new SymbolicImageStream(resource);
			assertNotNull(stream);
			assertEquals(resource, stream.getResourceID());
			assertEquals(i, ((int) stream.getSymbol() & 0xff));
			
			try (SeekableByteChannel channel = stream.getChannel()) {
				assertTrue(channel.isOpen());
				assertEquals(0, channel.position());
				ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
				int read = channel.read(dst);
				assertEquals(16, read);
				assertEquals(16, channel.position());
				dst.flip();
				assertEquals(16, dst.remaining());

				// Now check the contents of the read operation.
				while (dst.hasRemaining()) {
					assertEquals(i, ((int) dst.get() & 0xff));
				}
			}
		}
	}

}
