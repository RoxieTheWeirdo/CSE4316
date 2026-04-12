package com.example.fitbite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationHistory extends AppCompatActivity {

    private LinearLayout historyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_history);
        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        historyContainer = findViewById(R.id.history_container);
        loadHistory();
    }
    private void loadHistory() {
        historyContainer.removeAllViews();
        JSONArray history = Notifications.getNotificationHistory(this);

        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < history.length(); i++) {
            JSONObject notif = history.optJSONObject(i);
            if (notif == null) continue;

            View bubble = inflater.inflate(R.layout.notification_history_item, historyContainer, false);

            TextView title = bubble.findViewById(R.id.inAppTitle);
            TextView message = bubble.findViewById(R.id.inAppMessage);
            TextView time = bubble.findViewById(R.id.inAppTime);
            ImageView trash = bubble.findViewById(R.id.inAppTrash);

            title.setText(notif.optString("title"));
            message.setText(notif.optString("message"));
            time.setText(notif.optString("time"));

            final int index = i;
            trash.setOnClickListener(v -> {
                Notifications.removeNotificationFromHistory(this, index);
                loadHistory(); // refresh
            });

            historyContainer.addView(bubble);
        }
    }
}