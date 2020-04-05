package com.bss.maxencecoulibaly.familychat.utils.dialogs;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.images.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class FullScreenImageDialog {

    private RelativeLayout layout;

    private TouchImageView imageView;

    private Button closeBtn;
    private Button downloadBtn;

    private AppCompatActivity activity;

    public FullScreenImageDialog(RelativeLayout layout, AppCompatActivity activity) {
        this.layout = layout;
        this.activity = activity;

        initDialog();
    }

    private void initDialog() {
        imageView = (TouchImageView) layout.findViewById(R.id.fullImageView);
        closeBtn = (Button) layout.findViewById(R.id.closeFullScreenImageBtn);
        downloadBtn = (Button) layout.findViewById(R.id.downloadImageBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GeneralUtil.checkStoragePermission(activity)) {
                    try {
                        //to get the image from the ImageView (say iv)
                        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bitmap = draw.getBitmap();

                        FileOutputStream outStream = null;
                        File sdCard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdCard.getAbsolutePath() + "/KITApp");
                        dir.mkdirs();
                        String fileName = String.format("%d.jpg", System.currentTimeMillis());
                        File outFile = new File(dir, fileName);
                        outStream = new FileOutputStream(outFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        hide();
                        Toast.makeText(activity, activity.getResources().getString(R.string.download_image_success), Toast.LENGTH_SHORT).show();
                    } catch (IOException ex) {
                        onDownloadFail(ex);
                    }
                }
                else {
                    GeneralUtil.requestStoragePermission(activity);
                }
            }
        });
    }

    public void setImage(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void show() {
        layout.setVisibility(RelativeLayout.VISIBLE);
    }

    public void hide() {
        layout.setVisibility(RelativeLayout.GONE);
    }

    public abstract void onDownloadFail(Exception e);

}
