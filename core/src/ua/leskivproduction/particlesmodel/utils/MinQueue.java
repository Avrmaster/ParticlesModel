package ua.leskivproduction.particlesmodel.utils;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinQueue<E extends Comparable<E>> {
    private int n;
    private E[] arr;

    public MinQueue() {
        arr = (E[])new Comparable[0];
    }

    public void add(E item) {
        if (++n >= arr.length)
            resize_arr(n*2);

        arr[n] = item;
        swim(n);
    }

    private void swim(int q) {
        while (q>1 && less(q, q/2)) {
            swap(q, q/2);
            q = q/2;
        }
    }
    private void sink(int q) {
        while (2*q <= n) {
            int j = 2*q; //potential smallest son
            if (j<n && less(j+1, j)) j++; //nope, this one

            if (less(q, j))
                break; //i'm smaller than my smallest son. That's right!

            swap(q, j);
            q = j;
        }
    }

    private void swap(int i, int j) {
        E temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    private boolean less(int i, int j) {
        return arr[i].compareTo(arr[j]) < 0;
    }

    public E removeMin() {
        if (n <= 0)
            throw new NoSuchElementException("unable get min from item from empty queue!");

        E min = arr[1];
        arr[1] = null;
        swap(1, n--);
        sink(1);

        if (n <= arr.length/4) {
            resize_arr(arr.length/2);
        }

        return min;
    }
    public E getMin() {
        if (n <= 0)
            throw new NoSuchElementException("unable get min from item from empty queue!");

        return arr[1];
    }

    public int size() {
        return n;
    }

    @Override
    public String toString() {
        return Arrays.toString(arr);
    }

    private void resize_arr(int new_length) {
        E[] newArr = (E[])new Comparable[new_length];
        for (int i = 0; i < Math.min(arr.length, new_length); ++i) {
            newArr[i] = arr[i];
        }
        arr = newArr;
    }

}

class Tester {

    public static void main(String[] args) {
        MinQueue<Integer> queue = new MinQueue<Integer>();

        System.out.println(queue);
        queue.add(3);
        queue.add(4);
        queue.add(7);
        queue.add(6);
        queue.add(2);
        queue.add(8);
        queue.add(9);
        queue.add(10);
        queue.add(1);
        queue.add(0);
        queue.add(-3);

        System.out.println(queue);
        System.out.println("Size: "+queue.size());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue.removeMin());
        System.out.println(queue);
        queue.add(1);
        queue.add(2);
        queue.add(3);
        queue.add(4);
        queue.add(5);
        queue.add(6);
        queue.add(0);
        System.out.println(queue);

    }
}


