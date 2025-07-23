package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beowner.adapter.ServiceSummaryAdapter;
import com.example.beowner.model.ServiceItem;
import com.google.firebase.auth.FirebaseAuth; // Tambahkan import ini
import com.google.firebase.auth.FirebaseUser; // Tambahkan import ini
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AturPesananActivity extends AppCompatActivity {

    private static final String TAG = "AturPesananActivity";

    // UI Elements
    private ImageView backArrow;
    private RecyclerView recyclerViewSelectedLayanan;
    private Spinner spinnerParfum, spinnerAntarJemput, spinnerDiskon;
    private EditText etCatatan;
    private TextView tvGrandTotalAmount;
    private Button btnBuatPesanan;

    // Data yang akan diterima dari TambahLayananActivity
    private String customerName, customerPhone, customerAddress;
    private String selectedDuration;
    private List<ServiceItem> selectedServicesList;
    private double totalAmount = 0.0;
    private Date orderDate;

    // Adapter untuk ringkasan layanan
    private ServiceSummaryAdapter serviceSummaryAdapter;

    // Objek Firebase Firestore dan Auth
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth; // Deklarasi FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_pesanan);

        // --- Inisialisasi Firebase Firestore dan Auth ---
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inisialisasi FirebaseAuth

        // --- Ambil Data yang Dikirim dari TambahLayananActivity ---
        Intent intent = getIntent();
        if (intent != null) {
            customerName = intent.getStringExtra("nama_pelanggan");
            customerPhone = intent.getStringExtra("no_handphone");
            customerAddress = intent.getStringExtra("alamat");
            selectedDuration = intent.getStringExtra("selected_duration");
            totalAmount = intent.getDoubleExtra("total_amount", 0.0);
            selectedServicesList = (List<ServiceItem>) intent.getSerializableExtra("selected_services_list");

            Log.d(TAG, "Data Diterima di AturPesanan: Nama=" + customerName + ", Durasi=" + selectedDuration + ", Total=" + totalAmount);
            if (selectedServicesList != null) {
                Log.d(TAG, "Jumlah Layanan Terpilih: " + selectedServicesList.size());
            }
        } else {
            selectedServicesList = new ArrayList<>();
        }

        // --- Inisialisasi Elemen UI ---
        backArrow = findViewById(R.id.backArrow);
        recyclerViewSelectedLayanan = findViewById(R.id.recyclerViewSelectedLayanan);
        spinnerParfum = findViewById(R.id.spinnerParfum);
        spinnerAntarJemput = findViewById(R.id.spinnerAntarJemput);
        spinnerDiskon = findViewById(R.id.spinnerDiskon);
        etCatatan = findViewById(R.id.etCatatan);
        tvGrandTotalAmount = findViewById(R.id.tvGrandTotalAmount);
        btnBuatPesanan = findViewById(R.id.btnBuatPesanan);

        // --- Click Listener untuk Tombol Panah Kembali ---
        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // --- Setup Spinner (Dropdown) ---
        setupSpinners();

        // --- Setup RecyclerView untuk Layanan Terpilih ---
        serviceSummaryAdapter = new ServiceSummaryAdapter(selectedServicesList);
        recyclerViewSelectedLayanan.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSelectedLayanan.setAdapter(serviceSummaryAdapter);

        // --- Tampilkan Total yang Diterima ---
        tvGrandTotalAmount.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", totalAmount));

        // --- Click Listener untuk Tombol "Buat Pesanan" ---
        btnBuatPesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dapatkan nilai dari Spinner dan EditText catatan
                String parfumOption = spinnerParfum.getSelectedItem().toString();
                String antarJemputOption = spinnerAntarJemput.getSelectedItem().toString();
                String diskonOption = spinnerDiskon.getSelectedItem().toString();
                String catatanText = etCatatan.getText().toString().trim();

                // Validasi akhir
                if (selectedServicesList == null || selectedServicesList.isEmpty()) {
                    Toast.makeText(AturPesananActivity.this, "Tidak ada layanan yang dipilih.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (totalAmount <= 0) {
                    Toast.makeText(AturPesananActivity.this, "Total pesanan harus lebih dari Rp 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                orderDate = new Date(); // Catat waktu pesanan dibuat

                // Dapatkan UID pengguna yang sedang login
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(AturPesananActivity.this, "Anda harus login untuk membuat pesanan.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userId = currentUser.getUid(); // UID pengguna yang login

                // --- LOGIKA PENYIMPANAN KE FIREBASE FIRESTORE ---
                Map<String, Object> order = new HashMap<>();
                order.put("customerName", customerName);
                order.put("customerPhone", customerPhone);
                order.put("customerAddress", customerAddress);
                order.put("selectedDuration", selectedDuration);
                order.put("totalAmount", totalAmount);
                order.put("parfumOption", parfumOption);
                order.put("antarJemputOption", antarJemputOption);
                order.put("diskonOption", diskonOption);
                order.put("catatan", catatanText);
                order.put("orderDate", orderDate);
                order.put("status", "Menunggu Diproses");
                order.put("userId", userId); // <<< TAMBAHKAN FIELD USER ID DI SINI

                List<Map<String, Object>> servicesDetails = new ArrayList<>();
                for (ServiceItem item : selectedServicesList) {
                    Map<String, Object> serviceDetail = new HashMap<>();
                    serviceDetail.put("id", item.getId());
                    serviceDetail.put("name", item.getName());
                    serviceDetail.put("pricePerUnit", item.getPricePerUnit());
                    serviceDetail.put("unit", item.getUnit());
                    serviceDetail.put("quantity", item.getQuantity());
                    serviceDetail.put("subtotal", item.getSubtotal());
                    servicesDetails.add(serviceDetail);
                }
                order.put("services", servicesDetails);

                Toast.makeText(AturPesananActivity.this, "Membuat pesanan...", Toast.LENGTH_SHORT).show();

                mFirestore.collection("orders")
                        .add(order)
                        .addOnSuccessListener(documentReference -> {
                            String orderId = documentReference.getId();
                            Log.d(TAG, "Pesanan berhasil disimpan dengan ID: " + orderId);
                            Toast.makeText(AturPesananActivity.this, "Pesanan Berhasil Dibuat!", Toast.LENGTH_LONG).show();

                            // Jadwalkan notifikasi
                            schedulePickupReminders(orderId, orderDate, selectedDuration, customerName);

                            Intent successIntent = new Intent(AturPesananActivity.this, PesananSelesaiActivity.class);
                            successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(successIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error saat menyimpan pesanan", e);
                            Toast.makeText(AturPesananActivity.this, "Gagal Membuat Pesanan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    // --- Metode Bantuan ---

    private void setupSpinners() {
        ArrayAdapter<CharSequence> parfumAdapter = ArrayAdapter.createFromResource(this,
                R.array.parfum_options, android.R.layout.simple_spinner_item);
        parfumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParfum.setAdapter(parfumAdapter);

        ArrayAdapter<CharSequence> antarJemputAdapter = ArrayAdapter.createFromResource(this,
                R.array.antar_jemput_options, android.R.layout.simple_spinner_item);
        antarJemputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAntarJemput.setAdapter(antarJemputAdapter);

        ArrayAdapter<CharSequence> diskonAdapter = ArrayAdapter.createFromResource(this,
                R.array.diskon_options, android.R.layout.simple_spinner_item);
        diskonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiskon.setAdapter(diskonAdapter);
    }

    private Date calculatePickupDeadline(Date orderDate, String duration) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(orderDate);

        int hoursToAdd = 0;
        if (duration.contains("72 Jam")) {
            hoursToAdd = 72;
        } else if (duration.contains("24 Jam")) {
            hoursToAdd = 24;
        } else if (duration.contains("6 Jam")) {
            hoursToAdd = 6;
        }

        if (hoursToAdd > 0) {
            cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
            return cal.getTime();
        }
        return null;
    }

    private void schedulePickupReminders(String orderId, Date orderDate, String selectedDuration, String customerName) {
        Date deadline = calculatePickupDeadline(orderDate, selectedDuration);
        if (deadline == null) {
            Log.e(TAG, "Deadline tidak dapat dihitung untuk pesanan: " + orderId);
            return;
        }

        long deadlineMillis = deadline.getTime();
        long nowMillis = System.currentTimeMillis();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long twoHourBefore = deadlineMillis - (2 * 60 * 60 * 1000);
        if (twoHourBefore > nowMillis) {
            scheduleSingleReminder(alarmManager, orderId, twoHourBefore, NotificationReceiver.EXTRA_NOTIFICATION_TYPE_2_HOUR, customerName + " (2 jam)", 1);
            Log.d(TAG, "Notifikasi 2 jam dijadwalkan untuk: " + orderId);
        }

        long thirtyMinBefore = deadlineMillis - (30 * 60 * 1000);
        if (thirtyMinBefore > nowMillis) {
            scheduleSingleReminder(alarmManager, orderId, thirtyMinBefore, NotificationReceiver.EXTRA_NOTIFICATION_TYPE_30_MIN, customerName + " (30 menit)", 2);
            Log.d(TAG, "Notifikasi 30 menit dijadwalkan untuk: " + orderId);
        }

        long fiveMinBefore = deadlineMillis - (5 * 60 * 1000);
        if (fiveMinBefore > nowMillis) {
            scheduleSingleReminder(alarmManager, orderId, fiveMinBefore, NotificationReceiver.EXTRA_NOTIFICATION_TYPE_5_MIN, customerName + " (5 menit)", 3);
            Log.d(TAG, "Notifikasi 5 menit dijadwalkan untuk: " + orderId);
        }

        Log.d(TAG, "Semua pengingat untuk pesanan " + orderId + " telah dijadwalkan.");
    }

    private void scheduleSingleReminder(AlarmManager alarmManager, String orderId, long triggerTimeMillis, String notificationType, String notificationMessage, int requestCodeSuffix) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_ORDER_ID, orderId);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TYPE, notificationType);
        int requestCode = (orderId.hashCode() % 100000) + requestCodeSuffix; // Modulo untuk memastikan requestCode tidak terlalu besar
        intent.putExtra("notification_id", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        }

        Log.d(TAG, "Alarm set for " + orderId + " (" + notificationType + ") at " + new Date(triggerTimeMillis) + " with requestCode: " + requestCode);
    }
}