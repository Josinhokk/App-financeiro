package com.example.appfinanceiro.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MascaraMoedaTest {

    @Test
    public void textoVazioOuNuloValeZero() {
        assertEquals(0L, MascaraMoeda.extrairCentavos(""));
        assertEquals(0L, MascaraMoeda.extrairCentavos(null));
        assertEquals(0L, MascaraMoeda.extrairCentavos("R$ "));
    }

    @Test
    public void digitosViramCentavosDaDireitaParaEsquerda() {
        assertEquals(5L, MascaraMoeda.extrairCentavos("5"));
        assertEquals(50L, MascaraMoeda.extrairCentavos("50"));
        assertEquals(500L, MascaraMoeda.extrairCentavos("500"));
        assertEquals(1234L, MascaraMoeda.extrairCentavos("1234"));
    }

    @Test
    public void ignoraSimboloEPontuacaoDoProprioCampo() {
        // O watcher realimenta o campo já formatado; reler o texto tem de ser idempotente.
        assertEquals(1234L, MascaraMoeda.extrairCentavos("R$ 12,34"));
        assertEquals(123456L, MascaraMoeda.extrairCentavos("R$ 1.234,56"));
        assertEquals(80000L, MascaraMoeda.extrairCentavos("R$ 800,00"));
    }

    @Test
    public void reformatarOProprioResultadoNaoMudaOValor() {
        long[] amostras = {0, 1, 99, 100, 73000, 240000, 999999999L};
        for (long c : amostras) {
            assertEquals(c, MascaraMoeda.extrairCentavos(MascaraMoeda.formatarCentavos(c)));
        }
    }

    @Test
    public void zerosAEsquerdaSaoDescartados() {
        assertEquals(0L, MascaraMoeda.extrairCentavos("000"));
        assertEquals(5L, MascaraMoeda.extrairCentavos("0005"));
        // Zero interno continua valendo.
        assertEquals(1005L, MascaraMoeda.extrairCentavos("1005"));
    }

    @Test
    public void truncaNoLimiteEmVezDeEstourarOLong() {
        String excessivo = "9".repeat(40);
        long r = MascaraMoeda.extrairCentavos(excessivo);
        assertEquals(MascaraMoeda.MAX_DIGITOS, String.valueOf(r).length());
        assertTrue(r > 0);
    }

    @Test
    public void formatarUsaPadraoPtBr() {
        String txt = MascaraMoeda.formatarCentavos(123450);
        assertTrue(txt.startsWith("R$"));
        assertTrue(txt.endsWith("1.234,50"));
    }
}
