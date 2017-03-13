package com.seecen.letris.ui;

import com.seecen.letris.controller.Mode;
import com.seecen.letris.util.ColorsKit;
import com.seecen.letris.util.FontsKit;
import com.seecen.letris.util.Logo;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by Constantine on 2016-06-20 0020.
 */

/**
 * 主面板
 */
public class MainBoard extends Panel implements Runnable,MouseMotionListener,MouseListener
{
	//LOGO下方字体
	private Font logoFont=new Font("Courier New",Font.BOLD,32);
	//LOGO下方字母L
	private Font logoFontL=new Font("Courier New",Font.BOLD,36);
	//Enter按钮字体
	private Font enterButtonFont=new Font("Courier New",Font.BOLD,42);

	//父窗口
	private MainFrame frame;

	//记录鼠标按下的位置，用于拖动窗口
	private int oldX=0;
	private int oldY=0;

	//恒等变换
	private static final AffineTransform identity=new AffineTransform();
	//缓冲图
	private BufferedImage bufferedImage=new BufferedImage(361,611,BufferedImage.TYPE_INT_ARGB);
	//缓冲Graphics对象
	private Graphics2D buffg2d=bufferedImage.createGraphics();

	//弹出窗口缓冲图以及Graphics对象
	private BufferedImage popImg=new BufferedImage(361,220,BufferedImage.TYPE_INT_ARGB);
	private Graphics2D popg2d=popImg.createGraphics();

	//弹出菜单缩放程度
	private double scale=1;

	//弹出菜单是否显示
	private boolean menuShow=false;

	//弹出菜单矩形区域
	private static final Rectangle menuBox=new Rectangle((361-180)/2,440,180,50);
	//开始按钮区域
	private static final Rectangle startBox=new Rectangle(80,340,80,35);
	//退出按钮区域
	private static final Rectangle exitBox=new Rectangle(361-80-80,340,80,35);
	private Rectangle modeBox=new Rectangle((361-200)/2,220,200,40);
	private Rectangle hardBox=new Rectangle((361-200)/2,280,200,40);

	//模式字符串，经典模式、放松模式
	private static final Mode.ModeEnum[] modes={Mode.ModeEnum.Classic,Mode.ModeEnum.Relax};
	//难度字符串，简单，中等，困难
	private static final String[] level={"Easy","Middle","Hard"};

	//记录模式
	private int modeIdx=0;
	private int levelIdx=0;


	public MainBoard(MainFrame frame)
	{
		//保存窗口引用
		this.frame=frame;

		paintPopMenu(popg2d);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	//重绘界面
	@Override
	public void repaint()
	{
		Graphics g=this.getGraphics();

		if(g==null)
			super.repaint();
		else
			paint(g);
	}

	/***
	 * 绘制启动画面
	 * @param g2d
	 */

	public void paintStartup(Graphics2D g2d)
	{
		//清除恒等变换
		g2d.setTransform(identity);
		//抗锯齿
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//用白色填充背景
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,361,getHeight());

		g2d.drawImage(Logo.logoImg,(getWidth()-Logo.logoImg.getWidth())/2,80,this);


		//所有方块宽度
		String letris="LETRIS";
		int blocksW= Mode.Block.BLOCK_SIZE*letris.length();
		//起始位置
		int startX=(getWidth()-blocksW)/2;

		//设置字体
		g2d.setFont(FontsKit.blockLetterFont);
		FontRenderContext frc=g2d.getFontRenderContext();


		for (int i = 0; i <letris.length(); i++)
		{
			//绘制方块，第一个特殊处理
			if(i==0)
			{
				g2d.setColor(ColorsKit.RED);
				g2d.fillRect(startX+i*Mode.Block.BLOCK_SIZE,240,
						Mode.Block.BLOCK_SIZE,
						Mode.Block.BLOCK_SIZE+1);
			}
			else
			{
				g2d.setColor(Color.GRAY);
				g2d.drawRect(startX+i*Mode.Block.BLOCK_SIZE,240,
						Mode.Block.BLOCK_SIZE,
						Mode.Block.BLOCK_SIZE);
			}


			String letter=letris.substring(i,i+1);

			//测量字母外围矩形
			TextLayout layout=new TextLayout(letter,FontsKit.blockLetterFont,frc);
			Rectangle2D rect=layout.getBounds();
			int letterW=g2d.getFontMetrics().stringWidth(letter);
			rect.setRect(startX+i*Mode.Block.BLOCK_SIZE+(Mode.Block.BLOCK_SIZE-rect.getWidth())/2,
					240+(Mode.Block.BLOCK_SIZE-letterW)/2,
					letterW,rect.getHeight());

			g2d.setColor(i==0?Color.WHITE:Color.BLACK);
			//绘制字母
			g2d.drawString(letter,
					(int)rect.getX(),
					(int)(rect.getY()+rect.getHeight()+2));
		}

		//绘制PLAY按钮背景
		g2d.setColor(ColorsKit.RED);
		g2d.fillRect((getWidth()-180)/2,440,180,50);

		//绘制文字PLAY
		g2d.setColor(Color.WHITE);
		int w=g2d.getFontMetrics().stringWidth("PLAY");
		g2d.drawString("PLAY",(getWidth()-w)/2,475);

	}

