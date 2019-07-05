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

package com.evimetry.aff4.resolver;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.evimetry.aff4.Containers;
import com.evimetry.aff4.IAFF4Resolver;
import com.evimetry.aff4.container.TestContainer;

/**
 * Tests of the resolver.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLightResolver {

	/**
	 * Test for base folder with our sample images
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test
	public void testBaseFolder() throws Exception {
		URL url = TestContainer.class.getResource("/");
		File file = Paths.get(url.toURI()).toFile();
		IAFF4Resolver resolver = Containers.createResolver(file);
		assertTrue(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));
		assertTrue(resolver.hasResource("aff4://ce24a0d0-a540-442a-939e-938b848add9a"));
		assertTrue(resolver.hasResource("aff4://686e3512-b568-48fd-ac7b-73764b98a9aa"));
		assertTrue(resolver.hasResource("aff4://7a86cb01-217c-4852-b8e0-c94be1ca5ac5"));
		assertFalse(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77045"));
	}

	/**
	 * Test for base folder with our sample images
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test
	public void testBaseFolderFile() throws Exception {
		URL url = TestContainer.class.getResource("/Base-Linear.aff4");
		File file = Paths.get(url.toURI()).toFile();
		IAFF4Resolver resolver = Containers.createResolver(file);
		assertTrue(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));
		assertTrue(resolver.hasResource("aff4://ce24a0d0-a540-442a-939e-938b848add9a"));
		assertTrue(resolver.hasResource("aff4://686e3512-b568-48fd-ac7b-73764b98a9aa"));
		assertTrue(resolver.hasResource("aff4://7a86cb01-217c-4852-b8e0-c94be1ca5ac5"));
		assertFalse(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77045"));
	}

	/**
	 * Test for base folder with our sample striped images
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test
	public void testStripedFolder() throws Exception {
		URL url = TestContainer.class.getResource("/Striped");
		File file = Paths.get(url.toURI()).toFile();
		IAFF4Resolver resolver = Containers.createResolver(file);
		assertFalse(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));
		assertTrue(resolver.hasResource("aff4://7cbb47d0-b04c-42bc-8c04-87b7782739ad"));
		assertTrue(resolver.hasResource("aff4://51725cd9-3769-4be7-a8ab-94e3ea62bf9a"));
	}

	/**
	 * Test for base folder with our sample striped images
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test
	public void testStripedFolderFile() throws Exception {
		URL url = TestContainer.class.getResource("/Striped/Base-Linear_1.aff4");
		File file = Paths.get(url.toURI()).toFile();
		IAFF4Resolver resolver = Containers.createResolver(file);
		assertFalse(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));
		assertTrue(resolver.hasResource("aff4://7cbb47d0-b04c-42bc-8c04-87b7782739ad"));
		assertTrue(resolver.hasResource("aff4://51725cd9-3769-4be7-a8ab-94e3ea62bf9a"));
	}

	/**
	 * Test for empty folder.
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test
	public void testEmptyFolder() throws Exception {
		URL url = TestContainer.class.getResource("/Empty");
		File file = Paths.get(url.toURI()).toFile();
		IAFF4Resolver resolver = Containers.createResolver(file);
		assertFalse(resolver.hasResource("aff4://685e15cc-d0fb-4dbc-ba47-48117fc77044"));
	}

	/**
	 * Test for missing folder.
	 * 
	 * @throws Exception If the location is missing or unable to be read.
	 */
	@Test(expected = IOException.class)
	public void testNotExistFolder() throws Exception {
		URL url = TestContainer.class.getResource("/Striped");
		File file = Paths.get(url.toURI() + "2").toFile();
		Containers.createResolver(file);
	}
}
