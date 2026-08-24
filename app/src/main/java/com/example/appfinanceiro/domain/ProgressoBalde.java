package com.example.appfinanceiro.domain;

import com.example.appfinanceiro.data.entity.Balde;

/**
 * Os três estados da barra de progresso do balde (seção 9): verde até 70%,
 * âmbar de 70% a 100%, vermelho acima.
 *
 * A seção chama isso de principal canal de feedback do app e exige leitura em
 * meio segundo — por isso o estado é derivado aqui, e não montado na tela.
 */
public final class ProgressoBalde {

    public enum Estado { VERDE, AMBAR, VERMELHO }

    /** Teto do percentual devolvido. Acima disso o número já não informa nada. */
    public static final int MAX_PERCENTUAL = 999;

    private ProgressoBalde() {}

    /**
     * Quanto o balde tem para gastar no mês, antes de descontar os gastos.
     * Balde que rola saldo soma a herança dos meses anteriores.
     */
    public static long baseCentavos(Balde balde) {
        long base = balde.getValorMensalCentavos();
        if (balde.isRolaSaldo()) {
            base += balde.getSaldoAcumuladoCentavos();
        }
        return base;
    }

    /** Percentual da base já consumido, arredondado e limitado a MAX_PERCENTUAL. */
    public static int percentualUsado(long baseCentavos, long gastoCentavos) {
        if (gastoCentavos <= 0) return 0;
        // Sem base e com gasto, o balde está estourado por definição.
        if (baseCentavos <= 0) return MAX_PERCENTUAL;

        long percentual = (gastoCentavos * 100 + baseCentavos / 2) / baseCentavos;
        return (int) Math.min(MAX_PERCENTUAL, percentual);
    }

    /** "Verde até 70%" é inclusivo: exatamente 70% ainda é confortável. */
    public static Estado estado(int percentualUsado) {
        if (percentualUsado <= 70) return Estado.VERDE;
        if (percentualUsado <= 100) return Estado.AMBAR;
        return Estado.VERMELHO;
    }

    /** O que a barra desenha — nunca passa de 100, mesmo com o balde estourado. */
    public static int percentualParaBarra(int percentualUsado) {
        return Math.min(100, Math.max(0, percentualUsado));
    }
}
