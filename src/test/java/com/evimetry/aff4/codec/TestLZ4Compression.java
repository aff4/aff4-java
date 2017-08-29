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
package com.evimetry.aff4.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

/**
 * Test the LZ4 Codec.
 */
public class TestLZ4Compression {

	private final String srcText = "alksjdfwienflkdfasdfasfasdfasdfasdfadfasdflka jflaskjadflk;jas ;lkdfjlaskdfjlaskdjflkalksjdfwienflkdfasdfasfasdfasdfasdalksjdfwi";
	private final int blockLength = srcText.length();

	@Test
	public void testLZ4() throws IOException {
		LZ4Factory lz4factory = LZ4Factory.fastestJavaInstance();
		LZ4Compressor compressor = lz4factory.fastCompressor();
		
		ByteBuffer source = ByteBuffer.allocateDirect(blockLength).order(ByteOrder.LITTLE_ENDIAN);
		source.put(srcText.getBytes());
		source.position(0);

		ByteBuffer destination = ByteBuffer.allocateDirect(blockLength * 2).order(ByteOrder.LITTLE_ENDIAN);
		compressor.compress(source, destination);
		destination.flip();
		source.position(0);
		assertEquals(83, destination.remaining());

		// Test our decompression.
		CompressionCodec c = CompressionCodec.getCodec(AFF4Lexicon.LZ4Compression, blockLength);
		ByteBuffer dec = c.decompress(destination);
		
		// Ensure our source buffers position is unchanged.
		assertEquals(0, destination.position());
		
		assertEquals(0, dec.position());
		assertEquals(128, dec.remaining());
		assertTrue(dec.compareTo(source) == 0);
	}
	
}
