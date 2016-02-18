package bot.neural;

import logic.Grid;
import logic.Move;

public class Instance {
	
	private int[] steps;
	private int piece;
	private int moveCode;

	public Instance(boolean[][] grid, int piece, Move move) {
		
		steps = getSteps(grid);
		this.piece = piece;
		this.moveCode = move2Code(piece, move);
	}
	
	private static int[] getSteps(boolean[][] grid) {
		
		int[] heights = Grid.getHeights(grid);
		int[] steps = new int[heights.length - 1];
		
		for (int i = 0; i < steps.length; i++)
			steps[i] = Math.max(-3, Math.min(3, heights[i + 1] - heights[i]));
		
		return steps;
	}
	
	private static int move2Code(int piece, Move move) {
		
		int offset = 0;
		
		for (int i = 0; i < move.rotation; i++) {
			
			int[][] posList = Move.REL_POS_LIST[piece][i];
			
			int colMin = posList[0][1];
			int colMax = posList[0][1];
			
			for (int j = 0; j < posList.length; j++) {
				
				if (colMin > posList[i][1]) colMin = posList[i][1];
				if (colMax < posList[i][1]) colMax = posList[i][1];
			}
			
			offset += colMax - colMin + 1;
		}
		
		return offset + move.basePosition.y;
	}
	
	private static Move code2Move(int piece, int code) {
		
		// TODO
		
		return null;
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < steps.length; i++) string += steps[i] + ", ";
		
		return string + piece + ", " + moveCode + "\n";
	}
}
