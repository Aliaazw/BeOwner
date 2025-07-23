package com.example.beowner; // PASTIKAN PACKAGE INI SESUAI
// Atau package com.example.beowner.receiver jika Anda membuat folder receiver

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.beowner.model.Order;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";

    // --- KONSTANTA BARU DITAMBAHKAN ---
    public static final String EXTRA_NOTIFICATION_TYPE_2_HOUR = "2_hour";
    public static final String EXTRA_NOTIFICATION_TYPE_30_MIN = "30_min";
    public static final String EXTRA_NOTIFICATION_TYPE_5_MIN = "5_min";
    // --- AKHIR KONSTANTA BARU ---
    @Override
    public void onReceive(Context context, Intent intent) {
        String orderId = intent.getStringExtra(EXTRA_ORDER_ID);
        String notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);
        int notificationId = intent.getIntExtra("notification_id", (int) System.currentTimeMillis()); // Ambil ID notifikasi

        Log.d(TAG, "NotificationReceiver triggered for Order ID: " + orderId + ", Type: " + notificationType);

        if (orderId == null) {
            Log.w(TAG, "Order ID is null, cannot process notification.");
            return;
        }

        // Inisialisasi Firestore untuk mengecek status pesanan (khusus notifikasi 5 menit)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            Log.d(TAG, "Order status for " + orderId + ": " + order.getStatus());

                            // Logic untuk notifikasi 5 menit terakhir: cek apakah sudah "Selesai"
                            if (notificationType != null && notificationType.equals("5_min") && order.getStatus() != null && order.getStatus().equalsIgnoreCase("Selesai")) {
                                Log.d(TAG, "Order " + orderId + " sudah Selesai, notifikasi 5 menit dibatalkan.");
                                // Tidak perlu menampilkan notifikasi karena sudah dikonfirmasi
                                return;
                            }

                            // Jika bukan notifikasi 5 menit terakhir, atau notifikasi 5 menit tapi belum Selesai, tampilkan
                            String title = "Pengambilan Cucian Segera!";
                            String message = "Pesanan " + order.getCustomerName() + " (" + order.getOrderId() + ") siap diambil dalam " + getReadableNotificationType(notificationType) + ".";

                            NotificationHelper.showNotification(context, title, message, notificationId, order.getOrderId());
                            Log.d(TAG, "Notifikasi ditampilkan untuk Order ID: " + orderId);

                        } else {
                            Log.w(TAG, "Order document " + orderId + " not found.");
                        }
                    } else {
                        Log.w(TAG, "DocumentSnapshot does not exist for order ID: " + orderId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching order document for notification: " + e.getMessage()));
    }

    private String getReadableNotificationType(String type) {
        if (type == null) return "waktu dekat";
        switch (type) {
            case "2_hour": return "2 jam";
            case "30_min": return "30 menit";
            case "5_min": return "5 menit";
            default: return "segera";
        }
    }
}