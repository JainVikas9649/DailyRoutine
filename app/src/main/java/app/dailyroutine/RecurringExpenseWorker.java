package app.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecurringExpenseWorker extends Worker {
    private final Context context;

    public RecurringExpenseWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("expense_list", "");
        Gson gson = new Gson();

        Type type = new com.google.gson.reflect.TypeToken<List<ExpenseModel>>() {}.getType();
        List<ExpenseModel> expenses = gson.fromJson(json, type);

        if (expenses == null) expenses = new ArrayList<>();

        boolean updated = false;
        String today = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());

        List<ExpenseModel> newExpenses = new ArrayList<>();
        for (ExpenseModel e : expenses) {
            if (e.isRecurring()) {
                if (shouldAddToday(e.getRecurrenceType(), e.getDate(), today)) {
                    ExpenseModel copy = new ExpenseModel();
                    copy.setNote(e.getNote());
                    copy.setAmount(e.getAmount());
                    copy.setDate(today);
                    // copy.setCategory(e.getCategory()); // Uncomment if using categories
                    copy.setRecurring(true);
                    copy.setRecurrenceType(e.getRecurrenceType());
                    newExpenses.add(copy);
                    updated = true;
                }
            }
        }

        if (updated) {
            expenses.addAll(newExpenses);
            SharedPreferences.Editor editor = prefs.edit();
            String updatedJson = gson.toJson(expenses);
            editor.putString("expense_list", updatedJson);
            editor.apply();
        }

        return Result.success();
    }


    private boolean shouldAddToday(String recurrenceType, String lastDate, String today) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            Date last = sdf.parse(lastDate);
            Date now = sdf.parse(today);

            long diff = now.getTime() - last.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            switch (recurrenceType) {
                case "Daily": return days >= 1;
                case "Weekly": return days >= 7;
                case "Monthly": return days >= 30;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
