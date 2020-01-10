package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.models.Family;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    // Constant variables
    private static final String TAG = "MainActivity";

    // View variables

    // Program variables
    private String familyCode;
    private String mUid;
    private String mUsername;

    private SharedPreferences mSharedPreferences;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

    }

    @Override
    public void onResume() {
        super.onResume();

        if(checkSignIn()) {

            if(checkActivation()) {
                mUid = mFirebaseUser.getUid();
                mUsername = mFirebaseUser.getDisplayName();

                final SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(familyCode != null) {
                    editor.putString(Constants.PREF_FAMILY_CODE, familyCode);
                }

                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                        }
                        String notificationToken;

                        try {
                            notificationToken = task.getResult().getToken();
                        } catch (NullPointerException e) {
                            notificationToken = null;
                        }

                        editor.putString(Constants.PREF_USER_TOKEN, notificationToken);
                        writeToken(notificationToken);
                        subscribeToTopics();

                        DatabaseReference famRef = mFirebaseDatabaseReference.child(Constants.FAMILIES_CHILD).child(familyCode);
                        famRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Family family = dataSnapshot.getValue(Family.class);
                                editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
                                editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
                                editor.apply();

                                createProfile();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                editor.apply();

                                createProfile();
                            }
                        });

                    }
                });

            }
            else {
                // User hasn't entered a family
                startActivity(new Intent(MainActivity.this, ActivationActivity.class));
            }

        }
        else {
            // User hasn't signed in
            startActivity(new Intent(this, SignInActivity.class));
        }

    }

    public boolean checkActivation() {
        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        if(familyCode == null) {
            // User hasn't entered a family
            return false;
        }
        else {
            return true;
        }
    }

    public boolean checkSignIn() {
        // Set default username is anonymous.
        mUsername = Constants.ANONYMOUS;

        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            return false;
        } else {
            if(mUid == null) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.PREF_USER_ID, mFirebaseUser.getUid());
                editor.putString(Constants.PREF_USER_EMAIL, mFirebaseUser.getEmail());
                editor.apply();
            }
            return true;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void writeToken(final String token) {
        mFirebaseDatabaseReference.child(Constants.USER_TOKENS_CHILD).child(familyCode).child(mUid).setValue(token);
    }

    private void createProfile(){
        DatabaseReference profiles = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUid);
        profiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).child(mUid).setValue(new Profile(
                            mUsername, null, null, mFirebaseUser.getEmail(), null,
                            null, null, null, null, null, null, null, mUid
                    ), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                startActivity(new Intent(MainActivity.this, FeedActivity.class));
                            }
                            else {
                                startActivity(new Intent(MainActivity.this, ActivationActivity.class));
                            }
                        }
                    });
                }
                else {
                    startActivity(new Intent(MainActivity.this, FeedActivity.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
            }
        });
    }

    private void subscribeToTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(familyCode + Constants.POST_TOPIC_CONNECTOR + Constants.POSTS_GENERAL_CATEGORY)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "Successfully subscribed to general topic.");
                        }
                        else {
                            Crashlytics.log("Error subscribing to general topic: " + task.getException());
                        }
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic(familyCode + Constants.POST_TOPIC_CONNECTOR + Constants.POSTS_TRAVEL_CATEGORY)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "Successfully subscribed to travel topic.");
                        }
                        else {
                            Crashlytics.log("Error subscribing to travel topic: " + task.getException());
                        }
                    }
                });;
        FirebaseMessaging.getInstance().subscribeToTopic(familyCode + Constants.POST_TOPIC_CONNECTOR + Constants.POSTS_EVENTS_CATEGORY)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "Successfully subscribed to events topic.");
                        }
                        else {
                            Crashlytics.log("Error subscribing to events topic: " + task.getException());
                        }
                    }
                });;
    }

}
