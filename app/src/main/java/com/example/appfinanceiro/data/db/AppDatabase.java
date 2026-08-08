package com.example.appfinanceiro.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.appfinanceiro.data.dao.BaldeDao;
import com.example.appfinanceiro.data.dao.TransacaoDao;
import com.example.appfinanceiro.data.entity.Balde;
import com.example.appfinanceiro.data.entity.Transacao;

@Database(
        entities = {Balde.class, Transacao.class},
        version = 1,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public static final String NAME = "appfinanceiro.db";

    public abstract BaldeDao baldeDao();
    public abstract TransacaoDao transacaoDao();
}
