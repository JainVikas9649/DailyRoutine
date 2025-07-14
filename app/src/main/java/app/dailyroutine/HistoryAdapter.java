package app.dailyroutine;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<HistoryModel> historyList;
    private final OnDeleteListener deleteListener;
    private static final int ANIMATION_DURATION = 300;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public HistoryAdapter(List<HistoryModel> historyList, OnDeleteListener deleteListener) {
        this.historyList = historyList;
        this.deleteListener = deleteListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView dateIcon;
        TextView dateText, timeText, amountText;
        MaterialButton deleteButton;
        View swipeIndicator;

        public ViewHolder(View view) {
            super(view);
            cardView = (MaterialCardView) view;
            dateIcon = view.findViewById(R.id.dateIcon);
            dateText = view.findViewById(R.id.dateText);
            timeText = view.findViewById(R.id.timeText);
            amountText = view.findViewById(R.id.amountText);
            deleteButton = view.findViewById(R.id.deleteButton);
            swipeIndicator = view.findViewById(R.id.swipeIndicator);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryModel model = historyList.get(position);

        // Parse and format date/time
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.getDefault());
            Date date = inputFormat.parse(model.getDateTime());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

            holder.dateText.setText(dateFormat.format(date));
            holder.timeText.setText(timeFormat.format(date));
        } catch (ParseException e) {
            holder.dateText.setText(model.getDateTime());
            holder.timeText.setText("");
        }

        // Set amount with formatting
        holder.amountText.setText("₹" + String.format(Locale.getDefault(), "%,d", model.getAmount()));

        // Set click listeners
        holder.deleteButton.setOnClickListener(v -> {
            animateButtonClick(holder.deleteButton);
            deleteListener.onDelete(holder.getAdapterPosition());
        });

        // Card click animation
        holder.cardView.setOnClickListener(v -> animateCardClick(holder.cardView));

        // Animate item entrance
        animateItemEntrance(holder.cardView, position);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    private void animateItemEntrance(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationX(100f);

        view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(position * 50L) // Stagger animation
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateCardClick(MaterialCardView cardView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.95f, 1f);

        scaleX.setDuration(150);
        scaleY.setDuration(150);

        scaleX.start();
        scaleY.start();
    }

    private void animateButtonClick(MaterialButton button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.8f, 1f);

        scaleX.setDuration(100);
        scaleY.setDuration(100);

        scaleX.start();
        scaleY.start();
    }
}
