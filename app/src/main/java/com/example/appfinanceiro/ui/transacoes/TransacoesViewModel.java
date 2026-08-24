package com.example.appfinanceiro.ui.transacoes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.appfinanceiro.AppFinanceiroApplication;
import com.example.appfinanceiro.domain.AgrupamentoPorData;
import com.example.appfinanceiro.domain.ItemLista;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Lista o mês de competência corrente. Competência e não caixa: a pergunta que
 * a tela responde é "o que eu decidi gastar", não "o que saiu da conta".
 */
public class TransacoesViewModel extends AndroidViewModel {

    private final YearMonth mes;
    private final LiveData<List<ItemLista>> linhas;

    public TransacoesViewModel(@NonNull Application app) {
        super(app);
        mes = YearMonth.now();

        LiveData<List<com.example.appfinanceiro.data.dao.TransacaoComBalde>> fonte =
                AppFinanceiroApplication.from(app).transacaoRepository()
                        .observarComBaldePorPeriodo(mes.atDay(1), mes.atEndOfMonth());

        linhas = Transformations.map(fonte,
                lista -> AgrupamentoPorData.agrupar(lista, LocalDate.now()));
    }

    public LiveData<List<ItemLista>> linhas() {
        return linhas;
    }

    public YearMonth mes() {
        return mes;
    }
}
