package com.example.appfinanceiro.domain;

/**
 * O que impede um lançamento de ser salvo. Deliberadamente curto: no caminho
 * de três toques (seção 7.1) só valor e balde são obrigatórios — categoria,
 * descrição e conta são opcionais, porque um gasto sem categoria é
 * infinitamente melhor que um gasto não lançado.
 */
public final class ValidacaoTransacao {

    public enum Erro {
        NENHUM,
        VALOR_ZERO,
        VALOR_NEGATIVO,
        SEM_BALDE
    }

    private ValidacaoTransacao() {}

    public static Erro validar(long valorCentavos, Long baldeId) {
        if (valorCentavos < 0) return Erro.VALOR_NEGATIVO;
        if (valorCentavos == 0) return Erro.VALOR_ZERO;
        if (baldeId == null) return Erro.SEM_BALDE;
        return Erro.NENHUM;
    }

    public static boolean valido(long valorCentavos, Long baldeId) {
        return validar(valorCentavos, baldeId) == Erro.NENHUM;
    }
}
