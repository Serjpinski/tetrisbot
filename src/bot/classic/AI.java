package bot.classic;

import java.util.ArrayList;
import java.util.Random;

import logic.Piece;
import logic.Position;

public class AI extends Thread {

	public final static int HEIGHT = 20;
	public final static int WIDTH = 10;
	
	// Pesos aprendidos
	//private static EvalWeights weights = new EvalWeights(0.62, 0.10, 0.26, 0.02);
	private static EvalWeights weights = new EvalWeights(0.60, 0.28, 0.07, 0.05);
	
	/**
	 * Aprende los pesos de los subscores.
	 */
	public static void learnWeights() {
		
		Random r = new Random();

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
				
				// Normaliza la suma de los pesos a 1;
				double sum = gapW1 + avgHeiW1 + maxHeiW1 + skylineW1;
				weights.weights[0] = gapW1 / sum;
				weights.weights[1] = avgHeiW1 / sum;
				weights.weights[2] = maxHeiW1 / sum;
				weights.weights[3] = skylineW1 / sum;
				
				boolean[][] grid = new boolean[HEIGHT][WIDTH];
				for (int x = 0; x < grid.length; x++)
					for (int y = 0; y < grid[0].length; y++)
						grid[x][y] = false;

				int lines = 0;

				int activePiece = r.nextInt(7);
				int nextPiece = r.nextInt(7);
				Piece best = search(grid, activePiece, nextPiece, weights);

				while (best != null) {

					best.place(grid);
					lines += best.getLinesCleared();

					printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Best weights: " + gapW2 + ", " + avgHeiW2
							+ ", " + maxHeiW2 + ", " + skylineW2 + "]");
					System.out.println("[Best average lines: " + bestAvgLines + "]");
					System.out.println();

					activePiece = nextPiece;
					nextPiece = r.nextInt(7);
					best = search(grid, activePiece, nextPiece, weights);
				}

				if (maxLines == -1 || lines > maxLines) maxLines = lines;
				if (maxLines == -1 || lines < minLines) minLines = lines;
				totalLines += lines;
			}
			
			double avgLines = (totalLines - maxLines - minLines) / 2.0;
			
			if (avgLines > bestAvgLines) {
				
				// Sube el liston al promedio de los dos
				bestAvgLines = (bestAvgLines + avgLines) / 2;
				
				gapW2 = gapW1;
				avgHeiW2 = avgHeiW1;
				maxHeiW2 = maxHeiW1;
				skylineW2 = skylineW1;
			}
			else {
				
				// Baja el liston para los siguientes intentos
				bestAvgLines *= 0.9;
				
				gapW1 = gapW2;
				avgHeiW1 = avgHeiW2;
				maxHeiW1 = maxHeiW2;
				skylineW1 = skylineW2;
			}
			
			switch (iter % 4) {
				
				// Aumenta o disminuye uno de los pesos (cada iteracion se modifica menos)
				case 0: gapW1 = Math.max(1, gapW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
				case 1: avgHeiW1 = Math.max(1, avgHeiW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
				case 2: maxHeiW1 = Math.max(1, maxHeiW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
				case 3: skylineW1 = Math.max(1, skylineW1 + (100 - iter) * (2 * ((iter/4) % 2) - 1)); break;
			}
			
			iter++;
		}
	}

	/**
	 * Busca el mejor movimiento dadas la pieza actual, la siguiente y el tablero.
	 */
	public static Piece search(boolean[][] grid, int activePiece, int nextPiece, EvalWeights weights) {
		
		ArrayList<Piece> placements = getPlacements(activePiece, grid);
		Piece best = null;
		double bestEval = 2;
		
		for (int i = 0; i < placements.size(); i++) {
			
			Piece placement = placements.get(i);
			
			// Simula el movimiento
			placement.place(grid);
			
			ArrayList<Piece> placements2 = getPlacements(nextPiece, grid);
			double bestEval2 = 2;
			
			for (int j = 0; j < placements2.size(); j++) {
				
				Piece placement2 = placements2.get(j);
				
				// Simula el movimiento
				placement2.place(grid);
				
				double eval = eval(grid, weights);
				
				if (eval < bestEval2) bestEval2 = eval;
				
				// Deshace el movimiento
				placement2.remove(grid);
			}
			
			if (bestEval2 < bestEval) {
				
				bestEval = bestEval2;
				best = placement;
			}

			// Deshace el movimiento
			placement.remove(grid);
		}
		
		if (best != null) best.setScore(bestEval);
		return best;
	}
	
	/**
	 * Calcula la score de un estado concreto del grid, sin tener en cuenta
	 * ningun otro dato. Cuanto menos score mejor es el estado.
	 */
	private static double eval(boolean[][] grid, EvalWeights weights) {
				
		double gapScore = 0; // Penaliza los huecos debajo de bloques
		double avgHeiScore = 0; // Penaliza la altura promedio
		double maxHeiScore = 0; // Penaliza la altura maxima
		double skylineScore = 0; // Favorece que haya variaciones de altura adecuadas
		
		int height1 = -1;
		int height2 = -1;
		int height3 = -1;
		
		boolean up1Steps = false; // Hay escalones hacia arriba de 1 de alto
		boolean down1Steps = false; // Hay escalones hacia abajo de 1 de alto
		int flatSteps = 0; // Numero de escalones planos (0 de alto)
		int pits = 0; // Numero de fosos (de 2 o mas de profundidad)
		
		for (int j = 0; j < grid[0].length; j++) {
			
			int blocksAbove = 0; // Numero de bloques encima de la casilla actual
			int distToBlock = -1; // Distancia al bloque mas cercano encima de la casilla actual, -1 si no hay
			
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
		
		// Calcula las subscores y las normaliza entre 0 y 1
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
	 * Calcula todos los posibles movimientos dada un tipo de pieza y el tablero.
	 */
	private static ArrayList<Piece> getPlacements(int pieceType, boolean[][] grid) {
		
		ArrayList<Piece> placements = new ArrayList<Piece>();
		
		for (int j = 0; j < Piece.numOfRotations(pieceType); j++) {
				
			for (int x = 0; x < grid.length; x++) {

				for (int y = 0; y < grid[0].length; y++) {

					Position position = new Position(x, y);
					Piece piece = new Piece(pieceType, j, position);

					if (piece.canBePlaced(grid)) placements.add(piece);
				}
			}
		}
		
		return placements;
	}
	
	/**
	 * Pinta el tablero.
	 */
	private static void printGrid(boolean[][] grid) {
		
		for (int i = 0; i < grid.length; i++) {
			
			System.out.print("|");
			
			for (int j = 0; j < grid[0].length; j++) {
				
				if (grid[i][j] == true) System.out.print("\u2588\u2588");
				else System.out.print("  ");
			}
			
			System.out.println("|");
		}
	}
	
	public static void main (String[] args) {
	
		learnWeights();
	}
}
