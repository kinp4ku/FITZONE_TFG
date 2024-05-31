package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InicioSesion extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private Intent i;
    private static Context contexto;
    private EditText contra, email;
    private Button login;
    private CheckBox cb;
    private TextView titulo;

    private String correo, contraseña;
    private static String PREFS_KEY = "Preferencias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        contexto = getApplicationContext();


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Inicializar vistas
        login = findViewById(R.id.loginButton);
        email = findViewById(R.id.ETTextEmail);
        contra = findViewById(R.id.ETTextPassword);
        cb = findViewById(R.id.cbMantener);
        titulo = findViewById(R.id.txt_inicioSesion);

        // Configurar animaciones
        Animation animacionArriba = AnimationUtils.loadAnimation(this, R.anim.arriba);
        Animation trans = AnimationUtils.loadAnimation(this, R.anim.transparencia);
        ImageView circulo = findViewById(R.id.circulo);
        ImageView circulito = findViewById(R.id.circulito);
        circulo.setAnimation(animacionArriba);
        circulito.setAnimation(trans);

        email.setText("");
        contra.setText("");
        cb.setChecked(true);
        email.setText(leerValor(contexto,"mail"));
        contra.setText(leerValor(contexto,"pass"));

        // Ocultar elementos
        cb.setVisibility(View.INVISIBLE);
        email.setVisibility(View.INVISIBLE);
        contra.setVisibility(View.INVISIBLE);
        circulito.setVisibility(View.INVISIBLE);
        login.setVisibility(View.INVISIBLE);
        titulo.setVisibility(View.INVISIBLE);

        // Esperar 2 segundos antes de mostrar los elementos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cb.setVisibility(View.VISIBLE);
                email.setVisibility(View.VISIBLE);
                contra.setVisibility(View.VISIBLE);
                circulito.setVisibility(View.VISIBLE);
                login.setVisibility(View.VISIBLE);
                titulo.setVisibility(View.VISIBLE);
            }
        }, 2000); // 2000 milisegundos = 2 segundos

        if(!leerValor(contexto,"mail").equals("")) {
            cb.setChecked(true);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), contra.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (cb.isChecked()) {
                            guardarValor(contexto, "mail", email.getText().toString());
                            guardarValor(contexto, "pass", contra.getText().toString());
                        } else {
                            guardarValor(contexto, "mail", "");
                            guardarValor(contexto, "pass", "");
                        }
                        getTipoUser();
                    } else {
                        showAlert();
                    }
                }
            });
        }else{
            cb.setChecked(false);
        }
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

    public void setLogin(View v) {
        String emailText = email.getText().toString();
        String passwordText = contra.getText().toString();

        if (!emailText.isEmpty() && !passwordText.isEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText, hashContra(passwordText))
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if (cb.isChecked()) {
                                    guardarValor(contexto, "mail", emailText);
                                    guardarValor(contexto, "pass", hashContra(passwordText));
                                } else {
                                    guardarValor(contexto, "mail", "");
                                    guardarValor(contexto, "pass", "");
                                }
                                getTipoUser();
                            } else {
                                showAlert();
                            }
                        }
                    });
        }
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Error de autentificación.");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void guardarValor(Context context, String keyPref, String valor) {
        SharedPreferences acceso = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = acceso.edit();
        editor.putString(keyPref, valor);
        editor.apply(); // Usar apply() en lugar de commit()
    }

    public static String leerValor(Context context, String keyPref) {
        SharedPreferences acceso = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  acceso.getString(keyPref, "");
    }

    public static Context getContext() {
        return contexto;
    }

    /*private static void signInWithGoogle(){
        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
        String idToken = credential.getGoogleIdToken();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }*/
    private void getTipoUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("infoUsuarios")
                .whereEqualTo("EMAIL", email.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String tipo="";
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                tipo =(String) document.getData().get("TIPO").toString();
                                if(tipo.equals("Técnico")){
                                    tecnico();
                                }else{
                                    usuario();
                                }
                            }
                        } else {
                            Log.d("0000", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    public void tecnico(){
        i = new Intent(InicioSesion.this, Tecnico.class);
        i.putExtra("email", email.getText().toString());
        startActivity(i);
        email.setText("");
        contra.setText("");
    }

    public void usuario(){
        i = new Intent(InicioSesion.this, PantallaInicio.class);
        i.putExtra("email", email.getText().toString());
        startActivity(i);
        email.setText("");
        contra.setText("");
    }
}