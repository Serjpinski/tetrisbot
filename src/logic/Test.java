package logic;

import java.util.Random;

public class Test {

	public static void main (String[] args) {

		testClassic(true);
	}

	/**
	 * Tests the classic bot.
	 */
	private static void testClassic(boolean relaxMode) {

		Random rand = new Random();

		int iter = 1;
		long totalLines = 0;
		int minLines = -1;
		int maxLines = -1;
		long totalMoves = 0;
		double totalMoveTime = 0;

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

				Grid.printGrid(grid);
				System.out.println("[Lines: " + lines + "]");
				System.out.println("[Iteration: " + iter + "]");
				System.out.println("[Avg lines: " + totalLines / (double) iter + "]");
				System.out.println("[Min lines: " + minLines + "]");
				System.out.println("[Max lines: " + maxLines + "]");
				System.out.println("[Avg move time: " + totalMoveTime / totalMoves + "]");
				System.out.println();

				if (relaxMode) try { Thread.sleep(1); } catch (InterruptedException e) {}

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
