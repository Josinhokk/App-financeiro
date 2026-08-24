package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;

import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.entity.TipoBalde;
import com.example.appfinanceiro.domain.ProgressoBalde.Estado;

import org.junit.Test;

public class ProgressoBaldeTest {

    private static Balde balde(long mensalCentavos, boolean rola, long acumuladoCentavos) {
        Balde b = new Balde();
        b.setNome("teste");
        b.setTipo(TipoBalde.LIVRE);
        b.setValorMensalCentavos(mensalCentavos);
        b.setRolaSaldo(rola);
        b.setSaldoAcumuladoCentavos(acumuladoCentavos);
        return b;
    }

    @Test
    public void baseIgnoraAcumuladoQuandoBaldeNaoRola() {
        // Lazer zera todo mês: o que sobrou do mês passado não conta.
        assertEquals(80000, ProgressoBalde.baseCentavos(balde(80000, false, 50000)));
    }

    @Test
    public void baseSomaAcumuladoQuandoBaldeRola() {
        // Switch 2 empilha: base do mês é dotação + herança.
        assertEquals(81000, ProgressoBalde.baseCentavos(balde(27000, true, 54000)));
    }

    @Test
    public void semGastoOPercentualEhZero() {
        assertEquals(0, ProgressoBalde.percentualUsado(80000, 0));
        assertEquals(0, ProgressoBalde.percentualUsado(80000, -100));
    }

    @Test
    public void percentualEhArredondado() {
        assertEquals(50, ProgressoBalde.percentualUsado(80000, 40000));
        assertEquals(25, ProgressoBalde.percentualUsado(80000, 20000));
        // 1/3 arredonda para 33
        assertEquals(33, ProgressoBalde.percentualUsado(30000, 10000));
    }

    @Test
    public void baldeSemDotacaoComGastoEstaEstourado() {
        int p = ProgressoBalde.percentualUsado(0, 100);
        assertEquals(ProgressoBalde.MAX_PERCENTUAL, p);
        assertEquals(Estado.VERMELHO, ProgressoBalde.estado(p));
    }

    @Test
    public void baldeSemDotacaoESemGastoNaoEstaEstourado() {
        assertEquals(0, ProgressoBalde.percentualUsado(0, 0));
        assertEquals(Estado.VERDE, ProgressoBalde.estado(0));
    }

    @Test
    public void percentualExcessivoEhLimitado() {
        assertEquals(ProgressoBalde.MAX_PERCENTUAL,
                ProgressoBalde.percentualUsado(100, 100_000_000L));
    }

    @Test
    public void fronteirasDosTresEstados() {
        assertEquals(Estado.VERDE, ProgressoBalde.estado(0));
        assertEquals(Estado.VERDE, ProgressoBalde.estado(69));
        // "Verde até 70%" é inclusivo.
        assertEquals(Estado.VERDE, ProgressoBalde.estado(70));
        assertEquals(Estado.AMBAR, ProgressoBalde.estado(71));
        assertEquals(Estado.AMBAR, ProgressoBalde.estado(100));
        assertEquals(Estado.VERMELHO, ProgressoBalde.estado(101));
    }

    @Test
    public void barraNuncaPassaDeCem() {
        assertEquals(100, ProgressoBalde.percentualParaBarra(101));
        assertEquals(100, ProgressoBalde.percentualParaBarra(ProgressoBalde.MAX_PERCENTUAL));
        assertEquals(0, ProgressoBalde.percentualParaBarra(-5));
        assertEquals(42, ProgressoBalde.percentualParaBarra(42));
    }

    @Test
    public void casoRealDoPlanoLazerCom612Disponiveis() {
        // Seção 6: R$ 612 livres de R$ 800 significa R$ 188 gastos, 24% usado.
        Balde lazer = balde(80000, false, 0);
        int p = ProgressoBalde.percentualUsado(ProgressoBalde.baseCentavos(lazer), 18800);
        assertEquals(24, p);
        assertEquals(Estado.VERDE, ProgressoBalde.estado(p));
    }
}
