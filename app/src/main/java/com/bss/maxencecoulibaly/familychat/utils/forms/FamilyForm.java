package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;

public abstract class FamilyForm implements AppForm {

    private RelativeLayout layout;

    private EditText familyNameInput;

    private ImageView imageView;

    private Button addImageBtn;
    private Button createFamilyBtn;
    private Button backBtn;

    private TextView emailLabel;

    private Uri imageUri;

    private AppCompatActivity activity;

    public FamilyForm(RelativeLayout layout, AppCompatActivity activity) {
        this.layout = layout;
        this.activity = activity;

        initForm();
    }

    public abstract void addImageClick();
    public abstract void onBackClick();

    private void initForm() {
        familyNameInput = (EditText) layout.findViewById(R.id.familyNameInput);
        imageView = (ImageView) layout.findViewById(R.id.familyImageView);
        addImageBtn = (Button) layout.findViewById(R.id.addImageBtn);
        createFamilyBtn = (Button) layout.findViewById(R.id.createFamilyBtn);
        backBtn = (Button) layout.findViewById(R.id.backBtn);
        emailLabel = (TextView) layout.findViewById(R.id.emailLabel);

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri == null) {
                    addImageClick();
                }
                else {
                    removeImage();
                }
            }
        });

        createFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()) {
                    submit();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClick();
            }
        });
    }

    @Override
    public boolean isValid() {
        boolean good = true;

        if(TextUtils.isEmpty(familyNameInput.getText())) {
            familyNameInput.setError("Family name is required");
            good = false;
        }

        return good;
    }

    public String getName() {
        return familyNameInput.getText().toString();
    }

    public void setName(String name) {
        familyNameInput.setText(name, TextView.BufferType.EDITABLE);
    }

    @Override
    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    @Override
    public void hide() {
        reset();
        layout.setVisibility(RelativeLayout.GONE);
    }

    public void hideEmailLabel() {
        emailLabel.setVisibility(TextView.GONE);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setEdit() {
        createFamilyBtn.setText(activity.getResources().getString(R.string.save));
    }

    public void setImageUri(Uri uri) {
        imageUri = uri;
        imageView.setImageURI(uri);
        addImageBtn.setText(activity.getResources().getString(R.string.remove_image));
    }

    public void removeImage() {
        imageUri = null;
        imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_avatar));
        addImageBtn.setText(activity.getResources().getString(R.string.add_picture));
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void reset() {
        familyNameInput.getText().clear();
        imageUri = null;
        imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_avatar));
    }

}
