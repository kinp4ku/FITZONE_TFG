package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {
    private Set<String> horariosReservados = new HashSet<>();
    private Context context;
    private ArrayList<Horario> horarioArrayList;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HorarioAdapter(Context context, ArrayList<Horario> horarioArrayList, Set<String> horariosReservados) {
        this.context = context;
        this.horarioArrayList = horarioArrayList;
        this.horariosReservados = horariosReservados;
        this.storage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        Horario horario = horarioArrayList.get(position);

        holder.nombreC.setText(horario.getNombreEjercicio());
        holder.horaI.setText(horario.getHoraInicio());
        holder.horaF.setText(horario.getHoraFin());

        // Verificar si las plazas disponibles son mayores al máximo
        if (horario.getPlazasDisponibles() > horario.getMaximoPlazas()) {
            horario.setPlazasDisponibles(horario.getMaximoPlazas());
        }

        // Cambiar el color y texto si la clase está completa
        if (horario.getPlazasDisponibles() <= 0) {
            holder.plazasDisponibles.setTextColor(Color.RED);
            holder.plazasDisponibles.setText("COMPLETA");
            holder.reservaCheckBox.setEnabled(false);
        } else {
            holder.plazasDisponibles.setTextColor(Color.BLACK);
            holder.plazasDisponibles.setText("Disponibles: " + horario.getPlazasDisponibles());
            holder.reservaCheckBox.setEnabled(true);
        }

        holder.reservaCheckBox.setOnCheckedChangeListener(null);
        holder.reservaCheckBox.setChecked(horariosReservados.contains(horario.getId()));

        holder.reservaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != horariosReservados.contains(horario.getId())) {
                if (isChecked) {
                    reservarClase(horario, holder);
                } else {
                    cancelarReservaClase(horario, holder);
                }
            }
        });

        // Cargar imagen usando Glide
        String nombreImagen = obtenerNombreImagen(horario.getUrl_img());
        if (nombreImagen != null) {
            String rutaImagen = "img_clases/" + nombreImagen;
            StorageReference storageRef = storage.getReference().child(rutaImagen);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context).load(uri).into(holder.img)).addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al obtener la URL de la imagen:", e));
        }

        // Verificar si el usuario ha reservado esta clase
        verificarReservaUsuario(horario, holder);
    }

    private void reservarClase(Horario horario, HorarioViewHolder holder) {
        String userEmail = auth.getCurrentUser().getEmail();
        db.collection("Horarios").document(horario.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int plazasDisponibles = documentSnapshot.getLong("plazasDisponibles").intValue();
                        if (plazasDisponibles > 0) {
                            horario.decrementarPlazas();
                            holder.plazasDisponibles.setText("Disponibles: " + horario.getPlazasDisponibles());
                            horariosReservados.add(horario.getId());
                            guardarReservaEnBaseDeDatos(horario);
                            guardarReservaEnColeccion(horario, userEmail);
                        } else {
                            holder.reservaCheckBox.setChecked(false);
                        }
                    } else {
                        Log.d("HorarioAdapter", "No se encontró el documento para el horario " + horario.getId());
                    }
                })
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al obtener el número de plazas disponibles", e));
    }

    private void cancelarReservaClase(Horario horario, HorarioViewHolder holder) {
        String userEmail = auth.getCurrentUser().getEmail();
        horario.incrementarPlazas();
        holder.plazasDisponibles.setText("Disponibles: " + horario.getPlazasDisponibles());
        horariosReservados.remove(horario.getId());
        eliminarReservaDeBaseDeDatos(horario);
        eliminarReservaDeColeccion(horario, userEmail);
    }

    private void guardarReservaEnBaseDeDatos(Horario horario) {
        String userEmail = auth.getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("Horarios").document(horario.getId());
        docRef.update("plazasDisponibles", horario.getPlazasDisponibles())
                .addOnSuccessListener(aVoid -> Log.d("HorarioAdapter", "Reserva guardada en base de datos"))
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al guardar reserva", e));

        db.collection("infoUsuarios").document(userEmail).update("reservas", FieldValue.arrayUnion(horario.getId()));
    }

    private void eliminarReservaDeBaseDeDatos(Horario horario) {
        String userEmail = auth.getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("Horarios").document(horario.getId());
        docRef.update("plazasDisponibles", horario.getPlazasDisponibles())
                .addOnSuccessListener(aVoid -> Log.d("HorarioAdapter", "Reserva eliminada de la base de datos"))
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al eliminar reserva", e));

        db.collection("infoUsuarios").document(userEmail).update("reservas", FieldValue.arrayRemove(horario.getId()));
    }

    private void guardarReservaEnColeccion(Horario horario, String userEmail) {
        DocumentReference docRef = db.collection(horario.getClaseReserva()).document(horario.getHoraInicio() + "-" + horario.getHoraFin());
        docRef.update("reservaUsuario", FieldValue.arrayUnion(userEmail))
                .addOnSuccessListener(aVoid -> Log.d("HorarioAdapter", "Reserva guardada en " + horario.getClaseReserva()))
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al guardar reserva en " + horario.getClaseReserva(), e));
    }

    private void eliminarReservaDeColeccion(Horario horario, String userEmail) {
        DocumentReference docRef = db.collection(horario.getClaseReserva()).document(horario.getHoraInicio() + "-" + horario.getHoraFin());
        docRef.update("reservaUsuario", FieldValue.arrayRemove(userEmail))
                .addOnSuccessListener(aVoid -> Log.d("HorarioAdapter", "Reserva eliminada de " + horario.getClaseReserva()))
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al eliminar reserva de " + horario.getClaseReserva(), e));
    }

    private void verificarReservaUsuario(Horario horario, HorarioViewHolder holder) {
        String userEmail = auth.getCurrentUser().getEmail();
        String claseReserva = horario.getClaseReserva();
        String documentId = horario.getHoraInicio() + "-" + horario.getHoraFin();

        db.collection(claseReserva).document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object reservaUsuarioObj = documentSnapshot.get("reservaUsuario");
                        if (reservaUsuarioObj instanceof ArrayList) {
                            ArrayList<String> reservaUsuario = (ArrayList<String>) reservaUsuarioObj;
                            if (reservaUsuario.contains(userEmail)) {
                                horariosReservados.add(horario.getId());
                                holder.reservaCheckBox.setChecked(true);
                            } else {
                                horariosReservados.remove(horario.getId());
                                holder.reservaCheckBox.setChecked(false);
                            }
                        } else {
                            Log.e("HorarioAdapter", "El campo reservaUsuario no es una lista: " + reservaUsuarioObj);
                            horariosReservados.remove(horario.getId());
                            holder.reservaCheckBox.setChecked(false);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HorarioAdapter", "Error al verificar reserva del usuario", e));
    }


    private String obtenerNombreImagen(String urlImg) {
        if (urlImg == null || urlImg.isEmpty()) {
            return null;
        }
        String[] partes = urlImg.split("/");
        return partes.length > 0 ? partes[partes.length - 1] : null;
    }

    @Override
    public int getItemCount() {
        return horarioArrayList.size();
    }

    public static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreC, horaI, horaF, plazasDisponibles;
        ImageView img;
        CheckBox reservaCheckBox;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreC = itemView.findViewById(R.id.nombreEjercicio);
            horaI = itemView.findViewById(R.id.horaInicio);
            horaF = itemView.findViewById(R.id.horaFin);
            plazasDisponibles = itemView.findViewById(R.id.plazasDisponibles);
            img = itemView.findViewById(R.id.iconImageView);
            reservaCheckBox = itemView.findViewById(R.id.reservaCheckBox);
        }
    }
}
