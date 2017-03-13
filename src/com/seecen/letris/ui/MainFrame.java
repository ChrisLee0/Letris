package com.seecen.letris.ui;

import com.seecen.letris.controller.ClassicMode;
import com.seecen.letris.controller.Mode;
import com.seecen.letris.controller.RelaxMode;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Constantine on 2016-06-20 0020.
 */
public class MainFrame extends JFrame
{
	private Mode mode=new ClassicMode();

	private MainBoard mainBoard = new MainBoard(this);
	private GameBoard gameBoard = new GameBoard(this,mode);

	public static final int width=361;
	public static final int height=611;


	public MainFrame()
	{
		super();

		setTitle("Letris");
		setSize(width, height);

		//关闭窗口时退出程序
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//无边框
		this.setUndecorated(true);
		//窗口屏幕居中
		setLocationRelativeTo(null);
		//显示启动面板
		showMainBoard();
		//显示游戏面板
//		showGameBoard();


		//显示窗口
		setVisible(true);

		//无法调整大小
//		setResizable(false);


		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				System.out.println("key");
			}
		});


	}


	public void showMainBoard()
	{
		setContentPane(mainBoard);
		setVisible(true);
	}

	public void showGameBoard()
	{
		setContentPane(gameBoard);
		setVisible(true);

		gameBoard.updateWord();
		mode.startGame();
	}


}
