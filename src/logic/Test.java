package logic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import bot.classic.ClassicBot;
import bot.neural.Instance;
import bot.neural.InstanceRed;

public class Test {

	public static void main (String[] args)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		//		testRandom(1);

		//		testClassicPred(1, 1, null);

		//		testClassicPred(Integer.parseInt(args[0]), 1,
		//				(args.length > 1 && args[1].equals("d")) ?
		//						Instance.initDataset("p" + args[0]) : null);

//		testClassicPredRed(Integer.parseInt(args[0]), 1,
//				(args.length > 1 && args[1].equals("d")) ?
//						InstanceRed.initDataset("p" + args[0]) : null);

				testNeural(1, 1);
	}

	/**
	 * Tests the neural bot based in the classic bot with only active piece
	 * plus piece prediction.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void testNeural(int predDepth, int delay)
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
			Move best = neuralBot.search(predDepth, grid, rand.nextInt(7));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

//				Move classic = bot.classic.ClassicBot.search(predDepth, grid, best.piece);

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}
				
//				System.out.println("Rot: " + (best.rotation - classic.rotation)
//						+ " Col: " + (best.basePosition.y - classic.basePosition.y));
//				
//				try { Thread.sleep(1000); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = neuralBot.search(predDepth, grid, rand.nextInt(7));
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	public static void testClassicPredRed(int predDepth, int delay, FileWriter[] dataset) throws IOException {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		int minLines = -1;
		int maxLines = -1;
		long totalMoves = 0;
		double totalMoveTime = 0;
		double totalEval = 0;

		int[][] totalSamples = null;
		int[] minSamples = null;

		if (dataset != null) {

			totalSamples = new int[7][];
			for (int i = 0; i < 7; i++) totalSamples[i] = new int[Move.COL_VAR_SUM_LIST[i]];
			minSamples = new int[7];
		}

		while (true) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			long t0 = System.nanoTime();
			Move best = bot.classic.ClassicBot.searchRed(predDepth,
					InstanceRed.getSteps(grid), rand.nextInt(7)).fixRow(grid);
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				if (dataset != null) {

					InstanceRed instance = new InstanceRed(grid, best);
					int piece = best.piece;
					int code = instance.code;
					int samples = totalSamples[piece][code];

					if (minSamples[piece] >= samples - 10) {

						dataset[best.piece].write(new InstanceRed(grid, best) + "\n");
						dataset[best.piece].flush();

						totalSamples[piece][code]++;
						
						if (minSamples[piece] == samples) {

							minSamples[piece]++;

							for (int i = 0; i < totalSamples[piece].length; i++)
								if (totalSamples[piece][i] < minSamples[piece])
									minSamples[piece] = totalSamples[piece][i];
						}
					}
				}

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				if (dataset != null) System.out.println("[Min samples: " + Arrays.toString(minSamples) + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = bot.classic.ClassicBot.searchRed(predDepth,
						InstanceRed.getSteps(grid), rand.nextInt(7)).fixRow(grid);
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	/**
	 * Tests the classic bot with only active piece plus piece prediction.
	 * If dataset is not null, it saves a instance for each move.
	 * @throws IOException 
	 */
	public static void testClassicPred(int predDepth, int delay, FileWriter[] dataset) throws IOException {

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
			Move best = bot.classic.ClassicBot.search(predDepth, grid, rand.nextInt(7));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				if (dataset != null) {

					dataset[best.piece].write(new Instance(grid, best) + "\n");
					dataset[best.piece].flush();
				}

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = bot.classic.ClassicBot.search(predDepth, grid, rand.nextInt(7));
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
	public static void testClassicNext(int delay) {

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
			Move best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece);
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);

				t0 = System.nanoTime();
				best = bot.classic.ClassicBot.search(grid, activePiece, nextPiece);
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	/**
	 * Tests the classic bot with only active piece.
	 */
	public static void testClassic(int delay) {

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
			Move best = bot.classic.ClassicBot.search(grid, rand.nextInt(7));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}

				t0 = System.nanoTime();
				best = bot.classic.ClassicBot.search(grid, rand.nextInt(7));
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}

	public static void testRandom(int delay) {

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

				totalEval += ClassicBot.eval(grid);

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) (iter - 1) + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println("[Avg eval: " + totalEval / totalMoves + "]");
				System.out.println();

				if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) {}

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
