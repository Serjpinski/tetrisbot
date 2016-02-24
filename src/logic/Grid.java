package logic;

public class Grid {
	
	public static final int HEIGHT = 20;
	public static final int WIDTH = 10;
	
	/**
	 * Generates an empty grid.
	 * @return The new grid
	 */
	public static boolean[][] emptyGrid() {
		
		return new boolean[Grid.HEIGHT][Grid.WIDTH];
	}

	/**
	 * Draws the grid.
	 */
	public static void printGrid(boolean[][] grid) {

		for (int i = 0; i < grid.length; i++) {

			System.out.print("|");

			for (int j = 0; j < grid[0].length; j++) {

				if (grid[i][j] == true) System.out.print("\u2588\u2588");
				else System.out.print("  ");
			}

			System.out.println("|");
		}
	}
	
	/**
	 * Computes the heights of the grid
	 */
	public static int[] getHeights(boolean[][] grid) {
		
		int[] heights = new int[grid[0].length];
		
		for (int j = 0; j < grid[0].length; j++) {
			
			int height = 0;
			
			for (int i = 0; i < grid.length && height == 0; i++)
				if (grid[i][j] == true) height = grid.length - i;
			
			heights[j] = height;
		}
		
		return heights;
	}
	
	/**
	 * Computes the average height of the grid
	 */
	public static double getAvgHeight(boolean[][] grid) {
		
		int[] heights = getHeights(grid);
		
		int totalHeight = 0;
		
		for (int i = 0; i < grid[0].length; i++) totalHeight += heights[i];
		
		return totalHeight / (double) grid[0].length;
	}
	
	public static int[] getSteps(boolean[][] grid) {
		
		int[] heights = Grid.getHeights(grid);
		int[] steps = new int[heights.length - 1];
		
		for (int i = 0; i < steps.length; i++)
			steps[i] = Math.max(-3, Math.min(3, heights[i + 1] - heights[i]));
		
		return steps;
	}
}
