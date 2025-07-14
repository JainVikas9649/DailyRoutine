package app.dailyroutine;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EnhancedReminderActivity extends AppCompatActivity implements EnhancedReminderAdapter.OnReminderActionListener {

    private SmartReminderManager reminderManager;
    private EnhancedReminderAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvTotalReminders, tvActiveReminders, tvUpcomingReminders;
    private TextView tvEmptyState;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddReminder;

    private List<EnhancedReminderModel> allReminders;
    private List<EnhancedReminderModel> filteredReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_reminder);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupTabs();
        setupFab();
        loadReminders();
        updateStatistics();
        tvEmptyState.setOnClickListener(v -> {
            Intent intent = new Intent(EnhancedReminderActivity.this, MainActivity.class);
            intent.putExtra("open_reminder_dialog", true); // pass flag
            startActivity(intent);
        });
    }

    private void initializeViews() {
        reminderManager = new SmartReminderManager(this);
        
        recyclerView = findViewById(R.id.recyclerViewReminders);
        tvTotalReminders = findViewById(R.id.tvTotalReminders);
        tvActiveReminders = findViewById(R.id.tvActiveReminders);
        tvUpcomingReminders = findViewById(R.id.tvUpcomingReminders);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddReminder = findViewById(R.id.fabAddReminder);

        allReminders = new ArrayList<>();
        filteredReminders = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Smart Reminders");
        }
    }

    private void setupRecyclerView() {
        adapter = new EnhancedReminderAdapter(this, filteredReminders, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Active"));
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"));
        tabLayout.addTab(tabLayout.newTab().setText("Expense"));
        tabLayout.addTab(tabLayout.newTab().setText("Health"));
        tabLayout.addTab(tabLayout.newTab().setText("Work"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterReminders(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFab() {
        fabAddReminder.setOnClickListener(v -> showAddReminderDialog());
    }

    private void loadReminders() {
        allReminders = reminderManager.getAllReminders();
        filteredReminders.clear();
        filteredReminders.addAll(allReminders);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void filterReminders(int tabPosition) {
        filteredReminders.clear();

        switch (tabPosition) {
            case 0: // All
                filteredReminders.addAll(allReminders);
                break;
            case 1: // Active
                filteredReminders.addAll(reminderManager.getActiveReminders());
                break;
            case 2: // Upcoming
                filteredReminders.addAll(reminderManager.getUpcomingReminders());
                break;
            case 3: // Expense
                filteredReminders.addAll(reminderManager.getRemindersByCategory("Expense"));
                break;
            case 4: // Health
                filteredReminders.addAll(reminderManager.getRemindersByCategory("Health"));
                break;
            case 5: // Work
                filteredReminders.addAll(reminderManager.getRemindersByCategory("Work"));
                break;
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateStatistics() {
        int total = allReminders.size();
        int active = reminderManager.getActiveReminders().size();
        int upcoming = reminderManager.getUpcomingReminders().size();

        tvTotalReminders.setText(String.valueOf(total));
        tvActiveReminders.setText(String.valueOf(active));
        tvUpcomingReminders.setText(String.valueOf(upcoming));
    }

    private void updateEmptyState() {
        if (filteredReminders.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get dialog components
        EditText etReminderTitle = dialogView.findViewById(R.id.etReminderTitle);
        ChipGroup chipGroupCategories = dialogView.findViewById(R.id.chipGroupCategories);
        ChipGroup chipGroupRepeat = dialogView.findViewById(R.id.chipGroupRepeat);
        Slider sliderPriority = dialogView.findViewById(R.id.sliderPriority);
        Button btnSelectTime = dialogView.findViewById(R.id.btnSelectTime);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveReminder = dialogView.findViewById(R.id.btnSaveReminder);

        // Variables to store selected values
        final int[] selectedHour = {Calendar.getInstance().get(Calendar.HOUR_OF_DAY)};
        final int[] selectedMinute = {Calendar.getInstance().get(Calendar.MINUTE)};
        final String[] selectedDate = {getCurrentDate()};
        final String[] selectedCategory = {"Expense"};
        final String[] selectedRepeat = {"Daily"};

        // Setup category selection
        setupCategoryChips(chipGroupCategories, selectedCategory);

        // Setup repeat selection
        setupRepeatChips(chipGroupRepeat, selectedRepeat);

        // Time picker
        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        btnSelectTime.setText(String.format(Locale.getDefault(),
                                "%02d:%02d", hourOfDay, minute));
                    }, selectedHour[0], selectedMinute[0], true);
            timePickerDialog.show();
        });

        // Date picker
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);
                        selectedDate[0] = DateFormat.getDateInstance().format(selectedCal.getTime());
                        btnSelectDate.setText(selectedDate[0]);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSaveReminder.setOnClickListener(v -> {
            String title = etReminderTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show();
                return;
            }

            int priority = (int) sliderPriority.getValue();

            // Create enhanced reminder
            EnhancedReminderModel reminder = new EnhancedReminderModel(
                    title, selectedCategory[0], selectedHour[0], selectedMinute[0],
                    selectedDate[0], selectedRepeat[0], priority
            );

            // Save reminder
            reminderManager.saveReminder(reminder);

            // Refresh the list
            loadReminders();
            updateStatistics();

            Toast.makeText(this, "Smart reminder created successfully! 🎉", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupCategoryChips(ChipGroup chipGroup, String[] selectedCategory) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory[0] = chip.getText().toString().replace("💰 ", "")
                            .replace("🏥 ", "").replace("💼 ", "").replace("👤 ", "");
                }
            });
        }
    }

    private void setupRepeatChips(ChipGroup chipGroup, String[] selectedRepeat) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedRepeat[0] = chip.getText().toString().toLowerCase();
                }
            });
        }
    }

    private void showEditReminderDialog(EnhancedReminderModel reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get dialog components
        EditText etReminderTitle = dialogView.findViewById(R.id.etReminderTitle);
        ChipGroup chipGroupCategories = dialogView.findViewById(R.id.chipGroupCategories);
        ChipGroup chipGroupRepeat = dialogView.findViewById(R.id.chipGroupRepeat);
        Slider sliderPriority = dialogView.findViewById(R.id.sliderPriority);
        Button btnSelectTime = dialogView.findViewById(R.id.btnSelectTime);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSaveReminder = dialogView.findViewById(R.id.btnSaveReminder);

        // Pre-fill with existing data
        etReminderTitle.setText(reminder.getTitle());
        sliderPriority.setValue(reminder.getPriority());
        btnSelectTime.setText(reminder.getFormattedTime());
        btnSelectDate.setText(reminder.getDate());
        btnSaveReminder.setText("Update Reminder");

        // Variables to store selected values
        final int[] selectedHour = {reminder.getHour()};
        final int[] selectedMinute = {reminder.getMinute()};
        final String[] selectedDate = {reminder.getDate()};
        final String[] selectedCategory = {reminder.getCategory()};
        final String[] selectedRepeat = {reminder.getRepeatType()};

        // Pre-select category chip
        selectCategoryChip(chipGroupCategories, reminder.getCategory());

        // Pre-select repeat chip
        selectRepeatChip(chipGroupRepeat, reminder.getRepeatType());

        // Setup category selection
        setupCategoryChips(chipGroupCategories, selectedCategory);

        // Setup repeat selection
        setupRepeatChips(chipGroupRepeat, selectedRepeat);

        // Time picker
        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        btnSelectTime.setText(String.format(Locale.getDefault(),
                                "%02d:%02d", hourOfDay, minute));
                    }, selectedHour[0], selectedMinute[0], true);
            timePickerDialog.show();
        });

        // Date picker
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year, month, dayOfMonth);
                        selectedDate[0] = DateFormat.getDateInstance().format(selectedCal.getTime());
                        btnSelectDate.setText(selectedDate[0]);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSaveReminder.setOnClickListener(v -> {
            String title = etReminderTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show();
                return;
            }

            int priority = (int) sliderPriority.getValue();

            // Update reminder
            reminder.setTitle(title);
            reminder.setCategory(selectedCategory[0]);
            reminder.setHour(selectedHour[0]);
            reminder.setMinute(selectedMinute[0]);
            reminder.setDate(selectedDate[0]);
            reminder.setRepeatType(selectedRepeat[0]);
            reminder.setPriority(priority);

            // Save reminder
            reminderManager.saveReminder(reminder);

            // Refresh the list
            loadReminders();
            updateStatistics();

            Toast.makeText(this, "Reminder updated successfully! ✅", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void selectCategoryChip(ChipGroup chipGroup, String category) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            String chipText = chip.getText().toString();
            if (chipText.toLowerCase().contains(category.toLowerCase())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void selectRepeatChip(ChipGroup chipGroup, String repeatType) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(repeatType)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private String getCurrentDate() {
        return DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
    }

    // Enhanced Reminder Adapter Callbacks
    @Override
    public void onToggleReminder(EnhancedReminderModel reminder, boolean isActive) {
        reminderManager.toggleReminderStatus(reminder, isActive);
        String message = isActive ? "Reminder activated" : "Reminder deactivated";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        updateStatistics();
    }

    @Override
    public void onEditReminder(EnhancedReminderModel reminder) {
        showEditReminderDialog(reminder);
    }

    @Override
    public void onDeleteReminder(EnhancedReminderModel reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete '" + reminder.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    reminderManager.deleteReminder(reminder);
                    loadReminders();
                    updateStatistics();
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDuplicateReminder(EnhancedReminderModel reminder) {
        EnhancedReminderModel duplicate = reminderManager.duplicateReminder(reminder);
        loadReminders();
        updateStatistics();
        Toast.makeText(this, "Reminder duplicated", Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        loadReminders();
        updateStatistics();
    }
}
