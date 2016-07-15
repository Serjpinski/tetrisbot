package core;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Class containing generic purpose methods.
 */
public class Misc {
	
	/**
	 * Generates a string representing the given double.
	 */
	public static String doubleToString(double d) {
		
		return String.format(Locale.US, "%.6f", d);
	}

	/**
	 * Generates a string representing the given array of double.
	 */
	public static String arrayToString(double[] array) {
		
		if (array.length == 0) return "[]";
		
		String string = "[" + doubleToString(array[0]);
		for (int i = 1; i < array.length; i++) string += ", " + doubleToString(array[i]);
		return string + "]";
	}

	/**
	 * Generates a string representing the given ArrayList of double.
	 */
	public static String arrayListToString(ArrayList<Double> array) {
		
		if (array.size() == 0) return "[]";
		
		String string = "[" + doubleToString(array.get(0));
		for (int i = 1; i < array.size(); i++) string += ", " + doubleToString(array.get(i));
		return string + "]";
	}

	/**
	 * Normalizes the array to have sum 1.
	 */
	public static void normalizeArray(double[] array) {
		
		double sum = 0;
		for (int i = 0; i < array.length; i++) sum += array[i];
		if (sum > 0) for (int i = 0; i < array.length; i++) array[i] /= sum;
	}
}
