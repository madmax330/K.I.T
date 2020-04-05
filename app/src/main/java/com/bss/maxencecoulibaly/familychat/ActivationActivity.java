package com.bss.maxencecoulibaly.familychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bss.maxencecoulibaly.familychat.utils.Constants;
import com.bss.maxencecoulibaly.familychat.utils.DatabaseUtil;
import com.bss.maxencecoulibaly.familychat.utils.GeneralUtil;
import com.bss.maxencecoulibaly.familychat.utils.adapters.FamilyRecyclerAdapter;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.FamiliesDialog;
import com.bss.maxencecoulibaly.familychat.utils.dialogs.LoadingDialog;
import com.bss.maxencecoulibaly.familychat.utils.forms.ActivationForm;
import com.bss.maxencecoulibaly.familychat.utils.forms.FamilyForm;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUploader;
import com.bss.maxencecoulibaly.familychat.utils.images.ImageUtil;
import com.bss.maxencecoulibaly.familychat.utils.models.Family;
import com.bss.maxencecoulibaly.familychat.utils.models.UserFamily;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivationActivity extends AppCompatActivity {

    // Static variables
    public static final String TAG = "ActivationActivity";

    // View variables
    private LinearLayoutManager mLinearLayoutManager;

    private ActivationForm activationForm;
    private FamilyForm familyForm;

    private LoadingDialog loadingDialog;
    private FamiliesDialog familiesDialog;

    // Program variables
    private SharedPreferences mSharedPreferences;
    private DatabaseReference mFirebaseDatabaseReference;
    private Map<String, Object> databaseUpdates;

    private String mUid;
    private String mUserEmail;
    private Uri mImageUri;

    private Family mFamily;

    private FamilyRecyclerAdapter familyRecyclerAdapter;

    private ImageUploader imageUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);
        mSharedPreferences = getSharedPreferences(Constants.USERS_PREFS, Context.MODE_PRIVATE);

        mUid = mSharedPreferences.getString(Constants.PREF_USER_ID, null);
        mUserEmail = mSharedPreferences.getString(Constants.PREF_USER_EMAIL, null);

        // Init program variables
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseUpdates = new HashMap<>();

        loadingDialog = new LoadingDialog((RelativeLayout) findViewById(R.id.loadingLayout), this);

        familiesDialog = new FamiliesDialog((RelativeLayout) findViewById(R.id.familiesLayout), this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        familiesDialog.recyclerView.setLayoutManager(mLinearLayoutManager);

        activationForm = new ActivationForm((RelativeLayout) findViewById(R.id.activationForm)) {
            @Override
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.verifying_family));
                loadingDialog.show();
                GeneralUtil.hideKeyboard(ActivationActivity.this);

                final String code = activationForm.getCode();
                final DatabaseReference databaseReference = mFirebaseDatabaseReference
                        .child(Constants.FAMILIES_CHILD).child(code);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            Toast.makeText(ActivationActivity.this,
                                    getResources().getString(R.string.cannot_find_family, code),
                                    Toast.LENGTH_SHORT).show();
                            activationForm.reset();
                            loadingDialog.hide();
                        }
                        else {
                            final Family family = dataSnapshot.getValue(Family.class);

                            UserFamily userFamily = new UserFamily(code, family.getName(), family.getPhotoUrl(), false);

                            databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.USERFAMILIES_CHILD, mUid, code}), userFamily);
                            mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError == null) {
                                        databaseUpdates.clear();
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constants.PREF_FAMILY_CODE, code);
                                        editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
                                        editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
                                        editor.apply();
                                        startActivity(new Intent(ActivationActivity.this, MainActivity.class));
                                    }
                                    else {
                                        Toast.makeText(ActivationActivity.this,
                                                getResources().getString(R.string.error_joining_family),
                                                Toast.LENGTH_SHORT).show();
                                        activationForm.reset();
                                        loadingDialog.hide();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled:" + databaseError);
                    }
                });
            }

            @Override
            public void onNewFamilyClick() {
                hide();
                familyForm.show();
            }

            @Override
            public void onFamiliesClick() {
                familiesDialog.show();
            }
        };

        familyForm = new FamilyForm((RelativeLayout) findViewById(R.id.familyForm), this) {
            @Override
            public void onBackClick() {
                hide();
                activationForm.show();
            }

            @Override
            public void addImageClick() {
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = ImageUtil.createImageFile(ActivationActivity.this);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.d(TAG, "FileError: error creating file " + ex.toString());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mImageUri = FileProvider.getUriForFile(ActivationActivity.this,
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
            public void submit() {
                loadingDialog.setText(getResources().getString(R.string.creating_family));
                loadingDialog.show();
                GeneralUtil.hideKeyboard(ActivationActivity.this);

                if(getImageUri() != null) {
                    createFamily(getImageUri());
                }
                else {
                    createFamily(null);
                }

            }
        };

        imageUploader = new ImageUploader(this) {
            @Override
            public void onSuccess(Uri uri) {
                mFamily.setPhotoUrl(uri.toString());
                saveFamily(mFamily);
            }

            @Override
            public void onFail(Exception e) {
                Log.w(TAG, "Unable to load image: ", e);
                Toast.makeText(ActivationActivity.this, getResources().getString(R.string.error_creating_family), Toast.LENGTH_SHORT).show();
                loadingDialog.hide();
            }
        };

    }

    @Override
    public void onPause() {
        if(familyRecyclerAdapter != null) {
            familyRecyclerAdapter.stopListening();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mUid != null) {
            loadFamilies();
        }
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
        loadingDialog.setText(getResources().getString(R.string.loading));
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
                changeFamily(family);
            }

            @Override
            public void onDataChanged() {
                loadingDialog.hide();
                if(familyRecyclerAdapter.getItemCount() > 0) {
                    activationForm.showFamilies();
                }
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

    private void createFamily(Uri uri) {
        final String code = mFirebaseDatabaseReference.child(Constants.FAMILIES_CHILD).push().getKey();
        Family family = new Family(code, new Date().getTime(), mUid, mUserEmail, familyForm.getName(), null);
        if(uri != null) {
               StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.STORAGE_FAMILY_PHOTOS_CHILD)
                       .child(code).child(uri.getLastPathSegment());
               mFamily = family;
               imageUploader.uploadImage(storageReference, uri, ImageUtil.THUMBNAIL_MAX_SIZE);
        }
        else {
            saveFamily(family);
        }


    }

    private void saveFamily(Family family) {
        // Send email
        databaseUpdates.put(DatabaseUtil.getDatabasePath(new String[] {Constants.FAMILIES_CHILD, family.getCode()}), family);
        mFirebaseDatabaseReference.updateChildren(databaseUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null) {
                    databaseUpdates.clear();
                    Toast.makeText(ActivationActivity.this,
                            getResources().getString(R.string.use_email_code),
                            Toast.LENGTH_LONG).show();
                    loadingDialog.hide();
                    familyForm.hide();
                    activationForm.show();
                }
                else {
                    Log.w(TAG, databaseError.toException());
                    Toast.makeText(ActivationActivity.this,
                            getResources().getString(R.string.error_creating_family),
                            Toast.LENGTH_SHORT).show();
                    loadingDialog.hide();
                }
            }
        });
    }

    private void changeFamily(UserFamily family) {
        loadingDialog.setText(getResources().getString(R.string.changing_family));
        loadingDialog.show();

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.PREF_FAMILY_CODE, family.getId());
        editor.putString(Constants.PREF_FAMILY_NAME, family.getName());
        editor.putString(Constants.PREF_FAMILY_PHOTO, family.getPhotoUrl());
        editor.apply();
        startActivity(new Intent(ActivationActivity.this, MainActivity.class));

    }
}
