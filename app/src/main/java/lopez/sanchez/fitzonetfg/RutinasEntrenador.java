package lopez.sanchez.fitzonetfg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RutinasEntrenador extends Fragment {

    private RecyclerView recyclerViewRutinas;
    private RutinaAdapter rutinaAdapter;
    private List<String> nombreRutinas;
    private List<String> nombreRutinasFiltradas;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas_entrenador, container, false);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        recyclerViewRutinas = view.findViewById(R.id.recyclerViewRutinas);
        searchView = view.findViewById(R.id.searchView);
        recyclerViewRutinas.setLayoutManager(new LinearLayoutManager(requireContext()));
        nombreRutinas = new ArrayList<>();
        nombreRutinasFiltradas = new ArrayList<>();
        rutinaAdapter = new RutinaAdapter(requireContext(), nombreRutinas);
        recyclerViewRutinas.setAdapter(rutinaAdapter);

        db = FirebaseFirestore.getInstance();

        obtenerNombresRutinasDesdeFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarRutinas(newText);
                return true;
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> mostrarAgregarRutinaCard());
        return view;
    }

    private void obtenerNombresRutinasDesdeFirestore() {
        db.collection("Rutinas")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore ERROR", "Error getting documents.", error);
                        progressDialog.dismiss();
                        return;
                    }

                    nombreRutinas.clear();
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String nombreRutina = dc.getDocument().getId();
                            nombreRutinas.add(nombreRutina);
                        }
                    }

                    nombreRutinasFiltradas.addAll(nombreRutinas);

                    rutinaAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                });
    }

    private void filtrarRutinas(String searchText) {
        nombreRutinas.clear();
        if (TextUtils.isEmpty(searchText)) {
            nombreRutinas.addAll(nombreRutinasFiltradas);
        } else {
            for (String nombreRutina : nombreRutinasFiltradas) {
                if (nombreRutina.toLowerCase().contains(searchText.toLowerCase())) {
                    nombreRutinas.add(nombreRutina);
                }
            }
        }
        rutinaAdapter.notifyDataSetChanged();
    }

    private void mostrarAgregarRutinaCard() {
        AddRutinaCardFragment addRutinaCardFragment = new AddRutinaCardFragment(this);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_rutinaEntrenador, addRutinaCardFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            obtenerNombresRutinasDesdeFirestore();
        }
    }
}

