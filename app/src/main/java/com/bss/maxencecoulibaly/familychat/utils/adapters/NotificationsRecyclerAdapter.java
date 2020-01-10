package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.models.Chat;
import com.bss.maxencecoulibaly.familychat.utils.models.Notification;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.NotificationViewHolder> {

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleView;
        TextView textView;
        TextView dateView;

        public NotificationViewHolder(View v) {
            super(v);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            titleView = (TextView) itemView.findViewById(R.id.titleLabel);
            textView = (TextView) itemView.findViewById(R.id.textLabel);
            dateView = (TextView) itemView.findViewById(R.id.dateView);
        }

    }

    private AppCompatActivity activity;

    private List<Notification> dataSet;

    public NotificationsRecyclerAdapter(AppCompatActivity activity) {
        this.activity = activity;

        dataSet = new ArrayList<Notification>();
    }

    public abstract void onItemClick(Notification notification);

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new NotificationViewHolder(inflater.inflate(R.layout.item_notification, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder viewHolder, int position) {
        final Notification notification = dataSet.get(position);

        viewHolder.titleView.setText(notification.getTitle());
        viewHolder.textView.setText(notification.getText());
        viewHolder.dateView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(notification.getDate())));

        if(!notification.isOpened()) {
            viewHolder.titleView.setTypeface(null, Typeface.BOLD);
            viewHolder.textView.setTypeface(null, Typeface.BOLD);
            viewHolder.imageView.setImageTintList(activity.getResources().getColorStateList(R.color.colorPrimary));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(notification);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void addNotification(Notification notification) {
        dataSet.add(notification);
    }
}
