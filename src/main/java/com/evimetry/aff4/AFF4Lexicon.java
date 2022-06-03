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

import java.util.HashMap;
import java.util.Map;

/**
 * The default AFF4 Lexicon as published in the AFF4 Standard v1.0 document.
 */
public enum AFF4Lexicon {

	/**
	 * Resource type is Unknown. (No direct mapping of resource name to predefined Lexicon item).
	 */
	UNKNOWN(AFF4.AFF4_BASE_URI + "Unknown"),

	/**
	 * Base RDF type.
	 */
	RDFType("rdf:type"),
	
	/**
	 * Base type for AFF4 Zip64-based containers.
	 */
	ZipVolume(AFF4.AFF4_BASE_URI + "ZipVolume"),
	
	/**
	 * The creation time of the volume.
	 */
	CreationTime(AFF4.AFF4_BASE_URI + "creationTime"),

	/**
	 * Object type is Map.
	 */
	Map(AFF4.AFF4_BASE_URI + "Map"),
	/**
	 * Object type is ImageStream.
	 */
	ImageStream(AFF4.AFF4_BASE_URI + "ImageStream"),

	/**
	 * Object type is Image.
	 */
	Image(AFF4.AFF4_BASE_URI + "Image"),
	/**
	 * The Object is a Contiguous Image. Typically an Image of a disk, volume, tape, etc.
	 */
	ContiguousImage(AFF4.AFF4_BASE_URI + "ContiguousImage"),
	/**
	 * The Object is a Discontiguous Image. Typically an image of Physical Memory.
	 */
	DiscontiguousImage(AFF4.AFF4_BASE_URI + "DiscontiguousImage"),
	/**
	 * The Object is an Image of a Disk.
	 */
	DiskImage(AFF4.AFF4_BASE_URI + "DiskImage"),
	/**
	 * The Object is an Image of a Disk Volume / Partition.
	 */
	VolumeImage(AFF4.AFF4_BASE_URI + "VolumeImage"),
	/**
	 * The Object is an Image of physical memory.
	 */
	MemoryImage(AFF4.AFF4_BASE_URI + "MemoryImage"),

	/**
	 * Defined ImageStream that contains Zeros.
	 */
	Zero(AFF4.AFF4_BASE_URI + "Zero"),
	/**
	 * Defined ImageStream for Unknown Data.
	 */
	UnknownData(AFF4.AFF4_BASE_URI + "UnknownData"),
	/**
	 * Defined ImageStream for UnreadableData.
	 */
	UnreadableData(AFF4.AFF4_BASE_URI + "UnreadableData"),

	/**
	 * Defined ImageStream for SymbolicStream.
	 */
	SymbolicData(AFF4.AFF4_BASE_URI + "SymbolicStream"),

	/**
	 * Snappy Compression
	 */
	SnappyCompression("http://code.google.com/p/snappy/"),

	/**
	 * Deflate Compression
	 */
	DeflateCompression("https://tools.ietf.org/html/rfc1951"),

	/**
	 * LZ4 Compression
	 */
	LZ4Compression("https://code.google.com/p/lz4/"),

	/**
	 * No Compression.
	 */
	NoCompression(AFF4.AFF4_BASE_URI + "NullCompressor"),
	/**
	 * Zlib Compression (defunct)
	 */
	ZlibCompression("https://www.ietf.org/rfc/rfc1950.txt"),

	/*
	 * Object properties.
	 */

	/**
	 * The tool as listed in the version.txt file.
	 */
	Tool(AFF4.AFF4_BASE_URI + "tool"),
	/**
	 * The minor version as listed in the version.txt file.
	 */
	minorVersion(AFF4.AFF4_BASE_URI + "minorVersion"),
	/**
	 * The major version as listed in the version.txt file.
	 */
	majorVersion(AFF4.AFF4_BASE_URI + "majorVersion"),

	/**
	 * The size of the object (aff4:Map and aff4:ImageStream).
	 */
	size(AFF4.AFF4_BASE_URI + "size"),
	/**
	 * The chunk size being utilise in aff4:ImageStream.
	 */
	chunkSize(AFF4.AFF4_BASE_URI + "chunkSize"),
	/**
	 * The number of chunks per segment being utilise in aff4:ImageStream.
	 */
	chunksInSegment(AFF4.AFF4_BASE_URI + "chunksInSegment"),
	/**
	 * The compression method being utilise in aff4:ImageStream.
	 */
	compressionMethod(AFF4.AFF4_BASE_URI + "compressionMethod"),
	/**
	 * The aff4:ImageStream to be used between regions in aff4:Map.
	 */
	mapGapDefaultStream(AFF4.AFF4_BASE_URI + "mapGapDefaultStream"),
	/**
	 * The filename for any segment URI.
	 */
	fileName(AFF4.AFF4_BASE_URI + "fileName"),
	/**
	 * The Map or ImageStream which corresponds to the bytestream of the Image
	 */
	dataStream(AFF4.AFF4_BASE_URI + "dataStream"),
	/**
	 * Child ImageStream’s that are used for block storage
	 */
	dependentStream(AFF4.AFF4_BASE_URI + "dependentStream"),
	/**
	 * Backwards pointer to the parent of this object.
	 */
	target(AFF4.AFF4_BASE_URI + "target"),

