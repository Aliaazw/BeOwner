package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Untuk debugging
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast; // Untuk Toast

import androidx.appcompat.app.AppCompatActivity;

public class TambahPesananActivity extends AppCompatActivity {

    private static final String TAG = "TambahPesananActivity"; // TAG untuk Logcat

    private EditText etNamaPelanggan, etNoHandphone, etAlamat;
    private Button btnTambahkan;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pesanan);

        etNamaPelanggan = findViewById(R.id.etNamaPelanggan);
        etNoHandphone = findViewById(R.id.etNoHandphone);
        etAlamat = findViewById(R.id.etAlamat);
        btnTambahkan = findViewById(R.id.btnTambahkan);
        backArrow = findViewById(R.id.backArrow);

        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        btnTambahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namaPelanggan = etNamaPelanggan.getText().toString().trim();
                String noHandphone = etNoHandphone.getText().toString().trim();
                String alamat = etAlamat.getText().toString().trim();

                // Validasi input
                if (TextUtils.isEmpty(namaPelanggan)) {
                    etNamaPelanggan.setError("Nama Pelanggan wajib diisi");
                    etNamaPelanggan.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(noHandphone)) {
                    etNoHandphone.setError("No Handphone wajib diisi");
                    etNoHandphone.requestFocus();
                    return;
                }

                // --- TOAST DIAGNOSIS START: Cek nilai sebelum dikirim (INI AKAN MUNCUL DI LAYAR) ---
                Toast.makeText(TambahPesananActivity.this,
                        "Debug: Nama = '" + namaPelanggan + "', HP = '" + noHandphone + "'",
                        Toast.LENGTH_LONG).show();
                // --- TOAST DIAGNOSIS END ---

                // --- LOG DIAGNOSIS START: Cek nilai sebelum dikirim ---
                Log.d(TAG, "Nilai Nama Pelanggan sebelum dikirim: '" + namaPelanggan + "'");
                Log.d(TAG, "Nilai No Handphone sebelum dikirim: '" + noHandphone + "'");
                Log.d(TAG, "Nilai Alamat sebelum dikirim: '" + alamat + "'");
                // --- LOG DIAGNOSIS END ---

                Log.d(TAG, "Data Pelanggan Valid, Pindah ke PilihDurasiActivity");
                // Toast.makeText(TambahPesananActivity.this, "Data Pelanggan OK, Pilih Durasi", Toast.LENGTH_SHORT).show(); // Ini Toast yang asli

                Intent intent = new Intent(TambahPesananActivity.this, PilihDurasiActivity.class);
                intent.putExtra("nama_pelanggan", namaPelanggan);
                intent.putExtra("no_handphone", noHandphone);
                intent.putExtra("alamat", alamat);
                startActivity(intent);
            }
        });
    }
}