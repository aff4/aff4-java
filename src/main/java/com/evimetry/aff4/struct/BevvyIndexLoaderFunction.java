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
package com.evimetry.aff4.struct;

import java.util.function.Function;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.container.AFF4ZipContainer;

/**
 * A bevvy loader for the bevvy cache.
 */
public class BevvyIndexLoaderFunction implements Function<Integer, BevvyIndex> {

	private final static Logger logger = LoggerFactory.getLogger(BevvyIndexLoaderFunction.class);

	/**
	 * The resource we are servicing
	 */
	private final String resource;
	/**
	 * The parent container file
	 */
	private final AFF4ZipContainer parent;
	/**
	 * The parent zip container.
	 */
	private final ZipFile zipContainer;

	/**
	 * Create a bevvy loader for the bevvy cache.
	 * 
	 * @param resource The resource we are servicing
	 * @param parent The parent container file
	 * @param zipContainer The parent zip container.
	 */
	public BevvyIndexLoaderFunction(String resource, AFF4ZipContainer parent, ZipFile zipContainer) {
		this.resource = resource;
		this.parent = parent;
		this.zipContainer = zipContainer;
	}

	@Override
	public BevvyIndex apply(Integer t) {
		try {
			return new BevvyIndex(resource, t, parent, zipContainer);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
