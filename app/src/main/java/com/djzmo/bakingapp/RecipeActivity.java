package com.djzmo.bakingapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.djzmo.bakingapp.data.IngredientContract;
import com.djzmo.bakingapp.data.RecipeContract;
import com.djzmo.bakingapp.data.StepContract;
import com.djzmo.bakingapp.databinding.ActivityRecipesBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecipeActivity extends AppCompatActivity implements RecipeAdapter.RecipeAdapterOnClickHandler {

    public static final String RECIPES_KEY = "recipes";
    public static final String LIST_STATE_KEY = "list_state";
    public static final String LIST_POS_KEY = "list_pos";

    private ActivityRecipesBinding mBinding;
    private RecyclerView mRecyclerView;
    private RecipeAdapter mAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;
    private Button mErrorTryAgainButton;
    private int mCurrentPosition;
    private Parcelable mLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipes);

        mCurrentPosition = 0;

        GridLayoutManager layoutManager = new GridLayoutManager(this, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 3);

        mAdapter = new RecipeAdapter(this, this);

        mRecyclerView = mBinding.rvRecipes;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mLoadingIndicator = mBinding.pbRecipes;
        mErrorTextView = mBinding.tvRecipeError;
        mErrorTryAgainButton = mBinding.btnTryAgain;

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });

        if(savedInstanceState == null)
            loadData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(RECIPES_KEY, mAdapter.getData());
        mLayoutState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mLayoutState);
        outState.putInt(LIST_POS_KEY, ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        RecipeInformation[] data = (RecipeInformation[]) savedInstanceState.getParcelableArray(RECIPES_KEY);
        mAdapter.setData(data);
        mLayoutState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        mCurrentPosition = savedInstanceState.getInt(LIST_POS_KEY);
        showDataView();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mLayoutState != null)
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutState);

        if(mCurrentPosition > 0)
            mRecyclerView.scrollToPosition(mCurrentPosition);
    }

    private void loadData() {
        showLoadingIndicator();
        new FetchDataTask().execute();
    }

    private void showDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void showErrorMessage() {
        mErrorTextView.setText(R.string.error_message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(String message, boolean withButton) {
        mErrorTextView.setText(message);

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTryAgainButton.setVisibility(withButton ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mErrorTryAgainButton.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(RecipeInformation information) {
        Intent intent = new Intent(this, StepListActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, information.remoteId);
        startActivity(intent);
    }

    public class FetchDataTask extends AsyncTask<String, Void, RecipeInformation[]> {

        boolean mNoConnection;

        @Override
        protected RecipeInformation[] doInBackground(String... strings) {
            RecipeInformation[] data = null;

            Cursor cursor = getContentResolver().query(RecipeContract.RecipeEntry.RECIPES_CONTENT_URI, null, null, null, RecipeContract.RecipeEntry.COLUMN_TIMESTAMP);
            int count = cursor != null ? cursor.getCount() : 0;

            // Loading from network
            if(count == 0) {
                if(!NetworkUtils.isOnline(RecipeActivity.this))
                {
                    mNoConnection = true;
                    return null;
                }

                URL url = NetworkUtils.buildCommonUrl(RecipeActivity.this, NetworkUtils.DATA_SOURCE_URL);

                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    String jsonString = CommonUtils.readInputStream(urlConnection.getInputStream());
                    JSONArray results = new JSONArray(jsonString);
                    data = new RecipeInformation[results.length()];
                    ContentResolver contentResolver = getContentResolver();

                    for(int i = 0; i < results.length(); ++i) {
                        JSONObject item = results.getJSONObject(i);

                        RecipeInformation info = new RecipeInformation();
                        info.remoteId = Integer.toString(item.getInt("id"));
                        info.name = item.getString("name");
                        info.servings = item.getInt("servings");
                        info.image = item.getString("image");
                        data[i] = info;

                        ContentValues values = new ContentValues();
                        values.put(RecipeContract.RecipeEntry.COLUMN_REMOTE_ID, info.remoteId);
                        values.put(RecipeContract.RecipeEntry.COLUMN_NAME, info.name);
                        values.put(RecipeContract.RecipeEntry.COLUMN_SERVINGS, info.servings);
                        values.put(RecipeContract.RecipeEntry.COLUMN_IMAGE, info.image);

                        Uri recipeUri = contentResolver.insert(RecipeContract.RecipeEntry.RECIPES_CONTENT_URI, values);
                        long recipeId = Long.parseLong(recipeUri.getLastPathSegment());

                        JSONArray ingredients = item.getJSONArray("ingredients");
                        for(int j = 0; j < ingredients.length(); ++j) {
                            JSONObject ingredient = ingredients.getJSONObject(j);
                            values.clear();
                            values.put(IngredientContract.IngredientEntry.COLUMN_RECIPE_ID, recipeId);
                            values.put(IngredientContract.IngredientEntry.COLUMN_NAME, ingredient.getString("ingredient"));
                            values.put(IngredientContract.IngredientEntry.COLUMN_QUANTITY, ingredient.getInt("quantity"));
                            values.put(IngredientContract.IngredientEntry.COLUMN_MEASURE, ingredient.getString("measure"));

                            contentResolver.insert(RecipeContract.RecipeEntry.INGREDIENTS_CONTENT_URI, values);
                        }

                        JSONArray steps = item.getJSONArray("steps");
                        for(int j = 0; j < steps.length(); ++j) {
                            JSONObject step = steps.getJSONObject(j);
                            values.clear();
                            values.put(StepContract.StepEntry.COLUMN_RECIPE_ID, recipeId);
                            values.put(StepContract.StepEntry.COLUMN_REMOTE_ID, step.getInt("id"));
                            values.put(StepContract.StepEntry.COLUMN_SHORT_DESCRIPTION, step.getString("shortDescription"));
                            values.put(StepContract.StepEntry.COLUMN_DESCRIPTION, step.getString("description"));
                            values.put(StepContract.StepEntry.COLUMN_VIDEO_URL, step.getString("videoURL"));
                            values.put(StepContract.StepEntry.COLUMN_THUMBNAIL_URL, step.getString("thumbnailURL"));

                            contentResolver.insert(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, values);
                        }
                    }
                }
                catch(IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            // Loading from DB
            else {
                data = new RecipeInformation[count];

                for(int i = 0; i < count; ++i) {
                    cursor.moveToPosition(i);

                    RecipeInformation info = new RecipeInformation();
                    info.remoteId = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_REMOTE_ID));
                    info.name = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_NAME));
                    info.servings = cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SERVINGS));
                    info.image = cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE));
                    data[i] = info;
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(RecipeInformation[] data) {
            if(data != null) {
                showDataView();
                mAdapter.setData(data);
            }
            else if(mNoConnection)
                showErrorMessage(getString(R.string.no_internet_connection), true);
            else showErrorMessage(getString(R.string.error_message));
        }
    }
}
