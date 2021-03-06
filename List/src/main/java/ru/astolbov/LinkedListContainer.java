package ru.astolbov;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@ThreadSafe
public class LinkedListContainer<E> implements SimpleContainer<E>, Iterable<E> {

    @GuardedBy("this")
    private Node<E> first = null;
    @GuardedBy("this")
    private Node<E> last = null;
    @GuardedBy("this")
    private int modCount = 0;
    @GuardedBy("this")
    private int size = 0;

    @Override
    public synchronized void add(E value) {
        Node<E> newNode = new Node<>(value, this.last, null);
        if (this.last != null) {
            this.last.next = newNode;
        } else {
            this.first = newNode;
        }
        this.last = newNode;
        this.modCount++;
        this.size++;
    }

    @Override
    public E get(int index) {
        return getNode(index).element;
    }

    private synchronized Node<E> getNode(int index) {
        checkIndex(index);
        Node<E> res = null;
        int pos = 0;
        for (Node<E> current = this.first; pos <= index & current != null; current = current.next, pos++) {
            res = current;
        }
        return res;
    }

    public synchronized E delete(int index) {
        Node<E> deletedNode = getNode(index);
        if (deletedNode.prev != null) {
            deletedNode.prev.next = deletedNode.next;
        } else {
            this.first = deletedNode.next;
        }
        if (deletedNode.next != null) {
            deletedNode.next.prev = deletedNode.prev;
        } else {
            last = deletedNode.prev;
        }
        size--;
        return deletedNode.element;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iter();
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * @param value element whose presence in this list is to be tested
     * @return true if this list contains the specified element
     */
    public synchronized boolean contains(Object value) {
        boolean res = false;
        for (Node<E> current = this.first; current != null; current = current.next) {
            if (current.element.equals(value)) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * Checks that the index is within the size of the array.
     * @param index
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
    }

    private static class Node<E> {
        E element;
        Node<E> prev;
        Node<E> next;

        public Node(E element, Node<E> prev, Node<E> next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
        }
    }

    public synchronized int getSize() {
        return size;
    }

    private class Iter implements Iterator<E> {
        Node<E> cursor = LinkedListContainer.this.first;
        int fixModCount = LinkedListContainer.this.modCount;

        @Override
        public synchronized boolean hasNext() {
            checkModification();
            return cursor != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E res = cursor.element;
            cursor = cursor.next;
            return res;
        }

        private void checkModification() {
            if (this.fixModCount != LinkedListContainer.this.modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
