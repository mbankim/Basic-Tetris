package tetris;

import java.awt.*;
import java.util.*;

public class Tetris
{

	//to check lines cleared, must be accessed by TetrisPanel
	public int linesCleared = 0;

	//Field
	private final int FIELD_HEIGHT = 20;
	private final int FIELD_WIDTH = 10;
	private int[][] field = new int[FIELD_WIDTH][FIELD_HEIGHT];
	private final int MARGIN_Y = 2;
	private final int MARGIN_X = 6;
	private final int GRID_WIDTH = 2;
	private final int TILE_FACTOR = 24;

	//Queue
	private final String QUEUE = "NEXT";
	private final int SHIFT_Y = 2;
	private final int Q_SIZE = 4;
	private final int BOX_DIM = 4;
	private final int GAP = 1;
	private int[] pieceQueue = new int[Q_SIZE];

	//Hold
	private final String HOLD = "HOLD";
	private final int FONT_FACTOR_HOLD = 2;
	private final int FONT_ADJ_HOLD = 1;
	private int fontSize_hold;
	private int shadowShift_hold;
	private int[][] holdPiece = new int[4][4];
	private int[][] tempPiece;
	private boolean holding = false;
	private boolean canSwap = true;

	//Pseudo-random piece generator
	private boolean[] bag = new boolean[7];
	private int pieceCount = 7;
	private int random;

	//Pause
	private final String PAUSE = "<PAUSED>";
	private final int FONT_ADJ_PAUSE = 5;
	private final int SHADOW_FACTOR = 15;
	private final Color FONT_COLOR = Color.WHITE;
	private final Color FONT_SHADOW_COLOR = Color.DARK_GRAY;
	private final String FONT_TYPE = "Dialogue";
	private boolean paused = false;
	private int fontSize_pause;
	private int shadowShift_pause;

	//Game over
	private final String GAME_OVER = "<GAME OVER>";
	private final String RESTART = "Press r to play again.";
	private final int FONT_ADJ_GAMEOVER = 7;
	private final int FONT_ADJ_RESTART_X = 5;
	private final int FONT_ADJ_RESTART_Y = 1;
	private int fontSize_restart;
	private int shadowShift_restart;
	private boolean gameOver = false;

	//Pieces, colors, shadows
	private int[][] curPiece;
	private int shadow_X, shadow_Y;
	private int curPiece_X, curPiece_Y, tileSize, curPiece_dim;
	private final Color[] COLOR = {new Color(0, 46, 50), Color.CYAN, Color.YELLOW, new Color(147, 112, 219), Color.ORANGE, new Color(8, 146, 208), Color.GREEN, Color.RED, Color.LIGHT_GRAY, Color.DARK_GRAY};
	private final int[][][] PIECE = {
		//line
		{{0, 0, 0, 0},
		{1, 1, 1, 1},
		{0, 0, 0, 0},
		{0, 0, 0, 0}},

		//Square
		{{0, 0, 0, 0},
		{0, 2, 2, 0},
		{0, 2, 2, 0},
		{0, 0, 0, 0}},

		//T
		{{0, 3, 0},
		{3, 3, 0},
		{0, 3, 0}},

		//J
		{{0, 0, 0},
		{4, 0, 0},
		{4, 4, 4}},

		//L
		{{0, 0, 0},
		{0, 0, 5},
		{5, 5, 5}},

		//Z
		{{0, 0, 6},
		{0, 6, 6},
		{0, 6, 0}},

		//S
		{{0, 7, 0},
		{0, 7, 7},
		{0, 0, 7}}
	};


	public Tetris()
	{
		//puts pieces in the queue
		for(int i = 0; i < Q_SIZE; i++)
			pieceQueue[i] = pieceGen();

		setNewPiece();
	}

	//User input methods - only called when the user inputs commands

	public boolean move(int dir)
	{
		/*If the left/right movement is valid,
		 *it returns true. It also moves the piece.
		 *If it is invalid, the piece does not move.
		 *The method will return false and the program
		 *will not repaint.
		 *
		 *It calls the method to move the shadow as well.
		 */
		
		//1 is right
		//-1 is left

		if (checkCollision(curPiece_X+dir, curPiece_Y, curPiece))
			return false;

		curPiece_X += dir;
		setShadow();
		return true;
	}

