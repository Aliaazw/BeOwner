package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth; // Tambahkan import ini
import com.google.firebase.auth.FirebaseUser; // Tambahkan import ini
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;

import com.example.beowner.adapter.OrderAdapter;
import com.example.beowner.model.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String currentUserId; // Tambahkan ini untuk menyimpan UID pengguna saat ini

    private RecyclerView ordersRecyclerView;
    private OrderAdapter ordersAdapter;
    private List<Order> todayOrdersList;

    private TextView tvKiloanValue, tvSatuanValue, tvMeteranValue;
    private TextView usernameTextView; // Untuk menampilkan username
    private ImageView userIcon; // Untuk user icon


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        usernameTextView = findViewById(R.id.usernameTextView); // Inisialisasi
        userIcon = findViewById(R.id.userIcon); // Inisialisasi

        // Ambil UID pengguna saat ini
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Jika tidak ada user login, langsung ke LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, com.example.beowner.ui.login.LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
            return; // Penting: keluar dari onCreate
        }
        currentUserId = currentUser.getUid();
        // Set username di header
        usernameTextView.setText(currentUser.getEmail()); // Atau currentUser.getDisplayName() jika ada


        // --- Inisialisasi Header dan Tombol Logout ---
        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        if (logoutIcon != null) {
            logoutIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, com.example.beowner.ui.login.LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(MainActivity.this, "Berhasil Logout!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Inisialisasi Tombol Aksi Cepat ---
        setupQuickActionButtons();

        // --- SETUP RECYCLERVIEW UNTUK PESANAN HARI INI ---
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        todayOrdersList = new ArrayList<>();

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrderAdapter(todayOrdersList, OrderAdapter.VIEW_TYPE_MAIN_SUMMARY);
        ordersRecyclerView.setAdapter(ordersAdapter);
        // --- AKHIR SETUP RECYCLERVIEW ---

        // --- INISIALISASI TEXTVIEW UNTUK NILAI RINGKASAN (Kiloan, Satuan, Meteran) ---
        tvKiloanValue = findViewById(R.id.tvKiloanValue);
        tvSatuanValue = findViewById(R.id.tvSatuanValue);
        tvMeteranValue = findViewById(R.id.tvMeteranValue);

        // --- Ambil Data Pesanan dari Firestore ---
        fetchOrdersFromFirestore();
    }

    private void setupQuickActionButtons() {
        View btnTambahPesananLayout = findViewById(R.id.btnTambahPesanan);
        ImageView iconTambahPesanan = btnTambahPesananLayout.findViewById(R.id.actionIcon);
        TextView textTambahPesanan = btnTambahPesananLayout.findViewById(R.id.actionText);
        iconTambahPesanan.setImageResource(R.drawable.icon_tambah_pesanan);
        textTambahPesanan.setText(R.string.tambah_pesanan);
        btnTambahPesananLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TambahPesananActivity.class);
            startActivity(intent);
        });

        View btnCariPesananLayout = findViewById(R.id.btnCariPesanan);
        ImageView iconCariPesanan = btnCariPesananLayout.findViewById(R.id.actionIcon);
        TextView textCariPesanan = btnCariPesananLayout.findViewById(R.id.actionText);
        iconCariPesanan.setImageResource(R.drawable.icon_cari);
        textCariPesanan.setText(R.string.cari_pesanan);
        if (btnCariPesananLayout != null) {
            btnCariPesananLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CariPesananActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Tombol Cari Pesanan diklik!", Toast.LENGTH_SHORT).show();
            });
        }

        View btnLaporanPerkembanganLayout = findViewById(R.id.btnLaporan);
        ImageView iconLaporanPerkembangan = btnLaporanPerkembanganLayout.findViewById(R.id.actionIcon);
        TextView textLaporanPerkembangan = btnLaporanPerkembanganLayout.findViewById(R.id.actionText);
        iconLaporanPerkembangan.setImageResource(R.drawable.laporan);
        textLaporanPerkembangan.setText(R.string.laporan_perkembangan);
        if (btnLaporanPerkembanganLayout != null) {
            btnLaporanPerkembanganLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LaporanPerkembanganActivity.class);
                startActivity(intent);
            });
        }

        View btnEditHargaLayout = findViewById(R.id.cardEditHarga);
        ImageView iconEditHarga = btnEditHargaLayout.findViewById(R.id.actionIcon);
        TextView textEditHarga = btnEditHargaLayout.findViewById(R.id.actionText);
        iconEditHarga.setImageResource(R.drawable.edit_harga);
        textEditHarga.setText(R.string.edit_harga);
        if (btnEditHargaLayout != null) {
            btnEditHargaLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EditHargaActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Tombol Edit Harga diklik!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void fetchOrdersFromFirestore() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId is null, cannot fetch orders.");
            return;
        }

        mFirestore.collection("orders")
                .whereEqualTo("userId", currentUserId) // <<< TAMBAHKAN FILTER BERDASARKAN USER ID
                .whereIn("status", Arrays.asList("Antrian", "Menunggu Diproses", "Siap Diambil"))
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            Toast.makeText(MainActivity.this, "Gagal mengambil data pesanan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            todayOrdersList.clear();
                            double totalKiloan = 0;
                            double totalSatuan = 0;
                            double totalMeteran = 0;

                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                try {
                                    Order order = document.toObject(Order.class);
                                    order.setOrderId(document.getId());

                                    todayOrdersList.add(order);

                                    // Hitung total kuantitas untuk ringkasan
                                    List<Map<String, Object>> services = order.getServices();
                                    if (services != null) {
                                        for (Map<String, Object> serviceMap : services) {
                                            String unit = (String) serviceMap.get("unit");
                                            Long quantityLong = (Long) serviceMap.get("quantity");
                                            int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                                            if (unit != null) {
                                                if (unit.equalsIgnoreCase("kg")) {
                                                    totalKiloan += quantity;
                                                } else if (unit.equalsIgnoreCase("pcs") || unit.equalsIgnoreCase("potong")) {
                                                    totalSatuan += quantity;
                                                } else if (unit.equalsIgnoreCase("m")) {
                                                    totalMeteran += quantity;
                                                }
                                            }
                                        }
                                    }

                                } catch (Exception ex) {
                                    Log.e(TAG, "Error parsing order document " + document.getId() + ": " + ex.getMessage());
                                }
                            }
                            ordersAdapter.notifyDataSetChanged();

                            tvKiloanValue.setText(String.format(Locale.getDefault(), "%,.0f kg", totalKiloan));
                            tvSatuanValue.setText(String.format(Locale.getDefault(), "%,.0f pcs", totalSatuan));
                            tvMeteranValue.setText(String.format(Locale.getDefault(), "%,.0f m", totalMeteran));

                            Log.d(TAG, "Data Pesanan Berhasil Diambil. Jumlah: " + todayOrdersList.size());
                            Log.d(TAG, "Ringkasan: Kiloan=" + totalKiloan + ", Satuan=" + totalSatuan + ", Meteran=" + totalMeteran);
                        } else {
                            Log.d(TAG, "QuerySnapshot is null.");
                        }
                    }
                });
    }
}