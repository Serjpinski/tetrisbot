package bot.neural;

import core.Grid;
import core.Move;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

public class NeuralBot {

	private EasyPredictModelWrapper[] models;

	public NeuralBot(int predDepth)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String baseName = "bot.neural.ann.ANNp" + predDepth + "piece";
		models = new EasyPredictModelWrapper[7];

		for (int i = 0; i < models.length; i++)
			models[i] = new EasyPredictModelWrapper(
					(hex.genmodel.GenModel) Class.forName(baseName + i).newInstance());
	}

	public Move search(int predDepth, boolean[][] grid, int piece, boolean reduced) {

		RowData sample = new RowData();

		if (reduced) {

			int[] steps = Grid.getSteps(grid);
			for (int i = 0; i < steps.length; i++) sample.put("i" + i, (double) steps[i]);
		}
		else {

			for (int i = 0; i < grid.length; i++)
				for (int j = 0; j < grid[0].length; j++)
					sample.put("i" + i * grid[0].length + j, grid[i][j] ? 1.0 : 0.0);
		}

		try {

			MultinomialModelPrediction prediction = models[piece].predictMultinomial(sample);
			return Move.code2Move(Integer.parseInt(prediction.label), piece, grid);
		}
		catch (PredictException e) {

			e.printStackTrace();
		}

		return null;
	}
}
