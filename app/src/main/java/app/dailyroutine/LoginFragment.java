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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, googleSignInButton, biometricButton;
    private GoogleSignInClient googleSignInClient;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupGoogleSignIn();
        setupBiometric();
        setupClickListeners();
    }

    private void initViews(View view) {
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        biometricButton = view.findViewById(R.id.biometricButton);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void setupBiometric() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(requireContext());

            biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(requireContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    performBiometricLogin();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Login 👆")
                    .setSubtitle("Use your fingerprint to login securely")
                    .setNegativeButtonText("Cancel")
                    .build();
        } else {
            biometricButton.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        if (biometricButton.getVisibility() == View.VISIBLE) {
            biometricButton.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
        }
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        ((AuthActivity) requireActivity()).showLoading(true, "Signing in...");

        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            AuthActivity authActivity = (AuthActivity) requireActivity();

            // Check if credentials match registered user
            if (authActivity.validateLoginCredentials(email, password)) {
                // Get user data and login
                AuthActivity.UserData userData = authActivity.getUserDataByEmail(email);
                if (userData != null) {
                    Log.d(TAG, "Login successful for: " + userData.name);
                    authActivity.onAuthSuccess(userData.name, userData.email, userData.phone, userData.gender);
                } else {
                    authActivity.showLoading(false, "");
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Check demo credentials as fallback
                if (email.equals("demo@example.com") && password.equals("password123")) {
                    authActivity.onAuthSuccess("Demo User", email, "", "");
                } else {
                    authActivity.showLoading(false, "");
                    Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1500);
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void signInWithGoogle() {
        ((AuthActivity) requireActivity()).showLoading(true, "Connecting to Google...");

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String name = account.getDisplayName();
                        String email = account.getEmail();

                        Log.d(TAG, "Google Sign-In Success: " + name + ", " + email);
                        ((AuthActivity) requireActivity()).onAuthSuccess(name, email, "", "");

                    } catch (ApiException e) {
                        Log.e(TAG, "Google Sign-In Failed: " + e.getStatusCode());
                        ((AuthActivity) requireActivity()).showLoading(false, "");
                        Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ((AuthActivity) requireActivity()).showLoading(false, "");
                }
            }
    );

    private void performBiometricLogin() {
        // Load saved credentials from SharedPreferences
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("biometric_credentials", android.content.Context.MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "");
        String savedName = prefs.getString("name", "");

        if (!savedEmail.isEmpty()) {
            Log.d(TAG, "Biometric login successful for: " + savedName);
            ((AuthActivity) requireActivity()).onAuthSuccess(savedName, savedEmail, "", "");
        } else {
            Toast.makeText(requireContext(), "No saved credentials found. Please login normally first.", Toast.LENGTH_LONG).show();
        }
    }
}
