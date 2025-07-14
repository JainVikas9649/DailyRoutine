package app.dailyroutine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<NotificationModel> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onNotificationClick(NotificationModel notification);
        void onNotificationDismiss(NotificationModel notification);
        void onNotificationMarkAsRead(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifications, OnNotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_modern, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        
        holder.titleText.setText(notification.getTypeIcon() + " " + notification.getTitle());
        holder.messageText.setText(notification.getMessage());
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        holder.timeText.setText(sdf.format(new Date(notification.getTimestamp())));
        
        // Set priority indicator
        holder.priorityIndicator.setBackgroundColor(notification.getPriorityColor());
        
        // Set read/unread state
        if (notification.isRead()) {
            holder.cardView.setAlpha(0.7f);
            holder.unreadIndicator.setVisibility(View.GONE);
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        }
        
        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
        
        holder.dismissButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationDismiss(notification);
            }
        });
        
        holder.markReadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationMarkAsRead(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView titleText, messageText, timeText;
        View priorityIndicator, unreadIndicator;
        ImageView dismissButton, markReadButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.notificationCard);
            titleText = itemView.findViewById(R.id.notificationTitle);
            messageText = itemView.findViewById(R.id.notificationMessage);
            timeText = itemView.findViewById(R.id.notificationTime);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            dismissButton = itemView.findViewById(R.id.dismissButton);
            markReadButton = itemView.findViewById(R.id.markReadButton);
        }
    }
}