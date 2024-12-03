package com.example.donatec.inicio;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.donatec.R;
import com.example.donatec.SessionManager;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class InicioFragment extends Fragment {
    private ChipGroup chipGroupInicio;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<Donacion> ListaDonaciones;
    private DonacionAdapter adapter;
    private SessionManager sessionManager;
    private DonationUtility donationUtility;
    private RequestQueue requestQueue;
    private ChipUtility chipUtility;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Obtener la referencia al ChipGroup
        this.chipGroupInicio = view.findViewById(R.id.chipGroupInicio);

        // Obtener la referencia al SwipeRefreshLayout
        this.swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Obtener la referencia al RecyclerView
        this.recyclerView = view.findViewById(R.id.recycler_inicio);

        // Definir el layout manager como lista vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // inicializar la lista de donaciones
        this.ListaDonaciones = new ArrayList<>();

        // inicializar el adaptador del recycler con la lista de donaciones
        this.adapter = new DonacionAdapter(ListaDonaciones, getActivity());

        // Asignar el adaptador al RecyclerView
        this.recyclerView.setAdapter(adapter);

        // inicializando el session manager
        this.sessionManager = new SessionManager(getActivity());

        // Obtener la cola de solicitudes de Volley
        this.requestQueue = Volley.newRequestQueue(getActivity());

        // Crear una instancia de DonationUtility
        this.donationUtility = new DonationUtility(getActivity(), ListaDonaciones, adapter, requestQueue, sessionManager);

        // Llamar al método para obtener la lista de donaciones
        this.donationUtility.getDonations(getString(R.string.api_base_url) + "donations/" + sessionManager.getStateUser(), false);

        // inicializacion de chipUtility
        this.chipUtility = new ChipUtility(getActivity(), chipGroupInicio, requestQueue, sessionManager, donationUtility);

        // agregar evento de refrescar al SwipeRefreshLayout
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Acción a realizar al deslizar hacia abajo
                Toast.makeText(getContext(), chipUtility.getTextChipSelected() + ", actualizando...", Toast.LENGTH_SHORT).show();

                String chipText = chipUtility.getTextChipSelected();
                String categoryEnpoint = chipText.equals("Todas") ? "" : "/" + chipText;
                String endpoint = getActivity().getString(R.string.api_base_url) + "donations/" + sessionManager.getStateUser() + categoryEnpoint;

                // Llamar al método para obtener la lista de donaciones
                donationUtility.getDonations(endpoint, true);

                // Detener la animación de actualización
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

}