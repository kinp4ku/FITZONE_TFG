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

    private List<Integer> dias;
    private int diaHoy;

    public DaysAdapter(List<Integer> dias) {
        this.dias = dias;
        Calendar calendar = Calendar.getInstance();
        this.diaHoy = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fecha, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int day = dias.get(position);
        holder.textViewDay.setText(String.valueOf(day));

        // Cambiamos el color de fondo si el día coincide con el día actual
        if (day == diaHoy) {
            holder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDay = itemView.findViewById(R.id.nombreEjercicio);
        }
    }
}