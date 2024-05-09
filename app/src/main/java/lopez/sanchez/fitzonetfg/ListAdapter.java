package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{

    private List<listElement> mData;
    private LayoutInflater mInflater;
    private Context context;

    public ListAdapter(List<listElement> itemList, Context context){
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = itemList;
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.item_day, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position){
        holder.bindData(mData.get(position));
    }

    public void setItems(List<listElement> items){
        mData = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iconImage;
        TextView name, horaI, horaF;

        ViewHolder(View itemView){
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImageView); // Actualizado aquí
            name = itemView.findViewById(R.id.nombreEjercicio);
            horaI = itemView.findViewById(R.id.Repeticiones);
            horaF = itemView.findViewById(R.id.Trabajo);
        }

        void bindData(final listElement item){
            iconImage.setImageResource(item.getColor()); // Actualizado aquí
            name.setText(item.getName());
            horaI.setText(item.getHoraInicio());
            horaF.setText(item.getHoraFin());
        }
    }
}