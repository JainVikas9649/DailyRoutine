    package app.dailyroutine;

    import android.Manifest;
    import android.animation.AnimatorSet;
    import android.animation.ObjectAnimator;
    import android.animation.ValueAnimator;
    import android.content.ContentValues;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    import android.util.Log;
    import android.view.View;
    import android.view.animation.AccelerateDecelerateInterpolator;
    import android.view.animation.BounceInterpolator;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.cardview.widget.CardView;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.core.content.FileProvider;

    import com.google.android.material.button.MaterialButton;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;
    import com.itextpdf.text.BaseColor;
    import com.itextpdf.text.Document;
    import com.itextpdf.text.Element;
    import com.itextpdf.text.Font;
    import com.itextpdf.text.Paragraph;
    import com.itextpdf.text.Phrase;
    import com.itextpdf.text.pdf.PdfPCell;
    import com.itextpdf.text.pdf.PdfPTable;
    import com.itextpdf.text.pdf.PdfWriter;
    import com.prolificinteractive.materialcalendarview.CalendarDay;
    import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

    import java.io.File;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.lang.reflect.Type;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.Set;
    import java.util.TreeMap;

    public class CalendarActivity extends AppCompatActivity {
TextView monthTotalAmount ;
        MaterialCalendarView calendarView;
        MaterialButton todayButton, exportButton;
        TextView selectedDateText, selectedDateTotal, selectedDateCount;
        TextView   expenseDaysCount, avgPerDay;
        CardView selectedDateCard, monthSummaryCard;
        private boolean isCardAnimating = false;

        HashMap<String, List<ExpenseModel>> expenseMap = new HashMap<>();
      //  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
      SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_calendar);
            calendarView = findViewById(R.id.calendarView);
             expenseDaysCount = findViewById(R.id.expenseDaysCount);
            avgPerDay = findViewById(R.id.avgPerDay);
            selectedDateText = findViewById(R.id.selectedDateText);
            selectedDateTotal = findViewById(R.id.selectedDateTotal);
            selectedDateCount = findViewById(R.id.selectedDateCount);
            selectedDateCard = findViewById(R.id.selectedDateCard);
            monthSummaryCard = findViewById(R.id.monthSummaryCard);
             exportButton = findViewById(R.id.exportButton);
            findViewById(R.id.backButton).setOnClickListener(v -> finish());
            loadExpenses();
            monthTotalAmount = findViewById(R.id.monthTotalAmount);
            findViewById(R.id.statsButton).setOnClickListener(v -> {
                animateButtonClick(v);
                showMonthlyStats();
            });
            todayButton = findViewById(R.id.todayButton);

            todayButton.setOnClickListener(v -> {
                animateButtonClick(v);
                goToToday();
            });
            showMonthlyStats1();
            exportButton.setOnClickListener(v -> {
                animateButtonClick(v);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    generatePdf();
                }
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
            calendarView.addDecorator(new EventDecorator(Color.RED, decoratedDays));
        }

        private void showExpenseDetails(String date, List<ExpenseModel> expenses) {
            StringBuilder builder = new StringBuilder("Expenses on " + date + ":\n\n");
            double total = 0;
            for (ExpenseModel e : expenses) {
                builder.append("₹").append(e.getAmount()).append(" - ").append(e.getNote()).append("\n");
                total += e.getAmount();
            }
            builder.append("\nTotal: ₹").append(total);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Expense Details")
                    .setMessage(builder.toString())
                    .setPositiveButton("OK", null)
                    .show();
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
                    .setNeutralButton("📤 Export", (dialog, which) -> generatePdf())
                    .show();
        }
        private void showMonthlyStats1() {
            // Show comprehensive monthly statistics
            Calendar currentMonth = Calendar.getInstance();
            CalendarDay currentCalendarDay = calendarView.getCurrentDate();
            if (currentCalendarDay != null) {
                currentMonth.set(Calendar.YEAR, currentCalendarDay.getYear());
                currentMonth.set(Calendar.MONTH, currentCalendarDay.getMonth());
            }

            double monthTotal1 = 0;
            int totalTransactions = 0;
            int daysWithExpenses1 = 0;

            for (String dateKey : expenseMap.keySet()) {
                try {
                    Calendar expenseDate = Calendar.getInstance();
                    expenseDate.setTime(sdf.parse(dateKey));

                    if (expenseDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                            expenseDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {

                        List<ExpenseModel> expenses = expenseMap.get(dateKey);
                        for (ExpenseModel expense : expenses) {
                            monthTotal1 += expense.getAmount();
                            totalTransactions++;
                        }
                        daysWithExpenses1++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.getTime());
//            expenseDaysCount.setText( String.valueOf(daysWithExpenses1));

            if (daysWithExpenses1 > 0) {
                avgPerDay.setText("₹ " +(int) (monthTotal1 / daysWithExpenses1));

            }
            monthTotalAmount.setText("₹ " +(int) monthTotal1);

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
        private void goToToday() {
            calendarView.setSelectedDate(CalendarDay.today());
            calendarView.setCurrentDate(CalendarDay.today());
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
//            animateCounterValue(monthTotalAmount, monthTotal, "₹");
//            animateCounterValue(expenseDaysCount, daysWithExpenses, "");
//            animateCounterValue(avgPerDay, avgPerDayValue, "₹");
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
        private void generatePdf() {
            Map<String, Double> dailyTotals = getDailyTotals();
            double overallTotal = 0;
            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);

            String name = prefs.getString("name", "Guest");
            String email = prefs.getString("email", "N/A");
            String phone = prefs.getString("phone", "N/A");
            //  String fileName = "ExpenseSummary_" + System.currentTimeMillis() + ".pdf";
            String fileName = "ExpenseSummary_" + name + System.currentTimeMillis() +".pdf";
            Document document = new Document();


            try {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                } else {
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!path.exists()) path.mkdirs();
                    File file = new File(path, fileName);
                    uri = Uri.fromFile(file);
                }

                if (uri == null) {
                    Toast.makeText(this, "Failed to create file!", Toast.LENGTH_SHORT).show();
                    return;
                }

                PdfWriter.getInstance(document, getContentResolver().openOutputStream(uri));
                document.open();

                // Fonts
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
                Font subFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                Font tableFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                Font nameFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.DARK_GRAY);

                // Title
                Paragraph title = new Paragraph("Expense Summary", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));

                // User Info
                PdfPTable userInfoTable = new PdfPTable(1);
                userInfoTable.setWidthPercentage(100);
                userInfoTable.setSpacingAfter(10f);

                PdfPCell userInfoCell = new PdfPCell();
                userInfoCell.setPadding(10);
                userInfoCell.setBackgroundColor(new BaseColor(240, 240, 240));
                userInfoCell.setPhrase(new Phrase("Name: " + name + "\nEmail: " + email + "\nPhone: " + phone, subFont));
                userInfoTable.addCell(userInfoCell);
                document.add(userInfoTable);

                // Table Header
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{2f, 1f});

                PdfPCell dateHeader = new PdfPCell(new Phrase("Date", headerFont));
                dateHeader.setBackgroundColor(BaseColor.DARK_GRAY);
                dateHeader.setPadding(8);
                table.addCell(dateHeader);

                PdfPCell amountHeader = new PdfPCell(new Phrase("Amount (₹)", headerFont));
                amountHeader.setBackgroundColor(BaseColor.DARK_GRAY);
                amountHeader.setPadding(8);
                table.addCell(amountHeader);

                // Table Rows
                for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                    table.addCell(new PdfPCell(new Phrase(entry.getKey(), tableFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%.2f", entry.getValue()), tableFont)));
                    overallTotal += entry.getValue();
                }

                document.add(table);

                // Total Amount
                Paragraph total = new Paragraph("Overall Total: ₹" + String.format(Locale.getDefault(), "%.2f", overallTotal), subFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);

                // Signature
                document.add(new Paragraph("\n\n"));
                Paragraph signature = new Paragraph(name, nameFont); // Set dynamic user name
                signature.setAlignment(Element.ALIGN_RIGHT);
                document.add(signature);

                document.close();
                Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
            }
        }
        private Map<String, Double> getDailyTotals() {
            SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
            String json = prefs.getString("expense_list", null);
            Type type = new TypeToken<List<ExpenseModel>>() {}.getType();
            List<ExpenseModel> expenses = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);

            Map<String, Double> dateTotals = new TreeMap<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()); // from model
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());   // for graph

            for (ExpenseModel model : expenses) {
                try {
                    Date date = inputFormat.parse(model.getDate());
                    String displayDate = displayFormat.format(date);
                    dateTotals.put(displayDate, dateTotals.getOrDefault(displayDate, 0.0) + model.getAmount());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Fill missing dates for this month
            Calendar start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH, 1);
            Calendar today = Calendar.getInstance();

            while (!start.after(today)) {
                String displayDate = displayFormat.format(start.getTime());
                dateTotals.putIfAbsent(displayDate, 0.0);
                start.add(Calendar.DAY_OF_MONTH, 1);
            }

            return dateTotals;
        }

    }
