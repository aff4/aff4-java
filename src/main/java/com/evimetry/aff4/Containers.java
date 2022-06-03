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
package com.evimetry.aff4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evimetry.aff4.container.AFF4ZipContainer;
import com.evimetry.aff4.resolver.LightResolver;

/**
 * AFF4 Container Factory methods
 */
public class Containers {

	private final static Logger logger = LoggerFactory.getLogger(Containers.class);

	/**
	 * Open the given file as a AFF4 Container.
	 * <p>
	 * The container will be supplied a default Lightweight Resolver to assist in looking for elements outside of it's
	 * own container.
	 * 
	 * @param file The file to open
	 * @return A AFF4 container instance
	 * @throws IOException If the file does not exist or is not readable.
	 * @throws UnsupportedOperationException If the container type is not supported.
	 */
	public static IAFF4Container open(File file) throws IOException, UnsupportedOperationException {
		IAFF4Container container = openContainer(file);
		container.setResolver(createResolver(file));
		return container;
	}

	/**
	 * Open the given file as a AFF4 Container
	 * 
	 * @param file The file to open
	 * @param resolver Set the container to utilise the given AFF4 object resolver to look for objects outside of it's
	 *        own container.
	 * @return A AFF4 container instance
	 * @throws IOException If the file does not exist or is not readable.
	 * @throws UnsupportedOperationException If the container type is not supported.
	 */
	public static IAFF4Container open(File file, IAFF4Resolver resolver)
			throws IOException, UnsupportedOperationException {
		IAFF4Container container = openContainer(file);
		container.setResolver(resolver);
		return container;
	}

	/**
	 * Open the given file as an AFF4 Container
	 * 
	 * @param file The file to open
	 * @return A AFF4 container instance
	 * @throws IOException If the file does not exist or is not readable.
	 */
	private static IAFF4Container openContainer(File file) throws IOException {
		if (!file.exists() || !file.canRead()) {
			throw new IOException("File does not exist or is not readable");
		}
		if (file.isDirectory()) {
			throw new UnsupportedOperationException("AFF4 Folder implementations are not supported in this version");
		}

		String resourceID = getResourceID(file);
		if (resourceID == null || resourceID.isEmpty()) {
			throw new IOException("File does not appear to be an AFF4 File.");
		}
		try {
			return new AFF4ZipContainer(resourceID, file, new ZipFile(file));
		} catch (Throwable e) {
			if (e instanceof IOException) {
				throw e;
			}
			throw new IOException(e);
		}
	}

	/**
	 * Get the resource ID string from the AFF4 container.
	 * <p>
	 * This implementation will use both the comment and contents of the 'container.description' file with the latter
	 * overriding the first if both present.
	 * 
	 * @param file The file to open for the resource string.
	 * @return The found resource ID.
	 */
	public static String getResourceID(File file) {
		/*
		 * We need to use the JRE implementation to get the zip comment, as the Apache-Commons implementation doesn't
		 * give us that option? This is really stupid. but let's live with it. (And we need to use the Apache-Commons
		 * zip implementation, as it gives us the information we need to access the actual raw contents of the zip
		 * container).
		 */
		if (!file.exists() || file.isDirectory() || !file.canRead()) {
			return null;
		}
		String resourceID = null;
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(file, java.util.zip.ZipFile.OPEN_READ)) {
			resourceID = zip.getComment();

			// Now look for container.description file.
			ZipEntry entry = zip.getEntry(AFF4.FILEDESCRIPTOR);
			if (entry != null) {
				try (InputStream is = zip.getInputStream(entry)) {
					resourceID = IOUtils.toString(is, StandardCharsets.UTF_8);
				}
			}
		} catch (ZipException e) {
			logger.error("'" + file.toString() + "' Failed reading '" + AFF4.FILEDESCRIPTOR + "' with error: " + e.getMessage());
			/*
			 * Retry with Apache Commons to read the container.description file.
			 */
			try (ZipFile aZip = new ZipFile(file, "UTF-8", true)) {
				ZipArchiveEntry zae = aZip.getEntry(AFF4.FILEDESCRIPTOR);
				if (zae != null) {
					try (InputStream is = aZip.getInputStream(zae)) {
						resourceID = IOUtils.toString(is, StandardCharsets.UTF_8);
					}
				}
			} catch (IOException e1) {
				logger.error("'" + file.toString() + "' Failed reading '" + AFF4.FILEDESCRIPTOR + "' with error: " + e.getMessage(), e1);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return (resourceID == null) ? null : resourceID.trim();
	}

	/**
	 * Create a new lightweight AFF4 Resolver to allow correct access of striped images.
	 * <p>
	 * If the path points to a file, then the path used will be the parent folder of the file.
	 * 
	 * @param path The path to utilise.
	 * @return A lightweight resolver.
	 * @throws IOException If the file does not exist or is not readable.
	 */
	public static IAFF4Resolver createResolver(File path) throws IOException {
		if (!path.isDirectory()) {
			path = path.getAbsoluteFile().getParentFile();
			if (path == null) {
				throw new IOException("Path does not exist or is not readable");
			}
		}
		if (!path.exists() || !path.canRead()) {
			throw new IOException("Path does not exist or is not readable");
		}
		return new LightResolver(AFF4.generateID(), path);
	}

	/**
	 * Determine if a AFF4 container based on the filename.
	 * 
	 * @param file The file
	 * @return TRUE if the filename suggests a AFF4 container.
	 */
	public static boolean isAFF4Container(String file) {
		final String filename = file.toLowerCase();
		return filename.endsWith(".af4") || filename.endsWith(".aff4");
	}
}
