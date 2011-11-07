package org.ruscoe.example.tilegame;

import android.content.Context;

/**
 * An extension of GameImage, GameUi represents a UI element provided
 * for the user.
 * 
 * In addition to the GameImage properties, UI elements include a
 * state identifier, allowing buttons to appear in different states
 * with associated drawables.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class GameUi extends GameUnit
{
	public static final int STATE_NORMAL = 1;
	public static final int STATE_INACTIVE = 2;
	public static final int STATE_ACTIVE = 3;
	public static final int STATE_READY = 4;
	
	private int mState = STATE_NORMAL;
	
	private int mDrawableStateNormal = 0;
	private int mDrawableStateInactive = 0;
	private int mDrawableStateActive = 0;
	private int mDrawableStateReady = 0;
	private boolean mVisible = true;

	private Context mContext = null;
	
	public GameUi(Context context, int drawable)
	{
		super(context, drawable);
		this.mContext = context;
		this.mDrawableStateNormal = drawable;
	}

	public void setStateNormal()
	{
		this.mState = STATE_NORMAL;
		
		if (this.mDrawableStateNormal > 0)
		{
			this.setDrawable(this.mContext, this.mDrawableStateNormal);
		}
	}
	
	public void setStateInactive()
	{
		this.mState = STATE_INACTIVE;
		
		if (this.mDrawableStateInactive > 0)
		{
			this.setDrawable(this.mContext, this.mDrawableStateInactive);
		}
	}
	
	public void setStateActive()
	{
		this.mState = STATE_ACTIVE;
		
		if (this.mDrawableStateActive > 0)
		{
			this.setDrawable(this.mContext, this.mDrawableStateActive);
		}
	}
	
	public void setStateReady()
	{
		this.mState = STATE_READY;
		
		if (this.mDrawableStateReady > 0)
		{
			this.setDrawable(this.mContext, this.mDrawableStateReady);
		}
	}
	
	public int getDrawableStateNormal()
	{
		return this.mDrawableStateNormal;
	}

	public void setDrawableStateNormal(int mDrawableStateNormal)
	{
		this.mDrawableStateNormal = mDrawableStateNormal;
	}

	public int getDrawableStateInactive()
	{
		return this.mDrawableStateInactive;
	}

	public void setDrawableStateInactive(int mDrawableStateInactive)
	{
		this.mDrawableStateInactive = mDrawableStateInactive;
	}

	public int getDrawableStateActive()
	{
		return this.mDrawableStateActive;
	}

	public void setDrawableStateActive(int mDrawableStateActive)
	{
		this.mDrawableStateActive = mDrawableStateActive;
	}

	public int getDrawableStateReady()
	{
		return this.mDrawableStateReady;
	}

	public void setDrawableStateReady(int mDrawableStateReady)
	{
		this.mDrawableStateReady = mDrawableStateReady;
	}
	
	public boolean isStateNormal()
	{
		return (this.mState == STATE_NORMAL);
	}
	
	public boolean isStateInactive()
	{
		return (this.mState == STATE_INACTIVE);
	}
	
	public boolean isVisible()
	{
		return this.mVisible;
	}
	
	public void setVisible(boolean visible)
	{
		this.mVisible = visible;
	}
}
