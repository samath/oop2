import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Random;

import javax.swing.*;


public class JBrainTetris extends JTetris {
	
	protected JCheckBox brainMode;
	protected JCheckBox fallingMode;
	protected JSlider adversary;
	protected DefaultBrain brain;
	
	private Random gen;
	
	private Brain.Move nextMove;

	JBrainTetris(int pixels) {
		super(pixels);
		brain = new DefaultBrain();
		gen = new Random();
	}
	
	@Override
	public JComponent createControlPanel() {
		JComponent panel = super.createControlPanel();
		
		panel.add(new JLabel("Brain:"));
		brainMode = new JCheckBox("Brain active");
		panel.add(brainMode);
		
		fallingMode = new JCheckBox("Animate Falling");
		fallingMode.setSelected(true);
		panel.add(fallingMode);
		
		// ADVERSARY slider
		JPanel row = new JPanel();
		row.add(new JLabel("Adversary"));
		adversary = new JSlider(0, 100, 0);	// min, max, current
		adversary.setPreferredSize(new Dimension(100, 15));
		row.add(adversary);
		panel.add(row);
		
		return panel;		
	}
	
	@Override
	public void tick(int verb) {
		if (!gameOn) return;
		
		if (currentPiece != null) {
			board.undo();	// remove the piece from its old position
		}
		
		if(verb == DOWN && nextMove != null && brainMode.isSelected()) {
			if(!nextMove.piece.equals(currentPiece)) {
				tryMove(ROTATE);				
			}
			if(nextMove.x < currentX) {
				tryMove(LEFT);
			}
			if(nextMove.x > currentX) { 
				tryMove(RIGHT);
			}
		}
		
		// Sets the newXXX ivars
		computeNewPosition(verb);
		
		// try out the new position (rolls back if it doesn't work)
		int result = setCurrent(newPiece, newX, newY);
		
		// if row clearing is going to happen, draw the
		// whole board so the green row shows up
		if (result ==  Board.PLACE_ROW_FILLED) {
			repaint();
		}
		

		boolean failed = (result >= Board.PLACE_OUT_BOUNDS);
		
		// if it didn't work, put it back the way it was
		if (failed) {
			if (currentPiece != null) board.place(currentPiece, currentX, currentY);
			repaintPiece(currentPiece, currentX, currentY);
		}
		
		/*
		 How to detect when a piece has landed:
		 if this move hits something on its DOWN verb,
		 and the previous verb was also DOWN (i.e. the player was not
		 still moving it),	then the previous position must be the correct
		 "landed" position, so we're done with the falling of this piece.
		*/
		if (failed && verb==DOWN && !moved) {	// it's landed
		
			int cleared = board.clearRows();
			if (cleared > 0) {
				// score goes up by 5, 10, 20, 40 for row clearing
				// clearing 4 gets you a beep!
				switch (cleared) {
					case 1: score += 5;	 break;
					case 2: score += 10;  break;
					case 3: score += 20;  break;
					case 4: score += 40; Toolkit.getDefaultToolkit().beep(); break;
					default: score += 50;  // could happen with non-standard pieces
				}
				updateCounters();
				repaint();	// repaint to show the result of the row clearing
			}
			
			
			// if the board is too tall, we've lost
			if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
				stopGame();
			}
			// Otherwise add a new piece and keep playing
			else {
				addNewPiece();
			}
		}
		
		// Note if the player made a successful non-DOWN move --
		// used to detect if the piece has landed on the next tick()
		moved = (!failed && verb!=DOWN);
	}
	
	private void tryMove(int verb) {
		computeNewPosition(verb);
		int result = board.place(newPiece, newX, newY);
		if(result <= Board.PLACE_ROW_FILLED) {
			updateCurrent();
		}
		board.undo();		
	}
	
	private void updateCurrent() {
		currentPiece = newPiece;
		currentX = newX;
		currentY = newY;
	}
	
	@Override
	public void addNewPiece() {
		count++;
		score++;
		
		if (testMode && count == TEST_LIMIT+1) {
			 stopGame();
			 return;
		}

		// commit things the way they are
		board.commit();
		currentPiece = null;

		Piece piece = pickNextPiece();
		
		// Center it up at the top
		int px = (board.getWidth() - piece.getWidth())/2;
		int py = board.getHeight() - piece.getHeight();
		
		// add the new piece to be in play
		nextMove = (brainMode.isSelected()) ? brain.bestMove(board, piece, HEIGHT, nextMove) : null;
		int result = setCurrent(piece, px, py);
		
		// This probably never happens, since
		// the blocks at the top allow space
		// for new pieces to at least be added.
		if (result>Board.PLACE_ROW_FILLED) {
			stopGame();
		}

		updateCounters();
	}
	
	@Override
	public Piece pickNextPiece() {
		boolean random = (adversary.getValue() < gen.nextInt(adversary.getMaximum()));
		if(random) {
			return super.pickNextPiece();
		} else {
			double max = -1;
			Piece worst = new Piece("");
			for(Piece p : pieces) {
				Brain.Move m = brain.bestMove(board, p, HEIGHT, null);
				double value = (m != null) ? m.score : Integer.MAX_VALUE;
				if(value > max) {
					max = value;
					worst = p;
				}
			}
			return worst;
		}
		
	}
	
	/**
	 Creates a frame with a JBrainTetris.
	*/
	public static void main(String[] args) {
		// Set GUI Look And Feel Boilerplate.
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JBrainTetris tetris = new JBrainTetris(16);
		JFrame frame = JTetris.createFrame(tetris);
		frame.setVisible(true);
	}
	
}
