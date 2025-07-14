package app.dailyroutine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView totalTextView, itemCountTextView;
    private MaterialCardView totalCard;
    private View emptyStateLayout;
    private CircularProgressIndicator loadingProgress;
    private MaterialButton backButton, sortButton, clearAllButton;

    private List<HistoryModel> historyList = new ArrayList<>();
    private HistoryAdapter adapter;
    private SharedPreferences prefs;
    private boolean isAscending = false;

    private static final int ANIMATION_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadHistoryWithAnimation();
    }

    private void initViews() {
        totalTextView = findViewById(R.id.totalTextView);
        itemCountTextView = findViewById(R.id.itemCountTextView);
        totalCard = findViewById(R.id.totalCard);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgress = findViewById(R.id.loadingProgress);
        backButton = findViewById(R.id.backButton);
        sortButton = findViewById(R.id.sortButton);
        clearAllButton = findViewById(R.id.clearAllButton);

        prefs = getSharedPreferences("MonthlyHistory", MODE_PRIVATE);

         animateViewsIn();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(historyList, new HistoryAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position) {
                showDeleteConfirmation(position);
            }
        });

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);

        // Enhanced item animator
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(ANIMATION_DURATION);
        animator.setRemoveDuration(ANIMATION_DURATION);
        animator.setMoveDuration(ANIMATION_DURATION);
        animator.setChangeDuration(ANIMATION_DURATION);
        historyRecyclerView.setItemAnimator(animator);

        // Swipe to delete
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmation(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(historyRecyclerView);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            animateViewsOut();
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, ANIMATION_DURATION);
        });

        sortButton.setOnClickListener(v -> toggleSort());

        clearAllButton.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void loadHistoryWithAnimation() {
        showLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadHistory();
            showLoading(false);

            if (historyList.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                animateRecyclerView();
            }
        }, 500); // Simulate loading time
    }

    private void loadHistory() {
        historyList.clear();
        Map<String, ?> all = prefs.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                historyList.add(new HistoryModel(entry.getKey(), (Integer) entry.getValue()));
            }
        }

        sortHistoryByDate();
        updateTotalWithAnimation();
        adapter.notifyDataSetChanged();
    }

    private void sortHistoryByDate() {
        Collections.sort(historyList, new Comparator<HistoryModel>() {
            @Override
            public int compare(HistoryModel h1, HistoryModel h2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.getDefault());
                    Date date1 = sdf.parse(h1.getDateTime());
                    Date date2 = sdf.parse(h2.getDateTime());

                    if (isAscending) {
                        return date1.compareTo(date2);
                    } else {
                        return date2.compareTo(date1);
                    }
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    private void toggleSort() {
        isAscending = !isAscending;
        sortButton.setText(isAscending ? "Oldest First" : "Newest First");

        sortHistoryByDate();
        adapter.notifyDataSetChanged();
        animateRecyclerView();
    }

    private void updateTotalWithAnimation() {
        int sum = 0;
        for (HistoryModel item : historyList) {
            sum += item.getAmount();
        }

         ValueAnimator animator = ValueAnimator.ofInt(0, sum);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            totalTextView.setText("₹" + animatedValue);
        });
        animator.start();

         String countText = historyList.size() + (historyList.size() == 1 ? " entry" : " entries");
        itemCountTextView.setText(countText);

        // Animate total card
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(totalCard, "scaleX", 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(totalCard, "scaleY", 0.95f, 1.0f);
        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);
        scaleX.start();
        scaleY.start();
    }

    private void showDeleteConfirmation(int position) {
        HistoryModel item = historyList.get(position);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?\n\nAmount: ₹" + item.getAmount() + "\nDate: " + item.getDateTime())
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(position))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyItemChanged(position); // Reset swipe
                })
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    private void showClearAllConfirmation() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, "No entries to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all " + historyList.size() + " entries? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(int position) {
        if (position >= 0 && position < historyList.size()) {
            HistoryModel removed = historyList.get(position);
            historyList.remove(position);

            // Remove from SharedPreferences
            prefs.edit().remove(removed.getDateTime()).apply();

            // Animate removal
            adapter.notifyItemRemoved(position);

            // Update total with animation
            updateTotalWithAnimation();

            // Show empty state if needed
            if (historyList.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(this::showEmptyState, ANIMATION_DURATION);
            }

            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllHistory() {
        int itemCount = historyList.size();
        historyList.clear();

        // Clear SharedPreferences
        prefs.edit().clear().apply();

        // Animate removal
        adapter.notifyItemRangeRemoved(0, itemCount);

        // Update total
        updateTotalWithAnimation();

        // Show empty state
        new Handler(Looper.getMainLooper()).postDelayed(this::showEmptyState, ANIMATION_DURATION);

        Toast.makeText(this, "All entries cleared", Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        emptyStateLayout.setAlpha(0f);
        emptyStateLayout.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void hideEmptyState() {
        if (emptyStateLayout.getVisibility() == View.VISIBLE) {
            emptyStateLayout.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateLayout.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void animateViewsIn() {
        // Animate total card
        totalCard.setTranslationY(-100f);
        totalCard.setAlpha(0f);
        totalCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateViewsOut() {
        totalCard.animate()
                .translationY(-100f)
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .start();

        historyRecyclerView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void animateRecyclerView() {
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        historyRecyclerView.startAnimation(slideIn);
    }

    @Override
    public void onBackPressed() {
        animateViewsOut();
        new Handler(Looper.getMainLooper()).postDelayed(super::onBackPressed, ANIMATION_DURATION);
    }
}
