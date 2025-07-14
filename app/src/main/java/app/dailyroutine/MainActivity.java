package app.dailyroutine;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executor;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MainActivity extends AppCompatActivity implements EnhancedReminderAdapter.OnReminderActionListener {
    MaterialCardView percentageButton;

    // Permission request codes - MATCH WITH AddExpenseDialog
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_GALLERY = 1002;
    private static final int VOICE_INPUT_REQUEST_CODE = 300;
    private static final int CAMERA_PERMISSION_CODE = 1003;

    MaterialCardView cvMenuBar;
    ExtendedFloatingActionButton mainFab;

    // UI Components
    RecyclerView recyclerView;
    TextView totalText, emptyText, budgetStatusText;
    ImageView filterButton, clearFilterButton;
    MaterialCardView filterLayout, clearFilterLayout;
    List<ExpenseModel> expenses;
    ExpenseAdapter adapter;
    ExpenseModel recentlyDeletedExpense;
    int recentlyDeletedPosition = -1;
    double monthlyBudget = 0.0;

    // New UI Components for modern layout
    TextView greetingText, todayDateText, percentageText, budgetRemainingText;
    MaterialCardView addExpenseCard, setBudgetCard, analyticsCard;

    // Enhanced Reminder System
    private SmartReminderManager reminderManager;
    private EnhancedReminderAdapter enhancedReminderAdapter;
    private List<EnhancedReminderModel> enhancedReminders;

    // Legacy reminder support
    private RecyclerView reminderRecyclerView;

    // Notification System Components
    private MaterialCardView notificationCard;
    private TextView notificationBadge;
    private View notificationOverlay;
    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notifications;
    private NotificationManager notificationManager;
    private boolean isNotificationPanelOpen = false;

    // Other components
    RelativeLayout profileCardContainer;
    TextView profileName, profileEmail, profilePhone, profileGender;
    ImageView closeProfile;
    private ProgressBar progressBar;
    TextView textView;
    DrawerLayout drawerLayout;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    // Photo and location related
    private String currentPhotoPath = "";
    private FusedLocationProviderClient fusedLocationClient;
    private AddExpenseDialog currentDialog;
LinearLayout layoutMenu;
    // Pending action types
    private enum PendingAction {
        NONE, CAMERA, GALLERY, LOCATION
    }
    private PendingAction pendingAction = PendingAction.NONE;
    private CustomNotificationManager customNotificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("biometric_prefs", MODE_PRIVATE);
        boolean isBiometricShown = prefs.getBoolean("isBiometricShown", false);

        if (!isBiometricShown) {
            showBiometricPrompt();
            prefs.edit().putBoolean("isBiometricShown", true).apply();
        }
        refreshUserData();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_budget_tracker);

        if (getIntent().getBooleanExtra("open_reminder_dialog", false)) {
            showEnhancedReminderDialog();
        }

        // Initialize enhanced reminder system
        reminderManager = new SmartReminderManager(this);

        // Initialize notification system
        initializeNotificationSystem();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MobileAds.initialize(this, initializationStatus -> {
            // Optionally check initializationStatus
        });

        initializeViews();
        setupNavigation();

        // Load data and create adapter
        loadExpenses();
        loadMonthlyBudget();
        createAdapter();

        setupExpenseList();
        setupBottomNavigation();
        setupPermissions();

        // Update UI
        updateTotalAnimated();
        updateBudgetProgress();
        toggleEmptyState();

        double todayTotal = getTodayTotalExpenses();
        updateSavingsStreak(todayTotal, 10.0);
        TextView streakText = findViewById(R.id.streakText);
        int currentStreak = getCurrentStreak();
        streakText.setText("🔥" + currentStreak + "-day");
        scheduleDailySmsWithWorkManager();
        updateBudgetPercentage();
        updateGreetingText();
        updateTodayDate();

        if (currentStreak == 7 || currentStreak == 30) {
            Toast.makeText(this, "🎉 Congrats! " + currentStreak + "-day streak!", Toast.LENGTH_LONG).show();
        }

        filterLayout.setVisibility(View.VISIBLE);
        checkAndShowBudgetPopup();

        // Generate initial notifications
        generateInitialNotifications();
        schedulePeriodicNotifications();
    }


private void initializeNotificationSystem() {
    customNotificationManager = new CustomNotificationManager(this); // FIXED
    notifications = new ArrayList<>();
    loadNotifications();
}
    private void initializeNotificationViews() {
        notificationCard = findViewById(R.id.notificationCard);
        notificationBadge = findViewById(R.id.notificationBadge);
        // FIXED: Find the notification badge properly
        View badgeView = notificationCard.findViewById(R.id.notificationBadge);
        if (badgeView instanceof TextView) {
            notificationBadge = (TextView) badgeView;
        }
        // Create notification overlay programmatically
        createNotificationOverlay();

        // Set click listener for notification icon
        notificationCard.setOnClickListener(v -> toggleNotificationPanel());

        updateNotificationBadge();
    }

