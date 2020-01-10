package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.EditProfileActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;

import java.util.ArrayList;
import java.util.List;

public abstract class SiblingAdapter extends RecyclerView.Adapter<SiblingAdapter.SiblingViewHolder> {

    public static class SiblingViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        Button linkBtn;
        String mUid;

        public SiblingViewHolder(View v) {
            super(v);
            nameView = (TextView) itemView.findViewById(R.id.nameView);
            linkBtn = (Button) itemView.findViewById(R.id.linkBtn);
        }

    }

    private AppCompatActivity activity;

    private List<Profile> mDataset;

    public SiblingAdapter(AppCompatActivity activity) {
        this.activity = activity;
        mDataset = new ArrayList<Profile>();
    }

    public void setResults(List<Profile> data) {
        mDataset.clear();
        mDataset.addAll(data);
    }

    @Override
    public SiblingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new SiblingViewHolder(inflater.inflate(R.layout.item_sibling, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final SiblingViewHolder viewHolder, int position) {
        Profile profile = mDataset.get(position);

        viewHolder.mUid = profile.getId();
        viewHolder.nameView.setText(profile.getName());
        viewHolder.linkBtn.setText(activity.getResources().getString(R.string.minus_sign));

        viewHolder.linkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnClick(viewHolder.mUid);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public abstract void onBtnClick(String id);

}
