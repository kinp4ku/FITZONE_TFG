package lopez.sanchez.fitzonetfg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Horarios extends AppCompatActivity {
    List<listElement> elements;
    RecyclerView recyclerViewDays, recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

       dias();


        init();
    }

    public void dias(){
        recyclerViewDays = findViewById(R.id.listaDias);
        recyclerViewDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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

        ListAdapter listAdapter = new ListAdapter(elements, this);
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }
    public void Reservar (View v){
        // Código para abrir la otra actividad
        Intent intent = new Intent(this, Zumba.class);
        startActivity(intent);
    }

}