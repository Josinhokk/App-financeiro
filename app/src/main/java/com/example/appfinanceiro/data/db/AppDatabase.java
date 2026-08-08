package com.example.appfinanceiro.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
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

    private static final String DB_NAME = "appfinanceiro.db";
    private static volatile AppDatabase instancia;

    public abstract BaldeDao baldeDao();
    public abstract TransacaoDao transacaoDao();

    public static AppDatabase get(Context ctx) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    ).build();
                }
            }
        }
        return instancia;
    }
}
