package lopez.sanchez.fitzonetfg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.tasks.OnCompleteListener;

public class Registarse extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText nombre, apellidos, dni, correo, contraseña;
    private RadioButton sexoM, sexoF, tipoU, tipoT;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mfirestore;
    private Button google, registrarse;
    private int RC_SIGN_IN = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registarse);

        mAuth = FirebaseAuth.getInstance();
        mfirestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.img_camara);
        nombre = findViewById(R.id.txt_nombre);
        apellidos = findViewById(R.id.txt_apellidos);
        dni = findViewById(R.id.txt_dni);
        correo = findViewById(R.id.txt_correo);
        contraseña = findViewById(R.id.txt_contraseñaUsu);
        sexoM = findViewById(R.id.rb_hombre);
        sexoF = findViewById(R.id.rb_mujer);
        tipoU = findViewById(R.id.rb_usuario);
        tipoT = findViewById(R.id.rb_tecnico);
        google = findViewById(R.id.button_google);
        registrarse = findViewById(R.id.b_registrarse);
    }

    private void setClickListeners() {
        imageView.setOnClickListener(v -> openGallery());

        google.setOnClickListener(v -> googleSignIn());

     //   registrarse.setOnClickListener(v -> guardarDatos());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void guardarDatos() {
        String nombreUsuario = nombre.getText().toString().trim();
        String apellidosUsuario = apellidos.getText().toString().trim();
        String dniUsuario = dni.getText().toString().trim();
        String emailUsuario = correo.getText().toString().trim();
        String contraseñaUsuario = contraseña.getText().toString().trim();
        String sexoUsuario = sexo();
        String tipoUsuario = tipo();

        if (nombreUsuario.isEmpty() || emailUsuario.isEmpty() || contraseñaUsuario.isEmpty() || sexoUsuario.isEmpty() || tipoUsuario.isEmpty()) {
            Toast.makeText(this, "Rellenar los campos", Toast.LENGTH_SHORT).show();
        } else {
            postUsuario(nombreUsuario, apellidosUsuario, dniUsuario, emailUsuario, contraseñaUsuario, sexoUsuario, tipoUsuario);
        }
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
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Datos correctos", Toast.LENGTH_SHORT).show();
                    pantallaInicio();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al ingresar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String sexo() {
        if (sexoF.isChecked()) {
            return "Mujer";
        } else if (sexoM.isChecked()) {
            return "Hombre";
        } else {
            return "No seleccionado";
        }
    }

    private String tipo() {
        if (tipoU.isChecked()) {
            return "Usuario";
        } else if (tipoT.isChecked()) {
            return "Técnico";
        } else {
            return "Usuario";
        }
    }

    private void pantallaInicio() {
        Intent intent = new Intent(this, PantallaInicio.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult();
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Inicio de sesión con Google exitoso: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        pantallaInicio();
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setSign(View v) {
        guardarDatos();
        if (!correo.getText().toString().isEmpty() && !contraseña.getText().toString().isEmpty()) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo.getText().toString(), contraseña.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        pantallaInicio();
                    } else {
                        showAlert();
                    }
                }
            });
        }

    }

    public void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Error de autentificación.");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

