package com.example.appfinanceiro.data.db;

import androidx.room.TypeConverter;

import com.example.appfinanceiro.data.entity.TipoBalde;
import com.example.appfinanceiro.data.entity.TipoTransacao;

import java.time.LocalDate;

public final class Converters {

    private Converters() {}

    @TypeConverter
    public static Long fromLocalDate(LocalDate d) {
        return d == null ? null : d.toEpochDay();
    }

    @TypeConverter
    public static LocalDate toLocalDate(Long v) {
        return v == null ? null : LocalDate.ofEpochDay(v);
    }

    @TypeConverter
    public static String fromTipoBalde(TipoBalde t) {
        return t == null ? null : t.name();
    }

    @TypeConverter
    public static TipoBalde toTipoBalde(String v) {
        return v == null ? null : TipoBalde.valueOf(v);
    }

    @TypeConverter
    public static String fromTipoTransacao(TipoTransacao t) {
        return t == null ? null : t.name();
    }

    @TypeConverter
    public static TipoTransacao toTipoTransacao(String v) {
        return v == null ? null : TipoTransacao.valueOf(v);
    }
}
