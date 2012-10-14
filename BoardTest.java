import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated, stick, stickRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(4, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		stick = new Piece(Piece.STICK_STR);
		stickRotated = stick.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	@Test
	public void baseTest() {
		assertEquals(4, b.getWidth());
		assertEquals(6, b.getHeight());
		assertTrue(b.getGrid(0, 0));
		assertTrue(b.getGrid(1, 1));
		assertFalse(b.getGrid(0, 1));
		assertTrue(b.getGrid(-1, 0));
		assertTrue(b.getGrid(2, 6));
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
	
	@Test
	public void testStickUndo() {
		b.commit();
		
		
		int dropHeight = b.dropHeight(stickRotated, 0);
		assertEquals(2, dropHeight);
		assertEquals(Board.PLACE_ROW_FILLED, b.place(stickRotated, 0, dropHeight));
		
		assertEquals(4, b.getRowWidth(2));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(3));
		
		b.undo();
		
		assertEquals(0, b.getRowWidth(2));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(0, b.getColumnHeight(3));
	}
	
	@Test
	public void testStickClearRows() {
		b.commit();
		b.place(stickRotated, 0, 2);
		
		assertEquals(1, b.clearRows());
		
		assertEquals(0, b.getRowWidth(2));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(0, b.getColumnHeight(3));
		
		b.commit();
	}
	
	@Test
	public void testClearAndUndo() {
		b.commit();
		assertEquals(Board.PLACE_ROW_FILLED, b.place(stick, 3, 0));
		assertEquals(1, b.clearRows());
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(1, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(3));
		
		b.undo();
		
	}
	
	@Test
	public void testInvalidPlaces() {
		b.commit();
		b.place(stickRotated, 0, 2);
		try {
			b.place(stickRotated, 0, 3);
			assert false : "Should throw runtime exception";
		} catch (RuntimeException e) {
			//empty
		}
		b.commit();
		assertEquals(Board.PLACE_BAD, b.place(stickRotated, 0, 1));
		assertEquals(Board.PLACE_OUT_BOUNDS, b.place(stickRotated, -1, 3));
		assertEquals(Board.PLACE_OUT_BOUNDS, b.place(stickRotated, 0, -1));
		assertEquals(Board.PLACE_OUT_BOUNDS, b.place(stickRotated, 1, 3));
		assertEquals(Board.PLACE_OUT_BOUNDS, b.place(stickRotated, 0, 6));	
	}
	
	
}