	public boolean rotateC()
	{
		/*Rotates the piece clockwise if the rotation is permitted.
		 * The rotation is not allowed if the piece collides on
		 * multiple sides. The rotation is sometimes permitted if
		 * it would collide on one side. See checkCollisionException() for
		 * more details.
		 */
		
		for(int i = 0; i < curPiece_dim-1; i++)
		{
			for(int j = 0; j < curPiece_dim-i-1; j++)
				swap(curPiece, i, j, curPiece_dim-j-1, curPiece_dim-i-1);
		}

		for(int i = 0; i < curPiece_dim; i++)
		{
			for(int j = 0; j < curPiece_dim/2; j++)
				swap(curPiece, i, j, i, curPiece_dim-j-1);
		}

		if (checkCollision(curPiece_X, curPiece_Y, curPiece))
		{
			if(checkCollisionException()) return true;
			rotateCC();
			return false;
		}

		setShadow();
		return true;
	}

	public boolean rotateCC()
	{
		//Same as rotateC but counterclockwise 
		
		for(int i = 0; i < curPiece_dim - 1; i++)
		{
			for(int j = i+1; j < curPiece_dim; j++)
				swap(curPiece, i, j, j, i);
		}

		for(int i = 0; i < curPiece_dim; i++)
		{
			for(int j = 0; j < curPiece_dim/2; j++)
				swap(curPiece, i, j, i, curPiece_dim-j-1);
		}

		if (checkCollision(curPiece_X, curPiece_Y, curPiece))
		{
			if(checkCollisionException()) return true;
			rotateC();
			return false;
		}

		setShadow();
		return true;
	}

	public void hold()
	{
		/*Allows the user to hold a piece. A hold is only permitted
		 * once per piece (i.e. you cannot swap a piece infinitely).
		 * 
		 * The first hold places the piece into the "hold" area and 
		 * sets a new piece to take its place.
		 * Each hold after the first swaps the current piece with the
		 * held piece.
		 */
		
		if(canSwap)
		{
			if(holding)
			{
				canSwap = false;
				tempPiece = new int[holdPiece.length][holdPiece.length];
				for(int i = 0; i < holdPiece.length; i++)
					tempPiece[i] = Arrays.copyOf(holdPiece[i], holdPiece.length);
				holdPiece = new int[curPiece_dim][curPiece_dim];
				for(int i = 0; i < curPiece_dim; i++)
					holdPiece[i] = Arrays.copyOf(curPiece[i], curPiece.length);
				setCurPiece(tempPiece);
			}
			else
			{
				holdPiece = new int[curPiece_dim][curPiece_dim];
				for(int i = 0; i < curPiece_dim; i++)
					holdPiece[i] = Arrays.copyOf(curPiece[i], curPiece_dim);
				holding = true;
				setNewPiece();
			}
		}
	}

	public void pause()
	{
		/*Only the pause graphics are handled in this class.
		 * The game functions are paused in TetrisPanel.
		 */
		
		paused = !paused;
	}


	//passive methods - always running, called either by the timer or by other methods
	public void setNewPiece()
	{
		/*When a piece is blended or the first time a piece is held,
		 * this method moves a piece from the queue into the game.
		 * Note that pieces are generated pseudo-randomly. More info
		 * in method pieceGen()
		 */
		
		setCurPiece(PIECE[pieceQueue[0]]);

		//Moving the queue forward
		for(int i = 0; i < Q_SIZE-1; i++)
			pieceQueue[i] = pieceQueue[i+1];
		pieceQueue[Q_SIZE-1] = pieceGen();
	}

	public void setCurPiece(int[][] piece)
	{
		/*
		 * Handles the parameters of the piece being added
		 * (dimensions, X/Y coordinates, type)
		 * 
		 * Separate from setNewPiece() so that hold() can
		 * set the held piece as the current piece.
		 * 
		 * If the piece collides when it spawns, the game is over
		 */
		
		//sets the current piece's X, Y, dimensions, shadow
		curPiece_dim = piece.length;
		curPiece = new int[curPiece_dim][curPiece_dim];
		for(int i = 0; i < curPiece_dim; i++)
			curPiece[i] = Arrays.copyOf(piece[i], curPiece_dim);

		//makes sure the piece is placed at the very top of the field
		curPiece_X = (FIELD_WIDTH-curPiece_dim)/2;
		curPiece_Y = -2;
		while(checkCollision(curPiece_X, curPiece_Y, curPiece) && curPiece_Y <= 0)
			curPiece_Y++;
		
		if(curPiece_Y > 0)
		{
			gameOver = true;
			return;
		}
		
		setShadow();
	}

