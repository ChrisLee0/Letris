package com.seecen.letris.ui;


import com.seecen.letris.controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.seecen.letris.util.ColorsKit;


public class GameBoard extends Panel implements KeyListener
{
	private GameTitle gameTitle=new GameTitle();
	private GameBottom gameBottom=new GameBottom();
	private GameCanvas gameCanvas;
	final AffineTransform identity=new AffineTransform();


	private Font wordFont=new Font("微软雅黑",Font.PLAIN,24);


	private Mode mode;
	private  JFrame frame;
	private volatile boolean falling=false;

	public GameBoard(JFrame frame,Mode mode)
	{
		this.frame=frame;
		this.mode=mode;
		gameCanvas=new GameCanvas(mode,this);
		mode.setCanvas(gameCanvas);

		setLayout(new BorderLayout());
		add(gameTitle,BorderLayout.NORTH);
		add(gameBottom,BorderLayout.SOUTH);
		add(gameCanvas,BorderLayout.CENTER);

		addKeyListener(this);

	}

	//单词区重绘
	public void updateWord()
	{
		gameBottom.repaint();
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		System.out.println("key");
		if(e.getKeyCode()==KeyEvent.VK_SPACE) System.out.println("space");
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e){}

	private class  GameTitle extends Canvas
	{
		private int oldX=0;
		private int oldY=0;
		private Font titleFont=new Font("Courier New",Font.PLAIN,24);


		public GameTitle()
		{
			setSize(200,80);

			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					oldX=e.getX();
					oldY=e.getY();

				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					if(frame.getY()<0)
						frame.setLocation(frame.getX(),0);
					if(e.getXOnScreen()<4)
						frame.setLocation(0,frame.getY());
					int width=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
					if(width-e.getXOnScreen()<4)
						frame.setLocation(width-getWidth(),frame.getY());
				}
			});

			addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseDragged(MouseEvent e)
				{
					int xOnScreen = e.getXOnScreen();
					int yOnScreen = e.getYOnScreen();
					int xx = xOnScreen - oldX;
					int yy = yOnScreen - oldY;
					frame.setLocation(xx, yy);
				}
			});
		}

		@Override
		public void paint(Graphics g)
		{
			Graphics2D g2d=(Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(ColorsKit.RED);
			g2d.fillRect(0,30,getWidth(),50);

			g2d.setColor(ColorsKit.GRAY);
			g2d.fillRect(0,0,getWidth(),30);


			g2d.setFont(titleFont);


			FontRenderContext frc=g2d.getFontRenderContext();
			TextLayout layout=new TextLayout("Letris",titleFont,frc);
			Rectangle2D rect=layout.getBounds();

			double x=(getWidth()-rect.getWidth())/2;
			double y=(50-rect.getHeight())/2;

			g2d.setPaint(Color.WHITE);
			g2d.drawString("Letris",(int)x,30-(int)((30-rect.getHeight())/2));


			g2d.setTransform(identity);

			g2d.translate(getWidth()-15,15);
			g2d.rotate(Math.PI/4);

			int boxsize=7;

			//g2d.drawRect(-10,-10,20,20);
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.drawLine(-boxsize,0,boxsize,0);
			g2d.drawLine(0,-boxsize,0,boxsize);


		}
	}

	private class GameBottom extends Canvas implements MouseListener
	{
		private final int[] arrowX={(50-32)/2,50-(50-32)/2,50/2};
		private final int[] arrowY={26,26,42};

		private BufferedImage bufferedImage=new BufferedImage(361,50,BufferedImage.TYPE_INT_ARGB);
		private Graphics2D buffg2d=bufferedImage.createGraphics();


		public GameBottom()
		{
			setSize(200,50);

			addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e)
				{
					if(e.getX()>50 && e.getX()<getWidth()-50)
						setCursor(new Cursor(Cursor.HAND_CURSOR));
					else
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR ));
				}
			});

			addMouseListener(this);
		}

		@Override
		public void paint(Graphics g)
		{
			//绘图到缓冲区
			paint(buffg2d);

			g.drawImage(bufferedImage,0,0,this);
		}

		public void paint(Graphics2D g2d)
		{
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setTransform(identity);


			//底部背景
			switch (mode.getStatus())
			{
				case EMPTY:
					g2d.setColor(ColorsKit.LIGHT_GRAY);
					break;
				case WRONG:
					g2d.setColor(ColorsKit.RED);
					break;
				case CORRECT:
					g2d.setColor(ColorsKit.BLUE);
					break;
			}
			g2d.fillRect(0,0,getWidth(),getHeight());


			//取得用户输入字符
			String word=mode.getWord();

			//设置字体为白色
			g2d.setColor(Color.WHITE);
			g2d.setFont(wordFont);

			//取得字体宽度
			int wordWidth=g2d.getFontMetrics().stringWidth(word);
			//绘制字体
			g2d.drawString(word,(getWidth()-wordWidth)/2,35);

			g2d.translate(0,0);
			//绘制左按钮
			drawPause(g2d);

			g2d.translate(getWidth()-50,0);
			//绘制右按钮
			drawDownArrow(g2d);

		}

		private void drawPause(Graphics2D g2d)
		{
			//底部按钮左侧背景
			g2d.setColor(ColorsKit.GRAY);
			g2d.fillRect(0,0,50,50);

			//左下角暂停按钮
			int pauseWidth=10;
			int paddingSide=10;
			int paddingTopBottom=10;
			g2d.setColor(Color.GRAY);
			g2d.fillRect(paddingSide,paddingTopBottom,pauseWidth,getHeight()-2*paddingTopBottom);
			g2d.fillRect(50-paddingSide-pauseWidth,paddingTopBottom,pauseWidth,getHeight()-2*paddingTopBottom);

			g2d.fillRect(getWidth()-50+((50-20)/2),10,20,20);
		}

		private void drawDownArrow(Graphics2D g2d)
		{
			//底部按钮右侧
			g2d.setColor(ColorsKit.GRAY);
			g2d.fillRect(0,0,50,50);

			g2d.setColor(Color.GRAY);
			g2d.fillRect((50-20)/2,8,20,20);

			Polygon polygon=new Polygon(arrowX,arrowY,arrowX.length);
			g2d.fill(polygon);

		}

		private void drawMagnifier(Graphics2D g2d)
		{

		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			//点击左侧暂停按钮
			if(e.getX()<50)
				mode.pause();
			//点击右侧下落按钮
			else if(e.getX()>getWidth()-50)
			{
				mode.down();

				if(mode.isFull())
					return;

				//窗口震下效果
				//如果已经在下落，返回，防止其他线程进入
				if(falling)
					return;

				Thread t=new Thread()
				{
					@Override
					public void run()
					{
						falling=true;
						int y=frame.getY();
						int desty=y+6;
						while (frame.getY()<desty)
						{
							frame.setLocation(frame.getX(),frame.getY()+1);
							try
							{
								Thread.sleep(10);
							}
							catch (InterruptedException e)
							{}
						}

						while (frame.getY()>y)
						{
							frame.setLocation(frame.getX(),frame.getY()-1);
							try
							{
								Thread.sleep(10);
							}
							catch (InterruptedException e)
							{}
						}
						falling=false;
					}
				};
				t.setPriority(3);
				t.start();
			}
			//点击中间单词按钮
			else
			{
				//清除单词方块，是一个单词，则清除方块并下落，否则清除所有方块选中
				if(mode.cleanBlocks())
					//移动方块
					mode.movingBlocks();

				//游戏底部重绘
				gameBottom.repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent e)
		{

		}

		@Override
		public void mouseReleased(MouseEvent e)
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


	}
}
