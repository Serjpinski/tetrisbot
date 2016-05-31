package neural;

import java.util.ArrayList;

import core.Grid;
import core.Move;
import core.Test;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

public class NeuralAI {

	private static final double MIN_PROB = 0.00001;

	private EasyPredictModelWrapper[] models;

	public NeuralAI(boolean reduced, int predDepth)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String baseName = "neural.ann." + (reduced ? "reduced" : "full") + ".ANNp" + predDepth + "piece";
		models = new EasyPredictModelWrapper[7];

		for (int i = 0; i < models.length; i++)
			models[i] = new EasyPredictModelWrapper(
					(hex.genmodel.GenModel) Class.forName(baseName + i).newInstance());
	}

	public Move search(boolean reduced, boolean[][] grid, int activePiece, int predDepth, boolean hybrid) {

		RowData sample = new RowData();

		if (reduced) {

			int[] steps = Grid.getSteps(grid);
			for (int i = 0; i < steps.length; i++) sample.put("i" + i, (double) steps[i]);
		}
		else {

			for (int i = 0; i < grid.length; i++)
				for (int j = 0; j < grid[0].length; j++)
					sample.put("i" + (i * grid[0].length + j), grid[i][j] ? 1.0 : 0.0);
		}

		try {

			MultinomialModelPrediction prediction = models[activePiece].predictMultinomial(sample);

			if (hybrid) {

				double[] probs = prediction.classProbabilities;
				ArrayList<Move> candidates = new ArrayList<Move>();

				for (int i = 0; i < probs.length; i++) {

					if (probs[i] >= MIN_PROB) {

						int code = Integer.parseInt(models[activePiece].getResponseDomainValues()[i]);
						Move move = Move.code2Move(code, activePiece, grid);
						if (move != null) candidates.add(move);
					}
				}

				if (candidates.isEmpty()) return Move.code2Move(Integer.parseInt(prediction.label), activePiece, grid);

				Move best = candidates.get(0);
				
				if (candidates.size() == 1) return best;
				
				best.place(grid);
				best.setScore(heuristic.HeuristicAI.evalPred(false, grid, null, predDepth));
				best.remove(grid);

				for (int i = 1; i < candidates.size(); i++) {

					Move move = candidates.get(i);
					
					move.place(grid);
					move.setScore(heuristic.HeuristicAI.evalPred(false, grid, null, predDepth));
					move.remove(grid);
					
					if (move.getScore() < best.getScore()) best = move;
				}
				
				Test.HYBRID_EVAL_CALLS += candidates.size();

				return best;
			}
			
			return Move.code2Move(Integer.parseInt(prediction.label), activePiece, grid);
		}
		catch (PredictException e) {

			e.printStackTrace();
		}

		return null;
	}
}
