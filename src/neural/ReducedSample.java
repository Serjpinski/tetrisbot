package neural;

import java.util.Scanner;

import core.Grid;
import core.Move;

public class ReducedSample extends Sample {
	
	public int[] steps;

	public ReducedSample(boolean[][] grid, Move move) {
		
		steps = Grid.getSteps(grid);
		moveCode = Move.move2Code(move);
	}

	public ReducedSample(String string) {
		
		steps = new int[9];		
		Scanner s = new Scanner(string);
		s.useDelimiter(", ");
		
		for (int i = 0; i < steps.length; i++) steps[i] = s.nextInt();
		
		moveCode = s.nextInt();
		s.close();
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < steps.length; i++) string += steps[i] + ", ";
		
		return string + moveCode;
	}
}
