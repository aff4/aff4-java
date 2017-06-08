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
package com.evimetry.aff4.resolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.AFF4Lexicon;
import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Container;
import com.evimetry.aff4.IAFF4Resolver;
import com.evimetry.aff4.IAFF4Resource;
import com.evimetry.aff4.resource.AFF4Resource;

/**
 * A lightweight aff4:Resolver.
 * <p>
 * The resolver interface only supports the query for Volume Resource IDs. (minimum required via IAFF4Resolver
 * interface). Additionally, this will only resolve AFF4 container volumeIDs.
 * <p>
 * This resolver does not track resources that have been opened.
 */
public class LightResolver extends AFF4Resource implements IAFF4Resolver {

	private final static Logger logger = LoggerFactory.getLogger(LightResolver.class);

	/**
	 * The base path.
	 */
	private final File path;

	/**
	 * Map of volumeIDs to filenames
	 */
	protected final Map<String, File> volumes = Collections.synchronizedMap(new ConcurrentHashMap<>());

	/**
	 * Create a new lightweight resolver.
	 * 
	 * @param resource The resource to apply to this resolver
	 * @param path The base path to start scanning for files.
	 */
	public LightResolver(String resource, File path) {
		super(resource);
		this.path = path;
		properties.put(AFF4Lexicon.fileName, Collections.singletonList(path.getAbsolutePath()));
		scanForAFF4Volumes(path.toPath());
	}

	@Override
	public IAFF4Resource open(String urn) {
		if (hasResource(urn)) {
			if(urn.equals(getResourceID())){
				return this;
			}
			File parentFile = null;
			synchronized (volumes) {
				parentFile = volumes.get(urn);
			}
			if (parentFile.exists() && parentFile.canRead()) {
				try {
					IAFF4Container con = Containers.open(parentFile);
					con.setResolver(this);
					return con;
				} catch (Throwable e) {
					logger.warn(e.getMessage(), e);
				}
			} else if (!parentFile.exists()) {
				// resource no longer exists, so remove from known resources.
				synchronized (volumes) {
					volumes.values().remove(parentFile);
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasResource(String urn) {
		if(urn == null || urn.trim().isEmpty()){
			return false;
		}
		synchronized (volumes) {
			if (volumes.containsKey(urn)) {
				// we know of this one...
				return true;
			}
		}
		// rescan hierarchy for new files in case it was added AFTER we were originally mounted.
		scanForAFF4Volumes(path.toPath());
		synchronized (volumes) {
			return volumes.containsKey(urn);
		}
	}

	/**
	 * Scan for AFF4 files in the given Base Path.
	 * 
	 * @param basePath The base path to start scanning from.
	 */
	private void scanForAFF4Volumes(final Path basePath) {
		if (basePath == null) {
			logger.warn("Path is NULL!");
			return;
		}
		File path = basePath.toFile();
		if (!path.exists() || !path.isDirectory() || !path.canRead()) {
			return;
		}
		try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(basePath)) {
			for (Path p : stream) {
				if (p.toFile().isDirectory()) {
					scanForAFF4Volumes(p);
				} else {
					if (Containers.isAFF4Container(p.getFileName().toString())) {
						if (p.toFile().canRead()) {
							synchronized (volumes) {
								// only scan if we don't already have this file in volumes.
								if (!volumes.values().contains(p.toFile())) {
									try {
										String volumeID = Containers.getResourceID(p.toFile());
										if (volumeID != null) {
											volumes.put(volumeID, p.toFile());
										}
									} catch (Exception e) {
										logger.warn(e.getMessage());
									}
								}
							}
						} else {
							logger.info("Skipping {}. No read permission.", p.toAbsolutePath());
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
