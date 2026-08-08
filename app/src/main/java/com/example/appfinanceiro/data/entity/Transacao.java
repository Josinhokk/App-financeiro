package com.example.appfinanceiro.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/**
 * Evento de competência. Consome balde em {@link #dataCompetencia} e
 * sai da conta em {@link #dataCaixa} (podem coincidir em compras à vista).
 *
 * Conta, Categoria e Parcelamento serão declarados como FK à medida que
 * as entidades forem criadas nas próximas fases.
 */
@Entity(
        tableName = "transacoes",
        foreignKeys = {
                @ForeignKey(
                        entity = Balde.class,
                        parentColumns = "id",
                        childColumns = "baldeId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("dataCompetencia"),
                @Index("baldeId"),
                @Index("parcelamentoId")
        }
)
public class Transacao {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "valorCentavos")
    private long valorCentavos;

    @NonNull
    private TipoTransacao tipo = TipoTransacao.SAIDA;

    @NonNull
    @ColumnInfo(name = "dataCompetencia")
    private LocalDate dataCompetencia = LocalDate.now();

    @Nullable
    @ColumnInfo(name = "dataCaixa")
    private LocalDate dataCaixa;

    private String descricao;

    @ColumnInfo(name = "contaId")
    private long contaId;

    @Nullable
    @ColumnInfo(name = "categoriaId")
    private Long categoriaId;

    @Nullable
    @ColumnInfo(name = "baldeId")
    private Long baldeId;

    @Nullable
    @ColumnInfo(name = "parcelamentoId")
    private Long parcelamentoId;

    @Nullable
    @ColumnInfo(name = "numeroParcela")
    private Integer numeroParcela;

    @Nullable
    @ColumnInfo(name = "recorrenciaId")
    private Long recorrenciaId;

    public Transacao() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getValorCentavos() { return valorCentavos; }
    public void setValorCentavos(long valorCentavos) { this.valorCentavos = valorCentavos; }

    @NonNull public TipoTransacao getTipo() { return tipo; }
    public void setTipo(@NonNull TipoTransacao tipo) { this.tipo = tipo; }

    @NonNull public LocalDate getDataCompetencia() { return dataCompetencia; }
    public void setDataCompetencia(@NonNull LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }

    @Nullable public LocalDate getDataCaixa() { return dataCaixa; }
    public void setDataCaixa(@Nullable LocalDate dataCaixa) { this.dataCaixa = dataCaixa; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public long getContaId() { return contaId; }
    public void setContaId(long contaId) { this.contaId = contaId; }

    @Nullable public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(@Nullable Long categoriaId) { this.categoriaId = categoriaId; }

    @Nullable public Long getBaldeId() { return baldeId; }
    public void setBaldeId(@Nullable Long baldeId) { this.baldeId = baldeId; }

    @Nullable public Long getParcelamentoId() { return parcelamentoId; }
    public void setParcelamentoId(@Nullable Long parcelamentoId) { this.parcelamentoId = parcelamentoId; }

    @Nullable public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(@Nullable Integer numeroParcela) { this.numeroParcela = numeroParcela; }

    @Nullable public Long getRecorrenciaId() { return recorrenciaId; }
    public void setRecorrenciaId(@Nullable Long recorrenciaId) { this.recorrenciaId = recorrenciaId; }
}
