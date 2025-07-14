//package app.dailyroutine;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class SplashActivity extends AppCompatActivity {
//
//    private static final int SPLASH_DURATION = 2500; // 2.5 seconds
//    public static String nameGlobal;
//    public static String emailGlobal;
//    public static String phoneGlobal;
//    public static String genderGlobal;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
//
////        new Handler().postDelayed(() -> {
////            startActivity(new Intent(SplashActivity.this, MainActivity.class));
////            finish();
////        }, SPLASH_DURATION);
//        new Handler().postDelayed(() -> {
//
//            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
//            boolean isSaved = prefs.getBoolean("isUserInfoSaved", false);
//            nameGlobal = prefs.getString("name", "Guest");
//            emailGlobal = prefs.getString("email", "");
//            phoneGlobal = prefs.getString("phone", "");
//            genderGlobal = prefs.getString("gender", "");
//            Log.d("Details fpr spalsh", "onCreate: " +nameGlobal +emailGlobal +phoneGlobal + genderGlobal);
//            if (isSaved) {
//                refreshUserData(); // Always load fresh data
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//
//            } else {
//                startActivity(new Intent(SplashActivity.this, UserInfoDialog.class));
//              //  startActivity(new Intent(SplashActivity.this, SplashLogin.class));
//                overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out);
//            }
//            finish();
//        }, SPLASH_DURATION);
//    }
//    private void refreshUserData() {
//        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
//        nameGlobal = prefs.getString("name", "Guest");
//        emailGlobal = prefs.getString("email", "");
//        phoneGlobal = prefs.getString("phone", "");
//        genderGlobal = prefs.getString("gender", "");
//
//        Log.d("SplashActivity", "User Data Refreshed: " +
//                nameGlobal + ", " + emailGlobal + ", " + phoneGlobal + ", " + genderGlobal);
//    }
//}
package app.dailyroutine;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private static final int ANIMATION_DELAY = 500; // 0.5 seconds

    // Global user data
    public static String nameGlobal;
    public static String emailGlobal;
    public static String phoneGlobal;
    public static String genderGlobal;

    private LottieAnimationView lottieView;
    private TextView appNameText, taglineText;
    private Handler splashHandler;
    TextView versionText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        startAnimations();
        checkUserAuthStatus();
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        versionText.setText("Version "+ versionName );
    }

    private void initViews() {
        lottieView = findViewById(R.id.lottieView);
        appNameText = findViewById(R.id.appNameText);
        taglineText = findViewById(R.id.taglineText);
        versionText = findViewById(R.id.versionText);

    }

    private void startAnimations() {
        // Animate app name
        appNameText.setAlpha(0f);
        appNameText.setTranslationY(50f);
        appNameText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(ANIMATION_DELAY)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate tagline
        taglineText.setAlpha(0f);
        taglineText.setTranslationY(30f);
        taglineText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(ANIMATION_DELAY + 300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Scale animation for Lottie
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(lottieView, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(lottieView, "scaleY", 0.8f, 1.0f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        scaleX.setStartDelay(ANIMATION_DELAY);
        scaleY.setStartDelay(ANIMATION_DELAY);
        scaleX.start();
        scaleY.start();
    }

    private void checkUserAuthStatus() {
        splashHandler = new Handler(Looper.getMainLooper());
        splashHandler.postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
            boolean isUserLoggedIn = prefs.getBoolean("isUserInfoSaved", false);

            // Load user data
            loadUserData(prefs);

            if (isUserLoggedIn && isValidUserData()) {
                // User is logged in with valid data
                Log.d("SplashActivity", "User authenticated, navigating to MainActivity");
                navigateToMain();
            } else {
                // User needs to authenticate
                Log.d("SplashActivity", "User not authenticated, navigating to AuthActivity");
                navigateToAuth();
            }
        }, SPLASH_DURATION);
    }

    private void loadUserData(SharedPreferences prefs) {
        nameGlobal = prefs.getString("name", "");
        emailGlobal = prefs.getString("email", "");
        phoneGlobal = prefs.getString("phone", "");
        genderGlobal = prefs.getString("gender", "");

        Log.d("SplashActivity", "Loaded user data: " +
                "Name=" + nameGlobal +
                ", Email=" + emailGlobal +
                ", Phone=" + phoneGlobal +
                ", Gender=" + genderGlobal);
    }

    private boolean isValidUserData() {
        // Check if essential user data exists
        return nameGlobal != null && !nameGlobal.trim().isEmpty() &&
                emailGlobal != null && !emailGlobal.trim().isEmpty();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public static void refreshUserData(SharedPreferences prefs) {
        nameGlobal = prefs.getString("name", "");
        emailGlobal = prefs.getString("email", "");
        phoneGlobal = prefs.getString("phone", "");
        genderGlobal = prefs.getString("gender", "");

        Log.d("SplashActivity", "User data refreshed globally");
    }

    public static void clearUserData() {
        nameGlobal = "";
        emailGlobal = "";
        phoneGlobal = "";
        genderGlobal = "";

        Log.d("SplashActivity", "User data cleared globally");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing
        super.onBackPressed();
    }
}
