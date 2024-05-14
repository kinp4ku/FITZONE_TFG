package lopez.sanchez.fitzonetfg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PantallaInicio extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_inicio);

        bottomNavigationView = findViewById(R.id.bottomNavView);
        frameLayout = findViewById(R.id.frame);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                if (item.getItemId() == R.id.horario) {
                    fragment = new HorariosFragment();
                } else if (item.getItemId() == R.id.rutinas) {
                    //fragment = new RutinasFragment();
                } else if (item.getItemId() == R.id.ejercicios) {
                    fragment = new EjerciciosFragment();
                } else if (item.getItemId() == R.id.ajustes) {
                   fragment = new AjustesFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }

                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    public void HorarioPantalla(View v) {
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, Horarios.class);
        startActivity(intent);
    }

    public void AjustesPantalla(View v) {
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, Ajustes.class);
        startActivity(intent);
    }
}