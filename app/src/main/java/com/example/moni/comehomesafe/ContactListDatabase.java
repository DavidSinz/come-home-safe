package com.example.moni.comehomesafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ContactListDatabase {
    private static final String DATABASE_NAME = "contactlist.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "contactlistitems";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_NUMBER = "number";

    public static final int COLUMN_NAME_INDEX = 1;
    public static final int COLUMN_NUMBER_INDEX = 2;

    private ContactDBOpenHelper dbHelper;

    private SQLiteDatabase db;

    public ContactListDatabase(Context context) {
        dbHelper = new ContactDBOpenHelper(context, DATABASE_NAME, null,
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

    public long insertContactItem(ContactItem item) {
        ContentValues newContactValues = new ContentValues();

        newContactValues.put(KEY_NAME, item.getName());
        newContactValues.put(KEY_NUMBER, item.getNumber());

        return db.insert(DATABASE_TABLE, null, newContactValues);
    }

    public void removeContactItem(ContactItem item) {
        String whereClause = KEY_NAME + " = '" + item.getName() + "' AND " + KEY_NUMBER + " = '" + item.getNumber() + "'";

        db.delete(DATABASE_TABLE, whereClause, null);
    }

    public ArrayList<ContactItem> getAllContactItems() {
        ArrayList<ContactItem> items = new ArrayList<>();
        Cursor cursor = db.query(DATABASE_TABLE, new String[] { KEY_ID,
                KEY_NAME, KEY_NUMBER}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(COLUMN_NAME_INDEX);
                String number = cursor.getString(COLUMN_NUMBER_INDEX);

                items.add(new ContactItem(name, number));

            } while (cursor.moveToNext());
        }
        return items;
    }

    private class ContactDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table "
                + DATABASE_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_NAME
                + " text not null, " + KEY_NUMBER + " text);";

        public ContactDBOpenHelper(Context c, String dbname, SQLiteDatabase.CursorFactory factory, int version) {
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

