package lopez.sanchez.fitzonetfg;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<Usuario> usuariosList;
    private List<Boolean> usuariosSeleccionados;

    public UserAdapter(Context context, List<Usuario> userList, List<Boolean> selectedUsers) {
        this.context = context;
        this.usuariosList = userList;
        this.usuariosSeleccionados = selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario user = usuariosList.get(position);
        holder.userNameTextView.setText(user.getNombre());
        holder.userApellidosTextView.setText(user.getApellidos());
        holder.userCheckBox.setChecked(usuariosSeleccionados.get(position));

        holder.userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            usuariosSeleccionados.set(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return usuariosList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userApellidosTextView;
        CheckBox userCheckBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name);
            userCheckBox = itemView.findViewById(R.id.user_checkbox);
            userApellidosTextView= itemView.findViewById(R.id.user_surname);
        }
    }
}
