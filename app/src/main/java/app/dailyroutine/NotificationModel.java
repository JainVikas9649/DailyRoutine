package app.dailyroutine;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private Priority priority;
    private long timestamp;
    private boolean isRead;
    private String actionData;

    public enum NotificationType {
        BUDGET_ALERT,
        SPENDING_TIP,
        EXPENSE_REMINDER,
        SAVINGS_GOAL,
        WEEKLY_SUMMARY,
        ACHIEVEMENT,
        BILL_REMINDER
    }

    public enum Priority {
        LOW(1),
        MEDIUM(2),
        HIGH(3);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public NotificationModel() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.isRead = false;
    }

    public NotificationModel(String title, String message, NotificationType type, Priority priority, long timestamp) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getActionData() { return actionData; }
    public void setActionData(String actionData) { this.actionData = actionData; }

    public String getTypeIcon() {
        switch (type) {
            case BUDGET_ALERT: return "⚠️";
            case SPENDING_TIP: return "💡";
            case EXPENSE_REMINDER: return "📝";
            case SAVINGS_GOAL: return "🎯";
            case WEEKLY_SUMMARY: return "📊";
            case ACHIEVEMENT: return "🏆";
            case BILL_REMINDER: return "💳";
            default: return "🔔";
        }
    }

    public int getPriorityColor() {
        switch (priority) {
            case HIGH: return android.graphics.Color.parseColor("#F44336");
            case MEDIUM: return android.graphics.Color.parseColor("#FF9800");
            case LOW: return android.graphics.Color.parseColor("#4CAF50");
            default: return android.graphics.Color.parseColor("#2196F3");
        }
    }
}