	public boolean fall()
	{
		/*Every time a piece falls
		 * this method is called.
		 * 
		 * Moves the piece down.
		 * 
		 * If the piece would collide with the bottom,
		 * Blends the piece with the field.
		 */

		 //false means the piece collided with something

		if (checkCollision(curPiece_X, curPiece_Y+1, curPiece))
		{
			blend();
			return false;
		}

		curPiece_Y++;
		return true;
	}

	public boolean checkCollision(int newX, int newY, int[][] piece)
	{
		/*Checks if the movement will cause a collision of the piece
		 * with the new X and Y coordinates of the top left corner.
		 * Returns true if the movement is not permitted. 
		 * Returns false if there is no collision.
		 */
		
		for (int i = 0; i < curPiece_dim; i++)
		{
			for (int j = 0; j < curPiece_dim; j++)
			{
				if (piece[i][j] != 0)
				{
					if (newX+i < 0 || newX+i >= FIELD_WIDTH || newY+j < 0 || newY+j >= FIELD_HEIGHT || field[newX+i][newY+j] != 0)
						return true;
				}
			}
		}
		return false;
	}

	private boolean checkCollisionException()
	{
		/*Allows the player to rotate blocks beside objects if
		 * the block would have otherwise rotated and collided. 
		 * Does not allow a movement if the moved block causes
		 * the piece to collide on the opposite side.		 * 
		 */
		
		if(!checkCollision(curPiece_X+1, curPiece_Y, curPiece))
		{
			curPiece_X++;
			setShadow();
			return true;
		}
		if(!checkCollision(curPiece_X-1, curPiece_Y, curPiece))
		{
			curPiece_X--;
			setShadow();
			return true;
		}
		if(!checkCollision(curPiece_X, curPiece_Y+1, curPiece))
		{
			curPiece_Y++;
			setShadow();
			return true;
		}
		if(!checkCollision(curPiece_X, curPiece_Y-1, curPiece))
		{
			curPiece_Y--;
			setShadow();
			return true;
		}
		return false;
	}

	public void setShadow()
	{
		/*The method is called every time a piece moves.
		 * A shadow is generated that tells the user where
		 * the piece will fall.
		 */
		
		shadow_X = curPiece_X;
		shadow_Y = curPiece_Y;
		while (!checkCollision(shadow_X, shadow_Y+1, curPiece))
			shadow_Y++;
	}

	public void blend()
	{
		/*
		 * Makes the current piece part of the field.
		 * Then checks for line clears.
		 * Finally, generates a new piece.
		 */
		
		for (int i = 0; i < curPiece_dim; i++)
		{
			for (int j = 0; j < curPiece_dim; j++)
			{
				if (curPiece[i][j] != 0)
					field[curPiece_X+i][curPiece_Y+j] = curPiece[i][j];
			}
		}

		//line clears

		boolean thisLine;

		for (int i = 0; i < FIELD_HEIGHT; i++)
		{
			thisLine = true;
			for(int j = 0; j < FIELD_WIDTH; j++)
			{
				if (field[j][i] == 0)
				{
					thisLine = false;
					j = FIELD_WIDTH;
				}
			}

			if (thisLine)
			{
				linesCleared++;
				for (int k = i; k > 0; k--)
				{
					for (int j = 0; j < FIELD_WIDTH; j++)
						field[j][k] = field[j][k-1];
				}

				//case for top row
				for (int j = 0; j < FIELD_WIDTH; j++)
					field[j][0] = 0;
			}
		}

		canSwap = true;
		setNewPiece();
	}

	private void swap(int[][] arr, int x1, int y1, int x2, int y2)
	{
		int temp = arr[x2][y2];
		arr[x2][y2] = arr[x1][y1];
		arr[x1][y1] = temp;
	}

	private int pieceGen()
	{
		/*In Tetris, pieces are not actually generated randomly.
		 * Instead, the 7 tetrominoes are placed in a random order
		 * and no piece gets repeated until all tetrominoes have
		 * been used. This pseudo-random style of generating pieces
		 * is imitated in this method. The array is called "bag" because
		 * this style of random selection is similar to filling a
		 * bag with 7 pieces and picking from it until no pieces
		 * remain.
		 */

		if (pieceCount > 1)
		{
			random = (int) (pieceCount*Math.random());
			for(int i = 0; i <= random; i++)
			{
				if(bag[i])
					random++;
			}
			bag[random] = true;
			pieceCount--;
			return random;
		}

		for(int i = 0; i < 7; i++)
		{
			if(!bag[i])
			{
				random = i;
				i = 7;
			}
		}
		pieceCount = 7;
		for(int i = 0; i < 7; i++)
			bag[i] = false;
		return random;
	}

