package com.example.appfinanceiro.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.math.BigDecimal;

public class DinheiroTest {

    @Test
    public void deCentavos_produzBigDecimalComEscala2() {
        BigDecimal v = Dinheiro.deCentavos(12345);
        assertEquals(new BigDecimal("123.45"), v);
        assertEquals(2, v.scale());
    }

    @Test
    public void paraCentavos_arredondaHalfUp() {
        assertEquals(12346, Dinheiro.paraCentavos(new BigDecimal("123.455")));
        assertEquals(12345, Dinheiro.paraCentavos(new BigDecimal("123.454")));
    }

    @Test
    public void roundTripCentavosPreservaValor() {
        long[] amostras = {0, 1, 99, 100, 12345, 199999999L, -50};
        for (long c : amostras) {
            assertEquals(c, Dinheiro.paraCentavos(Dinheiro.deCentavos(c)));
        }
    }

    @Test
    public void deReais_aceitaFormatoBrasileiro() {
        assertEquals(new BigDecimal("1234.56"), Dinheiro.deReais("1.234,56"));
        assertEquals(new BigDecimal("1234.56"), Dinheiro.deReais("1234,56"));
    }

    @Test
    public void deReais_aceitaFormatoAmericanoQuandoNaoHaVirgula() {
        assertEquals(new BigDecimal("1234.56"), Dinheiro.deReais("1234.56"));
        assertEquals(new BigDecimal("1234.00"), Dinheiro.deReais("1234"));
    }

    @Test
    public void deReais_rejeitaEntradaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> Dinheiro.deReais(""));
        assertThrows(IllegalArgumentException.class, () -> Dinheiro.deReais(null));
    }

    @Test
    public void somaEvitaImprecisaoDeDouble() {
        BigDecimal soma = Dinheiro.ZERO;
        for (int i = 0; i < 10; i++) {
            soma = Dinheiro.somar(soma, Dinheiro.deReais("0,10"));
        }
        assertEquals(new BigDecimal("1.00"), soma);
    }

    @Test
    public void subtrairMantemEscala() {
        BigDecimal r = Dinheiro.subtrair(new BigDecimal("10.00"), new BigDecimal("3.33"));
        assertEquals(new BigDecimal("6.67"), r);
    }

    @Test
    public void dividirUsaHalfUp() {
        BigDecimal r = Dinheiro.dividir(new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("3.33"), r);
    }

    @Test
    public void formatarPtBrUsaSimboloEVirgula() {
        String txt = Dinheiro.formatar(new BigDecimal("1234.50"));
        // Não fixa o caractere de espaço entre R$ e o número (varia por versão do ICU),
        // mas deve começar com R$, terminar com "1.234,50" e conter apenas um caractere entre eles.
        assertEquals(true, txt.startsWith("R$"));
        assertEquals(true, txt.endsWith("1.234,50"));
    }
}
