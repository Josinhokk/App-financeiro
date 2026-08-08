package com.example.appfinanceiro.core;

import java.time.LocalDate;

/**
 * Rótulos de data em pt-BR. Os nomes de mês são fixos aqui em vez de vir do
 * {@code DateTimeFormatter}: a abreviação do ICU muda entre versões do Android
 * (com ponto, sem ponto, maiúscula) e a lista de transações precisa ser estável.
 */
public final class DatasPtBr {

    private static final String[] MESES = {
            "jan", "fev", "mar", "abr", "mai", "jun",
            "jul", "ago", "set", "out", "nov", "dez"
    };

    public static final String HOJE = "Hoje";
    public static final String ONTEM = "Ontem";

    private DatasPtBr() {}

    /**
     * "Hoje", "Ontem", "8 de ago" no ano corrente, "8 de ago de 2025" fora dele.
     * Datas futuras caem no formato absoluto — parcelas projetadas são o caso.
     */
    public static String rotuloRelativo(LocalDate data, LocalDate hoje) {
        if (data.equals(hoje)) return HOJE;
        if (data.equals(hoje.minusDays(1))) return ONTEM;

        String base = data.getDayOfMonth() + " de " + MESES[data.getMonthValue() - 1];
        if (data.getYear() != hoje.getYear()) {
            base += " de " + data.getYear();
        }
        return base;
    }

    /** Sempre absoluto, sem "hoje"/"ontem". Para campos de formulário. */
    public static String rotuloAbsoluto(LocalDate data) {
        return String.format("%02d/%02d/%d",
                data.getDayOfMonth(), data.getMonthValue(), data.getYear());
    }
}