	boolean checkGameOver()
	{
		return gameOver;
	}

	//graphics
	public void display (Graphics g, Dimension dim)
	{
		/*
		 * Deals with graphics.
		 * When the game is paused or over, the pieces are
		 * not displayed.
		 */
		
		//parameters that can change with screen size
		//includes tile size and font size
		//shadow used to make words legible
		
		tileSize = Math.min(dim.width, dim.height)/TILE_FACTOR;
		fontSize_pause = tileSize*2;
		fontSize_hold = (int) (tileSize*1.2);
		fontSize_restart = (int) (tileSize*1.1);
		shadowShift_pause = fontSize_pause/SHADOW_FACTOR;
		shadowShift_hold = fontSize_hold/SHADOW_FACTOR;
		shadowShift_restart = fontSize_restart/SHADOW_FACTOR;


		//FIELD

		//background of the field

		g.setColor(COLOR[0]);
		g.fillRect(tileSize*MARGIN_X, tileSize*MARGIN_Y, tileSize*FIELD_WIDTH, tileSize*FIELD_HEIGHT);

		if(!paused)
		{
			//piece shadow
			g.setColor(COLOR[9]);
			for (int i = 0; i < curPiece_dim; i++)
			{
				for (int j = 0; j < curPiece_dim; j++)
				{
					if (curPiece[i][j] != 0)
						g.fillRect(tileSize*(shadow_X+i+MARGIN_X), tileSize*(shadow_Y+j+MARGIN_Y), tileSize, tileSize);
				}
			}

			//field
			for (int i = 0; i < FIELD_WIDTH; i++)
			{
				for (int j = 0; j < FIELD_HEIGHT; j++)
				{
					if (field[i][j] > 0)
					{
						g.setColor(COLOR[field[i][j]]);
						g.fillRect(tileSize*(MARGIN_X+i), tileSize*(MARGIN_Y+j), tileSize, tileSize);
					}
				}
			}

			//piece
			for(int i = 0; i < curPiece_dim; i++)
			{
				for(int j = 0; j < curPiece_dim; j++)
				{
					if (curPiece[i][j] != 0)
					{
						g.setColor(COLOR[curPiece[i][j]]);
						g.fillRect(tileSize*(curPiece_X+i+MARGIN_X), tileSize*(curPiece_Y+j+MARGIN_Y), tileSize, tileSize);
					}
				}
			}
		}

		//grid lines
		g.setColor(COLOR[8]);
		for(int i = 0; i < FIELD_WIDTH+1; i++)
			g.fillRect((MARGIN_X+i)*tileSize-GRID_WIDTH/2, MARGIN_Y*tileSize, GRID_WIDTH, tileSize*FIELD_HEIGHT);

		for(int i = 0; i < FIELD_HEIGHT+1; i++)
			g.fillRect(MARGIN_X*tileSize, (MARGIN_Y+i)*tileSize-GRID_WIDTH/2, tileSize*FIELD_WIDTH, GRID_WIDTH);


		//PIECE QUEUE

		//text
		g.setFont(new Font(FONT_TYPE, Font.PLAIN, fontSize_hold));
		g.setColor(FONT_SHADOW_COLOR);
		g.drawString(QUEUE, tileSize*(MARGIN_X+GAP+FIELD_WIDTH)+shadowShift_hold, tileSize*(MARGIN_Y+FONT_ADJ_HOLD)+ shadowShift_hold);
		g.setColor(FONT_COLOR);
		g.drawString(QUEUE, tileSize*(MARGIN_X+GAP+FIELD_WIDTH), tileSize*(MARGIN_Y+FONT_ADJ_HOLD));

		for(int q = 0; q < Q_SIZE; q++)
		{
			//display area
			g.setColor(COLOR[0]);
			g.fillRect(tileSize*(MARGIN_X+GAP+FIELD_WIDTH), tileSize*(MARGIN_Y+SHIFT_Y+q*(GAP+BOX_DIM)), tileSize*BOX_DIM, tileSize*BOX_DIM);

			//piece
			if(!paused)
			{
				for(int i = 0; i < PIECE[pieceQueue[q]].length; i++)
				{
					for(int j = 0; j < PIECE[pieceQueue[q]].length; j++)
					{
						if (PIECE[pieceQueue[q]][i][j] != 0)
						{
							g.setColor(COLOR[PIECE[pieceQueue[q]][i][j]]);
							g.fillRect(tileSize*(i+MARGIN_X+GAP+FIELD_WIDTH), tileSize*(j+MARGIN_Y+SHIFT_Y+q*(GAP+BOX_DIM)), tileSize, tileSize);
						}
					}
				}
			}

			//grid lines
			g.setColor(COLOR[8]);
			for(int i = 0; i < BOX_DIM+1; i++)
				g.fillRect(tileSize*(i+MARGIN_X+GAP+FIELD_WIDTH), tileSize*(SHIFT_Y+MARGIN_Y+q*(GAP+BOX_DIM)), GRID_WIDTH, tileSize*BOX_DIM);

			for(int i = 0; i < BOX_DIM+1; i++)
				g.fillRect(tileSize*(MARGIN_X+GAP+FIELD_WIDTH), tileSize*(i+MARGIN_Y+SHIFT_Y+q*(GAP+BOX_DIM)), tileSize*BOX_DIM, GRID_WIDTH);
		}



		//PIECE HOLD
		// display area
		g.setColor(COLOR[0]);
		g.fillRect(tileSize*GAP, tileSize*(MARGIN_Y+SHIFT_Y), tileSize*BOX_DIM, tileSize*BOX_DIM);

		//text
		g.setFont(new Font(FONT_TYPE, Font.PLAIN, fontSize_hold));
		g.setColor(FONT_SHADOW_COLOR);
		g.drawString(HOLD, GAP*tileSize+shadowShift_hold, (MARGIN_Y+FONT_ADJ_HOLD)*tileSize + shadowShift_hold);
		g.setColor(FONT_COLOR);
		g.drawString(HOLD, GAP*tileSize, (MARGIN_Y+FONT_ADJ_HOLD)*tileSize);

		//piece
		if(!paused)
		{
			for(int i = 0; i < holdPiece.length; i++)
			{
				for(int j = 0; j < holdPiece.length; j++)
				{
					if (holdPiece[i][j] != 0)
					{
						g.setColor(COLOR[holdPiece[i][j]]);
						g.fillRect(tileSize*(i+GAP), tileSize*(j+MARGIN_Y+SHIFT_Y), tileSize, tileSize);
					}
				}
			}
		}

		//grid lines
		g.setColor(COLOR[8]);
		for(int i = 0; i < BOX_DIM+1; i++)
			g.fillRect((GAP+i)*tileSize-GRID_WIDTH/2, (MARGIN_Y+SHIFT_Y)*tileSize, GRID_WIDTH, tileSize*BOX_DIM);

		for(int i = 0; i < BOX_DIM+1; i++)
			g.fillRect(GAP*tileSize, (SHIFT_Y+MARGIN_Y+i)*tileSize-GRID_WIDTH/2, tileSize*BOX_DIM, GRID_WIDTH);

		if(paused && !gameOver)
		{
			g.setFont(new Font(FONT_TYPE, Font.PLAIN, fontSize_pause));
			g.setColor(FONT_SHADOW_COLOR);
			g.drawString(PAUSE, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_PAUSE)*tileSize+shadowShift_pause, (MARGIN_Y+FIELD_HEIGHT/2)*tileSize + shadowShift_pause);
			g.setColor(FONT_COLOR);
			g.drawString(PAUSE, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_PAUSE)*tileSize, (MARGIN_Y+FIELD_HEIGHT/2)*tileSize);
		}
		else if (gameOver)
		{
			g.setFont(new Font(FONT_TYPE, Font.PLAIN, fontSize_pause));
			g.setColor(FONT_SHADOW_COLOR);
			g.drawString(GAME_OVER, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_GAMEOVER)*tileSize+shadowShift_pause, (MARGIN_Y+FIELD_HEIGHT/2)*tileSize + shadowShift_pause);
			g.setColor(FONT_COLOR);
			g.drawString(GAME_OVER, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_GAMEOVER)*tileSize, (MARGIN_Y+FIELD_HEIGHT/2)*tileSize);
			
			g.setFont(new Font(FONT_TYPE, Font.PLAIN, fontSize_restart));
			g.setColor(FONT_SHADOW_COLOR);
			g.drawString(RESTART, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_RESTART_X)*tileSize+shadowShift_restart, (MARGIN_Y+FIELD_HEIGHT/2 + FONT_ADJ_RESTART_Y)*tileSize + shadowShift_restart);
			g.setColor(FONT_COLOR);
			g.drawString(RESTART, (MARGIN_X+FIELD_WIDTH/2-FONT_ADJ_RESTART_X)*tileSize, (MARGIN_Y+FIELD_HEIGHT/2 + FONT_ADJ_RESTART_Y)*tileSize);
		}
	}

}