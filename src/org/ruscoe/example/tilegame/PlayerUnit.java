package org.ruscoe.example.tilegame;

import android.content.Context;

/**
 * An extension of GameUnit, PlayerUnit represents the player-controlled
 * unit in the game.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class PlayerUnit extends GameUnit
{
	public static final int SPEED = 3;
	
	Context mContext;
	
	private int mUnmodifiedX = 0;
	private int mUnmodifiedY = 0;
	
	public PlayerUnit(Context context, int drawable)
	{
		super(context, drawable);
		this.mContext = context;
	}

	public int getUnmodifiedX()
	{
		return this.mUnmodifiedX;
	}

	public void setUnmodifiedX(int unmodifiedX)
	{
		this.mUnmodifiedX = unmodifiedX;
	}

	public int getUnmodifiedY()
	{
		return this.mUnmodifiedY;
	}

	public void setUnmodifiedY(int unmodifiedY)
	{
		this.mUnmodifiedY = unmodifiedY;
	}
}
