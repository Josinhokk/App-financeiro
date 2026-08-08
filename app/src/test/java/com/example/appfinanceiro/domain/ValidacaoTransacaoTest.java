package com.example.appfinanceiro.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.appfinanceiro.domain.ValidacaoTransacao.Erro;

import org.junit.Test;

public class ValidacaoTransacaoTest {

    @Test
    public void valorPositivoComBaldeEhValido() {
        assertEquals(Erro.NENHUM, ValidacaoTransacao.validar(1, 1L));
        assertTrue(ValidacaoTransacao.valido(123456, 9L));
    }

    @Test
    public void valorZeroRejeitado() {
        // O campo nasce em R$ 0,00; salvar sem digitar nada não pode passar.
        assertEquals(Erro.VALOR_ZERO, ValidacaoTransacao.validar(0, 1L));
        assertFalse(ValidacaoTransacao.valido(0, 1L));
    }

    @Test
    public void valorNegativoRejeitado() {
        // Saída já é negativa por natureza; o sinal vem do tipo, não do valor.
        assertEquals(Erro.VALOR_NEGATIVO, ValidacaoTransacao.validar(-1, 1L));
    }

    @Test
    public void semBaldeRejeitado() {
        assertEquals(Erro.SEM_BALDE, ValidacaoTransacao.validar(1000, null));
    }

    @Test
    public void valorEhValidadoAntesDoBalde() {
        // No fluxo de três toques o valor vem primeiro; o erro tem de refletir isso.
        assertEquals(Erro.VALOR_ZERO, ValidacaoTransacao.validar(0, null));
    }
}
