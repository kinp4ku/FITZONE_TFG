package lopez.sanchez.fitzonetfg;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ContactoFragment extends Fragment {

    private EditText barraDeBusqueda;
    private CheckBox checkBoxSeleccionadas;
    private Button buttonEnviarRutina;
    private Button buttonBorrar;
    private RecyclerView usuariosRecyclerView;

    private UserAdapter usuariosAdapter;
    private List<Usuario> ususariosList;
    private List<Boolean> usuariosSeleccionados;

    private Button buttonBorrarRutina;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    boolean ningunUsuarioSeleccionado = false;
    boolean ningunaRutinaBorrada = false;
    String currentEmail, currentContraseña;

    private static final String I_EMAIL = "email";
    private static final String I_CONTRASENA = "contraseña";
    public static ContactoFragment newInstance(String email, String contraseña) {
        ContactoFragment fragment = new ContactoFragment();
        Bundle args = new Bundle();
        args.putString(I_EMAIL, email);
        args.putString(I_CONTRASENA, contraseña);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacto, container, false);

        barraDeBusqueda = view.findViewById(R.id.search_bar);
        checkBoxSeleccionadas = view.findViewById(R.id.select_all_checkbox);
        buttonEnviarRutina = view.findViewById(R.id.send_routine_button);
        buttonBorrar = view.findViewById(R.id.delete_button);
        usuariosRecyclerView = view.findViewById(R.id.users_recycler_view);
        buttonBorrarRutina = view.findViewById(R.id.borrarRutina);

        if (getArguments() != null) {
            currentEmail = getArguments().getString(I_EMAIL);
            currentContraseña = getArguments().getString(I_CONTRASENA);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ususariosList = new ArrayList<>();
        usuariosSeleccionados = new ArrayList<>();
        usuariosAdapter = new UserAdapter(requireContext(), ususariosList, usuariosSeleccionados);

        usuariosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usuariosRecyclerView.setAdapter(usuariosAdapter);

        loadUsers("");

        barraDeBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkBoxSeleccionadas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < usuariosSeleccionados.size(); i++) {
                usuariosSeleccionados.set(i, isChecked);
            }
            usuariosAdapter.notifyDataSetChanged();
        });

        buttonEnviarRutina.setOnClickListener(v -> {
            mostrarVentanaRutina();
        });

        buttonBorrar.setOnClickListener(v -> {
            if (anyUserSelected()) {
                eliminarUsuariosSeleccionados();
            } else {
                Toast.makeText(getContext(), "Primero debes seleccionar un usuario y luego presionar el botón de eliminar.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBorrarRutina.setOnClickListener(v -> {
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
                    ususariosList.clear();
                    usuariosSeleccionados.clear();
                    String lowerCaseQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombre = document.getString("NOMBRE");
                        String tipo = document.getString("TIPO");
                        String mail = document.getString("EMAIL");
                        String contra = document.getString("CONTRASEÑA");
                        if (nombre != null && nombre.toLowerCase().contains(lowerCaseQuery)) {
                            Usuario user = document.toObject(Usuario.class);
                            user.setId(document.getId());  // Agregar el ID del documento al usuario
                            user.setCorreo(mail);
                            user.setContraseña(contra);
                            if(tipo.equals("Usuario")){
                                ususariosList.add(user);
                                usuariosSeleccionados.add(false);
                            }
                        }
                    }
                    usuariosAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    private void obtenerRutinasDisponibles(Spinner routineSpinner) {
        db.collection("Rutinas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> rutinas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreRutina = document.getId(); // Obtenemos el ID de la rutina como nombre
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
        for (int i = 0; i < usuariosSeleccionados.size(); i++) {
            if (usuariosSeleccionados.get(i)) {
                Usuario user = ususariosList.get(i);
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
        // Verificamos si hay usuarios seleccionados
        for (int i = 0; i < usuariosSeleccionados.size(); i++) {
            if (usuariosSeleccionados.get(i)) {
                ningunUsuarioSeleccionado = true;
                Usuario user = ususariosList.get(i);
                String userId = user.getId();

                // Actualizamos el campo de RUTINA a vacío para el usuario seleccionado
                db.collection("infoUsuarios").document(userId)
                        .update("RUTINA", "")
                        .addOnSuccessListener(aVoid -> {
                            ningunaRutinaBorrada = true;
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al eliminar la rutina para " + user.getNombre() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        if (!ningunUsuarioSeleccionado) {
            Toast.makeText(getContext(), "Primero debes seleccionar un usuario.", Toast.LENGTH_SHORT).show();
        } else {
            if (ningunaRutinaBorrada) {
                Toast.makeText(getContext(), "Rutina eliminada correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Ningún usuario tiene una rutina asignada.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void eliminarUsuariosSeleccionados() {
        Usuario user=null;
        int i, j=0;
        boolean anyUserSelected = false;
        int contadorUsuariosABorrar = 0;

        for (i = usuariosSeleccionados.size() - 1; i >= 0; i--) {
            if (usuariosSeleccionados.get(i)) {
                anyUserSelected = true;
                user = ususariosList.get(i);
                contadorUsuariosABorrar++;
                j = i;
            }
        }
        if(contadorUsuariosABorrar == 1){
            mostrarDialogoConfirmacionEliminarUsuario(user, j);
            usuariosAdapter.notifyDataSetChanged();
        }else if(contadorUsuariosABorrar>1){
            Toast.makeText(getContext(), "Por motivos de seguridad, seleccione un solo usuario a borrar", Toast.LENGTH_SHORT).show();
        }
        if (!anyUserSelected) {
            Toast.makeText(getContext(), "Primero debes seleccionar un usuario.", Toast.LENGTH_SHORT).show();
        }

    }

    private void mostrarDialogoConfirmacionEliminarUsuario(Usuario user, int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmar Eliminado");
        builder.setMessage("¿Estás seguro que quieres borrar a " + user.getNombre() + " " + user.getApellidos() + "?");
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            db.collection("infoUsuarios").document(user.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        mAuth.signOut();
                        String contraseñaUser = user.getContraseña();
                        String emailUser = user.getCorreo();
                        mAuth.signInWithEmailAndPassword(emailUser, contraseñaUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Eliminamos el usuario autenticado
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    currentUser.delete().addOnSuccessListener(aVoid2 -> {
                                            // Eliminamos foto
                                            eliminarFotoUsuario(emailUser);
                                            })
                                            .addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    mAuth.signInWithEmailAndPassword(currentEmail, currentContraseña).addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            // Eliminamos el usuario de Firestore
                                                            usuariosSeleccionados.remove(i);
                                                            ususariosList.remove(i);
                                                            usuariosAdapter.notifyDataSetChanged();
                                                            Toast.makeText(getContext(), "Usuario eliminado: " + user.getNombre(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getContext(), "Error al eliminar usuario de Firebase Authentication: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(getContext(), "Error al iniciar sesión con el usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }


                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al eliminar usuario de FireStore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }).setNegativeButton("Cancelar", (dialog, which) -> {

            dialog.dismiss();
        });
        builder.create().show();
    }
    private void eliminarFotoUsuario(String email)
    {
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        StorageReference reference = storageReference.child("img_perfiles/foto_"+ email);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FirebaseStorage", "Archivo borrado exitosamente");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("FirebaseStorage", "Error al borrar el archivo", exception);
            }
        });
    }
    private boolean anyUserSelected() {
        for (Boolean isSelected : usuariosSeleccionados) {
            if (isSelected) {
                return true;
            }
        }
        return false;
    }
}
