package lopez.sanchez.fitzonetfg;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class HorariosFragment extends Fragment {
    private ArrayList<Horario> horarioArrayList;
    private HorarioAdapter horarioAdapter;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    RecyclerView recyclerViewDays, recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horarios, container, false);

        recyclerViewDays = view.findViewById(R.id.listaDias);
        recyclerView = view.findViewById(R.id.list);

        dias();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        horarioArrayList = new ArrayList<>();
        horarioAdapter = new HorarioAdapter(requireContext(), horarioArrayList, new HashSet<String>());

        recyclerView.setAdapter(horarioAdapter);

        db = FirebaseFirestore.getInstance();

        obtenerHorarioDesdeFirestore();

        return view;
    }

    public void dias() {
        recyclerViewDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Integer> daysOfMonth = generateDaysForCurrentMonth();
        DaysAdapter daysAdapter = new DaysAdapter(daysOfMonth);
        recyclerViewDays.setAdapter(daysAdapter);
        scrollToCurrentDay(daysOfMonth);
    }

    private void scrollToCurrentDay(List<Integer> daysOfMonth) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int index = daysOfMonth.indexOf(currentDay);
        if (index != -1) {
            recyclerViewDays.scrollToPosition(index);
            recyclerViewDays.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewDays.getLayoutManager();
                if (layoutManager != null) {
                    int screenWidth = recyclerViewDays.getWidth();
                    View childView = layoutManager.findViewByPosition(index);
                    if (childView != null) {
                        int itemWidth = childView.getWidth();
                        int scrollTo = childView.getLeft() - (screenWidth / 2 - itemWidth / 2);
                        recyclerViewDays.smoothScrollBy(scrollTo, 0);
                    }
                }
            });
        }
    }

    private List<Integer> generateDaysForCurrentMonth() {
        List<Integer> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        return days;
    }

    private void obtenerHorarioDesdeFirestore() {
        db.collection("Horarios")
                .orderBy("horaInicio", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore ERROR", "Error getting documents.", error);
                        progressDialog.dismiss();
                        return;
                    }

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Horario horario = dc.getDocument().toObject(Horario.class);
                            horario.setId(dc.getDocument().getId());
                            horarioArrayList.add(horario);
                            Log.d("Firestore DATA", "Horario añadido: " + horario.getNombreEjercicio());
                        } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            Horario horario = dc.getDocument().toObject(Horario.class);
                            horario.setId(dc.getDocument().getId());
                            actualizarHorarioEnLista(horario);
                            Log.d("Firestore DATA", "Horario modificado: " + horario.getNombreEjercicio());
                        }
                    }

                    horarioAdapter.notifyDataSetChanged();
                    Log.d("Firestore DATA", "Total horarios: " + horarioArrayList.size());

                    progressDialog.dismiss();
                });
    }

    private void actualizarHorarioEnLista(Horario horarioActualizado) {
        for (int i = 0; i < horarioArrayList.size(); i++) {
            if (horarioArrayList.get(i).getId().equals(horarioActualizado.getId())) {
                horarioArrayList.set(i, horarioActualizado);
                break;
            }
        }
    }
}
