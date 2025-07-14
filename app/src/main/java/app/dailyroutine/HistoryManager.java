package app.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryManager {

    private static final String PREF_NAME = "MonthlyHistory";
    private static final String TAG = "HistoryManager";

    /**
     * Save a new history entry with current timestamp
     */
    public static void saveMonthlyHistory(Context context, int totalAmount) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Create unique timestamp with date and time
            String timestamp = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
                    .format(new Date());

            editor.putInt(timestamp, totalAmount);
            boolean success = editor.commit(); // Use commit for immediate save

            Log.d(TAG, "History saved: " + timestamp + " -> ₹" + totalAmount + " (Success: " + success + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error saving history", e);
        }
    }

    /**
     * Get all history entries as a map
     */
    public static Map<String, Integer> getAllHistory(Context context) {
        Map<String, Integer> result = new HashMap<>();

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allEntries = prefs.getAll();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    result.put(entry.getKey(), (Integer) entry.getValue());
                }
            }

            Log.d(TAG, "Retrieved " + result.size() + " history entries");
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving history", e);
        }

        return result;
    }

    /**
     * Get all history entries as a list of HistoryModel objects
     */
    public static List<HistoryModel> getAllHistoryList(Context context) {
        List<HistoryModel> historyList = new ArrayList<>();

        try {
            Map<String, Integer> historyMap = getAllHistory(context);

            for (Map.Entry<String, Integer> entry : historyMap.entrySet()) {
                historyList.add(new HistoryModel(entry.getKey(), entry.getValue()));
            }

            Log.d(TAG, "Converted to " + historyList.size() + " HistoryModel objects");
        } catch (Exception e) {
            Log.e(TAG, "Error converting history to list", e);
        }

        return historyList;
    }

    /**
     * Delete a specific history entry
     */
    public static boolean deleteHistoryEntry(Context context, String timestampKey) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.remove(timestampKey);
            boolean success = editor.commit();

            Log.d(TAG, "History entry deleted: " + timestampKey + " (Success: " + success + ")");
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting history entry", e);
            return false;
        }
    }

    /**
     * Clear all history entries
     */
    public static boolean clearAllHistory(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear();
            boolean success = editor.commit();

            Log.d(TAG, "All history cleared (Success: " + success + ")");
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all history", e);
            return false;
        }
    }

    /**
     * Get total amount from all history entries
     */
    public static int getTotalAmount(Context context) {
        int total = 0;

        try {
            Map<String, Integer> history = getAllHistory(context);

            for (Integer amount : history.values()) {
                total += amount;
            }

            Log.d(TAG, "Total amount calculated: ₹" + total);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total amount", e);
        }

        return total;
    }

    /**
     * Get count of history entries
     */
    public static int getHistoryCount(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            int count = prefs.getAll().size();

            Log.d(TAG, "History count: " + count);
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Error getting history count", e);
            return 0;
        }
    }
}
