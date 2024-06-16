package lopez.sanchez.fitzonetfg;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservasClasesFragment extends Fragment {

    private static final String TAG = "ReservasClasesFragment";

    private FirebaseFirestore db;
    private List<Map<String, Object>> listaClases;
    private RecyclerView recyclerViewClasesReservas;

    public ReservasClasesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas_clases, container, false);

        recyclerViewClasesReservas = view.findViewById(R.id.recyclerViewClasesReservas);
        recyclerViewClasesReservas.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = FirebaseFirestore.getInstance();
        listaClases = new ArrayList<>();

        loadClases();

        return view;
    }

    private void mostrarClases() {
        ClaseReservaAdapter adapter = new ClaseReservaAdapter(requireContext(), listaClases);
        recyclerViewClasesReservas.setAdapter(adapter);
    }

    private void loadClases() {
        db.collection("Horarios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> clase = new HashMap<>();
                            clase.put("nombreClase", document.getId());
                            clase.put("url_img", document.getString("url_img"));
                            clase.put("horaInicio", document.getString("horaInicio"));
                            clase.put("horaFin", document.getString("horaFin"));
                            listaClases.add(clase);
                        }
                        mostrarClases();
                    } else {
                        Log.e(TAG, "Error al cargar las clases", task.getException());
                        Toast.makeText(requireContext(), "Error al cargar las clases: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}