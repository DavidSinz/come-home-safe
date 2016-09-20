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
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_TABLE = "placeslistitems";

    public static final String KEY_ID = "_id";
    public static final String KEY_PLACE = "place";
    public static final String KEY_STREET = "street";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_ZIP_CODE = "zipcode";
    public static final String KEY_CITY = "city";

    public static final int COLUMN_PLACE_INDEX = 1;
    public static final int COLUMN_STREET_INDEX = 2;
    public static final int COLUMN_NUMBER_INDEX = 3;
    public static final int COLUMN_ZIP_CODE_INDEX = 4;
    public static final int COLUMN_CITY_INDEX = 5;

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

    public long insertPlaceItem(PlacesItem item) {
        ContentValues newPlaceValues = new ContentValues();

        newPlaceValues.put(KEY_STREET, item.getStreet());
        newPlaceValues.put(KEY_NUMBER, item.getNumber());
        newPlaceValues.put(KEY_ZIP_CODE, item.getZipCode());
        newPlaceValues.put(KEY_NUMBER, item.getNumber());
        newPlaceValues.put(KEY_CITY, item.getCity());

        return db.insert(DATABASE_TABLE, null, newPlaceValues);
    }

    public void removePlacesItem(PlacesItem item) {
        String whereClause = KEY_PLACE + " = '" + item.getPlace() + "' AND "
                + KEY_STREET + " = '" + item.getStreet() + "' AND "
                + KEY_NUMBER + " = '" + item.getNumber() + "'";

        db.delete(DATABASE_TABLE, whereClause, null);
    }

    public ArrayList<PlacesItem> getAllPlacesItems() {
        ArrayList<PlacesItem> items = new ArrayList<>();
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_PLACE, KEY_STREET, KEY_NUMBER, KEY_ZIP_CODE, KEY_CITY}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String place = cursor.getString(COLUMN_PLACE_INDEX);
                String street = cursor.getString(COLUMN_STREET_INDEX);
                String number = cursor.getString(COLUMN_NUMBER_INDEX);
                String zipCode = cursor.getString(COLUMN_ZIP_CODE_INDEX);
                String city = cursor.getString(COLUMN_CITY_INDEX);

                items.add(new PlacesItem(place, street, number, zipCode, city));

            } while (cursor.moveToNext());
        }
        return items;
    }

    private class PlacesDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " ("
                + KEY_ID + " integer primary key autoincrement, "
                + KEY_PLACE + " text not null, "
                + KEY_STREET + " text, "
                + KEY_NUMBER + " text, "
                + KEY_ZIP_CODE + " text, "
                + KEY_CITY + " text);";

        public PlacesDBOpenHelper(Context c, String dbname, SQLiteDatabase.CursorFactory factory, int version) {
            super(c, dbname, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1: db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN INVENTORY TEXT");
            }
        }
    }
}

