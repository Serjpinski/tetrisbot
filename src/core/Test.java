package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import heuristic.HeuristicAI;
import neural.ErrorHistory;
import neural.FullSample;
import neural.ReducedSample;
import neural.Sample;

public class Test {

	private static final String[] DEFAULT_ARGS = new String[] { "n1rh", "100" };
	
	public static long HYBRID_EVAL_CALLS = 0;
	public static int[] HYBRID_EVAL_CALL_FREQS = new int[35];

	public static void main (String[] args)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		if (args.length == 0 || args[0].length() < 1) args = DEFAULT_ARGS;

		char mode = args[0].charAt(0);

		if (mode == 'r') testRandom(args[0].substring(1).contains("v"));
		else {

			if (args[0].length() < 2) args = DEFAULT_ARGS;

			String predDepth = args[0].charAt(1) + "";
			String optional = args[0].substring(2);
			int maxIter = -1;
			if (args.length > 1) maxIter = Integer.parseInt(args[1]);

			if (mode == 'h') testHeuristic(optional.contains("r"), false, Integer.parseInt(predDepth),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null,
							optional.contains("v"), maxIter);

			if (mode == 'H') testHeuristic(optional.contains("r"), true, Integer.parseInt(predDepth),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null,
							optional.contains("v"), maxIter);

			if (mode == 'n') testNeural(optional.contains("r"), Integer.parseInt(predDepth),
					optional.contains("v"), maxIter, optional.contains("e"),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null,
					optional.contains("h"));
		}

