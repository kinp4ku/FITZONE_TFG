package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

public class InicioSesion extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private Context contexto;
    private EditText contra, email;
    private Button login;
    private CheckBox cb;
    private TextView titulo;

    private static String PREFS_KEY = "ficheroPreferencias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        contexto = this;

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

        // Ocultar elementos
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
                email.setVisibility(View.VISIBLE);
                contra.setVisibility(View.VISIBLE);
                circulito.setVisibility(View.VISIBLE);
                login.setVisibility(View.VISIBLE);
                titulo.setVisibility(View.VISIBLE);
            }
        }, 2000); // 2000 milisegundos = 2 segundos
    }

    public void setLogin(View v) {
        String emailText = email.getText().toString();
        String passwordText = contra.getText().toString();

        if (!emailText.isEmpty() && !passwordText.isEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(InicioSesion.this, PantallaInicio.class);
                                intent.putExtra("email", emailText);
                                startActivity(intent);

                                if (cb.isChecked()) {
                                    guardarValor(contexto, "mail", emailText);
                                    guardarValor(contexto, "pass", passwordText);
                                } else {
                                    guardarValor(contexto, "mail", "");
                                    guardarValor(contexto, "pass", "");
                                }
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

    private static void guardarValor(Context context, String keyPref, String valor) {
        SharedPreferences acceso = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = acceso.edit();
        editor.putString(keyPref, valor);
        editor.apply(); // Usar apply() en lugar de commit()
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
}
