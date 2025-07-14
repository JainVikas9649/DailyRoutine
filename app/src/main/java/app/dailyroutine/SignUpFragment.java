package app.dailyroutine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment {

    private static final String TAG = "SignUpFragment";

    private TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText nameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    private Spinner genderSpinner;
    private MaterialCheckBox termsCheckbox;
    private MaterialButton signUpButton, googleSignUpButton;
    private GoogleSignInClient googleSignInClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupGoogleSignIn();
        setupGenderSpinner();
        setupClickListeners();
    }

    private void initViews(View view) {
        nameLayout = view.findViewById(R.id.nameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        phoneLayout = view.findViewById(R.id.phoneLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);

        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);

        genderSpinner = view.findViewById(R.id.genderSpinner);
        termsCheckbox = view.findViewById(R.id.termsCheckbox);
        signUpButton = view.findViewById(R.id.signUpButton);
        googleSignUpButton = view.findViewById(R.id.googleSignUpButton);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Select Gender", "Male", "Female", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> performSignUp());
        googleSignUpButton.setOnClickListener(v -> signUpWithGoogle());
    }

    private void performSignUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();

        if (!validateInput(name, email, phone, password, confirmPassword, gender)) {
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(requireContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        ((AuthActivity) requireActivity()).showLoading(true, "Creating account...");

        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            AuthActivity authActivity = (AuthActivity) requireActivity();

            // Save user credentials for future login
            authActivity.saveUserCredentials(email, password, name, phone, gender);

            Log.d(TAG, "Sign up successful for: " + name);

            // Complete the authentication process
            authActivity.onAuthSuccess(name, email, phone, gender);

            Toast.makeText(requireContext(), "Account created successfully! 🎉", Toast.LENGTH_SHORT).show();

        }, 2000);
    }

    private boolean validateInput(String name, String email, String phone, String password, String confirmPassword, String gender) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            nameLayout.setError("Name must be at least 2 characters");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            phoneLayout.setError("Phone number is required");
            isValid = false;
        } else if (!phone.matches("\\d{8,14}")) {
            phoneLayout.setError("Enter a valid phone number (8-14 digits)");
            isValid = false;
        } else {
            phoneLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 8) {
            passwordLayout.setError("Password must be at least 8 characters");
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            passwordLayout.setError("Password must contain uppercase, lowercase, number and special character");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Confirm password is required");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(requireContext(), "Please select your gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }



    private void signUpWithGoogle() {
        ((AuthActivity) requireActivity()).showLoading(true, "Connecting to Google...");

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignUpLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> googleSignUpLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String name = account.getDisplayName();
                        String email = account.getEmail();

                        Log.d(TAG, "Google Sign-Up Success: " + name + ", " + email);

                        // Save Google credentials for future login
                        ((AuthActivity) requireActivity()).saveUserCredentials(email, "google_auth", name, "", "");

                        ((AuthActivity) requireActivity()).onAuthSuccess(name, email, "", "");

                    } catch (ApiException e) {
                        Log.e(TAG, "Google Sign-Up Failed: " + e.getStatusCode());
                        ((AuthActivity) requireActivity()).showLoading(false, "");
                        Toast.makeText(requireContext(), "Google Sign-Up failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ((AuthActivity) requireActivity()).showLoading(false, "");
                }
            }
    );
}
