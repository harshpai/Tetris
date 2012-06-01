import java.util.Arrays;

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
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = false;
	private boolean committed;
	private int maxHeight;
	private int widths[];  // secondary arrays that store widths and heights
	private int heights[]; // of filled blocks

	//backup ivars for undo
	private boolean[][] gridBackup;
	private int widthsBackup[];
	private int heightsBackup[];


	// Here a few trivial methods are provided:

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;

		widths = new int[height];
		heights = new int[width];

		//initialize backup arrays
		gridBackup = new boolean[width][height];
		widthsBackup = new int[height];
		heightsBackup = new int[width];

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


		return maxHeight;
	}

	/**
	 * private helper method that recomputes the maxHeight field
	 * */
	private void recomputeMaxheight()
	{
		maxHeight = 0;
		for (int i = 0 ; i<heights.length;i++)
		{
			if(maxHeight<heights[i])
				maxHeight = heights[i];
		}
	}

	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	 */
	public void sanityCheck() {

		if (DEBUG) {

			System.out.print(this);
			int[] widthsCheck = new int[height];
			int maxHeightCheck =0;
			for(int i =0; i< width;i++){
				int heightCheck = 0;
				for(int j =0; j< height;j++){
					if(grid[i][j])
					{
						heightCheck = j+1;
						widthsCheck[j]++;

						if(maxHeightCheck<j+1)
							maxHeightCheck = j+1;
					}
				}
				if(heightCheck!=heights[i])
					throw new RuntimeException("Heights check failed");
			}
			if(!Arrays.equals(widthsCheck, widths))
				throw new RuntimeException("Widths check failed");

			if(maxHeightCheck != maxHeight)
				throw new RuntimeException("Max Height check failed");

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
		int result = 0;
		int[] skirt = piece.getSkirt();
		for(int i =0 ; i < skirt.length;i++)
		{
			int y = heights[x+i]-skirt[i];
			if(y>result)
				result=y;
		}
		return result;
	}


	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		return heights[x];
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	 */
	public int getRowWidth(int y) {
		return widths[y];
	}


	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	 */
	public boolean getGrid(int x, int y) {
		return (x<0 || y< 0 || x>=width || y >=height || grid[x][y]);
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
		if (!committed) throw new RuntimeException("place commit problem");
		committed = false;
		backup();
		int result = PLACE_OK;
		int pieceX,pieceY;

		TPoint body[] = piece.getBody();
		for(int i =0; i < body.length;i++)
		{
			pieceX = x+body[i].x;
			pieceY = y+body[i].y;

			if(pieceX<0 || pieceY< 0 || pieceX>=width || pieceY >=height )
			{
				result = PLACE_OUT_BOUNDS;
				break;
			}

			if(grid[pieceX][pieceY])
			{
				result = PLACE_BAD;
				break;
			}

			grid[pieceX][pieceY] = true;

			if(heights[pieceX]<pieceY+1)
				heights[pieceX]=pieceY+1;

			widths[pieceY]++;

			if(widths[pieceY] == width)
				result = PLACE_ROW_FILLED;
		}
		recomputeMaxheight();
		sanityCheck();
		return result;
	}


	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		if(committed)
		{
			committed=false;
			backup();
		}

		boolean hasFilledRow = false;
		int rowTo,rowFrom,rowsCleared;
		rowsCleared = 0;

		// clearing row using a single pass method given in the handout
		for(rowTo=0,rowFrom =1;rowFrom<maxHeight;rowTo++,rowFrom++)
		{
			if(!hasFilledRow && widths[rowTo]==width)
			{
				hasFilledRow=true;
				rowsCleared++;
			}

			while(hasFilledRow && rowFrom<maxHeight && widths[rowFrom]==width)
			{
				rowsCleared++;
				rowFrom++;
			}

			if(hasFilledRow)
				copySingleRow(rowTo,rowFrom);

		}

		if(hasFilledRow)
			fillEmptyRows(rowTo,maxHeight);

		// This is my version of optimized code to quickly update the heights array.
		// Rather than iterating through the grid every time to compute the heights
		// we do the following.
		// Case 1) Usually the height of every column decreases by the no of rows cleared
		// Case 2) The exception to this happens when there is a gap below the cleared out row.
		// Here we compute the height of the column using a for loop in O(n) time.
		// Example
		// ++++++  <-- row cleared out
		// +++ ++  <-- gap in the 4th column makes updated height
		// +++ ++  <-- of the 4th column using case 1 invalid
		// + ++++
		for(int i =0;i < heights.length;i++)
		{
			heights[i]-=rowsCleared;
			if(heights[i]>0 && !grid[i][heights[i]-1])
			{
				heights[i]=0;
				for (int j = 0;j<maxHeight;j++ )
					if(grid[i][j])
						heights[i] = j+1;
			}
		}

		recomputeMaxheight();

		sanityCheck();
		return rowsCleared;
	}


	/**
	 * private helper method that fills empty rows between
	 * specified low and high rows
	 * */
	private void fillEmptyRows(int lowRow, int highRow) {

		for(int j = lowRow;j<highRow;j++){
			widths[j]=0;
			for(int i = 0;i<width;i++)
				grid[i][j] =false;

		}
	}


	/**
	 * private helper method that copies a single row
	 * If the rowFrom parameter is more than the max row
	 * index specified by maxHeight, then empty the row
	 * pointed by rowTo.
	 * */
	private void copySingleRow(int rowTo, int rowFrom) {

		if(rowFrom<maxHeight)
		{
			for(int i = 0;i<width;i++)
			{
				grid[i][rowTo] = grid[i][rowFrom];
				widths[rowTo] = widths[rowFrom];
			}
		}
		else
		{
			for(int i = 0;i<width;i++)
			{
				grid[i][rowTo] = false;
				widths[rowTo] = 0;
			}
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
		if(!committed)
			swap();
		commit();
		sanityCheck();
	}


	/**
	 Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}



	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility)
	 */
	@Override
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

	/**
	 * private helper method to make a backup of board state before it is modified
	 * */
	private void backup(){

		System.arraycopy(widths, 0, widthsBackup, 0, widths.length);
		System.arraycopy(heights, 0, heightsBackup, 0, heights.length);
		for(int i =0;i<grid.length;i++)
			System.arraycopy(grid[i], 0, gridBackup[i], 0, grid[i].length);
	}

	/**
	 * swaps the pointers to "backup" and "main" to do a quick and dirty restore
	 * */
	private void swap(){

		int[] temp = widthsBackup;
		widthsBackup = widths;
		widths = temp;

		temp = heightsBackup;
		heightsBackup = heights;
		heights = temp;

		boolean[][] gridtemp = gridBackup;
		gridBackup = grid;
		grid = gridtemp;

		recomputeMaxheight();
	}
}


