package lopez.sanchez.fitzonetfg;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddRutinaCardFragment extends Fragment {

    private RutinasEntrenador fragment;
    private ArrayList<Ejercicio> ejercicioArrayList;
    private EjercicioAdapter ejercicioAdapter;
    private FirebaseFirestore db;
    private AutoCompleteTextView editTextNombreEjercicio;
    private EditText editTextNombreRutina;
    private ArrayList<String> allEjerciciosList;

    public AddRutinaCardFragment(RutinasEntrenador fragment) {
        this.fragment = fragment;
        this.ejercicioArrayList = new ArrayList<>();
        this.ejercicioAdapter = new EjercicioAdapter(fragment.requireContext(), ejercicioArrayList);
        this.db = FirebaseFirestore.getInstance();
        this.allEjerciciosList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_rutina, container, false);

        // Vaciar la lista de ejercicios al crear la vista
        ejercicioArrayList.clear();
        ejercicioAdapter.notifyDataSetChanged();

        Button buttonAñadirEjercicio = view.findViewById(R.id.buttonAñadirEjercicio);
        Button buttonCancelar = view.findViewById(R.id.buttonCancelar);
        Button buttonAñadirRutina = view.findViewById(R.id.buttonAñadirRutina);
        RecyclerView recyclerViewEjercicios = view.findViewById(R.id.recyclerViewEjercicios);
        editTextNombreEjercicio = view.findViewById(R.id.etAñadir);
        editTextNombreRutina = view.findViewById(R.id.editTextNombreRutina);

        recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
        recyclerViewEjercicios.setAdapter(ejercicioAdapter);
        cargarEjercicios();

        editTextNombreEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                AutoCompletar(s.toString());
            }
        });

        buttonAñadirEjercicio.setOnClickListener(v -> {
            String nombreEjercicio = editTextNombreEjercicio.getText().toString().trim();
            if (!nombreEjercicio.isEmpty()) {
                // Llamar al método para buscar el ejercicio en la base de datos y añadirlo a la lista
                buscarEjercicioEnBaseDeDatos(nombreEjercicio);
            } else {
                Toast.makeText(fragment.requireContext(), "Por favor, introduce el nombre del ejercicio", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancelar.setOnClickListener(v -> getParentFragmentManager().beginTransaction().remove(AddRutinaCardFragment.this).commit());

        buttonAñadirRutina.setOnClickListener(v -> {
            String nombreRutina = editTextNombreRutina.getText().toString().trim();
            if (!nombreRutina.isEmpty() && !ejercicioArrayList.isEmpty()) {
                // Guardar la rutina en Firestore
                guardarRutinaEnFirestore(nombreRutina, ejercicioArrayList);
                // Cerrar el fragmento
                getParentFragmentManager().beginTransaction().remove(AddRutinaCardFragment.this).commit();
            } else {
                Toast.makeText(fragment.requireContext(), "Por favor, introduce el nombre de la rutina y añade al menos un ejercicio", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void AutoCompletar(String query) {
        ArrayList<String> matchedEjercicios = new ArrayList<>();
        for (String ejercicio : allEjerciciosList) {
            if (ejercicio.toLowerCase().contains(query.toLowerCase())) {
                matchedEjercicios.add(ejercicio);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, matchedEjercicios);
        editTextNombreEjercicio.setAdapter(adapter);
        editTextNombreEjercicio.showDropDown();
    }

    private void cargarEjercicios() {
        db.collection("Ejercicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Ejercicio ejercicio = document.toObject(Ejercicio.class);
                        allEjerciciosList.add(ejercicio.getNombreEjercicio());
                    }
                });
    }

    private void buscarEjercicioEnBaseDeDatos(String nombreEjercicio) {
        db.collection("Ejercicios")
                .whereEqualTo("nombreEjercicio", nombreEjercicio)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Ejercicio ejercicio = document.toObject(Ejercicio.class);
                            ejercicioArrayList.add(ejercicio);
                        }
                        ejercicioAdapter.notifyDataSetChanged();
                        editTextNombreEjercicio.setText("");
                    } else {
                        Toast.makeText(fragment.requireContext(), "El ejercicio no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarRutinaEnFirestore(String nombreRutina, ArrayList<Ejercicio> ejercicioArrayList) {
        DocumentReference rutinaRef = db.collection("Rutinas").document(nombreRutina);
        ArrayList<String> nombresEjercicios = new ArrayList<>();
        for (Ejercicio ejercicio : ejercicioArrayList) {
            nombresEjercicios.add(ejercicio.getNombreEjercicio());
        }
        Map<String, Object> rutinaMap = new HashMap<>();
        rutinaMap.put("nombreEjercicios", nombresEjercicios);
        rutinaRef.set(rutinaMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(fragment.requireContext(), "Rutina guardada correctamente", Toast.LENGTH_SHORT).show();
                    fragment.onActivityResult(1, Activity.RESULT_OK, null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(fragment.requireContext(), "Error al guardar la rutina: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
