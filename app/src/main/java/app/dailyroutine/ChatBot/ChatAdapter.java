package app.dailyroutine.ChatBot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import app.dailyroutine.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatModel> messages;
    private final Context context;

    public ChatAdapter(List<ChatModel> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel message = messages.get(position);

        if (message.isUser()) {
            setupUserMessage(holder, message);
        } else {
            setupBotMessage(holder, message);
        }

        // Add entrance animation
        animateMessage(holder.itemView, position);
    }

    private void setupUserMessage(ChatViewHolder holder, ChatModel message) {
        // Hide bot layout, show user layout
        holder.botMessageLayout.setVisibility(View.GONE);
        holder.userMessageLayout.setVisibility(View.VISIBLE);

        // Set message content
        holder.userMessageText.setText(message.getMessage());
        holder.userTimestamp.setText(message.getTimestamp());

        // Set message status (delivered, read, etc.)
        holder.messageStatus.setImageResource(R.drawable.ic_check_double);
        holder.messageStatus.setColorFilter(context.getColor(R.color.success_color));
    }

    private void setupBotMessage(ChatViewHolder holder, ChatModel message) {
        // Hide user layout, show bot layout
        holder.userMessageLayout.setVisibility(View.GONE);
        holder.botMessageLayout.setVisibility(View.VISIBLE);

        // Set message content
        holder.botMessageText.setText(message.getMessage());
        holder.botTimestamp.setText(message.getTimestamp());

        // Add special styling for different message types
        if (message.getMessage().contains("🎉") || message.getMessage().contains("✅")) {
            holder.botMessageCard.setCardBackgroundColor(context.getColor(R.color.success_light));
        } else if (message.getMessage().contains("⚠️") || message.getMessage().contains("❌")) {
            holder.botMessageCard.setCardBackgroundColor(context.getColor(R.color.warning_light));
        } else {
            holder.botMessageCard.setCardBackgroundColor(context.getColor(R.color.white));
        }
    }

    private void animateMessage(View view, int position) {
        // Staggered animation for messages
        Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        slideIn.setStartOffset(position * 100L);
        view.startAnimation(slideIn);

        // Scale animation for emphasis
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        // Bot message views
        LinearLayout botMessageLayout;
        ImageView botAvatar;
        MaterialCardView botMessageCard;
        TextView botMessageText;
        TextView botTimestamp;

        // User message views
        LinearLayout userMessageLayout;
        ImageView userAvatar;
        MaterialCardView userMessageCard;
        TextView userMessageText;
        TextView userTimestamp;
        ImageView messageStatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize bot message views
            botMessageLayout = itemView.findViewById(R.id.botMessageLayout);
            botAvatar = itemView.findViewById(R.id.botAvatar);
            botMessageCard = itemView.findViewById(R.id.botMessageCard);
            botMessageText = itemView.findViewById(R.id.botMessageText);
            botTimestamp = itemView.findViewById(R.id.botTimestamp);

            // Initialize user message views
            userMessageLayout = itemView.findViewById(R.id.userMessageLayout);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userMessageCard = itemView.findViewById(R.id.userMessageCard);
            userMessageText = itemView.findViewById(R.id.userMessageText);
            userTimestamp = itemView.findViewById(R.id.userTimestamp);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }
}
