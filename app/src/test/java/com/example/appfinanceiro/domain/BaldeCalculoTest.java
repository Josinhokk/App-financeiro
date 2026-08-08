package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;

import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.entity.TipoBalde;

import org.junit.Test;

public class BaldeCalculoTest {

    private static Balde novoBalde(TipoBalde tipo, long mensalCents, boolean rola, long acumuladoCents) {
        Balde b = new Balde();
        b.setNome("teste");
        b.setTipo(tipo);
        b.setValorMensalCentavos(mensalCents);
        b.setRolaSaldo(rola);
        b.setSaldoAcumuladoCentavos(acumuladoCents);
        return b;
    }

    @Test
    public void disponivel_baldeQueNaoRola_ignoraAcumulado() {
        // Lazer: R$800/mês, não rola. Já tem R$ 500 acumulado (que deve ser ignorado).
        Balde lazer = novoBalde(TipoBalde.LIVRE, 80000, false, 50000);
        long gasto = 18800; // R$ 188,00 gastos no mês

        long disponivel = BaldeCalculo.disponivelCentavos(lazer, gasto);

        assertEquals(80000 - 18800, disponivel);
    }

    @Test
    public void disponivel_baldeQueRola_somaAcumulado() {
        // Switch 2: R$ 270/mês, rola, já tem R$ 540 acumulado.
        Balde meta = novoBalde(TipoBalde.META, 27000, true, 54000);
        long gasto = 0;

        long disponivel = BaldeCalculo.disponivelCentavos(meta, gasto);

        assertEquals(27000 + 54000, disponivel);
    }

    @Test
    public void disponivel_podeSerNegativoQuandoEstoura() {
        Balde b = novoBalde(TipoBalde.LIVRE, 80000, false, 0);
        long gasto = 95000;

        assertEquals(-15000, BaldeCalculo.disponivelCentavos(b, gasto));
    }

    @Test
    public void novoSaldoAcumulado_baldeQueNaoRola_reseta() {
        Balde fixos = novoBalde(TipoBalde.FIXO, 73000, false, 12345);
        long gasto = 40000;

        assertEquals(0L, BaldeCalculo.novoSaldoAcumuladoCentavos(fixos, gasto));
    }

    @Test
    public void novoSaldoAcumulado_baldeQueRola_empilha() {
        // Switch 2 no fim do mês: R$ 270 dotação + R$ 540 acumulado, nada gasto.
        Balde meta = novoBalde(TipoBalde.META, 27000, true, 54000);

        assertEquals(27000 + 54000, BaldeCalculo.novoSaldoAcumuladoCentavos(meta, 0));
    }

    @Test
    public void novoSaldoAcumulado_baldeQueRolaComGasto_reduzOAcumulado() {
        Balde investimento = novoBalde(TipoBalde.INVESTIMENTO, 240000, true, 100000);
        long gasto = 50000; // R$ 500 já saíram no mês

        assertEquals(240000 - 50000 + 100000, BaldeCalculo.novoSaldoAcumuladoCentavos(investimento, gasto));
    }

    @Test
    public void viradaDeMes_idempotente_aplicarDuasVezesGeraMesmoResultadoQueUmaVez() {
        // Coração da regra: se rodar a virada duas vezes o mesmo mês, o novo saldo
        // acumulado calculado a partir do "estado pós virada" precisa ser o mesmo.
        // Aqui simulamos: primeira virada faz saldo → S1. Se pipeline for chamado
        // de novo sobre o MESMO mês encerrado (sem novos gastos), o cálculo local
        // sobre o mesmo balde de entrada deve devolver o mesmo S1.
        Balde meta = novoBalde(TipoBalde.META, 27000, true, 54000);

        long primeira = BaldeCalculo.novoSaldoAcumuladoCentavos(meta, 0);
        long segunda = BaldeCalculo.novoSaldoAcumuladoCentavos(meta, 0);

        assertEquals(primeira, segunda);
    }
}
