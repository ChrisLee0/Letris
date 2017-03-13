package com.seecen.letris.controller;

import com.seecen.letris.ui.GameCanvas;
import com.seecen.letris.util.ColorsKit;
import com.seecen.letris.util.Dictionary;
import com.seecen.letris.util.FontsKit;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * Created by Constantine on 2016-06-20 0020.
 */
public abstract class Mode  implements Runnable
{

	//行与列方块的数量
	public final static int BLOCKS_ROW_NUM = 8;
	public final static int BLOCKS_CLO_NUM = 6;

	//各个方块状态
	protected volatile Block[][] blocks = new Block[BLOCKS_ROW_NUM][BLOCKS_CLO_NUM];

	//被选中的方块
	protected ArrayList<Block> selectedBlockList =new ArrayList<>();

	//被清除的方块
	protected ArrayList<Block> flyingBlocks=new ArrayList<>();

	//状态
	public enum GameStatus{
		EMPTY,//未输入单词
		WRONG,//单词拼写错误
		CORRECT};//单词正确

	//游戏状态，默认为空
	private GameStatus status=GameStatus.EMPTY;

	//还未点击单词
	private boolean untype=true;

	//游戏绘图区
	protected GameCanvas canvas;

	//下落线程
	protected Thread downThread;

	//刷新界面线程
	protected Thread refreshThread;

	//刷新界面计数器
	protected volatile int refreshThreadCount=0;

	//是否暂停
	protected boolean isPause=false;

	//是否满了
	protected boolean isFull=false;

	//当前正在下落的方块
	protected Block currentBlock;

	//当前列数设置为第一列
	protected int downclo = 0;

	//是否加速下落
	protected boolean speedup=false;
	protected Random random=new Random();

	//下落速度
	protected int speed=1;

	//仿射变换
	AffineTransform affineTransform=new AffineTransform();

