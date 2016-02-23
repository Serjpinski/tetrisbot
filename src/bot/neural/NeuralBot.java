package bot.neural;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;
import logic.Move;

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

	public Move search(int predDepth, boolean[][] grid, int piece) {
		
		int[] steps = InstanceRed.getSteps(grid);
		
		RowData instance = new RowData();
		for (int i = 0; i < steps.length; i++) instance.put("i" + i, (double) steps[i]);

//		RowData instance = new RowData();
//		for (int i = 0; i < grid.length; i++)
//			for (int j = 0; j < grid[0].length; j++)
//				instance.put("i" + i * grid[0].length + j, grid[i][j] ? 1.0 : 0.0);
		
		try {
			
//			RegressionModelPrediction prediction = models[piece].predictRegression(instance);
//			int code = (int) Math.max(0, Math.min(Move.COL_VAR_SUM_LIST[piece] - 1, Math.round(prediction.value)));
			
			MultinomialModelPrediction prediction = models[piece].predictMultinomial(instance);
			
			return InstanceRed.code2Move(Integer.parseInt(prediction.label), piece, grid);
		}
		catch (PredictException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
}