		//		testHeuristicStress(false, 2, true, -1);
	}

	/**
	 * Tests the neural bot based in the classic bot with only active piece
	 * plus piece prediction.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	public static void testNeural(boolean reduced, int predDepth, boolean verbose,
			int maxIter, boolean errors, FileWriter[] dataset, boolean hybrid)
					throws InstantiationException, IllegalAccessException,
					ClassNotFoundException, IOException {

		Random rand = new Random();
		neural.NeuralAI neuralAI = new neural.NeuralAI(reduced, predDepth);

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;
		ErrorHistory errHist = new ErrorHistory();
		long totalPossibleMoves = 0;
		long initTime = System.currentTimeMillis();

		while (iter != maxIter + 1) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;
			int moves = 0;
			int activePiece = rand.nextInt(7);

			long t0 = System.nanoTime();
			Move best = neuralAI.search(reduced, grid, activePiece, predDepth, hybrid);
			long t1 = System.nanoTime() - t0;
			
			if (hybrid) totalPossibleMoves += Move.NUM_MOVES_LIST[activePiece];

			while (best != null) {

				totalMoveTime += t1 / 1000000.0;
				totalMoves++;
				moves++;

				if (errors) {

					Move heuristic;

					if (reduced) heuristic = HeuristicAI.search(Grid.getSteps(grid), activePiece, null, predDepth);
					else heuristic = HeuristicAI.search(grid, activePiece, null, predDepth);

					if (heuristic != null) {

						if (best.equals(heuristic)) errHist.addMove(moves, false);
						else {

							errHist.addMove(moves, true);

							if (dataset != null) {

								Sample sample;

								if (reduced) sample = new ReducedSample(grid, heuristic);
								else sample = new FullSample(grid, heuristic);

								dataset[activePiece].write(sample + "\n");
								dataset[activePiece].flush();

								if (errHist.getTotalErrors() == 70000) return;
							}
						}
					}
				}

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += reduced ? HeuristicAI.evalReduced(grid, null) : HeuristicAI.evalFull(grid, null);

				if (verbose) {

					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
					System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
					System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
					System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
					
					if (hybrid) {
						
						System.out.println("hybrid freqs = " + Arrays.toString(HYBRID_EVAL_CALL_FREQS));
						System.out.println("total moves = " + totalMoves);
						System.out.println("[hybrid ratio = "
							+ Misc.doubleToString((HYBRID_EVAL_CALLS / (double) totalPossibleMoves)) + "]");
					}

					if (errors) {

						System.out.println("errors = " + Misc.arrayToString(errHist.getErrorRatios()));
						System.out.println("global error = " + Misc.doubleToString(errHist.getGlobalRatio()));
						if (dataset != null) System.out.println("total errors = " + errHist.getTotalErrors());
					}

					System.out.println("lines = " + Arrays.toString(histLines.toArray(new Integer[0])));
					System.out.println();
				}

				activePiece = rand.nextInt(7);

				t0 = System.nanoTime();
				best = neuralAI.search(reduced, grid, activePiece, predDepth, hybrid);
				t1 = System.nanoTime() - t0;
				
				if (hybrid) totalPossibleMoves += Move.NUM_MOVES_LIST[activePiece];
			}

			totalLines += lines;
			mean = totalLines / (double) iter;
			histLines.add(lines);
			for (int i = 0; i < histLines.size(); i++) stdDev += Math.pow(histLines.get(i) - mean, 2);
			stdDev = Math.sqrt(stdDev / histLines.size());

			if (!verbose) {

				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				
				if (hybrid) {
					
					System.out.println("hybrid freqs = " + Arrays.toString(HYBRID_EVAL_CALL_FREQS));
					System.out.println("total moves = " + totalMoves);
					System.out.println("[hybrid ratio = "
						+ Misc.doubleToString((HYBRID_EVAL_CALLS / (double) totalPossibleMoves)) + "]");
				}

				if (errors) {

					System.out.println("errors = " + Misc.arrayToString(errHist.getErrorRatios()));
					System.out.println("global error = " + Misc.doubleToString(errHist.getGlobalRatio()));
					if (dataset != null) System.out.println("total errors = " + errHist.getTotalErrors());
				}

				System.out.println("lines = " + Arrays.toString(histLines.toArray(new Integer[0])));
				System.out.println();
			}

			iter++;
		}
		
		System.out.println("total time = " + Misc.doubleToString(
				(System.currentTimeMillis() - initTime) / 3600000.0) + " h");
	}

	public static void testHeuristic(boolean reduced, boolean next, int predDepth, FileWriter[] dataset,
			boolean verbose, int maxIter) throws IOException {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;
		long initTime = System.currentTimeMillis();

		while (iter != maxIter + 1) {

			boolean[][] grid = Grid.emptyGrid();
			int lines = 0;

			Move best = null;
			int activePiece = rand.nextInt(7);
			int nextPiece = rand.nextInt(7);

			long t0 = System.nanoTime();

			if (reduced) {

				if (next) best = HeuristicAI.search(
						Grid.getSteps(grid), activePiece, nextPiece, null, predDepth).fixRow(grid);
				else best = HeuristicAI.search(
						Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
			}
			else {

				if (next) best = HeuristicAI.search(grid, activePiece, nextPiece, null, predDepth);
				else best = HeuristicAI.search(grid, activePiece, null, predDepth);
			}

			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0;
				totalMoves++;

				if (dataset != null) {

					Sample sample;

					if (reduced) sample = new ReducedSample(grid, best);
					else sample = new FullSample(grid, best);

					dataset[activePiece].write(sample + "\n");
					dataset[activePiece].flush();
				}

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += reduced ? HeuristicAI.evalReduced(grid, null) : HeuristicAI.evalFull(grid, null);

				if (verbose) {

					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
					System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
					System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
					System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
					System.out.println("lines = " + Arrays.toString(histLines.toArray(new Integer[0])));
					System.out.println();
				}

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);

				t0 = System.nanoTime();

				if (reduced) {

					if (next) best = HeuristicAI.search(
							Grid.getSteps(grid), activePiece, nextPiece, null, predDepth).fixRow(grid);
					else best = HeuristicAI.search(
							Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
				}
				else {

					if (next) best = HeuristicAI.search(grid, activePiece, nextPiece, null, predDepth);
					else best = HeuristicAI.search(grid, activePiece, null, predDepth);
				}

				t1 = System.nanoTime() - t0;
			}

			totalLines += lines;
			mean = totalLines / (double) iter;
			histLines.add(lines);
			for (int i = 0; i < histLines.size(); i++) stdDev += Math.pow(histLines.get(i) - mean, 2);
			stdDev = Math.sqrt(stdDev / histLines.size());

			if (!verbose) {

				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println("lines = " + Arrays.toString(histLines.toArray(new Integer[0])));
				System.out.println();
			}

			iter++;
		}
		
		System.out.println("total time = " + Misc.doubleToString(
				(System.currentTimeMillis() - initTime) / 3600000.0) + " h");
	}

	public static void testRandom(boolean verbose) {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEvalReduced = 0;
		double totalEvalFull = 0;

		while (true) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			long t0 = System.nanoTime();
			ArrayList<Move> moves = Move.getMoves(rand.nextInt(7), grid);
			Move best = moves.get(rand.nextInt(moves.size()));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0;
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();

				totalEvalReduced += HeuristicAI.evalReduced(grid, null);
				totalEvalFull += HeuristicAI.evalFull(grid, null);

				if (verbose) {

					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
					System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
					System.out.println("[Avg eval (reduced): " + Misc.doubleToString(totalEvalReduced / totalMoves) + "]");
					System.out.println("[Avg eval (full): " + Misc.doubleToString(totalEvalFull / totalMoves) + "]");
					System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
					System.out.println();
				}

				t0 = System.nanoTime();
				moves = Move.getMoves(rand.nextInt(7), grid);
				if (moves.isEmpty()) best = null;
				else best = moves.get(rand.nextInt(moves.size()));
				t1 = System.nanoTime() - t0;
			}

			totalLines += lines;
			mean = totalLines / (double) iter;
			histLines.add(lines);
			for (int i = 0; i < histLines.size(); i++) stdDev += Math.pow(histLines.get(i) - mean, 2);
			stdDev = Math.sqrt(stdDev / histLines.size());

			if (!verbose) {

				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[Avg eval (reduced): " + Misc.doubleToString(totalEvalReduced / totalMoves) + "]");
				System.out.println("[Avg eval (full): " + Misc.doubleToString(totalEvalFull / totalMoves) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println();
			}

			iter++;
		}
	}

	public static void testHeuristicStress(boolean reduced, int predDepth, boolean verbose, int maxIter)
			throws IOException {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		while (iter != maxIter + 1) {

			boolean[][] grid = Grid.emptyGrid();
			int lines = 0;

			Move best = null;

			int activePiece = rand.nextInt(7);

			long t0 = System.nanoTime();

			if (reduced) best = HeuristicAI.search(Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
			else best = HeuristicAI.search(grid, activePiece, null, predDepth);

			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0;
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += reduced ? HeuristicAI.evalReduced(grid, null) : HeuristicAI.evalFull(grid, null);

				if (verbose) {

					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
					System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
					System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
					System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
					System.out.println();
				}

				if (totalMoves % 2 == 1) activePiece = rand.nextInt(7);
				else {

					double worstScore = 0;

					for (int i = 0; i < 7; i++) {

						Move move = HeuristicAI.search(grid, i, null, 0);

						if (move != null) {

							if (move.getScore() > worstScore) {

								worstScore = move.getScore();
								activePiece = i;
							}
						}
					}
				}

				t0 = System.nanoTime();

				if (reduced) best = HeuristicAI.search(Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
				else best = HeuristicAI.search(grid, activePiece, null, predDepth);

				t1 = System.nanoTime() - t0;
			}

			totalLines += lines;
			mean = totalLines / (double) iter;
			histLines.add(lines);
			for (int i = 0; i < histLines.size(); i++) stdDev += Math.pow(histLines.get(i) - mean, 2);
			stdDev = Math.sqrt(stdDev / histLines.size());

			if (!verbose) {

				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println();
			}

			iter++;
		}

		System.out.println("lines = " + Arrays.toString(histLines.toArray(new Integer[0])));
	}
}
