package comm.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class containing various useful methods for dealing with arrays.
 * 
 * @author Andrei Purcarus
 *
 */
public final class ArrayUtility {
	/**
	 * Converts the given array to a List.
	 */
	public static final <T> List<T> toList(T[] array) {
		List<T> list = new ArrayList<T>();
		for (T element : array) {
			list.add(element);
		}
		return list;
	}

	/**
	 * Converts the given array of ints to an array of Integer objects.
	 */
	public static final Integer[] toObject(int[] array) {
		Integer[] objectArray = new Integer[array.length];
		for (int i = 0; i < array.length; ++i)
			objectArray[i] = array[i];
		return objectArray;
	}

	/**
	 * Converts the given array of doubles to an array of Double objects.
	 */
	public static final Double[] toObject(double[] array) {
		Double[] objectArray = new Double[array.length];
		for (int i = 0; i < array.length; ++i)
			objectArray[i] = array[i];
		return objectArray;
	}

	/**
	 * Converts the given array of Integers to an array of int primitives.
	 */
	public static final int[] toPrimitive(Integer[] array) {
		int[] primitiveArray = new int[array.length];
		for (int i = 0; i < array.length; ++i)
			primitiveArray[i] = array[i];
		return primitiveArray;
	}

	/**
	 * Converts the given array of Doubles to an array of double primitives.
	 */
	public static final double[] toPrimitive(Double[] array) {
		double[] primitiveArray = new double[array.length];
		for (int i = 0; i < array.length; ++i)
			primitiveArray[i] = array[i];
		return primitiveArray;
	}

	private ArrayUtility() {

	}
}
