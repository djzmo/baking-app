package com.djzmo.bakingapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class RecipeContentProvider extends ContentProvider {

    private RecipeDbHelper mRecipeDb;
    private IngredientDbHelper mIngredientDb;
    private StepDbHelper mStepDb;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static final int RECIPES = 100;
    public static final int RECIPE_WITH_ID = 101;
    public static final int INGREDIENTS = 200;
    public static final int INGREDIENTS_WITH_RECIPE_ID = 201;
    public static final int STEPS = 300;
    public static final int STEPS_WITH_RECIPE_ID = 301;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_RECIPES, RECIPES);
        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_RECIPES + "/#", RECIPE_WITH_ID);
        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_INGREDIENTS, INGREDIENTS);
        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_INGREDIENTS + "/#", INGREDIENTS_WITH_RECIPE_ID);
        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_STEPS, STEPS);
        uriMatcher.addURI(RecipeContract.AUTHORITY, RecipeContract.PATH_STEPS + "/#", STEPS_WITH_RECIPE_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mRecipeDb = new RecipeDbHelper(context);
        mIngredientDb = new IngredientDbHelper(context);
        mStepDb = new StepDbHelper(context);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase recipeDb = mRecipeDb.getReadableDatabase();
        final SQLiteDatabase ingredientDb = mIngredientDb.getReadableDatabase();
        final SQLiteDatabase stepDb = mStepDb.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor = null;
        String argId = null;

        switch(match) {
            case RECIPES:
                retCursor = recipeDb.query(RecipeContract.RecipeEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INGREDIENTS:
                retCursor = ingredientDb.query(IngredientContract.IngredientEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INGREDIENTS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);

                if(selection != null && selection.length() > 0)
                    selection += " AND ";
                else selection = IngredientContract.IngredientEntry.COLUMN_RECIPE_ID + " = ?";

                if(selectionArgs != null && selectionArgs.length > 0)
                {
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    newSelectionArgs[selectionArgs.length] = argId;
                    selectionArgs = newSelectionArgs;
                }
                else selectionArgs = new String[] { argId };

                retCursor = ingredientDb.query(IngredientContract.IngredientEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STEPS:
                retCursor = stepDb.query(StepContract.StepEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STEPS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);

                if(selection != null && selection.length() > 0)
                    selection += " AND ";
                else selection = StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?";

                if(selectionArgs != null && selectionArgs.length > 0)
                {
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    newSelectionArgs[selectionArgs.length] = argId;
                    selectionArgs = newSelectionArgs;
                }
                else selectionArgs = new String[] { argId };

                retCursor = stepDb.query(StepContract.StepEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase recipeDb = mRecipeDb.getWritableDatabase();
        final SQLiteDatabase ingredientDb = mIngredientDb.getReadableDatabase();
        final SQLiteDatabase stepDb = mStepDb.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        String argId = null;

        switch(match) {
            case RECIPES:
                long id = recipeDb.insert(RecipeContract.RecipeEntry.TABLE_NAME, null, contentValues);

                if(id > 0)
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.RECIPES_CONTENT_URI, id);
                else throw new SQLException("Unable to insert data into " + uri);

                break;
            case INGREDIENTS:
                id = ingredientDb.insert(IngredientContract.IngredientEntry.TABLE_NAME, null, contentValues);

                if(id > 0)
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.INGREDIENTS_CONTENT_URI, id);
                else throw new SQLException("Unable to insert data into " + uri);
                break;
            case INGREDIENTS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);
                contentValues.put(IngredientContract.IngredientEntry.COLUMN_RECIPE_ID, Integer.parseInt(argId));
                id = ingredientDb.insert(IngredientContract.IngredientEntry.TABLE_NAME, null, contentValues);

                if(id > 0)
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.INGREDIENTS_CONTENT_URI, id);
                else throw new SQLException("Unable to insert data into " + uri);
                break;
            case STEPS:
                id = stepDb.insert(StepContract.StepEntry.TABLE_NAME, null, contentValues);

                if(id > 0)
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, id);
                else throw new SQLException("Unable to insert data into " + uri);
                break;
            case STEPS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);
                contentValues.put(StepContract.StepEntry.COLUMN_RECIPE_ID, Integer.parseInt(argId));
                id = stepDb.insert(StepContract.StepEntry.TABLE_NAME, null, contentValues);

                if(id > 0)
                    returnUri = ContentUris.withAppendedId(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, id);
                else throw new SQLException("Unable to insert data into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        final SQLiteDatabase recipeDb = mRecipeDb.getWritableDatabase();
        final SQLiteDatabase ingredientDb = mIngredientDb.getWritableDatabase();
        final SQLiteDatabase stepDb = mStepDb.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deletedCount = 0;
        String argId = null;

        switch(match) {
            case RECIPE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                ingredientDb.delete(IngredientContract.IngredientEntry.TABLE_NAME, IngredientContract.IngredientEntry.COLUMN_RECIPE_ID + " = ?", new String[] {id});
                stepDb.delete(StepContract.StepEntry.TABLE_NAME, StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[] {id});
                deletedCount = recipeDb.delete(RecipeContract.RecipeEntry.TABLE_NAME, RecipeContract.RecipeEntry._ID + " = ?", new String[] {id});
                break;
            case INGREDIENTS:
                deletedCount = ingredientDb.delete(IngredientContract.IngredientEntry.TABLE_NAME, s, strings);
                break;
            case INGREDIENTS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);
                deletedCount = ingredientDb.delete(IngredientContract.IngredientEntry.TABLE_NAME, IngredientContract.IngredientEntry.COLUMN_RECIPE_ID + " = ?", new String[] { argId });
                break;
            case STEPS:
                deletedCount = stepDb.delete(StepContract.StepEntry.TABLE_NAME, s, strings);
                break;
            case STEPS_WITH_RECIPE_ID:
                argId = uri.getPathSegments().get(1);
                deletedCount = stepDb.delete(StepContract.StepEntry.TABLE_NAME, StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[] { argId });
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        if(deletedCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return deletedCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
