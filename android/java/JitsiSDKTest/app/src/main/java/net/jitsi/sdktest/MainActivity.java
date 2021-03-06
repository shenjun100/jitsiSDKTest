package net.jitsi.sdktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextURL = findViewById(R.id.serverURL);
        String serverURLStr = "https://" + editTextURL.getText().toString();

        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            //serverURL = new URL("https://call.phonecrypt.org");
            serverURL = new URL(serverURLStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                //.setFeatureFlag("toolbox.enabled", false)
                .setFeatureFlag("filmstrip.enabled", false)
                .setFeatureFlag("fullscreen.enabled", false)
                .setFeatureFlag("add-people.enabled", false)
                .setFeatureFlag("chat.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("calendar.enabled", false)
                .setFeatureFlag("help.enabled", false)
                .setFeatureFlag("raise-hand.enabled", false)
                .setFeatureFlag("live-streaming.enabled", false)
                .setFeatureFlag("kick-out.enabled", false)
                .setFeatureFlag("video-share.enabled", false)
                .setFeatureFlag("recording.enabled", false)
                .setFeatureFlag("meeting-name.enabled", false)
                .setFeatureFlag("meeting-password.enabled", false)
                .setFeatureFlag("toolbox.alwaysVisible", true)
                .setFeatureFlag("notifications.enabled", false)
                .setFeatureFlag("tile-view.enabled", true)
                .setFeatureFlag("pip.enabled", false)
                .setFeatureFlag("audio-mute.enabled", true)
                .setFeatureFlag("video-mute.enabled", true)
                .setFeatureFlag("security-options.enabled", false)
                .setFeatureFlag("server-url-change.enabled", false)
                .setFeatureFlag("lobby-mode.enabled", false)
                .setFeatureFlag("conference-timer.enabled", true)
                .setFeatureFlag("audio-only.enabled", true)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    public void onButtonClick(View v) {
        EditText editText = findViewById(R.id.conferenceName);
        Switch switchMedia = findViewById(R.id.switchMedia);
        String text = editText.getText().toString();
        EditText editTextURL = findViewById(R.id.serverURL);
        String serverURLStr = "https://" + editTextURL.getText().toString();

        URL serverURL;
        try {
            serverURL = new URL(serverURLStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        if (text.length() > 0) {
            // Build options object for joining the conference. The SDK will merge the default
            // one we set earlier and this one when joining.
            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(text)
                    //.setAudioOnly(!switchMedia.isChecked())
                    .setSubject(" ")
                    // Settings for audio and video
                    //.setAudioMuted(true)
                    .setVideoMuted(!switchMedia.isChecked())
                    .build();
            // Launch the new activity with the given options. The launch() method takes care
            // of creating the required Intent and passing the options.
            JitsiMeetActivity.launch(this, options);
        }
    }

    public void onButtonClickHangUp(View v) {
        hangUp();
    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }
}
