package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.BottomMenuAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ProfileAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.MapProfilesDialog;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity
        implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    // Constant variables
    private static final String TAG = "MapActivity";

    // View variables
    private Toolbar mActionBar;

    private LinearLayoutManager mLinearLayoutManager;

    private TextView mAddLocationView;

    private LoadingDialog loadingDialog;
    private MapProfilesDialog mapProfilesDialog;

    private BottomMenuAdapter bottomMenuAdapter;

    // Program variables
    private String familyCode;

    private String mUid;

    private GoogleMap mMap;

    private List<String> mLocations;

    private List<Profile> mProfileList;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_profiles));
        bottomMenuAdapter = new BottomMenuAdapter((LinearLayout) findViewById(R.id.tabBar), "map", this);

        mAddLocationView = (TextView) findViewById(R.id.addLocationView);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapProfilesDialog = new MapProfilesDialog((RelativeLayout) findViewById(R.id.mapProfilesLayout));
        mLinearLayoutManager = new LinearLayoutManager(this);
        mapProfilesDialog.recyclerView.setLayoutManager(mLinearLayoutManager);

        // Initialize program variables
        profileAdapter = new ProfileAdapter(this, "left") {
            @Override
            public void onItemClick(ProfileViewHolder viewHolder, Profile profile) {
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                intent.putExtra(Constants.EXTRA_USER_ID, viewHolder.getUid());
                intent.putExtra(Constants.EXTRA_RETURN_SCREEN, "map");
                startActivity(intent);
            }
        };

        mProfileList = new ArrayList<Profile>();
        mLocations = new ArrayList<String>();

        mapProfilesDialog.recyclerView.setAdapter(profileAdapter);

        loadingDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void loadProfiles() {
        DatabaseReference profileRef = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Profile temp = snapshot.getValue(Profile.class);
                        temp.setId(snapshot.getKey());
                        if(temp.getCity() != null && temp.getCountry() != null) {
                            String location = (temp.getCity() + ", " + temp.getCountry()).toLowerCase().trim();
                            if(!mLocations.contains(location)) {
                                mLocations.add(location);
                            }
                            if(temp.getId().equals(mUid)) {
                                mAddLocationView.setVisibility(TextView.GONE);
                            }
                            mProfileList.add(temp);
                        }
                    }
                    loadMapLocations();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Could not load user profiles. " + databaseError);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerClickListener(this);
        loadProfiles();
    }

    public void loadMapLocations() {
        Geocoder geocoder = new Geocoder(this);
        for(String loc: mLocations) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(loc, 1);
                for(Address address: addresses) {
                    if(address.hasLatitude() && address.hasLongitude()) {
                        addPoint(mMap, loc, new LatLng(address.getLatitude(), address.getLongitude()));
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "error finding location " + loc + " " + e.toString());
            }
        }
        loadingDialog.hide();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String location = (String) marker.getTag();

        showLocationProfiles(location);

        return true;
    }

    public void showLocationProfiles(String location) {
        String tempLocation = "";

        List<Profile> list = new ArrayList<Profile>();
        for(Profile profile: mProfileList) {
            if(location.contains(profile.getCity().toLowerCase()) && location.contains(profile.getCountry().toLowerCase())) {
                if(TextUtils.isEmpty(tempLocation)) {
                    tempLocation = profile.getCity() + ", " + profile.getCountry();
                }
                list.add(profile);
            }
        }

        profileAdapter.setResults(list);

        mapProfilesDialog.setTitle(getResources().getString(R.string.people_in_location, tempLocation));
        mapProfilesDialog.show();

    }

    public void addPoint(GoogleMap map, String name, LatLng loc) {
        Marker marker = map.addMarker(new MarkerOptions().position(loc));
        marker.setTag(name);
    }

}
