package com.stream.app.Adapters;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.stream.app.Classes.CacheDataSourceFactory;
import com.stream.app.Classes.MyUtils;
import com.stream.app.Classes.PostButtonBounceInterpolator;
import com.stream.app.Classes.RegularPost;
import com.stream.app.R;

import java.util.ArrayList;
import java.util.List;

public class RegularPostAdapter extends RecyclerView.Adapter<RegularPostAdapter.MyViewHolder> {

    // Regular home posts list
    private List<RegularPost> postsList;
    private Context mContext;

    // View holders to control exoplayers
    private MyViewHolder firstHolder, secondHolder;

    // Main class constructor
    public RegularPostAdapter(List<RegularPost> postsList, Context context) {
        this.postsList = postsList;
        this.mContext = context;
    }

    // Main view holder class references
    public class MyViewHolder extends RecyclerView.ViewHolder {

        // A home posts' layout View references
        private PlayerView mPostExoPlayerView;
        private ImageView mPostDpImageView, mHeartImageView, mCommentImageView, mFullscreenIcon;
        private RelativeLayout mMainHolder;
        private LinearLayout mPlayPauseHolder, mPostInteractionHolder , mPostCountsHolder, mUserIdentityHolder;
        private TextView mUserNameTextView, mPostTitle, mViewsCountTextView, mHeartCountTextView, mCommentCountTextView;
        private ImageView mPlayBtn, mPauseBtn;

        // Holders ID for previous, after and third view holder assignment management
        private int holderID;

        // Exoplayer references
        private String vidAddress;
        private ExoPlayer mExoPlayer;
        private MediaSource mMediaSource;
        private int mExoPlayerWindowIndex = 0;
        private long mPlaybackPosition = 0;

        // Setting holder id method for management
        public void setHolderID(int holderID) {
            this.holderID = holderID;
        }

        // Getting holder id when managing
        public int getHolderID() {
            return holderID;
        }

        private MyViewHolder(View view) {
            super(view);

            // Connecting home post layout views to code
            mMainHolder = (RelativeLayout) view.findViewById(R.id.main_holder);
            mPostTitle = (TextView) view.findViewById(R.id.title);
            mPostExoPlayerView = (PlayerView) view.findViewById(R.id.playerview);

            mPlayBtn = (ImageView) view.findViewById(R.id.play_button);
            mPauseBtn = (ImageView) view.findViewById(R.id.pause_button);
            mPlayPauseHolder = (LinearLayout) view.findViewById(R.id.play_pause_holder);

            mUserNameTextView = (TextView) view.findViewById(R.id.username);
            mPostDpImageView = (ImageView) view.findViewById(R.id.regular_post_dp);
            mUserIdentityHolder = (LinearLayout) view.findViewById(R.id.user_identity_holder);

            mHeartImageView = (ImageView) view.findViewById(R.id.heart_button);
            mCommentImageView = (ImageView) view.findViewById(R.id.comment_button);

            mViewsCountTextView = (TextView) view.findViewById(R.id.views_count);
            mHeartCountTextView = (TextView) view.findViewById(R.id.heart_count);
            mCommentCountTextView = (TextView) view.findViewById(R.id.comment_count);
            mPostCountsHolder = (LinearLayout) view.findViewById(R.id.counts_holder);

            mPostInteractionHolder = (LinearLayout) view.findViewById(R.id.post_interaction_button_holder);
            mFullscreenIcon = (ImageView) view.findViewById(R.id.fullscreen_icon);

            // Setting a fixed size to the home post
            setMainHolderHeight();
            // Setting up the posts click listeners
            setUpPostClickListeners();

        }

        // Exoplayer events listener
        Player.EventListener mExoPlayerEventListener = new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playWhenReady){
                    playPauseBtnToggle(true);
                }else{
                    playPauseBtnToggle(false);
                }

                // Giving Player state response to user
                switch (playbackState){

                    case ExoPlayer.STATE_READY: {
                        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    break;
                    case ExoPlayer.STATE_BUFFERING: {}
                    case ExoPlayer.STATE_ENDED: {
                        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    case ExoPlayer.STATE_IDLE: {
                        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    break;
                }

            }

            // On error keep on trying to ready the player on and on (This happens when internet connection unintentionally goes)
            @Override
            public void onPlayerError(ExoPlaybackException error) {

                mExoPlayer.prepare(mMediaSource, false, false);

            }
            // Not needed default methods
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}
            @Override
            public void onLoadingChanged(boolean isLoading) {}
            @Override
            public void onPositionDiscontinuity(int reason) {}
            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
            @Override
            public void onSeekProcessed() {}
            @Override
            public void onRepeatModeChanged(int repeatMode) {}
            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
        };

