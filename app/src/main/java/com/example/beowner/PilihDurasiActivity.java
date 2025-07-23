package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PilihDurasiActivity extends AppCompatActivity {

    private static final String TAG = "PilihDurasiActivity";

    private CardView cardReguler, cardEkspres, cardKilat;
    private ImageView backArrow;
    private Button btnLanjut;

    private String selectedDuration = "";

    private String customerName;
    private String customerPhone;
    private String customerAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_durasi);

        // --- Ambil Data Pelanggan dari Intent ---
        Intent intentFromPrev = getIntent();
        if (intentFromPrev != null) {
            customerName = intentFromPrev.getStringExtra("nama_pelanggan");
            customerPhone = intentFromPrev.getStringExtra("no_handphone");
            customerAddress = intentFromPrev.getStringExtra("alamat");

            // --- LOG DIAGNOSIS START: Cek nilai yang diterima ---
            Log.d(TAG, "Data Pelanggan Diterima di PilihDurasi: Nama=" + customerName + ", HP=" + customerPhone + ", Alamat=" + customerAddress);
            Toast.makeText(PilihDurasiActivity.this,
                    "Debug PilihDurasi: Nama='" + customerName + "', HP='" + customerPhone + "'",
                    Toast.LENGTH_LONG).show();
            // --- LOG DIAGNOSIS END ---
        }

        // Inisialisasi elemen UI
        backArrow = findViewById(R.id.backArrow);
        cardReguler = findViewById(R.id.cardReguler);
        cardEkspres = findViewById(R.id.cardEkspres);
        cardKilat = findViewById(R.id.cardKilat);
        btnLanjut = findViewById(R.id.btnLanjut);

        // Click listener untuk tombol back
        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Click listeners untuk pilihan durasi
        cardReguler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDuration = "Reguler (72 Jam)";
                Toast.makeText(PilihDurasiActivity.this, "Durasi Reguler dipilih", Toast.LENGTH_SHORT).show();
                navigateToTambahLayanan();
            }
        });

        cardEkspres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDuration = "Ekspres (24 Jam)";
                Toast.makeText(PilihDurasiActivity.this, "Durasi Ekspres dipilih", Toast.LENGTH_SHORT).show();
                navigateToTambahLayanan();
            }
        });

        cardKilat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDuration = "Kilat (6 Jam)";
                Toast.makeText(PilihDurasiActivity.this, "Durasi Kilat dipilih", Toast.LENGTH_SHORT).show();
                navigateToTambahLayanan();
            }
        });

        // Click listener untuk Tombol Lanjut di bagian bawah
        if(btnLanjut != null) {
            btnLanjut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedDuration.isEmpty()) {
                        Toast.makeText(PilihDurasiActivity.this, "Mohon pilih durasi terlebih dahulu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PilihDurasiActivity.this, "Tombol Lanjut diklik. Durasi: " + selectedDuration, Toast.LENGTH_SHORT).show();
                        navigateToTambahLayanan();
                    }
                }
            });
        }
    }

    // Metode untuk berpindah ke layar Tambah Layanan
    private void navigateToTambahLayanan() {
        Intent intent = new Intent(PilihDurasiActivity.this, TambahLayananActivity.class);
        // --- TERUSKAN DATA PELANGGAN KE ACTIVITY SELANJUTNYA ---
        intent.putExtra("nama_pelanggan", customerName);
        intent.putExtra("no_handphone", customerPhone);
        intent.putExtra("alamat", customerAddress);
        // --- TERUSKAN JUGA DURASI YANG DIPILIH ---
        intent.putExtra("selected_duration", selectedDuration);
        startActivity(intent);
    }
}