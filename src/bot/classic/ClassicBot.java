package bot.classic;

import java.util.ArrayList;

import core.Grid;
import core.Move;

public class ClassicBot {

	// Learned weights
	//private static double[] weights = new double[] {0.62, 0.10, 0.26, 0.02};
	//private static double[] weights = new double[] {0.60, 0.28, 0.07, 0.05};
	//private static double[] weights = new double[] {0.000000, 0.092270, 0.000000, 0.136056, 0.106642, 0.635045, 0.029987};
	private static double[] weights = new double[] {0.183911, 0.311367, 0.007796, 0.045386, 0.205541, 0.225869, 0.020129};

	/**
	 * Looks for the best move given the current piece, the next one and the grid.
	 */
	public static Move search(boolean[][] grid, int activePiece, int nextPiece,
			double[] weights, int predDepth) {
		
		if (weights == null) weights = ClassicBot.weights;

		ArrayList<Move> moves = Move.getMoves(activePiece, grid);
		Move best = null;
		double bestEval = Double.MAX_VALUE;

		for (int i = 0; i < moves.size(); i++) {

			Move move = moves.get(i);

			// Simulates the move
			move.place(grid);

			Move move2 = search(grid, nextPiece, weights, predDepth);

			if (move2 != null) {

				double eval = move2.getScore();

				if (eval < bestEval) {

					bestEval = eval;
					best = move;
				}
			}

			// Undoes the simulation
			move.remove(grid);
		}

		if (best != null) best.setScore(bestEval);
		return best;
	}

	/**
	 * Looks for the best move given the current piece and the grid, predicting
	 * possible upcoming pieces.
	 */
	public static Move search(boolean[][] grid, int activePiece, double[] weights, int predDepth) {
		
		if (weights == null) weights = ClassicBot.weights;
	
		ArrayList<Move> moves = Move.getMoves(activePiece, grid);
		Move best = null;
		double bestEval = Double.MAX_VALUE;
	
		for (int i = 0; i < moves.size(); i++) {
	
			Move move = moves.get(i);
	
			// Simulates the move
			move.place(grid);
	
			double eval;
	
			if (predDepth == 0) eval = eval(grid, weights);
			else {
	
				double totalEval = 0;
				boolean failed = false;
	
				for (int j = 0; j < 7; j++) {
	
					Move move2 = search(grid, j, weights, predDepth - 1);
	
					if (move2 != null) totalEval += move2.getScore();
					else failed = true;
				}
	
				if (failed) eval = Double.MAX_VALUE;
				else eval = totalEval / 7;
	
				//				eval = 0;
				//
				//				for (int j = 0; j < 7; j++) {
				//
				//					Move move2 = search(predDepth - 1, grid, j, weights);
				//
				//					if (move2 != null) {
				//
				//						if (move2.getScore() > eval) eval = move2.getScore();
				//					}
				//					else eval = Double.MAX_VALUE;
				//				}
			}
	
			if (eval < bestEval) {
	
				bestEval = eval;
				best = move;
			}
	
			// Undoes the simulation
			move.remove(grid);
		}
	
		if (best != null) best.setScore(bestEval);
		return best;
	}


	public static Move search(int[] steps, int activePiece, double[] weights, int predDepth) {
	
		int[] heights = new int[steps.length + 1];
		int minHeight = 0;
	
		heights[0] = 0;
	
		for (int i = 0; i < steps.length; i++) {
	
			heights[i + 1] = heights[i] + steps[i];
			if (heights[i + 1] < minHeight) minHeight = heights[i + 1];
		}
	
		boolean[][] grid = Grid.emptyGrid();
	
		for (int j = 0; j < grid[0].length; j++)
			for (int i = 0; i < heights[j] - minHeight; i++)
				grid[grid.length - i - 1][j] = true;
	
		return search(grid, activePiece, weights, predDepth);
	}

