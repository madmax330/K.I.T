package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ProfileAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "NewChatActivity";

    // View variables
    private Toolbar mActionBar;

    private SearchView mSearchView;
    private RecyclerView mProfileRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private LoadingDialog loadingDialog;

    // Program variables
    private String familyCode;

    private String mUid;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize firebase database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize view components.
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProfileRecyclerView = (RecyclerView) findViewById(R.id.profilesRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mProfileRecyclerView.setLayoutManager(mLinearLayoutManager);

        profileAdapter = new ProfileAdapter(this, "left") {
            @Override
            public void onItemClick(ProfileViewHolder viewHolder, Profile profile) {
                Intent intent = new Intent(NewChatActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CHAT_ID, getChatKeyFromIds(mUid, viewHolder.getUid()));
                // Pass chat ID, chat user ID, chat user name, chat user photo url
                intent.putExtra(Constants.EXTRA_CHAT_USER_ID, viewHolder.getUid());
                intent.putExtra(Constants.EXTRA_CHAT_NAME, profile.getName());
                intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, profile.getPhotoUrl());
                // load chat screen
                startActivity(intent);
            }
        };

        mProfileRecyclerView.setAdapter(profileAdapter);

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
    public void onDestroy() {
        super.onDestroy();
    }

    public void load_profiles() {
        loadingDialog.setText(getResources().getString(R.string.loading_profiles));
        loadingDialog.show();

        Query profilesQuery = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).orderByChild("name");
        profilesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot profile : dataSnapshot.getChildren()) {
                        Profile temp = profile.getValue(Profile.class);
                        temp.setId(profile.getKey());
                        if (temp.getUserId() != null && !temp.getId().equals(mUid)) {
                            profileAdapter.addProfile(temp);
                        }
                    }
                    profileAdapter.setResults(null);
                    loadingDialog.hide();
                } else {
                    Toast.makeText(NewChatActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(NewChatActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getChatKeyFromIds(String id1, String id2) {
        String[] arr = {id1.replace("-", "").trim(), id2.replace("-", "").trim()};
        Arrays.sort(arr);
        return arr[0] + "-" + arr[1];
    }

}
