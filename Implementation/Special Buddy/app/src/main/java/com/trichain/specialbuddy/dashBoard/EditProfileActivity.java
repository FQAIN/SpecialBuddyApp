package com.trichain.specialbuddy.dashBoard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.chat.data.SharedPreferenceHelper;
import com.trichain.specialbuddy.chat.data.StaticConfig;
import com.trichain.specialbuddy.chat.data.User;
import com.trichain.specialbuddy.chat.ui.UserProfileFragment;
import com.trichain.specialbuddy.util.ImageUtils;
import com.trichain.specialbuddy.databinding.ActivityEditProfileBinding;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class EditProfileActivity extends AppCompatActivity {


    ActivityEditProfileBinding b;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    StorageReference storageRef;
    private static final String TAG = "RegisterActivity";
    String fileUrl = "";
    private FirebaseUser user;
    ActivityResultLauncher<Intent> imageLauncher;
    Context context;

    private LovelyProgressDialog waitingDialog;
    private DatabaseReference userDB;
    private User myAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        context = EditProfileActivity.this;
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
        userDB.addListenerForSingleValueEvent(userListener);

        waitingDialog = new LovelyProgressDialog(context);
        storageRef = FirebaseStorage.getInstance().getReference();// You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            waitingDialog.setMessage("Uploading diary");
                            waitingDialog.show();
                            // There are no request codes
                            Intent data = result.getData();
                            Uri filePath = data.getData();
                            File f = new File(String.valueOf(filePath));
                            String name = System.currentTimeMillis() + "." + f.getName();
                            // Create a reference to "mountains.jpg"
                            StorageReference diaryRef = storageRef.child("Diaries/" + name);

                            UploadTask uploadTask = diaryRef.putFile(filePath);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    // Continue with the task to get the download URL
                                    b.fileTL.setText("Diary: Uploaded");
                                    return diaryRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    waitingDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        fileUrl = task.getResult().toString();
                                        Log.e(TAG, "onComplete: " + task.getResult().toString());
                                    } else {
                                        Log.e(TAG, "onComplete: " + task.getException());
                                    }
                                }
                            });


                        }
                    }
                });
        imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            try {
                                waitingDialog.setMessage("Uploading Image....");
                                waitingDialog.show();
                                InputStream inputStream = context.getContentResolver().openInputStream(result.getData().getData());

                                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                                myAccount.avata = imageBase64;
                                setImageAvatar(context, imageBase64);
                                waitingDialog.setCancelable(false)
                                        .setTitle("Avatar updating....")
                                        .setTopColorRes(R.color.colorPrimary)
                                        .show();

                                userDB.child("avata").setValue(imageBase64)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    waitingDialog.dismiss();
                                                    SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(context);
                                                    preferenceHelper.saveUserInfo(myAccount);
                                                    b.placeholderImage.setImageDrawable(ImageUtils.roundedImage(context, liteImage));

                                                    new LovelyInfoDialog(context)
                                                            .setTopColorRes(R.color.colorPrimary)
                                                            .setTitle("Success")
                                                            .setMessage("Update avatar successfully!")
                                                            .show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                waitingDialog.dismiss();
                                                Log.d("Update Avatar", "failed");
                                                new LovelyInfoDialog(context)
                                                        .setTopColorRes(R.color.colorAccent)
                                                        .setTitle("False")
                                                        .setMessage("False to update avatar")
                                                        .show();
                                            }
                                        });
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                });
        b.fileTL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                fileLauncher.launch(intent);

            }
        });
        b.placeholderImage.setOnClickListener(onAvatarClick);
    }

    private ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            myAccount = dataSnapshot.getValue(User.class);

            setImageAvatar(context, myAccount.avata);
            SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(context);
//            preferenceHelper.saveUserInfo(myAccount);
            setUpUi(preferenceHelper.getUserInfo());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(UserProfileFragment.class.getName(), "loadPost:onCancelled", databaseError.toException());
        }
    };

    private void setImageAvatar(Context context, String imgBase64) {
        try {
            Resources res = getResources();
            Bitmap src;
            if (imgBase64.equals("default")) {
                src = BitmapFactory.decodeResource(res, R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            b.placeholderImage.setImageDrawable(ImageUtils.roundedImage(context, src));
        } catch (Exception e) {
        }
    }

    private View.OnClickListener onAvatarClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            new AlertDialog.Builder(EditProfileActivity.this)
                    .setTitle("Avatar")
                    .setMessage("Are you sure want to change avatar profile?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_PICK);
                            imageLauncher.launch(intent);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
        }
    };

    public void validate(View v) {
        if (b.nameED.getText().toString().isEmpty()) {
            b.nameED.setError("Please fill this");
            b.nameED.requestFocus();
        } else if (!b.emailED.getText().toString().contains("@")) {
            b.emailED.setError("Please enter a valid email");
            b.emailED.requestFocus();
        } else if (b.phoneED.getText().toString().isEmpty()) {
            b.phoneED.setError("Please fill this");
            b.phoneED.requestFocus();
        } else {
            networkUpdate();
        }
    }

    public void setUpUi(User myAccount) {
        b.nameED.setText(myAccount.name);
        b.phoneED.setText(myAccount.phone);
        b.emailED.setText(myAccount.email);
        b.fileTL.setText(myAccount.diary==null||myAccount.diary.isEmpty()?"Upload Diary":"Update Diary");


    }

    private void networkUpdate() {

        User newUser = SharedPreferenceHelper.getInstance(EditProfileActivity.this).getUserInfo();
        newUser.email = b.emailED.getText().toString();
        newUser.name = b.nameED.getText().toString();
        newUser.phone = b.phoneED.getText().toString();
        newUser.diary = (fileUrl == null || fileUrl.isEmpty()) ? newUser.diary : fileUrl;
        newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
        FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).setValue(newUser);
        SharedPreferenceHelper.getInstance(EditProfileActivity.this).saveUserInfo(newUser);


    }
}