package com.example.beowner.adapter; // PASTIKAN PACKAGE INI SESUAI

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beowner.R;
import com.example.beowner.model.ServiceItem; // Import model ServiceItem
import java.util.List;

public class ServiceSummaryAdapter extends RecyclerView.Adapter<ServiceSummaryAdapter.ServiceSummaryViewHolder> {

    private List<ServiceItem> serviceList;

    public ServiceSummaryAdapter(List<ServiceItem> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_layanan_summary.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layanan_summary, parent, false);
        return new ServiceSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceSummaryViewHolder holder, int position) {
        ServiceItem serviceItem = serviceList.get(position);
        // Menampilkan nama layanan dan jumlahnya
        holder.tvSummaryServiceName.setText(serviceItem.getName() + " (" + serviceItem.getQuantity() + " " + serviceItem.getUnit() + ")");
        // Menampilkan subtotal untuk layanan ini
        holder.tvSummaryServiceSubtotal.setText("Rp " + String.format("%,.0f", serviceItem.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    // --- ViewHolder Class ---
    public static class ServiceSummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSummaryServiceName;
        TextView tvSummaryServiceSubtotal;

        public ServiceSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSummaryServiceName = itemView.findViewById(R.id.tvSummaryServiceName);
            tvSummaryServiceSubtotal = itemView.findViewById(R.id.tvSummaryServiceSubtotal);
        }
    }
}