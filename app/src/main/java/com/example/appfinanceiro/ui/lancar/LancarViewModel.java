package com.example.appfinanceiro.ui.lancar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appfinanceiro.AppFinanceiroApplication;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.repository.BaldeRepository;
import com.example.appfinanceiro.data.repository.TransacaoRepository;
import com.example.appfinanceiro.domain.LancamentoRapido;

import java.time.LocalDate;
import java.util.List;

public class LancarViewModel extends AndroidViewModel {

    private final TransacaoRepository transacoes;
    private final LiveData<List<Balde>> baldes;

    public LancarViewModel(@NonNull Application app) {
        super(app);
        AppFinanceiroApplication a = AppFinanceiroApplication.from(app);
        transacoes = a.transacaoRepository();
        BaldeRepository baldeRepo = a.baldeRepository();
        baldes = baldeRepo.observarAtivosPorFrequencia();
    }

    /** Ordenados por frequência de uso: o balde mais provável fica na frente. */
    public LiveData<List<Balde>> baldes() {
        return baldes;
    }

    public void lancar(long valorCentavos, long baldeId, LocalDate data, String descricao) {
        transacoes.inserir(LancamentoRapido.criar(valorCentavos, baldeId, data, descricao));
    }
}
