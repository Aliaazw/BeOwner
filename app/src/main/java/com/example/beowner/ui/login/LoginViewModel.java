package com.example.beowner.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Handler; // <<< PASTIKAN IMPORT INI ADA
import android.os.Looper; // <<< PASTIKAN IMPORT INI ADA

import android.util.Patterns;

import com.example.beowner.data.LoginRepository;
import com.example.beowner.data.Result;
import com.example.beowner.data.model.LoggedInUser;
import com.example.beowner.R;

import java.util.concurrent.ExecutorService; // <<< PASTIKAN IMPORT INI ADA
import java.util.concurrent.Executors; // <<< PASTIKAN IMPORT INI ADA

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    // Tambahkan ExecutorService dan Handler untuk threading
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;


    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        // Inisialisasi thread pool dan handler
        this.executorService = Executors.newFixedThreadPool(2); // Contoh: 2 thread dalam pool
        this.mainThreadHandler = new Handler(Looper.getMainLooper()); // Handler untuk post ke main thread
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // Jalankan operasi login di background thread
        executorService.execute(() -> {
            // Panggil metode login dari repository (ini akan memblokir thread ini)
            Result<LoggedInUser> result = loginRepository.login(username, password);

            // Setelah hasilnya didapatkan, post kembali ke main thread untuk update LiveData
            mainThreadHandler.post(() -> {
                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed)); // Pesan error jika gagal
                }
            });
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Penting: Matikan thread pool ketika ViewModel tidak lagi digunakan
        executorService.shutdownNow();
    }
}