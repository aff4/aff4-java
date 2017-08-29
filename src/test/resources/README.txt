Evimetry Standard v1.0 Images

These images are produced using the Evimetry 2.2 AFF4 implementation.

	Base-Linear.aff4				:	Linear image of the entire source device. 
	Base-Linear-AllHashes.aff4		:	Linear image of the entire source device, using (MD5, SHA1, SHA256, SHA512, Blake2b) blockhashes
	Base-Linear-ReadError.aff4		: 	Linear image of an entire source device, where a read error occurred. 
	Base-Allocated.aff4				: 	Linear image of volume & FS metadata, and all allocated blocks in the filesytem.
	SpannedImages\
		Base-Linear_1.aff4			:	Linear image of the entire source device (striped).
		Base-Linear_2.aff4			:	Linear image of the entire source device (striped).

The following Images were produced via unit tests for emulation of Physical Memory acquisition:
	Micro7.001.aff4
	Micro9.001.aff4
	

  
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