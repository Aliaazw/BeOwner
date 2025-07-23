package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.beowner.ui.login.LoginActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText etUsernameSignUp, etPasswordSignUp, etConfirmPasswordSignUp;
    private Button btnSignUp;
    private ProgressBar loadingSignUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etUsernameSignUp = findViewById(R.id.etUsernameSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etConfirmPasswordSignUp = findViewById(R.id.etConfirmPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        loadingSignUp = findViewById(R.id.loadingSignUp);

        // --- Inisialisasi tombol back (jika ada di layout) ---
        // Anda bisa menambahkan ImageView dengan id "backArrow" di activity_signup.xml
        // ImageView backArrow = findViewById(R.id.backArrow);
        // if (backArrow != null) {
        //     backArrow.setOnClickListener(new View.OnClickListener() {
        //         @Override
        //         public void onClick(View v) {
        //             finish(); // Kembali ke Activity sebelumnya (LoginActivity)
        //         }
        //     });
        // }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etUsernameSignUp.getText().toString().trim();
                String password = etPasswordSignUp.getText().toString().trim();
                String confirmPassword = etConfirmPasswordSignUp.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etUsernameSignUp.setError("Email tidak boleh kosong");
                    etUsernameSignUp.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    etPasswordSignUp.setError("Password tidak boleh kosong");
                    etPasswordSignUp.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    etConfirmPasswordSignUp.setError("Konfirmasi Password tidak boleh kosong");
                    etConfirmPasswordSignUp.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    etPasswordSignUp.setError("Password minimal 6 karakter");
                    etPasswordSignUp.requestFocus();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    etConfirmPasswordSignUp.setError("Password tidak cocok");
                    etConfirmPasswordSignUp.requestFocus();
                    return;
                }

                loadingSignUp.setVisibility(View.VISIBLE);
                registerUser(email, password);
            }
        });

    } // Akhir onCreate()

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingSignUp.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmailAndPassword:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show(); // Toast sukses daftar

                            // --- BARIS KODE BARU UNTUK LOGOUT OTOMATIS SETELAH DAFTAR ---
                            mAuth.signOut(); // Logout user yang baru saja didaftarkan secara otomatis
                            Log.d(TAG, "User " + user.getEmail() + " automatically logged out after signup.");
                            // --- AKHIR KODE BARU ---

                            // Arahkan kembali ke LoginActivity agar user login manual
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            // Flag untuk membersihkan tumpukan Activity di atas LoginActivity
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Tutup SignUpActivity
                        } else {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                            String errorMessage = "Pendaftaran Gagal: " + (task.getException() != null ? task.getException().getMessage() : "Terjadi kesalahan tidak dikenal.");

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Pendaftaran Gagal: Email ini sudah terdaftar.";
                            }

                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}