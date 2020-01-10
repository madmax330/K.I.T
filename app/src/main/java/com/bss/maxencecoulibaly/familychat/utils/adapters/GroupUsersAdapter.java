package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.GroupDetailsActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupUsersAdapter extends RecyclerView.Adapter<GroupUsersAdapter.GroupChatViewHolder> {

    public static class GroupChatViewHolder extends RecyclerView.ViewHolder {
        TextView profileNameView;
        TextView profileEmailView;
        CircleImageView profileImageView;
        CheckBox profileCheckBox;
        String mUid;

        public GroupChatViewHolder(View v) {
            super(v);
            profileNameView = (TextView) itemView.findViewById(R.id.profileNameView);
            profileEmailView = (TextView) itemView.findViewById(R.id.profileEmailView);
            profileImageView = (CircleImageView) itemView.findViewById(R.id.profileImageView);
            profileCheckBox = (CheckBox) itemView.findViewById(R.id.profileCheckBox);
        }

    }

    private AppCompatActivity activity;

    private List<Profile> dataSet;
    private List<Profile> profileList;
    private ArrayList<String> groupUsers;

    private String id;
    private boolean resultsSet;
    private boolean edit;

    public GroupUsersAdapter(AppCompatActivity activity, String id) {
        this.activity = activity;
        this.id = id;

        dataSet = new ArrayList<Profile>();
        profileList = new ArrayList<Profile>();
    }

    @Override
    public GroupChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new GroupChatViewHolder(inflater.inflate(R.layout.item_group_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final GroupChatViewHolder viewHolder, int position) {
        Profile profile = dataSet.get(position);
        
        viewHolder.mUid = profile.getUserId();

        viewHolder.profileNameView.setText(profile.getName());
        viewHolder.profileEmailView.setText(profile.getEmail());

        ImageUtil.loadImage(activity, viewHolder.profileImageView, profile.getPhotoUrl(), true);

        viewHolder.profileCheckBox.setChecked(groupUsers.contains(viewHolder.mUid));

        // Add new user to group
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(groupUsers.contains(viewHolder.mUid)) {
                    viewHolder.profileCheckBox.setChecked(false);
                    groupUsers.remove(viewHolder.mUid);
                }
                else {
                    viewHolder.profileCheckBox.setChecked(true);
                    groupUsers.add(viewHolder.mUid);
                }
            }
        });

        // Handle checkbox click
        viewHolder.profileCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(groupUsers.contains(viewHolder.mUid)) {
                    viewHolder.profileCheckBox.setChecked(false);
                    groupUsers.remove(viewHolder.mUid);
                }
                else {
                    viewHolder.profileCheckBox.setChecked(true);
                    groupUsers.add(viewHolder.mUid);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void filterContacts(String name) {
        dataSet.clear();
        if(name.isEmpty()) {
            dataSet.addAll(profileList);
        }
        else {
            name = name.toLowerCase();
            for(Profile p: profileList) {
                if(p.getName().toLowerCase().contains(name)) {
                    dataSet.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setResults() {
        if(!resultsSet) {
            dataSet.addAll(profileList);
            resultsSet = true;
            notifyDataSetChanged();
        }
    }

    public void addProfile(Profile profile) {
        profileList.add(profile);
    }
    
    public void setGroupUsers(List<String> users) {
        groupUsers = new ArrayList<String>();
        if(users != null) {
            groupUsers.addAll(users);
            edit = true;
        }
        else {
            groupUsers.add(id);
            edit = false;
        }
    }

    public ArrayList<String> getGroupUsers() {
        return groupUsers;
    }
    
    public boolean isEdit() {
        return edit;
    }
}
