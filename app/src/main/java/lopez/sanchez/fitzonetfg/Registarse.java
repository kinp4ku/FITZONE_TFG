package lopez.sanchez.fitzonetfg;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

public class Registarse extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
   // private static final int RC_SIGN_IN = 9001;
    private ImageView imageView;
    EditText nombre, apellidos, dni, correo, contraseña;
    RadioButton sexoM,sexoF, tipoU, tipoT;
    FitZoneData fitZoneData;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    Button google, registrarse;
    int RC_SIGN_IN = 20;

    String nombreUsuario, apellidosUsuario, dniUsuario, emailUsuario, contraseñaUsuario, sexoUsuario, tipoUsuario;

    private FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registarse);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar opciones de inicio de sesión con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        BotonGoogle();


        //guardo lo datos
        guardarDatos();
        mfirestore = FirebaseFirestore.getInstance();


        // Inicializar imageView
        imageView = findViewById(R.id.img_camara);

        // Inicializar FitZoneData
      /*  fitZoneData = new FitZoneData(this);
        fitZoneData.open();*/

        // Asignar onClickListener a la imagen
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Inicializar los otros elementos de la vista
        nombre = findViewById(R.id.txt_nombre);
        apellidos = findViewById(R.id.txt_apellidos);
        dni = findViewById(R.id.txt_dni);
        correo = findViewById(R.id.txt_correo);
        contraseña = findViewById(R.id.txt_contraseñaUsu);
        sexoM = findViewById(R.id.rb_hombre);
        sexoF = findViewById(R.id.rb_mujer);
        tipoU = findViewById(R.id.rb_usuario);
        tipoT = findViewById(R.id.rb_tecnico);
    }

    private void openGallery() {
        // Crear un intent para abrir la galería
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Iniciar la actividad de la galería
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == RC_SIGN_IN) {
            // Resultado del inicio de sesión con Google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
*/
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(this, "Error al iniciar sesión con Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Inicio de sesión con Google exitoso: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        // viajará a la actividad principal
                         pantallaInicio();

                    } else {
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public String sexo() {
        if (sexoF.isChecked()) {
            return "Hombre";
        } else if (sexoM.isChecked()) {
            return "Mujer";
        } else {
            return "No seleccionado";
        }
    }

    public String tipo() {
        if (tipoU.isChecked()) {
            return "Usuario";
        } else if (tipoT.isChecked()) {
            return "Técnico";
        } else {
            Toast.makeText(this, "Debes seleccionar un tipo", Toast.LENGTH_SHORT).show();
            return "Usuario";
        }
    }

    public void onRegistrarClick(View view) {
        // Obtener los datos del formulario
        String nombreUsuario = nombre.getText().toString();
        String apellidosUsuario = apellidos.getText().toString();
        String dniUsuario = dni.getText().toString();
        String emailUsuario = correo.getText().toString();
        String contraseñaUsuario = contraseña.getText().toString();
        String sexoUsuario = sexo();
        String tipoUsuario = tipo();

        // Guardar los datos del usuario en Firebase
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
        String userId = usuariosRef.push().getKey();
        Usuario usuario = new Usuario(nombreUsuario, apellidosUsuario, dniUsuario, emailUsuario, contraseñaUsuario, sexoUsuario, tipoUsuario);
        usuariosRef.child(userId).setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    // Registro exitoso, mostrar Toast y esperar 2 segundos
                    Toast.makeText(getApplicationContext(), "Registrado correctamente", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(
                            () -> {
                                // Iniciar sesión con Google después de 2 segundos
                                pantallaInicio();
                            }, 2000);
                })
                .addOnFailureListener(e -> {
                    // Error al registrar, mostrar Toast de error
                    Toast.makeText(getApplicationContext(), "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void pantallaInicio(){
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, PantallaInicio.class);
        startActivity(intent);
    }
/*  FitZoneData fitZoneData;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;*/
    public void BotonGoogle(){
        google= findViewById(R.id.button_google);
        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        GoogleSignInOptions gso =new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleSignInClient=GoogleSignIn.getClient(this, gso);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();

            }
        });
    }

    public void googleSignIn(){
        Intent intent= mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == RC_SIGN_IN) {
            // Resultado del inicio de sesión con Google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        fitZoneData.close();
    }

    public void guardarDatos(){
        registrarse = findViewById(R.id.b_registrarse);

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombreUsuario = nombre.getText().toString().trim();
                apellidosUsuario = apellidos.getText().toString().trim();
                dniUsuario = dni.getText().toString().trim();
                emailUsuario = correo.getText().toString().trim();
                contraseñaUsuario = contraseña.getText().toString().trim();
                sexoUsuario = sexo();
                tipoUsuario = tipo();

                if(nombreUsuario.isEmpty() || emailUsuario.isEmpty() || contraseñaUsuario.isEmpty() || sexoUsuario.isEmpty() ||  tipoUsuario.isEmpty()){
                    Toast.makeText(Registarse.this, "Rellenar los campos", Toast.LENGTH_SHORT).show();
                }else{
                    postUsuario(nombreUsuario, apellidosUsuario, dniUsuario, emailUsuario, contraseñaUsuario, sexoUsuario, tipoUsuario);
                }

            }
        });

    }

    private void postUsuario(String nombreUsuario, String apellidosUsuario, String dniUsuario, String emailUsuario, String contraseñaUsuario, String sexoUsuario, String tipoUsuario) {
        Map<String, Object> map = new HashMap<>();
        map.put("NOMBRE", nombreUsuario);
        map.put("APELLIDOS", apellidosUsuario);
        map.put("DNI", dniUsuario);
        map.put("EMAIL", emailUsuario);
        map.put("CONTRASEÑA", contraseñaUsuario);
        map.put("SEXO", sexoUsuario);
        map.put("TIPO", tipoUsuario);

        mfirestore.collection("infoUsuarios")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Datos correctos", Toast.LENGTH_SHORT).show();
                       // finish();
                        pantallaInicio();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error - al ingresar", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
