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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.SiblingAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.ProfileForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "EditProfileActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mSiblingsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private Button mSaveProfileBtn;

    private LoadingDialog loadingDialog;
    private ProfileForm profileForm;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;
    private String mUserId;
    private Profile mUserProfile;

    private SiblingAdapter mSiblingAdapter;

    private boolean mNewStaticProfile;
    private boolean flagBoth;
    private boolean flagPhoto;
    private boolean flagCover;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private ImageUploader imageUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
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

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);

        mSiblingsRecyclerView = (RecyclerView) findViewById(R.id.siblingsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mSiblingsRecyclerView.setLayoutManager(mLinearLayoutManager);

        mSaveProfileBtn = (Button) findViewById(R.id.saveProfileBtn);

        mSaveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(profileForm.isValid()) {
                    profileForm.submit();
                }
            }
        });

        profileForm = new ProfileForm((LinearLayout) findViewById(R.id.profileForm), this) {

            @Override
            public void addSiblingAction() {
                Intent intent = new Intent(EditProfileActivity.this, LinkProfileActivity.class);
                intent.putExtra(Constants.EXTRA_LINK_PROFILE_TYPE, "sibling");
                intent.putExtra(Constants.EXTRA_USER_ID, mUserId);
                startActivity(intent);
                finish();
            }

            @Override
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.saving_profile));
                loadingDialog.show();
                saveProfile();
            }
        };

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "flagBoth: " + flagBoth + " flagPhoto: " + flagPhoto + " flagCover: " + flagCover);
                if(flagBoth) {
                    if(flagCover) {
                        mUserProfile.setCover_photoUrl(uri.toString());
                        setProfile();
                    }
                    else {
                        mUserProfile.setPhotoUrl(uri.toString());
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(Constants.PREF_USER_PHOTO_URL, mUserProfile.getPhotoUrl());
                        editor.apply();
                        loadImages(null);
                    }
                }
                else {
                    if(flagPhoto) {
                        mUserProfile.setPhotoUrl(uri.toString());
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(Constants.PREF_USER_PHOTO_URL, mUserProfile.getPhotoUrl());
                        editor.apply();
                    }
                    else {
                        mUserProfile.setCover_photoUrl(uri.toString());
                    }
                    setProfile();
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.error_updating_images), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        };

        //Initialize program variables
        mNewStaticProfile = !(getIntent().getStringExtra(Constants.EXTRA_NEW_STATIC_PROFILE) == null);

        mUserId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        if (mUserId == null) {
            mUserId = mUid;
        }
        mSiblingAdapter = new SiblingAdapter(this) {
            @Override
            public void onBtnClick(String uid) {
                mFirebaseDatabaseReference.child(Constants.USER_SIBLING_CHILD).child(familyCode).child(mUserId).child(uid).removeValue();
                mFirebaseDatabaseReference.child(Constants.USER_SIBLING_CHILD).child(familyCode).child(uid).child(mUserId).removeValue();
                startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
            }
        };

        mSiblingsRecyclerView.setAdapter(mSiblingAdapter);

        flagBoth = false;
        flagPhoto = false;
        flagCover = false;

        if (mNewStaticProfile) {
            actionBar.setTitle(getResources().getString(R.string.new_static_profile));
            mUserId = null;
            mUserProfile = new Profile();

            profileForm.hideSpouse();
            profileForm.hideFather();
            profileForm.hideMother();
            profileForm.hideSiblings();

            mSaveProfileBtn.setText(getResources().getString(R.string.save_profile));
        } else {
            loadProfile();
        }
    }

    @Override

    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                if (mNewStaticProfile) {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUid));
                } else {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNewStaticProfile) {
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUid));
        } else {
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    profileForm.setPhotoUri(data.getData());
                    Log.d(TAG, "Uri: " + data.getData().toString());
                }
            }
        } else if (requestCode == Constants.REQUEST_COVER_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    profileForm.setCoverUri(data.getData());
                    Log.d(TAG, "Cover Uri: " + data.getData().toString());
                }
            }
        }
    }

    private void loadProfile() {
        loadingDialog.setText(getResources().getString(R.string.loading_profile));
        loadingDialog.show();

        DatabaseReference profileRef = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUserId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUserProfile = dataSnapshot.getValue(Profile.class);
                    profileForm.loadProfile(mUserProfile);

                    loadLinkedProfile(mUserProfile.getFather(), "father");
                    loadLinkedProfile(mUserProfile.getMother(), "mother");
                    loadLinkedProfile(mUserProfile.getSpouse(), "spouse");

                    ImageUtil.loadImage(EditProfileActivity.this, profileForm.getUserPhoto(), mUserProfile.getPhotoUrl(), true);
                    if (mUserProfile.getCover_photoUrl() != null) {
                        ImageUtil.loadImage(EditProfileActivity.this, profileForm.getCoverPhoto(), mUserProfile.getCover_photoUrl(), true);
                    }
                    loadSiblings();

                    loadingDialog.hide();
                    profileForm.setDynamic(mUserProfile.getUserId() != null);

                } else {
                    loadingDialog.hide();
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingDialog.hide();
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLinkedProfile(final String profileId, final String type) {
        if(profileId != null) {
            DatabaseReference reference = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(profileId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final Profile profile = dataSnapshot.getValue(Profile.class);
                        profile.setId(profileId);
                        profileForm.setLinkedProfile(type, profile, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUserId).child(type).removeValue();
                                if(type.equals("spouse")) {
                                    mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(profile.getId()).child(type).removeValue();
                                }
                                startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Mother set in user profile, but not found. " + databaseError);
                }
            });
        }
        else {
            profileForm.setLinkedProfile(type, null, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(EditProfileActivity.this, LinkProfileActivity.class);
                    intent.putExtra(Constants.EXTRA_LINK_PROFILE_TYPE, type);
                    intent.putExtra(Constants.EXTRA_USER_ID, mUserId);
                    startActivity(intent);
                }
            });
        }

    }

    private void loadSiblings() {
        // Load all user siblings
        final List<Profile> siblings = new ArrayList<Profile>();
        Query siblingsQuery = mFirebaseDatabaseReference.child(Constants.USER_SIBLING_CHILD).child(familyCode).child(mUserId);
        siblingsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(final DataSnapshot profile: dataSnapshot.getChildren()) {
                        // Add sibling profile to list
                        DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(profile.getKey());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    Profile temp = dataSnapshot.getValue(Profile.class);
                                    temp.setId(profile.getKey());
                                    siblings.add(temp);
                                    mSiblingAdapter.setResults(siblings);
                                    mSiblingAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled:" + databaseError);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
            }
        });
    }

    private void saveProfile() {
        loadingDialog.setText(getResources().getString(R.string.saving_profile));
        loadingDialog.show();

        mUserProfile.setName(profileForm.getName());
        mUserProfile.setOccupation(TextUtils.isEmpty(profileForm.getOccupation()) ? null : profileForm.getOccupation());
        mUserProfile.setPhone(TextUtils.isEmpty(profileForm.getPhone()) ? null : profileForm.getPhone());
        mUserProfile.setEmail(profileForm.getEmail());
        mUserProfile.setCity(TextUtils.isEmpty(profileForm.getCity()) ? null : profileForm.getCity());
        mUserProfile.setCountry(TextUtils.isEmpty(profileForm.getCountry()) ? null : profileForm.getCountry());
        mUserProfile.setDateOfBirth(profileForm.getDOB());
        if (!mNewStaticProfile) {
            loadImages(mUserId);
        } else {
            loadImages(mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).push().getKey());
        }
    }

    private void loadImages(String key) {
        mUserId = key != null ? key : mUserId;
        StorageReference photoReference = FirebaseStorage.getInstance()
                .getReference(Constants.STORAGE_PROFILES_CHILD).child(familyCode).child(mUserId).child(Constants.STORAGE_PROFILE_PHOTOS_CHILD);
        StorageReference coverReference = FirebaseStorage.getInstance()
                .getReference(Constants.STORAGE_PROFILES_CHILD).child(familyCode).child(mUserId).child(Constants.STORAGE_COVER_PHOTOS_CHILD);
        flagBoth = profileForm.getPhotoUri() != null && profileForm.getCoverUri() != null;
        Log.d(TAG, "flagBoth: " + flagBoth + " flagPhoto: " + flagPhoto + " flagCover: " + flagCover);
        if(flagBoth) {
            if(!flagPhoto) {
                flagPhoto = true;
                imageUploader.uploadImage(photoReference.child(profileForm.getPhotoUri().getLastPathSegment()), profileForm.getPhotoUri(), ImageUtil.THUMBNAIL_MAX_SIZE);
            }
            else {
                flagCover = true;
                imageUploader.uploadImage(coverReference.child(profileForm.getCoverUri().getLastPathSegment()), profileForm.getCoverUri(), ImageUtil.POST_IMAGE_MAX_SIZE);
            }
        }
        else if(profileForm.getPhotoUri() != null) {
            flagPhoto = true;
            imageUploader.uploadImage(photoReference.child(profileForm.getPhotoUri().getLastPathSegment()), profileForm.getPhotoUri(), ImageUtil.THUMBNAIL_MAX_SIZE);
        }
        else if(profileForm.getCoverUri() != null) {
            flagCover = true;
            imageUploader.uploadImage(coverReference.child(profileForm.getCoverUri().getLastPathSegment()), profileForm.getCoverUri(), ImageUtil.POST_IMAGE_MAX_SIZE);
        }
        else{
            setProfile();
        }
    }

    private void setProfile() {
        mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUserId).setValue(mUserProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null) {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                }
                else {
                    Log.w(TAG, "Update profile failed for user: " + mUid + ". ", databaseError.toException());
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.error_saving_profile), Toast.LENGTH_SHORT).show();
                    loadingDialog.hide();
                }
            }
        });
    }

}
