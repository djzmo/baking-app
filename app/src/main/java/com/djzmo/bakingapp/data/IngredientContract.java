package com.djzmo.bakingapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class IngredientContract {

    public static final class IngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_RECIPE_ID = "recipeId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_TIMESTAMP = "created_at";
    }

}
