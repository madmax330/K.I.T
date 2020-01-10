package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class ChatMessageForm implements AppForm {

    private RelativeLayout layout;

    private EditText messageInput;

    private Button imageBtn;
    private Button sendBtn;

    Uri imageUri;

    public ChatMessageForm(RelativeLayout layout) {
        this.layout = layout;

        initForm();
    }

    public String getMessage() {
        if(TextUtils.isEmpty(messageInput.getText())) {
            return null;
        }
        else {
            return messageInput.getText().toString();
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    private void initForm() {
        messageInput = (EditText) layout.findViewById(R.id.messageInput);
        imageBtn = (Button) layout.findViewById(R.id.addImageBtn);
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

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImage();
            }
        });

        messageInput.addTextChangedListener(new TextWatcher() {
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

    public boolean isValid() {
        boolean good = true;
        if(TextUtils.isEmpty(messageInput.getText()) && imageUri == null) {
            messageInput.setError("You must have some text or an image to send a message");
            good = false;
        }

        return good;
    }

    public void setImageUri(Uri imageUri, AppCompatActivity activity) {
        this.imageUri = imageUri;
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            Drawable drawable = Drawable.createFromStream(inputStream, imageUri.toString() );
            imageBtn.setBackground(drawable);
        } catch (FileNotFoundException e) {
            Toast.makeText(activity, "Error loading image file", Toast.LENGTH_SHORT).show();
        }
    }

    public void setSend(boolean bool) {
        sendBtn.setEnabled(bool);
    }

    public void reset(AppCompatActivity activity) {
        messageInput.getText().clear();
        imageBtn.setBackground(activity.getResources().getDrawable(R.drawable.image));
        imageUri = null;
    }

    public void requestFocus() {
        messageInput.requestFocus();
    }

    public void hide() {
        return;
    }

    public void show() {
        return;
    }

    public abstract void addImage();

}
