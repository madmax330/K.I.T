package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.ChatActivity;
import com.bss.maxencecoulibaly.familychat.NewPostActivity;
import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;

import java.io.File;
import java.io.IOException;

public abstract class PostForm implements AppForm {

    private AppCompatActivity activity;

    private RelativeLayout layout;

    private EditText messageInput;

    private ImageView imageView;

    private Button addImageBtn;
    private Button removeImageBtn;

    private Uri imageUri;

    public PostForm(RelativeLayout layout, AppCompatActivity activity) {
        this.activity = activity;
        this.layout = layout;

        initForm();
    }

    public String getMessage() {
        return TextUtils.isEmpty(messageInput.getText()) ? null : messageInput.getText().toString();
    }

    private void initForm() {
        messageInput = (EditText) layout.findViewById(R.id.postMessageInput);
        imageView = (ImageView) layout.findViewById(R.id.postImageView);
        addImageBtn = (Button) layout.findViewById(R.id.addImageBtn);
        removeImageBtn = (Button) layout.findViewById(R.id.removeImageBtn);

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GeneralUtil.checkCameraPermission(activity) && GeneralUtil.checkStoragePermission(activity)) {
                    Intent pickIntent = new Intent();
                    pickIntent.setType("image/*");
                    pickIntent.setAction(Intent.ACTION_GET_CONTENT);

                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = ImageUtil.createImageFile(activity);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.d("NewPostActivity", "FileError: error creating file " + ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(activity,
                                "com.example.android.fileprovider",
                                photoFile);
                        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        takeIntent.putExtra(Constants.EXTRA_PICK_INTENT, Constants.EXTRA_PICK_INTENT);
                    }
                    String title = activity.getResources().getString(R.string.take_or_select_picture);

                    Intent intent = Intent.createChooser(pickIntent, title);
                    intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeIntent});

                    activity.startActivityForResult(intent, Constants.REQUEST_IMAGE);
                }
                else {
                    if(!GeneralUtil.checkStoragePermission(activity)) {
                        GeneralUtil.requestStoragePermission(activity);
                    } else if(!GeneralUtil.checkCameraPermission(activity)) {
                        GeneralUtil.requestCameraPermission(activity);
                    }
                }
            }
        });

        removeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage();
            }
        });
    }

    @Override
    public boolean isValid() {
        boolean good = true;

        if(TextUtils.isEmpty(messageInput.getText()) && imageUri == null) {
            messageInput.setError(activity.getResources().getString(R.string.post_empty));
            good = false;
        }

        return good;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri uri) {
        if(uri != null) {
            imageUri = uri;
        }
        imageView.setImageURI(imageUri);
        imageView.setVisibility(ImageView.VISIBLE);
    }

    private void removeImage() {
        imageUri = null;
        imageView.setImageURI(null);
        imageView.setVisibility(ImageView.GONE);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setText(String text) {
        messageInput.setText(text, TextView.BufferType.EDITABLE);
    }

    @Override
    public void hide() {
        return ;
    }

    @Override
    public void show() {
        return ;
    }
}
