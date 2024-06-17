package lopez.sanchez.fitzonetfg;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class InicioSesion extends AppCompatActivity {

    private Intent i;
    private static Context contexto;
    private EditText contra, email;
    private Button login, volver;
    private CheckBox cb;
    private TextView titulo;
    private ImageView imagenMostrarContraseña;
    private static String PREFS_KEY = "Preferencias";
    private NfcAdapter nfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
        contexto = getApplicationContext();

        login = findViewById(R.id.loginButton);
        volver = findViewById(R.id.btnVolver);
        email = findViewById(R.id.ETTextEmail);
        contra = findViewById(R.id.ETTextPassword);
        cb = findViewById(R.id.cbMantener);
        titulo = findViewById(R.id.txt_inicioSesion);
        imagenMostrarContraseña = findViewById(R.id.imgMostrarContra);

        // Mostrar u ocultar contraseña
        imagenMostrarContraseña.setOnClickListener(new View.OnClickListener() {
            boolean passwordVisible = false;
            @Override
            public void onClick(View v) {
                if (passwordVisible) {
                    contra.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                    imagenMostrarContraseña.setImageResource(R.drawable.ojo);
                } else {
                    contra.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                    imagenMostrarContraseña.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
            }
        });

        // Animaciones
        Animation animacionArriba = AnimationUtils.loadAnimation(this, R.anim.arriba);
        Animation trans = AnimationUtils.loadAnimation(this, R.anim.transparencia);
        ImageView circulo = findViewById(R.id.circulo);
        ImageView circulito = findViewById(R.id.circulito);
        circulo.setAnimation(animacionArriba);
        circulito.setAnimation(trans);

        email.setText("");
        contra.setText("");
        cb.setChecked(true);
        email.setText(leerValor(contexto, "mail"));
        contra.setText(leerValor(contexto, "pass"));

        // Ocultar elementos
        cb.setVisibility(View.INVISIBLE);
        email.setVisibility(View.INVISIBLE);
        contra.setVisibility(View.INVISIBLE);
        circulito.setVisibility(View.INVISIBLE);
        login.setVisibility(View.INVISIBLE);
        titulo.setVisibility(View.INVISIBLE);
        volver.setVisibility(View.INVISIBLE);
        imagenMostrarContraseña.setVisibility(View.INVISIBLE);

        // Mostrar elementos después de un retardo
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            cb.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            contra.setVisibility(View.VISIBLE);
            circulito.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            titulo.setVisibility(View.VISIBLE);
            imagenMostrarContraseña.setVisibility(View.VISIBLE);
            volver.setVisibility(View.VISIBLE);
        }, 2000);

        // Verificamos si hay credenciales guardados
        if (!leerValor(contexto, "mail").equals("")) {
            cb.setChecked(true);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), contra.getText().toString()).addOnCompleteListener(this, task -> {
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
            });
        } else {
            cb.setChecked(false);
        }

        // Inicializamos NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_SHORT).show();
            return;
        }

        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Error al añadir MIME type", e);
        }

        handleNfcIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
            );

            IntentFilter[] intentFiltersArray = new IntentFilter[] {
                    new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                    new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            };

            String[][] techListsArray = new String[][] {
                    new String[] { NfcA.class.getName() },
                    new String[] { NfcB.class.getName() },
                    new String[] { NfcF.class.getName() },
                    new String[] { NfcV.class.getName() },
                    new String[] { Ndef.class.getName() }
            };

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            Log.d("NFC", "onResume: Foreground dispatch habilitado");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            Log.d("NFC", "onPause: Foreground dispatch deshabilitado");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("NFC", "onNewIntent: Intent NFC detectado");

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Log.d("NFC", "onNewIntent: Etiqueta NFC detectada");
                processNfcTag(tag);
            } else {
                Log.e("NFC", "onNewIntent: No se pudo obtener la etiqueta NFC");
                Toast.makeText(this, "No se pudo obtener la etiqueta NFC", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                processNfcTag(tag);
            } else {
                Toast.makeText(this, "No se pudo obtener la etiqueta NFC", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processNfcTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Toast.makeText(this, "NFC no soporta NDEF", Toast.LENGTH_SHORT).show();
                return;
            }

            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage != null) {
                for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
                    if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                            Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                        byte[] payload = ndefRecord.getPayload();
                        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                        int languageCodeLength = payload[0] & 0x3F;
                        String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

                        // Asumimos que el texto contiene "email:contraseña"
                        String[] credentials = text.split(":");
                        if (credentials.length == 2) {
                            email.setText(credentials[0]);
                            contra.setText(credentials[1]);

                            // Autenticar usuario automáticamente
                            login.performClick();
                        } else {
                            Toast.makeText(this, "Formato de NFC incorrecto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            ndef.close();
        } catch (Exception e) {
            Log.e("NFC", "Error al leer la etiqueta NFC", e);
            Toast.makeText(this, "Error al leer la etiqueta NFC", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashContra(String contra){
        StringBuilder hexHash = null;
        try {
            // Obtenemos una instancia del algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convertimos la contraseña a bytes
            byte[] passwordBytes = contra.getBytes(StandardCharsets.UTF_8);

            // Calculamos el hash
            byte[] hashBytes = digest.digest(passwordBytes);

            // Convertimos el hash a hexadecimal
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
        editor.apply();
    }

    public static String leerValor(Context context, String keyPref) {
        SharedPreferences acceso = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  acceso.getString(keyPref, "");
    }

    public static Context getContext() {
        return contexto;
    }

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
        i.putExtra("contraseña", hashContra(contra.getText().toString()));
        startActivity(i);
        email.setText("");
        contra.setText("");
    }

    public void usuario(){
        i = new Intent(InicioSesion.this, PantallaInicio.class);
        i.putExtra("email", email.getText().toString());
        i.putExtra("contraseña", hashContra(contra.getText().toString()));
        startActivity(i);
        email.setText("");
        contra.setText("");
    }

    public void volver(View v){
        i = new Intent(InicioSesion.this, InicioApp.class);
        startActivity(i);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // No llamar a super.onBackPressed() para deshabilitar el botón de volver
    }
}