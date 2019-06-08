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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;

import org.junit.Test;

import com.evimetry.aff4.AFF4Lexicon;

/**
 * Test the Deflate Codec.
 */
public class TestZlibCompression {

	private final String srcText = "alksjdfwienflkdfasdfasfasdfasdfasdfadfasdflka jflaskjadflk;jas ;lkdfjlaskdfjlaskdjflkalksjdfwienflkdfasdfasfasdfasdfasdalksjdfwi";
	private final int blockLength = srcText.length();

	@Test
	public void testDeflate() throws IOException {

		ByteBuffer source = ByteBuffer.allocateDirect(blockLength).order(ByteOrder.LITTLE_ENDIAN);
		source.put(srcText.getBytes());
		source.position(0);
		
		ByteBuffer destination = ByteBuffer.allocateDirect(blockLength * 2).order(ByteOrder.LITTLE_ENDIAN);
		
		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, false);
		compressor.setInput(srcText.getBytes());
		compressor.finish();
		
		byte[] d = new byte[blockLength * 2];
		int res = compressor.deflate(d);
		compressor.end();
		destination.put(d);
		destination.position(0);
		destination.limit(res);

		// Test our decompression.
		CompressionCodec c = CompressionCodec.getCodec(AFF4Lexicon.ZlibCompression, blockLength);
		ByteBuffer dec = c.decompress(destination);
		
		// Ensure our source buffers position is unchanged.
		assertEquals(0, destination.position());
		assertEquals(0, dec.position());
		assertEquals(128, dec.remaining());
		assertTrue(dec.compareTo(source) == 0);
	}
	
}
