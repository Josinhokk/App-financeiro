package com.example.appfinanceiro.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appfinanceiro.data.entity.Balde;

import java.util.List;

@Dao
public interface BaldeDao {

    @Insert
    long inserir(Balde balde);

    @Update
    void atualizar(Balde balde);

    @Delete
    void deletar(Balde balde);

    @Query("SELECT * FROM baldes WHERE arquivado = 0 ORDER BY ordem, nome")
    List<Balde> listarAtivos();

    @Query("SELECT * FROM baldes WHERE arquivado = 0 ORDER BY ordem, nome")
    LiveData<List<Balde>> observarAtivos();

    @Query("SELECT * FROM baldes WHERE id = :id")
    Balde buscarPorId(long id);

    @Query("SELECT * FROM baldes WHERE id = :id")
    LiveData<Balde> observarPorId(long id);

    @Query("SELECT COUNT(*) FROM baldes")
    int contarTodos();

    /**
     * Arquivar em vez de deletar: as transações já lançadas continuam
     * apontando para o balde, então apagar a linha destruiria histórico.
     */
    @Query("UPDATE baldes SET arquivado = 1 WHERE id = :id")
    void arquivar(long id);

    @Query("SELECT COALESCE(MAX(ordem), -1) + 1 FROM baldes")
    int proximaOrdem();

    @Query("UPDATE baldes SET saldoAcumuladoCentavos = :saldoCentavos WHERE id = :id")
    void atualizarSaldoAcumulado(long id, long saldoCentavos);

    @Query("SELECT COALESCE(SUM(valorMensalCentavos), 0) FROM baldes WHERE arquivado = 0")
    long somaDotacoesMensaisCentavos();
}
