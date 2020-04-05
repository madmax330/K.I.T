package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.ChatActivity;
import com.bss.maxencecoulibaly.familychat.FeedActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.ChatMessage;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.DateFormat;
import java.util.Date;

public abstract class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<ChatMessage, ChatRecyclerAdapter.MessageViewHolder> {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout sCell;
        TextView sMsgView;
        ImageView sMsgImageView;
        TextView sMsgDateView;

        LinearLayout rCell;
        TextView rMsgView;
        ImageView rMsgImageView;
        TextView rMsgDateView;
        TextView msgSenderView;

        public MessageViewHolder(View view) {
            super(view);

            sCell = (LinearLayout) itemView.findViewById(R.id.sCell);
            sMsgView = (TextView) itemView.findViewById(R.id.sMsgView);
            sMsgImageView = (ImageView) itemView.findViewById(R.id.sMsgImageView);
            sMsgDateView = (TextView) itemView.findViewById(R.id.sMsgDateView);

            rCell = (LinearLayout) itemView.findViewById(R.id.rCell);
            rMsgView = (TextView) itemView.findViewById(R.id.rMsgView);
            rMsgImageView = (ImageView) itemView.findViewById(R.id.rMsgImageView);
            rMsgDateView = (TextView) itemView.findViewById(R.id.rMsgDateView);
            msgSenderView = (TextView) itemView.findViewById(R.id.msgSenderView);
        }

    }

    private ChatActivity activity;

    public abstract void onImageClick(Drawable drawable);
    public abstract void beforeImageLoad();

    public ChatRecyclerAdapter(FirebaseRecyclerOptions<ChatMessage> options, ChatActivity activity) {
        super(options);

        this.activity = activity;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, final ChatMessage message) {

        if(message.getUserId().equals(activity.getUid())) {
            viewHolder.sCell.setVisibility(LinearLayout.VISIBLE);
            viewHolder.rCell.setVisibility(LinearLayout.GONE);
            if(message.getMessage() != null) {
                viewHolder.sMsgView.setText(message.getMessage());
                viewHolder.sMsgView.setVisibility(TextView.VISIBLE);
            }
            else {
                viewHolder.sMsgView.setVisibility(TextView.GONE);
            }
            if(message.getPhotoUrl() != null) {
                if(message.getThumbnail() != null) {
                    ImageUtil.loadImage(activity, viewHolder.sMsgImageView, message.getThumbnail(), false);
                }
                else {
                    viewHolder.sMsgImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_image));
                }
                viewHolder.sMsgImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        beforeImageLoad();
                        Glide.with(activity.getApplicationContext())
                                .load(message.getPhotoUrl())
                                .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                        onImageClick(resource);
                                    }

                                    @Override
                                    public void onLoadFailed(Drawable drawable) {
                                        Toast.makeText(activity, activity.getResources().getString(R.string.loading_image_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
            else {
                viewHolder.sMsgImageView.setVisibility(ImageView.GONE);
            }
            viewHolder.sMsgDateView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.getTimestamp())));
            viewHolder.sMsgDateView.setVisibility(TextView.VISIBLE);
        }
        else {
            viewHolder.rCell.setVisibility(LinearLayout.VISIBLE);
            viewHolder.sCell.setVisibility(LinearLayout.GONE);
            if(message.getMessage() != null) {
                viewHolder.rMsgView.setText(message.getMessage());
                viewHolder.rMsgView.setVisibility(TextView.VISIBLE);
            }
            else {
                viewHolder.rMsgView.setVisibility(TextView.GONE);
            }
            if(message.getPhotoUrl() != null) {
                if(message.getThumbnail() != null) {
                    ImageUtil.loadImage(activity, viewHolder.rMsgImageView, message.getThumbnail(), false);
                }
                else {
                    viewHolder.rMsgImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_image));
                }
                viewHolder.rMsgImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        beforeImageLoad();
                        Glide.with(activity.getApplicationContext())
                                .load(message.getPhotoUrl())
                                .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                        onImageClick(resource);
                                    }

                                    @Override
                                    public void onLoadFailed(Drawable drawable) {
                                        Toast.makeText(activity, activity.getResources().getString(R.string.loading_image_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
            else {
                viewHolder.rMsgImageView.setVisibility(ImageView.GONE);
            }
            viewHolder.rMsgDateView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.getTimestamp())));
            viewHolder.rMsgDateView.setVisibility(TextView.VISIBLE);
        }
    }

}
