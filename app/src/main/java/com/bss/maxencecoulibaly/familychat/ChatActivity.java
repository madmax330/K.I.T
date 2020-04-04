package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ActionBarAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ChatRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.ConfirmationDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FullScreenImageDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.ChatMessageForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Chat;
import com.bss.maxencecoulibaly.familychat.utils.models.ChatMessage;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "ChatActivity";

    // View variables
    private Toolbar mActionBar;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ScrollView mChatScrollView;

    private TextView mNoResultView;

    private LoadingDialog loadingDialog;
    private FullScreenImageDialog fullScreenImageDialog;
    private ConfirmationDialog confirmationDialog;

    private ChatMessageForm chatMessageForm;

    private ActionBarAdapter actionBarAdapter;

    // Program variables
    private String familyCode;

    private String mChatToastMsg;
    private String mChatId;
    private String mChatUserId;
    private String mChatName;
    private String mChatPhotoUrl;

    private String mLastUserId = "test";

    private String mUid;
    private String mUsername;
    private String mPhotoUrl;

    private Uri mImageUri;
    private ChatMessage mChatMessage;

    private SharedPreferences mSharedPreferences;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private ChatRecyclerAdapter chatRecyclerAdapter;

    private ImageUploader imageUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBarAdapter = new ActionBarAdapter(actionBar);

        mChatToastMsg = getIntent().getStringExtra(Constants.EXTRA_CHAT_TOAST_MSG);
        mChatId = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);
        mChatUserId = getIntent().getStringExtra(Constants.EXTRA_CHAT_USER_ID);
        mChatName = getIntent().getStringExtra(Constants.EXTRA_CHAT_NAME);
        mChatPhotoUrl = getIntent().getStringExtra(Constants.EXTRA_CHAT_PHOTOURL);

        if (mChatToastMsg != null) {
            Toast.makeText(this, mChatToastMsg, Toast.LENGTH_SHORT).show();
        }

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        // Initialize view components
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        fullScreenImageDialog = new FullScreenImageDialog((RelativeLayout) findViewById(R.id.fullImageLayout), this) {
            @Override
            public void onDownloadFail(Exception e) {
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.download_image_fail), Toast.LENGTH_SHORT).show();
                Log.w(TAG, e);
            }
        };
        confirmationDialog = new ConfirmationDialog((RelativeLayout) findViewById(R.id.confirmationLayout));
        confirmationDialog.setTitle(getResources().getString(R.string.delete_chat_verify));

        confirmationDialog.setConfirm(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteChat();
            }
        });

        confirmationDialog.setCancel(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationDialog.hide();
            }
        });

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChatScrollView = (ScrollView) findViewById(R.id.chatScrollView);

        mNoResultView = (TextView) findViewById(R.id.noResultView);

        chatMessageForm = new ChatMessageForm((RelativeLayout) findViewById(R.id.chatMessageForm)) {

            @Override
            public void addImage() {
                if (GeneralUtil.checkCameraPermission(ChatActivity.this) && GeneralUtil.checkStoragePermission(ChatActivity.this)) {
                    Intent pickIntent = new Intent();
                    pickIntent.setType("image/*");
                    pickIntent.setAction(Intent.ACTION_GET_CONTENT);

                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = ImageUtil.createImageFile(ChatActivity.this);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.d(TAG, "FileError: error creating file " + ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mImageUri = FileProvider.getUriForFile(ChatActivity.this,
                                "com.example.android.fileprovider",
                                photoFile);
                        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                        takeIntent.putExtra(Constants.EXTRA_PICK_INTENT, Constants.EXTRA_PICK_INTENT);
                    }
                    String title = getResources().getString(R.string.take_or_select_picture);

                    Intent intent = Intent.createChooser(pickIntent, title);
                    intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeIntent});

                    mNoResultView.setVisibility(TextView.INVISIBLE);
                    startActivityForResult(intent, Constants.REQUEST_IMAGE);
                } else {
                    if (!GeneralUtil.checkStoragePermission(ChatActivity.this)) {
                        GeneralUtil.requestStoragePermission(ChatActivity.this);
                    } else if (!GeneralUtil.checkCameraPermission(ChatActivity.this)) {
                        GeneralUtil.requestCameraPermission(ChatActivity.this);
                    }
                }
            }

            @Override
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.sending_message));
                loadingDialog.show();

                String text = chatMessageForm.getMessage();
                mImageUri = chatMessageForm.getImageUri();
                ChatMessage message = new ChatMessage(
                        mUid, mChatUserId, mUsername, text,
                        null, new Date().getTime(), false
                );
                message.setId(mFirebaseDatabaseReference.child(Constants.MESSAGES_CHILD).child(familyCode).child(mChatId).push().getKey());
                if (mImageUri != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference(Constants.STORAGE_CHATS_CHILD).child(familyCode).child(mUid).child(mChatId).child(message.getId()).child(mImageUri.getLastPathSegment());
                    mChatMessage = message;
                    imageUploader.uploadImage(storageReference, mImageUri, ImageUtil.POST_IMAGE_MAX_SIZE);
                } else {
                    newMessage(message);
                }
            }
        };

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(Uri uri) {
                mChatMessage.setPhotoUrl(uri.toString());
                newMessage(mChatMessage);
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.error_sending_message), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        };

        // Load profile
        actionBarAdapter.setTitle(mChatName);
        actionBarAdapter.loadProfile(mChatPhotoUrl, this);

        mFirebaseDatabaseReference.child(Constants.NOTIFICATIONS_CHILD).child(familyCode).child(mUid).child(Constants.CHAT_NOTIFICATION).child(mChatId).setValue(null);

    }

    @Override
    public void onPause() {
        chatRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChat();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.deleteBtn: {
                confirmationDialog.show();
                return true;
            }

            case android.R.id.home: {
                startActivity(new Intent(ChatActivity.this, ChatsActivity.class));
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri temp = data.getData();
                    if (temp != null) {
                        mImageUri = temp;
                    }
                    chatMessageForm.setImageUri(mImageUri, this);
                    chatMessageForm.setSend(true);
                    Log.d(TAG, "Uri: " + mImageUri.toString());
                } else {
                    chatMessageForm.setImageUri(mImageUri, this);
                    chatMessageForm.setSend(true);
                    Log.d(TAG, "Uri: " + mImageUri.toString());
                }
            }
        }

    }

    private void loadChat() {
        loadingDialog.setText(getResources().getString(R.string.loading_chat));
        loadingDialog.show();

        SnapshotParser<ChatMessage> parser = new SnapshotParser<ChatMessage>() {
            @Override
            public ChatMessage parseSnapshot(DataSnapshot dataSnapshot) {
                ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    msg.setId(dataSnapshot.getKey());
                }
                return msg;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(Constants.MESSAGES_CHILD).child(familyCode).child(mChatId);
        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(messagesRef, parser)
                        .build();

        if (chatRecyclerAdapter != null) {
            chatRecyclerAdapter.stopListening();
        }

        chatRecyclerAdapter = new ChatRecyclerAdapter(options, this) {
            @Override
            public void onImageClick(Drawable drawable) {
                loadingDialog.hide();
                fullScreenImageDialog.show();
                fullScreenImageDialog.setImage(drawable);
            }

            @Override
            public void beforeImageLoad() {
                loadingDialog.setText(getResources().getString(R.string.loading));
                loadingDialog.show();
            }

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
                if (chatRecyclerAdapter.getItemCount() == 0) {
                    mNoResultView.setVisibility(TextView.VISIBLE);
                } else {
                    scrollView();
                    // mMessageRecyclerView.smoothScrollToPosition(chatRecyclerAdapter.getItemCount() - 1);
                    DatabaseReference chatsRef = mFirebaseDatabaseReference.child(Constants.CHATS_CHILD).child(familyCode).child(mUid).child(mChatId);
                    chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mFirebaseDatabaseReference.child(Constants.CHATS_CHILD).child(familyCode).child(mUid).child(mChatId).child("notified").setValue(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            return;
                        }
                    });
                }

            }
        };

        chatRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollView();
                // mMessageRecyclerView.scrollToPosition(itemCount - 1);
            }
        });

        mMessageRecyclerView.setAdapter(chatRecyclerAdapter);

        chatRecyclerAdapter.startListening();
    }

    private void newMessage(final ChatMessage msg) {

        DatabaseReference chatsRef = mFirebaseDatabaseReference.child(Constants.CHATS_CHILD).child(familyCode).child(mUid).child(mChatId);

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // check if chat already exists
                Long time = new Date().getTime();
                if (!dataSnapshot.exists()) {
                    // Create new chats
                    Chat chat1 = new Chat(mChatName, mChatPhotoUrl, mUid, mChatUserId, msg.getMessage(), time, false);
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mUid, mChatId}), chat1);
                    Chat chat2 = new Chat(mUsername, mPhotoUrl, mUid, mChatUserId, msg.getMessage(), time, true);
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mChatUserId, mChatId}), chat2);
                } else {
                    // update latest message for chat1
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mUid, mChatId, "latestMessage"}), msg.getMessage());
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mUid, mChatId, "latestActivity"}), time);

                    // update latest message for chat2
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mChatUserId, mChatId, "latestMessage"}), msg.getMessage());
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mChatUserId, mChatId, "latestActivity"}), time);
                    databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mChatUserId, mChatId, "notified"}), true);
                }

                // create new message
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.MESSAGES_CHILD, familyCode, mChatId, msg.getId()}), msg);

                mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            databaseUpdates.clear();
                            chatMessageForm.reset(ChatActivity.this);
                        } else {
                            Log.w(TAG, "Send message failed for chat: " + mChatId + " for user: " + mUid + ". ", databaseError.toException());
                            Toast.makeText(ChatActivity.this, getResources().getString(R.string.error_sending_message), Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.hide();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.error_creating_chat), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        });
    }

    private void deleteChat() {
        loadingDialog.setText(getResources().getString(R.string.deleting_chat));
        loadingDialog.show();

        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[]{Constants.CHATS_CHILD, familyCode, mUid, mChatId}), null);

        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    databaseUpdates.clear();
                    startActivity(new Intent(ChatActivity.this, ChatsActivity.class));
                } else {
                    Log.w(TAG, "Delete chat failed for chat: " + mChatId + " for user: " + mUid + ". ", databaseError.toException());
                    Toast.makeText(ChatActivity.this, getResources().getString(R.string.error_deleting_chat), Toast.LENGTH_SHORT).show();
                    loadingDialog.hide();
                }
            }
        });
    }

    public void scrollView() {
        mChatScrollView.post(new Runnable() {
            @Override
            public void run() {
                mChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                chatMessageForm.requestFocus();
            }
        });
    }

    public String getUid() {
        return mUid;
    }

    public String getLastUserId() {
        return mLastUserId;
    }

    public void setLastUserId(String id) {
        mLastUserId = id;
    }

}
