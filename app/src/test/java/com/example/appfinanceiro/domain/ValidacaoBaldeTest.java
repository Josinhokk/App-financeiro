package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.appfinanceiro.domain.ValidacaoBalde.Erro;

import org.junit.Test;

public class ValidacaoBaldeTest {

    @Test
    public void baldeComNomeEValorAceito() {
        assertEquals(Erro.NENHUM, ValidacaoBalde.validar("Lazer", 80000));
        assertTrue(ValidacaoBalde.valido("Lazer", 80000));
    }

    @Test
    public void nomeVazioOuSoEspacoRejeitado() {
        assertEquals(Erro.NOME_VAZIO, ValidacaoBalde.validar(null, 100));
        assertEquals(Erro.NOME_VAZIO, ValidacaoBalde.validar("", 100));
        assertEquals(Erro.NOME_VAZIO, ValidacaoBalde.validar("   ", 100));
        assertFalse(ValidacaoBalde.valido("  ", 100));
    }

    @Test
    public void nomeAcimaDoLimiteRejeitado() {
        String noLimite = "a".repeat(ValidacaoBalde.MAX_NOME);
        String passando = "a".repeat(ValidacaoBalde.MAX_NOME + 1);
        assertEquals(Erro.NENHUM, ValidacaoBalde.validar(noLimite, 0));
        assertEquals(Erro.NOME_LONGO, ValidacaoBalde.validar(passando, 0));
    }

    @Test
    public void dotacaoZeroEhValida() {
        // Balde de acumulação pode nascer sem dotação mensal definida.
        assertEquals(Erro.NENHUM, ValidacaoBalde.validar("Switch 2", 0));
    }

    @Test
    public void dotacaoNegativaRejeitada() {
        assertEquals(Erro.VALOR_NEGATIVO, ValidacaoBalde.validar("Lazer", -1));
    }

    @Test
    public void nomeEhValidadoAntesDoValor() {
        // Com os dois errados, o nome é o que o usuário vê primeiro no formulário.
        assertEquals(Erro.NOME_VAZIO, ValidacaoBalde.validar("", -500));
    }
}
