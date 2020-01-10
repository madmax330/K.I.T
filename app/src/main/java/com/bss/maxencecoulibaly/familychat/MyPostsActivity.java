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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ActionBarAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.MyPostsAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FullScreenImageDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.images.TouchImageView;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.PostLike;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bss.maxencecoulibaly.familychat.utils.models.UserPost;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.Auth;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    // Constant variables
    private static String TAG = "MyPostsActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mNoResultView;

    private LoadingDialog loadingDialog;
    private FullScreenImageDialog fullScreenImageDialog;

    private ActionBarAdapter actionBarAdapter;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private MyPostsAdapter myPostsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBarAdapter = new ActionBarAdapter(actionBar);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        fullScreenImageDialog = new FullScreenImageDialog((RelativeLayout) findViewById(R.id.fullImageLayout), this) {
            @Override
            public void onDownloadFail(Exception e) {
                Toast.makeText(MyPostsActivity.this, getResources().getString(R.string.download_image_fail), Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        };
        mPostRecyclerView = (RecyclerView) findViewById(R.id.postsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        mNoResultView = (TextView) findViewById(R.id.noResultView);

        // Initialize program variables
        myPostsAdapter = new MyPostsAdapter(this, mFirebaseDatabaseReference, mUid, familyCode) {

            @Override
            public void onImageClick(Drawable drawable) {
                fullScreenImageDialog.setImage(drawable);
                fullScreenImageDialog.show();
            }
        };
        mPostRecyclerView.setAdapter(myPostsAdapter);

        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, mPhotoUrl);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, mUsername);

        actionBarAdapter.setTitle(mUsername);
        actionBarAdapter.loadProfile(mPhotoUrl, this);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        loadingDialog.setText(getResources().getString(R.string.loading_posts));
        loadingDialog.show();

        DatabaseReference reference = mFirebaseDatabaseReference.child(Constants.USERPOSTS_CHILD).child(familyCode).child(mUid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getChildrenCount() > 0) {
                        final ArrayList<Post> posts = new ArrayList<Post>();
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            final UserPost userPost = snapshot.getValue(UserPost.class);
                            DatabaseReference postRef = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(userPost.getCategory()).child(userPost.getPostId());
                            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snap) {
                                    if(snap.exists()) {
                                        Post temp = snap.getValue(Post.class);
                                        temp.setId(snap.getKey());
                                        posts.add(temp);
                                        if(posts.size() == (int) dataSnapshot.getChildrenCount()) {
                                            myPostsAdapter.setResults(posts);
                                            loadingDialog.hide();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled: Failed to load post " + userPost.getPostId() + " " + databaseError);
                                }
                            });
                        }
                    }
                    else {
                        mNoResultView.setVisibility(TextView.VISIBLE);
                        loadingDialog.hide();
                    }
                }
                else{
                    mNoResultView.setVisibility(TextView.VISIBLE);
                    loadingDialog.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Failed to load posts for user " + mUid + " " + databaseError);
            }
        });
    }
}
