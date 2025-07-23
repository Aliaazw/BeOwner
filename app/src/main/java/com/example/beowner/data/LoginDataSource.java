package com.example.beowner.data;

import android.util.Log; // Untuk Log
import com.example.beowner.data.model.LoggedInUser;
import com.google.android.gms.tasks.Task; // Untuk task Firebase
import com.google.firebase.auth.AuthResult; // Untuk hasil autentikasi
import com.google.firebase.auth.FirebaseAuth; // Untuk Firebase Auth
import com.google.firebase.auth.FirebaseUser; // Untuk objek user Firebase
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private static final String TAG = "LoginDataSource"; // Tag untuk Logcat

    // Inisialisasi Firebase Auth di sini agar bisa diakses
    private FirebaseAuth firebaseAuth;

    public LoginDataSource() {
        firebaseAuth = FirebaseAuth.getInstance(); // Mendapatkan instance Firebase Auth
    }

    public Result<LoggedInUser> login(String email, String password) {
        try {
            // Kita akan menggunakan TaskCompletionSource untuk mengubah Task Firebase
            // menjadi Result.Success atau Result.Error yang diharapkan oleh ViewModel
            final Task<AuthResult> authTask = firebaseAuth.signInWithEmailAndPassword(email, password);

            // Kita harus menunggu hasil dari task Firebase secara sinkron (blocking) di sini,
            // yang tidak ideal untuk UI thread. Namun, untuk integrasi sederhana dengan template ini,
            // kita akan menggunakan `Tasks.await()`. Dalam aplikasi nyata, ini harus dilakukan secara asinkron
            // atau menggunakan ViewModel untuk mengobservasi Task.
            AuthResult authResult = Tasks.await(authTask); // Menunggu hasil secara sinkron

            FirebaseUser user = authResult.getUser();

            if (user != null) {
                // Dapatkan display name dari user, jika null, gunakan email sebagai display name
                String displayName = user.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = user.getEmail() != null ? user.getEmail().split("@")[0] : "Pengguna"; // Ambil bagian email sebelum @
                }
                LoggedInUser loggedInUser =
                        new LoggedInUser(
                                user.getUid(), // Menggunakan UID dari Firebase
                                displayName); // Menggunakan display name asli dari Firebase atau email
                return new Result.Success<>(loggedInUser);
            } else {
                return new Result.Error(new IOException("Firebase login failed: No user returned"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error logging in with Firebase: " + e.getMessage(), e);
            // FirebaseAuthException akan memiliki pesan yang lebih spesifik
            return new Result.Error(new IOException("Error logging in: " + e.getMessage(), e));
        }
    }

    public void logout() {
        firebaseAuth.signOut(); // Melakukan proses logout dengan Firebase
        Log.d(TAG, "User logged out from Firebase.");
    }
}