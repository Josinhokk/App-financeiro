package com.example.appfinanceiro.data.dao;

import androidx.room.ColumnInfo;

/** Projeção usada por {@link TransacaoDao#compromissoFuturoPorMes}. */
public class CompromissoMensal {

    @ColumnInfo(name = "mes")
    public String mes;

    @ColumnInfo(name = "totalCentavos")
    public long totalCentavos;

    public CompromissoMensal() {}

    public CompromissoMensal(String mes, long totalCentavos) {
        this.mes = mes;
        this.totalCentavos = totalCentavos;
    }
}
