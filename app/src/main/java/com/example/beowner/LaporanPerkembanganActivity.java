package com.example.beowner; // PASTIKAN PACKAGE INI SESUAI

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth; // Tambahkan import ini
import com.google.firebase.auth.FirebaseUser; // Tambahkan import ini
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.beowner.adapter.OrderAdapter;
import com.example.beowner.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaporanPerkembanganActivity extends AppCompatActivity {

    private static final String TAG = "LaporanPerkembangan";

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth; // Tambahkan ini
    private String currentUserId; // Tambahkan ini

    // UI Elements
    private ImageView backArrow;
    private TextView tvSaldoKas, tvNilaiPesanan, tvJumlahPesanan, tvPesananBatal, tvPesananSelesai, tvLaporanNoResults;
    private RecyclerView recyclerViewLaporanPesanan;

    // Data
    private List<Order> laporanOrderList;
    private OrderAdapter laporanOrderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_perkembangan);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inisialisasi

        // Ambil UID pengguna saat ini
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk melihat laporan.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada user login
            return;
        }
        currentUserId = currentUser.getUid();


        // Inisialisasi UI
        backArrow = findViewById(R.id.backArrow);
        tvSaldoKas = findViewById(R.id.tvSaldoKas);
        tvNilaiPesanan = findViewById(R.id.tvNilaiPesanan);
        tvJumlahPesanan = findViewById(R.id.tvJumlahPesanan);
        tvPesananBatal = findViewById(R.id.tvPesananBatal);
        tvPesananSelesai = findViewById(R.id.tvPesananSelesai);
        tvLaporanNoResults = findViewById(R.id.tvLaporanNoResults);
        recyclerViewLaporanPesanan = findViewById(R.id.recyclerViewLaporanPesanan);

        // Tombol Kembali
        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Setup RecyclerView
        laporanOrderList = new ArrayList<>();
        recyclerViewLaporanPesanan.setLayoutManager(new LinearLayoutManager(this));
        laporanOrderAdapter = new OrderAdapter(laporanOrderList, OrderAdapter.VIEW_TYPE_LAPORAN_SUMMARY);
        recyclerViewLaporanPesanan.setAdapter(laporanOrderAdapter);

        // Ambil data laporan dari Firestore
        fetchLaporanDataFromFirestore();
    }

    private void fetchLaporanDataFromFirestore() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId is null, cannot fetch report data.");
            return;
        }

        mFirestore.collection("orders")
                .whereEqualTo("userId", currentUserId) // <<< TAMBAHKAN FILTER BERDASARKAN USER ID
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed to fetch laporan data.", e);
                            Toast.makeText(LaporanPerkembanganActivity.this, "Gagal memuat laporan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            tvLaporanNoResults.setVisibility(View.VISIBLE);
                            tvLaporanNoResults.setText(R.string.error_loading_results);
                            return;
                        }

                        laporanOrderList.clear();
                        double totalNilaiPesanan = 0;
                        int jumlahTotalPesanan = 0;
                        int jumlahPesananBatal = 0;
                        int jumlahPesananSelesai = 0;

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                try {
                                    Order order = document.toObject(Order.class);
                                    order.setOrderId(document.getId());

                                    // Filter hanya pesanan yang statusnya 'Selesai' atau 'Dibatalkan' untuk ditampilkan di laporan
                                    if (order.getStatus() != null &&
                                            (order.getStatus().equalsIgnoreCase("Selesai") ||
                                                    order.getStatus().equalsIgnoreCase("Dibatalkan"))) {
                                        laporanOrderList.add(order);

                                        jumlahTotalPesanan++;
                                        if (order.getStatus().equalsIgnoreCase("Selesai")) {
                                            jumlahPesananSelesai++;
                                            totalNilaiPesanan += order.getTotalAmount();
                                        } else if (order.getStatus().equalsIgnoreCase("Dibatalkan")) {
                                            jumlahPesananBatal++;
                                        }
                                    }

                                } catch (Exception ex) {
                                    Log.e(TAG, "Error parsing laporan order document " + document.getId() + ": " + ex.getMessage());
                                }
                            }
                            Log.d(TAG, "Data Laporan berhasil dimuat. Jumlah item: " + laporanOrderList.size());
                        } else {
                            Log.d(TAG, "QuerySnapshot laporan kosong.");
                        }

                        laporanOrderAdapter.notifyDataSetChanged();

                        tvSaldoKas.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", totalNilaiPesanan));
                        tvNilaiPesanan.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", totalNilaiPesanan));
                        tvJumlahPesanan.setText(String.format(Locale.getDefault(), "%d Pesanan", jumlahTotalPesanan));
                        tvPesananBatal.setText(String.format(Locale.getDefault(), "%d Pesanan", jumlahPesananBatal));
                        tvPesananSelesai.setText(String.format(Locale.getDefault(), "%d Pesanan", jumlahPesananSelesai));

                        if (laporanOrderList.isEmpty()) {
                            tvLaporanNoResults.setVisibility(View.VISIBLE);
                        } else {
                            tvLaporanNoResults.setVisibility(View.GONE);
                        }
                    }
                });
    }
}