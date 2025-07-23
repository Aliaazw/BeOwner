package com.example.beowner; // Pastikan ini sama dengan package Anda

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView; // Untuk ikon check
import android.widget.TextView; // Untuk teks konfirmasi
import android.widget.Toast; // Untuk Toast

import androidx.appcompat.app.AppCompatActivity;

public class PesananSelesaiActivity extends AppCompatActivity {

    private Button btnKembaliKeDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesanan_selesai);

        btnKembaliKeDashboard = findViewById(R.id.btnKembaliKeDashboard);

        if (btnKembaliKeDashboard != null) {
            btnKembaliKeDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kembali ke MainActivity dan bersihkan semua Activity di atasnya
                    Intent intent = new Intent(PesananSelesaiActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Tutup PesananSelesaiActivity
                }
            });
        }

        // Di sini nanti Cik bisa menambahkan logika untuk menyimpan pesanan ke Firebase Firestore
        // jika keputusan final disimpan dari layar ini (bukan dari AturPesananActivity)
    }
}