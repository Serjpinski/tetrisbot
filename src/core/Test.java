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

			if (mode == 'c') testClassic(optional.contains("r"), Integer.parseInt(predDepth),
					optional.contains("d") ? Sample.initDataset("p" + predDepth, optional.contains("r")) : null);

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
		int minLines = -1;
		int maxLines = -1;
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
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				try { Thread.sleep(1); } catch (InterruptedException e) {}

				//				System.out.println("Rot: " + (best.rotation - classic.rotation)
				//						+ " Col: " + (best.basePosition.y - classic.basePosition.y));
				//				
				//				try { Thread.sleep(1000); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = neuralBot.search(predDepth, grid, rand.nextInt(7), reduced);
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	public static void testClassic(boolean reduced, int predDepth, FileWriter[] dataset)
			throws IOException {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		int minLines = -1;
		int maxLines = -1;
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		//		int[][] totalSamples = null;
		//		int[] minSamples = null;
		//
		//		if (dataset != null) {
		//
		//			totalSamples = new int[7][];
		//			for (int i = 0; i < 7; i++) totalSamples[i] = new int[Move.COL_VAR_SUM_LIST[i]];
		//			minSamples = new int[7];
		//		}

		while (true) {

			boolean[][] grid = Grid.emptyGrid();
			int lines = 0;

			Move best = null;
			int piece = rand.nextInt(7);

			long t0 = System.nanoTime();

			if (reduced) best = bot.classic.ClassicBot.search(
					Grid.getSteps(grid), piece, null, predDepth).fixRow(grid);
			else best = bot.classic.ClassicBot.search(grid, piece, null, predDepth);

			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				if (dataset != null) {

					Sample sample;

					if (reduced) sample = new ReducedSample(grid, best);
					else sample = new FullSample(grid, best);

					dataset[piece].write(sample + "\n");
					dataset[piece].flush();

					//					int numSamples = totalSamples[piece][sample.moveCode];
					//
					//					if (minSamples[piece] >= numSamples - 10) {
					//
					//						dataset[piece].write(sample + "\n");
					//						dataset[piece].flush();
					//
					//						totalSamples[piece][sample.moveCode]++;
					//
					//						if (minSamples[piece] == numSamples) {
					//
					//							minSamples[piece]++;
					//
					//							for (int i = 0; i < totalSamples[piece].length; i++)
					//								if (totalSamples[piece][i] < minSamples[piece])
					//									minSamples[piece] = totalSamples[piece][i];
					//						}
					//					}
				}

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid, null);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				//				if (dataset != null) System.out.println("[Min samples: " + Arrays.toString(minSamples) + "]");
				System.out.println();

				try { Thread.sleep(1); } catch (InterruptedException e) {}

				piece = rand.nextInt(7);

				t0 = System.nanoTime();

				if (reduced) best = bot.classic.ClassicBot.search(
						Grid.getSteps(grid), piece, null, predDepth).fixRow(grid);
				else best = bot.classic.ClassicBot.search(grid, piece, null, predDepth);

				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	/**
	 * Tests the classic bot with both active and next piece.
	 */
	public static void testClassicNext(int depthPred) {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		int minLines = -1;
		int maxLines = -1;
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		while (true) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			int activePiece = rand.nextInt(7);
			int nextPiece = rand.nextInt(7);

			long t0 = System.nanoTime();
			Move best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece, null, depthPred);
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
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				try { Thread.sleep(1); } catch (InterruptedException e) {}

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);

				t0 = System.nanoTime();
				best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece, null, depthPred);
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	public static void testRandom() {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		int minLines = -1;
		int maxLines = -1;
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
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				try { Thread.sleep(1); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				moves = Move.getMoves(rand.nextInt(7), grid);
				if (moves.isEmpty()) best = null;
				else best = moves.get(rand.nextInt(moves.size()));
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}
}
