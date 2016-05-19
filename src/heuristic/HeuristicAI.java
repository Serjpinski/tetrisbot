package heuristic;

import java.util.ArrayList;

import core.Grid;
import core.Move;

public class HeuristicAI {

	// Learned weights
	//public static double[] weights = new double[] {0.60, 0.28, 0.07, 0.05};
	public static double[] weightsReduced = new double[] {0.095728, 0.016116, 0.044371, 0.172680, 0.592818, 0.000262, 0.000262, 0.003391, 0.000676, 0.018473, 0.055223};
	public static double[] weightsFull = new double[] {0.046319, 0.013574, 0.044530, 0.309644, 0.411024, 0.000156, 0.000156, 0.039325, 0.001695, 0.019479, 0.114099};

	public static Move search(boolean[][] grid, int activePiece, int nextPiece,
			double[] weights, int predDepth) {

		return search(false, grid, activePiece, nextPiece, weights, predDepth);
	}

	public static Move search(int[] steps, int activePiece, int nextPiece,
			double[] weights, int predDepth) {

		return search(true, Grid.getGrid(steps), activePiece, nextPiece, weights, predDepth);
	}

	private static Move search(boolean reduced, boolean[][] grid, int activePiece, int nextPiece,
			double[] weights, int predDepth) {

		ArrayList<Move> moves = Move.getMoves(activePiece, grid);
		Move best = null;
		double bestEval = Double.MAX_VALUE;

		for (int i = 0; i < moves.size(); i++) {

			Move move = moves.get(i);

			// Simulates the move
			move.place(grid);

			Move move2 = search(reduced, grid, nextPiece, weights, predDepth);

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

	public static Move search(boolean[][] grid, int activePiece,
			double[] weights, int predDepth) {

		return search(false, grid, activePiece, weights, predDepth);
	}

	public static Move search(int[] steps, int activePiece,
			double[] weights, int predDepth) {

		return search(true, Grid.getGrid(steps), activePiece, weights, predDepth);
	}

	private static Move search(boolean reduced, boolean[][] grid, int activePiece,
			double[] weights, int predDepth) {

		ArrayList<Move> moves = Move.getMoves(activePiece, grid);
		Move best = null;
		double bestEval = Double.MAX_VALUE;

		for (int i = 0; i < moves.size(); i++) {

			Move move = moves.get(i);

			// Simulates the move
			move.place(grid);

			double eval;

			if (predDepth == 0) eval = reduced ? evalReduced(grid, weights) : evalFull(grid, weights);
			else {

				double totalEval = 0;
				boolean failed = false;

				for (int j = 0; j < 7; j++) {

					Move move2 = search(reduced, grid, j, weights, predDepth - 1);

					if (move2 != null) totalEval += move2.getScore();
					else failed = true;
				}

				if (failed) eval = Double.MAX_VALUE;
				else eval = totalEval / 7;
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

	public static double eval4(boolean[][] grid, double[] weights) {

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

		return weights[0] * gapScore
				+ weights[1] * avgHeiScore
				+ weights[2] * maxHeiScore
				+ weights[3] * skylineScore;
	}

	public static double eval15(boolean[][] grid, double[] weights) {

		double avgHeight = 0;
		double avgSquaredHeight = 0;
		double heightVar = 0;
		double squaredHeightVar = 0;
		double maxHeight = 0;
		double gaps = 0;
		double weightedGaps = 0;
		double weightedGaps2 = 0;
		double up1Steps = 1;
		double down1Steps = 1;
		double flatSteps = 1;
		double pits22 = 0;
		double pits23 = 0;
		double pits33 = 0;

		int height1 = -1;
		int height2 = -1;
		int height3 = -1;

		for (int j = 0; j < grid[0].length; j++) {

			boolean lastWasBlock = false;
			int blocksAbove = 0;
			int ceilWidth = 0;
			int distToLastBlock = -1;

			height3 = height2;
			height2 = height1;
			height1 = 0;

			for (int i = 0; i < grid.length; i++) {

				if (grid[i][j] == true) {

					blocksAbove++;
					distToLastBlock = 0;

					if (height1 == 0) height1 = grid.length - i;

					if (lastWasBlock) ceilWidth++;
					else ceilWidth = 1;

					lastWasBlock = true;
				}
				else {

					if (distToLastBlock != -1) {

						weightedGaps += (1 + (grid.length - i + blocksAbove) / (2 * grid.length))
								/ (2 * Math.pow(2, distToLastBlock));

						gaps++;

						distToLastBlock++;
					}

					if (ceilWidth > 0) weightedGaps2 += ceilWidth;

					lastWasBlock = false;
				}
			}

			avgHeight += height1;
			avgSquaredHeight += Math.pow(height1, 2);

			if (maxHeight < height1) maxHeight = height1;

			if (height2 != -1) {

				int step = height1 - height2;

				heightVar += Math.abs(step);
				squaredHeightVar += Math.pow(step, 2);

				if (step == 1) up1Steps = 0;
				else if (step == -1) down1Steps = 0;
				else if (step == 0) flatSteps = 0;
				else if (step == 2) {

					if (j == 1) pits23++;
					else if (height3 - height2 == 2) pits22++;
					else if (height3 - height2 == 3) pits23++;
				}
				else if (step > 2) {

					if (j == 1) pits33++;
					else if (height3 - height2 == 2) pits23++;
					else if (height3 - height2 == 3) pits33++;
				}
				else if (j == grid[0].length) {

					if (step == -2) pits23++;
					else pits33++;
				}
			}
		}

		avgHeight /= (grid[0].length * grid.length);
		avgSquaredHeight = Math.sqrt(avgSquaredHeight) / (grid[0].length * grid.length);
		heightVar /= (3 * grid[0].length);
		squaredHeightVar =  Math.sqrt(squaredHeightVar) / (3 * grid[0].length);
		maxHeight /= grid.length;
		gaps /= (grid.length * grid[0].length);
		weightedGaps /= (0.25 * grid.length * grid[0].length);
		weightedGaps2 /= (0.25 * grid.length * grid[0].length);
		pits22 = Math.pow(pits22, 2) / 25;
		pits23 = Math.pow(pits23, 2) / 25;
		pits33 = Math.pow(pits33, 2) / 25;

		return weights[0] * avgHeight
				+ weights[1] * avgSquaredHeight
				+ weights[2] * heightVar
				+ weights[3] * squaredHeightVar
				+ weights[4] * maxHeight
				+ weights[5] * Math.pow(maxHeight, 2)
				+ weights[6] * gaps
				+ weights[7] * weightedGaps
				+ weights[8] * weightedGaps2
				+ weights[9] * up1Steps
				+ weights[10] * down1Steps
				+ weights[11] * flatSteps
				+ weights[12] * pits22
				+ weights[13] * pits23
				+ weights[14] * pits33;
	}

	public static double evalReduced(boolean[][] grid, double[] weights) {

		if (weights == null) weights = HeuristicAI.weightsReduced;

		double avgSquaredHeight = 0;
		double heightVar = 0;
		double squaredHeightVar = 0;
		double gaps = 0;
		double weightedGaps = 0;
		double up1Steps = 1;
		double down1Steps = 1;
		double flatSteps = 1;
		double pits22 = 0;
		double pits23 = 0;
		double pits33 = 0;

		int height1 = -1;
		int height2 = -1;
		int height3 = -1;

		for (int j = 0; j < grid[0].length; j++) {

			int blocksAbove = 0;
			int distToLastBlock = -1;

			height3 = height2;
			height2 = height1;
			height1 = 0;

			for (int i = 0; i < grid.length; i++) {

				if (grid[i][j] == true) {

					blocksAbove++;
					distToLastBlock = 0;

					if (height1 == 0) height1 = grid.length - i;
				}
				else {

					if (distToLastBlock != -1) {

						weightedGaps += (1 + (grid.length - i + blocksAbove) / (2 * grid.length))
								/ (2 * Math.pow(2, distToLastBlock));

						gaps++;

						distToLastBlock++;
					}
				}
			}

			avgSquaredHeight += Math.pow(height1, 2);

			if (height2 != -1) {

				int step = height1 - height2;

				heightVar += Math.abs(step);
				squaredHeightVar += Math.pow(step, 2);

				if (step == 1) up1Steps = 0;
				else if (step == -1) down1Steps = 0;
				else if (step == 0) flatSteps = 0;
				else if (step == 2) {

					if (j == 1) pits23++;
					else if (height3 - height2 == 2) pits22++;
					else if (height3 - height2 == 3) pits23++;
				}
				else if (step > 2) {

					if (j == 1) pits33++;
					else if (height3 - height2 == 2) pits23++;
					else if (height3 - height2 == 3) pits33++;
				}
				else if (j == grid[0].length) {

					if (step == -2) pits23++;
					else pits33++;
				}
			}
		}

		avgSquaredHeight = Math.sqrt(avgSquaredHeight) / (grid[0].length * grid.length);
		heightVar /= (3 * grid[0].length);
		squaredHeightVar =  Math.sqrt(squaredHeightVar) / (3 * grid[0].length);
		gaps /= (grid.length * grid[0].length);
		weightedGaps /= (0.25 * grid.length * grid[0].length);
		pits22 = Math.pow(pits22, 2) / 25;
		pits23 = Math.pow(pits23, 2) / 25;
		pits33 = Math.pow(pits33, 2) / 25;

		return weights[0] * avgSquaredHeight
				+ weights[1] * heightVar
				+ weights[2] * squaredHeightVar
				+ weights[3] * gaps
				+ weights[4] * weightedGaps
				+ weights[5] * up1Steps
				+ weights[6] * down1Steps
				+ weights[7] * flatSteps
				+ weights[8] * pits22
				+ weights[9] * pits23
				+ weights[10] * pits33;
	}

	public static double evalFull(boolean[][] grid, double[] weights) {

		if (weights == null) weights = HeuristicAI.weightsFull;

		return evalReduced(grid, weights);
	}

	public static double evalPred(boolean reduced, boolean[][] grid, double[] weights, int predDepth) {
	
		double eval;
	
		if (predDepth == 0) eval = reduced ? evalReduced(grid, weights) : evalFull(grid, weights);
		else {
	
			double totalEval = 0;
			boolean failed = false;
	
			for (int j = 0; j < 7; j++) {
	
				Move move2 = search(reduced, grid, j, weights, predDepth - 1);
	
				if (move2 != null) totalEval += move2.getScore();
				else failed = true;
			}
	
			if (failed) eval = Double.MAX_VALUE;
			else eval = totalEval / 7;
		}
	
		return eval;
	}
}
