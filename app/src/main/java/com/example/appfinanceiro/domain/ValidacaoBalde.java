package com.example.appfinanceiro.domain;

/**
 * Regras do formulário de balde. Diferente das validações da seção 4.4 — que
 * apenas sinalizam — estas bloqueiam o salvamento, porque um balde sem nome ou
 * com dotação negativa corromperia o cálculo de disponível.
 */
public final class ValidacaoBalde {

    public enum Erro {
        NENHUM,
        NOME_VAZIO,
        NOME_LONGO,
        VALOR_NEGATIVO
    }

    public static final int MAX_NOME = 40;

    private ValidacaoBalde() {}

    public static Erro validar(String nome, long valorMensalCentavos) {
        if (nome == null) return Erro.NOME_VAZIO;

        String limpo = nome.trim();
        if (limpo.isEmpty()) return Erro.NOME_VAZIO;
        if (limpo.length() > MAX_NOME) return Erro.NOME_LONGO;
        if (valorMensalCentavos < 0) return Erro.VALOR_NEGATIVO;

        return Erro.NENHUM;
    }

    public static boolean valido(String nome, long valorMensalCentavos) {
        return validar(nome, valorMensalCentavos) == Erro.NENHUM;
    }
}
