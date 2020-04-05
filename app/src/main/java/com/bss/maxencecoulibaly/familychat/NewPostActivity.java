package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.ConfirmationDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.PostForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPostActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "NewPostActivity";

    // View variables
    private Toolbar mActionBar;

    private CircleImageView mUserImageView;
    private TextView mUserNameView;

    private LoadingDialog loadingDialog;
    private ConfirmationDialog confirmationDialog;

    private PostForm postForm;

    // Program variables
    private String mUid;
    private String mUsername;
    private String mPhotoUrl;
    private String familyCode;

    private String mPostCategory;

    private String mPostId;
    private Post mPost;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private ImageUploader imageUploader;

    private boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        mPostId = getIntent().getStringExtra(Constants.EXTRA_POST_ID);
        mPostCategory = getIntent().getStringExtra(Constants.EXTRA_POST_CATEGORY);

        if (mPostId != null) {
            edit = true;
            actionBar.setTitle(getResources().getString(R.string.edit_post));
            loadPost();
        }
        else {
            edit = false;
        }

        // Initialize view variables
        mUserImageView = (CircleImageView) findViewById(R.id.userImageView);
        mUserNameView = (TextView) findViewById(R.id.userNameView);

        ImageUtil.loadImage(this, mUserImageView, mPhotoUrl, true);
        mUserNameView.setText(mUsername);

        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);

        confirmationDialog = new ConfirmationDialog((RelativeLayout) findViewById(R.id.confirmationLayout));
        confirmationDialog.setTitle(getResources().getString(R.string.delete_post_verify));

        confirmationDialog.setConfirm(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.setText(getResources().getString(R.string.deleting_post));
                loadingDialog.show();

                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POSTS_CHILD, familyCode, mPostCategory, mPostId}), null);
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USERPOSTS_CHILD, familyCode, mUid, mPostId}), null);

                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseUpdates.clear();
                            startActivity(new Intent(NewPostActivity.this, MyPostsActivity.class));
                        }
                        else {
                            Log.w(TAG, "Unable to delete post.", databaseError.toException());
                            Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_deleting_post), Toast.LENGTH_SHORT).show();
                            loadingDialog.hide();
                        }
                    }
                });

            }
        });

        confirmationDialog.setCancel(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationDialog.hide();
            }
        });

        postForm = new PostForm((RelativeLayout) findViewById(R.id.postForm), this) {
            @Override
            public void submit() {
                if (edit) {
                    updatePost();
                } else {
                    createPost();
                }
            }
        };

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(Uri uri) {
                mPost.setPhotoUrl(uri.toString());

                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseUpdates.clear();
                            if (edit) {
                                startActivity(new Intent(NewPostActivity.this, MyPostsActivity.class));
                            } else {
                                startActivity(new Intent(NewPostActivity.this, FeedActivity.class).putExtra(Constants.EXTRA_POST_CATEGORY, mPostCategory));
                            }
                        }
                        else {
                            Log.w(TAG, "Unable to create post.", databaseError.toException());
                            Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_creating_post), Toast.LENGTH_SHORT).show();
                            loadingDialog.hide();
                        }
                    }
                });
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_creating_post), Toast.LENGTH_SHORT).show();
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    postForm.setImageUri(data.getData());
                    if(data.getData() != null) {
                        Log.d(TAG, "Uri: " + data.getData().toString());
                    }
                }
                else {
                    postForm.setImageUri(null);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_post_menu, menu);
        if (mPostId != null) {
            menu.findItem(R.id.deletePostBtn).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.newPostBtn: {
                if(postForm.isValid()) {
                    postForm.submit();
                }
                return true;
            }

            case R.id.deletePostBtn: {
                confirmationDialog.show();
                return true;
            }

            case android.R.id.home: {
                if (mPostId != null) {
                    startActivity(new Intent(NewPostActivity.this, MyPostsActivity.class));
                    return true;
                } else {
                    super.onOptionsItemSelected(item);
                }
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPostId != null) {
            startActivity(new Intent(NewPostActivity.this, MyPostsActivity.class));
        }
        else {
            super.onBackPressed();
        }
    }

    private void loadPost() {
        DatabaseReference databaseReference = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(mPostCategory).child(mPostId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mPost = dataSnapshot.getValue(Post.class);
                    if(mPost.getPhotoUrl() != null) {
                        ImageUtil.loadImage(NewPostActivity.this, postForm.getImageView(), mPost.getPhotoUrl(), false);
                    }
                    postForm.setText(mPost.getText());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_loading_post), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPost() {
        loadingDialog.setText(getResources().getString(R.string.saving_post));
        loadingDialog.show();

        mPost = new Post(
                mUid, postForm.getMessage(), null, null, new Date().getTime(), mPostCategory
        );
        String postKey = mFirebaseDatabaseReference.child(Constants.POSTS_CHILD).child(familyCode).child(mPostCategory).push().getKey();

        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POSTS_CHILD, familyCode, mPostCategory, postKey}), mPost);
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USERPOSTS_CHILD, familyCode, mUid, postKey, "postId"}), postKey);
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USERPOSTS_CHILD, familyCode, mUid, postKey, "category"}), mPostCategory);

        if(postForm.getImageUri() == null) {
            mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError == null) {
                        databaseUpdates.clear();
                        startActivity(new Intent(NewPostActivity.this, FeedActivity.class).putExtra(Constants.EXTRA_POST_CATEGORY, mPostCategory));
                    }
                    else {
                        Log.w(TAG, "Unable to create post.", databaseError.toException());
                        Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_creating_post), Toast.LENGTH_SHORT).show();
                        loadingDialog.hide();
                    }
                }
            });
        }
        else {
            mPostId = postKey;
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference(Constants.STORAGE_POSTS_CHILD).child(familyCode).child(mUid).child(mPostCategory).child(mPostId).child(postForm.getImageUri().getLastPathSegment());
            imageUploader.uploadImage(storageReference, postForm.getImageUri(), ImageUtil.POST_IMAGE_MAX_SIZE);
        }
    }

    private void updatePost() {
        loadingDialog.setText(getResources().getString(R.string.saving_post));
        loadingDialog.show();

        mPost.setText(postForm.getMessage());
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.POSTS_CHILD, familyCode, mPostCategory, mPostId}), mPost);

        if(postForm.getImageUri() == null) {
            mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError == null) {
                        databaseUpdates.clear();
                        startActivity(new Intent(NewPostActivity.this, MyPostsActivity.class));
                    }
                    else {
                        Log.w(TAG, "Unable to update post.", databaseError.toException());
                        Toast.makeText(NewPostActivity.this, getResources().getString(R.string.error_saving_post), Toast.LENGTH_SHORT).show();
                        loadingDialog.hide();
                    }
                }
            });
        }
        else {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference(Constants.STORAGE_POSTS_CHILD).child(familyCode).child(mUid).child(mPostCategory).child(mPostId).child(postForm.getImageUri().getLastPathSegment());
            imageUploader.uploadImage(storageReference, postForm.getImageUri(), ImageUtil.POST_IMAGE_MAX_SIZE);
        }
    }

}
