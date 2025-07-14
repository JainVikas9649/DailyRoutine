package app.dailyroutine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmartReminderManager {
    private static final String PREFS_NAME = "smart_reminders";
    private static final String KEY_REMINDERS = "reminders_list";
    private static final String TAG = "SmartReminderManager";

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public SmartReminderManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveReminder(EnhancedReminderModel reminder) {
        List<EnhancedReminderModel> reminders = getAllReminders();

        // Check if reminder already exists (for updates)
        int existingIndex = -1;
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            // Cancel existing notification first
            cancelNotification(reminders.get(existingIndex));
            reminders.set(existingIndex, reminder);
        } else {
            reminders.add(reminder);
        }

        saveAllReminders(reminders);

        // Schedule notification if reminder is active
        if (reminder.isActive()) {
            scheduleNotification(reminder);
        }

        Log.d(TAG, "Reminder saved: " + reminder.getTitle());
    }

    public void deleteReminder(EnhancedReminderModel reminder) {
        List<EnhancedReminderModel> reminders = getAllReminders();
        reminders.removeIf(r -> r.getId().equals(reminder.getId()));
        saveAllReminders(reminders);
        cancelNotification(reminder);
        Log.d(TAG, "Reminder deleted: " + reminder.getTitle());
    }

    public List<EnhancedReminderModel> getAllReminders() {
        String json = prefs.getString(KEY_REMINDERS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<EnhancedReminderModel>>(){}.getType();
        List<EnhancedReminderModel> reminders = gson.fromJson(json, type);
        return reminders != null ? reminders : new ArrayList<>();
    }

    public List<EnhancedReminderModel> getActiveReminders() {
        List<EnhancedReminderModel> allReminders = getAllReminders();
        List<EnhancedReminderModel> activeReminders = new ArrayList<>();

        for (EnhancedReminderModel reminder : allReminders) {
            if (reminder.isActive()) {
                activeReminders.add(reminder);
            }
        }

        return activeReminders;
    }

    public List<EnhancedReminderModel> getUpcomingReminders() {
        List<EnhancedReminderModel> allReminders = getAllReminders();
        List<EnhancedReminderModel> upcomingReminders = new ArrayList<>();

        for (EnhancedReminderModel reminder : allReminders) {
            if (reminder.isUpcoming()) {
                upcomingReminders.add(reminder);
            }
        }

        return upcomingReminders;
    }

    public List<EnhancedReminderModel> getRemindersByCategory(String category) {
        List<EnhancedReminderModel> allReminders = getAllReminders();
        List<EnhancedReminderModel> categoryReminders = new ArrayList<>();

        for (EnhancedReminderModel reminder : allReminders) {
            if (reminder.getCategory().equalsIgnoreCase(category)) {
                categoryReminders.add(reminder);
            }
        }

        return categoryReminders;
    }

    private void saveAllReminders(List<EnhancedReminderModel> reminders) {
        String json = gson.toJson(reminders);
        prefs.edit().putString(KEY_REMINDERS, json).apply();
        Log.d(TAG, "Saved " + reminders.size() + " reminders");
    }

    private void scheduleNotification(EnhancedReminderModel reminder) {
        if (!reminder.isActive()) {
            Log.d(TAG, "Reminder is not active, skipping scheduling");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - requesting permission");
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(context, EnhancedNotificationReceiver.class);
        intent.setAction("REMINDER_NOTIFICATION");
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_category", reminder.getCategory());
        intent.putExtra("reminder_priority", reminder.getPriority());

        int requestCode = reminder.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate the trigger time
        Calendar calendar = Calendar.getInstance();

        // Parse the date if it's not today
        if (!reminder.getDate().equals(getCurrentDate())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                Date reminderDate = sdf.parse(reminder.getDate());
                if (reminderDate != null) {
                    calendar.setTime(reminderDate);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing reminder date: " + reminder.getDate(), e);
                // Use today's date as fallback
            }
        }

        // Set the time
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has passed today, schedule for tomorrow (for daily reminders)
        if (calendar.getTimeInMillis() <= System.currentTimeMillis() &&
                reminder.getRepeatType().equalsIgnoreCase("daily")) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long triggerTime = calendar.getTimeInMillis();

        try {
            switch (reminder.getRepeatType().toLowerCase()) {
                case "once":
                    if (triggerTime > System.currentTimeMillis()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );
                        Log.d(TAG, "Scheduled one-time reminder: " + reminder.getTitle() +
                                " at " + new Date(triggerTime));
                    } else {
                        Log.w(TAG, "One-time reminder time has passed: " + reminder.getTitle());
                    }
                    break;

                case "daily":
                    // For daily reminders, use setRepeating if available, otherwise setExactAndAllowWhileIdle
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );
                        // Note: For true daily repetition, you'd need to reschedule in the receiver
                    } else {
                        alarmManager.setRepeating(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                AlarmManager.INTERVAL_DAY,
                                pendingIntent
                        );
                    }
                    Log.d(TAG, "Scheduled daily reminder: " + reminder.getTitle() +
                            " at " + new Date(triggerTime));
                    break;

                case "weekly":
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                    Log.d(TAG, "Scheduled weekly reminder: " + reminder.getTitle() +
                            " at " + new Date(triggerTime));
                    break;

                case "monthly":
                    scheduleMonthlyReminder(alarmManager, calendar, pendingIntent);
                    Log.d(TAG, "Scheduled monthly reminder: " + reminder.getTitle() +
                            " at " + new Date(triggerTime));
                    break;

                default:
                    Log.w(TAG, "Unknown repeat type: " + reminder.getRepeatType());
                    break;
            }

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when scheduling notification", e);
            // Try with a non-exact alarm as fallback
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d(TAG, "Scheduled non-exact alarm as fallback");
            } catch (Exception fallbackException) {
                Log.e(TAG, "Failed to schedule fallback alarm", fallbackException);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }

    private void scheduleMonthlyReminder(AlarmManager alarmManager, Calendar calendar, PendingIntent pendingIntent) {
        // Schedule for next month same date and time
        calendar.add(Calendar.MONTH, 1);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    private void cancelNotification(EnhancedReminderModel reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, EnhancedNotificationReceiver.class);
        intent.setAction("REMINDER_NOTIFICATION");
        int requestCode = reminder.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled notification for: " + reminder.getTitle());
    }

    public void toggleReminderStatus(EnhancedReminderModel reminder, boolean isActive) {
        reminder.setActive(isActive);
        saveReminder(reminder);

        if (!isActive) {
            cancelNotification(reminder);
        }
    }

    public EnhancedReminderModel duplicateReminder(EnhancedReminderModel original) {
        EnhancedReminderModel duplicate = new EnhancedReminderModel(
                original.getTitle() + " (Copy)",
                original.getCategory(),
                original.getHour(),
                original.getMinute(),
                original.getDate(),
                original.getRepeatType(),
                original.getPriority()
        );

        saveReminder(duplicate);
        return duplicate;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Smart suggestions based on user patterns
    public List<String> getSmartSuggestions() {
        List<String> suggestions = new ArrayList<>();
        List<EnhancedReminderModel> reminders = getAllReminders();

        // Analyze patterns and suggest common reminders
        boolean hasExpenseReminder = false;
        boolean hasHealthReminder = false;

        for (EnhancedReminderModel reminder : reminders) {
            if (reminder.getCategory().equalsIgnoreCase("expense")) {
                hasExpenseReminder = true;
            }
            if (reminder.getCategory().equalsIgnoreCase("health")) {
                hasHealthReminder = true;
            }
        }

        if (!hasExpenseReminder) {
            suggestions.add("💰 Set a daily expense tracking reminder");
        }

        if (!hasHealthReminder) {
            suggestions.add("🏥 Add a medication or health check reminder");
        }

        suggestions.add("💼 Create work break reminders");
        suggestions.add("💪 Set fitness and exercise reminders");
        suggestions.add("🍽️ Add meal planning reminders");

        return suggestions;
    }

    // Method to reschedule daily reminders (call this from the notification receiver)
    public void rescheduleRepeatingReminder(String reminderId) {
        List<EnhancedReminderModel> reminders = getAllReminders();
        for (EnhancedReminderModel reminder : reminders) {
            if (reminder.getId().equals(reminderId) && reminder.isActive()) {
                if (reminder.getRepeatType().equalsIgnoreCase("daily")) {
                    // Reschedule for tomorrow
                    scheduleNotification(reminder);
                    Log.d(TAG, "Rescheduled daily reminder: " + reminder.getTitle());
                } else if (reminder.getRepeatType().equalsIgnoreCase("weekly")) {
                    // Reschedule for next week
                    scheduleNotification(reminder);
                    Log.d(TAG, "Rescheduled weekly reminder: " + reminder.getTitle());
                }
                break;
            }
        }
    }
}
