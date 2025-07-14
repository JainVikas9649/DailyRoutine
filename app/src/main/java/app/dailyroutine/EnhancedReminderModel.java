package app.dailyroutine;

import java.util.Calendar;
import java.util.Locale;

public class EnhancedReminderModel {
    private String id;
    private String title;
    private String category;
    private int hour;
    private int minute;
    private String date;
    private String repeatType; // "once", "daily", "weekly", "monthly"
    private int priority; // 1-5
    private boolean isActive;
    private long createdAt;
    private String categoryIcon;

    public EnhancedReminderModel(String title, String category, int hour, int minute, 
                               String date, String repeatType, int priority) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.category = category;
        this.hour = hour;
        this.minute = minute;
        this.date = date;
        this.repeatType = repeatType;
        this.priority = priority;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.categoryIcon = getCategoryIcon(category);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.categoryIcon = getCategoryIcon(category);
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getCategoryIcon() { return categoryIcon; }

    public String getFormattedTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        
        int displayHour = hour;
        String amPm = "AM";
        
        if (hour == 0) {
            displayHour = 12;
        } else if (hour == 12) {
            amPm = "PM";
        } else if (hour > 12) {
            displayHour = hour - 12;
            amPm = "PM";
        }
        
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }

    public String getPriorityColor() {
        switch (priority) {
            case 1: return "#4CAF50"; // Low - Green
            case 2: return "#8BC34A"; // Low-Medium - Light Green
            case 3: return "#FFC107"; // Medium - Yellow
            case 4: return "#FF9800"; // High - Orange
            case 5: return "#F44336"; // Critical - Red
            default: return "#9E9E9E"; // Default - Gray
        }
    }

    public String getPriorityText() {
        switch (priority) {
            case 1: return "Low";
            case 2: return "Low-Medium";
            case 3: return "Medium";
            case 4: return "High";
            case 5: return "Critical";
            default: return "Medium";
        }
    }

    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "expense": return "💰";
            case "health": return "🏥";
            case "work": return "💼";
            case "personal": return "👤";
            case "fitness": return "💪";
            case "food": return "🍽️";
            case "travel": return "✈️";
            case "shopping": return "🛒";
            default: return "⏰";
        }
    }

    public boolean isUpcoming() {
        Calendar now = Calendar.getInstance();
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.HOUR_OF_DAY, hour);
        reminderTime.set(Calendar.MINUTE, minute);
        
        return reminderTime.after(now) && isActive;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnhancedReminderModel other = (EnhancedReminderModel) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
