package app.dailyroutine.ChatBot;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.dailyroutine.MainActivity;
import app.dailyroutine.ProfileActivity;
import app.dailyroutine.R;
import app.dailyroutine.SettingsActivity;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatModel> chatList;
    private LinearLayout optionContainer, typingIndicator, emptyStateLayout;
    private TextView statusText;
    private MaterialButton backButton, menuButton;
    private FloatingActionButton voiceFab;

    private int currentStage = 0;
    private int botMessageCount = 0;
    private boolean isTyping = false;

    private static final int ANIMATION_DURATION = 300;
    private static final int TYPING_DELAY = 1500;
    private static final int MAX_MESSAGES = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_ai_layout);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        showWelcomeAnimation();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        optionContainer = findViewById(R.id.optionContainer);
        typingIndicator = findViewById(R.id.typingIndicator);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        statusText = findViewById(R.id.statusText);
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);
        voiceFab = findViewById(R.id.voiceFab);

        chatList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(chatList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Add scroll listener for better UX
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 0) {
//                    hideVoiceFab();
//                } else {
//                    showVoiceFab();
//                }
            }
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateExit();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, ANIMATION_DURATION);
        });

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> showMenuOptions(v));
        }
        voiceFab.setOnClickListener(v -> {
            animateFabClick();
            Snackbar.make(findViewById(android.R.id.content),
                    "Voice input coming soon!", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void showWelcomeAnimation() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        emptyStateLayout.setAlpha(0f);
        emptyStateLayout.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            hideEmptyState();
                            showInitialOptions();
                        }, 2000);
                    }
                })
                .start();
    }

    private void hideEmptyState() {
        emptyStateLayout.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void showInitialOptions() {
        addBotMessage("Hi! I'm your Smart Financial Assistant. How can I help you today? 🤖");

        String[] options = {
                "💸 Why am I overspending?",
                "💰 How can I save more?",
                "📊 Suggest a money habit",
                "📅 How much did I spend last week?",
                "📈 What's my biggest expense category?",
                "💡 Give me a daily saving tip",
                "🎯 How to stick to my budget?",
                "⏰ Remind me to save",
                "📱 Analyze my subscriptions"
        };

        showOptions(options, 1);
    }

    private void showOptions(String[] options, int nextStage) {
        if (botMessageCount >= MAX_MESSAGES) {
            endConversation();
            return;
        }

        optionContainer.removeAllViews();

        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            MaterialCardView optionCard = createOptionCard(option, nextStage);

            // Add with staggered animation
            optionCard.setAlpha(0f);
            optionCard.setTranslationY(50f);
            optionContainer.addView(optionCard);

            final int delay = i * 100;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                optionCard.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }, delay);
        }
    }

    private MaterialCardView createOptionCard(String option, int nextStage) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);
        card.setCardElevation(4f);
        card.setRadius(16f);
        card.setCardBackgroundColor(getColor(R.color.white));
        card.setStrokeWidth(1);
        card.setStrokeColor(getColor(R.color.light_gray));

        // Replace this line:
        //card.setForeground(getDrawable(android.R.attr.selectableItemBackground));

        // With this:
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            card.setForeground(getDrawable(outValue.resourceId));
        } else {
            // For older versions, use a ripple effect drawable
            card.setForeground(ContextCompat.getDrawable(this, R.drawable.ripple_effect));
        }

        TextView textView = new TextView(this);
        textView.setText(option);
        textView.setTextSize(16f);
        textView.setTextColor(getColor(R.color.text_primary));
        textView.setPadding(24, 20, 24, 20);
        textView.setCompoundDrawablePadding(16);

        card.addView(textView);

        card.setOnClickListener(v -> {
            animateCardClick(card);
            handleOptionClick(option, nextStage);
        });

        return card;
    }

    private void handleOptionClick(String selectedText, int nextStage) {
        // Add user message
        addUserMessage(selectedText);

        // Clear options with animation
        clearOptionsWithAnimation();

        // Update stage and show typing
        currentStage = nextStage;
        showTypingIndicator();

        // Handle bot response after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideTypingIndicator();
            handleBotResponse(selectedText);
        }, TYPING_DELAY);
    }

    private void clearOptionsWithAnimation() {
        for (int i = 0; i < optionContainer.getChildCount(); i++) {
            View child = optionContainer.getChildAt(i);
            child.animate()
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(i * 50L)
                    .start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            optionContainer.removeAllViews();
        }, ANIMATION_DURATION + 200);
    }

    private void handleBotResponse(String selectedText) {
        String response = "";
        String[] nextOptions = null;
        int nextStage = -1;

        switch (currentStage) {
            case 1:
                if (selectedText.contains("overspending")) {
                    response = "I analyzed your spending patterns. You've exceeded your Food budget 3 times this month, spending ₹2,400 extra. Your main triggers are weekend dining and late-night food orders. 📊";
                    nextOptions = new String[]{"📋 Show detailed breakdown", "⏰ Set daily spending alerts", "🔄 Reset my budget limits"};
                    nextStage = 2;
                } else if (selectedText.contains("save more")) {
                    response = "Great question! Based on your spending history, I recommend the 50/30/20 rule. You're currently saving 12%, but we can get you to 20% by cutting weekend entertainment by 30%. 💪";
                    nextOptions = new String[]{"💡 Show me saving strategies", "🔄 Enable round-up savings", "📱 Set up automatic transfers"};
                    nextStage = 2;
                } else if (selectedText.contains("habit")) {
                    response = "Here's a powerful habit: Every Sunday, review your week's spending and set limits for the upcoming week. This simple practice can reduce overspending by 40%! 🎯";
                    nextOptions = new String[]{"📅 Set weekly review reminder", "📊 Track my progress", "💬 Get motivational quotes"};
                    nextStage = 2;
                } else if (selectedText.contains("last week")) {
                    response = "Last week you spent ₹4,700 total:\n• Food: ₹1,800 (38%)\n• Transport: ₹1,000 (21%)\n• Entertainment: ₹900 (19%)\n• Shopping: ₹600 (13%)\n• Others: ₹400 (9%) 📈";
                    nextOptions = new String[]{"📊 Compare with previous weeks", "🎯 Set weekly spending limit", "📱 Get daily spending updates"};
                    nextStage = 2;
                } else if (selectedText.contains("expense category")) {
                    response = "Your biggest expense category is Food at ₹6,000/month (35% of total spending). This is 20% above the recommended 25% for food expenses. 🍽️";
                    nextOptions = new String[]{"🍳 Get meal planning tips", "📊 Set food budget alerts", "🛒 Track grocery vs dining out"};
                    nextStage = 2;
                } else if (selectedText.contains("daily saving tip")) {
                    response = "💡 Today's Smart Tip: Skip one food delivery order this week and cook at home instead. This simple change can save you ₹500-800 weekly! 🏠👨‍🍳";
                    nextOptions = new String[]{"🍳 Get easy recipes", "📱 More daily tips", "⏰ Set cooking reminders"};
                    nextStage = 2;
                } else if (selectedText.contains("stick to my budget")) {
                    response = "Here are 3 proven strategies:\n1. Use the envelope method for discretionary spending\n2. Check your balance before every purchase\n3. Set up spending alerts at 80% of budget limits 🎯";
                    nextOptions = new String[]{"📱 Enable smart alerts", "📊 Visualize my spending", "🎯 Set micro-goals"};
                    nextStage = 2;
                } else if (selectedText.contains("remind me")) {
                    response = "Perfect! I'll send you daily saving reminders at 9 PM. Each reminder will include your daily spending summary and a personalized tip. 📱⏰";
                    nextOptions = new String[]{"⏰ Change reminder time", "📅 Make it weekly instead", "❌ Cancel reminders"};
                    nextStage = 2;
                } else if (selectedText.contains("subscriptions")) {
                    response = "I found 5 active subscriptions totaling ₹2,100/month:\n• Netflix: ₹649\n• Spotify: ₹119\n• Amazon Prime: ₹329\n• Gym: ₹800\n• Adobe: ₹203\n\nYou haven't used Adobe in 2 months! 📱";
                    nextOptions = new String[]{"❌ Cancel unused subscriptions", "📊 Set subscription budget limit", "🔄 Enable auto-tracking"};
                    nextStage = 2;
                }
                break;

            case 2:
                response = getStage2Response(selectedText);
                nextOptions = new String[]{"🎯 Set a savings challenge", "📊 Show my progress", "💡 Get more tips"};
                nextStage = 3;
                break;

            case 3:
                response = getStage3Response(selectedText);
                nextOptions = new String[]{"🏆 Start 30-day challenge", "📈 View detailed analytics", "🔄 Restart conversation"};
                nextStage = 4;
                break;

            default:
                response = "That's all for now! You're on the right track to better financial health. Keep up the great work! 🌟";
                endConversation();
                return;
        }

        addBotMessage(response);

        // Make variables effectively final for lambda usage
        final String[] finalNextOptions = nextOptions;
        final int finalNextStage = nextStage;

        if (finalNextOptions != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showOptions(finalNextOptions, finalNextStage);
            }, 500);
        }
    }

    private String getStage2Response(String selectedText) {
        if (selectedText.contains("breakdown") || selectedText.contains("detailed")) {
            return "Here's your detailed breakdown:\n\n📅 Monday: ₹680 (Lunch ₹320, Coffee ₹160, Snacks ₹200)\n📅 Tuesday: ₹520 (Groceries ₹400, Coffee ₹120)\n📅 Wednesday: ₹890 (Dinner out ₹650, Lunch ₹240)\n\nPattern: You spend 60% more on weekdays vs weekends! 📊";
        } else if (selectedText.contains("alerts") || selectedText.contains("reminder")) {
            return "✅ Smart alerts activated! You'll get notifications when:\n• You're at 50% of daily budget\n• You're at 80% of category budget\n• You make an unusual large purchase\n• Weekly spending review is due 📱";
        } else if (selectedText.contains("strategies") || selectedText.contains("tips")) {
            return "🎯 Top 3 Saving Strategies for you:\n\n1. **Meal Prep Sundays**: Save ₹1,200/week\n2. **30-Second Rule**: Wait 30 seconds before any purchase >₹500\n3. **Visual Budget**: Use our spending tracker daily\n\nThese can increase your savings by 35%! 💪";
        } else if (selectedText.contains("round-up") || selectedText.contains("automatic")) {
            return "🔄 Round-up savings activated! Every purchase will be rounded to the nearest ₹10, and the difference goes to your savings.\n\nExample: ₹47 purchase → ₹3 saved\nEstimated monthly savings: ₹400-600! 💰";
        } else {
            return "Great choice! This feature will help you build better financial habits. Your personalized plan is being created... 🎯";
        }
    }

    private String getStage3Response(String selectedText) {
        if (selectedText.contains("challenge")) {
            return "🏆 30-Day Money Challenge Started!\n\nWeek 1: Track every expense\nWeek 2: Cut one unnecessary expense\nWeek 3: Find one new way to save\nWeek 4: Plan next month's budget\n\nReward: ₹500 bonus to savings! 🎉";
        } else if (selectedText.contains("progress") || selectedText.contains("analytics")) {
            return "📈 Your Financial Health Score: 7.2/10\n\n✅ Strengths:\n• Consistent income tracking\n• Good emergency fund\n\n⚠️ Areas to improve:\n• Food spending (35% vs 25% recommended)\n• Impulse purchases\n\nYou're doing great! 🌟";
        } else {
            return "Perfect! You're building excellent financial habits. Remember, small consistent changes lead to big results over time! 💪";
        }
    }

    private void addBotMessage(String message) {
        String timestamp = getCurrentTimestamp();
        ChatModel chatModel = new ChatModel(message, false, timestamp);
        chatList.add(chatModel);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
        botMessageCount++;
    }

    private void addUserMessage(String message) {
        String timestamp = getCurrentTimestamp();
        ChatModel chatModel = new ChatModel(message, true, timestamp);
        chatList.add(chatModel);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
    }

    private void showTypingIndicator() {
        isTyping = true;
        statusText.setText("Typing...");
        typingIndicator.setVisibility(View.VISIBLE);
        typingIndicator.setAlpha(0f);
        typingIndicator.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void hideTypingIndicator() {
        isTyping = false;
        statusText.setText("Online");
        typingIndicator.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        typingIndicator.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void endConversation() {
        addBotMessage("🎉 Great conversation! You're well on your way to better financial health. Feel free to come back anytime for more personalized advice!\n\nStay smart with your money! 💰✨");

        // Show restart option
        String[] restartOptions = {"🔄 Start new conversation", "📊 View my financial summary", "❌ Close chat"};
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showRestartOptions(restartOptions);
        }, 1000);
    }

    private void showRestartOptions(String[] options) {
        optionContainer.removeAllViews();

        for (String option : options) {
            MaterialCardView card = createRestartCard(option);
            optionContainer.addView(card);
        }
    }

    private MaterialCardView createRestartCard(String option) {
        MaterialCardView card = createOptionCard(option, -1);

        card.setOnClickListener(v -> {
            if (option.contains("Start new")) {
                restartConversation();
            } else if (option.contains("summary")) {
                showFinancialSummary();
            } else {
                finish();
            }
        });

        return card;
    }

    private void restartConversation() {
        // Reset state
        botMessageCount = 0;
        currentStage = 0;
        chatList.clear();
        chatAdapter.notifyDataSetChanged();
        optionContainer.removeAllViews();

        // Start fresh
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showInitialOptions();
        }, 500);
    }

    private void showFinancialSummary() {
        addBotMessage("📊 Your Financial Summary:\n\n💰 Total Savings: ₹45,000\n📈 Monthly Income: ₹75,000\n💸 Monthly Expenses: ₹52,000\n📊 Savings Rate: 31%\n\n🎯 Goal: Increase to 35% by next month!\n\nYou're doing excellent! 🌟");
    }

    private void showMenuOptions(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu, popupMenu.getMenu());


        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                Intent intent = new Intent(AIChatActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_profile) {
                Intent intent1 = new Intent(AIChatActivity.this, ProfileActivity.class);
                startActivity(intent1);
                return true;
            } else if (id == R.id.menu_settings) {
                Intent intent2 = new Intent(AIChatActivity.this, SettingsActivity.class);
                startActivity(intent2);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void scrollToBottom() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (chatList.size() > 0) {
                chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
            }
        }, 100);
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    // Animation Methods
    private void animateCardClick(View card) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1f);

        scaleX.setDuration(150);
        scaleY.setDuration(150);

        scaleX.start();
        scaleY.start();
    }

    private void animateFabClick() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(voiceFab, "scaleX", 1f, 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(voiceFab, "scaleY", 1f, 0.8f, 1f);

        scaleX.setDuration(150);
        scaleY.setDuration(150);

        scaleX.start();
        scaleY.start();
    }

    private void showVoiceFab() {
        if (voiceFab.getVisibility() != View.VISIBLE) {
            voiceFab.setVisibility(View.VISIBLE);
            voiceFab.setScaleX(0f);
            voiceFab.setScaleY(0f);
            voiceFab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .start();
        }
    }

    private void hideVoiceFab() {
        if (voiceFab.getVisibility() == View.VISIBLE) {
            voiceFab.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            voiceFab.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void animateExit() {
        chatRecyclerView.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(ANIMATION_DURATION)
                .start();

        optionContainer.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    @Override
    public void onBackPressed() {
        animateExit();
        new Handler(Looper.getMainLooper()).postDelayed(super::onBackPressed, ANIMATION_DURATION);
    }
}
