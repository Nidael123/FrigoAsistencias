package com.example.frigoasistencias2.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Managerbd extends SQLiteOpenHelper {
    String t_registro = "Create table t_registro ( cedula varchar(20),fechaingreso varchar(30),estadosubido varchar(2) default 'P')";
    String t_version = "Create table t_version(id_version integer)";
    String t_descansos = "Create table t_descansos(cedula varchar(15),fechaingreso varchar(30),estadobanio varchar(2))";
    String t_personaserror = "Create table t_personaserror (cedula varchar(30),fecha varchar(30))";

    //estadoeliminar : si se elimina el usuario de la list   'E' = eliminado 'A'= activo 'C' cancelado
    //estadosubido: si la informacion fue subida al sistema principal 'P' = pendiente 'S' = subido 'E' error
    //estado banio: 'N' ninguno 'S' Salio
    public Managerbd(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(t_registro);
        db.execSQL(t_version);
        db.execSQL(t_descansos);
        db.execSQL(t_personaserror);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    }
}
