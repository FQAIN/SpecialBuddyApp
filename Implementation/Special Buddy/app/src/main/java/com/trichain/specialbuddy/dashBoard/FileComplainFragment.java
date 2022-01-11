package com.trichain.specialbuddy.dashBoard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.trichain.specialbuddy.databinding.FragmentProfileBinding;

public class FileComplainFragment extends Fragment {

    private FragmentProfileBinding b;

    private static final String TAG = "FileComplainFragment";


    public FileComplainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = new View(getContext());
        Log.e(TAG, "onClick: complain");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"specialbuddyinfo@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Complain on the app");
        i.putExtra(Intent.EXTRA_TEXT   , "Hello, I would like to file a complain...");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}