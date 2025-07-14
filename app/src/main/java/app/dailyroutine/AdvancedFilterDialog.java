package app.dailyroutine;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdvancedFilterDialog {

    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterCriteria criteria);
    }

    public static class FilterCriteria {
        public boolean isDateRangeEnabled = false;
        public String fromDate = "";
        public String toDate = "";

        public boolean isAmountRangeEnabled = false;
        public double minAmount = 0;
        public double maxAmount = 10000;

        public boolean isCategoryEnabled = false;
        public List<String> selectedCategories = new ArrayList<>();

        public boolean isSearchEnabled = false;
        public String searchText = "";

        public boolean isQuickFilterEnabled = false;
        public String quickFilterType = "";

        public boolean hasPhoto = false;
        public boolean hasLocation = false;

        public boolean isEmpty() {
            return !isDateRangeEnabled && !isAmountRangeEnabled &&
                    !isCategoryEnabled && !isSearchEnabled &&
                    !isQuickFilterEnabled && !hasPhoto && !hasLocation;
        }

        public String getDescription() {
            List<String> descriptions = new ArrayList<>();

            if (isQuickFilterEnabled) {
                descriptions.add(quickFilterType);
            }

            if (isDateRangeEnabled) {
                descriptions.add("Date: " + fromDate + " to " + toDate);
            }

            if (isAmountRangeEnabled) {
                descriptions.add("Amount: ₹" + (int)minAmount + " - ₹" + (int)maxAmount);
            }

            if (isCategoryEnabled && !selectedCategories.isEmpty()) {
                descriptions.add("Categories: " + selectedCategories.size() + " selected");
            }

            if (isSearchEnabled && !searchText.isEmpty()) {
                descriptions.add("Search: \"" + searchText + "\"");
            }

            if (hasPhoto) {
                descriptions.add("With Photo");
            }

            if (hasLocation) {
                descriptions.add("With Location");
            }

            return String.join(", ", descriptions);
        }
    }

    private Context context;
    private OnFilterAppliedListener listener;
    private AlertDialog dialog;
    private View dialogView; // FIXED: Add dialogView as class member

    // Views
    private ChipGroup quickFilterChips, categoryChips;
    private Switch switchDateRange, switchAmountRange, switchCategory, switchSearch;
    private TextInputEditText editFromDate, editToDate, editMinAmount, editMaxAmount, editSearchText;
    private RangeSlider amountRangeSlider;
    private TextView amountRangeText, resetFiltersText;

    // Data
    private FilterCriteria currentCriteria = new FilterCriteria();
    private String[] categories = {
            "Food & Drinks", "Transportation", "Shopping", "Entertainment",
            "Bills & Utilities", "Health & Fitness", "Education", "Other"
    };

    public AdvancedFilterDialog(Context context, OnFilterAppliedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_advanced_filter, null); // FIXED: Store as class member
        initViews(dialogView);
        setupViews();

        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.show();
    }

    private void initViews(View view) {
        quickFilterChips = view.findViewById(R.id.quickFilterChips);
        categoryChips = view.findViewById(R.id.categoryChips);

        switchDateRange = view.findViewById(R.id.switchDateRange);
        switchAmountRange = view.findViewById(R.id.switchAmountRange);
        switchCategory = view.findViewById(R.id.switchCategory);
        switchSearch = view.findViewById(R.id.switchSearch);

        editFromDate = view.findViewById(R.id.editFromDate);
        editToDate = view.findViewById(R.id.editToDate);
        editMinAmount = view.findViewById(R.id.editMinAmount);
        editMaxAmount = view.findViewById(R.id.editMaxAmount);
        editSearchText = view.findViewById(R.id.editSearchText);

        amountRangeSlider = view.findViewById(R.id.amountRangeSlider);
        amountRangeText = view.findViewById(R.id.amountRangeText);
        resetFiltersText = view.findViewById(R.id.resetFiltersText);
    }

    private void setupViews() {
        setupQuickFilters();
        setupDateRange();
        setupAmountRange();
        setupCategoryFilter();
        setupSearchFilter();
        setupActionButtons();
    }

    private void setupQuickFilters() {
        // FIXED: Use dialogView instead of view and proper chip references
        Chip chipToday = dialogView.findViewById(R.id.chipToday);
        Chip chipThisWeek = dialogView.findViewById(R.id.chipThisWeek);
        Chip chipThisMonth = dialogView.findViewById(R.id.chipThisMonth);
        Chip chipWithPhoto = dialogView.findViewById(R.id.chipWithPhoto);
        Chip chipWithLocation = dialogView.findViewById(R.id.chipWithLocation);

        chipToday.setOnClickListener(v -> {
            clearOtherQuickFilters(chipToday);
            if (chipToday.isChecked()) {
                currentCriteria.isQuickFilterEnabled = true;
                currentCriteria.quickFilterType = "Today";
            } else {
                currentCriteria.isQuickFilterEnabled = false;
                currentCriteria.quickFilterType = "";
            }
        });

        chipThisWeek.setOnClickListener(v -> {
            clearOtherQuickFilters(chipThisWeek);
            if (chipThisWeek.isChecked()) {
                currentCriteria.isQuickFilterEnabled = true;
                currentCriteria.quickFilterType = "This Week";
            } else {
                currentCriteria.isQuickFilterEnabled = false;
                currentCriteria.quickFilterType = "";
            }
        });

        chipThisMonth.setOnClickListener(v -> {
            clearOtherQuickFilters(chipThisMonth);
            if (chipThisMonth.isChecked()) {
                currentCriteria.isQuickFilterEnabled = true;
                currentCriteria.quickFilterType = "This Month";
            } else {
                currentCriteria.isQuickFilterEnabled = false;
                currentCriteria.quickFilterType = "";
            }
        });

        chipWithPhoto.setOnClickListener(v -> {
            currentCriteria.hasPhoto = chipWithPhoto.isChecked();
        });

        chipWithLocation.setOnClickListener(v -> {
            currentCriteria.hasLocation = chipWithLocation.isChecked();
        });
    }

    private void clearOtherQuickFilters(Chip selectedChip) {
        for (int i = 0; i < quickFilterChips.getChildCount(); i++) {
            View child = quickFilterChips.getChildAt(i);
            if (child instanceof Chip && child != selectedChip) {
                Chip chip = (Chip) child;
                if (chip.getId() == R.id.chipToday ||
                        chip.getId() == R.id.chipThisWeek ||
                        chip.getId() == R.id.chipThisMonth) {
                    chip.setChecked(false);
                }
            }
        }
    }

    private void setupDateRange() {
        switchDateRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dialogView.findViewById(R.id.dateRangeContainer).setVisibility( // FIXED: Use dialogView
                    isChecked ? View.VISIBLE : View.GONE);
            currentCriteria.isDateRangeEnabled = isChecked;
        });

        editFromDate.setOnClickListener(v -> showDatePicker(true));
        editToDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void setupAmountRange() {
        switchAmountRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dialogView.findViewById(R.id.amountRangeContainer).setVisibility( // FIXED: Use dialogView
                    isChecked ? View.VISIBLE : View.GONE);
            currentCriteria.isAmountRangeEnabled = isChecked;
        });

        amountRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                currentCriteria.minAmount = values.get(0);
                currentCriteria.maxAmount = values.get(1);

                editMinAmount.setText(String.valueOf((int) currentCriteria.minAmount));
                editMaxAmount.setText(String.valueOf((int) currentCriteria.maxAmount));
                amountRangeText.setText("₹" + (int) currentCriteria.minAmount +
                        " - ₹" + (int) currentCriteria.maxAmount);
            }
        });
    }

    private void setupCategoryFilter() {
        switchCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            categoryChips.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            currentCriteria.isCategoryEnabled = isChecked;
        });

        // Add category chips
        for (String category : categories) {
            Chip chip = new Chip(context);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!currentCriteria.selectedCategories.contains(category)) {
                        currentCriteria.selectedCategories.add(category);
                    }
                } else {
                    currentCriteria.selectedCategories.remove(category);
                }
            });
            categoryChips.addView(chip);
        }
    }

    private void setupSearchFilter() {
        switchSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dialogView.findViewById(R.id.searchContainer).setVisibility( // FIXED: Use dialogView
                    isChecked ? View.VISIBLE : View.GONE);
            currentCriteria.isSearchEnabled = isChecked;
        });
    }

    private void setupActionButtons() {
        resetFiltersText.setOnClickListener(v -> resetAllFilters());

        dialogView.findViewById(R.id.btnCancelFilter).setOnClickListener(v -> dialog.dismiss()); // FIXED: Use dialogView

        dialogView.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> { // FIXED: Use dialogView
            // Get search text
            if (currentCriteria.isSearchEnabled) {
                currentCriteria.searchText = editSearchText.getText().toString().trim();
            }

            // Get manual amount inputs if they override slider
            if (currentCriteria.isAmountRangeEnabled) {
                String minStr = editMinAmount.getText().toString().trim();
                String maxStr = editMaxAmount.getText().toString().trim();

                if (!minStr.isEmpty()) {
                    try {
                        currentCriteria.minAmount = Double.parseDouble(minStr);
                    } catch (NumberFormatException ignored) {}
                }

                if (!maxStr.isEmpty()) {
                    try {
                        currentCriteria.maxAmount = Double.parseDouble(maxStr);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (listener != null) {
                listener.onFilterApplied(currentCriteria);
            }

            dialog.dismiss();
        });
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    String selectedDate = sdf.format(calendar.getTime());

                    if (isFromDate) {
                        editFromDate.setText(selectedDate);
                        currentCriteria.fromDate = selectedDate;
                    } else {
                        editToDate.setText(selectedDate);
                        currentCriteria.toDate = selectedDate;
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void resetAllFilters() {
        currentCriteria = new FilterCriteria();

        // Reset all switches
        switchDateRange.setChecked(false);
        switchAmountRange.setChecked(false);
        switchCategory.setChecked(false);
        switchSearch.setChecked(false);

        // Reset quick filter chips
        for (int i = 0; i < quickFilterChips.getChildCount(); i++) {
            View child = quickFilterChips.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }

        // Reset category chips
        for (int i = 0; i < categoryChips.getChildCount(); i++) {
            View child = categoryChips.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }

        // Reset input fields
        editFromDate.setText("");
        editToDate.setText("");
        editMinAmount.setText("");
        editMaxAmount.setText("");
        editSearchText.setText("");

        // Reset slider
        amountRangeSlider.setValues(0f, 10000f);

        Toast.makeText(context, "All filters reset", Toast.LENGTH_SHORT).show();
    }
}
