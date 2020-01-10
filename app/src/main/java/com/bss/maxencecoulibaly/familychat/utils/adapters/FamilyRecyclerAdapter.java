package com.bss.maxencecoulibaly.familychat.utils.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.models.UserFamily;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public abstract class FamilyRecyclerAdapter extends FirebaseRecyclerAdapter<UserFamily, FamilyRecyclerAdapter.FamilyViewHolder> {

    public static class FamilyViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView notificationsView;
        TextView codeView;

        public FamilyViewHolder(View view) {
            super(view);

            nameView = (TextView) itemView.findViewById(R.id.familyNameView);
            notificationsView = (TextView) itemView.findViewById(R.id.familyNotificationsView);
            codeView = (TextView) itemView.findViewById(R.id.familyCodeView);
        }

    }

    public FamilyRecyclerAdapter(FirebaseRecyclerOptions<UserFamily> options) {
        super(options);
    }

    @Override
    public FamilyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new FamilyViewHolder(inflater.inflate(R.layout.item_family, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(FamilyViewHolder viewHolder, int position, final UserFamily family) {
        viewHolder.nameView.setText(family.getName());
        viewHolder.codeView.setText(family.getId());

        if(family.isNotifications()) {
            viewHolder.notificationsView.setVisibility(TextView.VISIBLE);
        }
        else {
            viewHolder.notificationsView.setVisibility(TextView.INVISIBLE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(family);
            }
        });
    }

    public abstract void onItemClick(UserFamily family);

}