	/**
	 * The Volume that the Image Stream or Map is stored in
	 */
	stored(AFF4.AFF4_BASE_URI + "stored"),
	/**
	 * The hash of the stream
	 */
	hash(AFF4.AFF4_BASE_URI + "hash"),

	/**
	 * The SHA1 data type
	 */
	SHA1(AFF4.AFF4_BASE_URI + "SHA1"),
	/**
	 * The SHA256 data type.
	 */
	SHA256(AFF4.AFF4_BASE_URI + "SHA256"),
	/**
	 * The SHA512 data type.
	 */
	SHA512(AFF4.AFF4_BASE_URI + "SHA512"),
	/**
	 * The MD5 data type.
	 */
	MD5(AFF4.AFF4_BASE_URI + "MD5"),
	/**
	 * The Blake2B data type.
	 */
	Blake2b(AFF4.AFF4_BASE_URI + "blake2b"),
	/**
	 * The Blake2B data type. (evimetry legacy).
	 */
	Blake2bEv(AFF4.AFF4_BASE_URI + "Blake2b"),

	/**
	 * Hash of index segment
	 */
	mapIdxHash(AFF4.AFF4_BASE_URI + "mapIdxHash"),
	/**
	 * Hash of point segment
	 */
	mapPointHash(AFF4.AFF4_BASE_URI + "mapPointHash"),
	/**
	 * Hash of mapPath segment
	 */
	mapPathHash(AFF4.AFF4_BASE_URI + "mapPathHash"),
	/**
	 * Hash of full map
	 */
	mapHash(AFF4.AFF4_BASE_URI + "mapHash"),
	/**
	 * Acquisition details
	 */
	acquisitionCompletionState(AFF4.AFF4_BASE_URI + "acquisitionCompletionState"),
	/**
	 * Acquisition type.
	 */
	acquisitionType(AFF4.AFF4_BASE_URI + "acquisitionType"), 
	
	/**
	 * Optional field to define the page size for physical memory acquisitions
	 */
	pageSize(AFF4.AFF4_BASE_URI + "pageSize"), 
	/**
	 * Optional field to define the page table offset for physical memory acquisitions
	 */
	memoryPageTableEntryOffset(AFF4.AFF4_BASE_URI + "memoryPageTableEntryOffset"), 
	/**
	 * Optional field to define the installed memory size for physical memory acquisitions
	 */
	memoryInstalledSize(AFF4.AFF4_BASE_URI + "memoryInstalledSize"), 
	/**
	 * Optional field to define the addressable memory size for physical memory acquisitions
	 */
	memoryAddressableSize(AFF4.AFF4_BASE_URI + "memoryAddressableSize"), 
	/**
	 * Optional field to define the block/sector size for disk related acquisitions
	 */
	blockSize(AFF4.AFF4_BASE_URI + "blockSize"), 
	/**
	 * Optional field to define the device name (as per Operating System) for disk related acquisitions
	 */
	diskDeviceName(AFF4.AFF4_BASE_URI + "diskDeviceName"), 
	/**
	 * Optional field to define the device firmware for disk related acquisitions
	 */
	diskFirmware(AFF4.AFF4_BASE_URI + "diskFirmware"), 
	/**
	 * Optional field to define the device interface type for disk related acquisitions
	 */
	diskInterfaceType(AFF4.AFF4_BASE_URI + "diskInterfaceType"), 
	/**
	 * Optional field to define the device make for disk related acquisitions
	 */
	diskMake(AFF4.AFF4_BASE_URI + "diskMake"), 
	/**
	 * Optional field to define the device model for disk related acquisitions
	 */
	diskModel(AFF4.AFF4_BASE_URI + "diskModel"), 
	/**
	 * Optional field to define the device serial number for disk related acquisitions
	 */
	diskSerial(AFF4.AFF4_BASE_URI + "diskSerial"), 
	/**
	 * Optional field to define the sector count for disk related acquisitions
	 */
	sectorCount(AFF4.AFF4_BASE_URI + "sectorCount"),
	
