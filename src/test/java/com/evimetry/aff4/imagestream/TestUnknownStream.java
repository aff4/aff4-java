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
package com.evimetry.aff4.imagestream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;

/**
 * Test for creation and read of aff4:UnknownData
 */
public class TestUnknownStream {

	/**
	 * Test the creation of the stream, ensuring start values are correct.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testCreate() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.UnknownData.getValue(), stream.getResourceID());

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
	public void testWriteFail() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.UnknownData.getValue(), stream.getResourceID());

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
	public void testTruncateFail() throws IOException {
		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		assertNotNull(stream);
		assertEquals(AFF4Lexicon.UnknownData.getValue(), stream.getResourceID());

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
		byte[] expected = "UNKNOWNUNKNOWNUN".getBytes();

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
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
			for (byte b : expected) {
				assertEquals(b, dst.get());
			}
		}
	}

	/**
	 * Test simple 1MB read from the stream
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testRead1MB() throws IOException {
		byte[] expected = "UNKNOWN".getBytes();
		long sz = 1024 * 1024;
		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
			ByteBuffer dst = ByteBuffer.allocateDirect((int) sz).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			assertEquals(sz, read);
			assertEquals(sz, channel.position());
			dst.flip();
			assertEquals(sz, dst.remaining());

			// Now check the contents of the read operation.
			int count = 0;
			while (dst.hasRemaining()) {
				assertEquals(expected[count++ % expected.length], dst.get());
			}
		}
	}

	/**
	 * Test simple 16 byte read from the stream which overlaps a 1MB boundary point.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadOverlap1MB() throws IOException {
		byte[] expected = "KNUNKNOWNUNKNOWN".getBytes();

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());

			channel.position((1024 * 1024) - 2);
			ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			assertEquals(16, read);
			dst.flip();
			assertEquals((1024 * 1024) + 14, channel.position());
			assertEquals(16, dst.remaining());

			// Now check the contents of the read operation.
			for (byte b : expected) {
				assertEquals(b, dst.get());
			}
		}
	}

	/**
	 * Test simple 16 byte read from the stream which overlaps a 1MB boundary point.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadOverlap2MB() throws IOException {
		byte[] expected = "KNUNKNOWNUNKNOWN".getBytes();

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());

			channel.position((2 * 1024 * 1024) - 2);
			ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			dst.flip();
			assertEquals(16, read);
			assertEquals((2 * 1024 * 1024) + 14, channel.position());
			assertEquals(16, dst.remaining());

			// Now check the contents of the read operation.
			for (byte b : expected) {
				assertEquals(b, dst.get());
			}
		}
	}

	/**
	 * Test simple 16 byte read from the stream which overlaps a 1MB boundary point.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadOverlap3MB() throws IOException {
		byte[] expected = "KNUNKNOWNUNKNOWN".getBytes();

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());

			channel.position((3 * 1024 * 1024) - 2);
			ByteBuffer dst = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			dst.flip();
			assertEquals(16, read);
			assertEquals((3 * 1024 * 1024) + 14, channel.position());
			assertEquals(16, dst.remaining());

			// Now check the contents of the read operation.
			for (byte b : expected) {
				assertEquals(b, dst.get());
			}
		}
	}

	/**
	 * Test read attempt with a NULL buffer.
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadNullBuffer() throws IOException {

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
			int read = channel.read(null);
			assertEquals(0, read);
			assertEquals(0, channel.position());
		}
	}

	/**
	 * Test simple empty buffer read from the stream
	 * 
	 * @throws IOException IO operation failure
	 */
	@Test
	public void testReadEmptyBuffer() throws IOException {

		IAFF4ImageStream stream = ImageStreamFactory.createUnknownStream();
		try (SeekableByteChannel channel = stream.getChannel()) {
			assertTrue(channel.isOpen());
			assertEquals(0, channel.position());
			ByteBuffer dst = ByteBuffer.allocateDirect(0).order(ByteOrder.LITTLE_ENDIAN);
			int read = channel.read(dst);
			assertEquals(0, read);
			assertEquals(0, channel.position());
			assertEquals(0, dst.remaining());
		}
	}
}