	//绘制菜单层
	public void paintMenu(Graphics2D g2d)
	{
		//清除恒等变换
		g2d.setTransform(identity);
		//抗锯齿
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);


		//绘制阴影层
		g2d.setColor(new Color(0,0,0,100));
		g2d.fillRect(0,0,361,611);

		//被缩放后的图像大小
		double imgwidth=popImg.getWidth()*scale;
		double imgheight=popImg.getHeight()*scale;

		//移动坐标原点
		g2d.translate((361-imgwidth)/2,200+(200-imgheight)/2);
		//缩放
		g2d.scale(scale,scale);
		//绘制弹出菜单
		g2d.drawImage(popImg,0,0,this);


	}

	//绘制弹出菜单
	private void paintPopMenu(Graphics2D g2d)
	{
		//去除任何变换
		g2d.setTransform(identity);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		int width=361;

		//绘制弹出框背景
		g2d.setColor(Color.WHITE);
		g2d.fillRoundRect((width-320)/2,10,320,200,8,8);

		//绘制右上角圆背景
		g2d.fillOval(320+10,0,20,20);

		//去除变换
		g2d.transform(identity);

		g2d.setFont(FontsKit.popMenuFont);

		//绘制模式切换框
		g2d.setColor(ColorsKit.LINE);
		g2d.fillRoundRect((width-200)/2,30,200,40,8,8);

		//绘制模式切换框左箭头
		int[] leftArrowXs={(width-200)/2+4,(width-200)/2+10+4,(width-200)/2+10+4};
		int[] leftArrowYs={30+20,30+10,30+40-10};
		Polygon leftArrow=new Polygon(leftArrowXs,leftArrowYs,leftArrowXs.length);
		g2d.setColor(ColorsKit.GRAY);
		g2d.fill(leftArrow);

		//绘制模式切换框右箭头
		int[] rightArrowXs={(width-200)/2+200-10-4,(width-200)/2+200-10-4,(width-200)/2+200-4};
		int[] rightArrowYs={30+10,30+40-10,30+20};
		Polygon rightArrow=new Polygon(rightArrowXs,rightArrowYs,rightArrowXs.length);
		g2d.fill(rightArrow);

		int strw=g2d.getFontMetrics().stringWidth(modes[modeIdx].toString());
		g2d.drawString(modes[modeIdx].toString(),(width-strw)/2,30+40-12);


		//绘制难度切换框
		g2d.setColor(ColorsKit.LINE);
		g2d.fillRoundRect((width-200)/2,90,200,40,8,8);
		g2d.setColor(ColorsKit.GRAY);
		leftArrow.translate(0,60);
		g2d.fill(leftArrow);
		rightArrow.translate(0,60);
		g2d.fill(rightArrow);

		strw=g2d.getFontMetrics().stringWidth(level[levelIdx]);
		g2d.drawString(level[levelIdx],(width-strw)/2,90+40-12);


		//绘制开始按钮
		g2d.setColor(ColorsKit.RED);
		g2d.fillRoundRect(80,150,80,35,8,8);
		g2d.setColor(Color.WHITE);
		g2d.drawString("GO",80+(80-g2d.getFontMetrics().stringWidth("GO"))/2,
				150+35-10);


		//绘制退出
		g2d.setColor(ColorsKit.LIGHT_GRAY);
		g2d.fillRoundRect(width-80-80,150,80,35,8,8);
		g2d.setColor(Color.WHITE);
		g2d.drawString("EXIT",width-80-80+(80-g2d.getFontMetrics().stringWidth("EXIT"))/2,
				150+35-10);

		//绘制关闭按钮图标
		//移动坐标
		g2d.translate(width-21,10);
		//旋转45度
		g2d.rotate(Math.PI/4);
		//按钮边长
		int boxsize=7;


		//绘制×
		g2d.setColor(Color.GRAY);
		g2d.drawLine(-boxsize,0,boxsize,0);
		g2d.drawLine(0,-boxsize,0,boxsize);

	}



	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d=(Graphics2D) g;

		//绘制启动菜单
		paintStartup(buffg2d);

		//如果显示菜单
		if(menuShow)
			//重绘菜单到缓冲图
			paintMenu(buffg2d);

		g2d.drawImage(bufferedImage,0,0,this);




	}

	@Override
	public void run()
	{
		scale=0.01;
		while(scale<1.1)
		{
			scale+=0.04;
			repaint();
			try
			{
				Thread.sleep(3);
			}
			catch (InterruptedException e) {}
		}
		while(scale>1.0)
		{
			scale-=0.01;
			repaint();
			try
			{
				Thread.sleep(3);
			}
			catch (InterruptedException e) {}
		}

		if(scale<1 || scale>1)
			scale=1;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		//移动到Play按钮变手
		if(!menuShow && menuBox.contains(e.getPoint()))
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		else
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR ));
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		int xOnScreen = e.getXOnScreen();
		int yOnScreen = e.getYOnScreen();
		int xx = xOnScreen - oldX;
		int yy = yOnScreen - oldY;
		frame.setLocation(xx, yy);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		//如果窗口超过屏幕顶部，则让窗口贴屏幕顶部
		if(frame.getY()<0)
			frame.setLocation(frame.getX(),0);

		if(e.getXOnScreen()<4)
			frame.setLocation(0,frame.getY());
		int width=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		if(width-e.getXOnScreen()<4)
			frame.setLocation(width-getWidth(),frame.getY());

	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		oldX=e.getX();
		oldY=e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		//显示了菜单
		if(menuShow)
		{
			//点击了关闭按钮
			Rectangle r=new Rectangle(330,190,20,20);
			if(r.contains(e.getPoint()))
			{
				Thread t=new Thread()
				{
					@Override
					public void run()
					{
						while (scale>0.1)
						{
							scale-=0.04;
							repaint();
							try
							{
								Thread.sleep(4);
							}
							catch (InterruptedException e){}
						}

						menuShow=false;
						repaint();
					}
				};
				t.start();
			}
			//点击了开始游戏按钮
			else if(startBox.contains(e.getPoint()))
			{
				this.frame.setMode(modes[modeIdx]);
				frame.showGameBoard();
			}
			//点击了退出按钮
			else if(exitBox.contains(e.getPoint()))
				System.exit(0);
			//点击了模式区域
			else if(modeBox.contains(e.getPoint()))
			{
				modeIdx=(modeIdx+1)%modes.length;
				paintPopMenu(popg2d);
				repaint();

			}
			//点击了难度区域
			else if(hardBox.contains(e.getPoint()))
			{
				if(e.getX()<360/2)
					levelIdx--;
				else
					levelIdx++;

				if(levelIdx==-1)
					levelIdx=2;
				else if(levelIdx==3)
					levelIdx=0;

				paintPopMenu(popg2d);
				repaint();
			}

		}
		else
		{
			//点击了PLAY按钮
			if(menuBox.contains(e.getPoint()))
			{
				menuShow=true;
				repaint();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR ));

				Thread t=new Thread(MainBoard.this);
				t.start();
			}
		}





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