        // Initializing the posts exoplayer
        private void initializeExoPlayer(){

            if(mExoPlayer == null){

                mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                        new DefaultRenderersFactory(mContext),
                        new DefaultTrackSelector(),
                        new DefaultLoadControl());

                mPostExoPlayerView.setPlayer(mExoPlayer);

                mExoPlayer.seekTo(mExoPlayerWindowIndex, mPlaybackPosition);

                mExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                prepareExoPlayer();
            }

        }

        // Preparing the exoplayer with the video address
        private void prepareExoPlayer(){

            Uri uri = Uri.parse(vidAddress);
            mMediaSource = buildMediaSource(uri);
            mExoPlayer.prepare(mMediaSource, false, false);

            mExoPlayer.addListener(mExoPlayerEventListener);

        }

        // Starting the exoplayer manually
        public void startExoPlayer(){
            if(mExoPlayer != null)
                mExoPlayer.setPlayWhenReady(true);
        }

        // Stopping the exoplayer manually
        public void stopExoPlayer(){
            if(mExoPlayer != null)
                mExoPlayer.setPlayWhenReady(false);
        }

        // Building mediasource for exoplayer
        private MediaSource buildMediaSource(Uri uri) {

            return new ExtractorMediaSource(uri,
                    new CacheDataSourceFactory(mContext, 100 * 1024 * 1024, 5 * 1024 * 1024),
                    new DefaultExtractorsFactory(),
                    null,
                    null);

        }

        // Setting a fixed height for the root view
        private void setMainHolderHeight(){

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int pixels = (int) (4 * scale + 0.5f);

            int adjustedHeight = width - pixels ;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adjustedHeight);
            mMainHolder.setLayoutParams(params);

        }

        // Bounce animation for the post interaction buttons
        private void bounceAnimation(final View v){

            final Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.post_button_bounce);

            PostButtonBounceInterpolator interpolator = new PostButtonBounceInterpolator(0.08, 40);
            myAnim.setInterpolator(interpolator);

            v.startAnimation(myAnim);

        }

        // Setting up method for all the click listeners
        private void setUpPostClickListeners(){

            mPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPauseBtnToggle(true);
                    startExoPlayer();
                }
            });

            mPauseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPauseBtnToggle(false);
                    stopExoPlayer();
                }
            });

            mPostExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
                @Override
                public void onVisibilityChange(int visibility) {

                    if(visibility == View.VISIBLE){

                        setFadeAnim(mPlayPauseHolder, true);
                        setFadeAnim(mFullscreenIcon, true);
                        setFadeAnim(mUserIdentityHolder, true);
                        setFadeAnim(mPostTitle, true);
                        setFadeAnim(mPostInteractionHolder, true);
                        setFadeAnim(mPostCountsHolder, true);
                        setFadeAnim(mViewsCountTextView, true);

                    }else{

                        setFadeAnim(mPlayPauseHolder, false);
                        setFadeAnim(mFullscreenIcon, false);
                        setFadeAnim(mUserIdentityHolder, false);
                        setFadeAnim(mPostTitle, false);
                        setFadeAnim(mPostInteractionHolder, false);
                        setFadeAnim(mPostCountsHolder, false);
                        setFadeAnim(mViewsCountTextView, false);

                    }

                }
            });

            mHeartImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHeartImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_icon_red));
                    bounceAnimation(mHeartImageView);
                }
            });

            mCommentImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bounceAnimation(mCommentImageView);


                }
            });

            mFullscreenIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });

            mUserIdentityHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });

        }

        private void setFadeAnim(final View v, final boolean fadeIn){

            Animation fade = new AlphaAnimation(1, 0);
            fade.setInterpolator(new FastOutSlowInInterpolator());
            if(fadeIn){
                v.setVisibility(View.VISIBLE);
                fade = new AlphaAnimation(0, 1);
                fade.setInterpolator(new AnticipateOvershootInterpolator());
            }
            fade.setDuration(400);
            fade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(!fadeIn){
                        v.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            v.startAnimation(fade);

        }

        private void playPauseBtnToggle(boolean play){

            if(play){

                mPlayBtn.setVisibility(View.GONE);
                mPauseBtn.setVisibility(View.VISIBLE);

            }else{

                mPlayBtn.setVisibility(View.VISIBLE);
                mPauseBtn.setVisibility(View.GONE);

            }

        }

        // Releasing the exoplayer manually ( When home fragment gets paused or stopped )
        private void releaseExoPlayer() {

            if(mExoPlayer != null) {
                mPlaybackPosition = mExoPlayer.getCurrentPosition();
                mExoPlayerWindowIndex = mExoPlayer.getCurrentWindowIndex();
                mExoPlayer.removeListener(mExoPlayerEventListener);
                mExoPlayer.release();
                mExoPlayer = null;
            }

        }
    }

    // Inflating the post layout
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.regular_post_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // Getting the posts instance to get that specific posts data and contents
        RegularPost post = postsList.get(position);

        // Getting posts video url
        holder.vidAddress = post.getPostContentUrl();

        // Setting up posts views
        holder.mUserNameTextView.setText(post.getPostUserName());
        holder.mViewsCountTextView.setText(String.valueOf(MyUtils.formatNumbers(post.getPostViewsCount())) + " views");
        holder.mHeartCountTextView.setText(String.valueOf(MyUtils.formatNumbers(post.getPostHeartCount())) + " hearts");
        holder.mCommentCountTextView.setText(String.valueOf(MyUtils.formatNumbers(post.getPostCommentCount())) + " comments");
        holder.mPostTitle.setText( "How's ya doing bros? Just a stroll through the woods! Hey wassup boizzz" );

        // For spannable see more text click event
        // TODO holder.mPostDescription.setMovementMethod(LinkMovementMethod.getInstance());

        // Setting up posts users dp
        Glide.with(mContext)
                .load(post.getPostUserDpUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.mPostDpImageView);

        // Setting a unique holder id for managing the previous, after and third viewholders instances
        holder.setHolderID(position);

    }

    // Default method
    @Override
    public int getItemCount() {
        return postsList.size();
    }

    // Method when a view gets attached to the recyclersview ( Needed for managing the exoplayers autoplay and pause )
    @Override
    public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        // Initializing exoplayer
        holder.initializeExoPlayer();

        if(firstHolder == null){
            firstHolder = holder;
            return;
        }

        if(holder.getHolderID() == firstHolder.getHolderID() + 1){
            secondHolder = holder;
        }else if(holder.getHolderID() == firstHolder.getHolderID() + 2){
            // Fix
            firstHolder.releaseExoPlayer();
            firstHolder = secondHolder;
            secondHolder = holder;
        }else if(holder.getHolderID() == firstHolder.getHolderID() - 1){

            // Verifies that secondHolder is not null after fragment popped from backstack
            if(secondHolder != null)
                secondHolder.releaseExoPlayer();
            secondHolder = firstHolder;
            firstHolder = holder;

        }


    }

    // Method when a view gets detached to the recyclersview ( Needed for managing the exoplayers autoplay and pause )
    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.stopExoPlayer();

    }

    // Checks the visibility percentage of a post on user screen
    public void checkPercentageAndStartStopPlayer(ArrayList<Integer> arrayList){

        if(arrayList.size() == 2){

            Integer previousPer = arrayList.get(0);
            Integer afterPer = arrayList.get(1);

            if(previousPer < 50 && afterPer > 50){
                firstHolder.stopExoPlayer();
                secondHolder.startExoPlayer();
            }else if(previousPer > 50 && afterPer < 50){
                firstHolder.startExoPlayer();
                secondHolder.stopExoPlayer();
            }

        }

    }

    // Initializes all viewholders' exoplayers manually (When home fragment gets paused or stopped)
    public void initExoPlayerFromFragment(){

        if(firstHolder != null)
            firstHolder.initializeExoPlayer();
        if(secondHolder != null)
            secondHolder.initializeExoPlayer();
    }

    // Releases all viewholders' exoplayers manually (When home fragment gets resumed or started)
    public void releaseExoPlayerFromFragment(){

        if(firstHolder != null)
            firstHolder.releaseExoPlayer();
        if(secondHolder != null)
            secondHolder.releaseExoPlayer();

    }

}