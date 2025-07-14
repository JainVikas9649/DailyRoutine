package app.dailyroutine;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private RelativeLayout profileCardContainer;
    private CardView profileCard;
    private LottieAnimationView profileImage;
    private TextView statusText;
    private ImageButton closeProfile;
    private MaterialButton btnUpdateProfile, btnSettings, btnLogout;
    private boolean isEditing = false;
    private EditText nameField, emailField, phoneField, genderField;
    // Profile info views
    private View nameItem, emailItem, phoneItem, genderItem, joinDateItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_card);

        initViews();
        setupProfileData();
        setupClickListeners();
        animateEntrance();
    }

    private void initViews() {
        profileCardContainer = findViewById(R.id.profileCardContainer);
        profileCard = findViewById(R.id.profileCard);
        profileImage = findViewById(R.id.profileImage);
        statusText = findViewById(R.id.statusText);
        closeProfile = findViewById(R.id.closeProfile);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

        // Profile info items
        nameItem = findViewById(R.id.nameItem);
        emailItem = findViewById(R.id.emailItem);
        phoneItem = findViewById(R.id.phoneItem);
        genderItem = findViewById(R.id.genderItem);
        joinDateItem = findViewById(R.id.joinDateItem);
    }

    private void setupProfileData() {
        // Get user data from global variables or SharedPreferences
        String name = SplashActivity.nameGlobal;
        String email = SplashActivity.emailGlobal;
        String phone = SplashActivity.phoneGlobal;
        String gender = SplashActivity.genderGlobal;

        // If global data is empty, load from SharedPreferences
        if (name == null || name.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
            name = prefs.getString("name", "Guest User");
            email = prefs.getString("email", "No email provided");
            phone = prefs.getString("phone", "No phone provided");
            gender = prefs.getString("gender", "Not specified");
        }

        // Setup profile information
        setupProfileItem(nameItem, "Full Name", name, R.drawable.ic_person);
        setupProfileItem(emailItem, "Email Address", email, R.drawable.ic_email);
        setupProfileItem(phoneItem, "Phone Number", phone.isEmpty() ? "" : phone, R.drawable.ic_phone);
        setupProfileItem(genderItem, "Gender", gender.isEmpty() ? "" : gender, R.drawable.ic_gender);
        
        // Setup join date
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        long loginTime = prefs.getLong("login_time", System.currentTimeMillis());
        String joinDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(loginTime));
        setupProfileItem(joinDateItem, "Member Since", joinDate, R.drawable.ic_calendar);

        // Setup status
        boolean isVerified = !email.isEmpty() && !email.equals("No email provided");
        if (isVerified) {
            statusText.setText("✓ Verified Account");
            statusText.setBackgroundColor(getColor(R.color.success));
        } else {
            statusText.setText("⚠ Incomplete Profile");
            statusText.setBackgroundColor(getColor(R.color.warning));
        }

        // Start profile animation
        profileImage.playAnimation();
        Drawable drawable = profileImage.getDrawable();
        if (drawable instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) drawable).start();
        }
    }

