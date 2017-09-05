package com.djzmo.bakingapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class StepContract {

    public static final class StepEntry implements BaseColumns {
        public static final String TABLE_NAME = "steps";
        public static final String COLUMN_RECIPE_ID = "recipeId";
        public static final String COLUMN_REMOTE_ID = "remoteId";
        public static final String COLUMN_SHORT_DESCRIPTION = "shortDescription";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VIDEO_URL = "videoUrl";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_TIMESTAMP = "created_at";
    }

}
