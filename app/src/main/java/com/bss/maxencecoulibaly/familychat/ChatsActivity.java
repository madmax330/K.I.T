package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.adapters.BottomMenuAdapter;
import com.bss.maxencecoulibaly.familychat.utils.adapters.ChatListRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.models.Chat;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatsActivity extends AppCompatActivity {

    // Constant variables
    private static final String TAG = "ChatsActivity";

    // View variables
    private Toolbar mActionBar;

    private RecyclerView mChatRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private TextView mNoResultView;

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

    private ChatListRecyclerAdapter chatListRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUsername = mSharedPreferences.getString(Constants.PREF_USER_NAME, null);
        mPhotoUrl = mSharedPreferences.getString(Constants.PREF_USER_PHOTO_URL, null);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        loadingDialog.setText(getResources().getString(R.string.loading_chats));

        mChatRecyclerView = (RecyclerView) findViewById(R.id.chatsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);

        mNoResultView = (TextView) findViewById(R.id.noResultView);

        bottomMenuAdapter = new BottomMenuAdapter((LinearLayout) findViewById(R.id.tabBar), "chats", this);

    }

    @Override
    public void onPause() {
        chatListRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.newChatBtn:
                startActivity(new Intent(ChatsActivity.this, NewChatActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadChats() {
        loadingDialog.show();

        SnapshotParser<Chat> parser = new SnapshotParser<Chat>() {
            @Override
            public Chat parseSnapshot(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    chat.setId(dataSnapshot.getKey());
                }
                return chat;
            }
        };

        Query query = mFirebaseDatabaseReference.child(Constants.CHATS_CHILD).child(familyCode).child(mUid).orderByChild("latestActivity");
        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(query, parser)
                        .build();

        if(chatListRecyclerAdapter != null) {
            chatListRecyclerAdapter.stopListening();
        }

        chatListRecyclerAdapter = new ChatListRecyclerAdapter(options, this, mFirebaseDatabaseReference) {

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
                if(chatListRecyclerAdapter.getItemCount() == 0) {
                    mNoResultView.setVisibility(TextView.VISIBLE);
                }
            }

        };

        chatListRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatCount = chatListRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added chat.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mChatRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mChatRecyclerView.setAdapter(chatListRecyclerAdapter);

        chatListRecyclerAdapter.startListening();
    }

    public String getUid() {
        return mUid;
    }

    public String getFamilyCode() {
        return familyCode;
    }

}
