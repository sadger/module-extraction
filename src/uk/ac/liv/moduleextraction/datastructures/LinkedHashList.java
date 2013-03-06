package uk.ac.liv.moduleextraction.datastructures;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

/*
 * == Properties ==
 * - Doubly linked
 * - Constant time removal/addition
 * - Stays in order entered
 */

public class LinkedHashList<E> extends AbstractSequentialList<E> {

	private DoubleNode listStart = null;
	private DoubleNode listEnd = null;
	private HashMap<E, DoubleNode> nodes;

	public LinkedHashList(Collection<? extends E> collection) {
		nodes = new HashMap<E, DoubleNode>(collection.size());
		for(E item : collection)
			add(item);
	}

	public LinkedHashList() {
		nodes = new HashMap<E, DoubleNode>();
	}
	
	@Override
	public boolean add(E item) {
//		if(size() >= 1230 && size() <=1240)
//			System.out.println("Adding " + item);
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
			return true;
		}
		System.out.println("Not adding " + item);
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		boolean result = false;
		for(E e : arg0){
			result |= add(e);
		}
		return result;
	}

	
	public E getFirst(){
		return listStart.value;
	}
	
	public E getLast(){
		return listEnd.value;
	}

	@Override
	public void clear() {
		listStart = null;
		nodes.clear();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof LinkedHashList<?>){
			LinkedHashList<?> list = (LinkedHashList<?>) o;
			if(list.size() != size())
				return false;
			else
				return super.equals(o);
		}
		return super.equals(o);
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
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
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
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Iterator<?> i = c.iterator(); i.hasNext();){
				modified |= remove(i.next());
		} 
		return modified;
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
	public ListIterator<E> listIterator(int index) {
		return new LinkedIterator(index);
	}
	

	private class LinkedIterator implements ListIterator<E>{
		private DoubleNode nextNode;
		private int nextIndex;
		
		public LinkedIterator(int index) {
			int size = size();
			
			if(index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index:" + index + ", Size:" + size);
			
			/* If the index is less than half way along */
//			if (index < (size >> 1)) {
				 nextNode = listStart;
				 /*Start from front end to find item */
				 for (nextIndex=0; nextIndex<index; nextIndex++){
					 nextNode = nextNode.next;
				 }
//			}
//			else{
//				/* Otherwise start at back end */
//				nextNode = listEnd;
//				for (nextIndex=size; nextIndex>=index; nextIndex--){
//					 nextNode = nextNode.previous;
//				}
//			}
		}
		
		@Override
		public boolean hasNext() {
			return nextNode != null;
		}

		@Override
		public boolean hasPrevious() {
			return nextNode.previous != null;
		}

		@Override
		public E next() {
			E value = nextNode.value;
			nextNode = nextNode.next;
			nextIndex++;
			return value;
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public E previous() {
			nextNode = nextNode.previous;
			E value = nextNode.value;
			return value;
		}

		@Override
		public int previousIndex() {
			return nextIndex-1;
		}
		
		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
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
	







}
