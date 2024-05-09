package lopez.sanchez.fitzonetfg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/*
enum class ProviderType{
    BASIC,
    GOOGLE
}*/
public class InicioSesion extends AppCompatActivity {
    EditText contraseña;
    EditText correo ;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inicio_sesion);
      /*  //FireBase:
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message", "Integracion de Firebase completa");
        analytics.logEvent("InitScreen", bundle);


        // Setup
         bundle = getIntent().getExtras();
        String email = (bundle != null) ? bundle.getString("email") : null;
        String provider = (bundle != null) ? bundle.getString("provider") : null;
        setup(email != null ? email : "", provider != null ? provider : "");

        //GUARDADO DE DATOS:
        SharedPreferences prefs = getSharedPreferences("UsuariosFitZone", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("contraseña", provider);
        editor.apply();



        */

mAuth = FirebaseAuth.getInstance();
        //creo nimaciones para dar la transición de inicio de sesión
        Animation animacionArriba = AnimationUtils.loadAnimation(this, R.anim.arriba);
        Animation trans = AnimationUtils.loadAnimation(this, R.anim.transparencia);
        ImageView circulo = findViewById(R.id.circulo);
        ImageView circulito = findViewById(R.id.circulito);
        contraseña = findViewById(R.id.txt_contraseña);
        correo = findViewById(R.id.txt_gmail);
        Button logIn = findViewById(R.id.btn_entrar);
        circulo.setAnimation(animacionArriba);
        circulito.setAnimation(trans);

       // circulito.setAnimation(trans);
        correo.setVisibility(View.INVISIBLE);
        contraseña.setVisibility(View.INVISIBLE);
        circulito.setVisibility(View.INVISIBLE);
        logIn.setVisibility(View.INVISIBLE);
        // Esperar 2 segundos antes de mostrar los elementos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Mostrar los elementos después de 2 segundos
                correo.setVisibility(View.VISIBLE);
                contraseña.setVisibility(View.VISIBLE);
                circulito.setVisibility(View.VISIBLE);
                logIn.setVisibility(View.VISIBLE);
                //se debería de poner una transición para que saliera la img para iniciar sesión
            }
        }, 2000); // 2000 milisegundos = 2 segundos

    }


    /*
    private void setup(String email, String provider) {
       // title = "Inicio";
        correo.setText(email);
        contraseña.setText(provider);
        /*logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(UsuariosFitZone, Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                FirebaseAuth.getInstance().signOut();
                onBackPressed();
            }
        });*/
    //}

    public void EntradaApp(View v){
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, PantallaInicio.class);
        startActivity(intent);
    }

    public void logIn(View v){
        String emailUser = correo.getText().toString().trim();
        String passUser = contraseña.getText().toString().trim();

        if(emailUser.isEmpty() && passUser.isEmpty()){
            Toast.makeText(this, "Rellenar datos", Toast.LENGTH_SHORT).show();
        }else{
            loginUser(emailUser, passUser);
        }
    }

    private void loginUser(String emailUser, String passUser) {

        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                   startActivity(new Intent(InicioSesion.this, PantallaInicio.class));
                    Toast.makeText(InicioSesion.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(InicioSesion.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InicioSesion.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}










