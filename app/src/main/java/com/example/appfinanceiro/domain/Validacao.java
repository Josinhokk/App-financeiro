package com.example.appfinanceiro.domain;

import java.time.LocalDate;

/** Regras 4.4 do plano — sinalizam, não bloqueiam. */
public final class Validacao {

    private Validacao() {}

    /** Soma das dotações dos baldes ≤ renda mensal cadastrada. */
    public static boolean dotacoesExcedemRenda(long somaDotacoesCentavos, long rendaMensalCentavos) {
        return somaDotacoesCentavos > rendaMensalCentavos;
    }

    /**
     * Divergência entre o total calculado da fatura e o valor real informado.
     * Positivo = o real é maior que o calculado (provável lançamento esquecido).
     * Negativo = o calculado é maior que o real (provável lançamento duplicado).
     */
    public static long divergenciaFaturaCentavos(long calculadoCentavos, long realCentavos) {
        return realCentavos - calculadoCentavos;
    }

    /** Alerta de digitação: caixa não pode ser anterior à competência. */
    public static boolean dataCaixaAnteriorACompetencia(LocalDate dataCompetencia, LocalDate dataCaixa) {
        return dataCaixa != null && dataCaixa.isBefore(dataCompetencia);
    }
}
