package lopez.sanchez.fitzonetfg;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactoFragment extends Fragment {

    private EditText searchBar;
    private CheckBox selectAllCheckbox;
    private Button sendRoutineButton;
    private Button deleteButton;
    private RecyclerView usersRecyclerView;

    private UserAdapter userAdapter;
    private List<Usuario> userList;
    private List<Boolean> selectedUsers;
    private Button deleteRoutineButton;
    private FirebaseFirestore db;
    boolean anyUserSelected = false;
    boolean anyRoutineDeleted = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacto, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        selectAllCheckbox = view.findViewById(R.id.select_all_checkbox);
        sendRoutineButton = view.findViewById(R.id.send_routine_button);
        deleteButton = view.findViewById(R.id.delete_button);
        usersRecyclerView = view.findViewById(R.id.users_recycler_view);
       deleteRoutineButton = view.findViewById(R.id.borrarRutina);
        db = FirebaseFirestore.getInstance();

        userList = new ArrayList<>();
        selectedUsers = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext(), userList, selectedUsers);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersRecyclerView.setAdapter(userAdapter);

        loadUsers("");

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        selectAllCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < selectedUsers.size(); i++) {
                selectedUsers.set(i, isChecked);
            }
            userAdapter.notifyDataSetChanged();
        });

        sendRoutineButton.setOnClickListener(v -> {
            mostrarVentanaRutina();
        });

        deleteButton.setOnClickListener(v -> {
            if (anyUserSelected()) {
                eliminarUsuariosSeleccionados();
            } else {
                Toast.makeText(getContext(), "Primero debes seleccionar un usuario y luego presionar el botón de eliminar.", Toast.LENGTH_SHORT).show();
            }
        });

        deleteRoutineButton.setOnClickListener(v -> {
            if (anyUserSelected()) {
               eliminarRutina();
            } else {
                Toast.makeText(getContext(), "Primero debes seleccionar un usuario.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadUsers(String query) {
        db.collection("infoUsuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    selectedUsers.clear();
                    String lowerCaseQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombre = document.getString("NOMBRE");
                        if (nombre != null && nombre.toLowerCase().contains(lowerCaseQuery)) {
                            Usuario user = document.toObject(Usuario.class);
                            user.setId(document.getId());  // Agregar el ID del documento al usuario
                            userList.add(user);
                            selectedUsers.add(false);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        /*db.collection("infoUsuarios")
                .whereGreaterThanOrEqualTo("NOMBRE", query)
                .whereLessThanOrEqualTo("NOMBRE", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    selectedUsers.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Usuario user = document.toObject(Usuario.class);
                        user.setId(document.getId());  // Agregar el ID del documento al usuario
                        userList.add(user);
                        selectedUsers.add(false);
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });*/
    }

    private void mostrarVentanaRutina() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_rutina, null);
        builder.setView(dialogView);

        Spinner routineSpinner = dialogView.findViewById(R.id.routine_spinner);
        Button acceptButton = dialogView.findViewById(R.id.accept_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        obtenerRutinasDisponibles(routineSpinner);

        AlertDialog dialog = builder.create();

        acceptButton.setOnClickListener(v -> {
            String rutinaSeleccionada = routineSpinner.getSelectedItem().toString();
            guardarRutinaSeleccionada(rutinaSeleccionada);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarVentanaRutinaParaEliminar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_rutina, null);
        builder.setView(dialogView);

        Spinner routineSpinner = dialogView.findViewById(R.id.routine_spinner);
        Button acceptButton = dialogView.findViewById(R.id.accept_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        obtenerRutinasDisponibles(routineSpinner);

        AlertDialog dialog = builder.create();

        acceptButton.setOnClickListener(v -> {
            String rutinaSeleccionada = routineSpinner.getSelectedItem().toString();
            guardarRutinaSeleccionada(rutinaSeleccionada);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void obtenerRutinasDisponibles(Spinner routineSpinner) {
        db.collection("Rutinas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> rutinas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreRutina = document.getId(); // Obtener el ID de la rutina como nombre
                        rutinas.add(nombreRutina);
                    }

                    if (rutinas.isEmpty()) {
                        Toast.makeText(getContext(), "No hay rutinas disponibles.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, rutinas);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    routineSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al obtener las rutinas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarRutinaSeleccionada(String rutinaSeleccionada) {
        for (int i = 0; i < selectedUsers.size(); i++) {
            if (selectedUsers.get(i)) {
                Usuario user = userList.get(i);
                db.collection("infoUsuarios").document(user.getId())
                        .update("RUTINA", rutinaSeleccionada)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Rutina actualizada para " + user.getNombre(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al actualizar la rutina para " + user.getNombre() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void eliminarRutina() {


        // Verificar si hay usuarios seleccionados
        for (int i = 0; i < selectedUsers.size(); i++) {
            if (selectedUsers.get(i)) {
                anyUserSelected = true;
                Usuario user = userList.get(i);
                String userId = user.getId();

                // Actualizar el campo de RUTINA a vacío para el usuario seleccionado
                db.collection("infoUsuarios").document(userId)
                        .update("RUTINA", "")
                        .addOnSuccessListener(aVoid -> {
                            anyRoutineDeleted = true;
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al eliminar la rutina para " + user.getNombre() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        // Mostrar mensajes según el resultado
        if (!anyUserSelected) {
            Toast.makeText(getContext(), "Primero debes seleccionar un usuario.", Toast.LENGTH_SHORT).show();
        } else {
            if (anyRoutineDeleted) {
                Toast.makeText(getContext(), "Rutina eliminada correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ningún usuario tiene una rutina asignada.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void eliminarUsuariosSeleccionados() {
        boolean anyUserSelected = false;

        for (int i = selectedUsers.size() - 1; i >= 0; i--) {
            if (selectedUsers.get(i)) {
                anyUserSelected = true;
                Usuario user = userList.get(i);
                db.collection("infoUsuarios").document(user.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Usuario eliminado: " + user.getNombre(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al eliminar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                userList.remove(i);
                selectedUsers.remove(i);
            }
        }

        if (!anyUserSelected) {
            Toast.makeText(getContext(), "Primero debes seleccionar un usuario.", Toast.LENGTH_SHORT).show();
        }

        userAdapter.notifyDataSetChanged();
    }

    private boolean anyUserSelected() {
        for (Boolean isSelected : selectedUsers) {
            if (isSelected) {
                return true;
            }
        }
        return false;
    }
}
