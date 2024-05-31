package lopez.sanchez.fitzonetfg;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.getIntent;

import static lopez.sanchez.fitzonetfg.InicioSesion.guardarValor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class AjustesFragment extends Fragment implements View.OnClickListener {

    private LinearLayout layoutCambiarContraseña;
    private LinearLayout layoutEditarTelefono;
    private LinearLayout layoutEditarUsuarioDatos;
    private LinearLayout layoutDesconectarse;
    private TextView textViewNombreUsuario, desconectarTV;
    private ImageView btnDesconectar;
    private Dialog dialogo;
    String userEmail;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);
        userEmail = getActivity().getIntent().getStringExtra("email");

        // Inicializar los LinearLayout
        mAuth = FirebaseAuth.getInstance();
        layoutCambiarContraseña = view.findViewById(R.id.layoutCambiarContraseña);
        layoutEditarTelefono = view.findViewById(R.id.layoutEditarTelefono);
        layoutEditarUsuarioDatos = view.findViewById(R.id.layoutEditarUsuarioDatos);
        layoutDesconectarse = view.findViewById(R.id.layoutDesconectarse);
        textViewNombreUsuario = view.findViewById(R.id.textViewNombreUsuario);
        btnDesconectar = view.findViewById(R.id.btnDesconectarse);
        desconectarTV = view.findViewById(R.id.desconectarTV);

        // Configurar listeners para los LinearLayout
        layoutCambiarContraseña.setOnClickListener(this);
        layoutEditarTelefono.setOnClickListener(this);
        layoutEditarUsuarioDatos.setOnClickListener(this);
        layoutDesconectarse.setOnClickListener(this);


        getUserData(userEmail);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layoutEditarTelefono) {
            mostrarDialogoTelefono();
        } else if (v.getId() == R.id.layoutCambiarContraseña) {
            mostrarDialogoContraseña();
        }else if (v.getId() == R.id.layoutEditarUsuarioDatos) {
            mostrarDialogoModificarDatosUsuario();
        }else if (v.getId() == R.id.layoutDesconectarse) {
            setLogOut();
        }
    }

    private void getUserData(String userEmail) {
        // Inicializar Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("infoUsuarios")
                .whereEqualTo("EMAIL", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                textViewNombreUsuario.setText((CharSequence) document.getData().get("NOMBRE"));
                            }
                        } else {
                            Log.d("0000", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void mostrarDialogoTelefono() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_telefono);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Configurar el botón de cerrar
        ImageButton btnCerrar = dialogo.findViewById(R.id.btn_salirContraseña);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        // Configurar el botón de cambiar teléfono
        Button btnCambiarTelefono = dialogo.findViewById(R.id.btn_cambiarContraseña);
        EditText editTextTelefonoNuevo = dialogo.findViewById(R.id.txt_nuevaContraseña);

        btnCambiarTelefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevoTelefono = editTextTelefonoNuevo.getText().toString().trim();
                if (!nuevoTelefono.isEmpty()) {
                    // Lógica para cambiar el teléfono en Firebase
                    // FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(...)
                    Toast.makeText(getContext(), "Teléfono actualizado correctamente", Toast.LENGTH_SHORT).show();
                    dialogo.dismiss();
                } else {
                    Toast.makeText(getContext(), "Por favor, ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogo.show();
    }

    private void mostrarDialogoContraseña() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_password);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Configurar el botón de cerrar
        ImageButton btnCerrar = dialogo.findViewById(R.id.btn_salirContraseña);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        // Configurar el botón de cambiar contraseña
        Button btnCambiarContraseña = dialogo.findViewById(R.id.btn_cambiarContraseña);
        EditText editTextNuevoContraseña = dialogo.findViewById(R.id.txt_nuevaContraseña);
        EditText editTextConfirmaContraseña = dialogo.findViewById(R.id.txt_confirmoContraseña);

        // Configurar la imagen del ojo para mostrar/ocultar contraseña
        ImageView imgMostrarContraseña = dialogo.findViewById(R.id.btn_ojo);
        imgMostrarContraseña.setOnClickListener(new View.OnClickListener() {
            boolean passwordVisible = false;

            @Override
            public void onClick(View v) {
                if (passwordVisible) {
                    // Ocultar contraseña
                    editTextNuevoContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editTextConfirmaContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                    imgMostrarContraseña.setImageResource(R.drawable.ojo); // Cambiar icono a ojo abierto
                } else {
                    // Mostrar contraseña
                    editTextNuevoContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextConfirmaContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                    imgMostrarContraseña.setImageResource(R.drawable.baseline_remove_red_eye_24); // Cambiar icono a ojo cerrado
                }
            }
        });

        btnCambiarContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevaContraseña = editTextNuevoContraseña.getText().toString().trim();
                String confirmarContraseña = editTextConfirmaContraseña.getText().toString().trim();
                if(!nuevaContraseña.isEmpty() && !confirmarContraseña.isEmpty()){
                    if (nuevaContraseña.equals(confirmarContraseña)) {
                        if(reglasContraseña(nuevaContraseña)){
                            // Lógica para cambiar la contraseña en Firebase
                            actualizarContraseña(nuevaContraseña);
                            Toast.makeText(getContext(), "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                            dialogo.dismiss();
                        }else{
                            showAlert("Introduzca al menos una mayúscula, una minúscula, un número y un símbolo. \nLongitud mínima de 8 caracteres");
                            editTextNuevoContraseña.setText("");
                            editTextConfirmaContraseña.setText("");
                        }
                    } else {
                        Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                        editTextNuevoContraseña.setText("");
                        editTextConfirmaContraseña.setText("");
                    }
                }else {
                    Toast.makeText(getContext(), "Los campos están vacíos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogo.show();
    }

    private boolean validarDNI(String dni)
    {
        if(dni.length() != 9) //Si el DNI tiene algo distinto a 9 caracteres no es valido
        {
            return false;
        }
        else if(!Character.isLetter(dni.substring(8).charAt(0))) // Si el último caracter no es una letra no es valido
        {
            return false;
        }
        else if(!dni.substring(0,8).matches("-?\\d+(\\.\\d+)?")) //Si algun caracter dentro de los numeros no es un numero no es valido
        {
            return false;
        }
        String letra = dni.substring(8).toUpperCase();
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";

        String numeros = dni.substring(0, 8);
        int numerosInt = Integer.parseInt(numeros);
        int resto = numerosInt % 23;
        String letraCorrecta = letrasValidas.substring(resto, resto+1);

        return letraCorrecta.equals(letra);
    }

    private void mostrarDialogoModificarDatosUsuario(){
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_modificar_usuario);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNombreUsuario = dialogo.findViewById(R.id.editTextNombreUsuario);
        EditText etApellidosUsuario = dialogo.findViewById(R.id.editTextApellidosUsuario);
        EditText etTelefonoUsuario = dialogo.findViewById(R.id.editTextTelefono);
        EditText etDniUsuario = dialogo.findViewById(R.id.editTextDni);
        RadioButton rbHombre = dialogo.findViewById(R.id.radioButtonM);
        RadioButton rbMujer = dialogo.findViewById(R.id.radioButtonF);

        Button cancelarModificacion = dialogo.findViewById(R.id.buttonCancelarModiicacion);
        Button modificacionUsuario = dialogo.findViewById(R.id.buttonModificarUsuario);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("infoUsuarios")
                .whereEqualTo("EMAIL", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                etNombreUsuario.setText((CharSequence) document.getData().get("NOMBRE"));
                                etApellidosUsuario.setText((CharSequence) document.getData().get("APELLIDOS"));
                                etTelefonoUsuario.setText((CharSequence) document.getData().get("TELEFONO"));
                                etDniUsuario.setText((CharSequence) document.getData().get("DNI"));
                                if(document.getData().get("SEXO").toString().equals("Mujer")){
                                    rbMujer.setChecked(true);
                                } else if (document.getData().get("SEXO").toString().equals("Hombre")) {
                                    rbHombre.setChecked(true);
                                }
                            }
                        } else {
                            Log.d("0000", "Error getting documents: ", task.getException());
                        }
                    }
                });

        cancelarModificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        modificacionUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("infoUsuarios")
                        .whereEqualTo("EMAIL", userEmail).get()
                        .addOnSuccessListener(documentReference -> {
                            DocumentSnapshot documentSnapshot = documentReference.getDocuments().get(0);
                            String documentId = documentSnapshot.getId();

                            if(!etNombreUsuario.getText().toString().trim().isEmpty() &&
                                !etApellidosUsuario.getText().toString().trim().isEmpty() &&
                                !etDniUsuario.getText().toString().trim().isEmpty() &&
                                !etTelefonoUsuario.getText().toString().trim().isEmpty()){

                                if(validarDNI(etDniUsuario.getText().toString())) {

                                    // Campos que deseamos actualizar
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("NOMBRE", etNombreUsuario.getText().toString());
                                    updates.put("APELLIDOS", etApellidosUsuario.getText().toString());
                                    updates.put("DNI", etDniUsuario.getText().toString());
                                    updates.put("TELEFONO", etTelefonoUsuario.getText().toString());
                                    if (rbMujer.isChecked()) {
                                        updates.put("SEXO", "Mujer");
                                    } else if (rbHombre.isChecked()) {
                                        updates.put("SEXO", "Hombre");
                                    } else {
                                        updates.put("SEXO", "No seleccionado");
                                    }


                                    // Actualiza el documento
                                    db.collection("infoUsuarios").document(documentId)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getActivity(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                                                dialogo.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getActivity(), "Error al actualizar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                dialogo.dismiss();
                                            });
                                }else{
                                    Toast.makeText(getContext(), "DNI inexistente.", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(getContext(), "Algún campo está vacío.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialogo.show();
    }

    public void actualizarContraseña(String nueva) {
        // Obtén la instancia del usuario actual
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // Actualiza la contraseña en Firebase Authentication
            mAuth.getCurrentUser().updatePassword(hashContra(nueva))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Obtener el ID del documento del usuario en Firestore
                                db.collection("infoUsuarios")
                                        .whereEqualTo("EMAIL", userEmail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        DocumentReference docRef = db.collection("infoUsuarios").document(document.getId());
                                                        // Actualiza el campo de la contraseña en Firestore
                                                        docRef.update("CONTRASEÑA", hashContra(nueva))
                                                                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Documento actualizado con éxito."))
                                                                .addOnFailureListener(e -> Log.e("FirestoreHelper", "Error al actualizar el documento", e));
                                                    }
                                                    guardarValor(getContext(), "mail", "");
                                                    guardarValor(getContext(), "pass", "");
                                                } else {
                                                    Log.d("FirestoreHelper", "No se encontró el documento del usuario o hubo un error");
                                                }
                                            }
                                        });
                            } else {
                                Log.e("FirebaseAuth", "Error al actualizar la contraseña", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("FirebaseAuth", "No se encontró el usuario autenticado");
        }
    }

    public boolean reglasContraseña(String pass) {
        boolean cumple = true;
        int contadorMayus=0, contadorMinus=0, contadorNum=0, contadorSimbolo=0;
        char[] caracteres = pass.toCharArray();

        //Longitud mínima.
        if (!(caracteres.length >= 8)) {
            cumple = false;
        }

        for (char caracter : caracteres) {
            //Al menos un símbolo
            if (!(Character.isDigit(caracter) || Character.isLetter(caracter))) {
                contadorSimbolo++;
            }

            //Al menos un número
            if (Character.isDigit(caracter)) {
                contadorNum++;
            }

            //Al menos una mayúscula
            if (Character.isUpperCase(caracter)) {
                contadorMayus++;
            }

            //Al menos una minúscula
            if (Character.isLowerCase(caracter)) {
                contadorMinus++;
            }
        }

        if(contadorMayus==0 || contadorMinus==0 || contadorNum==0 || contadorSimbolo==0){
            cumple = false;
        }

        return cumple;
    }

    private String hashContra(String contra){
        StringBuilder hexHash = null;
        try {
            // Obtén una instancia del algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convierte la contraseña a bytes
            byte[] passwordBytes = contra.getBytes(StandardCharsets.UTF_8);

            // Calcula el hash
            byte[] hashBytes = digest.digest(passwordBytes);

            // Convierte el hash a una representación hexadecimal
            hexHash = new StringBuilder();
            for (byte b : hashBytes) {
                hexHash.append(String.format("%02x", b));
            }


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexHash.toString();
    }

    public void setLogOut(){
        guardarValor(getContext(), "mail", "");
        guardarValor(getContext(), "pass", "");
        mAuth.signOut();
        getActivity().onBackPressed();
    }

    public void showAlert(String mns) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Contraseña inválida");
        builder.setMessage(mns);
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
