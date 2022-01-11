package com.trichain.specialbuddy.chat.data;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferenceHelper {
    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    public static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_AVATA = "avata";
    private static String SHARE_KEY_UID = "uid";
    private static String SHARE_KEY_DIARY = "diary";
    private static String SHARE_KEY_PHONE = "phone";
    private static String SHARE_KEY_DOB = "dob";
    private static String SHARE_KEY_DISABILITY = "disability";
    private static String SHARE_KEY_GENDER = "gender";


    private SharedPreferenceHelper() {}

    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = context.getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void saveUserInfo(User user) {
        editor.putString(SHARE_KEY_NAME, user.name);
        editor.putString(SHARE_KEY_EMAIL, user.email);
        editor.putString(SHARE_KEY_AVATA, user.avata);
        editor.putString(SHARE_KEY_PHONE, user.phone);
        editor.putString(SHARE_KEY_DOB, user.dob);
        editor.putString(SHARE_KEY_DIARY, user.diary);
        editor.putString(SHARE_KEY_DISABILITY, user.disability);
        editor.putString(SHARE_KEY_GENDER, user.gender);
        editor.putString(SHARE_KEY_UID, StaticConfig.UID);
        editor.apply();
    }

    public User getUserInfo(){
        String avatar = preferences.getString(SHARE_KEY_AVATA, "default");
        String userName = preferences.getString(SHARE_KEY_NAME, "");
        String email = preferences.getString(SHARE_KEY_EMAIL, "");
        String phone = preferences.getString(SHARE_KEY_PHONE, "");
        String dob = preferences.getString(SHARE_KEY_DOB, "");
        String gender = preferences.getString(SHARE_KEY_GENDER, "");
        String disability = preferences.getString(SHARE_KEY_DISABILITY, "");
        String diary = preferences.getString(SHARE_KEY_DIARY, "");

        User user = new User();
        user.name = userName;
        user.email = email;
        user.avata = avatar;
        user.phone = phone;
        user.dob = dob;
        user.gender = gender;
        user.disability = disability;
        user.diary = diary;

        return user;
    }

    public String getUID(){
        return preferences.getString(SHARE_KEY_UID, "");
    }

}
