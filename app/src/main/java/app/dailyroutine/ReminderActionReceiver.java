package app.dailyroutine;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class ReminderActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String reminderId = intent.getStringExtra("reminder_id");

        if (reminderId == null) {
            Log.e(TAG, "Reminder ID is null");
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancel the notification
        int notificationId = reminderId.hashCode();
        notificationManager.cancel(notificationId);

        if ("SNOOZE".equals(action)) {
            snoozeReminder(context, reminderId);
            showToast(context, "Reminder snoozed for 10 minutes");
        } else if ("COMPLETE".equals(action)) {
            markReminderComplete(context, reminderId);
            showToast(context, "Reminder marked as complete");
        }

        Log.d(TAG, "Action: " + action + " for reminder: " + reminderId);
    }

    private void snoozeReminder(Context context, String reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - permission not granted");
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                return;
            }
        }

        Intent intent = new Intent(context, EnhancedNotificationReceiver.class);
        intent.setAction("REMINDER_NOTIFICATION");
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("reminder_title", "Snoozed Reminder");
        intent.putExtra("reminder_category", "reminder");
        intent.putExtra("reminder_priority", 3);

        int requestCode = (reminderId + "_snooze").hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10); // Snooze for 10 minutes

        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
            Log.d(TAG, "Reminder snoozed successfully for 10 minutes");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when scheduling snooze alarm", e);
            // Fallback to non-exact alarm
            try {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Fallback: Reminder snoozed with non-exact alarm");
            } catch (Exception fallbackException) {
                Log.e(TAG, "Failed to schedule fallback alarm", fallbackException);
            }
        }
    }

    private void markReminderComplete(Context context, String reminderId) {
        Log.d(TAG, "Reminder " + reminderId + " marked as complete");
        // You could update the reminder status in SmartReminderManager here
        // SmartReminderManager reminderManager = new SmartReminderManager(context);
        // reminderManager.markReminderComplete(reminderId);
    }

    private void showToast(Context context, String message) {
        // Create a simple notification instead of toast for better visibility
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("toast_message", message);
        context.startActivity(intent);
    }
}