//    private void setupProfileItem(View itemView, String label, String value, int iconRes) {
//        TextView fieldLabel = itemView.findViewById(R.id.fieldLabel);
//        TextView fieldValue = itemView.findViewById(R.id.fieldValue);
//        ImageView fieldIcon = itemView.findViewById(R.id.fieldIcon);
//        ImageView copyIcon = itemView.findViewById(R.id.copyIcon);
//
//        fieldLabel.setText(label);
//        fieldValue.setText(value);
//        fieldIcon.setImageResource(iconRes);
//
//        // Setup copy functionality
//        copyIcon.setOnClickListener(v -> {
//            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText(label, value);
//            clipboard.setPrimaryClip(clip);
//            Toast.makeText(this, label + " copied to clipboard", Toast.LENGTH_SHORT).show();
//
//            // Animate copy icon
//            ObjectAnimator.ofFloat(copyIcon, "rotation", 0f, 360f)
//                    .setDuration(300)
//                    .start();
//        });
//    }
private void setupProfileItem(View itemView, String label, String value, int iconRes) {
    TextView fieldLabel = itemView.findViewById(R.id.fieldLabel);
    EditText fieldValue = itemView.findViewById(R.id.fieldValue); // change to EditText
    ImageView fieldIcon = itemView.findViewById(R.id.fieldIcon);
    ImageView copyIcon = itemView.findViewById(R.id.copyIcon);

    fieldLabel.setText(label);
    fieldValue.setText(value);
    fieldValue.setEnabled(false); // disabled by default
    fieldIcon.setImageResource(iconRes);

    // Reference for save
    if (label.equals("Full Name")) nameField = fieldValue;
    else if (label.equals("Email Address")) emailField = fieldValue;
    else if (label.equals("Phone Number")) phoneField = fieldValue;
    else if (label.equals("Gender")) genderField = fieldValue;

    copyIcon.setOnClickListener(v -> {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, fieldValue.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " copied to clipboard", Toast.LENGTH_SHORT).show();

        ObjectAnimator.ofFloat(copyIcon, "rotation", 0f, 360f).setDuration(300).start();
    });
}
    private void setupClickListeners() {
        // Close profile
        closeProfile.setOnClickListener(v -> closeProfile());
        profileCardContainer.setOnClickListener(v -> closeProfile());
        
        // Prevent card clicks from closing
        profileCard.setOnClickListener(v -> {
            // Do nothing - prevent event propagation
        });

        // Update profile
    //    btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnUpdateProfile.setOnClickListener(v -> {
            if (!isEditing) {
                // Enable editing mode
                nameField.setEnabled(true);
                emailField.setEnabled(true);
                phoneField.setEnabled(true);
                genderField.setEnabled(true);

                btnUpdateProfile.setText("Save");
                isEditing = true;
            } else {
                // Save updated data
                String newName = nameField.getText().toString().trim();
                String newEmail = emailField.getText().toString().trim();
                String newPhone = phoneField.getText().toString().trim();
                String newGender = genderField.getText().toString().trim();

                SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("name", newName);
                editor.putString("email", newEmail);
                editor.putString("phone", newPhone);
                editor.putString("gender", newGender);
                editor.apply();

                SplashActivity.nameGlobal = newName;
                SplashActivity.emailGlobal = newEmail;
                SplashActivity.phoneGlobal = newPhone;
                SplashActivity.genderGlobal = newGender;

                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                // Disable editing mode
                nameField.setEnabled(false);
                emailField.setEnabled(false);
                phoneField.setEnabled(false);
                genderField.setEnabled(false);

                btnUpdateProfile.setText("Update Profile");
                isEditing = false;
            }
        });
        // Settings
        btnSettings.setOnClickListener(v -> openSettings());

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void animateEntrance() {
        profileCardContainer.setVisibility(View.VISIBLE);
        profileCardContainer.setAlpha(0f);
        profileCard.setScaleX(0.7f);
        profileCard.setScaleY(0.7f);

        // Animate background
        profileCardContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Animate card
        profileCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(100)
                .start();
    }

    private void closeProfile() {
        profileCard.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(300)
                .start();

        profileCardContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    profileCardContainer.setVisibility(View.GONE);
                    finish();
                })
                .start();
    }

    private void updateProfile() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("isUpdate", true);
        intent.putExtra("name", SplashActivity.nameGlobal);
        intent.putExtra("email", SplashActivity.emailGlobal);
        intent.putExtra("phone", SplashActivity.phoneGlobal);
        intent.putExtra("gender", SplashActivity.genderGlobal);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from your account?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear user data
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Clear global variables
        SplashActivity.clearUserData();

        // Navigate to auth screen
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        closeProfile();
        super.onBackPressed();
    }
}
