package app.dailyroutine.ChatBot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.BuildConfig;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import app.dailyroutine.R;

public class PrivacyBottomSheetDialog extends AppCompatActivity {

    private Context context;
    private BottomSheetDialog dialog;
 private TextView versionid;
    public PrivacyBottomSheetDialog(Context context) {
        this.context = context;
        setupDialog();
    }

    @SuppressLint("SetTextI18n")
    private void setupDialog() {
        dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.privacy_bottom_sheet, null);

        dialog.setContentView(view);
        versionid = view.findViewById(R.id.versionid);
        try {
            String versionName = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            versionid.setText("Version " + versionName);

            versionid.setText("Daily Routine v" + versionName + "• Last updated: Today");
        } catch (PackageManager.NameNotFoundException e) {
            versionid.setText("Daily Routine v1.0• Last updated: Today");
        }
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

        // Setup RecyclerView for privacy items
        RecyclerView recyclerView = view.findViewById(R.id.privacyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new PrivacyAdapter(getPrivacyItems()));

        // Setup buttons
        MaterialButton closeButton = view.findViewById(R.id.closeButton);
        MaterialButton deleteDataButton = view.findViewById(R.id.deleteDataButton);


        closeButton.setOnClickListener(v -> dialog.dismiss());
        deleteDataButton.setOnClickListener(v -> showDeleteDataConfirmation());


     }

    private List<PrivacyItem> getPrivacyItems() {
        List<PrivacyItem> items = new ArrayList<>();

        items.add(new PrivacyItem(
                R.drawable.ic_shield_check,
                "Local Data Storage",
                "All your financial data is stored securely on your device only. No cloud storage or external servers.",
                "#4CAF50"
        ));

        items.add(new PrivacyItem(
                R.drawable.ic_bank_security,
                "Financial Data Protection",
                "Your spending records, budgets, and financial insights are encrypted and protected with industry-standard security.",
                "#2196F3"
        ));

        items.add(new PrivacyItem(
                R.drawable.ic_no_share,
                "Zero Third-Party Sharing",
                "We never share, sell, or transmit your personal financial information to any third parties or advertisers.",
                "#FF9800"
        ));

        items.add(new PrivacyItem(
                R.drawable.ic_user_control,
                "Complete User Control",
                "You have full control over your data. Export, backup, or delete all information at any time.",
                "#9C27B0"
        ));

        items.add(new PrivacyItem(
                R.drawable.ic_no_tracking,
                "No Tracking or Analytics",
                "We don't use tracking pixels, analytics, or collect usage data. Your privacy is completely protected.",
                "#607D8B"
        ));

        items.add(new PrivacyItem(
                R.drawable.ic_permissions,
                "Minimal Permissions",
                "We only request essential permissions needed for core functionality like notifications and storage.",
                "#795548"
        ));

        return items;
    }

    private void showDeleteDataConfirmation() {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete All Data")
                .setMessage("This will permanently delete all your financial records, budgets, and app data. This action cannot be undone.")
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Delete All", (dialogInterface, i) -> {
                    deleteAllAppData();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAllAppData() {
        try {
            // Clear SharedPreferences
            context.getSharedPreferences("expenses", Context.MODE_PRIVATE)
                    .edit().clear().apply();
            context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                    .edit().clear().apply();
            context.getSharedPreferences("StreakPrefs", Context.MODE_PRIVATE)
                    .edit().clear().apply();
            context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
                    .edit().clear().apply();

            Toast.makeText(context, "All data has been deleted successfully", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // Privacy Item Model
    public static class PrivacyItem {
        public int iconRes;
        public String title;
        public String description;
        public String colorHex;

        public PrivacyItem(int iconRes, String title, String description, String colorHex) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
            this.colorHex = colorHex;
        }
    }

    // Privacy Adapter
    private class PrivacyAdapter extends RecyclerView.Adapter<PrivacyAdapter.ViewHolder> {
        private List<PrivacyItem> items;

        public PrivacyAdapter(List<PrivacyItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.privacy_item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PrivacyItem item = items.get(position);

            holder.icon.setImageResource(item.iconRes);
            holder.title.setText(item.title);
            holder.description.setText(item.description);

            // Set icon tint color
            holder.icon.setColorFilter(Color.parseColor(item.colorHex));

            // Add subtle animation
            holder.cardView.setOnClickListener(v -> {
                holder.cardView.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            holder.cardView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, description;
            MaterialCardView cardView;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.privacyIcon);
                title = itemView.findViewById(R.id.privacyTitle);
                description = itemView.findViewById(R.id.privacyDescription);
                cardView = itemView.findViewById(R.id.privacyCard);
            }
        }
    }
}