	/**
	 * Computes the score for a grid state, without considering any other data.
	 * Lower score means better grid state.
	 */
	public static double evalOld(boolean[][] grid, double[] weights) {
		
		if (weights == null) weights = ClassicBot.weights;

		double gapScore = 0; // Penalizes the gaps below placed blocks
		double avgHeiScore = 0; // Penalizes the average height
		double maxHeiScore = 0; // Penalizes the maximum height
		double skylineScore = 0; // Rewards convenient height variations

		int height1 = -1;
		int height2 = -1;
		int height3 = -1;

		boolean up1Steps = false; // There are height-1-down-up steps
		boolean down1Steps = false; // There are height-1-up-down steps
		int flatSteps = 0; // Number of flat steps
		int pits = 0; // Number of pits (depth 2 or more)

		for (int j = 0; j < grid[0].length; j++) {

			int blocksAbove = 0; // Number of blocks above the current cell
			int distToBlock = -1; // Distance to the nearest block above the current cell, -1 if none

			height3 = height2;
			height2 = height1;
			height1 = 0;

			for (int i = 0; i < grid.length; i++) {

				if (grid[i][j] == true) {

					blocksAbove++;
					distToBlock = 0;

					if (height1 == 0) height1 = grid.length - i;
				}
				else {

					if (distToBlock != -1) {

						double gapSubscore = 1 + (grid.length - i + blocksAbove) / (2 * grid.length);
						gapSubscore /= 2 * Math.pow(2, distToBlock);
						gapScore += gapSubscore;

						distToBlock++;
					}
				}
			}

			avgHeiScore += Math.pow(height1, 2);

			if (maxHeiScore < height1) maxHeiScore = height1;

			if (height2 != -1) {

				int step = height1 - height2;

				if (step == 1) up1Steps = true;
				else if (step == -1) down1Steps = true;
				else if (step == 0) flatSteps++;
				else if (step > 1 && (j == 1 || height3 - height2 > 1)) pits++;
				else if (step < -1 && j == grid[0].length) pits++;
			}
		}

		// Computes the subscores and normalizes them between 0 and 1
		gapScore = gapScore / (0.25 * grid.length * grid[0].length);
		avgHeiScore = Math.sqrt(avgHeiScore) / (grid[0].length * grid.length);
		maxHeiScore = Math.pow(maxHeiScore, 2) / Math.pow(grid.length, 2);
		skylineScore = (up1Steps ? 0 : 0.2) + (down1Steps ? 0 : 0.2)
				+ (flatSteps == 0 ? 0.3 : (0.1 / flatSteps))
				+ 0.3 - 0.3 / (pits + 1);

		/*System.out.println("gapScore: " + gapScore);
		System.out.println("avgHeiScore: " + avgHeiScore);
		System.out.println("maxHeiScore: " + maxHeiScore);
		System.out.println("skylineScore: " + skylineScore);
		System.out.println("WgapScore: " + gapW * gapScore);
		System.out.println("WavgHeiScore: " + avgHeiW * avgHeiScore);
		System.out.println("WmaxHeiScore: " + maxHeiW * maxHeiScore);
		System.out.println("WskylineScore: " + skylineW * skylineScore);*/

		return weights[0] * gapScore
				+ weights[1] * avgHeiScore
				+ weights[2] * maxHeiScore
				+ weights[3] * skylineScore;
	}

	/**
	 * Computes the score for a grid state, without considering any other data.
	 * Lower score means better grid state.
	 */
	public static double eval(boolean[][] grid, double[] weights) {
		
		if (weights == null) weights = ClassicBot.weights;

		double avgHeight = 0;
		double avgSquaredHeight = 0;
		double maxHeight = 0;
		double gaps = 0;
		double weightedGaps = 0;
		double skylineScore = 0;

		int height1 = -1;
		int height2 = -1;
		int height3 = -1;

		boolean up1Steps = false;
		boolean down1Steps = false;
		int flatSteps = 0;
		int pits = 0;

		for (int j = 0; j < grid[0].length; j++) {

			int blocksAbove = 0;
			int distToBlock = -1;

			height3 = height2;
			height2 = height1;
			height1 = 0;

			for (int i = 0; i < grid.length; i++) {

				if (grid[i][j] == true) {

					blocksAbove++;
					distToBlock = 0;

					if (height1 == 0) height1 = grid.length - i;
				}
				else {

					if (distToBlock != -1) {

						double gapSubscore = 1 + (grid.length - i + blocksAbove) / (2 * grid.length);
						gapSubscore /= 2 * Math.pow(2, distToBlock);
						weightedGaps += gapSubscore;
						gaps++;

						distToBlock++;
					}
				}
			}

			avgHeight += height1;
			avgSquaredHeight += Math.pow(height1, 2);

			if (maxHeight < height1) maxHeight = height1;

			if (height2 != -1) {

				int step = height1 - height2;

				if (step == 1) up1Steps = true;
				else if (step == -1) down1Steps = true;
				else if (step == 0) flatSteps++;
				else if (step > 1 && (j == 1 || height3 - height2 > 1)) pits++;
				else if (step < -1 && j == grid[0].length) pits++;
			}
		}

		// Computes the subscores and normalizes them between 0 and 1
		avgHeight = avgHeight / (grid[0].length * grid.length);
		avgSquaredHeight = Math.sqrt(avgSquaredHeight) / (grid[0].length * grid.length);
		maxHeight = maxHeight / grid.length;
		gaps = gaps / (grid.length * grid[0].length);
		weightedGaps = weightedGaps / (0.25 * grid.length * grid[0].length);
		skylineScore = (up1Steps ? 0 : 0.2) + (down1Steps ? 0 : 0.2)
				+ (flatSteps == 0 ? 0.3 : (0.1 / flatSteps))
				+ 0.3 - 0.3 / (pits + 1);

		/*System.out.println("gapScore: " + gapScore);
		System.out.println("avgHeiScore: " + avgHeiScore);
		System.out.println("maxHeiScore: " + maxHeiScore);
		System.out.println("skylineScore: " + skylineScore);
		System.out.println("WgapScore: " + gapW * gapScore);
		System.out.println("WavgHeiScore: " + avgHeiW * avgHeiScore);
		System.out.println("WmaxHeiScore: " + maxHeiW * maxHeiScore);
		System.out.println("WskylineScore: " + skylineW * skylineScore);*/

		return weights[0] * avgHeight
				+ weights[1] * avgSquaredHeight
				+ weights[2] * maxHeight
				+ weights[3] * Math.pow(maxHeight, 2)
				+ weights[4] * gaps
				+ weights[5] * weightedGaps
				+ weights[6] * skylineScore;
	}
}
