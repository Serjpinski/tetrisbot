package logic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import bot.classic.Bot;
import bot.neural.DatasetGenerator;
import bot.neural.Instance;

public class Test {

	public static void main (String[] args) throws IOException {

		testClassicPred(2, 1, DatasetGenerator.initDataset());
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
			Move best = bot.classic.Bot.search(grid, rand.nextInt(7));
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();
				
				totalEval += Bot.eval(grid);

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
				best = bot.classic.Bot.search(grid, rand.nextInt(7));
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
			Move best = bot.classic.Bot.search(predDepth, grid, rand.nextInt(7));
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
				
				totalEval += Bot.eval(grid);

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
				best = bot.classic.Bot.search(predDepth, grid, rand.nextInt(7));
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
			Move best = bot.classic.Bot.search(grid, activePiece, nextPiece);
			long t1 = System.nanoTime() - t0;

			while (best != null) {

				totalMoveTime += t1 / 1000000.0; // Move time in ms
				totalMoves++;

				best.place(grid);
				lines += best.getLinesCleared();
				
				totalEval += Bot.eval(grid);

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
				best = bot.classic.Bot.search(grid, activePiece, nextPiece);
				t1 = System.nanoTime() - t0;
			}

			if (lines > maxLines) maxLines = lines;
			if (minLines == -1 || lines < minLines) minLines = lines;
			totalLines += lines;

			iter++;
		}
	}
}
