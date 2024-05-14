package lopez.sanchez.fitzonetfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/*
        if(v.getId()==R.id.btnCambiarContraseña){
            mostrarOcultarLayout(layoutCambiarContraseña);
        }else if (v.getId()==R.id.btnEditarTelefono){
            mostrarOcultarLayout(layoutEditarTelefono);
        }else if(v.getId()==R.id.btnEditarUsuario){
            mostrarOcultarLayout(layoutEditarUsuario);
        }else{
            desconectarse();
        }*/
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class AjustesFragment extends Fragment implements View.OnClickListener {

    private LinearLayout layoutCambiarContraseña;
    private LinearLayout layoutEditarTelefono;
    private LinearLayout layoutEditarUsuarioDatos;

    private EditText editTextNuevaContraseña;
    private EditText editTextConfirmarContraseña;
    private EditText editTextTelefono;
    private EditText editTextNombre;
    private EditText editTextApellido;
    private EditText editTextDNI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        // Obtener referencias de las vistas del layout
        layoutCambiarContraseña = view.findViewById(R.id.layoutCambiarContraseña);
        layoutEditarTelefono = view.findViewById(R.id.layoutEditarTelefono);
        layoutEditarUsuarioDatos = view.findViewById(R.id.layoutEditarUsuarioDatos);

        // Configurar listeners para los botones
        ImageButton btnCambiarContraseña = view.findViewById(R.id.btnCambiarContraseña);
        ImageButton btnEditarTelefono = view.findViewById(R.id.btnEditarTelefono);
        ImageButton btnEditarUsuario = view.findViewById(R.id.btnEditarUsuario);
        ImageButton btnDesconectarse = view.findViewById(R.id.btnDesconectarse);

        btnCambiarContraseña.setOnClickListener(this);
        btnEditarTelefono.setOnClickListener(this);
        btnEditarUsuario.setOnClickListener(this);
        btnDesconectarse.setOnClickListener(this);

        // Obtener referencias de EditText
        editTextNuevaContraseña = view.findViewById(R.id.editTextNuevaContraseña);
        editTextConfirmarContraseña = view.findViewById(R.id.editTextConfirmarContraseña);
        editTextTelefono = view.findViewById(R.id.editTextTelefono);
        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextApellido = view.findViewById(R.id.editTextApellido);
        editTextDNI = view.findViewById(R.id.editTextDNI);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnCambiarContraseña){
            mostrarLayoutCambiarContraseña();
        }else if (v.getId()==R.id.btnEditarTelefono){
            mostrarLayoutEditarTelefono();
        }else if(v.getId()==R.id.btnEditarUsuario){
            mostrarLayoutEditarUsuario();
        }else{
            desconectarse();
        }

    }

    private void mostrarLayoutCambiarContraseña() {
      //  ocultarTodosLosLayouts();
        layoutCambiarContraseña.setVisibility(View.VISIBLE);
        guardarContraseña(layoutCambiarContraseña);
    }

    private void mostrarLayoutEditarTelefono() {
      //  ocultarTodosLosLayouts();
        layoutEditarTelefono.setVisibility(View.VISIBLE);
    }

    private void mostrarLayoutEditarUsuario() {
      //  ocultarTodosLosLayouts();
        layoutEditarUsuarioDatos.setVisibility(View.VISIBLE);
        guardarDatosUsuario(layoutEditarUsuarioDatos);
    }

    private void ocultarTodosLosLayouts() {
        layoutCambiarContraseña.setVisibility(View.GONE);
        layoutEditarTelefono.setVisibility(View.GONE);
        layoutEditarUsuarioDatos.setVisibility(View.GONE);
    }

    private void desconectarse() {
        Toast.makeText(getContext(), "Desconectando...", Toast.LENGTH_SHORT).show();
        // Aquí implementa la lógica para desconectar al usuario
    }

    // Método para guardar la nueva contraseña
    public void guardarContraseña(View view) {
        String nuevaContraseña = editTextNuevaContraseña.getText().toString();
        String confirmarContraseña = editTextConfirmarContraseña.getText().toString();

        if (!nuevaContraseña.isEmpty() && !confirmarContraseña.isEmpty()) {
            if (nuevaContraseña.equals(confirmarContraseña)) {
                // Aquí puedes implementar la lógica para guardar la nueva contraseña
                Toast.makeText(getContext(), "Contraseña guardada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Ingresa una nueva contraseña y confírmala", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para guardar los datos del usuario
    public void guardarDatosUsuario(View view) {
        String nombre = editTextNombre.getText().toString();
        String apellido = editTextApellido.getText().toString();
        String dni = editTextDNI.getText().toString();

        if (!nombre.isEmpty() && !apellido.isEmpty() && !dni.isEmpty()) {
            // Aquí puedes implementar la lógica para guardar los datos del usuario
            Toast.makeText(getContext(), "Datos del usuario guardados correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

}
