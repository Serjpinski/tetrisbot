package logic;

public class Position {

	public final int x; // Row
	public final int y; // Col
	
	public Position (int x, int y) {
		
		this.x = x;
		this.y = y;
	}
	
	public static Position sum (Position pos1, Position pos2) {
		
		return new Position(pos1.x + pos2.x, pos1.y + pos2.y);
	}
	
	public String toString () {
		
		return "[" + x + ", " + y + "]";
	}
}
