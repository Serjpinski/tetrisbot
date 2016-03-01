package bot.classic;

import core.Misc;

public class EvalInd {

	public double[] weights;
	public double eval;
	public int age;
	
	public EvalInd(double[] weights, double eval, int age) {
		
		this.weights = weights;
		this.eval = eval;
		this.age = age;
	}
	
	public String toString() {
		
		return Misc.arrayToString(weights) + " = " + Misc.doubleToString(eval) + " (age " + age + ")";
	}
}