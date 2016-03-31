package neural;

import java.util.Scanner;

import core.Grid;
import core.Move;

public class FullSample extends Sample {
	
	public boolean[][] grid;

	public FullSample(boolean[][] grid, Move move) {
		
		this.grid = grid;
		moveCode = Move.move2Code(move);
	}
	
	public FullSample(String string) {
		
		grid = Grid.emptyGrid();		
		Scanner s = new Scanner(string);
		s.useDelimiter(", ");
		
		for (int i = 0; i < grid.length; i++)
			for (int j = 0; j < grid[0].length; j++)
				grid[i][j] = s.nextInt() == 1;
		
		moveCode = s.nextInt();
		s.close();
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
