package com.bss.maxencecoulibaly.familychat.utils.dialogs;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;

public class LoadingDialog {

    private AppCompatActivity activity;

    private RelativeLayout layout;
    private ProgressBar progressBar;
    private TextView textView;

    public LoadingDialog(RelativeLayout layout, AppCompatActivity activity) {
        this.activity = activity;
        this.layout = layout;
        this.progressBar = layout.findViewById(R.id.progressBar);
        this.textView = layout.findViewById(R.id.progressText);
    }

    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void setText(String text) {
        textView.setText(text);
    }

}
