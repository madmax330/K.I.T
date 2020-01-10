package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilesActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "ProfilesActivity";

    // View variables
    private Toolbar mActionBar;

    private SearchView mSearchView;
    private RecyclerView mProfileRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private LoadingDialog loadingDialog;

    private BottomMenuAdapter bottomMenuAdapter;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        // Initialize view components
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_profiles));

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProfileRecyclerView = (RecyclerView) findViewById(R.id.profilesRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mProfileRecyclerView.setLayoutManager(mLinearLayoutManager);

        bottomMenuAdapter = new BottomMenuAdapter((LinearLayout) findViewById(R.id.tabBar), "directory", this);

        // Load profiles
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        profileAdapter = new ProfileAdapter(this, "left") {
            @Override
            public void onItemClick(ProfileViewHolder viewHolder, Profile profile) {
                startActivity(new Intent(ProfilesActivity.this, ProfileActivity.class).putExtra(Constants.EXTRA_USER_ID, viewHolder.getUid()));
            }
        };

        mProfileRecyclerView.setAdapter(profileAdapter);

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
    public void onResume() {
        super.onResume();
        loadProfiles();
    }

    public void loadProfiles() {
        loadingDialog.show();
        Query profilesQuery = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).orderByChild("name");
        profilesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    profileAdapter.clear();
                    for(DataSnapshot profile: dataSnapshot.getChildren()) {
                        Profile temp = profile.getValue(Profile.class);
                        temp.setId(profile.getKey());
                        profileAdapter.addProfile(temp);
                    }
                    profileAdapter.setResults(null);
                    loadingDialog.hide();
                }
                else {
                    Toast.makeText(ProfilesActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(ProfilesActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
