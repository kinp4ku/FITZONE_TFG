package lopez.sanchez.fitzonetfg;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.ViewHolder> {

    private List<Integer> days;
    private int currentDay;

    public DaysAdapter(List<Integer> days) {
        this.days = days;
        Calendar calendar = Calendar.getInstance();
        this.currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fecha, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int day = days.get(position);
        holder.textViewDay.setText(String.valueOf(day));

        // Cambiar el color de fondo si el día coincide con el día actual
        if (day == currentDay) {
            holder.itemView.setBackgroundColor(Color.BLUE); // Cambiar a cualquier color deseado
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Cambiar a cualquier color deseado
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDay = itemView.findViewById(R.id.nombreEjercicio);
        }
    }
}