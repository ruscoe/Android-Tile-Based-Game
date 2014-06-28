package org.ruscoe.example.tilegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ruscoe.example.tilegame.PlayerUnit;
import org.ruscoe.example.tilegame.R;
import org.ruscoe.example.tilegame.data.GameLevelTileData;
import org.ruscoe.example.tilegame.data.GameTileData;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The game view and main game thread.
 * 
 * GameView creates a new thread (GameThread) to handle all calculations
 * and drawing of game components.
 * 
 * GameThread contains the run() function, which serves as the game loop,
 * updating each cycle while the game is running.
 * 
 * To see how game level data is parsed and turned into a playable, tile level,
 * see the function GameView.parseGameLevelData.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback
{
	private static final int CONTROLS_PADDING = 10;

	private static final int START_STAGE = 1;
	private static final int START_LEVEL = 1;

	private static final int DIRECTION_UP = 1;
	private static final int DIRECTION_DOWN = 2;
	private static final int DIRECTION_LEFT = 3;
	private static final int DIRECTION_RIGHT = 4;

	public static final int STATE_RUNNING = 1;
	public static final int STATE_PAUSED = 2;

	private int mScreenXMax = 0;
	private int mScreenYMax = 0;
	private int mScreenXCenter = 0;
	private int mScreenYCenter = 0;
	private int mScreenXOffset = 0;
	private int mScreenYOffset = 0;

	private float mScreenDensity;
	
	private Context mGameContext;
	private Play mGameActivity;
	private SurfaceHolder mGameSurfaceHolder = null;

	private boolean updatingGameTiles = false;

	private GameTileData mGameTileData = null;
	private GameLevelTileData mGameLevelTileData = null;

	private PlayerUnit mPlayerUnit = null;

	private int mPlayerStage = START_STAGE;
	private int mPlayerLevel = START_LEVEL;

	private Bitmap mBackgroundImage = null;

	private int mGameState;

	private boolean mGameRun = true;

	private boolean mPlayerMoving = false;
	private int mPlayerVerticalDirection = 0;
	private int mPlayerHorizontalDirection = 0;

	private GameUi mCtrlUpArrow = null;
	private GameUi mCtrlDownArrow = null;
	private GameUi mCtrlLeftArrow = null;
	private GameUi mCtrlRightArrow = null;
	
	private Paint mUiTextPaint = null;
	private String mLastStatusMessage = "";

	/**
	 * Templates defining all available game tiles.
	 */
	private HashMap<Integer, ArrayList<Integer>> mGameTileTemplates = null;

	/**
	 * Bitmap instances for each game tile type.
	 */
	private HashMap<Integer, Bitmap> mGameTileBitmaps = new HashMap<Integer, Bitmap>();

	/**
	 * GameTile instances for each game tile used by the current level.
	 */
	private List<GameTile> mGameTiles = new ArrayList<GameTile>();

	private int mPlayerStartTileX = 0;
	private int mPlayerStartTileY = 0;
	
	private int mTileWidth = 0;
	private int mTileHeight = 0;

	class GameThread extends Thread
	{
		public GameThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler)
		{
			mGameSurfaceHolder = surfaceHolder;
			mGameContext = context;

			Resources res = context.getResources();

			mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.canvas_bg_01);

			Display display = mGameActivity.getWindowManager().getDefaultDisplay();
			mScreenXMax = display.getWidth();
			mScreenYMax = display.getHeight();
			mScreenXCenter = (mScreenXMax / 2);
			mScreenYCenter = (mScreenYMax / 2);

			setGameStartState();
		}

		/**
		 * Callback invoked when the surface dimensions change.
		 */
		public void setSurfaceSize(int width, int height)
		{
			// synchronized to make sure these all change atomically
			synchronized (mGameSurfaceHolder)
			{
				mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage,
						width, height, true);
			}
		}

		/**
		 * Sets the run status of the game loop inside the game thread.
		 * @param boolean run - true when game should run, false otherwise.
		 */
		public void setRunning(boolean run)
		{
			mGameRun = run;
		}

		/**
		 * Sets the game state to running.
		 */
		public void doStart()
		{
			setState(STATE_RUNNING);
		}

		/**
		 * Sets the game state
		 * @param int mode - May be STATE_RUNNING or STATE_PAUSED
		 */
		public void setState(int state)
		{
			mGameState = state;
		}
		
		/**
		 * Contains the main game loop, which updates all elements of the game.
		 */
		@Override
		public void run()
		{
			while (mGameRun)
			{
				Canvas c = null;
				try
				{
					c = mGameSurfaceHolder.lockCanvas(null);
					synchronized (mGameSurfaceHolder)
					{
						if (mGameState == STATE_RUNNING)
						{
							updatePlayerUnit();
						}

						doDraw(c);
					}
				} finally
				{
					if (c != null)
					{
						mGameSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}

			return;
		}

		/**
		 * Pauses the game.
		 */
		public void pause()
		{
			synchronized (mGameSurfaceHolder)
			{
				if (mGameState == STATE_RUNNING)
				{
					setState(STATE_PAUSED);
				}
			}
		}

		/**
		 * Unpauses the game.
		 */
		public void unpause()
		{
			synchronized (mGameSurfaceHolder)
			{
				if (mGameState != STATE_RUNNING)
				{
					setState(STATE_RUNNING);
				}
			}
		}

		/**
		 * Centers the game view around the location of the player unit.
		 */
		private void centerView()
		{
			mPlayerUnit.setUnmodifiedX(mPlayerUnit.getX() + mScreenXCenter);
			mPlayerUnit.setUnmodifiedY(mPlayerUnit.getY() + mScreenYCenter);

			mScreenXOffset = (mPlayerUnit.getX() - mScreenXCenter);
			mScreenYOffset = (mPlayerUnit.getY() - mScreenYCenter);

			mPlayerUnit.setX(mScreenXCenter);
			mPlayerUnit.setY(mScreenYCenter);
		}

		/**
		 * Draws all visual elements of the game.
		 * @param Canvas canvas
		 */
		private void doDraw(Canvas canvas)
		{
			centerView();

			if (canvas != null)
			{
				canvas.drawBitmap(mBackgroundImage, 0, 0, null);

				if (!updatingGameTiles)
				{
					drawGameTiles(canvas);
				}

				if (mPlayerUnit != null)
				{
					canvas.drawBitmap(mPlayerUnit.getBitmap(), mPlayerUnit.getX(),
						mPlayerUnit.getY(), null);
				}

				drawControls(canvas);

				canvas.drawText(mLastStatusMessage, 30, 50, mUiTextPaint);
			}
		}

		/**
		 * Draws the game tiles used in the current level.
		 * @param Canvas canvas
		 */
		private void drawGameTiles(Canvas canvas)
		{
			int gameTilesSize = mGameTiles.size();
			for (int i = 0; i < gameTilesSize; i++)
			{
				if (mGameTiles.get(i) != null)
				{
					mGameTiles.get(i).setX(
							mGameTiles.get(i).getX() - mScreenXOffset);
					mGameTiles.get(i).setY(
							mGameTiles.get(i).getY() - mScreenYOffset);

					if (mGameTiles.get(i).isVisible())
					{
						canvas.drawBitmap(mGameTiles.get(i).getBitmap(),
								mGameTiles.get(i).getX(), mGameTiles.get(i)
										.getY(), null);
					}
				}
			}
		}

		/**
		 * Draws the game controls.
		 * @param Canvas canvas
		 */
		private void drawControls(Canvas canvas)
		{
			canvas.drawBitmap(mCtrlUpArrow.getBitmap(), mCtrlUpArrow.getX(), mCtrlUpArrow.getY(), null);
			canvas.drawBitmap(mCtrlDownArrow.getBitmap(), mCtrlDownArrow.getX(), mCtrlDownArrow.getY(), null);
			canvas.drawBitmap(mCtrlLeftArrow.getBitmap(), mCtrlLeftArrow.getX(), mCtrlLeftArrow.getY(), null);
			canvas.drawBitmap(mCtrlRightArrow.getBitmap(), mCtrlRightArrow.getX(), mCtrlRightArrow.getY(), null);
		}

		/**
		 * Updates the direction, position and state of the player unit.
		 */
		private void updatePlayerUnit()
		{
			GameTile collisionTile = null;

			if (mPlayerMoving)
			{
				int differenceX = 0;
				int differenceY = 0;
				int newX = mPlayerUnit.getX();
				int newY = mPlayerUnit.getY();

				if (mPlayerHorizontalDirection != 0)
				{
					differenceX = (mPlayerHorizontalDirection == DIRECTION_RIGHT) ? getPixelValueForDensity(PlayerUnit.SPEED) : getPixelValueForDensity(-PlayerUnit.SPEED);
					newX = (mPlayerUnit.getX() + differenceX);
				}

				if (mPlayerVerticalDirection != 0)
				{
					differenceY = (mPlayerVerticalDirection == DIRECTION_DOWN) ? getPixelValueForDensity(PlayerUnit.SPEED) : getPixelValueForDensity(-PlayerUnit.SPEED);
					newY = (mPlayerUnit.getY() + differenceY);
				}

				collisionTile = getCollisionTile(newX, newY, mPlayerUnit.getWidth(), mPlayerUnit .getHeight());

				if ((collisionTile != null)
						&& collisionTile.isBlockerTile())
				{
					handleTileCollision(collisionTile);
				} else
				{
					mPlayerUnit.setX(newX);
					mPlayerUnit.setY(newY);
				}
			}
		}

		/**
		 * Detects a collision between a game unit and a game tile,
		 * returns the collision tile if available.
		 * 
		 * @param x - The X (horizontal) position of the game unit. 
		 * @param y - The Y (vertical) position of the game unit.
		 * @param width - The width of the game unit.
		 * @param height - The height of the game unit.
		 * @return GameTile - The collision game tile, if available.
		 */
		private GameTile getCollisionTile(int x, int y, int width, int height)
		{
			GameTile gameTile = null;

			int gameTilesSize = mGameTiles.size();
			for (int i = 0; i < gameTilesSize; i++)
			{
				gameTile = (GameTile) mGameTiles.get(i);
				if ((gameTile != null) && gameTile.isCollisionTile())
				{
					// Make sure tiles don't collide with themselves
					if ((gameTile.getX() == x) && (gameTile.getY() == y))
					{
						continue;
					}

					if (gameTile.getCollision(x, y, width, height))
					{
						return gameTile;
					}
				}
			}
			return null;
		}

		/**
		 * Handles a collision between the player unit and a game tile.
		 * @param GameTile gameTile - The collision game tile.
		 */
		private void handleTileCollision(GameTile gameTile)
		{
			if (gameTile != null)
			{
				switch (gameTile.getType())
				{
				case GameTile.TYPE_DANGEROUS:
					handleDangerousTileCollision();
					break;
				case GameTile.TYPE_EXIT:
					handleExitTileCollision();
					break;
				default:
					mLastStatusMessage = "Collision with regular tile";
				}
			}
		}

		/**
		 * Handles a collision between the player unit and a dangerous
		 * game tile.
		 */
		private void handleDangerousTileCollision()
		{
			mLastStatusMessage = "Collision with dangerous tile";
		}

		/**
		 * Handles a collision between the player unit and an exit
		 * game tile.
		 */
		private void handleExitTileCollision()
		{
			mLastStatusMessage = "Collision with exit tile";
		}
	}

	private GameThread thread;

	/**
	 * The game view.
	 * @param Context context
	 * @param Activity activity
	 * @param int stage - The stage to load.
	 * @param int level - The level to load.
	 * @param float screenDensity - The screen density.
	 */
	public GameView(Context context, Play activity, int stage, int level, float screenDensity)
	{
		super(context);

		mGameContext = context;
		mGameActivity = activity;
		
		mScreenDensity = screenDensity;

		mPlayerStage = stage;
		mPlayerLevel = level;

		mGameTileData = new GameTileData(context);
		mGameLevelTileData = new GameLevelTileData(context);

		mGameTileTemplates = mGameTileData.getTilesData();

		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new GameThread(holder, context, null);

		setFocusable(true);

		mUiTextPaint = new Paint();
		mUiTextPaint.setStyle(Paint.Style.FILL);
		mUiTextPaint.setColor(Color.YELLOW);
		mUiTextPaint.setAntiAlias(true);
		
		Typeface uiTypeface = Typeface.createFromAsset(activity.getAssets(), "fonts/Molot.otf");
		if (uiTypeface != null)
		{
			mUiTextPaint.setTypeface(uiTypeface);
		}
		mUiTextPaint.setTextSize(mGameContext.getApplicationContext().getResources().getDimensionPixelSize(R.dimen.ui_text_size));

		startLevel();
		thread.doStart();
	}

	/**
	 * Gets the game thread.
	 * @return GameThread
	 */
	public GameThread getThread()
	{
		return thread;
	}

	/**
	 * Callback invoked when the surface dimensions change.
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		thread.setSurfaceSize(width, height);
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder)
	{
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created

		if (thread.getState() == Thread.State.TERMINATED)
		{
			thread = new GameThread(holder, getContext(), new Handler());
			thread.setRunning(true);
			thread.start();
			thread.doStart();
			startLevel();
		}
		else
		{
			thread.setRunning(true);
			thread.start();
		}
	}

	/*
	 * Callback invoked when the Surface has been destroyed.
	 */
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		boolean retry = true;
		thread.setRunning(false);
		while (retry)
		{
			try
			{
				thread.join();
				retry = false;
			} catch (InterruptedException e)
			{
				Log.e("Tile Game Example", e.getMessage());
			}
		}
	}

	/**
	 * Detects and handles touch events from the user.
	 * @param MotionEvent event
	 * @return boolean
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int eventAction = event.getAction();

		switch (eventAction)
		{
		case MotionEvent.ACTION_DOWN:

			if (mGameState == STATE_RUNNING)
			{
				final int x = (int) event.getX();
				final int y = (int) event.getY();
				
				if (mCtrlUpArrow.getImpact(x, y))
				{
					Log.d("Tile Game Example", "Pressed up arrow");
					mLastStatusMessage = "Moving up";
					mPlayerVerticalDirection = DIRECTION_UP;
					mPlayerMoving = true;
				}
				else if (mCtrlDownArrow.getImpact(x, y))
				{
					Log.d("Tile Game Example", "Pressed down arrow");
					mLastStatusMessage = "Moving down";
					mPlayerVerticalDirection = DIRECTION_DOWN;
					mPlayerMoving = true;
				}
				else if (mCtrlLeftArrow.getImpact(x, y))
				{
					Log.d("Tile Game Example", "Pressed left arrow");
					mLastStatusMessage = "Moving left";
					mPlayerHorizontalDirection = DIRECTION_LEFT;
					mPlayerMoving = true;
				}
				else if (mCtrlRightArrow.getImpact(x, y))
				{
					Log.d("Tile Game Example", "Pressed right arrow");
					mLastStatusMessage = "Moving right";
					mPlayerHorizontalDirection = DIRECTION_RIGHT;
					mPlayerMoving = true;
				}
			}

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mPlayerMoving = false;
			mPlayerVerticalDirection = 0;
			mPlayerHorizontalDirection = 0;
			break;
		}

		return true;
	}

	/**
	 * Initializes and sets the on-screen position of the game controls. 
	 */
	private void setControlsStart()
	{
		if (mCtrlDownArrow == null)
		{
			mCtrlDownArrow = new GameUi(mGameContext, R.drawable.ctrl_down_arrow);
			
			mCtrlDownArrow.setX(mScreenXMax - ((mCtrlDownArrow.getWidth() * 2) + getPixelValueForDensity(CONTROLS_PADDING)));
			mCtrlDownArrow.setY(mScreenYMax - (mCtrlDownArrow.getHeight() + getPixelValueForDensity(CONTROLS_PADDING)));
		}

		if (mCtrlUpArrow == null)
		{
			mCtrlUpArrow = new GameUi(mGameContext, R.drawable.ctrl_up_arrow);
			
			mCtrlUpArrow.setX(mCtrlDownArrow.getX());
			mCtrlUpArrow.setY(mCtrlDownArrow.getY() - (mCtrlUpArrow.getHeight() * 2));
		}

		if (mCtrlLeftArrow == null)
		{
			mCtrlLeftArrow = new GameUi(mGameContext, R.drawable.ctrl_left_arrow);
			mCtrlLeftArrow.setX(mCtrlDownArrow.getX() - mCtrlLeftArrow.getWidth());
			mCtrlLeftArrow.setY(mCtrlDownArrow.getY() - mCtrlLeftArrow.getHeight());
		}

		if (mCtrlRightArrow == null)
		{
			mCtrlRightArrow = new GameUi(mGameContext, R.drawable.ctrl_right_arrow);
			
			mCtrlRightArrow.setX(mScreenXMax - (mCtrlLeftArrow.getWidth() + getPixelValueForDensity(CONTROLS_PADDING)));
			mCtrlRightArrow.setY(mCtrlLeftArrow.getY());
		}
	}
	
	/**
	 * Initializes and sets the starting location of the player unit.
	 */
	private void setPlayerStart()
	{
		if (mPlayerUnit == null)
		{
			mPlayerUnit = new PlayerUnit(mGameContext, R.drawable.player_unit);
		}

		int playerStartX = (mPlayerStartTileX * mPlayerUnit.getWidth());
		int playerStartY = (mPlayerStartTileY * mPlayerUnit.getHeight());

		Log.d("Tile Game Example", "Player unit starting at X: " + playerStartX + ", Y: " + playerStartY);

		mPlayerUnit.setX(playerStartX);
		mPlayerUnit.setY(playerStartY);
		mPlayerUnit.setUnmodifiedX(0);
		mPlayerUnit.setUnmodifiedY(0);
	}
	
	/**
	 * Parses game level data to create a tile-based level.
	 * Tile positioning logic expects all game tiles to
	 * maintain a consistent width and height.
	 */
	private void parseGameLevelData()
	{
		updatingGameTiles = true;

		ArrayList<String> gameLevelData = mGameLevelTileData.getGameLevelData(mPlayerStage, mPlayerLevel);

		String levelTileData = gameLevelData.get(GameLevelTileData.FIELD_ID_TILE_DATA);

		if (levelTileData == null)
		{
			return;
		}

		// Get player start position.
		mPlayerStartTileX = Integer.parseInt(gameLevelData.get(GameLevelTileData.FIELD_ID_PLAYER_START_TILE_X));
		mPlayerStartTileY = Integer.parseInt(gameLevelData.get(GameLevelTileData.FIELD_ID_PLAYER_START_TILE_Y));

		// Clear any existing loaded game tiles.
		mGameTiles.clear();

		// Split level tile data by line.
		String[] tileLines = levelTileData.split(GameLevelTileData.TILE_DATA_LINE_BREAK);

		Bitmap bitmap = null;
		Point tilePoint = new Point(0, 0);
		int tileX = 0;
		int tileY = 0;

		int tileKey = 0;

		// Loop through each line of the level tile data.
		for (String tileLine : tileLines)
		{
			tileX = 0;

			// Split tile data line by tile delimiter, producing an array of tile IDs.
			String[] tiles = tileLine.split(",");

			// Loop through the tile IDs, creating a new GameTile instance for each one.
			for (String tile : tiles)
			{
				// Get tile definition for the current tile ID.
				ArrayList<Integer> tileData = mGameTileTemplates.get(Integer.parseInt(tile));

				// Check for valid tile data.
				if ((tileData != null)
						&& (tileData.size() > 0)
						&& (tileData.get(GameTileData.FIELD_ID_DRAWABLE) > 0))
				{
					// Set tile position.
					tilePoint.x = tileX;
					tilePoint.y = tileY;
					
					GameTile gameTile = new GameTile(mGameContext, tilePoint);

					// Set tile bitmap.
					bitmap = setAndGetGameTileBitmap(tileData.get(GameTileData.FIELD_ID_DRAWABLE));
					gameTile.setBitmap(bitmap);

					// Set tile type.
					gameTile.setType(tileData.get(GameTileData.FIELD_ID_TYPE));

					// Set tile visibility.
					if (tileData.get(GameTileData.FIELD_ID_VISIBLE) == 0)
					{
						gameTile.setVisible(false);
					}

					gameTile.setKey(tileKey);

					// If undefined, set global tile width / height values.
					if (mTileWidth == 0)
					{
						mTileWidth = gameTile.getWidth();
					}
					if (mTileHeight == 0)
					{
						mTileHeight = gameTile.getHeight();
					}
					
					// Add new game tile to loaded game tiles.
					mGameTiles.add(gameTile);

					tileKey++;
				}

				// Increment next tile X (horizontal) position by tile width.
				tileX += mTileWidth;
			}

			// Increment next tile Y (vertical) position by tile width.
			tileY += mTileHeight;
		}

		updatingGameTiles = false;
	}
	
	/**
	 * Sets the state for a new game.
	 */
	private void setGameStartState()
	{
		setControlsStart();
		setPlayerStart();
	}
	
	/**
	 * Loads and starts the current level.
	 */
	private void startLevel()
	{
		parseGameLevelData();
		setPlayerStart();

		thread.unpause();
	}

	/**
	 * Stores a bitmap for use by a game tile in a level.
	 * @param int resourceId - The bitmap resource ID.
	 * @return Bitmap - The Bitmap instance for the given resource ID.
	 */
	private Bitmap setAndGetGameTileBitmap(int resourceId)
	{
		if (!mGameTileBitmaps.containsKey(resourceId))
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeResource(mGameContext
					.getResources(), resourceId);

			if (bitmap != null)
			{
				mGameTileBitmaps.put(resourceId, bitmap);
			}
		}

		return mGameTileBitmaps.get(resourceId);
	}
	
	private int getPixelValueForDensity(int pixels)
	{
		return (int) (pixels * mScreenDensity);
	}
}
