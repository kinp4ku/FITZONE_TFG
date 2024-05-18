package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        this.storage = FirebaseStorage.getInstance(); // Inicializar Firebase Storage
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

        holder.nombreE.setText(ejercicio.getNombreEjercicio());
        holder.trabajoE.setText(ejercicio.getTrabajoEjercicio());
        holder.repeticionesE.setText(ejercicio.getRepeticionesEjercicio());

        // Construir la ruta de la imagen en Firebase Storage
        String nombreImagen = obtenerNombreImagen(ejercicio.getUrl_img());
        if (nombreImagen != null) {
            String rutaImagen = "img_ejercicios/" + nombreImagen;
            StorageReference storageRef = storage.getReference().child(rutaImagen);

            // Obtener la URI de la imagen para cargarla con Glide
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Cargar la imagen usando Glide
                    Glide.with(context)
                            .load(uri)
                            .into(holder.img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Manejar errores al obtener la URL de la imagen
                    Log.e("EjercicioAdapter", "Error al obtener la URL de la imagen:", e);
                }
            });
        }
    }

    // Método para obtener el nombre de la imagen de la URL
    private String obtenerNombreImagen(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Ejemplo de implementación para obtener el nombre de la imagen de la URL
            // Puedes ajustar esta lógica según el formato de tus URLs de imagen
            String[] parts = imageUrl.split("/");
            String filename = parts[parts.length - 1]; // Última parte de la URL
            return filename.replace(".jpg", ""); // Eliminar la extensión
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return ejercicioArrayList.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreE, trabajoE, repeticionesE;
        ImageView img;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreE = itemView.findViewById(R.id.nombreEjercicio);
            trabajoE = itemView.findViewById(R.id.Trabajo);
            repeticionesE = itemView.findViewById(R.id.Repeticiones);
            img = itemView.findViewById(R.id.iconImageView);
        }
    }
}

