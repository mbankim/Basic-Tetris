package tetris;

import java.util.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class TetrisPanel extends Panel implements KeyListener
{
	//Tetris object
	private Tetris tetris;

	//input key commands
	private final int MOVE_R = KeyEvent.VK_RIGHT;
	private final int MOVE_L = KeyEvent.VK_LEFT;
	private final int FALL = KeyEvent.VK_DOWN;
	private final int DROP = KeyEvent.VK_SPACE;
	private final int ROTATE_C = KeyEvent.VK_Z;
	private final int ROTATE_C_ALT = KeyEvent.VK_UP;
	private final int ROTATE_CC = KeyEvent.VK_X;
	private final int PAUSE = KeyEvent.VK_P;
	private final int HOLD = KeyEvent.VK_A;
	private final int RESET = KeyEvent.VK_R;
	private final int RIGHT = 1;
	private final int LEFT = -1;

	//level
	private final int[] DROP_TIME = {100, 75, 50, 25, 12, 6, 3, 1};
	private final int LEVEL_MAX = 7;
	private int level = 0;
	private int dropTimeCnt = 0;
	private final int LEVEL_UP = 20;

	//pause
	private boolean gamePaused = false;
	
	//game over
	private boolean gameOver = false;

	//DBG instant fields
	private Dimension dim;
	private Color bg = new Color(119, 158, 203);
	BufferedImage osi;
	Graphics osg;

	//timer instant fields
	private long delay = 0;
	private long interval = 10;
	Timer time = new Timer();
	TimerTask tick = new TimerTask()
	{
		@Override
		public void run()
		{
			
			if(tetris.checkGameOver())
			{
				gameOver = true;
				return;
			}
			
			if(gamePaused) return;
	
			if (tetris.linesCleared >= LEVEL_UP && level < LEVEL_MAX)
			{
				tetris.linesCleared = tetris.linesCleared%LEVEL_UP;
				level++;
			}

			if(dropTimeCnt < DROP_TIME[level])
				dropTimeCnt++;
			else
			{
				tetris.fall();
				repaint();
				dropTimeCnt = 0;
			}
		}
	};

	//key input
	@Override
	public void keyPressed(KeyEvent key)
	{
		boolean repaint = true;
		int keyCode = key.getKeyCode();
		if(gamePaused && keyCode != PAUSE)
			return;
		if (gameOver && keyCode != RESET)
			return;
		switch(keyCode)
		{
		case MOVE_R:
			if(!tetris.move(RIGHT))
				repaint = false;
			break;
		case MOVE_L:
			if(!tetris.move(LEFT))
				repaint = false;
			break;
		case FALL:
			if(!tetris.fall())
				repaint = false;
			break;
		case DROP:
			while (tetris.fall()){};
			break;
		case ROTATE_C:
		case ROTATE_C_ALT:
			if(!tetris.rotateC())
				repaint = false;
			break;
		case ROTATE_CC:
			if(!tetris.rotateCC())
				repaint = false;
			break;
		case HOLD:
			tetris.hold();
			break;
		case PAUSE:
			gamePaused = !gamePaused;
			tetris.pause();
			break;
		case RESET:
			tetris = new Tetris();
			gameOver = false;
			break;
		default:
			repaint = false;
			break;
		}
		if(repaint) repaint();
	}
	//constructor
	public TetrisPanel()
	{
		tetris = new Tetris();
		addKeyListener(this);
		setBackground(bg);
		time.scheduleAtFixedRate(tick, delay, interval);
	}

	//DBG implementation
	@Override
	public void paint(Graphics g)
	{
		dim = getSize();
		osi = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		osg = osi.getGraphics();
		update(g);
	}

	@Override
	public void update(Graphics g)
	{
		osg.setColor(bg);
		osg.fillRect(0, 0, dim.width, dim.height);
		tetris.display(osg, dim);
		g.drawImage(osi, 0, 0, this);
	}

	//unused overridden KeyListener methods

	@Override
	public void keyReleased(KeyEvent key){}
	@Override
	public void keyTyped(KeyEvent key){}
}