package com.trichain.specialbuddy.dashBoard;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.auth.LoginActivity;
import com.trichain.specialbuddy.chat.data.SharedPreferenceHelper;
import com.trichain.specialbuddy.chat.data.StaticConfig;
import com.trichain.specialbuddy.chat.model.User;
import com.trichain.specialbuddy.chat.ui.UserProfileFragment;
import com.trichain.specialbuddy.chat.util.ImageUtils;
import com.trichain.specialbuddy.databinding.ActivityDashBoardBinding;
//import com.trichain.specialbuddy.dashBoard.databinding.ActivityDashBoardBinding;

public class DashBoard extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDashBoardBinding b;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    private User myAccount;
    private DatabaseReference userDB;
    private static final String TAG = "DashBoard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityDashBoardBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        setSupportActionBar(b.appBarDashBoard.toolbar);
//        binding.appBarDashBoard.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        initFirebase();
        DrawerLayout drawer = b.drawerLayout;
        NavigationView navigationView = b.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_my_profile, R.id.nav_contact_list, R.id.nav_find_buddy, R.id.nav_buddy_request, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dash_board);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        stUpUI();
    }

    private void stUpUI() {
//        b.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                if (item.getItemId()==R.id.nav_logout){
//                    Log.e(TAG, "onClick: logout" );
//                    FirebaseAuth.getInstance().signOut();
//                    DashBoard.this.finish();
//                    return true;
//                }
//                return true;
//            }
//        });
    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky
        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in

            StaticConfig.UID = user.getUid();
            getUserDetails();

        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.e(TAG, "onAuthStateChanged: has user" );
                    StaticConfig.UID = user.getUid();
                    getUserDetails();
                } else {
                    DashBoard.this.finish();
                    // User is signed in
                    startActivity(new Intent(DashBoard.this, LoginActivity.class));
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void getUserDetails() {
        Log.e(TAG, "getUserDetails: " );
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        userDB.addValueEventListener(userListener);
    }
    private ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            Log.e(TAG, "onDataChange: "+dataSnapshot.getValue().toString() );
            myAccount = dataSnapshot.getValue(User.class);

            ((TextView)b.navView.getHeaderView(0).findViewById(R.id.nameTv)).setText(myAccount.name);
            ((TextView)b.navView.getHeaderView(0).findViewById(R.id.emailTv)).setText(myAccount.email);
            SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(DashBoard.this);
            preferenceHelper.saveUserInfo(myAccount);
            setImageAvatar(DashBoard.this, myAccount.avata);
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

            ((ImageView)b.navView.getHeaderView(0).findViewById(R.id.imageView)).setImageDrawable(ImageUtils.roundedImage(context, src));
        } catch (Exception e) {
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_board, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dash_board);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}