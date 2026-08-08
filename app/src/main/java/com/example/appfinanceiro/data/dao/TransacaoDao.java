package com.example.appfinanceiro.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appfinanceiro.data.entity.Transacao;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface TransacaoDao {

    @Insert
    long inserir(Transacao transacao);

    @Insert
    List<Long> inserirTodas(List<Transacao> transacoes);

    @Update
    void atualizar(Transacao transacao);

    @Delete
    void deletar(Transacao transacao);

    @Query("SELECT * FROM transacoes WHERE id = :id")
    Transacao buscarPorId(long id);

    /**
     * Soma dos gastos (SAIDA) do balde no período de competência.
     * Base da query 4.1 do plano.
     */
    @Query("SELECT COALESCE(SUM(valorCentavos), 0) FROM transacoes " +
            "WHERE baldeId = :baldeId " +
            "AND tipo = 'SAIDA' " +
            "AND dataCompetencia BETWEEN :inicio AND :fim")
    long somaGastosDoPeriodoCentavos(long baldeId, LocalDate inicio, LocalDate fim);

    /**
     * Cronograma de compromissos futuros vindos de parcelamentos,
     * agrupados por mês de competência (yyyy-MM). Base da query 4.3.
     * <p>
     * {@code dataCompetencia} é armazenada como epoch day (long via TypeConverter),
     * por isso a formatação usa {@code date(..., 'unixepoch')} com o valor convertido em segundos.
     */
    @Query("SELECT strftime('%Y-%m', date(dataCompetencia * 86400, 'unixepoch')) AS mes, " +
            "       SUM(valorCentavos) AS totalCentavos " +
            "FROM transacoes " +
            "WHERE parcelamentoId IS NOT NULL " +
            "AND dataCompetencia >= :hoje " +
            "GROUP BY mes " +
            "ORDER BY mes")
    List<CompromissoMensal> compromissoFuturoPorMes(LocalDate hoje);

    @Query("SELECT * FROM transacoes " +
            "WHERE dataCompetencia BETWEEN :inicio AND :fim " +
            "ORDER BY dataCompetencia DESC, id DESC")
    List<Transacao> listarPorPeriodo(LocalDate inicio, LocalDate fim);

    @Query("SELECT * FROM transacoes " +
            "WHERE dataCompetencia BETWEEN :inicio AND :fim " +
            "ORDER BY dataCompetencia DESC, id DESC")
    LiveData<List<Transacao>> observarPorPeriodo(LocalDate inicio, LocalDate fim);
}
