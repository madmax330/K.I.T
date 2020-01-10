package com.bss.maxencecoulibaly.familychat.utils.dialogs;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;

public class FamiliesDialog {

    private AppCompatActivity activity;

    private RelativeLayout layout;

    private TextView title;

    private Button closeBtn;

    public RecyclerView recyclerView;

    public FamiliesDialog(RelativeLayout layout, AppCompatActivity activity) {
        this.activity = activity;
        this.layout = layout;

        initDialog();
    }

    private void initDialog() {
        title = (TextView) layout.findViewById(R.id.dialogTitle);
        closeBtn = (Button) layout.findViewById(R.id.closeDialogBtn);
        recyclerView = (RecyclerView) layout.findViewById(R.id.familiesRecyclerView);

        title.setText(activity.getResources().getString(R.string.my_families));

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });
    }

    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
    }

}
