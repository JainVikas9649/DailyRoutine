package app.dailyroutine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    double latitude1  ;
    double longitude1  ;
    private Context context;
    private List<ExpenseModel> originalList;   // Master list
    private List<ExpenseModel> displayList;    // Visible list
    private OnDeleteClickListener deleteClickListener;
    private OnItemClickListener itemClickListener;
    private OnPhotoClickListener photoClickListener;
    private OnLocationClickListener locationClickListener;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private String currentPhotoPath;
    public interface OnDeleteClickListener {
        void onDelete(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(ExpenseModel expense, int position);
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(ExpenseModel expense, String photoPath);
    }

    public interface OnLocationClickListener {
        void onLocationClick(ExpenseModel expense, double latitude, double longitude, String address);
    }

    public ExpenseAdapter(Context context, List<ExpenseModel> list, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.originalList = new ArrayList<>(list);
        this.displayList = new ArrayList<>(list);
        this.deleteClickListener = deleteClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.photoClickListener = listener;
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.locationClickListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseModel model = displayList.get(position);

        // Set basic data
        holder.dateText.setText(model.getDate());
        holder.amountText.setText(model.getFormattedAmount());
        holder.noteText.setText(model.getNote());
        holder.categoryText.setText(model.getCategoryDisplayName());

        // Set category icon and color
        holder.categoryIcon.setImageResource(model.getCategoryIcon());
        int categoryColor = ContextCompat.getColor(context, model.getCategoryColor());
        holder.categoryIconCard.setCardBackgroundColor(categoryColor);

        // Show/hide recurring indicator
        if (model.isRecurring()) {
            holder.recurringIcon.setVisibility(View.VISIBLE);
            holder.recurringText.setVisibility(View.VISIBLE);
            holder.recurringText.setText(model.getRecurrenceType());
        } else {
            holder.recurringIcon.setVisibility(View.GONE);
            holder.recurringText.setVisibility(View.GONE);
        }

        // Enhanced photo indicator with click functionality
        if (model.hasPhoto() && !model.getPhotoPath().isEmpty()) {
            holder.photoIcon.setVisibility(View.VISIBLE);
            holder.photoIcon.setOnClickListener(v -> handlePhotoClick(model));

            // Add visual feedback for photo availability
            holder.photoIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));

            // Optional: Show small preview in the icon (you can uncomment this if you want)
            // loadPhotoPreview(holder.photoIcon, model.getPhotoPath());
        } else {
            holder.photoIcon.setVisibility(View.GONE);
            holder.photoIcon.setOnClickListener(null);
        }

        // Enhanced location indicator with click functionality
        if (model.hasLocation() && !model.getLocationAddress().isEmpty()) {
            holder.locationIcon.setVisibility(View.VISIBLE);
            holder.locationIcon.setOnClickListener(v -> handleLocationClick(model));

            // Add visual feedback for location availability
            holder.locationIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning));
        } else {
            holder.locationIcon.setVisibility(View.GONE);
            holder.locationIcon.setOnClickListener(null);
        }

        // Animation
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_scale);
        holder.itemView.startAnimation(animation);

        // Click listeners
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDelete(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(model, holder.getAdapterPosition());
            } else {
                // Default behavior: show expense details
                showExpenseDetails(model);
            }
        });

        // Long click for additional options
        holder.itemView.setOnLongClickListener(v -> {
            showExpenseOptionsDialog(model, holder.getAdapterPosition());
            return true;
        });
    }

    private void handlePhotoClick(ExpenseModel expense) {
        if (photoClickListener != null) {
            photoClickListener.onPhotoClick(expense, expense.getPhotoPath());
        } else {
            // Default behavior: show photo in dialog
            showPhotoDialog(expense.getPhotoPath());
        }
    }

    private void handleLocationClick(ExpenseModel expense) {
        if (locationClickListener != null) {
            locationClickListener.onLocationClick(expense, expense.getLatitude(),
                    expense.getLongitude(), expense.getLocationAddress());
        } else {
            // Default behavior: show location options
            showLocationDialog(expense);
        }
    }
    private void showPhotoDialog(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            Toast.makeText(context, "Photo not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(photoPath);
        Log.e("ExpenseAdapter", "Photo path: " + photoPath);
        Log.e("ExpenseAdapter", "File exists: " + photoFile.exists());

        if (!photoFile.exists()) {
            Toast.makeText(context, "Photo file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri photoUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                photoFile
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photo_view, null);
        ImageView photoImageView = dialogView.findViewById(R.id.photoImageView);

        try {
            photoImageView.setImageURI(photoUri);
            context.grantUriPermission(context.getPackageName(), photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            Toast.makeText(context, "Error loading photo", Toast.LENGTH_SHORT).show();
            return;
        }

        builder.setView(dialogView);
        builder.setTitle("Expense Photo");
        builder.setPositiveButton("Close", null);
        builder.setNeutralButton("Share", (dialog, which) -> sharePhoto(photoPath));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLocationDialog(ExpenseModel expense) {
        String[] options = {"View Address", "Open in Maps", "Share Location"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Location Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showAddressDialog(expense.getLocationAddress());
                    break;
                case 1:
                    Log.d("map constaints", "openInMaps:1212 " + expense.getLatitude());
                    Log.d("map constaints", "openInMapsqwqw:1212 " + expense.getLongitude());
                    getLatLngFromAddress(expense.getLocationAddress());

                    openInMaps(latitude1, longitude1);
                    // openInMaps(expense.getLatitude(), expense.getLongitude());
                    break;
                case 2:
                    getLatLngFromAddress(expense.getLocationAddress());
                    shareLocation(latitude1, longitude1, expense.getLocationAddress());
                    break;
            }
        });
        builder.show();
    }

    private void showAddressDialog(String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Expense Location");
        builder.setMessage(address);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    private void getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                 latitude1 = addresses.get(0).getLatitude();
                 longitude1 = addresses.get(0).getLongitude();

                Log.d("map converted", "Latitude: " + latitude1);
                Log.d("map converted", "Longitude: " + longitude1);


            } else {
                Toast.makeText(context, "Unable to find location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Geocoder failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void openInMaps(double latitude, double longitude) {
        Log.d("map constaints", "openInMaps: " + latitude);
        Log.d("map constaints", "openInMapsqwqw: " + longitude);
        try {
            @SuppressLint("DefaultLocale") String uri = String.format("geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // Fallback to browser
                @SuppressLint("DefaultLocale") String url = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePhoto(String photoPath) {
        try {
            File photoFile = new File(photoPath);
            Uri photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context, context.getPackageName() + ".fileprovider", photoFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Expense photo");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share Photo"));
        } catch (Exception e) {
            Toast.makeText(context, "Unable to share photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLocation(double latitude, double longitude, String address) {
        try {
            String locationText = String.format("Expense Location: %s\nCoordinates: %f, %f",
                    address, latitude, longitude);
            String mapsUrl = String.format("https://maps.google.com/?q=%f,%f", latitude, longitude);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, locationText + "\n" + mapsUrl);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Expense Location");

            context.startActivity(Intent.createChooser(shareIntent, "Share Location"));
        } catch (Exception e) {
            Toast.makeText(context, "Unable to share location", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExpenseDetails(ExpenseModel expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        StringBuilder details = new StringBuilder();
        details.append("Amount: ").append(expense.getFormattedAmount()).append("\n");
        details.append("Category: ").append(expense.getCategoryDisplayName()).append("\n");
        details.append("Note: ").append(expense.getNote()).append("\n");
        details.append("Date: ").append(expense.getDate()).append("\n");

        if (expense.isRecurring()) {
            details.append("Recurring: ").append(expense.getRecurrenceType()).append("\n");
        }

        if (expense.hasPhoto()) {
            details.append("📷 Photo attached\n");
        }

        if (expense.hasLocation()) {
            details.append("📍 Location: ").append(expense.getLocationAddress()).append("\n");
        }

        builder.setTitle("Expense Details");
        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", null);

        if (expense.hasPhoto()) {
            builder.setNeutralButton("View Photo", (dialog, which) -> showPhotoDialog(expense.getPhotoPath()));
        }

        if (expense.hasLocation()) {
            builder.setNegativeButton("View Location", (dialog, which) -> showLocationDialog(expense));
        }

        builder.show();
    }

    private void showExpenseOptionsDialog(ExpenseModel expense, int position) {
        List<String> options = new ArrayList<>();
        options.add("View Details");
        options.add("Delete");

        if (expense.hasPhoto()) {
            options.add("View Photo");
        }

        if (expense.hasLocation()) {
            options.add("View Location");
        }

        String[] optionsArray = options.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Expense Options");
        builder.setItems(optionsArray, (dialog, which) -> {
            String selectedOption = optionsArray[which];
            switch (selectedOption) {
                case "View Details":
                    showExpenseDetails(expense);
                    break;
                case "Delete":
                    if (deleteClickListener != null) {
                        deleteClickListener.onDelete(position);
                    }
                    break;
                case "View Photo":
                    showPhotoDialog(expense.getPhotoPath());
                    break;
                case "View Location":
                    showLocationDialog(expense);
                    break;
            }
        });
        builder.show();
    }

    // Optional: Load small photo preview into the icon
    private void loadPhotoPreview(ImageView imageView, String photoPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                // Create a small circular preview
                int size = 24; // dp
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
                imageView.setImageBitmap(scaledBitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            // Keep the default icon if preview fails
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // Enhanced filter methods
    public void filterByPhotoAvailability(boolean hasPhoto) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : originalList) {
            if (expense.hasPhoto() == hasPhoto) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    public void filterByLocationAvailability(boolean hasLocation) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : originalList) {
            if (expense.hasLocation() == hasLocation) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    public void filterByAttachments(boolean hasAttachments) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : originalList) {
            if ((expense.hasPhoto() || expense.hasLocation()) == hasAttachments) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    // Existing methods remain the same...
    public void updateList(List<ExpenseModel> filteredList) {
        displayList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    public void restoreFullList() {
        displayList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    public void addExpense(ExpenseModel model) {
        originalList.add(0, model); // Add to beginning
        displayList.add(0, model);
        notifyItemInserted(0);
    }

    public void addExpenseAt(int position, ExpenseModel model) {
        originalList.add(position, model);
        displayList.add(position, model);
        notifyItemInserted(position);
    }

    public void removeExpense(int position) {
        if (position >= 0 && position < displayList.size()) {
            ExpenseModel toRemove = displayList.get(position);
            originalList.remove(toRemove);
            displayList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateExpense(int position, ExpenseModel updatedModel) {
        if (position >= 0 && position < displayList.size()) {
            ExpenseModel oldModel = displayList.get(position);
            int originalIndex = originalList.indexOf(oldModel);

            if (originalIndex != -1) {
                originalList.set(originalIndex, updatedModel);
            }
            displayList.set(position, updatedModel);
            notifyItemChanged(position);
        }
    }

    public void clearAllExpenses() {
        originalList.clear();
        displayList.clear();
        notifyDataSetChanged();
    }

    public List<ExpenseModel> getAllExpenses() {
        return new ArrayList<>(originalList);
    }

    public List<ExpenseModel> getDisplayedExpenses() {
        return new ArrayList<>(displayList);
    }

    // Filter methods
    public void filterByCategory(String category) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : originalList) {
            if (expense.getCategory().equalsIgnoreCase(category)) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    public void filterByAmount(double minAmount, double maxAmount) {
        List<ExpenseModel> filtered = new ArrayList<>();
        for (ExpenseModel expense : originalList) {
            if (expense.getAmount() >= minAmount && expense.getAmount() <= maxAmount) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    public void searchExpenses(String query) {
        if (query == null || query.trim().isEmpty()) {
            restoreFullList();
            return;
        }

        List<ExpenseModel> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ExpenseModel expense : originalList) {
            if (expense.getNote().toLowerCase().contains(lowerQuery) ||
                    expense.getCategory().toLowerCase().contains(lowerQuery) ||
                    expense.getLocationAddress().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(expense.getAmount()).contains(lowerQuery)) {
                filtered.add(expense);
            }
        }
        updateList(filtered);
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView categoryIconCard;
        ImageView categoryIcon, recurringIcon, photoIcon, locationIcon;
        TextView dateText, amountText, noteText, categoryText, recurringText;
        ImageButton deleteButton;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIconCard = itemView.findViewById(R.id.categoryIconCard);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            dateText = itemView.findViewById(R.id.dateText);
            amountText = itemView.findViewById(R.id.amountText);
            noteText = itemView.findViewById(R.id.noteText);
            categoryText = itemView.findViewById(R.id.categoryText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            recurringIcon = itemView.findViewById(R.id.recurringIcon);
            recurringText = itemView.findViewById(R.id.recurringText);
            photoIcon = itemView.findViewById(R.id.photoIcon);
            locationIcon = itemView.findViewById(R.id.locationIcon);
        }
    }
}
