package com.seecen.letris.ui;


import com.seecen.letris.controller.Mode;
import com.seecen.letris.util.ColorsKit;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * Created by Constantine on 2016-06-20 0020.
 */
public class GameCanvas extends Canvas implements MouseListener,MouseMotionListener
{
	private final Mode mode;
	private final GameBoard gameBoard;
	//缓冲图
	private BufferedImage offscreen;
	//缓冲图Graphics对象
	private Graphics2D big2d;

	public GameCanvas(Mode mode,GameBoard gameBoard)
	{
		this.mode=mode;
		this.gameBoard=gameBoard;
		offscreen=new BufferedImage(361,481,BufferedImage.TYPE_INT_ARGB);
		big2d=offscreen.createGraphics();
		big2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		paint(big2d);

		addMouseMotionListener(this);
		addMouseListener(this);

	}


	@Override
	public void repaint()
	{
		//绘制缓冲图
		paint(big2d);
		paint(this.getGraphics());
	}

	@Override
	public void paint(Graphics g)
	{
		//把缓冲图绘制到Canvas上
		Graphics2D g2d=(Graphics2D)g;
		g2d.drawImage(offscreen,0,0,this);

	}

	/**
	 * 绘制图像
	 * @param g2d
	 */
	public void paint(Graphics2D g2d)
	{
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());


		g2d.setColor(ColorsKit.LINE);
		//画行线
		for (int i = 0; i <= Mode.BLOCKS_ROW_NUM; i++)
		{
			g2d.drawLine(0,i*Mode.Block.BLOCK_SIZE,
					Mode.Block.BLOCK_SIZE*Mode.BLOCKS_CLO_NUM,
					i*Mode.Block.BLOCK_SIZE);
		}
		//画列线
		for (int i = 0; i <= Mode.BLOCKS_CLO_NUM; i++)
		{
			g2d.drawLine(i*Mode.Block.BLOCK_SIZE,0,
					i*Mode.Block.BLOCK_SIZE,
					Mode.Block.BLOCK_SIZE*Mode.BLOCKS_ROW_NUM);
		}

		//获取方块表
		Mode.Block[][] blocks=mode.getBlock();

		//绘制当前正在下落的方块
		Mode.Block currentBlock=mode.getCurrentBlock();
		if(currentBlock!=null)
			currentBlock.paint(g2d);

		//绘制各个方块
		for (int row = 0; row < blocks.length; row++)
			for (int col = 0;col < blocks[row].length; col++)
				if(blocks[row][col]!=null)
					blocks[row][col].paint(g2d);

//		//绘制炸飞的方块
//		for(Mode.Block b : mode.getFlyingBlocks())
//			b.paintFlying(g2d);

	}


	@Override
	public void mouseClicked(MouseEvent e)
	{
		//
		if(mode.onclick(e.getPoint()))
		{
			//提醒游戏单词区重绘
			gameBoard.updateWord();
		}

	}



	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}
}
