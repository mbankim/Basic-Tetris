package tetris;

import java.awt.*;
import java.awt.event.*;

public class ProgramWindow extends Frame implements WindowListener
{

	private TetrisPanel tp = new TetrisPanel();

	public ProgramWindow()
	{
		setTitle("Tetris");
		setSize(1300, 700);
		setLocation(100,100);
		setResizable(true);
		addWindowListener(this);
		add(tp);
		setVisible(true);
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		dispose();
		System.exit(0);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
}