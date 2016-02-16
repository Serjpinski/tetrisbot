package logic;

import java.util.ArrayList;

public class Piece {

	public static final int[][][][] REL_POS_LIST = new int[][][][]
	// int[] posRelABase[nPiezas][nEstados][nBloques/Pieza][(x,y)]
	{  {  {  {0,0},{1,0},{2,0},{2,1}  },  // pieza 0 (J), estadoDeGiro 0
		{  {1,0},{1,1},{1,2},{0,2}  },  // pieza 0 (J), estadoDeGiro 1
		{  {2,1},{1,1},{0,1},{0,0}  },  // pieza 0 (J), estadoDeGiro 2
		{  {0,2},{0,1},{0,0},{1,0}  }   // pieza 0 (J), estadoDeGiro 3
	},
	{  {  {0,0},{1,0},{1,1},{2,0}  },   // pieza 1 (T), estadoDeGiro 0
		{  {1,0},{1,1},{0,1},{1,2}  },   // pieza 1 (T), estadoDeGiro 1
		{  {2,1},{1,1},{1,0},{0,1}  },   // pieza 1 (T), estadoDeGiro 2
		{  {0,2},{0,1},{1,1},{0,0}  }    // pieza 1 (T), estadoDeGiro 3
	},
	{  {  {2,0},{1,0},{0,0},{0,1}  },   // pieza 2 (L), estadoDeGiro 0
		{  {1,2},{1,1},{1,0},{0,0}  },   // pieza 2 (L), estadoDeGiro 1
		{  {0,1},{1,1},{2,1},{2,0}  },   // pieza 2 (L), estadoDeGiro 2
		{  {0,0},{0,1},{0,2},{1,2}  }    // pieza 2 (L), estadoDeGiro 3
	},
	{  {  {0,0},{1,0},{2,0},{3,0}  },   // pieza 3 (I), estadoDeGiro 0
		{  {0,0},{0,1},{0,2},{0,3}  },   // pieza 3 (I), estadoDeGiro 1
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // no usada
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // no usada
	},
	{  {  {0,1},{1,1},{1,0},{2,0}  },   // pieza 4 (S), estadoDeGiro 0
		{  {0,0},{0,1},{1,1},{1,2}  },   // pieza 4 (S), estadoDeGiro 1
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // no usada
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // no usada
	},
	{  {  {0,0},{1,0},{1,1},{2,1}  },   // pieza 5 (Z), estadoDeGiro 0
		{  {1,0},{1,1},{0,1},{0,2}  },   // pieza 5 (Z), estadoDeGiro 1
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // no usada
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // no usada
	},
	{  {  {0,0},{1,0},{1,1},{0,1}  },   // pieza 6 (O), estadoDeGiro 0
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // no usada
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  },   // no usada
		{  {-1,-1},{-1,-1},{-1,-1},{-1,-1}  }    // no usada
	}
	};
	
	public final int type;
	public final int rotation;
	public final Position basePosition;
	public final Position[] relativePositions;
	public final Position[] absolutePositions;
	
	private ArrayList<Integer> linesCleared;
	private double score;
	
	/**
	 * Crea una pieza (posible movimiento) dados su tipo, estado de giro y posicion base.
	 */
	public Piece (int type, int rotation, Position basePosition) {
		
		this.type = type;
		this.rotation = rotation;
		this.basePosition = basePosition;
		
		relativePositions = new Position[4];
		
		for (int i = 0; i < 4; i++)
			relativePositions[i] = new Position(
					REL_POS_LIST[type][rotation][i][1],
					REL_POS_LIST[type][rotation][i][0]);
		
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
	
	public void setScore (double score) {
		
		this.score = score;
	}

	public static int numOfRotations (int type) {
		
		if (type >= 6) return 1;
		else if (type >= 3) return 2;
		else return 4;
	}

	/**
	 * Comprueba si la pieza puede ser colocada en el tablero.
	 */
	public boolean canBePlaced (boolean[][] grid) {
		
		return !intersects(grid) && !floats(grid) && canDrop(grid);
	}

	/**
	 * Comprueba si la pieza ocupa el espacio de otros bloques.
	 */
	private boolean intersects (boolean[][] grid) {

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
	 * Comprueba si la pieza esta flotando o esta apoyada en un bloque ya colocado.
	 * Nota: se considera que intersects(grid) devuelve false.
	 */
	private boolean floats (boolean[][] grid) {

		for (int i = 0; i < absolutePositions.length; i++) {
			
			Position position = absolutePositions[i];
			
			if (position.x == grid.length - 1) return false;
			else if (grid[position.x + 1][position.y] == true) return false;
		}
		
		return true;
	}

	/**
	 * Comprueba si la pieza tiene espacio libre para caer al objetivo.
	 * Nota: se considera que intersects(grid) devuelve false.
	 */
	private boolean canDrop (boolean[][] grid) {

		for (int i = 0; i < absolutePositions.length; i++) {
			
			Position position = absolutePositions[i];
			
			for (int x = 0; x < position.x; x++)
				if (grid[x][position.y] == true) return false;
		}
		
		return true;
	}

	/**
	 * Simula la colocacion de la pieza.
	 */
	public void place (boolean[][] grid) {
		
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
	 * Elimina las lineas necesarias despues de colocar la pieza.
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
	 * Retira la pieza colocada para la simulacion.
	 */
	public void remove (boolean[][] grid) {
		
		if (linesCleared != null) {
			
			restoreLines(grid);			
			linesCleared = null;
			
			for (int i = 0; i < absolutePositions.length; i++)
				grid[absolutePositions[i].x][absolutePositions[i].y] = false;
		}
	}

	/**
	 * Restaura las lineas eliminadas al poner la pieza.
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
}
