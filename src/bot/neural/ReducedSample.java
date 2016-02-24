package bot.neural;

import logic.Grid;
import logic.Move;

public class ReducedSample extends Sample {
	
	public int[] steps;

	public ReducedSample(boolean[][] grid, Move move) {
		
		steps = Grid.getSteps(grid);
		moveCode = Move.move2Code(move);
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < steps.length; i++) string += steps[i] + ", ";
		
		return string + moveCode;
	}
}
