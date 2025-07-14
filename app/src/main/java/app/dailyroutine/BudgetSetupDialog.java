package app.dailyroutine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class BudgetSetupDialog extends Dialog {

    private Context context;
    private OnBudgetSetListener listener;
    
    // UI Components
    private LinearLayout contentContainer, successContainer;
    private ImageView dialogIcon, successIcon;
    private TextView dialogTitle, dialogSubtitle;
    private TextInputEditText budgetInput;
    private MaterialCardView suggestion1, suggestion2, suggestion3, suggestion4;
    private ChipGroup periodChipGroup;
    private MaterialButton skipButton, setBudgetButton;
    
    // Data
    private String selectedPeriod = "monthly";
    private double budgetAmount = 0.0;
    
    // Animation
    private boolean isAnimating = false;

    public interface OnBudgetSetListener {
        void onBudgetSet(double amount, String period);
        void onSkipped();
    }

    public BudgetSetupDialog(@NonNull Context context, OnBudgetSetListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        initializeDialog();
    }

    private void initializeDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_budget_setup);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        
        // Make dialog background transparent for custom styling
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        initializeViews();
        setupClickListeners();
        setupAnimations();
    }

    private void initializeViews() {
        contentContainer = findViewById(R.id.contentContainer);
        successContainer = findViewById(R.id.successContainer);
        dialogIcon = findViewById(R.id.dialogIcon);
        successIcon = findViewById(R.id.successIcon);
        dialogTitle = findViewById(R.id.dialogTitle);
        dialogSubtitle = findViewById(R.id.dialogSubtitle);
        budgetInput = findViewById(R.id.budgetInput);
        suggestion1 = findViewById(R.id.suggestion1);
        suggestion2 = findViewById(R.id.suggestion2);
        suggestion3 = findViewById(R.id.suggestion3);
        suggestion4 = findViewById(R.id.suggestion4);
        periodChipGroup = findViewById(R.id.periodChipGroup);
        skipButton = findViewById(R.id.skipButton);
        setBudgetButton = findViewById(R.id.setBudgetButton);
    }

    private void setupClickListeners() {
        // Budget input text watcher
        budgetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSetButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Suggestion cards
        suggestion1.setOnClickListener(v -> selectSuggestion(5000, suggestion1));
        suggestion2.setOnClickListener(v -> selectSuggestion(10000, suggestion2));
        suggestion3.setOnClickListener(v -> selectSuggestion(15000, suggestion3));
        suggestion4.setOnClickListener(v -> selectSuggestion(25000, suggestion4));

        // Period selection
        periodChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.weeklyChip) {
                selectedPeriod = "weekly";
            } else if (checkedId == R.id.monthlyChip) {
                selectedPeriod = "monthly";
            } else if (checkedId == R.id.yearlyChip) {
                selectedPeriod = "yearly";
            }
        });

        // Action buttons
        skipButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSkipped();
            }
            dismiss();
        });

        setBudgetButton.setOnClickListener(v -> setBudget());
    }

    private void selectSuggestion(double amount, MaterialCardView selectedCard) {
        // Animate card selection
        animateCardSelection(selectedCard);
        
        // Set budget amount
        budgetInput.setText(String.valueOf((int) amount));
        budgetAmount = amount;
        
        // Auto-submit after short delay
        new Handler().postDelayed(this::setBudget, 300);
    }

    private void animateCardSelection(MaterialCardView card) {
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());
        
        scaleX.start();
        scaleY.start();
    }

    private void setBudget() {
        String budgetText = budgetInput.getText().toString().trim();
        
        if (budgetText.isEmpty()) {
            Toast.makeText(context, "Please enter a budget amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            budgetAmount = Double.parseDouble(budgetText);
            if (budgetAmount <= 0) {
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isAnimating) return;
        
        showSuccessState();
    }

    private void showSuccessState() {
        isAnimating = true;
        
        // Update icon and title
        dialogIcon.setImageResource(R.drawable.ic_check_circle);
        dialogTitle.setText("Budget Set Successfully!");
        dialogSubtitle.setText(String.format("Your %s budget of ₹%.0f has been set successfully!", 
                selectedPeriod, budgetAmount));
        
        // Animate transition to success state
        contentContainer.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentContainer.setVisibility(View.GONE);
                        showSuccessAnimation();
                    }
                });
    }

    private void showSuccessAnimation() {
        successContainer.setVisibility(View.VISIBLE);
        successContainer.setAlpha(0f);
        successContainer.setScaleX(0.8f);
        successContainer.setScaleY(0.8f);
        
        successContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Animate success icon
                        animateSuccessIcon();
                        
                        // Auto-dismiss after delay
                        new Handler().postDelayed(() -> {
                            saveBudgetAndDismiss();
                        }, 2000);
                    }
                });
    }

    private void animateSuccessIcon() {
        // Pulse animation for success icon
        ObjectAnimator pulse = ObjectAnimator.ofFloat(successIcon, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(successIcon, "scaleY", 1f, 1.2f, 1f);
        
        pulse.setDuration(600);
        pulseY.setDuration(600);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulseY.setRepeatMode(ValueAnimator.REVERSE);
        
        pulse.start();
        pulseY.start();
    }

    private void saveBudgetAndDismiss() {
        // Save budget to SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("expenses", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("monthly_budget", (float) budgetAmount);
        editor.putString("budget_period", selectedPeriod);
        editor.putBoolean("budget_setup_completed", true);
        editor.apply();
        
        // Notify listener
        if (listener != null) {
            listener.onBudgetSet(budgetAmount, selectedPeriod);
        }
        
        // Dismiss dialog
        dismiss();
    }

    private void updateSetButtonState() {
        String budgetText = budgetInput.getText().toString().trim();
        boolean isValid = !budgetText.isEmpty() && 
                         !budgetText.equals("0") && 
                         !budgetText.equals("0.0");
        
        setBudgetButton.setEnabled(isValid);
        setBudgetButton.setAlpha(isValid ? 1f : 0.5f);
    }

    private void setupAnimations() {
        // Initial entrance animation
        View dialogView = findViewById(android.R.id.content);
        if (dialogView != null) {
            dialogView.setScaleX(0.8f);
            dialogView.setScaleY(0.8f);
            dialogView.setAlpha(0f);
            
            dialogView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    public static boolean shouldShowBudgetDialog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("expenses", Context.MODE_PRIVATE);
        float budget = prefs.getFloat("monthly_budget", 0f);
        boolean setupCompleted = prefs.getBoolean("budget_setup_completed", false);
        
        return budget <= 0 || !setupCompleted;
    }
}
