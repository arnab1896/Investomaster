package com.aastudio.investomater.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Abhidnya on 1/7/2017.
 */

public class NSEDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NSEDatabase";
    private static final String TABLE_COMPANIES = "companies";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATABASE_CODE = "database_code";
    private static final String KEY_DATASET_CODE = "dataset_code";

    public NSEDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COMPANIES_TABLE = "CREATE TABLE " + TABLE_COMPANIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_DATABASE_CODE + " TEXT," + KEY_DATASET_CODE + " TEXT)";
        db.execSQL(CREATE_COMPANIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANIES);
        // Create tables again
        onCreate(db);
    }

    public void addNSEEntry(NSEEntry nseEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, nseEntry.getCompanyName());
        values.put(KEY_DATABASE_CODE, nseEntry.getDataBaseCode());
        values.put(KEY_DATASET_CODE, nseEntry.getDataSetCode());

        // Inserting Row
        db.insert(TABLE_COMPANIES, null, values);
        db.close();
    }

    public NSEEntry getNSEEntry(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_COMPANIES, new String[]{KEY_ID,
                        KEY_NAME}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        NSEEntry nseEntry = new NSEEntry(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3));
        // return contact
        return nseEntry;
    }

    public ArrayList<NSEEntry> getAllEntries() {
        ArrayList<NSEEntry> nseEntries = new ArrayList<NSEEntry>();

        String selectQuery = "SELECT  * FROM " + TABLE_COMPANIES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                NSEEntry nseEntry = new NSEEntry();
                nseEntry.setId(Integer.parseInt(cursor.getString(0)));
                nseEntry.setCompanyName(cursor.getString(1));
                nseEntry.setDataBaseCode(cursor.getString(2));
                nseEntry.setDataSetCode(cursor.getString(3));
                nseEntries.add(nseEntry);
            } while (cursor.moveToNext());
        }

        return nseEntries;
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_COMPANIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateNSEEntry(NSEEntry nseEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, nseEntry.getCompanyName());
        values.put(KEY_DATABASE_CODE, nseEntry.getDataBaseCode());
        values.put(KEY_DATASET_CODE, nseEntry.getDataSetCode());

        // updating row
        return db.update(TABLE_COMPANIES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(nseEntry.getId())});
    }
}
