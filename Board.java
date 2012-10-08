// Board.java

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private final int width;
	private final int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	
	private int[] filled;
	private int[] heights;
	
	private enum State { TRUE, FALSE, BACKED_UP }
	private class Move {
		public Piece piece;
		public int x, y;
		public Move(Piece p, int a, int b) {
			piece = p;
			x = a;
			y = b;
		}
	}
	
	private State committed;
	private Move last;
	private boolean[][] gridBackup;
	private int[] filledBackup;
	private int[] heightsBackup;
	
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];	
		
		filled = new int[height];
		heights = new int[width];
		
		committed = State.TRUE;
		gridBackup = new boolean[width][height];
		filledBackup = new int[height];
		heightsBackup = new int[width];
		last = new Move(new Piece(""), 0, 0);
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		int max = 0;
		for(int i = 0; i < width; i++) {
			if(heights[i] > max) max = heights[i];
		}
		return max;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int max = 0, h;
		for(int i = 0; i < piece.getWidth(); i++) {
			if(x + i >= 0 && x + i < width && 
					(h = heights[x + i] - piece.getSkirt()[i]) > max) {
				max = h;
			}
		}
		return max;
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		assert (x >= 0 && x < width) : "x out of bounds in getColumnHeight";
		return heights[x];
	}
	
	private void fixHeights() {
		for(int i = 0; i < width; i++) {
			while(heights[i] > 0 && !grid[i][heights[i] - 1])
				heights[i]--;
		}
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		assert (y >= 0 && y < height) : "y out of bounds in getRowWidth";
		 return filled[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height)
			return true;
		return grid[x][y];
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (committed != State.TRUE) throw new RuntimeException("place commit problem");
		
		if(x < 0 || y < 0 || x + piece.getWidth() > width || y + piece.getHeight() > height) 
			return PLACE_OUT_BOUNDS;
		for(TPoint p : piece.getBody()) {
			if(grid[p.x + x][p.y + y]) return PLACE_BAD;
		}
		
		committed = State.FALSE;
		last = new Move(piece, x, y);
		
		System.arraycopy(heights, 0, heightsBackup, 0, heights.length);
		for(TPoint p : piece.getBody()) {
			grid[p.x + x][p.y + y] = true;
			filled[p.y + y]++;
			if(heights[p.x + x] < p.y + y + 1)
				heights[p.x + x] = p.y + y + 1;
		}
		for(int i = y; i < y + piece.getHeight(); i++) {
			if(filled[i] == width) return PLACE_ROW_FILLED;
		}
		
		return PLACE_OK;
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		committed = State.FALSE;
		backup();
		
		int rowsCleared = 0;
		
		int next = 0, maxHeight = getMaxHeight();
		for(int i = 0; i < maxHeight; i++) {
			if(filled[i] == width) {
				rowsCleared++;
			} else if (filled[i] > 0){
				copyRow(next, i);
				next++;
			}
		}
		while(next < maxHeight) {
			copyRow(next, height);
			next++;
		}
		
		for(int i = 0; i < width; i++) {
			heights[i] -= rowsCleared;
		}
		fixHeights();
		
		// YOUR CODE HERE
		sanityCheck();
		return rowsCleared;
	}
	
	private void copyRow(int dest, int src) {
		if(dest == src) return;
		boolean outOfBounds = (src >= height || src < 0);
		filled[dest] = (outOfBounds) ? 0 : filled[src];
		for(int i = 0; i < width; i++) {
			grid[i][dest] = (outOfBounds) ? false : grid[i][src];
		}
	}



	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(committed == State.TRUE) return;
		else {
			int[] temp3 = heights;
			heights = heightsBackup;
			heightsBackup = temp3;	
		
			if(committed == State.BACKED_UP) {
				boolean[][] temp1 = grid;
				grid = gridBackup;
				gridBackup = temp1;

				int[] temp2 = filled;
				filled = filledBackup;
				filledBackup = temp2;		
			} else {
				for(TPoint point : last.piece.getBody()) {
					grid[point.x + last.x][point.y + last.y] = false;
					filled[point.y + last.y]--;
				}
			}
		}
		
		committed = State.TRUE;
	}
	
	private void backup() {
		System.arraycopy(filled, 0, filledBackup, 0, filled.length);
		System.arraycopy(heights, 0, heightsBackup, 0, heights.length);
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				gridBackup[i][j] = grid[i][j];
			}
		}
		if(committed == State.FALSE) {
			committed = State.BACKED_UP;
		}
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = State.TRUE;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


