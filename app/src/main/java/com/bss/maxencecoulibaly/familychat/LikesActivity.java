package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.LikesRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FullScreenImageDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.PostLike;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "LikesActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mLikesRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private CircleImageView mUserImageView;
    private TextView mUserNameView;
    private ImageView mPostImageView;
    private TextView mPostTextView;
    private TextView mPostTimeView;

    private FullScreenImageDialog fullScreenImageDialog;
    private LoadingDialog loadingDialog;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private String mPostId;
    private String mPostCategory;
    private boolean mMyPosts;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private LikesRecyclerAdapter likesRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize view components
        fullScreenImageDialog = new FullScreenImageDialog((RelativeLayout) findViewById(R.id.fullImageLayout), this) {
            @Override
            public void onDownloadFail(Exception e) {
                Toast.makeText(LikesActivity.this, getResources().getString(R.string.download_image_fail), Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        };
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_post));

        mLikesRecyclerView = (RecyclerView) findViewById(R.id.likesRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLikesRecyclerView.setLayoutManager(mLinearLayoutManager);

        mUserNameView = (TextView) findViewById(R.id.userNameView);
        mUserImageView = (CircleImageView) findViewById(R.id.userImageView);
        mPostTextView = (TextView) findViewById(R.id.postTextView);
        mPostTimeView = (TextView) findViewById(R.id.postTimeView);
        mPostImageView = (ImageView) findViewById(R.id.postImageView);

        // Initialize program variables
        mPostId = getIntent().getStringExtra(Constants.EXTRA_POST_ID);
        if(mPostId == null) {
            startActivity(new Intent(this, FeedActivity.class));
        }
        mPostCategory = getIntent().getStringExtra(Constants.EXTRA_POST_CATEGORY);
        if(mPostCategory == null) {
            mPostCategory = Constants.POSTS_GENERAL_CATEGORY;
        }

        mMyPosts = getIntent().getStringExtra(Constants.EXTRA_MY_POSTS) != null;

        // Load elements from database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabaseReference.child(Constants.NOTIFICATIONS_CHILD).child(familyCode).child(mUid).child(Constants.POST_NOTIFICATION).child(Constants.LIKE_NOTIFICATION).child(mPostId).setValue(null);

        loadPost();

    }

    @Override
    public void onPause() {
        likesRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingDialog.show();
        loadLikes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home: {
                if(mMyPosts) {
                    startActivity(new Intent(LikesActivity.this, MyPostsActivity.class));
                }
                else {
                    startActivity(new Intent(LikesActivity.this, FeedActivity.class));
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(mMyPosts) {
            startActivity(new Intent(LikesActivity.this, MyPostsActivity.class));
        }
        else {
            startActivity(new Intent(LikesActivity.this, FeedActivity.class));
        }
    }

    private void loadPost() {
        DatabaseReference postRef = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(mPostCategory).child(mPostId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    final Post post = dataSnapshot.getValue(Post.class);
                    if(post.getText() != null) {
                        mPostTextView.setText(post.getText());
                    }
                    if(post.getPhotoUrl() != null) {
                        if(post.getThumbnail() != null) {
                            ImageUtil.loadImage(LikesActivity.this, mPostImageView, post.getThumbnail(), false);
                        }
                        else {
                            mPostImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_image));
                            mPostImageView.setVisibility(ImageView.VISIBLE);
                        }
                        mPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Glide.with(getApplicationContext())
                                        .load(post.getPhotoUrl())
                                        .into(new SimpleTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                                fullScreenImageDialog.setImage(resource);
                                                fullScreenImageDialog.show();
                                            }
                                        });
                            }
                        });
                    }
                    else {
                        mPostImageView.setVisibility(ImageView.GONE);
                    }
                    mPostTimeView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(post.getTimestamp())));
                    if(post.getCategory() != null) {
                        mPostCategory = post.getCategory();
                    }

                    // Get user profile
                    DatabaseReference profileRef = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(post.getUserId());
                    profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Profile profile = dataSnapshot.getValue(Profile.class);
                                mUserNameView.setText(profile.getName());
                                ImageUtil.loadImage(LikesActivity.this, mUserImageView, profile.getPhotoUrl(), true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: Post user not found for user " + mUid + " for post " + post.getId() + ". " + databaseError);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Post not found. " + databaseError);
            }
        });
    }

    private void loadLikes() {
        SnapshotParser<PostLike> parser = new SnapshotParser<PostLike>() {
            @Override
            public PostLike parseSnapshot(DataSnapshot dataSnapshot) {
                PostLike like = dataSnapshot.getValue(PostLike.class);
                if (like != null) {
                    like.setId(dataSnapshot.getKey());
                }
                return like;
            }
        };

        DatabaseReference likesRef = mFirebaseDatabaseReference.child(Constants.POST_LIKES_CHILD).child(familyCode).child(mPostId);
        FirebaseRecyclerOptions<PostLike> options =
                new FirebaseRecyclerOptions.Builder<PostLike>()
                        .setQuery(likesRef, parser)
                        .build();

        likesRecyclerAdapter = new LikesRecyclerAdapter(options, this, mFirebaseDatabaseReference, familyCode) {

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
            }

        };

        likesRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = likesRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mLikesRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mLikesRecyclerView.setAdapter(likesRecyclerAdapter);

        likesRecyclerAdapter.startListening();

    }

}
