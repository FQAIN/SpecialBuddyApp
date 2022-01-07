package com.trichain.specialbuddy.dashBoard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trichain.specialbuddy.R;
import com.trichain.specialbuddy.chat.model.User;
import com.trichain.specialbuddy.dashBoard.OtherActivity;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapsFragment";
    List<User> users = new ArrayList<>();

    SupportMapFragment mapFragment;
    boolean isReady = false;
    GoogleMap g;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setMyLocationEnabled(true);
            isReady = true;
            g = googleMap;
            googleMap.setOnInfoWindowClickListener(MapsFragment.this);
            addToMap();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getAllBuddies();
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    private void getAllBuddies() {

        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "getAllBuddies: " + dataSnapshot.toString());
                users.clear();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    User userInfo = d.getValue(User.class);
                    userInfo.id=d.getKey();
                    users.add(userInfo);
                }
                addToMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void addToMap() {
        if (isReady) {
            g.clear();
            if (users.size() > 0) {for (int i = 0; i < users.size(); i++) {

                    User u = users.get(i);
                    LatLng m = new LatLng(u.mLat, u.mLong);
                    Marker x = g.addMarker(
                            new MarkerOptions()
                                    .position(m).title(u.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.head)));
                    x.setTag(u.id);
//                    lastMarker = m;
                }
                if (g.isMyLocationEnabled()){
//
//                    LatLng lastMarker = new LatLng(g.getMyLocation().getLatitude(),g.getMyLocation().getLongitude());
//
//                    g.animateCamera(CameraUpdateFactory.newLatLngZoom(lastMarker, 8));
                }
//                g.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        } else {
            Log.e(TAG, "addToMap: not ready");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        String p = (String) marker.getTag();
        Log.e(TAG, "onMarkerClick: profile-" + p);
        Intent i= new Intent(getActivity(), OtherActivity.class);
        i.putExtra("id",String.valueOf(p));
        startActivity(i);

    }
}