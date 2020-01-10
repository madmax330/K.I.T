package com.bss.maxencecoulibaly.familychat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.BottomMenuAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ProfileAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "ProfileActivity";

    // View variables
    private CircleImageView mUserPhotoView;
    private ImageView mCoverPhotoView;

    private TextView mNameView;
    private TextView mOccupationView;
    private TextView mPhoneView;
    private TextView mEmailView;
    private TextView mCityView;
    private TextView mCountryView;
    private TextView mDOBView;

    private TextView mSpouseNameView;
    private TextView mFatherNameView;
    private TextView mMotherNameView;

    private TextView mSpouseEmailView;
    private TextView mFatherEmailView;
    private TextView mMotherEmailView;

    private TextView mSpouseNoResultView;
    private TextView mFatherNoResultView;
    private TextView mMotherNoResultView;
    private TextView mSiblingsNoResultView;

    private CircleImageView mSpouseImageView;
    private CircleImageView mFatherImageView;
    private CircleImageView mMotherImageView;

    private RelativeLayout mSpouseLayout;
    private RelativeLayout mFatherLayout;
    private RelativeLayout mMotherLayout;

    private RecyclerView mSiblingsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private Button mAddStaticBtn;
    private Button mEditBtn;
    private Button mMyPostsBtn;
    private Button mSettingsBtn;

    private LoadingDialog loadingDialog;

    private BottomMenuAdapter bottomMenuAdapter;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;
    private String mUserId;
    private String mReturnScreen;
    private Profile mUserProfile;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private ProfileAdapter mSiblingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Initialize program variables
        mUserId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        if(mUserId == null){
            mUserId = mUid;
        }
        mReturnScreen = getIntent().getStringExtra(Constants.EXTRA_RETURN_SCREEN);

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_profile));
        bottomMenuAdapter = new BottomMenuAdapter((LinearLayout) findViewById(R.id.tabBar), "profile", this);

        mUserPhotoView = (CircleImageView) findViewById(R.id.userPhotoView);
        mCoverPhotoView = (ImageView) findViewById(R.id.userCoverPhotoView);
        mSiblingsRecyclerView = (RecyclerView) findViewById(R.id.siblingsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mSiblingsRecyclerView.setLayoutManager(mLinearLayoutManager);

        mEditBtn = (Button) findViewById(R.id.editBtn);
        mMyPostsBtn = (Button) findViewById(R.id.myPostsBtn);
        mAddStaticBtn = (Button) findViewById(R.id.addStaticBtn);
        mSettingsBtn = (Button) findViewById(R.id.settingsBtn);

        mNameView = (TextView) findViewById(R.id.nameView);
        mOccupationView = (TextView) findViewById(R.id.occupationView);
        mPhoneView = (TextView) findViewById(R.id.phoneView);
        mEmailView = (TextView) findViewById(R.id.emailView);
        mCityView = (TextView) findViewById(R.id.cityView);
        mCountryView = (TextView) findViewById(R.id.countryView);
        mDOBView = (TextView) findViewById(R.id.dobView);

        mSpouseLayout = (RelativeLayout) findViewById(R.id.spouseLayout);
        mFatherLayout = (RelativeLayout) findViewById(R.id.fatherLayout);
        mMotherLayout = (RelativeLayout) findViewById(R.id.motherLayout);

        mSpouseNameView = (TextView) findViewById(R.id.spouseNameView);
        mFatherNameView = (TextView) findViewById(R.id.fatherNameView);
        mMotherNameView = (TextView) findViewById(R.id.motherNameView);

        mSpouseEmailView = (TextView) findViewById(R.id.spouseEmailView);
        mFatherEmailView = (TextView) findViewById(R.id.fatherEmailView);
        mMotherEmailView = (TextView) findViewById(R.id.motherEmailView);

        mSpouseNoResultView = (TextView) findViewById(R.id.spouseNoResultView);
        mFatherNoResultView = (TextView) findViewById(R.id.fatherNoResultView);
        mMotherNoResultView = (TextView) findViewById(R.id.motherNoResultView);
        mSiblingsNoResultView = (TextView) findViewById(R.id.siblingsNoResultView);

        mSpouseImageView = (CircleImageView) findViewById(R.id.spouseImageView);
        mFatherImageView = (CircleImageView) findViewById(R.id.fatherImageView);
        mMotherImageView = (CircleImageView) findViewById(R.id.motherImageView);

        // Initialize program variables

        mSiblingAdapter = new ProfileAdapter(this, "center") {
            @Override
            public void onItemClick(ProfileViewHolder viewHolder, Profile profile) {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, viewHolder.getUid()));
            }
        };
        mSiblingsRecyclerView.setAdapter(mSiblingAdapter);

        // To edit profile
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserProfile.getUserId() == null) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                }
                else {
                    if(mUserId.equals(mUid)) {
                        startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                    }
                    else {
                        Toast.makeText(ProfileActivity.this, getResources().getString(R.string.cant_edit_active_profile), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // View my posts
        mMyPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, MyPostsActivity.class));
            }
        });

        // To settings
        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            }
        });

        // New static profile
        mAddStaticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_NEW_STATIC_PROFILE, "true"));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    @Override
    public void onBackPressed() {
        if(mReturnScreen != null) {
            switch (mReturnScreen) {
                case "map":
                    startActivity(new Intent(this, MapActivity.class));
            }
        }
        else {
            startActivity(new Intent(this, ProfilesActivity.class));
        }
    }

    private void loadProfile() {
        loadingDialog.show();

        DatabaseReference profileRef = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUserId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mUserProfile = dataSnapshot.getValue(Profile.class);
                    displayValues(mUserProfile);

                    loadLinkedProfile(mUserProfile.getSpouse(), mSpouseLayout, mSpouseImageView, mSpouseNameView, mSpouseEmailView, mSpouseNoResultView);
                    loadLinkedProfile(mUserProfile.getFather(), mFatherLayout, mFatherImageView, mFatherNameView, mFatherEmailView, mFatherNoResultView);
                    loadLinkedProfile(mUserProfile.getMother(), mMotherLayout, mMotherImageView, mMotherNameView, mMotherEmailView, mMotherNoResultView);

                    if(mUserProfile.getPhotoUrl() != null) {
                        if(mUserId.equals(mUid)) {
                            String photoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, "t");
                            if(!photoUrl.equals(mUserProfile.getPhotoUrl())) {
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString(Constants.PREF_USER_PHOTO_URL, mUserProfile.getPhotoUrl());
                                editor.apply();
                            }
                            String name = mSharedPreferences.getString(Constants.PREF_USER_NAME, "t");
                            if(!name.equals(mUserProfile.getName())) {
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString(Constants.PREF_USER_NAME, mUserProfile.getName());
                                editor.apply();
                            }
                        }
                        ImageUtil.loadImage(ProfileActivity.this, mUserPhotoView, mUserProfile.getPhotoUrl(), true);
                    }

                    if(mUserProfile.getCover_photoUrl() != null) {
                        ImageUtil.loadImage(ProfileActivity.this, mCoverPhotoView, mUserProfile.getCover_photoUrl(), false);
                    }

                    if(!mUserId.equals(mUid)) {
                        mMyPostsBtn.setVisibility(Button.INVISIBLE);
                        mAddStaticBtn.setVisibility(Button.INVISIBLE);
                        mSettingsBtn.setVisibility(Button.INVISIBLE);
                    }
                    if(!mUserId.equals(mUid) && mUserProfile.getUserId() != null) {
                        mEditBtn.setVisibility(Button.INVISIBLE);
                    }

                    // Load siblings
                    loadSiblings();

                }
                else {
                    Log.d(TAG, "profileFailed:" + mUserId);
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(ProfileActivity.this, getResources().getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLinkedProfile(String profileId, final RelativeLayout layout, final CircleImageView imageView, final TextView nameView, final TextView emailView, final TextView noResults) {
        if(profileId != null) {
            DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(profileId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        final Profile profile = dataSnapshot.getValue(Profile.class);
                        profile.setId(dataSnapshot.getKey());
                        nameView.setText(profile.getName());
                        emailView.setText(profile.getEmail());
                        ImageUtil.loadImage(ProfileActivity.this, imageView, profile.getPhotoUrl(), true);

                        layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, profile.getId()));
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Mother set in user profile, but not found. " + databaseError);
                }
            });
            layout.setVisibility(RelativeLayout.VISIBLE);
            noResults.setVisibility(TextView.GONE);
        }
        else {
            layout.setVisibility(RelativeLayout.GONE);
            noResults.setVisibility(TextView.VISIBLE);
        }
    }

    private void loadSiblings() {
        DatabaseReference siblingsRef = mFirebaseDatabaseReference.child(Constants.USER_SIBLING_CHILD).child(familyCode).child(mUserId);
        siblingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getChildrenCount() != 0) {
                        mSiblingsNoResultView.setVisibility(TextView.GONE);
                        mSiblingsRecyclerView.setVisibility(RecyclerView.VISIBLE);
                        final List<Profile> siblings = new ArrayList<Profile>();
                        for(final DataSnapshot snap: dataSnapshot.getChildren()) {
                            // Add sibling profile to list
                            final String key = snap.getKey();
                            DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(key);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot profile) {
                                    if(profile.exists()) {
                                        Profile temp = profile.getValue(Profile.class);
                                        temp.setId(key);
                                        siblings.add(temp);
                                        if(siblings.size() == dataSnapshot.getChildrenCount()) {
                                            mSiblingAdapter.setResults(siblings);
                                            loadingDialog.hide();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled:" + databaseError);
                                }
                            });
                        }
                    }
                    else {
                        mSiblingsNoResultView.setVisibility(TextView.VISIBLE);
                        mSiblingsRecyclerView.setVisibility(RecyclerView.GONE);
                        loadingDialog.hide();
                    }
                }
                else {
                    mSiblingsNoResultView.setVisibility(TextView.VISIBLE);
                    mSiblingsRecyclerView.setVisibility(RecyclerView.GONE);
                    loadingDialog.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
            }
        });
    }

    private void displayValues(Profile profile) {

        mNameView.setText(profile.getName());
        mEmailView.setText(profile.getEmail());
        if(profile.getOccupation() != null) {
            mOccupationView.setText(profile.getOccupation());
        }
        if(profile.getPhone() != null) {
            mPhoneView.setText(profile.getPhone());
        }
        if(profile.getCity() != null) {
            mCityView.setText(profile.getCity());
        }
        if(profile.getCountry() != null) {
            mCountryView.setText(profile.getCountry());
        }
        if(profile.getDateOfBirth() != null) {
            mDOBView.setText(DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(new Date(profile.getDateOfBirth())));
        }
    }

}
