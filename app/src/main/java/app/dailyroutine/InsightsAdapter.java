package app.dailyroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.InsightViewHolder> {
    
    private final List<InsightModel> insights;

    public InsightsAdapter(List<InsightModel> insights) {
        this.insights = insights;
    }

    @NonNull
    @Override
    public InsightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insight, parent, false);
        return new InsightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsightViewHolder holder, int position) {
        InsightModel insight = insights.get(position);
        
        holder.titleText.setText(insight.getTitle());
        holder.descriptionText.setText(insight.getDescription());
        
        // Set icon and color based on type
        switch (insight.getType()) {
            case TREND:
                holder.iconView.setImageResource(R.drawable.ic_trending_up);
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.primary_light));
                break;
            case WARNING:
                holder.iconView.setImageResource(R.drawable.ic_warning);
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.warning_light));
                break;
            case TIP:
                holder.iconView.setImageResource(R.drawable.ic_lightbulb);
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.success_light));
                break;
            case SAVINGS:
                holder.iconView.setImageResource(R.drawable.ic_savings);
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.success_light));
                break;
            default:
                holder.iconView.setImageResource(R.drawable.ic_info);
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.background_secondary));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return insights.size();
    }

    static class InsightViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView iconView;
        TextView titleText;
        TextView descriptionText;

        public InsightViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            iconView = itemView.findViewById(R.id.insightIcon);
            titleText = itemView.findViewById(R.id.insightTitle);
            descriptionText = itemView.findViewById(R.id.insightDescription);
        }
    }
}
