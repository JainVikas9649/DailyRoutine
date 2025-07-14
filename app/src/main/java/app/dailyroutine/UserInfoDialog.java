package app.dailyroutine;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class UserInfoDialog extends AppCompatActivity {
    EditText nameInput, phoneInput, emailInput;
    Spinner genderSpinner;
    Button submitBtn;
    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;

    ViewPager2 viewPager;
    LinearLayout dotLayout;
    List<Integer> images;
    List<String> titles;
    SliderAdapter adapter;
    ImageView[] dots;

    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;
   // @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_user_info); // set content first
//        viewPager = findViewById(R.id.viewPager);
//        dotLayout = findViewById(R.id.dotLayout11);
//
//        images = Arrays.asList(
//                R.drawable.images111,
//                R.drawable.image222,
//                R.drawable.image333
//        );
//         titles = Arrays.asList(
//                "Escape the Ordinary,\nEmbrace the Journey!",
//                "Discover Hidden Gems\nAround the World",
//                "Adventure Awaits,\nStart Your Journey"
//        );
//
//        adapter = new SliderAdapter(this, images, titles);
//        viewPager.setAdapter(adapter);
//
//        setupIndicators(images.size());
//        selectDot(0);
//
//        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                selectDot(position);
//                resetAutoSlide(); // restart timer when user manually swipes
//            }
//        });
//
//        startAutoSlide();
//        CardView userInfoCard = findViewById(R.id.userInfoCard);
//        userInfoCard.setScaleX(0f);
//        userInfoCard.setScaleY(0f);
//        userInfoCard.setAlpha(0f);
//
//        userInfoCard.animate()
//                .scaleX(1f)
//                .scaleY(1f)
//                .alpha(1f)
//                .setDuration(600)
//                .setStartDelay(200)
//                .start();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
////        SignInButton googleSignInButton = findViewById(R.id.googleWrapper);
////        for (int i = 0; i < googleSignInButton.getChildCount(); i++) {
////            View v = googleSignInButton.getChildAt(i);
////            if (v instanceof TextView) {
////                ((TextView) v).setText("Sign in with Google");
////
////                break;
////            }
////        }
//        findViewById(R.id.customGoogleButton).setOnClickListener(v -> {
//
//            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
//                // Now launch again
//                googleSignIn();
//            });        });
//        // Slide-up animation
//        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
//        userInfoCard.startAnimation(slideUp);
//
//        // View references
//        nameInput = findViewById(R.id.editName);
//        phoneInput = findViewById(R.id.editPhone);
//        emailInput = findViewById(R.id.editEmail);
//        genderSpinner = findViewById(R.id.genderSpinner);
//        submitBtn = findViewById(R.id.btnSubmit);
//
//        // Get intent and check if update
//        Intent intent = getIntent();
//        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
//        if (isUpdate) {
//            // Pre-fill the data only in update mode
//            String name = intent.getStringExtra("name");
//            String email = intent.getStringExtra("email");
//            String phone = intent.getStringExtra("phone");
//            String gender = intent.getStringExtra("gender");
//
//            nameInput.setText(name);
//            emailInput.setText(email);
//            phoneInput.setText(phone);
//
//            emailInput.setText(email);
//            phoneInput.setText(phone);
//
//            // Assuming gender options are from R.array.gender_options
//            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                    R.array.gender_options, android.R.layout.simple_spinner_item);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            genderSpinner.setAdapter(adapter);
//
//            int genderIndex = adapter.getPosition(gender);
//            if (genderIndex >= 0) {
//                genderSpinner.setSelection(genderIndex);
//            }
//        }
//
//
//        // Gender spinner setup
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, new String[]{"Male", "Female"});
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        genderSpinner.setAdapter(adapter);
//
//        // Submit button logic
//        submitBtn.setOnClickListener(v -> {
//            String name = nameInput.getText().toString().trim();
//            String phone = phoneInput.getText().toString().trim();
//            String email = emailInput.getText().toString().trim();
//
//            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
//                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (!phone.matches("\\d{8,14}")) {
//                Toast.makeText(this, "Enter a valid phone number (8 to 14 digits)", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            SharedPreferences prefs1 = getSharedPreferences("user_info", MODE_PRIVATE);
//            SharedPreferences.Editor editor = prefs1.edit();
//            editor.putString("name", name);
//            editor.putString("phone", phone);
//            editor.putString("email", email);
//            editor.putString("gender", genderSpinner.getSelectedItem().toString());
//            editor.putBoolean("isUserInfoSaved", true);
//            editor.apply();
//            Toast.makeText(this, "Thank you for sharing your details", Toast.LENGTH_SHORT).show();
//            Intent intent1 = new Intent(UserInfoDialog.this, MainActivity.class);
//            startActivity(intent1);
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//            finish();
//        });
//    }

