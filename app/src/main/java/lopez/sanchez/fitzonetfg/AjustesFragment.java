package lopez.sanchez.fitzonetfg;

import static android.app.Activity.RESULT_OK;
import static lopez.sanchez.fitzonetfg.InicioSesion.guardarValor;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AjustesFragment extends Fragment implements View.OnClickListener {

    private LinearLayout layoutCambiarContraseña;
    private LinearLayout layoutEditarTelefono;
    private LinearLayout layoutEditarUsuarioDatos;
    private LinearLayout layoutDesconectarse, l_Tutorial;
    private TextView textViewNombreUsuario, textViewTutorial;
    private ImageView imagenPerfil, btnInstagram;
    private Dialog dialogo;

    Intent intent;
    String userEmail;
    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                        imagenPerfil.setImageBitmap(bitmap);
                        subirImagen();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);
        userEmail = getActivity().getIntent().getStringExtra("email");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        intent = new Intent(getContext(), InicioSesion.class);

        // Inicializamos los LinearLayout
        layoutCambiarContraseña = view.findViewById(R.id.layoutCambiarContraseña);
        layoutEditarTelefono = view.findViewById(R.id.layoutBorrarCuenta);
        layoutEditarUsuarioDatos = view.findViewById(R.id.layoutEditarUsuarioDatos);
        layoutDesconectarse = view.findViewById(R.id.layoutDesconectarse);
        textViewNombreUsuario = view.findViewById(R.id.textViewNombreUsuario);
        imagenPerfil = view.findViewById(R.id.img_fotoPerfilAjustes);
        btnInstagram = view.findViewById(R.id.btnInstagram);
        l_Tutorial= view.findViewById(R.id.layoutTutorial);
        textViewTutorial = view.findViewById(R.id.textViewTutorial);

        // Configuramos listeners para los LinearLayout, botones y la foto
        imagenPerfil.setOnClickListener(v -> openGallery());
        layoutCambiarContraseña.setOnClickListener(this);
        layoutEditarTelefono.setOnClickListener(this);
        layoutEditarUsuarioDatos.setOnClickListener(this);
        layoutDesconectarse.setOnClickListener(this);
        btnInstagram.setOnClickListener(v ->  abrirInstagram());
        l_Tutorial = view.findViewById(R.id.layoutTutorial);
        textViewTutorial.setOnClickListener(this::tutorial);

        getUserData(userEmail);

        return view;
    }

    public void tutorial(View view) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_tutorial);

        // Referenciamos el VideoView y el botón de cierre
        VideoView videoView = dialog.findViewById(R.id.videoView);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);

        // Configuramos el VideoView con un video desde una URL
        String videoUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.tutorialfitzone;
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);

        // Configuramos el MediaController para permitir controles de reproducción
        MediaController mediaController = new MediaController(requireContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> videoView.start());

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layoutBorrarCuenta) {
            mostrarDialogoBorrarCuenta();
        } else if (v.getId() == R.id.layoutCambiarContraseña) {
            mostrarDialogoContraseña();
        }else if (v.getId() == R.id.layoutEditarUsuarioDatos) {
            mostrarDialogoModificarDatosUsuario();
        }else if (v.getId() == R.id.layoutDesconectarse) {
            setLogOut();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen"));
    }

    private void abrirInstagram() {
        String instagramProfile = "fitzone_superate"; // Nombre de usuario del perfil de Instagram que deseamos abrir

        // URL directa del perfil de Instagram
        String uriString = "http://instagram.com/" + instagramProfile;

        // Intent para abrir el perfil de Instagram
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uriString));

        // Verificar si Instagram está instalado y si se puede manejar el intent
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Si Instagram está instalado, abrir el perfil en la aplicación de Instagram
            intent.setPackage("com.instagram.android");
        } else {
            // Si Instagram no está instalado, abrir el perfil en el navegador
            intent.setData(Uri.parse(uriString));
        }

        // Lanzar el intent
        startActivity(intent);
    }


    private void subirImagen() {
        if (selectedImageUri != null) {
            // Subir la imagen seleccionada
            StorageReference reference = storageReference;
            reference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), "No se ha podido subir la imagen a la BBDD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Si selectedImageUri es null, subimos la imagen predeterminada
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imagen_pred);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Subir la imagen predeterminada
            StorageReference reference = storageReference;
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(), "No se ha podido subir la imagen predeterminada a la BBDD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getUserData(String userEmail) {
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
                                cargarImagen(userEmail);
                            }
                        } else {
                            Log.d("0000", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void cargarImagen(String email)
    {
        storageReference = FirebaseStorage.getInstance().getReference("img_perfiles/foto_" + email);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(AjustesFragment.this).load(uri).into(imagenPerfil);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    private void mostrarDialogoBorrarCuenta() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_borrar_cuenta);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btnCerrar = dialogo.findViewById(R.id.btn_cancelar);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        Button btnContinuar = dialogo.findViewById(R.id.btn_continuarBorrado);

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Última oportunidad");
                builder.setMessage("¿Estás seguro que quieres borrar tu cuenta?");
                builder.setPositiveButton("Confirmar", (dialog, which) -> {
                    db.collection("infoUsuarios").whereEqualTo("EMAIL", userEmail).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("infoUsuarios").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Documento eliminado exitosamente
                                            eliminarFotoUsuario((String) document.getData().get("EMAIL"));
                                            Log.d("TAG", "Documento eliminado exitosamente");
                                        });

                            }}})
                            .addOnSuccessListener(aVoid -> {
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            currentUser.delete()
                                                    .addOnCompleteListener(deleteTask -> {
                                                        if (deleteTask.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                                                            guardarValor(getContext(), "mail", "");
                                                            guardarValor(getContext(), "pass", "");
                                                            startActivity(intent);
                                                        }else{
                                                            Toast.makeText(getContext(), "Error al eliminar usuario de Firebase Authentication: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al eliminar usuario de FireStore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                }).setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.create().show();
        }});
        dialogo.show();
    }

    private void eliminarFotoUsuario(String email)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
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

    private void mostrarDialogoContraseña() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_password);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //Botón de cerrar
        ImageButton btnCerrar = dialogo.findViewById(R.id.btn_salirContraseña);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        Button btnCambiarContraseña = dialogo.findViewById(R.id.btn_continuarBorrado);
        EditText editTextNuevoContraseña = dialogo.findViewById(R.id.txt_nuevaContraseña);
        EditText editTextConfirmaContraseña = dialogo.findViewById(R.id.txt_confirmoContraseña);

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

    private boolean validarTelefono(String tel){
        Pattern patron = Pattern.compile("^[0-9]{9}$");
        Pattern patron2 = Pattern.compile("^[+]?[0-9]{10,13}$");
        return !TextUtils.isEmpty(tel) && (patron.matcher(tel).matches() || patron2.matcher(tel).matches());
    }

    private void mostrarDialogoModificarDatosUsuario() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_modificar_usuario);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNombreUsuario = dialogo.findViewById(R.id.editTextNombreUsuario);
        EditText etApellidosUsuario = dialogo.findViewById(R.id.editTextApellidosUsuario);
        EditText etTelefonoUsuario = dialogo.findViewById(R.id.editTextTelefono);
        EditText etDniUsuario = dialogo.findViewById(R.id.editTextDni);
        RadioButton rbHombre = dialogo.findViewById(R.id.radioButtonM);
        RadioButton rbMujer = dialogo.findViewById(R.id.radioButtonF);

        Button btnCancelarModificacion = dialogo.findViewById(R.id.buttonCancelarModiicacion);
        Button btnModificarUsuario = dialogo.findViewById(R.id.buttonModificarUsuario);

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
                                if (document.getData().get("SEXO").toString().equals("Mujer")) {
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

        btnCancelarModificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        btnModificarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("infoUsuarios")
                        .whereEqualTo("EMAIL", userEmail).get()
                        .addOnSuccessListener(documentReference -> {
                            DocumentSnapshot documentSnapshot = documentReference.getDocuments().get(0);
                            String documentId = documentSnapshot.getId();

                            if (!etNombreUsuario.getText().toString().trim().isEmpty() &&
                                    !etApellidosUsuario.getText().toString().trim().isEmpty() &&
                                    !etDniUsuario.getText().toString().trim().isEmpty() &&
                                    !etTelefonoUsuario.getText().toString().trim().isEmpty()) {

                                if (validarDNI(etDniUsuario.getText().toString())) {
                                    if(validarTelefono(etTelefonoUsuario.getText().toString())){

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
                                    }else {
                                        Toast.makeText(getContext(), "Número de teléfono incorrecto.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "DNI inexistente.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Algún campo está vacío.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialogo.show();
    }
    private void Tutorial(View v){
        // TODO: METER VIDEO
    }

    public void actualizarContraseña(String nueva) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().updatePassword(hashContra(nueva))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                db.collection("infoUsuarios")
                                        .whereEqualTo("EMAIL", userEmail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        DocumentReference docRef = db.collection("infoUsuarios").document(document.getId());
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
            // Instancia algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convertimos la contraseña a bytes
            byte[] passwordBytes = contra.getBytes(StandardCharsets.UTF_8);

            // Calculamos el hash
            byte[] hashBytes = digest.digest(passwordBytes);

            // Convertimos el hash a hexadecimal
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
        intent=new Intent(getContext(), InicioSesion.class);
        startActivity(intent);
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
