import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated,stick,square;

	// This shows how to build things in setUp() to re-use
	// across tests.

	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);

		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();

		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();

		stick = new Piece(Piece.STICK_STR);
		square = new Piece(Piece.SQUARE_STR);

		b.place(pyr1, 0, 0);
	}

	// Check the basic width/height/max after the one placement
	@Test
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}

	// Place sRotated into the board, then check some measures
	@Test
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}

	// Make  more tests, by putting together longer series of
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.

	// clearing out 3 rows
	@Test
	public void testBoard1(){
		b.commit();
		int result = b.place(stick, 0, 1);
		assertEquals(Board.PLACE_OK, result);

		b.commit();
		result = b.place(sRotated, 1, 1);

		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(5, b.getMaxHeight());
		assertEquals(4,b.getColumnHeight(1));
		assertEquals(3,b.getColumnHeight(2));
		assertEquals(2,b.getRowWidth(3));

		int numRowsCleared = b.clearRows();
		assertEquals(3,numRowsCleared);
		assertEquals(1,b.getRowWidth(1));

		b.undo();

		assertEquals(5, b.getMaxHeight());
		assertEquals(2,b.getColumnHeight(1));
		assertEquals(1,b.getColumnHeight(2));
		assertEquals(1,b.getRowWidth(4));

		//checking board width and height
		assertEquals(3, b.getWidth());
		assertEquals(6, b.getHeight());

	}

	// clearing out 4 rows
	@Test
	public void testBoard2(){
		assertTrue(b.getGrid(1, 1));

		b.undo();

		int result = b.place(square, 0, 0);
		b.commit();

		assertEquals(Board.PLACE_OK, result);

		result = b.place(square, 0, 2);
		b.commit();

		assertEquals(Board.PLACE_OK, result);

		result = b.place(square, 0, 4);
		b.commit();

		assertEquals(Board.PLACE_OK, result);

		result = b.place(stick, 2, 0);
		b.commit();

		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(6, b.getMaxHeight());

		int numRowsCleared = b.clearRows();

		assertEquals(4,numRowsCleared);
		assertEquals(2, b.getMaxHeight());
		assertEquals(2,b.getColumnHeight(1));
		assertEquals(0,b.getColumnHeight(2));
		assertEquals(2,b.getRowWidth(1));

		assertTrue(b.getGrid(78, 56));
	}

	// clearing rows with gaps in between
	// verifying dropHeights
	@Test
	public void testBoard3(){
		//checking board width and height
		assertEquals(3, b.getWidth());
		assertEquals(6, b.getHeight());

		b.commit();

		assertEquals(2,b.dropHeight(sRotated, 0));
		assertEquals(1,b.dropHeight(stick, 2));
		assertEquals(1,b.dropHeight(pyr2, 1));

		int result = b.place(pyr4, 0, 1);

		assertEquals(Board.PLACE_OK, result);
		assertEquals(2,b.dropHeight(sRotated, 1));
		assertEquals(1,b.dropHeight(stick, 2));
		assertEquals(2,b.getRowWidth(2));

		b.undo();

		assertEquals(2,b.getMaxHeight());

		result = b.place(pyr3, 0, 2);

		assertEquals(Board.PLACE_ROW_FILLED, result);

		b.commit();
		result = b.place(pyr3, 0, 4);

		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(3,b.clearRows());

	}

	@Test
	public void testBoard4(){
		assertEquals(3,b.getRowWidth(0));
		assertEquals(1,b.clearRows());

		b.commit();

		assertEquals(1,b.getRowWidth(0));

		int result = b.place(pyr4, 0, 0);
		int rowsCleared = b.clearRows();
		b.commit();

		assertEquals(Board.PLACE_OK, result);
		assertEquals(0,rowsCleared);
		assertEquals(3,b.getMaxHeight());

		result = b.place(sRotated, 1, 1);
		assertEquals(4,b.getMaxHeight());
		rowsCleared = b.clearRows();
		b.commit();

		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(2,rowsCleared);
		assertEquals(2,b.getMaxHeight());

		result = b.place(sRotated, 1, 1);
		rowsCleared = b.clearRows();

		assertEquals(Board.PLACE_OK, result);
		assertEquals(0,rowsCleared);
		assertEquals(4,b.dropHeight(square, 1));
		assertEquals(3,b.dropHeight(sRotated, 1));
		assertEquals(2,b.getRowWidth(0));
		assertEquals(1,b.getRowWidth(3));
		assertEquals(4,b.getColumnHeight(1));

		assertTrue(!b.getGrid(0, 1));
		assertTrue(b.getGrid(1, 3));
	}

}
