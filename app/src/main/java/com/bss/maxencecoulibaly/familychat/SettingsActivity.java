package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.FamilyRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.ConfirmationDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.EditFamilyDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FamiliesDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.FamilyForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.UserFamily;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SettingsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    // Static variables
    public static final String TAG = "SettingsActivity";

    // View variables
    private Toolbar mActionBar;

    private LinearLayoutManager mLinearLayoutManager;

    private TextView mCodeView;
    private TextView mNameView;
    private ImageView mImageView;

    private Button mEditFamilyBtn;
    private Button mChangeFamilyBtn;
    private Button mJoinFamilyBtn;
    private Button mQuitFamilyBtn;
    private Button mSignOutBtn;

    private LoadingDialog loadingDialog;
    private ConfirmationDialog confirmationDialog;
    private FamiliesDialog familiesDialog;
    private EditFamilyDialog editFamilyDialog;
    private FamilyForm familyForm;

    // Program variables
    private SharedPreferences mSharedPreferences;
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private FamilyRecyclerAdapter familyRecyclerAdapter;

    private String mUid;
    private String familyCode;
    private String familyName;
    private String familyPhoto;
    private Uri mImageUri;

    private ImageUploader imageUploader;

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        familyCode = mSharedPreferences.getString(Constants.PREF_FAMILY_CODE, null);
        familyName = mSharedPreferences.getString(Constants.PREF_FAMILY_NAME, null);
        familyPhoto = mSharedPreferences.getString(Constants.PREF_FAMILY_PHOTO, null);
        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);

        mActionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Realtime Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Init firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Initialize view variables
        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);
        editFamilyDialog = new EditFamilyDialog((RelativeLayout) findViewById(R.id.familyFormLayout));

        confirmationDialog = new ConfirmationDialog((RelativeLayout) findViewById(R.id.confirmationLayout));
        confirmationDialog.setCancel(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationDialog.hide();
            }
        });

        familyForm = new FamilyForm((RelativeLayout) findViewById(R.id.familyFormLayout), this) {
            @Override
            public void addImageClick() {
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = ImageUtil.createImageFile(SettingsActivity.this);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.d(TAG, "FileError: error creating file " + ex.toString());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mImageUri = FileProvider.getUriForFile(SettingsActivity.this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    takeIntent.putExtra(Constants.EXTRA_PICK_INTENT, Constants.EXTRA_PICK_INTENT);
                }
                String title = getResources().getString(R.string.take_or_select_picture);

                Intent intent = Intent.createChooser(pickIntent, title);
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {takeIntent});

                startActivityForResult(intent, Constants.REQUEST_IMAGE);
            }

            @Override
            public void onBackClick() {
                editFamilyDialog.hide();
            }

            @Override
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.saving_family));
                loadingDialog.show();
                GeneralUtil.hideKeyboard(SettingsActivity.this);

                if(getImageUri() != null) {
                    updateFamily(getImageUri());
                }
                else {
                    updateFamily(null);
                }
            }
        };

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(Uri uri) {
                mImageUri = uri;
                databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.FAMILIES_CHILD, familyCode, Constants.PHOTO_URL_FIELD}), uri.toString());
                saveFamily();
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_updating_family), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        };

        mCodeView = (TextView) findViewById(R.id.codeView);
        mNameView = (TextView) findViewById(R.id.nameView);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mEditFamilyBtn = (Button) findViewById(R.id.editFamilyBtn);
        mChangeFamilyBtn = (Button) findViewById(R.id.changeFamilyBtn);
        mJoinFamilyBtn = (Button) findViewById(R.id.joinFamilyBtn);
        mQuitFamilyBtn = (Button) findViewById(R.id.quitFamilyBtn);
        mSignOutBtn = (Button) findViewById(R.id.signOutBtn);

        familiesDialog = new FamiliesDialog((RelativeLayout) findViewById(R.id.familiesLayout), this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        familiesDialog.recyclerView.setLayoutManager(mLinearLayoutManager);

        mNameView.setText(familyName);
        mCodeView.setText(familyCode.substring(1));
        ImageUtil.loadImage(this, mImageView, familyPhoto, true);

        mEditFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                familyForm.hideEmailLabel();
                familyForm.setEdit();
                familyForm.setName(familyName);
                ImageUtil.loadImage(SettingsActivity.this, familyForm.getImageView(), familyPhoto, true);
                familyForm.show();
            }
        });

        mChangeFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                familiesDialog.show();
            }
        });

        mJoinFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFamily(null);
            }
        });

        mQuitFamilyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationDialog.setTitle(getResources().getString(R.string.quit_family_verify, familyName));

                confirmationDialog.setConfirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(familyRecyclerAdapter.getItemCount() > 0) {
                            quitFamily(familyRecyclerAdapter.getItem(0));
                        }
                        else {
                            quitFamily(null);
                        }
                    }
                });

                confirmationDialog.show();
            }
        });

        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmationDialog.setTitle(getResources().getString(R.string.sign_out_verify));

                confirmationDialog.setConfirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signOut();
                    }
                });

                confirmationDialog.show();

            }
        });

    }

    @Override
    public void onPause() {
        familyRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFamilies();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == Constants.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri temp = data.getData();
                    if(temp != null) {
                        mImageUri = temp;
                    }
                    familyForm.setImageUri(mImageUri);
                    Log.d(TAG, "Uri: " + mImageUri.toString());
                }
            }
        }

    }

    private void loadFamilies() {
        loadingDialog.setText(getResources().getString(R.string.loading_settings));
        loadingDialog.show();

        SnapshotParser<UserFamily> parser = new SnapshotParser<UserFamily>() {
            @NonNull
            @Override
            public UserFamily parseSnapshot(@NonNull DataSnapshot snapshot) {
                UserFamily family = snapshot.getValue(UserFamily.class);
                if(family != null) {
                    family.setId(snapshot.getKey());
                }
                return family;
            }
        };

        DatabaseReference reference = mFirebaseDatabaseReference.child(Constants.USERFAMILIES_CHILD).child(mUid);
        FirebaseRecyclerOptions<UserFamily> options = new FirebaseRecyclerOptions.Builder<UserFamily>().setQuery(reference, parser).build();

        if(familyRecyclerAdapter != null) {
            familyRecyclerAdapter.stopListening();
        }

        familyRecyclerAdapter = new FamilyRecyclerAdapter(options) {
            @Override
            public void onItemClick(UserFamily family) {
                if(family.getId().equals(familyCode)) {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.is_current_family), Toast.LENGTH_SHORT);
                }
                else {
                    changeFamily(family);
                }
            }

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
            }
        };

        familyRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatCount = familyRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added chat.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    familiesDialog.recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        familiesDialog.recyclerView.setAdapter(familyRecyclerAdapter);

        familyRecyclerAdapter.startListening();
    }

    private void changeFamily(UserFamily family) {
        loadingDialog.setText(getResources().getString(R.string.changing_family));
        loadingDialog.show();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if(family == null) {
            editor.putString(Constants.PREF_FAMILY_CODE, null);
            editor.putString(Constants.PREF_FAMILY_NAME, null);
            editor.putString(Constants.PREF_FAMILY_PHOTO, null);
        }
        else {
            editor.putString(Constants.PREF_FAMILY_CODE, family.getId());
            editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
            editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
        }

        editor.apply();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));

    }

    private void quitFamily(UserFamily family) {
        loadingDialog.setText(getResources().getString(R.string.quitting_family));
        loadingDialog.show();

        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.PROFILES_CHILD, familyCode, mUid}), null);
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USERFAMILIES_CHILD, mUid, familyCode}), null);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        if(family == null) {
            // User is not in any family
            editor.putString(Constants.PREF_FAMILY_CODE, null);
            editor.putString(Constants.PREF_FAMILY_NAME, null);
            editor.putString(Constants.PREF_FAMILY_PHOTO, null);
            editor.putString(Constants.PREF_FIRST_LOGIN, null);

        }
        else {
            // Load other user family
            editor.putString(Constants.PREF_FAMILY_CODE, family.getId());
            editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
            editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());

        }

        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null) {
                    databaseUpdates.clear();
                    editor.apply();
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                }
                else {
                    Toast.makeText(SettingsActivity.this,
                            "Error quitting family, try again ",
                            Toast.LENGTH_SHORT).show();
                    loadingDialog.hide();
                }
            }
        });
    }

    private void signOut() {
        loadingDialog.setText(getResources().getString(R.string.signing_out));
        loadingDialog.show();

        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
    }

    private void updateFamily(Uri uri) {

        if(uri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.STORAGE_FAMILY_PHOTOS_CHILD)
                    .child(familyCode).child(uri.getLastPathSegment());
            imageUploader.uploadImage(storageReference, uri, ImageUtil.THUMBNAIL_MAX_SIZE);
        }
        else {
            saveFamily();
        }


    }

    private void saveFamily() {
        // Send email
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.FAMILIES_CHILD, familyCode, Constants.NAME_FIELD}), familyForm.getName());
        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(Constants.PREF_FAMILY_NAME, familyForm.getName());
                    editor.putString(Constants.PREF_FAMILY_PHOTO, mImageUri.toString());
                    editor.apply();

                    mNameView.setText(familyForm.getName());
                    ImageUtil.loadImage(SettingsActivity.this, mImageView, mImageUri.toString(), true);
                    databaseUpdates.clear();
                    loadingDialog.hide();
                    editFamilyDialog.hide();
                    familyForm.reset();
                }
                else {
                    Log.w(TAG, databaseError.toException());
                    Toast.makeText(SettingsActivity.this,
                            getResources().getString(R.string.error_updating_family),
                            Toast.LENGTH_SHORT).show();
                    loadingDialog.hide();
                }
            }
        });
    }
}
