package com.ToxicBakery.lollipop.demo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ToxicBakery.lollipop.R;

public class WearableNotificationsActivity extends Activity implements View.OnClickListener {

    public static final int NOTIFICATION_ID = 32143;
    private static final String TAG = WearableNotificationsActivity.class.getSimpleName();
    private static final String EXTRA_EVENT_ID = TAG + ".EXTRA_EVENT_ID";
    private static final String EXTRA_EVENT_TEXT = TAG + ".EXTRA_EVENT_TEXT";
    private static final int EVENT_RELAUNCH = 1;
    private static final int EVENT_TALK_BACK = 2;
    private Button buttonSendText;
    private Button buttonClear;
    private EditText editTextSendText;
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable_notifications);

        notificationManager = NotificationManagerCompat.from(this);

        buttonSendText = (Button) findViewById(R.id.demo_wearable_notifications_button_send);
        buttonClear = (Button) findViewById(R.id.demo_wearable_notifications_button_clear);
        editTextSendText = (EditText) findViewById(R.id.demo_wearable_notifications_edittext_send_text);

        buttonSendText.setOnClickListener(this);
        buttonClear.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_EVENT_ID)) {
            switch (intent.getIntExtra(EXTRA_EVENT_ID, 0)) {
                case EVENT_RELAUNCH:
                    // Nothing to do
                    break;
                case EVENT_TALK_BACK:
                    editTextSendText.setText("Watch got: " + intent.getStringExtra(EXTRA_EVENT_TEXT));
                    notificationManager.cancel(NOTIFICATION_ID);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.demo_wearable_notifications_button_clear:
                editTextSendText.setText("");
                break;
            case R.id.demo_wearable_notifications_button_send:
                String textToSend = editTextSendText.getText().toString();

                Intent intentRelaunchActivity = new Intent(this, WearableNotificationsActivity.class);
                intentRelaunchActivity.putExtra(EXTRA_EVENT_ID, EVENT_RELAUNCH);

                Intent intentTalkBack = new Intent(this, WearableNotificationsActivity.class);
                intentTalkBack.putExtra(EXTRA_EVENT_TEXT, textToSend);
                intentTalkBack.putExtra(EXTRA_EVENT_ID, EVENT_TALK_BACK);

                PendingIntent pendingIntentRelaunchActivity = PendingIntent.getActivity(this, EVENT_RELAUNCH,
                        intentRelaunchActivity, 0);
                PendingIntent pendingIntentTalkBack = PendingIntent.getActivity(this, EVENT_TALK_BACK,
                        intentTalkBack, 0);

                NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_menu_start_conversation,
                        getString(R.string.demo_wearable_notifications_notification_talk_back),
                        pendingIntentTalkBack).build();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.demo_wearable_notifications_notification_title))
                        .setContentText(textToSend)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{100L, 100L})
                        .setContentIntent(pendingIntentRelaunchActivity)
                        .extend(new NotificationCompat.WearableExtender().addAction(action));

                editTextSendText.setText("");

                notificationManager.notify(NOTIFICATION_ID, builder.build());
                break;
        }
    }
}
