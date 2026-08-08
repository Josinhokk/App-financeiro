package com.example.appfinanceiro.data.repository;

import androidx.lifecycle.LiveData;

import com.example.appfinanceiro.data.dao.TransacaoDao;
import com.example.appfinanceiro.data.entity.Transacao;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

public class TransacaoRepository {

    private final TransacaoDao dao;
    private final Executor io;

    public TransacaoRepository(TransacaoDao dao, Executor io) {
        this.dao = dao;
        this.io = io;
    }

    public LiveData<List<Transacao>> observarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return dao.observarPorPeriodo(inicio, fim);
    }

    public void inserir(Transacao t) {
        io.execute(() -> dao.inserir(t));
    }

    public void atualizar(Transacao t) {
        io.execute(() -> dao.atualizar(t));
    }

    public void deletar(Transacao t) {
        io.execute(() -> dao.deletar(t));
    }
}
