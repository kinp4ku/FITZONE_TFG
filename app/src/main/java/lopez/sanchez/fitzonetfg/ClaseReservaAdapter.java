package lopez.sanchez.fitzonetfg;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClaseReservaAdapter extends RecyclerView.Adapter<ClaseReservaAdapter.ClaseViewHolder> {

    private static final String TAG = "ClaseReservaAdapter";

    private List<Map<String, Object>> listaClases;
    private Context context;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    public ClaseReservaAdapter(Context context, List<Map<String, Object>> listaClases) {
        this.context = context;
        this.listaClases = listaClases;
        this.storage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ClaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva_clases, parent, false);
        return new ClaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaseViewHolder holder, int position) {
        Map<String, Object> clase = listaClases.get(position);
        holder.bind(clase);
    }

    @Override
    public int getItemCount() {
        return listaClases.size();
    }

    public class ClaseViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewNombre;
        private ImageView imageViewClase;
        private Button showEmailsButton;

        public ClaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.nombreEjercicio);
            imageViewClase = itemView.findViewById(R.id.iconImageView);
            showEmailsButton = itemView.findViewById(R.id.showEmailsButton);
        }

        public void bind(Map<String, Object> clase) {
            textViewNombre.setText(clase.get("nombreClase").toString());
            String imageUrl = clase.get("url_img").toString();
            cargarImagenDesdeStorage(imageUrl);

            String nombreClase = clase.get("nombreClase").toString();
            String documentId = clase.get("horaInicio").toString() + "-" + clase.get("horaFin").toString();

            showEmailsButton.setOnClickListener(v -> cargarCorreos(nombreClase, documentId));
        }

        private void cargarImagenDesdeStorage(String imageUrl) {
            String nombreImagen = obtenerNombreImagen(imageUrl);
            if (nombreImagen != null) {
                String rutaImagen = "img_clases/" + nombreImagen;
                StorageReference storageRef = storage.getReference().child(rutaImagen);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context).load(uri).into(imageViewClase);
                }).addOnFailureListener(e -> Log.e(TAG, "Error al obtener la URL de la imagen:", e));
            }
        }

        private String obtenerNombreImagen(String url) {
            return url.substring(url.lastIndexOf('/') + 1);
        }

        private void cargarCorreos(String nombreClase, String documentId) {
            db.collection(nombreClase).document(documentId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Object reservaUsuarioObj = documentSnapshot.get("reservaUsuario");
                            if (reservaUsuarioObj instanceof ArrayList) {
                                ArrayList<String> reservaUsuario = (ArrayList<String>) reservaUsuarioObj;
                                mostrarEmailsEnDialog(reservaUsuario);
                            } else {
                                Log.e(TAG, "El campo reservaUsuario no es una lista: " + reservaUsuarioObj);
                                Toast.makeText(context, "Error al cargar los correos de reserva para la clase " + nombreClase, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error al cargar los correos de reserva para la clase " + nombreClase, e));
        }

        private void mostrarEmailsEnDialog(List<String> listaEmails) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Correos Electrónicos Reservados");

            View view = LayoutInflater.from(context).inflate(R.layout.item_lista_reservas, null);
            ListView listViewEmails = view.findViewById(R.id.listViewEmails);

            if (listaEmails.isEmpty()) {
                List<String> noReservas = new ArrayList<>();
                noReservas.add("NO HAY RESERVAS");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, noReservas);
                listViewEmails.setAdapter(adapter);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, listaEmails);
                listViewEmails.setAdapter(adapter);
            }

            builder.setView(view);
            builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
    }
}