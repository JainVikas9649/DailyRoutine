package app.dailyroutine;



import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class PiggyBankActivity extends AppCompatActivity {

    private ImageView piggyBankImage;
    private TextView savingsText,   textGoalDisplay;
    private EditText editGoalAmount;
    private Button feedButton, setGoalBtn;
    TextView achievementText;
    private static final String SHARED_PREFS = "piggyBankPrefs";
    private static final String SAVINGS_KEY = "savings_amount";
    private static final String GOAL_KEY = "goal_amount";

    private int savingsAmount ;
    private int goalAmount ;
    private final int feedAmount = 10; // ₹10 per coin fed
    MaterialCardView cardAchievement ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piggy_bank);

        piggyBankImage = findViewById(R.id.imagePiggyBank);
        savingsText = findViewById(R.id.textSavingsAmount);
        feedButton = findViewById(R.id.buttonFeedCoin);
           cardAchievement = findViewById(R.id.textAchievement);
        achievementText = findViewById(R.id.textAchievementContent);
        textGoalDisplay = findViewById(R.id.textGoalDisplay);
        editGoalAmount = findViewById(R.id.editGoalAmount);
        setGoalBtn = findViewById(R.id.buttonSetGoal);

        ImageView backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> onBackPressed());

        feedButton.setOnClickListener(v -> addCoin());

        setGoalBtn.setOnClickListener(v -> {
            String input = editGoalAmount.getText().toString().trim();
            if (!input.isEmpty()) {
                goalAmount = Integer.parseInt(input);
                textGoalDisplay.setText("Goal: ₹" + goalAmount);
                updatePiggyBankImage();
                checkAchievements();
                setGoalBtn.setVisibility(View.GONE);
                editGoalAmount.setVisibility(View.GONE);
            }
            saveGoal();
        });

        Button btnReset = findViewById(R.id.btnResetPiggy);
        btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Piggy Bank")
                    .setMessage("Are you sure you want to reset your savings?")
                    .setPositiveButton("Yes", (dialog, which) -> resetPiggyBank())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Load previously saved data
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        savingsAmount = sharedPreferences.getInt(SAVINGS_KEY, 0);
         savingsAmount = sharedPreferences.getInt(SAVINGS_KEY, 0);
        goalAmount = sharedPreferences.getInt(GOAL_KEY, 0);

        if (goalAmount > 0) {
            textGoalDisplay.setText("Goal: ₹" + goalAmount);
            editGoalAmount.setVisibility(View.GONE);
            setGoalBtn.setVisibility(View.GONE);
         }
        updateUI();
    }

    private void addCoin() {
        savingsAmount += feedAmount;
        saveSavings();
        updateUI();
    }

    private void saveSavings() {
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(SAVINGS_KEY, savingsAmount);
        editor.apply();
    }
    private void saveGoal() {
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(GOAL_KEY, goalAmount);
        editor.apply();
    }
    private void updateUI() {
        savingsText.setText("Savings: ₹" + savingsAmount);
        updatePiggyBankImage();
        checkAchievements();
    }

//    private void updatePiggyBankImage() {
//        Drawable drawable = piggyBankImage.getBackground();
//        if (drawable instanceof LayerDrawable) {
//            LayerDrawable layerDrawable = (LayerDrawable) drawable;
//            Drawable layer = layerDrawable.findDrawableByLayerId(R.id.fillClip);
//            if (layer instanceof ClipDrawable) {
//                ClipDrawable clipDrawable = (ClipDrawable) layer;
//                int maxLevel = 10000;
//                double percentFull = (goalAmount == 0) ? 0 : (double) savingsAmount / goalAmount;
//                int level = (int) (percentFull * maxLevel);
//                clipDrawable.setLevel(level);
//            }
//        }
//    }
private void updatePiggyBankImage() {
    ProgressBar progressBar = findViewById(R.id.progressFill);
    if (goalAmount > 0) {
        double percentFull = (double) savingsAmount / goalAmount;
        int progress = (int) (percentFull * 100); // percent out of 100
        progressBar.setProgress(progress);
    } else {
        progressBar.setProgress(0);
    }
}
    private void checkAchievements() {
        if (goalAmount == 0) {
            achievementText.setText("");
            cardAchievement.setVisibility(View.GONE);

            return;
        }

        double progress = (double) savingsAmount / goalAmount;
        if (progress >= 1.0) {
            cardAchievement.setVisibility(View.VISIBLE);

            achievementText.setText("🎉 Goal Achieved! Great job!");
        } else if (progress >= 0.75) {
            cardAchievement.setVisibility(View.VISIBLE);

            achievementText.setText("👏 75% to goal!");
        } else if (progress >= 0.5) {
            cardAchievement.setVisibility(View.VISIBLE);

            achievementText.setText("👍 Halfway there!");
        } else if (progress >= 0.25) {
            cardAchievement.setVisibility(View.VISIBLE);

            achievementText.setText("⭐ Getting started!");
        } else {
            cardAchievement.setVisibility(View.VISIBLE);

            achievementText.setText("");
        }
    }

    private void resetPiggyBank() {
        savingsAmount = 0;
        goalAmount = 0;

        // Optional: clear saved savings from preferences
        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        updateUI();
        editGoalAmount.setText("");
        textGoalDisplay.setText("");
        setGoalBtn.setVisibility(View.VISIBLE);
        editGoalAmount.setVisibility(View.VISIBLE);
    }
}