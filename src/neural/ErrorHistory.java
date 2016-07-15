package neural;

import java.util.ArrayList;

/**
 * Class that handles the error data from an automated test.
 */
public class ErrorHistory {

	private ArrayList<Integer> errors;
	private ArrayList<Integer> moves;
	private int totalErrors;
	private int totalMoves;
	
	public ErrorHistory() {
		
		errors = new ArrayList<Integer>();
		moves = new ArrayList<Integer>();
		totalErrors = 0;
		totalMoves = 0;
	}
	
	/**
	 * Adds a move to the log.
	 */
	public void addMove(int time, boolean error) {
		
		while (moves.size() < time) {
			
			moves.add(0);
			errors.add(0);
		}
		
		moves.set(time - 1, moves.get(time - 1) + 1);
		
		if (error) {
			
			errors.set(time - 1, errors.get(time - 1) + 1);
			totalErrors++;
		}
		
		totalMoves++;
	}
	
	/**
	 * Computes the error ratios.
	 */
	public double[] getErrorRatios() {
		
		double[] ratios = new double[moves.size()];
		
		for (int i = 0; i < ratios.length; i++)
			ratios[i] = errors.get(i) / (double) moves.get(i);
		
		return ratios;
	}
	
	/**
	 * Computes the global ratio.
	 */
	public double getGlobalRatio() {
		
		return totalErrors / (double) totalMoves;
	}
	
	/**
	 * Returns the total errors.
	 */
	public int getTotalErrors() {
		
		return totalErrors;
	}
}
