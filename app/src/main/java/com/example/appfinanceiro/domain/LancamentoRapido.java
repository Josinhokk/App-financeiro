package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.data.entity.Transacao;
import com.example.appfinanceiro.data.entity.TipoTransacao;

import java.time.LocalDate;

/**
 * Monta a transação do caminho de três toques (seção 7.1).
 *
 * Um lançamento rápido é sempre uma saída à vista, e é aí que mora a regra:
 * à vista significa competência e caixa na mesma data (ADR-03). Cartão e
 * parcelamento — que separam as duas datas — vêm por outros caminhos.
 */
public final class LancamentoRapido {

    /** Enquanto a entidade Conta não existe, toda transação nasce sem conta. */
    public static final long SEM_CONTA = 0L;

    private LancamentoRapido() {}

    public static Transacao criar(long valorCentavos, long baldeId, LocalDate data, String descricao) {
        ValidacaoTransacao.Erro erro = ValidacaoTransacao.validar(valorCentavos, baldeId);
        if (erro != ValidacaoTransacao.Erro.NENHUM) {
            throw new IllegalArgumentException("lançamento inválido: " + erro);
        }

        Transacao t = new Transacao();
        t.setValorCentavos(valorCentavos);
        t.setTipo(TipoTransacao.SAIDA);
        t.setDataCompetencia(data);
        t.setDataCaixa(data);
        t.setBaldeId(baldeId);
        t.setContaId(SEM_CONTA);
        t.setDescricao(normalizar(descricao));
        return t;
    }

    /** Descrição em branco vira null: coluna vazia é ruído no banco. */
    private static String normalizar(String descricao) {
        if (descricao == null) return null;
        String limpo = descricao.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
