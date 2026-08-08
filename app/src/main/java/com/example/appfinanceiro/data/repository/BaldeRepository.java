package com.example.appfinanceiro.data.repository;

import androidx.lifecycle.LiveData;

import com.example.appfinanceiro.data.dao.BaldeDao;
import com.example.appfinanceiro.data.dao.TransacaoDao;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.domain.BaldeCalculo;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.Executor;

public class BaldeRepository {

    private final BaldeDao baldeDao;
    private final TransacaoDao transacaoDao;
    private final Executor io;

    public BaldeRepository(BaldeDao baldeDao, TransacaoDao transacaoDao, Executor io) {
        this.baldeDao = baldeDao;
        this.transacaoDao = transacaoDao;
        this.io = io;
    }

    public LiveData<List<Balde>> observarAtivos() {
        return baldeDao.observarAtivos();
    }

    public LiveData<Balde> observarPorId(long id) {
        return baldeDao.observarPorId(id);
    }

    public void inserir(Balde b) {
        io.execute(() -> baldeDao.inserir(b));
    }

    /** Balde novo entra no fim da lista; a ordem é resolvida na thread de IO. */
    public void inserirNoFim(Balde b) {
        io.execute(() -> {
            b.setOrdem(baldeDao.proximaOrdem());
            baldeDao.inserir(b);
        });
    }

    public void arquivar(long id) {
        io.execute(() -> baldeDao.arquivar(id));
    }

    public void atualizar(Balde b) {
        io.execute(() -> baldeDao.atualizar(b));
    }

    public void deletar(Balde b) {
        io.execute(() -> baldeDao.deletar(b));
    }

    /** Chame em background. Devolve o disponível do balde no mês em centavos. */
    public long disponivelCentavosSync(Balde b, YearMonth mes) {
        long gasto = transacaoDao.somaGastosDoPeriodoCentavos(
                b.getId(), mes.atDay(1), mes.atEndOfMonth());
        return BaldeCalculo.disponivelCentavos(b, gasto);
    }
}
