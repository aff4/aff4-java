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

package com.evimetry.aff4.map.collection;

import java.util.Random;

/**
 * A treap implementation.
 * <p>
 * This implementation uses Longs as the key for the treap being produced.
 *
 * @param <V> The element to be hosted in this treap.
 */
public class LongTreap<V> {

	/**
	 * Random number generator for node insertion
	 */
	protected final Random random;
	/**
	 * Null node.
	 */
	protected final Node tnull;
	/**
	 * The root node for this treap.
	 */
	protected Node root;
	/**
	 * The size (number of nodes) in this treap.
	 */
	protected int size;

	/**
	 * The last {@link #remove(long)} operation removed a node in the treap.
	 */
	private boolean nodeDeleted = false;

	/**
	 * Internal Node implementation.
	 */
	public class Node {
		/**
		 * The key value for this node
		 */
		private long key;
		/**
		 * The object value for this node.
		 */
		private V value;

		/**
		 * The nodes priority
		 */
		protected int priority;
		/**
		 * The node to the left of this one in the treap.
		 */
		Node left;
		/**
		 * The node to the right of this one in the treap.
		 */
		Node right;

		/**
		 * Create a new treap node with a new random priority
		 * 
		 * @param key The key value for this node
		 * @param value The object value for this node.
		 */
		protected Node(long key, V value) {
			this(key, newRandom(), value);
		}

		/**
		 * Create a new treap node with the given key, value and priority.
		 * 
		 * @param key The key value for this node
		 * @param priority The node priority.
		 * @param value The object value for this node.
		 */
		protected Node(long key, int priority, V value) {
			this.key = key;
			this.value = value;
			this.priority = priority;
			left = tnull;
			right = tnull;
		}

		/**
		 * Get the Key for this node
		 * 
		 * @return The key value
		 */
		protected long getKey() {
			return key;
		}

		/**
		 * Get the object for this node
		 * 
		 * @return The object.
		 */
		protected V getValue() {
			return value;
		}

		@Override
		public String toString() {
			if (key == Long.MIN_VALUE) {
				return String.format("TNULL prio %d", priority);
			}
			return String.format("key: %08x prio: %d node: %s", key, priority, value);
		}

		/**
		 * Consistency check
		 * 
		 * @return TRUE if this node's left/right nodes are consistent.
		 */
		boolean isConsistent() {
			if (this != tnull && (left == this || right == this))
				return false;

			if (left != tnull) {
				if (!left.isConsistent())
					return false;

				if (!(left.priority <= priority))
					return false;
			}
			if (right != tnull) {
				if (!right.isConsistent())
					return false;
				if (!(right.priority <= priority))
					return false;
			}

			return true;
		}
	}

	/**
	 * A NULL node.
	 */
	private class NullNode extends Node {

		public NullNode() {
			super(0, Integer.MIN_VALUE, null);
		}

		public String toString() {
			return String.format("TNULL prio %d", priority);
		}

	}

	/**
	 * Create a new long treap with the default PRNG.
	 */
	public LongTreap() {
		this(new FastRandom());
	}

	/**
	 * Create a new long treap with the given PRNG.
	 * 
	 * @param r The PRNG to utilise for this treap.
	 */
	public LongTreap(Random r) {
		random = r;
		size = 0;
		tnull = new NullNode();
		tnull.left = tnull;
		tnull.right = tnull;
		root = tnull;
	}

	/**
	 * Get the size of the treap
	 * 
	 * @return The number of nodes in the treap
	 */
	public int size() {
		return size;
	}

	/**
	 * Clear all entries in the treap.
	 */
	public void clear() {
		size = 0;
		root = tnull;
	}

	/**
	 * Add the defined item into the treap.
	 * 
	 * @param key The key for the item
	 * @param value The value of the item
	 * @return The previous item, if replacements occurs.
	 */
	public V put(long key, V value) {
		Node old = search(key);
		if (old != null) {
			V oldValue = old.getValue();
			old.value = value;
			return oldValue;
		}
		insert(new Node(key, value));
		size += 1;
		return null;
	}

	/**
	 * Remove the given item from the treap.
	 * 
	 * @param key The key value to remove
	 * @return TRUE if a node was removed from the treap.
	 */
	public boolean remove(long key) {
		nodeDeleted = false;
		root = recTreapDelete(key, root);
		size -= 1;
		return nodeDeleted;
	}

	/**
	 * Determine if the treap contains the given key
	 * 
	 * @param key The key to look for in the treap.
	 * @return TRUE if the treap contains the given value.
	 */
	public boolean containsKey(long key) {
		Node node = search(key);
		return (node != tnull && node != null);
	}

	/**
	 * Get the value for the given key
	 * 
	 * @param key The key to search for.
	 * @return The value for the given key.
	 */
	public V get(long key) {
		Node node = search(key);
		if (node == tnull || node == null)
			return null;
		return node.getValue();
	}

	/**
	 * Find the previous value for the given key.
	 * 
	 * @param key The key to search for.
	 * @return The value for the given key.
	 */
	public V findPrevious(long key) {
		Node res = psearch(key);
		if (res != null)
			return res.getValue();
		else
			return null;
	}

