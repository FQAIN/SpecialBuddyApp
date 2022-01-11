package com.trichain.specialbuddy;

import static com.trichain.specialbuddy.chat.data.SharedPreferenceHelper.SHARE_USER_INFO;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Locale;

public class MyAccessibleClass extends AccessibilityService {
    TextToSpeech t1;
    private static final String TAG = "MyAccessibilityClass";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        final int eventType = event.getEventType();
        String eventText = null;
        switch(eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "Clicked: ";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "Focused: ";
                break;
        }

        eventText = eventText + event.getContentDescription();


        speakToUser(eventText);
    }

    private void speakToUser(String eventText) {

        SharedPreferences s= getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
        if (s.getBoolean("accessibility",false)){

            Log.e(TAG, "speakToUser: " );
            t1.speak(eventText, TextToSpeech.QUEUE_FLUSH, null);
        }else{
            Log.e(TAG, "speakToUser: isOff" );
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e(TAG, "onInit: " );
                if(status != TextToSpeech.ERROR) {
                    Log.e(TAG, "onInit: " +status);
                    t1.setLanguage(Locale.UK);
                }else{
                    Log.e(TAG, "onInit: "+status );
//                    t1= new TextToSpeech(getApplicationContext(),this);
                }
            }
        });
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]
                {"com.trichain.specialbuddy"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }
}
