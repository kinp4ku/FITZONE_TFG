package lopez.sanchez.fitzonetfg;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RutinasFragment extends Fragment {

    private TextView routineNameTextView;
    private RecyclerView exerciseRecyclerView;
    private EjercicioAdapter ejercicioAdapter;
    private ArrayList<Ejercicio> ejercicioList;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas, container, false);

        routineNameTextView = view.findViewById(R.id.routineNameTextView);
        exerciseRecyclerView = view.findViewById(R.id.exerciseRecyclerView);

        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ejercicioList = new ArrayList<>();
        ejercicioAdapter = new EjercicioAdapter(requireContext(), ejercicioList);
        exerciseRecyclerView.setAdapter(ejercicioAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        obtenerRutinaDesdeFirestore();

        return view;
    }

    private void obtenerRutinaDesdeFirestore() {
        if (auth.getCurrentUser() == null) {
            progressDialog.dismiss();
            return;
        }

        String userEmail = auth.getCurrentUser().getEmail();

        db.collection("infoUsuarios")
                .whereEqualTo("EMAIL", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String nombreRutina = documentSnapshot.getString("RUTINA");
                            if (nombreRutina != null && !nombreRutina.isEmpty()) {
                                routineNameTextView.setText(nombreRutina);
                                obtenerNombreEjerciciosDesdeRutinas(nombreRutina);
                            } else {
                                routineNameTextView.setText("NO TIENE RUTINA ASIGNADA, CONTACTA CON UN TÉCNICO");
                                progressDialog.dismiss();
                            }
                            break;
                        }
                    } else {
                        routineNameTextView.setText("NO TIENE RUTINA ASIGNADA, CONTACTA CON UN TÉCNICO");
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                });
    }

    private void obtenerNombreEjerciciosDesdeRutinas(String nombreRutina) {
        db.collection("Rutinas").document(nombreRutina)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> nombresEjercicios = (List<String>) documentSnapshot.get("nombreEjercicios");
                        if (nombresEjercicios != null && !nombresEjercicios.isEmpty()) {
                            obtenerDetallesEjercicios(nombresEjercicios);
                        } else {
                            progressDialog.dismiss();
                        }
                    } else {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                });
    }
    private void obtenerDetallesEjercicios(List<String> nombresEjercicios) {
        ejercicioList.clear();

        AtomicInteger counter = new AtomicInteger(0);
        for (String nombreEjercicio : nombresEjercicios) {
            db.collection("Ejercicios")
                    .whereEqualTo("nombreEjercicio", nombreEjercicio)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                Ejercicio ejercicio = documentSnapshot.toObject(Ejercicio.class);
                                if (ejercicio != null) {
                                    ejercicioList.add(ejercicio);
                                }
                            } else {
                                Log.e("Firestore", "Documento no existe.");
                            }

                            if (counter.incrementAndGet() == nombresEjercicios.size()) {
                                ejercicioAdapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (counter.incrementAndGet() == nombresEjercicios.size()) {
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}