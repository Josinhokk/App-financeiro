package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.core.DatasPtBr;
import com.example.appfinanceiro.data.dao.TransacaoComBalde;
import com.example.appfinanceiro.data.entity.TipoTransacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transforma a lista achatada que vem do banco em linhas de tela, inserindo um
 * cabeçalho a cada troca de dia de competência.
 *
 * Pressupõe a entrada já ordenada por {@code dataCompetencia} decrescente, que
 * é como o DAO devolve — agrupar aqui evita uma segunda passada de ordenação.
 */
public final class AgrupamentoPorData {

    private AgrupamentoPorData() {}

    public static List<ItemLista> agrupar(List<TransacaoComBalde> transacoes, LocalDate hoje) {
        List<ItemLista> linhas = new ArrayList<>();
        if (transacoes == null || transacoes.isEmpty()) return linhas;

        int i = 0;
        while (i < transacoes.size()) {
            LocalDate dia = transacoes.get(i).transacao.getDataCompetencia();

            // Varre o bloco contíguo do mesmo dia para somar antes de emitir o cabeçalho.
            int fim = i;
            long total = 0;
            while (fim < transacoes.size()
                    && transacoes.get(fim).transacao.getDataCompetencia().equals(dia)) {
                total += gastoDe(transacoes.get(fim));
                fim++;
            }

            linhas.add(new ItemLista.Cabecalho(dia, DatasPtBr.rotuloRelativo(dia, hoje), total));
            for (int j = i; j < fim; j++) {
                linhas.add(new ItemLista.Lancamento(transacoes.get(j)));
            }
            i = fim;
        }

        return linhas;
    }

    /** Só saída soma no total do dia; entrada não é gasto. */
    private static long gastoDe(TransacaoComBalde t) {
        return t.transacao.getTipo() == TipoTransacao.SAIDA
                ? t.transacao.getValorCentavos()
                : 0L;
    }
}
