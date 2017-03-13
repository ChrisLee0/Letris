package com.seecen.letris.controller;

import java.util.Random;

/**
 * Created by Constantine on 2016-06-20 0020.
 */
public class ClassicMode extends Mode
{

	public ClassicMode()
	{
		super();
	}


	/***
	 * 加速下落
	 */
	@Override
	public void down()
	{
		//还没有下落完,并且没有暂停
		if(!isFull() && !isPause)
		{
			speedup=true;
		}
	}

	/**
	 * 暂停线程
	 */
	@Override
	public void pause()
	{
		if(!isFull())
		{
			if(isPause)
			{
				isPause=false;
				downThread=new Thread(this);
				downThread.start();
			}
			else
			{
				isPause=true;
			}
		}
	}
}
