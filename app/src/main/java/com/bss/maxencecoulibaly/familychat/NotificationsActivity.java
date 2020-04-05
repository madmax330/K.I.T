package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.NotificationsRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.models.Chat;
import com.bss.maxencecoulibaly.familychat.utils.models.Family;
import com.bss.maxencecoulibaly.familychat.utils.models.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationsActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "NotificationsActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mNotificationsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private TextView mNoResultView;

    private LoadingDialog loadingDialog;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private NotificationsRecyclerAdapter notificationsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);

        mNotificationsRecyclerView = (RecyclerView) findViewById(R.id.notificationsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mNotificationsRecyclerView.setLayoutManager(mLinearLayoutManager);

        notificationsRecyclerAdapter = new NotificationsRecyclerAdapter(this) {

            @Override
            public void onItemClick(final Notification notification) {
                loadingDialog.setText(getResources().getString(R.string.loading));
                loadingDialog.show();

                switch (notification.getCategory()) {
                    case Constants.NOTIFICATION_CHAT:
                        if (notification.getFamily().equals(familyCode)) {
                            toChatActivity(notification);
                        } else {
                            changeFamily(notification);
                        }
                        break;

                    case Constants.NOTIFICATION_COMMENT: {
                        if (notification.getFamily().equals(familyCode)) {
                            toCommentsActivity(notification);
                        } else {
                            changeFamily(notification);
                        }
                        break;
                    }

                    case Constants.NOTIFICATION_LIKE: {
                        if (notification.getFamily().equals(familyCode)) {
                            toLikesActivity(notification);
                        } else {
                            changeFamily(notification);
                        }
                        break;
                    }
                }
            }
        };

        mNotificationsRecyclerView.setAdapter(notificationsRecyclerAdapter);

        mNoResultView = (TextView) findViewById(R.id.noResultView);

    }


    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        loadingDialog.setText(getResources().getString(R.string.loading_notifications));
        loadingDialog.show();

        DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.NOTIFICATIONS_CHILD).child(familyCode).child(mUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String type = snapshot.getKey();
                        if (type.equals(Constants.CHAT_NOTIFICATION)) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                final Notification notification = snapshot1.getValue(Notification.class);
                                notification.setId(snapshot1.getKey());
                                notificationsRecyclerAdapter.addNotification(notification);
                            }
                        } else {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                final Notification notification = snapshot1.getValue(Notification.class);
                                notification.setId(snapshot1.getKey());
                                notificationsRecyclerAdapter.addNotification(notification);
                            }
                        }
                    }
                    notificationsRecyclerAdapter.notifyDataSetChanged();
                    loadingDialog.hide();
                } else {
                    mNoResultView.setVisibility(TextView.VISIBLE);
                    loadingDialog.hide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingDialog.hide();
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(NotificationsActivity.this, getResources().getString(R.string.error_loading_notifications), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void toChatActivity(final Notification notification) {
        DatabaseReference ref = mFirebaseDatabaseReference.child(Constants.CHATS_CHILD).child(notification.getFamily()).child(notification.getObjectId());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chat.setId(dataSnapshot.getKey());

                    Intent intent = new Intent(NotificationsActivity.this, ChatActivity.class);
                    intent.putExtra(Constants.EXTRA_CHAT_ID, chat.getId());
                    // Pass chat ID, chat user ID, chat user name, chat user photo url
                    intent.putExtra(Constants.EXTRA_CHAT_USER_ID, ((chat.getUser1().equals(mUid)) ? chat.getUser2() : chat.getUser1()));
                    intent.putExtra(Constants.EXTRA_CHAT_NAME, chat.getName());
                    intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, chat.getPhotoUrl());
                    startActivity(intent);

                } else {
                    startActivity(new Intent(NotificationsActivity.this, ChatsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                startActivity(new Intent(NotificationsActivity.this, ChatsActivity.class));
            }
        });
    }

    private void toCommentsActivity(Notification notification) {
        Intent intent = new Intent(NotificationsActivity.this, CommentsActivity.class);
        intent.putExtra(Constants.EXTRA_POST_ID, notification.getObjectId());
        intent.putExtra(Constants.EXTRA_POST_CATEGORY, notification.getExtras());
        startActivity(intent);
    }

    private void toLikesActivity(Notification notification) {
        Intent intent = new Intent(NotificationsActivity.this, LikesActivity.class);
        intent.putExtra(Constants.EXTRA_POST_ID, notification.getObjectId());
        intent.putExtra(Constants.EXTRA_POST_CATEGORY, notification.getExtras());
        startActivity(intent);
    }

    private void changeFamily(final Notification notification) {

        DatabaseReference familyRef = mFirebaseDatabaseReference.child(Constants.FAMILIES_CHILD).child(notification.getFamily());
        familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Family family = dataSnapshot.getValue(Family.class);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(Constants.PREF_FAMILY_CODE, family.getCode());
                    editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
                    editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
                    editor.apply();

                    switch (notification.getCategory()) {
                        case Constants.NOTIFICATION_CHAT:
                            toChatActivity(notification);
                            break;

                        case Constants.NOTIFICATION_COMMENT: {
                            toCommentsActivity(notification);
                            break;
                        }

                        case Constants.NOTIFICATION_LIKE: {
                            toLikesActivity(notification);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NotificationsActivity.this, getResources().getString(R.string.error_opening_notification), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
