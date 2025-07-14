package app.dailyroutine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class EnhancedNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "smart_reminders_channel";
    private static final String TAG = "EnhancedNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder notification triggered");

        String action = intent.getAction();
        if (!"REMINDER_NOTIFICATION".equals(action)) {
            Log.d(TAG, "Ignoring intent with action: " + action);
            return;
        }

        String reminderId = intent.getStringExtra("reminder_id");
        String title = intent.getStringExtra("reminder_title");
        String category = intent.getStringExtra("reminder_category");
        int priority = intent.getIntExtra("reminder_priority", 3);

        if (reminderId == null || title == null) {
            Log.e(TAG, "Missing required extras in intent");
            return;
        }

        createNotificationChannel(context);
        showNotification(context, reminderId, title, category, priority);

        // Reschedule repeating reminders
        SmartReminderManager reminderManager = new SmartReminderManager(context);
        reminderManager.rescheduleRepeatingReminder(reminderId);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Smart Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for smart reminders");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 250, 250, 250});
                channel.setShowBadge(true);

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    private void showNotification(Context context, String reminderId, String title,
                                  String category, int priority) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create intent to open the app
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create action buttons
        Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class);
        snoozeIntent.setAction("SNOOZE");
        snoozeIntent.putExtra("reminder_id", reminderId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent completeIntent = new Intent(context, ReminderActionReceiver.class);
        completeIntent.setAction("COMPLETE");
        completeIntent.putExtra("reminder_id", reminderId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context, 2, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get category icon
        String categoryIcon = getCategoryIcon(category);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon as fallback
                .setContentTitle(categoryIcon + " " + title)
                .setContentText("Tap to open app or use quick actions")
                .setPriority(getNotificationPriority(priority))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_media_pause, "Snooze 10min", snoozePendingIntent)
                .addAction(android.R.drawable.ic_menu_save, "Mark Done", completePendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't forget: " + title + "\nCategory: " + category))
                .setColor(getPriorityColor(priority))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        // Show notification
        int notificationId = reminderId.hashCode();
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Notification shown for: " + title + " with ID: " + notificationId);
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "⏰";

        switch (category.toLowerCase()) {
            case "expense": return "💰";
            case "health": return "🏥";
            case "work": return "💼";
            case "personal": return "👤";
            case "fitness": return "💪";
            case "food": return "🍽️";
            default: return "⏰";
        }
    }

    private int getNotificationPriority(int priority) {
        switch (priority) {
            case 1:
            case 2: return NotificationCompat.PRIORITY_LOW;
            case 3: return NotificationCompat.PRIORITY_DEFAULT;
            case 4:
            case 5: return NotificationCompat.PRIORITY_HIGH;
            default: return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private int getPriorityColor(int priority) {
        switch (priority) {
            case 1: return Color.parseColor("#4CAF50"); // Green
            case 2: return Color.parseColor("#8BC34A"); // Light Green
            case 3: return Color.parseColor("#FFC107"); // Yellow
            case 4: return Color.parseColor("#FF9800"); // Orange
            case 5: return Color.parseColor("#F44336"); // Red
            default: return Color.parseColor("#2196F3"); // Blue
        }
    }
}
