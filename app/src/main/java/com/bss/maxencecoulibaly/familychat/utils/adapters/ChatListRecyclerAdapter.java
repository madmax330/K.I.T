package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.ChatActivity;
import com.bss.maxencecoulibaly.familychat.ChatsActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Chat;
import com.bss.maxencecoulibaly.familychat.utils.models.GroupChat;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class ChatListRecyclerAdapter extends FirebaseRecyclerAdapter<Chat, ChatListRecyclerAdapter.ChatViewHolder> {

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        CircleImageView chatImageView;
        TextView chatNameView;
        TextView latestMessageView;
        TextView latestActivityView;
        String mChatId;
        String mUid;
        String mUsername;
        String mPhotoUrl;

        public ChatViewHolder(View v) {
            super(v);
            chatImageView = (CircleImageView) itemView.findViewById(R.id.chatImageView);
            chatNameView = (TextView) itemView.findViewById(R.id.chatNameView);
            latestMessageView = (TextView) itemView.findViewById(R.id.latestMessageView);
            latestActivityView = (TextView) itemView.findViewById(R.id.latestActivityView);
        }

    }

    ChatsActivity activity;
    DatabaseReference database;

    public ChatListRecyclerAdapter(FirebaseRecyclerOptions<Chat> options, ChatsActivity activity, DatabaseReference database) {
        super(options);

        this.activity = activity;
        this.database = database;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ChatViewHolder(inflater.inflate(R.layout.item_chat, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final ChatViewHolder viewHolder, int position, final Chat chat) {
        viewHolder.mChatId = chat.getId();
        if (chat.getUser2() == null) {
            // Group chat
            DatabaseReference ref = database.child(Constants.GROUPCHATS_CHILD).child(activity.getFamilyCode()).child(viewHolder.mChatId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        GroupChat groupChat = dataSnapshot.getValue(GroupChat.class);
                        viewHolder.mUid = groupChat.getUser1();
                        if (groupChat.getName() != null) {
                            viewHolder.mUsername = groupChat.getName();
                            viewHolder.chatNameView.setText(groupChat.getName());
                            viewHolder.chatNameView.setVisibility(TextView.VISIBLE);
                        }
                        if (groupChat.getLatestMessage() != null) {
                            viewHolder.latestMessageView.setText(groupChat.getLatestMessage());
                            viewHolder.latestMessageView.setVisibility(TextView.VISIBLE);
                            if(chat.isNotified()) {
                                viewHolder.latestMessageView.setTypeface(null, Typeface.BOLD);
                            }
                        }
                        if (groupChat.getLatestActivity() != null) {
                            viewHolder.latestActivityView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(groupChat.getLatestActivity())));
                            viewHolder.latestActivityView.setVisibility(TextView.VISIBLE);
                        }
                        ImageUtil.loadImage(activity, viewHolder.chatImageView, groupChat.getPhotoUrl(), true);
                        if (groupChat.getPhotoUrl() != null) {
                            viewHolder.mPhotoUrl = groupChat.getPhotoUrl();
                        }

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(activity, ChatActivity.class);
                                intent.putExtra(Constants.EXTRA_CHAT_ID, viewHolder.mChatId);
                                // Pass chat ID, chat user ID, chat user name, chat user photo url
                                intent.putExtra(Constants.EXTRA_CHAT_NAME, viewHolder.mUsername);
                                intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, viewHolder.mPhotoUrl);
                                intent.putExtra(Constants.EXTRA_GROUP_CHAT, "true");
                                // load chat screen
                                activity.startActivity(intent);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("ChatsActivity", "onCancelled: Unable to load group chat: " + viewHolder.mChatId + ". " + databaseError);
                }
            });



        } else {
            viewHolder.mUid = ((chat.getUser1().equals(activity.getUid())) ? chat.getUser2() : chat.getUser1());
            if (chat.getName() != null) {
                viewHolder.mUsername = chat.getName();
                viewHolder.chatNameView.setText(chat.getName());
                viewHolder.chatNameView.setVisibility(TextView.VISIBLE);
            }
            if (chat.getLatestMessage() != null) {
                viewHolder.latestMessageView.setText(chat.getLatestMessage());
                viewHolder.latestMessageView.setVisibility(TextView.VISIBLE);
                if(chat.isNotified()) {
                    viewHolder.latestMessageView.setTypeface(null, Typeface.BOLD);
                }
            }
            else {
                viewHolder.latestMessageView.setText(activity.getResources().getString(R.string.image));
                viewHolder.latestMessageView.setVisibility(TextView.VISIBLE);
                if(chat.isNotified()) {
                    viewHolder.latestMessageView.setTypeface(null, Typeface.BOLD);
                }
            }
            if (chat.getLatestActivity() != null) {
                viewHolder.latestActivityView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(chat.getLatestActivity())));
                viewHolder.latestActivityView.setVisibility(TextView.VISIBLE);
            }

            // Get chat user profile
            DatabaseReference profileRef = database.child(Constants.PROFILES_CHILD).child(activity.getFamilyCode()).child(viewHolder.mUid);
            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Profile profile = dataSnapshot.getValue(Profile.class);
                        viewHolder.chatNameView.setText(profile.getName());
                        ImageUtil.loadImage(activity, viewHolder.chatImageView, profile.getPhotoUrl(), true);
                        if (profile.getPhotoUrl() != null) {
                            viewHolder.mPhotoUrl = profile.getPhotoUrl();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("ChatsActivity", "onCancelled: Chat user not found " + viewHolder.mUid + ". " + databaseError);
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    intent.putExtra(Constants.EXTRA_CHAT_ID, viewHolder.mChatId);
                    // Pass chat ID, chat user ID, chat user name, chat user photo url
                    intent.putExtra(Constants.EXTRA_CHAT_USER_ID, viewHolder.mUid);
                    intent.putExtra(Constants.EXTRA_CHAT_NAME, viewHolder.mUsername);
                    intent.putExtra(Constants.EXTRA_CHAT_PHOTOURL, viewHolder.mPhotoUrl);
                    // load chat screen
                    activity.startActivity(intent);
                }
            });
        }

    }

}
