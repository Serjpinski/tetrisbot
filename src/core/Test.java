package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import bot.classic.ClassicBot;
import bot.neural.FullSample;
import bot.neural.ReducedSample;
import bot.neural.Sample;

public class Test {

	private static final String[] DEFAULT_ARGS = new String[] { "c0r" };

	public static void main (String[] args)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		if (args.length == 0 || args[0].length() < 1) args = DEFAULT_ARGS;

		char mode = args[0].charAt(0);

		if (mode == 'r') testRandom();
		else {

			if (args[0].length() < 2) args = DEFAULT_ARGS;

			String predDepth = args[0].charAt(1) + "";
			String optional = args[0].substring(2);

			if (mode == 'c') testClassic(optional.contains("r"), false, Integer.parseInt(predDepth),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null,
					optional.contains("v"));

			if (mode == 'C') testClassic(optional.contains("r"), true, Integer.parseInt(predDepth),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null,
					optional.contains("v"));

			if (mode == 'n') testNeural(optional.contains("r"), Integer.parseInt(predDepth));
		}
	}

	/**
	 * Tests the neural bot based in the classic bot with only active piece
	 * plus piece prediction.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void testNeural(boolean reduced, int predDepth)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		Random rand = new Random();
		bot.neural.NeuralBot neuralBot = new bot.neural.NeuralBot(predDepth);

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		while (true) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			long t0 = System.nanoTime();
			Move best = neuralBot.search(predDepth, grid, rand.nextInt(7), reduced);
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				//				Move classic = bot.classic.ClassicBot.search(predDepth, grid, best.piece);

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid, null);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[SD/Mean: " + Misc.doubleToString(stdDev / mean) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println();

				//				System.out.println("Rot: " + (best.rotation - classic.rotation)
				//						+ " Col: " + (best.basePosition.y - classic.basePosition.y));
				//				
				//				try { Thread.sleep(1000); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = neuralBot.search(predDepth, grid, rand.nextInt(7), reduced);
				t1 = System.nanoTime() - t0;
			}
			
			totalLines += lines;
			mean = totalLines / (double) iter;
			histLines.add(lines);
			for (int i = 0; i < histLines.size(); i++) stdDev += Math.pow(histLines.get(i) - mean, 2);
			stdDev = Math.sqrt(stdDev / histLines.size());

			iter++;
		}
	}

	public static void testClassic(boolean reduced, boolean next, int predDepth, FileWriter[] dataset, boolean verbose)
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

		while (true) {

			boolean[][] grid = Grid.emptyGrid();
			int lines = 0;

			Move best = null;
			int activePiece = rand.nextInt(7);
			int nextPiece = rand.nextInt(7);

			long t0 = System.nanoTime();

			if (reduced) {
				
				if (next) best = bot.classic.ClassicBot.search(
						Grid.getSteps(grid), activePiece, nextPiece, null, predDepth).fixRow(grid);
				else best = bot.classic.ClassicBot.search(
						Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
			}
			else {
				
				if (next) best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece, null, predDepth);
				else best = bot.classic.ClassicBot.search(grid, activePiece, null, predDepth);
			}

			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
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

				totalEval += ClassicBot.eval(grid, null);

				if (verbose) {
					
					Grid.printGrid(grid);
					System.out.println("[Lines: " + lines + "]");
					System.out.println("[Iteration: " + iter + "]");
					System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
					System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
					System.out.println("[SD/Mean: " + Misc.doubleToString(stdDev / mean) + "]");
					System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
					System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
					System.out.println();
				}
				
				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);

				t0 = System.nanoTime();

				if (reduced) {
					
					if (next) best = bot.classic.ClassicBot.search(
							Grid.getSteps(grid), activePiece, nextPiece, null, predDepth).fixRow(grid);
					else best = bot.classic.ClassicBot.search(
							Grid.getSteps(grid), activePiece, null, predDepth).fixRow(grid);
				}
				else {
					
					if (next) best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece, null, predDepth);
					else best = bot.classic.ClassicBot.search(grid, activePiece, null, predDepth);
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
				System.out.println("[SD/Mean: " + Misc.doubleToString(stdDev / mean) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println();
			}

			iter++;
		}
	}

	public static void testRandom() {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		double mean = 0;
		double stdDev = 0;
		ArrayList<Integer> histLines = new ArrayList<Integer>();
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		while (true) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			long t0 = System.nanoTime();
			ArrayList<Move> moves = Move.getMoves(rand.nextInt(7), grid);
			Move best = moves.get(rand.nextInt(moves.size()));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid, null);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Mean: " + Misc.doubleToString(mean) + "]");
				System.out.println("[StdDev: " + Misc.doubleToString(stdDev) + "]");
				System.out.println("[SD/Mean: " + Misc.doubleToString(stdDev / mean) + "]");
				System.out.println("[Avg time: " + Misc.doubleToString(totalMoveTime / totalMoves) + "]");
				System.out.println("[Avg eval: " + Misc.doubleToString(totalEval / totalMoves) + "]");
				System.out.println();

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

			iter++;
		}
	}
}