//    private void createNotificationOverlay() {
//        // Create overlay layout
//        notificationOverlay = LayoutInflater.from(this).inflate(R.layout.notification_overlay, null);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                Gravity.TOP
//        );
//        // Add to root layout
//        ViewGroup rootLayout = findViewById(android.R.id.content);
//        rootLayout.addView(notificationOverlay);
//
//        // Initialize RecyclerView
//        notificationRecyclerView = notificationOverlay.findViewById(R.id.notificationRecyclerView);
//        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Create adapter
//        notificationAdapter = new NotificationAdapter(this, notifications, new NotificationAdapter.OnNotificationActionListener() {
//            @Override
//            public void onNotificationClick(NotificationModel notification) {
//                handleNotificationClick(notification);
//            }
//
//            @Override
//            public void onNotificationDismiss(NotificationModel notification) {
//                dismissNotification(notification);
//            }
//
//            @Override
//            public void onNotificationMarkAsRead(NotificationModel notification) {
//                markNotificationAsRead(notification);
//            }
//        });
//
//        notificationRecyclerView.setAdapter(notificationAdapter);
//
//        // Set up close button
//        View closeButton = notificationOverlay.findViewById(R.id.closeNotificationPanel);
//        closeButton.setOnClickListener(v -> closeNotificationPanel());
//
//        // Set up clear all button
//        View clearAllButton = notificationOverlay.findViewById(R.id.clearAllNotifications);
//        clearAllButton.setOnClickListener(v -> clearAllNotifications());
//
//        // Initially hide overlay
//        notificationOverlay.setVisibility(View.GONE);
//    }
private void createNotificationOverlay() {
    notificationOverlay = LayoutInflater.from(this)
            .inflate(R.layout.notification_overlay, null);

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.TOP
    );

    ViewGroup rootLayout = findViewById(android.R.id.content);
    rootLayout.addView(notificationOverlay, params);

    // RecyclerView setup
    notificationRecyclerView = notificationOverlay.findViewById(R.id.notificationRecyclerView);
    notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    notificationAdapter = new NotificationAdapter(this, notifications, new NotificationAdapter.OnNotificationActionListener() {
        @Override public void onNotificationClick(NotificationModel n) { handleNotificationClick(n); }
        @Override public void onNotificationDismiss(NotificationModel n) { dismissNotification(n); }
        @Override public void onNotificationMarkAsRead(NotificationModel n) { markNotificationAsRead(n); }
    });
    notificationRecyclerView.setAdapter(notificationAdapter);

    // Close & Clear buttons
    notificationOverlay.findViewById(R.id.closeNotificationPanel)
            .setOnClickListener(v -> closeNotificationPanel());

    notificationOverlay.findViewById(R.id.clearAllNotifications)
            .setOnClickListener(v -> clearAllNotifications());

    // Hide initially
    notificationOverlay.setVisibility(View.GONE);
}
    private void toggleNotificationPanel() {
        if (isNotificationPanelOpen) {
            closeNotificationPanel();
        } else {
            openNotificationPanel();
        }
    }

    private void openNotificationPanel() {
        isNotificationPanelOpen = true;
        notificationOverlay.setVisibility(View.VISIBLE);

        // Animate slide down
        TranslateAnimation slideDown = new TranslateAnimation(0, 0, -notificationOverlay.getHeight(), 0);
        slideDown.setDuration(300);
        slideDown.setFillAfter(true);
        notificationOverlay.startAnimation(slideDown);

        // Mark all notifications as read when panel opens
        markAllNotificationsAsRead();

        // Animate notification icon
        animateNotificationIcon();
    }

    private void closeNotificationPanel() {
        isNotificationPanelOpen = false;

        // Animate slide up
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, -notificationOverlay.getHeight());
        slideUp.setDuration(300);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                notificationOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        notificationOverlay.startAnimation(slideUp);
    }

    private void animateNotificationIcon() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        notificationCard.startAnimation(scaleAnimation);
    }

    private void handleNotificationClick(NotificationModel notification) {
        switch (notification.getType()) {
            case BUDGET_ALERT:
                showBudgetDetailsDialog();
                break;
            case SPENDING_TIP:
                showSpendingTipDialog(notification);
                break;
            case EXPENSE_REMINDER:
                showAddExpenseDialog();
                break;
            case SAVINGS_GOAL:
                showSavingsGoalDialog(notification);
                break;
            case WEEKLY_SUMMARY:
                Intent intent = new Intent(this, MonthlySummaryActivity.class);
                startActivity(intent);
                break;
        }

        markNotificationAsRead(notification);
        closeNotificationPanel();
    }

    private void dismissNotification(NotificationModel notification) {
        notifications.remove(notification);
        notificationAdapter.notifyDataSetChanged();
        updateNotificationBadge();
        saveNotifications();
    }

    private void markNotificationAsRead(NotificationModel notification) {
        notification.setRead(true);
        notificationAdapter.notifyDataSetChanged();
        updateNotificationBadge();
        saveNotifications();
    }

    private void markAllNotificationsAsRead() {
        for (NotificationModel notification : notifications) {
            notification.setRead(true);
        }
        notificationAdapter.notifyDataSetChanged();
        updateNotificationBadge();
        saveNotifications();
    }

    private void clearAllNotifications() {
        notifications.clear();
        notificationAdapter.notifyDataSetChanged();
        updateNotificationBadge();
        saveNotifications();
        closeNotificationPanel();
        Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
    }

    private void updateNotificationBadge() {
        int unreadCount = 0;
        for (NotificationModel notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }

        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(Math.min(unreadCount, 99)));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private void generateInitialNotifications() {
        // Clear existing notifications
        notifications.clear();

        // Generate budget alerts
        generateBudgetAlerts();

        // Generate spending tips
        generateSpendingTips();

        // Generate expense reminders
        generateExpenseReminders();

        // Generate savings goals
        generateSavingsGoals();

        // Update UI
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
        updateNotificationBadge();
        saveNotifications();
    }

    private void generateBudgetAlerts() {
        if (monthlyBudget <= 0) return;

        double totalExpenses = calculateTotalAmount();
        double percentage = (totalExpenses / monthlyBudget) * 100;

        if (percentage >= 90) {
            addNotification(new NotificationModel(
                    "Budget Alert",
                    "You've spent " + (int)percentage + "% of your monthly budget!",
                    NotificationModel.NotificationType.BUDGET_ALERT,
                    NotificationModel.Priority.HIGH,
                    System.currentTimeMillis()
            ));
        } else if (percentage >= 75) {
            addNotification(new NotificationModel(
                    "Budget Warning",
                    "You've spent " + (int)percentage + "% of your monthly budget. Consider reducing expenses.",
                    NotificationModel.NotificationType.BUDGET_ALERT,
                    NotificationModel.Priority.MEDIUM,
                    System.currentTimeMillis()
            ));
        }
    }

    private void generateSpendingTips() {
        String[] tips = {
                "💡 Try the 50/30/20 rule: 50% needs, 30% wants, 20% savings",
                "🛒 Make a shopping list before going to the store to avoid impulse purchases",
                "☕ Consider making coffee at home instead of buying it daily",
                "📱 Review your subscriptions and cancel unused ones",
                "🚗 Use public transport or carpool to save on fuel costs",
                "🍽️ Cook meals at home more often to reduce food expenses",
                "💳 Pay off credit card balances to avoid interest charges",
                "🎯 Set specific savings goals to stay motivated",
                "📊 Track your expenses daily to identify spending patterns",
                "💰 Use cashback apps and coupons when shopping"
        };

        // Add 2-3 random tips
        for (int i = 0; i < 3; i++) {
            String tip = tips[(int) (Math.random() * tips.length)];
            addNotification(new NotificationModel(
                    "Smart Spending Tip",
                    tip,
                    NotificationModel.NotificationType.SPENDING_TIP,
                    NotificationModel.Priority.LOW,
                    System.currentTimeMillis() + (i * 1000)
            ));
        }
    }

    private void generateExpenseReminders() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Evening reminder to log expenses
        if (hour >= 18) {
            addNotification(new NotificationModel(
                    "Daily Expense Check",
                    "Don't forget to log today's expenses before bed!",
                    NotificationModel.NotificationType.EXPENSE_REMINDER,
                    NotificationModel.Priority.MEDIUM,
                    System.currentTimeMillis()
            ));
        }
    }

    private void generateSavingsGoals() {
        double totalExpenses = calculateTotalAmount();
        double potentialSavings = monthlyBudget - totalExpenses;

        if (potentialSavings > 0) {
            addNotification(new NotificationModel(
                    "Savings Opportunity",
                    "Great job! You can save ₹" + (int)potentialSavings + " this month if you stick to your budget.",
                    NotificationModel.NotificationType.SAVINGS_GOAL,
                    NotificationModel.Priority.LOW,
                    System.currentTimeMillis()
            ));
        }
    }

    private void addNotification(NotificationModel notification) {
        notifications.add(0, notification); // Add to beginning of list

        // Limit to 50 notifications
        if (notifications.size() > 50) {
            notifications.remove(notifications.size() - 1);
        }
    }

