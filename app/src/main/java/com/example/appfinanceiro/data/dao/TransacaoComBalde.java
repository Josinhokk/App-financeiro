package com.example.appfinanceiro.data.dao;

import androidx.annotation.Nullable;
import androidx.room.Embedded;

import com.example.appfinanceiro.data.entity.Transacao;

/**
 * Transação com o nome e a cor do balde que ela consumiu, resolvidos no
 * próprio SELECT. Evita uma consulta por linha na hora de desenhar a lista.
 *
 * Os campos do balde são nulos quando a transação não tem balde — entradas
 * não têm, e o balde pode ter sido arquivado.
 */
public class TransacaoComBalde {

    @Embedded
    public Transacao transacao;

    @Nullable
    public String baldeNome;

    @Nullable
    public String baldeCorHex;
}
