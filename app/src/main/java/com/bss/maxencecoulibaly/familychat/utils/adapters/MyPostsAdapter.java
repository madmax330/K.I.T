package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
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
import com.bss.maxencecoulibaly.familychat.LikesActivity;
import com.bss.maxencecoulibaly.familychat.NewPostActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Post;
import com.bss.maxencecoulibaly.familychat.utils.models.PostLike;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {
    
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
        Button editBtn;

        boolean liked = false;
        long likes = 0;
        String postCategory;

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
            editBtn = (Button) itemView.findViewById(R.id.editBtn);
        }

    }

    private AppCompatActivity activity;
    
    private DatabaseReference database;
    
    private String id;
    private String familyCode;
    
    private List<Post> mDataset;

    public abstract void onImageClick(Drawable drawable);
    
    public MyPostsAdapter(AppCompatActivity activity, DatabaseReference database, String id, String familyCode) {
        this.activity = activity;
        this.database = database;
        this.id = id;
        this.familyCode = familyCode;
        
        mDataset = new ArrayList<Post>();
    }

    public void setResults(List<Post> posts) {
        mDataset.clear();
        mDataset.addAll(posts);
        notifyDataSetChanged();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new PostViewHolder(inflater.inflate(R.layout.item_my_post, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final PostViewHolder viewHolder, int position) {
        final Post post = mDataset.get(position);

        final String postId = post.getId();
        viewHolder.postCategory = post.getCategory();
        viewHolder.postTextView.setText(post.getText());
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
                    Glide.with(activity.getApplicationContext())
                            .load(post.getPhotoUrl())
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                    onImageClick(resource);
                                }
                            });
                }
            });
        } else {
            viewHolder.postImageView.setVisibility(ImageView.GONE);
        }

        // Get user profile
        DatabaseReference profileRef = database.child(Constants.PROFILES_CHILD).child(familyCode).child(post.getUserId());
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
                Log.d("MyPostsActivity", "onCancelled: Post user not found for user " + id + " for post " + postId + ". " + databaseError);
            }
        });

        // Get comment count
        DatabaseReference commCountRef = database.child(Constants.POST_COMMENTS_CHILD).child(familyCode).child(postId);
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
                Log.d("MyPostsActivity", "onCancelled: User comment count not found for user " + id + " for post " + postId + ". " + databaseError);
            }
        });

        // Get like count
        DatabaseReference likeCountRef = database.child(Constants.POST_LIKES_CHILD).child(familyCode).child(postId);
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
                Log.d("MyPostsActivity", "onCancelled: User comment count not found for user " + id + " for post " + postId + ". " + databaseError);
            }
        });

        // Check if post was already liked
        DatabaseReference likeRef = database.child(Constants.USER_LIKES_CHILD).child(familyCode).child(id).child(postId);
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
                Log.d("MyPostsActivity", "onCancelled: User like not found for user " + id + " for post " + postId + ". " + databaseError);
            }
        });

        // Comment button action
        viewHolder.commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CommentsActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, postId);
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, post.getCategory());
                intent.putExtra(Constants.EXTRA_MY_POSTS, "true");
                activity.startActivity(intent);
            }
        });

        // Like button action
        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.liked) {
                    database.child(Constants.USER_LIKES_CHILD).child(familyCode).child(id).child(postId).removeValue();
                    database.child(Constants.POST_LIKES_CHILD).child(familyCode).child(postId).child(id).removeValue();
                    viewHolder.liked = false;
                    viewHolder.likes -= 1;
                    viewHolder.likeBtn.setBackground(activity.getResources().getDrawable(R.drawable.like));
                    viewHolder.likesTextView.setText(activity.getResources().getString(R.string.number_likes, viewHolder.likes));
                } else {
                    PostLike like = new PostLike(id, postId, viewHolder.postCategory, post.getUserId(), new Date().getTime());
                    database.child(Constants.POST_LIKES_CHILD).child(familyCode).child(postId)
                            .child(id).setValue(like, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                String key = databaseReference.getKey();
                                Map<String, Object> userLike = new HashMap<String, Object>();
                                userLike.put(key, true);
                                database.child(Constants.USER_LIKES_CHILD).child(familyCode).child(id).child(postId).setValue(userLike);
                                viewHolder.liked = true;
                                viewHolder.likes += 1;
                                viewHolder.likeBtn.setBackground(activity.getResources().getDrawable(R.drawable.liked));
                                viewHolder.likesTextView.setText(activity.getResources().getString(R.string.number_likes, viewHolder.likes));
                            } else {
                                Log.w("MyPostsActivity", "Unable to like post " + postId + " for user " + id + ". ", databaseError.toException());
                                Toast.makeText(activity, activity.getResources().getString(R.string.error_liking_post), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // Like label action -> view likes
        viewHolder.likesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, LikesActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, post.getId());
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, post.getCategory());
                intent.putExtra(Constants.EXTRA_MY_POSTS, "true");
                activity.startActivity(intent);
            }
        });

        // Comment label action -> view comments
        viewHolder.commentsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CommentsActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, postId);
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, post.getCategory());
                intent.putExtra(Constants.EXTRA_MY_POSTS, "true");
                activity.startActivity(intent);
            }
        });

        viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, NewPostActivity.class);
                intent.putExtra(Constants.EXTRA_POST_ID, postId);
                intent.putExtra(Constants.EXTRA_POST_CATEGORY, post.getCategory());
                activity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    
}
