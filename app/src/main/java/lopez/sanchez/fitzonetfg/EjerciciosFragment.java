package lopez.sanchez.fitzonetfg;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Locale;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class EjerciciosFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Ejercicio> ejercicioArrayList;
    private EjercicioAdapter ejercicioAdapter;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private SearchView searchView;
    private Button scanButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ejercicios, container, false);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ejercicioArrayList = new ArrayList<>();
        ejercicioAdapter = new EjercicioAdapter(requireContext(), ejercicioArrayList);
        recyclerView.setAdapter(ejercicioAdapter);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener datos de Firestore
        obtenerEjerciciosDesdeFirestore();

        // Configurar SearchView
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarEjercicios(newText);
                return true;
            }
        });

        // Configurar botón de escanear
        scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(EjerciciosFragment.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR code");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setOrientationLocked(true); // Bloquear la orientación a vertical
            integrator.initiateScan();
        });


        return view;
    }

    private void obtenerEjerciciosDesdeFirestore() {
        db.collection("Ejercicios")
                .orderBy("nombreEjercicio", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore ERROR", "Error getting documents.", error);
                        progressDialog.dismiss();
                        return;
                    }

                    ejercicioArrayList.clear(); // Clear the list before adding new data
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Ejercicio ejercicio = dc.getDocument().toObject(Ejercicio.class);
                            ejercicioArrayList.add(ejercicio);
                        }
                    }

                    // Notificar al adaptador que se han agregado datos
                    ejercicioAdapter.notifyDataSetChanged();

                    // Ocultar el diálogo de progreso
                    progressDialog.dismiss();
                });
    }

    private void filtrarEjercicios(String texto) {
        ArrayList<Ejercicio> listaFiltrada = new ArrayList<>();
        for (Ejercicio ejercicio : ejercicioArrayList) {
            if (ejercicio.getNombreEjercicio().toLowerCase(Locale.ROOT).contains(texto.toLowerCase(Locale.ROOT))) {
                listaFiltrada.add(ejercicio);
            }
        }
        ejercicioAdapter.filtrarLista(listaFiltrada);
    }

    @Override

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Si se canceló el escaneo
                Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                // Se ha escaneado un código QR
                String qrContent = result.getContents();
                // Aquí puedes hacer lo que quieras con el contenido del código QR
                Toast.makeText(requireContext(), "Contenido del QR: " + qrContent, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
