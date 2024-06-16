package lopez.sanchez.fitzonetfg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InicioApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_app);
    }
    public void InicioSesion (View v){
        Intent intent = new Intent(this, InicioSesion.class);
        startActivity(intent);
    }
    public void Registrarse (View v){
        Intent intent = new Intent(this, Registrarse.class);
        startActivity(intent);
    }
}