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
	private Piece pyr1, pyr2, pyr3;
	private Piece s, sRotated;

	
	@Before
	public void setUp() throws Exception {
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
	}
	
	// Here are some sample tests to get you started
	
	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		assertPieceEquals(pyr2, new Piece("0 1 1 0 1 1 1 2"));
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
		
		for(Piece p : Piece.getPieces()) {
			assertTrue(p.getWidth() < 5 && p.getWidth() > 0);
			assertTrue(p.getHeight() < 5 && p.getHeight() > 0);
		}
	}
	
	@Test
	public void testStringConstructor() {
		assertTrue((new Piece("0 0")).equals(new Piece(new TPoint[]{ new TPoint(0,0) })));
		assertTrue((new Piece(Piece.SQUARE_STR)).equals(Piece.getPieces()[Piece.SQUARE]));
		assertTrue((new Piece("1 1 0 0 1 0 0 1")).equals(Piece.getPieces()[Piece.SQUARE]));
	}
	
	@Test
	public void testFastRotation() {
		assertNull((new Piece("0 0")).fastRotation());
		for(Piece p : Piece.getPieces())
			assertNotNull(p.fastRotation());
		
		assertEquals(Piece.getPieces()[Piece.SQUARE].fastRotation(), Piece.getPieces()[Piece.SQUARE]);
		assertFalse(Piece.getPieces()[Piece.STICK].fastRotation().equals(Piece.getPieces()[Piece.STICK]));
		assertEquals(Piece.getPieces()[Piece.STICK].fastRotation().fastRotation(), Piece.getPieces()[Piece.STICK]);
		
		assertPieceEquals(Piece.getPieces()[Piece.L1].fastRotation().fastRotation(), new Piece("0 2 1 0 1 1 1 2"));
		
		for(Piece p : Piece.getPieces()) {
			assertEquals(p, (p.fastRotation().fastRotation().fastRotation().fastRotation()));
		}
		
	}
	
	@Test
	public void testSlowRotation() {
		assertPieceEquals(Piece.getPieces()[Piece.SQUARE], (new Piece("0 0 1 0 0 1 1 1").computeNextRotation()));
		assertPieceEquals(Piece.getPieces()[Piece.L1].computeNextRotation().computeNextRotation(), new Piece("0 2 1 0 1 1 1 2"));
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
		
		assertTrue(Arrays.equals(new int[] { 0 }, Piece.getPieces()[Piece.STICK].getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0}, Piece.getPieces()[Piece.L1].getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0}, Piece.getPieces()[Piece.L2].getSkirt()));
		
		assertTrue(Arrays.equals(new int[] { 0, 1, 0, 1, 0},
				(new Piece("0 0 1 1 2 0 3 1 4 0").getSkirt())));
	}

	@Test
	public void testEquals() {
		assertTrue(Piece.getPieces()[Piece.SQUARE].equals(new Piece("0 0 1 1 1 0 0 1")));
		assertFalse(Piece.getPieces()[Piece.SQUARE].equals(new Piece("0 0 0 1 1 1")));
		assertFalse((new Piece("0 0 1 0 1 1")).equals(Piece.getPieces()[Piece.SQUARE]));
	}
	
	@Test
	public void testRotationDimensions() {
		for(Piece p : Piece.getPieces()) {
			assertEquals(p.getHeight(), p.computeNextRotation().getWidth());
			assertEquals(p.getWidth(), p.computeNextRotation().getHeight());
		}
	}

	private static void assertPieceEquals(Piece p1, Piece p2) {
		if(!p1.equals(p2)) 
			assert false : "Pieces not equal: " + printBody(p1) + " " + printBody(p2);
	}

	private static String printBody(Piece piece) {
		String s = "";
		for(TPoint p : piece.getBody()) {
			s += p.x + " " + p.y + " ";
		}
		return s;
	}
	
	
	
} 
