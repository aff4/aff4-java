/*
  This file is part of AFF4 Java.

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
package com.evimetry.aff4.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class NameCodec {

	/**
	 * Correctly encode the name URL for zip containers.
	 * 
	 * @param url The URL to encode
	 * @return The encoded url
	 */
	public static final String encode(String url) {
		return url.replaceAll("aff4://", "aff4%3A%2F%2F");
	}

	/**
	 * Decode the path URL
	 * 
	 * @param path The path string to decode
	 * @return The decoded path.
	 */
	public static final String decode(String path) {
		try {
			return URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return path;
		}
	}

	/**
	 * Attempt to sanitise the given resource string
	 *
	 * @param res The resource string to sanitise
	 * @param resourceID The resource ID.
	 * @return The sanitised resource string.
	 */
	public static final String SanitizeResource(String res, String resourceID) {
		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		var encodedResourceID = NameCodec.encode(resourceID);
		if (res.startsWith(resourceID)) {
			res = res.substring(resourceID.length());
		} else if (res.startsWith(encodedResourceID)) {
			res = res.substring(encodedResourceID.length());
		}
		// Convert any "aff4://" characters to "aff4%3A%2F%2F"
		res = NameCodec.encode(res);
		// strip any leading "/"
		while (res.startsWith("/")) {
			res = res.substring(1);
		}
		return res;
	}
}
