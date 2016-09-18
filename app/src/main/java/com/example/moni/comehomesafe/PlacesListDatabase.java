package com.example.moni.comehomesafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class PlacesListDatabase {

    private static final String DATABASE_NAME = "placeslist.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "placeslistitems";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PLACE = "place";

    public static final int COLUMN_NAME_INDEX = 1;
    public static final int COLUMN_PlACE_INDEX = 2;

    private PlacesDBOpenHelper dbHelper;

    private SQLiteDatabase db;

    public PlacesListDatabase(Context context) {
        dbHelper = new PlacesDBOpenHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
    }

    public void open() throws SQLException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public long insertPlacesItem(PlacesItem item) {
        ContentValues newPlacesValues = new ContentValues();

        newPlacesValues.put(KEY_NAME, item.getName());
        newPlacesValues.put(KEY_PLACE, item.getPlace());

        return db.insert(DATABASE_TABLE, null, newPlacesValues);
    }

    public void removePlacesItem(PlacesItem item) {
        String whereClause = KEY_NAME + " = '" + item.getName() + "' AND " + KEY_PLACE + " = '" + item.getPlace() + "'";

        db.delete(DATABASE_TABLE, whereClause, null);
    }

    public ArrayList<PlacesItem> getAllPlacesItems() {
        ArrayList<PlacesItem> items = new ArrayList<>();
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                KEY_NAME, KEY_PLACE}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(COLUMN_NAME_INDEX);
                String place = cursor.getString(COLUMN_PlACE_INDEX);

                items.add(new PlacesItem(name, place));

            } while (cursor.moveToNext());
        }
        return items;
    }

    private class PlacesDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table "
                + DATABASE_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_NAME
                + " text not null, " + KEY_PLACE + " text);";

        public PlacesDBOpenHelper(Context c, String dbname, SQLiteDatabase.CursorFactory factory, int version) {
            super(c, dbname, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
