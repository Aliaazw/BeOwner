package com.example.beowner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Tambahkan import untuk ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beowner.R;
import com.example.beowner.model.ServiceItem;
import java.util.List;
import java.util.Locale; // Pastikan ini ada untuk formatting harga

public class HargaLayananAdapter extends RecyclerView.Adapter<HargaLayananAdapter.HargaLayananViewHolder> {

    private List<ServiceItem> serviceList;
    private OnServiceActionListener listener; // Mengubah nama listener

    // Interface baru untuk menangani aksi edit dan hapus
    public interface OnServiceActionListener {
        void onEditClick(ServiceItem serviceItem);
        void onDeleteClick(ServiceItem serviceItem);
    }

    // Constructor yang diupdate
    public HargaLayananAdapter(List<ServiceItem> serviceList, OnServiceActionListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HargaLayananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_harga_layanan, parent, false);
        return new HargaLayananViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HargaLayananViewHolder holder, int position) {
        ServiceItem serviceItem = serviceList.get(position);

        // Menampilkan nama layanan
        holder.tvServiceName.setText(serviceItem.getName());

        // Menampilkan harga dengan format mata uang (Rp)
        // Kita gabungkan unit di sini saja, contoh: "Rp 25.000 / pcs"
        String formattedPrice = String.format(Locale.getDefault(), "Rp %,.0f / %s", serviceItem.getPricePerUnit(), serviceItem.getUnit());
        holder.tvServicePrice.setText(formattedPrice);


        // Menangani klik tombol EDIT
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditClick(serviceItem); // <-- Pastikan ini memanggil onEditClick
                }
            }
        });

        // Menangani klik tombol DELETE
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(serviceItem); // <-- Pastikan ini memanggil onDeleteClick
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class HargaLayananViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;    // Nama layanan (dulu tvHargaServiceName)
        TextView tvServicePrice;   // Harga layanan (dulu etHargaService, sekarang TextView)
        ImageView ivEdit;          // Ikon Edit
        ImageView ivDelete;        // Ikon Delete

        public HargaLayananViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}