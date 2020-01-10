package com.bss.maxencecoulibaly.familychat.utils.dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;

public class ConfirmationDialog {

    private RelativeLayout layout;

    private TextView title;

    private Button confirmBtn;
    private Button cancelBtn;

    public ConfirmationDialog(RelativeLayout layout) {
        this.layout = layout;

        initLayout();
    }

    private void initLayout() {
        title = (TextView) layout.findViewById(R.id.confirmationTitle);
        confirmBtn = (Button) layout.findViewById(R.id.confirmationConfirmBtn);
        cancelBtn = (Button) layout.findViewById(R.id.confirmationCancelBtn);
    }

    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
    }

    public void setTitle(String text) {
        title.setText(text);
    }

    public void setConfirm(View.OnClickListener listener) {
        confirmBtn.setOnClickListener(null);
        confirmBtn.setOnClickListener(listener);
    }

    public void setCancel(View.OnClickListener listener) {
        cancelBtn.setOnClickListener(null);
        cancelBtn.setOnClickListener(listener);
    }

}
