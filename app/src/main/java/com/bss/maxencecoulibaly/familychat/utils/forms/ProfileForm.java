package com.bss.maxencecoulibaly.familychat.utils.forms;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.maxencecoulibaly.familychat.R;
import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class ProfileForm implements AppForm {

    private AppCompatActivity activity;

    private LinearLayout layout;

    private CircleImageView userPhoto;
    private ImageView coverPhoto;

    private EditText nameInput;
    private EditText occupationInput;
    private EditText phoneInput;
    private EditText emailInput;
    private EditText cityInput;
    private EditText countryInput;
    private EditText dobInput;

    private Button fatherBtn;
    private Button motherBtn;
    private Button spouseBtn;
    private Button addSiblingBtn;

    private RelativeLayout spouseLayout;
    private RelativeLayout fatherLayout;
    private RelativeLayout motherLayout;
    private RelativeLayout siblingsLayout;

    private TextView fatherName;
    private TextView motherName;
    private TextView spouseName;

    private Uri photoUri;
    private Uri coverUri;

    private boolean dynamic;

    public ProfileForm(LinearLayout layout, AppCompatActivity activity) {
        this.layout = layout;
        this.activity = activity;

        initForm();
    }

    public abstract void addSiblingAction();

    private void initForm() {

        userPhoto = (CircleImageView) layout.findViewById(R.id.userPhotoView);
        coverPhoto = (ImageView) layout.findViewById(R.id.userCoverPhotoView);

        nameInput = (EditText) layout.findViewById(R.id.nameInput);
        occupationInput = (EditText) layout.findViewById(R.id.occupationInput);
        phoneInput = (EditText) layout.findViewById(R.id.phoneInput);
        emailInput = (EditText) layout.findViewById(R.id.emailInput);
        cityInput = (EditText) layout.findViewById(R.id.cityInput);
        countryInput = (EditText) layout.findViewById(R.id.countryInput);
        dobInput = (EditText) layout.findViewById(R.id.dobInput);

        fatherBtn = (Button) layout.findViewById(R.id.fatherBtn);
        motherBtn = (Button) layout.findViewById(R.id.motherBtn);
        spouseBtn = (Button) layout.findViewById(R.id.spouseBtn);
        addSiblingBtn = (Button) layout.findViewById(R.id.addSiblingBtn);

        spouseLayout = (RelativeLayout) layout.findViewById(R.id.spouseLayout);
        fatherLayout = (RelativeLayout) layout.findViewById(R.id.motherLayout);
        motherLayout = (RelativeLayout) layout.findViewById(R.id.fatherLayout);
        siblingsLayout = (RelativeLayout) layout.findViewById(R.id.siblingLayout);

        fatherName = (TextView) layout.findViewById(R.id.fatherNameView);
        motherName = (TextView) layout.findViewById(R.id.motherNameView);
        spouseName = (TextView) layout.findViewById(R.id.spouseNameView);

        userPhoto.setOnClickListener(new View.OnClickListener() {
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

        coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GeneralUtil.checkStoragePermission(activity)) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, Constants.REQUEST_COVER_IMAGE);
                }
                else {
                    GeneralUtil.requestStoragePermission(activity);
                }
            }
        });

        addSiblingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSiblingAction();
            }
        });

    }

    @Override
    public boolean isValid() {
        boolean good = true;

        if(TextUtils.isEmpty(nameInput.getText())){
            nameInput.setError(activity.getResources().getString(R.string.name_required));
            good = false;
        }
        if(dynamic) {
            if(TextUtils.isEmpty(emailInput.getText())){
                emailInput.setError(activity.getResources().getString(R.string.email_required));
                good = false;
            }
        }
        if(!TextUtils.isEmpty(dobInput.getText())) {
            SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            try {
                Date d=dateFormat.parse(dobInput.getText().toString());
            }
            catch(Exception e) {
                //java.text.ParseException: Unparseable date: Geting error
                dobInput.setError(activity.getResources().getString(R.string.invalid_date));
            }
        }

        return good;
    }

    public void hide() {
        return;
    }

    public void show() {
        return;
    }

    /*

        View functions

     */

    public void loadProfile(Profile profile) {
        nameInput.setText(profile.getName());
        emailInput.setText(profile.getEmail());
        if (profile.getOccupation() != null) {
            occupationInput.setText(profile.getOccupation());
        }
        if (profile.getPhone() != null) {
            phoneInput.setText(profile.getPhone());
        }
        if (profile.getCity() != null) {
            cityInput.setText(profile.getCity());
        }
        if (profile.getCountry() != null) {
            countryInput.setText(profile.getCountry());
        }
        if (profile.getDateOfBirth() != null) {
            SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                dobInput.setText(dateFormat.format(new Date(profile.getDateOfBirth())));
            } catch(Exception e) {
                Log.w("PROFILEFORM:", e);
            }
        }
    }

    public void setLinkedProfile(String type, Profile profile, View.OnClickListener listener) {
        switch (type) {
            case "spouse":
                if(profile != null) {
                    spouseName.setText(profile.getName());
                    spouseBtn.setText(activity.getResources().getString(R.string.minus_sign));
                    spouseBtn.setBackground(activity.getResources().getDrawable(R.drawable.minus));
                }
                else {
                    spouseName.setText(activity.getResources().getString(R.string.na));
                    spouseBtn.setBackground(activity.getResources().getDrawable(R.drawable.plus));
                }
                spouseBtn.setOnClickListener(listener);
                break;
            case "father":
                if(profile != null) {
                    fatherName.setText(profile.getName());
                    fatherBtn.setText(activity.getResources().getString(R.string.minus_sign));
                    fatherBtn.setBackground(activity.getResources().getDrawable(R.drawable.minus));
                }
                else {
                    fatherName.setText(activity.getResources().getString(R.string.na));
                    fatherBtn.setBackground(activity.getResources().getDrawable(R.drawable.plus));
                }

                fatherBtn.setOnClickListener(listener);
                break;
            case "mother":
                if(profile != null) {
                    motherName.setText(profile.getName());
                    motherBtn.setText(activity.getResources().getString(R.string.minus_sign));
                    motherBtn.setBackground(activity.getResources().getDrawable(R.drawable.minus));
                }
                else {
                    motherName.setText(activity.getResources().getString(R.string.na));
                    motherBtn.setBackground(activity.getResources().getDrawable(R.drawable.plus));
                }
                motherBtn.setOnClickListener(listener);
                break;
            default:
                break;
        }
    }

    public void hideSpouse() {
        spouseLayout.setVisibility(RelativeLayout.GONE);
    }

    public void hideFather() {
        fatherLayout.setVisibility(RelativeLayout.GONE);
    }

    public void hideMother() {
        motherLayout.setVisibility(RelativeLayout.GONE);
    }

    public void hideSiblings() {
        siblingsLayout.setVisibility(RelativeLayout.GONE);
    }


    /*

        Utility functions

     */

    public String getName() {
        return nameInput.getText().toString();
    }

    public String getOccupation() {
        return occupationInput.getText().toString();
    }

    public String getPhone() {
        return phoneInput.getText().toString();
    }

    public String getEmail() {
        return emailInput.getText().toString();
    }

    public String getCity() {
        return cityInput.getText().toString().trim();
    }

    public String getCountry() {
        return countryInput.getText().toString().trim();
    }

    public Long getDOB() {
        SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dobInput.getText().toString()).getTime();
        } catch(Exception e) {
            return null;
        }
    }

    public Uri getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(Uri uri) {
        coverUri = uri;
        coverPhoto.setImageURI(uri);
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri uri) {
        photoUri = uri;
        userPhoto.setImageURI(uri);
    }

    public CircleImageView getUserPhoto() {
        return userPhoto;
    }

    public ImageView getCoverPhoto() {
        return coverPhoto;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
