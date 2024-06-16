package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {
    private Context context;
    private ArrayList<Ejercicio> ejercicioArrayList;
    private FirebaseStorage storage;

    public EjercicioAdapter(Context context, ArrayList<Ejercicio> ejercicioArrayList) {
        this.context = context;
        this.ejercicioArrayList = ejercicioArrayList;
        this.storage = FirebaseStorage.getInstance(); // Inicializamos Firebase Storage
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicioArrayList.get(position);

        holder.nombreEjercicio.setText(ejercicio.getNombreEjercicio());
        holder.trabajoEjercicio.setText(ejercicio.getTrabajoEjercicio());
        holder.repeticionesEjercicio.setText(ejercicio.getRepeticionesEjercicio());

        // Construimos la ruta de la imagen en Firebase Storage
        String nombreImagen = obtenerNombreImagen(ejercicio.getUrl_img());
        if (nombreImagen != null) {
            String rutaImagen = "img_ejercicios/" + nombreImagen;
            StorageReference storageRef = storage.getReference().child(rutaImagen);

            // Obtenemos la URI de la imagen para cargarla con Glide
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Cargamos la imagen usando Glide
                Glide.with(context)
                        .load(uri)
                        .into(holder.imagenEjercicio);
            });
        }
    }

    // Método para obtener el nombre de la imagen de la URL
    private String obtenerNombreImagen(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String[] parts = imageUrl.split("/");
            return parts[parts.length - 1]; // Última parte de la URL
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return ejercicioArrayList.size();
    }

    public void filtrarLista(ArrayList<Ejercicio> listaFiltrada) {
        ejercicioArrayList = listaFiltrada;
        notifyDataSetChanged();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreEjercicio;
        TextView trabajoEjercicio;
        TextView repeticionesEjercicio;
        ImageView imagenEjercicio;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicio);
            trabajoEjercicio = itemView.findViewById(R.id.trabajoEjercicio);
            repeticionesEjercicio = itemView.findViewById(R.id.repeticionesEjercicio);
            imagenEjercicio = itemView.findViewById(R.id.iconImageView);
        }
    }
}