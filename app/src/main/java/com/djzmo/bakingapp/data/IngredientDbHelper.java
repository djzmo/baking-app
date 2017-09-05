package com.djzmo.bakingapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IngredientDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ingredients.db";
    private static final int DATABASE_VERSION = 1;

    public IngredientDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE " + IngredientContract.IngredientEntry.TABLE_NAME + " (" +
                IngredientContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IngredientContract.IngredientEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                IngredientContract.IngredientEntry.COLUMN_NAME + " VARCHAR(256) NOT NULL, " +
                IngredientContract.IngredientEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                IngredientContract.IngredientEntry.COLUMN_MEASURE + " VARCHAR(32) NOT NULL, " +
                IngredientContract.IngredientEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IngredientContract.IngredientEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
