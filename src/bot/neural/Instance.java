package bot.neural;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import logic.Grid;
import logic.Move;
import logic.Position;

public class Instance {
	
	public boolean[][] grid;
	public int code;

	public Instance(boolean[][] grid, Move move) {
		
		this.grid = grid;
		this.code = move2Code(move);
	}
	
	public static int move2Code(Move move) {
		
		int offset = 0;
		
		for (int i = 0; i < move.rotation; i++)
			offset += Move.COL_VAR_LIST[move.piece][i];
		
		return offset + move.basePosition.y;
	}
	
	public static Move code2Move(int code, int piece, boolean[][] grid) {
		
		int offset = 0;
		int rotation = 0;
		int colVariance = Move.COL_VAR_LIST[piece][rotation];
		
		while (offset + colVariance <= code) {
			
			offset += colVariance;
			colVariance = Move.COL_VAR_LIST[piece][++rotation];
		}
		
		int col = code - offset;
		
		Move move = new Move(piece, rotation, new Position(0, col)).fixRow(grid);
		if (move != null) return move;
		
		System.out.println("piece " + piece + " code " + code + " rot " + rotation + " col " + col);
		move = new Move(piece, rotation, new Position(5, 5));
		move.place(grid);
		Grid.printGrid(grid);
		
		return null;
	}

	public static String getHeader() {
		
		String header = "";
		
		for (int i = 0; i < 200; i++) header += "i" + i + ", ";
		
		return header + "o";
	}
	
	public String toString() {
		
		String string = "";
		
		for (int i = 0; i < grid.length; i++)
			for (int j = 0; j < grid[0].length; j++)
				if (grid[i][j] == true) string += "1, ";
				else string += "0, ";
		
		return string + code;
	}
	
	public static FileWriter[] initDataset(String baseName) throws IOException {

		FileWriter[] dataset = new FileWriter[7];

		for (int i = 0; i < 7; i++) {

			File file = new File(System.getProperty("user.dir") + "/" + baseName + "piece" + i + ".csv");

			if (file.exists()) dataset[i] = new FileWriter(file, true);
			else {

				dataset[i] = new FileWriter(file, true);
				dataset[i].write(getHeader() + "\n");
			}
		}

		return dataset;
	}
}
