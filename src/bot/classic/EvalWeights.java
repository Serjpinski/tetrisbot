package bot.classic;

import java.util.Random;

public class EvalWeights {

	public static final int NUM = 4;
	
	public double[] weights;
	
	public EvalWeights(Random r) {
		
		weights = new double[NUM];
		set(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());
	}
	
	public EvalWeights(double gapW, double avgHeiW, double maxHeiW, double skylineW) {
		
		weights = new double[NUM];
		set(gapW, avgHeiW, maxHeiW, skylineW);
	}
	
	public EvalWeights(double[] weights) {
		
		set(weights);
	}
	
	public void set(double gapW, double avgHeiW, double maxHeiW, double skylineW) {
		
		weights[0] = gapW;
		weights[1] = avgHeiW;
		weights[2] = maxHeiW;
		weights[3] = skylineW;
		normalize();
	}
	
	public void set(double[] weights) {
		
		this.weights = weights;
		normalize();
	}
	
	public void normalize() {
		
		double sum = weights[0] + weights[1] + weights[2] + weights[3];
		
		if (sum > 0) {
			
			weights[0] = weights[0] / sum;
			weights[1] = weights[1] / sum;
			weights[2] = weights[2] / sum;
			weights[3] = weights[3] / sum;
		}
	}
	
	public String toString() {
		
		return String.format("[%.6f, %.6f, %.6f, %.6f]",
				weights[0], weights[1], weights[2], weights[3]);
	}
}