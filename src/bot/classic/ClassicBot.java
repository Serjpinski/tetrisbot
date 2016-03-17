package bot.classic;

import java.util.ArrayList;

import core.Grid;
import core.Move;

public class ClassicBot {

	// Learned weights
	private static double[] weights = new double[] {0.095728, 0.016116, 0.044371, 0.172680, 0.592818, 0.000262, 0.000262, 0.003391, 0.000676, 0.018473, 0.055223};
	
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
	
	public static Move search(int[] steps, int activePiece, int nextPiece,
			double[] weights, int predDepth) {
	
		return search(Grid.getGrid(steps), activePiece, nextPiece, weights, predDepth);
	}
	
	public static Move search(boolean[][] grid, int activePiece,
			double[] weights, int predDepth) {
		
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


	public static Move search(int[] steps, int activePiece,
			double[] weights, int predDepth) {
	
		return search(Grid.getGrid(steps), activePiece, weights, predDepth);
	}
	
	public static double eval(boolean[][] grid, double[] weights) {
		
		if (weights == null) weights = ClassicBot.weights;
		
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
}
