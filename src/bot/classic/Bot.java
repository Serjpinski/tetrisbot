package bot.classic;

import java.util.ArrayList;
import java.util.Random;

import logic.Grid;
import logic.Move;
import logic.Position;

public class Bot {

	// Learned weights
	//private static EvalWeights weights = new EvalWeights(0.62, 0.10, 0.26, 0.02);
	private static EvalWeights weights = new EvalWeights(0.60, 0.28, 0.07, 0.05);

	public static void main (String[] args) {

		learnWeightsOld();
	}

	/**
	 * Learns the weights for the subscores with the old algorithm. For the new
	 * genetic algorithm use the class EvalLearner.
	 */
	public static void learnWeightsOld() {

		Random rand = new Random();

		double bestAvgLines = 0;

		int gapW1 = 300;
		int avgHeiW1 = 300;
		int maxHeiW1 = 300;
		int skylineW1 = 300;
		int gapW2 = gapW1;
		int avgHeiW2 = avgHeiW1;
		int maxHeiW2 = maxHeiW1;
		int skylineW2 = skylineW1;

		int iter = 0;

		while (true) {

			int minLines = -1;
			int maxLines = -1;
			int totalLines = 0;

			for (int i = 0; i < 4; i++) {

				// Normalizes the sum of weights to 1
				double sum = gapW1 + avgHeiW1 + maxHeiW1 + skylineW1;
				weights.weights[0] = gapW1 / sum;
				weights.weights[1] = avgHeiW1 / sum;
				weights.weights[2] = maxHeiW1 / sum;
				weights.weights[3] = skylineW1 / sum;

				boolean[][] grid = Grid.emptyGrid();

				int lines = 0;

				int activePiece = rand.nextInt(7);
				int nextPiece = rand.nextInt(7);
				Move best = search(grid, activePiece, nextPiece, weights);

				while (best != null) {

					best.place(grid);
					lines += best.getLinesCleared();

					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Best weights: " + gapW2 + ", " + avgHeiW2
							+ ", " + maxHeiW2 + ", " + skylineW2 + "]");
					System.out.println("[Best average lines: " + bestAvgLines + "]");
					System.out.println();

					activePiece = nextPiece;
					nextPiece = rand.nextInt(7);
					best = search(grid, activePiece, nextPiece, weights);
				}

				if (lines > maxLines) maxLines = lines;
				if (minLines == -1 || lines < minLines) minLines = lines;
				totalLines += lines;
			}

			double avgLines = (totalLines - maxLines - minLines) / 2.0;

			if (avgLines > bestAvgLines) {

				// Raises the threshold
				bestAvgLines = (bestAvgLines + avgLines) / 2;

				gapW2 = gapW1;
				avgHeiW2 = avgHeiW1;
				maxHeiW2 = maxHeiW1;
				skylineW2 = skylineW1;
			}
			else {

				// Lowers the threshold
				bestAvgLines *= 0.9;

				gapW1 = gapW2;
				avgHeiW1 = avgHeiW2;
				maxHeiW1 = maxHeiW2;
				skylineW1 = skylineW2;
			}

			switch (iter % 4) {

			// Increases or decreases one of the weights (lower variation on each iteration)
			case 0: gapW1 = Math.max(1, gapW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
			case 1: avgHeiW1 = Math.max(1, avgHeiW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
			case 2: maxHeiW1 = Math.max(1, maxHeiW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
			case 3: skylineW1 = Math.max(1, skylineW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
			}

			iter++;
		}
	}

	/**
	 * Looks for the best move given the current piece, the next one and the grid.
	 */
	public static Move search(boolean[][] grid, int activePiece, int nextPiece, EvalWeights weights) {

		ArrayList<Move> moves = getMoves(activePiece, grid);
		Move best = null;
		double bestEval = 2;

		for (int i = 0; i < moves.size(); i++) {

			Move move = moves.get(i);

			// Simulates the move
			move.place(grid);

			ArrayList<Move> moves2 = getMoves(nextPiece, grid);
			double bestEval2 = 2;

			for (int j = 0; j < moves2.size(); j++) {

				Move move2 = moves2.get(j);

				// Simulates the move
				move2.place(grid);

				double eval = eval(grid, weights);

				if (eval < bestEval2) bestEval2 = eval;

				// Undoes the simulation
				move2.remove(grid);
			}

			if (bestEval2 < bestEval) {

				bestEval = bestEval2;
				best = move;
			}

			// Undoes the simulation
			move.remove(grid);
		}

		if (best != null) best.setScore(bestEval);
		return best;
	}

	public static Move search(boolean[][] grid, int activePiece, int nextPiece) {

		return search(grid, activePiece, nextPiece, weights);
	}

	/**
	 * Computes the score for a grid state, without considering any other data.
	 * Lower score means better grid state.
	 */
	private static double eval(boolean[][] grid, EvalWeights weights) {

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

		return weights.weights[0] * gapScore
				+ weights.weights[1] * avgHeiScore
				+ weights.weights[2] * maxHeiScore
				+ weights.weights[3] * skylineScore;
	}

	/**
	 * Computes all possible moves given a piece and the grid.
	 */
	private static ArrayList<Move> getMoves(int piece, boolean[][] grid) {

		ArrayList<Move> moves = new ArrayList<Move>();

		for (int j = 0; j < Move.numOfRotations(piece); j++) {

			for (int x = 0; x < grid.length; x++) {

				for (int y = 0; y < grid[0].length; y++) {

					Position position = new Position(x, y);
					Move move = new Move(piece, j, position);

					if (move.canBePlaced(grid)) moves.add(move);
				}
			}
		}

		return moves;
	}
}
