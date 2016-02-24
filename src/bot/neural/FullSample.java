package bot.neural;

import logic.Move;

public class FullSample extends Sample {
	
	public boolean[][] grid;

	public FullSample(boolean[][] grid, Move move) {
		
		this.grid = grid;
		moveCode = Move.move2Code(move);
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < grid.length; i++)
			for (int j = 0; j < grid[0].length; j++)
				if (grid[i][j] == true) string += "1, ";
				else string += "0, ";
		
		return string + moveCode;
	}
}