	//透明度1
	private static final AlphaComposite alpha1_0=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	//透明度
	private static final AlphaComposite alphaVar=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.5f);

	/**
	 * 模式构造函数
	 */
	public Mode()
	{
		status=GameStatus.EMPTY;
	}

	/**
	 * 开始游戏
	 */
	public void startGame()
	{
		//创建方块下落线程
		downThread=new Thread(this);
		//启动方块下落线程
		downThread.start();
	}



	/**
	 * 开始启动界面刷新线程
	 */
	protected void startRefresh()
	{

		refreshThreadCount++;
		if(refreshThreadCount>1)
			return;

		//创建刷新线程
		refreshThread=new Thread()
		{
			@Override
			public void run()
			{

				while (refreshThreadCount>0)
				{
					canvas.repaint();
					System.out.println("count="+refreshThreadCount);
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						return;
					}
				}
				canvas.repaint();
			}
		};
		refreshThread.start();
	}

	/**
	 * 释放刷新界面线程，计数器减1
	 */
	public void releaseRefresh()
	{
		refreshThreadCount--;
		if(refreshThreadCount<0)
			refreshThreadCount=0;
	}

	/**
	 * 方块下落线程
	 */
	@Override
	public void run()
	{
		//开始刷新
		startRefresh();

		//方块未满
		while(!isFull())
		{
			if(currentBlock==null)
			{
				//方块列数、X、Y
				Block block = new Block(downclo, -Block.BLOCK_SIZE);
				block.setLetter((char)('A'+(Math.abs(random.nextInt()%26))));
				currentBlock = block;
			}
			//不断下落
			while(currentBlock.down(speedup?15:speed,blocks,this))
			{

				if(isPause==true)
				{
					//释放刷新
					releaseRefresh();
					return;
				}

				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
					System.out.println("try exit");
					//释放刷新
					releaseRefresh();
					return;
				}
			}
			speedup=false;
			//下落完成，方块已经合并到所有方块数组里
			currentBlock=null;
			//列数增加
			downclo++;

			//当前列到尽头后，折回第一列
			if(downclo ==BLOCKS_CLO_NUM)
				downclo =0;
		}

		//释放刷新
		releaseRefresh();
	}

	/**
	 * 取得方块数组
	 * @return 返回方块数组
	 */
	public Block[][] getBlock()
	{
		return blocks;
	}

	/**
	 * 取得当前下落的方块
	 * @return 返回当前下落方块
	 */
	public Block getCurrentBlock(){return  currentBlock;}

	/**
	 * 获得被炸飞的方块列表
	 * @return 被炸飞的方块列表
	 */
	public List<Block> getFlyingBlocks()
	{
		return flyingBlocks;
	}

	/**
	 * 取得用户已经点击的单词
	 * @return 返回单词字符串
	 */
	public String getWord()
	{
		if(selectedBlockList.size()==0 && untype)
			return "LETRIS";

		StringBuffer sb=new StringBuffer();
		for(Block b: selectedBlockList)
			sb.append(b.getLetter());
		return sb.toString();
	}


	/**
	 * 是否满了
	 * @return true代表满了,false代表还没满
	 */
	public boolean isFull()
	{
		return isFull;
	}

	/**
	 * 设置是否满了
	 * @param full 设置状态
	 */
	public void setFull(boolean full)
	{
		isFull = full;
	}

	/**
	 * 方块加速下落
	 */
	public abstract void down();

	/**
	 * 游戏暂停
	 */
	public abstract void pause();

	/**
	 * 清除单词
	 * @return 是否需要移动方块
	 */
	public boolean cleanBlocks()
	{
		switch (status)
		{
			case CORRECT:

				//遍历被选中的单词
				for(Block b : selectedBlockList)
				{
					//添加到炸飞的方块里
					flyingBlocks.add(b);
					//方块表格里清空
					blocks[b.blockRow][b.blockCol]=null;
				}

				//被选中的方块清空
				selectedBlockList.clear();
				//状态置为空
				status=GameStatus.EMPTY;


				return true;
			case WRONG:
				for(Block b : selectedBlockList)
					b.setSelected(false);
				selectedBlockList.clear();
				status=GameStatus.EMPTY;

				return false;
			case EMPTY:
			default:
					return false;
		}
	}

	//取得游戏状态
	public GameStatus getStatus()
	{
		return status;
	}

	//设置Canvas
	public void setCanvas(GameCanvas canvas)
	{
		this.canvas=canvas;
	}

	public boolean onclick(Point point)
	{

		int row=point.y/Block.BLOCK_SIZE;
		int col=point.x/Block.BLOCK_SIZE;

		if(blocks[row][col]!=null)
		{
			//如果之前已经被选中
			if(blocks[row][col].isSelected())
				//移除这个方块
				selectedBlockList.remove(blocks[row][col]);
			else
				//添加方块
				selectedBlockList.add(blocks[row][col]);

			//反转这个方块的状态
			blocks[row][col].reverseSelect();

			//有选中的方块
			if(selectedBlockList.size()>0)
			{
				if(selectedBlockList.size()>=3 && Dictionary.isWord(getWord()))
					status=GameStatus.CORRECT;
				else
					status=GameStatus.WRONG;
			}
			else
				status=GameStatus.EMPTY;

			//已经输入单词
			untype=false;

			//重绘画布
			if(refreshThreadCount==0)
				canvas.repaint();

			//返回真，告诉canvas需要重绘
			return true;
		}


		if(currentBlock!=null && currentBlock.contains(point))
		{
			if(currentBlock.isSelected())
				selectedBlockList.remove(currentBlock);
			else
			{
				currentBlock.setSelected(true);
				selectedBlockList.add(currentBlock);
			}

			currentBlock.setSelected(currentBlock.isSelected());

			//重绘画布
			if(refreshThreadCount==0)
				canvas.repaint();

			return true;
		}


		return false;
	}

	/**
	 * 清除方块后移动方块
	 */
	public void movingBlocks()
	{
		boolean hasEmpty=false;

		int rowEmptyBottom=-1;

		List<Block> movingBlocks=new ArrayList<Block>();

		//列从左往右
		for (int clo = 0; clo < BLOCKS_CLO_NUM; clo++)
		{
			//是否有空
			hasEmpty=false;
			//最底部空位行
			rowEmptyBottom=-1;
			//行从下到上
			for (int row = BLOCKS_ROW_NUM-1; row >= 0 ; row--)
			{
				//如果当前位置为空
				if(blocks[row][clo]==null)
				{
					//下面没有空位
					if (!hasEmpty)
					{
						//记录空位位置
						rowEmptyBottom=row;
						//已经有空位
						hasEmpty=true;
					}
				}
				//当前位置不为空
				else
				{
					//下面有空位
					if(hasEmpty)
					{
						//当前方块目标行位置置为底部行空位
						blocks[row][clo].setBlockRow(rowEmptyBottom);
						//把当前方块移到底部空位
						blocks[rowEmptyBottom][clo]=blocks[row][clo];
						//当前空位置空
						blocks[row][clo]=null;
						//添加到需要下落的方块
						movingBlocks.add(blocks[rowEmptyBottom][clo]);
						hasEmpty=false;
						row=rowEmptyBottom;
					}
				}
			}
		}

		//方块掉落线程
		Thread t=new Thread()
		{
			@Override
			public void run()
			{
				//开始刷新线程
				startRefresh();

				while (movingBlocks.size()>0)
				{
					for (int i = 0; i <movingBlocks.size(); i++)
					{
						Block b=movingBlocks.get(i);
						//下落完成后移除
						if(!b.falling(5))
							movingBlocks.remove(b);
					}

					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				//释放刷新线程
				releaseRefresh();
			}
		};
		t.setPriority(6);
		t.start();
	}

	/**
	 * 方块类
	 */
	public class Block
	{
		//方块大小
		public final static int BLOCK_SIZE = 60;

		//字母
		private char letter = 0;

		//是否被选中
		private boolean selected = false;

		//行
		private int blockRow=-1;

		//列
		private int blockCol=-1;

		//矩形
		private Rectangle rect;

		//被清除的时间戳
		private long timestamp;

		//透明度
		private double opcity;

		//目标炸飞位置
		private Point destFlyPoint;

		/**
		 *
		 * @param col 所属列
		 * @param posY 初始Y
		 */
		public Block(int col,int posY)
		{
			//列
			this.blockCol=col;
			//方块位置和形状
			rect=new Rectangle(BLOCK_SIZE*col,posY,BLOCK_SIZE,BLOCK_SIZE);
		}

		/**
		 * 绘制当前方块
		 * @param g2d Graphics2D 绘图对象
		 */
		public void paint(Graphics2D g2d)
		{
				//是否被选中
				if(selected)
				{
					if(status==GameStatus.WRONG)
						g2d.setPaint(ColorsKit.RED);
					else
						g2d.setPaint(ColorsKit.BLUE);
				}
				else
					g2d.setPaint(Color.WHITE);

				//填充方块背景
				g2d.fill(rect);


				g2d.setFont(FontsKit.blockLetterFont);
				g2d.setColor(Color.BLACK);
				int letterWidth=g2d.getFontMetrics().stringWidth(letter+"");
				int letterHeight=g2d.getFontMetrics().getHeight();
				Rectangle2D.Double rt=new Rectangle2D.Double();
				rt.setRect(rect.getX()+((BLOCK_SIZE-letterWidth)/2.0),rect.getY(),letterWidth,letterHeight);
				//绘制字母
				//g2d.draw(rt);
				g2d.drawString(String.valueOf(letter),(int)rt.getX(),(int)rt.getY()+(int)rt.getHeight());

				//绘制边框
				g2d.setPaint(Color.GRAY);
				g2d.draw(rect);

		}

		/**
		 * 绘制炸飞的方块
		 * @param g2d Graphics2D 绘图对象
		 */
		public void paintFlying(Graphics2D g2d)
		{
			g2d.setComposite(alphaVar);

			g2d.setColor(Color.GREEN);
			g2d.fill(rect);

			g2d.setComposite(alpha1_0);
		}

		public char getLetter()
		{
			return letter;
		}

		public void setLetter(char letter)
		{
			if (letter >= 'A' && letter <= 'Z') this.letter = letter;
		}

		public boolean isSelected()
		{
			return selected;
		}

		/**
		 * 反转选择状态
		 */
		public void reverseSelect()
		{
			selected=!selected;
		}

		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}

		/**
		 * 设置方块的目标行数
		 * @param blockRow
		 */
		public void setBlockRow(int blockRow)
		{
			this.blockRow = blockRow;
			//设置目标Y
			int destY=blockRow*BLOCK_SIZE;
		}

		public Point getDestFlyPoint()
		{
			return destFlyPoint;
		}

		public void setDestFlyPoint(Point destFlyPoint)
		{
			this.destFlyPoint = destFlyPoint;
		}

		public double getOpcity()
		{
			return opcity;
		}

		public void setOpcity(double opcity)
		{
			this.opcity = opcity;
		}

		public Rectangle getRect()
		{
			return rect;
		}

		public void setRect(Rectangle rect)
		{
			this.rect = rect;
		}

		public long getTimestamp()
		{
			return timestamp;
		}

		public void setTimestamp(long timestamp)
		{
			this.timestamp = timestamp;
		}

		public boolean contains(Point point)
		{
			return rect.contains(point);
		}

		/**
		 *
		 * @param distance 下落距离
		 * @param blocks 所有方块
		 * @return 是否需要继续下落，true代表还没有下落完成，false代表下落完成
		 */
		boolean down(int distance,Block[][] blocks,Mode mode)
		{
			//目标Y位置
			int destY=0;
			//目标行数，设为异常值
			int destRow;

			//从上往下第一个方块行数假设为最底部的下一行，即没有方块
			int bottomRow=blocks.length;

			//从这一列从上往下遍历
			for (int row = 0; row <blocks.length; row++)
				//找到最顶部方块
				if(blocks[row][this.blockCol]!=null)
				{
					//记录顶部方块
					bottomRow=row;
					break;
				}

			//这一列已经满了
			if(bottomRow<=0)
			{
				mode.setFull(true);
				return false;
			}
			//目标行
			destRow=bottomRow-1;

			//计算目标Y位置
			destY=BLOCK_SIZE*destRow;

			//下落后的位置
			double y=rect.getY()+distance;
			//如果超过最终位置，则设置为最终位置
			y=y>destY?destY:y;
			//方块设置为下落后位置
			rect.setRect(rect.getX(),y,
					rect.getWidth(),rect.getHeight());
			//返回是否到达目标位置
			if(y==destY)
			{
				blocks[destRow][blockCol]=this;
				//设置当前方块的所在行
				setBlockRow(destRow);
				return false;
			}
			else
				return true;
		}

		/***
		 *方块下落，在消除单词后留下空位时调用
		 * @param distance 下落距离
		 * @return 是否下落完成,true未完成,false完成
		 */
		public boolean falling(int distance)
		{
			//目标Y位置,行*方块大小
			double destY=this.blockRow*BLOCK_SIZE;
			//当前Y位置
			double y=rect.getY();
			//当前位置加下落距离
			y+=distance;

			//当前位置大于目标位置
			if(y>=destY)
				//使当前位置不超过目标位置
				y=destY;

			//设置新的位置
			rect.setLocation((int)rect.getX(),(int)y);

			//当前Y是否小于目标Y
			return y<destY;
		}


		public void cleared()
		{
			timestamp=System.currentTimeMillis();

		}
	}

}


