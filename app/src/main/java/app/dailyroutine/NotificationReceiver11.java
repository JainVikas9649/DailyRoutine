package app.dailyroutine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver11 extends BroadcastReceiver {

@Override
public void onReceive(Context context, Intent intent) {
    Log.d("NotificationReceiver11", "Reminder triggered");

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager == null) {
        Log.e("NotificationReceiver11", "NotificationManager is null");
        return;
    }

    String channelId = "reminder_channel_id";

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        NotificationChannel channel = new NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH);
//        notificationManager.createNotificationChannel(channel);
//    }
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
//        }
//    }
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Reminder")
            .setContentText("Please add your all split")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);
    int notificationId = (int) System.currentTimeMillis();
    notificationManager.notify(notificationId, builder.build());
  //  notificationManager.notify(1001, builder.build());
}
}
