package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class ValidacaoTest {

    @Test
    public void dotacoes_dentroDaRenda_naoAlertam() {
        // Fixos 730 + Investimento 2400 + Lazer 800 + Switch 270 = 4200.
        long soma = 73000 + 240000 + 80000 + 27000;
        long renda = 500000; // R$ 5.000

        assertFalse(Validacao.dotacoesExcedemRenda(soma, renda));
    }

    @Test
    public void dotacoes_excedendoRenda_alertam() {
        long soma = 600000;
        long renda = 500000;

        assertTrue(Validacao.dotacoesExcedemRenda(soma, renda));
    }

    @Test
    public void dotacoes_igualARenda_naoAlerta() {
        // Empatar não é estouro.
        assertFalse(Validacao.dotacoesExcedemRenda(500000, 500000));
    }

    @Test
    public void divergenciaFatura_positivaIndicaLancamentoEsquecido() {
        // Calculado 400,00. Real 458,00. Faltam R$ 58 no app.
        assertEquals(5800L, Validacao.divergenciaFaturaCentavos(40000, 45800));
    }

    @Test
    public void divergenciaFatura_negativaIndicaDuplicidade() {
        assertEquals(-1500L, Validacao.divergenciaFaturaCentavos(50000, 48500));
    }

    @Test
    public void divergenciaFatura_zeroConcilia() {
        assertEquals(0L, Validacao.divergenciaFaturaCentavos(45800, 45800));
    }

    @Test
    public void dataCaixaAnteriorACompetencia_alertaQuandoCaixaMenor() {
        LocalDate comp = LocalDate.of(2026, 8, 20);
        LocalDate caixa = LocalDate.of(2026, 8, 10);

        assertTrue(Validacao.dataCaixaAnteriorACompetencia(comp, caixa));
    }

    @Test
    public void dataCaixaAnteriorACompetencia_naoAlertaQuandoCaixaIgualOuMaior() {
        LocalDate comp = LocalDate.of(2026, 8, 20);
        assertFalse(Validacao.dataCaixaAnteriorACompetencia(comp, comp));
        assertFalse(Validacao.dataCaixaAnteriorACompetencia(comp, LocalDate.of(2026, 9, 10)));
    }

    @Test
    public void dataCaixaNula_naoAlerta() {
        assertFalse(Validacao.dataCaixaAnteriorACompetencia(LocalDate.of(2026, 8, 20), null));
    }
}
