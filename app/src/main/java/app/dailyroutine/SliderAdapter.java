package app.dailyroutine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.ViewHolder> {

    Context context;
    List<Integer> imageList;
    List<String> titles;

    public SliderAdapter(Context context, List<Integer> imageList, List<String> titles) {
        this.context = context;
        this.imageList = imageList;
        this.titles = titles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.sliderImage.setImageResource(imageList.get(position));
        holder.sliderTitle.setText(titles.get(position));

        holder.sliderButton.setOnClickListener(v ->
                Toast.makeText(context, "Clicked: " + titles.get(position), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sliderImage;
        TextView sliderTitle;
        Button sliderButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sliderImage = itemView.findViewById(R.id.sliderImage);
            sliderTitle = itemView.findViewById(R.id.sliderTitle);
            sliderButton = itemView.findViewById(R.id.sliderButton);
        }
    }
}
