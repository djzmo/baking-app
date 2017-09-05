package com.djzmo.bakingapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StepDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "steps.db";
    private static final int DATABASE_VERSION = 1;

    public StepDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " + StepContract.StepEntry.TABLE_NAME + " (" +
                StepContract.StepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StepContract.StepEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                StepContract.StepEntry.COLUMN_REMOTE_ID + " INTEGER NOT NULL, " +
                StepContract.StepEntry.COLUMN_SHORT_DESCRIPTION + " VARCHAR(256) NOT NULL, " +
                StepContract.StepEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                StepContract.StepEntry.COLUMN_VIDEO_URL + " TEXT NULL, " +
                StepContract.StepEntry.COLUMN_THUMBNAIL_URL + " TEXT NULL, " +
                StepContract.StepEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StepContract.StepEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
