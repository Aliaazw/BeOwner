package com.example.beowner.model;

import java.io.Serializable;

public class ServiceItem implements Serializable {
    private String id;
    private String name;
    private double pricePerUnit;
    private String unit;
    private int quantity;

    // Constructor 1: id, name, pricePerUnit, unit (digunakan untuk membuat layanan baru)
    public ServiceItem(String id, String name, double pricePerUnit, String unit) {
        this.id = id;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.unit = unit;
        this.quantity = 0; // Default quantity
    }

    // Constructor 2: id, name, pricePerUnit, unit, quantity (digunakan untuk memuat data dari DB)
    public ServiceItem(String id, String name, double pricePerUnit, String unit, int quantity) {
        this.id = id;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.unit = unit;
        this.quantity = quantity;
    }

    // Constructor kosong (dibutuhkan oleh Firebase Firestore untuk deserialisasi)
    public ServiceItem() {
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPricePerUnit() { return pricePerUnit; }
    public String getUnit() { return unit; }
    public int getQuantity() { return quantity; }

    // --- Setters ---
    public void setId(String id) { this.id = id; } // Tambah setter untuk ID jika Firestore membutuhkan
    public void setName(String name) { this.name = name; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setQuantity(int quantity) { this.quantity = quantity; } // Ini sudah ada

    // Metode untuk menghitung subtotal
    public double getSubtotal() {
        return quantity * pricePerUnit;
    }
}