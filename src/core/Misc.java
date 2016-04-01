package core;

import java.util.ArrayList;
import java.util.Locale;

public class Misc {
	
	public static String doubleToString(double d) {
		
		return String.format(Locale.US, "%.6f", d);
	}

	public static String arrayToString(double[] array) {
		
		if (array.length == 0) return "[]";
		
		String string = "[" + doubleToString(array[0]);
		for (int i = 1; i < array.length; i++) string += ", " + doubleToString(array[i]);
		return string + "]";
	}

	public static String arrayListToString(ArrayList<Double> array) {
		
		if (array.size() == 0) return "[]";
		
		String string = "[" + doubleToString(array.get(0));
		for (int i = 1; i < array.size(); i++) string += ", " + doubleToString(array.get(i));
		return string + "]";
	}

	public static void normalizeArray(double[] array) {
		
		double sum = 0;
		for (int i = 0; i < array.length; i++) sum += array[i];
		if (sum > 0) for (int i = 0; i < array.length; i++) array[i] /= sum;
	}

	
}
