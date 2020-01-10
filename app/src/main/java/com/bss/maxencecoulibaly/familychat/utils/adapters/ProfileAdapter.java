package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView profileNameView;
        TextView profileEmailView;
        CircleImageView profileImageView;
        String mUid;

        public ProfileViewHolder(View v) {
            super(v);
            profileNameView = (TextView) itemView.findViewById(R.id.profileNameView);
            profileEmailView = (TextView) itemView.findViewById(R.id.profileEmailView);
            profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
        }

        public String getUid() {
            return mUid;
        }

    }

    private AppCompatActivity activity;

    private List<Profile> dataSet;
    private List<Profile> profileList;

    private boolean center;

    public abstract void onItemClick(ProfileViewHolder viewHolder, Profile profile);

    public ProfileAdapter(AppCompatActivity activity, String center) {
        this.activity = activity;
        this.center = center.equals("center");

        dataSet = new ArrayList<Profile>();
        profileList = new ArrayList<Profile>();
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        if(center) {
            return new ProfileViewHolder(inflater.inflate(R.layout.item_profile_centered, viewGroup, false));
        }
        else {
            return new ProfileViewHolder(inflater.inflate(R.layout.item_profile, viewGroup, false));
        }

    }

    @Override
    public void onBindViewHolder(final ProfileViewHolder viewHolder, int position) {
        final Profile profile = dataSet.get(position);

        viewHolder.mUid = profile.getId();

        viewHolder.profileNameView.setText(profile.getName());
        viewHolder.profileEmailView.setText(profile.getEmail());
        ImageUtil.loadImage(activity, viewHolder.profileImageView, profile.getPhotoUrl(), true);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(viewHolder, profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void filterContacts(String name) {
        dataSet.clear();
        if (name.isEmpty()) {
            dataSet.addAll(profileList);
        } else {
            name = name.toLowerCase();
            for (Profile p : profileList) {
                if (p.getName().toLowerCase().contains(name)) {
                    dataSet.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setResults(List<Profile> list) {
        dataSet.clear();
        dataSet.addAll(list != null ? list : profileList);
        notifyDataSetChanged();
    }

    public void addProfile(Profile profile) {
        profileList.add(profile);
    }

    public void clear() {
        profileList.clear();
    }
}
