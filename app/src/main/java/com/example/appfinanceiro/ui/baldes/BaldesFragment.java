package com.example.appfinanceiro.ui.baldes;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfinanceiro.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

/** Lista dos baldes ativos. Tocar num item edita; o FAB cria. */
public class BaldesFragment extends Fragment {

    public BaldesFragment() {
        super(R.layout.fragment_baldes);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);

        BaldesViewModel vm = new ViewModelProvider(this).get(BaldesViewModel.class);

        RecyclerView lista = v.findViewById(R.id.lista_baldes);
        View vazio = v.findViewById(R.id.estado_vazio);
        ExtendedFloatingActionButton fab = v.findViewById(R.id.fab_novo_balde);

        BaldeAdapter adapter = new BaldeAdapter(balde -> abrirFormulario(balde.getId()));
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        vm.baldes().observe(getViewLifecycleOwner(), baldes -> {
            adapter.submitList(baldes);
            boolean semBaldes = baldes == null || baldes.isEmpty();
            vazio.setVisibility(semBaldes ? View.VISIBLE : View.GONE);
            lista.setVisibility(semBaldes ? View.GONE : View.VISIBLE);
        });

        fab.setOnClickListener(x -> abrirFormulario(BaldeFormFragment.ID_NOVO));
    }

    private void abrirFormulario(long baldeId) {
        Bundle args = new Bundle();
        args.putLong(BaldeFormFragment.ARG_BALDE_ID, baldeId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_baldesFragment_to_baldeFormFragment, args);
    }
}
