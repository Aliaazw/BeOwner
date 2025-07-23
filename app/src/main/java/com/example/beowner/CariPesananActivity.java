package com.example.beowner; // Pastikan ini sama dengan package Anda

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Tambahkan import ini
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth; // Tambahkan import ini
import com.google.firebase.auth.FirebaseUser; // Tambahkan import ini
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException; // Tambahkan import ini
import com.google.firebase.firestore.EventListener; // Tambahkan import ini

import com.example.beowner.adapter.OrderAdapter;
import com.example.beowner.model.Order;
import com.example.beowner.model.ServiceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Map;

public class CariPesananActivity extends AppCompatActivity {

    private static final String TAG = "CariPesananActivity";

    // UI Elements
    private TextView tvScreenTitle;
    private TextView tvCustomerFilter;
    private EditText etSearchOrderMain;
    private FloatingActionButton fabBackToMain;
    private RecyclerView recyclerViewAllOrders;
    private TextView tvNoResults;

    // Firebase
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth; // Tambahkan ini
    private String currentUserId; // Tambahkan ini

    // Data dan Adapter untuk RecyclerView
    private List<Order> allOrderList;
    private List<Order> displayedOrderList;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_pesanan);

        // Inisialisasi Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inisialisasi

        // Ambil UID pengguna saat ini
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk melihat pesanan.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada user login
            return;
        }
        currentUserId = currentUser.getUid();


        // Inisialisasi UI
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvCustomerFilter = findViewById(R.id.tvCustomerFilter);
        etSearchOrderMain = findViewById(R.id.etSearchOrderMain);
        fabBackToMain = findViewById(R.id.fabBackToMain);
        recyclerViewAllOrders = findViewById(R.id.recyclerViewAllOrders);
        tvNoResults = findViewById(R.id.tvNoResults);

        // Setup RecyclerView
        allOrderList = new ArrayList<>();
        displayedOrderList = new ArrayList<>(); // Inisialisasi list yang ditampilkan
        recyclerViewAllOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(displayedOrderList, OrderAdapter.VIEW_TYPE_MAIN_SUMMARY);
        // Adapter menggunakan displayedOrderList
        recyclerViewAllOrders.setAdapter(orderAdapter);

        // Click listener untuk Tombol Kembali (FAB)
        if (fabBackToMain != null) {
            fabBackToMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Listener untuk Search Bar
        etSearchOrderMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterOrders(s.toString());
            }
        });

        // Panggil metode untuk mengambil data riil dari Firestore
        fetchOrdersFromFirestore();
    }

    private void fetchOrdersFromFirestore() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId is null, cannot fetch orders.");
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
                            Log.w(TAG, "Listen failed to fetch orders.", e);
                            Toast.makeText(CariPesananActivity.this, "Gagal memuat pesanan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            tvNoResults.setVisibility(View.VISIBLE);
                            tvNoResults.setText(R.string.error_loading_results);
                            return;
                        }

                        allOrderList.clear();
                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                try {
                                    Order order = document.toObject(Order.class);
                                    order.setOrderId(document.getId());
                                    allOrderList.add(order);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error parsing order document " + document.getId() + ": " + ex.getMessage());
                                }
                            }
                            Log.d(TAG, "Pesanan berhasil dimuat dari Firestore. Jumlah: " + allOrderList.size());
                        } else {
                            Log.d(TAG, "QuerySnapshot layanan kosong.");
                        }

                        filterOrders(etSearchOrderMain.getText().toString());
                    }
                });
    }

    private void filterOrders(String query) {
        displayedOrderList.clear();

        String lowercaseQuery = query.toLowerCase().trim();

        if (lowercaseQuery.isEmpty()) {
            displayedOrderList.addAll(allOrderList);
        } else {
            for (Order order : allOrderList) {
                boolean matchesOrderId = order.getOrderId() != null && order.getOrderId().toLowerCase().contains(lowercaseQuery);
                boolean matchesCustomerName = order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(lowercaseQuery);
                boolean matchesCustomerPhone = order.getCustomerPhone() != null && order.getCustomerPhone().contains(lowercaseQuery);

                if (matchesOrderId || matchesCustomerName || matchesCustomerPhone) {
                    displayedOrderList.add(order);
                }
            }
        }

        orderAdapter.notifyDataSetChanged();

        if (displayedOrderList.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            if (lowercaseQuery.isEmpty()) {
                tvNoResults.setText(R.string.search_prompt_text);
            } else {
                tvNoResults.setText(R.string.no_results_found);
            }
        } else {
            tvNoResults.setVisibility(View.GONE);
        }
    }
}