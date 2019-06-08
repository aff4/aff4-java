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
package com.evimetry.aff4.map.collection;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Basic unit tests for treap implementation.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LongTreapTest {

	/**
	 * Mock random number generator.
	 */
	static class MockRandom extends Random {
		private static final long serialVersionUID = -8464275634529967884L;
		int[] randoms;
		int current = 0;

		public MockRandom(int[] ints) {
			randoms = ints;
		}

		public int nextInt() {
			int res = randoms[current];
			current = current + 1;
			return res;
		}
	}

	@Test
	public void testDelete() {

		// key: 00009800 prio: 1223151319 node: TSection uoff= 0x9800 loff= 0x9800 len= 0x800
		// Lkey: 00009000 prio: -411449891 node: TSection uoff= 0x9000 loff= 0x9000 len= 0x1000

		int[] randoms = { 1223151319, -411449891 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(randoms));

		treap.put(0x00009800, 3);
		treap.put(0x00009000, 1);
		// System.out.println(treap.toString());
		assertTrue(treap.remove(0x00009800));

		assertEquals(1, (int) treap.get(0x00009000));

	}

	@Test
	public void testDelete2() {

		/*
		 * key: 00009800 prio: 823337390 node: TSection uoff= 0x9800 loff= 0x9800 len= 0x800 Rkey: 00000000 prio:
		 * -2147483648 node: null Lkey: 00009000 prio: 189758945 node: TSection uoff= 0x9000 loff= 0x9000 len= 0x1000
		 * Rkey: 00000000 prio: -2147483648 node: null Lkey: 00000000 prio: -2147483648 node: null
		 */

		int[] randoms = { 823337390, 189758945 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(randoms));

		treap.put(0x00009800, 3);
		treap.put(0x00009000, 1);
		// System.out.println(treap.toString());
		assertFalse(treap.remove(111));
		assertTrue(treap.remove(0x00009800));

		assertEquals(1, (int) treap.get(0x00009000));

	}

	@Test
	public void testEmpty() {
		LongTreap<Integer> treap = new LongTreap<Integer>();
		assertEquals(0, treap.size());
		assertTrue(treap.isEmpty());
	}

	@Test
	public void testSingle() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotSame(treap.tnull, r);
		assertEquals(treap.tnull, r.left);
		assertEquals(treap.tnull, r.right);
		assertEquals(1, treap.size());

		// psearch nothing prior to first entry
		assertNull(treap.psearch(0));

		// psearch for first entry address gives first entry
		assertNotSame(treap.tnull, treap.psearch(1));
		assertEquals(r, treap.psearch(1));

		// psearch for address after first entry gives first entry
		assertNotNull(treap.psearch(2));
		assertEquals(r, treap.psearch(2));

		// nsearch for prior to first gives first
		assertNotNull(treap.nsearch(0));
		assertEquals(r, treap.nsearch(0));

		// nsearch for address of first gives first
		assertNotNull(treap.nsearch(1));
		assertEquals(r, treap.nsearch(1));

		// nsearch of after first gives nothing
		assertNull(treap.nsearch(2));

	}

	@Test
	public void test12a() {
		int[] randoms = { 0, 1, 2 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(randoms));

		treap.put(1, 1);
		treap.put(2, 2);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 2, r.getValue());

		assertEquals(treap.tnull, r.right);
		assertNotNull(r.left);
		assertEquals((Integer) 1, r.left.getValue());

		assertEquals(2, treap.size());

		// nothing prior to first entry
		assertNull(treap.psearch(0));

		// psearch for first entry address gives first entry
		assertNotSame(treap.tnull, treap.psearch(1));
		assertEquals(treap.search(1), treap.psearch(1));

		// psearch for secong entry gives second entry
		assertNotSame(treap.tnull, treap.psearch(2));
		assertEquals(treap.search(2), treap.psearch(2));

		// psearch for address after second entry gives second entry
		assertNotSame(treap.tnull, treap.psearch(3));
		assertEquals(treap.search(2), treap.psearch(3));

		// nsearch for prior to first gives first
		assertNotSame(treap.tnull, treap.nsearch(0));
		assertEquals(treap.search(1), treap.nsearch(0));

		// nsearch for address of first gives first
		assertNotSame(treap.tnull, treap.nsearch(1));
		assertEquals(treap.search(1), treap.nsearch(1));

		// nsearch for address of second gives second
		assertNotSame(treap.tnull, treap.nsearch(2));
		assertEquals(treap.search(2), treap.nsearch(2));

		// nsearch of after first gives nothing
		assertNull(treap.nsearch(3));

	}

	@Test
	public void test21a() {
		int[] randoms = { 2, 1 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(randoms));

		treap.put(2, 2);
		treap.put(1, 1);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 2, r.getValue());

		assertEquals(treap.tnull, r.right);
		assertNotNull(r.left);
		assertEquals((Integer) 1, r.left.getValue());

		assertEquals(2, treap.size());

		// nothing prior to first entry
		assertNull(treap.psearch(0));

		// psearch for first entry address gives first entry
		assertNotNull(treap.psearch(1));
		assertEquals(treap.search(1), treap.psearch(1));

		// psearch for secong entry gives second entry
		assertNotNull(treap.psearch(2));
		assertEquals(treap.search(2), treap.psearch(2));

		// psearch for address after second entry gives second entry
		assertNotNull(treap.psearch(3));
		assertEquals(treap.search(2), treap.psearch(3));

		// nsearch for prior to first gives first
		assertNotNull(treap.nsearch(0));
		assertEquals(treap.search(1), treap.nsearch(0));

		// nsearch for address of first gives first
		assertNotNull(treap.nsearch(1));
		assertEquals(treap.search(1), treap.nsearch(1));

		// nsearch for address of second gives second
		assertNotNull(treap.nsearch(2));
		assertEquals(treap.search(2), treap.nsearch(2));

		// nsearch of after first gives nothing
		assertNull(treap.nsearch(3));
	}

	@Test
	public void test12b() {
		int[] randoms = { 2, 1 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(randoms));

		treap.put(1, 1);
		treap.put(2, 2);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 1, r.getValue());
		assertEquals(treap.tnull, r.left);
		assertNotNull(r.right);
		assertEquals((Integer) 2, r.right.getValue());

		assertEquals(2, treap.size());

		// nothing prior to first entry
		assertNull(treap.psearch(0));

		// psearch for first entry address gives first entry
		assertNotNull(treap.psearch(1));
		assertEquals(treap.search(1), treap.psearch(1));

		// psearch for secong entry gives second entry
		assertNotNull(treap.psearch(2));
		assertEquals(treap.search(2), treap.psearch(2));

		// psearch for address after second entry gives second entry
		assertNotNull(treap.psearch(3));
		assertEquals(treap.search(2), treap.psearch(3));

		// nsearch for prior to first gives first
		assertNotNull(treap.nsearch(0));
		assertEquals(treap.search(1), treap.nsearch(0));

		// nsearch for address of first gives first
		assertNotNull(treap.nsearch(1));
		assertEquals(treap.search(1), treap.nsearch(1));

		// nsearch for address of second gives second
		assertNotNull(treap.nsearch(2));
		assertEquals(treap.search(2), treap.nsearch(2));

		// nsearch of after first gives nothing
		assertNull(treap.nsearch(3));
	}

	@Test
	public void test21b() {
		int[] priorities = { 1, 2 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(priorities));

		treap.put(2, 2);
		treap.put(1, 1);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 1, r.getValue());
		assertEquals(treap.tnull, r.left);
		assertNotNull(r.right);
		assertEquals((Integer) 2, r.right.getValue());

		assertEquals(2, treap.size());

		// nothing prior to first entry
		assertNull(treap.psearch(0));

		// psearch for first entry address gives first entry
		assertNotNull(treap.psearch(1));
		assertEquals(treap.search(1), treap.psearch(1));

		// psearch for secong entry gives second entry
		assertNotNull(treap.psearch(2));
		assertEquals(treap.search(2), treap.psearch(2));

		// psearch for address after second entry gives second entry
		assertNotNull(treap.psearch(3));
		assertEquals(treap.search(2), treap.psearch(3));

		// nsearch for prior to first gives first
		assertNotNull(treap.nsearch(0));
		assertEquals(treap.search(1), treap.nsearch(0));

		// nsearch for address of first gives first
		assertNotNull(treap.nsearch(1));
		assertEquals(treap.search(1), treap.nsearch(1));

		// nsearch for address of second gives second
		assertNotNull(treap.nsearch(2));
		assertEquals(treap.search(2), treap.nsearch(2));

		// nsearch of after first gives nothing
		assertNull(treap.nsearch(3));
	}

	@Test
	public void test134() {
		int[] priorities = { 3, 2, 1 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(priorities));

		treap.put(1, 1);
		treap.put(3, 3);
		treap.put(4, 4);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 1, r.getValue());
		assertEquals(treap.tnull, r.left);
		assertNotNull(r.right);

		assertEquals((Integer) 3, r.right.getValue());
		assertEquals(treap.tnull, r.right.left);
		assertNotNull(r.right.right);

		assertEquals((Integer) 4, r.right.right.getValue());

		Integer lower = treap.findPrevious(2);
		assertEquals(lower, treap.get(1));

		assertEquals(3, treap.size());
	}

	@Test
	public void test134a() {
		int[] priorities = { 1, 3, 1 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(priorities));

		treap.put(1, 1);
		treap.put(3, 3);
		treap.put(4, 4);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 3, r.getValue());
		assertNotNull(r.left);
		assertNotNull(r.right);

		assertEquals((Integer) 4, r.right.getValue());

		assertEquals((Integer) 1, r.left.getValue());

		Integer lower = treap.findPrevious(2);
		assertEquals(lower, treap.get(1));

		assertEquals(3, treap.size());
	}

	@Test
	public void test134b() {
		int[] priorities = { 1, 2, 3 };
		LongTreap<Integer> treap = new LongTreap<Integer>(new MockRandom(priorities));

		treap.put(1, 1);
		treap.put(3, 3);
		treap.put(4, 4);

		LongTreap<Integer>.Node r = treap.getRoot();
		assertNotNull(r);
		assertEquals((Integer) 4, r.getValue());
		assertEquals(treap.tnull, r.right);
		assertNotNull(r.left);

		assertEquals((Integer) 3, r.left.getValue());
		assertEquals(treap.tnull, r.left.right);
		assertNotNull(r.left.left);

		assertEquals((Integer) 1, r.left.left.getValue());

		Integer lower = treap.findPrevious(2);
		assertEquals(lower, treap.get(1));

		assertEquals(3, treap.size());
	}

	@Test
	public void testSearch() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		treap.put(2, 2);
		treap.put(3, 3);

		assertEquals((Integer) 2, treap.get(2));
		assertEquals((Integer) 1, treap.get(1));
		assertEquals((Integer) 3, treap.get(3));

	}

	@Test
	public void testNoLowerBounds() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(3, 3);

		Integer lower = treap.findPrevious(2);
		assertNull(lower);
	}

	@Test
	public void testLowerBounds() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		treap.put(3, 3);

		Integer lower = treap.findPrevious(2);
		assertEquals(lower, treap.get(1));
	}

	@Test
	public void testInBetweenBounds() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		treap.put(3, 3);

		Integer lower = treap.findPrevious(2);
		Integer upper = treap.findNext(2);

		assertEquals(lower, treap.get(1));
		assertEquals(upper, treap.get(3));

	}

	@Test
	public void testInOnBounds() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		treap.put(3, 3);

		Integer lower = treap.findPrevious(1);

		assertEquals(lower, treap.get(1));

	}

	@Test
	public void testOnMap() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		treap.put(2, 1);
		treap.put(3, 3);

		Integer lower = treap.findPrevious(2);

		assertEquals(lower, treap.get(2));
	}

	@Test
	public void testContains() {
		LongTreap<Integer> treap = new LongTreap<Integer>();

		treap.put(1, 1);
		assertTrue(treap.containsKey((long) 1));
		assertFalse(treap.containsKey((long) 2));
	}

}
