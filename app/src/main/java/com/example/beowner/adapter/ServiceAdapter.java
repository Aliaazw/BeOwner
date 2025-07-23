package com.example.beowner.adapter; // PASTIKAN PACKAGE INI SESUAI

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout; // Untuk quantityControl
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beowner.R;
import com.example.beowner.model.ServiceItem; // Import model ServiceItem
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceItem> serviceList;
    private OnServiceQuantityChangeListener listener;

    // Interface untuk callback ketika jumlah layanan berubah
    public interface OnServiceQuantityChangeListener {
        void onQuantityChanged(ServiceItem serviceItem);
        void onAddServiceClicked(ServiceItem serviceItem); // Callback ketika tombol '+' pertama diklik
    }

    public ServiceAdapter(List<ServiceItem> serviceList, OnServiceQuantityChangeListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layanan, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem serviceItem = serviceList.get(position);
        holder.tvServiceName.setText(serviceItem.getName());
        holder.tvServicePrice.setText("Rp " + String.format("%,.0f", serviceItem.getPricePerUnit())); // Format harga
        holder.tvServiceQuantity.setText(String.valueOf(serviceItem.getQuantity()));

        // Tampilkan/sembunyikan tombol '+' atau quantityControl berdasarkan jumlah
        if (serviceItem.getQuantity() > 0) {
            holder.btnAddService.setVisibility(View.GONE);
            holder.quantityControl.setVisibility(View.VISIBLE);
        } else {
            holder.btnAddService.setVisibility(View.VISIBLE);
            holder.quantityControl.setVisibility(View.GONE);
        }

        // Click listener untuk tombol '+' pertama
        holder.btnAddService.setOnClickListener(v -> {
            serviceItem.setQuantity(1); // Set jumlah menjadi 1
            notifyItemChanged(position); // Perbarui tampilan item
            listener.onAddServiceClicked(serviceItem); // Beri tahu Activity
        });

        // Click listener untuk tombol kurang jumlah
        holder.btnDecreaseService.setOnClickListener(v -> {
            if (serviceItem.getQuantity() > 0) {
                serviceItem.setQuantity(serviceItem.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged(serviceItem); // Beri tahu Activity
            }
        });

        // Click listener untuk tombol tambah jumlah
        holder.btnIncreaseService.setOnClickListener(v -> {
            serviceItem.setQuantity(serviceItem.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged(serviceItem); // Beri tahu Activity
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    // --- ViewHolder Class ---
    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServicePrice;
        Button btnAddService;
        LinearLayout quantityControl;
        Button btnDecreaseService;
        TextView tvServiceQuantity;
        Button btnIncreaseService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            btnAddService = itemView.findViewById(R.id.btnAddService);
            quantityControl = itemView.findViewById(R.id.quantityControl);
            btnDecreaseService = itemView.findViewById(R.id.btnDecreaseService);
            tvServiceQuantity = itemView.findViewById(R.id.tvServiceQuantity);
            btnIncreaseService = itemView.findViewById(R.id.btnIncreaseService);
        }
    }
}