package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.LikesActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.PostLike;
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

public class LikesRecyclerAdapter extends FirebaseRecyclerAdapter<PostLike, LikesRecyclerAdapter.LikeViewHolder> {

    public static class LikeViewHolder extends RecyclerView.ViewHolder {

        TextView likeTextView;
        CircleImageView userImageView;
        TextView likeTimeView;

        public LikeViewHolder(View v) {
            super(v);
            likeTextView = (TextView) itemView.findViewById(R.id.likeTextView);
            userImageView = (CircleImageView) itemView.findViewById(R.id.userImageView);
            likeTimeView = (TextView) itemView.findViewById(R.id.likeTimeView);
        }

    }

    private AppCompatActivity activity;

    private DatabaseReference database;

    private String familyCode;

    public LikesRecyclerAdapter(FirebaseRecyclerOptions<PostLike> options, AppCompatActivity activity, DatabaseReference databaseReference, String familyCode) {
        super(options);

        this.activity = activity;
        this.database = databaseReference;
        this.familyCode = familyCode;
    }

    @Override
    public LikeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new LikeViewHolder(inflater.inflate(R.layout.item_like, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final LikeViewHolder viewHolder, int position, PostLike like) {

        viewHolder.likeTimeView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(like.getTimestamp())));

        DatabaseReference ref = database.child(Constants.PROFILES_CHILD).child(familyCode).child(like.getUserId());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    final Profile user = dataSnapshot.getValue(Profile.class);
                    ImageUtil.loadImage(activity, viewHolder.userImageView, user.getPhotoUrl(), true);
                    viewHolder.likeTextView.setText(activity.getResources().getString(R.string.user_liked_post, user.getName()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("LikesActivity", "onCancelled: Post user not found. " + databaseError);
            }
        });
    }


}
