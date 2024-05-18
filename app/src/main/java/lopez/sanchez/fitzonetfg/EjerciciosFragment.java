package lopez.sanchez.fitzonetfg;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class EjerciciosFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Ejercicio> ejercicioArrayList;
    private EjercicioAdapter ejercicioAdapter;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

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
}
