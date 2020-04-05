package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ActionBarAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.BottomMenuAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.FamilyRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.FeedRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FamiliesDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FullScreenImageDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.PostLike;
import com.bss.maxencecoulibaly.familychat.utils.models.UserFamily;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class FeedActivity extends AppCompatActivity {

    // Constant variables
    private static String TAG = "FeedActivity";

    // View variables
    private Toolbar mActionBar;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private LinearLayoutManager mFamiliesLinearLayoutManager;
    private TextView mNoResultView;

    private Button mGeneralBtn;
    private Button mTravelBtn;
    private Button mEventsBtn;

    private RelativeLayout mGeneralContainer;
    private RelativeLayout mTravelContainer;
    private RelativeLayout mEventsContainer;

    private FloatingActionButton mNotificationsBtn;
    private TextView mNotificationsBadge;

    private LoadingDialog loadingDialog;
    private FullScreenImageDialog fullScreenImageDialog;
    private FamiliesDialog familiesDialog;

    private ActionBarAdapter actionBarAdapter;
    private BottomMenuAdapter bottomMenuAdapter;

    // Program variables
    private String familyCode;
    private String familyName;
    private String familyPhoto;

    private String mUid;
    private String mUsername;
    private String mPostCategory;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FeedRecyclerAdapter feedRecyclerAdapter;
    private FamilyRecyclerAdapter familyRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);
        if(familyCode == null) {
            resetPrefs();
            startActivity(new Intent(this, ActivationActivity.class));
        }
        familyPhoto = mSharedPreferences.getString(Constants.PREF_FAMILY_PHOTO, null);
        familyName = mSharedPreferences.getString(Constants.PREF_FAMILY_NAME, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();

        actionBarAdapter = new ActionBarAdapter(actionBar);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        boolean first = mSharedPreferences.getString(Constants.PREF_FIRST_LOGIN, null) == null;

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
        }
        else{
            if(first) {
                // store user id in shared prefs.
                mUid = mFirebaseUser.getUid();
                mUsername = mFirebaseUser.getDisplayName();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.PREF_USER_ID, mUid);
                editor.putString(Constants.PREF_USER_NAME, mUsername);
                editor.putString(Constants.PREF_FIRST_LOGIN, "DONE");
                editor.apply();

                startActivity(new Intent(this, EditProfileActivity.class));
            }
        }

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        fullScreenImageDialog = new FullScreenImageDialog((RelativeLayout) findViewById(R.id.fullImageLayout), this) {
            @Override
            public void onDownloadFail(Exception e) {
                Toast.makeText(FeedActivity.this, getResources().getString(R.string.download_image_fail), Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        };
        bottomMenuAdapter = new BottomMenuAdapter((LinearLayout) findViewById(R.id.tabBar), "feed", this);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mPostRecyclerView = (RecyclerView) findViewById(R.id.postsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        mNoResultView = (TextView) findViewById(R.id.noResultView);

        mGeneralBtn = (Button) findViewById(R.id.generalBtn);
        mTravelBtn = (Button) findViewById(R.id.travelBtn);
        mEventsBtn = (Button) findViewById(R.id.eventsBtn);

        mGeneralContainer = (RelativeLayout) findViewById(R.id.generalContainer);
        mTravelContainer = (RelativeLayout) findViewById(R.id.travelContainer);
        mEventsContainer = (RelativeLayout) findViewById(R.id.eventsContainer);

        mNotificationsBadge = (TextView) findViewById(R.id.notificationsLabel);
        mNotificationsBtn = (FloatingActionButton) findViewById(R.id.notificationsBtn);

        familiesDialog = new FamiliesDialog((RelativeLayout) findViewById(R.id.familiesLayout), this);
        mFamiliesLinearLayoutManager = new LinearLayoutManager(this);
        familiesDialog.recyclerView.setLayoutManager(mFamiliesLinearLayoutManager);

        // Initialize program variables
        actionBarAdapter.setTitle(familyName);
        actionBarAdapter.loadProfile(familyPhoto, this);

        mPostCategory = getIntent().getStringExtra(Constants.EXTRA_POST_CATEGORY);
        if (mPostCategory == null) {
            mPostCategory = Constants.POSTS_GENERAL_CATEGORY;
        }
        setCategory(mPostCategory);

        // handle topic changes
        mGeneralBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory(Constants.POSTS_GENERAL_CATEGORY);
                loadPosts();
            }
        });

        mTravelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory(Constants.POSTS_TRAVEL_CATEGORY);
                loadPosts();
            }
        });

        mEventsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory(Constants.POSTS_EVENTS_CATEGORY);
                loadPosts();
            }
        });

        mNotificationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FeedActivity.this, NotificationsActivity.class));
            }
        });

        // Handle refresh request
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (feedRecyclerAdapter != null) {
                    feedRecyclerAdapter.stopListening();
                    feedRecyclerAdapter.startListening();
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });

    }

    @Override
    public void onPause() {
        familyRecyclerAdapter.stopListening();
        feedRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFamilies();
        loadPosts();
        loadNotifications();
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.newPostBtn:
                startActivity(new Intent(FeedActivity.this, NewPostActivity.class).putExtra(Constants.EXTRA_POST_CATEGORY, mPostCategory));
                return true;

            case R.id.changeFamilyBtn:
                familiesDialog.show();
                return true;
            case R.id.joinFamilyBtn:
                changeFamily(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadNotifications() {
        DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.NOTIFICATIONS_CHILD).child(familyCode).child(mUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    mNotificationsBadge.setVisibility(TextView.VISIBLE);
                    mNotificationsBtn.setVisibility(FloatingActionButton.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Problem loading user notifications");
            }
        });
    }

    private void loadFamilies() {
        loadingDialog.setText(getResources().getString(R.string.loading_settings));
        loadingDialog.show();

        SnapshotParser<UserFamily> parser = new SnapshotParser<UserFamily>() {
            @NonNull
            @Override
            public UserFamily parseSnapshot(@NonNull DataSnapshot snapshot) {
                UserFamily family = snapshot.getValue(UserFamily.class);
                if(family != null) {
                    family.setId(snapshot.getKey());
                }
                return family;
            }
        };

        DatabaseReference reference = mFirebaseDatabaseReference.child(Constants.USERFAMILIES_CHILD).child(mUid);
        FirebaseRecyclerOptions<UserFamily> options = new FirebaseRecyclerOptions.Builder<UserFamily>().setQuery(reference, parser).build();

        if(familyRecyclerAdapter != null) {
            familyRecyclerAdapter.stopListening();
        }

        familyRecyclerAdapter = new FamilyRecyclerAdapter(options) {
            @Override
            public void onItemClick(UserFamily family) {
                if(family.getId().equals(familyCode)) {
                    Toast.makeText(FeedActivity.this, getResources().getString(R.string.is_current_family), Toast.LENGTH_SHORT);
                }
                else {
                    changeFamily(family);
                }
            }

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
            }
        };

        familyRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatCount = familyRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added chat.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    familiesDialog.recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        familiesDialog.recyclerView.setAdapter(familyRecyclerAdapter);

        familyRecyclerAdapter.startListening();
    }

    private void loadPosts() {
        loadingDialog.setText(getResources().getString(R.string.loading_posts));
        loadingDialog.show();

        mNoResultView.setVisibility(TextView.INVISIBLE);

        SnapshotParser<Post> parser = new SnapshotParser<Post>() {
            @Override
            public Post parseSnapshot(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    post.setId(dataSnapshot.getKey());
                }
                return post;
            }
        };

        DatabaseReference postsRef = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(mPostCategory);
        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(postsRef.orderByKey(), parser)
                        .build();

        if (feedRecyclerAdapter != null) {
            feedRecyclerAdapter.stopListening();
        }

        feedRecyclerAdapter = new FeedRecyclerAdapter(options, this, mFirebaseDatabaseReference) {

            @Override
            public void onImageClick(Drawable drawable) {
                loadingDialog.hide();
                fullScreenImageDialog.show();
                fullScreenImageDialog.setImage(drawable);
            }

            @Override
            public void beforeImageLoad() {
                loadingDialog.setText(getResources().getString(R.string.loading));
                loadingDialog.show();
            }

            @Override
            public void onLikeClick(final PostViewHolder viewHolder, final Post post) {
                final String postId = post.getId();
                if (viewHolder.liked) {
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USER_LIKES_CHILD, familyCode, mUid, postId}), null);
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POST_LIKES_CHILD, familyCode, postId, mUid}), null);

                    mFirebaseDatabaseReference.updateChildren(databaseUpdates);
                    databaseUpdates.clear();

                    viewHolder.liked = false;
                    viewHolder.likes -= 1;
                    viewHolder.setBackground(getResources().getDrawable(R.drawable.like));
                    viewHolder.setText(getResources().getString(R.string.number_likes, viewHolder.likes));
                } else {
                    PostLike like = new PostLike(mUid, postId, mPostCategory, post.getUserId(), new Date().getTime());
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POST_LIKES_CHILD, familyCode, postId, mUid}), like);
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USER_LIKES_CHILD, familyCode, mUid, postId}), true);

                    mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                viewHolder.liked = true;
                                viewHolder.likes += 1;
                                viewHolder.setBackground(getResources().getDrawable(R.drawable.liked));
                                viewHolder.setText(getResources().getString(R.string.number_likes, viewHolder.likes));

                                databaseUpdates.clear();
                            } else {
                                Log.w("FeedActivity", "Unable to like post " + postId + " for user " + mUid + ". ", databaseError.toException());
                                Toast.makeText(FeedActivity.this, getResources().getString(R.string.error_liking_post), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
                if(feedRecyclerAdapter.getItemCount() == 0) {
                    mNoResultView.setVisibility(TextView.VISIBLE);
                }
            }

        };

        feedRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatCount = feedRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added chat.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mPostRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mPostRecyclerView.setAdapter(feedRecyclerAdapter);

        feedRecyclerAdapter.startListening();
    }

    public String getUid() {
        return mUid;
    }

    public String getCategory() {
        return mPostCategory;
    }

    public String getFamilyCode() {
        return familyCode;
    }

    private void resetPrefs() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.PREF_USER_PHOTO_URL, null);
        editor.putString(Constants.PREF_USER_NAME, null);
        editor.putString(Constants.PREF_FAMILY_CODE, null);
        editor.putString(Constants.PREF_USER_TOKEN, null);
        editor.apply();
    }

    private void setCategory(String category) {
        switch (category) {
            case Constants.POSTS_GENERAL_CATEGORY:
                mPostCategory = Constants.POSTS_GENERAL_CATEGORY;
                mGeneralContainer.setBackgroundColor(getResources().getColor(R.color.grey));
                mGeneralBtn.setTypeface(null, Typeface.BOLD);
                mTravelContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mTravelBtn.setTypeface(null, Typeface.NORMAL);
                mEventsContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mEventsBtn.setTypeface(null, Typeface.NORMAL);
                break;

            case Constants.POSTS_TRAVEL_CATEGORY:
                mPostCategory = Constants.POSTS_TRAVEL_CATEGORY;
                mGeneralContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mGeneralBtn.setTypeface(null, Typeface.NORMAL);
                mTravelContainer.setBackgroundColor(getResources().getColor(R.color.grey));
                mTravelBtn.setTypeface(null, Typeface.BOLD);
                mEventsContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mEventsBtn.setTypeface(null, Typeface.NORMAL);
                break;

            case Constants.POSTS_EVENTS_CATEGORY:
                mPostCategory = Constants.POSTS_EVENTS_CATEGORY;
                mGeneralContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mGeneralBtn.setTypeface(null, Typeface.NORMAL);
                mTravelContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mTravelBtn.setTypeface(null, Typeface.NORMAL);
                mEventsContainer.setBackgroundColor(getResources().getColor(R.color.grey));
                mEventsBtn.setTypeface(null, Typeface.BOLD);
                break;
        }
    }

    private void changeFamily(UserFamily family) {
        loadingDialog.setText(getResources().getString(R.string.changing_family));
        loadingDialog.show();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if(family == null) {
            editor.putString(Constants.PREF_FAMILY_CODE, null);
            editor.putString(Constants.PREF_FAMILY_NAME, null);
            editor.putString(Constants.PREF_FAMILY_PHOTO, null);
        }
        else {
            editor.putString(Constants.PREF_FAMILY_CODE, family.getId());
            editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
            editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
        }

        editor.apply();
        startActivity(new Intent(FeedActivity.this, MainActivity.class));

    }

}
