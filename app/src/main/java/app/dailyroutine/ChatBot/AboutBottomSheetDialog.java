package app.dailyroutine.ChatBot;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

import app.dailyroutine.R;

public class AboutBottomSheetDialog extends AppCompatActivity {

    private Context context;
    private BottomSheetDialog dialog;
    private TextView versionText;
    public AboutBottomSheetDialog(Context context) {
        this.context = context;
        setupDialog();
    }

    private void setupDialog() {
        dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.about_bottom_sheet, null);
        dialog.setContentView(view);
        versionText = view.findViewById(R.id.versionText);
        try {
            String versionName = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            versionText.setText("Version " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("Version 1.0");
        }
        // Setup animations
        setupAnimations(view);
        // FIXED: Properly set transparent background for rounded corners
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        // FIXED: Set behavior for better UX
        dialog.getBehavior().setPeekHeight(800);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);
//        versionText = view.findViewById(R.id.versionText);
//        String versionName = null;
//        try {
//            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        versionText.setText("Version " + versionName);

        // Setup RecyclerView for features
        RecyclerView featuresRecyclerView = view.findViewById(R.id.featuresRecyclerView);
        if (featuresRecyclerView != null) {
            featuresRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            featuresRecyclerView.setAdapter(new FeaturesAdapter(getFeaturesList()));
            // Animate features list
            animateFeaturesList(featuresRecyclerView);
        }

        // Setup buttons with null checks
        MaterialButton contactButton = view.findViewById(R.id.contactButton);
        MaterialButton shareButton = view.findViewById(R.id.shareButton);
        MaterialButton rateButton = view.findViewById(R.id.rateButton);
        MaterialButton closeButton = view.findViewById(R.id.closeButton);

        if (contactButton != null) {
            contactButton.setOnClickListener(v -> openEmail());
        }
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> shareApp());
        }
        if (rateButton != null) {
            rateButton.setOnClickListener(v -> rateApp());
        }
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void setupAnimations(View view) {
        // App icon animation
        ImageView appIcon = view.findViewById(R.id.appIcon);
        if (appIcon != null) {
            ObjectAnimator iconRotation = ObjectAnimator.ofFloat(appIcon, "rotation", 0f, 360f);
            iconRotation.setDuration(2000);
            iconRotation.setInterpolator(new AccelerateDecelerateInterpolator());
            iconRotation.setRepeatCount(ValueAnimator.INFINITE);
            iconRotation.setRepeatMode(ValueAnimator.RESTART);
            iconRotation.start();
        }



        ObjectAnimator pulse = ObjectAnimator.ofFloat(versionText, "alpha", 0.5f, 1.0f);
        pulse.setDuration(1500);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.start();
    }

    private void animateFeaturesList(RecyclerView recyclerView) {
        recyclerView.postDelayed(() -> {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                if (child != null) {
                    child.setAlpha(0f);
                    child.setTranslationX(300f);
                    child.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(500)
                            .setStartDelay(i * 100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }
            }
        }, 300);
    }

    private List<FeatureItem> getFeaturesList() {
        List<FeatureItem> features = new ArrayList<>();

        features.add(new FeatureItem(
                R.drawable.ic_calendar_feature,
                "Calendar View",
                "View and filter expenses by specific days using the calendar",
                "#4CAF50"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_reminder_feature,
                "Smart Reminders",
                "Set daily or custom reminders to log expenses or savings",
                "#FF9800"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_graph_feature,
                "Graph Analytics",
                "Visualize your spending and savings with intuitive bar and pie charts",
                "#2196F3"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_piggy_bank_feature,
                "Piggy Bank",
                "Save extra money in a virtual piggy bank to track savings goals",
                "#E91E63"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_history_feature,
                "Complete History",
                "Check complete history of all your expenses with reset records",
                "#9C27B0"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_settings_feature,
                "Customization",
                "Customize app preferences, themes, and notifications",
                "#607D8B"
        ));

        return features;
    }

    private void openEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:info.dailyroutine02@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Pocket Money Manager - Feedback");
            context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out Pocket Money Manager - the best app to track your daily expenses and manage money smartly! 💰📱");
            context.startActivity(Intent.createChooser(shareIntent, "Share App"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rateApp() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName()));
            context.startActivity(rateIntent);
        } catch (Exception e) {
            try {
                Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
                context.startActivity(rateIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    // Feature Item Model
    public static class FeatureItem {
        public int iconRes;
        public String title;
        public String description;
        public String colorHex;

        public FeatureItem(int iconRes, String title, String description, String colorHex) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
            this.colorHex = colorHex;
        }
    }

    // Features Adapter
    private class FeaturesAdapter extends RecyclerView.Adapter<FeaturesAdapter.ViewHolder> {
        private List<FeatureItem> features;

        public FeaturesAdapter(List<FeatureItem> features) {
            this.features = features;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.feature_item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeatureItem feature = features.get(position);

            if (holder.icon != null) {
                holder.icon.setImageResource(feature.iconRes);
                // Set icon tint color
                try {
                    holder.icon.setColorFilter(Color.parseColor(feature.colorHex));
                } catch (Exception e) {
                    // Fallback color if parsing fails
                    holder.icon.setColorFilter(Color.parseColor("#4CAF50"));
                }
            }

            if (holder.title != null) {
                holder.title.setText(feature.title);
            }

            if (holder.description != null) {
                holder.description.setText(feature.description);
            }

            // Add click animation
            if (holder.cardView != null) {
                holder.cardView.setOnClickListener(v -> {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.95f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.95f, 1f);
                    scaleX.setDuration(200);
                    scaleY.setDuration(200);
                    scaleX.start();
                    scaleY.start();
                });
            }
        }

        @Override
        public int getItemCount() {
            return features != null ? features.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, description;
            MaterialCardView cardView;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.featureIcon);
                title = itemView.findViewById(R.id.featureTitle);
                description = itemView.findViewById(R.id.featureDescription);
                cardView = itemView.findViewById(R.id.featureCard);
            }
        }
    }
}
