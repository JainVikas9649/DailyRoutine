package app.dailyroutine;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ImageButton btnCurrentLocation,  btnBack;
     Button btnConfirm ;
    private LocationSearchAdapter searchAdapter;
    private List<LocationSearchResult> searchResults;

    private LatLng selectedLocation;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        initViews();
        setupMap();
        setupSearch();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
     //   btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        searchResults = new ArrayList<>();
        searchAdapter = new LocationSearchAdapter(searchResults, this::onLocationSelected);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchAdapter);

        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        btnConfirm.setOnClickListener(v -> confirmLocation());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchLocations(s.toString());
                } else {
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("LocationPicker", "onMapReady() called");
        mMap = googleMap;

        // Set default location (India)
        LatLng defaultLocation = new LatLng(20.5937, 78.9629);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5));

        // Enable map controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We have custom button

        // Set map click listener
        mMap.setOnMapClickListener(this::onMapClick);

        // Request location permission and get current location
        if (checkLocationPermission()) {
            enableMyLocation();
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void onMapClick(LatLng latLng) {
        selectedLocation = latLng;
        updateMapMarker(latLng);
        getAddressFromLatLng(latLng);
    }

    private void onLocationSelected(LocationSearchResult result) {
        selectedLocation = new LatLng(result.latitude, result.longitude);
        selectedAddress = result.address;

        updateMapMarker(selectedLocation);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));

        // Hide search results
        clearSearchResults();
        searchEditText.setText(result.name);
        searchEditText.clearFocus();
    }

    private void updateMapMarker(LatLng location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Selected Location"));
        }
    }

    private void searchLocations(String query) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 5);

                List<LocationSearchResult> results = new ArrayList<>();
                if (addresses != null) {
                    for (Address address : addresses) {
                        LocationSearchResult result = new LocationSearchResult();
                        result.name = address.getFeatureName() != null ? address.getFeatureName() : query;
                        result.address = address.getAddressLine(0);
                        result.latitude = address.getLatitude();
                        result.longitude = address.getLongitude();
                        results.add(result);
                    }
                }

                runOnUiThread(() -> {
                    searchResults.clear();
                    searchResults.addAll(results);
                    searchAdapter.notifyDataSetChanged();
                    searchResultsRecyclerView.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                });

            } catch (IOException e) {
                Log.e("LocationPicker", "Geocoding failed", e);
                runOnUiThread(() -> Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void clearSearchResults() {
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        searchResultsRecyclerView.setVisibility(View.GONE);
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            selectedLocation = currentLatLng;

                            updateMapMarker(currentLatLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                            getAddressFromLatLng(currentLatLng);
                            Toast.makeText(this, "Current location found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LocationPicker", "Failed to get location", e);
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e("LocationPicker", "Location permission not granted", e);
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    selectedAddress = address.getAddressLine(0);

                    runOnUiThread(() -> {
                        searchEditText.setText(selectedAddress);
                    });
                }
            } catch (IOException e) {
                Log.e("LocationPicker", "Reverse geocoding failed", e);
                runOnUiThread(() -> {
                    selectedAddress = latLng.latitude + ", " + latLng.longitude;
                    searchEditText.setText(selectedAddress);
                });
            }
        }).start();
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", selectedLocation.latitude);
            resultIntent.putExtra("lon", selectedLocation.longitude);
            resultIntent.putExtra("address", selectedAddress);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    private void enableMyLocation() {
        if (checkLocationPermission() && mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e("LocationPicker", "Location permission error", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required for this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Helper classes
    public static class LocationSearchResult {
        public String name;
        public String address;
        public double latitude;
        public double longitude;
    }
}