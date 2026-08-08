package com.example.appfinanceiro.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Aritmética monetária segura. Armazenamento em long de centavos no banco,
 * BigDecimal em memória, escala 2 e {@link RoundingMode#HALF_UP} em toda divisão.
 */
public final class Dinheiro {

    public static final Locale PT_BR = new Locale("pt", "BR");
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private Dinheiro() {}

    public static BigDecimal deCentavos(long centavos) {
        return BigDecimal.valueOf(centavos, 2);
    }

    public static long paraCentavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    /** Aceita "1.234,56", "1234,56", "1234.56" ou "1234". */
    public static BigDecimal deReais(String texto) {
        if (texto == null) throw new IllegalArgumentException("texto nulo");
        String limpo = texto.trim();
        if (limpo.isEmpty()) throw new IllegalArgumentException("texto vazio");
        boolean temVirgula = limpo.indexOf(',') >= 0;
        if (temVirgula) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal deReais(long reais) {
        return BigDecimal.valueOf(reais).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal somar(BigDecimal... valores) {
        BigDecimal total = ZERO;
        for (BigDecimal v : valores) {
            total = total.add(v);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtrair(BigDecimal a, BigDecimal b) {
        return a.subtract(b).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiplicar(BigDecimal valor, BigDecimal fator) {
        return valor.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal dividir(BigDecimal valor, int divisor) {
        return valor.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    public static String formatar(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    public static String formatarCentavos(long centavos) {
        return formatar(deCentavos(centavos));
    }
}
