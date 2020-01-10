package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.GroupUsersAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.GroupChatForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.GroupChat;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailsActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "GroupDetailsActivity";

    // View variables
    private Toolbar mActionBar;

    private SearchView mSearchView;
    private RecyclerView mProfileRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private LoadingDialog loadingDialog;

    private GroupChatForm groupChatForm;

    // Program variables
    private String familyCode;

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private GroupChat mGroupChat;
    private String mChatId;
    private String mChatName;
    private String mChatPhotoUrl;

    private ArrayList<String> mOriginalUsers;

    private SharedPreferences mSharedPreferences;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private GroupUsersAdapter groupUsersAdapter;

    private ImageUploader imageUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
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
        loadingDialog.setText(getResources().getString(R.string.saving_group_chat));

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mProfileRecyclerView = (RecyclerView) findViewById(R.id.profilesRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mProfileRecyclerView.setLayoutManager(mLinearLayoutManager);

        groupChatForm = new GroupChatForm((RelativeLayout) findViewById(R.id.groupChatForm), this) {
            @Override
            public boolean extraValidation() {
                if(groupUsersAdapter.getGroupUsers().size() < 2) {
                    Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.group_2_users), Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }

            @Override
            public void submit() {
                if(isValid() && extraValidation()) {
                    loadingDialog.show();

                    if(groupUsersAdapter.isEdit()) {
                        editGroupChat();
                    }
                    else {
                        createGroupChat();
                    }
                }
            }
        };

        // Initialize program variables
        mChatId = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);
        mChatName = getIntent().getStringExtra(Constants.EXTRA_CHAT_NAME);
        mChatPhotoUrl = getIntent().getStringExtra(Constants.EXTRA_CHAT_PHOTOURL);

        groupUsersAdapter = new GroupUsersAdapter(this, mUid);

        groupUsersAdapter.setGroupUsers(getIntent().getStringArrayListExtra(Constants.EXTRA_GROUP_PARTICIPANTS));

        if(groupUsersAdapter.isEdit()) {
            actionBar.setTitle(getResources().getString(R.string.group_details));
            groupChatForm.setGroupName(mChatName);
            ImageUtil.loadImage(this, groupChatForm.getGroupPhoto(), mChatPhotoUrl, true);

            mOriginalUsers = new ArrayList<>();
            mOriginalUsers.addAll(groupUsersAdapter.getGroupUsers());

        }

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(final Uri uri) {
                mGroupChat.setPhotoUrl(uri.toString());

                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseUpdates.clear();
                            Intent intent = new Intent(GroupDetailsActivity.this, ChatActivity.class);
                            intent.putExtra(Constants.EXTRA_CHAT_TOAST_MSG, getResources().getString(R.string.group_chat_updated));
                            intent.putExtra(Constants.EXTRA_CHAT_ID, mChatId);
                            // Pass chat ID, chat user ID, chat user name, chat user photo url
                            intent.putExtra(Constants.EXTRA_CHAT_NAME, mGroupChat.getName());
                            intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, uri.toString());
                            intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
                            startActivity(intent);
                        }
                        else {
                            Log.w(TAG, "Unable to create group.", databaseError.toException());
                            Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_creating_group_chat), Toast.LENGTH_SHORT).show();
                            loadingDialog.hide();
                        }
                    }
                });


            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_creating_group_chat), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        };

        mProfileRecyclerView.setAdapter(groupUsersAdapter);

        // Handle search view input
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                groupUsersAdapter.filterContacts(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                groupUsersAdapter.filterContacts(s);
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
        loadProfiles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.newGroupBtn: {
                groupChatForm.submit();
                return true;
            }

            case android.R.id.home: {
                if(mChatId != null) {
                    Intent intent = new Intent(GroupDetailsActivity.this, ChatActivity.class);
                    intent.putExtra(Constants.EXTRA_CHAT_ID, mChatId);
                    intent.putExtra(Constants.EXTRA_CHAT_NAME, mChatName);
                    intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, mChatPhotoUrl);
                    intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
                    startActivity(intent);
                    return true;
                }
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(mChatId != null) {
            Intent intent = new Intent(GroupDetailsActivity.this, ChatActivity.class);
            intent.putExtra(Constants.EXTRA_CHAT_ID, mChatId);
            intent.putExtra(Constants.EXTRA_CHAT_NAME, mChatName);
            intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, mChatPhotoUrl);
            intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
            startActivity(intent);
        }
    }

    public void loadProfiles() {
        Query profilesQuery = mFirebaseDatabaseReference.child(Constants.PROFILES_CHILD).child(familyCode).orderByChild("name");
        profilesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot profile: dataSnapshot.getChildren()) {
                        Profile temp = profile.getValue(Profile.class);
                        temp.setId(profile.getKey());
                        if(temp.getUserId() != null && !temp.getId().equals(mUid)) {
                            groupUsersAdapter.addProfile(temp);
                        }
                    }
                    groupUsersAdapter.setResults();
                }
                else {
                    Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_loading_profiles), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    groupChatForm.setGroupPhoto(data.getData());
                    Log.d(TAG, "Uri: " + data.getData().toString());
                }
            }
        }
    }

    private void createGroupChat() {
        final String subject = groupChatForm.getName();
        mGroupChat = new GroupChat(
                mUid, subject, null, null, new Date().getTime(), true
        );

        final String groupKey = mFirebaseDatabaseReference.child(Constants.GROUPCHATS_CHILD).child(familyCode).push().getKey();
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.GROUPCHATS_CHILD, familyCode, groupKey}), mGroupChat);
        for(String user : groupUsersAdapter.getGroupUsers()) {
            databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.GROUPCHATUSERS_CHILD, familyCode, groupKey, user}), true);
            databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.CHATS_CHILD, familyCode, user, groupKey, "notified"}), true);
            databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.CHATS_CHILD, familyCode, user, groupKey, "latestActivity"}), mGroupChat.getLatestActivity());
        }

        if(groupChatForm.getPhotoUri() == null) {
            mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError == null) {
                        databaseUpdates.clear();
                        Intent intent = new Intent(GroupDetailsActivity.this, ChatActivity.class);
                        intent.putExtra(Constants.EXTRA_CHAT_ID, groupKey);
                        intent.putExtra(Constants.EXTRA_CHAT_NAME, subject);
                        intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
                        startActivity(intent);
                    }
                    else {
                        Log.w(TAG, "Unable to create group.", databaseError.toException());
                        Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_creating_group_chat), Toast.LENGTH_SHORT).show();
                        loadingDialog.hide();
                    }
                }
            });
        }
        else {
            mChatId = groupKey;
            StorageReference reference = FirebaseStorage.getInstance()
                    .getReference(Constants.STORAGE_CHATS_CHILD).child(familyCode).child(mUid).child(mChatId)
                    .child(Constants.STORAGE_GROUP_PHOTOS_CHILD).child(groupChatForm.getPhotoUri().getLastPathSegment());
            imageUploader.uploadImage(reference, groupChatForm.getPhotoUri(), ImageUtil.THUMBNAIL_MAX_SIZE);
        }

    }

    private void editGroupChat() {
        mChatName = groupChatForm.getName();

        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.GROUPCHATS_CHILD, familyCode, mChatId, "name"}), mChatName);

        manageGroupMembers();
    }

    private void manageGroupMembers() {
        if(mChatId != null && mChatName != null) {

            if(mOriginalUsers != null) {
                // Remove users that where removed from group chat
                for(String id: mOriginalUsers) {
                    if(!groupUsersAdapter.getGroupUsers().contains(id)){
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.GROUPCHATUSERS_CHILD, familyCode, mChatId, id}), null);
                        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.CHATS_CHILD, familyCode, id, mChatId}), null);
                    }
                }
            }

            // Update group users list
            for(String user: groupUsersAdapter.getGroupUsers()) {
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.GROUPCHATUSERS_CHILD, familyCode, mChatId, user}), true);
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.CHATS_CHILD, familyCode, user, mChatId, "notified"}), true);
            }

            if(groupChatForm.getPhotoUri() != null) {
                StorageReference reference = FirebaseStorage.getInstance()
                        .getReference(Constants.STORAGE_CHATS_CHILD).child(familyCode).child(mUid).child(mChatId)
                        .child(Constants.STORAGE_GROUP_PHOTOS_CHILD).child(groupChatForm.getPhotoUri().getLastPathSegment());
                imageUploader.uploadImage(reference, groupChatForm.getPhotoUri(), ImageUtil.THUMBNAIL_MAX_SIZE);
            }
            else {
                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseUpdates.clear();
                            Intent intent = new Intent(GroupDetailsActivity.this, ChatActivity.class);
                            intent.putExtra(Constants.EXTRA_CHAT_TOAST_MSG, getResources().getString(R.string.group_chat_updated));
                            intent.putExtra(Constants.EXTRA_CHAT_ID, mChatId);
                            // Pass chat ID, chat user ID, chat user name, chat user photo url
                            intent.putExtra(Constants.EXTRA_CHAT_NAME, mChatName);
                            intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, mChatPhotoUrl);
                            intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
                            startActivity(intent);
                        }
                        else {
                            Log.w(TAG, "Unable to create group.", databaseError.toException());
                            Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_creating_group_chat), Toast.LENGTH_SHORT).show();
                            loadingDialog.hide();
                        }
                    }
                });
            }
        }
        else {
            Toast.makeText(GroupDetailsActivity.this, getResources().getString(R.string.error_finding_group_chat), Toast.LENGTH_SHORT).show();
            loadingDialog.hide();
        }
    }

}
