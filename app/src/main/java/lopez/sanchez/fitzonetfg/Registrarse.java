package lopez.sanchez.fitzonetfg;

import static lopez.sanchez.fitzonetfg.InicioSesion.getContext;
import static lopez.sanchez.fitzonetfg.InicioSesion.guardarValor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

public class Registrarse extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView, imagenVer, imagenPerfil;
    private EditText nombre, apellidos, dni, correo, contraseña, telefono;
    private RadioButton sexoM, sexoF, tipoU, tipoT;
    private FirebaseAuth mAuth;
    private Context contexto;
    Intent i;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mfirestore;
    private Button volver, registrarse;
    private int RC_SIGN_IN = 20;
    private String mensajeError="";
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private NfcAdapter nfcAdapter;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        imagenPerfil.setImageBitmap(bitmap);
                        imageView.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registarse);
        contexto = this;

        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mfirestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Verifica si el dispositivo soporta NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        initializeViews();
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this,
                            getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                            PendingIntent.FLAG_MUTABLE
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

    private void processNfcTag(Tag tag) {
        Log.d("NFC", "processNfcTag: Etiqueta NFC detectada");
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Log.e("NFC", "processNfcTag: Etiqueta NFC no compatible");
                Toast.makeText(this, "Etiqueta NFC no compatible", Toast.LENGTH_SHORT).show();
                return;
            }

            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage != null) {
                NdefRecord[] records = ndefMessage.getRecords();
                boolean tagFound = false;
                for (NdefRecord record : records) {
                    if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                        byte[] payload = record.getPayload();
                        String text = new String(payload, 1, payload.length - 1, StandardCharsets.UTF_8);
                        Log.d("NFC", "processNfcTag: Texto leído de la etiqueta NFC: " + text);

                        if ("enTECNICO".equalsIgnoreCase(text.trim())) {
                            Log.d("NFC", "processNfcTag: Se encontró etiqueta NFC para TECNICO");
                            Toast.makeText(this, "Etiqueta para TECNICO detectada", Toast.LENGTH_SHORT).show();
                            runOnUiThread(() -> {
                                tipoT.setChecked(true);
                                tipoU.setChecked(false);
                            });
                            tagFound = true;
                            break;
                        } else if ("enUSUARIO".equalsIgnoreCase(text.trim())) {
                            Log.d("NFC", "processNfcTag: Se encontró etiqueta NFC para USUARIO");
                            Toast.makeText(this, "Etiqueta para USUARIO detectada", Toast.LENGTH_SHORT).show();
                            runOnUiThread(() -> {
                                tipoU.setChecked(true);
                                tipoT.setChecked(false);
                            });
                            tagFound = true;
                            break;
                        }
                    } else {
                        Log.d("NFC", "processNfcTag: Tipo de registro no compatible o no RTD_TEXT");
                    }
                }
                if (!tagFound) {
                    Log.d("NFC", "processNfcTag: No se encontró información válida en la etiqueta NFC");
                    Toast.makeText(this, "No se encontró información válida en la etiqueta NFC", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("NFC", "processNfcTag: No se encontró información en la etiqueta NFC");
                Toast.makeText(this, "No se encontró información en la etiqueta NFC", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("NFC", "processNfcTag: Error al procesar la etiqueta NFC", e);
            Toast.makeText(this, "Error al procesar la etiqueta NFC", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        imageView = findViewById(R.id.img_camara);
        imagenVer = findViewById(R.id.imgVer);
        imagenPerfil = findViewById(R.id.img_fotoPerfil);
        nombre = findViewById(R.id.txt_nombre);
        apellidos = findViewById(R.id.txt_apellidos);
        dni = findViewById(R.id.txt_dni);
        correo = findViewById(R.id.txt_correo);
        telefono = findViewById(R.id.editTextPhone);
        contraseña = findViewById(R.id.txt_contraseñaUsu);
        sexoM = findViewById(R.id.rb_hombre);
        sexoF = findViewById(R.id.rb_mujer);
        tipoU = findViewById(R.id.rb_usuario);
        tipoT = findViewById(R.id.rb_tecnico);
        volver = findViewById(R.id.btnVolver2);
        registrarse = findViewById(R.id.b_registrarse);
    }

    public boolean reglasContraseña(String pass) {

        boolean cumple = true;
        int contadorMayus=0, contadorMinus=0, contadorNum=0, contadorSimbolo=0;
        char[] caracteres = pass.toCharArray();

        //Longitud mínima.
        if (!(caracteres.length >= 8)) {
            cumple = false;
        }

        for (char caracter : caracteres) {
            //Al menos un símbolo
            if (!(Character.isDigit(caracter) || Character.isLetter(caracter))) {
                contadorSimbolo++;
            }

            //Al menos un número
            if (Character.isDigit(caracter)) {
                contadorNum++;
            }

            //Al menos una mayúscula
            if (Character.isUpperCase(caracter)) {
                contadorMayus++;
            }

            //Al menos una minúscula
            if (Character.isLowerCase(caracter)) {
                contadorMinus++;
            }
        }

        if(contadorMayus==0 || contadorMinus==0 || contadorNum==0 || contadorSimbolo==0){
            cumple = false;
        }

        return cumple;
    }

    private boolean validarTelefono(String tel){
        Pattern pattern = Pattern.compile("^[0-9]{9}$");
        Pattern pattern2 = Pattern.compile("^[+]?[0-9]{10,13}$");
        return !TextUtils.isEmpty(tel) && (pattern.matcher(tel).matches() || pattern2.matcher(tel).matches());
    }

    private boolean validarEmail(String emailUsuario){
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        return !TextUtils.isEmpty(emailUsuario) && pattern.matcher(emailUsuario).matches();
    }

    private boolean validarDNI(String dni)
    {
        if(dni.length() != 9) //Si el DNI tiene algo distinto a 9 caracteres no es valido
        {
            return false;
        }
        else if(!Character.isLetter(dni.substring(8).charAt(0))) // Si el último caracter no es una letra no es valido
        {
            return false;
        }
        else if(!dni.substring(0,8).matches("-?\\d+(\\.\\d+)?")) //Si algun caracter dentro de los numeros no es un numero no es valido
        {
            return false;
        }
        String letra = dni.substring(8).toUpperCase();
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";

        String numeros = dni.substring(0, 8);
        int numerosInt = Integer.parseInt(numeros);
        int resto = numerosInt % 23;
        String letraCorrecta = letrasValidas.substring(resto, resto+1);

        return letraCorrecta.equals(letra);
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

    private void setClickListeners() {
        imagenPerfil.setOnClickListener(v -> openGallery());
        imagenVer.setOnClickListener(new View.OnClickListener() {
            boolean passwordVisible = false;

            @Override
            public void onClick(View v) {
                if (passwordVisible) {
                    // Ocultar contraseña
                    contraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                    imagenVer.setImageResource(R.drawable.ojo); // Cambiar icono a ojo abierto
                } else {
                    // Mostrar contraseña
                    contraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                    imagenVer.setImageResource(R.drawable.baseline_remove_red_eye_24); // Cambiar icono a ojo cerrado
                }
            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volver();
            }
        });
        //google.setOnClickListener(v -> googleSignIn());

        //   registrarse.setOnClickListener(v -> guardarDatos());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Selecciona una imagen"));
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean guardarDatos() {
        String nombreUsuario = nombre.getText().toString().trim();
        String apellidosUsuario = apellidos.getText().toString().trim();
        String dniUsuario = dni.getText().toString().trim();
        String emailUsuario = correo.getText().toString().trim();
        String telefonoUsuario = telefono.getText().toString().trim();
        String hash;
        String contraseñaUsuario = contraseña.getText().toString();
        String sexoUsuario = sexo();
        String tipoUsuario = tipo();


        Boolean cumpleTodo = false;

        if(nombreUsuario.isEmpty() || emailUsuario.isEmpty() || contraseñaUsuario.isEmpty() || sexoUsuario.isEmpty() || tipoUsuario.isEmpty()) {
            mensajeError="Rellenar los campos";
        }else if(!validarDNI(dniUsuario)) {
            mensajeError = "DNI no válido";
        }else if(!validarEmail(emailUsuario)) {
                mensajeError="Correo electrónico inexistente";
        }else if (!reglasContraseña(contraseñaUsuario)) {
            mensajeError="Contraseña inválida.\nIntroduzca al menos una mayúscula, una minúscula, un número y un símbolo. \nLongitud mínima de 8 caracteres";
        }else if(!validarTelefono(telefonoUsuario)){
            mensajeError="Por favor, introduzca un teléfono válido.";
        }else if(sexoUsuario.equals("No seleccionado")) {
            mensajeError = "Por favor, introduzca su sexo.";
        }else{
            subirImagen(emailUsuario);
            hash = hashContra(contraseñaUsuario);
            String rutaImagen="foto_"+ emailUsuario;
            postUsuario(nombreUsuario, apellidosUsuario, dniUsuario, emailUsuario, telefonoUsuario, hash, sexoUsuario, tipoUsuario, rutaImagen);
            cumpleTodo = true;
        }
        return cumpleTodo;
    }
    private void subirImagen(String emailUsuario) {
        if (selectedImageUri != null) {
            // Subir la imagen seleccionada
            StorageReference reference = storageReference.child("img_perfiles/foto_"+ emailUsuario);
            reference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Registrarse.this, "No se ha podido subir la imagen a la BBDD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Si selectedImageUri es null, cargar y subir la imagen predeterminada
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imagen_pred);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Subir la imagen predeterminada
            StorageReference reference = storageReference.child("img_perfiles/foto_"+ emailUsuario);
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Registrarse.this, "No se ha podido subir la imagen predeterminada a la BBDD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void postUsuario(String nombreUsuario, String apellidosUsuario, String dniUsuario, String emailUsuario, String telefonoUsuario, String contraseñaUsuario, String sexoUsuario, String tipoUsuario, String fotoPerfil) {
        Map<String, Object> map = new HashMap<>();
        map.put("NOMBRE", nombreUsuario);
        map.put("APELLIDOS", apellidosUsuario);
        map.put("DNI", dniUsuario);
        map.put("EMAIL", emailUsuario);
        map.put("TELEFONO", telefonoUsuario);
        map.put("CONTRASEÑA", contraseñaUsuario);
        map.put("SEXO", sexoUsuario);
        map.put("TIPO", tipoUsuario);
        map.put("FOTO", fotoPerfil);

        mfirestore.collection("infoUsuarios")
                .add(map)
                .addOnSuccessListener(documentReference -> {
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

    private void Inicio() {
        Intent intent = new Intent(this, InicioApp.class);
        startActivity(intent);
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
                        guardarValor(getContext(), "mail", "");
                        guardarValor(getContext(), "pass", "");
                        Inicio();
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setSign(View v) {
        mfirestore.collection("infoUsuarios").whereEqualTo("EMAIL", correo.getText().toString()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> taskEmail) {
                        if (taskEmail.isSuccessful()) {
                            if (taskEmail.getResult().isEmpty()) {
                                if (guardarDatos()) {
                                    if (!correo.getText().toString().isEmpty() && !contraseña.getText().toString().isEmpty()) {
                                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo.getText().toString(), hashContra(contraseña.getText().toString())).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(contexto, "Usuario creado correctamente." , Toast.LENGTH_SHORT).show();
                                                    guardarValor(getContext(), "mail", "");
                                                    guardarValor(getContext(), "pass", "");
                                                    Inicio();
                                                } else {
                                                    if (mensajeError.equals("")) {
                                                        mensajeError = "Error de registro.";
                                                    }
                                                    showAlert(mensajeError);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    showAlert(mensajeError);
                                }
                            }else{
                                showAlert("Ya existe una cuenta con este correo electrónico.");
                            }
                        }
                    }
                });
    }

    public void showAlert(String mns) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(mns);
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void volver(){
        i = new Intent(Registrarse.this, InicioApp.class);
        startActivity(i);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // No llamear a super.onBackPressed() para deshabilitar el botón de volver
    }
}