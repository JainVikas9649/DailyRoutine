package app.dailyroutine;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import app.dailyroutine.ChatBot.AIChatActivity;
import app.dailyroutine.ChatBot.PrivacyBottomSheetDialog;
import app.dailyroutine.ChatBot.AboutBottomSheetDialog;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton backButton, editProfileButton;
    private SwitchCompat themeSwitch, notificationSwitch;
    private LinearLayout themeSettingLayout, languageSettingLayout, notificationSettingLayout;
    private LinearLayout privacySettingLayout, helpSettingLayout, aboutSettingLayout;
    private TextView userName, userEmail, themeDescription, languageDescription, versionText;
    private FloatingActionButton fabAiChat;

    // Data
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static final int ANIMATION_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(this);
        setContentView(R.layout.activity_settings);

        initViews();
        setupPreferences();
        loadUserData();
        setupClickListeners();
        setupSwitches();
        animateViewsIn();
    }

    private void initViews() {
        // Buttons
        backButton = findViewById(R.id.backButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        fabAiChat = findViewById(R.id.fabAiChat);

        // Switches
        themeSwitch = findViewById(R.id.themeSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);

        // Layouts
        themeSettingLayout = findViewById(R.id.themeSettingLayout);
        languageSettingLayout = findViewById(R.id.languageSettingLayout);
        notificationSettingLayout = findViewById(R.id.notificationSettingLayout);
        privacySettingLayout = findViewById(R.id.privacySettingLayout);
        helpSettingLayout = findViewById(R.id.helpSettingLayout);
        aboutSettingLayout = findViewById(R.id.aboutSettingLayout);

        // TextViews
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        themeDescription = findViewById(R.id.themeDescription);
        languageDescription = findViewById(R.id.languageDescription);
        versionText = findViewById(R.id.versionText);
    }

    private void setupPreferences() {
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void loadUserData() {
        // Load user information with null checks
        String name = "User";
        String email = "user@example.com";

        try {
            if (SplashActivity.nameGlobal != null && !SplashActivity.nameGlobal.isEmpty()) {
                name = SplashActivity.nameGlobal;
            }
            if (SplashActivity.emailGlobal != null && !SplashActivity.emailGlobal.isEmpty()) {
                email = SplashActivity.emailGlobal;
            }
        } catch (Exception e) {
            // Use default values if there's any issue
        }

        userName.setText(name);
        userEmail.setText(email);

        // Load current settings
        boolean isDarkTheme = preferences.getBoolean("dark_theme", false);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        String currentLanguage = preferences.getString("App_Lang", "en");

        themeSwitch.setChecked(isDarkTheme);
        notificationSwitch.setChecked(notificationsEnabled);

        updateThemeDescription(isDarkTheme);
        updateLanguageDescription(currentLanguage);

        // Set version info
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("Version " + versionName);
        } catch (Exception e) {
            versionText.setText("Version 1.0");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateViewsOut();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, ANIMATION_DURATION);
        });

        editProfileButton.setOnClickListener(v -> {
            animateButtonClick(editProfileButton);
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);        });

        themeSettingLayout.setOnClickListener(v -> {
            animateCardClick(themeSettingLayout);
            themeSwitch.setChecked(!themeSwitch.isChecked());
        });

        languageSettingLayout.setOnClickListener(v -> {
            animateCardClick(languageSettingLayout);
            showLanguageDialog();
        });

        notificationSettingLayout.setOnClickListener(v -> {
            animateCardClick(notificationSettingLayout);
            notificationSwitch.setChecked(!notificationSwitch.isChecked());
        });

        privacySettingLayout.setOnClickListener(v -> {
            animateCardClick(privacySettingLayout);
            showModernPrivacyDialog();
        });

        helpSettingLayout.setOnClickListener(v -> {
            animateCardClick(helpSettingLayout);
            openAiChat();
        });
        aboutSettingLayout.setOnClickListener(v -> {
            animateCardClick(aboutSettingLayout);
            showModernAboutDialog();
        });

        fabAiChat.setOnClickListener(v -> {
            animateFabClick();
            openAiChat();
        });
    }
    private void showModernAboutDialog() {
        AboutBottomSheetDialog aboutDialog = new AboutBottomSheetDialog(this);
        aboutDialog.show();
    }
    private void setupSwitches() {
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Only respond to user interaction
                toggleTheme(isChecked);
            }
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Only respond to user interaction
                toggleNotifications(isChecked);
            }
        });
    }

    private void toggleTheme(boolean isDark) {
        editor.putBoolean("dark_theme", isDark).apply();
        updateThemeDescription(isDark);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Snackbar.make(findViewById(android.R.id.content),
                isDark ? "Dark theme enabled" : "Light theme enabled",
                Snackbar.LENGTH_SHORT).show();

        // Recreate activity with delay for smooth transition
        new Handler(Looper.getMainLooper()).postDelayed(this::recreate, 500);
    }
    private void showModernPrivacyDialog() {
        PrivacyBottomSheetDialog privacyDialog = new PrivacyBottomSheetDialog(this);
        privacyDialog.show();
    }
    private void toggleNotifications(boolean enabled) {
        editor.putBoolean("notifications_enabled", enabled).apply();

        String message = enabled ? "Notifications enabled" : "Notifications disabled";
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void updateThemeDescription(boolean isDark) {
        if (themeDescription != null) {
            themeDescription.setText(isDark ? "Dark mode is active" : "Light mode is active");
        }
    }

    private void updateLanguageDescription(String langCode) {
        if (languageDescription != null) {
            String language = langCode.equals("hi") ? "हिंदी" : "English";
            languageDescription.setText("Current: " + language);
        }
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "हिंदी (Hindi)"};
        final String[] languageCodes = {"en", "hi"};

        String currentLang = preferences.getString("App_Lang", "en");
        int selectedIndex = currentLang.equals("hi") ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose Language")
                .setIcon(R.drawable.ic_language)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLangCode = languageCodes[which];
                    setLocale(this, selectedLangCode);
                    updateLanguageDescription(selectedLangCode);

                    dialog.dismiss();

                    // Restart app to apply language change
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




    private void openAiChat() {
        try {
            Intent intent = new Intent(this, AIChatActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "AI Chat not available", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Animation Methods
    private void animateViewsIn() {
        View[] views = {themeSettingLayout, languageSettingLayout, notificationSettingLayout,
                privacySettingLayout, helpSettingLayout, aboutSettingLayout};

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view != null) {
                view.setAlpha(0f);
                view.setTranslationX(100f);

                view.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setStartDelay(i * 50L)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }
        }

        // Animate FAB
        if (fabAiChat != null) {
            fabAiChat.setScaleX(0f);
            fabAiChat.setScaleY(0f);
            fabAiChat.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(300L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animateViewsOut() {
        View[] views = {themeSettingLayout, languageSettingLayout, notificationSettingLayout,
                privacySettingLayout, helpSettingLayout, aboutSettingLayout};

        for (View view : views) {
            if (view != null) {
                view.animate()
                        .alpha(0f)
                        .translationX(-100f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        }
    }

    private void animateCardClick(View view) {
        if (view != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f);

            scaleX.setDuration(150);
            scaleY.setDuration(150);

            scaleX.start();
            scaleY.start();
        }
    }

    private void animateButtonClick(View button) {
        if (button != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.9f, 1f);

            scaleX.setDuration(100);
            scaleY.setDuration(100);

            scaleX.start();
            scaleY.start();
        }
    }

    private void animateFabClick() {
        if (fabAiChat != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabAiChat, "scaleX", 1f, 0.8f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabAiChat, "scaleY", 1f, 0.8f, 1f);

            scaleX.setDuration(150);
            scaleY.setDuration(150);

            scaleX.start();
            scaleY.start();
        }
    }

    // Locale Methods
    public void loadLocale(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
            String lang = prefs.getString("App_Lang", "en");
            setLocale(context, lang);
        } catch (Exception e) {
            // Handle locale loading error
        }
    }

    public void setLocale(Context context, String langCode) {
        try {
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);

            Configuration config = new Configuration();
            config.setLocale(locale);

            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            SharedPreferences.Editor editor = context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();
            editor.putString("App_Lang", langCode);
            editor.apply();
        } catch (Exception e) {
            // Handle locale setting error
        }
    }

    @Override
    public void onBackPressed() {
        animateViewsOut();
        new Handler(Looper.getMainLooper()).postDelayed(super::onBackPressed, ANIMATION_DURATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Refresh user data when returning to settings
    }
}
