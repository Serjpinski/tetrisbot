package bot.neural;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import logic.Move;

public class Bot {
	
	private EasyPredictModelWrapper[] models;
	
	public Bot(int predDepth)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		String baseName = "bot.neural.ann.ANNp" + predDepth + "piece";
		models = new EasyPredictModelWrapper[7];
		
		for (int i = 0; i < models.length; i++)
			models[i] = new EasyPredictModelWrapper(
					(hex.genmodel.GenModel) Class.forName(baseName + i).newInstance());
	}

	public Move search(int predDepth, boolean[][] grid, int piece) {

		int[] steps = Instance.getSteps(grid);
		
		RowData instance = new RowData();
		for (int i = 0; i < steps.length; i++) instance.put("i" + i, (double) steps[i]);
		
		try {
			
			RegressionModelPrediction prediction = models[piece].predictRegression(instance);
			int code = (int) Math.max(0, Math.min(Move.COL_VAR_SUM_LIST[piece] - 1, Math.round(prediction.value)));
			
//			MultinomialModelPrediction prediction = models[piece].predictMultinomial(instance);
//			int code = Math.round(prediction.labelIndex);
			
			return Instance.code2Move(code, piece, grid);
		}
		catch (PredictException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
}
