package com.bss.maxencecoulibaly.familychat.utils.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtil {

    public static int COMPRESS_QUALITY = 100;
    public static final int THUMBNAIL_MAX_SIZE = 200;
    public static final int POST_IMAGE_MAX_SIZE = 400;

    public static Bitmap getScaledBitmap(Context context, Uri imageUri, double width, double height) throws FileNotFoundException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);

        options.inSampleSize = calculateInSampleSize(options, Double.valueOf(width).intValue(), Double.valueOf(height).intValue());

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);

    }

    public static void loadImage(Context context, final ImageView img, String url, boolean profile) {

        if (url == null) {
            if (profile) {
                img.setImageDrawable(context.getResources().getDrawable(R.drawable.default_avatar));
            } else {
                img.setImageDrawable(null);
            }
        } else {
            if (url.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(url);
                storageReference.getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    Glide.with(img.getContext())
                                            .load(downloadUrl)
                                            .into(img);
                                } else {
                                    Log.w("ImageUtility", "Getting download url was not successful.",
                                            task.getException());
                                }
                            }
                        });
            } else {
                Glide.with(img.getContext())
                        .load(url)
                        .into(img);
            }
        }
        img.setVisibility(ImageView.VISIBLE);

    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    || (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
