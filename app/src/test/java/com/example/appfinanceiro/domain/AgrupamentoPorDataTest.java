package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.appfinanceiro.data.dao.TransacaoComBalde;
import com.example.appfinanceiro.data.entity.TipoTransacao;
import com.example.appfinanceiro.data.entity.Transacao;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgrupamentoPorDataTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 8);

    private static TransacaoComBalde saida(LocalDate data, long centavos, String balde) {
        return criar(data, centavos, balde, TipoTransacao.SAIDA);
    }

    private static TransacaoComBalde criar(LocalDate data, long centavos, String balde, TipoTransacao tipo) {
        Transacao t = new Transacao();
        t.setDataCompetencia(data);
        t.setValorCentavos(centavos);
        t.setTipo(tipo);

        TransacaoComBalde tb = new TransacaoComBalde();
        tb.transacao = t;
        tb.baldeNome = balde;
        return tb;
    }

    @Test
    public void listaVaziaNaoGeraLinhas() {
        assertTrue(AgrupamentoPorData.agrupar(Collections.emptyList(), HOJE).isEmpty());
        assertTrue(AgrupamentoPorData.agrupar(null, HOJE).isEmpty());
    }

    @Test
    public void umDiaGeraUmCabecalhoSeguidoDosLancamentos() {
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(Arrays.asList(
                saida(HOJE, 1000, "Lazer"),
                saida(HOJE, 2000, "Fixos")
        ), HOJE);

        assertEquals(3, linhas.size());
        assertTrue(linhas.get(0) instanceof ItemLista.Cabecalho);
        assertTrue(linhas.get(1) instanceof ItemLista.Lancamento);
        assertTrue(linhas.get(2) instanceof ItemLista.Lancamento);
    }

    @Test
    public void cabecalhoSomaOTotalDoDia() {
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(Arrays.asList(
                saida(HOJE, 1000, "Lazer"),
                saida(HOJE, 2345, "Fixos")
        ), HOJE);

        ItemLista.Cabecalho c = (ItemLista.Cabecalho) linhas.get(0);
        assertEquals(3345, c.totalCentavos);
    }

    @Test
    public void trocaDeDiaAbreNovoCabecalho() {
        LocalDate ontem = HOJE.minusDays(1);
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(Arrays.asList(
                saida(HOJE, 1000, "Lazer"),
                saida(ontem, 500, "Fixos"),
                saida(ontem, 700, "Lazer")
        ), HOJE);

        assertEquals(5, linhas.size());
        assertEquals("Hoje", ((ItemLista.Cabecalho) linhas.get(0)).rotulo);
        assertEquals(1000, ((ItemLista.Cabecalho) linhas.get(0)).totalCentavos);
        assertEquals("Ontem", ((ItemLista.Cabecalho) linhas.get(2)).rotulo);
        assertEquals(1200, ((ItemLista.Cabecalho) linhas.get(2)).totalCentavos);
    }

    @Test
    public void entradaNaoContaNoTotalDoDia() {
        // Salário no mesmo dia de um gasto não pode inflar o "gasto do dia".
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(Arrays.asList(
                saida(HOJE, 1000, "Lazer"),
                criar(HOJE, 500000, null, TipoTransacao.ENTRADA)
        ), HOJE);

        assertEquals(1000, ((ItemLista.Cabecalho) linhas.get(0)).totalCentavos);
        // Mas continua aparecendo como linha.
        assertEquals(3, linhas.size());
    }

    @Test
    public void diasAlternadosNaoSaoFundidos() {
        // Entrada fora de ordem quebra o agrupamento por design: o DAO garante a ordem.
        LocalDate ontem = HOJE.minusDays(1);
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(Arrays.asList(
                saida(HOJE, 100, "a"),
                saida(ontem, 100, "b"),
                saida(HOJE, 100, "c")
        ), HOJE);

        long cabecalhos = linhas.stream().filter(l -> l instanceof ItemLista.Cabecalho).count();
        assertEquals(3, cabecalhos);
    }

    @Test
    public void preservaAOrdemDosLancamentosDentroDoDia() {
        List<TransacaoComBalde> entrada = new ArrayList<>(Arrays.asList(
                saida(HOJE, 111, "primeiro"),
                saida(HOJE, 222, "segundo"),
                saida(HOJE, 333, "terceiro")
        ));

        List<ItemLista> linhas = AgrupamentoPorData.agrupar(entrada, HOJE);

        assertEquals(111, ((ItemLista.Lancamento) linhas.get(1)).item.transacao.getValorCentavos());
        assertEquals(222, ((ItemLista.Lancamento) linhas.get(2)).item.transacao.getValorCentavos());
        assertEquals(333, ((ItemLista.Lancamento) linhas.get(3)).item.transacao.getValorCentavos());
    }

    @Test
    public void datasAntigasUsamRotuloAbsoluto() {
        LocalDate antiga = LocalDate.of(2026, 7, 31);
        List<ItemLista> linhas = AgrupamentoPorData.agrupar(
                Collections.singletonList(saida(antiga, 100, "Lazer")), HOJE);

        assertEquals("31 de jul", ((ItemLista.Cabecalho) linhas.get(0)).rotulo);
    }
}
