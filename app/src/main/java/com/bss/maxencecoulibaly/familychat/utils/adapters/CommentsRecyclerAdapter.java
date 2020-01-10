package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.PostComment;
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

public abstract class CommentsRecyclerAdapter extends FirebaseRecyclerAdapter<PostComment, CommentsRecyclerAdapter.CommentViewHolder> {

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView commentTextView;
        CircleImageView userImageView;
        TextView commentTimeView;
        TextView userNameView;

        public CommentViewHolder(View v) {
            super(v);
            commentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
            userImageView = (CircleImageView) itemView.findViewById(R.id.userImageView);
            commentTimeView = (TextView) itemView.findViewById(R.id.commentTimeView);
            userNameView = (TextView) itemView.findViewById(R.id.userNameView);
        }

    }

    private AppCompatActivity activity;

    private DatabaseReference database;

    private String familyCode;

    public CommentsRecyclerAdapter(FirebaseRecyclerOptions<PostComment> options, AppCompatActivity activity, DatabaseReference database, String familyCode) {
        super(options);

        this.activity = activity;
        this.database = database;
        this.familyCode = familyCode;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new CommentViewHolder(inflater.inflate(R.layout.item_comment, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final CommentViewHolder viewHolder, int position, PostComment comment) {

        if(comment.getComment() != null) {
            viewHolder.commentTextView.setText(comment.getComment());
        }
        viewHolder.commentTimeView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(comment.getTimestamp())));

        DatabaseReference ref = database.child(Constants.PROFILES_CHILD).child(familyCode).child(comment.getUserId());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    final Profile user = dataSnapshot.getValue(Profile.class);
                    ImageUtil.loadImage(activity, viewHolder.userImageView, user.getPhotoUrl(), true);
                    viewHolder.userNameView.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("CommentsActivity", "onCancelled: Post user not found. " + databaseError);
            }
        });

    }

}
