package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.example.beowner.adapter.ServiceAdapter;
import com.example.beowner.model.ServiceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TambahLayananActivity extends AppCompatActivity implements ServiceAdapter.OnServiceQuantityChangeListener {

    private static final String TAG = "TambahLayananActivity";

    // UI Elements
    private ImageView backArrow;
    private EditText etSearchLayanan;
    private RecyclerView recyclerViewLayanan;
    private TextView tvCustomerNameSummary;
    private TextView tvTotalLayanan;
    private Button btnLanjutLayanan;

    // Firebase
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth; // Tambahkan ini
    private String currentUserId; // Tambahkan ini

    private ServiceAdapter serviceAdapter;
    private List<ServiceItem> allServiceList;
    private List<ServiceItem> displayedServiceList;
    private List<ServiceItem> selectedServices;
    private double currentTotal = 0.0;

    private String customerName, customerPhone, customerAddress;
    private String selectedDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_layanan);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inisialisasi FirebaseAuth

        // Ambil UID pengguna saat ini
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk menambah layanan.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada user login
            return;
        }
        currentUserId = currentUser.getUid();


        Intent intentFromPrev = getIntent();
        if (intentFromPrev != null) {
            customerName = intentFromPrev.getStringExtra("nama_pelanggan");
            customerPhone = intentFromPrev.getStringExtra("no_handphone");
            customerAddress = intentFromPrev.getStringExtra("alamat");
            selectedDuration = intentFromPrev.getStringExtra("selected_duration");

            Log.d(TAG, "Data Diterima: Nama=" + customerName + ", HP=" + customerPhone + ", Durasi=" + selectedDuration);
        }

        backArrow = findViewById(R.id.backArrow);
        etSearchLayanan = findViewById(R.id.etSearchLayanan);
        recyclerViewLayanan = findViewById(R.id.recyclerViewLayanan);
        tvCustomerNameSummary = findViewById(R.id.tvCustomerNameSummary);
        tvTotalLayanan = findViewById(R.id.tvTotalLayanan);
        btnLanjutLayanan = findViewById(R.id.btnLanjutLayanan);

        if (customerName != null && !customerName.isEmpty()) {
            tvCustomerNameSummary.setText(customerName);
        } else {
            tvCustomerNameSummary.setText("Pelanggan Baru");
        }
        tvTotalLayanan.setText("Rp 0");

        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        allServiceList = new ArrayList<>();
        displayedServiceList = new ArrayList<>();
        selectedServices = new ArrayList<>();

        recyclerViewLayanan.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceAdapter(displayedServiceList, this);
        recyclerViewLayanan.setAdapter(serviceAdapter);

        etSearchLayanan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterServiceList(s.toString());
            }
        });

        btnLanjutLayanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTotal > 0 && !selectedServices.isEmpty()) {
                    Toast.makeText(TambahLayananActivity.this, "Total: Rp " + String.format(Locale.getDefault(), "%,.0f", currentTotal) + ". Lanjut ke Atur Pesanan.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(TambahLayananActivity.this, AturPesananActivity.class);
                    intent.putExtra("nama_pelanggan", customerName);
                    intent.putExtra("no_handphone", customerPhone);
                    intent.putExtra("alamat", customerAddress);
                    intent.putExtra("selected_duration", selectedDuration);
                    intent.putExtra("total_amount", currentTotal);
                    intent.putExtra("selected_services_list", (ArrayList<ServiceItem>) selectedServices);
                    startActivity(intent);
                } else {
                    Toast.makeText(TambahLayananActivity.this, "Pilih setidaknya satu layanan untuk melanjutkan.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fetchServiceDataFromFirestore(); // Panggil metode untuk mengambil data layanan dari Firestore
    }

    // --- Metode untuk Mengambil Data Layanan dari Firestore ---
    private void fetchServiceDataFromFirestore() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId is null, cannot fetch services.");
            return;
        }
        // >>> UBAH PATH COLLECTION DI SINI <<<
        mFirestore.collection("users").document(currentUserId).collection("services")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed to fetch user services.", e);
                            Toast.makeText(TambahLayananActivity.this, "Gagal memuat layanan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        allServiceList.clear();
                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                try {
                                    ServiceItem service = document.toObject(ServiceItem.class);
                                    service.setId(document.getId());
                                    allServiceList.add(service);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error parsing service document " + document.getId() + ": " + ex.getMessage());
                                }
                            }
                            Log.d(TAG, "Layanan berhasil dimuat dari Firestore. Jumlah: " + allServiceList.size());
                        } else {
                            Log.d(TAG, "QuerySnapshot layanan kosong.");
                        }

                        filterServiceList(etSearchLayanan.getText().toString());
                    }
                });
    }

    @Override
    public void onQuantityChanged(ServiceItem serviceItem) {
        updateSelectedServices(serviceItem);
        calculateAndDisplayTotal();
    }

    @Override
    public void onAddServiceClicked(ServiceItem serviceItem) {
        updateSelectedServices(serviceItem);
        calculateAndDisplayTotal();
    }

    private void updateSelectedServices(ServiceItem serviceItem) {
        boolean found = false;
        for (int i = 0; i < selectedServices.size(); i++) {
            if (selectedServices.get(i).getId().equals(serviceItem.getId())) {
                if (serviceItem.getQuantity() > 0) {
                    selectedServices.set(i, serviceItem);
                } else {
                    selectedServices.remove(i);
                }
                found = true;
                break;
            }
        }
        if (!found && serviceItem.getQuantity() > 0) {
            selectedServices.add(serviceItem);
        }
        Log.d(TAG, "Selected Services updated. Current count: " + selectedServices.size());
    }

    private void calculateAndDisplayTotal() {
        currentTotal = 0.0;
        for (ServiceItem item : selectedServices) {
            currentTotal += item.getSubtotal();
        }
        tvTotalLayanan.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", currentTotal));
        Log.d(TAG, "Current Total: " + currentTotal);
    }

    private void filterServiceList(String query) {
        displayedServiceList.clear();
        String lowercaseQuery = query.toLowerCase().trim();

        if (lowercaseQuery.isEmpty()) {
            displayedServiceList.addAll(allServiceList);
        } else {
            for (ServiceItem item : allServiceList) {
                if (item.getName() != null && item.getName().toLowerCase().contains(lowercaseQuery)) {
                    displayedServiceList.add(item);
                }
            }
        }
        serviceAdapter.notifyDataSetChanged();
    }
}