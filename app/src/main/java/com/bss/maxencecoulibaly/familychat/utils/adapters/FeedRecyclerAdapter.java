package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.CommentsActivity;
import com.bss.maxencecoulibaly.familychat.FeedActivity;
import com.bss.maxencecoulibaly.familychat.LikesActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class FeedRecyclerAdapter extends FirebaseRecyclerAdapter<Post, FeedRecyclerAdapter.PostViewHolder> {

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImageView;
        TextView userNameView;
        ImageView postImageView;
        TextView postTextView;
        TextView postTimeView;
        TextView likesTextView;
        TextView commentsTextView;

        Button commentsBtn;
        Button likeBtn;

        public boolean liked = false;
        public long likes = 0;

        public PostViewHolder(View v) {
            super(v);
            userImageView = (CircleImageView) itemView.findViewById(R.id.userImageView);
            userNameView = (TextView) itemView.findViewById(R.id.userNameView);
            postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
            postTextView = (TextView) itemView.findViewById(R.id.postTextView);
            postTimeView = (TextView) itemView.findViewById(R.id.postTimeView);
            likesTextView = (TextView) itemView.findViewById(R.id.likesText);
            commentsTextView = (TextView) itemView.findViewById(R.id.commentsText);

            commentsBtn = (Button) itemView.findViewById(R.id.commentsBtn);
            likeBtn = (Button) itemView.findViewById(R.id.likeBtn);
        }

        public void setBackground(Drawable drawable) {
            likeBtn.setBackground(drawable);
        }

        public void setText(String text) {
            likesTextView.setText(text);
        }
    }
    
    private FeedActivity activity;

    private DatabaseReference database;

    public abstract void onImageClick(Drawable drawable);
    public abstract void beforeImageLoad();
    public abstract void onLikeClick(PostViewHolder viewHolder, Post post);
    
    public FeedRecyclerAdapter(FirebaseRecyclerOptions<Post> options, FeedActivity activity, DatabaseReference database) {
        super(options);
        
        this.activity = activity;
        this.database = database;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final PostViewHolder viewHolder, int position, final Post post) {
        final String postId = post.getId();
        if(post.getText() != null) {
            viewHolder.postTextView.setText(post.getText());
        }
        else {
            viewHolder.postTextView.setVisibility(TextView.GONE);
        }
        viewHolder.postTimeView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(post.getTimestamp())));

        if (post.getPhotoUrl() != null) {
            if(post.getThumbnail() != null) {
                ImageUtil.loadImage(activity, viewHolder.postImageView, post.getThumbnail(), false);
            }
            else {
                viewHolder.postImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_image));
                viewHolder.postImageView.setVisibility(ImageView.VISIBLE);
            }
            viewHolder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    beforeImageLoad();
                    Glide.with(activity.getApplicationContext())
                            .load(post.getPhotoUrl())
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
        } else {
            viewHolder.postImageView.setVisibility(ImageView.GONE);
        }

        // Get user profile
        DatabaseReference profileRef = database.child(Constants.PROFILES_CHILD).child(activity.getFamilyCode()).child(post.getUserId());
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    viewHolder.userNameView.setText(profile.getName());
                    ImageUtil.loadImage(activity, viewHolder.userImageView, profile.getPhotoUrl(), true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FeedActivity", "onCancelled: Post user not found for user " + activity.getUid() + " for post " + postId + ". " + databaseError);
            }
        });

        // Get comment count
        DatabaseReference commCountRef = database.child(Constants.POST_COMMENTS_CHILD).child(activity.getFamilyCode()).child(postId);
        commCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.commentsTextView.setText(activity.getResources().getString(R.string.number_comments, dataSnapshot.getChildrenCount()));
                } else {
                    viewHolder.commentsTextView.setText(activity.getResources().getString(R.string.number_comments, 0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FeedActivity", "onCancelled: User comment count not found for user " + activity.getUid() + " for post " + postId + ". " + databaseError);
            }
        });

        // Get like count
        DatabaseReference likeCountRef = database.child(Constants.POST_LIKES_CHILD).child(activity.getFamilyCode()).child(postId);
        likeCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.likes = dataSnapshot.getChildrenCount();
                    viewHolder.likesTextView.setText(activity.getResources().getString(R.string.number_likes, viewHolder.likes));
                } else {
                    viewHolder.likesTextView.setText(activity.getResources().getString(R.string.number_likes, 0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FeedActivity", "onCancelled: User comment count not found for user " + activity.getUid() + " for post " + postId + ". " + databaseError);
            }
        });

        // Check if post was already liked
        DatabaseReference likeRef = database.child(Constants.USER_LIKES_CHILD).child(activity.getFamilyCode()).child(activity.getUid()).child(postId);
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.liked = true;
                    viewHolder.likeBtn.setBackground(activity.getResources().getDrawable(R.drawable.liked));
                } else {
                    viewHolder.likeBtn.setBackground(activity.getResources().getDrawable(R.drawable.like));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FeedActivity", "onCancelled: User like not found for user " + activity.getUid() + " for post " + postId + ". " + databaseError);
            }
        });

        // Comment button action
        viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CommentsActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, postId);
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, activity.getCategory());
                activity.startActivity(intent);
            }
        });

        // Like button action
        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLikeClick(viewHolder, post);
            }
        });

        // Like label action -> view likes
        viewHolder.likesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, LikesActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, post.getId());
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, activity.getCategory());
                activity.startActivity(intent);
            }
        });

        // Comment label action -> view comments
        viewHolder.commentsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CommentsActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, postId);
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, activity.getCategory());
                activity.startActivity(intent);
            }
        });

    }
    
}
