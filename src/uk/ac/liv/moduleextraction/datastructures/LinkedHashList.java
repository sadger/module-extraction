package uk.ac.liv.moduleextraction.datastructures;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/*
 * == Properties ==
 * - No repeat elements
 * - No adding elements
 * - Constant time removal
 * - Stays in order entered
 */

public class LinkedHashList<E> extends AbstractList<E> {

	private DoubleNode listStart = null;
	private DoubleNode listEnd = null;
	private HashMap<E, DoubleNode> nodes;
	
	public LinkedHashList(Collection<? extends E> collection) {
		nodes = new HashMap<E, DoubleNode>();
		for(E item : collection)
			insert(item);
	}

	private void insert(E item) {
		if(!nodes.containsKey(item)){
			DoubleNode newNode = null;
			if(listStart == null){
				newNode = new DoubleNode(listStart, item);
				listStart = newNode;
				listEnd = listStart;
			}
			else{
				newNode = new DoubleNode(listEnd, item);
				listEnd.next = newNode;
				listEnd = newNode;
			}
			nodes.put(item, newNode);
		}
	
	}

	@Override
	public void clear() {
		listStart = null;
		nodes.clear();
	}

	@Override
	public boolean contains(Object obj) {
		return nodes.containsKey(obj);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return nodes.keySet().containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return size() > 0;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			/* Take the iterator back to the start of the list */
			DoubleNode currentElement = listStart;
			
			@Override
			public boolean hasNext() {
				return currentElement != null;
			}

			@Override
			public E next() {
				E value = currentElement.value;
				currentElement = currentElement.next;
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean remove(Object obj) {
		if(obj == null || !nodes.containsKey(obj)){
			return false;
		}
		else{
			DoubleNode node = nodes.get(obj);
			if(node.equals(listStart)){
				listStart = node.next;
				if(node.next != null)
					node.next.previous = null;
			}
			else if(node.equals(listEnd)){
				node.previous.next = null;
				listEnd = node.previous;
			}
			else{
				node.previous.next = node.next;
				node.next.previous = node.previous;
			}
			nodes.remove(obj);
			return true;
		}
	
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removedAny = false;
		
		return removedAny;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public Object[] toArray() {
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return null;
	}
	
	@Override
	public boolean add(E arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		throw new UnsupportedOperationException();
	}
	
	public void thing() {
		DoubleNode n = listStart;
	    while (n != null)
	      {
	    	 System.out.println(n.value);
	         n = n.next;
	      }
	}

	private class DoubleNode{
		DoubleNode previous;
		E value;
		DoubleNode  next;
		
		public DoubleNode(DoubleNode prev, E value) {
			this.previous = prev;
			this.value = value; 
			this.next = null;
		}
	}
	
	@Override
	public E get(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	public static void main(String[] args) {
		HashSet<Integer> custom = new HashSet<Integer>();
		custom.add(10);
		custom.add(1);
		custom.add(20);
		custom.add(12);
		System.out.println(custom);
		
		ArrayList<Integer> ints = new ArrayList<Integer>(custom);
		System.out.println(ints);
		
		Collections.sort(ints);
		
		
		LinkedHashList<Integer> x = new LinkedHashList<Integer>(ints);
		System.out.println(x);
		System.out.println(x.containsAll(ints));

	}



}
