package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import game.core.Board;
import game.core.Piece;
import game.core.UserInterface;
import game.factories.PieceFactory;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class GameManager extends JPanel implements KeyListener {
	
	public static final Point DOWN = new Point(0, 1);
	public static final Point LEFT = new Point(-1, 0);
	public static final Point RIGHT = new Point(1, 0);
	private static final int LINE_SCORE = 100;
	private static final int BONUS_SCORE = 300;
	private static final int LINES_CLEARED_TO_SPEEDUP = 5;
	private static final double PIECE_SPEED_INCREMENT = 0.1;

	private final int width;
	private final int height;
	private final Color backgroundColor;

	private Board gameBoard;

	private Piece currentPiece;
	private double currentPieceSpeed;

	private Piece nextPiece;

	private int linesCleared;
	private int currentPieceSpeedCounter;
	private int currentScore;
	private int highScore = 0;
	private UserInterface ui;
	
	private TetrisMomento savedState;

	public GameManager(int width, int height) {
		this.width = width;
		this.height = height;
		
		backgroundColor = Color.WHITE;

		Point2D boardPos = new Point2D.Double(width / 3, -(height / Board.NUM_ROWS) * 2);
		gameBoard = new Board(boardPos, width, height);

		ui = new UserInterface();
		init();
		
		currentPiece = PieceFactory.generateRandomPiece(gameBoard, currentPieceSpeed);
		nextPiece = PieceFactory.generateRandomPiece(gameBoard, currentPieceSpeed);
		ui.newPiece(nextPiece);
	}
	
	private void init() {
		currentPieceSpeed = 1;
		linesCleared = 0;
		currentPieceSpeedCounter = 0;
		currentScore = 0;
		gameBoard.init();
		ui.restart();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(backgroundColor);
		g.fillRect(0, 0, width, height);
		
		gameBoard.draw(g);
		ui.draw(g);
	}

	public void update(double time) {
		currentPiece.update();
		if(currentPiece.isLanded()) {
			ArrayList<Integer> rowsToRemove = gameBoard.checkRows();
			
			if(!rowsToRemove.isEmpty()) {
				int rowsRemoved = gameBoard.removeRows(rowsToRemove);
				linesCleared += rowsRemoved;
				currentPieceSpeedCounter += rowsRemoved;
				currentScore = linesCleared * LINE_SCORE;
				
				if(rowsRemoved == 4) {
					currentScore += BONUS_SCORE;
				}
				
				if(currentPieceSpeedCounter >= LINES_CLEARED_TO_SPEEDUP) {
					currentPieceSpeed -= PIECE_SPEED_INCREMENT;
					currentPieceSpeedCounter -= LINES_CLEARED_TO_SPEEDUP;
				}

				ui.update(currentScore, linesCleared);
			}

			boolean lost = gameBoard.checkLoss();
			if(lost) {
				System.out.println("Final Lines Cleared: " + linesCleared);
				System.out.println("Final Score: " + currentScore);
				
				if(currentScore > highScore) {
					System.out.println("New Highscore!");
					highScore = currentScore;
					ui.endGameUpdate(highScore);
				}
				init();
			}
            currentPiece = nextPiece;
			nextPiece = PieceFactory.generateRandomPiece(gameBoard, currentPieceSpeed);
			
			ui.newPiece(nextPiece);
		}
        
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			currentPiece.move(LEFT);
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			currentPiece.move(RIGHT);
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			currentPiece.fallFast();
		}
		else if (e.getKeyCode() == KeyEvent.VK_X) {
			currentPiece.rotate(true);
		}
		else if (e.getKeyCode() == KeyEvent.VK_Z) {
			currentPiece.rotate(false);
		}
		else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			currentPiece.drop();
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP) {
			currentPiece.drop();
		}
		else if (e.getKeyCode() == KeyEvent.VK_W) {
			savedState = createMomento();
		}
		
	}

	private TetrisMomento createMomento() {
		TetrisMomento newMomento = new TetrisMomento();
		return newMomento;
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			currentPiece.stopFallingFast();
		}
	}

	public void keyTyped(KeyEvent arg0) {
		// Empty
	}
}