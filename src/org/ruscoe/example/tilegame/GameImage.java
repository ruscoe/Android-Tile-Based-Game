package org.ruscoe.example.tilegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * The GameImage class represents an image used in the game.
 * 
 * Each instance contains a Bitmap image, a width / height
 * and an on-screen position.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class GameImage 
{
	protected Bitmap mImg = null;
	protected int mX = 0;
	protected int mY = 0;
	protected int mWidth = 0;
	protected int mHeight = 0;

	public GameImage(Context context)
	{
	}
	
	public GameImage(Context context, int drawable)
	{
		this.setDrawable(context, drawable);
	}

	public void setDrawable(Context context, int drawable)
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		this.mImg = BitmapFactory.decodeResource(context.getResources(), drawable);

		mWidth = this.mImg.getWidth();
		mHeight = this.mImg.getHeight();
	}
	
	public void setBitmap(Bitmap bitmap)
	{
		if (bitmap != null)
		{
			this.mImg = bitmap;
			this.mWidth = bitmap.getWidth();
			this.mHeight = bitmap.getHeight();
		}
	}
	
	public Bitmap getBitmap()
	{
		return this.mImg;
	}

	public int getWidth()
	{
		return this.mWidth;
	}

	public int getHeight()
	{
		return this.mHeight;
	}
	
	void setX(int x)
	{
        this.mX = x;
    }

	public int getX()
	{
		return this.mX;
	}

	public void setY(int y)
	{
        this.mY = y;
	}

	public int getY()
	{
		return this.mY;
	}

	public void setCenterX(int centerX)
	{
		this.mX = (centerX - (this.getWidth() / 2));
	}
	
	public int getCenterX()
	{
		return (mX + (this.getWidth() / 2));
	}
	
	public void setCenterY(int centerY)
	{
		this.mY = (centerY - (this.getHeight() / 2));
	}
	
	public int getCenterY()
	{
		return (mY + (this.getHeight() / 2));
	}
}
