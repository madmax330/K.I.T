package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.bss.maxencecoulibaly.familychat.R;

public abstract class PostCommentForm implements AppForm {

    private RelativeLayout layout;

    private EditText commentInput;

    private Button sendBtn;

    public PostCommentForm(RelativeLayout layout) {
        this.layout = layout;

        initForm();
    }

    public String getComment() {
        return commentInput.getText().toString();
    }

    private void initForm() {
        commentInput = (EditText) layout.findViewById(R.id.commentInput);
        sendBtn = (Button) layout.findViewById(R.id.sendBtn);
        sendBtn.setEnabled(false);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()) {
                    submit();
                }
            }
        });

        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendBtn.setEnabled(true);
                } else {
                    sendBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean isValid() {
        boolean good = true;
        if(TextUtils.isEmpty(commentInput.getText())) {
            commentInput.setError("Comment cannot be empty");
            good = false;
        }

        return good;
    }

    public void reset() {
        commentInput.getText().clear();
    }

    @Override
    public void show() {
        return ;
    }

    @Override
    public void hide() {
        return ;
    }
}