//    public void googleSignIn() {
//        Log.d("GoogleSignIn", "Starting Google Sign-In process");
//
//        // Getting the intent from GoogleSignInClient
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        if (signInIntent != null) {
//            Log.d("GoogleSignIn", "SignIn Intent created successfully");
//            launcher.launch(signInIntent);
//        } else {
//            Log.e("GoogleSignIn", "SignIn Intent is null");
//        }
//    }
//
//    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                Log.d("GoogleSignIn", "Activity result received");
//
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Log.d("GoogleSignIn", "Activity result OK");
//
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
//                    if (task.isSuccessful()) {
//                        Log.d("GoogleSignIn", "Task successful");
//
//                        GoogleSignInAccount account = task.getResult();
//                        if (account != null) {
//                            Log.d("GoogleSignIn", "Account retrieved: " + account.getEmail());
//
//                            String idToken = account.getIdToken();
//                            String name = account.getDisplayName();
//                            String email = account.getEmail();
//
//                            if (idToken != null) {
//                                Log.d("GoogleSignIn", "ID Token: " + idToken);
//                            } else {
//                                Log.e("GoogleSignIn", "ID Token is null");
//                            }
//
//                            Log.d("GoogleSignIn", "Name: " + name + ", Email: " + email);
//
//                            // Call your login API
//                          //  mLoginWithSocialApi(name, email);
//                            SharedPreferences prefs1 = getSharedPreferences("user_info", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = prefs1.edit();
//                            editor.putString("name", name);
//
//                            editor.putString("email", email);
//
//                            editor.putBoolean("isUserInfoSaved", true);
//                            editor.apply();
//                            Toast.makeText(this, "Thank you for sharing your details", Toast.LENGTH_SHORT).show();
//                            Intent intent1 = new Intent(UserInfoDialog.this, MainActivity.class);
//                            startActivity(intent1);
//                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                            finish();
//                        } else {
//                            Log.e("GoogleSignIn", "GoogleSignInAccount is null");
//                            Toast.makeText(this, "Login Failed. Please try again later.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Log.e("GoogleSignIn", "SignIn task failed", task.getException());
//                        Toast.makeText(this, "Login Failed. Please try again later.", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Log.e("GoogleSignIn", "Activity result not OK. Result code: " + result.getResultCode());
//                    Toast.makeText(this, "Login Failed. Please try again later.", Toast.LENGTH_SHORT).show();
//                }
//            }
//    );
//    private void setupIndicators(int count) {
//        dots = new ImageView[count];
//        dotLayout.removeAllViews();
//
//        for (int i = 0; i < count; i++) {
//            dots[i] = new ImageView(this);
//            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_unselected));
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.setMargins(8, 0, 8, 0);
//            dotLayout.addView(dots[i], params);
//        }
//    }
//
//    private void selectDot(int index) {
//        for (int i = 0; i < dots.length; i++) {
//            dots[i].setImageDrawable(ContextCompat.getDrawable(this,
//                    i == index ? R.drawable.indicator_selected : R.drawable.indicator_unselected));
//        }
//    }
//
//    private void startAutoSlide() {
//        sliderRunnable = new Runnable() {
//            @Override
//            public void run() {
//                int currentItem = viewPager.getCurrentItem();
//                int nextItem = currentItem + 1;
//
//                if (nextItem >= images.size()) {
//                    // Instantly jump to first item without animation, then animate forward next time
//                    viewPager.setCurrentItem(0, false);
//                    nextItem = 1;
//                    sliderHandler.postDelayed(this, 2000);
//                    return;
//                }
//
//                viewPager.setCurrentItem(nextItem, true);
//                sliderHandler.postDelayed(this, 2000);
//            }
//        };
//        sliderHandler.postDelayed(sliderRunnable, 2000);
//    }
//
//    private void resetAutoSlide() {
//        sliderHandler.removeCallbacks(sliderRunnable);
//        sliderHandler.postDelayed(sliderRunnable, 2000);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sliderHandler.removeCallbacks(sliderRunnable);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startAutoSlide();
//    }
//    private void logout() {
//        FirebaseAuth.getInstance().signOut();
//
//    //  Constants.set_currentTime(this, "");
//        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
//        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SESSION", 0);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.apply();
//        SharedPreferences highlights = getSharedPreferences("Home_Highlight_Course", 0);
//        SharedPreferences.Editor editor1 = highlights.edit();
//        editor1.clear();
//        editor1.apply();
//    }
}
