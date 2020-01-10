package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ProfileAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LinkProfileActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "ProfilesActivity";

    // View variables
    private Toolbar mActionBar;

    private SearchView mSearchView;
    private RecyclerView mProfileRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private LoadingDialog loadingDialog;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private String mUserId;
    private String mLinkType;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_profile);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize firebase database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        // Initialize view components.
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_profiles));

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProfileRecyclerView = (RecyclerView) findViewById(R.id.profilesRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mProfileRecyclerView.setLayoutManager(mLinearLayoutManager);

        // Initialize program variables
        profileAdapter = new ProfileAdapter(this, "left") {
            @Override
            public void onItemClick(ProfileViewHolder viewHolder, Profile profile) {
                if (mUserId != null && mLinkType != null) {

                    if (mLinkType.equals("sibling")) {
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USER_SIBLING_CHILD, familyCode, mUserId, viewHolder.getUid()}), true);
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USER_SIBLING_CHILD, familyCode, viewHolder.getUid(), mUserId}), true);

                        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError == null) {
                                    databaseUpdates.clear();
                                    startActivity(new Intent(LinkProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                                }
                                else {
                                    Log.w(TAG, "Unable link profile", databaseError.toException());
                                    Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.error_linking_profile), Toast.LENGTH_SHORT).show();
                                    loadingDialog.hide();
                                }
                            }
                        });

                    } else if (mLinkType.equals("spouse")) {
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.PROFILES_CHILD, familyCode, mUserId, mLinkType}), viewHolder.getUid());
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.PROFILES_CHILD, familyCode, viewHolder.getUid(), mLinkType}), mUserId);

                        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError == null) {
                                    databaseUpdates.clear();
                                    startActivity(new Intent(LinkProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                                }
                                else {
                                    Log.w(TAG, "Unable link profile", databaseError.toException());
                                    Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.error_linking_profile), Toast.LENGTH_SHORT).show();
                                    loadingDialog.hide();
                                }
                            }
                        });

                    } else if (mLinkType.equals("father") || mLinkType.equals("mother")) {
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.PROFILES_CHILD, familyCode, mUserId, mLinkType}), viewHolder.getUid());

                        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError == null) {
                                    databaseUpdates.clear();
                                    startActivity(new Intent(LinkProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                                }
                                else {
                                    Log.w(TAG, "Unable link profile", databaseError.toException());
                                    Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.error_linking_profile), Toast.LENGTH_SHORT).show();
                                    loadingDialog.hide();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.profile_link_not_found), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.child_profile_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mProfileRecyclerView.setAdapter(profileAdapter);

        mUserId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        mLinkType = getIntent().getStringExtra(Constants.EXTRA_LINK_PROFILE_TYPE);

        // Handle search view input
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                profileAdapter.filterContacts(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                profileAdapter.filterContacts(s);
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        load_profiles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                startActivity(new Intent(LinkProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LinkProfileActivity.this, EditProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, mUserId));
    }

    public void load_profiles() {
        loadingDialog.show();

        Query profilesQuery = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).orderByChild("name");
        profilesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    profileAdapter.clear();
                    for(DataSnapshot profile: dataSnapshot.getChildren()) {
                        if(!profile.getKey().equals(mUid)) {
                            Profile temp = profile.getValue(Profile.class);
                            temp.setId(profile.getKey());
                            profileAdapter.addProfile(temp);
                        }
                    }
                    profileAdapter.setResults(null);
                    loadingDialog.hide();
                }
                else {
                    Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(LinkProfileActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
