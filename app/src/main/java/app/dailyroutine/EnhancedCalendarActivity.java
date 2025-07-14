package app.dailyroutine;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EnhancedCalendarActivity extends AppCompatActivity {

    MaterialCalendarView calendarView;
    HashMap<String, List<ExpenseModel>> expenseMap = new HashMap<>();
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    // UI Components
    TextView monthTotalAmount, expenseDaysCount, avgPerDay;
    TextView selectedDateText, selectedDateTotal, selectedDateCount;
    CardView selectedDateCard, monthSummaryCard;
    MaterialButton todayButton, exportButton;
    ImageView backButton ;
    // Animation variables
    private boolean isCardAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        backButton = findViewById(R.id.backButton);
        initializeViews();
        setupClickListeners();
        loadExpenses();
        updateMonthSummary();
        
        // Entrance animations
        animateEntranceViews();
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        monthTotalAmount = findViewById(R.id.monthTotalAmount);
        expenseDaysCount = findViewById(R.id.expenseDaysCount);
        avgPerDay = findViewById(R.id.avgPerDay);
        selectedDateText = findViewById(R.id.selectedDateText);
        selectedDateTotal = findViewById(R.id.selectedDateTotal);
        selectedDateCount = findViewById(R.id.selectedDateCount);
        selectedDateCard = findViewById(R.id.selectedDateCard);
        monthSummaryCard = findViewById(R.id.monthSummaryCard);
        todayButton = findViewById(R.id.todayButton);
        exportButton = findViewById(R.id.exportButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateButtonClick(v);
            finish();
        });

        findViewById(R.id.statsButton).setOnClickListener(v -> {
            animateButtonClick(v);
            showMonthlyStats();
        });

        todayButton.setOnClickListener(v -> {
            animateButtonClick(v);
            goToToday();
        });

        exportButton.setOnClickListener(v -> {
            animateButtonClick(v);
            exportExpenses();
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            handleDateSelection(date);
        });

        calendarView.setOnMonthChangedListener((widget, date) -> {
            updateMonthSummary();
            animateMonthChange();
        });
    }

    private void loadExpenses() {
        SharedPreferences prefs = getSharedPreferences("expenses", Context.MODE_PRIVATE);
        String json = prefs.getString("expense_list", null);

        Log.d("ExpenseLoad", "Raw JSON from SharedPreferences: " + json);

        List<ExpenseModel> allExpenses = new Gson().fromJson(json, new TypeToken<List<ExpenseModel>>(){}.getType());
        if (allExpenses == null) allExpenses = new ArrayList<>();

        Set<CalendarDay> decoratedDays = new HashSet<>();
        expenseMap.clear();

        for (ExpenseModel e : allExpenses) {
            try {
                Log.d("ExpenseLoad", "Saved Date: " + e.getDate());

                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(e.getDate()));

                String key = sdf.format(cal.getTime());

                expenseMap.putIfAbsent(key, new ArrayList<>());
                expenseMap.get(key).add(e);

                CalendarDay day = CalendarDay.from(cal);
                decoratedDays.add(day);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Log.d("ExpenseLoad", "Final ExpenseMap Keys: " + expenseMap.keySet());
        
        // Enhanced decorators with different colors based on expense amount
     //   calendarView.addDecorator(new EnhancedEventDecorator(expenseMap));
        HashSet<CalendarDay> low = new HashSet<>();
        HashSet<CalendarDay> mid = new HashSet<>();
        HashSet<CalendarDay> high = new HashSet<>();

        for (String dateKey : expenseMap.keySet()) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(dateKey));
                CalendarDay day = CalendarDay.from(cal);

                double total = 0;
                for (ExpenseModel e : expenseMap.get(dateKey)) {
                    total += e.getAmount();
                }

                if (total > 1000) {
                    high.add(day);
                } else if (total > 500) {
                    mid.add(day);
                } else {
                    low.add(day);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

// Now add separate decorators
        calendarView.addDecorator(new EnhancedEventDecorator(low, Color.parseColor("#4CAF50")));   // green
        calendarView.addDecorator(new EnhancedEventDecorator(mid, Color.parseColor("#FF9800")));   // orange
        calendarView.addDecorator(new EnhancedEventDecorator(high, Color.parseColor("#FF5722")));  // red
    }

    private void handleDateSelection(CalendarDay date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, date.getYear());
        cal.set(Calendar.MONTH, date.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, date.getDay());

        String formattedDate = sdf.format(cal.getTime());

        Log.d("CalendarDebug", "Selected Date: " + date.getDay() + "/" + (date.getMonth() + 1) + "/" + date.getYear());
        Log.d("CalendarDebug", "Formatted Date: " + formattedDate);

        if (expenseMap.containsKey(formattedDate)) {
            List<ExpenseModel> expenses = expenseMap.get(formattedDate);
            showSelectedDateInfo(formattedDate, expenses);
            showEnhancedExpenseDetails(formattedDate, expenses);
        } else {
            hideSelectedDateInfo();
            showNoExpenseMessage(formattedDate);
        }
    }

    private void showSelectedDateInfo(String date, List<ExpenseModel> expenses) {
        double total = 0;
        for (ExpenseModel e : expenses) {
            total += e.getAmount();
        }

        selectedDateText.setText(date);
        selectedDateTotal.setText("Total: ₹" + String.format("%.2f", total));
        selectedDateCount.setText(expenses.size() + " expense" + (expenses.size() > 1 ? "s" : ""));

        if (selectedDateCard.getVisibility() == View.GONE) {
            selectedDateCard.setVisibility(View.VISIBLE);
            animateCardSlideIn(selectedDateCard);
        } else {
            animateValueChange(selectedDateTotal);
        }
    }

    private void hideSelectedDateInfo() {
        if (selectedDateCard.getVisibility() == View.VISIBLE) {
            animateCardSlideOut(selectedDateCard);
        }
    }

    private void updateMonthSummary() {
        Calendar currentMonth = Calendar.getInstance();
        CalendarDay currentCalendarDay = calendarView.getCurrentDate();
        if (currentCalendarDay != null) {
            currentMonth.set(Calendar.YEAR, currentCalendarDay.getYear());
            currentMonth.set(Calendar.MONTH, currentCalendarDay.getMonth());
        }

        double monthTotal = 0;
        int daysWithExpenses = 0;

        for (String dateKey : expenseMap.keySet()) {
            try {
                Calendar expenseDate = Calendar.getInstance();
                expenseDate.setTime(sdf.parse(dateKey));

                if (expenseDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                    expenseDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
                    
                    List<ExpenseModel> expenses = expenseMap.get(dateKey);
                    for (ExpenseModel expense : expenses) {
                        monthTotal += expense.getAmount();
                    }
                    daysWithExpenses++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double avgPerDayValue = daysWithExpenses > 0 ? monthTotal / daysWithExpenses : 0;

        // Animate the values
        animateCounterValue(monthTotalAmount, monthTotal, "₹");
        animateCounterValue(expenseDaysCount, daysWithExpenses, "");
        animateCounterValue(avgPerDay, avgPerDayValue, "₹");
    }

    private void showEnhancedExpenseDetails(String date, List<ExpenseModel> expenses) {
        StringBuilder builder = new StringBuilder();
        builder.append("📅 ").append(date).append("\n\n");
        
        double total = 0;
        for (int i = 0; i < expenses.size(); i++) {
            ExpenseModel e = expenses.get(i);
            builder.append("💰 ₹").append(String.format("%.2f", e.getAmount()))
                   .append(" - ").append(e.getNote()).append("\n");
            total += e.getAmount();
            
            if (i < expenses.size() - 1) {
                builder.append("\n");
            }
        }
        
        builder.append("\n").append("━━━━━━━━━━━━━━━━━━━━").append("\n");
        builder.append("💳 Total: ₹").append(String.format("%.2f", total));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("💸 Expense Details")
                .setMessage(builder.toString())
                .setPositiveButton("✅ OK", null)
                .setNeutralButton("📊 View Stats", (dialog, which) -> showDayStats(expenses))
                .show();
    }

    private void showNoExpenseMessage(String date) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📅 " + date)
                .setMessage("🎉 No expenses recorded for this date!\n\nKeep up the good work with your spending!")
                .setPositiveButton("✅ OK", null)
                .setNeutralButton("➕ Add Expense", (dialog, which) -> {
                // Navigate to add expense activity
                Toast.makeText(this, "Add expense functionality", Toast.LENGTH_SHORT).show();
            })
                .show();
    }

    private void showDayStats(List<ExpenseModel> expenses) {
        // Calculate day statistics
        double total = 0;
        HashMap<String, Double> categoryTotals = new HashMap<>();
        
        for (ExpenseModel expense : expenses) {
            total += expense.getAmount();
            // You can add category logic here if ExpenseModel has category field
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 Day Statistics\n\n");
        stats.append("💰 Total Amount: ₹").append(String.format("%.2f", total)).append("\n");
        stats.append("📝 Number of Transactions: ").append(expenses.size()).append("\n");
        stats.append("📈 Average per Transaction: ₹").append(String.format("%.2f", total / expenses.size()));
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📊 Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showMonthlyStats() {
        // Show comprehensive monthly statistics
        Calendar currentMonth = Calendar.getInstance();
        CalendarDay currentCalendarDay = calendarView.getCurrentDate();
        if (currentCalendarDay != null) {
            currentMonth.set(Calendar.YEAR, currentCalendarDay.getYear());
            currentMonth.set(Calendar.MONTH, currentCalendarDay.getMonth());
        }

        double monthTotal = 0;
        int totalTransactions = 0;
        int daysWithExpenses = 0;

        for (String dateKey : expenseMap.keySet()) {
            try {
                Calendar expenseDate = Calendar.getInstance();
                expenseDate.setTime(sdf.parse(dateKey));

                if (expenseDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                    expenseDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
                    
                    List<ExpenseModel> expenses = expenseMap.get(dateKey);
                    for (ExpenseModel expense : expenses) {
                        monthTotal += expense.getAmount();
                        totalTransactions++;
                    }
                    daysWithExpenses++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.getTime());
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 ").append(monthName).append(" Statistics\n\n");
        stats.append("💰 Total Expenses: ₹").append(String.format("%.2f", monthTotal)).append("\n");
        stats.append("📝 Total Transactions: ").append(totalTransactions).append("\n");
        stats.append("📅 Days with Expenses: ").append(daysWithExpenses).append("\n");
        if (daysWithExpenses > 0) {
            stats.append("📈 Average per Day: ₹").append(String.format("%.2f", monthTotal / daysWithExpenses)).append("\n");
        }
        if (totalTransactions > 0) {
            stats.append("💳 Average per Transaction: ₹").append(String.format("%.2f", monthTotal / totalTransactions));
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📊 Monthly Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("📤 Export", (dialog, which) -> exportExpenses())
                .show();
    }

    private void goToToday() {
        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.setCurrentDate(CalendarDay.today());
    }

    private void exportExpenses() {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "expenses_" + System.currentTimeMillis() + ".csv");
            
            FileWriter writer = new FileWriter(file);
            writer.append("Date,Amount,Note\n");
            
            for (String date : expenseMap.keySet()) {
                List<ExpenseModel> expenses = expenseMap.get(date);
                for (ExpenseModel expense : expenses) {
                    writer.append(date).append(",")
                          .append(String.valueOf(expense.getAmount())).append(",")
                          .append(expense.getNote()).append("\n");
                }
            }
            
            writer.flush();
            writer.close();
            
            // Share the file
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Export Expenses"));
            
            Toast.makeText(this, "Expenses exported successfully!", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export expenses", Toast.LENGTH_SHORT).show();
        }
    }

    // Animation Methods
    private void animateEntranceViews() {
        // Animate top bar
        ObjectAnimator topBarSlide = ObjectAnimator.ofFloat(findViewById(R.id.topBar), "translationY", -200f, 0f);
        topBarSlide.setDuration(600);
        topBarSlide.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate summary card
        ObjectAnimator summaryScale = ObjectAnimator.ofFloat(monthSummaryCard, "scaleX", 0f, 1f);
        ObjectAnimator summaryScaleY = ObjectAnimator.ofFloat(monthSummaryCard, "scaleY", 0f, 1f);
        summaryScale.setDuration(500);
        summaryScaleY.setDuration(500);
        summaryScale.setStartDelay(200);
        summaryScaleY.setStartDelay(200);

        // Animate calendar
        View parentView = (View) calendarView.getParent();

     //   ObjectAnimator calendarAlpha = ObjectAnimator.ofFloat(calendarView.getParent(), "alpha", 0f, 1f);
        ObjectAnimator calendarAlpha = ObjectAnimator.ofFloat(parentView, "alpha", 0f, 1f);
        calendarAlpha.setDuration(800);
        calendarAlpha.setStartDelay(400);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topBarSlide, summaryScale, summaryScaleY, calendarAlpha);
        animatorSet.start();
    }

    private void animateButtonClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    private void animateCardSlideIn(View card) {
        if (isCardAnimating) return;
        isCardAnimating = true;
        
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(card, "translationY", 100f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
        
        slideIn.setDuration(400);
        fadeIn.setDuration(400);
        slideIn.setInterpolator(new BounceInterpolator());
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(slideIn, fadeIn);
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isCardAnimating = false;
            }
        });
        animatorSet.start();
    }

    private void animateCardSlideOut(View card) {
        if (isCardAnimating) return;
        isCardAnimating = true;
        
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(card, "translationY", 0f, 100f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(card, "alpha", 1f, 0f);
        
        slideOut.setDuration(300);
        fadeOut.setDuration(300);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(slideOut, fadeOut);
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                card.setVisibility(View.GONE);
                isCardAnimating = false;
            }
        });
        animatorSet.start();
    }

    private void animateCounterValue(TextView textView, double targetValue, String prefix) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) targetValue);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float currentValue = (float) animation.getAnimatedValue();
            if (prefix.equals("₹")) {
                textView.setText(prefix + String.format("%.0f", currentValue));
            } else {
                textView.setText(String.format("%.0f", currentValue) + prefix);
            }
        });
        
        animator.start();
    }

    private void animateValueChange(TextView textView) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.1f, 1f);
        
        pulse.setDuration(300);
        pulseY.setDuration(300);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(pulse, pulseY);
        animatorSet.start();
    }

    private void animateMonthChange() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(monthSummaryCard, "alpha", 1f, 0.7f, 1f);
        fadeOut.setDuration(500);
        fadeOut.start();
    }
}
