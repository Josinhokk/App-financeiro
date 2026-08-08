package com.example.appfinanceiro.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "baldes")
public class Balde {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nome = "";

    @NonNull
    private TipoBalde tipo = TipoBalde.LIVRE;

    @ColumnInfo(name = "valorMensalCentavos")
    private long valorMensalCentavos;

    @ColumnInfo(name = "rolaSaldo")
    private boolean rolaSaldo;

    @ColumnInfo(name = "saldoAcumuladoCentavos")
    private long saldoAcumuladoCentavos;

    private String corHex;
    private String icone;
    private int ordem;
    private boolean arquivado;

    public Balde() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getNome() { return nome; }
    public void setNome(@NonNull String nome) { this.nome = nome; }

    @NonNull public TipoBalde getTipo() { return tipo; }
    public void setTipo(@NonNull TipoBalde tipo) { this.tipo = tipo; }

    public long getValorMensalCentavos() { return valorMensalCentavos; }
    public void setValorMensalCentavos(long valorMensalCentavos) { this.valorMensalCentavos = valorMensalCentavos; }

    public boolean isRolaSaldo() { return rolaSaldo; }
    public void setRolaSaldo(boolean rolaSaldo) { this.rolaSaldo = rolaSaldo; }

    public long getSaldoAcumuladoCentavos() { return saldoAcumuladoCentavos; }
    public void setSaldoAcumuladoCentavos(long saldoAcumuladoCentavos) { this.saldoAcumuladoCentavos = saldoAcumuladoCentavos; }

    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }

    public boolean isArquivado() { return arquivado; }
    public void setArquivado(boolean arquivado) { this.arquivado = arquivado; }
}
