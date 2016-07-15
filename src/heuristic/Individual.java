package heuristic;

import core.Misc;

/**
 * Class containing all the information of an individual.
 */
public class Individual {

	public double[] weights;
	public double eval;
	public int age;
	
	public Individual(double[] weights, double eval, int age) {
		
		this.weights = weights;
		this.eval = eval;
		this.age = age;
	}
	
	public String toString() {
		
		return Misc.arrayToString(weights) + " = " + Misc.doubleToString(eval) + " (age " + age + ")";
	}
}