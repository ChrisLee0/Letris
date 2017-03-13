package com.seecen.letris.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Constantine on 2016-06-24 0024.
 */
public class Dictionary
{
	private static int[] wordHash;

	static
	{
		DataInputStream dis=null;
		try
		{
			File dbfile=new File("words.db");
			dis = new DataInputStream(new FileInputStream(dbfile));
			wordHash=new int[(int)(dbfile.length()/4)];
			for (int i = 0; i < wordHash.length; i++)
				wordHash[i]=dis.readInt();

		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error:"+e);
		}
		finally
		{
			if (dis!=null)
				try
				{
					dis.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		}

	}


	public static boolean isWord(String leters)
	{
		int hash=leters.hashCode();

		int left=0;
		int right=wordHash.length-1;
		int mid=-1;

		while (left<=right)
		{
			mid=(left+right)/2;

			if(hash==wordHash[mid])
				break;
			else
			{
				if(hash<wordHash[mid])
					right=mid-1;
				else
					left=mid+1;
			}
		}

		return left<=right;
	}
}
