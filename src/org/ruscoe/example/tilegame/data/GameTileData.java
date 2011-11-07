package org.ruscoe.example.tilegame.data;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * The GameTileData class represents a definition of a game
 * tile stored in the database.
 * 
 * @author Dan Ruscoe (ruscoe.org)
 * @version 1.0
 */
public class GameTileData extends GameDAO
{
	public static final String TABLE_NAME = "gameTileData";
	
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String DRAWABLE = "drawable";
	public static final String VISIBLE = "visible";
	
	public static final int FIELD_ID_ID = 0;
	public static final int FIELD_ID_NAME = 1;
	public static final int FIELD_ID_TYPE = 2;
	public static final int FIELD_ID_DRAWABLE = 3;
	public static final int FIELD_ID_VISIBLE = 4;

	public GameTileData(Context ctx)
	{
		super(ctx);
	}

	/**
	 * Gets a map containing definitions for all available game tiles.
	 * @return HashMap
	 */
	public HashMap<Integer, ArrayList<Integer>> getTilesData()
	{
    	SQLiteDatabase db = this.getReadableDatabase();

    	String[] from = { _ID, NAME, TYPE, DRAWABLE, VISIBLE };
    	Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, null);

    	HashMap<Integer, ArrayList<Integer>> tiles = new HashMap<Integer, ArrayList<Integer>>();

    	if (cursor != null)
    	{
    		while (cursor.moveToNext())
        	{
    			ArrayList<Integer> arrayList = new ArrayList<Integer>();
    			
    			arrayList.add(cursor.getInt(FIELD_ID_ID));
    			arrayList.add(cursor.getInt(FIELD_ID_NAME));
    			arrayList.add(cursor.getInt(FIELD_ID_TYPE));
    			arrayList.add(cursor.getInt(FIELD_ID_DRAWABLE));
    			arrayList.add(cursor.getInt(FIELD_ID_VISIBLE));

    			tiles.put(cursor.getInt(FIELD_ID_ID), arrayList);
        	}
    		cursor.close();
    	}

    	db.close();

    	return tiles;
	}
}
