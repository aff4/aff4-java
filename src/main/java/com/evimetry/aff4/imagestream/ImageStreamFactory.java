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

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.IAFF4ImageStream;

/**
 * Factory helper methods for creation of common Image Streams
 */
public class ImageStreamFactory {

	/**
	 * Create an aff4:Zero Image Stream
	 * 
	 * @return the constructed image stream.
	 */
	public static IAFF4ImageStream createZeroStream() {
		return new SymbolicImageStream(AFF4Lexicon.Zero.getValue(), (byte) 0);
	}

	/**
	 * Create an aff4:UnknownData Image Stream
	 * 
	 * @return the constructed image stream.
	 */
	public static IAFF4ImageStream createUnknownStream() {
		return new RepeatedImageStream(AFF4Lexicon.UnknownData.getValue());
	}
	
	/**
	 * Create an aff4:UnknownData Image Stream
	 * 
	 * @return the constructed image stream.
	 */
	public static IAFF4ImageStream createUnknownStream(String resource) {
		return new RepeatedImageStream(resource, "UNKNOWN");
	}

	/**
	 * Create an aff4:UnreadableData Image Stream
	 * 
	 * @return the constructed image stream.
	 */
	public static IAFF4ImageStream createUnreadableStream() {
		return new RepeatedImageStream(AFF4Lexicon.UnreadableData.getValue());
	}

	/**
	 * Create an aff4:SymbolicStreamXX Image Stream
	 * 
	 * @param symbol The symbol for the image stream
	 * @return the constructed image stream.
	 */
	public static IAFF4ImageStream createSymbolicStream(int symbol) {
		return new SymbolicImageStream(AFF4Lexicon.SymbolicData.getValue() + Integer.toHexString(symbol),
				(byte) symbol);
	}
}
