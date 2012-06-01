import java.awt.*;

import javax.swing.*;


@SuppressWarnings("serial")
public class JBrainTetris extends JTetris {

	// private ivars
	private DefaultBrain brain;
	private boolean DEBUG = false;
	private int currentCount;
	private Brain.Move bestMove;
	private Brain.Move move;

	// Controls
	protected JCheckBox brainMode;
	protected JSlider adversary;
	protected JCheckBox animateFall;
	protected JLabel okStatus;

	public JBrainTetris(int pixels) {
		super(pixels);
		brain = new DefaultBrain();
		currentCount = 0;
		bestMove = new Brain.Move();
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
		JFrame frame = JBrainTetris.createFrame(tetris);
		frame.setVisible(true);
	}

	/**
	 * Override createControlPanel method to tack on
	 * the Brain label and JCheckBox. Also adds Animate
	 * Fall checkbox and Adversary slider.
	 * */
	@Override
	public JComponent createControlPanel() {

		JPanel panel = (JPanel)super.createControlPanel();

		// Resize JPanel that contains the speed slider
		JPanel row = (JPanel)panel.getComponent(7);
		row.setMaximumSize(new Dimension(400,100));

		JCheckBox test = (JCheckBox)panel.getComponent(8);
		test.setVisible(DEBUG);

		// BRAIN CHECKBOX
		panel.add(Box.createVerticalStrut(12));
		panel.add(new JLabel("Brain:"));
		brainMode = new JCheckBox("Brain active");
		animateFall = new JCheckBox("Animate Fall");
		animateFall.setSelected(true);
		panel.add(brainMode);
		panel.add(animateFall);

		// ADVERSARY SLIDER
		JPanel little = new JPanel();
		little.add(Box.createVerticalStrut(25));

		little.add(new JLabel("Adversary:"));
		adversary = new JSlider(0, 100, 0);	// min, max, current
		adversary.setPreferredSize(new Dimension(100, 15));
		little.setMaximumSize(new Dimension(400,100));
		little.add(adversary);
		panel.add(little);

		//Ok status label
		JPanel okPanel = new JPanel();

		okStatus = new JLabel("ok");
		okPanel.add(okStatus);

		panel.add(okPanel);

		return panel;
	}

	/**
	 * The strategy is to override tick(), so that every time the system calls tick(DOWN)
	 * to move the piece down one, JBrainTetris takes the opportunity to move the piece a
	 * bit first. Our rule is that the brain may do up to one rotation and one left/right
	 * move each time tick(DOWN) is called: rotate the piece one rotation and move it left
	 * or right one position. With the brain on, the piece should drift down to its correct
	 * place. We use the "Animate Falling" checkbox (default to true) to  control how the
	 * brain works the piece once it is in the correct column but not yet landed. When animate
	 * is false, the brain can use the "DROP" command to drop the piece down into place.
	 * In any case, after the brain does its changes, the tick(DOWN) should	have its usual
	 * effect of trying to lower the piece by one. So on each tick, the brain will move the
	 * piece a little, and the piece will drop down one row. The user should still be able
	 * to use the keyboard to move the piece around while the brain is playing, but the
	 * brain will move the piece back on course. As the board gets full, the brain may fail
	 * to get the piece over fast enough.
	 * */
	@Override
	public void tick(int verb) {

		if(brainMode.isSelected() && verb == DOWN)
		{
			if(currentCount!= count)
			{
				currentCount = count;
				board.undo();
				bestMove = brain.bestMove(board, currentPiece, HEIGHT, bestMove);
			}

			if(bestMove != null)
			{
				// keep rotating once every tick(DOWN) till you get
				// the right orientation
				if(!currentPiece.equals(bestMove.piece))
				{
					currentPiece=currentPiece.fastRotation();
				}

				// move piece to left or right or DROP it depending
				// on the current piece and its location
				if(bestMove.x > currentX)	currentX++;
				else if(bestMove.x < currentX)	currentX--;
				else if(!animateFall.isSelected() && currentPiece.equals(bestMove.piece) && bestMove.x == currentX && bestMove.y != currentY)
					verb = DROP;
			}
		}

		super.tick(verb);
	}

	/**
	 * Override pickNextPiece to let adversary choose next piece based on slider position.
	 * If the slider is at 100, the adversary should always intervene. Create a random number
	 * between 1 and 99. If the random number is >= than the slider, then the piece should
	 * be chosen randomly as usual (just "super" on up). But if the random value is less,
	 * the mischief begins. In that case the "adversary" gets to pick the next piece. When
	 * the piece is chosen at random, setText() the status to "ok", otherwise set it to "*ok*".
	 * */
	@Override
	public Piece pickNextPiece() {
		int adversaryValue = adversary.getValue();
		int rand = random.nextInt(100);

		if(rand >= adversaryValue)
		{
			okStatus.setText("ok");
			return super.pickNextPiece();
		}
		else
		{
			okStatus.setText("*ok*");
			Piece worstPiece= null;
			double worseScore =0;

			for (int i = 0 ; i < pieces.length;i++)
			{
				move = brain.bestMove(board, pieces[i], HEIGHT, move);

				if(move==null)
					return super.pickNextPiece();

				if(move.score > worseScore)
				{
					worstPiece = pieces[i];
					worseScore = move.score;
				}
			}
			return worstPiece;
		}
	}


}
