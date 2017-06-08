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
public class TestDeflateCompression {

	private final String srcText = "alksjdfwienflkdfasdfasfasdfasdfasdfadfasdflka jflaskjadflk;jas ;lkdfjlaskdfjlaskdjflkalksjdfwienflkdfasdfasfasdfasdfasdalksjdfwi";
	private final int blockLength = srcText.length();

	@Test
	public void testDeflate() throws IOException {

		ByteBuffer source = ByteBuffer.allocateDirect(blockLength).order(ByteOrder.LITTLE_ENDIAN);
		source.put(srcText.getBytes());
		source.position(0);
		
		ByteBuffer destination = ByteBuffer.allocateDirect(blockLength * 2).order(ByteOrder.LITTLE_ENDIAN);
		
		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);
		compressor.setInput(srcText.getBytes());
		compressor.finish();
		
		byte[] d = new byte[blockLength * 2];
		int res = compressor.deflate(d);
		compressor.end();
		destination.put(d);
		destination.position(0);
		destination.limit(res);

		// Test our decompression.
		CompressionCodec c = CompressionCodec.getCodec(AFF4Lexicon.DeflateCompression, blockLength);
		ByteBuffer dec = c.decompress(destination);
		
		// Ensure our source buffers position is unchanged.
		assertEquals(0, destination.position());
		assertEquals(0, dec.position());
		assertEquals(128, dec.remaining());
		assertTrue(dec.compareTo(source) == 0);
	}
	
}