	/**
	 * Optional RDF type to define case details.
	 */
	CaseDetails(AFF4.AFF4_BASE_URI + "CaseDetails"),
	/**
	 * Optional RDF type to define case description.
	 */
	caseDescription(AFF4.AFF4_BASE_URI + "caseDescription"),
	/**
	 * Optional RDF type to define case name.
	 */
	caseName(AFF4.AFF4_BASE_URI + "caseName"),
	/**
	 * Optional RDF type to define case examiner who performed the acquisition.
	 */
	examiner(AFF4.AFF4_BASE_URI + "examiner"),
	
	
	/* Black Bag Technologies Custom Properties */
	/**
	 * APFS Container type
	 */
	APFSContainerImage(AFF4.BBT_BASE_URI + "APFSContainerImage"),
	/**
	 * APFS Container type
	 */
	APFSContainerType(AFF4.BBT_BASE_URI + "APFSContainerType"),
	/**
	 * APFS Container type (T2)
	 */
	APFST2ContainerType(AFF4.BBT_BASE_URI + "APFST2ContainerType"),
	/**
	 * APFS Container type (Fusion)
	 */
	APFSFusionContainerType(AFF4.BBT_BASE_URI + "APFSFusionContainerType"),
	/**
	 * APFS Container type (Standard disk)
	 */
	APFSStandardContainerType(AFF4.BBT_BASE_URI + "APFSStandardContainerType"),
	/**
	 * Volume/Disk contains extents.
	 */
	ContainsExtents(AFF4.BBT_BASE_URI + "ContainsExtents"),
	/**
	 * Volume/Disk contains unallocated regions (sparse).
	 */
	ContainsUnallocated(AFF4.BBT_BASE_URI + "ContainsUnallocated"),

	/* AFF4-L Properties */
	/**
	 * The original unencoded file path and name of a logical evidence object.
	 */
	originalFileName(AFF4.AFF4_BASE_URI + "originalFileName"),

	/**
	 * The birth time of a file's content and metadata.
 	 */
	birthTime(AFF4.AFF4_BASE_URI + "birthTime"),

	/**
	 * The last modified time of a file's content.
	 */
	lastWritten(AFF4.AFF4_BASE_URI + "lastWritten"),

	/**
	 * The last modified time of a file's filesystem metadata.
	 */
	recordChanged(AFF4.AFF4_BASE_URI + "recordChanged"),

	/**
	 * The last access time of a file's content.
	 */
	lastAccessed(AFF4.AFF4_BASE_URI + "lastAccessed"),

	/**
	 * Folder Class representing a suspect folder.
	 */
	Folder(AFF4.AFF4_BASE_URI + "Folder"),

	/**
	 * Folder Class representing a suspect folder.
	 */
	FolderImage(AFF4.AFF4_BASE_URI + "FolderImage"),

	/**
	 * FileImage Class representing a suspect file.
	 */
	FileImage(AFF4.AFF4_BASE_URI + "FileImage"),

	/**
	 * Property representing the FilesImages contained in a Folder.
	 */
	FilesImages(AFF4.AFF4_BASE_URI + "FilesImages"),

	/**
	 * Class representing a logical acquisition activity.
	 */
	LogicalAcquisitionTask(AFF4.AFF4_BASE_URI + "LogicalAcquisitionTask"),

	/**
	 * Property pointing to a Folder or FileImage which forms the root of an acquisition operation.
	 */
	FilesystemRoot(AFF4.AFF4_BASE_URI + "filesystemRoot");

	/**
	 * Map of all values to activities.
	 */
	private static final Map<String, AFF4Lexicon> typesByValue = new HashMap<String, AFF4Lexicon>();

	/*
	 * Construct a map of all possible values to allow rapid look up to convert values to enumeration.
	 */
	static {
		for (AFF4Lexicon type : AFF4Lexicon.values()) {
			typesByValue.put(type.value, type);
		}
	}

	/**
	 * The resource of the enum.
	 */
	private final String value;

	/**
	 * Create a new enumeration with the given ordinal.
	 * 
	 * @param value The ordinal to assign.
	 */
	private AFF4Lexicon(String value) {
		this.value = value;
	}

	/**
	 * Get the value of the enumeration.
	 * 
	 * @return The value of the enumeration.
	 */
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}

	/**
	 * Get the numeration for the given value
	 * 
	 * @param value The value to derive an enumeration from.
	 * @return A valid enumeration if the value exists, or a generic UNKNOWN operation if the value is unknown.
	 */
	public static AFF4Lexicon forValue(String value) {
		AFF4Lexicon e = typesByValue.get(value);
		return (e != null) ? e : UNKNOWN;
	}
}
