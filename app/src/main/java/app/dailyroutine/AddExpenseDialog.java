package app.dailyroutine;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseDialog {

    // Request codes
    public static final int REQUEST_IMAGE_CAPTURE = 1001;
    public static final int REQUEST_IMAGE_GALLERY = 1002;
    public static final int REQUEST_LOCATION_PICKER = 2001;

    public interface OnAddListener {
        void onAdd(double amount, String note, String category, boolean isRecurring,
                   String recurrenceType, String date, boolean hasPhoto, boolean hasLocation,
                   String photoPath, String locationAddress, double latitude, double longitude);
    }

    public interface PermissionCallback {
        void onCameraPermissionResult(boolean granted);
        void onLocationPermissionResult(boolean granted);
        void onStoragePermissionResult(boolean granted);
    }

    private Context context;
    private OnAddListener listener;
    private PermissionCallback permissionCallback;
    private AlertDialog dialog;
    private FusedLocationProviderClient fusedLocationClient;

    // Views
    private EditText amountInput, dateInput;
    private AutoCompleteTextView categoryInput, recurrenceSpinner, noteInput;
    private Switch recurringSwitch;
    private LinearLayout recurrenceLayout;
    private Button btn50, btn100, btn500, btn1000;
    private Button btnAddPhoto, btnAddLocation;
    private ImageView photoPreview;
    private LinearLayout photoContainer, locationContainer;
    private TextView locationText;

    // Data
    private String selectedDate;
    private boolean hasPhoto = false;
    private boolean hasLocation = false;
    private String currentPhotoPath = "";
    private String currentLocationAddress = "";
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private String[] categories = {
            "Food & Drinks", "Transportation", "Shopping", "Entertainment",
            "Bills & Utilities", "Health & Fitness", "Education", "Other"
    };

    private String[] recurrenceOptions = {
            "Daily", "Weekly", "Monthly", "Yearly"
    };

    public AddExpenseDialog(Context context, OnAddListener listener) {
        this.context = context;
        this.listener = listener;
        this.selectedDate = getCurrentDate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void setPermissionCallback(PermissionCallback callback) {
        this.permissionCallback = callback;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_expense, null);
        initViews(view);
        setupViews();

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("Add Expense", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override positive button to prevent auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateAndSubmit()) {
                dialog.dismiss();
            }
        });
    }

    private void initViews(View view) {
        amountInput = view.findViewById(R.id.amountInput);
        categoryInput = view.findViewById(R.id.categoryInput);
        noteInput = view.findViewById(R.id.noteInput);
        dateInput = view.findViewById(R.id.dateInput);
        recurringSwitch = view.findViewById(R.id.recurringSwitch);
        recurrenceSpinner = view.findViewById(R.id.recurrenceSpinner);
        recurrenceLayout = view.findViewById(R.id.recurrenceLayout);

        btn50 = view.findViewById(R.id.btn50);
        btn100 = view.findViewById(R.id.btn100);
        btn500 = view.findViewById(R.id.btn500);
        btn1000 = view.findViewById(R.id.btn1000);

        btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        btnAddLocation = view.findViewById(R.id.btnAddLocation);

        photoPreview = view.findViewById(R.id.photoPreview);
        photoContainer = view.findViewById(R.id.photoContainer);
        locationContainer = view.findViewById(R.id.locationContainer);
        locationText = view.findViewById(R.id.locationText);
    }

    private void setupViews() {
        // Set current date
        dateInput.setText(selectedDate);

        // Setup category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, categories);
        categoryInput.setAdapter(categoryAdapter);
        categoryInput.setText(categories[0], false);
        categoryInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                categoryInput.showDropDown();
            }
        });

        // Setup recurrence dropdown
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, recurrenceOptions);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
        recurrenceSpinner.setText(recurrenceOptions[2], false); // Monthly as default

        // Setup quick amount buttons
        btn50.setOnClickListener(v -> amountInput.setText("50"));
        btn100.setOnClickListener(v -> amountInput.setText("100"));
        btn500.setOnClickListener(v -> amountInput.setText("500"));
        btn1000.setOnClickListener(v -> amountInput.setText("1000"));

        // Setup recurring switch
        recurringSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurrenceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            recurrenceSpinner.setAdapter(recurrenceAdapter);
            recurrenceSpinner.setText(recurrenceOptions[0], false);
        });

        recurrenceSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                recurrenceSpinner.showDropDown();
            }
        });

        // Setup date picker
        dateInput.setOnClickListener(v -> showDatePicker());

        // Setup photo and location buttons
        btnAddPhoto.setOnClickListener(v -> handlePhotoClick());
        btnAddLocation.setOnClickListener(v -> handleLocationClick());

        // Setup smart note suggestions
        setupNoteSuggestions();
    }

    private void handleLocationClick() {
        if (hasLocation) {
            // Show options to change or remove location
            String[] options = {"Pick from Map", "Get Current Location", "Remove Location"};
            new AlertDialog.Builder(context)
                    .setTitle("Location Options")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                openLocationPicker();
                                break;
                            case 1:
                                getCurrentLocationDirectly();
                                break;
                            case 2:
                                removeLocation();
                                break;
                        }
                    })
                    .show();
        } else {
            // Show options to add location
            String[] options = {"Pick from Map", "Get Current Location"};
            new AlertDialog.Builder(context)
                    .setTitle("Add Location")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            openLocationPicker();
                        } else {
                            getCurrentLocationDirectly();
                        }
                    })
                    .show();
        }
    }

    private void openLocationPicker() {
        Intent intent = new Intent(context, LocationPickerActivity.class);
        ((Activity) context).startActivityForResult(intent, REQUEST_LOCATION_PICKER);
    }

    private void getCurrentLocationDirectly() {
        if (!checkLocationPermission()) {
            if (permissionCallback != null) {
                permissionCallback.onLocationPermissionResult(false);
            }
            return;
        }

        // Show loading state
        btnAddLocation.setText("📍 Getting Location...");
        btnAddLocation.setEnabled(false);

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        btnAddLocation.setEnabled(true);

                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            getAddressFromCoordinates(currentLatitude, currentLongitude);
                        } else {
                            btnAddLocation.setText("📍 Location");
                            Toast.makeText(context, "Unable to get current location. Try again or pick from map.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        btnAddLocation.setEnabled(true);
                        btnAddLocation.setText("📍 Location");
                        Log.e("LocationPicker", "Failed to get location", e);
                        Toast.makeText(context, "Failed to get location. Try picking from map.", Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            btnAddLocation.setEnabled(true);
            btnAddLocation.setText("📍 Location");
            Log.e("LocationPicker", "Location permission not granted", e);
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    currentLocationAddress = address.getAddressLine(0);
                } else {
                    currentLocationAddress = latitude + ", " + longitude;
                }
            } catch (IOException e) {
                currentLocationAddress = latitude + ", " + longitude;
            }

            ((Activity) context).runOnUiThread(() -> {
                hasLocation = true;
                updateLocationButton();
                showLocationInfo();
                Toast.makeText(context, "Location added successfully! 📍", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    public void onLocationResult(double latitude, double longitude, String address) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        currentLocationAddress = address;
        hasLocation = true;
        updateLocationButton();
        showLocationInfo();
        Toast.makeText(context, "Location selected successfully! 📍", Toast.LENGTH_SHORT).show();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void handlePhotoClick() {
        if (hasPhoto) {
            // Show options to remove or change photo
            String[] options = {"Take New Photo", "Choose from Gallery", "Remove Photo"};
            new AlertDialog.Builder(context)
                    .setTitle("Photo Options")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                requestCameraPermission();
                                break;
                            case 1:
                                requestStoragePermission();
                                break;
                            case 2:
                                removePhoto();
                                break;
                        }
                    })
                    .show();
        } else {
            // Show options to add photo
            String[] options = {"Take Photo", "Choose from Gallery"};
            new AlertDialog.Builder(context)
                    .setTitle("Add Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            requestCameraPermission();
                        } else {
                            requestStoragePermission();
                        }
                    })
                    .show();
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            if (permissionCallback != null) {
                permissionCallback.onCameraPermissionResult(false);
            }
        }
    }

    private void requestStoragePermission() {
        if (permissionCallback != null) {
            permissionCallback.onStoragePermissionResult(false);
        }
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {

                // Create image file
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(context,
                            context.getPackageName() + ".fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    Log.d("PhotoCapture", "Starting camera intent with path: " + currentPhotoPath);
                    ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(context, "Error creating image file", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No camera app available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PhotoCapture", "Error opening camera", e);
            Toast.makeText(context, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d("PhotoCapture", "Starting gallery intent");
                ((Activity) context).startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
            } else {
                Toast.makeText(context, "No gallery app available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PhotoCapture", "Error opening gallery", e);
            Toast.makeText(context, "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "EXPENSE_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        Log.d("PhotoCapture", "Created image file: " + currentPhotoPath);
        return image;
    }

    public void onPhotoResult(String photoPath) {
        Log.d("PhotoCapture", "onPhotoResult called with path: " + photoPath);

        if (photoPath != null && !photoPath.isEmpty()) {
            File photoFile = new File(photoPath);
            if (photoFile.exists() && photoFile.length() > 0) {
                currentPhotoPath = photoPath;
                hasPhoto = true;
                updatePhotoButton();
                showPhotoPreview(photoPath);
                Log.d("PhotoCapture", "Photo preview should be visible now");
                Toast.makeText(context, "Photo added successfully! 📷", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("PhotoCapture", "Photo file doesn't exist or is empty: " + photoPath);
                Toast.makeText(context, "Photo file not found or corrupted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("PhotoCapture", "Photo path is null or empty");
            Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoPreview(String photoPath) {
        try {
            Log.d("PhotoCapture", "Attempting to show photo preview for: " + photoPath);

            File file = new File(photoPath);
            if (!file.exists()) {
                Log.e("PhotoCapture", "Photo file does not exist: " + photoPath);
                return;
            }

            // Decode bitmap with proper scaling
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath, options);

            // Calculate sample size
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            int reqHeight = 200;
            int reqWidth = 200;
            int inSampleSize = 1;

            if (imageHeight > reqHeight || imageWidth > reqWidth) {
                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

            if (bitmap != null) {
                Log.d("PhotoCapture", "Bitmap decoded successfully, setting to ImageView");
                photoPreview.setImageBitmap(bitmap);
                photoContainer.setVisibility(View.VISIBLE);
                Log.d("PhotoCapture", "Photo container visibility set to VISIBLE");
            } else {
                Log.e("PhotoCapture", "Failed to decode bitmap from: " + photoPath);
            }
        } catch (Exception e) {
            Log.e("PhotoCapture", "Error showing photo preview", e);
            Toast.makeText(context, "Error loading photo preview", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    private void showLocationInfo() {
        locationContainer.setVisibility(View.VISIBLE);
        if (locationText != null) {
            locationText.setText(currentLocationAddress);
        }
    }

    private void removePhoto() {
        hasPhoto = false;
        currentPhotoPath = "";
        updatePhotoButton();
        photoContainer.setVisibility(View.GONE);
        Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show();
    }

    private void removeLocation() {
        hasLocation = false;
        currentLocationAddress = "";
        currentLatitude = 0.0;
        currentLongitude = 0.0;
        updateLocationButton();
        locationContainer.setVisibility(View.GONE);
        Toast.makeText(context, "Location removed", Toast.LENGTH_SHORT).show();
    }

    private void updatePhotoButton() {
        if (hasPhoto) {
            btnAddPhoto.setText("✅ Photo Added");
            btnAddPhoto.setBackgroundResource(R.drawable.button_success);
        } else {
            btnAddPhoto.setText("📷 Photo");
            btnAddPhoto.setBackgroundResource(R.drawable.button_outline);
        }
    }

    private void updateLocationButton() {
        if (hasLocation) {
            btnAddLocation.setText("✅ Location Added");
            btnAddLocation.setBackgroundResource(R.drawable.button_success);
        } else {
            btnAddLocation.setText("📍 Location");
            btnAddLocation.setBackgroundResource(R.drawable.button_outline);
        }
    }

    private void setupNoteSuggestions() {
        String[] suggestions = {
                "Breakfast", "Lunch", "Dinner", "Coffee", "Snacks",
                "Groceries", "Fuel", "Taxi", "Bus", "Train",
                "Shopping", "Clothes", "Electronics", "Books",
                "Movie", "Games", "Music", "Sports",
                "Electricity", "Water", "Internet", "Phone",
                "Medicine", "Doctor", "Gym", "Vitamins",
                "Course", "Books", "Stationery", "Fees"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        noteInput.setAdapter(adapter);
        noteInput.setThreshold(1);

        noteInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                noteInput.showDropDown();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    dateInput.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean validateAndSubmit() {
        String amountText = amountInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();

        // Validate amount
        if (amountText.isEmpty()) {
            Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show();
            amountInput.requestFocus();
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(context, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                amountInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            amountInput.requestFocus();
            return false;
        }

        // Validate note
        if (note.isEmpty()) {
            note = category; // Use category as default note
        }

        // Get recurring data
        boolean isRecurring = recurringSwitch.isChecked();
        String recurrenceType = isRecurring ? recurrenceSpinner.getText().toString() : null;

        // Submit data
        if (listener != null) {
            listener.onAdd(amount, note, category, isRecurring, recurrenceType,
                    selectedDate, hasPhoto, hasLocation, currentPhotoPath,
                    currentLocationAddress, currentLatitude, currentLongitude);
        }

        return true;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Public methods for MainActivity to call
    public void triggerCameraIntent() {
        openCamera();
    }

    public void triggerGalleryIntent() {
        openGallery();
    }
}