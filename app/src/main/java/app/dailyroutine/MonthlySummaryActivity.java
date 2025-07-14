package app.dailyroutine;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
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

import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MonthlySummaryActivity extends AppCompatActivity {

    // UI Components
    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;
    private MaterialButton backButton, menuButton, downloadPdfButton, shareButton;
    private MaterialButton lineChartButton, barChartButton, pieChartButton;
    private MaterialButtonToggleGroup chartTypeToggle;
    private TextView totalSpentText, avgDailyText, highestDayText, loadingText;
    private RecyclerView insightsRecyclerView;
    private LinearLayout loadingOverlay;
    private CircularProgressIndicator chartLoadingIndicator;

    // Data
    private Map<String, Double> dailyTotals;
    private List<InsightModel> insights;
    private InsightsAdapter insightsAdapter;
    private double totalSpent = 0;
    private double avgDaily = 0;
    private double highestDay = 0;

    private static final int ANIMATION_DURATION = 300;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_summary);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        initializeEmptyCharts(); // Initialize charts with empty data first
        loadData(); // This will load data and then setup charts properly
        animateViewsIn();
    }

    private void initViews() {
        try {
            // Charts
            lineChart = findViewById(R.id.lineChart);
            barChart = findViewById(R.id.barChart);
            pieChart = findViewById(R.id.pieChart);

            // Buttons
            backButton = findViewById(R.id.backButton);
            menuButton = findViewById(R.id.menuButton);
            downloadPdfButton = findViewById(R.id.downloadPdfButton);
            shareButton = findViewById(R.id.shareButton);

            // Chart type buttons
            chartTypeToggle = findViewById(R.id.chartTypeToggle);
            lineChartButton = findViewById(R.id.lineChartButton);
            barChartButton = findViewById(R.id.barChartButton);
            pieChartButton = findViewById(R.id.pieChartButton);

            // Text views
            totalSpentText = findViewById(R.id.totalSpentText);
            avgDailyText = findViewById(R.id.avgDailyText);
            highestDayText = findViewById(R.id.highestDayText);
            loadingText = findViewById(R.id.loadingText);

            // Other components
            insightsRecyclerView = findViewById(R.id.insightsRecyclerView);
            loadingOverlay = findViewById(R.id.loadingOverlay);
            chartLoadingIndicator = findViewById(R.id.chartLoadingIndicator);

            // Set default selection
            if (chartTypeToggle != null) {
                chartTypeToggle.check(R.id.lineChartButton);
            }

            Log.d("MonthlySummary", "Views initialized successfully");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error initializing views", e);
        }
    }

    private void setupClickListeners() {
        try {
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    animateViewsOut();
                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, ANIMATION_DURATION);
                });
            }

            if (menuButton != null) {
                menuButton.setOnClickListener(v -> showMenuOptions(v));
            }

            if (downloadPdfButton != null) {
                downloadPdfButton.setOnClickListener(v -> {
                    animateButtonClick(downloadPdfButton);
                    checkPermissionAndGeneratePdf();
                });
            }

            if (shareButton != null) {
                shareButton.setOnClickListener(v -> {
                    animateButtonClick(shareButton);
                    shareReport();
                });
            }

            if (chartTypeToggle != null) {
                chartTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                    if (isChecked && dailyTotals != null) {
                        switchChart(checkedId);
                    }
                });
            }

            Log.d("MonthlySummary", "Click listeners setup successfully");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up click listeners", e);
        }
    }

    private void setupRecyclerView() {
        try {
            insights = new ArrayList<>();
            insightsAdapter = new InsightsAdapter(insights);

            if (insightsRecyclerView != null) {
                insightsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                insightsRecyclerView.setAdapter(insightsAdapter);
            }

            Log.d("MonthlySummary", "RecyclerView setup successfully");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up RecyclerView", e);
        }
    }

    private void initializeEmptyCharts() {
        try {
            // Initialize charts with empty data to prevent crashes
            if (lineChart != null) {
                lineChart.clear();
                lineChart.getDescription().setEnabled(false);
                lineChart.setNoDataText("Loading data...");
                lineChart.invalidate();
            }

            if (barChart != null) {
                barChart.clear();
                barChart.getDescription().setEnabled(false);
                barChart.setNoDataText("Loading data...");
                barChart.invalidate();
            }

            if (pieChart != null) {
                pieChart.clear();
                pieChart.getDescription().setEnabled(false);
                pieChart.setNoDataText("Loading data...");
                pieChart.invalidate();
            }

            Log.d("MonthlySummary", "Empty charts initialized");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error initializing empty charts", e);
        }
    }

    private void loadData() {
        try {
            showChartLoading(true);

            // Simulate loading delay for better UX
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    dailyTotals = getDailyTotals();

                    if (dailyTotals != null) {
                        calculateStatistics();
                        updateSummaryCards();
                        generateInsights();
                        setupCharts(); // Only setup charts after data is loaded
                        Log.d("MonthlySummary", "Data loaded successfully. Total entries: " + dailyTotals.size());
                    } else {
                        Log.w("MonthlySummary", "No data found, using empty dataset");
                        dailyTotals = new TreeMap<>();
                        setupCharts();
                    }

                    showChartLoading(false);
                } catch (Exception e) {
                    Log.e("MonthlySummary", "Error in data loading", e);
                    showChartLoading(false);
                    showErrorMessage("Error loading data");
                }
            }, 1000);
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error starting data load", e);
            showErrorMessage("Error starting data load");
        }
    }

    private Map<String, Double> getDailyTotals() {
        try {
            SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
            String json = prefs.getString("expense_list", null);

            Log.d("MonthlySummary", "Expense JSON: " + (json != null ? "Found" : "Not found"));

            Type type = new TypeToken<List<ExpenseModel>>() {}.getType();
            List<ExpenseModel> expenses = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);

            Map<String, Double> dateTotals = new TreeMap<>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

            // Process expenses
            if (expenses != null) {
                for (ExpenseModel model : expenses) {
                    try {
                        if (model != null && model.getDate() != null) {
                            Date date = inputFormat.parse(model.getDate());
                            if (date != null) {
                                String displayDate = displayFormat.format(date);
                                double currentAmount = dateTotals.getOrDefault(displayDate, 0.0);
                                dateTotals.put(displayDate, currentAmount + model.getAmount());
                            }
                        }
                    } catch (ParseException e) {
                        Log.e("MonthlySummary", "Error parsing date: " + model.getDate(), e);
                    }
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

            Log.d("MonthlySummary", "Processed " + dateTotals.size() + " days of data");
            return dateTotals;

        } catch (Exception e) {
            Log.e("MonthlySummary", "Error getting daily totals", e);
            return new TreeMap<>(); // Return empty map instead of null
        }
    }

    private void calculateStatistics() {
        try {
            if (dailyTotals == null || dailyTotals.isEmpty()) {
                totalSpent = 0;
                avgDaily = 0;
                highestDay = 0;
                return;
            }

            totalSpent = 0;
            highestDay = 0;
            int daysWithExpenses = 0;

            for (Double amount : dailyTotals.values()) {
                if (amount != null) {
                    totalSpent += amount;
                    if (amount > 0) daysWithExpenses++;
                    if (amount > highestDay) highestDay = amount;
                }
            }

            avgDaily = daysWithExpenses > 0 ? totalSpent / daysWithExpenses : 0;

            Log.d("MonthlySummary", String.format("Statistics - Total: %.2f, Avg: %.2f, Highest: %.2f",
                    totalSpent, avgDaily, highestDay));
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error calculating statistics", e);
        }
    }

    private void updateSummaryCards() {
        try {
            // Animate the values
            if (totalSpentText != null) {
                animateValue(totalSpentText, 0, totalSpent, "₹");
            }
            if (avgDailyText != null) {
                animateValue(avgDailyText, 0, avgDaily, "₹");
            }
            if (highestDayText != null) {
                animateValue(highestDayText, 0, highestDay, "₹");
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error updating summary cards", e);
        }
    }

    private void animateValue(TextView textView, double startValue, double endValue, String prefix) {
        try {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                try {
                    float fraction = animation.getAnimatedFraction();
                    double currentValue = startValue + (endValue - startValue) * fraction;
                    textView.setText(prefix + String.format(Locale.getDefault(), "%.0f", currentValue));
                } catch (Exception e) {
                    Log.e("MonthlySummary", "Error in animation update", e);
                }
            });
            animator.start();
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error animating value", e);
            // Fallback to direct text setting
            textView.setText(prefix + String.format(Locale.getDefault(), "%.0f", endValue));
        }
    }

    private void generateInsights() {
        try {
            if (insights == null) {
                insights = new ArrayList<>();
            }
            insights.clear();

            if (dailyTotals == null || dailyTotals.isEmpty()) {
                insights.add(new InsightModel(
                        "📊 No Data",
                        "Start adding expenses to see insights",
                        InsightModel.InsightType.INFO
                ));
                if (insightsAdapter != null) {
                    insightsAdapter.notifyDataSetChanged();
                }
                return;
            }

            // Spending trend insight
            if (totalSpent > 0) {
                String trend = avgDaily > (totalSpent / dailyTotals.size()) ? "increasing" : "stable";
                insights.add(new InsightModel(
                        "📈 Spending Trend",
                        "Your daily spending is " + trend + " this month",
                        InsightModel.InsightType.TREND
                ));
            }

            // Highest spending day
            if (highestDay > 0) {
                String highestDayName = "";
                for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().equals(highestDay)) {
                        highestDayName = entry.getKey();
                        break;
                    }
                }
                insights.add(new InsightModel(
                        "💸 Peak Spending",
                        "Your highest spending was ₹" + String.format(Locale.getDefault(), "%.0f", highestDay) + " on " + highestDayName,
                        InsightModel.InsightType.WARNING
                ));
            }

            // Budget recommendation
            double recommendedDaily = totalSpent * 0.8 / Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            insights.add(new InsightModel(
                    "💡 Budget Tip",
                    "Try to limit daily spending to ₹" + String.format(Locale.getDefault(), "%.0f", recommendedDaily) + " to save 20%",
                    InsightModel.InsightType.TIP
            ));

            // Savings potential
            double potentialSavings = totalSpent * 0.15;
            insights.add(new InsightModel(
                    "💰 Savings Potential",
                    "You could save ₹" + String.format(Locale.getDefault(), "%.0f", potentialSavings) + " by reducing expenses by 15%",
                    InsightModel.InsightType.SAVINGS
            ));

            if (insightsAdapter != null) {
                insightsAdapter.notifyDataSetChanged();
            }

            Log.d("MonthlySummary", "Generated " + insights.size() + " insights");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error generating insights", e);
        }
    }

    private void setupCharts() {
        try {
            if (dailyTotals == null) {
                Log.w("MonthlySummary", "Cannot setup charts - dailyTotals is null");
                return;
            }

            setupLineChart();
            setupBarChart();
            setupPieChart();

            Log.d("MonthlySummary", "Charts setup completed");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up charts", e);
        }
    }

    private void setupLineChart() {
        try {
            if (lineChart == null || dailyTotals == null) {
                Log.w("MonthlySummary", "Cannot setup line chart - null components");
                return;
            }

            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            int index = 0;
            for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    entries.add(new Entry(index, entry.getValue().floatValue()));
                    labels.add(entry.getKey());
                    index++;
                }
            }

            if (entries.isEmpty()) {
                lineChart.clear();
                lineChart.setNoDataText("No expense data available");
                lineChart.invalidate();
                return;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Daily Spending (₹)");
            dataSet.setColor(getColor(R.color.primary_color));
            dataSet.setCircleColor(getColor(R.color.primary_color));
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(6f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getColor(R.color.primary_light));
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setValueTextSize(10f);
            dataSet.setValueTextColor(getColor(R.color.text_primary));

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // Customize chart
            lineChart.getDescription().setEnabled(false);
            lineChart.setTouchEnabled(true);
            lineChart.setPinchZoom(true);
            lineChart.setScaleEnabled(true);
            lineChart.setDrawGridBackground(false);
            lineChart.setExtraOffsets(10, 10, 10, 20);

            // X-axis
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(getColor(R.color.text_secondary));
            xAxis.setTextSize(10f);
            xAxis.setDrawGridLines(false);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int idx = (int) value;
                    return (idx >= 0 && idx < labels.size()) ? labels.get(idx) : "";
                }
            });

            // Y-axis
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTextColor(getColor(R.color.text_secondary));
            leftAxis.setDrawGridLines(true);
            leftAxis.setGridColor(getColor(R.color.light_gray));
            lineChart.getAxisRight().setEnabled(false);

            // Legend
            Legend legend = lineChart.getLegend();
            legend.setTextColor(getColor(R.color.text_primary));
            legend.setTextSize(12f);

            lineChart.animateXY(1000, 1000);
            lineChart.invalidate();

            Log.d("MonthlySummary", "Line chart setup completed with " + entries.size() + " entries");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up line chart", e);
        }
    }

    private void setupBarChart() {
        try {
            if (barChart == null || dailyTotals == null) {
                Log.w("MonthlySummary", "Cannot setup bar chart - null components");
                return;
            }

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            int index = 0;
            for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    entries.add(new BarEntry(index, entry.getValue().floatValue()));
                    labels.add(entry.getKey());
                    index++;
                }
            }

            if (entries.isEmpty()) {
                barChart.clear();
                barChart.setNoDataText("No expense data available");
                barChart.invalidate();
                return;
            }

            BarDataSet dataSet = new BarDataSet(entries, "Daily Spending (₹)");
            dataSet.setColor(getColor(R.color.secondary_color));
            dataSet.setValueTextSize(10f);
            dataSet.setValueTextColor(getColor(R.color.text_primary));

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.8f);
            barChart.setData(barData);

            // Customize chart
            barChart.getDescription().setEnabled(false);
            barChart.setTouchEnabled(true);
            barChart.setPinchZoom(false);
            barChart.setDrawGridBackground(false);
            barChart.setExtraOffsets(10, 10, 10, 20);

            // X-axis
            XAxis xAxis = barChart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(getColor(R.color.text_secondary));
            xAxis.setTextSize(10f);
            xAxis.setDrawGridLines(false);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int idx = (int) value;
                    return (idx >= 0 && idx < labels.size()) ? labels.get(idx) : "";
                }
            });

            // Y-axis
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTextColor(getColor(R.color.text_secondary));
            leftAxis.setDrawGridLines(true);
            leftAxis.setGridColor(getColor(R.color.light_gray));
            barChart.getAxisRight().setEnabled(false);

            // Legend
            Legend legend = barChart.getLegend();
            legend.setTextColor(getColor(R.color.text_primary));
            legend.setTextSize(12f);

            barChart.animateY(1000);
            barChart.invalidate();

            Log.d("MonthlySummary", "Bar chart setup completed");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up bar chart", e);
        }
    }

    private void setupPieChart() {
        try {
            if (pieChart == null || dailyTotals == null) {
                Log.w("MonthlySummary", "Cannot setup pie chart - null components");
                return;
            }

            // Group data by week for pie chart
            Map<String, Double> weeklyData = new HashMap<>();
            Calendar cal = Calendar.getInstance();

            for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                try {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                        Date date = sdf.parse(entry.getKey());
                        if (date != null) {
                            cal.setTime(date);
                            int week = cal.get(Calendar.WEEK_OF_MONTH);
                            String weekLabel = "Week " + week;
                            weeklyData.put(weekLabel, weeklyData.getOrDefault(weekLabel, 0.0) + entry.getValue());
                        }
                    }
                } catch (Exception e) {
                    Log.e("MonthlySummary", "Error processing pie chart entry", e);
                }
            }

            List<PieEntry> entries = new ArrayList<>();
            int[] colors = {
                    getColor(R.color.primary_color),
                    getColor(R.color.secondary_color),
                    getColor(R.color.success_color),
                    getColor(R.color.warning_color),
                    getColor(R.color.error_color)
            };

            for (Map.Entry<String, Double> entry : weeklyData.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
                }
            }

            if (entries.isEmpty()) {
                pieChart.clear();
                pieChart.setNoDataText("No expense data available");
                pieChart.invalidate();
                return;
            }

            PieDataSet dataSet = new PieDataSet(entries, "Weekly Distribution");
            dataSet.setColors(colors);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(getColor(R.color.white));
            dataSet.setSliceSpace(3f);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);

            // Customize chart
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.TRANSPARENT);
            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(45f);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);

            // Legend
            Legend legend = pieChart.getLegend();
            legend.setTextColor(getColor(R.color.text_primary));
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

            pieChart.animateY(1000);
            pieChart.invalidate();

            Log.d("MonthlySummary", "Pie chart setup completed");
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error setting up pie chart", e);
        }
    }

    private void switchChart(int checkedId) {
        try {
            // Hide all charts
            if (lineChart != null) lineChart.setVisibility(View.GONE);
            if (barChart != null) barChart.setVisibility(View.GONE);
            if (pieChart != null) pieChart.setVisibility(View.GONE);

            // Show selected chart with animation
            View selectedChart = null;
            if (checkedId == R.id.lineChartButton && lineChart != null) {
                selectedChart = lineChart;
            } else if (checkedId == R.id.barChartButton && barChart != null) {
                selectedChart = barChart;
            } else if (checkedId == R.id.pieChartButton && pieChart != null) {
                selectedChart = pieChart;
            }

            if (selectedChart != null) {
                selectedChart.setVisibility(View.VISIBLE);
                selectedChart.setAlpha(0f);
                selectedChart.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error switching chart", e);
        }
    }

    private void checkPermissionAndGeneratePdf() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                generatePdf();
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error checking permissions", e);
            showErrorMessage("Error checking permissions");
        }
    }

    private void generatePdf() {
        showLoading(true, "Generating PDF...");

        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                String name = prefs.getString("name", "Guest");
                String email = prefs.getString("email", "N/A");
                String phone = prefs.getString("phone", "N/A");

                String fileName = "ExpenseSummary_" + name + "_" + System.currentTimeMillis() + ".pdf";
                Document document = new Document();

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
                    runOnUiThread(() -> {
                        showLoading(false, "");
                        showErrorMessage("Failed to create file!");
                    });
                    return;
                }

                PdfWriter.getInstance(document, getContentResolver().openOutputStream(uri));
                document.open();

                // Enhanced PDF generation with better formatting
                generateEnhancedPdf(document, name, email, phone);

                document.close();

                runOnUiThread(() -> {
                    showLoading(false, "");
                    Snackbar.make(findViewById(android.R.id.content), "PDF saved to Downloads", Snackbar.LENGTH_LONG)
                            .setAction("OPEN", v -> openPdf(uri))
                            .show();
                });

            } catch (Exception e) {
                Log.e("MonthlySummary", "Error generating PDF", e);
                runOnUiThread(() -> {
                    showLoading(false, "");
                    showErrorMessage("Error creating PDF");
                });
            }
        }).start();
    }

    private void generateEnhancedPdf(Document document, String name, String email, String phone) throws Exception {
        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLUE);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
        Font subFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font tableFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        // Title
        Paragraph title = new Paragraph("Monthly Expense Summary", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        // Date range
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Paragraph dateRange = new Paragraph("Report for: " + monthFormat.format(new Date()), headerFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(20f);
        document.add(dateRange);

        // User info table
        PdfPTable userTable = new PdfPTable(2);
        userTable.setWidthPercentage(100);
        userTable.setSpacingAfter(20f);
        userTable.setWidths(new float[]{1f, 2f});

        addTableRow(userTable, "Name:", name, subFont, subFont);
        addTableRow(userTable, "Email:", email, subFont, subFont);
        addTableRow(userTable, "Phone:", phone, subFont, subFont);
        addTableRow(userTable, "Generated:", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()), subFont, subFont);

        document.add(userTable);

        // Summary statistics
        Paragraph summaryHeader = new Paragraph("Summary Statistics", headerFont);
        summaryHeader.setSpacingBefore(10f);
        summaryHeader.setSpacingAfter(10f);
        document.add(summaryHeader);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20f);
        summaryTable.setWidths(new float[]{1f, 1f});

        addTableRow(summaryTable, "Total Spent:", "₹" + String.format(Locale.getDefault(), "%.2f", totalSpent), subFont, subFont);
        addTableRow(summaryTable, "Average Daily:", "₹" + String.format(Locale.getDefault(), "%.2f", avgDaily), subFont, subFont);
        addTableRow(summaryTable, "Highest Day:", "₹" + String.format(Locale.getDefault(), "%.2f", highestDay), subFont, subFont);
        addTableRow(summaryTable, "Days with Expenses:", String.valueOf(getDaysWithExpenses()), subFont, subFont);

        document.add(summaryTable);

        // Daily breakdown
        if (dailyTotals != null && !dailyTotals.isEmpty()) {
            Paragraph dailyHeader = new Paragraph("Daily Breakdown", headerFont);
            dailyHeader.setSpacingBefore(10f);
            dailyHeader.setSpacingAfter(10f);
            document.add(dailyHeader);

            PdfPTable dailyTable = new PdfPTable(2);
            dailyTable.setWidthPercentage(100);
            dailyTable.setSpacingAfter(20f);
            dailyTable.setWidths(new float[]{1f, 1f});

            // Table headers
            PdfPCell dateHeader = new PdfPCell(new Phrase("Date", tableHeaderFont));
            dateHeader.setBackgroundColor(BaseColor.DARK_GRAY);
            dateHeader.setPadding(8);
            dailyTable.addCell(dateHeader);

            PdfPCell amountHeader = new PdfPCell(new Phrase("Amount (₹)", tableHeaderFont));
            amountHeader.setBackgroundColor(BaseColor.DARK_GRAY);
            amountHeader.setPadding(8);
            dailyTable.addCell(amountHeader);

            // Table rows
            for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) { // Only show days with expenses
                    PdfPCell dateCell = new PdfPCell(new Phrase(entry.getKey(), tableFont));
                    dateCell.setPadding(6);
                    dailyTable.addCell(dateCell);

                    PdfPCell amountCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%.2f", entry.getValue()), tableFont));
                    amountCell.setPadding(6);
                    amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    dailyTable.addCell(amountCell);
                }
            }

            document.add(dailyTable);
        }

        // Insights section
        if (insights != null && !insights.isEmpty()) {
            Paragraph insightsHeader = new Paragraph("Smart Insights", headerFont);
            insightsHeader.setSpacingBefore(10f);
            insightsHeader.setSpacingAfter(10f);
            document.add(insightsHeader);

            for (InsightModel insight : insights) {
                if (insight != null) {
                    Paragraph insightPara = new Paragraph("• " + insight.getTitle() + ": " + insight.getDescription(), subFont);
                    insightPara.setSpacingAfter(5f);
                    document.add(insightPara);
                }
            }
        }

        // Footer
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Generated by Pocket Money Manager", subFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(6);
        labelCell.setBorder(0);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(6);
        valueCell.setBorder(0);
        table.addCell(valueCell);
    }

    private int getDaysWithExpenses() {
        if (dailyTotals == null) return 0;

        int count = 0;
        for (Double amount : dailyTotals.values()) {
            if (amount != null && amount > 0) count++;
        }
        return count;
    }

    private void openPdf(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error opening PDF", e);
            showErrorMessage("No PDF viewer found");
        }
    }

    private void shareReport() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Monthly Expense Summary");

            StringBuilder shareText = new StringBuilder();
            shareText.append("📊 Monthly Expense Summary\n\n");
            shareText.append("💰 Total Spent: ₹").append(String.format(Locale.getDefault(), "%.2f", totalSpent)).append("\n");
            shareText.append("📅 Average Daily: ₹").append(String.format(Locale.getDefault(), "%.2f", avgDaily)).append("\n");
            shareText.append("📈 Highest Day: ₹").append(String.format(Locale.getDefault(), "%.2f", highestDay)).append("\n\n");
            shareText.append("Generated by Pocket Money Manager");

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            startActivity(Intent.createChooser(shareIntent, "Share Report"));
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error sharing report", e);
            showErrorMessage("Error sharing report");
        }
    }

    private void showMenuOptions(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu, popupMenu.getMenu());


        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                Intent intent = new Intent(MonthlySummaryActivity.this, MainActivity.class);
                startActivity(intent);
                 return true;
            } else if (id == R.id.menu_profile) {
                Intent intent1 = new Intent(MonthlySummaryActivity.this, ProfileActivity.class);
                startActivity(intent1);
                return true;
            } else if (id == R.id.menu_settings) {
                Intent intent2 = new Intent(MonthlySummaryActivity.this, SettingsActivity.class);
                startActivity(intent2);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showLoading(boolean show, String message) {
        try {
            if (loadingText != null && loadingOverlay != null) {
                if (show) {
                    loadingText.setText(message);
                    loadingOverlay.setVisibility(View.VISIBLE);
                    loadingOverlay.setAlpha(0f);
                    loadingOverlay.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION)
                            .start();
                } else {
                    loadingOverlay.animate()
                            .alpha(0f)
                            .setDuration(ANIMATION_DURATION)
                            .setListener(new android.animation.AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(android.animation.Animator animation) {
                                    loadingOverlay.setVisibility(View.GONE);
                                }
                            })
                            .start();
                }
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error showing loading", e);
        }
    }

    private void showChartLoading(boolean show) {
        try {
            if (chartLoadingIndicator != null) {
                chartLoadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error showing chart loading", e);
        }
    }

    private void showErrorMessage(String message) {
        try {
            if (findViewById(android.R.id.content) != null) {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error showing error message", e);
        }
    }

    // Animation Methods
    private void animateViewsIn() {
        try {
            View[] views = {totalSpentText, avgDailyText, highestDayText, chartTypeToggle, downloadPdfButton, shareButton};

            for (int i = 0; i < views.length; i++) {
                View view = views[i];
                if (view != null) {
                    view.setAlpha(0f);
                    view.setTranslationY(50f);
                    view.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(ANIMATION_DURATION)
                            .setStartDelay(i * 100L)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error animating views in", e);
        }
    }

    private void animateViewsOut() {
        try {
            View[] views = {totalSpentText, avgDailyText, highestDayText, lineChart, barChart, pieChart};

            for (View view : views) {
                if (view != null) {
                    view.animate()
                            .alpha(0f)
                            .translationY(-50f)
                            .setDuration(ANIMATION_DURATION)
                            .start();
                }
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error animating views out", e);
        }
    }

    private void animateButtonClick(View button) {
        try {
            if (button != null) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f);

                scaleX.setDuration(150);
                scaleY.setDuration(150);

                scaleX.start();
                scaleY.start();
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error animating button click", e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            animateViewsOut();
            new Handler(Looper.getMainLooper()).postDelayed(super::onBackPressed, ANIMATION_DURATION);
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error on back pressed", e);
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    generatePdf();
                } else {
                    showErrorMessage("Permission required to save PDF");
                }
            }
        } catch (Exception e) {
            Log.e("MonthlySummary", "Error handling permission result", e);
        }
    }
}
