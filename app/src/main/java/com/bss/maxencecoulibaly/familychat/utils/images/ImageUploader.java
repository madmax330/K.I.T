package com.bss.maxencecoulibaly.familychat.utils.images;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ImageUploader {

    private AppCompatActivity activity;

    public ImageUploader(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void uploadImage(final StorageReference reference, Uri uri, int size) {

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);

            bitmap.compress(Bitmap.CompressFormat.JPEG, ImageUtil.COMPRESS_QUALITY, byteArrayOutputStream);

            byte[] data = byteArrayOutputStream.toByteArray();
            UploadTask task = reference.putBytes(data);

            Task<Uri> urlTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()) {
                        onFail(task.getException());
                    }

                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        onSuccess(task.getResult());
                    }
                    else {
                        onFail(task.getException());
                    }
                }
            });

        } catch (IOException ex) {
            onFail(ex);
        }

    }

    public abstract void onSuccess(Uri uri);
    public abstract void onFail(Exception e);

}
