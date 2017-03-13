package com.seecen.letris.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Constantine on 2016-06-28 0028.
 */
public final class Logo
{
	private Logo()
	{}

	private final static int logoCellSize=25;
	private final static int margin=5;
	private final static int paddingTop=7;
	private final static int paddingLeft=24;

	public final static BufferedImage logoImg=new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);

	static
	{
		Graphics2D g2d=logoImg.createGraphics();

		//绘制Logo红色背景
		g2d.setColor(ColorsKit.RED);
		g2d.fillRect(0,0,100,100);

		//Logo方块
		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddingLeft,paddingTop,logoCellSize,logoCellSize);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddingLeft,paddingTop+logoCellSize+margin,logoCellSize,logoCellSize);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddingLeft,paddingTop+logoCellSize*2+margin*2,logoCellSize,logoCellSize);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddingLeft+logoCellSize+margin,paddingTop+logoCellSize*2+margin*2,logoCellSize,logoCellSize);

	}


}
