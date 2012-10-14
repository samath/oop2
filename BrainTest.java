import static org.junit.Assert.*;

import org.junit.*;

public class BrainTest {
	Board b;
	Brain brain;
	Piece[] pieces;
	
	private final int TEST_HEIGHT = 10;

	@Before
	public void setUp() throws Exception {
		b = new Board(5, 10);
		brain = new DefaultBrain();
		pieces = Piece.getPieces();
	}
	
	@Test
	public void baseTest() {
		
		assertEquals(Board.PLACE_OK, b.place(pieces[Piece.STICK].fastRotation(), 0, 0));
		b.commit();
		assertEquals(Board.PLACE_OK, b.place(pieces[Piece.S1], 0, 1));
		b.commit();
		assertEquals(3, b.getMaxHeight());

		Brain.Move m = brain.bestMove(b, pieces[Piece.STICK], TEST_HEIGHT, null);
		assertEquals(4, m.x);
		assertEquals(0, m.y);
		assertTrue(pieces[Piece.STICK].equals(m.piece));
		assertEquals(Board.PLACE_ROW_FILLED, b.place(m.piece, m.x, m.y));
		assertEquals(1, b.clearRows());
		b.commit();

		
	}

	
	
}
