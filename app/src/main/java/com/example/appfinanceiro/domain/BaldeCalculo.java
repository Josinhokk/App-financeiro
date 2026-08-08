package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.data.entity.Balde;

/**
 * Regras 4.1 e 4.2 do plano. Puras — não tocam Room.
 * O {@code gastoNoPeriodoCentavos} é injetado pelo chamador (que o obtém do DAO).
 */
public final class BaldeCalculo {

    private BaldeCalculo() {}

    /** 4.1 — Disponível no balde em um período (mês). */
    public static long disponivelCentavos(Balde balde, long gastoNoPeriodoCentavos) {
        long disponivel = balde.getValorMensalCentavos() - gastoNoPeriodoCentavos;
        if (balde.isRolaSaldo()) {
            disponivel += balde.getSaldoAcumuladoCentavos();
        }
        return disponivel;
    }

    /**
     * 4.2 — Novo {@code saldoAcumulado} após a virada de mês.
     * Balde que não rola volta a zero; balde que rola recebe o disponível do mês encerrado.
     */
    public static long novoSaldoAcumuladoCentavos(Balde balde, long gastoNoMesEncerradoCentavos) {
        if (!balde.isRolaSaldo()) return 0L;
        return disponivelCentavos(balde, gastoNoMesEncerradoCentavos);
    }
}
