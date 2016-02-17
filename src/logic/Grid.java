package logic;

public class Grid {
	
	public static final int HEIGHT = 20;
	public static final int WIDTH = 10;
	
	/**
	 * Generates an empty grid.
	 * @return The new grid
	 */
	public static boolean[][] emptyGrid() {
		
		boolean[][] grid = new boolean[Grid.HEIGHT][Grid.WIDTH];
		for (int x = 0; x < grid.length; x++)
			for (int y = 0; y < grid[0].length; y++)
				grid[x][y] = false;
		
		return grid;
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
}
