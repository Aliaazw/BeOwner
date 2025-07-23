package com.example.beowner.model; // PASTIKAN PACKAGE INI SESUAI

import java.io.Serializable; // Agar bisa dikirim antar Activity
import java.util.Date; // Untuk tanggal
import java.util.List; // Untuk daftar layanan
import java.util.Map; // Untuk detail layanan

public class Order implements Serializable {
    private String orderId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String selectedDuration;
    private double totalAmount;
    private String parfumOption;
    private String antarJemputOption;
    private String diskonOption;
    private String catatan;
    private Date orderDate; // Tanggal pesanan dibuat
    private String status; // Status pesanan (Antrian, Diproses, Siap Diambil, Selesai)
    private List<Map<String, Object>> services; // Detail layanan yang dipesan

    // Constructor kosong dibutuhkan oleh Firestore jika mengambil data sebagai objek
    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    // Constructor untuk membuat objek Order dari data
    public Order(String orderId, String customerName, String customerPhone, String customerAddress,
                 String selectedDuration, double totalAmount, String parfumOption,
                 String antarJemputOption, String diskonOption, String catatan,
                 Date orderDate, String status, List<Map<String, Object>> services) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.selectedDuration = selectedDuration;
        this.totalAmount = totalAmount;
        this.parfumOption = parfumOption;
        this.antarJemputOption = antarJemputOption;
        this.diskonOption = diskonOption;
        this.catatan = catatan;
        this.orderDate = orderDate;
        this.status = status;
        this.services = services;
    }

    // --- Getters ---
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public String getSelectedDuration() { return selectedDuration; }
    public double getTotalAmount() { return totalAmount; }
    public String getParfumOption() { return parfumOption; }
    public String getAntarJemputOption() { return antarJemputOption; }
    public String getDiskonOption() { return diskonOption; }
    public String getCatatan() { return catatan; }
    public Date getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public List<Map<String, Object>> getServices() { return services; }

    // --- Setters (Jika dibutuhkan untuk mengisi data setelah konstruksi) ---
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public void setSelectedDuration(String selectedDuration) { this.selectedDuration = selectedDuration; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setParfumOption(String parfumOption) { this.parfumOption = parfumOption; }
    public void setAntarJemputOption(String antarJemputOption) { this.antarJemputOption = antarJemputOption; }
    public void setDiskonOption(String diskonOption) { this.diskonOption = diskonOption; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setServices(List<Map<String, Object>> services) { this.services = services; }
}