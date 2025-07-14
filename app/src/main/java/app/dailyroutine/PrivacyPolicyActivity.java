package app.dailyroutine;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private FloatingActionButton fabScrollTop;
    private LinearLayout contentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        initializeViews();
        setupToolbar();
        setupScrollBehavior();
        animateContentEntry();
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        fabScrollTop = findViewById(R.id.fabScrollTop);
        contentContainer = findViewById(R.id.contentContainer);

        fabScrollTop.setOnClickListener(v -> {
            scrollView.smoothScrollTo(0, 0);
            animateFab(fabScrollTop, false);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Policy");
        }
    }

    private void setupScrollBehavior() {
        scrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 500) {
                animateFab(fabScrollTop, true);
            } else {
                animateFab(fabScrollTop, false);
            }
        });
    }

    private void animateFab(FloatingActionButton fab, boolean show) {
        if (show && fab.getVisibility() != View.VISIBLE) {
            fab.setVisibility(View.VISIBLE);
            fab.setScaleX(0f);
            fab.setScaleY(0f);
            fab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else if (!show && fab.getVisibility() == View.VISIBLE) {
            fab.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> fab.setVisibility(View.GONE))
                    .start();
        }
    }

    private void animateContentEntry() {
        contentContainer.setAlpha(0f);
        contentContainer.setTranslationY(50f);
        
        contentContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animate cards one by one
        animateCardsSequentially();
    }

    private void animateCardsSequentially() {
        LinearLayout container = findViewById(R.id.contentContainer);
        int childCount = container.getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            if (child instanceof MaterialCardView) {
                child.setAlpha(0f);
                child.setTranslationX(-100f);
                
                child.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(400)
                        .setStartDelay(i * 100L)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Add exit animation
        contentContainer.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    super.onBackPressed();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }
}
