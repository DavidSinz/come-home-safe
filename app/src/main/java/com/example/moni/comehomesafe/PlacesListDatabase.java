package com.example.moni.comehomesafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class PlacesListDatabase {
    private static final String DATABASE_NAME = "placelistdatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "placelistitems";

    public static final String KEY_ID = "_id";
    public static final String KEY_ADRESS = "adress";

    public static final int COLUMN_ADRESS_INDEX = 1;

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

        newPlacesValues.put(KEY_ADRESS, item.getAdress());

        return db.insert(DATABASE_TABLE, null, newPlacesValues);
    }

    public void removePlacesItem(PlacesItem item) {
        String whereClause = KEY_ADRESS + " = '" + item.getAdress() + "'";

        db.delete(DATABASE_TABLE, whereClause, null);
    }

    public ArrayList<PlacesItem> getAllPlacesItems() {
        ArrayList<PlacesItem> items = new ArrayList<>();
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                KEY_ADRESS}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String adress = cursor.getString(COLUMN_ADRESS_INDEX);

                items.add(new PlacesItem(adress));

            } while (cursor.moveToNext());
        }
        return items;
    }

    private class PlacesDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table "
                + DATABASE_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_ADRESS
                + " text not null);";

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

