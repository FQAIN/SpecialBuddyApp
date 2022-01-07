package com.trichain.specialbuddy.dashBoard.ui;

import static com.trichain.specialbuddy.chat.data.SharedPreferenceHelper.SHARE_USER_INFO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;

import com.trichain.specialbuddy.databinding.FragmentEnableAccBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnableAccFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnableAccFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EnableAccFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EnableAccFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnableAccFragment newInstance(String param1, String param2) {
        EnableAccFragment fragment = new EnableAccFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FragmentEnableAccBinding b;
    private static final String TAG = "EnableAccFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentEnableAccBinding.inflate(inflater, container, false);
        checkAccessibilityPermission();


        return b.getRoot();
    }

    boolean isRunning = false;

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
        SharedPreferences s = getActivity().getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);

        isRunning = true;
        if (s.getBoolean("accessibility", false)) {
            Log.e(TAG, "onResume: true" );
            b.accSwitch.setChecked(true);
        } else {
            Log.e(TAG, "onResume: false" );

            b.accSwitch.setChecked(false);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                isRunning = false;
            }
        }, 1000);

        b.accSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isRunning)
                    doSwitch(isChecked);

            }
        });
    }

    private void doSwitch(boolean isChecked) {
        Log.e(TAG, "doSwitch: " );
        SharedPreferences s = getActivity().getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
        s.edit().putBoolean("accessibility", isChecked).apply();
        if (isChecked) {

            int accessEnabled = 0;
            try {
                accessEnabled = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (accessEnabled == 0) {
                // if not construct intent to request permission
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // request permission via start activity for result
                startActivity(intent);
            }
        } else {

        }
    }

    public boolean checkAccessibilityPermission() {
        int accessEnabled = 0;
        try {
            accessEnabled = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessEnabled == 0) {
            // if not construct intent to request permission
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // request permission via start activity for result
            startActivity(intent);
            return false;
        } else {
            return true;
        }
    }
}