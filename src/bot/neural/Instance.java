package bot.neural;

import logic.Grid;
import logic.Move;
import logic.Position;

public class Instance {
	
	private int[] steps;
	private int moveCode;

	public Instance(boolean[][] grid, Move move) {
		
		steps = getSteps(grid);
		this.moveCode = move2Code(move);
	}
	
	private static int[] getSteps(boolean[][] grid) {
		
		int[] heights = Grid.getHeights(grid);
		int[] steps = new int[heights.length - 1];
		
		for (int i = 0; i < steps.length; i++)
			steps[i] = Math.max(-3, Math.min(3, heights[i + 1] - heights[i]));
		
		return steps;
	}
	
	private static int move2Code(Move move) {
		
		int offset = 0;
		
		for (int i = 0; i < move.rotation; i++)
			offset += Move.COL_VAR_LIST[move.piece][i];
		
		return offset + move.basePosition.y;
	}
	
	public static Move code2Move(int code, int piece, boolean[][] grid) {
		
		int offset = 0;
		int rotation = 0;
		int colVariance = Move.COL_VAR_LIST[piece][rotation];
		
		while(offset + colVariance <= code) {
			
			offset += colVariance;
			colVariance = Move.COL_VAR_LIST[piece][++rotation];
		}
		
		int col = code - offset;
		
		for (int i = 0; i < grid.length; i++) {
			
			Move move = new Move(piece, rotation, new Position(i, col));
			if (move.canBePlaced(grid)) return move;
		}
		
		return null;
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < steps.length; i++) string += steps[i] + ", ";
		
		return string + moveCode;
	}
}
