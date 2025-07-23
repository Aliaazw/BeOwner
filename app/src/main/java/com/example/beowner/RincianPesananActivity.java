package com.example.beowner; // PASTIKAN PACKAGE INI SESUAI

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Tambahkan import ini untuk getColorStateList

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.beowner.model.Order;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RincianPesananActivity extends AppCompatActivity {

    private static final String TAG = "RincianPesananActivity";

    // UI Elements
    private ImageView backArrow;
    private TextView tvStatus, tvTanggalMasuk, tvEstimasiSelesai, tvCatatan, tvParfum,
            tvAntarJemput, tvStatusPembayaran, tvTotalLayanan, tvTotalAntarJemput,
            tvTotalDiskon, tvTotalBayar;
    private Button btnPesananSiap, btnBatalkanPesanan;

    // Data Order
    private Order currentOrder;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_pesanan);

        mFirestore = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        backArrow = findViewById(R.id.backArrow);
        tvStatus = findViewById(R.id.tvStatus);
        tvTanggalMasuk = findViewById(R.id.tvTanggalMasuk);
        tvEstimasiSelesai = findViewById(R.id.tvEstimasiSelesai);
        tvCatatan = findViewById(R.id.tvCatatan);
        tvParfum = findViewById(R.id.tvParfum);
        tvAntarJemput = findViewById(R.id.tvAntarJemput);
        tvStatusPembayaran = findViewById(R.id.tvStatusPembayaran);
        tvTotalLayanan = findViewById(R.id.tvTotalLayanan);
        tvTotalAntarJemput = findViewById(R.id.tvTotalAntarJemput);
        tvTotalDiskon = findViewById(R.id.tvTotalDiskon);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        btnPesananSiap = findViewById(R.id.btnPesananSiap);
        btnBatalkanPesanan = findViewById(R.id.btnBatalkanPesanan);

        // Click listener untuk Tombol Kembali
        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Ambil objek Order dari Intent
        if (getIntent().hasExtra("order_detail")) {
            currentOrder = (Order) getIntent().getSerializableExtra("order_detail");
            if (currentOrder != null) {
                populateOrderDetails(currentOrder); // Isi data ke UI
            } else {
                Toast.makeText(this, "Gagal memuat detail pesanan.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Order object received as null.");
                finish();
            }
        } else {
            Toast.makeText(this, "Tidak ada data pesanan untuk ditampilkan.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No 'order_detail' extra found in Intent.");
            finish();
        }

        // --- Perbaikan Listener untuk tombol "Pesanan Telah Siap" / "Pesanan Selesai" ---
        btnPesananSiap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentOrder != null) {
                    String currentStatus = currentOrder.getStatus();
                    String nextStatus = "";

                    if (currentStatus != null) {
                        if (currentStatus.equalsIgnoreCase("Antrian") || currentStatus.equalsIgnoreCase("Menunggu Diproses")) {
                            nextStatus = "Siap Diambil";
                        } else if (currentStatus.equalsIgnoreCase("Siap Diambil")) {
                            nextStatus = "Selesai";
                        }
                    }

                    if (!nextStatus.isEmpty()) {
                        updateOrderStatus(nextStatus); // Panggil metode update dengan status berikutnya
                    } else {
                        Toast.makeText(RincianPesananActivity.this, "Status tidak dapat diubah.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // --- Akhir Perbaikan Listener ---

        // Setup Listener untuk tombol "Batalkan Pesanan"
        btnBatalkanPesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelConfirmationDialog();
            }
        });
    }

    // Metode untuk mengisi data pesanan ke UI
    private void populateOrderDetails(Order order) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

        tvStatus.setText(order.getStatus());
        tvTanggalMasuk.setText(dateFormat.format(order.getOrderDate()));

        Date estSelesaiDate = calculateEstimatedCompletionDate(order.getOrderDate(), order.getSelectedDuration());
        if (estSelesaiDate != null) {
            tvEstimasiSelesai.setText(dateFormat.format(estSelesaiDate));
        } else {
            tvEstimasiSelesai.setText("N/A");
        }

        tvCatatan.setText(order.getCatatan().isEmpty() ? "-" : order.getCatatan());
        tvParfum.setText(order.getParfumOption().isEmpty() ? "Tidak Ada" : order.getParfumOption());
        tvAntarJemput.setText(order.getAntarJemputOption());

        String paymentStatusText = "Belum Bayar";
        int paymentStatusColor = R.color.red_error;
        if (order.getStatus().equalsIgnoreCase("Selesai") || order.getStatus().equalsIgnoreCase("Siap Diambil")) {
            paymentStatusText = "Lunas";
            paymentStatusColor = R.color.green_success;
        }
        tvStatusPembayaran.setText(paymentStatusText);
        tvStatusPembayaran.setTextColor(ContextCompat.getColor(this, paymentStatusColor));


        tvTotalLayanan.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", order.getTotalAmount()));
        tvTotalAntarJemput.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", 0.0));
        tvTotalDiskon.setText("- Rp " + String.format(Locale.getDefault(), "%,.0f", 0.0));

        tvTotalBayar.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", order.getTotalAmount()));

        updateButtonVisibility(order.getStatus());
    }

    private Date calculateEstimatedCompletionDate(Date orderDate, String duration) {
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

    private void updateOrderStatus(String newStatus) {
        if (currentOrder == null || currentOrder.getOrderId() == null) {
            Toast.makeText(this, "Detail pesanan tidak valid untuk diperbarui.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestore.collection("orders").document(currentOrder.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RincianPesananActivity.this, "Status pesanan berhasil diperbarui menjadi: " + newStatus, Toast.LENGTH_SHORT).show();
                        currentOrder.setStatus(newStatus);
                        populateOrderDetails(currentOrder); // Refresh UI
                        // Jika ingin otomatis kembali ke daftar pesanan setelah Selesai/Dibatalkan:
                        // if (newStatus.equalsIgnoreCase("Selesai") || newStatus.equalsIgnoreCase("Dibatalkan")) {
                        //     finish();
                        // }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RincianPesananActivity.this, "Gagal memperbarui status pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating order status for " + currentOrder.getOrderId(), e);
                    }
                });
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Pesanan")
                .setMessage("Anda yakin ingin membatalkan pesanan ini?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateOrderStatus("Dibatalkan");
                    }
                })
                .setNegativeButton("Tidak", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateButtonVisibility(String status) {
        if (status == null) {
            btnPesananSiap.setVisibility(View.GONE);
            btnBatalkanPesanan.setVisibility(View.GONE);
            return;
        }

        if (status.equalsIgnoreCase("Antrian") || status.equalsIgnoreCase("Menunggu Diproses")) {
            btnPesananSiap.setVisibility(View.VISIBLE);
            btnBatalkanPesanan.setVisibility(View.VISIBLE);
            btnPesananSiap.setText(R.string.pesanan_telah_siap);
            btnPesananSiap.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.yellow_button));
            btnPesananSiap.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
        } else if (status.equalsIgnoreCase("Siap Diambil")) {
            btnPesananSiap.setVisibility(View.VISIBLE);
            btnPesananSiap.setText(R.string.pesanan_selesai_button_text); // String baru untuk "Pesanan Selesai"
            btnPesananSiap.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_success));
            btnPesananSiap.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnBatalkanPesanan.setVisibility(View.GONE);
        } else { // Selesai, Dibatalkan, atau status lain yang tidak bisa diubah
            btnPesananSiap.setVisibility(View.GONE);
            btnBatalkanPesanan.setVisibility(View.GONE);
        }
    }
}