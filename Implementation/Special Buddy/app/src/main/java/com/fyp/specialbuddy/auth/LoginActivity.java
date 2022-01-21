package com.fyp.specialbuddy.auth;

import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_DISABILITY;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_DOB;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_GENDER;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_LAT;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_LONG;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_NAME;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_PHONE;
import static com.fyp.specialbuddy.chat.data.StaticConfig.STR_EXTRA_USERNAME;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.fyp.specialbuddy.R;
import com.fyp.specialbuddy.chat.data.SharedPreferenceHelper;
import com.fyp.specialbuddy.chat.data.StaticConfig;
import com.fyp.specialbuddy.chat.data.User;
import com.fyp.specialbuddy.dashBoard.DashBoard;
import com.fyp.specialbuddy.databinding.ActivityLoginBinding;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.HashMap;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding b;

    private AuthUtils authUtils;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private boolean firstTimeAccess;
    private FirebaseAuth mAuth;

    private LovelyProgressDialog waitingDialog;
    private static final String TAG = "LoginActivity";
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_login);
        getSupportActionBar().setTitle("Login");
        initFirebase();

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            authUtils.createUser(data);

                        }
                    }
                });
    }

    public void validate(View view) {
        if (b.emailED.getText().toString().isEmpty()) {
            b.emailED.setError("Please fill this");
            b.emailED.requestFocus();
        } else if (b.passwordED.getText().toString().isEmpty()) {
            b.passwordED.setError("Please fill this");
            b.passwordED.requestFocus();
        } else if (!b.emailED.getText().toString().contains("@")) {
            b.emailED.setError("Please enter a valid email");
            b.emailED.requestFocus();
        } else {
            networkLogin();
        }
    }


    /**
     * Initialize
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        authUtils = new AuthUtils();
        user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            StaticConfig.UID = user.getUid();
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            startActivity(new Intent(LoginActivity.this, DashBoard.class));
            LoginActivity.this.finish();

        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    StaticConfig.UID = user.getUid();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (firstTimeAccess) {
                        startActivity(new Intent(LoginActivity.this, DashBoard.class));
                        LoginActivity.this.finish();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                firstTimeAccess = false;
            }
        };

        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
    }


    private void networkLogin() {
        b.progressCircular.show();
        authUtils.signIn(Objects.requireNonNull(b.emailED.getText()).toString(), b.passwordED.getText().toString(), false, null);
        b.progressCircular.hide();
    }

    public void signUpRedirect(View v) {
        someActivityResultLauncher.launch(new Intent(LoginActivity.this, RegisterActivity.class));
    }
    double mLat,mLong;

    class AuthUtils {
        /**
         * Action register
         */
        void createUser(Intent data) {
            waitingDialog.setIcon(R.drawable.ic_add_friend)
                    .setTitle("Registering....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            mAuth.createUserWithEmailAndPassword(data.getStringExtra(STR_EXTRA_USERNAME), data.getStringExtra(StaticConfig.STR_EXTRA_PASSWORD))
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            waitingDialog.dismiss();
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                new LovelyInfoDialog(LoginActivity.this) {
                                    @Override
                                    public LovelyInfoDialog setConfirmButtonText(String text) {
                                        findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dismiss();
                                            }
                                        });
                                        return super.setConfirmButtonText(text);
                                    }
                                }
                                        .setTopColorRes(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_add_friend)
                                        .setTitle("Register false")
                                        .setMessage("Email exist or weak password!")
                                        .setConfirmButtonText("ok")
                                        .setCancelable(true)
                                        .show();
                            } else {
                                mLat=data.getDoubleExtra(STR_EXTRA_LAT,	53.328430);
                                mLong=data.getDoubleExtra(STR_EXTRA_LONG,-6.304864);
                                signIn(data.getStringExtra(STR_EXTRA_USERNAME), data.getStringExtra(StaticConfig.STR_EXTRA_PASSWORD), true, data);

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            waitingDialog.dismiss();
                        }
                    })
            ;
        }


        /**
         * Action Login
         *
         * @param email
         * @param password
         */
        void signIn(String email, String password, boolean fromReg, Intent data) {
            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Login....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            waitingDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                new LovelyInfoDialog(LoginActivity.this) {
                                    @Override
                                    public LovelyInfoDialog setConfirmButtonText(String text) {
                                        findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dismiss();
                                            }
                                        });
                                        return super.setConfirmButtonText(text);
                                    }
                                }
                                        .setTopColorRes(R.color.colorAccent)
                                        .setIcon(R.drawable.ic_person_low)
                                        .setTitle("Login false")
                                        .setMessage("Email not exist or wrong password!")
                                        .setCancelable(true)
                                        .setConfirmButtonText("Ok")
                                        .show();
                            } else {
                                user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    Log.e(TAG, "onActivityResult: " + user.getUid());
                                } else {
                                    Log.e(TAG, "onActivityResult: user is null");
                                }
                                if (fromReg) {
                                    initNewUserInfo(data);
                                    Toast.makeText(LoginActivity.this, "Register and Login success", Toast.LENGTH_SHORT).show();

                                }
                                saveUserInfo();
                                startActivity(new Intent(LoginActivity.this, DashBoard.class));
                                LoginActivity.this.finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            waitingDialog.dismiss();
                        }
                    });
        }

        /**
         * saving user
         */
        void saveUserInfo() {
            FirebaseDatabase.getInstance().getReference().child("user/" + FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e(TAG, "onDataChange: " + dataSnapshot.toString());
                    waitingDialog.dismiss();
                    HashMap hashUser = (HashMap) dataSnapshot.getValue();
                    User userInfo = new User();
                    userInfo.name = (String) hashUser.get("name");
                    userInfo.email = (String) hashUser.get("email");
                    userInfo.avata = (String) hashUser.get("avata");
                    userInfo.phone = (String) hashUser.get(STR_EXTRA_PHONE);
                    userInfo.dob = (String) hashUser.get(STR_EXTRA_DOB);
                    userInfo.gender = (String) hashUser.get(STR_EXTRA_GENDER);
                    userInfo.disability = (String) hashUser.get(STR_EXTRA_DISABILITY);
                    try {
                        if (hashUser.get(STR_EXTRA_LAT) instanceof Double){

                            userInfo.mLat = (double) hashUser.get(STR_EXTRA_LAT);
                            userInfo.mLong = (double) hashUser.get(STR_EXTRA_LONG);
                        }else {
                            userInfo.mLat = 0.0;
                            userInfo.mLong = 0.0;
                        }

                    }catch (Exception e){

                    }
                    SharedPreferenceHelper.getInstance(LoginActivity.this).saveUserInfo(userInfo);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        /**
         *
         */
        void initNewUserInfo(Intent data) {
            User newUser = new User();
            newUser.email = data.getStringExtra(STR_EXTRA_USERNAME);
            newUser.name = data.getStringExtra(STR_EXTRA_NAME);
            newUser.phone = data.getStringExtra(STR_EXTRA_PHONE);
            newUser.dob = data.getStringExtra(STR_EXTRA_DOB);
            newUser.gender = data.getStringExtra(STR_EXTRA_GENDER);
            newUser.disability = data.getStringExtra(STR_EXTRA_DISABILITY);
            newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
            newUser.mLat = mLat==0?53.2734:mLat;
            newUser.mLong = mLong==0?-7.77832031:mLong;
            FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).setValue(newUser);
        }
    }
}