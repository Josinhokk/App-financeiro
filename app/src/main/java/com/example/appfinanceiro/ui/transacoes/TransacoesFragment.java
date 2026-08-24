package com.example.appfinanceiro.ui.transacoes;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfinanceiro.R;

/** Lançamentos do mês de competência corrente, agrupados por dia. */
public class TransacoesFragment extends Fragment {

    public TransacoesFragment() {
        super(R.layout.fragment_transacoes);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);

        TransacoesViewModel vm = new ViewModelProvider(this).get(TransacoesViewModel.class);

        RecyclerView lista = v.findViewById(R.id.lista_transacoes);
        View vazio = v.findViewById(R.id.estado_vazio);

        TransacaoAdapter adapter = new TransacaoAdapter();
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        vm.linhas().observe(getViewLifecycleOwner(), linhas -> {
            adapter.submitList(linhas);
            boolean semLancamentos = linhas == null || linhas.isEmpty();
            vazio.setVisibility(semLancamentos ? View.VISIBLE : View.GONE);
            lista.setVisibility(semLancamentos ? View.GONE : View.VISIBLE);
        });
    }
}