//    private void schedulePeriodicNotifications() {
//        // Schedule daily tip notifications
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        // Use WorkManager for periodic notifications
//        Data inputData = new Data.Builder()
//                .putString("notification_type", "daily_tip")
//                .build();
//
//        PeriodicWorkRequest dailyTipWork = new PeriodicWorkRequest.Builder(
//                NotificationWorker.class,
//                24, TimeUnit.HOURS
//        )
//                .setInitialDelay(calendar.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .setInputData(inputData)
//                .build();
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//                "DailyTipNotification",
//                ExistingPeriodicWorkPolicy.REPLACE,
//                dailyTipWork
//        );
//    }
    private void schedulePeriodicNotifications() {
        // Schedule daily tip notifications using AlarmManager instead of WorkManager
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Simple daily notification scheduling
        scheduleNotificationAlarm(calendar.getTimeInMillis());
    }
    private void scheduleNotificationAlarm(long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EnhancedNotificationReceiver.class);
        intent.setAction("DAILY_TIP_NOTIFICATION");
        intent.putExtra("notification_type", "daily_tip");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }
    private void showSpendingTipDialog(NotificationModel notification) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💡 Smart Spending Tip")
                .setMessage(notification.getMessage())
                .setPositiveButton("Got it!", null)
                .setNeutralButton("More Tips", (dialog, which) -> {
                    // Generate more tips
                    generateSpendingTips();
                    if (notificationAdapter != null) {
                        notificationAdapter.notifyDataSetChanged();
                    }
                    updateNotificationBadge();
                })
                .show();
    }

    private void showSavingsGoalDialog(NotificationModel notification) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎯 Savings Goal")
                .setMessage(notification.getMessage())
                .setPositiveButton("Set Goal", (dialog, which) -> {
                    // Open savings goal activity or dialog
                    showSetBudgetDialog();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void saveNotifications() {
        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(notifications);
        editor.putString("notification_list", json);
        editor.apply();
    }

    private void loadNotifications() {
        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        String json = prefs.getString("notification_list", null);
        Type type = new TypeToken<ArrayList<NotificationModel>>() {}.getType();
        notifications = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }

    // ==================== EXISTING METHODS (keeping all your original functionality) ====================

    private void checkAndShowBudgetPopup() {
        new Handler().postDelayed(() -> {
            if (BudgetSetupDialog.shouldShowBudgetDialog(this)) {
                showBudgetSetupDialog();
            }
        }, 1500);
    }

    private void showBudgetSetupDialog() {
        BudgetSetupDialog dialog = new BudgetSetupDialog(this, new BudgetSetupDialog.OnBudgetSetListener() {
            @Override
            public void onBudgetSet(double amount, String period) {
                monthlyBudget = amount;
                updateBudgetProgress();
                updateBudgetPercentage();
                Toast.makeText(MainActivity.this,
                        String.format("Budget set to ₹%.0f for %s period", amount, period),
                        Toast.LENGTH_LONG).show();
                animateBudgetStatusUpdate();

                // Generate new budget-related notifications
                generateBudgetAlerts();
                updateNotificationBadge();
            }

            @Override
            public void onSkipped() {
                Toast.makeText(MainActivity.this, "You can set budget later from settings", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void animateBudgetStatusUpdate() {
        budgetStatusText.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> {
                    budgetStatusText.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        ImageView menuButton = findViewById(R.id.menuButton);
        cvMenuBar = findViewById(R.id.cvMenuBar);
        layoutMenu = findViewById(R.id.layoutMenu);

        cvMenuBar.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        layoutMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        totalText = findViewById(R.id.totalText);
        emptyText = findViewById(R.id.emptyText);
        filterButton = findViewById(R.id.filterButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);
        filterLayout = findViewById(R.id.filterLayout);
        clearFilterLayout = findViewById(R.id.clearFilterLayout);
        budgetStatusText = findViewById(R.id.budgetStatusText);
        mainFab = findViewById(R.id.mainFab);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        percentageButton = findViewById(R.id.percentageButton);
        budgetRemainingText = findViewById(R.id.budgetRemainingText);
        greetingText = findViewById(R.id.greetingText);
        todayDateText = findViewById(R.id.todayDateText);
        percentageText = findViewById(R.id.percentageText);

        addExpenseCard = findViewById(R.id.addExpenseCard);
        setBudgetCard = findViewById(R.id.setBudgetCard);
        analyticsCard = findViewById(R.id.analyticsCard);

        updateGreetingText();
        updateTodayDate();

        reminderRecyclerView = findViewById(R.id.reminderRecyclerView);
        if (reminderRecyclerView != null) {
            reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        enhancedReminders = reminderManager.getAllReminders();

        // Initialize notification views
        initializeNotificationViews();
    }

    private void createAdapter() {
        adapter = new ExpenseAdapter(this, expenses, position -> {
            recentlyDeletedExpense = adapter.getAllExpenses().get(position);
            recentlyDeletedPosition = position;
            adapter.removeExpense(position);
            updateTotalAnimated();
            updateBudgetProgress();
            saveExpenses();
            showUndoSnackbar();
            toggleEmptyState();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.nav_header_username);
        TextView tipTextView = headerView.findViewById(R.id.textMoneyTip);
        String tip = MoneyTipsProvider.getTip(this);
        tipTextView.setText(tip);
        usernameTextView.setText("Hey, " + SplashActivity.nameGlobal);

        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }

    private void setupExpenseList() {
        mainFab.setOnClickListener(v -> showAddExpenseDialog());
        filterButton.setOnClickListener(v -> showFilterOptions());
        clearFilterButton.setOnClickListener(v -> clearFilters());
        budgetStatusText.setOnClickListener(v -> showSetBudgetDialog());

        setupQuickActionCards();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_SELECTED);
        bottomNavigationView.setOnItemSelectedListener(this::handleBottomNavigationItemSelected);
    }

    private void setupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void updateGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        String fullName = SplashActivity.nameGlobal != null ? SplashActivity.nameGlobal : "User";
        String firstName = fullName.trim().split(" ")[0];
        greetingText.setText(greeting + ",\n" + firstName + "! ✨");
    }

    private void updateTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        todayDateText.setText(today);
    }

    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_Calender1) {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_ResetData) {
            showResetDataDialog();
            return true;
        } else if (id == R.id.nav_savedReminder) {
            showEnhancedRemindersActivity();
        } else if (id == R.id.nav_piggy) {
            Intent intent = new Intent(this, PiggyBankActivity.class);
            startActivity(intent);
        } else if (id == R.id.total_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Privacy) {
            showPrivacyPolicy();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean handleBottomNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        animateNavItem(itemId);

        if (itemId == R.id.nav_dashboard) {
            return true;
        } else if (itemId == R.id.nav_ClockSet) {
            showEnhancedReminderDialog();
            return true;
        } else if (itemId == R.id.fabMic) {
            startVoiceInput();
            return true;
        } else if (itemId == R.id.nav_summary) {
            Intent intent = new Intent(MainActivity.this, MonthlySummaryActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_reset) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    // Enhanced Reminder Methods
    void showEnhancedReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etReminderTitle = dialogView.findViewById(R.id.etReminderTitle);
        Button btnSelectTime = dialogView.findViewById(R.id.btnSelectTime);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveReminder = dialogView.findViewById(R.id.btnSaveReminder);

        final int[] selectedHour = {Calendar.getInstance().get(Calendar.HOUR_OF_DAY)};
        final int[] selectedMinute = {Calendar.getInstance().get(Calendar.MINUTE)};
        final String[] selectedDate = {getCurrentDate()};
        final String[] selectedCategory = {"Expense"};
        final String[] selectedRepeat = {"Daily"};
        final int[] selectedPriority = {3};

        setupCategorySelection(dialogView, selectedCategory);
        setupRepeatSelection(dialogView, selectedRepeat);
        setupPrioritySelection(dialogView, selectedPriority);

        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        btnSelectTime.setText(String.format(Locale.getDefault(),
                                "%02d:%02d", hourOfDay, minute));
                    }, selectedHour[0], selectedMinute[0], true);
            timePickerDialog.show();
        });

        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);
                        selectedDate[0] = DateFormat.getDateInstance().format(selectedCal.getTime());
                        btnSelectDate.setText(selectedDate[0]);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSaveReminder.setOnClickListener(v -> {
            String title = etReminderTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show();
                return;
            }

            EnhancedReminderModel reminder = new EnhancedReminderModel(
                    title, selectedCategory[0], selectedHour[0], selectedMinute[0],
                    selectedDate[0], selectedRepeat[0], selectedPriority[0]
            );

            reminderManager.saveReminder(reminder);

            Toast.makeText(this, "Smart reminder created successfully! 🎉", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupCategorySelection(View dialogView, String[] selectedCategory) {
        // Implementation for category chip selection
    }

    private void setupRepeatSelection(View dialogView, String[] selectedRepeat) {
        // Implementation for repeat chip selection
    }

    private void setupPrioritySelection(View dialogView, int[] selectedPriority) {
        // Implementation for priority slider
    }

    private void showEnhancedRemindersActivity() {
        Intent intent = new Intent(this, EnhancedReminderActivity.class);
        startActivity(intent);
    }

    // Enhanced Reminder Adapter Callbacks
    @Override
    public void onToggleReminder(EnhancedReminderModel reminder, boolean isActive) {
        reminderManager.toggleReminderStatus(reminder, isActive);
        String message = isActive ? "Reminder activated" : "Reminder deactivated";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditReminder(EnhancedReminderModel reminder) {
        showEditReminderDialog(reminder);
    }

    @Override
    public void onDeleteReminder(EnhancedReminderModel reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    reminderManager.deleteReminder(reminder);
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDuplicateReminder(EnhancedReminderModel reminder) {
        EnhancedReminderModel duplicate = reminderManager.duplicateReminder(reminder);
        Toast.makeText(this, "Reminder duplicated", Toast.LENGTH_SHORT).show();
    }

    private void showEditReminderDialog(EnhancedReminderModel reminder) {
        // Implementation for editing existing reminder
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            EnhancedReminderModel reminder = new EnhancedReminderModel(
                    "Daily Expense Reminder", "Expense", hourOfDay, minute1,
                    getCurrentDate(), "daily", 3
            );

            reminderManager.saveReminder(reminder);
            Toast.makeText(this, "Reminder set for " + hourOfDay + ":" + minute1, Toast.LENGTH_SHORT).show();
        }, hour, minute, true);

        timePickerDialog.setCancelable(false);
        timePickerDialog.setCanceledOnTouchOutside(false);
        timePickerDialog.show();
    }

    private void showAddExpenseDialog() {
        currentDialog = new AddExpenseDialog(this,
                (amount, note, category, isRecurring, recurrenceType, date, hasPhoto, hasLocation, photoPath, locationAddress, latitude, longitude) -> {
                    ExpenseModel newExpense = new ExpenseModel(amount, note, category, isRecurring,
                            recurrenceType, date, hasPhoto, hasLocation, photoPath, locationAddress);

                    // Set coordinates if location is available
                    if (hasLocation) {
                        newExpense.setLatitude(latitude);
                        newExpense.setLongitude(longitude);
                    }

                    adapter.addExpense(newExpense);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    updateTotalAnimated();
                    updateBudgetProgress();
                    saveExpenses();
                    toggleEmptyState();

                    String successMessage = "Expense added successfully!";
                    if (hasPhoto) successMessage += " 📷";
                    if (hasLocation) successMessage += " 📍";

                    Toast.makeText(MainActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                    // Generate new notifications based on updated expenses
                    generateBudgetAlerts();
                    updateNotificationBadge();
                });

        currentDialog.setPermissionCallback(new AddExpenseDialog.PermissionCallback() {
            @Override
            public void onCameraPermissionResult(boolean granted) {
                if (!granted) {
                    requestCameraPermission();
                } else {
                    currentDialog.triggerCameraIntent();
                }
            }

            @Override
            public void onLocationPermissionResult(boolean granted) {
                if (!granted) {
                    requestLocationPermission();
                }
            }

            @Override
            public void onStoragePermissionResult(boolean granted) {
                if (!granted) {
                    requestStoragePermission();
                } else {
                    currentDialog.triggerGalleryIntent();
                }
            }
        });

        currentDialog.show();
    }

    // Permission request methods
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            if (currentDialog != null) {
                currentDialog.triggerCameraIntent();
            }
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            if (currentDialog != null) {
                // Handle location permission granted
            }
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (currentDialog != null) {
                currentDialog.triggerGalleryIntent();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                if (currentDialog != null) {
                    currentDialog.triggerGalleryIntent();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//            fusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(location -> {
//                        if (location != null && currentDialog != null) {
//                            currentDialog.onLocationResult(location.getLatitude(), location.getLongitude());
//                            Toast.makeText(this, "Location captured successfully!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your expense");
        try {
            startActivityForResult(intent, VOICE_INPUT_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndAddExpense(String spokenText) {
        Pattern pattern = Pattern.compile("(?i)(?:add)?\\s*₹?(\\d+)(?:\\s*(?:rupees|rs))?\\s*(?:for)?\\s*(.*)");
        Matcher matcher = pattern.matcher(spokenText);

        if (matcher.find()) {
            double amount = Double.parseDouble(matcher.group(1));
            String note = matcher.group(2).trim();

            ExpenseModel newExpense = new ExpenseModel(amount, note, getCurrentDate());
            adapter.addExpense(newExpense);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            updateTotalAnimated();
            updateBudgetProgress();
            saveExpenses();
            toggleEmptyState();

            Toast.makeText(this, "Expense added from voice", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not understand. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateNavItem(int itemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        @SuppressLint("RestrictedApi")
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            if (item.getItemId() == itemId) {
                ScaleAnimation scale = new ScaleAnimation(
                        0.9f, 1f, 0.9f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scale.setDuration(150);
                scale.setFillAfter(true);
                itemView.startAnimation(scale);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    if (currentDialog != null) {
                        currentDialog.triggerCameraIntent();
                    }
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                    if (currentDialog != null) {
                        currentDialog.triggerGalleryIntent();
                    }
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // FIXED: Enhanced onActivityResult with proper handling for both camera and gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("PhotoCapture", "MainActivity onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == VOICE_INPUT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                parseAndAddExpense(spokenText);
            }
        }
        // FIXED: Camera result handling
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("PhotoCapture", "Camera result received in MainActivity");

            if (currentDialog != null) {
                String photoPath = currentDialog.getCurrentPhotoPath();
                Log.d("PhotoCapture", "Photo path from dialog: " + photoPath);

                if (photoPath != null && !photoPath.isEmpty()) {
                    File photoFile = new File(photoPath);
                    if (photoFile.exists() && photoFile.length() > 0) {
                        Log.d("PhotoCapture", "Photo file exists, calling onPhotoResult");
                        currentDialog.onPhotoResult(photoPath);
                    } else {
                        Log.e("PhotoCapture", "Photo file doesn't exist or is empty");
                        Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("PhotoCapture", "Photo path is null or empty");
                    Toast.makeText(this, "Photo path not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PhotoCapture", "currentDialog is null");
            }
        }
        // FIXED: Gallery result handling
        else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Log.d("PhotoCapture", "Gallery result received: " + selectedImage);

            if (currentDialog != null && selectedImage != null) {
                String filePath = getRealPathFromURI(selectedImage);
                Log.d("PhotoCapture", "Gallery file path: " + filePath);

                if (filePath != null && !filePath.isEmpty()) {
                    currentDialog.onPhotoResult(filePath);
                } else {
                    Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == AddExpenseDialog.REQUEST_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lon = data.getDoubleExtra("lon", 0);
            String address = data.getStringExtra("address");
            if (currentDialog != null) {
                currentDialog.onLocationResult(lat, lon, address);
            }
        }
    }

    // FIXED: Improved method to get real path from URI
    private String getRealPathFromURI(Uri uri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "EXPENSE_GALLERY_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            File destinationFile = new File(storageDir, imageFileName);

            // Copy the image from URI to our app's directory
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                Log.d("PhotoCapture", "Gallery image copied to: " + destinationFile.getAbsolutePath());
                return destinationFile.getAbsolutePath();

            } catch (Exception e) {
                Log.e("PhotoCapture", "Error copying gallery image", e);
                return null;
            }

        } catch (Exception e) {
            Log.e("PhotoCapture", "Error getting real path from URI", e);
            return null;
        }
    }

    // Add all remaining methods from your original MainActivity
    private void refreshUserData() {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        SplashActivity.nameGlobal = prefs.getString("name", "Guest");
        SplashActivity.emailGlobal = prefs.getString("email", "");

        SplashActivity.emailGlobal = prefs.getString("email", "");
        SplashActivity.phoneGlobal = prefs.getString("phone", "");
        SplashActivity.genderGlobal = prefs.getString("gender", "");
    }

    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Biometric not supported or not enrolled", Toast.LENGTH_LONG).show();
                return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock App")
                .setSubtitle("Authenticate with fingerprint or face")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void showFilterOptions() {
        AdvancedFilterDialog filterDialog = new AdvancedFilterDialog(this, criteria -> {
            applyAdvancedFilter(criteria);
            showFilterResultBar(criteria);
        });
        filterDialog.show();
    }

    private void applyAdvancedFilter(AdvancedFilterDialog.FilterCriteria criteria) {
        if (criteria.isEmpty()) {
            adapter.restoreFullList();
            updateTotalAnimated();
            updateBudgetProgress();
            toggleEmptyState();
            hideFilterResultBar();
            return;
        }
        filterLayout.setVisibility(View.GONE);

        clearFilterLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.clearFilterLayout).setOnClickListener(v -> {
            clearFilters();
            hideFilterResultBar();
        });
        List<ExpenseModel> filtered = new ArrayList<>(adapter.getAllExpenses());

        if (criteria.isQuickFilterEnabled) {
            filtered = applyQuickFilter(filtered, criteria.quickFilterType);
        }

        if (criteria.isDateRangeEnabled && !criteria.fromDate.isEmpty() && !criteria.toDate.isEmpty()) {
            filtered = filterByDateRange(filtered, criteria.fromDate, criteria.toDate);
        }

        if (criteria.isAmountRangeEnabled) {
            filtered = filterByAmountRange(filtered, criteria.minAmount, criteria.maxAmount);
        }

        if (criteria.isCategoryEnabled && !criteria.selectedCategories.isEmpty()) {
            filtered = filterByCategories(filtered, criteria.selectedCategories);
        }

        if (criteria.isSearchEnabled && !criteria.searchText.isEmpty()) {
            filtered = filterBySearch(filtered, criteria.searchText);
        }

        if (criteria.hasPhoto) {
            filtered = filterByPhoto(filtered, true);
        }

        if (criteria.hasLocation) {
            filtered = filterByLocation(filtered, true);
        }

        adapter.updateList(filtered);
        updateTotal(filtered);
        updateBudgetProgress();
        toggleEmptyState();

        String message = filtered.isEmpty() ?
                "No expenses match your filters" :
                "Found " + filtered.size() + " matching expenses";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateTotal(List<ExpenseModel> filteredList) {
        double total = 0;
        for (ExpenseModel model : filteredList) {
            total += model.getAmount();
        }
        totalText.setText("₹" + total);
    }

    private List<ExpenseModel> applyQuickFilter(List<ExpenseModel> expenses, String filterType) {
        List<ExpenseModel> filtered = new ArrayList<>();
        Calendar today = Calendar.getInstance();

        for (ExpenseModel expense : expenses) {
            try {
                Date expenseDate = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).parse(expense.getDate());
                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTime(expenseDate);

                boolean matches = false;

                switch (filterType) {
                    case "Today":
                        matches = isSameDay(today, expenseCal);
                        break;
                    case "This Week":
                        matches = isSameWeek(today, expenseCal);
                        break;
                    case "This Month":
                        matches = isSameMonth(today, expenseCal);
                        break;
                }

                if (matches) {
                    filtered.add(expense);
                }
            } catch (Exception ignored) {}
        }

        return filtered;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    private List<ExpenseModel> filterByDateRange(List<ExpenseModel> expenses, String fromDate, String toDate) {
        List<ExpenseModel> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        try {
            Date from = sdf.parse(fromDate);
            Date to = sdf.parse(toDate);

            for (ExpenseModel expense : expenses) {
                Date expenseDate = sdf.parse(expense.getDate());
                if (expenseDate.compareTo(from) >= 0 && expenseDate.compareTo(to) <= 0) {
                    filtered.add(expense);
                }
            }
        } catch (Exception e) {
            return expenses;
        }

        return filtered;
    }

    private List<ExpenseModel> filterByAmountRange(List<ExpenseModel> expenses, double min, double max) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : expenses) {
            if (expense.getAmount() >= min && expense.getAmount() <= max) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    private List<ExpenseModel> filterByCategories(List<ExpenseModel> expenses, List<String> categories) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : expenses) {
            if (categories.contains(expense.getCategory())) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    private List<ExpenseModel> filterBySearch(List<ExpenseModel> expenses, String searchText) {
        List<ExpenseModel> filtered = new ArrayList<>();
        String lowerSearch = searchText.toLowerCase();

        for (ExpenseModel expense : expenses) {
            if (expense.getNote().toLowerCase().contains(lowerSearch) ||
                    expense.getCategory().toLowerCase().contains(lowerSearch) ||
                    expense.getLocationAddress().toLowerCase().contains(lowerSearch)) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    private List<ExpenseModel> filterByPhoto(List<ExpenseModel> expenses, boolean hasPhoto) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : expenses) {
            if (expense.hasPhoto() == hasPhoto) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    private List<ExpenseModel> filterByLocation(List<ExpenseModel> expenses, boolean hasLocation) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : expenses) {
            if (expense.hasLocation() == hasLocation) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    private void showFilterResultBar(AdvancedFilterDialog.FilterCriteria criteria) {
        View filterResultBar = findViewById(R.id.filterResultBar);
        TextView filterResultText = findViewById(R.id.filterResultText);

        if (filterResultBar != null && filterResultText != null) {
            filterResultBar.setVisibility(View.VISIBLE);
            filterResultText.setText("Active filters: " + criteria.getDescription());

            findViewById(R.id.clearFilterLayout).setOnClickListener(v -> {
                clearFilters();
                hideFilterResultBar();
            });
        }
    }

    private void hideFilterResultBar() {
        View filterResultBar = findViewById(R.id.filterResultBar);
        if (filterResultBar != null) {
            filterResultBar.setVisibility(View.GONE);
        }
    }

    private void clearFilters() {
        adapter.restoreFullList();
        updateTotalAnimated();
        updateBudgetProgress();
        clearFilterLayout.setVisibility(View.GONE);
        filterLayout.setVisibility(View.VISIBLE);
        toggleEmptyState();
        hideFilterResultBar();
        Toast.makeText(MainActivity.this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.select_dialog_item, null);

        EditText budgetInput = new EditText(this);
        budgetInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        budgetInput.setHint("Enter monthly budget");
        budgetInput.setText(String.valueOf(monthlyBudget));

        builder.setTitle("Set Monthly Budget")
                .setView(budgetInput)
                .setPositiveButton("Set", (dialog, which) -> {
                    String budgetStr = budgetInput.getText().toString().trim();
                    if (!budgetStr.isEmpty()) {
                        monthlyBudget = Double.parseDouble(budgetStr);
                        saveMonthlyBudget();
                        updateBudgetProgress();
                        Toast.makeText(this, "Budget set to ₹" + monthlyBudget, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showResetDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset All Data")
                .setMessage("This will delete all expenses, reset your budget and streak. Your total amount will be saved to history before deletion.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    double totalAmount = calculateTotalAmount();

                    if (totalAmount > 0) {
                        HistoryManager.saveMonthlyHistory(this, (int) totalAmount);
                        Toast.makeText(this, "Total amount ₹" + (int)totalAmount + " saved to history", Toast.LENGTH_LONG).show();
                    }

                    resetAllData();
                    Toast.makeText(this, "All data has been reset successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetAllData() {
        adapter.clearAllExpenses();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        monthlyBudget = 0.0;
        resetSavingsStreak();
        saveExpenses();
        saveMonthlyBudget();
        updateAllUIComponents();
    }

    private void resetSavingsStreak() {
        SharedPreferences prefs = getSharedPreferences("StreakPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("currentStreak", 0);
        editor.putString("lastSavedDate", "");
        editor.apply();

        TextView streakText = findViewById(R.id.streakText);
        if (streakText != null) {
            streakText.setText("🔥0-day");
        }
    }

    private void updateAllUIComponents() {
        updateTotalAnimated();
        budgetRemainingText.setText("");
        updateBudgetProgress();
        updateBudgetPercentage();

        if (progressBar != null) {
            progressBar.setProgress(0);
        }

        if (percentageText != null) {
            percentageText.setText("0%");
        }

        if (budgetStatusText != null) {
            budgetStatusText.setText("No budget set");
        }

        toggleEmptyState();

        TextView streakText = findViewById(R.id.streakText);
        if (streakText != null) {
            streakText.setText("🔥0-day");
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPrivacyPolicy() {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateBudgetProgress() {
        if (monthlyBudget <= 0) {
            budgetStatusText.setText("No budget set");
            progressBar.setProgress(0);
            return;
        }

        double totalExpenses = 0;
        if (adapter != null) {
            for (ExpenseModel expense : adapter.getAllExpenses()) {
                totalExpenses += expense.getAmount();
            }
        }

        int percentage = (int) ((totalExpenses / monthlyBudget) * 100);
        progressBar.setProgress(Math.min(percentage, 100));

        double remaining = monthlyBudget - totalExpenses;
        if (remaining > 0) {
            budgetStatusText.setText(String.format("Budget: ₹%.0f (₹%.0f left)", monthlyBudget, remaining));
            budgetRemainingText.setText((int) remaining+"left");
        } else {
            budgetStatusText.setText(String.format("Budget: ₹%.0f (₹%.0f over)", monthlyBudget, Math.abs(remaining)));
            budgetRemainingText.setText((int) remaining+"over");
        }
        updateBudgetPercentage();
    }

    private void updateBudgetPercentage() {
        if (adapter == null || monthlyBudget <= 0) {
            percentageText.setText("0%");
            return;
        }

        double totalExpenses = 0;
        for (ExpenseModel expense : adapter.getAllExpenses()) {
            totalExpenses += expense.getAmount();
        }

        int percentage = (int) ((totalExpenses / monthlyBudget) * 100);
        percentageText.setText(percentage + "%");

        percentageButton.setOnClickListener(v -> {
            showBudgetDetailsDialog();
            animatePercentageButton();
        });
    }

    private void animatePercentageButton() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        percentageButton.startAnimation(scaleAnimation);
    }

    private void showBudgetDetailsDialog() {
        double totalExpenses = 0;
        if (adapter != null) {
            for (ExpenseModel expense : adapter.getAllExpenses()) {
                totalExpenses += expense.getAmount();
            }
        }

        double remaining = monthlyBudget - totalExpenses;
        int percentage = monthlyBudget > 0 ? (int) ((totalExpenses / monthlyBudget) * 100) : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Budget Overview")
                .setMessage(String.format(Locale.getDefault(),
                        "Monthly Budget: ₹%.2f\nSpent: ₹%.2f (%d%%)\nRemaining: ₹%.2f",
                        monthlyBudget, totalExpenses, percentage, remaining))
                .setPositiveButton("OK", null)
                .setNeutralButton("Edit Budget", (dialog, which) -> showSetBudgetDialog())
                .show();
    }

    private void updateTotalAnimated() {
        if (adapter == null) {
            totalText.setText("₹0.00");
            return;
        }

        double total = 0;
        for (ExpenseModel model : adapter.getAllExpenses()) {
            total += model.getAmount();
        }

        String currentText = totalText.getText().toString().replaceAll("[^0-9.]", "");
        double current = currentText.isEmpty() ? 0 : Double.parseDouble(currentText);

        ValueAnimator animator = ValueAnimator.ofFloat((float) current, (float) total);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            totalText.setText(String.format("₹%.2f", animatedValue));
        });
        animator.start();
    }

    private void toggleEmptyState() {
        if (adapter != null) {
            if (adapter.getItemCount() == 0) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveExpenses() {
        if (adapter != null) {
            SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String json = new Gson().toJson(adapter.getAllExpenses());
            editor.putString("expense_list", json);
            editor.apply();
        }
    }

    private void loadExpenses() {
        SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
        String json = prefs.getString("expense_list", null);
        Type type = new TypeToken<ArrayList<ExpenseModel>>() {}.getType();
        expenses = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }

    private void loadMonthlyBudget() {
        SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
        monthlyBudget = prefs.getFloat("monthly_budget", 0f);
    }

    private void showUndoSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), "Expense deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    if (recentlyDeletedExpense != null) {
                        adapter.addExpenseAt(recentlyDeletedPosition, recentlyDeletedExpense);
                        updateTotalAnimated();
                        updateBudgetProgress();
                        saveExpenses();
                        toggleEmptyState();
                    }
                }).show();
    }

    private String getCurrentDate() {
        return DateFormat.getDateInstance().format(new Date());
    }

    private double getTodayTotalExpenses() {
        if (adapter == null) return 0.0;

        double total = 0.0;
        String today = DateFormat.getDateInstance().format(new Date());
        for (ExpenseModel model : adapter.getAllExpenses()) {
            if (model.getDate().equals(today)) {
                total += model.getAmount();
            }
        }
        return total;
    }

    private void updateSavingsStreak(double todayTotal, double limit) {
        SharedPreferences prefs = getSharedPreferences("StreakPrefs", MODE_PRIVATE);
        String lastDate = prefs.getString("lastSavedDate", "");
        int streak = prefs.getInt("currentStreak", 0);
        String today = getTodayDate();

        if (today.equals(lastDate)) return;

        SharedPreferences.Editor editor = prefs.edit();
        if (todayTotal <= limit) {
            streak++;
            editor.putInt("currentStreak", streak);
        } else {
            streak = 0;
            editor.putInt("currentStreak", streak);
        }
        editor.putString("lastSavedDate", today);
        editor.apply();
    }

    private int getCurrentStreak() {
        SharedPreferences prefs = getSharedPreferences("StreakPrefs", MODE_PRIVATE);
        return prefs.getInt("currentStreak", 0);
    }

    private String getTodayDate() {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
    }

    private void scheduleDailySmsWithWorkManager() {
        String nameUser = SplashActivity.nameGlobal;
        String phoneNumber = SplashActivity.phoneGlobal;
        int totalAmount = (int) calculateTotalAmount();

        Data inputData = new Data.Builder()
                .putString("nameUser", nameUser)
                .putString("phoneNumber", phoneNumber)
                .putInt("totalAmount", totalAmount)
                .build();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000;
        }

        PeriodicWorkRequest smsWorkRequest = new PeriodicWorkRequest.Builder(
                SmsWorker.class,
                24, TimeUnit.HOURS
        )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailySmsWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                smsWorkRequest
        );

        Log.d("SENDSMS", "Scheduled daily SMS using WorkManager");
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        if (adapter != null) {
            for (ExpenseModel model : adapter.getAllExpenses()) {
                total += model.getAmount();
            }
        }
        return total;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", MODE_PRIVATE);
        String langCode = prefs.getString("App_Lang", "en");

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    public void onBackPressed() {
        if (isNotificationPanelOpen) {
            closeNotificationPanel();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void saveMonthlyBudget() {
        SharedPreferences prefs = getSharedPreferences("expenses", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("monthly_budget", (float) monthlyBudget);
        editor.apply();
    }

    private void setupQuickActionCards() {
        if (addExpenseCard != null) {
            addExpenseCard.setOnClickListener(v -> {
                animateQuickActionCard(addExpenseCard);
                showAddExpenseDialog();
            });
        }

        if (setBudgetCard != null) {
            setBudgetCard.setOnClickListener(v -> {
                animateQuickActionCard(setBudgetCard);
                showSetBudgetDialog();
            });
        }

        if (analyticsCard != null) {
            analyticsCard.setOnClickListener(v -> {
                animateQuickActionCard(analyticsCard);
                Intent intent = new Intent(MainActivity.this, MonthlySummaryActivity.class);
                startActivity(intent);
            });
        }
    }

    private void animateQuickActionCard(View card) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.95f, 1.0f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(100);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        card.startAnimation(scaleAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            SharedPreferences prefs = getSharedPreferences("biometric_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isBiometricShown", false).apply();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level == TRIM_MEMORY_UI_HIDDEN) {
            SharedPreferences prefs = getSharedPreferences("biometric_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isBiometricShown", false).apply();
        }
    }
}