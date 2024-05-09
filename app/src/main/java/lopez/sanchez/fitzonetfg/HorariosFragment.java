package lopez.sanchez.fitzonetfg;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class HorariosFragment extends Fragment {
    List<listElement> elements;
    RecyclerView recyclerViewDays, recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horarios, container, false);

        recyclerViewDays = view.findViewById(R.id.listaDias); // Asigna la vista correctamente
        recyclerView = view.findViewById(R.id.list); // Asigna la vista del RecyclerView

        dias(); // Llama a días después de asignar recyclerViewDays
        init(); // Llama a init después de asignar recyclerView

        return view;
    }
    public void dias(){
        recyclerViewDays = recyclerViewDays.findViewById(R.id.listaDias);

        recyclerViewDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Integer> daysOfMonth = generateDaysForCurrentMonth();
        DaysAdapter daysAdapter = new DaysAdapter(daysOfMonth);
        recyclerViewDays.setAdapter(daysAdapter);
        scrollToCurrentDay(daysOfMonth);
    }

    //método para poder centrar el número del día en el que estamos.
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
    public void init() {
        elements = new ArrayList<>();
        elements.add(new listElement(R.drawable.zumba, "Zumba", "8:00", "8:45"));
        elements.add(new listElement(R.drawable.zumba, "Yoga", "8:45", "9:30"));
        elements.add(new listElement(R.drawable.zumba, "Ciclo Indoor", "10:00", "10:45"));
        elements.add(new listElement(R.drawable.zumba, "Pilates", "11:15", "12:00"));
        elements.add(new listElement(R.drawable.zumba, "Estiramientos", "13:00", "14:00"));
        elements.add(new listElement(R.drawable.zumba, "Zumba", "17:00", "17:45"));
        elements.add(new listElement(R.drawable.zumba, "Body Pump", "18:00", "18:45"));
        elements.add(new listElement(R.drawable.zumba, "Body Combat", "19:00", "19:45"));
        elements.add(new listElement(R.drawable.zumba, "CORE", "21:00", "21:45"));

        ListAdapter listAdapter = new ListAdapter(elements, getContext());

        recyclerView = recyclerView.findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listAdapter);
    }
}