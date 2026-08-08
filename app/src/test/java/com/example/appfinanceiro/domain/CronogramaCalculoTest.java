package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;

import com.example.appfinanceiro.data.dao.CompromissoMensal;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CronogramaCalculoTest {

    @Test
    public void listaVazia_naoGeraLiberacoes() {
        assertEquals(0, CronogramaCalculo.derivarLiberacoes(Collections.emptyList()).size());
    }

    @Test
    public void unicoMes_naoGeraLiberacoes() {
        List<CompromissoMensal> in = Collections.singletonList(new CompromissoMensal("2026-08", 39689));
        assertEquals(0, CronogramaCalculo.derivarLiberacoes(in).size());
    }

    @Test
    public void quedaDeMesParaMes_emiteLiberacaoNoMesQueCaiu() {
        // Ago R$ 396,89 → Set R$ 396,89 → Out R$ 396,89 → Nov R$ 226,04 (Alura terminou).
        // Espera uma liberação em 2026-11 de R$ 170,85 (diferença).
        List<CompromissoMensal> in = Arrays.asList(
                new CompromissoMensal("2026-08", 39689),
                new CompromissoMensal("2026-09", 39689),
                new CompromissoMensal("2026-10", 39689),
                new CompromissoMensal("2026-11", 22604)
        );

        List<CronogramaCalculo.Liberacao> out = CronogramaCalculo.derivarLiberacoes(in);

        assertEquals(1, out.size());
        assertEquals("2026-11", out.get(0).mes);
        assertEquals(17085L, out.get(0).valorCentavos);
    }

    @Test
    public void multiplasQuedas_emiteUmaLiberacaoPorTransicao() {
        List<CompromissoMensal> in = Arrays.asList(
                new CompromissoMensal("2026-08", 50000),
                new CompromissoMensal("2026-09", 30000),
                new CompromissoMensal("2026-10", 30000),
                new CompromissoMensal("2026-11", 10000)
        );

        List<CronogramaCalculo.Liberacao> out = CronogramaCalculo.derivarLiberacoes(in);

        assertEquals(2, out.size());
        assertEquals("2026-09", out.get(0).mes);
        assertEquals(20000L, out.get(0).valorCentavos);
        assertEquals("2026-11", out.get(1).mes);
        assertEquals(20000L, out.get(1).valorCentavos);
    }

    @Test
    public void aumentoNoCompromisso_naoGeraLiberacao() {
        // Novo parcelamento entra em outubro. Não é liberação, é aperto.
        List<CompromissoMensal> in = Arrays.asList(
                new CompromissoMensal("2026-08", 30000),
                new CompromissoMensal("2026-09", 30000),
                new CompromissoMensal("2026-10", 45000)
        );

        assertEquals(0, CronogramaCalculo.derivarLiberacoes(in).size());
    }
}
