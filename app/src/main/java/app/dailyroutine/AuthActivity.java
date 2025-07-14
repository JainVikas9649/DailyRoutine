package app.dailyroutine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private TextView switchModeText;
    private CircularProgressIndicator loadingIndicator;
    private TextView loadingText;
    private View loadingOverlay;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initViews();
        setupClickListeners();
        showLoginFragment();
    }

    private void initViews() {
        switchModeText = findViewById(R.id.switchModeText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingText = findViewById(R.id.loadingText);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupClickListeners() {
        switchModeText.setOnClickListener(v -> toggleAuthMode());
    }

    private void showLoginFragment() {
        isLoginMode = true;
        switchModeText.setText("Don't have an account? Sign Up");

        Fragment fragment = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void showSignUpFragment() {
        isLoginMode = false;
        switchModeText.setText("Already have an account? Sign In");

        Fragment fragment = new SignUpFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void toggleAuthMode() {
        if (isLoginMode) {
            showSignUpFragment();
        } else {
            showLoginFragment();
        }
    }

    public void showLoading(boolean show, String message) {
        if (show) {
            loadingText.setText(message);
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        } else {
            loadingOverlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> loadingOverlay.setVisibility(View.GONE))
                    .start();
        }
    }

    public void onAuthSuccess(String name, String email, String phone, String gender) {
        Log.d(TAG, "Authentication successful for: " + name);

        // Save user data to main preferences
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("gender", gender);
        editor.putBoolean("isUserInfoSaved", true);
        editor.apply();

        // Save biometric credentials if available
        SharedPreferences biometricPrefs = getSharedPreferences("biometric_credentials", MODE_PRIVATE);
        SharedPreferences.Editor biometricEditor = biometricPrefs.edit();
        biometricEditor.putString("name", name);
        biometricEditor.putString("email", email);
        biometricEditor.apply();

        // Update global variables
        SplashActivity.refreshUserData(prefs);

        showLoading(false, "");

        // Navigate to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // Helper method to save user credentials for login
    public void saveUserCredentials(String email, String password, String name, String phone, String gender) {
        SharedPreferences credentialsPrefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = credentialsPrefs.edit();
        editor.putString("email", email);
        editor.putString("password", password); // In production, hash this password!
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.putString("gender", gender);
        editor.apply();

        Log.d(TAG, "User credentials saved for: " + email);
    }

    // Helper method to validate login credentials
    public boolean validateLoginCredentials(String email, String password) {
        SharedPreferences credentialsPrefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String savedEmail = credentialsPrefs.getString("email", "");
        String savedPassword = credentialsPrefs.getString("password", "");

        boolean isValid = email.equals(savedEmail) && password.equals(savedPassword);
        Log.d(TAG, "Login validation for " + email + ": " + isValid);

        return isValid;
    }

    // Helper method to get user data by email
    public UserData getUserDataByEmail(String email) {
        SharedPreferences credentialsPrefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String savedEmail = credentialsPrefs.getString("email", "");

        if (email.equals(savedEmail)) {
            return new UserData(
                    credentialsPrefs.getString("name", ""),
                    credentialsPrefs.getString("email", ""),
                    credentialsPrefs.getString("phone", ""),
                    credentialsPrefs.getString("gender", "")
            );
        }
        return null;
    }

    // Helper class for user data
    public static class UserData {
        public String name, email, phone, gender;

        public UserData(String name, String email, String phone, String gender) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
        }
    }

    @Override
    public void onBackPressed() {
        if (loadingOverlay.getVisibility() == View.VISIBLE) {
            // Don't allow back press during loading
            return;
        }
        super.onBackPressed();
    }
}
