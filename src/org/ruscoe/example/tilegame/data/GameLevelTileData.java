package org.ruscoe.example.tilegame.data;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * The GameTileData class represents a definition of a game
 * level stored in the database.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class GameLevelTileData extends GameDAO
{
	public static final String TABLE_NAME = "gameLevelTileData";

	public static final String STAGE = "stage";
	public static final String LEVEL = "level";
	public static final String PLAYER_START_TILE_X = "playerStartTileX";
	public static final String PLAYER_START_TILE_Y = "playerStartTileY";
	public static final String TILE_DATA = "tileData";

	public static final int FIELD_ID_ID = 0;
	public static final int FIELD_ID_STAGE = 1;
	public static final int FIELD_ID_LEVEL = 2;
	public static final int FIELD_ID_PLAYER_START_TILE_X = 3;
	public static final int FIELD_ID_PLAYER_START_TILE_Y = 4;
	public static final int FIELD_ID_TILE_DATA = 5;

	public static final String TILE_DATA_LINE_BREAK = "//";

	public GameLevelTileData(Context ctx)
	{
		super(ctx);
	}

	/**
	 * Gets an array of game level data for a given stage and level.
	 * @param int stage - The game stage.
	 * @param level - The game level, relative to the stage.
	 * @return ArrayList
	 */
	public ArrayList<String> getGameLevelData(int stage, int level)
	{
    	SQLiteDatabase db = this.getReadableDatabase();
    	
    	String[] from = { _ID, STAGE, LEVEL, PLAYER_START_TILE_X, PLAYER_START_TILE_Y, TILE_DATA };
    	String where = STAGE + " = " + stage + " AND " + LEVEL + " = " + level;
    	
    	Cursor cursor = db.query(TABLE_NAME, from, where, null, null, null, null);
    	
    	ArrayList<String> levelData = new ArrayList<String>();
    	
    	if (cursor != null)
    	{
    		while (cursor.moveToNext())
        	{
    			levelData.add(cursor.getString(FIELD_ID_ID));
    			levelData.add(cursor.getString(FIELD_ID_STAGE));
    			levelData.add(cursor.getString(FIELD_ID_LEVEL));
    			levelData.add(cursor.getString(FIELD_ID_PLAYER_START_TILE_X));
    			levelData.add(cursor.getString(FIELD_ID_PLAYER_START_TILE_Y));
    			levelData.add(cursor.getString(FIELD_ID_TILE_DATA));
        	}
    		cursor.close();
    	}
    	
    	db.close();
    	return levelData;
	}
}
