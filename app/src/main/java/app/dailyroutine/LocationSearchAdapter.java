package app.dailyroutine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationSearchAdapter extends RecyclerView.Adapter<LocationSearchAdapter.ViewHolder> {
    
    private List<LocationPickerActivity.LocationSearchResult> results;
    private OnLocationSelectedListener listener;
    
    public interface OnLocationSelectedListener {
        void onLocationSelected(LocationPickerActivity.LocationSearchResult result);
    }
    
    public LocationSearchAdapter(List<LocationPickerActivity.LocationSearchResult> results, 
                                OnLocationSelectedListener listener) {
        this.results = results;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_search, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationPickerActivity.LocationSearchResult result = results.get(position);
        holder.nameText.setText(result.name);
        holder.addressText.setText(result.address);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(result);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return results.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, addressText;
        
        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            addressText = itemView.findViewById(R.id.addressText);
        }
    }
}