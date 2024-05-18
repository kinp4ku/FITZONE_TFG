package lopez.sanchez.fitzonetfg;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AjustesFragment extends Fragment implements View.OnClickListener {

    private LinearLayout layoutCambiarContraseña;
    private LinearLayout layoutEditarTelefono;
    private LinearLayout layoutEditarUsuarioDatos;
    private TextView textViewNombreUsuario;
    private Dialog dialogo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);
        String userEmail = getActivity().getIntent().getStringExtra("email");

        // Inicializar los LinearLayout
        layoutCambiarContraseña = view.findViewById(R.id.layoutCambiarContraseña);
        layoutEditarTelefono = view.findViewById(R.id.layoutEditarTelefono);
        layoutEditarUsuarioDatos = view.findViewById(R.id.layoutEditarUsuarioDatos);
        textViewNombreUsuario = view.findViewById(R.id.textViewNombreUsuario);

        // Configurar listeners para los LinearLayout
        layoutCambiarContraseña.setOnClickListener(this);
        layoutEditarTelefono.setOnClickListener(this);
        layoutEditarUsuarioDatos.setOnClickListener(this);

        getUserData(userEmail);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layoutEditarTelefono) {
            mostrarDialogoTelefono();
        }else if(v.getId() == R.id.layoutCambiarContraseña){
            mostrarDialogoContraseña();
        }
    }

    private void getUserData(String userEmail){
        // Inicializar Firestore
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
                            }
                        } else {
                            Log.d("0000", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void mostrarDialogoTelefono() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_telefono);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Configurar el botón de cerrar
        ImageButton btnCerrar = dialogo.findViewById(R.id.btn_salirContraseña);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        // Configurar el botón de cambiar teléfono
        Button btnCambiarTelefono = dialogo.findViewById(R.id.btn_cambiarContraseña);
        EditText editTextTelefonoNuevo = dialogo.findViewById(R.id.txt_nuevaContraseña);

        btnCambiarTelefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevoTelefono = editTextTelefonoNuevo.getText().toString().trim();
                if (!nuevoTelefono.isEmpty()) {
                    // Lógica para cambiar el teléfono en Firebase
                    // FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(...)
                    Toast.makeText(getContext(), "Teléfono actualizado correctamente", Toast.LENGTH_SHORT).show();
                    dialogo.dismiss();
                } else {
                    Toast.makeText(getContext(), "Por favor, ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogo.show();
    }

    private void mostrarDialogoContraseña() {
        dialogo = new Dialog(requireContext());
        dialogo.setContentView(R.layout.dialog_password);
        dialogo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Configurar el botón de cerrar
        ImageButton btnCerrar = dialogo.findViewById(R.id.btn_salirContraseña);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        // Configurar el botón de cambiar contraseña
        Button btnCambiarContraseña = dialogo.findViewById(R.id.btn_cambiarContraseña);
        EditText editTextNuevoContraseña = dialogo.findViewById(R.id.txt_nuevaContraseña);
        EditText editTextConfirmaContraseña = dialogo.findViewById(R.id.txt_confirmoContraseña);

        // Configurar la imagen del ojo para mostrar/ocultar contraseña
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
                    imgMostrarContraseña.setImageResource(R.drawable.baseline_remove_red_eye_24); // Cambiar icono a ojo cerrado
                } else {
                    // Mostrar contraseña
                    editTextNuevoContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextConfirmaContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                    imgMostrarContraseña.setImageResource(R.drawable.ojo); // Cambiar icono a ojo abierto
                }
            }
        });

        btnCambiarContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevaContraseña = editTextNuevoContraseña.getText().toString().trim();
                String confirmarContraseña = editTextConfirmaContraseña.getText().toString().trim();
                if (!nuevaContraseña.isEmpty() && !confirmarContraseña.isEmpty() && nuevaContraseña.equals(confirmarContraseña)) {
                    // Lógica para cambiar la contraseña en Firebase
                    // FirebaseAuth.getInstance().getCurrentUser().updatePassword(...)
                    Toast.makeText(getContext(), "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                    dialogo.dismiss();
                } else {
                    Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogo.show();
    }


}
