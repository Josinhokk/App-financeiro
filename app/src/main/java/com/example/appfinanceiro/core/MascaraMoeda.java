package com.example.appfinanceiro.core;

/**
 * Entrada de dinheiro por dígitos: o usuário digita 1, 2, 3, 4 e lê "R$ 12,34".
 *
 * O valor nunca vira texto com separador decimal no meio do caminho — sai de
 * dígitos direto para long de centavos. Isso elimina a classe inteira de bugs
 * de parse dependente de locale (ADR-02).
 */
public final class MascaraMoeda {

    /** 13 dígitos = até R$ 99.999.999.999,99, bem abaixo do teto do long. */
    public static final int MAX_DIGITOS = 13;

    private MascaraMoeda() {}

    /**
     * Lê qualquer texto e devolve os centavos que ele representa, ignorando
     * símbolo, pontuação e zeros à esquerda. Texto sem dígito nenhum vale zero.
     */
    public static long extrairCentavos(String texto) {
        if (texto == null) return 0L;

        StringBuilder digitos = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c < '0' || c > '9') continue;
            // Zero só conta depois que houver um dígito significativo.
            if (digitos.length() == 0 && c == '0') continue;
            digitos.append(c);
            if (digitos.length() == MAX_DIGITOS) break;
        }

        if (digitos.length() == 0) return 0L;
        return Long.parseLong(digitos.toString());
    }

    /** O que o campo deve exibir para um dado valor em centavos. */
    public static String formatarCentavos(long centavos) {
        return Dinheiro.formatarCentavos(centavos);
    }
}
