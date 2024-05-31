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
import java.util.List;
import java.util.Map;

public class AddRutinaCardFragment extends Fragment {

    private RutinasEntrenador fragment;
    private ArrayList<Ejercicio> ejercicioArrayList;
    private ArrayList<String> nombreEjerciciosArrayList;
    private EjercicioAdapter ejercicioAdapter;
    private FirebaseFirestore db;
    private List<Ejercicio> ejercicioList;
    private List<Boolean> selectedEjercicios;
    EditText editTextNombreRutina;
    Button buttonAñadirEjercicio, buttonCancelar, buttonAñadirRutina;
    RecyclerView recyclerViewEjercicios;
    AutoCompleteTextView editTextNombreEjercicio;
    public AddRutinaCardFragment(RutinasEntrenador fragment) {
        this.fragment = fragment;
        this.ejercicioArrayList = new ArrayList<>();
        this.ejercicioAdapter = new EjercicioAdapter(fragment.requireContext(), ejercicioArrayList);
        this.db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_rutina, container, false);

        editTextNombreRutina = view.findViewById(R.id.editTextNombreRutina);
        buttonAñadirEjercicio = view.findViewById(R.id.buttonAñadirEjercicio);
        buttonCancelar = view.findViewById(R.id.buttonCancelar);
        buttonAñadirRutina = view.findViewById(R.id.buttonAñadirRutina);
        recyclerViewEjercicios = view.findViewById(R.id.recyclerViewEjercicios);
        editTextNombreEjercicio = view.findViewById(R.id.etAñadir);


        recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
        recyclerViewEjercicios.setAdapter(ejercicioAdapter);
        cargarEjercicios();

        editTextNombreEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AutoCompletar();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonAñadirEjercicio.setOnClickListener(v -> {
            String nombreEjercicio = editTextNombreEjercicio.getText().toString().trim();
            if (!nombreEjercicio.isEmpty()) {
                // Llamar al método para buscar el ejercicio en la base de datos
                buscarEjercicioEnBaseDeDatos(nombreEjercicio);
            } else {
                Toast.makeText(fragment.requireContext(), "Por favor, introduce el nombre del ejercicio", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancelar.setOnClickListener(v -> {
            // Remove the fragment from the fragment manager
            getParentFragmentManager().beginTransaction().remove(AddRutinaCardFragment.this).commit();
        });

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

    private void AutoCompletar()
    {
        String[] ejercicios = new String[nombreEjerciciosArrayList.size()];
        ejercicios = nombreEjerciciosArrayList.toArray(ejercicios);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,ejercicios);
        editTextNombreEjercicio.setAdapter(adapter);

    }
private void cargarEjercicios()
{
    nombreEjerciciosArrayList= new ArrayList<String>();
    db.collection("Ejercicios")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Verificar si se encontró algún documento con el nombre del ejercicio
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Si se encontró el ejercicio en la base de datos, agregarlo a la lista
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Ejercicio ejercicio = document.toObject(Ejercicio.class);
                        nombreEjerciciosArrayList.add(ejercicio.getNombreEjercicio());
                    }
                }
            });
}
    private void buscarEjercicioEnBaseDeDatos(String nombreEjercicio) {
        // Realizar la consulta en la base de datos para buscar el ejercicio por su nombre
        db.collection("Ejercicios")
                .whereEqualTo("nombreEjercicio", nombreEjercicio)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Verificar si se encontró algún documento con el nombre del ejercicio
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Si se encontró el ejercicio en la base de datos, agregarlo a la lista
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Ejercicio ejercicio = document.toObject(Ejercicio.class);
                            ejercicioArrayList.add(ejercicio);
                        }
                        ejercicioAdapter.notifyDataSetChanged();
                        // Limpiar el campo de texto
                        editTextNombreEjercicio.setText("");
                    } else {
                        // Si no se encontró el ejercicio en la base de datos, mostrar un Toast
                        Toast.makeText(fragment.requireContext(), "El ejercicio no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void guardarRutinaEnFirestore(String nombreRutina, ArrayList<Ejercicio> ejercicioArrayList) {
        // Verificar si ya existe una rutina con el mismo nombre
        db.collection("Rutinas")
                .document(nombreRutina)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Si existe una rutina con el mismo nombre, mostrar un mensaje de error
                        Toast.makeText(fragment.requireContext(), "Ya existe una rutina con el mismo nombre", Toast.LENGTH_SHORT).show();
                        // Cambiar el color del EditText a rojo
                        editTextNombreRutina.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        // Deshabilitar el botón "Añadir Rutina"
                        actualizarEstadoButtonAñadirRutina(false);
                    } else {
                        // Si no existe una rutina con el mismo nombre, proceder a guardarla
                        // Crear un nuevo documento en la colección "Rutinas" con el nombre de la rutina como ID
                        DocumentReference rutinaRef = db.collection("Rutinas").document(nombreRutina);

                        // Crear un mapa para almacenar los ejercicios en la rutina
                        Map<String, Object> rutinaMap = new HashMap<>();
                        for (Ejercicio ejercicio : ejercicioArrayList) {
                            rutinaMap.put("nombreEjercicio", ejercicio.getNombreEjercicio());
                            rutinaRef.set(rutinaMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Rutina guardada exitosamente
                                        Toast.makeText(fragment.requireContext(), "Rutina guardada correctamente", Toast.LENGTH_SHORT).show();
                                        // Notificar al fragmento padre (RutinasFragment) que se ha agregado una nueva rutina
                                        fragment.onActivityResult(1, Activity.RESULT_OK, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Manejar errores en caso de que la operación falle
                                        Toast.makeText(fragment.requireContext(), "Error al guardar la rutina: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar errores en caso de que la consulta falle
                    Toast.makeText(fragment.requireContext(), "Error al verificar la existencia de la rutina: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarEstadoButtonAñadirRutina(boolean estado) {
        buttonAñadirRutina.setEnabled(estado);
    }
}
