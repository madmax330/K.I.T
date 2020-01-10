package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.bss.maxencecoulibaly.familychat.R;

public abstract class ActivationForm implements AppForm {

    private RelativeLayout layout;

    private EditText codeInput;

    private Button myFamiliesBtn;
    private Button confirmBtn;
    private Button newFamilyBtn;

    public ActivationForm(RelativeLayout layout) {
        this.layout = layout;

        initForm();
    }

    public String getCode() {
        // add dash for firebase ids
        return "-" + codeInput.getText().toString();
    }

    private void initForm(){
        codeInput = (EditText) layout.findViewById(R.id.familyCodeInput);
        myFamiliesBtn = (Button) layout.findViewById(R.id.myFamiliesBtn);
        confirmBtn = (Button) layout.findViewById(R.id.confirmBtn);
        newFamilyBtn = (Button) layout.findViewById(R.id.newFamilyBtn);

        myFamiliesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFamiliesClick();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()) {
                    submit();
                }
            }
        });

        newFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewFamilyClick();
            }
        });
    }

    @Override
    public boolean isValid() {
        boolean good = true;

        if(TextUtils.isEmpty(codeInput.getText())) {
            codeInput.setError("Code is required");
            good = false;
        }

        return good;
    }

    @Override
    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    @Override
    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
    }

    public void reset() {
        codeInput.getText().clear();
    }

    public void showFamilies() {
        myFamiliesBtn.setVisibility(Button.VISIBLE);
    }

    public abstract void onNewFamilyClick();
    public abstract void onFamiliesClick();

}
