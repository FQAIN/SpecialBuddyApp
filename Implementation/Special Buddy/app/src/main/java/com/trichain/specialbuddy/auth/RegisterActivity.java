package com.trichain.specialbuddy.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.chat.data.StaticConfig;
import com.trichain.specialbuddy.databinding.ActivityRegisterBinding;


public class RegisterActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int MY_PERMISSION_REQUEST_CODE_PHONE_STATE = 112;

    ActivityRegisterBinding b;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    double mLat, mLong;
    private static final String TAG = "RegisterActivity";


    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("Register");
        setUpSpinner();
        permissionCheck(false);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    private void permissionCheck(boolean shouldGetLocation) {
        int readPhoneStatePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            // If don't have permission so prompt the user.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                RegisterActivity.this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_CODE_PHONE_STATE
                );
            }
            return;
        } else {
            if (shouldGetLocation)
                requestLocation();
        }
    }

    private void setUpSpinner() {
        b.disabilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.disability));
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        b.disabilitySpinner.setAdapter(aa);
    }

    public void googleSignIn(View v) {
        // Configure Google Sign In
        /*GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);*/

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();

                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                                firebaseAuthWithGoogle(account.getIdToken());
                            } catch (ApiException e) {
                                // Google Sign In failed, update UI appropriately
                                Log.w(TAG, "Google sign in failed", e);
                            }
                        }
                    }
                });
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someActivityResultLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            updateUI(null);
                        }
                    }
                });
    }

    public void validate(View v) {
        if (b.emailED.getText().toString().isEmpty()) {
            b.emailED.setError("Please fill this");
            b.emailED.requestFocus();
        } else if (b.passwordED.getText().toString().isEmpty()) {
            b.passwordED.setError("Please fill this");
            b.passwordED.requestFocus();
        } else if (!b.emailED.getText().toString().contains("@")) {
            b.emailED.setError("Please enter a valid email");
            b.emailED.requestFocus();
        } else if (b.nameED.getText().toString().isEmpty()) {
            b.nameED.setError("Please fill this");
            b.nameED.requestFocus();
        } else if (b.dobED.getText().toString().isEmpty()) {
            b.dobED.setError("Please fill this");
            b.dobED.requestFocus();
        } else if (b.genderRG.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Kindly select the gender", Toast.LENGTH_SHORT).show();
        } else if (b.disabilitySpinner.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "Kindly select the Disability status", Toast.LENGTH_SHORT).show();
        } else {
            networkRegister();
        }
    }

    public void loginRedirect(View v) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        RegisterActivity.this.finish();
    }

    private void networkRegister() {

        Intent data = new Intent();
        data.putExtra(StaticConfig.STR_EXTRA_USERNAME, b.emailED.getText().toString());
        data.putExtra(StaticConfig.STR_EXTRA_NAME, b.nameED.getText().toString());
        data.putExtra(StaticConfig.STR_EXTRA_PHONE, b.phoneED.getText().toString());
        data.putExtra(StaticConfig.STR_EXTRA_DOB, b.dobED.getText().toString());
        data.putExtra(StaticConfig.STR_EXTRA_GENDER, b.genderRG.getCheckedRadioButtonId() == R.id.maleRB ? "Male" : "Female");
        data.putExtra(StaticConfig.STR_EXTRA_DISABILITY, b.disabilitySpinner.getSelectedItemPosition() == 0 ? "WIth disability" : "Without disability");
        data.putExtra(StaticConfig.STR_EXTRA_PASSWORD, b.passwordED.getText().toString());
        data.putExtra(StaticConfig.STR_EXTRA_LAT, mLat);
        data.putExtra(StaticConfig.STR_EXTRA_LONG, mLong);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e(TAG, "onLocationChanged: " + location.getLatitude());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        permissionCheck(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE_PHONE_STATE: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

//                    permissionCheck(false);
                    return;
                } else {
                    requestLocation();
                }

            }
            break;
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        Log.e(TAG, "requestLocation: ");
        final boolean[] hasSTartedS = {false};
        if (!isLocationEnabled()) {
            showAlert();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isLocationEnabled() && !hasSTartedS[0]) {
//                        ForegroundServiceLauncher.getInstance().startService(StatisticsActivity.this);
                        hasSTartedS[0] = true;
                    } else {
                        new Handler().postDelayed(this::run, 1000);
                    }
                }
            }, 1000);
            return;
        }
//        ForegroundServiceLauncher.getInstance().startService(this);
        // Create the location request
        mLocationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);
//        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            Log.e(TAG, "requestLocation");
            LocationServices.getFusedLocationProviderClient(RegisterActivity.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Log.e(TAG, "onLocationResult: " + locationResult.getLastLocation());
                    mLat = locationResult.getLastLocation().getLatitude();
                    mLong = locationResult.getLastLocation().getLongitude();
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                    if (!locationAvailability.isLocationAvailable()) {

                    }
                    Log.e(TAG, "onLocationAvailability: " + locationAvailability.toString());
                }
            }, Looper.myLooper());

        }
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        paramDialogInterface.dismiss();
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        paramDialogInterface.dismiss();
                        RegisterActivity.this.finish();
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}