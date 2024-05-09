package lopez.sanchez.fitzonetfg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    VideoView videoView;
    Button boton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (VideoView)findViewById(R.id.VideoPortada);
        Uri path = Uri.parse("android.resource://lopez.sanchez.fitzonetfg/"+R.raw.videoapp);
        videoView.setVideoURI(path);
        videoView.start();
        boton = (Button)findViewById(R.id.b_siguiente);
        boton.setVisibility(View.INVISIBLE);
        // Posterga la visibilidad del botón después de 3 segundos
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Muestra el botón después de 3 segundos
                boton.setVisibility(View.VISIBLE);
            }
        }, 3000);

    }
    public void nueva (View v){
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, InicioApp.class);
        startActivity(intent);
    }

}
