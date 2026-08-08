package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.example.appfinanceiro.data.entity.TipoTransacao;
import com.example.appfinanceiro.data.entity.Transacao;

import org.junit.Test;

import java.time.LocalDate;

public class LancamentoRapidoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 8, 8);

    @Test
    public void lancamentoRapidoEhSempreSaida() {
        Transacao t = LancamentoRapido.criar(1234, 3L, DIA, null);
        assertEquals(TipoTransacao.SAIDA, t.getTipo());
    }

    @Test
    public void aVistaColocaCompetenciaECaixaNaMesmaData() {
        // ADR-03: à vista, a decisão de gastar e a saída do dinheiro coincidem.
        Transacao t = LancamentoRapido.criar(5000, 1L, DIA, null);
        assertEquals(DIA, t.getDataCompetencia());
        assertEquals(DIA, t.getDataCaixa());
    }

    @Test
    public void preservaValorEBalde() {
        Transacao t = LancamentoRapido.criar(80000, 7L, DIA, null);
        assertEquals(80000, t.getValorCentavos());
        assertEquals(Long.valueOf(7L), t.getBaldeId());
    }

    @Test
    public void nasceSemContaEnquantoAEntidadeNaoExiste() {
        Transacao t = LancamentoRapido.criar(100, 1L, DIA, null);
        assertEquals(LancamentoRapido.SEM_CONTA, t.getContaId());
    }

    @Test
    public void descricaoEmBrancoViraNull() {
        assertNull(LancamentoRapido.criar(100, 1L, DIA, null).getDescricao());
        assertNull(LancamentoRapido.criar(100, 1L, DIA, "").getDescricao());
        assertNull(LancamentoRapido.criar(100, 1L, DIA, "   ").getDescricao());
    }

    @Test
    public void descricaoUtilEhAparadaEMantida() {
        Transacao t = LancamentoRapido.criar(100, 1L, DIA, "  Almoço  ");
        assertEquals("Almoço", t.getDescricao());
    }

    @Test
    public void naoNasceSemNadaDeParcelamentoOuRecorrencia() {
        Transacao t = LancamentoRapido.criar(100, 1L, DIA, null);
        assertNull(t.getParcelamentoId());
        assertNull(t.getNumeroParcela());
        assertNull(t.getRecorrenciaId());
        assertNull(t.getCategoriaId());
    }

    @Test
    public void recusaLancamentoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> LancamentoRapido.criar(0, 1L, DIA, null));
        assertThrows(IllegalArgumentException.class,
                () -> LancamentoRapido.criar(-500, 1L, DIA, null));
    }
}
