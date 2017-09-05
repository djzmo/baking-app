package com.djzmo.bakingapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djzmo.bakingapp.data.IngredientContract;
import com.djzmo.bakingapp.data.RecipeContract;
import com.djzmo.bakingapp.data.StepContract;
import com.djzmo.bakingapp.databinding.IngredientCardItemBinding;
import com.djzmo.bakingapp.databinding.StepDetailBinding;
import com.djzmo.bakingapp.databinding.StepIngredientsDetailBinding;
import com.djzmo.bakingapp.dummy.DummyContent;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * A fragment representing a single Step detail screen.
 * This fragment is either contained in a {@link StepListActivity}
 * in two-pane mode (on tablets) or a {@link StepDetailActivity}
 * on handsets.
 */
public class StepDetailFragment extends Fragment implements ExoPlayer.EventListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_STEP_REMOTE_ID = "step_remote_id";
    public static final int INTENT_STEP_REMOTE_ID = 100;
    public static final String ARG_RECIPE_ID = "recipe_id";
    public static final String PLAYER_POSITION_KEY = "player_position";
    private static final String TAG = StepDetailActivity.class.getSimpleName();

    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private RecipeInformation mRecipe;
    private StepInformation mStep;
    private String mRecipeId, mStepRemoteId, mPrevStepRemoteId, mNextStepRemoteId;
    private IngredientInformation[] mIngredients;
    private boolean mIsIngredients, mHasVideo;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StepDetailFragment() {
        mIsIngredients = false;
        mHasVideo = false;
        mStep = null;
        mRecipe = null;
        mIngredients = null;
        mPrevStepRemoteId = null;
        mNextStepRemoteId = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!getArguments().containsKey(ARG_RECIPE_ID) || !getArguments().containsKey(ARG_STEP_REMOTE_ID))
            return;

        String recipeId = getArguments().getString(ARG_RECIPE_ID);
        String stepRemoteId = getArguments().getString(ARG_STEP_REMOTE_ID);

        loadData(recipeId, stepRemoteId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mHasVideo)
            outState.putLong(PLAYER_POSITION_KEY, mExoPlayer.getCurrentPosition());
    }

    protected void loadData(String recipeId, String stepRemoteId) {
        Cursor cursor = getActivity().getContentResolver().query(RecipeContract.RecipeEntry.RECIPES_CONTENT_URI, null, RecipeContract.RecipeEntry._ID + " = ?", new String[] { recipeId }, null);
        cursor.moveToFirst();
        mIsIngredients = false;
        mRecipeId = recipeId;
        mStepRemoteId = stepRemoteId;
        mRecipe = new RecipeInformation(cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_REMOTE_ID)), cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_NAME)), cursor.getInt(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_SERVINGS)), cursor.getString(cursor.getColumnIndex(RecipeContract.RecipeEntry.COLUMN_IMAGE)));

        if(stepRemoteId.equals(getString(R.string.ingredients))) {
            mIsIngredients = true;

            cursor = getActivity().getContentResolver().query(RecipeContract.RecipeEntry.INGREDIENTS_CONTENT_URI, null, IngredientContract.IngredientEntry.COLUMN_RECIPE_ID + " = ?", new String[] { recipeId }, null);

            if(cursor != null && cursor.getCount() > 0) {
                mIngredients = new IngredientInformation[cursor.getCount()];
                for(int i = 0; i < cursor.getCount(); ++i) {
                    cursor.moveToPosition(i);

                    IngredientInformation ingredient = new IngredientInformation();
                    ingredient.name = cursor.getString(cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_NAME));
                    ingredient.quantity = cursor.getInt(cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_QUANTITY));
                    ingredient.measure = cursor.getString(cursor.getColumnIndex(IngredientContract.IngredientEntry.COLUMN_MEASURE));
                    mIngredients[i] = ingredient;
                }
            }

            getActivity().setTitle(getString(R.string.ingredients));
        }
        else {
            cursor = getActivity().getContentResolver().query(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, null, StepContract.StepEntry.COLUMN_REMOTE_ID + " = ? AND " + StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[] { stepRemoteId, recipeId }, null);

            if(cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mStep = new StepInformation(cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_REMOTE_ID)), cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_SHORT_DESCRIPTION)), cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_DESCRIPTION)), cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_VIDEO_URL)), cursor.getString(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_THUMBNAIL_URL)));
                getActivity().setTitle(mStep.shortDescription);
            }
        }

        if(!stepRemoteId.equals(getString(R.string.ingredients))) {
            cursor = getActivity().getContentResolver().query(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, null, StepContract.StepEntry.COLUMN_REMOTE_ID + " < ? AND " + StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[]{stepRemoteId, recipeId}, StepContract.StepEntry.COLUMN_REMOTE_ID + " DESC");

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mPrevStepRemoteId = Integer.toString(cursor.getInt(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_REMOTE_ID)));
            } else mPrevStepRemoteId = getString(R.string.ingredients);
        }

        if(stepRemoteId.equals(getString(R.string.ingredients)))
            mNextStepRemoteId = "0";
        else {
            cursor = getActivity().getContentResolver().query(RecipeContract.RecipeEntry.STEPS_CONTENT_URI, null, StepContract.StepEntry.COLUMN_REMOTE_ID + " > ? AND " + StepContract.StepEntry.COLUMN_RECIPE_ID + " = ?", new String[] { stepRemoteId, recipeId }, null);

            if(cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mNextStepRemoteId = Integer.toString(cursor.getInt(cursor.getColumnIndex(StepContract.StepEntry.COLUMN_REMOTE_ID)));
            }
            else mNextStepRemoteId = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        StepIngredientsDetailBinding ingredientsBinding = null;
        StepDetailBinding binding = null;

        if(mIsIngredients) {
            ingredientsBinding = DataBindingUtil.inflate(inflater, R.layout.step_ingredients_detail, container, false);
            rootView = ingredientsBinding.getRoot();
        }
        else {
            binding = DataBindingUtil.inflate(inflater, R.layout.step_detail, container, false);
            rootView = binding.getRoot();
        }

        if(getActivity().getClass().getSimpleName().equals(StepDetailActivity.class.getSimpleName()) && rootView.findViewById(R.id.step_controls) == null) {
            Intent data = new Intent();
            data.putExtra(ARG_STEP_REMOTE_ID, mStepRemoteId);
            getActivity().setResult(INTENT_STEP_REMOTE_ID, data);
            getActivity().finish();
        }

        mHasVideo = false;
        if (mIsIngredients) {
            LinearLayout llIngredients = ingredientsBinding.cardIngredientsList;
            for(int i = 0; i < mIngredients.length; ++i) {
                IngredientInformation ingredient = mIngredients[i];

                IngredientCardItemBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.ingredient_card_item, null, false);
                TextView tvQuantity = itemBinding.tvIngredientQuantity;
                TextView tvName = itemBinding.tvIngredientName;

                tvQuantity.setText(String.format("%d %s", ingredient.quantity, ingredient.measure.toLowerCase()));
                tvName.setText(ingredient.name);

                llIngredients.addView(itemBinding.getRoot());
            }
        }
        else {
            binding.stepDetail.setText(mStep.description);
            mPlayerView = binding.playerView;
            if(mStep != null && mStep.videoUrl != null && mStep.videoUrl.length() > 0) {
                initializeMediaSession();
                initializePlayer(Uri.parse(mStep.videoUrl));
                mHasVideo = true;

                if(savedInstanceState != null)
                    mExoPlayer.seekTo(savedInstanceState.getLong(PLAYER_POSITION_KEY));
            }
            else {
                mPlayerView.setVisibility(View.GONE);

                Guideline halfGuide = binding.horizontalHalf;
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) halfGuide.getLayoutParams();
                lp.guidePercent = 0;
                halfGuide.setLayoutParams(lp);
            }
        }

        // No use of binding because it is potentially null
        Button previousButton = (Button) rootView.findViewById(R.id.step_previous_button);
        Button nextButton = (Button) rootView.findViewById(R.id.step_next_button);

        if(previousButton != null) {
            if(mIsIngredients || mPrevStepRemoteId == null)
                previousButton.setEnabled(false);
            else {
                previousButton.setEnabled(true);
                previousButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mHasVideo) {
                            releasePlayer();
                            mMediaSession.setActive(false);
                        }

                        loadData(mRecipeId, mPrevStepRemoteId);

                        Fragment currentFragment = StepDetailFragment.this;
                        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                        fragTransaction.detach(currentFragment);

                        Bundle args = currentFragment.getArguments();
                        args.putString(ARG_RECIPE_ID, mRecipeId);
                        args.putString(ARG_STEP_REMOTE_ID, mPrevStepRemoteId);
                        currentFragment.setArguments(args);

                        fragTransaction.attach(currentFragment);
                        fragTransaction.commit();
                    }
                });
            }
        }

        if(nextButton != null) {
            if(mNextStepRemoteId == null)
                nextButton.setEnabled(false);
            else {
                nextButton.setEnabled(true);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mHasVideo) {
                            releasePlayer();
                            mMediaSession.setActive(false);
                        }

                        loadData(mRecipeId, mNextStepRemoteId);

                        Fragment currentFragment = StepDetailFragment.this;
                        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                        fragTransaction.detach(currentFragment);

                        Bundle args = currentFragment.getArguments();
                        args.putString(ARG_RECIPE_ID, mRecipeId);
                        args.putString(ARG_STEP_REMOTE_ID, mNextStepRemoteId);
                        currentFragment.setArguments(args);

                        fragTransaction.attach(currentFragment);
                        fragTransaction.commit();
                    }
                });
            }
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mHasVideo) {
            mHasVideo = false;
            releasePlayer();
            mMediaSession.setActive(false);
        }
    }

    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(getActivity(), TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }

    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(getActivity(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if((playbackState == ExoPlayer.STATE_READY)){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }
}
