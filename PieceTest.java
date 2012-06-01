import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4 ;
	private Piece square1,square2,square3;
	private Piece stick1, stick2, stick3;
	private Piece L21,L22,L24;
	private Piece s, sRotated;
	private Piece[] pieces;

	@Before
	public void setUp() throws Exception {

		pyr1 = new Piece(Piece.PYRAMID_STR);
		square1 = new Piece(Piece.SQUARE_STR);
		stick1 = new Piece(Piece.STICK_STR);
		L21 = new Piece(Piece.L2_STR);
		square2 = square1.computeNextRotation();
		square3 = square2.computeNextRotation();
		stick2 = stick1.computeNextRotation();
		stick3 = stick2.computeNextRotation();
		L22 = L21.computeNextRotation();
		L24 = L22.computeNextRotation().computeNextRotation().computeNextRotation();
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		pieces = Piece.getPieces();
	}

	//Sample tests (provided)

	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());

		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());

		// Now try with some other piece, made a different way
		assertEquals(1, stick1.getWidth());
		assertEquals(4, stick1.getHeight());
		assertEquals(4, stick2.getWidth());
		assertEquals(1, stick2.getHeight());

		//square
		assertEquals(2,square1.getHeight());
		assertEquals(2,square3.getHeight());

		//L and s
		assertEquals(3,L22.getWidth());
		assertEquals(3,sRotated.getHeight());
	}


	// Test the skirt returned by a few pieces
	@Test
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));

		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));

		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, stick2.getSkirt()));

		assertTrue(Arrays.equals(new int[] {1, 1, 0}, L22.getSkirt()));
	}

	// Test the equals on various pieces
	@Test
	public void testEquals() {
		//obvious fail
		assertTrue(!sRotated.equals(pyr3));
		assertTrue(!L22.equals(stick3));

		//check equals using a square
		assertTrue(square1.equals(square2));
		assertTrue(square1.equals(square3));
		assertTrue(!square1.equals(new Piece("1 0 0 0 2 0 1 1")));
		assertTrue(square1.equals(new Piece("1 0 0 0 1 1 0 1")));

		//check equals using stick and s
		assertTrue(!stick1.equals(stick2));
		assertTrue(stick1.equals(stick3));
		assertTrue(sRotated.equals(new Piece("0 1 1 1 0 2 1 0")));

		//have fun with L
		assertTrue(L21.equals(L24));
		assertTrue(!L21.equals(new Piece("0 0 1 0 0 1 0 2")));

		//check pyramid
		assertTrue(!pyr1.equals(pyr3));
		assertTrue(pyr2.equals(new Piece("1 1 1 0 0 1 1 2")));
		assertTrue(pyr3.equals(new Piece("0 1 1 1 2 1 1 0")));
		assertTrue(pyr4.equals(new Piece("1 1 0 2 0 1 0 0")));
	}

	// Test the fastRotation on various pieces
	@Test
	public void testFastRotation() {

		//check 1 or 2 rotations
		assertTrue(stick1.equals(pieces[Piece.STICK]));
		assertTrue(stick2.equals(pieces[Piece.STICK].fastRotation()));
		assertTrue(stick1.equals(pieces[Piece.STICK].fastRotation().fastRotation()));

		//rotate more than 4 times to check rotations are working correctly
		assertTrue(L24.equals(pieces[Piece.L2].fastRotation().fastRotation().fastRotation().fastRotation()));
		assertTrue(sRotated.equals(pieces[Piece.S1].fastRotation().fastRotation().fastRotation().fastRotation().fastRotation()));
		assertTrue(pyr3.equals(pieces[Piece.PYRAMID].fastRotation().fastRotation().fastRotation().fastRotation().fastRotation().fastRotation()));
		assertTrue(square2.equals(pieces[Piece.SQUARE].fastRotation().fastRotation().fastRotation().fastRotation().fastRotation().fastRotation().fastRotation()));
	}

}
