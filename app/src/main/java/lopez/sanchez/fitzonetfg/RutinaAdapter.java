package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private Context context;
    private List<String> rutinaList;

    public RutinaAdapter(Context context, List<String> rutinaList) {
        this.context = context;
        this.rutinaList = rutinaList;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        String nombreRutina = rutinaList.get(position);
        holder.nombreRutinaTextView.setText(nombreRutina);
    }

    @Override
    public int getItemCount() {
        return rutinaList.size();
    }

    public static class RutinaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreRutinaTextView;

        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreRutinaTextView = itemView.findViewById(R.id.nombreRutinaTextView);
        }
    }
}
