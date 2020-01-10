package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.bss.maxencecoulibaly.familychat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ActionBarAdapter {

    private ActionBar actionBar;

    public ActionBarAdapter(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public void loadProfile(String url, AppCompatActivity activity) {
        RequestOptions options = new RequestOptions();
        options.override(156);
        options.circleCrop();

        if(url != null) {
            Glide.with(activity.getApplicationContext())
                    .load(url)
                    .apply(options)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            actionBar.setIcon(resource);
                        }
                    });
        }
        else {
            actionBar.setIcon(activity.getResources().getDrawable(R.drawable.default_avatar));
        }
    }

    public void setTitle(String text) {
        actionBar.setTitle(text);
    }

}