	/**
	 * Find the previous value for the given key.
	 * 
	 * @param key The key to search for.
	 * @return The value for the given key.
	 */
	public V findNext(long key) {
		Node res = nsearch(key);
		if (res != null) {
			return res.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Is this treap empty.
	 * 
	 * @return TRUE is this treap is empty.
	 */
	public boolean isEmpty() {
		return root == tnull;
	}

	/*
	 ************************************************************************************
	 * 
	 * Internal search functions.
	 *
	 ************************************************************************************
	 */

	/**
	 * Search for the previous node
	 * 
	 * @param key The key to search
	 * @return The node for the given key.
	 */
	Node psearch(long key) {
		Node ret = tnull;
		Node cursor = (Node) root;

		while (cursor != tnull) {
			long cmp = key - cursor.getKey();
			if (cmp < 0) {
				cursor = cursor.left;
			} else if (cmp > 0) {
				ret = cursor;
				cursor = cursor.right;
			} else {
				ret = cursor;
				break;
			}
		}
		if (ret == tnull)
			return null;
		return ret;
	}

	/**
	 * Search for the given key
	 * 
	 * @param key The key to search for
	 * @return The node for the given key
	 */
	Node search(long key) {
		Node res = root;
		long cmp;

		while (res != tnull && (cmp = key - res.getKey()) != 0) {
			if (cmp < 0) {
				res = res.left;
			} else {
				res = res.right;
			}
		}
		if (res == tnull || res == null)
			return null;
		return res;
	}

	/**
	 * Search for the given key
	 * @param key The key to search for
	 * @return The node for the given key
	 */
	Node nsearch(long key) {
		Node ret = tnull;
		Node cursor = (Node) root;

		while (cursor != tnull) {
			long cmp = key - cursor.getKey();
			if (cmp < 0) {
				ret = cursor;
				cursor = cursor.left;
			} else if (cmp > 0) {
				cursor = cursor.right;
			} else {
				ret = cursor;
				break;
			}
		}

		if (ret == tnull)
			return null;
		return ret;
	}

	private Node rotateLeft(Node node) {
		Node cursor = node.right;
		node.right = cursor.left;
		cursor.left = node;
		return cursor;
	}

	private Node rotateRight(Node node) {
		Node cursor = node.left;
		node.left = cursor.right;
		cursor.right = node;
		return cursor;
	}

	private Node insertRecurse(Node currentNode, Node insertNode) {
		if (currentNode == tnull) {
			return insertNode;
		} else {
			Node res;
			long cmp = insertNode.getKey() - currentNode.getKey();
			if (cmp < 0) {
				Node left = insertRecurse(currentNode.left, insertNode);
				currentNode.left = left;
				if (left.priority <= currentNode.priority) {
					res = currentNode;
				} else {
					res = rotateRight(currentNode);
				}
			} else {
				Node right = insertRecurse(currentNode.right, insertNode);
				currentNode.right = right;

				if (right.priority <= currentNode.priority) {
					res = currentNode;
				} else {
					res = rotateLeft(currentNode);
				}
			}
			return res;
		}
	}

	private void insert(Node node) {
		root = insertRecurse(root, node);
	}

	private Node recTreapDelete(long key, Node treap) {
		if (treap == tnull) {
			Node res = rootDelete(treap, 1);
			return res;
		}

		if (key < treap.key) {
			Node res = recTreapDelete(key, treap.left);
			if (res != treap.left) {
				treap.left = res;
			}
			return treap;
		} else if (key > treap.key) {
			Node res = recTreapDelete(key, treap.right);
			if (res != treap.right) {
				treap.right = res;
			}
			return treap;
		} else {
			// key == treap.key
			nodeDeleted = true;
			return rootDelete(treap, 1);
		}
	}

	private boolean isLeafOrNull(Node treap) {
		if (treap == tnull)
			return true;
		return (treap.left == treap.right);
	}

	private Node rootDelete(Node treap, int depth) {
		// if leaf or null return null
		if (isLeafOrNull(treap)) {
			return tnull;
		}

		if (treap.left.priority > treap.right.priority) {
			treap = rotateRight(treap);
			treap.right = rootDelete(treap.right, depth + 1);
		} else {
			treap = rotateLeft(treap);
			treap.left = rootDelete(treap.left, depth + 1);
		}
		return treap;

	}
	
	/**
	 * Generate a new random priority. Ensure that the priority isn't the minimum Integer value, as we use that to
	 * represent -Infinity, per the 1996 algorithm.
	 * 
	 * @return The next random value.
	 */
	private final int newRandom() {
		int priority;
		do {
			priority = random.nextInt();
		} while (priority == Integer.MIN_VALUE);

		return priority;
	}
	
	@Override
	public String toString() {
		if (root == tnull)
			return "null\n";
		return toString(root, 0, "");
	}

	private String toString(Node root, int indent, String side) {
		String res = "";

		for (int i = 0; i < indent; i++)
			res = res + "    ";
		res = res + side + root.toString() + "\n";

		if (root != tnull) {
			res = res + toString(root.right, indent + 1, "R");

			res = res + toString(root.left, indent + 1, "L");
		}
		return res;
	}

	public Node getRoot() {
		return root;
	}

}
