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

package com.evimetry.aff4.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.evimetry.aff4.AFF4;
import com.evimetry.aff4.AFF4Lexicon;

/**
 * Tests for the registration of compression codecs.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCompressionCodec {

	@Test
	public void testSnappy() {
		testLexicon(AFF4Lexicon.SnappyCompression, SnappyCompression.class);
	}

	@Test
	public void testSnappyResource() {
		testResource(AFF4Lexicon.SnappyCompression, SnappyCompression.class);
	}

	@Test
	public void testLZ4() {
		testLexicon(AFF4Lexicon.LZ4Compression, LZ4Compression.class);
	}

	@Test
	public void testLZ4Resource() {
		testResource(AFF4Lexicon.LZ4Compression, LZ4Compression.class);
	}

	@Test
	public void testDeflate() {
		testLexicon(AFF4Lexicon.DeflateCompression, DeflateCompression.class);
	}

	@Test
	public void testDefalteResource() {
		testResource(AFF4Lexicon.DeflateCompression, DeflateCompression.class);
	}

	@Test
	public void testNull() {
		testLexicon(AFF4Lexicon.NoCompression, NullCompression.class);
	}

	@Test
	public void testNullResource() {
		testResource(AFF4Lexicon.NoCompression, NullCompression.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testBadValue() {
		testLexicon(AFF4Lexicon.SHA1, NullCompression.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testBadValueResource() {
		testResource(AFF4Lexicon.SHA1, NullCompression.class);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNullValue() {
		CompressionCodec.getCodec((AFF4Lexicon) null, AFF4.DEFAULT_CHUNK_SIZE);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNullValueResource() {
		CompressionCodec.getCodec((String) null, AFF4.DEFAULT_CHUNK_SIZE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSnappyZeroChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.SnappyCompression, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSnappyNegativeChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.SnappyCompression, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLZ4ZeroChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.LZ4Compression, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLZ4NegativeChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.LZ4Compression, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeflateZeroChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.DeflateCompression, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeflateNegativeChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.DeflateCompression, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoCompressionZeroChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.NoCompression, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoCompressionNegativeChunkSize() {
		CompressionCodec.getCodec(AFF4Lexicon.NoCompression, -1);
	}

	private void testLexicon(AFF4Lexicon codec, Class<?> class1) {
		CompressionCodec c = CompressionCodec.getCodec(codec, AFF4.DEFAULT_CHUNK_SIZE);
		assertNotNull(c);
		assertEquals(codec.getValue(), c.getResourceID());
		assertEquals(class1, c.getClass());
	}

	private void testResource(AFF4Lexicon codec, Class<?> class1) {
		CompressionCodec c = CompressionCodec.getCodec(codec.getValue(), AFF4.DEFAULT_CHUNK_SIZE);
		assertNotNull(c);
		assertEquals(codec.getValue(), c.getResourceID());
		assertEquals(class1, c.getClass());
	}
}
