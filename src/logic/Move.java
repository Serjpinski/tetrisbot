package logic;

import java.util.ArrayList;

public class Move {

	public static final int[][][][] REL_POS_LIST = new int[][][][] {

		// REL_POS_LIST[piece][rotation][block][(x,y)]
		{  {  {0,0},{0,1},{0,2},{1,2}  },  // piece 0 (J), rotation 0
			{  {0,1},{1,1},{2,1},{2,0}  },  // piece 0 (J), rotation 1
			{  {1,2},{1,1},{1,0},{0,0}  },  // piece 0 (J), rotation 2
			{  {2,0},{1,0},{0,0},{0,1}  }   // piece 0 (J), rotation 3
		},
		{  {  {0,0},{0,1},{1,1},{0,2}  },   // piece 1 (T), rotation 0
			{  {0,1},{1,1},{1,0},{2,1}  },   // piece 1 (T), rotation 1
			{  {1,2},{1,1},{0,1},{1,0}  },   // piece 1 (T), rotation 2
			{  {2,0},{1,0},{1,1},{0,0}  }    // piece 1 (T), rotation 3
		},
		{  {  {0,2},{0,1},{0,0},{1,0}  },   // piece 2 (L), rotation 0
			{  {2,1},{1,1},{0,1},{0,0}  },   // piece 2 (L), rotation 1
			{  {1,0},{1,1},{1,2},{0,2}  },   // piece 2 (L), rotation 2
			{  {0,0},{1,0},{2,0},{2,1}  }    // piece 2 (L), rotation 3
		},
		{  {  {0,0},{0,1},{0,2},{0,3}  },   // piece 3 (I), rotation 0
			{  {0,0},{1,0},{2,0},{3,0}  },   // piece 3 (I), rotation 1
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // not used
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // not used
		},
		{  {  {1,0},{1,1},{0,1},{0,2}  },   // piece 4 (S), rotation 0
			{  {0,0},{1,0},{1,1},{2,1}  },   // piece 4 (S), rotation 1
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // not used
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // not used
		},
		{  {  {0,0},{0,1},{1,1},{1,2}  },   // piece 5 (Z), rotation 0
			{  {0,1},{1,1},{1,0},{2,0}  },   // piece 5 (Z), rotation 1
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // not used
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // not used
		},
		{  {  {0,0},{0,1},{1,1},{1,0}  },   // piece 6 (O), rotation 0
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // not used
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // not used
			{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // not used
		}
	};
	
	// COL_VAR_LIST[piece][rotation]
	public static final int[][] COL_VAR_LIST = new int[][] {
		
		{8, 9, 8, 9},
		{8, 9, 8, 9},
		{8, 9, 8, 9},
		{7, 10},
		{8, 9},
		{8, 9},
		{9}
	};
	
	// COL_VAR_SUM_LIST[piece]
	public static final int[] COL_VAR_SUM_LIST = new int[] {34, 34, 34, 17, 17, 17, 9};
	
	// NUM_ROT_LIST[piece]
	public static final int[] NUM_ROT_LIST = new int[] {4, 4, 4, 2, 2, 2, 1};

	public final int piece;
	public final int rotation;
	public final Position basePosition;
	public final Position[] relativePositions;
	public final Position[] absolutePositions;

	private ArrayList<Integer> linesCleared;
	private double score;

	/**
	 * Creates a move (possible piece placement) given its piece type, rotation
	 * and base position.
	 */
	public Move(int piece, int rotation, Position basePosition) {

		this.piece = piece;
		this.rotation = rotation;
		this.basePosition = basePosition;

		relativePositions = new Position[4];

		for (int i = 0; i < 4; i++)
			relativePositions[i] = new Position(
					REL_POS_LIST[piece][rotation][i][0],
					REL_POS_LIST[piece][rotation][i][1]);

		absolutePositions = new Position[4];

		for (int i = 0; i < absolutePositions.length; i++)
			absolutePositions[i] = Position.sum(relativePositions[i], basePosition);

		linesCleared = null;
		score = 0;
	}

	public int getLinesCleared() {

		if (linesCleared == null) return -1;
		return linesCleared.size();
	}

	public double getScore () {

		return score;
	}

	public void setScore(double score) {

		this.score = score;
	}

	/**
	 * Checks if the piece can be placed on the grid.
	 */
	public boolean canBePlaced(boolean[][] grid) {

		return !intersects(grid) && !floats(grid) && canDrop(grid);
	}

	/**
	 * Checks if the piece overlaps with already placed blocks.
	 */
	private boolean intersects(boolean[][] grid) {

		for (int i = 0; i < absolutePositions.length; i++) {

			Position position = absolutePositions[i];

			if (position.x < 0 || position.x >= grid.length
					|| position.y < 0 || position.y >= grid[0].length)
				return true;

			if (grid[position.x][position.y] == true) return true;
		}

		return false;
	}

	/**
	 * Checks if the piece is "floating" (does not rest on a placed block).
	 */
	private boolean floats(boolean[][] grid) {

		for (int i = 0; i < absolutePositions.length; i++) {

			Position position = absolutePositions[i];

			if (position.x == grid.length - 1) return false;
			else if (grid[position.x + 1][position.y] == true) return false;
		}

		return true;
	}

	/**
	 * Checks if the piece can drop to the target position.
	 */
	private boolean canDrop(boolean[][] grid) {

		for (int i = 0; i < absolutePositions.length; i++) {

			Position position = absolutePositions[i];

			for (int x = 0; x < position.x; x++)
				if (grid[x][position.y] == true) return false;
		}

		return true;
	}

	/**
	 * Simulates the placement of the piece.
	 */
	public void place(boolean[][] grid) {

		if (linesCleared == null) {

			for (int i = 0; i < absolutePositions.length; i++)
				grid[absolutePositions[i].x][absolutePositions[i].y] = true;

			linesCleared = new ArrayList<Integer>();

			for (int i = 0; i < grid.length; i++) {

				boolean cleared = true;

				for (int j = 0; j < grid[0].length; j++) {

					if (grid[i][j] == false) cleared = false;				
				}

				if (cleared) {

					linesCleared.add(i);
				}
			}

			clearLines(grid);
		}
	}

	/**
	 * Removes the lines filled after simulating the move.
	 */
	private void clearLines(boolean[][] grid) {

		for (int k = 0; k < linesCleared.size(); k++) {

			for (int i = linesCleared.get(k); i >= 0; i--) {

				for (int j = 0; j < grid[0].length; j++) {

					if (i == 0) grid[i][j] = false;
					else grid[i][j] = grid[i-1][j];
				}
			}
		}
	}

	/**
	 * Undoes the simulated placement.
	 */
	public void remove(boolean[][] grid) {

		if (linesCleared != null) {

			restoreLines(grid);			
			linesCleared = null;

			for (int i = 0; i < absolutePositions.length; i++)
				grid[absolutePositions[i].x][absolutePositions[i].y] = false;
		}
	}

	/**
	 * Restores the cleared lines in the simulation.
	 */
	private void restoreLines(boolean[][] grid) {

		for (int k = linesCleared.size() - 1; k >= 0; k--) {

			for (int i = 0; i <= linesCleared.get(k); i++) {

				for (int j = 0; j < grid[0].length; j++) {

					if (i == linesCleared.get(k)) grid[i][j] = true;
					else grid[i][j] = grid[i+1][j];
				}
			}
		}
	}
	
	/**
	 * Returns the move equivalent to this but that is correctly placed,
	 * or null if it does not exist.
	 */
	public Move fixRow(boolean[][] grid) {
		
		for (int i = 0; i < grid.length; i++) {
			
			Move move = new Move(piece, rotation, new Position(i, basePosition.y));
			if (move.canBePlaced(grid)) return move;
		}
		
		return null;
	}
}
