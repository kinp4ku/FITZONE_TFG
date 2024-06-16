package lopez.sanchez.fitzonetfg;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReiniciarPlazasServicio extends JobService{
    private static final String TAG = "ReiniciarPlazasServicio";
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        reiniciarPlazas(params);
        return true;
    }

    private void reiniciarPlazas(JobParameters params) {
        db = FirebaseFirestore.getInstance();
        db.collection("Horarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Horario horario = document.toObject(Horario.class);
                        if (horario != null) {
                            horario.setPlazasDisponibles(horario.getMaximoPlazas());

                            db.collection("Horarios").document(document.getId())
                                    .update("plazasDisponibles", horario.getMaximoPlazas());

                            // Eliminamos las reservas de los usuarios
                            db.collection(horario.getClaseReserva()).document(horario.getHoraInicio() + "-" + horario.getHoraFin())
                                    .update("reservaUsuario", new ArrayList<String>())
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Reservas eliminadas para " + horario.getId()))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar reservas: ", e));
                        }
                    }
                    Log.d(TAG, "Todas las plazas y reservas han sido reiniciadas.");
                    jobFinished(params, false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al reiniciar plazas: ", e);
                    jobFinished(params, true); // Reschedule job
                });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}