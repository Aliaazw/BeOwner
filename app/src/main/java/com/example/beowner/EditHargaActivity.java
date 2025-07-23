package com.example.beowner; // PASTIKAN PACKAGE NAME INI SESUAI

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener; // Tambahkan import ini
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task; // Tambahkan import ini
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth; // Tambahkan import ini
import com.google.firebase.auth.FirebaseUser; // Tambahkan import ini
import com.google.firebase.firestore.CollectionReference; // Tambahkan import ini
import com.google.firebase.firestore.DocumentReference; // Tambahkan import ini
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch; // Tambahkan import ini

import com.example.beowner.adapter.HargaLayananAdapter;
import com.example.beowner.model.ServiceItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class EditHargaActivity extends AppCompatActivity implements HargaLayananAdapter.OnServiceActionListener {

    private static final String TAG = "EditHargaActivity";

    private ImageView backArrow;
    private RecyclerView recyclerViewHargaLayanan;
    private FloatingActionButton fabAddService;

    private List<ServiceItem> hargaLayananList;
    private HargaLayananAdapter hargaLayananAdapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth; // Deklarasi FirebaseAuth
    private String currentUserId; // Untuk menyimpan UID pengguna saat ini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_harga);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inisialisasi FirebaseAuth

        // Dapatkan UID pengguna saat ini
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk mengelola harga layanan.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada user login
            return;
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current User ID: " + currentUserId);


        backArrow = findViewById(R.id.backArrow);
        recyclerViewHargaLayanan = findViewById(R.id.recyclerViewHargaLayanan);
        fabAddService = findViewById(R.id.fabAddService);

        if (backArrow != null) {
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        hargaLayananList = new ArrayList<>();
        hargaLayananAdapter = new HargaLayananAdapter(hargaLayananList, this);

        recyclerViewHargaLayanan.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHargaLayanan.setAdapter(hargaLayananAdapter);

        // Panggil metode untuk memeriksa dan memuat layanan
        checkAndLoadUserServices();

        // Listener untuk FAB
        if (fabAddService != null) {
            fabAddService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddServiceDialog();
                }
            });
        }
    }

    // Metode baru untuk memeriksa dan memuat layanan pengguna
    private void checkAndLoadUserServices() {
        // Dapatkan referensi ke koleksi layanan pengguna saat ini
        CollectionReference userServicesRef = mFirestore.collection("users").document(currentUserId).collection("services");

        userServicesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().isEmpty()) {
                        // Koleksi layanan pengguna kosong, salin dari default_services
                        Log.d(TAG, "User services collection is empty. Copying from default_services.");
                        copyDefaultServicesToUser();
                    } else {
                        // Koleksi layanan pengguna sudah ada, langsung muat
                        Log.d(TAG, "User services collection exists. Fetching user-specific services.");
                        fetchHargaLayananFromFirestore();
                    }
                } else {
                    Log.e(TAG, "Error checking user services collection: " + task.getException());
                    Toast.makeText(EditHargaActivity.this, "Gagal memuat data layanan.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Metode baru untuk menyalin layanan default ke pengguna
    private void copyDefaultServicesToUser() {
        mFirestore.collection("default_services")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.w(TAG, "default_services collection is empty. No services to copy.");
                            Toast.makeText(EditHargaActivity.this, "Tidak ada layanan default untuk disalin.", Toast.LENGTH_LONG).show();
                            fetchHargaLayananFromFirestore(); // Tetap muat (akan kosong)
                            return;
                        }

                        WriteBatch batch = mFirestore.batch();
                        CollectionReference userServicesRef = mFirestore.collection("users").document(currentUserId).collection("services");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Buat dokumen baru di subkoleksi pengguna dengan data yang sama
                            DocumentReference newUserServiceRef = userServicesRef.document(); // Auto-ID
                            batch.set(newUserServiceRef, document.getData());
                            Log.d(TAG, "Adding default service to batch: " + document.getString("name"));
                        }

                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Default services copied successfully to user: " + currentUserId);
                                        Toast.makeText(EditHargaActivity.this, "Layanan default berhasil disalin!", Toast.LENGTH_SHORT).show();
                                        fetchHargaLayananFromFirestore(); // Setelah disalin, muat layanan pengguna
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error copying default services: " + e.getMessage());
                                        Toast.makeText(EditHargaActivity.this, "Gagal menyalin layanan default: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        fetchHargaLayananFromFirestore(); // Tetap coba muat, mungkin sebagian tersalin
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching default services: " + e.getMessage());
                        Toast.makeText(EditHargaActivity.this, "Gagal mengambil layanan default: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        fetchHargaLayananFromFirestore(); // Tetap coba muat (akan kosong)
                    }
                });
    }


    // Metode ini sekarang akan memuat layanan dari subkoleksi pengguna
    private void fetchHargaLayananFromFirestore() {
        if (currentUserId == null) {
            Log.w(TAG, "currentUserId is null, cannot fetch services.");
            return;
        }

        mFirestore.collection("users").document(currentUserId).collection("services") // <<< UBAH PATH DI SINI
                .orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed to fetch user services.", e);
                            Toast.makeText(EditHargaActivity.this, "Gagal memuat harga layanan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            hargaLayananList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String id = document.getId();
                                String name = document.getString("name");
                                Double price = document.getDouble("pricePerUnit");
                                String unit = document.getString("unit");

                                if (name != null && price != null && unit != null) {
                                    ServiceItem service = new ServiceItem(id, name, price, unit);
                                    hargaLayananList.add(service);
                                } else {
                                    Log.w(TAG, "Dokumen layanan tidak lengkap: " + document.getId());
                                }
                            }
                            hargaLayananAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Harga Layanan pengguna berhasil dimuat. Jumlah: " + hargaLayananList.size());
                        } else {
                            Log.d(TAG, "QuerySnapshot layanan pengguna kosong.");
                        }
                    }
                });
    }

    private void showAddServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_service, null);

        EditText etServiceName = dialogView.findViewById(R.id.etServiceNameDialog);
        EditText etServicePrice = dialogView.findViewById(R.id.etServicePriceDialog);
        EditText etServiceUnit = dialogView.findViewById(R.id.etServiceUnitDialog);

        builder.setView(dialogView)
                .setTitle("Tambah Layanan Baru")
                .setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = etServiceName.getText().toString().trim();
                        String priceStr = etServicePrice.getText().toString().trim();
                        String unit = etServiceUnit.getText().toString().trim();

                        if (name.isEmpty() || priceStr.isEmpty() || unit.isEmpty()) {
                            Toast.makeText(EditHargaActivity.this, "Mohon lengkapi semua data layanan.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double price;
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(EditHargaActivity.this, "Harga harus berupa angka yang valid.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addServiceToFirestore(name, price, unit);
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Metode ini sekarang akan menambahkan layanan ke subkoleksi pengguna
    private void addServiceToFirestore(String name, double price, String unit) {
        if (currentUserId == null) {
            Toast.makeText(this, "User ID tidak tersedia. Gagal menambahkan layanan.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> serviceData = new HashMap<>();
        serviceData.put("name", name);
        serviceData.put("pricePerUnit", price);
        serviceData.put("unit", unit);

        mFirestore.collection("users").document(currentUserId).collection("services") // <<< UBAH PATH DI SINI
                .add(serviceData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(EditHargaActivity.this, "Layanan '" + name + "' berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Layanan baru ditambahkan ke user " + currentUserId + " dengan ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditHargaActivity.this, "Gagal menambahkan layanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error adding new service for user " + currentUserId, e);
                    }
                });
    }

    @Override
    public void onEditClick(ServiceItem serviceItem) {
        // Tampilkan dialog edit
        showEditServiceDialog(serviceItem);
    }

    private void showEditServiceDialog(ServiceItem serviceItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_service, null); // Bisa pakai layout yang sama

        EditText etServiceName = dialogView.findViewById(R.id.etServiceNameDialog);
        EditText etServicePrice = dialogView.findViewById(R.id.etServicePriceDialog);
        EditText etServiceUnit = dialogView.findViewById(R.id.etServiceUnitDialog);

        // Isi data yang sudah ada
        etServiceName.setText(serviceItem.getName());
        etServicePrice.setText(String.format(Locale.getDefault(), "%.0f", serviceItem.getPricePerUnit()));
        etServiceUnit.setText(serviceItem.getUnit());

        // Nama layanan tidak bisa diedit
        etServiceName.setEnabled(false);

        builder.setView(dialogView)
                .setTitle("Edit Layanan: " + serviceItem.getName())
                .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // String name = etServiceName.getText().toString().trim(); // Nama tidak diedit
                        String priceStr = etServicePrice.getText().toString().trim();
                        String unit = etServiceUnit.getText().toString().trim();

                        if (priceStr.isEmpty() || unit.isEmpty()) {
                            Toast.makeText(EditHargaActivity.this, "Harga dan unit tidak boleh kosong.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double price;
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(EditHargaActivity.this, "Harga harus berupa angka yang valid.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Buat ServiceItem baru dengan perubahan
                        ServiceItem updatedService = new ServiceItem(serviceItem.getId(), serviceItem.getName(), price, unit);
                        simpanPerubahanHargaToFirestore(updatedService);
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // Metode ini sekarang akan menghapus layanan dari subkoleksi pengguna
    @Override
    public void onDeleteClick(ServiceItem serviceItem) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Layanan")
                .setMessage("Anda yakin ingin menghapus layanan '" + serviceItem.getName() + "'?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteServiceFromFirestore(serviceItem);
                    }
                })
                .setNegativeButton("Tidak", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteServiceFromFirestore(ServiceItem serviceItem) {
        if (currentUserId == null) {
            Toast.makeText(this, "User ID tidak tersedia. Gagal menghapus layanan.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestore.collection("users").document(currentUserId).collection("services") // <<< UBAH PATH DI SINI
                .document(serviceItem.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditHargaActivity.this, serviceItem.getName() + " berhasil dihapus.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Layanan " + serviceItem.getName() + " dihapus dari user " + currentUserId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditHargaActivity.this, "Gagal menghapus " + serviceItem.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting document " + serviceItem.getId() + " for user " + currentUserId, e);
                    }
                });
    }

    // Metode ini sekarang akan memperbarui layanan di subkoleksi pengguna
    private void simpanPerubahanHargaToFirestore(ServiceItem item) {
        if (currentUserId == null) {
            Toast.makeText(this, "User ID tidak tersedia. Gagal memperbarui layanan.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("pricePerUnit", item.getPricePerUnit());
        updates.put("name", item.getName()); // Pastikan nama juga diupdate jika bisa diedit
        updates.put("unit", item.getUnit()); // Pastikan unit juga diupdate jika bisa diedit


        mFirestore.collection("users").document(currentUserId).collection("services") // <<< UBAH PATH DI SINI
                .document(item.getId())
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Harga untuk " + item.getName() + " berhasil diperbarui untuk user " + currentUserId);
                        Toast.makeText(EditHargaActivity.this, "Harga " + item.getName() + " diperbarui!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Gagal memperbarui harga untuk " + item.getName() + " user " + currentUserId + ": " + e.getMessage());
                        Toast.makeText(EditHargaActivity.this, "Gagal update harga " + item.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}