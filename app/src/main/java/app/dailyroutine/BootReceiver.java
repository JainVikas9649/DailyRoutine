package app.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Device booted, rescheduling reminders");
            
            // Reschedule all active reminders
            SmartReminderManager reminderManager = new SmartReminderManager(context);
            List<EnhancedReminderModel> activeReminders = reminderManager.getActiveReminders();
            
            for (EnhancedReminderModel reminder : activeReminders) {
                reminderManager.saveReminder(reminder); // This will reschedule the reminder
                Log.d(TAG, "Rescheduled reminder: " + reminder.getTitle());
            }
            
            Log.d(TAG, "Rescheduled " + activeReminders.size() + " reminders");
        }
    }
}
