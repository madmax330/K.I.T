package com.bss.maxencecoulibaly.familychat.utils.dialogs;

import android.widget.RelativeLayout;

public class EditFamilyDialog {

    private RelativeLayout layout;

    public EditFamilyDialog(RelativeLayout layout) {
        this.layout = layout;
    }

    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
    }

}
