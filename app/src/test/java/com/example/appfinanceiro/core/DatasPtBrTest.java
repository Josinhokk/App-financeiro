package com.example.appfinanceiro.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

public class DatasPtBrTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 8);

    @Test
    public void hojeEOntemUsamRotuloRelativo() {
        assertEquals("Hoje", DatasPtBr.rotuloRelativo(HOJE, HOJE));
        assertEquals("Ontem", DatasPtBr.rotuloRelativo(HOJE.minusDays(1), HOJE));
    }

    @Test
    public void diasAnterioresUsamDiaEMes() {
        assertEquals("5 de ago", DatasPtBr.rotuloRelativo(LocalDate.of(2026, 8, 5), HOJE));
        assertEquals("31 de jul", DatasPtBr.rotuloRelativo(LocalDate.of(2026, 7, 31), HOJE));
    }

    @Test
    public void anoDiferenteApareceNoRotulo() {
        assertEquals("25 de dez de 2025", DatasPtBr.rotuloRelativo(LocalDate.of(2025, 12, 25), HOJE));
    }

    @Test
    public void datasFuturasUsamFormatoAbsoluto() {
        // Parcelas projetadas caem aqui; "amanhã" seria enganoso numa lista.
        assertEquals("9 de ago", DatasPtBr.rotuloRelativo(HOJE.plusDays(1), HOJE));
        assertEquals("1 de mar de 2027", DatasPtBr.rotuloRelativo(LocalDate.of(2027, 3, 1), HOJE));
    }

    @Test
    public void viradaDeAnoNaoConfundeOntem() {
        LocalDate primeiroDeJaneiro = LocalDate.of(2026, 1, 1);
        assertEquals("Ontem", DatasPtBr.rotuloRelativo(LocalDate.of(2025, 12, 31), primeiroDeJaneiro));
    }

    @Test
    public void todosOsMesesTemAbreviacao() {
        for (int mes = 1; mes <= 12; mes++) {
            String r = DatasPtBr.rotuloRelativo(LocalDate.of(2026, mes, 15), HOJE);
            assertEquals(3, r.substring(r.lastIndexOf(' ') + 1).length());
        }
    }

    @Test
    public void rotuloAbsolutoUsaDiaMesAnoComZeroAEsquerda() {
        assertEquals("08/08/2026", DatasPtBr.rotuloAbsoluto(HOJE));
        assertEquals("01/03/2027", DatasPtBr.rotuloAbsoluto(LocalDate.of(2027, 3, 1)));
    }
}
