package com.example.beowner.ui.login; // PASTIKAN PACKAGE NAME INI SESUAI

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beowner.MainActivity;
import com.example.beowner.R;
import com.example.beowner.SignUpActivity;
import com.example.beowner.databinding.ActivityLoginBinding;
import com.example.beowner.ui.login.LoginViewModel;
import com.example.beowner.ui.login.LoginViewModelFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    private FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- LOG DIAGNOSIS START: VERIFIKASI PELUNCURAN ACTIVITY ---
        Log.d(TAG, "onCreate() of LoginActivity is called.");
        Toast.makeText(this, "LoginActivity Sedang Dimuat!", Toast.LENGTH_SHORT).show();
        // --- LOG DIAGNOSIS END ---


        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.etUsername;
        final EditText passwordEditText = binding.etPassword;
        final Button loginButton = binding.btnLogin;
        final ProgressBar loadingProgressBar = binding.loading;

        final TextView tvSignUpPrompt = binding.tvSignUpPrompt;
        final TextView tvForgotPassword = binding.tvForgotPassword;


        // Log ini akan memberitahu kita apakah tvSignUpPrompt ditemukan oleh View Binding
        if (tvSignUpPrompt == null) {
            Log.e(TAG, "tvSignUpPrompt IS NULL! ID tidak ditemukan di XML atau binding error.");
            Toast.makeText(this, "ERROR: Link Sign Up tidak ditemukan!", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "tvSignUpPrompt DITEMUKAN. ID valid.");
        }


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User sudah login: " + currentUser.getEmail());
            Toast.makeText(LoginActivity.this, "Sudah login sebagai: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            navigateToDashboard();
        }


        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    navigateToDashboard();
                }
                setResult(Activity.RESULT_OK);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        if (tvSignUpPrompt != null) {
            tvSignUpPrompt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Sign Up prompt CLICKED!");
                    Toast.makeText(LoginActivity.this, "Sign Up diklik! Navigasi...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(LoginActivity.this, "Fitur Lupa Password belum diimplementasi", Toast.LENGTH_SHORT).show();
                }
            });
        }

    } // Akhir onCreate()

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}