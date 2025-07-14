package app.dailyroutine;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class EnhancedReminderAdapter extends RecyclerView.Adapter<EnhancedReminderAdapter.ViewHolder> {
    
    private List<EnhancedReminderModel> reminders;
    private Context context;
    private OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onToggleReminder(EnhancedReminderModel reminder, boolean isActive);
        void onEditReminder(EnhancedReminderModel reminder);
        void onDeleteReminder(EnhancedReminderModel reminder);
        void onDuplicateReminder(EnhancedReminderModel reminder);
    }

    public EnhancedReminderAdapter(Context context, List<EnhancedReminderModel> reminders, 
                                 OnReminderActionListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder_enhanced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnhancedReminderModel reminder = reminders.get(position);
        
        holder.tvCategoryIcon.setText(reminder.getCategoryIcon());
        holder.tvReminderTitle.setText(reminder.getTitle());
        holder.tvReminderTime.setText(reminder.getFormattedTime());
        holder.tvReminderRepeat.setText(reminder.getRepeatType().substring(0, 1).toUpperCase() + 
                                      reminder.getRepeatType().substring(1));
        
        // Set priority indicator color
        holder.viewPriorityIndicator.setBackgroundColor(Color.parseColor(reminder.getPriorityColor()));
        
        // Set switch state
        holder.switchReminderStatus.setChecked(reminder.isActive());
        holder.switchReminderStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setActive(isChecked);
            if (listener != null) {
                listener.onToggleReminder(reminder, isChecked);
            }
        });

        // Set up more options menu
        holder.btnMoreOptions.setOnClickListener(v -> showPopupMenu(v, reminder));

        // Dim inactive reminders
        float alpha = reminder.isActive() ? 1.0f : 0.6f;
        holder.itemView.setAlpha(alpha);
    }

    private void showPopupMenu(View view, EnhancedReminderModel reminder) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.reminder_options_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                if (listener != null) listener.onEditReminder(reminder);
                return true;
            } else if (itemId == R.id.menu_duplicate) {
                if (listener != null) listener.onDuplicateReminder(reminder);
                return true;
            } else if (itemId == R.id.menu_delete) {
                if (listener != null) listener.onDeleteReminder(reminder);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void updateReminders(List<EnhancedReminderModel> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    public void addReminder(EnhancedReminderModel reminder) {
        reminders.add(0, reminder);
        notifyItemInserted(0);
    }

    public void removeReminder(EnhancedReminderModel reminder) {
        int position = reminders.indexOf(reminder);
        if (position != -1) {
            reminders.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateReminder(EnhancedReminderModel reminder) {
        int position = reminders.indexOf(reminder);
        if (position != -1) {
            reminders.set(position, reminder);
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvReminderTitle, tvReminderTime, tvReminderRepeat;
        View viewPriorityIndicator;
        SwitchMaterial switchReminderStatus;
        ImageButton btnMoreOptions;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvReminderTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime);
            tvReminderRepeat = itemView.findViewById(R.id.tvReminderRepeat);
            viewPriorityIndicator = itemView.findViewById(R.id.viewPriorityIndicator);
            switchReminderStatus = itemView.findViewById(R.id.switchReminderStatus);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}
