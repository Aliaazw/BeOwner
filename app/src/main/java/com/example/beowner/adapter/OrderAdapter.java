package com.example.beowner.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat; // Tambahkan import ini

import com.example.beowner.R;
import com.example.beowner.model.Order;
import com.example.beowner.RincianPesananActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar; // Tambahkan import ini untuk estimasi selesai

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private SimpleDateFormat dateFormatListItem = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormatLaporan = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Untuk Tanggal Masuk/Selesai Laporan
    private int displayMode;

    // Konstanta untuk berbagai mode tampilan
    public static final int VIEW_TYPE_DETAIL = 1; // Untuk CariPesananActivity (item_order_summary_detail)
    public static final int VIEW_TYPE_MAIN_SUMMARY = 2; // Untuk MainActivity (item_order_main_summary)
    public static final int VIEW_TYPE_LAPORAN_SUMMARY = 3; // Untuk LaporanPerkembanganActivity (item_laporan_order_summary)

    public OrderAdapter(List<Order> orderList, int displayMode) {
        this.orderList = orderList;
        this.displayMode = displayMode;
    }

    @Override
    public int getItemViewType(int position) {
        return displayMode;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_DETAIL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_summary_detail, parent, false);
        } else if (viewType == VIEW_TYPE_MAIN_SUMMARY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_main_summary, parent, false);
        } else { // VIEW_TYPE_LAPORAN_SUMMARY
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_order_summary, parent, false);
        }
        return new OrderViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (holder.viewType == VIEW_TYPE_DETAIL) {
            // Logika untuk layout item_order_summary_detail.xml (CariPesananActivity)
            holder.tvOrderCategory.setText(order.getSelectedDuration());
            holder.tvCustomerName.setText(order.getCustomerName());

            if (order.getOrderDate() != null) {
                holder.tvDateMasuk.setText(holder.itemView.getContext().getString(R.string.date_masuk_label) + dateFormatListItem.format(order.getOrderDate()));
            } else {
                holder.tvDateMasuk.setText(holder.itemView.getContext().getString(R.string.date_masuk_label) + "N/A");
            }
            holder.tvEstSelesai.setText(holder.itemView.getContext().getString(R.string.est_selesai_label) + (order.getStatus() != null ? order.getStatus() : "N/A")); // Placeholder Est Selesai
            holder.tvTotalPrice.setText("Rp " + String.format(Locale.getDefault(), "%,.0f", order.getTotalAmount()));

            holder.tvOrderStatusTag.setText(order.getStatus());
            int tagColorResId = getStatusColor(order.getStatus());
            holder.tvOrderStatusTag.setBackgroundResource(tagColorResId);

            String paymentStatusText = "Belum Bayar";
            int paymentStatusColor = R.color.red_error;
            if (order.getStatus() != null && (order.getStatus().equals("Selesai") || order.getStatus().equals("Siap Diambil"))) {
                paymentStatusText = "Lunas";
                paymentStatusColor = R.color.green_success;
            }
            holder.tvPaymentStatus.setText(paymentStatusText);
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), paymentStatusColor)); // Gunakan ContextCompat

        } else if (holder.viewType == VIEW_TYPE_MAIN_SUMMARY) {
            // Logika untuk layout item_order_main_summary.xml (MainActivity)
            holder.tvCustomerNameSimple.setText(order.getCustomerName());

            if (order.getOrderDate() != null) {
                holder.tvDateMasukSimple.setText(holder.itemView.getContext().getString(R.string.date_masuk_label) + dateFormatListItem.format(order.getOrderDate()));
            } else {
                holder.tvDateMasukSimple.setText(holder.itemView.getContext().getString(R.string.date_masuk_label) + "N/A");
            }

            holder.tvOrderStatusTagSimple.setText(order.getStatus());
            int tagColorResId = getStatusColor(order.getStatus());
            holder.tvOrderStatusTagSimple.setBackgroundResource(tagColorResId);

        } else { // holder.viewType == VIEW_TYPE_LAPORAN_SUMMARY
            // Logika untuk layout item_laporan_order_summary.xml (LaporanPerkembanganActivity)
            holder.tvCustomerNameLaporan.setText(order.getCustomerName());
            holder.tvTotalPriceLaporan.setText("total : Rp " + String.format(Locale.getDefault(), "%,.0f", order.getTotalAmount()));
            holder.tvStatusLaporan.setText(order.getStatus());

            if (order.getOrderDate() != null) {
                holder.tvDateMasukLaporan.setText("Masuk : " + dateFormatLaporan.format(order.getOrderDate()));
            } else {
                holder.tvDateMasukLaporan.setText("Masuk : N/A");
            }

            // Untuk "Selesai", kita perlu field tanggal selesai di model Order
            // Jika Anda memiliki field `completionDate` di model Order:
            // if (order.getCompletionDate() != null) {
            //     holder.tvDateSelesaiLaporan.setText("Selesai : " + dateFormatLaporan.format(order.getCompletionDate()));
            // } else {
            // Untuk sementara, kita asumsikan jika status Selesai, tanggal selesainya sama dengan orderDate jika tidak ada field khusus
            if (order.getStatus() != null && order.getStatus().equalsIgnoreCase("Selesai") && order.getOrderDate() != null) {
                holder.tvDateSelesaiLaporan.setText("Selesai : " + dateFormatLaporan.format(order.getOrderDate()));
            } else {
                holder.tvDateSelesaiLaporan.setText("Selesai : N/A");
            }
            // }
            // Atur warna background status tag untuk laporan
            int tagColorResId;
            if (order.getStatus() != null && order.getStatus().equalsIgnoreCase("Dibatalkan")) {
                tagColorResId = R.color.dark_gray; // Abu-abu untuk dibatalkan
            } else if (order.getStatus() != null && order.getStatus().equalsIgnoreCase("Selesai")) {
                tagColorResId = R.color.green_success; // Hijau untuk selesai
            } else {
                tagColorResId = R.color.light_gray; // Default
            }
            holder.tvStatusLaporan.setBackgroundResource(tagColorResId);
            holder.tvStatusLaporan.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)); // Text putih
        }

        // --- OnClickListener untuk seluruh item (berlaku untuk semua viewType) ---
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), RincianPesananActivity.class);
                intent.putExtra("order_detail", order); // Mengirim objek Order (harus Serializable)
                holder.itemView.getContext().startActivity(intent);
            }
        });
        // --- Akhir OnClickListener item ---
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Metode helper untuk mendapatkan warna status (untuk Main Summary dan Detail)
    private int getStatusColor(String status) {
        if (status == null) return R.color.light_gray;
        switch (status) {
            case "Antrian":
            case "Menunggu Diproses":
                return R.color.orange_status;
            case "Siap Diambil":
            case "Selesai":
                return R.color.green_success;
            case "Dibatalkan":
                return R.color.dark_gray;
            default:
                return R.color.light_gray;
        }
    }

    // --- ViewHolder Class ---
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        // Untuk item_order_summary_detail.xml
        TextView tvOrderCategory, tvOrderNota, tvCustomerName, tvDateMasuk, tvEstSelesai, tvTotalPrice, tvPaymentStatus, tvOrderStatusTag;
        ImageView basketIcon, dateIcon;

        // Untuk item_order_main_summary.xml
        TextView tvCustomerNameSimple, tvDateMasukSimple, tvOrderStatusTagSimple;

        // Untuk item_laporan_order_summary.xml
        TextView tvCustomerNameLaporan, tvDateMasukLaporan, tvDateSelesaiLaporan, tvTotalPriceLaporan, tvStatusLaporan;

        int viewType; // Menyimpan tipe view untuk ViewHolder ini

        public OrderViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if (viewType == VIEW_TYPE_DETAIL) {
                // Inisialisasi untuk item_order_summary_detail.xml
                tvOrderCategory = itemView.findViewById(R.id.tvOrderCategory);
                tvOrderNota = itemView.findViewById(R.id.tvOrderNota);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                basketIcon = itemView.findViewById(R.id.basketIcon);
                dateIcon = itemView.findViewById(R.id.dateIcon);
                tvDateMasuk = itemView.findViewById(R.id.tvDateMasuk);
                tvEstSelesai = itemView.findViewById(R.id.tvEstSelesai);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
                tvOrderStatusTag = itemView.findViewById(R.id.tvOrderStatusTag);
            } else if (viewType == VIEW_TYPE_MAIN_SUMMARY) {
                // Inisialisasi untuk item_order_main_summary.xml
                tvCustomerNameSimple = itemView.findViewById(R.id.tvCustomerNameSimple);
                tvDateMasukSimple = itemView.findViewById(R.id.tvDateMasukSimple);
                tvOrderStatusTagSimple = itemView.findViewById(R.id.tvOrderStatusTagSimple);
            } else { // VIEW_TYPE_LAPORAN_SUMMARY
                // Inisialisasi untuk item_laporan_order_summary.xml
                tvCustomerNameLaporan = itemView.findViewById(R.id.tvCustomerNameLaporan);
                tvDateMasukLaporan = itemView.findViewById(R.id.tvDateMasukLaporan);
                tvDateSelesaiLaporan = itemView.findViewById(R.id.tvDateSelesaiLaporan);
                tvTotalPriceLaporan = itemView.findViewById(R.id.tvTotalPriceLaporan);
                tvStatusLaporan = itemView.findViewById(R.id.tvStatusLaporan);
            }
        }
    }
}