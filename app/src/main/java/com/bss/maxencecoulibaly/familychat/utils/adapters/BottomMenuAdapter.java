package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.ChatsActivity;
import com.bss.maxencecoulibaly.familychat.FeedActivity;
import com.bss.maxencecoulibaly.familychat.MapActivity;
import com.bss.maxencecoulibaly.familychat.ProfileActivity;
import com.bss.maxencecoulibaly.familychat.ProfilesActivity;
import com.bss.maxencecoulibaly.familychat.R;

public class BottomMenuAdapter {

    private LinearLayout layout;

    private RelativeLayout mapLayout;
    private ImageView mapImage;
    private TextView mapLabel;

    private RelativeLayout chatsLayout;
    private ImageView chatsImage;
    private TextView chatsLabel;

    private RelativeLayout feedLayout;
    private ImageView feedImage;
    private TextView feedLabel;

    private RelativeLayout directoryLayout;
    private ImageView directoryImage;
    private TextView directoryLabel;

    private RelativeLayout profileLayout;
    private ImageView profileImage;
    private TextView profileLabel;

    private String name;

    private AppCompatActivity activity;

    public BottomMenuAdapter(LinearLayout layout, String name, AppCompatActivity activity) {
        this.layout = layout;
        this.name = name;
        this.activity = activity;

        initMenu();
    }

    private void initMenu() {
        mapLayout = (RelativeLayout) layout.findViewById(R.id.mapTab);
        mapImage = (ImageView) layout.findViewById(R.id.mapIcon);
        mapLabel = (TextView) layout.findViewById(R.id.mapLabel);

        chatsLayout = (RelativeLayout) layout.findViewById(R.id.chatsTab);
        chatsImage = (ImageView) layout.findViewById(R.id.chatsIcon);
        chatsLabel = (TextView) layout.findViewById(R.id.chatsLabel);

        feedLayout = (RelativeLayout) layout.findViewById(R.id.feedTab);
        feedImage = (ImageView) layout.findViewById(R.id.feedIcon);
        feedLabel = (TextView) layout.findViewById(R.id.feedLabel);

        directoryLayout = (RelativeLayout) layout.findViewById(R.id.directoryTab);
        directoryImage = (ImageView) layout.findViewById(R.id.directoryIcon);
        directoryLabel = (TextView) layout.findViewById(R.id.directoryLabel);

        profileLayout = (RelativeLayout) layout.findViewById(R.id.profileTab);
        profileImage = (ImageView) layout.findViewById(R.id.profileIcon);
        profileLabel = (TextView) layout.findViewById(R.id.profileLabel);

        mapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("map")){
                    activity.startActivity(new Intent(activity, MapActivity.class));
                }
            }
        });

        chatsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("chats")){
                    activity.startActivity(new Intent(activity, ChatsActivity.class));
                }
            }
        });

        feedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("feed")){
                    activity.startActivity(new Intent(activity, FeedActivity.class));
                }
            }
        });

        directoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("directory")){
                    activity.startActivity(new Intent(activity, ProfilesActivity.class));
                }
            }
        });

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("profile")){
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                }
            }
        });

        switch (name) {
            case "map":
                mapImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                mapLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
            case "chats":
                chatsImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                chatsLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
            case "feed":
                feedImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                feedLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
            case "directory":
                directoryImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                directoryLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
            case "profile":
                profileImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                profileLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
            default:
                feedImage.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
                feedLabel.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                break;
        }
    }

}
