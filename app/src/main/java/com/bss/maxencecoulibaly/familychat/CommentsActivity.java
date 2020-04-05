package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.CommentsRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FullScreenImageDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.PostCommentForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.PostComment;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "CommentsActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mCommentsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private CircleImageView mUserImageView;
    private TextView mUserNameView;
    private ImageView mPostImageView;
    private TextView mPostTextView;
    private TextView mPostTimeView;

    private String mPostCategory;
    private boolean mMyPosts;

    private FullScreenImageDialog fullScreenImageDialog;
    private LoadingDialog loadingDialog;
    private PostCommentForm postCommentForm;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mPostId;

    private Post mPost;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Init firebase database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        // Initialize view components
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        fullScreenImageDialog = new FullScreenImageDialog((RelativeLayout) findViewById(R.id.fullImageLayout), this) {
            @Override
            public void onDownloadFail(Exception e) {
                Toast.makeText(CommentsActivity.this, getResources().getString(R.string.download_image_fail), Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        };
        mCommentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mCommentsRecyclerView.setLayoutManager(mLinearLayoutManager);

        mUserNameView = (TextView) findViewById(R.id.userNameView);
        mUserImageView = (CircleImageView) findViewById(R.id.userImageView);
        mPostTextView = (TextView) findViewById(R.id.postTextView);
        mPostTimeView = (TextView) findViewById(R.id.postTimeView);
        mPostImageView = (ImageView) findViewById(R.id.postImageView);

        postCommentForm = new PostCommentForm((RelativeLayout) findViewById(R.id.commentForm)) {
            @Override
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.posting_comment));
                loadingDialog.show();

                try {
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                } catch(NullPointerException e) {
                    Log.d(TAG, "Closing input keyboard failed.");
                }
                String commText = postCommentForm.getComment();
                PostComment comment = new PostComment(mUid, mPostId, mPostCategory, mPost.getUserId(), commText, new Date().getTime());
                String commentKey = mFirebaseDatabaseReference.child(Constants.POST_COMMENTS_CHILD).child(familyCode).child(mPostId).push().getKey();
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POST_COMMENTS_CHILD, familyCode, mPostId, commentKey}), comment);
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USER_COMMENTS_CHILD, familyCode, mUid, commentKey}), mPostId);

                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseUpdates.clear();
                            postCommentForm.reset();
                            loadingDialog.hide();
                        }
                        else {
                            Log.w(TAG, "Unable to comment post: " + mPostId + " for user: " + mUid + ". ", databaseError.toException());
                            Toast.makeText(CommentsActivity.this, getResources().getString(R.string.error_commenting_post), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        // Initialize program variables
        mPostId = getIntent().getStringExtra(Constants.EXTRA_POST_ID);
        if(mPostId == null) {
            startActivity(new Intent(this, FeedActivity.class));
            finish();
        }
        mPostCategory = getIntent().getStringExtra(Constants.EXTRA_POST_CATEGORY);
        if(mPostCategory == null) {
            mPostCategory = Constants.POSTS_GENERAL_CATEGORY;
        }

        mMyPosts = getIntent().getStringExtra(Constants.EXTRA_MY_POSTS) != null;

        mFirebaseDatabaseReference.child(Constants.NOTIFICATIONS_CHILD).child(familyCode).child(mUid).child(Constants.POST_NOTIFICATION).child(Constants.COMMENT_NOTIFICATION).child(mPostId).setValue(null);

        loadPost();

    }

    @Override
    public void onPause() {
        commentsRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadComments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home: {
                if(mMyPosts) {
                    startActivity(new Intent(CommentsActivity.this, MyPostsActivity.class));
                }
                else {
                    startActivity(new Intent(CommentsActivity.this, FeedActivity.class));
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
            startActivity(new Intent(CommentsActivity.this, MyPostsActivity.class));
        }
        else {
            startActivity(new Intent(CommentsActivity.this, FeedActivity.class));
        }
    }

    private void loadPost() {
        loadingDialog.setText(getResources().getString(R.string.loading_post));
        loadingDialog.show();

        DatabaseReference postRef = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(mPostCategory).child(mPostId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mPost = dataSnapshot.getValue(Post.class);

                    if(mPost.getText() != null) {
                        mPostTextView.setText(mPost.getText());
                    }
                    if(mPost.getPhotoUrl() != null) {
                        if(mPost.getThumbnail() != null) {
                            ImageUtil.loadImage(CommentsActivity.this, mPostImageView, mPost.getThumbnail(), false);
                        }
                        else {
                            mPostImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_image));
                            mPostImageView.setVisibility(ImageView.VISIBLE);
                        }
                        mPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Glide.with(getApplicationContext())
                                        .load(mPost.getPhotoUrl())
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

                    mPostTimeView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(mPost.getTimestamp())));
                    if(mPost.getCategory() != null) {
                        mPostCategory = mPost.getCategory();
                    }

                    // Get user profile
                    DatabaseReference profileRef = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mPost.getUserId());
                    profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Profile profile = dataSnapshot.getValue(Profile.class);
                                mUserNameView.setText(profile.getName());
                                ImageUtil.loadImage(CommentsActivity.this, mUserImageView, profile.getPhotoUrl(), true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: Post user not found for user " + mUid + " for post " + mPost.getId() + ". " + databaseError);
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

    private void loadComments() {
        loadingDialog.setText(getResources().getString(R.string.loading_comments));
        loadingDialog.show();

        // Load comments
        SnapshotParser<PostComment> parser = new SnapshotParser<PostComment>() {
            @Override
            public PostComment parseSnapshot(DataSnapshot dataSnapshot) {
                PostComment comment = dataSnapshot.getValue(PostComment.class);
                if (comment != null) {
                    comment.setId(dataSnapshot.getKey());
                }
                return comment;
            }
        };

        DatabaseReference commentsRef = mFirebaseDatabaseReference.child(Constants.POST_COMMENTS_CHILD).child(familyCode).child(mPostId);
        FirebaseRecyclerOptions<PostComment> options =
                new FirebaseRecyclerOptions.Builder<PostComment>()
                        .setQuery(commentsRef, parser)
                        .build();

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(options, this, mFirebaseDatabaseReference, familyCode) {

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
            }

        };

        commentsRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int commentCount = commentsRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (commentCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mCommentsRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mCommentsRecyclerView.setAdapter(commentsRecyclerAdapter);

        commentsRecyclerAdapter.startListening();
    }

}
