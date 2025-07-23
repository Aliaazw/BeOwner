package com.example.beowner; // PASTIKAN PACKAGE INI SESUAI
// Atau package com.example.beowner.utils jika Anda membuat folder utils

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "beowner_notification_channel";
    private static final String CHANNEL_NAME = "Pengingat BeOwner";
    private static final String CHANNEL_DESCRIPTION = "Notifikasi pengingat pesanan dan deadline pengambilan";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Penting: HIGH agar notifikasi muncul lebih menonjol
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId, String orderId) {
        // Intent yang akan terbuka saat notifikasi diklik (misal: RincianPesananActivity)
        Intent intent = new Intent(context, RincianPesananActivity.class);
        intent.putExtra("order_id", orderId); // Kirim order ID agar bisa di-load di RincianPesananActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // Gunakan ID notifikasi sebagai request code agar unik
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE wajib untuk Android S (API 31) ke atas
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ganti dengan ikon notifikasi Anda
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Notifikasi akan hilang setelah diklik

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}