package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class GroupChatForm implements AppForm {

    private RelativeLayout layout;

    private CircleImageView groupPhoto;

    private EditText nameInput;

    private Button imageBtn;

    private Uri photoUri;

    private AppCompatActivity activity;

    public GroupChatForm(RelativeLayout layout, AppCompatActivity activity) {
        this.layout = layout;
        this.activity = activity;

        initForm();
    }

    private void initForm() {
        groupPhoto = (CircleImageView) layout.findViewById(R.id.groupImageView);
        imageBtn = (Button) layout.findViewById(R.id.addImageBtn);
        nameInput = (EditText) layout.findViewById(R.id.groupNameInput);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GeneralUtil.checkStoragePermission(activity)) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, Constants.REQUEST_IMAGE);
                }
                else {
                    GeneralUtil.requestStoragePermission(activity);
                }
            }
        });
    }

    @Override
    public boolean isValid() {
        boolean good = true;

        if(TextUtils.isEmpty(nameInput.getText())) {
            nameInput.setError(activity.getResources().getString(R.string.name_required));
            good = false;
        }

        return good;
    }

    @Override
    public void hide() {
        return ;
    }

    @Override
    public void show() {
        return ;
    }

    public String getName() {
        return nameInput.getText().toString();
    }

    public void setGroupName(String text) {
        nameInput.setText(text, TextView.BufferType.EDITABLE);
    }

    public CircleImageView getGroupPhoto() {
        return groupPhoto;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setGroupPhoto(Uri uri) {
        photoUri = uri;
        groupPhoto.setImageURI(uri);
    }

    public abstract boolean extraValidation();
}
