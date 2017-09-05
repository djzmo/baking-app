package com.djzmo.bakingapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.djzmo.bakingapp.data.IngredientContract;
import com.djzmo.bakingapp.data.RecipeContract;
import com.djzmo.bakingapp.data.StepContract;
import com.djzmo.bakingapp.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StepDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StepListActivity extends AppCompatActivity {

    private final static String SELECTED_STEP_KEY = "selected_step_remote_id";

    private boolean mTwoPane;
    private int mRecipeId;
    private String mSelectedStepRemoteId;
    private RecipeInformation mRecipe;
    private StepInformation[] mSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_list);

        mSelectedStepRemoteId = null;

        Intent intent = getIntent();
        String recipeRemoteId = intent.getStringExtra(Intent.EXTRA_TEXT);
        Cursor cursor = getContentResolver().query(RecipeContract.RecipeEntry.RECIPES_CONTENT_URI, null, RecipeContract.RecipeEntry.COLUMN_REMOTE_ID + " = ?", new String[] { recipeRemoteId }, null);

        if(cursor == null || cursor.getCount() == 0)
            finish();

        cursor.moveToFirst();
        mRecipeId = cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry._ID));
        mRecipe = new RecipeInformation(recipeRemoteId, cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_NAME)), cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SERVINGS)), cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE)));

        cursor = getContentResolver().query(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, null, StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[] { Integer.toString(mRecipeId) }, null);

        mSteps = null;
        if(cursor != null && cursor.getCount() > 0) {
            mSteps = new StepInformation[cursor.getCount()];
            for(int i = 0; i < cursor.getCount(); ++i) {
                cursor.moveToPosition(i);

                StepInformation step = new StepInformation();
                step.remoteId = cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_REMOTE_ID));
                step.shortDescription = cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_SHORT_DESCRIPTION));
                step.description = cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_DESCRIPTION));
                step.videoUrl = cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_VIDEO_URL));
                step.thumbnailUrl = cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_THUMBNAIL_URL));
                mSteps[i] = step;
            }
        }

        setTitle(mRecipe.name);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        View recyclerView = findViewById(R.id.step_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.step_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mTwoPane)
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.step_detail_container))
                    .commit();

        super.onSaveInstanceState(outState);

        if(mSelectedStepRemoteId != null)
            outState.putString(SELECTED_STEP_KEY, mSelectedStepRemoteId);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(mSelectedStepRemoteId == null)
            mSelectedStepRemoteId = savedInstanceState.getString(SELECTED_STEP_KEY);
        if(mTwoPane && mSelectedStepRemoteId != null) {
            Bundle arguments = new Bundle();
            arguments.putString(StepDetailFragment.ARG_STEP_REMOTE_ID, mSelectedStepRemoteId);
            arguments.putString(StepDetailFragment.ARG_RECIPE_ID, Integer.toString(mRecipeId));
            StepDetailFragment fragment = new StepDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == StepDetailFragment.INTENT_STEP_REMOTE_ID) {
            mSelectedStepRemoteId = data.getStringExtra(StepDetailFragment.ARG_STEP_REMOTE_ID);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new StepAdapter(mSteps));
    }

    public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

        private final StepInformation[] mValues;

        public StepAdapter(StepInformation[] items) {
            mValues = new StepInformation[items.length + 1];
            mValues[0] = null;
            for(int i = 1; i <= items.length; ++i)
                mValues[i] = items[i - 1];
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.step_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues[position];

            final String stepRemoteId = holder.mItem == null ? getString(R.string.ingredients) : holder.mItem.remoteId;

            if(holder.mItem != null) {
                holder.mIdView.setText(mValues[position].remoteId);
                holder.mContentView.setText(mValues[position].shortDescription);
            }
            else {
                holder.mIdView.setText("?");
                holder.mContentView.setText(getString(R.string.ingredients));
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedStepRemoteId = stepRemoteId;
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(StepDetailFragment.ARG_STEP_REMOTE_ID, stepRemoteId);
                        arguments.putString(StepDetailFragment.ARG_RECIPE_ID, Integer.toString(mRecipeId));
                        StepDetailFragment fragment = new StepDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.step_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, StepDetailActivity.class);
                        intent.putExtra(StepDetailFragment.ARG_STEP_REMOTE_ID, stepRemoteId);
                        intent.putExtra(StepDetailFragment.ARG_RECIPE_ID, Integer.toString(mRecipeId));

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public StepInformation mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
